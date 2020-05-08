 package org.oobium.build.workspace;
 
 import static org.oobium.utils.FileUtils.copyJarEntry;
 import static org.oobium.utils.FileUtils.createJar;
 import static org.oobium.utils.FileUtils.extract;
 import static org.oobium.utils.FileUtils.writeFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.oobium.build.BuildBundle;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.FileUtils;
 
 public class ClientExporter {
 
 	private final Logger logger;
 	private final Workspace workspace;
 	private final Module module;
 	private final File exportDir;
 	private final File tmpDir;
 
 	private Project target;
 	private Mode mode;
 	private boolean full;
 	private boolean clean;
 	private boolean includeSource;
 	
 	public ClientExporter(Workspace workspace, Module module) {
 		this.logger = LogProvider.getLogger(BuildBundle.class);
 		this.workspace = workspace;
 		this.module = module;
 		this.exportDir = workspace.getExportDir();
 		this.tmpDir = new File(exportDir, "tmp");
 		
 		this.mode = Mode.PROD;
 	}
 
 	public void setFull(boolean fullExport) {
 		this.full = fullExport;
 	}
 	
 	public void setTarget(Project project) {
 		this.target = project;
 	}
 	
 	private String relativePath(File binFile, int binPathLength) {
 		String relativePath = binFile.getAbsolutePath().substring(binPathLength);
 		if('\\' == File.separatorChar) {
 			relativePath = relativePath.replace('\\', '/');
 		}
 		return relativePath;
 	}
 
 	private String relativeSrcPath(File srcFile, File binFile, int binPathLength) {
 		String relativePath = binFile.getParent().substring(binPathLength) + File.separator + srcFile.getName();
 		if('\\' == File.separatorChar) {
 			relativePath = relativePath.replace('\\', '/');
 		}
 		return relativePath;
 	}
 	
 	public void includeSource(boolean include) {
 		this.includeSource = include;
 	}
 	
 	/**
 	 * Export the application, configured for the given mode.
 	 * @return a File object for the exported jar.
 	 * @throws IOException
 	 */
 	public File[] export() throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 		
 		if(tmpDir.exists()) {
 			FileUtils.deleteContents(tmpDir);
 		} else {
 			tmpDir.mkdirs();
 		}
 
 		try {
 			File targetDir;
 			if(target == null) {
 				targetDir = exportDir;
 			} else {
 				targetDir = new File(target.file, "oobium");
 			}
 
 			if(target instanceof AndroidApp) {
 				return exportAndroid(targetDir);
 			} else {
 				return exportJava(targetDir);
 			}
 		} finally {
 			FileUtils.delete(tmpDir);
 		}
 	}
 
 	private File[] exportAndroid(File targetDir) throws IOException {
 		Map<String, File> files = new HashMap<String, File>();
 
 		// export core oobium jar
 		File oobiumJar = new File(targetDir, "org.oobium.android.jar");
 		addFiles("org.jboss.netty", "org.jboss.netty.handler.codec.http", files);
 		addFiles("org.oobium.client", files);
 		addFiles("org.oobium.app.common", "org.oobium.app.http", files);
 		addFile("org.oobium.logging", "org.oobium.logging.Logger", files);
 		addFile("org.oobium.logging", "org.oobium.logging.LogProvider", files);
 		addAndroidLogger(files);
 		addFiles("org.oobium.persist", files);
 		addFiles("org.oobium.persist.http", files);
 		addFiles("org.oobium.utils", files);
 
 		oobiumJar = createJar(oobiumJar, files);
 		
 		files = new HashMap<String, File>();
 
 		// export application jar
 		Set<Bundle> bundles = new TreeSet<Bundle>();
 		Map<Bundle, List<Bundle>> deps = module.getDependencies(workspace, mode);
 		bundles.addAll(deps.keySet());
 		bundles.add(module);
 		
 		int len = module.bin.getAbsolutePath().length() + 1;
 		if(full) {
 			addFiles(module.name, files);
 		} else {
 			for(File model : module.findModels()) {
 				File genModel = module.getGenModel(model);
 				File[] modelClasses = module.getBinFiles(model);
 				File[] genModelClasses = module.getBinFiles(genModel);
 				for(File modelClass : modelClasses) {
 					files.put(relativePath(modelClass, len), modelClass);
 				}
 				for(File genModelClass : genModelClasses) {
 					files.put(relativePath(genModelClass, len), genModelClass);
 				}
 				if(includeSource) {
 					files.put(relativeSrcPath(model, modelClasses[0], len), model);
 					files.put(relativeSrcPath(genModel, genModelClasses[0], len), genModel);
 				}
 			}
 		}
 		
 		String name = module.name + ".android.jar";
 		logger.info("creating client jar: " + name);
 		File applicationJar = createJar(targetDir, name, files);
 		if(target != null) {
 			target.addBuildPath("oobium/" + name, "lib");
 		}
 
 		if(oobiumJar == null) {
 			return new File[] { applicationJar };
 		} else {
 			if(target != null) {
 				target.addBuildPath("oobium/" + oobiumJar.getName(), "lib");
 			}
 			return new File[] { oobiumJar, applicationJar };
 		}
 	}
 	
 	private File[] exportJava(File targetDir) throws IOException {
 		// export netty jar (should just have a jar built already...)
 		Map<String, File> nettyFiles = new HashMap<String, File>();
 		addFiles("org.jboss.netty", nettyFiles);
 		
 		File nettyJar = createJar(targetDir, "org.jboss.netty.jar", nettyFiles);
 
 		
 		// export core oobium jar
 		Map<String, File> oobiumFiles = new HashMap<String, File>();
 		addFiles("org.oobium.client", oobiumFiles);
 		addFiles("org.oobium.app.common", "org.oobium.app.http", oobiumFiles);
 		addFiles("org.oobium.logging", oobiumFiles);
 		addFiles("org.oobium.persist", oobiumFiles);
 		addFiles("org.oobium.persist.http", oobiumFiles);
 		addFiles("org.oobium.utils", oobiumFiles);
 
 		File oobiumJar = createJar(targetDir, "org.oobium.jar", oobiumFiles);
 		
 
 		// export application jar
 		Set<Bundle> bundles = new TreeSet<Bundle>();
 		Map<Bundle, List<Bundle>> deps = module.getDependencies(workspace, mode);
 		bundles.addAll(deps.keySet());
 		bundles.add(module);
 		
 		Map<String, File> applicationFiles = new HashMap<String, File>();
 		int len = module.bin.getAbsolutePath().length() + 1;
 		if(full) {
 			addFiles(module.name, applicationFiles);
 		} else {
 			for(File model : module.findModels()) {
 				File genModel = module.getGenModel(model);
 				File[] genModelClasses = module.getBinFiles(genModel);
 				for(File genModelClass : genModelClasses) {
 					applicationFiles.put(relativePath(genModelClass, len), genModelClass);
 				}
 				if(includeSource) {
 					applicationFiles.put(relativeSrcPath(genModel, genModelClasses[0], len), genModel);
 				}
 				if(target != null) {
 					createClientModel(model);
 				}
 			}
 		}
 
 		File applicationJar = createJar(targetDir, module.name + (full ? ".jar" : ".models.jar"), applicationFiles);
 
 		
 		if(target != null) {
 			target.addBuildPath("oobium/" + nettyJar.getName(), "lib");
 			target.addBuildPath("oobium/" + applicationJar.getName(), "lib");
 			target.addBuildPath("oobium/" + oobiumJar.getName(), "lib");
 		}
 		
 		return new File[] { nettyJar, oobiumJar, applicationJar };
 	}
 	
 	private void createClientModel(File model) {
 		if(target != null) {
 			String pkg = module.packageName(model);
 			String name = module.getModelName(model);
 			String sname = name + "Model";
 
 			String src = 
 				"package " + pkg + ";\n" +
 				"\n" +
 				"public class " + name + " extends " + sname + " {\n" +
 				"\n" +
 				"}";
 
 			String path = model.getAbsolutePath().substring(module.src.getAbsolutePath().length());
 			writeFile(target.src, path, src);
 		}
 	}
 	
 	private void addAndroidLogger(Map<String, File> files) {
 		File file = writeFile(tmpDir, "LoggerImpl.class", getClass().getResourceAsStream("LoggerImpl.class.android"));
 		files.put("org/oobium/logging/LoggerImpl.class", file);
 		if(includeSource) {
 			file = writeFile(tmpDir, "LoggerImpl.java", getClass().getResourceAsStream("LoggerImpl.java.android"));
 			files.put("org/oobium/logging/LoggerImpl.java", file);
 		}
 	}
 	
 	private void addFiles(String bundleName, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			files.putAll(extract(bundle.file, new File(tmpDir, bundle.name)));
 		} else {
 			files.putAll(bundle.getBuildFiles(includeSource));
 		}
 	}
 
 	private void addFile(String bundleName, String className, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			String entryName = className.replace('.', '/') + ".class";
 			String dstName = bundle.file.getName();
 			dstName = dstName.substring(0, dstName.lastIndexOf('.'));
 			File dir = new File(tmpDir, dstName);
 			if(!dir.isDirectory()) {
 				dir.mkdirs();
 			}
 			File dst = copyJarEntry(bundle.file, entryName, dir);
 			files.put(entryName, dst);
 		} else {
 			int len = bundle.bin.getAbsolutePath().length() + 1;
 			File srcFile = new File(bundle.src, className.replace('.', File.separatorChar) + ".java");
 			File[] binFiles = bundle.getBinFiles(srcFile);
 			for(File binFile : binFiles) {
 				files.put(relativePath(binFile, len), binFile);
 			}
 			if(includeSource) {
 				files.put(relativeSrcPath(srcFile, binFiles[0], len), srcFile);
 			}
 		}
 	}
 	
 	private void addFiles(String bundleName, String packageName, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			String regex = packageName.replace('.', '/') + "/.+";
 			files.putAll(extract(bundle.file, new File(tmpDir, bundle.name), regex));
 		} else {
 			File pkg = new File(bundle.src, packageName.replace('.', File.separatorChar));
 			if(pkg.isDirectory()) {
 				int len = bundle.bin.getAbsolutePath().length() + 1;
 				for(File srcFile : pkg.listFiles()) {
 					File[] binFiles = bundle.getBinFiles(srcFile);
 					for(File binFile : binFiles) {
 						files.put(relativePath(binFile, len), binFile);
 					}
 					if(includeSource) {
 						files.put(relativeSrcPath(srcFile, binFiles[0], len), srcFile);
 					}
 				}
 			} else {
 				pkg = new File(bundle.file, packageName.replace('.', File.separatorChar));
 				if(pkg.isDirectory()) {
 					int len = bundle.file.getAbsolutePath().length() + 1;
 					for(File binFile : pkg.listFiles()) {
 						if(binFile.isFile()) {
 							files.put(relativePath(binFile, len), binFile);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 }
