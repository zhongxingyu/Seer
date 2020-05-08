 package net.mccg.lunt;
 
 import java.io.DataInputStream;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 public class LuntDeployTask extends Task{
 
 	private String luntUsername;
 	private String luntPassword;
 	private String luntServer;
 	private String project;
 	private String schedule;
 	private String artifact;
 	private String deployUsername;
 	private String deployPassword;
 	private String deployServer;
 	private String deployDir;
 	private String isLocalDeploy;
 	
 	public void execute() throws BuildException {
 		try {
 			String filepath = getLuntService(getLuntProject()).getArtifactUrl();
 			deploy(filepath);
 		} catch (Exception e) {
 			throw new BuildException(e);
 		}
 	}
 
 	LuntProject getLuntProject() {
 		return new LuntProject(luntUsername, luntPassword, luntServer, project, schedule, artifact);
 	}
 
 	LuntServiceImpl getLuntService(LuntProject luntProject) throws Exception {
 		return new LuntServiceImpl(luntProject);
 	}
 
 	SshServiceImpl getSshService() throws Exception {
 		return new SshServiceImpl(deployServer, deployUsername, deployPassword);
 	}
 
 	void deploy(final String filepath) {
	    log("Deploying Lunt Artifact: " + filepath);
 		try {
 			URL url = new URL(filepath);
 			URLConnection urlConn = url.openConnection();
 			urlConn.setDoInput(true);
 			urlConn.setUseCaches(false);
 			DataInputStream dis = new DataInputStream(urlConn.getInputStream());
 			
 			if(isLocalCopy()){
			    FileOutputStream fos = new FileOutputStream(deployDir+"/"+getArtifactName(artifact));
 			    int c;
 		        while ((c = dis.read()) != -1) 
 		            fos.write(c);
 		        fos.close();
 			} else {
			    getSshService().sftp(dis, deployDir, getArtifactName(artifact));
 			}
 		} catch (Exception e) {
 			throw new BuildException(e);
 		}
 	}
 
	private String getArtifactName(final String artifact) {
        if(artifact.indexOf("/") != -1)
            return artifact.substring(artifact.lastIndexOf("/")+1);
        return artifact;
    }
	
 	private boolean isLocalCopy() {
         return "true".equalsIgnoreCase(isLocalDeploy);
     }
 
     public void setLuntUsername(String luntUsername) {
 		this.luntUsername = luntUsername;
 	}
 
 	public void setLuntPassword(String luntPassword) {
 		this.luntPassword = luntPassword;
 	}
 
 	public void setLuntServer(String luntServer) {
 		this.luntServer = luntServer;
 	}
 
 	public void setDeployUsername(String deployUsername) {
 		this.deployUsername = deployUsername;
 	}
 
 	public void setDeployPassword(String deployPassword) {
 		this.deployPassword = deployPassword;
 	}
 
 	public void setDeployServer(String deployServer) {
 		this.deployServer = deployServer;
 	}
 
 	public void setProject(String project) {
 		this.project = project;
 	}
 
 	public void setSchedule(String schedule) {
 		this.schedule = schedule;
 	}
 
 	public void setArtifact(String artifact) {
 		this.artifact = artifact;
 	}
 
 	public void setDeployDir(String deployDir) {
 		this.deployDir = deployDir;
 	}
 
     public void setIsLocalDeploy(String isLocalDeploy) {
         this.isLocalDeploy = isLocalDeploy;
     }
 }
