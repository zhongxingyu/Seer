 package br.ufpr.c3sl.connection;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import javax.swing.JOptionPane;
 
 import br.ufpr.c3sl.daoFactory.DAOFactory;
import br.ufpr.c3sl.session.Session;
 
 public class Internet {
 
 	/**
 	 * Checks if there is Internet connection
 	 * through dummy request
 	 * @return false if there is no connection with the internet
 	 * 		   true if there is connection with tehe internet 	
 	 */
 	public static boolean isReachable()
 	{
 		try {
 			//make a URL to a known source
 			URL url = new URL("http://www.google.com");
 			//URL url = new URL("http://localhost");
 
 			//open a connection to that source
 			HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
 
 			//trying to retrieve data from the source. If there
 			//is no connection, this line will fail
 			Object objData = urlConnect.getContent();
 			System.out.println(objData);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Checks if there is Internet connection
 	 * @return true if there is no connection with the internet
 	 * 		   false if there is connection with tehe internet 	
 	 */
 	public static boolean isNotReachable(){
 		return !isReachable();
 	}
 
 	/**
 	 * Checks if there is Internet connection
 	 * And change the execution mode to local if 
 	 * there is no  internet connection
 	 */
 	public static void verifyConnection(){
 		verify("Sua conexão com a internet foi perdida, " + 
 				"\nCom isso, o modo de execução foi mudado para Local, quando a \nconexão for reestabelicida " +
 				"\nenvie os dados para servidor através do botão enviar \nque está barra de menu.");
 	}
 	
 	/**
 	 * Checks if there is Internet connection
 	 * And change the execution mode to local if 
 	 * there is no  internet connection
 	 * @param msg The message that will be showed if there is no connection
 	 */
 	public static void verifyConnection(String msg){
 		verify(msg);
 	}
 	
 	private static void verify(String msg){
		if(DAOFactory.DATABASE_CHOOSE == DAOFactory.MYSQL && Internet.isNotReachable()){
 			DAOFactory.DATABASE_CHOOSE = DAOFactory.DB4O;
			Session.setMode("Local");
 			JOptionPane.showMessageDialog(null, msg);
 		}
 	}
 }
