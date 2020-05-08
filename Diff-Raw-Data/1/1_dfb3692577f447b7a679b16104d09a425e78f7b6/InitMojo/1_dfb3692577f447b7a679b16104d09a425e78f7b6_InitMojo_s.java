 package com.ii2d.genthemall.maven.plugin;
 
 import groovy.util.ConfigObject;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import com.ii2d.genthemall.template.Template;
 import com.ii2d.genthemall.template.TemplateHolder;
 import com.ii2d.genthemall.util.GeneratorUtils;
 
 /**
  * @goal init
  */
 public class InitMojo extends AbstractGenerateMojo {
 	
 	/**
 	 * @parameter default-value="common,springmvc"
 	 */
 	private String initType;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void doExecute() throws MojoExecutionException, MojoFailureException {
 		try {
 			List<Template> tmplList = new ArrayList<Template>();
 			for(String dir: initType.split(",")) {
 				TemplateHolder initTmpl = getTemplateHolder("classpath:gtinit/" + dir);
 				tmplList.addAll(initTmpl.getTemplates());
 			}
 			ConfigObject config = new ConfigObject();
 			for(Entry<Object, Object> e:this.project.getProperties().entrySet()) {
 				Object key = e.getKey();
 				if(key instanceof String) {
 					key = ((String) key).replaceAll("\\.", "_");
 				}
 				config.put(key, e.getValue());
 			}
 			DatabaseSource ds = this.getDatabaseSource();
 			config.put("dataSource", ds);
 			for(Template t: tmplList) {
 				String basePath = getTargetBasePath(t.getType());
 				String destPath = FilenameUtils.concat(basePath, t.getPath());
 				getLog().info("Init for: " + t.getName() + " to dest path: " + destPath);
 				GeneratorUtils.generate(t, config, destPath);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MojoExecutionException(e.getMessage());
 		}
 		
 	}
 
 }
