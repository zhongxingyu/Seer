 package GUI;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 
 import javax.print.attribute.standard.MediaSize.Other;
 import javax.swing.*;
 
 public class ContainerRightClickMenu extends JPopupMenu
 {
 	private ContainerWindow main_window;
 	private Point pos;
 	
 	public ContainerRightClickMenu(ContainerWindow main_window)
 	{
 		super();
 		
 		this.main_window = main_window;
 		
 		JMenuItem item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.create_module(Engine.Constants.MODULE_OSCILLATOR);
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Create Oscillator");
 		add(item);
 		
 //		item = new JMenuItem(new AbstractAction()
 //		{
 //			public void actionPerformed(ActionEvent e)
 //			{
 //				ContainerRightClickMenu.this.create_module(Engine.Constants.MODULE_CONTAINER);
 //				ContainerRightClickMenu.this.hide();
 //			}
 //		}
 //		);
 //		item.setText("Create Container");
 //		add(item);
 		
 		item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.create_module(Engine.Constants.MODULE_CONSTANT);
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Create Constant");
 		add(item);
 		
 		item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.create_module(Engine.Constants.MODULE_COPYER);
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Create Copyer");
 		add(item);
 		
 		item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.create_module(Engine.Constants.MODULE_MERGER);
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Create Merger");
 		add(item);
 		
 		item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.main_window.engine.set_frequency(440, 554.365, 659.26); // A C# E (A chord)
 				ContainerRightClickMenu.this.main_window.engine.start_playing();
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Start Playing");
 		add(item);
 		
 		item = new JMenuItem(new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				ContainerRightClickMenu.this.main_window.engine.stop_playing();
 				ContainerRightClickMenu.this.hide();
 			}
 		}
 		);
 		item.setText("Stop Playing");
 		add(item);
 	}
 	
 	public void open(Point pos)
 	{
 		this.pos = pos;
 		setLocation(pos);
 		setVisible(true);
 	}
 	
 	public void hide()
 	{
 		setVisible(false);
 	}
 	
 	public void create_module(int type)
 	{
 		try
 		{
 			SwingUtilities.convertPointFromScreen(pos, main_window);
			pos.x -= main_window.getInsets().left;
			pos.y -= main_window.getInsets().top + main_window.menu.getHeight();
			main_window.add_module(type, pos.x, pos.y);
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
