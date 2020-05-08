 package ui.isometric.builder.inspectors;
 
 import game.*;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import ui.isometric.abstractions.IsoImage;
 import ui.isometric.builder.InspectorPanel;
 import ui.isometric.libraries.IsoRendererLibrary;
 import util.Direction;
 import util.ImagePanel;
 
 /**
  * This class is the base class for a single GameThing inspector.
  * It is a self-configuring JPanel and can be added to many interfaces
  * @author melby
  *
  */
 abstract public class GameThingInspector<T extends GameThing> extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private ImagePanel image;
 	private T thing;
 	private IsoImage isoImage;
 
 	private InspectorPanel panel;
 
 	/**
 	 * Create a GameThingInspector with a given GameThing and InspectorPanel
 	 * @param t - any GameThing
 	 * @param inspectorPanel - the InspectorPanel managing this GameThingInspector
 	 */
 	public GameThingInspector(T t, InspectorPanel inspectorPanel) {
 		this.thing = t;
 		this.isoImage = IsoRendererLibrary.newImageFromGameThing(null, t, Direction.NORTH);
		if(isoImage == null) {
			throw new RuntimeException("Null image from inspector: "+t.renderer());
		}
 		panel = inspectorPanel;
 		
 		this.setSize(0, 50);
 		this.setPreferredSize(new Dimension(200000, 50));
 		
 		image = new ImagePanel(isoImage.image());
 		image.setSize(isoImage.width(), isoImage.height());
 		this.add(image);
 		
 		JButton rotateCW = new JButton("Rotate CW");
 		rotateCW.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				rotateCW();
 			}
 		});
 		this.add(rotateCW);
 		
 		JButton rotateCCW = new JButton("Rotate CCW");
 		rotateCCW.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				rotateCCW();
 			}
 		});
 		this.add(rotateCCW);
 		
 		JButton remove = new JButton("Remove");
 		remove.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				remove();
 			}
 		});
 		this.add(remove);
 		
 		this.validate();
 	}
 	
 	/**
 	 * Notify any inspectors of changes
 	 */
 	protected void refresh() {
 		InspectorPanel.signalUpdate();
 	}
 	
 	/**
 	 * Rotate the GameThing CW
 	 */
 	protected void rotateCW() {
 		Location l = thing.location();
 		if(l instanceof Level.Location) {
 			((Level.Location)l).rotate(Direction.EAST).put(thing);
 		}
 		else {
 			System.err.println("Trying to rotate something weird"); // TODO: fix
 		}
 		this.refresh();
 	}
 
 	/**
 	 * Rotate the GameThing CCW
 	 */
 	protected void rotateCCW() {
 		Location l = thing.location();
 		if(l instanceof Level.Location) {
 			((Level.Location)l).rotate(Direction.WEST).put(thing);
 		}
 		else {
 			System.err.println("Trying to rotate something weird"); // TODO: fix
 		}
 		this.refresh();
 	}
 	
 	/**
 	 * Remove the GameThing
 	 */
 	protected void remove() {
 		game.LocationS.NOWHERE.put(thing);
 		panel.builder().gameWorld().forget(thing);
 		this.refresh();
 	}
 	
 	/**
 	 * Get the game thing
 	 * @return
 	 */
 	protected T thing() {
 		return thing;
 	}
 }
