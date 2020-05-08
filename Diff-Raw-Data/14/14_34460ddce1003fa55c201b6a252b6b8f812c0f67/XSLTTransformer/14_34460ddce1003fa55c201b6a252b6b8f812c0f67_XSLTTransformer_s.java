 package org.siddhiesb.engine.util;
 
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
 
     @Override
     public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
         xsltKey = ((ConstantExpressionExecutor) attributeExpressionExecutors.get(0)).execute(null).toString();
     }
 
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
 
         Pipe pipe = (Pipe) passThruContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
         if (pipe != null) {
             inputStream = pipe.getInputStream();
         }
 
         /*ToDo : Do we have to synchronize here? */
         if (isCreationOrRecreationRequired(passThruContext, xsltKey)) {
             cTemplate = createTemplate(passThruContext, xsltKey);
         } else {
             cTemplate = cachedTemplatesMap.get(xsltKey);
         }
         System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
 
         OutputStream msgContextOutStream = pipe.resetOutputStream();
         ByteArrayOutputStream transformedBaos = new ByteArrayOutputStream();
         transform(inputStream, transformedBaos, cTemplate);
 
         ByteArrayOutputStream transformedOutNew = new ByteArrayOutputStream();
         IOUtils.write(transformedBaos.toByteArray(), transformedOutNew);
         BufferedInputStream bufferedStream = new BufferedInputStream(new ByteArrayInputStream(transformedOutNew.toByteArray()));
         passThruContext.setProperty(PassThroughConstants.BUFFERED_INPUT_STREAM, bufferedStream);
         IOUtils.write(transformedBaos.toByteArray(),msgContextOutStream);
 
         pipe.setRawSerializationComplete(true);
 
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
