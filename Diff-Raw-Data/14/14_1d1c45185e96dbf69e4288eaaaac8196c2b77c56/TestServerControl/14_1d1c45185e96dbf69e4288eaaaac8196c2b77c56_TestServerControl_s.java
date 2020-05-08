 package com.hexcore.cas.control.server.test;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.hexcore.cas.control.protocol.ByteNode;
 import com.hexcore.cas.control.protocol.CAPMessageProtocol;
 import com.hexcore.cas.control.protocol.DictNode;
 import com.hexcore.cas.control.protocol.DoubleNode;
 import com.hexcore.cas.control.protocol.IntNode;
 import com.hexcore.cas.control.protocol.ListNode;
 import com.hexcore.cas.control.protocol.Message;
 import com.hexcore.cas.control.protocol.Node;
 import com.hexcore.cas.control.server.Simulator;
 import com.hexcore.cas.control.server.ThreadWork;
 import com.hexcore.cas.math.Recti;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.TriangleGrid;
 import com.hexcore.cas.model.World;
 import com.hexcore.cas.utilities.Log;
 
 
 public class TestServerControl extends TestCase
 {
 	private static final String TAG = "Test";
 	private static final int TEST_CLIENT_PORT = 3339;
 	
 	public Simulator server = null;
 	public int genNum = 1;
 	
 	private void testClientWork(ThreadWork[] cW)
 	{
 		Log.information(TAG, "Testing clientWork was set correctly");
 		assertEquals(4, cW.length);
 		
 		assertEquals(0, cW[0].getID());
 		assertEquals(4, cW[0].getGrid().getWidth());
 		assertEquals(4, cW[0].getGrid().getHeight());
 		assertEquals(1.0, cW[0].getGrid().getCell(0, 0).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(1, 0).getValue(0));
 		assertEquals(0.0, cW[0].getGrid().getCell(2, 0).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(3, 0).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(0, 1).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(1, 1).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(2, 1).getValue(0));
 		assertEquals(0.0, cW[0].getGrid().getCell(3, 1).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(0, 2).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(1, 2).getValue(0));
 		assertEquals(0.0, cW[0].getGrid().getCell(2, 2).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(3, 2).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(0, 3).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(1, 3).getValue(0));
 		assertEquals(1.0, cW[0].getGrid().getCell(2, 3).getValue(0));
 		assertEquals(0.0, cW[0].getGrid().getCell(3, 3).getValue(0));
 		assertEquals(1, cW[0].getWorkableArea().getPosition().y);
 		assertEquals(1, cW[0].getWorkableArea().getPosition().x);
 		assertEquals(2, cW[0].getWorkableArea().getSize().y);
 		assertEquals(2, cW[0].getWorkableArea().getSize().x);
 
 		assertEquals(2, cW[2].getID());
 		assertEquals(4, cW[2].getGrid().getWidth());
 		assertEquals(4, cW[2].getGrid().getHeight());
 		assertEquals(1.0, cW[2].getGrid().getCell(0, 0).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(1, 0).getValue(0));
 		assertEquals(0.0, cW[2].getGrid().getCell(2, 0).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(3, 0).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(0, 1).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(1, 1).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(2, 1).getValue(0));
 		assertEquals(0.0, cW[2].getGrid().getCell(3, 1).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(0, 2).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(1, 2).getValue(0));
 		assertEquals(0.0, cW[2].getGrid().getCell(2, 2).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(3, 2).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(0, 3).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(1, 3).getValue(0));
 		assertEquals(1.0, cW[2].getGrid().getCell(2, 3).getValue(0));
 		assertEquals(0.0, cW[2].getGrid().getCell(3, 3).getValue(0));
 		assertEquals(1, cW[2].getWorkableArea().getPosition().y);
 		assertEquals(1, cW[2].getWorkableArea().getPosition().x);
 		assertEquals(2, cW[2].getWorkableArea().getSize().y);
 		assertEquals(2, cW[2].getWorkableArea().getSize().x);
 
 		assertEquals(1, cW[1].getID());
 		assertEquals(4, cW[1].getGrid().getWidth());
 		assertEquals(4, cW[1].getGrid().getHeight());
 		assertEquals(0.0, cW[1].getGrid().getCell(0, 0).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(1, 0).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(2, 0).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(3, 0).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(0, 1).getValue(0));
 		assertEquals(0.0, cW[1].getGrid().getCell(1, 1).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(2, 1).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(3, 1).getValue(0));
 		assertEquals(0.0, cW[1].getGrid().getCell(0, 2).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(1, 2).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(2, 2).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(3, 2).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(0, 3).getValue(0));
 		assertEquals(0.0, cW[1].getGrid().getCell(1, 3).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(2, 3).getValue(0));
 		assertEquals(1.0, cW[1].getGrid().getCell(3, 3).getValue(0));
 		assertEquals(1, cW[1].getWorkableArea().getPosition().y);
 		assertEquals(1, cW[1].getWorkableArea().getPosition().x);
 		assertEquals(2, cW[1].getWorkableArea().getSize().y);
 		assertEquals(2, cW[1].getWorkableArea().getSize().x);
 
 		assertEquals(3, cW[3].getID());
 		assertEquals(4, cW[3].getGrid().getWidth());
 		assertEquals(4, cW[3].getGrid().getHeight());
 		assertEquals(0.0, cW[3].getGrid().getCell(0, 0).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(1, 0).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(2, 0).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(3, 0).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(0, 1).getValue(0));
 		assertEquals(0.0, cW[3].getGrid().getCell(1, 1).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(2, 1).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(3, 1).getValue(0));
 		assertEquals(0.0, cW[3].getGrid().getCell(0, 2).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(1, 2).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(2, 2).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(3, 2).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(0, 3).getValue(0));
 		assertEquals(0.0, cW[3].getGrid().getCell(1, 3).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(2, 3).getValue(0));
 		assertEquals(1.0, cW[3].getGrid().getCell(3, 3).getValue(0));
 		assertEquals(1, cW[3].getWorkableArea().getPosition().y);
 		assertEquals(1, cW[3].getWorkableArea().getPosition().x);
 		assertEquals(2, cW[3].getWorkableArea().getSize().y);
 		assertEquals(2, cW[3].getWorkableArea().getSize().x);
 
 		Log.information(TAG, "SUCCESS - clientWork was set correctly");
 	}
 	
 	private void testGridAfterFlips(Grid grid)
 	{
 		Log.information(TAG, "Testing grid was calculated and set correctly");
 			
 		assertEquals('R', grid.getTypeSymbol());
 		assertEquals(4, grid.getWidth());
 		assertEquals(4, grid.getHeight());
 		assertEquals(1.0, grid.getCell(0, 0).getValue(0));
 		assertEquals(1.0, grid.getCell(1, 0).getValue(0));
 		assertEquals(0.0, grid.getCell(2, 0).getValue(0));
 		assertEquals(1.0, grid.getCell(3, 0).getValue(0));
 		
 		assertEquals(1.0, grid.getCell(0, 1).getValue(0));
 		assertEquals(0.0, grid.getCell(1, 1).getValue(0));
 		assertEquals(1.0, grid.getCell(2, 1).getValue(0));
 		assertEquals(1.0, grid.getCell(3, 1).getValue(0));
 		
 		assertEquals(1.0, grid.getCell(0, 2).getValue(0));
 		assertEquals(1.0, grid.getCell(1, 2).getValue(0));
 		assertEquals(0.0, grid.getCell(2, 2).getValue(0));
 		assertEquals(1.0, grid.getCell(3, 2).getValue(0));
 		
 		assertEquals(1.0, grid.getCell(0, 3).getValue(0));
 		assertEquals(0.0, grid.getCell(1, 3).getValue(0));
 		assertEquals(1.0, grid.getCell(2, 3).getValue(0));
 		assertEquals(1.0, grid.getCell(3, 3).getValue(0));
 
 		Log.information(TAG, "SUCCESS - grid was calculated and set correctly");
 	}
 	
 	private void testNameList(List<String> list)
 	{
 		Log.information(TAG, "Testing nameList was set correctly");
 		
 		for (String address : list)
 			assertEquals("localhost", address);
 
 		Log.information(TAG, "SUCCESS - nameList was set correctly");
 	}
 	
 	private void testWorkables(Recti[] w, int NOC)
 	{
 		Log.information(TAG, "Testing workables was set correctly");
 
 		Log.information(TAG, "\tnumOfClients");
 		assertEquals(1, NOC);
 
 		Log.information(TAG, "\tclientWorkables");
 
 		assertEquals(0, w[0].getPosition().x);
 		assertEquals(0, w[0].getPosition().y);
 		assertEquals(2, w[0].getSize().x);
 		assertEquals(2, w[0].getSize().y);
 
 		assertEquals(2, w[1].getPosition().x);
 		assertEquals(0, w[1].getPosition().y);
 		assertEquals(2, w[1].getSize().x);
 		assertEquals(2, w[1].getSize().y);
 
 		assertEquals(0, w[2].getPosition().x);
 		assertEquals(2, w[2].getPosition().y);
 		assertEquals(2, w[2].getSize().x);
 		assertEquals(2, w[2].getSize().y);
 
 		assertEquals(2, w[3].getPosition().x);
 		assertEquals(2, w[3].getSize().x);
 		assertEquals(2, w[3].getSize().y);
 
 		Log.information(TAG, "SUCCESS - workables was set correctly");
 	}
 	
 	public void testServer()
 		throws IOException
 	{
 		ClientThread client = new ClientThread();
 		client.create();
 		client.start();
 		
 		try
 		{
 			Thread.sleep(1000);
 		}
 		catch (InterruptedException e1)
 		{
 			e1.printStackTrace();
 		}
 
 		Log.information(TAG, "=============================================");
 		Log.information(TAG, "TESTING SERVER CONTROL IN DISTRIBUTION SYSTEM");
 		Log.information(TAG, "=============================================");
 		RectangleGrid g = new RectangleGrid(new Vector2i(4, 4), new Cell(1));
 		for(int y = 0; y < 4; y++)
 			for(int x = 0; x < 4; x++)
 				g.getCell(x, y).setValue(0, 0.0);
 		g.getCell(2, 0).setValue(0, 1.0);
 		g.getCell(1, 1).setValue(0, 1.0);
 		g.getCell(2, 2).setValue(0, 1.0);
 		g.getCell(1, 3).setValue(0, 1.0);
 		/*
 		 * [0.0][0.0][1.0][0.0]
 		 * [0.0][1.0][0.0][0.0]
 		 * [0.0][0.0][1.0][0.0]
 		 * [0.0][1.0][0.0][0.0]
 		 */
 		
 		//================================== Creating ServerOverseer ==================================
 		World theWorld = new World(); 
 		
 		server = new Simulator(theWorld, TEST_CLIENT_PORT);
 		
 		ArrayList<String> nameList = new ArrayList<String>();
 		nameList.add("localhost");
 		server.setClientNames(nameList);
 		
 		testNameList(server.getClientAddresses());
 
 		Log.debug(TAG, " -- Start server");
 		server.start();
 		
 		try
 		{
 			Thread.sleep(1000);
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 		
 		Log.debug(TAG, " -- Second connect");
 		server.forceConnect(0);
 		
 		try
 		{
 			Thread.sleep(1000);
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 		Log.debug(TAG, " -- Get status");
 		server.requestStatuses();
 		
 		try
 		{
 			Thread.sleep(1000);
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 		
 		Log.debug(TAG, " -- Simulate");
 		theWorld.addGeneration(g);
 		server.simulate(genNum);
 		
 		while(!server.isFinished())
 		{
 			try
 			{
 				Thread.sleep(2500);
 			}
 			catch(Exception ex)
 			{
 				ex.printStackTrace();
 			}
 		}
 		
 		Log.debug(TAG, " -- Finished");
 		
 		Grid grid = server.getGrid();
 		testGridAfterFlips(grid);
 		
 		int NOC = server.getNumberOfClients();
 		Recti[] w = server.getClientWorkables();
 		testWorkables(w, NOC);
 	
 		ThreadWork[] cW = server.getClientWork();
 		testClientWork(cW);
 		
 		assertEquals(2, theWorld.getNumGenerations());
 		
 		server.disconnect();
 		
 		try 
 		{
 			client.join();
 		} 
 		catch (InterruptedException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		Log.information(TAG, "Testing Distribution System complete");
 	}
 	
 	private class ClientThread extends Thread
 	{
 		private static final String TAG = "ClientThread";
 		
 		private CAPMessageProtocol capMP = null;
 		private boolean sentAccept = false;
 		private Grid grid = null;
 		private int gen = 0;
 		private int ID = -1;
 		private static final int PROTOCOL_VERSION = 1;
 		private Recti workable = null;
 		private ServerSocket sock = null;
 
 		public void accept()
 			throws IOException
 		{
 			Log.debug(TAG, "Creating client");
 			capMP = new CAPMessageProtocol(sock.accept());
 			capMP.start();
 		
 			if(capMP != null)
 				Log.information(TAG, "Successfully made a connection to the server");
 		}
 		
 		public void create()
 		{
 			try
 			{
 				sock = new ServerSocket(TEST_CLIENT_PORT);
 			}
 			catch (UnknownHostException e)
 			{
 				e.printStackTrace();
 			}
 			catch(IOException ex)
 			{
 				ex.printStackTrace();
 			}
 		}
 		
 		public void run()
 		{
 			try
 			{
 				accept();
 			}
 			catch(IOException e)
 			{
 				e.printStackTrace();
 			}
 			
 			Message message = null;
 			
 			//Waiting for CONNECT message type.
 			Log.debug(TAG, "Waiting for a connect message...");
 			message = null;
 			while (message == null) message = capMP.waitForMessage();
 	
 			if (message != null)
 			{
 				DictNode header = message.getHeader();
 				if(header.get("TYPE").toString().equals("CONNECT"))
 				{
 					Log.debug(TAG, "Received connect message.");
 					if(header.has("VERSION"))
 					{
 						if(PROTOCOL_VERSION == ((IntNode)header.get("VERSION")).getIntValue())
 						{
 							DictNode h = new DictNode();
 							h.addToDict("TYPE", new ByteNode("ACCEPT"));
 							h.addToDict("VERSION", new IntNode(PROTOCOL_VERSION));
 							DictNode b = new DictNode();
 							b.addToDict("CORES", new IntNode(2));
 							
 							Message msg = new Message(h, b);
 							capMP.sendMessage(msg);
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
 							capMP.sendMessage(msg);
 							
 							Log.information(TAG, "Rejected connection from server");
 						}
 					}
 					else
 						capMP.sendState(2, "VERSION MISSING");
 				}
 				else
 					fail("Expected 1st CONNECT message");
 			}
 			
 			//Waiting for CONNECT message type to raise error.
 			Log.debug(TAG, "Waiting for a second connect message.");
 			message = null;
 			while (message == null) message = capMP.waitForMessage();
 
 			if (message != null)
 			{
 				DictNode header = message.getHeader();
 				if(header.get("TYPE").toString().equals("CONNECT"))
 				{
 					Log.debug(TAG, "Received connect message.");
 					if(sentAccept)
 					{
 						capMP.sendState(2, "CONNECT MESSAGE HAS ALREADY BEEN RECEIVED");
 					}
 				}
 				else
 					fail("Expected 2nd CONNECT message");
 			}
 			
 			//Waiting for QUERY message type
 			Log.debug(TAG, "Waiting for a query message.");
 			message = null;
 			while (message == null) message = capMP.waitForMessage();
 
 			if (message != null)
 			{
 				DictNode header = message.getHeader();
 				if (header.get("TYPE").toString().equals("QUERY"))
 					Log.debug(TAG, "Got status reply");
 				else
 					fail("Expected QUERY message");
 			}
 			
 			for(int a = 0; /*a < 4 * genNum*/; a++)
 			{
 				//Waiting for GRID message type.
 				Log.debug(TAG, "Waiting for grid message number " + (a + 1));
 				message = null;
 				while (message == null) message = capMP.waitForMessage();
 
 				if (message != null)
 				{
 					DictNode header = message.getHeader();
 					DictNode body = (DictNode)message.getBody();
 					if(header.get("TYPE").toString().equals("GRID"))
 					{
 						Vector2i size = null;
 						Recti area = null;
 						int n = -1;
 						char type = 'X';
 						Grid grid = null;
 						int id = -1;
 						int gen = 0;
 						
 						if(body == null)
 						{
 							capMP.sendState(2, "GRID MISSING A BODY");
 						}			
 						else if(!body.has("SIZE"))
 						{
 							capMP.sendState(2, "GRID MISSING A SIZE");
 						}
 						else if(!body.has("AREA"))
 						{
 							capMP.sendState(2, "GRID MISSING AN AREA");
 						}
 						else if(!body.has("PROPERTIES"))
 						{
 							capMP.sendState(2, "GRID MISSING THE PROPERTY AMOUNT");
 						}
 						else if(!body.has("GRIDTYPE"))
 						{
 							capMP.sendState(2, "GRID MISSING THE GRID TYPE");
 						}
 						else if(!body.has("DATA"))
 						{
 							capMP.sendState(2, "GRID DATA MISSING");
 						}
 						else if(!body.has("ID"))
 						{
 							capMP.sendState(2, "GRID ID MISSING");
 						}
 						else if(!body.has("GENERATION"))
 						{
 							capMP.sendState(2, "GRID GENERATION MISSING");
 						}
 						
 						id = ((IntNode)body.get("ID")).getIntValue();
 						
 						gen = ((IntNode)body.get("GENERATION")).getIntValue();
 						
 						ArrayList<Node> sizeList = ((ListNode)body.get("SIZE")).getListValues();
 						size = new Vector2i(((IntNode)sizeList.get(0)).getIntValue(), ((IntNode)sizeList.get(1)).getIntValue());
 	
 						ArrayList<Node> areaList2 = ((ListNode)body.get("AREA")).getListValues();
 						area = new Recti(new Vector2i(((IntNode)areaList2.get(0)).getIntValue(), ((IntNode)areaList2.get(1)).getIntValue()), new Vector2i(((IntNode)areaList2.get(2)).getIntValue(), ((IntNode)areaList2.get(3)).getIntValue()));
 	
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
 								capMP.sendState(2, "GRID TYPE INVALID");
 								return;
 						}
 						
 						ArrayList<Node> rows2 = ((ListNode)body.get("DATA")).getListValues();
 						for(int y = 0; y < rows2.size(); y++)
 						{
 							ArrayList<Node> currRow2 = ((ListNode)rows2.get(y)).getListValues(); 
 							for(int x = 0; x < currRow2.size(); x++)
 							{
 								ArrayList<Node> currCell2 = ((ListNode)currRow2.get(x)).getListValues();
 								for(int i = 0; i < currCell2.size(); i++)
 								{
 									grid.getCell(x, y).setValue(i, ((DoubleNode)currCell2.get(i)).getDoubleValue());
 								}
 							}
 						}
 	
 						this.grid = grid;
 						this.workable = area;
 						this.ID = id;
 						this.gen = gen;
 					}
 					else if (header.get("TYPE").toString().equals("DISCONNECT"))
 						break;
 					else
 						fail("Expected either GRID or DISCONNECT message");
 				}
 				
 				//Create and send grid
 				for(int y = workable.getPosition().y; y < workable.getPosition().y + workable.getSize().y; y++)
 				{
 					for(int x = workable.getPosition().x; x < workable.getPosition().x + workable.getSize().x; x++)
 					{
 						for(int i = 0; i < grid.getCell(x, y).getValueCount(); i++)
 						{
 							int val = (grid.getCell(x, y).getValue(i) == 0) ? 1 : 0;
 							grid.getCell(x, y).setValue(i, val);
 						}
 					}
 				}
 				
 				ListNode sizeNode = new ListNode();
 				sizeNode.addToList(new IntNode(grid.getWidth()));
 				sizeNode.addToList(new IntNode(grid.getHeight()));
 				
 				ListNode rows = new ListNode();
 				for(int y = 0; y < workable.getSize().y; y++)
 				{
 					ListNode currRow = new ListNode(); 
 					for(int x = 0; x < workable.getSize().x; x++)
 					{
 						ListNode currCell = new ListNode();
 						for(int i = 0; i < grid.getNumProperties(); i++)
 							currCell.addToList(new DoubleNode(grid.getCell(workable.getPosition().add(x, y)).getValue(i)));
 						
 						currRow.addToList(currCell);
 					}
 					rows.addToList(currRow);
 				}
 				
 				ListNode areaList = new ListNode();
 				areaList.addToList(new IntNode(workable.getPosition().x));
 				areaList.addToList(new IntNode(workable.getPosition().y));
 				areaList.addToList(new IntNode(workable.getSize().x));
 				areaList.addToList(new IntNode(workable.getSize().y));
 				
 				DictNode d = new DictNode();
 				d.addToDict("DATA", rows);
 				int val = (a <= (4 * genNum - 2)) ? 1 : 0;
 				d.addToDict("MORE", new IntNode(val));
 				d.addToDict("GENERATION", new IntNode(gen));
 				d.addToDict("ID", new IntNode(ID));
 	
 				DictNode h = new DictNode();
 				h.addToDict("TYPE", new ByteNode("RESULT"));
 				h.addToDict("VERSION", new IntNode(PROTOCOL_VERSION));
 	
 				Log.information(TAG, "Sending result");
 				Message msg = new Message(h, d);
 				
 				try
 				{
 					Thread.sleep(1000);
 				}
 				catch(Exception ex)
 				{
 					ex.printStackTrace();
 				}
 				
 				capMP.sendMessage(msg);
 			}
 			
 			if(message != null)
 			{
 				Log.debug(TAG, "Received disconnect message");
 				DictNode header = message.getHeader();
 				if(header.get("TYPE").toString().equals("DISCONNECT"))
 				{
 					capMP.disconnect();
 					try
 					{
 						sock.close();
 					}
 					catch(IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 				else
 					fail("Expected DISCONNECT message");
 			}
 		}
 	}
 }
