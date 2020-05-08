 package com.googlecode.qualitas.internal.bpel.instrumentation;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.googlecode.qualitas.engines.api.configuration.ProcessType;
 import com.googlecode.qualitas.engines.api.core.Bundle;
 import com.googlecode.qualitas.engines.api.core.Entry;
 import com.googlecode.qualitas.engines.api.instrumentation.InstrumentationException;
 import com.googlecode.qualitas.engines.api.instrumentation.InstrumentationPhase;
 import com.googlecode.qualitas.engines.api.instrumentation.Instrumentor;
 import com.googlecode.qualitas.utils.dom.DOMUtils;
 import com.googlecode.qualitas.utils.xslt.XSLTUtils;
 
 /**
  * The Class GenericBpelInstrumentor.
  */
 @InstrumentationPhase
 public class GenericBpelInstrumentor implements Instrumentor {
 
     /** The Constant LOG. */
     private static final Log LOG = LogFactory.getLog(GenericBpelInstrumentor.class);
 
     /** The Constant WS_BPEL_TRANSFORM_XSL. */
     private static final String WS_BPEL_TRANSFORM_XSL = 
         "/com/googlecode/qualitas/internal/bpel/instrumentation/bpel.xsl";
 
     /** The Constant QUALITAS_EXECUTION_MONITOR_PACKAGE. */
     private static final String QUALITAS_EXECUTION_MONITOR_PACKAGE = 
         "/com/googlecode/qualitas/internal/monitor/webservice/";
 
     /** The Constant QUALITAS_EXECUTION_MONITOR_WSDL_NAME. */
     private static final String QUALITAS_EXECUTION_MONITOR_WSDL_NAME = 
         "QualitasMonitorService.wsdl";
 
     /** The Constant QUALITAS_EXECUTION_MONITOR_ARTIFACTS. */
     private static final String QUALITAS_EXECUTION_MONITOR_ARTIFACTS_NAME = 
         "QualitasMonitorServiceArtifacts.wsdl";
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.google.code.qualitas.engines.api.component.Component#isSupported(
      * com.google.code.qualitas.engines.api.core.ProcessType)
      */
     public boolean isSupported(ProcessType processType) {
         return processType == ProcessType.WS_BPEL_2_0_APACHE_ODE;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.google.code.qualitas.engines.api.instrumentation.Instrumentor#instrument
      * (com.google.code.qualitas.engines.api.core.Bundle)
      */
     public void instrument(Bundle bundle) throws InstrumentationException {
 
         Entry qualitasConfiguration = getQualitasConfiguration(bundle);
         Entry mainProcessDefinition = getMainProcessDefinition(bundle);
         Document configurationDocument = null;
         Document processDocument = null;
 
         try {
             configurationDocument = DOMUtils.parse(new ByteArrayInputStream(qualitasConfiguration
                     .getContent()));
         } catch (Exception e) {
             String msg = "Could not parse qualitas configuration for "
                     + bundle.getMainProcessQName();
             LOG.error(msg, e);
             throw new InstrumentationException(msg, e);
         }
         try {
             processDocument = DOMUtils.parse(new ByteArrayInputStream(mainProcessDefinition
                     .getContent()));
         } catch (Exception e) {
             String msg = "Could not parse main process definition for "
                     + bundle.getMainProcessQName();
             LOG.error(msg, e);
             throw new InstrumentationException(msg, e);
         }
 
         Node importedConfigurationDocument = processDocument.importNode(
                 configurationDocument.getDocumentElement(), true);
         processDocument.getDocumentElement().appendChild(importedConfigurationDocument);
 
         byte[] content = null;
         try {
             content = enhanceProcessDefinition(processDocument);
         } catch (Exception e) {
             String msg = "Could not instrument process definition " + bundle.getMainProcessQName();
             LOG.debug(msg, e);
             throw new InstrumentationException(msg, e);
         }
 
         mainProcessDefinition.setContent(content);
         try {
             // override main process definition
             bundle.addEntry(mainProcessDefinition);
         } catch (IOException e) {
             String msg = "Could not override main process definition for "
                     + bundle.getMainProcessQName();
             LOG.error(msg, e);
             throw new InstrumentationException(msg, e);
         }
 
         try {
             addQualitasFiles(bundle);
         } catch (Exception e) {
             String msg = "Could not add qualitas monitor WSDL files into "
                     + bundle.getMainProcessQName();
             LOG.debug(msg, e);
             throw new InstrumentationException(msg, e);
         }
     }
 
     /**
      * Gets the main process definition.
      * 
      * @param bundle
      *            the bundle
      * @return the main process definition
      * @throws InstrumentationException
      *             the instrumentation exception
      */
     private Entry getMainProcessDefinition(Bundle bundle) throws InstrumentationException {
         Entry mainProcessDefinition = null;
         try {
             mainProcessDefinition = bundle.getMainProcessDefinition();
         } catch (IOException e) {
             String msg = "Could not get main process definition for "
                     + bundle.getMainProcessQName();
             LOG.debug(msg, e);
             throw new InstrumentationException(msg, e);
         }
         return mainProcessDefinition;
     }
 
     /**
      * Gets the qualitas configuration.
      * 
      * @param bundle
      *            the bundle
      * @return the qualitas configuration
      * @throws InstrumentationException
      *             the instrumentation exception
      */
     private Entry getQualitasConfiguration(Bundle bundle) throws InstrumentationException {
         Entry qualitasConfiguration = null;
         try {
             if (!bundle.isInstrumentable()) {
                 throw new InstrumentationException("Could not find qualitas.xml configuration for "
                         + bundle.getMainProcessQName());
             }
             qualitasConfiguration = bundle.getQualitasConfiguration();
         } catch (IOException e) {
             String msg = "Could not get qualitas.xml configuration for "
                     + bundle.getMainProcessQName();
             LOG.debug(msg, e);
             throw new InstrumentationException(msg, e);
         }
         return qualitasConfiguration;
     }
 
     /**
      * Adds the qualitas files.
      * 
      * @param bundle
      *            the bundle
      * @throws IOException
      *             the IO exception
      */
     private void addQualitasFiles(Bundle bundle) throws IOException {
        InputStream monitorIs = GenericBpelInstrumentor.class.getResourceAsStream(
                 QUALITAS_EXECUTION_MONITOR_PACKAGE + QUALITAS_EXECUTION_MONITOR_WSDL_NAME);
        byte[] qualitasExecutionMonitorWSDL;
        try {
            qualitasExecutionMonitorWSDL = IOUtils.toByteArray(monitorIs);
        } finally {
            monitorIs.close();
        }
 
         Entry qualitasExecutionMonitorWSDLEntry = new Entry(QUALITAS_EXECUTION_MONITOR_WSDL_NAME,
                 qualitasExecutionMonitorWSDL);
         bundle.addEntry(qualitasExecutionMonitorWSDLEntry);
 
        InputStream artifactsIs = GenericBpelInstrumentor.class.getResourceAsStream(
                 QUALITAS_EXECUTION_MONITOR_PACKAGE + QUALITAS_EXECUTION_MONITOR_ARTIFACTS_NAME);
        byte[] qualitasExecutionMonitorArtifacts;
        try {
            qualitasExecutionMonitorArtifacts = IOUtils.toByteArray(artifactsIs);
        } finally {
            artifactsIs.close();
        }
 
         Entry qualitasExecutionMonitorArtifactsEntry = new Entry(
                 QUALITAS_EXECUTION_MONITOR_ARTIFACTS_NAME, qualitasExecutionMonitorArtifacts);
         bundle.addEntry(qualitasExecutionMonitorArtifactsEntry);
     }
 
     /**
      * Enhance process definition.
      * 
      * @param processDefinition
      *            the process definition
      * @return the byte[]
      * @throws IOException
      *             the IO exception
      * @throws TransformerException
      *             the transformer exception
      */
     private byte[] enhanceProcessDefinition(Document processDefinition) throws IOException,
             TransformerException {
        Source stylesheet = new StreamSource(GenericBpelInstrumentor.class.getResourceAsStream(
                 WS_BPEL_TRANSFORM_XSL));
         Source source = new DOMSource(processDefinition);
         StreamResult result = new StreamResult();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         result.setOutputStream(outputStream);
 
         XSLTUtils.transformDocument(stylesheet, source, result, null);
 
         byte[] content = outputStream.toByteArray();
 
         return content;
     }
 
 }
