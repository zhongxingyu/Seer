 package ucbang.network;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.*;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.MenuBar;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Entity;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ServerBrowser extends JFrame implements ActionListener{
 	ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
 	JTable servertable;
 	JScrollPane scrollPane;
 	JButton choose, refresh;
	ServerTableModel tm;
 	public ServerBrowser(){
 		downloadList();
 		setPreferredSize(new Dimension(480, 320));
 		setSize(new Dimension(480, 320));
 		this.setTitle("Server Browser");
		tm = new ServerTableModel(servers);
 		servertable=new JTable(tm);
 		scrollPane = new JScrollPane(servertable);
 		servertable.setFillsViewportHeight(true);
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridy=1;
 		gbc.weightx=1;
 		gbc.weighty=1;
 		gbc.gridx=1;
 		gbc.gridheight=4;
 		gbc.gridwidth=2;
 		gbc.fill=GridBagConstraints.BOTH;
 		this.add(scrollPane,gbc);
 		gbc.fill=GridBagConstraints.HORIZONTAL;
 		gbc.weighty=0;
 		gbc.gridy=5;
 
 		gbc.gridx=2;
 		gbc.gridheight=1;
 		gbc.gridwidth=1;
 		choose = new JButton("Choose");
 		this.add(choose,gbc);
 		choose.addActionListener(this);
 		gbc.gridx=1;
 		refresh = new JButton("Refresh");
 		refresh.addActionListener(this);
 		this.add(refresh,gbc);
 		this.pack();
 		this.setVisible(true);
 	}
 	public String chooseServer(){
 		//TODO: Work out a choosing thing.
 		return servers.get(0).ip;
 	}
 	public static void main(String args[]) {
 		new ServerBrowser();
 	}
 
 	public void downloadList() {
 		servers.clear();
 		URL url;
 		try {
 			url = new URL("http://cardgameservers.appspot.com/xml");
 			HttpURLConnection hConnection = (HttpURLConnection) url
 					.openConnection();
 			HttpURLConnection.setFollowRedirects(true);
 			if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
 				DocumentBuilderFactory dbf = DocumentBuilderFactory
 						.newInstance();
 				dbf.setIgnoringElementContentWhitespace(true);
 				DocumentBuilder db = dbf.newDocumentBuilder();
 				Document doc = db.parse(hConnection.getInputStream());
 				for (Node server = doc.getFirstChild().getChildNodes().item(1); server != null; server = server
 						.getNextSibling()) {
 					if (server.getNodeName().equals("server")) {
 						servers.add(processServerNode(server));
 					}
 				}
 				/*
 				 * BufferedReader is = new BufferedReader(new
 				 * InputStreamReader(hConnection.getInputStream()));
 				 * while(is.ready()) System.out.println(is.readLine());
 				 */
 				hConnection.disconnect();
 				Iterator<ServerInfo> iter = servers.iterator();
 				while (iter.hasNext()) {
 					System.out.println(iter.next());
 				}
 			} else {
 				System.out.println("Ugh, something bad happened");
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	ServerInfo processServerNode(Node n) {
 		NodeList ns = n.getChildNodes();
 		return new ServerInfo(ns.item(1).getTextContent(), ns.item(3)
 				.getTextContent(), ns.item(5).getTextContent());
 	}
 
 	void recPrint(Node n) {
 		if (!n.getNodeName().equals("#text"))
 			System.out.println(n.getNodeName() + ":" + n.getTextContent());
 		NodeList ns = n.getChildNodes();
 		for (Node a = ns.item(0); a != null; a = a.getNextSibling()) {
 			recPrint(a);
 		}
 	}
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if(e.getSource().equals(refresh)){
 			downloadList();
			tm.setData(servers);
			tm.fireTableDataChanged();
 		}else if(e.getSource().equals(choose)){
 			int i=servertable.getSelectedRow();
 			System.out.println("Joining "+servers.get(i).ip);
 			new Client(servers.get(i).ip, true);
 		}
 		
 	}
 	private static class MyErrorHandler implements ErrorHandler {
 
 		private PrintWriter out;
 
 		MyErrorHandler(PrintWriter out) {
 			this.out = out;
 		}
 
 		private String getParseExceptionInfo(SAXParseException spe) {
 			String systemId = spe.getSystemId();
 			if (systemId == null) {
 				systemId = "null";
 			}
 			String info = "URI=" + systemId + " Line=" + spe.getLineNumber()
 					+ ": " + spe.getMessage();
 			return info;
 		}
 
 		public void warning(SAXParseException spe) throws SAXException {
 			out.println("Warning: " + getParseExceptionInfo(spe));
 		}
 
 		public void error(SAXParseException spe) throws SAXException {
 			String message = "Error: " + getParseExceptionInfo(spe);
 			throw new SAXException(message);
 		}
 
 		public void fatalError(SAXParseException spe) throws SAXException {
 			String message = "Fatal Error: " + getParseExceptionInfo(spe);
 			throw new SAXException(message);
 		}
 	}
 	private class ServerInfo {
 		String name;
 		String ip;
 		String type;
 
 		public ServerInfo(String name, String ip, String type) {
 			this.name = name;
 			this.ip = ip;
 			this.type = type;
 		}
 
 		public String toString() {
 			return type + " server \"" + name + "\" on " + ip;
 		}
 	}
 	class ServerTableModel extends AbstractTableModel {
 		String[] columns = {"Name","IP Address", "Game Type"};
 	    private String[][] data;
 	    public ServerTableModel(ArrayList<ServerInfo> list){
 	    	data = new String[list.size()][3];
 	    	Iterator<ServerInfo> iter = list.iterator();
 	    	int i=0;
 	    	while(iter.hasNext()){
 	    		ServerInfo temp = iter.next();
 	    		data[i][0]=temp.name;
 	    		data[i][1]=temp.ip;
 	    		data[i][2]=temp.type;
 	    		i++;
 	    	}
 	    }
	    public void setData(ArrayList<ServerInfo> list){
	    	data = new String[list.size()][3];
	    	Iterator<ServerInfo> iter = list.iterator();
	    	int i=0;
	    	while(iter.hasNext()){
	    		ServerInfo temp = iter.next();
	    		data[i][0]=temp.name;
	    		data[i][1]=temp.ip;
	    		data[i][2]=temp.type;
	    		i++;
	    	}
	    }
 	    public int getColumnCount() {
 	        return columns.length;
 	    }
 
 	    public int getRowCount() {
 	        return data.length;
 	    }
 
 	    public String getColumnName(int col) {
 	        return columns[col];
 	    }
 
 	    public Object getValueAt(int row, int col) {
 	        return data[row][col];
 	    }
     }
 
 
 }
