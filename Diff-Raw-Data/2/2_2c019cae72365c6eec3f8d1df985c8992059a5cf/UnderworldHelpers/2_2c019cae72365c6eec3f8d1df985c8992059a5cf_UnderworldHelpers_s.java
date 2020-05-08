 package org.vpac.grisu.plugins.underworld;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 public class UnderworldHelpers
 {
 
     public UnderworldHelpers()
     {
     }
 
     private static final XPath getXPath()
     {
         XPathFactory xpathfactory = XPathFactory.newInstance();
         XPath xpath = xpathfactory.newXPath();
         xpath.setNamespaceContext(new UnderworldNamespaceContext());
         return xpath;
     }
 
     public static String getMaxTimesteps(Document inputXmlFile)
     {
         String expression = "/uw:StGermainData/uw:param[@name='maxTimeSteps']";
         NodeList resultNodes = null;
         try
         {
             resultNodes = (NodeList)xpath.evaluate(expression, inputXmlFile, XPathConstants.NODESET);
         }
         catch(XPathExpressionException e)
         {
             myLogger.warn("No output directory information in underworld xml file.");
             e.printStackTrace();
             return null;
         }
         String output_path = resultNodes.item(0).getTextContent();
         return output_path;
     }
 
     public static String getOutputDirectory(Document inputXmlFile)
     {
         String expression = "/uw:StGermainData/uw:param[@name='outputPath']";
         NodeList resultNodes = null;
         try
         {
             resultNodes = (NodeList)xpath.evaluate(expression, inputXmlFile, XPathConstants.NODESET);
         }
         catch(XPathExpressionException e)
         {
             myLogger.warn("No output directory information in underworld xml file.");
             e.printStackTrace();
             return null;
         }
         String output_path = resultNodes.item(0).getTextContent();
        
         if ( output_path.endsWith("/") ) {
         	return output_path.substring(0, output_path.length()-1);
         } else {
         	return output_path;
         }
     }
 
     static final Logger myLogger = Logger.getLogger(UnderworldHelpers.class.getName());
     private static final XPath xpath = getXPath();
 
 }
 
