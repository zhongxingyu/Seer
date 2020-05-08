 package com.schmeedy.pdt.joomla.ui.server.view;
 
 import java.util.Set;
 
 import org.eclipse.core.databinding.observable.IObservable;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IMapChangeListener;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.map.MapChangeEvent;
 import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
 import org.eclipse.core.databinding.observable.set.IObservableSet;
 import org.eclipse.emf.databinding.EMFProperties;
 import org.eclipse.emf.databinding.FeaturePath;
 import org.eclipse.emf.databinding.IEMFListProperty;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.part.ViewPart;
 
 import com.schmeedy.pdt.joomla.common.ui.JoomlaCommonUiPlugin;
 import com.schmeedy.pdt.joomla.core.server.IJoomlaDeployer;
 import com.schmeedy.pdt.joomla.core.server.cfg.DeploymentDescriptor;
 import com.schmeedy.pdt.joomla.core.server.cfg.DeploymentRuntime;
 import com.schmeedy.pdt.joomla.core.server.cfg.JoomlaExtensionDeployment;
 import com.schmeedy.pdt.joomla.core.server.cfg.JoomlaServerConfigurationPackage;
 import com.schmeedy.pdt.service.registry.ServiceRegistry;
 
 /*
  * tree data binding code is based on http://tomsondev.bestsolution.at/2009/06/08/galileo-emf-databinding--part-3/
  */
 public class JoomlaServersView extends ViewPart {
 	
 	private final IJoomlaDeployer deployer = ServiceRegistry.getInstance().getService(IJoomlaDeployer.class);
 	private final DeploymentDescriptor deploymentDescriptor = deployer.getDeploymentDescriptor();
 	private TreeViewer deploymentTreeViewer;
 	
 	public JoomlaServersView() {}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		final GridLayout gl_parent = new GridLayout(1, false);
 		gl_parent.marginWidth = 0;
 		gl_parent.marginHeight = 0;
 		parent.setLayout(gl_parent);
 		
 		deploymentTreeViewer = new TreeViewer(parent, SWT.BORDER);
 		final Tree tree = deploymentTreeViewer.getTree();
 		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		{ // configure tree viewer
 			final ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new DeploymentTreeObservableFactory(), new DeploymentTreeStructureAdvisor());
 			deploymentTreeViewer.setContentProvider(contentProvider);
 			
 			final IObservableSet knownElements = contentProvider.getKnownElements();
 			final IObservableMap[] lpMaps = new IObservableMap[2];
 			
 			lpMaps[0] = EMFProperties.value(FeaturePath.fromList(
 					JoomlaServerConfigurationPackage.Literals.DEPLOYMENT_RUNTIME__SERVER,
 					JoomlaServerConfigurationPackage.Literals.LOCAL_JOOMLA_SERVER__NAME
 				)).observeDetail(knownElements);
 	
 			lpMaps[1] = EMFProperties.value(FeaturePath.fromList(
 					JoomlaServerConfigurationPackage.Literals.DEPLOYMENT_RUNTIME__SERVER,
 					JoomlaServerConfigurationPackage.Literals.LOCAL_JOOMLA_SERVER__EXACT_VERSION
 			)).observeDetail(knownElements);
 			
 			deploymentTreeViewer.setLabelProvider(new DeploymentTreeLabelProvider(lpMaps));
 			
 			final IObservableList inputObservable = EMFProperties.list(JoomlaServerConfigurationPackage.Literals.DEPLOYMENT_DESCRIPTOR__RUNTIMES).observe(deploymentDescriptor);
 			deploymentTreeViewer.setInput(inputObservable);
 		}
 		
 		getSite().setSelectionProvider(deploymentTreeViewer);
 	}
 
 	@Override
 	public void setFocus() {
 		deploymentTreeViewer.getTree().setFocus();
 	}
 	
 	private class DeploymentTreeLabelProvider extends StyledCellLabelProvider {
 		private final Image joomlaServerImage = JoomlaCommonUiPlugin.getInstance().getImage(JoomlaCommonUiPlugin.IMG_OBJ_JOOMLA);
 		
 		private final IMapChangeListener mapChangeListener = new IMapChangeListener() {
 			@Override
 			public void handleMapChange(MapChangeEvent event) {
 				final Set<?> affectedElements = event.diff.getChangedKeys();
 				if (!affectedElements.isEmpty()) {
 					final LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(DeploymentTreeLabelProvider.this,
 							affectedElements.toArray());
 					fireLabelProviderChanged(newEvent);
 				}
 			}
 		};
 
 		public DeploymentTreeLabelProvider(IObservableMap... attributeMaps) {
 			for (int i = 0; i < attributeMaps.length; i++) {
 				attributeMaps[i].addMapChangeListener(mapChangeListener);
 			}
 		}
 
 		@Override
 		public void update(ViewerCell cell) {
 			if (cell.getElement() instanceof DeploymentRuntime) {
 				final DeploymentRuntime runtime = (DeploymentRuntime) cell.getElement();
 				final StyledString label = new StyledString(runtime.getServer().getName());
 				final String versionDecoration = " [" + runtime.getServer().getExactVersion() + "]";
 				label.append(versionDecoration, StyledString.QUALIFIER_STYLER);
 				cell.setImage(joomlaServerImage);
 				cell.setText(label.getString());
 				cell.setStyleRanges(label.getStyleRanges());
 			} else if (cell.getElement() instanceof JoomlaExtensionDeployment) {
 				cell.setText("TODO: implement label providers for deployed extensions");
 			}
 		}
 	}
 	
 	private static class DeploymentTreeObservableFactory implements IObservableFactory {
 		private final IEMFListProperty deployedExtensionsProperty = EMFProperties.list(JoomlaServerConfigurationPackage.Literals.DEPLOYMENT_RUNTIME__DEPLOYED_EXTENSIONS);
 		
 		@Override
 		public IObservable createObservable(Object target) {
 			if (target instanceof IObservableList) {
 				return (IObservable) target;
 			} else if (target instanceof DeploymentRuntime) {
				return deployedExtensionsProperty.observe(deployedExtensionsProperty);
 			}
 			return null;
 		}
 	}
 	
 	private static class DeploymentTreeStructureAdvisor extends TreeStructureAdvisor {
 		@Override
 		public Object getParent(Object element) {
 			if (element instanceof JoomlaExtensionDeployment) {
 				return ((JoomlaExtensionDeployment) element).getRuntime();
 			}
 			return null;
 		}
 		
 		@Override
 		public Boolean hasChildren(Object element) {
 			if (element instanceof DeploymentRuntime) {
 				return !((DeploymentRuntime) element).getDeployedExtensions().isEmpty();
 			}
 			return null;
 		}
 	}
 	
 }
