 package M1.overview;
 
 import M2.Configuration;
 
 public class ClientServerConfiguration extends Configuration {
 
 	public ClientServerConfiguration() throws Exception {
 		super("ClientServerConfiguration", 0);
		this.addComponent(new ClientComponent(this));
		this.addComponent(new ServerComponent(this));
 	}
 
 }
