 package de.hub.clickwatch.analysis.results.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.xml.type.AnyType;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 import com.google.inject.Inject;
 
 import de.hub.clickwatch.analysis.results.Result;
 import de.hub.clickwatch.analysis.results.Results;
 import de.hub.clickwatch.analysis.results.ResultsFactory;
 import de.hub.clickwatch.analysis.results.ResultsPlugin;
 import de.hub.clickwatch.analysis.results.util.builder.AxisBuilder;
 import de.hub.clickwatch.analysis.results.util.builder.ChartBuilder;
 import de.hub.clickwatch.analysis.results.util.builder.XYBuilder;
 import de.hub.clickwatch.model.Handler;
 import de.hub.clickwatch.model.Node;
 import de.hub.clickwatch.model.provider.XmlAttributeValue;
 import de.hub.clickwatch.recoder.cwdatabase.Record;
 import de.hub.clickwatch.recorder.database.DataBaseUtil;
 import de.hub.clickwatch.ui.PluginActivator;
 import de.hub.clickwatch.util.Throwables;
 
 public class SimpleAnalysis implements IObjectActionDelegate {
 	
 	private @Inject DataBaseUtil dbUtil;
 
 	private Shell shell;
 	private XmlAttributeValue value;
 
 	public SimpleAnalysis() {
 		super();
 		PluginActivator.getInstance(); // just to activate buddy		
 	}
 
 	@Override
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 	}
 
 	@Override
 	public void run(IAction action) {
 		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
 		try {
 			progressDialog.run(true, false, new IRunnableWithProgress() {
 				@Override
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					try {
 						SimpleAnalysis.this.run(monitor);
 					} catch (final Exception e) {
 						shell.getDisplay().syncExec(new Runnable() {
 							@Override
 							public void run() {
 								MessageDialog.openError(shell, "Error in main", "Exception while executing analysis: " + e.getMessage());		
 							}
 						});
 					}		
 				}
 			});
 		} catch (Exception e) {
 			Throwables.propagate(e);
 		}
 	}	
 
 	private void run(IProgressMonitor monitor) {		
 		EObject object = value.getObject();
 		Node node = null;
 		Handler handler = null;
 		Record record = null;
 		while (object != null) {
 			if (object instanceof Handler) {
 				handler = (Handler)object;
 			} else if (object instanceof Node) {
 				node = (Node)object;
 			} else if (object instanceof Record) {
 				record = (Record)object;
 			} 
 			object = object.eContainer();
 		}
 		if (record == null) {
 			return;
 		}
 		
 		object = value.getObject();
 		List<EStructuralFeature> containmentFeatures = new ArrayList<EStructuralFeature>();
 		while (!(object instanceof Handler)) {
 			containmentFeatures.add(0, object.eContainmentFeature());
 			object = object.eContainer();
 		}
 		
 		Results results = ResultsPlugin.getInstance().getResults();
 		Result result = ResultsFactory.eINSTANCE.createResult();
		result.setName(value.getFeature().getName());
 		result.setTimestamp(new Date());
 		results.getResults().add(result);
 		
 		result.getCharts().add(ChartBuilder.newChartBuilder()
 				.withName(result.getName() + " over time")
 				.withType(XYBuilder.newXYBuilder())
 				.withValueSpecs(AxisBuilder.newAxisBuilder().withColumn(0).withName("time"))
 				.withValueSpecs(AxisBuilder.newAxisBuilder().withColumn(1).withName(value.getEntry().getEStructuralFeature().getName())).build());
 		
 		Iterator<Handler> iterator = dbUtil.getHandlerIterator(
 				DataBaseUtil.createHandle(record, node.getINetAddress(), handler.getQualifiedName()),
 				monitor);
 		while (iterator.hasNext()) {			
 			Handler newHandler = iterator.next();
 			long time = newHandler.getTimestamp() - record.getStart();
 			FeatureMap map = newHandler.getAny();
 			AnyType container = null;
 			loop: for(EStructuralFeature feature: containmentFeatures) {
 				for (FeatureMap.Entry fme: map) {
 					if (fme.getEStructuralFeature().getName().equals(feature.getName())) {
 						if (fme.getValue() instanceof AnyType) {
 							container = (AnyType)fme.getValue();
 							map = ((AnyType)fme.getValue()).getAny();
 							continue loop;
 						}
 					}
 				}
 				return;
 			}
 			loop: for (FeatureMap.Entry fme: container.getAnyAttribute()) {
 				if (fme.getEStructuralFeature().getName().equals(value.getEntry().getEStructuralFeature().getName())) {
 					String stringValue = (String)fme.getValue();
 					
 					result.getDataSet().add(time, Double.parseDouble(stringValue));
 					break loop;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {	
 		if (selection instanceof IStructuredSelection) {
 			value = (XmlAttributeValue)((IStructuredSelection) selection).getFirstElement();
 		}
 	}
 
 }
