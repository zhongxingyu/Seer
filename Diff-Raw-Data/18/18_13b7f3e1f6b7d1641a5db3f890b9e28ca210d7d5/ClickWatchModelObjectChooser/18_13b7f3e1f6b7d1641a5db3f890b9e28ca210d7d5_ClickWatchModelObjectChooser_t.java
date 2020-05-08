 package de.hub.clickwatch.transformationLauncher.dialog;
 
import org.eclipse.debug.internal.ui.SWTFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import de.hub.clickwatch.model.provider.ClickWatchModelItemProviderAdapterFactory;
 
 /**
  * The Dialog to choose the object inside a clickwatch model
  * 
  * @author Lars George
  * 
  */
 public class ClickWatchModelObjectChooser extends Dialog {
 
 	private TreeViewer treeViewer;
 	private ComposedAdapterFactory adapterFactory;
 	private Resource source;
 	private String elementURI = "";
 
 	public ClickWatchModelObjectChooser(Shell parentShell, Resource source) {
 		super(parentShell);
 		this.source = source;
 		initialize();
		setShellStyle(SWT.RESIZE | SWT.TITLE);
 	}
 
 	private void initialize() {
 		adapterFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		adapterFactory
 				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		adapterFactory
 				.addAdapterFactory(new ClickWatchModelItemProviderAdapterFactory());
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 
 		Composite composite = (Composite) super.createDialogArea(parent);
		
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		
 		// composite.setLayout(new GridLayout(1, false));
 
 		treeViewer = new TreeViewer(composite);
 		treeViewer.setContentProvider(new AdapterFactoryContentProvider(
 				adapterFactory));
 		treeViewer.setLabelProvider(new AdapterFactoryLabelProvider(
 				adapterFactory));
 
 		new AdapterFactoryTreeEditor(treeViewer.getTree(), adapterFactory);
 		treeViewer.setInput(source);
 		treeViewer.expandToLevel(2);
 		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (!event.getSelection().isEmpty()) {
 					Object firstElement = ((IStructuredSelection)event.getSelection()).getFirstElement();
 					if (firstElement instanceof EObject) {
 						URI eProxyURI = EcoreUtil
 								.getURI((EObject) firstElement);
 						setElementURI(eProxyURI.fragment());
 					}
 
 				}
 			}
 		});
 
 		return composite;
 	}
 
 	private void setElementURI(String uri) {
 		this.elementURI = uri;
 	}
 
 	public String getSelecteID() {
 		return this.elementURI;
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(500, 500);
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 
 		newShell.setText("Choose the model object");
 	}
 
 }
