 package com.illmeyer.polygraph.syslib.directives;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.illmeyer.polygraph.core.CoreConstants;
 import com.illmeyer.polygraph.core.data.MessagePart;
 
 import freemarker.core.Environment;
 import freemarker.template.TemplateDirectiveBody;
 import freemarker.template.TemplateDirectiveModel;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateModel;
 import freemarker.template.TemplateScalarModel;
 
 public class TextPartDirective implements TemplateDirectiveModel {
 	private static final Log log = LogFactory.getLog(TextPartDirective.class);
 
 	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
 			TemplateDirectiveBody body) throws TemplateException, IOException {
 
 		if (env.getCustomAttribute(CoreConstants.ECA_CURRENT_PART)!=null)
 			throw new TemplateException("cannot nest message parts",env);
 
 		@SuppressWarnings("unchecked")
 		Map<String,MessagePart> partMap = (Map<String, MessagePart>) env.getCustomAttribute(CoreConstants.ECA_PARTS); 
 		
 		if (partMap==null) {
 			partMap = new HashMap<String, MessagePart>();
 			env.setCustomAttribute(CoreConstants.ECA_PARTS, partMap);
 		}
 		
 		String charsetName="UTF-8";
 		Object parEncoding =params.get("encoding"); 
 		if (parEncoding!=null && parEncoding instanceof TemplateScalarModel) {
 			charsetName=((TemplateScalarModel)parEncoding).getAsString();
 		}
 		try {
 			Charset.forName(charsetName);
 		} catch (Exception e) {
 			throw new TemplateException("specified charset '"+charsetName+"' is not available on this JVM",env);
 		}
 		
 		String partName=null;
 		Object parName =params.get("name"); 
 		if (parName!=null && parName instanceof TemplateScalarModel) {
 			partName=((TemplateScalarModel)parName).getAsString();
 		}
 		if (partName==null || partName.trim().isEmpty()) throw new TemplateException("textpart needs a name",env);
 		if (partMap.containsKey(partName)) log.warn("part '"+partName+"' is already defined");
 			
 		MessagePart currentPart = new MessagePart();
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		OutputStreamWriter bosw = new OutputStreamWriter(bos, charsetName);
 		env.setCustomAttribute(CoreConstants.ECA_CURRENT_PART, currentPart);
 		body.render(bosw);
 		env.removeCustomAttribute(CoreConstants.ECA_CURRENT_PART);
 		bosw.close();
 		currentPart.setMessage(bos.toByteArray());
 		currentPart.setEncoding(charsetName);
 		partMap.put(partName, currentPart);
 	}
 
 }
