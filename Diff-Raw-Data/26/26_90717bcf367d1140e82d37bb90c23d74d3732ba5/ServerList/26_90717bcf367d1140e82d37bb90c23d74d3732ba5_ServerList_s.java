 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import javax.swing.JList;
 import javax.swing.JViewport;
 import javax.swing.ListSelectionModel;
 
 /**
  * JList that displays JLabels as items in the list. This will allow me to
  * display an image next to the text (for server status).
  * 
  * @author ives
  * 
  */
 public class ServerList extends javax.swing.JList implements ComponentListener
 {
 	private static final long serialVersionUID = 1L;
 
 	ServerListModel serverListModel;
 
 	public ServerList(ServerListModel serverListModel)
 	{
 		super(serverListModel);
 		this.serverListModel = serverListModel;
 		setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		setVisibleRowCount(-1);
 		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		setCellRenderer(new ServerCellRenderer());
 		addComponentListener(this);
 	}
 
 	@Override
 	public void clearSelection()
 	{
 		setSelectedIndices(new int[0]);
 	}
 
 	@Override
 	public void componentResized(ComponentEvent e)
 	{
 		JList list = (JList) e.getSource();
 		JViewport viewport = (JViewport) list.getParent();
 
 		Dimension newSize = viewport.getExtentSize();
 		System.out.println(list.getInsets());
 		final int totalWidth = newSize.width - list.getInsets().left
 				- list.getInsets().right - 1;
 		final int serverCount = totalWidth / 200;
 
 		if (serverCount == 0)
 			return;
 
 		final int serverWidth = totalWidth / serverCount;
 
 		System.out.println("Total width = " + totalWidth);
 		System.out.println("Server width = " + serverWidth);
 		System.out.println("Server count = " + serverCount);
 
		ServerCellRenderer.serverWidth = serverWidth - 2 * serverCount;
 
 		list.revalidate();
 		list.repaint();
 
 		System.out.println(list);
 
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void componentShown(ComponentEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent e)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
