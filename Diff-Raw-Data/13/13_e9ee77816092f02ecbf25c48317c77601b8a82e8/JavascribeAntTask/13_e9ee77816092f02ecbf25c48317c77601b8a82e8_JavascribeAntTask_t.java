 package net.sf.javascribe;
 
 import java.io.File;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 public class JavascribeAntTask extends Task {
 	String zipFile = null;
 	String javascribeHome = null;
 
	public void execute() throws BuildException {
     	if (zipFile==null) {
     		throw new BuildException("Javascribe Ant Task requires attribute 'zipFile'");
     	}
     	
     	if (javascribeHome==null) {
     		javascribeHome = System.getProperty("JAVASCRIBE_HOME");
     	}
     	if (javascribeHome==null) {
     		throw new BuildException("Javascribe Ant Task will not run unless given a Javascribe Home location.");
     	}
     	File f = new File(zipFile);
     	if ((!f.exists()) || (f.isDirectory())) {
     		throw new BuildException("Javascribe requires a valid zipFile argument.");
     	}
     	try {
     		new JavascribeLauncher(javascribeHome).invokeJavascribe(f);
     	} catch(Exception e) {
    		e.printStackTrace();
     		throw new BuildException("Javascribe invokation failed",e);
     	}
     }
     
    public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}

	public void setJavascribeHome(String javascribeHome) {
		this.javascribeHome = javascribeHome;
	}

 }
 
