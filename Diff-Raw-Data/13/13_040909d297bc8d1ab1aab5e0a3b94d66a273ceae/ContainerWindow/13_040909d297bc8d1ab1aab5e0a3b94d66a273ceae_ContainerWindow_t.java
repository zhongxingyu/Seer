 package GUI;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.swing.*;
 
 import ModuleGUIs.ConstantGUI;
 import ModuleGUIs.Output;
 
 public class ContainerWindow extends JFrame
 {
 	/**
 	 * No idea what this is
 	 */
 	private static final long serialVersionUID = 2466772835685193131L;
 	
 	public Engine.EngineMaster engine;
 	public Modules.Container container;
 	
 	// FIXME: Remove this
 	public LinkedList<ModuleGUI> module_list;
 	public JPanel central_container;
 	
 	private static final int DEFAULT_WINDOW_WIDTH = 900;
 	private static final int DEFAULT_WINDOW_HEIGHT = 600;
 	
 	private ContainerRightClickMenu popup_menu;
 	
	public JMenu menu;
 	
 	public ContainerOverlay overlay;
 	
 	private GlobalAL globalAL;
 	
 	public boolean is_connecting;
 	public PortGUI first_port;
 	
 	// Rightclick menu listener
 	class PopupListener extends MouseAdapter
 	{
 		public void mousePressed(MouseEvent e)
 		{
 			if (ContainerWindow.this.is_connecting)
 			{
 				ContainerWindow.this.is_connecting = false;
 				ContainerWindow.this.first_port = null;
 			}
 			else if (e.isPopupTrigger())
 			{
 				popup_menu.open(e.getLocationOnScreen());
 			}
 			else
 			{
 				popup_menu.hide();
 			}
 			
 			ModuleGUI m;
 			Iterator<ModuleGUI> i = module_list.listIterator();
 			while (i.hasNext())
 			{
 				m = i.next();
 				if (m instanceof ModuleGUIs.ConstantGUI)
 				{
 					// Yay hacks to prevent focus
 					requestFocusInWindow();
 				}
 			}
 			
 		}
 		
 		public void mouseReleased(MouseEvent e)
 		{
 			if (ContainerWindow.this.is_connecting)
 			{
 				ContainerWindow.this.is_connecting = false;
 				ContainerWindow.this.first_port = null;
 			}
 			else if (e.isPopupTrigger())
 			{
 				popup_menu.open(e.getLocationOnScreen());
 			}
 		}
 	}
 	
 	public ContainerWindow(Engine.EngineMaster engine) throws IOException
 	{
 		this(engine, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
 	}
 	
 	public ContainerWindow(Engine.EngineMaster engine, int window_width, int window_height) throws IOException
 	{
 		// We keep a pointer to the our container
 		container = engine.main_container;
 		this.engine = engine;
 		
 		module_list = new LinkedList<ModuleGUI>();
 
         // Basic GUI stuff
         setTitle("");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         // FIXME: Don't hardcode position
         setBounds(683 - window_width/2, 384 - window_height/2, window_width, window_height);
         setLayout(new BorderLayout());
         
         // TODO: Flesh out menu
         JMenuBar menu_bar;
         menu_bar = new JMenuBar();
         menu = new JMenu("Testing");
         menu.add(new JMenuItem("Mc Chicken"));
         menu_bar.add(menu);
         add(menu_bar, BorderLayout.PAGE_START);
 
         central_container = new JPanel();
         add(central_container, BorderLayout.CENTER);
         central_container.setLayout(null);
         
         // Rightclick menu event catcher
         central_container.addMouseListener(new PopupListener());
         popup_menu = new ContainerRightClickMenu(this);
         
         // Glass pane for drawing pipes
         overlay = new ContainerOverlay(this);
         setGlassPane(overlay);
 		overlay.setSize(this.getSize());
 		overlay.setLocation(new Point(0, 0));
 		overlay.setOpaque(false);
 		overlay.setVisible(true);
 		
 		globalAL = new GlobalAL(this);
 		getToolkit().addAWTEventListener(globalAL, AWTEvent.MOUSE_EVENT_MASK);
 		getToolkit().addAWTEventListener(globalAL, AWTEvent.MOUSE_MOTION_EVENT_MASK);
 
 		// FIXME
 		// This will eventually disappear
 		ModuleGUIs.Input m;
 		m = new ModuleGUIs.Input(this, this.container);
 		module_list.add(m);
 		central_container.add(m);
 		m.setLocation(300, 200);
 		
 		ModuleGUIs.Output m_gui;
 		m_gui = new ModuleGUIs.Output(this, this.container);
 		module_list.add(m_gui);
 		central_container.add(m_gui);
 		m_gui.setLocation(300, 300);
 	}
 	
 	public void add_module(int type, int x, int y) throws IOException
 	{
 		Engine.Module m = container.add_module(type);
 		ModuleGUI m_gui;
 		if (type == Engine.Constants.MODULE_CONSTANT)
 		{
 			m_gui = new ModuleGUIs.ConstantGUI(this, m);
 		}
 		else
 		{
 			m_gui = new ModuleGUIs.DefaultModuleGUI(this, m);
 		}
 		module_list.add(m_gui);
 		central_container.add(m_gui);
 		m_gui.setLocation(x, y);
 	}
 }
