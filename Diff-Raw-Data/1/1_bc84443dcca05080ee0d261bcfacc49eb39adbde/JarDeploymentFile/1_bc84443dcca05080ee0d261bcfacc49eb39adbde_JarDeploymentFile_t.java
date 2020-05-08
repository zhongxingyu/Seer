 package com.polopoly.ps.hotdeploy.file;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 public class JarDeploymentFile extends AbstractDeploymentObject implements
 		DeploymentFile {
 
 	protected JarFile file;
 	protected ZipEntry entry;
 
 	public JarDeploymentFile(JarFile file, ZipEntry entry) {
 		this.file = file;
 		this.entry = entry;
 	}
 
 	public InputStream getInputStream() throws FileNotFoundException {
 		if (entry == null) {
 			throw new FileNotFoundException("While reading " + this
 					+ ": file not found");
 		}
 
 		try {
 			return file.getInputStream(entry);
 		} catch (IOException e) {
 			throw new FileNotFoundException("While reading " + this + ": "
 					+ e.getMessage());
 		}
 	}
 
 	public String getName() {
 		String entryName = null;
 
 		if (entry != null) {
 			entryName = entry.getName();
 
 			if (entryName.endsWith("/")) {
 				entryName = entryName.substring(0, entryName.length() - 1);
 			}
 		}
 
 		String fileName = file.getName();
 
 		// for equality we don't consider the path of a JAR file, only its name,
 		// since it is likely to
 		// be found in multiple places such as the maven repository and
 		// web-inf/lib.
 		int fileSlash = fileName.lastIndexOf(File.separatorChar);
 
 		if (fileSlash != -1) {
 			fileName = fileName.substring(fileSlash + 1);
 		}
 
 		return fileName + "!" + (entryName != null ? entryName : "n/a");
 	}
 
 	@Override
 	public String toString() {
 		String name = null;
 
 		if (entry != null) {
 			name = entry.getName();
 
 			if (name.endsWith("/")) {
 				name = name.substring(0, name.length() - 1);
 			}
 		}
 
 		return file.getName() + "!" + (name != null ? name : "n/a");
 	}
 
 	public URL getBaseUrl() throws MalformedURLException {
 		String name = appendSlashInFront(getNameOfDirectoryWithinJar());
 		return new URL("jar:file:"
 				+ (new File(file.getName())).getAbsolutePath() + "!" + name);
 	}
 
 	public String getNameOfDirectoryWithinJar() {
 		String name = entry.getName();
 
 		int i = name.lastIndexOf("/");
 
 		if (i != -1) {
 			name = name.substring(0, i + 1);
 		} else {
 			name = "/";
 		}
 		return name;
 	}
 
 	private String appendSlashInFront(String orig){
 		if (!orig.startsWith("/")) {
 			orig = "/" + orig;
 		}
 		return orig;
 	}
 
 	public JarFile getJarFile() {
 		return file;
 	}
 
 	public String getNameWithinJar() {
 		if (entry != null) {
 			return entry.getName();
 		} else {
 			return "";
 		}
 	}
 
 	public long getQuickChecksum() {
 		return entry.getTime();
 	}
 
 	public long getSlowChecksum() {
 		return entry.getCrc();
 	}
 
 	public boolean imports(DeploymentObject object) {
 		return object.equals(this);
 	}
 }
