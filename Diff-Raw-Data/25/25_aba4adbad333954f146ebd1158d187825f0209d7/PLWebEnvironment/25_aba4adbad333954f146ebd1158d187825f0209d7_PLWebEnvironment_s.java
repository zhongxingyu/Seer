 package org.plweb.suite.webstart;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PLWebEnvironment {
 
 	private static PLWebEnvironment instance = null;
 
 	public static PLWebEnvironment getInstance() {
 		if (instance == null) {
 			return instance = new PLWebEnvironment();
 		} else {
 			return instance;
 		}
 	}
 
 	public PLWebEnvironment() {
		setDiskRoot(new File(System.getProperty("plweb.diskroot")).getPath());
		setUrlPackage(System.getProperty("plweb.urlpackage"));
		setUrlPackageAsc(System.getProperty("plweb.urlpackage_asc"));
		setUrlLesson(System.getProperty("plweb.urllesson"));
		setLessonId(System.getProperty("plweb.lessonid"));
		setLessonPath(System.getProperty("plweb.lessonpath"));
		setLessonFile(System.getProperty("plweb.lessonfile"));
		setJEditPath(System.getProperty("plweb.jeditpath"));
		setPluginPath(System.getProperty("plweb.pluginpath"));
		setAdImage(System.getProperty("plweb.adimage"));
		setAdUrl(System.getProperty("plweb.adurl"));
 
 		List<String> plugins = new ArrayList<String>();
 		List<String> pluginsAsc = new ArrayList<String>();
 		for (Object okey : System.getProperties().keySet()) {
 			if (okey instanceof String) {
 				String key = (String) okey;
				if (key.startsWith("plweb.plugins.")) {
 					plugins.add(System.getProperty(key));
 					pluginsAsc.add(System.getProperty(key.replace(".plugins.",
 							".plugins_asc.")));
 				}
 			}
 		}
 		this.plugins = new String[plugins.size()];
 		for (int i = 0; i < plugins.size(); i++) {
 			this.plugins[i] = plugins.get(i);
 		}
 		this.pluginsAsc = new String[pluginsAsc.size()];
 		for (int i = 0; i < pluginsAsc.size(); i++) {
 			this.pluginsAsc[i] = pluginsAsc.get(i);
 		}
 	}
 
 	private String diskRoot;
 	private String urlPackage;
 	private String urlPackageAsc;
 	private String urlLesson;
 	private String lessonId;
 	private String lessonPath;
 	private String lessonFile;
 	private String jEditPath;
 	private String pluginPath;
 	private String[] plugins;
 	private String[] pluginsAsc;
 	private String adImage;
 	private String adUrl;
 
 	public String getAdImage() {
 		return adImage;
 	}
 
 	public void setAdImage(String adImage) {
 		this.adImage = adImage;
 	}
 
 	public String getAdUrl() {
 		return adUrl;
 	}
 
 	public void setAdUrl(String adUrl) {
 		this.adUrl = adUrl;
 	}
 
 	public String getUrlPackageAsc() {
 		return urlPackageAsc;
 	}
 
 	public void setUrlPackageAsc(String urlPackageAsc) {
 		this.urlPackageAsc = urlPackageAsc;
 	}
 
 	public String[] getPluginsAsc() {
 		return pluginsAsc;
 	}
 
 	public void setPluginsAsc(String[] pluginsAsc) {
 		this.pluginsAsc = pluginsAsc;
 	}
 
 	public String getPluginPath() {
 		return pluginPath;
 	}
 
 	public void setPluginPath(String pluginPath) {
 		this.pluginPath = pluginPath;
 	}
 
 	public String[] getPlugins() {
 		return plugins;
 	}
 
 	public void setPlugins(String[] plugins) {
 		this.plugins = plugins;
 	}
 
 	public String getJEditPath() {
 		return jEditPath;
 	}
 
 	public void setJEditPath(String editPath) {
 		jEditPath = editPath;
 	}
 
 	public String getLessonFile() {
 		return lessonFile;
 	}
 
 	public void setLessonFile(String lessonFile) {
 		this.lessonFile = lessonFile;
 	}
 
 	public String getLessonPath() {
 		return lessonPath;
 	}
 
 	public void setLessonPath(String lessonPath) {
 		this.lessonPath = lessonPath;
 	}
 
 	public String getLessonId() {
 		return lessonId;
 	}
 
 	public void setLessonId(String lessonId) {
 		this.lessonId = lessonId;
 	}
 
 	public String getUrlLesson() {
 		return urlLesson;
 	}
 
 	public void setUrlLesson(String urlLesson) {
 		this.urlLesson = urlLesson;
 	}
 
 	public String getUrlPackage() {
 		return urlPackage;
 	}
 
 	public void setUrlPackage(String urlPackage) {
 		this.urlPackage = urlPackage;
 	}
 
 	public String getDiskRoot() {
 		return diskRoot;
 	}
 
 	public void setDiskRoot(String diskRoot) {
 		this.diskRoot = diskRoot;
 	}
 }
