 package com.github.arnostv.xquery.coverage.saxon;
 
 import net.sf.saxon.lib.ModuleURIResolver;
 
 import java.io.PrintStream;
 import java.util.Set;
 
 public class CoverageSuiteReportWriter {
 
     private final CoverageCollector collector;
     private final ReportPlaintextRenderer renderer = new ReportPlaintextRenderer();
     private final ModuleURIResolver moduleURIResolver;
     private PrintStream out;
 
     public CoverageSuiteReportWriter(PrintStream out, CoverageCollector collector, ModuleURIResolver moduleURIResolver) {
         this.out = out;
         this.collector = collector;
         this.moduleURIResolver = moduleURIResolver;
     }
 
     public void writeReport() {
         final Set<String> involvedModules = collector.involvedModules();
 
         for (String module : involvedModules) {
            if (module!=null) {  // FIXME this not-null check should not be here, it's just to avoid some wrong events with null module name
                 writeModuleReport(module);
            }
         }
     }
 
     private void writeModuleReport(String module) {
 
         final CoverageReport report = collector.generateCoverageReport(module, moduleURIResolver);
 
         final String codeCoverage = renderer.renderToPlaintext(report);
 
         String result =
                 "*** " + module + " ***\n\n" +
                         codeCoverage;
 
         out.println(result);
     }
 }
