 package com.adaptiweb.utils.ci;
 
 import java.io.File;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
 import org.springframework.util.ResourceUtils;
 
 import com.adaptiweb.utils.commons.VariableResolver;
 
 public class LiveFileHandler implements LiveFile {
 
	private static final String CLASSPATH_URL = "classpath:";
 	private VariableResolver variables;
 	private File file;
 	private String fileName;
 	private long lastChecked;
 	private long changesCheckPeriod = 2 * 60 * 1000; //default 2 minutes
 	private long lastChanged;
 	private String templateResource;
 
 	public void setPropertyFile(String fileName) {
 		this.fileName = fileName;
 		this.file = null;
 	}
 	
 	/**
 	 * Enables substituting variables in values. Optional.
 	 */
 	public void setVariables(VariableResolver variables) {
 		this.variables = variables;
 	}
 	
 	/**
 	 * Minimal change checking period in seconds.
 	 * Optional, default is 2 minutes.
 	 */
 	public void setChangesCheckPeriod(int changesCheckPeriod) {
 		this.changesCheckPeriod = changesCheckPeriod * 1000L;
 	}
 	
 	/**
 	 * Enable auto creating missing file during initialization. Optional.
 	 */
 	public void setTemplateResource(String templateResource) {
 		this.templateResource = templateResource;
 	}
 	
 	public void checkChanges(FileLoader loader) {
 		long currentTime = System.currentTimeMillis();
 		if (currentTime - lastChecked < changesCheckPeriod) return;
 		lastChecked = currentTime;
 
 		long changedTime = getFile().lastModified();
 		if (changedTime == lastChanged) return;
 		lastChanged = changedTime;
 		
 		try {
 			loader.loadFile(getFile());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String substituteVariables(String string) {
 		return variables == null ? string : variables.replaceVariables(string);
 	}
 	
 	@PostConstruct
 	public File getFile() {
 		if (file == null) {
 			if (fileName == null)
 				throw new IllegalStateException("Attribute 'propertyFile' wasn't initialized!");
 			
 			file = new File(substituteVariables(fileName));
 			
 			if (!file.exists() && templateResource != null)
 				createDefaultFile(substituteVariables(templateResource), file);
 		}
 		return file;
 	}
 	
 	public static void createDefaultFile(String templateResource, File file) {
		InputStream is = null;
		OutputStream os = null;
 		try {
 			FileUtils.forceMkdir(file.getParentFile());
			
			if (templateResource.startsWith(CLASSPATH_URL)) {
				templateResource = templateResource.substring(CLASSPATH_URL.length());
				is = new ClassPathResource(templateResource).getInputStream();
				os = new FileOutputStream(file);
				IOUtils.copy(is, os);
			} else {
				FileUtils.copyFile(ResourceUtils.getFile(templateResource), file, false);
			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
 		}
 	}
 }
