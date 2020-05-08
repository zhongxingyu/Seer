 package io.furyhunter.ld26editor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 
 import javax.swing.JPanel;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.MouseWheelEvent;
 
 public class EditorPanel extends JPanel {
 
 	private Level level;
 	
 	private EditorMain myparent;
 	
 	public String metadata;
 	
 	public static final int GRID_SIZE = 16;
 	
 	public int offx;
 	public int offy;
 	
 	private EntityTypes type = EntityTypes.Tile;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4523300610111813322L;
 
 	/**
 	 * Create the panel.
 	 */
 	public EditorPanel(EditorMain parent) {
 		addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent e) {
				offy += e.getWheelRotation() * GRID_SIZE * 2;
 				repaint();
 			}
 		});
 		myparent = parent;
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getButton() == MouseEvent.BUTTON3) {
 					Class itstype = level.remove(e.getX() + offx, e.getY() + offy);
 					if (itstype != null) {
 						for (EntityTypes v : EntityTypes.values()) {
 							if (v.type.equals(itstype)) {
 								type = v;
 								myparent.getComboBox().setSelectedItem(v);
 								break;
 							}
 						}
 						repaint();
 					}
 				}
 				if (e.getButton() == MouseEvent.BUTTON1) {
 					Entity en = null;
 					
 					level.add(type, e.getX() - (e.getX() % GRID_SIZE) + offx, e.getY() - (e.getY() % GRID_SIZE) + offy, metadata);
 					repaint();
 				}
 			}
 		});
 		addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				repaint();
 			}
 		});
 		level = new Level();
 		
 		setPreferredSize(new Dimension(640, 480));
 	}
 	
 	public void setLevel(Level l) {
 		level = l;
		offx = 0;
		offy = 0;
 		repaint();
 	}
 	
 	public void setType(EntityTypes types) {
 		type = types;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		g.fillRect(0, 0, 640, 480);
 		level.offx = offx;
 		level.offy = offy;
 		level.draw(g);
 		
 		g.setColor(new Color(32, 32, 32));
 		for (int i = 0; i < 640; i += GRID_SIZE) {
 			g.drawLine(i, 0, i, 480);
 		}
 		
 		for (int i = 0; i < 480; i += GRID_SIZE) {
 			g.drawLine(0, i, 640, i);
 		}
 	}
 
 	public Level getLevel() {
 		return level;
 	}
 }
