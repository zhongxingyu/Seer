 package com.hexcore.cas.control.client;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import com.hexcore.cas.control.protocol.ByteNode;
 import com.hexcore.cas.control.protocol.CAPInformationProcessor;
 import com.hexcore.cas.control.protocol.CAPMessageProtocol;
 import com.hexcore.cas.control.protocol.DictNode;
 import com.hexcore.cas.control.protocol.DoubleNode;
 import com.hexcore.cas.control.protocol.IntNode;
 import com.hexcore.cas.control.protocol.ListNode;
 import com.hexcore.cas.control.protocol.Message;
 import com.hexcore.cas.control.protocol.Node;
 import com.hexcore.cas.control.protocol.Overseer;
 import com.hexcore.cas.math.Recti;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.TriangleGrid;
 import com.hexcore.cas.utilities.Log;
 
 public class CAPIPClient extends CAPInformationProcessor
 {
 	private static final String TAG = "CAPIPClient";
 	
 	private boolean sentAccept = false;
 	private boolean valid = false;
 	private CAPMessageProtocol protocol = null;
 	private ClientOverseer parent = null;
 	private ServerSocket sock = null;
 	
 	public CAPIPClient(Overseer o)
 		throws IOException
 	{
 		super();
 		
 		Log.information(TAG, "Creating Client...");
 		
 		parent = (ClientOverseer)o;
 		try
 		{
 			sock = new ServerSocket(3119);
 			valid = true;
 			Log.information(TAG, "Socket listening on " + sock.getLocalPort());
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Could not bind socket to port 3119 - " + ex.getMessage());
 		}
 	}
 	
 	public boolean isValid()
 	{
 		return valid;
 	}
 	
 	@Override
 	public void disconnect()
 	{
 		Log.information(TAG, "Disconnecting...");
 		
 		valid = false;
 		super.disconnect();
 				
 		try
 		{
 			if (sock != null) sock.close();
 		}
 		catch(IOException e)
 		{
 			Log.error(TAG, "Error closing ServerSocket");
 			e.printStackTrace();
 		}
 		
 		sentAccept = false;
 	}
 
 	public void sendResult(Grid g, Recti area, boolean more, int id)
 	{
 		ListNode rows = new ListNode();
 		for(int y = 0; y < g.getHeight(); y++)
 		{
 			ListNode currRow = new ListNode(); 
 			for(int x = 0; x < g.getWidth(); x++)
 			{
 				ListNode currCell = new ListNode();
 				for(int i = 0; i < g.getCell(x, y).getValueCount(); i++)
 				{
 					currCell.addToList(new DoubleNode(g.getCell(x, y).getValue(i)));
 				}
 				currRow.addToList(currCell);
 			}
 			rows.addToList(currRow);
 		}
 		
 		DictNode d = new DictNode();
 		d.addToDict("DATA", rows);
 		d.addToDict("MORE", new IntNode(more ? 1 : 0));
 		d.addToDict("ID", new IntNode(id));
 		sendResult(d);
 	}
 	
 	private void sendResult(DictNode d)
 	{
 		Log.information(TAG, "Sending result");
 		Message msg = new Message(makeHeader("RESULT"), d);
 		protocol.sendMessage(msg);
 	}
 	
 	public void sendState(int is)
 	{
 		DictNode body = new DictNode();
 		body.addToDict("STATE", new IntNode(is));
 		
 		Message msg = new Message(makeHeader("STATUS"), body);
 		protocol.sendMessage(msg);
 	}
 	
 	public void sendState(int is, String mess)
 	{
 		DictNode body = new DictNode();
 		body.addToDict("STATE", new IntNode(is));
 		body.addToDict("MSG", new ByteNode(mess));
 		
 		Message msg = new Message(makeHeader("STATUS"), body);
 		protocol.sendMessage(msg);
 	}
 
 	@Override
 	protected void interpretInput(Message message)
 	{
 		DictNode header = message.getHeader();
 		DictNode body = (DictNode)message.getBody();
 		
 		if(!header.has("TYPE"))
 		{
 			sendState(2, "MESSAGE TYPE NOT FOUND");
 			return;
 		}
 		
 		Log.information(TAG, "Got message: " + header.get("TYPE").toString());
 		
 		if(header.get("TYPE").toString().equals("CODE"))
 		{
 			if(!sentAccept)
 			{
 				sendState(2, "CONNECT MESSAGE HAS NOT BEEN RECEIVED YET");
 				return;
 			}
 
 			if(!body.has("DATA"))
 			{
 				sendState(2, "DATA MISSING FOR CODE MESSAGE TYPE");
 				return;
 			}
 
 			//parent.setRules(((ByteNode)codeInfo.get("DATA")).getByteValues());
 			Log.warning(TAG, "DATA message unimplemented");
 		}
 		else if(header.get("TYPE").toString().equals("CONNECT"))
 		{
 			if(sentAccept)
 			{
 				sendState(2, "CONNECT MESSAGE HAS ALREADY BEEN RECEIVED");
 				return;
 			}
 			else if(header.has("VERSION"))
 			{
 				if(PROTOCOL_VERSION == ((IntNode)header.get("VERSION")).getIntValue())
 				{
 					DictNode h = new DictNode();
 					h.addToDict("TYPE", new ByteNode("ACCEPT"));
 					h.addToDict("VERSION", new IntNode(PROTOCOL_VERSION));
 					
 					Message msg = new Message(h);
 					protocol.sendMessage(msg);
 					sentAccept = true;
 					
 					Log.information(TAG, "Accepted connection from server");
 				}
 				else
 				{
 					DictNode h = new DictNode();
 					h.addToDict("TYPE", new ByteNode("REJECT"));
 					h.addToDict("VERSION", new IntNode(PROTOCOL_VERSION));
 					
 					DictNode b = new DictNode();
 					b.addToDict("MSG", new ByteNode("VERSIONS INCOMPATIBLE"));
 					Message msg = new Message(h, b);
 					protocol.sendMessage(msg);
 					
 					Log.information(TAG, "Rejected connection from server");
 				}
 			}
 			else
 				sendState(2, "VERSION MISSING");
 		}
 		else if(header.get("TYPE").toString().equals("DISCONNECT"))
 		{
 			if(!sentAccept)
 			{
 				sendState(2, "CONNECT MESSAGE HAS NOT BEEN RECEIVED YET");
 				return;
 			}
 			
 			System.out.println("CAPIPClient: Got disconnect");
 			
 			parent.disconnect();
 		}
 		else if(header.get("TYPE").toString().equals("GRID"))
 		{			
 			if(!sentAccept)
 			{
 				sendState(2, "CONNECT MESSAGE HAS NOT BEEN RECEIVED YET");
 				return;
 			}
 			
 			Vector2i size = null;
 			Recti area = null;
 			int n = -1;
 			char type = 'X';
 			Grid grid = null;
 			int id = -1;
 			
 			if(body == null)
 			{
 				sendState(2, "GRID MISSING A BODY");
 				return;
 			}			
 			else if(!body.has("SIZE"))
 			{
 				sendState(2, "GRID MISSING A SIZE");
 				return;
 			}
 			else if(!body.has("AREA"))
 			{
 				sendState(2, "GRID MISSING AN AREA");
 				return;
 			}
 			else if(!body.has("PROPERTIES"))
 			{
 				sendState(2, "GRID MISSING THE PROPERTY AMOUNT");
 				return;
 			}
 			else if(!body.has("GRIDTYPE"))
 			{
 				sendState(2, "GRID MISSING THE GRID TYPE");
 				return;
 			}
 			else if(!body.has("DATA"))
 			{
 				sendState(2, "GRID DATA MISSING");
 				return;
 			}
 			else if(!body.has("ID"))
 			{
 				sendState(2, "GRID ID MISSING");
 				return;
 			}
 						
 			ArrayList<Node> sizeList = ((ListNode)body.get("SIZE")).getListValues();
 			size = new Vector2i(((IntNode)sizeList.get(0)).getIntValue(), ((IntNode)sizeList.get(1)).getIntValue());
 
 			ArrayList<Node> areaList = ((ListNode)body.get("AREA")).getListValues();
 			area = new Recti(new Vector2i(((IntNode)areaList.get(0)).getIntValue(), ((IntNode)areaList.get(1)).getIntValue()), new Vector2i(((IntNode)areaList.get(2)).getIntValue(), ((IntNode)areaList.get(3)).getIntValue()));
 
 			id = ((IntNode)body.get("ID")).getIntValue();
 			
 			n = ((IntNode)body.get("PROPERTIES")).getIntValue();
 			
 			type = body.get("GRIDTYPE").toString().charAt(0);
 			
 			switch(type)
 			{
 				case 'h':
 				case 'H':
 					grid = new HexagonGrid(size, new Cell(n));
 					break;
 				case 't':
 				case 'T':
 					grid = new TriangleGrid(size, new Cell(n));
 					break;
 				case 'r':
 				case 'R':
 					grid = new RectangleGrid(size, new Cell(n));
 					break;
 				default:
 					sendState(2, "GRID TYPE INVALID");
 					return;
 			}
 			
 			ArrayList<Node> rows = ((ListNode)body.get("DATA")).getListValues();
 			for(int y = 0; y < rows.size(); y++)
 			{
 				ArrayList<Node> currRow = ((ListNode)rows.get(y)).getListValues(); 
 				for(int x = 0; x < currRow.size(); x++)
 				{
 					ArrayList<Node> currCell = ((ListNode)currRow.get(x)).getListValues();
 					for(int i = 0; i < currCell.size(); i++)
 					{
 						grid.getCell(x, y).setValue(i, ((DoubleNode)currCell.get(i)).getDoubleValue());
 					}
 				}
 			}
 
 			System.out.println("CAPIPClient: Got work from server");
 			parent.addGrid(grid, area, id);
 		}
 		else if(header.get("TYPE").toString().compareTo("QUERY") == 0)
 		{
 			if(!sentAccept)
 			{
 				sendState(2, "CONNECT MESSAGE HAS NOT BEEN RECEIVED YET");
 				return;
 			}
 			sendState(parent.checkState());
 		}
 		else
 			sendState(2, "MESSAGE TYPE NOT RECOGNISED");
 	}
 	
 	@Override
 	public void start()
 	{
 		setup();
 		running = true;
 		super.start();
 	}
 	
 	public void setup()
 	{
 		Log.information(TAG, "Waiting for server...");
 		
 		try
 		{
 			Socket clientSocket = sock.accept();
 			Log.information(TAG, "Server connected");
 			protocol = new CAPMessageProtocol(clientSocket);
 			connected = true;
 		}
 		catch(IOException e)
 		{
 			Log.error(TAG, "Error starting protocol");
 			e.printStackTrace();
 		}
 	}
 		
 	@Override
 	public void run()
 	{				
 		Log.information(TAG, "Client Running...");
 		
 		protocol.start();
 		
 		while (running && protocol.isRunning())
 		{
 			Message message = protocol.waitForMessage();
 			if (message == null) continue; 
 				
 			interpretInput(message);
 		}
 
		Log.information(TAG, "Stopping client...");
 		protocol.disconnect();
		
		try
		{
			sock.close();
		} 
		catch (IOException e)
		{
		}
 	}
 }
