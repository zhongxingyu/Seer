 package game;
 
 import interfaces.IClient;
 import interfaces.IGame;
 import interfaces.ILogin;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.rmi.Naming;
 
 public class ClientData implements IClient, Runnable, Serializable{
 	
 	private static final long serialVersionUID = 1L;
 	String ip;
 	ILogin login_stub;
 	public ClientData() throws Exception{
		String remoteIp = "157.27.243.172";
		if(remoteIp != null){
			this.ip = remoteIp;
		} else {
			InetAddress ip = InetAddress.getLocalHost();
			String ipp = ip.getHostAddress().toString();
			this.ip = ipp;
		}
 	}
 	
 	public static void main(String arg[]) throws Exception{
		/*System.setSecurityManager(new SecurityManager());
 		ClientData cd = new ClientData();
 		cd.login_stub = (ILogin)Naming.lookup("//"+cd.ip+":2222/ProxyServer");
 		LoginFrame lf = new LoginFrame(cd.login_stub);
 		lf.init(cd);
 		/*while( !lf.LoginDone ){System.out.println("nope");};
 		System.out.println("Login Done");
 		IGame GameStub = (IGame)Naming.lookup("//"+cd.ip+":2222/ProxyServer");
 		System.out.println("Lookup Done");*/
 	}
 
 	public void actionPerformed(Boolean logged ) throws Exception{
 		if(logged){
 			System.out.println("Login Done");
 			IGame GameStub = (IGame)Naming.lookup("//"+ip+":2222/ProxyServer");;
 			DisplayExample l = new DisplayExample(GameStub);
 			System.out.println("Created DisplayExample");
 			l.start();
 			System.out.println("Window Closed");
 		}
 	}
 	
 	@Override
 	public void run() {
 		try{
 			System.setSecurityManager(new SecurityManager());
 			ClientData cd = new ClientData();
 			ILogin LoginStub = (ILogin)Naming.lookup("//"+cd.ip+":2222/ProxyServer");
 			LoginFrame lf = new LoginFrame(LoginStub);
 			lf.init(cd);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 }
