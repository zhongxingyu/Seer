 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import org.dasein.cloud.compute.VmState;
 
 import be.ac.ua.comp.scarletnebula.core.Server;
 import be.ac.ua.comp.scarletnebula.gui.addserverwizard.AddServerWizard;
 
 class ServerListMouseListener implements MouseListener
 {
 	private GUI gui;
 	private ServerListModel serverListModel;
 	private ServerList serverlist;
 
 	ServerListMouseListener(GUI gui, ServerList serverlist,
 			ServerListModel serverListModel)
 	{
 		super();
 		this.gui = gui;
 		this.serverlist = serverlist;
 		this.serverListModel = serverListModel;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e)
 	{
 		if (e.isPopupTrigger())
 		{
 			ServerList list = (ServerList) e.getSource();
 			int indexOfSelectedServer = list.locationToIndex(e.getPoint());
 
 			// TODO:
 			// When no servers OR a single server is selected, choose what
 			// server this will take effect on based on the current mouse
 			// position. If more servers are selected, apply action to all
 			// selected servers!
 			if (list.getSelectedIndices().length <= 1)
 			{
 				list.setSelectedIndex(indexOfSelectedServer);
 			}
 			final Server selectedServer = serverListModel
 					.getVisibleServerAtIndex(indexOfSelectedServer);
 
 			if (selectedServer == null)
 				return;
 
 			VmState status = selectedServer.getStatus();
 
 			JPopupMenu popup = new JPopupMenu();
 			JMenuItem pauseResume = new JMenuItem(
 					(status == VmState.PAUSED) ? "Resume" : "Pause",
 					new ImageIcon(getClass().getResource("/images/paused.png")));
 
 			pauseResume.addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					gui.pauseSelectedServers();
 				}
 			});
 
 			JMenuItem reboot = new JMenuItem("Reboot", new ImageIcon(getClass()
 					.getResource("/images/restarting.png")));
 			reboot.addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					gui.rebootSelectedServers();
 				}
 			});
 
 			JMenuItem terminate = new JMenuItem("Terminate", new ImageIcon(
 					getClass().getResource("/images/terminated.png")));
 			terminate.addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					gui.terminateSelectedServers();
 				}
 			});
 
 			JMenuItem refresh = new JMenuItem("Refresh", new ImageIcon(
 					getClass().getResource("/images/refresh16.png")));
 			refresh.addActionListener(new ActionListener()
 			{
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					gui.refreshSelectedServers();
 				}
 			});
 
 			JMenuItem unlink = new JMenuItem("Unlink Instance", new ImageIcon(
 					getClass().getResource("/images/unlink16.png")));
 			unlink.addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					gui.unlinkSelectedServers();
 				}
 			});
 
 			if (status != VmState.RUNNING && status != VmState.PAUSED)
 			{
 				pauseResume.setEnabled(false);
 				reboot.setEnabled(false);
 				terminate.setEnabled(false);
 			}
 
 			popup.add(pauseResume);
 			popup.add(reboot);
 			popup.add(terminate);
 			popup.add(refresh);
 			popup.add(unlink);
 
 			popup.show(e.getComponent(), e.getX(), e.getY());
 		}
 		else if (e.getClickCount() == 2)
 		{
 			// If the user double clicked a null server, this is taken as a hint
 			// to create a new server!
 			if (serverlist.startNewServerServerSelected())
 			{
 				new AddServerWizard(gui, gui);
 			}
			else if (serverlist.getSelectedServers().size() > 0)
 			// Normal double click on a server
 			{
 				new PropertiesWindow(gui, serverlist.getSelectedServers());
 			}
 		}
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
