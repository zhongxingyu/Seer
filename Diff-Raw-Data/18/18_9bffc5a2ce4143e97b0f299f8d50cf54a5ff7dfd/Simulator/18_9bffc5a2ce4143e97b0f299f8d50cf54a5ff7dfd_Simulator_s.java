 package com.hexcore.cas.control.server;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryMXBean;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.hexcore.cas.math.Recti;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.World;
 import com.hexcore.cas.utilities.Log;
 
 public class Simulator extends Thread
 {
 	private static final String TAG = "Server";
 	
 	private List<InetSocketAddress> clientAddresses = null;
 	
 	private AtomicBoolean connected = new AtomicBoolean(false);
 	private AtomicBoolean isFinishedGenerations = new AtomicBoolean(false);
 	private AtomicBoolean paused = new AtomicBoolean(false);
 	private AtomicBoolean reset = new AtomicBoolean(false);
 	
 	private volatile int currentGeneration = 0;
 	private volatile int numOfGenerations = 0;
 	private int threadWorkID = 0;
 	
 	private Recti[] clientWorkables = null;
 	private ThreadWork[] clientWork = null;
 	private World world = null;
 	
 	private CAPIPServer informationProcessor = null;
 	private int clientPort;
 	
 	private Grid 	grid = null;
 	private byte[]	ruleByteCode = null;
 		
 	public Simulator(World world, int clientPort)
 	{
 		this.clientPort = clientPort;
 		this.world = world;
 		this.ruleByteCode = null;
 	}
 		
 	public ThreadWork[] getClientWork()
 	{
 		return clientWork;
 	}
 	
 	public List<InetSocketAddress> getClientAddresses()
 	{
 		return clientAddresses;
 	}
 	
 	public Recti[] getClientWorkables()
 	{
 		return clientWorkables;
 	}
 	
 	public Grid getGrid()
 	{
 		return grid;
 	}
 	
 	public void setGrid(Grid g)
 	{
 		grid = g.clone();
 	}
 	
 	public boolean isFinished()
 	{
 		return isFinishedGenerations.get();
 	}
 		
 	public void pause()
 	{
 		paused.set(true);
 	}
 	
 	public void play()
 	{		
 		numOfGenerations = -1;
 		
 		if (paused.getAndSet(false)) 
 			startGeneration();
 	}
 	
 	public void step()
 	{		
 		numOfGenerations = 1;
 		
 		if (paused.getAndSet(false)) 
 			startGeneration();
 	}	
 		
 	public void requestStatuses()
 	{
 		Log.information(TAG, "Requesting client statuses");
 		informationProcessor.updateClientStatus();
 	}
 	
 	public void forceConnect(int index)
 	{
 		informationProcessor.forceConnect(index);
 	}
 	
 	public void reset()
 	{
 		reset.set(true);
 		numOfGenerations = 0;
 		world.reset();
 		informationProcessor.setGeneration(currentGeneration);
 	}
 	
 	public void setRuleBytecode(byte[] ruleByteCode)
 	{
 		this.ruleByteCode = ruleByteCode;
 	}
 	
 	public byte[] getRuleByteCode()
 	{
 		return ruleByteCode;
 	}
 
 	public void setClients(List<InetSocketAddress> names)
 	{
 		clientAddresses = names;
 	}
 		
 	public void simulate(int gN)
 	{
 		Log.information(TAG, "Waiting for clients to send connect...");
 		while (informationProcessor.getConnectedAmount() != clientAddresses.size()) {}
 
 		Log.information(TAG, "Starting simulation...");
 		setGrid(world.getLastGeneration());
 		numOfGenerations = gN;
 		
 		informationProcessor.sendByteCode(ruleByteCode);
 		
 		calculateSplits();
 		splitGrids();
 
 		isFinishedGenerations.set(false);
 		currentGeneration = 0;
 		
 		if (!paused.get()) startGeneration();
 	}
 		
 	public void disconnect()
 	{
 		informationProcessor.disconnect();
 	}
 	
 	public void finishedGeneration()
 	{
 		threadWorkID = 0;
 		
 		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
 		long usedHeap = bean.getHeapMemoryUsage().getUsed();
 		long maxHeap = bean.getHeapMemoryUsage().getMax();
 		System.out.println("Bean heap memory : " + usedHeap);
 		System.out.println("Bean heap memory MAX : " + maxHeap);
 		if(usedHeap < maxHeap - (100 * 1024 * 1024))
 		{
 			world.addGeneration(grid.clone());
 		}
 		else
 		{
 			System.out.println("Out of memory!  Pausing system...");
 			paused.set(true);
 		}
 		
 		splitGrids();
 		
 		Log.information(TAG, "Finished generation: " + currentGeneration);
 		
 		if (reset.getAndSet(false))
 		{
 			paused.set(true);
 			setGrid(world.getInitialGeneration());
 			calculateSplits();
 			splitGrids();
 		}
 		
 		if (!paused.get()) startGeneration();
 	}
 	
 	@Override
 	public void run()
 	{
 		informationProcessor = new CAPIPServer(this, clientPort);
 		informationProcessor.connectClients(clientAddresses);
 		informationProcessor.start();
 		connected.set(true);
 	}
 	
 	class GenerationThread extends Thread
 	{
 		@Override
 		public void run()
 		{			
 			if (numOfGenerations == 0)
 			{
 				System.out.println("Finished!");
 				isFinishedGenerations.set(true);
 				paused.set(true);
 				return;
 			}
 			
 			Log.information(TAG, "Clients: " + informationProcessor.getConnectedAmount());
 			if (informationProcessor.getConnectedAmount() == 0)
 			{
 				System.out.println("No clients connected, pausing...");
 				paused.set(true);
 				return;
 			}
 			
 			if (numOfGenerations > 0) numOfGenerations--;
 			
 			informationProcessor.setClientWork(grid, clientWork, currentGeneration);
 			informationProcessor.sendInitialGrids();
 		}
 	}
 	
 	/////////////////////////////////////////////
 	/// Private functions
 	
 	private void startGeneration()
 	{
 		isFinishedGenerations.set(false);
 		
 		currentGeneration++;
 		GenerationThread generationThread = new GenerationThread();
 		generationThread.start();		
 	}
 		
 	/*
 	 * @author Abby Kumar
 	 */
 	private static Recti[] divideToClients(Vector2i sizeOfGrid, int splittingFactor, int minimumSize)
 	{
 		int temp;
 		//calculate how many splits needed:
 		if (sizeOfGrid.x > sizeOfGrid.y)
 			temp = 1;
 		else
 			temp = -1;
 		
 		int x = 1, y = 1;
 		int product = x * y;
 		
 		while(product < splittingFactor)
 		{
 			if (temp > 0)
 			{
 				if (x * 2 * minimumSize >= sizeOfGrid.x) break;
 				x += x;
 				temp = temp * (-1);
 			}
 			else if(temp < 0)
 			{
 				if (y * 2 * minimumSize >= sizeOfGrid.y) break;
 				y += y;
 				temp = temp * (-1);
 			}
 			product = x * y;
 		}
 		
 		int splitx[] = new int [x+1];
 		int splity[] = new int [y+1];
 		
 		for(int a = 0; a < x; a++)
 		{
 			splitx[a] = 0;
 		}
 		for(int a = 0; a < y; a++)
 		{
 			splity[a] = 0;
 		}
 		int i = 1, j = 1;
 		
 		double px = (double)sizeOfGrid.x/x;
 		double py = (double)sizeOfGrid.y/y;
 		
 		int posx = 0;
 		int posy = 0;
 		
 		double cx = 0.0;
 		double cy = 0.0;
 		
 		while(i < splitx.length)
 		{
 			while((posx-cx) < px)
 			{
 					posx++;
 			}
 			splitx[i] = posx;
 			i++;
 			cx += px;
 		}
 		
 		while(j < splity.length)
 		{
 			while((posy-cy) < py)
 			{
 					posy++;
 			}
 			splity[j] = posy;
 			j++;
 			cy += py;
 		}
 		Recti[] split = new Recti[product];
 		int c = 0;
 		
 		for(int a = 0; a < splity.length-1; a++)
 		{
 			for(int b = 0; b < splitx.length-1; b++)
 			{
 				int widthx = splitx[b+1] - splitx[b];
 				int widthy = splity[a+1] - splity[a];
 				split[c++] = new Recti(new Vector2i(splitx[b],splity[a]), new Vector2i(widthx, widthy));
 			}
 		}
 		
 		return split;
 	}
 	
 	private void calculateSplits()
 	{
 		Recti[] splits = divideToClients(grid.getSize(), informationProcessor.getTotalCoreAmount() * 4, 1);
 		clientWorkables = new Recti[splits.length];
 		Log.information(TAG, "Split size: " + splits.length);
 		
 		for(int i = 0; i < splits.length; i++)
 			clientWorkables[i] = new Recti(splits[i].getPosition(), splits[i].getSize());
 	}
 	
 	private void splitGrids()
 	{
 		if(grid.isWrappable())
 			splitWrappableGrids();
 		else
 			splitNonwrappableGrids();
 	}
 	
 	private void splitNonwrappableGrids()
 	{
 		int numOfClientWorks = clientWorkables.length;
 		clientWork = new ThreadWork[numOfClientWorks];
 		
 		for(int i = 0; i < numOfClientWorks; i++)
 		{
 			Vector2i borderSize = grid.getNeighbourhoodRange();
 			
 			int left = borderSize.x;
 			int right = borderSize.x;
 			int top = borderSize.y;
 			int bottom = borderSize.y;
 
 			Recti workArea = clientWorkables[i];
 			Vector2i workStart = workArea.getPosition();
 			Vector2i workEnd = workArea.getPosition().add(workArea.getSize());
 			
 			if(workStart.x - left < 0) left = workStart.x;
 			if(workStart.y - top < 0) top = workStart.y;
 			if(workEnd.x >= grid.getWidth()) right = workEnd.x - grid.getWidth();
 			if(workEnd.y >= grid.getHeight()) bottom = workEnd.y - grid.getHeight();
 			
 			Vector2i position = new Vector2i(workArea.getPosition().x - left, workArea.getPosition().y - top);
 			Vector2i size = new Vector2i(workArea.getSize().x + left + right, workArea.getSize().y + top + bottom);
 			Grid workingGrid = grid.getType().create(size, grid.getNumProperties());
 			
 			for(int y = 0; y < size.y; y++)
 				for(int x = 0; x < size.x; x++)
 					workingGrid.setCell(x, y, grid.getCell(position.add(x, y)));
 			
 			Recti aw = new Recti(new Vector2i(left, top), workArea.getSize());
 			
 			clientWork[i] = new ThreadWork(currentGeneration, threadWorkID++, workingGrid, workArea.getPosition(), aw);
 		}
 	}
 	
 	private void splitWrappableGrids()
 	{
 		int numOfClientWorks = clientWorkables.length;
 		clientWork = new ThreadWork[numOfClientWorks];
 		
 		for(int i = 0; i < numOfClientWorks; i++)
 		{
 			Vector2i borderSize = grid.getNeighbourhoodRange();
 			Recti workArea = clientWorkables[i];
 			Vector2i size = new Vector2i(workArea.getSize().x + (2 * borderSize.x), workArea.getSize().y + (2 * borderSize.y));
 			
 			Grid workingGrid = grid.getType().create(size, grid.getNumProperties());
 	
 			int gYPos = 0;
 			int gXPos = 0;
 			for(int y = workArea.getPosition().y - borderSize.y; y < workArea.getPosition().y + workArea.getSize().y + borderSize.y; y++)
 			{
 				for(int x = workArea.getPosition().x - borderSize.x; x < workArea.getPosition().x + workArea.getSize().x + borderSize.x; x++)
 				{
 					int xx = (grid.getWidth() + x) % grid.getWidth();
 					int yy = (grid.getHeight() + y) % grid.getHeight();
 					for(int j = 0; j < grid.getCell(xx, yy).getValueCount(); j++)
 					{
 						workingGrid.getCell(gXPos, gYPos).setValue(j, grid.getCell(xx, yy).getValue(j));
 					}
 					gXPos++;
 					if(gXPos >= workArea.getSize().x + (borderSize.x * 2))
 						gXPos = 0;
 				}
 				gYPos++;
 			}
 			
 			Recti aw = new Recti(borderSize, workArea.getSize());
 			clientWork[i] = new ThreadWork(currentGeneration, threadWorkID++, workingGrid, workArea.getPosition(), aw);
 		}
 	}
 }
