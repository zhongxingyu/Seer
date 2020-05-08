 package org.gears;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.context.Context;
 import org.gears.connection.Connection;
 import org.gears.connection.ConnectionFactory;
 import org.gears.pkmg.Installer;
 import org.gears.pkmg.InstallerFactory;
 import org.gears.template.Templaton;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 
 
 public class Instance {
 	
 	private ConnectionFactory connFactory    = ConnectionFactory.getInstance();
 	private InstallerFactory  installFactory = InstallerFactory.getInstance();
 	
 	private static final Logger LOG = Logger.getLogger(Instance.class);
 	
 	private Connection connection  = null;
 	protected Installer  installer = null;
 	
 	String fqdn;
 	String sshPermKeyPath;
 
 	public Instance(String fqdn, String sshPermKeyPath, Connection connection, Installer installer) {
 		this.sshPermKeyPath = sshPermKeyPath;
 		this.fqdn = fqdn;
 		
 		this.connection = connection;
 		this.installer = installer;
 	}
 	
 	public Instance(String fqdn, String sshPermKeyPath) {
 		this.sshPermKeyPath = sshPermKeyPath;
 		this.fqdn = fqdn;
 		
 		this.connection = this.connFactory.getSSHConnection();
 		this.installer  = this.installFactory.getDebianInstaller();
 	}
 
 	public void setInstaller(Installer installer) {
 		this.installer = installer;		
 	}
 
 	public void setConnection(Connection connection) {
 		this.connection = connection;
 	}
 	
 	public void setFQDN(String fqdn) {
 		this.fqdn = fqdn;
 	}
 	public void setSSHPermKeyPath(String sshPermKeyPath) {
 		this.sshPermKeyPath = sshPermKeyPath;
 	}
 	
 	public String getFQDN() {
 		return fqdn;
 	}
 	public String getSSHPermKeyPath() {
 		return sshPermKeyPath;
 	}
 
 	public boolean connect() {
 		return this.connection.connect(this);
 	}
 	
 	public boolean disconnect() {
 		return this.connection.disconnect(this);
 	}
 
 	public boolean update()  { 
 		return command(this.installer.update());
 	}
 	
 	public boolean install(String commands) {
 		return command(this.installer.install(commands));
 	}
 	
 	public boolean install(String flags, String commands) {
 //		return this.installer.install(flags, commands);		
 		return command(this.installer.install(flags, commands));
 	}
 	
 	public boolean openPort(String value) {
 		return command(this.installer.openPort(value));
 	}
 	
 	public boolean start(String value) {
 		return command(this.installer.start(value));
 	}
 	
 	public boolean restart(String service) {
 		return command(this.installer.restart(service));
 	}
 	
 	public boolean command(String commands) {
 		return this.connection.command(commands);
 	}
 	
 	public boolean render(String source, String dest, Context context) {
 		Templaton templaton = Templaton.getInstance();
 		
 		File destFile = new File(dest);
 		// Make sure parent directory exists for destination file
 		command(String.format("mkdir -p %s", destFile.getParentFile()));
 		
 		String document = "";
 		
 		if(source.endsWith(".vm")){
 			LOG.info("Found vm file, sending to template engine");
 			document = templaton.render(source, context).toString();
 		} else {
 			try {
 				URL url = Resources.getResource(source);
 				document = Resources.toString(url, Charsets.UTF_8);
 				LOG.info("Found regular file, not sending to template engine");
 				command(String.format( "echo -e \"%s\" > %s", document.replace("\"", "\\\"").replace("$", "\\$"), dest));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
		
		
		
 		return true;
 	}
 
 	public void execute(Gear gear) {
 		gear.execute();
 	}
 
 }
