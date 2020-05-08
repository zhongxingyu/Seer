 package com.cpcookieman.cookieirc;
 
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
 import javax.swing.JWindow;
 
 public class CommandProcessor
 {
 	public Thread currentSpinner;
 	public JWindow newFrame;
 	
 	public void closeSpinner()
 	{
 		newFrame.dispose();
 		Main.frame.setEnabled(true);
 	}
 	
 	public void startSpinner()
 	{
 		currentSpinner = new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				newFrame = new JWindow();
 				Main.frame.setEnabled(false);
 				newFrame.setVisible(false);
 				Toolkit tk = Toolkit.getDefaultToolkit();
 				URL url = getClass().getResource("spinner.gif");
 				Image img = tk.createImage(url);
 				tk.prepareImage(img, -1, -1, null);
 				ImageIcon spinny = new ImageIcon(img);
 				JLabel image = new JLabel(spinny);
 				newFrame.add(image);
 				newFrame.pack();
 				newFrame.addMouseListener(new MouseListener()
 				{
 					@Override
 					public void mouseClicked(MouseEvent arg0)
 					{
 						closeSpinner();
 					}
 					@Override
 					public void mouseEntered(MouseEvent arg0) {}
 					@Override
 					public void mouseExited(MouseEvent arg0) {}
 					@Override
 					public void mousePressed(MouseEvent arg0) {}
 					@Override
 					public void mouseReleased(MouseEvent arg0) {}
 				});
 				newFrame.setLocationRelativeTo(Main.frame);
 				newFrame.setVisible(true);
 				//TODO Fix the spinner
 			}
 		});
 		currentSpinner.run();
 	}
 	
 	public void process(String s, int tab)
 	{
 		if(s.equals(""))
 		{
 			// Do absolutely nothing.
 		}
 		else if(s.equals("/spinner"))
 		{
 			startSpinner();
 		}
 		else if(s.equals("/debug"))
 		{
 			int i = 0;
 			Main.gui.tabs.get(tab).output.append("\n### BEGIN DEBUG LOG ###");
 			while(true)
 			{
 				try
 				{
 					Main.gui.tabs.get(tab).output.append("\n" + Main.getDebugMessages()[i].toString());
 					i++;
 				}
 				catch(Exception e)
 				{
 					break;
 				}
 			}
 			Main.gui.tabs.get(tab).output.append("\n### END DEBUG LOG ###");
 		}
 		else if(s.startsWith("/quit") || s.startsWith("/exit"))
 		{
 			System.exit(0);
 		}
 		else if(s.startsWith("/reloaduser"))
 		{
 			Main.gui.tabs.get(tab).onUpdate();
 		}
 		else if(s.startsWith("/nick "))
 		{
 			Main.gui.tabs.get(tab).addMessage(Main.user, s);
 			Main.user = s.substring(6);
 			Main.gui.tabs.get(tab).tabSpecificProcess(s);
 		}
 		else if(s.startsWith("/close"))
 		{
 			if(tab != 0)
 			{
 				Main.gui.tabs.get(tab).closed = true;
 				Main.gui.jTabbedPane1.remove(Main.gui.tabs.get(tab));
 				Main.debug("Closed tab: " + tab);
 			}
 			else
 			{
 				Main.gui.tabs.get(tab).addMessage("You shouldn't close the console tab!");
 			}
 		}
 		else if(s.startsWith("/join "))
 		{
 			String working = s.substring(6);
 			if(!working.startsWith("#"))
 			{
 				working = "#" + working;
 			}
 			Main.debug("Attempted to join channel " + working);
 			if(Main.gui.tabs.get(tab).action == -1)
 			{
 				Main.gui.tabs.get(tab).addMessage("Not on a server oriented tab!");
 			}
 			else
 			{
 				int i = Main.gui.addTab(new Tab(working, true, 1));
 				Main.gui.tabs.get(i).server = Main.gui.tabs.get(tab).server;
 				Main.gui.tabs.get(i).serverid = Main.gui.tabs.get(tab).serverid;
 				Main.gui.tabs.get(i).addMessage("Entering " + working + "...");
 				Main.servers.get(Main.gui.tabs.get(i).serverid).joinChannel(working);
 			}
 		}
 		else if(s.startsWith("/msg "))
 		{
 			String working = s.substring(5);
 			Main.debug("Messaging " + working);
 			if(Main.gui.tabs.get(tab).action == -1)
 			{
 				Main.gui.tabs.get(tab).addMessage("Not on a server oriented tab!");
 			}
 			else
 			{
 				int i = Main.gui.addTab(new Tab(working, false, 2));
 				Main.gui.tabs.get(i).server = Main.gui.tabs.get(tab).server;
 				Main.gui.tabs.get(i).serverid = Main.gui.tabs.get(tab).serverid;
 				Main.gui.tabs.get(i).addMessage("Chatting with " + working + "...");
 			}
 		}
 		else if(s.startsWith("/server ") || s.startsWith("/connect "))
 		{
 			try
 			{
 				startSpinner();
				currentSpinner.join();
				Thread.sleep(500);
 				String working;
 				if(s.startsWith("/server"))
 				{
 					working = s.substring(8);
 				}
 				else
 				{
 					working = s.substring(9);
 				}
 				int i = Main.gui.addTab(new Tab(working, false, 0));
 				Main.gui.tabs.get(i).addMessage("Connecting to " + working + "...");
 				Main.debug("Connecting to server with arguments '" + working + "'");
 				Main.servers.add(new ServerConnection(working, ++Main.servercounter, i));
 				Main.gui.tabs.get(i).server = working;
 				Main.gui.tabs.get(i).serverid = Main.servercounter;
 				Main.debug("Connected to server '" + working + "'");
 				closeSpinner();
 			}
 			catch(Exception e)
 			{
 				closeSpinner();
 				Main.gui.tabs.get(tab).addMessage("/server or /connect", false);
 				Main.gui.tabs.get(tab).addMessage("Starts a connection with a server. Takes server IP as an argument.", false);
 			}
 		}
 		else
 		{
 			Main.gui.tabs.get(tab).addMessage(Main.user, s);
 			Main.gui.tabs.get(tab).tabSpecificProcess(s);
 			try
 			{
 				Main.gui.tabs.get(tab).getServer().sendMessage(Main.gui.tabs.get(tab).title, s);
 			}
 			catch(NullPointerException e)
 			{
 				Main.gui.tabs.get(tab).addMessage("I'm not sure I understand \"" + s + "\" (Command not recognized)");
 			}
 		}	
 	}
 }
