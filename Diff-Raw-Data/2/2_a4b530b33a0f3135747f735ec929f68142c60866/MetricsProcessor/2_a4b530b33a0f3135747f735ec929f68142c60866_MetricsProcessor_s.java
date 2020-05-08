 /*
  * Copyright 2010 jccastrejon
  *  
  * This file is part of MexADL.
  *
  * MexADL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MexADL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with MexADL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package mx.itesm.mexadl.metrics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
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
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(MetricsProcessor.class.getName());
 
     /**
      * XPath expression to identify the MexADL quality metrics.
      */
     private static XPath metricsPath;
 
     /**
      * AspectJ template that defines the quality metrics to be measured in the
      * components of a system's architecture.
      */
     private static Template aspectTemplate;
 
     static {
         try {
             MetricsProcessor.aspectTemplate = Util.getVelocityTemplate(MetricsProcessor.class, "aspect");
             MetricsProcessor.metricsPath = XPath.newInstance("//mexadl:maintainabilityMetrics");
         } catch (Exception e) {
             MetricsProcessor.logger.log(Level.WARNING, "Error loading MetricsProcessor:", e);
         }
     }
 
     @Override
    @SuppressWarnings("unchecked")
     public void processDocument(final Document document, final String xArchFilePath) throws Exception {
         String referenceId;
         XPath metricsRefPath;
         MetricsData metricsData;
         Map<String, Object> metric;
         Map<String, Object> metricSet;
         Map<String, Object> definition;
         Map<String, Object> properties;
         List<Element> metricDefinitions;
         List<Map<String, Object>> metrics;
         List<Element> metricRefDefinitions;
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
                     // Add the metrics data to the type with the
                     // MaintainabilityMetrics reference
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
 
                     // Add the same metrics data for types associated by means
                     // of a MaintainabilityMetricsRef XLink reference
                     metricsRefPath = XPath.newInstance("//mexadl:maintainabilityMetricsRef[@xlink:href=\"#"
                             + metricDefinition.getAttributeValue("id", Util.MEXADL_NAMESPACE) + "\"]");
                     metricRefDefinitions = metricsRefPath.selectNodes(document);
                     if ((metricRefDefinitions != null) && (!metricRefDefinitions.isEmpty())) {
                         for (Element metricRefDefinition : metricRefDefinitions) {
                             definition = (HashMap) ((HashMap) definition).clone();
                             referenceId = Util.getLinkImplementationClass(document, Util.getIdValue(metricRefDefinition
                                     .getParentElement()));
 
                             if (referenceId != null) {
                                 definition.put("type", referenceId);
                                 definition.put("typeName", Util.getValidName(referenceId));
                                 definitionsList.add(definition);
                             }
                         }
                     }
                 }
             }
 
             // Create the metrics aspect only if valid associations were found
             if (!definitionsList.isEmpty()) {
                 properties = new HashMap<String, Object>();
                 properties.put("definitionsList", definitionsList);
                 properties.put("annotations", Util.getAnnotations(document));
                 Util.createFile(document, xArchFilePath, MetricsProcessor.aspectTemplate, properties, "MetricsAspect",
                         Util.getDocumentName(document), Util.ASPECTJ_EXTENSION);
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
