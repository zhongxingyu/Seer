 package edu.wpi.first.smartdashboard.gui;
 
 import edu.wpi.first.smartdashboard.StateManager;
 import edu.wpi.first.smartdashboard.gui.DisplayElementRegistry.NoElementsRegisteredForType;
 import edu.wpi.first.smartdashboard.gui.elements.BooleanBox;
 import edu.wpi.first.smartdashboard.gui.elements.BooleanPic;
 import edu.wpi.first.smartdashboard.gui.elements.Compass;
 import edu.wpi.first.smartdashboard.gui.elements.FormattedField;
 import edu.wpi.first.smartdashboard.gui.elements.LinePlot;
 import edu.wpi.first.smartdashboard.gui.elements.ProgressBar;
 import edu.wpi.first.smartdashboard.gui.elements.SimpleDial;
 import edu.wpi.first.smartdashboard.gui.elements.SpeedField;
 import edu.wpi.first.smartdashboard.state.Record;
 import edu.wpi.first.smartdashboard.gui.elements.TextBox;
 import edu.wpi.first.smartdashboard.gui.elements.TimeField;
 import edu.wpi.first.smartdashboard.gui.elements.VerticalProgressBar;
 import edu.wpi.first.smartdashboard.gui.elements.VideoBox;
 import edu.wpi.first.smartdashboard.gui.layout.LayoutAllocator;
 import edu.wpi.first.smartdashboard.gui.layout.LayoutAllocator.LayoutAllocation;
 import edu.wpi.first.smartdashboard.main;
 import edu.wpi.first.smartdashboard.types.Types;
 import edu.wpi.first.smartdashboard.util.StatefulDisplayElement;
 import edu.wpi.first.smartdashboard.util.IStateListener;
 import edu.wpi.first.smartdashboard.util.IStateUpdatable;
 import edu.wpi.first.smartdashboard.util.DisplayElement;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.NotSerializableException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.AbstractAction;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 /**
  *
  * @author pmalmsten
  */
 public class MainWindow extends JFrame implements IStateListener {
     public static final int LAYOUT_REGION_SIDE_LENGTH = 25;
 
     private static MainWindow instance = null;
     private JPanel m_elemPanel;
     private StatusBar m_statusBar;
     private StateManager m_stateMan;
     boolean dragging = false;
     Component glassPane;
     Component contentPane;
     JMenuBar menuBar;
     JPopupMenu popupMenu;
     DisplayElement selectedElement;
     PropertyEditor propEditor = null;
     JMenu changeToMenu;
     private static MenuListener disableGlassPaneOnMenu;
 
     /**
      * Initializes the singleton MainWindow
      */
     public static void init(StateManager stateMan) {
 	if (instance == null) {
 	    instance = new MainWindow(stateMan);
 	}
     }
 
     private void setupChangeToPopupMenu() {
 	if (selectedElement instanceof StatefulDisplayElement) {
 	    List<Class> choices;
 	    Record record = ((StatefulDisplayElement) selectedElement).getRecord();
 	    try {
 		choices = DisplayElementRegistry.elementsForType(record.getType());
 	    } catch (NoElementsRegisteredForType ex) {
 		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 		changeToMenu.setEnabled(false);
 		return; // no choices - make the ChangeTo menu item insensitive
 	    }
 	    changeToMenu.removeAll();
 	    for (Class c : choices) {
 		changeToMenu.add(new ChangeToAction(c.getSimpleName(), c));
 	    }
 	}
 
 	// all all the classes from "choices" to the menu
 
     }
 
     /**
      * Create the menu bar and submenus for the dashboard
      *
      * @return menuBar returns a menubar that can be added to the frame
      */
     private JMenuBar createMenu() {
 	JMenuBar customMenuBar = new JMenuBar();
 	JMenu fileMenu = new JMenu("File");
 
 	fileMenu.addMenuListener(disableGlassPaneOnMenu);
 
 	JMenuItem loadMenu = new JMenuItem("Load");
 	loadMenu.addActionListener(new ActionListener() {
 
 	    public void actionPerformed(ActionEvent ae) {
 		MainWindow.load();
 	    }
 	});
 	fileMenu.add(loadMenu);
 	JMenuItem saveMenu = new JMenuItem("Save");
 	saveMenu.addActionListener(new ActionListener() {
 
 	    public void actionPerformed(ActionEvent ae) {
 		MainWindow.save();
 	    }
 	});
 	fileMenu.add(saveMenu);
 
 	JMenuItem preferencesMenu = new JMenuItem(new PreferencesAction("Preferences..."));
 
 	fileMenu.add(preferencesMenu);
 
 	JMenuItem exitMenu = new JMenuItem("Exit");
 	exitMenu.addActionListener(new ActionListener() {
 
 	    public void actionPerformed(ActionEvent ae) {
 		MainWindow.exit();
 	    }
 	});
 	fileMenu.add(exitMenu);
 
         JMenu viewMenu = new JMenu("View");
         viewMenu.addMenuListener(disableGlassPaneOnMenu);
         
         JCheckBoxMenuItem snapToGridChckbox = new JCheckBoxMenuItem("Snap to Grid");
         snapToGridChckbox.setState(DashboardPrefs.getInstance().getSnapToGrid());
         snapToGridChckbox.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 DashboardPrefs p = DashboardPrefs.getInstance();
 
                 p.setSnapToGrid(!p.getSnapToGrid());
             }
         });
         viewMenu.add(snapToGridChckbox);
 
 	JMenuItem normalSizeMenu = new JMenuItem("Normal Size");
 	normalSizeMenu.addActionListener(new ActionListener() {
 		
 		public void actionPerformed(ActionEvent ae) {
 		    setSize(800, 480);
 		    System.out.println("Normal Size...");
 		}
 	    });
 	viewMenu.add(normalSizeMenu);
 
 	JMenuItem addLabelMenu = new JMenuItem("Add Label");
 	addLabelMenu.addActionListener(new ActionListener() {
 		
 		public void actionPerformed(ActionEvent ae) {
 		    System.out.println("Adding a label...");
 		    addField(new FormattedLabel());
 		}
 	    });
 	viewMenu.add(addLabelMenu);
 		
 	customMenuBar.add(fileMenu);
         customMenuBar.add(viewMenu);
 	return customMenuBar;
     }
 
     /**
      * Creates a Swing GUI on which all other UI elements are placed
      */
     private MainWindow(final StateManager stateMan) {
         final DashboardPrefs prefs = DashboardPrefs.getInstance();
         LayoutAllocator.init(LAYOUT_REGION_SIDE_LENGTH, prefs.getWidth(), prefs.getHeight());
 
 	// initialize the registry with all the UI classes
 	// for now, you must do this for every UI class
         DisplayElementRegistry.register(BooleanBox.getSupportedTypes(), BooleanBox.class);
 	DisplayElementRegistry.register(BooleanPic.getSupportedTypes(), BooleanPic.class);
 	DisplayElementRegistry.register(FormattedField.getSupportedTypes(), FormattedField.class);
 	DisplayElementRegistry.register(TimeField.getSupportedTypes(), TimeField.class);
 	DisplayElementRegistry.register(SpeedField.getSupportedTypes(), SpeedField.class);
 	DisplayElementRegistry.register(SimpleDial.getSupportedTypes(), SimpleDial.class);
         DisplayElementRegistry.register(Compass.getSupportedTypes(), Compass.class);
         DisplayElementRegistry.register(LinePlot.getSupportedTypes(), LinePlot.class);
         DisplayElementRegistry.register(TextBox.getSupportedTypes(), TextBox.class);
 	DisplayElementRegistry.register(ProgressBar.getSupportedTypes(), ProgressBar.class);
 	DisplayElementRegistry.register(VerticalProgressBar.getSupportedTypes(), VerticalProgressBar.class);
 	DisplayElementRegistry.register(Types.Type.NONE, VideoBox.class);
 
 	m_stateMan = stateMan;
 
 	stateMan.registerForAnnouncements(this);
 	final DragListener dragListener = new DragListener();
 
 	EventQueue.invokeLater(new Runnable() {
             
 	    @Override
 	    public void run() {
 		
 		// create context menu for right-click on DisplayElement
 		popupMenu = new JPopupMenu();
 		JMenuItem propertiesItem = new JMenuItem(new PropertiesItemAction("Properties..."));
 		changeToMenu = new JMenu("Change to...");
 		//JMenuItem hideItem = new JMenuItem(new HideItemAction("Hide"));
 		//popupMenu.add(hideItem);
 		popupMenu.add(changeToMenu);
 		popupMenu.add(propertiesItem);
 
 		glassPane = getGlassPane();
 		contentPane = getContentPane();
 		glassPane.addMouseListener(dragListener);
 		glassPane.addMouseMotionListener(dragListener);
 		glassPane.setVisible(true);
 
                 disableGlassPaneOnMenu = new MenuListener() {
                     public void menuSelected(MenuEvent e) {
                         glassPane.setVisible(false);
                     }
 
                     public void menuDeselected(MenuEvent e) {
                         glassPane.setVisible(true);
                     }
 
                     public void menuCanceled(MenuEvent e) {
                         glassPane.setVisible(true);
                     }
                 };
 
                 popupMenu.addPopupMenuListener(new PopupMenuListener() {
                     public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                         glassPane.setVisible(false);
                     }
 
                     public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        glassPane.setVisible(true);
                     }
 
                     public void popupMenuCanceled(PopupMenuEvent e) {
                         glassPane.setVisible(true);
                     }
                 });
 
 		m_elemPanel = new JPanel();
 		m_elemPanel.setLayout(null);
 
 		m_statusBar = new StatusBar();
 		stateMan.notifyOnBadData(m_statusBar);
 		stateMan.notifyOnGoodData(m_statusBar);
 
 		menuBar = createMenu();
 		setJMenuBar(menuBar);
 
 		// Final Preparations
 		setMinimumSize(new Dimension(300, 200));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 			    MainWindow.exit();
 			}
 		    });
                 
 		setPreferredSize(new Dimension(prefs.getWidth(), prefs.getHeight()));
 		MainWindow.this.setLocation(prefs.getX(), prefs.getY());
 		addComponentListener(new ComponentListener() {
 
 		    public void componentResized(ComponentEvent ce) {
                         int newWidth = LayoutAllocator.restrictedWidthResize(MainWindow.this.getWidth());
                         int newHeight = LayoutAllocator.restrictedHeightResize(MainWindow.this.getHeight());
 
                         setSize(newWidth, newHeight);
 
 			prefs.setHeight(newHeight);
 			prefs.setWidth(newWidth);
 		    }
 
 		    public void componentMoved(ComponentEvent ce) {
 			prefs.setX(MainWindow.this.getX());
 			prefs.setY(MainWindow.this.getY());
 		    }
 
 		    public void componentShown(ComponentEvent ce) {
 		    }
 
 		    public void componentHidden(ComponentEvent ce) {
 		    }
 		});
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(m_elemPanel, BorderLayout.CENTER);
 		getContentPane().add(m_statusBar, BorderLayout.SOUTH);
 
 		pack();
 		setVisible(true);
 	    }
 	});
     }
 
     /**
      * Returns the MainWindow singleton
      * @return The MainWindow singleton
      */
     public static MainWindow getInstance() {
 	return instance;
     }
 
     /**
      * Saves the current state of this MainWindow and any significant objects
      * it contains.
      */
     public static void save() {
 	FileOutputStream fh = null;
 	try {
 	    String path = System.getProperty("user.home") + File.separator + "SmartDashboard.serialized";
 	    fh = new FileOutputStream(path);
 	    ObjectOutputStream objOut = new ObjectOutputStream(fh);
 	    getInstance().saveState(objOut);
 	    objOut.close();
 	} catch (NotSerializableException ex) {
 	    ex.printStackTrace();
 	} catch (FileNotFoundException ex) {
 	    ex.printStackTrace();
 	} catch (IOException ex) {
 	    ex.printStackTrace();
 	}
 
     }
 
     /**
      * Ask to save, then exit
      */
     public static void exit() {
 	int result = JOptionPane.showConfirmDialog(
 			 MainWindow.getInstance().contentPane,
 		         new String[] {"Do you wish to save this layout?"},
 			 "Save before quiting?",
 			 JOptionPane.YES_NO_CANCEL_OPTION,
 			 JOptionPane.WARNING_MESSAGE);
 
 	if (result == JOptionPane.YES_OPTION) {
 	    MainWindow.save();
 	    System.exit(0);
 	} else if (result == JOptionPane.NO_OPTION) {
 	    System.exit(0);
 	}
     }
 
 
     /**
      * Writes this object's and any containted objects' important state
      * information to the given ObjectOutputStream.
      * @param objOut The stream to write to.
      * @throws IOException
      */
     public void saveState(ObjectOutputStream objOut) throws IOException {
 	objOut.writeObject(m_elemPanel);
 	m_stateMan.saveState(objOut);
         LayoutAllocator.saveState(objOut);
     }
 
     /**
      * Loads the current state of this MainWindow and any significant objects
      * it contains
      */
     public static void load() {
 	FileInputStream fh = null;
 	try {
 	    String path = System.getProperty("user.home") + File.separator + "SmartDashboard.serialized";
 	    fh = new FileInputStream(path);
 	    ObjectInputStream objIn = new ObjectInputStream(fh);
             main.prepareForSerializationLoad();
 	    getInstance().loadState(objIn);
 	    objIn.close();
 	} catch (FileNotFoundException ex) {
 	    ex.printStackTrace();
 	} catch (IOException ex) {
 	    ex.printStackTrace();
 	} catch (ClassNotFoundException ex) {
 	    ex.printStackTrace();
 	} finally {
             main.finalizeSerializationLoad();
         }
     }
 
     /**
      * Loads this object's and any containted objects' important state
      * information from the given ObjectInputStream.
      * @param objIn The stream to read.
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void loadState(ObjectInputStream objIn) throws ClassNotFoundException, IOException {
 	final JPanel newElemPanel = (JPanel) objIn.readObject();
 
 	EventQueue.invokeLater(new Runnable() {
 
 	    public void run() {
 		remove(m_elemPanel);
 		add(newElemPanel, BorderLayout.CENTER);
 		m_elemPanel = newElemPanel;
 		m_elemPanel.revalidate();
 		m_elemPanel.repaint();
 	    }
 	});
 
 	m_stateMan.loadState(objIn);
         LayoutAllocator.loadState(objIn);
     }
 
     public void addField(DisplayElement elem) {
         Dimension size = elem.getPreferredSize();
         LayoutAllocation la = LayoutAllocator.allocate(size.width, size.height);
         addField(elem, la);
     }
 
     public void addField(final DisplayElement elem, final LayoutAllocation la) {
 	EventQueue.invokeLater(new Runnable() {
 
 	    public void run() {
 		elem.init();
                 Dimension size = elem.getPreferredSize();
 
                 if(la != null) {
                     elem.setLayoutAllocation(la);
                     m_elemPanel.add(elem);
                     elem.setSize(size.width, size.height);
                     elem.setLocation(la.point);
                     m_elemPanel.revalidate();
                     m_elemPanel.repaint();
                 } else {
                     elem.disconnect();
                     System.err.println("Unable to allocate new DisplayElement of width " +
                                        size.width + " pixels and height " + size.height +
                                        " pixels.");
                 }
 	    }
 	});
     }
 
     /**
      * Implements IStateListener
      */
     public IStateUpdatable newField(final Record r) {
 //        List<IDisplayElementFactory> l;
 //        final IDisplayElementFactory e;
 	try {
 	    List<Class> choices = DisplayElementRegistry.elementsForType(r.getType());
 	    final StatefulDisplayElement elem = (StatefulDisplayElement) choices.get(0).newInstance();
 	    elem.setFieldName(r.getName());
 	    elem.setRecord(r);
 
 	    addField(elem);
 
 	    return elem;
 
 	} catch (Exception ex) {
 	    ex.printStackTrace();
 	    EventQueue.invokeLater(new Runnable() {
 
 		public void run() {
 		    JOptionPane.showMessageDialog(null,
 			    "Something went wrong creating a field whose type was: " + r.getType(),
 			    "Unrecognized Type Received",
 			    JOptionPane.WARNING_MESSAGE);
 		}
 	    });
 	    return null;
 	}
     }
 
     class DragListener implements MouseMotionListener, MouseListener {
 
 	Component dragTarget;
 	Point dragPosition;
         Point dragPositionIntegrated;
 
 	private void captureMouse(MouseEvent e) {
 	    dragging = false;
 	    Point glassPanePoint = e.getPoint();
 	    dragPosition = SwingUtilities.convertPoint(glassPane,
 		    glassPanePoint, m_elemPanel);
 
 	    // In menu bar?
 	    if (dragPosition.y <= 0 && (dragPosition.y + menuBar.getHeight()) >= 0) {
 		Point menuBarPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, menuBar);
 		Component component = SwingUtilities.getDeepestComponentAt(menuBar, menuBarPoint.x, menuBarPoint.y);
 
 		if (component != null) {
 		    Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
 		    component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(),
 			    e.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
 		}
 		return;
 	    }
 
 	    // Not in menu bar
 	    dragTarget = SwingUtilities.getDeepestComponentAt(m_elemPanel, dragPosition.x, dragPosition.y);
 
 	    // Get whole DisplayElement for dragging instead of individual components
 	    while (dragTarget != null && !(dragTarget instanceof DisplayElement)) {
 		dragTarget = dragTarget.getParent();
 	    }
 
 	    if (dragTarget != null) {
 		if (e.isPopupTrigger()) {
 		    if (dragTarget instanceof DisplayElement) {
 			selectedElement = (DisplayElement) dragTarget;
 			setupChangeToPopupMenu();
 			popupMenu.show(MainWindow.this, e.getX(), e.getY());
 		    }
 		} else {
 		    if (e.getClickCount() == 2 && dragTarget instanceof StatefulDisplayElement) {
 			selectedElement = (StatefulDisplayElement) dragTarget;
 			if (propEditor != null) {
 			    propEditor.setDisplayElement(selectedElement);
 			}
 		    } else {
 			dragging = true;
                         dragPositionIntegrated = dragTarget.getLocation();
 		    }
 		}
 		return;
 	    }
 	}
 
 	public void mouseDragged(MouseEvent me) {
 	    if (!dragging) {
 		return;
 	    }
 	    Point clickPoint = SwingUtilities.convertPoint(glassPane, me.getPoint(), m_elemPanel);
             
 
             int xoffset = clickPoint.x - dragPosition.x;
 	    int yoffset = clickPoint.y - dragPosition.y;
             
             dragPositionIntegrated.x += xoffset;
             dragPositionIntegrated.y += yoffset;
 
             if(DashboardPrefs.getInstance().getSnapToGrid()) {
                 dragTarget.setLocation(
                         LayoutAllocator.floorCoordinateToRegion(dragPositionIntegrated.x),
                         LayoutAllocator.floorCoordinateToRegion(dragPositionIntegrated.y));
             } else {
                 dragTarget.setLocation(dragPositionIntegrated);
             }
 
             dragPosition = clickPoint;
 	}
 
 	public void mouseMoved(MouseEvent me) {
 	}
 
 	public void mouseClicked(MouseEvent me) {
 	}
 
 	public void mousePressed(MouseEvent me) {
 	    captureMouse(me);
 	}
 
 	public void mouseReleased(MouseEvent me) {
 	    if(dragging) {
 		Point clickPoint = SwingUtilities.convertPoint(glassPane, me.getPoint(), m_elemPanel);
 		DisplayElement dragTargetDE = (DisplayElement) dragTarget;
 
 		int xoffset = clickPoint.x - dragPosition.x;
 		int yoffset = clickPoint.y - dragPosition.y;
 		Point newLocation = new Point(xoffset + dragTarget.getX(), yoffset + dragTarget.getY());
 		
 		// This code assumes that dragTargetDE.getLayoutAllocation() will never be null since
 		// objects are put on the screen (and are this draggable) only if they were assigned an allocation
 		LayoutAllocation newLa = LayoutAllocator.forceMoveAllocation(dragTargetDE.getLayoutAllocation(), newLocation);
 		dragTargetDE.setLayoutAllocation(newLa);
 		dragTargetDE.setLocation(newLa.point);
 	    }
 	    dragging = false;
             captureMouse(me);
 	}
 
 	public void mouseEntered(MouseEvent me) {
 	    dragging = false;
 	}
 
 	public void mouseExited(MouseEvent me) {
 	    dragging = false;
 	}
     }
 
     class PreferencesAction extends AbstractAction {
 
 	PreferencesDialog preferencesDialog = null;
 
 	private PreferencesAction(String string) {
 	    super(string);
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 	    if (preferencesDialog == null) {
 		preferencesDialog = new PreferencesDialog(MainWindow.this);
 	    }
             preferencesDialog.updateFromPreferences();
 	    preferencesDialog.setVisible(true);
 	}
     }
 
     /**
      * Implement the popup menu items for right-clicking on a display element
      */
     /**
      * Hide a display element from view.
      * The display element is removed from the list of updated items for the
      * associated Record object and the element is removed from it's parent.
      */
     class HideItemAction extends AbstractAction {
 
 	private HideItemAction(String string) {
 	    super(string);
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 	    if (selectedElement instanceof StatefulDisplayElement) {
 		System.out.println("Removing: " +
 				   ((StatefulDisplayElement) selectedElement)
 				                                .getFieldName());
 	    }
 	    selectedElement.disconnect();
             selectedElement.getLayoutAllocation().deallocate();
 	    selectedElement.getParent().remove(selectedElement);
 	    m_elemPanel.revalidate();
 	    m_elemPanel.repaint();
 	}
     }
 
     /**
      * Display the properties for a display element.
      * The properties are displayed for a display element so they can be
      * viewed and updated.
      */
     class PropertiesItemAction extends AbstractAction {
 
 	private PropertiesItemAction(String string) {
 	    super(string);
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 	    if (propEditor == null) {
 		propEditor = new PropertyEditor(MainWindow.this);
 	    }
 	    propEditor.setDisplayElement(selectedElement);
 	    propEditor.setVisible(true);
 	}
     }
 
     /**
      * Change a display element to another display element type.
      * First, delete the item, then create a new one at the same position
      * and connected to the same record.
      */
     class ChangeToAction extends AbstractAction {
 	Class elementClass;
 
 	private ChangeToAction(String string, Class elementClass) {
 	    super(string);
 	    this.elementClass = elementClass;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 	    if (selectedElement instanceof StatefulDisplayElement) {
 		final StatefulDisplayElement newElement;
 		Record record = ((StatefulDisplayElement) selectedElement).getRecord();
 		final Point location = new Point(selectedElement.getLocation());
 		selectedElement.disconnect();
 		selectedElement.getLayoutAllocation().deallocate();
 		selectedElement.getParent().remove(selectedElement);
 		try {
 		    newElement = (StatefulDisplayElement) elementClass.newInstance();
 		} catch (InstantiationException ex) {
 		    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 		    return;
 		} catch (IllegalAccessException ex) {
 		    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 		    return;
 		}
 		
 		newElement.setFieldName(record.getName());
 		newElement.setRecord(record);
 
 		Dimension size = newElement.getPreferredSize();
 		LayoutAllocation la = LayoutAllocator.allocate(location, size.width, size.height);
 
 		// If the old object's position can't be reclaimed, find a new one
 		if(la == null)
 		    la = LayoutAllocator.allocate(location, size.width, size.height);
             
 		record.addStateReceiver(newElement);
 		addField(newElement, la);
 
 		if(record.getValue() != null)
 		    newElement.update(record);
 	    }
 	}
     }
 
     public void redrawDisplayElements() {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 m_elemPanel.revalidate();
                 m_elemPanel.repaint();
             }
         });
     }
 }
