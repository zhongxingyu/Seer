 package client;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.JFrame;
 
 import data.CalendarModel;
 
 import framePackage.DefaultView;
 import framePackage.Login;
 
 import networking.Constants;
 import networking.packages.Response;
 
 public class Program {
 	private static InetAddress serverAddress;
 	public static Socket server;
 	private static BlockingQueue<Response> queueForHandlingResponses;
 	public final static CalendarModel calendarModel = new CalendarModel();
 	public static Client client;
 	public static RequestHandler reqHandler;
 	private static JFrame loginFrame;
 	private static Login loginPanel;
 
 	public static void main(String[] args) throws UnknownHostException {
 		queueForHandlingResponses = new LinkedBlockingQueue<Response>();
 		serverAddress = InetAddress.getByName(Constants.serverIP);
 		try {
 			server = new Socket(serverAddress, Constants.port);
 			client = new Client(InetAddress.getByName(Constants.serverIP));
 			reqHandler = new RequestHandler();
 			startThreads();
 		} catch (IOException e) {
 			System.out.println("The server is down");
 		}
 		loginPanel = new Login();
 		loginFrame = new JFrame("SUPA CALENDA!");
 		loginFrame.setContentPane(loginPanel);
 		loginFrame.pack();
 		loginFrame.setLocationRelativeTo(null);
 		loginFrame.setVisible(true);
		loginFrame.getRootPane().setDefaultButton(loginPanel.button);
 //		
 		
 	}
 	
 
 	
 	public static void startThreads(){
 		Thread t = new Thread(new ResponseListener(server, queueForHandlingResponses));
 		t.start();
 		Thread t2 = new Thread(new ResponseHandler(queueForHandlingResponses));
 		t2.start();
 	}
 	
 	public static void loginOK() {
 		System.out.println("loginok");
 		DefaultView dw = new DefaultView(loginPanel.getUsername());
 		JFrame frame = dw.getFrame();
 		frame.setBounds(0, 0, 1260, 768);
 		frame.setVisible(true);
 		loginFrame.setVisible(false);
 	}
 	public static void loginWrong() {
 		System.out.println("loginWRONG");
 	}
 	public static void loginNoExist() {
 		System.out.println("loginNOExist");
 		
 	}
 }
