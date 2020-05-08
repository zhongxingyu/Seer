 /*
   Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.
 
   This software is furnished under a license and may be used only in accordance
   with the terms of that license. This software and documentation, and its
   copyrights are owned by Wabasoft and are protected by copyright law.
 
   THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
   AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
   OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
   INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
   SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
   WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
   MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
   ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
   YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 package waba.applet;
 
 /*
  * Note: Everything that calls waba code in these classes must be
  * synchronized with respect to the Applet uiLock object to allow waba
  * programs to be single threaded. This is because of the multi-threaded
  * nature of Java and because timers use multiple threads.
  *
  * Because all calls into waba are synchronized and users can't call this code,
  * they can't deadlock the program in any way. If we moved the synchronization
  * into waba code, we would have the possibility of deadlock.
  */
 
 import waba.ui.*;
import waba.sys.*;
 
 	class myWindAdap extends java.awt.event.WindowAdapter
 	{
 		Applet applet;
 
 		public myWindAdap(Applet a)
 		{
 			applet = a;			
 		}
 
 		public void windowClosing(java.awt.event.WindowEvent e)
 		{
 			applet.destroy();
 			System.exit(0);
 		}
 	}
 
 public class Applet extends java.applet.Applet
 {
 	String className;
 	Frame frame = null;
 	public MainWindow mainWindow;
 	public boolean isApplication = false;
 	public boolean isColor = false;
 	public int width;
 	public int height;
 
 	public static Object uiLock = new Object();
 	
 	public static Applet currentApplet;
 
 
 	public void init()
 	{
 		setLayout(new java.awt.BorderLayout());
 		// NOTE: getParameter() and size() don't function in a
 		// java applet constructor, so we need to call them here
 		if (!isApplication)
 			{
 				className = getParameter("appClass");
 				width = size().width;
 				height = size().height;
 			}
		Vm.isApplication = isApplication;
 		currentApplet = this;
 		try
 			{
 				Class c = Class.forName(className);
 				mainWindow = (MainWindow)c.newInstance();
 			}
 		catch (Exception e) { e.printStackTrace(); }
 		
 		
 		synchronized(Applet.uiLock)
 			{
 				mainWindow.onStart();
 			}
 		// NOTE: java will call a partially constructed object
 		// if show() is called before all the objects are constructed
 		/*dima
 		  if (frame != null)
 		  frame.show();
 		*/
 	}
 
 	public void start()
 	{
		Vm.isApplication = false;
 		currentApplet = this;
 	}
 
 	public void destroy()
 	{
 		if (mainWindow == null)
 			return;
 		mainWindow._stopTimer();
 		synchronized(Applet.uiLock)
 			{
 				mainWindow.onExit();
 			}
 	}
 
 	public static void main(String args[])
 	{
 		boolean isColor = false;
 		int width = 160;
 		int height = 160;
 		int count = args.length;
 		if (count == 0)
 			{
 				System.out.println("ERROR: you must supply a class name");
 				return;
 			}
 		for (int i = 0; i < count - 1; i++)
 			{
 				if (args[i].equals("/w"))
 					{
 						if (++i < count - 1)
 							try { width = Integer.parseInt(args[i]); }
 						catch (Exception e)
 							{
 								System.out.println("ERROR: bad width");
 							}
 					}
 				else if (args[i].equals("/h"))
 					{
 						if (++i < count - 1)
 							try { height = Integer.parseInt(args[i]); }
 						catch (Exception e)
 							{
 								System.out.println("ERROR: bad height");
 							}
 					}
 				else if (args[i].equals("/color"))
 					isColor = true;
 			}
 		Applet applet = new Applet();
 		applet.className = args[count - 1];
 		applet.isApplication = true;
 		applet.isColor = isColor;
 		applet.width = width;
 		applet.height = height;
 		Frame frame = new Frame();
 		frame.setLayout(new java.awt.BorderLayout());
 		frame.add("Center", applet);
 		myWindAdap windAdap = new myWindAdap(applet);
 		frame.addWindowListener(windAdap);
 		frame.resize(width, height);
 		frame.setVisibleSize(width, height);
 		//	frame.show();
 		applet.frame = frame;
 		// NOTE: java requires us to do this to make sure things paint
 		//	frame.hide();
 		frame.setResizable(false);//dima
 		java.awt.Insets insets;
 		try
 			{
 				insets = frame.getInsets();
 			}
 		catch (NoSuchMethodError e)
 			{
 				insets = frame.insets(); // this is the JDK 1.02 call to get insets
 			}
 		/*dima
 		  if (insets == null)
 		  insets = new java.awt.Insets(0, 0, 0, 0);
 		  //		frame.resize(width + insets.left + insets.right,height + insets.top + insets.bottom);
 		  frame.setSize(width + insets.left + insets.right,height + insets.top + insets.bottom);
 		  java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		  java.awt.Dimension df = frame.getSize();
 		
 		  frame.setLocation(d.width/2 - df.width/2,d.height/2 - df.height/2);
 		*/		
 		
 		applet.init();
 		if (frame != null){
 			frame.setWabaWindow(applet.mainWindow);
 			frame.show();
 		}
 	
 	}
 }
 
