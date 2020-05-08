 package csheets.ext.share.core;
 
 import java.io.*;
 import java.net.*;
 import java.util.logging.*;
 
 import javax.swing.JOptionPane;
 
 import csheets.core.Cell;
 import csheets.core.formula.compiler.FormulaCompilationException;
 
 /**
  * Class that implement the client in the extension
  * 
  * @see Runnable
  * @author Andre
  * 
  */
 public class Client implements Runnable {
 	/** the ip of the server */
 	private String IP;
 	/** the port of the connection */
 	private int port;
 	/** the cell where the program will copy */
 	private Cell cellStart;
 	/** the connection to the server */
 	private Connections connection;
 
 	private final CellNetworkListenerClient listener = new CellNetworkListenerClient();
 
 	/**
 	 * Create a new client
 	 */
 	public Client() {
 	}
 
 	/**
 	 * Create internaly a new client
 	 * 
 	 * @param IP
 	 *            of the server
 	 * @param port
 	 *            the port of the connection
 	 * @param cellStart
 	 *            the cell where the program will copy
 	 */
 	private Client(String IP, int port, Cell cellStart) {
 		this.IP = IP;
 		this.port = port;
 		this.cellStart = cellStart;
 	}
 
 	/**
 	 * Create internaly a new client
 	 * 
 	 * @param connection
 	 *            the connection to the server
 	 * @param cellStart
 	 *            the cell where the program will copy
 	 */
 	private Client(Connections connection, Cell cellStart) {
 		this.connection = connection;
 		this.cellStart = cellStart;
 	}
 
 	/**
 	 * Method that will start the client and receive cells throw network
 	 * 
 	 * @param IP
 	 *            the server ip
 	 * @param port
 	 *            the connection port of the server
 	 * @param cellStart
 	 *            cell where we paste the content of the shared cells
 	 */
 	public void startClient(String IP, int port, Cell cellStart) {
 		Thread thr = new Thread(new Client(IP, port, cellStart));
 		thr.start();
 	}
 
 	/**
 	 * Method that will start the client and receive cells throw network
 	 * 
 	 * @param connection
 	 *            the connection to the server
 	 * @param cellStart
 	 *            cell where we paste the content of the shared cells
 	 */
 	public void startClient(Connections connection, Cell cellStart) {
 		Thread thr = new Thread(new Client(connection, cellStart));
 		thr.start();
 	}
 
 	/**
 	 * The method that will treat the incoming cells
 	 * 
 	 * @param cellStart
 	 *            cell where we paste the content
 	 * @param cli
 	 *            the socket of connection
 	 * @throws IOException
 	 *             throw this exception if the I/O have errors
 	 * @throws ClassNotFoundException
 	 *             throw this exception if the object passed throw network was
 	 *             invalid
 	 * @throws FormulaCompilationException
 	 *             throw if the cell does not respect the formula compiler
 	 */
 	private synchronized void receive(Cell cellStart, Socket cli)
 			throws IOException, ClassNotFoundException,
 			FormulaCompilationException {
 		boolean isCell = true;
 		int cellStartRow = cellStart.getAddress().getRow();
 		int cellStartColumn = cellStart.getAddress().getColumn();
 		OutputStream out = cli.getOutputStream();
 		DataOutputStream outStream = new DataOutputStream(out);
 		outStream.writeUTF("send me data");
 		while (isCell) {
 			ObjectInputStream inStream = new ObjectInputStream(
 					cli.getInputStream());
 			CellNetwork cell = (CellNetwork) inStream.readObject();
 			if (cell.isCell()) {
 
 				this.cellStart
 						.getSpreadsheet()
 						.getCell(cellStartColumn + cell.getColumn(),
 								cellStartRow + cell.getRow())
 						.setContent(cell.getContent());
 
 				this.cellStart
 						.getSpreadsheet()
 						.getCell(cellStartColumn + cell.getColumn(),
 								cellStartRow + cell.getRow())
 						.addCellListener(listener);
 
 			} else {
 				isCell = false;
 			}
 		}
 		outStream.writeUTF("Close yourself");
 		// cli.close();
 	}
 
 	/*
 	 * Method that when changes occurred on cells of client's share, listener
 	 * changes a flag value and this method can run and send that update to the
 	 * server
 	 */
 	public synchronized void sendToServer(Socket sock) throws IOException,
 			InterruptedException {
 
 		while (true) {
 			Thread.sleep(100);
 
 			if (listener.getFlag() == true) {
 
 				OutputStream out = sock.getOutputStream();
 				DataOutputStream outStream = new DataOutputStream(out);
 				outStream.writeUTF("send me updated data");
 
 				CellNetwork cell = new CellNetwork(listener.getCell()
 						.getContent(), listener.getCell().getAddress().getRow()
 						- cellStart.getAddress().getRow(), listener.getCell()
 						.getAddress().getColumn()
 						- cellStart.getAddress().getColumn(), true);
 
 				ObjectOutputStream objectOut = new ObjectOutputStream(
 						sock.getOutputStream());
 				objectOut.writeObject(cell);
				System.out.println(cell.getContent());
 
 			}
 			listener.setFlag(false);
 
 		}
 
 	}
 
 	/**
 	 * The running thread
 	 */
 	@Override
 	public void run() {
 		try {
 
 			Socket cli;
 			if (IP != null) {
 				cli = new Socket(IP, port);
 			} else
 				cli = new Socket(connection.getIP(), connection.getPort());
 			receive(cellStart, cli);
 			Thread thr = new Thread(new ThreadClient(port, cellStart, cli,
 					listener));
 			thr.start();
 			while (true) {
 
 				sendToServer(cli);
 			}
 
 		} catch (UnknownHostException e) {
 			JOptionPane.showMessageDialog(null, "Connection Error");
 			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Connection Error");
 			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
 		} catch (ClassNotFoundException e) {
 			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
 		} catch (FormulaCompilationException e) {
 			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
