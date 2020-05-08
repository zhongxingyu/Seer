 package mx.itesm.mexadl.metrics.tools;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.xpath.XPath;
 
 /**
  * Retrieve quality metrics from <a
  * href="http://javancss.codehaus.org/">JavaNCSS</a> results.
  * 
  * @author jccastrejon
  * 
  */
 public class JavaNcssMetrics extends XmlFileMetrics {
 
     @Override
     @SuppressWarnings("unchecked")
     protected Map<String, Map<String, Integer>> doGetMetrics(List<Element> elements, final File resultsDir)
             throws Exception {
         Document report;
         XPath functionPath;
         String functionName;
         int maxCyclomaticComplexity;
         List<Element> functionElements;
         int currentCyclomaticComplexity;
         Map<String, Integer> classMetrics;
         Map<String, Map<String, Integer>> returnValue;
 
         returnValue = new HashMap<String, Map<String, Integer>>(elements.size());
         for (Element clazz : elements) {
             classMetrics = new HashMap<String, Integer>();
             classMetrics.put("ccn", 0); // Default CCN
 
             returnValue.put(clazz.getChildText("name"), classMetrics);
             for (Element child : (List<Element>) clazz.getChildren()) {
                 if (!child.getName().equals("name")) {
                     classMetrics.put(child.getName(), Integer.parseInt(child.getText()));
                 }
             }
         }
 
         // Get the maximum value of method CCN associated to a class
         functionPath = XPath.newInstance("//function");
         report = this.getMetricsReport(this.getReportPath(resultsDir));
         functionElements = functionPath.selectNodes(report);
         if ((functionElements != null) && (!functionElements.isEmpty())) {
             for (Element function : functionElements) {
                 functionName = function.getChildText("name");
                 classMetrics = returnValue.get(functionName.substring(0, functionName.lastIndexOf('.')));
 
                maxCyclomaticComplexity = classMetrics.get("ccn");
                currentCyclomaticComplexity = Integer.parseInt(function.getChildText("ccn"));
                if (currentCyclomaticComplexity > maxCyclomaticComplexity) {
                    classMetrics.put("ccn", currentCyclomaticComplexity);
                 }
             }
         }
 
         return returnValue;
     }
 
     @Override
     protected File getReportPath(final File resultsDir) {
         return new File(resultsDir, "/javancss.xml");
     }
 
     @Override
     protected String getXPathExpression() {
         return "//object";
     }
 }
