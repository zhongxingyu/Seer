 package javachallenge.graphics;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javachallenge.common.BlockType;
 import javachallenge.common.Map;
 import javachallenge.common.Point;
 import javachallenge.graphics.components.FileChooser;
 import javachallenge.graphics.components.MapPanel;
 import javachallenge.graphics.components.Panel;
 import javachallenge.graphics.components.Sprite;
 import javachallenge.graphics.util.AnimatedImage;
 import javachallenge.graphics.util.ColorMaker;
 import javachallenge.graphics.util.ImageHolder;
 import javachallenge.graphics.util.Position;
 import javachallenge.server.ServerMap;
 
 public class MapEditor extends PlayGround {
 	public static int blockTypes = 3;
 	
 	public MapEditor() {
 		super ("GoDitor");
 	}
 	
 	@Override
 	public void addMapPanel(MapPanel mapPanel) {
 		ArrayList<Point> spawnLocations = new ArrayList<Point>();
 		ArrayList<Point> flagLocations = new ArrayList<Point>();	
 		mapPanel.setMap(new ServerMap(mapPanel.getMapWidth(), mapPanel.getMapHeight(), 0,
 			spawnLocations, flagLocations));
 	
 		super.addMapPanel(mapPanel);
 	}
 	
 	@Override
 	public void updateDimensions() {
 		Dimension size = getSize();
 		mapPanel.setLocation(10, 10);
 		mapPanel.setSize(size.width * 3 / 4, size.height - 60);
 		sidebar.setLocation(20 + mapPanel.getWidth(), 10);
 		sidebar.setSize(size.width - sidebar.getX() - 30, size.height - 60);
 	}
 	
 	@Override
 	public void addSideBar() {
 		// TODO Auto-generated method stub
 		//super.addSideBar();
		sidebar = new Panel (ColorMaker.shadedPanelBack);
 		sidebar.setLayout(new FlowLayout());
 		sidebar.add(new FileChooser.FileChooserButton("save", false, "Choose target", "data/maps/", "map", "Map files") {
 			{ setForeground(ColorMaker.fieldBackground); }
 			@Override
 			public void onAccept(String filename) {
 				try {
 					getMapPanel().getMap().save(filename);
 					System.err.println("Map saved Successfully");
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public static void main(String[] args) {
 		final MapEditor mapEditor = new MapEditor();
 		Scanner scanner = new Scanner(System.in);
 		mapEditor.createScreenElements(new MapPanel(new ServerMap(scanner.nextInt(), scanner.nextInt(), 0, 
 				new ArrayList<Point>(), new ArrayList<Point>())) {
 			@Override
 			public void onClick(int x, int y) {
 				int type = (mapEditor.getMapPanel().getMap().getBlockType(new Point (x, y)).ordinal() + 1) % blockTypes;
 				mapEditor.getMapPanel().setBlock(x, y, type);
 				mapEditor.getMapPanel().getMap().setBlockType(new Point (x, y), BlockType.values()[type]);
 			}
 			@Override
 			public void onControllClick(int x, int y) {
 				Sprite flag = new AnimatedImage(ImageHolder.Objects.fire, 125, new Position(x, y));
 				map.getFlagLocations().add(new Point(x, y));
 				mapEditor.getMapPanel().addToContainer(flag, 2);
 			}
 		});
 		scanner.close();
 	}
 }
