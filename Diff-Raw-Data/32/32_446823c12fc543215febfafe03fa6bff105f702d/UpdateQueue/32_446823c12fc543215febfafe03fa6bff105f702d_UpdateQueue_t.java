 package net.sourceforge.eclipseccase;
 
 import java.util.LinkedList;
 
 import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
 import org.eclipse.core.runtime.IStatus;
 
 public class UpdateQueue
 {
 	private static UpdateQueue instance = new UpdateQueue();
 
 	private Thread worker;
 	private LinkedList queue;
 	private boolean active;
 
 	public static UpdateQueue getInstance()
 	{
 		return instance;
 	}
 
 	private UpdateQueue()
 	{
 		start();
 	}
 
 	public synchronized void start()
 	{
 		if (active)
 			throw new IllegalStateException("Cannot UpdateQueue as it is already running");
 		active = true;
 		queue = new LinkedList();
 		worker = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				updateLoop();
 			}
 		}, "ClearcaseProvider Update Thread");
 		worker.start();
 	}
 
 	public synchronized void stop()
 	{
 		active = false;
 		worker.interrupt();
 		worker = null;
 	}
 
 	public boolean isUpdatesPending()
 	{
 		return (queue.size() > 0);
 	}
 	
 	public void addFirst(Runnable cmd)
 	{
 		synchronized (queue)
 		{
 			queue.addFirst(cmd);
 			queue.notify();
 		}
 	}
 
	public boolean contains(Runnable cmd)
	{
		return queue.contains(cmd);
	}
	
 	public void add(Runnable cmd)
 	{
 		synchronized (queue)
 		{
 			queue.add(cmd);
 			queue.notify();
 		}
 	}
 
 	public void remove(Runnable cmd)
 	{
 		synchronized (queue)
 		{
 			queue.remove(cmd);
 		}
 	}
 
 	private void updateLoop()
 	{
 		while (active)
 		{
 			try
 			{
 				Runnable cmd = null;
 				synchronized (queue)
 				{
 					if (queue.isEmpty())
 					{
 						queue.wait();
 					}
 					cmd = (Runnable) queue.removeFirst();
 				}
 				cmd.run();
 			}
 			catch (InterruptedException ex)
 			{
 			}
 			catch (Throwable ex)
 			{
 				ClearcasePlugin.log(
 					IStatus.ERROR,
 					"Error running command in update queue",
 					ex);
 			}
 		}
 	}
 }
