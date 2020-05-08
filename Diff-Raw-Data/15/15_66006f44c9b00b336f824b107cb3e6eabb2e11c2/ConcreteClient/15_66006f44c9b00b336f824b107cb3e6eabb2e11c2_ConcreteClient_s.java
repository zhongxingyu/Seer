 package Client;
 
 import java.awt.Desktop;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * 
  * @author Andrea SMETKO
  * Eurecom DSMWare Project 2013 - Andrea SMETKO - Francois KY
  * Professor Y.ROUDIER
  *
  */
 
 
 public class ConcreteClient extends AbstractClient{
 
 	Socket socket = null;
 	PrintWriter out = null;
 	BufferedReader in = null;
 	String searchedItem = null;
 	String line = null;
 	List<Float> list = new ArrayList<Float>();
 	Map<Float, String> map = new HashMap<Float, String>();

 	/**
 	 * @param args
 	 * @throws InterruptedException
 	 */
 
 
 	public void getResults(final int socketNumber)
 	{
 		System.out.println("Enter the item you want to search for : ");
 		try{
 			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
 			searchedItem = bufferRead.readLine();
 		}
 		catch(IOException e) {}
 
 		try{
 			socket = new Socket("localhost", socketNumber);
 			System.out.println("Client connected on port " + String.valueOf(socketNumber));
 			out = new PrintWriter(socket.getOutputStream(), 
 					true);
 			in = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 		} catch (UnknownHostException e) {
 			System.exit(1);
 		} catch  (IOException e) {
 			System.out.println("Cannot connect to the server! A new connection will trigger in 5sec ");
 			Timer timer = new Timer();
 			timer.schedule(new TimerTask() {
 				  @Override
 				  public void run() {
 					  System.out.println("New connection ");
 					  getResults(socketNumber);
 					  System.out.println("New connection established ");
 				  }
				}, 5*1000);
 		}
 		
 		out.println(searchedItem);
 
 	}
 
 	
 	public void printResults()
 	{		
 		try{
 
 			File f = new File("index.html");
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			
 			bw.write("<html>");
 			bw.write("<head>");
 			bw.write("<meta charset='utf-8'>");
 			bw.write("<title>* DSMWare *</title>");
 			bw.write("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
 			bw.write("<link href='bootstrap/css/bootstrap.css' rel='stylesheet'>");
 			bw.write("<link href='css/flat-ui.css' rel='stylesheet'>");
 			bw.write("</head>");
 			bw.write("<body>");
 			bw.write("<div class='demo-headline'><h1 class='demo-logo'>DSMWare Project - Spring 2013<small>SMETKO Andrea - KY Francois</small></h1></div>");
 			bw.write("<div class='container'>");
 			bw.write("<div class='span11'>");
 			bw.write("<h3 class='demo-panel-title'>Your search is: </h3>");
 			bw.write("<input name='tagsinput' id='tagsinput' class='tagsinput' value='" + searchedItem.replace(' ', ',') + "' />");
 			bw.write("</div>");
 			bw.write("<center><div class='span11'><a href='#Bot' class='btn btn-large btn-block btn-info'>Go to the bottom of the list </a></div></center>");
 			bw.write("<a name='Top'></a>");
 			bw.write("</br>");
 			bw.write("</br>");
 			bw.write("<div class='span11'>");
 			bw.write("<div class='demo-text-box prl'>");
 		
 			while (!(line = in.readLine()).equals("\u0004")) {
 				map.put(Float.valueOf(line.split("-")[line.split("-").length-2]), line);
 			}
 
 			for (Float str : map.keySet()) {
 				list.add(str);
 			}
 
 			Collections.sort(list);
 			for (Float str : list) {
 				bw.write("<div class='fui-radio-unchecked'></div>");
 				bw.write(" " + map.get(str));
 				bw.write("</br>");
 				bw.write("</br>");
 			}
 
 			bw.write("</div>");
 			bw.write("</div>");
 			bw.write("<center><div class='span11'><a href='#Top' class='btn btn-large btn-block btn-primary'>Go to the top of the list </a></div></center>");
 			bw.write("</div>");
 			bw.write("<a name='Bot'></a>");
 			bw.write("<script src='js/jquery-1.8.3.min.js'></script><script src='js/jquery-ui-1.10.3.custom.min.js'></script><script src='js/bootstrap.min.js'></script><script src='js/bootstrap-select.js'></script><script src='js/bootstrap-switch.js'></script><script src='js/jquery.tagsinput.js'></script><script src='js/application.js'></script>");
 			bw.write("</body>");
 			bw.write("</html>");
 			bw.close();
 			Desktop.getDesktop().browse(f.toURI());
 
 			in.close();
 		} catch (IOException e){
 			System.out.println("Read failed");
 		}
 
 	}
 	/**
 	 * @param args
 	 * @throws InterruptedException
 	 */
 	public static void main (String args[]) throws InterruptedException
 	{
 		ConcreteClient cc = new ConcreteClient();
 		cc.getResults(4444);
 		cc.printResults();
 
 	}		      
 
 
 
 
 }
 
