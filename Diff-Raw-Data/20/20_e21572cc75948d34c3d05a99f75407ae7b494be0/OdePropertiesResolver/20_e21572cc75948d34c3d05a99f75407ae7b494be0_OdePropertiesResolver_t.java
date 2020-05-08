 package com.google.code.qualitas.engines.ode.resolution;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.code.p.qualitas.engines.api.configuration.QualitasConfiguration;
 import com.google.code.qualitas.engines.api.core.Bundle;
import com.google.code.qualitas.engines.api.resolution.Properties;
import com.google.code.qualitas.engines.api.resolution.PropertiesResolver;
import com.google.code.qualitas.engines.api.resolution.ResolutionException;
 import com.google.code.qualitas.engines.ode.component.AbstractOdeComponent;
 import com.google.code.qualitas.engines.ode.core.OdeBundle;
 
 /**
  * The Class OdePropertiesResolver.
  */
 public class OdePropertiesResolver extends AbstractOdeComponent implements PropertiesResolver {
 
     /** The Constant LOG. */
     private static final Log LOG = LogFactory.getLog(OdePropertiesResolver.class);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.google.code.qualitas.engines.api.resolver.PropertiesResolver#resolve
      * (com.google.code.qualitas.engines.api.core.Bundle)
      */
     @Override
     public Properties resolve(Bundle bundle) throws ResolutionException {
 
         checkBundle(bundle);
 
         OdeBundle odeBundle = (OdeBundle) bundle;
 
         Document document = null;
 
         try {
             InputStream is = new ByteArrayInputStream(odeBundle.getOdeDescriptor().getContent());
             document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
         } catch (Exception e) {
             String msg = "Could not read ODE descriptor from bundle " + odeBundle.getDirTempPath();
             LOG.error(msg, e);
             throw new ResolutionException(msg, e);
         }
 
         Properties properties = new Properties();
 
         Element deployElement = document.getDocumentElement();
 
         QName processQName = getProcessQName(deployElement);
         List<QName> servicesQNames = getServicesQName(deployElement);
         QualitasConfiguration qualitasConfiguration = null;
 
         try {
             qualitasConfiguration = getQualitasConfiguration(odeBundle);
         } catch (IOException e) {
             String msg = "Could not read Qualitas Configuration from bundle "
                     + odeBundle.getDirTempPath();
             LOG.error(msg, e);
             throw new ResolutionException(msg, e);
         }
 
         properties.setProcessQName(processQName);
         properties.setServicesQNames(servicesQNames);
         properties.setQualitasConfiguration(qualitasConfiguration);
 
         bundle.setMainProcessName(processQName.toString());
 
         return properties;
     }
 
     /**
      * Gets the process q name.
      * 
      * @param deployElement
      *            the deploy element
      * @return the process q name
      */
     private QName getProcessQName(Element deployElement) {
         Element processElement = (Element) deployElement.getElementsByTagName("process").item(0);
         String processName = processElement.getAttribute("name");
         String prefix = processName.substring(0, processName.indexOf(':'));
         String localPart = processName.substring(processName.indexOf(':') + 1);
 
         String namespaceURI = findNamespaceForPrefix(processElement, prefix);
 
         QName processQName = new QName(namespaceURI, localPart, prefix);
         return processQName;
     }
 
     /**
      * Gets the services q name.
      * 
      * @param deployElement
      *            the deploy element
      * @return the services q name
      */
     private List<QName> getServicesQName(Element deployElement) {
         Element processElement = (Element) deployElement.getElementsByTagName("process").item(0);
 
         NodeList services = processElement.getElementsByTagName("invoke");
 
         List<QName> servicesQNames = new ArrayList<QName>();
         for (int i = 0; i < services.getLength(); i++) {
             Element serviceElement = (Element) ((Element) services.item(i)).getElementsByTagName(
                     "service").item(0);
             String serviceName = serviceElement.getAttribute("name");
             String prefix = serviceName.substring(0, serviceName.indexOf(':'));
             String localPart = serviceName.substring(serviceName.indexOf(':') + 1);
 
             String namespaceURI = findNamespaceForPrefix(processElement, prefix);
 
             servicesQNames.add(new QName(namespaceURI, localPart, prefix));
         }
 
         return servicesQNames;
     }
 
     /**
      * Finds namespace for prefix.
      * 
      * @param node
      *            the node
      * @param prefix
      *            the prefix
      * @return the string
      */
     private String findNamespaceForPrefix(Node node, String prefix) {
         NamedNodeMap namedNodeMap = node.getAttributes();
 
         for (int i = 0; i < namedNodeMap.getLength(); i++) {
             Attr attribute = (Attr) namedNodeMap.item(i);
             String attributeName = attribute.getNodeName();
             if (attributeName.equals("xmlns:" + prefix)) {
                 return attribute.getNodeValue();
             }
         }
 
         return findNamespaceForPrefix(node.getParentNode(), prefix);
     }
 }
