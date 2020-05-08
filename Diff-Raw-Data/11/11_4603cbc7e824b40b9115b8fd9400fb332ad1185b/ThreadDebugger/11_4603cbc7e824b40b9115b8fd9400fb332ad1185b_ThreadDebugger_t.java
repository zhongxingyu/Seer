 package cm.generic;
 
 import java.util.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 
 /**
  * @author vikrams
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class ThreadDebugger extends Debug
 {
 	static     Hashtable registeredThreads = new Hashtable();
 	
 	static 	int nThreads;
 
 	static     JFrame threadControlFrame = new JFrame("Thread Debugger");
 	static     JPanel threadControlPanel =  new JPanel();
 	static     Box verticalBox = new Box(BoxLayout.Y_AXIS);
 	static     ActionListener threadToggler;
 	
 	static	WindowObservable windowObservable = new WindowObservable();
 
 	static
 	{
 		nThreads = 0;	
 
 		threadToggler = new ActionListener()
   		{
   			public void actionPerformed(ActionEvent e)
   			{
   				String action = e.getActionCommand();
   				// ignore "start " / "pause " - we have the thread name as the key into the hashtable
   				
 		 	 	String threadName = action.substring(6,action.length());
 				ThreadToDebug ttd = (ThreadToDebug)registeredThreads.get(threadName);	
 				if (ttd == null)
 				{
 					// System.err.println("\nthreadName = " + threadName);
 					return;
 				}
 							  			
   				boolean paused = toggleAndReturnNewState(ttd); 				
   			
   				if (!paused)
   				{
 					resume(ttd);
   				}
   			}			
   		};	
   		
   		threadControlPanel.add(verticalBox);
 		threadControlFrame.getContentPane().add(threadControlPanel);
 		threadControlFrame.pack();
 		setPosition();		
 
 		threadControlFrame.addWindowListener(windowObservable);
 	}	
 	
 	public ThreadDebugger()
 	{
 		System.err.println("\nThreadDebugger constructor");
 	}
 	
 	public static void registerMyself(Thread t)
 	{
 		synchronized (registeredThreads)
 		{
 			if (registeredThreads.get(t.getName()) != null)
 			{
 				return;	
 			}
 			ThreadToDebug ttdTemp = new ThreadToDebug(t);
 			registeredThreads.put(t.getName(),ttdTemp);				
 			nThreads++;
			println("ThreadDebugger.register("+t+" COUNT = " + 
				nThreads);
 			verticalBox.add(ttdTemp.button);
 			threadControlFrame.pack();
 			setPosition();			
 		}
 	}
 	
 	public	static boolean toggleAndReturnNewState(ThreadToDebug ttd)
 	{
 		ttd.button.setBackground(Color.yellow);
 		return ttd.toggleAndReturnNewState();
 	}
 		
 	public static void waitIfPaused(Thread t)
 	{
 		ThreadToDebug ttd = (ThreadToDebug)registeredThreads.get(t.getName());				
 		Object mylock = ttd.lock;
 		synchronized (mylock)
 		{
 			boolean paused = ttd.paused;
 			if (paused)
 			{
 				try
 				{
 					System.err.println("\nPAUSING THREAD " + t.getName());
 					ttd.button.setBackground(Color.red);
 					mylock.wait();	
 				}catch (InterruptedException e)
 				{
 				}
 			}		
 		}	
 	}		
 	
 	public	static void resume(ThreadToDebug ttd)
 	{
 		Object mylock = ttd.lock;
 		
 		synchronized (mylock)
 		{
 			System.err.println("\nRESTARTING THREAD " + ttd.thread.getName());					
 			mylock.notify();
 			ttd.button.setBackground(Color.green);
 		}				
 	}	
 	
 	public static void removeMyself(Thread t)
 	{
 		ThreadToDebug removedTtd = (ThreadToDebug)registeredThreads.remove(t.getName());
 		verticalBox.remove(removedTtd.button);
 		threadControlFrame.pack();						
 		setPosition();
 	}
 	
 	static int xOriginal, yOriginal;
 		
 	public static void setPosition(int x, int y)
 	{
 		xOriginal = x;
 		yOriginal = y;
 		threadControlFrame.setLocation(x - currentWidth(),y - currentHeight());	
 	}
 	
 	static void setPosition()
 	{
 		threadControlFrame.setLocation(xOriginal - currentWidth(),yOriginal - currentHeight());					
 	}
 	
 	protected static int currentWidth()
 	{
 		return threadControlFrame.getWidth();	
 	}
 	
 	protected static int currentHeight()
 	{
 		return threadControlFrame.getHeight();		
 	}
 	public static void show()
 	{
 		threadControlFrame.setVisible(true);	
 	}
 	public static void hide()
 	{
 		threadControlFrame.setVisible(false);	
 	}
 
 	public static void addObserver(Observer o)
 	{
 	   windowObservable.addObserver(o);
 	}
 
 	static class WindowObservable extends ObservableDebug
 	   implements WindowListener
 	{
 	   public void windowClosing(WindowEvent e)
 	   {
 	      println("ThreadDebugger.windowClosing()");
 	      setChanged();
 	      notifyObservers("thread_debugger_close");
 	   }
 	   public void windowOpened(WindowEvent e)
 	   {
 	   }
 	   public void windowClosed(WindowEvent e)
 	   {
 	   }
 	   public void windowIconified(WindowEvent e)
 	   {
 	   }
 	   public void windowDeiconified(WindowEvent e)
 	   {
 	   }
 	   public void windowActivated(WindowEvent e)
 	   {
 	   }
 	   public void windowDeactivated(WindowEvent e)
 	   {
 	   }
 	}
 }	
 
 class ThreadToDebug
 {
    Object	lock	= new Object();
    JButton	button;
    Thread	thread;
    boolean paused = false;
    
    ThreadToDebug(Thread thread)
    {
       this.thread	= thread;
       button = new JButton("Pause " + thread.getName());
       button.addActionListener(ThreadDebugger.threadToggler);						
    }
    
    public	boolean toggleAndReturnNewState()
    {
       paused = !paused;
       
       if (!paused)
       {
 	 button.setText("Pause " + thread.getName());
       }
       else
       {
 	 button.setText("Start " + thread.getName());			
       }
       return paused;						
    }		
 }
 
