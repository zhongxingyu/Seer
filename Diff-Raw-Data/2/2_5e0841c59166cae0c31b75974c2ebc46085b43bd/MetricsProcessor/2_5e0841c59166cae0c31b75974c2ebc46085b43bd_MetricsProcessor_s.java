 package mx.itesm.mexadl.metrics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mx.itesm.mexadl.MexAdlProcessor;
 import mx.itesm.mexadl.util.Util;
 
 import org.apache.velocity.Template;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.xpath.XPath;
 
 /**
  * The MetricsProcessor class generates an aspect that introduces the
  * implementation of the mx.itesm.mexadl.metrics.MaintainabilityMetrics
  * interface, to specially marked components in an xADL architecture definition.
  * 
  * @author jccastrejon
  * 
  */
 public class MetricsProcessor implements MexAdlProcessor {
 
     /**
      * XPath expression to identify the MexADL quality metrics.
      */
     private static XPath metricsPath;
 
     /**
      * AspectJ template that defines the quality metrics to be measured in the
      * components of a system's architecture.
      */
     private static Template aspectTemplate;
 
     /**
      * Interface that will hold the quality metrics to be measured in a
      * component of a system's architecture.
      */
     private static Template interfaceTemplate;
 
     static {
         try {
             MetricsProcessor.aspectTemplate = Util.getVelocityTemplate(MetricsProcessor.class, "aspect");
             MetricsProcessor.interfaceTemplate = Util.getVelocityTemplate(MetricsProcessor.class, "interface");
             MetricsProcessor.metricsPath = XPath.newInstance("//mexadl:maintainabilityMetrics");
         } catch (Exception e) {
             System.out.println("Error loading MetricsProcessor");
             e.printStackTrace();
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void processDocument(Document document, String xArchFilePath) throws Exception {
         MetricsData metricsData;
         Map<String, Object> metric;
         Map<String, Object> metricSet;
         Map<String, Object> definition;
         Map<String, Object> properties;
         List<Element> metricDefinitions;
         List<Map<String, Object>> metrics;
         List<Map<String, Object>> metricSets;
         List<Map<String, Object>> definitionsList;
 
         metricDefinitions = MetricsProcessor.metricsPath.selectNodes(document);
         if ((metricDefinitions != null) && (!metricDefinitions.isEmpty())) {
             definitionsList = new ArrayList<Map<String, Object>>();
             for (Element metricDefinition : metricDefinitions) {
                 definition = new HashMap<String, Object>();
                 metricSets = new ArrayList<Map<String, Object>>();
                 metricsData = this.getMetricsData(document, metricDefinition);
 
                 // Process the file only if a valid xADL type is associated to
                 // the metrics definition
                 if (metricsData.getType() != null) {
                     definitionsList.add(definition);
                     definition.put("metricSets", metricSets);
                     definition.put("type", metricsData.getType());
                     definition.put("typeName", Util.getValidName(metricsData.getType()));
                     definition.put("metricsClass", MaintainabilityMetrics.class.getName());
 
                     for (MetricsData metricSetDefinition : metricsData.getMetrics()) {
                         metricSet = new HashMap<String, Object>();
                         metricSets.add(metricSet);
 
                         metrics = new ArrayList<Map<String, Object>>();
                         metricSet.put("name", metricSetDefinition.getType());
                         metricSet.put("type", metricSetDefinition.getType().substring(0, 1).toUpperCase()
                                 + metricSetDefinition.getType().substring(1));
                         metricSet.put("metrics", metrics);
                         for (String metricName : metricSetDefinition.getData().keySet()) {
                             metric = new HashMap<String, Object>();
                             metrics.add(metric);
 
                             metric.put("name", metricName);
                             metric.put("value", metricSetDefinition.getData().get(metricName));
                         }
                     }
 
                     // Create the metrics interface
                     Util.createJavaFile(document, xArchFilePath, MetricsProcessor.interfaceTemplate, definition,
                            "MetricsInterface", definition.get("typeName").toString());
                 }
             }
 
             // Create the metrics aspect only if valid associations were found
             if (!definitionsList.isEmpty()) {
                 properties = new HashMap<String, Object>();
                 properties.put("definitionsList", definitionsList);
                 Util.createJavaFile(document, xArchFilePath, MetricsProcessor.aspectTemplate, properties,
                         "MetricsAspect", Util.getDocumentName(document));
             }
         }
     }
 
     /**
      * Get the quality metrics associated the the specified element.
      * 
      * @param element
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     private MetricsData getMetricsData(final Document document, final Element element) throws JDOMException {
         MetricsData metricSet;
         MetricsData returnValue;
         List<MetricsData> metricSets;
 
         returnValue = null;
         if (element != null) {
             returnValue = new MetricsData();
             metricSets = new ArrayList<MetricsData>();
             returnValue.setType(Util.getLinkImplementationClass(document, Util.getIdValue(element.getParentElement())));
             returnValue.setMetrics(metricSets);
 
             for (Element child : (List<Element>) element.getChildren()) {
                 if (child.getNamespace() == Util.MEXADL_NAMESPACE) {
                     metricSet = new MetricsData();
                     metricSets.add(metricSet);
 
                     metricSet.setType(child.getName());
                     metricSet.setData(new HashMap<String, String>());
                     for (Attribute attribute : (List<Attribute>) child.getAttributes()) {
                         if (attribute.getNamespace() == Util.MEXADL_NAMESPACE) {
                             metricSet.getData().put(attribute.getName(), attribute.getValue());
                         }
                     }
                 }
             }
         }
 
         return returnValue;
     }
 }
