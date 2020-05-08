 package com.gentics.cr.template;
 
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
 import org.apache.velocity.runtime.resource.util.StringResourceRepository;
 
 import com.gentics.cr.CRException;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class VelocityTemplateManager implements ITemplateManager {
 
 	private String encoding;
 	private HashMap<String, Object> objectstoput;
 	
 	//Templatecache
 	private HashMap<String, Template> templates;
 	
 	/**
 	 * Create Instance
 	 * @param encoding
 	 */
 	public VelocityTemplateManager(String encoding)
 	{
 		this.encoding = encoding;
 		this.objectstoput = new HashMap<String,Object>();
		
 	}
 	
 	
 	/**
 	 * @see com.gentics.cr.template.ITemplateManager#put(java.lang.Object)
 	 */
 	public void put(String key, Object value) {
 		this.objectstoput.put(key, value);
 	}
 
 	/**
 	 * @see com.gentics.cr.template.ITemplateManager#render(java.lang.String, java.lang.Object, java.lang.Object)
 	 */
 	public String render(String templateName, String templateSource) throws CRException {
 		String renderedTemplate=null;
 		StringResourceRepository rep = StringResourceLoader.getRepository();
 		rep.setEncoding(this.encoding);
 		try {
 			
 			Template template=this.templates.get(templateName);
 			if(template==null)
 			{
 				rep.putStringResource(templateName, templateSource);
 				template = Velocity.getTemplate(templateName);
 				rep.removeStringResource(templateName);
 				this.templates.put(templateName, template);
 			}	
 			
 			VelocityContext context = new VelocityContext();
 			Iterator<String> it = this.objectstoput.keySet().iterator();
 			while(it.hasNext())
 			{
 				String key = it.next();
 				context.put(key, this.objectstoput.get(key));
 			}
 			StringWriter ret = new StringWriter();
 			template.merge(context,ret);
 			renderedTemplate = ret.toString();
 		} catch (ResourceNotFoundException e) {
 			throw new CRException(e);
 		} catch (ParseErrorException e) {
 			throw new CRException(e);
 		} catch (Exception e) {
 			throw new CRException(e);
 		}finally{
 			this.objectstoput = new HashMap<String,Object>();
 		}
 		return renderedTemplate;
 	}
 
 }
