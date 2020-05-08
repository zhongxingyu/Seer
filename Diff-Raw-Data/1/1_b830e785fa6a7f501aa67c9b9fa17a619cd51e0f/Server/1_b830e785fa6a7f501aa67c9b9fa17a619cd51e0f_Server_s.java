 package csheets.ext.share.core;
 
 import java.io.*;
 import java.net.*;
 import java.util.logging.*;
 
 import javax.swing.JOptionPane;
 
 import csheets.core.Cell;
 
 /**
  * Class that implement the server in the extension
  * 
  * @author Andre
  * 
  */
 public class Server implements Runnable {
     /** the connection port */
     private int port;
     /** the cells we will pass throw network */
     private Cell[][] cells;
 
     /**
      * Create a new server
      */
     public Server() {
     }
 
     /**
      * Create internaly a new client
      * 
      * @param port
      *            the connection port
      * @param cells
      *            the cells we will pass throw network
      */
     private Server(int port, Cell[][] cells) {
 	this.port = port;
 	this.cells = cells;
     }
 
     /**
      * Method that will start the server and share the cells throw network
      * 
      * @param port
      *            connection port
      * @param cells
      *            value that will be shared throw network
      */
     public void startServer(int port, Cell[][] cells) {
 	Thread thr = new Thread(new Server(port, cells));
 	try {
 	    thr.start();
 	} catch (Exception e) {
 	    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
 	}
 
     }
 
     /**
      * Method that will send the information throw network
      * 
      * @param cells
      *            value that will be shared throw network
      * @param svr
      *            the socket of connection
      * @throws IOException
      *             throw this exception if the I/O have errors
      */
     private void send(Cell[][] cells, ServerSocket svr) throws IOException {
 	boolean isAlive = true;
 	while (isAlive) {
 	    Socket sock = svr.accept();
 	    DataInputStream in = new DataInputStream(sock.getInputStream());
 	    if (in.readUTF().equals("send me data")) {
 		for (int i = 0; i < cells.length; i++) {
 		    for (int j = 0; j < cells[i].length; j++) {
 			CellNetwork cell = new CellNetwork(
 				cells[i][j].getContent(), i, j, true);
 			ObjectOutputStream outStream = new ObjectOutputStream(
 				sock.getOutputStream());
 			outStream.writeObject(cell);
 		    }
 		}
 		CellNetwork cell = new CellNetwork("", 0, 0, false);
 		ObjectOutputStream outStream = new ObjectOutputStream(
 			sock.getOutputStream());
 		outStream.writeObject(cell);
 	    }
 	    if (in.readUTF().equals("Close yourself")) {
 		isAlive = false;
 	    }
 	    sock.close();
 	}
     }
 
     /**
      * Running thread
      */
     @Override
     public void run() {
 	try {
 	    ServerSocket svr = new ServerSocket(port);
 	    send(cells, svr);
 	} catch (IOException e) {
 	    JOptionPane.showMessageDialog(null, "Connection Error");
 	    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
 	}
     }
 }
