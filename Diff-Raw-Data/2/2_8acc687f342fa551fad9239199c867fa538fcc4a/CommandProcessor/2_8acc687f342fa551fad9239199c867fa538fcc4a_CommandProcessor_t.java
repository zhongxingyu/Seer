 package com.cpcookieman.cookieirc;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JWindow;
 
 public class CommandProcessor
 {
 	public void process(String s, int tab)
 	{
 		if(s.equals(""))
 		{
 			
 		}
 		else if(s.equals("/spinner"))
 		{
 			new Thread(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					final JWindow newFrame = new JWindow();
 					Main.frame.setEnabled(false);
 					newFrame.setVisible(false);
 					newFrame.add(new JLabel(new ImageIcon("res/spinner.gif")));
 					newFrame.pack();
 					newFrame.addMouseListener(new MouseListener()
 					{
 						@Override
 						public void mouseClicked(MouseEvent arg0)
 						{
 							newFrame.dispose();
 							Main.frame.setEnabled(true);
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
 				}
 			}).run();
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
 		else if(s.startsWith("/quit"))
 		{
 			System.exit(0);
 		}
 		else if(s.startsWith("/reloaduser"))
 		{
 			Main.gui.tabs.get(tab).onAction();
 		}
 		else if(s.startsWith("/nick "))
 		{
 			Main.gui.tabs.get(tab).addMessage(Main.user, s);
 			Main.gui.tabs.get(tab).tabSpecificProcess(s);
 			Main.user = s.substring(6);
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
 				//TODO Add pm joining code
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
 			}
 			catch(Exception e)
 			{
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
