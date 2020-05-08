 package ui.isometric.builder;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.dnd.DropTarget;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.filechooser.FileFilter;
 
 import client.ClientMessageHandler;
 
 import data.Database;
 
 import serialization.ParseException;
 import ui.isometric.IsoCanvas;
 import ui.isometric.IsoInterface;
 import ui.isometric.abstractions.IsoObject;
 import ui.isometric.abstractions.IsoPlayer;
 import ui.isometric.builder.things.ThingCreator;
 import ui.isometric.builder.things.ThingCreatorDnD;
 import ui.isometric.datasource.IsoChangeLevelDataSource;
 import ui.isometric.libraries.IsoRendererLibrary;
 import util.Direction;
 import util.ImagePanel;
 import util.Resources;
 
 import game.*;
 
 /**
  * 
  * The overall class that manages a builder user interface
  * 
  * @author melby
  *
  */
 public class BuilderInterface implements IsoInterface {
 	private JFrame frame;
 	private InspectorPanel inspector;
 	private LibraryFrame library;
 	private String frameName;
 	private ToolPanel tools;
 	
 	private IsoCanvas canvas;
 	
 	private GameWorld world;
 	private IsoChangeLevelDataSource dataSource;
 	
 	private static final String EXTENTION = "wblrd";
 	
 	private ThingCreator storedCreator = null;
 	private GameThing moving = null;
 	
 	private Mode mode = Mode.ADD;
 	
 	/**
 	 * The world builder mode
 	 * @author ruarusmelb
 	 *
 	 */
 	private enum Mode {
 		ADD,
 		DELETE,
 		MOVE;
 	}
 	
 	/**
 	 * The tool panel at the top of the WorldBuilder
 	 * @author ruarusmelb
 	 *
 	 */
 	private class ToolPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 		private LevelPanel level;
 		private ImagePanel image;
 		private ImagePanel movingImage;
 		private JRadioButton add;
 		private JRadioButton delete;
 		private JRadioButton move;
 		
 		/**
 		 * Create a default ToolPanel
 		 */
 		public ToolPanel() {
 			JButton up = new JButton("Up a Level");
 			up.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					levelUp();
 				}
 			});
 			this.add(up);
 			JButton down = new JButton("Down a Level");
 			down.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					levelDown();
 				}
 			});
 			this.add(down);
 			JButton rotate = new JButton("Rotate");
 			rotate.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					rotate();
 				}
 			});
 			this.add(rotate);
 			level = new LevelPanel(dataSource.level());
 			this.add(level);
 			
 			add = new JRadioButton("Add");
 			add.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setMode(Mode.ADD);
 				}
 			});
 			this.add(add);
 			delete = new JRadioButton("Delete");
 			delete.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setMode(Mode.DELETE);
 				}
 			});
 			this.add(delete);
 			move = new JRadioButton("Move");
 			move.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setMode(Mode.MOVE);
 				}
 			});
 			this.add(move);
 			
 			ButtonGroup group = new ButtonGroup();
 			group.add(add);
 			group.add(delete);
 			group.add(move);
 			
 			image = new ImagePanel(IsoRendererLibrary.maskTile());
 			image.setDropTarget(new DropTarget(canvas, new ThingCreatorDnD.ThingDropListener(new ThingCreatorDnD.ThingDropListener.ThingDropListenerAction() {
 				@Override
 				public void thingCreatorDroped(Component onto, Point location, ThingCreator creator) {
 					setCreator(creator);
 				}
 			})));
 			this.add(image);
 			
 			movingImage = new ImagePanel(null);
 			this.add(movingImage);
 			
 			refreshMode();
 		}
 		
 		/**
 		 * Notify this toolpanel that the level has changed
 		 */
 		public void refreshLevel() {
 			level.setLevel(dataSource.level());
 		}
 		
 		/**
 		 * Notify this ToolPanel that the creator has changed
 		 */
 		public void refreshCreator() {
 			BufferedImage i = (storedCreator != null)?storedCreator.previewImage():IsoRendererLibrary.maskTile();
 			image.setImage(i);
 			this.validate();
 			this.repaint();
 		}
 		
 		/**
 		 * Notify this ToolPanel that the mode has changed
 		 */
 		public void refreshMode() {
 			add.setSelected(mode == Mode.ADD);
 			delete.setSelected(mode == Mode.DELETE);
 			move.setSelected(mode == Mode.MOVE);
 		}
 		
 		/**
 		 * Notify this ToolPanel that the moving thing has changed
 		 */
 		public void refreshMoving() {
 			BufferedImage i = (moving != null)?IsoRendererLibrary.imageForRendererName(moving.renderer(), Direction.NORTH).image():null;
 			movingImage.setImage(i);
 			this.doLayout();
 			this.validate();
 			this.repaint();
 		}
 	}
 	
 	/**
 	 * Create a world builder interface with a given GameWorld and ClientMessageHandler
 	 * @param name
 	 * @param world
 	 * @param logic
 	 */
 	public BuilderInterface(String name, final GameWorld world, ClientMessageHandler logic) {
 		this.world = world;
 		this.frameName = name;
 		
 		dataSource = new IsoChangeLevelDataSource(this.world);
 		
 		frame = new JFrame(name);
 		
 		JMenuBar bar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem save = new JMenuItem("Save");
 		file.add(save);
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				save();
 			}
 		});
 		JMenuItem load = new JMenuItem("Load");
 		file.add(load);
 		load.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				load();
 			}
 		});
 		bar.add(file);
 		frame.setJMenuBar(bar);
 		
 		frame.setLayout(new BorderLayout());
 		
 		tools = new ToolPanel();
 		frame.add(tools, BorderLayout.NORTH);
 		
 		canvas = new IsoCanvas(dataSource);
 		canvas.addSelectionCallback(new IsoCanvas.SelectionCallback() {
 			@Override
 			public void selected(final IsoObject i, final Location l, MouseEvent event) {
 				if(event.getButton() == MouseEvent.BUTTON3 || event.isControlDown()) { // Right click
 					if(mode == Mode.ADD && storedCreator != null) {
 						if(storedCreator != null) {
 							canvas.calculateTypesAtAtPoint(event.getPoint());
 							l.put(storedCreator.createThing(world, l));
 						}
 					}
 					if(mode == Mode.DELETE) {
 						if(i != null && i.gameThing() != null) {
 							LocationS.NOWHERE.put(i.gameThing());
 							world.forget(i.gameThing());
 						}
 					}
 					if(mode == Mode.MOVE) {
 						if(moving == null) {
 							if(i != null && i.gameThing() != null) {
 								LocationS.NOWHERE.put(i.gameThing());
 								setMoving(i.gameThing());
 							}
 						}
 						else {
 							if(moving != null) {
 								l.put(moving);
 								setMoving(null);
 							}
 						}
 					}
 				}
 				else {
 					if(event.isShiftDown()) {
 						if(i != null) {
 							Location loc = i.gameThing().location();
 							if(loc instanceof Level.Location) {
 								((Level.Location)loc).rotate(Direction.EAST).put(i.gameThing());
 							}
 						}
 					}
 					else {
 						inspect(l);
 					}
 				}
 			}
 		});
 		canvas.setDropTarget(new DropTarget(canvas, new ThingCreatorDnD.ThingDropListener(new ThingCreatorDnD.ThingDropListener.ThingDropListenerAction() {
 			@Override
 			public void thingCreatorDroped(Component onto, Point location, ThingCreator creator) {
 				if(onto instanceof IsoCanvas) {
 					setCreator(creator);
 					IsoCanvas canvas = (IsoCanvas)onto;
 					canvas.calculateTypesAtAtPoint(location);
 					Location l = dataSource.level().location(canvas.getCachedSelectedSquarePosition(), Direction.NORTH);
 					l.put(creator.createThing(world, l));
 				}
 			}
 		})));
 		frame.add(canvas, BorderLayout.CENTER);
 		
 		inspector = new InspectorPanel(this);
 		inspector.getContentPane().setLayout(new BoxLayout(inspector.getContentPane(), BoxLayout.Y_AXIS));
 		inspector.getContentPane().add(Box.createVerticalGlue());
 		inspector.validate();
 		
 		library = new LibraryFrame(this);
 		
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		screen.height -= 100;
 		double perx = 0.3;
 		
 		library.setLocation(0, 0);
 		library.setSize((int)(screen.width*perx), (int)(screen.height*0.5));
 		inspector.setLocation(0, (int)(screen.height*0.5));
 		inspector.setSize((int)(screen.width*perx), (int)(screen.height*0.5));
 		frame.setLocation((int)(screen.width*perx), 0);
 		frame.setSize((int)(screen.width*(1-perx)), (int)(screen.height));
 	}
 	
 	/**
 	 * Set the moving character
 	 * @param t
 	 */
 	private void setMoving(GameThing t) {
 		if(moving != null && t != null) {
 			if(JOptionPane.showConfirmDialog(frame, "Are you sure you wish to reset the current item being moved?") == JOptionPane.CANCEL_OPTION) {
 				tools.refreshMoving();
 				return;
 			}
 		}
 		
 		moving = t;
 		
 		tools.refreshMoving();
 	}
 	
 	/**
 	 * Set the stored creator
 	 * @param creator
 	 */
 	private void setCreator(ThingCreator creator) {
 		storedCreator = creator;
 		tools.refreshCreator();
 	}
 	
 	/**
 	 * Set the mode
 	 * @param m
 	 */
 	private void setMode(Mode m) {
 		if(mode == Mode.MOVE && m != Mode.MOVE && moving != null) {
 			if(JOptionPane.showConfirmDialog(frame, "Are you sure you wish to delete the current item being moved?") == JOptionPane.CANCEL_OPTION) {
 				tools.refreshMode();
 				return;
 			}
 			
 			setMoving(null);
 		}
 		
 		mode = m;
 		tools.refreshMode();
 	}
 	
 	/**
 	 * Move up a level
 	 */
 	private void levelUp() {
 		dataSource.goUp();
 		tools.refreshLevel();
 	}
 	
 	/**
 	 * Move down a level
 	 */
 	private void levelDown() {
 		dataSource.goDown();
 		tools.refreshLevel();
 	}
 	
 	/**
 	 * Rotate
 	 */
 	private void rotate() {
 		canvas.setViewDirection(canvas.viewDirection().compose(Direction.EAST));
 	}
 	
 	@Override
 	public void show() {
 		frame.setVisible(true);
 		inspector.setVisible(true);
 		library.setVisible(true);
 	}
 	
 	/**
 	 * Inspect a given location
 	 * @param l
 	 */
 	private void inspect(Location l) {
 		inspector.inspect(l);
 	}
 
 	@Override
 	public GameWorld world() {
 		return world;
 	}
 	
 	/**
 	 * Save the world
 	 */
 	public void save() {
 		String file = Database.treeToXML(world.toTree());
 		
 		JFileChooser chooser = new JFileChooser();
 		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File save = chooser.getSelectedFile().getAbsoluteFile();
 			if(!save.getAbsolutePath().endsWith("."+EXTENTION)) {
 				save = new File(save.getAbsolutePath() + "." + EXTENTION);
 			}
 			if(save.exists()) {
 				if(JOptionPane.showConfirmDialog(frame, "This file exists, are you sure you wish to overwrite it?", null, JOptionPane.OK_CANCEL_OPTION, 0) == JOptionPane.CANCEL_OPTION) {
 					return;
 				}
 			}
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(save));
 				writer.write(file);
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Load the world
 	 */
 	public void load() {
 		String loaded = null;
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File arg0) {
 				return arg0.isFile() && arg0.getAbsolutePath().endsWith(EXTENTION);
 			}
 
 			@Override
 			public String getDescription() {
 				return "World Builder File";
 			}
 		});
 		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File load = chooser.getSelectedFile().getAbsoluteFile();
 			try {
 				loaded = Resources.loadTextFile(load.getAbsolutePath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			return;
 		}
 		
 		if(loaded == null) {
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 			return;
 		}
 		
 		try {
 			world.fromTree(Database.xmlToTree(loaded));
 		} catch (ParseException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 		}
 	}
 	
 	/**
 	 * Get the name of the frame
 	 * @return
 	 */
 	public String frameName() {
 		return frameName;
 	}
 
 	@Override
 	public IsoCanvas canvas() {
 		return canvas;
 	}
 
 	@Override
 	public void performActionOn(String interaction, GameThing thing) {
 		thing.interact(interaction, null);
 	}
 
 	@Override
 	public IsoPlayer player() {
 		return null;
 	}
 
 	@Override
 	public void incomingChat(String message, Color color) {
 		System.out.println("Got Chat: "+message);
 	}
 
 	@Override
 	public void sendChatMessage(String message) {
 		System.out.println("Send Chat: "+message);
 	}
 }
