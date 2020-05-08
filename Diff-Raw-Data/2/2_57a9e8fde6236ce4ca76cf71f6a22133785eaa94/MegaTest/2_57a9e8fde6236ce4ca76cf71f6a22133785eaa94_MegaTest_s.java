 package test;
 
 import java.util.HashMap;
 
 import file.FileServer;
 
 public class MegaTest {
 	public static void main(String[] args) {
 		
 		String usr = "Marcin";
 		String thisIp = "192.168.80.132";
 		String bootstrapIp = "192.168.80.131";
 		int portOut= 6666;
 		int portFile= 13267;
		int bootstrapPort= 2024;
 		String fname="kuku";
 		String path="kuku//";
 		HashMap<String,User> users= new HashMap<String,User>();
 		User u = new User(usr, bootstrapIp, portFile, portOut);
 		users.put(usr, u);
 		
 		NodTest nod = new NodTest(bootstrapIp, bootstrapPort, users, u);
 		
 		TestTreeClientHandler klient = new TestTreeClientHandler(nod);
 		
 		TestTreeServer serwer = new TestTreeServer(null,fname, usr, path, portFile, portOut, thisIp);
 		new Thread(klient).start();
 		new Thread(serwer).start();
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//serwer.ft.addUser("usr2", path);
 		@SuppressWarnings("unused")
 		FileServer server = new FileServer(serwer.ft, usr, portFile);
  		while(true){
  			for (TestTreeClientWBootstrap tc : klient.clients) {
 				if(tc.changed){
 					System.err.println("Wszedem w zmian pliku. FolderTree -> changed = true");
 	 				serwer.ft.update(tc.ft.getFolder());
 	 				tc.changed=false;
 				}else{
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					//System.err.println("Nie byo zmiany w pliku. FolderTree -> changed = false");
 				}
 			}
  		}
 
 	}
 	
 }
