 package org.eweb4j.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 
 public abstract class ScanPackage {
 	
 	public final static Log log = LogFactory.getConfigLogger(ScanPackage.class);
 	
 	private Collection<String> packages = new HashSet<String>();
 	private Collection<String> jars = new HashSet<String>();
 	private Collection<String> classpaths = new HashSet<String>();
 	private String currentClassPath = null;
 	private Collection<String> scans = null;
 	private Collection<String> jarNames = new HashSet<String>();
 	
 	public String readAnnotation(Collection<String> scans) {
 		this.scans = scans;
 		String error = null;
 		
 		try {
 			if (scans == null)
 				return error;
 			
 			for (String scan : this.scans){
 				if (scan.startsWith("AP:")){
 					classpaths.add(scan.replace("AP:", "").replace("${RootPath}", ConfigConstant.ROOT_PATH.subSequence(0, ConfigConstant.ROOT_PATH.length()-1)));
 				}else if (scan.startsWith("JAR:")){
 					jarNames.add(scan.replace("JAR:", "").replace(".jar", ""));
 				}else{
 					packages.add(scan);
 				}
 			}
 			
 			classpaths.add(FileUtil.getTopClassPath(ScanPackage.class));
 			classpaths.add(FileUtil.getLib());
 			log.debug("classpaths -> " + classpaths);
 			log.debug("jarNames -> "+jarNames);
 			for (String jar : FileUtil.getJars()){
				filterScanJar(jarNames, jar);
 			}
 			
 			// 扫描ClassPath中的*.class
 			scanClassPath();
 			// 这两个是有先后顺序的！
 			// 扫描jar包
 			log.debug("jars -> " + jars);
 			scanJar();
 
 		} catch (Exception e) {
 			error = CommonUtil.getExceptionString(e);
 			log.error(error);
 		}
 
 		return error;
 	}
 
	private void filterScanJar(Collection<String> jarNames, String jar) {
 		final String name = new File(jar).getName().replace(".jar", "");
 		for (String scan : jarNames){
 			if (scan.equals("*")){
 				jars.add(jar);
 			}else if (scan.startsWith("*") && scan.endsWith("*")){
 				if (name.contains(scan.subSequence(1, scan.lastIndexOf("*")))){
 					jars.add(jar);
 				}
 			}else if (scan.startsWith("*")){
 				if (name.endsWith(scan.substring(1)))
 					jars.add(jar);
 			}else if (scan.endsWith("*")){
 				if (name.startsWith(scan.substring(0, scan.lastIndexOf("*"))))
 					jars.add(jar);
 			}
 			
 			//equals
			if (scan.equals(name)){
 				jars.add(jar);
 			}
 		}
 	}
 	
 	private void scanClassPath() throws Exception{
 		for (String classpath : classpaths){
 			scanDir(classpath);
 		}
 	}
 	
 	private void scanDir(String classDir) throws Exception {
 		File dir = null;
 		dir = new File(classDir);
 		log.debug("scan classpath -> " + dir);
 		// 递归文件目录
 		if (dir.isDirectory()){
 			this.currentClassPath = dir.getAbsolutePath();
 			scanFile(dir);
 		}
 	}
 
 	/**
 	 * 扫描class文件
 	 * 
 	 * @param dir
 	 * @param actionPackage
 	 * @throws Exception
 	 */
 	private void scanFile(File dir) throws Exception {
 		if (!dir.isDirectory())
 			return;
 
 		File[] files = dir.listFiles();
 		if (files == null || files.length == 0)
 			return;
 		
 		for (File f : files) {
 			if (f.isDirectory())
 				scanFile(f);
 			else if (f.isFile()) {
 				if (!f.getName().endsWith(".class")){
 					if (f.getName().endsWith(".jar")){
 						final String jar = f.getAbsolutePath();
						filterScanJar(jarNames, jar);
 					}
 					continue;
 				}
 
 				String clsName = f.getAbsolutePath().replace(this.currentClassPath, "").replace(File.separator, ".").replace(".class", "").substring(1);
 				boolean isPkg = false;
 				for (String pkg : packages){
 					if (".".equals(pkg) || clsName.startsWith(pkg)){
 						isPkg = true;
 						break;
 					}
 				}
 				
 				if (!isPkg)
 					continue;
 				
 				if (!handleClass(clsName))
 					continue;
 			}
 		}
 	}
 
 	/**
 	 * scan package by jars
 	 * 
 	 * @param jarsParentDirPath
 	 * @param packageName
 	 * @throws Exception
 	 */
 	private void scanJar() {
 		if (jars == null)
 			return;
 
 		for (String p : jars) {
 			File f = new File(p);
 			ZipInputStream zin = null;
 			ZipEntry entry = null;
 			try {
 				zin = new ZipInputStream(new FileInputStream(f));
 
 				log.debug("scanning jar -> " + f.getAbsolutePath());
 
 				while ((entry = zin.getNextEntry()) != null) {
 					
 					String entryName = entry.getName().replace('/', '.');
 					boolean isPkg = false;
 					for (String pkg : packages){
 						if (".".equals(pkg) || entryName.startsWith(pkg)){
 							isPkg = true;
 							break;
 						}
 					}
 					
 					if (isPkg){
 						if (!entryName.endsWith(".class"))
 							continue;
 						
 						final String className = entryName.replace(".class", "");
 						try {
 							if (!handleClass(className))
 								continue;
 						} catch (Error e) {
 							continue;
 						} catch (Exception e) {
 							continue;
 						}
 					}
 
 					zin.closeEntry();
 				}
 				zin.close();
 			} catch (Error e) {
 				continue;
 			} catch (Exception e) {
 				continue;
 			}
 		}
 	}
 	
 	protected abstract boolean handleClass(String className);
 }
