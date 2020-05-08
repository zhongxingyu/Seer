 package it.polito.atlas.alea2.components;
 
 import static it.polito.atlas.alea2.components.DisplaySingleton.display;
 import static org.eclipse.swt.SWT.OPEN;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import it.polito.atlas.alea2.Annotation;
 import it.polito.atlas.alea2.Project;
 import it.polito.atlas.alea2.Slice;
 import it.polito.atlas.alea2.Storage;
 import it.polito.atlas.alea2.Track;
 import it.polito.atlas.alea2.TrackLIS;
 import it.polito.atlas.alea2.TrackText;
 import it.polito.atlas.alea2.TrackVideo;
 import it.polito.atlas.alea2.adapters.NewAnnotationAdapter;
 import it.polito.atlas.alea2.db.DBStorage;
 import it.polito.atlas.alea2.functions.ProjectManager;
 import it.polito.atlas.alea2.initializer.TabFolderInitializer;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * @author  DANGELOA
  */
 public class MainWindowShell {
 
 	private static final Shell mainShell;
 	/**
 	 * @uml.property  name="layout"
 	 */
 	private static final FormLayout layout;
 
 	/**
 	 * @return  the layout
 	 * @uml.property  name="layout"
 	 */
 	public static FormLayout getLayout() {
 		return layout;
 	}
 
 	private static boolean run;
 
 	public static Shell shell() {
 		return mainShell;
 	}
 
 	static {
 		mainShell = new Shell(display());
 		mainShell.setText("Alea2");
 		layout = new FormLayout();
 		mainShell.setLayout(layout);
 		mainShell.addShellListener(new ShellListener() {
 			
 			@Override
 			public void shellIconified(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellDeiconified(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellDeactivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				ProjectManager.beforeClose();
 			}
 			
 			@Override
 			public void shellActivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 
 	/**
 	 * Annotation Editor list
 	 */
 	public static List<AnnotationShell> annotationShells = new ArrayList<AnnotationShell>();
 
 	/**
 	 * Open Projects list
 	 * @uml.property  name="projects"
 	 */
 	private static List<Project> projects = new ArrayList<Project>();
 
 	/**
 	 * @return  the Open Projects List
 	 * @uml.property  name="projects"
 	 */
 	public static List<Project> getProjects() {
 		return projects;
 	}
 
 	/**
 	 * @return the current Project
 	 */
 	public static Project getCurrentProject() {
 		TabFolder tf = TabFolderInitializer.getTabFolder();
 		
 		if ((tf == null) || (tf.getItemCount() == 0))
 			return null;
 		if (MainWindowShell.getProjects().size() != tf.getItemCount())  {
 			System.out.println("The number of TabItems and OpenProjects are differents");
 			return null;
 		}
 		return MainWindowShell.getProjects().get(tf.getSelectionIndex());
 	}
 
 	/**
 	 * @return the current Tree
 	 */
 	public static Tree getCurrentTree() {
 		Project p = getCurrentProject();
 		if (p == null)
 			return null;
 		TabFolder tf = TabFolderInitializer.getTabFolder();
 		Tree t = (Tree) p.link;
 		if (t != (Tree) tf.getItem(tf.getSelectionIndex()).getControl()) {
 			System.out.println("Project link and current Tree mismatch");
 		}
 		return t;
 	}
 
 	/**
 	 * @return  the Storage
 	 * @uml.property  name="storage"
 	 */
 	public static Storage getStorage() {
 		return storage;
 	}
 
 	/**
 	 * Projects Storage
 	 * @uml.property  name="storage"
 	 */
 	private static Storage storage = new DBStorage();
 
 	/**
 	 * Create a new TabItem and load the Project.
 	 * Returns false if Project is already loaded
 	 * @param p Project to load
 	 */
 	public static boolean openProject(Project p) {
 		if (projects.contains(p))
 			return false;
 		if (!projects.add(p))
 			return false;
 		
 		TabFolder tabFolder = TabFolderInitializer.getTabFolder();
 		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
 		tabItem.setText(p.getName());
 		tabItem.setData(p);
 		
 		// create the tree		
 		final Tree tree = createTree(tabFolder);
 		tabItem.setControl(tree);
 		addProjectData(tree, p);
 		return true;
 	}
 	
     private static final Tree createTree(TabFolder tabFolder) {
 		final Tree tree = new Tree(tabFolder, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		// check/select listener
 	    tree.addListener(SWT.Selection, new Listener() {
 	        public void handleEvent(Event event) {
 	          String string = event.detail == SWT.CHECK ? "Checked/Unchecked" : "Selected";
 	          System.out.println(event.item + " " + string);
 	        }
 	    });
 
 	    // Tree pop up menus
 	    MenuItem item;
 	    // Slice context menu
 	    final Menu contextMenuSlice = new Menu(mainShell, SWT.POP_UP);
 	    item = new MenuItem(contextMenuSlice, SWT.PUSH);
 	    item.setText("Remove Slice");
 	    item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event arg0) {
 				for (TreeItem i : tree.getSelection()) {
 					Slice s;
 					try {
 						s = (Slice) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (s == null) {
 						System.out.println("No link betweek TreeItem and Slice");
 						return;
 					}
 			    	s.remove();
 			    	MainWindowShell.updateTree(getCurrentTree(), getCurrentProject());
 				}
 			}
 		});
 
 	    // Track context menu
 	    final Menu contextMenuTrack = new Menu(mainShell, SWT.POP_UP);
 	    item = new MenuItem(contextMenuTrack, SWT.PUSH);
 	    item.setText("Add Slice");
 	    item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				//MessageBox mb = new MessageBox(instance);
 				//mb.setMessage("Insert the name: ");
 				//mb.open();
 				for (TreeItem i : tree.getSelection()) {
 					Track t;
 					try {
 						t = (Track) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (t == null) {
 						System.out.println("No link betweek TreeItem and Track");
 						return;
 					}
 			    	Slice s = new Slice(t, 0, 0);
 			    	t.addSlice(s);
 			    	MainWindowShell.updateTree(getCurrentTree(), getCurrentProject());
 				}
 			}	    	
 	    });
 	    item = new MenuItem(contextMenuTrack, SWT.PUSH);
 	    item.setText("Remove Track");
 	    item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event arg0) {
 				for (TreeItem i : tree.getSelection()) {
 					Track t;
 					try {
 						t = (Track) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (t == null) {
 						System.out.println("No link betweek TreeItem and Track");
 						return;
 					}
 			    	t.remove();
 			    	MainWindowShell.updateTree(getCurrentTree(), getCurrentProject());
 				}
 			}
 		});
 
 	    // Annotation context menu
 	    final Menu contextMenuAnnotation = new Menu(mainShell, SWT.POP_UP);
 	    final Menu lisMenu = new Menu(mainShell, SWT.DROP_DOWN);
 	    item = new MenuItem(contextMenuAnnotation, SWT.CASCADE);
 	    item.setText("Add Track LIS");	    
 	    item.setMenu(lisMenu);    
 	    addLISMenu(mainShell, lisMenu, getLisListener());
 
 	    item = new MenuItem(contextMenuAnnotation, SWT.PUSH);
 	    item.setText("Add Track Video");	    
 	    item.addListener(SWT.Selection, getVideoListener());
 	    
 	    item = new MenuItem(contextMenuAnnotation, SWT.PUSH);
 	    item.setText("Add Track Text");
 	    item.addListener(SWT.Selection, getTextListener());
 	    
 	    item = new MenuItem(contextMenuAnnotation, SWT.PUSH);
 	    item.setText("Remove Annotation");
 	    item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event arg0) {
 				for (TreeItem i : tree.getSelection()) {
 					Annotation a;
 					try {
 						a = (Annotation) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (a == null) {
 						System.out.println("No link betweek TreeItem and Slice");
 						return;
 					}
 			    	a.remove();
 			    	MainWindowShell.updateTree(getCurrentTree(), getCurrentProject());
 				}
 			}
 		});
 
 	    // no selected items context menu
 	    final Menu contextMenuAddAnnotation = new Menu(mainShell, SWT.POP_UP);
 	    item = new MenuItem(contextMenuAddAnnotation, SWT.PUSH);
 	    item.setText("Add Annotation");
 	    item.addSelectionListener(new NewAnnotationAdapter());
 	    
 		// Mouse listener: assign the right context menu by the item type
 	    tree.addListener(SWT.MouseDown, new Listener() {
 
 	    	public void handleEvent(Event event) {
 	    		Point point = new Point(event.x, event.y);
 	    		// System.out.println("Point: " + point.x + "," + point.y);
 	    		TreeItem item = tree.getItem(point);
 	    		if (item != null) {
 	    			// System.out.println("Mouse inside item");
 	    			for (TreeItem i : tree.getSelection()) {
 	    				Object o=i.getData();
 	    				if (o instanceof Annotation) {
 	    					tree.setMenu(contextMenuAnnotation);
 	    				} else if (o instanceof Track) {
 	    					tree.setMenu(contextMenuTrack);
 	    				} else if (o instanceof Slice) {
 	    					tree.setMenu(contextMenuSlice);
 	    				} else {
 	    	    			System.out.println("Unknown item type: no context menu");
 	    					tree.setMenu(null);
 	    				}
 	    				if (o != null)
 	    					System.out.println(i + ": " + o.getClass());
 	    			}
 	    		} else {
 	    			// System.out.println("No Item");
 		         	// System.out.println("Rect: " + r.x + "," + r.y + "," + r.width + "," + r.height);
 		         	tree.setMenu(contextMenuAddAnnotation);
 	    		}
 	    	}
 	    });
 		
 		tree.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				TreeItem item = (TreeItem) arg0.item;
 				TreeItem oldItem = item;
 				Object o = item.getData();
 
 				while (true) {
 					if (o instanceof Annotation) {
 						getCurrentProject().setCurrentAnnotation((Annotation) o);
 						return;
 					} else {
 						oldItem=item;
 						item=item.getParentItem();
 						o=item.getData();
 						if ((item == null) || (item == oldItem))
 							return;
 						if (o == null)
 							return;
 					}
 				}
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 
 	    // columns
 		tree.setHeaderVisible(true);
 	    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
 	    column1.setText("Name");
 	    column1.setWidth(200);
 	    column1.setMoveable(true);
 	    TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
 	    column2.setText("Type");
 	    column2.setWidth(200);
 	    column2.setMoveable(true);
 	    TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
 	    column3.setText("Info");
 	    column3.setWidth(200);
 	    column3.setMoveable(true);
 		tree.pack();
 		return tree;
 	}
 
 	static void addLISMenu(Shell instance, Menu lisMenu, Listener lisListener) {
         MenuItem item;
 		final Menu handsMenu = new Menu(instance, SWT.DROP_DOWN);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Hands");	    
 	    item.setMenu(handsMenu);
 		    item = new MenuItem(handsMenu, SWT.CASCADE);
 		    item.setText("Both Hands");
 		    item.addListener(SWT.Selection, lisListener);
 		    item = new MenuItem(handsMenu, SWT.CASCADE);
 		    item.setText("Left Hand");
 		    item.addListener(SWT.Selection, lisListener);
 		    item = new MenuItem(handsMenu, SWT.CASCADE);
 		    item.setText("Right Hand");	    
 	    item.addListener(SWT.Selection, lisListener);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Orientation");
 	    item.addListener(SWT.Selection, lisListener);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Torso");
 	    item.addListener(SWT.Selection, lisListener);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Shoulder");
 	    item.addListener(SWT.Selection, lisListener);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Head");
 	    item.addListener(SWT.Selection, lisListener);		    
 	    final Menu facialMenu = new Menu(instance, SWT.DROP_DOWN);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Facial");	    
 	    item.setMenu(facialMenu);
		    item = new MenuItem(lisMenu, SWT.CASCADE);
 		    item.setText("Eyes");
 		    item.addListener(SWT.Selection, lisListener);	
		    item = new MenuItem(lisMenu, SWT.CASCADE);
 		    item.setText("Nose");
 		    item.addListener(SWT.Selection, lisListener);	
		    item = new MenuItem(lisMenu, SWT.CASCADE);
 		    item.setText("Cheeks");
 		    item.addListener(SWT.Selection, lisListener);	
 	    final Menu labialMenu = new Menu(instance, SWT.DROP_DOWN);
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Labial");	    
 	    item.setMenu(labialMenu);
		    item = new MenuItem(lisMenu, SWT.CASCADE);
 		    item.setText("Mouth");
 		    item.addListener(SWT.Selection, lisListener);	
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Gaze");
 	    item.addListener(SWT.Selection, lisListener);	
 	    item = new MenuItem(lisMenu, SWT.CASCADE);
 	    item.setText("Custom");
 	    item.addListener(SWT.Selection, lisListener);	
 	}
 
 	static Listener getVideoListener() {
 		return new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				String path=openVideoShell(shell());
 				Tree tree = getCurrentTree();
 				if ((path == null) || (tree  == null))
 					return;
 				for (TreeItem i : tree.getSelection()) {
 					Annotation a;
 					try {
 						a = (Annotation) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (a == null) {
 						System.out.println("No link betweek TreeItem and Annotation");
 						return;
 					}
 			    	String newTrackName = path;
 			    	TrackVideo t = new TrackVideo(a, newTrackName);
 					a.addTrackVideo(t);
 					updateTree(tree, a.getParent());
 				}
 			}
 		};
 	}
 	static Listener getTextListener() {
 		return new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				Tree tree = getCurrentTree();
 				if (tree  == null)
 					return;
 				for (TreeItem i : tree.getSelection()) {
 					Annotation a;
 					try {
 						a = (Annotation) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (a == null) {
 						System.out.println("No link betweek TreeItem and Annotation");
 						return;
 					}
 					MenuItem item = (MenuItem) event.widget;
 			        String newTrackName = item.getText();
 			    	TrackText t = new TrackText(a, newTrackName);
 					a.addTrackText(t);
 					updateTree(tree, a.getParent());
 				}
 			}	    	
 	    };
 	}
 	
 	static Listener getLisListener() {
 		return new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				Tree tree = getCurrentTree();
 				if (tree  == null)
 					return;
 				for (TreeItem i : tree.getSelection()) {
 					Annotation a;
 					try {
 						a = (Annotation) i.getData();
 					} catch (Exception e) {
 						System.out.println(e.getMessage());
 						return;
 					}
 					if (a == null) {
 						System.out.println("No link betweek TreeItem and Annotation");
 						return;
 					}
 					MenuItem item = (MenuItem) event.widget;
 			        String newTrackName = item.getText();
 			    	TrackLIS t = new TrackLIS(a, newTrackName);
 					a.addTrackLIS(t);
 					updateTree(tree, a.getParent());
 				}
 			}	    	
 	    };
 	}
 
 	public static void updateTree(Tree tree, Project p) {
 		if ((tree == null) || (p == null))
 			return;
 		tree.removeAll();
 		addProjectData(tree, p);
 	}
 
 	/** fill tree with project data
      * 
      * @param p
      */
 	public static void addProjectData(Tree tree, Project p) {
 		p.link = tree;
 		tree.setData(p);
 	    for (Annotation a : p.getAnnotations()) {
 	    	TreeItem aItem = addAnnotationData(tree, a);
 	    	if (aItem != null)
 	    		aItem.setExpanded(true);
 		}
 	}
 
 	private static TreeItem addAnnotationData(Tree tree, Annotation a) {
 		TreeItem aItem = new TreeItem(tree, SWT.NONE);
 		aItem.setText(new String[] { a.getName(), "annotation", String.valueOf(a.getTracks().size()) + " tracks"});
 		aItem.setData(a);
 		for (Track t : a.getTracks()) {
 			addTrackData(aItem, t);
 		}
 		aItem.setExpanded(true);
 		return aItem;
 	}
 
 	private static TreeItem addTrackData(TreeItem aItem, Track t) {
     	TreeItem tItem = new TreeItem(aItem, SWT.NONE);
 		tItem.setText(new String[] { t.getName(), t.getTypeString() + " track", String.valueOf(t.getSlices().size()) + " slices"});
 		tItem.setData(t);
     	t.link = tItem;
 		int i=0;
 		for (Slice s : t.getSlices()) {
 			addSliceData(tItem, s, ++i);
 		}
 		tItem.setExpanded(true);
 		return tItem;
 	}
 
 	private static TreeItem addSliceData(TreeItem tItem, Slice s, int i) {
 		TreeItem sItem = new TreeItem(tItem, SWT.NONE);
 		sItem.setText(new String[] { "slice " + i, "slice", "period: " + SWTPlayer.timeString(s.getStartTime()) + " - " + SWTPlayer.timeString(s.getEndTime())});
 		sItem.setData(s);
 		return sItem;
 	}
 
 	public static String openVideoShell(Shell shell) {
 		FileDialog dialog = new FileDialog(shell, OPEN);
 		
 		String[] filterNames = new String[] { "Video Files (*.avi, *.mov, *.mpg, *.mp4)", "All Files (*)"};
 		String[] filterExtensions = new String[] { "*.avi; *.mov; *.mpg; *.mp4", "*" };
 
 		dialog.setFilterNames(filterNames);
 		dialog.setFilterExtensions(filterExtensions);
 
 		String path = dialog.open();
 		return path;
 	}
 
 	public static void runShell() {
 		if (!run) {
 			mainShell.setSize(640, 480);
 			mainShell.setLocation(SWT.DEFAULT, SWT.DEFAULT);
 
 			mainShell.open();
 
 			while (!mainShell.isDisposed()) {
 				if (!display().readAndDispatch()) {
 					display().sleep();
 				}
 			}
 			MainWindowShell.dispose();
 			display().dispose();
 		}
 	}
 
 	private static void dispose() {
 		for (AnnotationShell a : MainWindowShell.annotationShells)
 			if (a!=null) 
 				if (a.shell()!=null)
 					a.shell().close();
 	}
 
 	public static Display getDiplay() {
 		return display();
 	}
 
 	// verificare il punto in cui si deve chiamare
 	public static void removeAnnotationShell(AnnotationShell annotationShell) {
 		annotationShells.remove(annotationShell);
 	}
 }
