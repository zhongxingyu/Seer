 package mindpin.java_step_tester.socket;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import mindpin.java_step_tester.thread.CreateServerThread;
 
 
 
 public class JUNIT4Server{
 	
 	private static final int DEFAULT_SERVER_PORT = 10001;
 	
 	public static void main(String[] args) throws IOException{
 		int port = get_port();
 		String message = "java_step_tester starting on 0.0.0.0:" + port + " ...";
 		
 		ServerSocket server = new ServerSocket(port);
 		System.out.println(message);
 		
 		while (true){
 			try {
 				Socket socket = server.accept();
 				new CreateServerThread(socket).start();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private static int get_port() {
 		String port_str = System.getProperty("java_step_tester.port");
 		if(port_str == null || port_str.equals("")){
 			return DEFAULT_SERVER_PORT;
 		}else{
			return Integer.parseInt(port_str);
 		}
 	}
 	
 	public static void log(String log){
 		if(Thread.currentThread().getClass() == CreateServerThread.class){
 			CreateServerThread thread = (CreateServerThread)Thread.currentThread();
 			thread.record_log(log);
 		}else{
 			System.out.println(log);
 		}
 	}
 }
