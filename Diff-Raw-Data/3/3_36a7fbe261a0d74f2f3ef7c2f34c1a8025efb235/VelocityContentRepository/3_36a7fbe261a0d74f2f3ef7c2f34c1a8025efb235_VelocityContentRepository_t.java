 package com.gentics.cr.rest.velocity;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.tools.generic.EscapeTool;
 
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
 	private ITemplate errorTemplate;
 
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
 		logger.error("Error getting result.",ex);
 		ensureTemplateManager();
 		try {
 			loadTemplate(true);
 			templateManager.put("exception",ex);
 			templateManager.put("debug",isDebug);
 			String encoding = this.getResponseEncoding();
 			templateManager.put("encoding",encoding);
			//TODO use errorTemplateName (absolute) instead of errorTemplate.getKey()
 			String output = templateManager.render(errorTemplate.getKey(), errorTemplate.getSource());
 			stream.write(output.getBytes(encoding));
 		} catch (Exception e){
 			logger.error("Cannot succesfully respond with error template.",e);
 		}
 		
 	
 	}
 
 	@Override
 	public void toStream(OutputStream stream) throws CRException {
 		try {
 			ensureTemplateManager();
 			loadTemplate();
 			templateManager.put("resolvables", this.resolvableColl);
 			String encoding = this.getResponseEncoding();
 			templateManager.put("encoding",encoding);
			//TODO use templateName (absolute) instead of errorTemplate.getKey()
 			String output = templateManager.render(template.getKey(), template.getSource());
 			stream.write(output.getBytes(encoding));
 		} catch (CRException e) {
 			respondWithError(stream, e, false);
 		} catch (IOException e) {
 			logger.error("Cannot write to Output stream.",e);
 		}
 	}
 	
 	private void ensureTemplateManager(){
 		if(templateManager==null){
 			templateManager=config.getTemplateManager();
 			templateManager.put("esc", new EscapeTool());
 		}
 	}
 	
 	private void loadTemplate() throws CRException{
 		loadTemplate(false);
 	}
 	
 	private void loadTemplate(boolean loadErrorTemplate) throws CRException{
 		if((template==null && !loadErrorTemplate) || (errorTemplate==null && loadErrorTemplate) || templateReloading){
 			String templatePath = (String) config.get(TEMPLATEPATH_KEY);
 			try{
 				if(loadErrorTemplate) {
 					//make velocity.vm to velocity.error.vm
 					File errorTemplateFile = new File(templatePath);
 					String directoryName = errorTemplateFile.getParent();
 					String fileName = errorTemplateFile.getName();
 					String fileExtension = fileName.replaceAll(".*\\.", "");
 					fileName = fileName.replaceAll("(.*)\\..*", "$1");
 					templatePath = fileName + ".error."+fileExtension;
 					if(directoryName!=null) templatePath = directoryName + File.separator + templatePath;
 					errorTemplate = getFileTemplate(templatePath);
 				} else {
 					template = getFileTemplate(templatePath);
 				}
 				
 			}
 			catch(Exception e)
 			{
 				log.error("FAILED TO LOAD VELOCITY TEMPLATE FROM "+template,e);
 			}
 		}
 		if(template==null && !loadErrorTemplate){
 			throw new CRException(new CRError("ERROR","The template "+template+" cannot be found."));
 		}
 		if(errorTemplate==null && loadErrorTemplate){
 			throw new CRException(new CRError("ERROR","The template "+template+" cannot be found."));
 		}
 	}
 	
 	private FileTemplate getFileTemplate(String templatePath) throws FileNotFoundException, CRException{
 		File file = new File(templatePath);
 		if(!file.isAbsolute()){
 			file= new File(CRConfigUtil.DEFAULT_TEMPLATE_PATH+File.separator+templatePath);
 		}
 		return new FileTemplate(new FileInputStream(file));
 	}
 	
 }
