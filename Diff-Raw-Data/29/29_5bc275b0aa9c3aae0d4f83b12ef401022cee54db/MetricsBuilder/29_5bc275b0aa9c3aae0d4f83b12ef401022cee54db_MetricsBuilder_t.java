 package descent.ui.metrics;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 import descent.core.ICompilationUnit;
 import descent.core.JavaModelException;
 import descent.core.dom.AST;
 import descent.core.dom.ASTParser;
 import descent.core.dom.CompilationUnit;
 import descent.ui.metrics.builder.CompilationUnitMetricProcessor;
 import descent.ui.metrics.builder.ExportingMetricProcessor;
 import descent.ui.metrics.builder.MarkingMetricProcessor;
 import descent.ui.metrics.calculators.Calculator;
 import descent.ui.metrics.calculators.LinesOfCodeCalculator;
 import descent.ui.metrics.calculators.NumberOfFieldsCalculator;
 import descent.ui.metrics.calculators.NumberOfLevelsCalculator;
 import descent.ui.metrics.calculators.NumberOfParametersCalculator;
 import descent.ui.metrics.calculators.NumberOfStatementsCalculator;
 import descent.ui.metrics.export.Exporter;
 import descent.ui.metrics.location.MetricLocation;
 
 public final class MetricsBuilder extends IncrementalProjectBuilder {
     public static final String BUILDER_ID = "descent.ui.metrics.MetricsBuilder";
     public static final String MARKER_ID = "descent.ui.metrics.MetricsMarker";
 
     private Calculator[] allCalculators;
 
     public MetricsBuilder() {
         initialiseCalculators();
     }
 
     private void initialiseCalculators() {
         allCalculators = new Calculator[]{new NumberOfLevelsCalculator(), new NumberOfParametersCalculator(), new NumberOfStatementsCalculator(), new NumberOfFieldsCalculator(), new LinesOfCodeCalculator()};
     }
 
     private void setMetricProcessor(CompilationUnitMetricProcessor processor) {
         for (int i = 0; i < allCalculators.length; i++) {
             allCalculators[i].setListener(processor);
         }
     }
 
     public void export(IProject project, Exporter[] exporters, IProgressMonitor monitor) throws IOException, CoreException {
         monitor.beginTask("Exporting Metrics", 2);
         doBuild(CompilationUnitList.createFullList(project), new ExportingMetricProcessor(exporters), monitor);
     }
 
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
         try {
             return buildProtected(kind, monitor);
         } catch (IOException ioex) {
             throw new CoreException(new Status(IStatus.ERROR, MetricsPlugin.PLUGIN_ID, 0, "IOException during build: " + ioex.getMessage(), ioex));
         }
 
     }
 
     private IProject[] buildProtected(int kind, IProgressMonitor monitor) throws CoreException, IOException {
         switch (kind) {
             case IncrementalProjectBuilder.AUTO_BUILD : {
                 doAutoBuild(monitor);
                 return null;
             }
             case IncrementalProjectBuilder.FULL_BUILD : {
                 doFullBuild(monitor);
                 return null;
             }
             case IncrementalProjectBuilder.INCREMENTAL_BUILD : {
                 doMarkingBuild(CompilationUnitList.createResourceDeltaList(getProject(), getDelta(getProject())), monitor);
                 return null;
             }
             default : {
                 throw new IllegalArgumentException("Unknown build kind:" + kind);
             }
         }
     }
 
     private void doAutoBuild(IProgressMonitor monitor) throws CoreException, IOException {
         IResourceDelta resourceDelta = getDelta(getProject());
         if (resourceDelta != null) {
             doMarkingBuild(CompilationUnitList.createResourceDeltaList(getProject(), getDelta(getProject())), monitor);
         } else {
             doFullBuild(monitor);
         }
     }
 
     private void doFullBuild(IProgressMonitor monitor) throws CoreException, IOException {
         getProject().deleteMarkers(MetricsBuilder.MARKER_ID, true, IResource.DEPTH_INFINITE);
         doMarkingBuild(CompilationUnitList.createFullList(getProject()), monitor);
     }
 
     private void doMarkingBuild(CompilationUnitList compilationUnits, IProgressMonitor monitor) throws CoreException, IOException {
         doBuild(compilationUnits, new MarkingMetricProcessor(), monitor);
     }
 
     private void doBuild(CompilationUnitList compilationUnits, CompilationUnitMetricProcessor metricProcessor, IProgressMonitor monitor) throws CoreException, IOException {
         monitor.beginTask("Measuring Metrics", compilationUnits.size());
 
         initialiseCalculatorsForBuild(metricProcessor);
         measureCompilationUnits(compilationUnits, metricProcessor, monitor);
         complete(monitor, metricProcessor);
     }
 
     private void complete(IProgressMonitor monitor, CompilationUnitMetricProcessor metricProcessor) throws IOException {
         metricProcessor.complete(monitor);
         setMetricProcessor(null);
     }
 
     private void measureCompilationUnits(CompilationUnitList compilationUnits, CompilationUnitMetricProcessor metricProcessor, IProgressMonitor monitor) throws CoreException {
         Calculator[] calculators = getEnabledCalculators();
         for (int i = 0; i < compilationUnits.size() && !monitor.isCanceled() ; i++) {
             measureCompilationUnit(compilationUnits.get(i), calculators, metricProcessor, monitor);
         }
     }
 
     private void initialiseCalculatorsForBuild(CompilationUnitMetricProcessor metricProcessor) {
         setMetricProcessor(metricProcessor);
     }
 
     private Calculator[] getEnabledCalculators() {
         Set calculatorsToUse = new HashSet();
         for (int i = 0; i < allCalculators.length; i++) {
             if (isEnabled(allCalculators[i])) {
                 calculatorsToUse.add(allCalculators[i]);
             }
         }
 
         return (Calculator[]) calculatorsToUse.toArray(new Calculator[calculatorsToUse.size()]);
     }
 
     private boolean isEnabled(Calculator calculator) {
         String[] metricIds = calculator.getMetricIds();
         for (int i = 0; i < metricIds.length; i++) {
             if (MetricsPlugin.getDefault().isMetricEnabled(metricIds[i])) {
                 return true;
             }
         }
 
         return false;
     }
 
     private void measureCompilationUnit(ICompilationUnit compilationUnit, Calculator[] enabledCalculators, CompilationUnitMetricProcessor metricProcessor, IProgressMonitor monitor) throws CoreException {
         monitor.subTask("Measuring: " + compilationUnit.getElementName());
 
         metricProcessor.setUpForCompilationUnit(compilationUnit);
         
        ASTParser parser = ASTParser.newParser(AST.D2);
         parser.setSource(compilationUnit);
         parser.setResolveBindings(true);
         CompilationUnit unit = (CompilationUnit) parser.createAST(null);
         
         measureCompilationUnit(compilationUnit, unit, enabledCalculators);
         MetricLocation.clearLocations();
         monitor.worked(1);
     }
 
     private void measureCompilationUnit(ICompilationUnit unparsedCompilationUnit, CompilationUnit parsedCompilationUnit, Calculator[] enabledCalculators) {
         for (int i = 0; i < enabledCalculators.length; i++) {
             measureCompilationUnitWithCalculator(unparsedCompilationUnit, parsedCompilationUnit, enabledCalculators[i]);
         }
     }
 
     private void measureCompilationUnitWithCalculator(ICompilationUnit unparsedCompilationUnit, CompilationUnit parsedCompilationUnit, Calculator calculator) {
         try {
             calculator.measure(unparsedCompilationUnit, parsedCompilationUnit);
         } catch (RuntimeException rex) {
             MetricsPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, MetricsPlugin.PLUGIN_ID, IStatus.OK, "Error while calculating metrics for " + unparsedCompilationUnit.getResource().getProjectRelativePath().toOSString(), rex));
             throw rex;
         } catch (JavaModelException jmex) {
             MetricsPlugin.getDefault().getLog().log(jmex.getStatus());
         }
     }
 }
