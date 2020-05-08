 package ogx;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.border.TitledBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import ogx.control.Control;
 import ogx.model.MethodListModel;
 import ogx.model.ImageModel;
 import ogx.model.ImagesStruct;
 import ogx.model.MethodModel;
 import ogx.view.ImageLabel;
 import ogx.view.JSONFilter;
 import ogx.view.MethodListView;
 import ogx.view.MethodSelection;
 
 public class Main implements MouseListener, 
 							 ActionListener, 
 							 DragGestureListener,
 							 DragSourceListener,
 							 DropTargetListener {
 	
 	ImagesStruct images_struct = new ImagesStruct();
 	Control controller = null;
 	JFrame frame = new JFrame("Calculation2D");
 	JMenuBar bar = new JMenuBar();
 	JToolBar toolbar = new JToolBar();
 	JTree treeview = new JTree(images_struct.getRoot());
 	ImageLabel imagelabel = new ImageLabel();
 	JLabel histogramlabel = new JLabel();
 	Dimension imagelabelsize = new Dimension(635, 475);
 	JPanel treepanel = new JPanel(new BorderLayout());
 	JPanel imagepanel = new JPanel(new BorderLayout());
 	JPanel calcpanel = new JPanel(new GridLayout(10, 1, 10, 10));
 	JPanel topcalcpanel = new JPanel();
 	JPanel bottomcalcpanel = new JPanel();
 	JPanel paramspanel = new JPanel();
 	JList<MethodModel> methodlist = new JList<MethodModel>();
 	JList<MethodModel> methodsequence = new JList<MethodModel>();
 	JButton runbutton = new JButton("Run", new ImageIcon("../resources/run.png"));
 	ImageModel current_image = null;
 	MethodListView listview = null;
 	DragSource dragsource;
 	DropTarget droptarget;
 	
 	static final String load_label = "Load";
 	static final String save_label = "Save";
 	static final String set_roi_label = "Set region of interest";
 	static final String clear_roi_label = "Clear region of interest";
 	static final String zoomin_label = "Zoom in";
 	static final String zoomout_label = "Zoom out";
 	static final String fitview_label = "Fit to window";
 	static final String nominal_label = "Nominal size (1:1)";
 	static final String delete_node = "Delete selected node from structure";
 	
 	Main() {
 		setupGUI();
 		dragsource = new DragSource();
 	    dragsource.createDefaultDragGestureRecognizer(methodlist, DnDConstants.ACTION_COPY, this);
 	    droptarget = new DropTarget(methodsequence, this);
 	  }
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Main gui = new Main();
 		//System.err.println(System.getProperty("user.dir"));
 		//System.err.println(args[0]);
 		if (gui.setupController(args)) {
 			gui.setupAndShowWindow();
 		}
 		else {
 			dispErrorMessage("Could not start server 'calc2d.exe'. Did you forget to build it?");
 		}
 	}
 	
 	public static void dispErrorMessage(String error_message) {
 		JOptionPane.showMessageDialog(null, error_message, "Error", JOptionPane.ERROR_MESSAGE);
 	}
 	
 	public void setupAndShowWindow() {
 		// set up frame
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    frame.addWindowListener(new java.awt.event.WindowAdapter() {
 	        public void windowClosing(WindowEvent winEvt) {
 	            if (controller.disconnectCalcServer()) System.exit(0);
 	            dispErrorMessage(controller.getLastError());
 	            System.exit(1);
 	        }
 	    });
 
 	    frame.setJMenuBar(bar);
 	    frame.setLayout(new BorderLayout());
 	    frame.getContentPane().add(toolbar, BorderLayout.NORTH);
 	    frame.getContentPane().add(treepanel, BorderLayout.WEST);
 	    frame.getContentPane().add(imagepanel, BorderLayout.CENTER);
 	    frame.getContentPane().add(calcpanel, BorderLayout.EAST);
 	    frame.getContentPane().add(new JLabel("Future progress bar", JLabel.CENTER), BorderLayout.SOUTH);
 	    frame.pack();
 	    frame.setVisible(true);
 	}
 	
 	public boolean setupController(String[] args) {	
 		controller = new Control(images_struct);
 		if (controller.connectServer(args[0])) {
 		    MethodListModel methods = controller.getMethods();
 		    String last_error = controller.getLastError();
 		    if (last_error.equals("")) {
 			    MethodListModel sequence = new MethodListModel();
 				methodlist.setModel(methods);
 				methodsequence.setModel(sequence);
 			
 				listview = new MethodListView(sequence, paramspanel, images_struct);
 				methodsequence.addListSelectionListener(listview);
 				listview.bindList(methodsequence);
 				methodlist.setSelectedIndex(0);
 				return true;
 		    }
 		    else {
 		    	dispErrorMessage(last_error);
 		    }
 		}
 		return false;
 	}
 	
 	private void setupGUI() {
 		// create menu
 	    JMenu file = new JMenu("File");
 		JMenu view = new JMenu("View");
 		JMenu tools = new JMenu("Tools");
 		JMenu help = new JMenu("Help");
 	    
 	    // fill menu file
 	    file.add(addMenuItem(load_label));
 	    file.add(addMenuItem(save_label));
 	    file.addSeparator();
 	    file.add(new AbstractAction("Exit") {
 					private static final long serialVersionUID = 1L;
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						System.exit(0);
 					}
 	    		}
 	        	);
 	    bar.add(file);
 	    
 	    // fill menu view
 	    view.add(addMenuItem(zoomin_label));
 	    view.add(addMenuItem(zoomout_label));
 	    view.add(addMenuItem(fitview_label));
 	    view.add(addMenuItem(nominal_label));
 	    bar.add(view);
 	    
 	    // fill menu tools
 	    tools.add(addMenuItem(set_roi_label));
 	    tools.add(addMenuItem(clear_roi_label));
 	    tools.add(addMenuItem(delete_node));
 	    bar.add(tools);
 
 	    // fill menu help
 	    help.add(new JMenuItem("About"));
 	    bar.add(help);
 	    
 	 // fill  toolbar
 	    toolbar.add(addToolbarButton(load_label, new ImageIcon("../resources/load.png")));
 	    toolbar.add(addToolbarButton(save_label, new ImageIcon("../resources/save.png")));
 	    toolbar.addSeparator();
 	    toolbar.add(addToolbarButton(zoomin_label, new ImageIcon("../resources/zoomin.png")));
 	    toolbar.add(addToolbarButton(zoomout_label, new ImageIcon("../resources/zoomout.png")));
 	    toolbar.add(addToolbarButton(fitview_label, new ImageIcon("../resources/fitview.png")));
 	    toolbar.add(addToolbarButton(nominal_label, new ImageIcon("../resources/nominalview.png")));
 	    toolbar.addSeparator();
 	    toolbar.add(addToolbarButton(set_roi_label, new ImageIcon("../resources/setroi.png")));
 	    toolbar.add(addToolbarButton(clear_roi_label, new ImageIcon("../resources/clearroi.png")));
 	    toolbar.add(addToolbarButton(delete_node, new ImageIcon("../resources/remove.png")));
 	    toolbar.setFloatable(false);
 	    toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 	    
 	    // set up tree-view
 	    //treeview.setMinimumSize(new Dimension(256, 200));
 	    //treeview.setPreferredSize(new Dimension(256, 375));
 	    treeview.addMouseListener(this);
 	    treeview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 	    
 	    // set up histogram label
 	    histogramlabel.setMaximumSize(new Dimension(256, 100));
 	    histogramlabel.setPreferredSize(new Dimension(256, 100));
 	    	
 	    // set up label for image display
 	    imagelabel.setMinimumSize(new Dimension(200, 200));
 	    imagelabel.setPreferredSize(imagelabelsize);
 	    imagelabel.setImageDimension(imagelabelsize);
 	    
 	    // set up components for executing methods
 	    methodlist.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    methodsequence.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    methodsequence.addMouseListener(this);
 	    runbutton.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    runbutton.addActionListener(this);
 	    runbutton.setActionCommand("run_method");
 	    
 	    // set up panels for layouts
 	    JPanel histogrampanel = new JPanel();
 	    histogrampanel.setBorder(BorderFactory.createTitledBorder("Current histogram"));
 	    ((TitledBorder)histogrampanel.getBorder()).setTitleJustification(TitledBorder.CENTER);
 	    histogrampanel.add(histogramlabel);
 	    treepanel.add(treeview, BorderLayout.CENTER);
 	    treepanel.add(histogrampanel, BorderLayout.SOUTH);
 	    treepanel.add(new JScrollPane(treeview));
 	    treepanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 	    imagepanel.setMinimumSize(new Dimension(300, 300));
 	    imagepanel.setPreferredSize(new Dimension(640, 480));
 	    imagepanel.add(imagelabel);
 	    imagepanel.add(new JScrollPane(imagelabel));
 	    
 	    BoxLayout boxlayout = new BoxLayout(topcalcpanel, BoxLayout.Y_AXIS);
 	    topcalcpanel.setLayout(boxlayout);
 	    topcalcpanel.setPreferredSize(new Dimension(300, 300));
 	    topcalcpanel.setBorder(BorderFactory.createTitledBorder("Operations"));
 	    ((TitledBorder)topcalcpanel.getBorder()).setTitleJustification(TitledBorder.CENTER);
 	    
 	    Box verticalBox = Box.createVerticalBox();
 	    
 	    JPanel buttons_panel = new JPanel(new GridLayout(1,3));
 	    JPanel methodlist_panel = new JPanel(new BorderLayout());
 	    JPanel methodsequence_panel = new JPanel(new BorderLayout());
 
 	    JButton load_sequence_btn = new JButton("Load");
 	    JButton save_sequence_btn = new JButton("Save");
 	    load_sequence_btn.addActionListener(this);
 	    load_sequence_btn.setActionCommand("load_sequence");
 	    save_sequence_btn.addActionListener(this);
 	    save_sequence_btn.setActionCommand("save_sequence");
 	    
 	    buttons_panel.add(runbutton);
 	    buttons_panel.add(load_sequence_btn);
 	    buttons_panel.add(save_sequence_btn);
 	    
 	    methodlist_panel.add(new JLabel("Available calculation methods:"), BorderLayout.NORTH);
 	    methodlist_panel.add(new JScrollPane(methodlist), BorderLayout.CENTER);
 	    
 	    methodsequence_panel.add(new JLabel("Current calculation sequence:"), BorderLayout.NORTH);
 	    methodsequence_panel.add(new JScrollPane(methodsequence), BorderLayout.CENTER);
 	    
 	    verticalBox.add(methodlist_panel);
 	    verticalBox.add(Box.createVerticalStrut(5));
 	    verticalBox.add(methodsequence_panel);
 	    verticalBox.add(Box.createVerticalStrut(5));
 	    verticalBox.add(buttons_panel);
 	    verticalBox.add(Box.createVerticalStrut(5));
 	    
 	    topcalcpanel.add(verticalBox);
 	    
 //	    topcalcpanel.add(new JLabel("Available calculation methods:"));
 //	    topcalcpanel.add(methodlist);
 //	    topcalcpanel.add(new JLabel("Current calculation sequence:"));
 //	    topcalcpanel.add(methodsequence);
 	    //topcalcpanel.add(new JScrollPane(methodlist));
 	    //topcalcpanel.add(new JScrollPane(methodsequence));
 	    //topcalcpanel.add(Box.createVerticalStrut(10));
 	    //topcalcpanel.add(runbutton);
 	    //topcalcpanel.add(Box.createVerticalStrut(10));
 	    
 	    boxlayout = new BoxLayout(bottomcalcpanel, BoxLayout.Y_AXIS);
 	    bottomcalcpanel.setLayout(boxlayout);
 	    bottomcalcpanel.add(paramspanel);
 	    JScrollPane paramscroll = new JScrollPane(paramspanel);
 	    paramscroll.setBorder(BorderFactory.createEmptyBorder());
 	    bottomcalcpanel.add(paramscroll);
 	    
 	    calcpanel.setLayout(new BorderLayout());
 	    calcpanel.setPreferredSize(new Dimension(300, 480));
 	    calcpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 	    calcpanel.add(topcalcpanel, BorderLayout.NORTH);
 	    calcpanel.add(bottomcalcpanel, BorderLayout.CENTER);
 	}
 	
 	private JMenuItem addMenuItem(String name) {
 		JMenuItem item = new JMenuItem(name);
 		item.setActionCommand(name);
 		item.addActionListener(this);
 		return item;
 	}
 	
 	private JButton addToolbarButton(String name, ImageIcon icon) {
 		JButton button = new JButton("");
 		button.setToolTipText(name);
 		button.setActionCommand(name);
 		button.setIcon(icon);
 		button.addActionListener(this);
 		return button;
 	}
 
 	// MouseListener
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		int button = arg0.getButton();
 		if (arg0.getComponent().equals(methodsequence)) {
 			if (button == MouseEvent.BUTTON3) {
 				methodsequence.setSelectedIndex( methodsequence.locationToIndex(arg0.getPoint()) );
 				int selected_index = methodsequence.getSelectedIndex();
 				((MethodListModel)methodsequence.getModel()).removeElement(selected_index);
 				listview.removeMethodModelView(selected_index);
 				methodsequence.clearSelection();
 				methodsequence.updateUI();
 				//paramspanel.updateUI();
 			}
 		}
 	}
 
 	// MouseListener
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		
 	}
 
 	// MouseListener
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		
 	}
 
 	// MouseListener
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		
 	}
 
 	// MouseListener
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		int button = arg0.getButton();
 		if (arg0.getComponent().equals(treeview)) {
 			treeview.setSelectionPath(treeview.getClosestPathForLocation(arg0.getX(), arg0.getY()));
 			DefaultMutableTreeNode chosen_node = (DefaultMutableTreeNode)treeview.getLastSelectedPathComponent();
 			if (!chosen_node.equals(images_struct.getRoot())) {
 				switch (button) {
 				case MouseEvent.BUTTON1 :
 					current_image = (ImageModel) chosen_node.getUserObject();
 					imagelabel.setImage(current_image.getImage());
 					imagelabel.updateUI();
 					
 					// update histogram view
 					histogramlabel.setIcon(new ImageIcon(current_image.getHistogramImage()));
 					histogramlabel.updateUI();
 					
 				break;
 //				case MouseEvent.BUTTON3 :
 //					DefaultMutableTreeNode parent_node = (DefaultMutableTreeNode)chosen_node.getParent();
 //					if (chosen_node.getLevel() > 0) {
 //						if (chosen_node.getChildCount() > 0) {
 //							chosen_node.removeAllChildren();
 //						}
 //						parent_node.remove(chosen_node);
 //						treeview.clearSelection();
 //					}
 //					treeview.updateUI();
 //					imagelabel.setImage(null);
 //					imagelabel.updateUI();
 //					histogramlabel.setIcon(null);
 //					histogramlabel.updateUI();
 //					listview.update();
 //				break;
 				}
 			}	
 			else {
 				imagelabel.setImage(null);
 				imagelabel.updateUI();
 				histogramlabel.setIcon(null);
 				histogramlabel.updateUI();
 			}
 		}
 	}
 
 	// ActionListener
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if (command == load_label) {
 			javax.swing.filechooser.FileFilter ff = new ogx.view.ImageFilter();
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(ff);
 			int returnVal = fc.showOpenDialog(fc);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        	if (!controller.loadImage(fc.getSelectedFile().getPath())) {
 	        		dispErrorMessage(controller.getLastError());
 	        	}
 
 	        	treeview.updateUI();
        		treeview.expandRow(treeview.getRowCount()-1);
         		treeview.setSelectionRow(treeview.getRowCount()-1);
         		listview.update();
         		
         		DefaultMutableTreeNode chosen_node = (DefaultMutableTreeNode)treeview.getLastSelectedPathComponent();
         		current_image = (ImageModel) chosen_node.getUserObject();
 				imagelabel.setImage(current_image.getImage());
         		imagelabelsize.setSize(current_image.getImage().getWidth(), current_image.getImage().getHeight());
 				imagelabel.setPreferredSize(imagelabelsize);
 				imagelabel.updateUI();
 	        }
 		}
 		else if (command == save_label) {
 			javax.swing.filechooser.FileFilter ff = new ogx.view.ImageFilter();
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(ff);
 			int returnVal = fc.showSaveDialog(fc);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        	DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) treeview.getLastSelectedPathComponent();
 	        	if (selected_node.getLevel() == 2) selected_node = (DefaultMutableTreeNode) selected_node.getParent();
 	        	if (!controller.saveImage(fc.getSelectedFile().getPath(), selected_node)) {
 	        		dispErrorMessage(controller.getLastError());
 	        	}
 	        }
 		}
 		else if (command == fitview_label) {
 			imagelabelsize.setSize(imagepanel.getWidth()-5, imagepanel.getHeight()-5);
 			imagelabel.setPreferredSize(imagelabelsize);
 			imagelabel.updateUI();
 		}
 		else if (command == nominal_label) {
 			if (current_image != null) {
 				imagelabelsize.setSize(current_image.getImage().getWidth(), current_image.getImage().getHeight());
 				imagelabel.setPreferredSize(imagelabelsize);
 				imagelabel.updateUI();
 			}
 		}
 		else if (command == zoomin_label) {
 			imagelabelsize.setSize(imagelabelsize.getWidth()+10, imagelabelsize.getHeight()+10);
 			imagelabel.setPreferredSize(imagelabelsize);
 			imagelabel.updateUI();
 		}
 		else if (command == zoomout_label) {
 			imagelabelsize.setSize(imagelabelsize.getWidth()-10, imagelabelsize.getHeight()-10);
 			imagelabel.setPreferredSize(imagelabelsize);
 			imagelabel.updateUI();
 		}
 		else if (command == set_roi_label) {
 			Rectangle label_roi = imagelabel.getSelection();
 			if (current_image != null && label_roi != null) {
 				if (label_roi.width != 0 && label_roi.height != 0) {
 					Rectangle image_roi = new Rectangle(label_roi);
 					if (image_roi.x < current_image.getImage().getWidth() && image_roi.y < current_image.getImage().getHeight()) {
 						setCurrentImageROI(null);
 						double scaleX = (double)current_image.getImage().getWidth()/(double)imagelabelsize.width;
 						double scaleY = (double)current_image.getImage().getHeight()/(double)imagelabelsize.height;
 						image_roi.x = (int)(image_roi.x*scaleX);
 						image_roi.y = (int)(image_roi.y*scaleY);
 						image_roi.width = (int)(image_roi.width*scaleX);
 						image_roi.height = (int)(image_roi.height*scaleY);
 						if (image_roi.x + image_roi.width > current_image.getImage().getWidth()) {
 							image_roi.width = current_image.getImage().getWidth() - image_roi.x;
 						}
 						if (image_roi.y + image_roi.height > current_image.getImage().getHeight()) {
 							image_roi.height = current_image.getImage().getHeight() - image_roi.y;
 						}
 						setCurrentImageROI(image_roi);
 						imagelabel.getSelection().width = 0;
 						imagelabel.getSelection().height = 0;
 					}
 				}
 			}
 			histogramlabel.updateUI();
 			imagelabel.updateUI();
 		}
 		else if (command == clear_roi_label) {
 			if (current_image != null) {
 				setCurrentImageROI(null);
 			}
 			histogramlabel.updateUI();
 			imagelabel.updateUI();
 		}
 		else if (command == delete_node) {
 			DefaultMutableTreeNode chosen_node = (DefaultMutableTreeNode)treeview.getLastSelectedPathComponent();
 			if (chosen_node != null) {
 				DefaultMutableTreeNode parent_node = (DefaultMutableTreeNode)chosen_node.getParent();
 				if (chosen_node.getLevel() > 0) {
 					ImageModel current_obj = (ImageModel)chosen_node.getUserObject();
 					//if (chosen_node.getLevel() == 2) {
 						if (!current_obj.getName().equals("R") && 
 							!current_obj.getName().equals("G") && 
 							!current_obj.getName().equals("B")) {
 							if (chosen_node.getChildCount() > 0) {
 								chosen_node.removeAllChildren();
 							}
 							parent_node.remove(chosen_node);
 							treeview.clearSelection();
 							treeview.updateUI();
 							imagelabel.setImage(null);
 							imagelabel.updateUI();
 							histogramlabel.setIcon(null);
 							histogramlabel.updateUI();
 							listview.update();
 						}
 					//}
 				}
 			}
 		}
 		else if (command == "run_method") {
 			for (int i = 0; i < methodsequence.getModel().getSize(); ++i) {
 				methodsequence.setSelectedIndex(i);
 				if (!controller.runMethod(listview.getCurrentMethodModel())) {
 					dispErrorMessage(controller.getLastError());
 				}
 			}
 			//controller.runMethod(listview.getCurrentMethodModel());
 			//setCurrentImageROI(null);
 			//setCurrentImageROI(current_image.getROI());
 			treeview.updateUI();
 			histogramlabel.updateUI();
 			imagelabel.updateUI();
 			listview.update();
 		}
 		else if (command == "load_sequence") {
 			JFileChooser fc = new JFileChooser();
 			javax.swing.filechooser.FileFilter ff = new JSONFilter();
 			fc.setFileFilter(ff);
 			int returnVal = fc.showOpenDialog(fc);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        	listview.clearMethodModelView();
 	        	MethodListModel sequencemodel = (MethodListModel)methodsequence.getModel();
 	        	if (sequencemodel.load(fc.getSelectedFile().getPath(), (MethodListModel) methodlist.getModel())) {
 		        	for (int i = 0; i < sequencemodel.getSize(); ++i) {
 		        		listview.addMethodModelView(sequencemodel.getElementAt(i));
 		        	}
 		        	methodsequence.updateUI();
 	        	}
 	        	else {
 	        		dispErrorMessage("Invalid file or some of calculation methods are not available.");
 	        	}
 	        }
 		}
 		else if (command == "save_sequence") {
 			JFileChooser fc = new JFileChooser();
 			javax.swing.filechooser.FileFilter ff = new JSONFilter();
 			fc.setFileFilter(ff);
 			int returnVal = fc.showSaveDialog(fc);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        	for (int i = 0; i < methodsequence.getModel().getSize(); ++i) {
 	        		listview.updateMethodModel(i);
 	        	}
 	        	((MethodListModel)methodsequence.getModel()).save(fc.getSelectedFile().getPath());
 	        }
 		}
 		else {
 			System.err.println("Invoked command: " + command);
 		}
 	}
 	
 	void setCurrentImageROI(Rectangle roi) {
 		DefaultMutableTreeNode current_node = (DefaultMutableTreeNode)treeview.getLastSelectedPathComponent();
 		if (current_node.getLevel() == 2) {
 			current_node = (DefaultMutableTreeNode)current_node.getParent();
 		}
 		if (current_node.getLevel() == 1) {
 			// we are in image
 			((ImageModel)current_node.getUserObject()).setROI(roi);
 			for (int i = 0; i < current_node.getChildCount(); ++i) {
 				((ImageModel)((DefaultMutableTreeNode)current_node.getChildAt(i)).getUserObject()).setROI(roi);
 			}
 			histogramlabel.setIcon(new ImageIcon(current_image.getHistogramImage()));
 		}
 	}
 
 	@Override
 	public void dragGestureRecognized(DragGestureEvent dge) {
 		MethodSelection transferable = new MethodSelection(methodlist.getSelectedValue());
 	    dragsource.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
 	}
 
 	@Override
 	public void dragEnter(DropTargetDragEvent dtde) {
 		
 	}
 
 	@Override
 	public void dragExit(DropTargetEvent dte) {
 		
 	}
 
 	@Override
 	public void dragOver(DropTargetDragEvent dtde) {
 		
 	}
 
 	@Override
 	public void drop(DropTargetDropEvent dtde) {
 		Transferable tr = dtde.getTransferable();
 	    DataFlavor[] flavors = tr.getTransferDataFlavors();
 	    
 	    for (int i = 0; i < flavors.length; ++i) {
 	    	try {
 				Object obj = tr.getTransferData(flavors[i]);
 				if (obj != null) {
 					if (obj instanceof MethodModel) {
 						MethodModel new_method = new MethodModel((MethodModel)obj);
 						((MethodListModel)methodsequence.getModel()).addMethodModel(new_method);
 						listview.addMethodModelView(new_method);
 						listview.update();
 						methodsequence.updateUI();
 						paramspanel.updateUI();
 						break;
 					}
 				}
 			} catch (UnsupportedFlavorException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    }
 	    dtde.dropComplete(true);
 	    return;
 	}
 
 	@Override
 	public void dropActionChanged(DropTargetDragEvent dtde) {
 		
 	}
 
 	@Override
 	public void dragDropEnd(DragSourceDropEvent dsde) {
 		
 	}
 
 	@Override
 	public void dragEnter(DragSourceDragEvent dsde) {
 		
 	}
 
 	@Override
 	public void dragExit(DragSourceEvent dse) {
 		
 	}
 
 	@Override
 	public void dragOver(DragSourceDragEvent dsde) {
 		
 	}
 
 	@Override
 	public void dropActionChanged(DragSourceDragEvent dsde) {
 		
 	}
 }
