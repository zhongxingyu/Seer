 package org.siddhiesb.engine.util;
 
 import org.siddhiesb.common.api.CommonAPIConstants;
 import org.siddhiesb.common.api.PassThruContext;
 import org.wso2.siddhi.core.config.SiddhiContext;
 import org.wso2.siddhi.core.event.AtomicEvent;
 import org.wso2.siddhi.core.event.in.InEvent;
 import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
 import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
 import org.wso2.siddhi.core.executor.function.FunctionExecutor;
 import org.wso2.siddhi.query.api.definition.Attribute;
 
 import org.apache.commons.io.IOUtils;
 import org.siddhiesb.common.api.PassThruContext;
 import org.siddhiesb.transport.passthru.PassThroughConstants;
 import org.siddhiesb.transport.passthru.Pipe;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;
 
 
 @SiddhiExtension(namespace = "siddhiESB", function = "xsltTransform")
 public class XSLTTransformer extends FunctionExecutor {
     private final Object transformerLock = new Object();
     private Map<String, Templates> cachedTemplatesMap = new Hashtable<String, Templates>();
     private TransformerFactory transFact = TransformerFactory.newInstance();
     private String xsltKey;
 
     private ExpressionExecutor expressionExecutor;
 
    private static final String SOAP_11_ENV_ST = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
    private static final String SOAP_ENV_END_11 = "</soapenv:Body></soapenv:Envelope>";


     @Override
     public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
         xsltKey = ((ConstantExpressionExecutor) attributeExpressionExecutors.get(0)).execute(null).toString();
     }
 
 
 public void	destroy() {}
 
 
 
     @Override
     protected Object process(Object eventObj) {
         String resultVal = "false";
         if (eventObj instanceof InEvent) {
             if (((InEvent) eventObj).getData0() instanceof PassThruContext) {
                 PassThruContext passThruContext = (PassThruContext) ((InEvent) eventObj).getData0();
                 try {
                     boolean res = transformMessage(passThruContext, xsltKey);
                     if (res) {
                         resultVal = "true";
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         return resultVal;
     }
 
     public Attribute.Type getReturnType() {
         return Attribute.Type.STRING;
     }
 
 
     @Override
     public Object execute(AtomicEvent event) {
 
         return process(event);
     }
 
 
     private boolean transformMessage(PassThruContext passThruContext, String xsltKey) throws Exception{
 
         InputStream inputStream = null;
         Templates cTemplate = null;
 
         Pipe pipe1 = (Pipe) passThruContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
         if (pipe1 != null) {
             inputStream = pipe1.getInputStream();
         }
 
         if (isCreationOrRecreationRequired(passThruContext, xsltKey)) {
             synchronized (transformerLock) {
                 cTemplate = createTemplate(passThruContext, xsltKey);
             }
         } else {
             synchronized (transformerLock) {
                 cTemplate = cachedTemplatesMap.get(xsltKey);
             }
         }
 
 
         System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
         Pipe pipe = (Pipe) passThruContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
         OutputStream pipeOutputStream = pipe.resetOutputStream();
         ByteArrayOutputStream transformedBaos = new ByteArrayOutputStream();
         transform(inputStream, transformedBaos, cTemplate);
 
         ByteArrayOutputStream _transformedOutMessageNew = new ByteArrayOutputStream();
        IOUtils.write(SOAP_11_ENV_ST.getBytes(), _transformedOutMessageNew);
         IOUtils.write(transformedBaos.toByteArray(), _transformedOutMessageNew);
        IOUtils.write(SOAP_ENV_END_11.getBytes(), _transformedOutMessageNew);
 
 
         IOUtils.write(_transformedOutMessageNew.toByteArray(), pipeOutputStream);
         pipe.setRawSerializationComplete(true);
 
 
         /* Workaround to handle content length for responses. */
         if (CommonAPIConstants.MESSAGE_DIRECTION_RESPONSE.equals(passThruContext.getProperty(CommonAPIConstants.MESSAGE_DIRECTION))) {
             Map headersMap = (Map) passThruContext.getProperty(PassThroughConstants.HTTP_HEADERS);
             headersMap.put(PassThroughConstants.CONTENT_LENGTH, transformedBaos.size());
         }
 
         return true;
     }
 
     private boolean isCreationOrRecreationRequired(PassThruContext passThruContext, String xsltKey) {
 
 
         // if there are no cachedTemplates inside cachedTemplatesMap or
         // if the template related to this generated key is not cached
         // then it need to be cached
         if (cachedTemplatesMap.isEmpty() || !cachedTemplatesMap.containsKey(xsltKey)) {
             return true;
         }
         return false;
     }
 
 
     private Templates createTemplate(PassThruContext passThruContext, String xsltKey) {
         // Assign created template
         Templates cachedTemplates = null;
 
         try {
             String workingDir = System.getProperty("user.dir");
             StreamSource xsltSource = new StreamSource(new File(workingDir + xsltKey));
             cachedTemplates = transFact.newTemplates(xsltSource);
             if (cachedTemplates == null) {
                 // if cached template creation failed
             } else {
                 // if cached template is created then put it in to cachedTemplatesMap
                 cachedTemplatesMap.put(xsltKey, cachedTemplates);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return cachedTemplates;
     }
 
     private void transform(InputStream xmlIn, OutputStream out, Templates templates) throws Exception {
         Transformer trans = templates.newTransformer();
         Source source = new StreamSource(xmlIn);
         Result resultXML = new StreamResult(out);
         trans.transform(source, resultXML);
     }
 
 }
