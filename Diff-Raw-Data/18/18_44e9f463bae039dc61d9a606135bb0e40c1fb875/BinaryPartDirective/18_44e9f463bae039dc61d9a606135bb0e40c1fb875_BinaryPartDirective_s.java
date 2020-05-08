 package com.illmeyer.polygraph.syslib.directives;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.output.NullWriter;
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
 
 public class BinaryPartDirective implements TemplateDirectiveModel {
 	private static final Log log = LogFactory.getLog(BinaryPartDirective.class);
 
 	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
 			TemplateDirectiveBody body) throws TemplateException, IOException {
 
 		if (env.getCustomAttribute(CoreConstants.ECA_CURRENT_PART)!=null)
 			throw new TemplateException("cannot nest message parts",env);
 		
 		@SuppressWarnings("unchecked")
 		Map<String,MessagePart> partMap = (Map<String, MessagePart>) env.getCustomAttribute(CoreConstants.ECA_PARTS); 
 		
 		if (partMap==null) {
 			partMap = new HashMap<String, MessagePart>();
 			env.setCustomAttribute(CoreConstants.ECA_PARTS, partMap);
 		}
 		
 		String partName=null;
 		Object parName =params.get("name"); 
 		if (parName!=null && parName instanceof TemplateScalarModel) {
 			partName=((TemplateScalarModel)parName).getAsString();
 		}
		if (partName==null || partName.trim().isEmpty()) throw new TemplateException("textpart needs a name",env);
 		if (partMap.containsKey(partName)) log.warn("part '"+partName+"' is already defined");
 			
 		MessagePart currentPart = new MessagePart();
 		env.setCustomAttribute(CoreConstants.ECA_CURRENT_PART, currentPart);
 		body.render(NullWriter.NULL_WRITER);
 		env.removeCustomAttribute(CoreConstants.ECA_CURRENT_PART);
 		partMap.put(partName, currentPart);
 	}
 }
