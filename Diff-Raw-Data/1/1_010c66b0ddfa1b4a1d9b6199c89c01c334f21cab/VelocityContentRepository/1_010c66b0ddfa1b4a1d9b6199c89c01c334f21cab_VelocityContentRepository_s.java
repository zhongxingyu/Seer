 package com.gentics.cr.rest.velocity;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRError;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.rest.ContentRepository;
 import com.gentics.cr.template.FileTemplate;
 import com.gentics.cr.template.ITemplate;
 import com.gentics.cr.template.ITemplateManager;
 
 public class VelocityContentRepository extends ContentRepository {
 
 	private CRConfigUtil config;
 	private ITemplateManager templateManager;
 	private ITemplate template;
 	private boolean templateReloading = false;
 	
 	private static final String TEMPLATEPATH_KEY = "cr.velocity.defaulttemplate";
 	private static final String TEMPLATERELOADING_KEY = "cr.velocity.templatereloading";
 	
 	public VelocityContentRepository(String[] attr, String encoding,
 			String[] options, CRConfigUtil configUtil) {
 		super(attr, encoding, options);
 		config = configUtil;
 		templateReloading = Boolean.parseBoolean((String) config.get(TEMPLATERELOADING_KEY));
 	}
 
 	private Logger logger = Logger.getLogger(VelocityContentRepository.class);
 	@Override
 	public void respondWithError(OutputStream stream, CRException ex,
 			boolean isDebug) {
 	}
 
 	@Override
 	public void toStream(OutputStream stream) throws CRException {
 		try {
 			if(templateManager==null)templateManager=config.getTemplateManager();
 			//TODO add parameter for template reloading in config so you can develop velocity template more easy
 			if(template==null || templateReloading){
 				String templatePath = (String) config.get(TEMPLATEPATH_KEY);
 				try{
 					File file = new File(templatePath);
 					if(!file.isAbsolute()){
 						file= new File(CRConfigUtil.DEFAULT_TEMPLATE_PATH+File.separator+templatePath);
 					}
 					template = new FileTemplate(new FileInputStream(file));
 				}
 				catch(Exception e)
 				{
 					log.error("FAILED TO LOAD VELOCITY TEMPLATE FROM "+template,e);
 				}
 			}
 			if(template==null){
 				CRError error = new CRError("ERROR","The template "+template+" cannot be found.");
 				respondWithError(stream, new CRException(error), false);
 			}
 			templateManager.put("resolvables", this.resolvableColl);
 			String output = templateManager.render(template.getKey(), template.getSource());
 			stream.write(output.getBytes(this.getResponseEncoding()));
 		} catch (IOException e) {
 			logger.error("Cannot write to Output stream.",e);
 		}
 
 	}
 
 }
