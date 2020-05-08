 package renderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Point2D;
 
 import javax.swing.BorderFactory;
 import javax.swing.JTextField;
 
 import engine.Base;
 
 /**
  * This class display a base.
  * @author Yuki
  *
  */
 @SuppressWarnings("serial")
 public class BaseSprite extends Sprite implements MouseListener{
 	/**
 	 * startingBase and endingBase are static variable, useful to decide in which direction the player sends units.
 	 */
 	private static Base startingBase;
 	private static Base endingBase;
 	/**
 	 * nbAgents is the JTextField which is used to display the nbAgents of the correpsonding base.
 	 */
 	private JTextField nbAgents;
 	/**
 	 * engineBase is a reference to the corresponding base of this sprite.
 	 */
 	private Base engineBase;
 
 	public BaseSprite(Base newBase) {
 		super();
 		this.setLayout(new BorderLayout());
 		
 		this.engineBase = newBase;
 		
 		this.nbAgents = new JTextField(String.valueOf(newBase.getNbAgents()));
 		this.nbAgents.setPreferredSize(new Dimension(23, 20));
 		this.nbAgents.setDisabledTextColor(new Color(255, 255, 255));
 		this.nbAgents.setEnabled(false);
 		this.nbAgents.setBorder(null);
 		this.nbAgents.setHorizontalAlignment(JTextField.CENTER);
 		this.nbAgents.setOpaque(false);
 		this.nbAgents.setIgnoreRepaint(false); // for better performance
 		this.nbAgents.addMouseListener(this);
 		this.add(this.nbAgents, BorderLayout.CENTER);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		this.setBorder(null);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		if(this.engineBase.isAPlayerBase()){
 			this.setOpaque(true);
 			this.setBackground(new Color(255, 100, 100));
 		}
 
 		if(BaseSprite.startingBase == null && this.engineBase.isAPlayerBase()) {
 			BaseSprite.startingBase = this.engineBase;
 		}
 		if (BaseSprite.startingBase != null && BaseSprite.startingBase != this.engineBase) {
 			BaseSprite.endingBase = this.engineBase;
 		}
		
		if(BaseSprite.startingBase != null && !BaseSprite.startingBase.getOwner().isPlayer()){
			BaseSprite.startingBase = null;
			BaseSprite.endingBase = null;
		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		this.setOpaque(false);
 		this.setBackground(null); // i don't know why i need to indicate a new background color...
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	
 	public static void resetStartingBase() {
 		BaseSprite.startingBase = null;
 	}
 	
 	public static void resetEndingBase() {
 		BaseSprite.endingBase = null;
 	}
 	
 	// GETTERS & SETTERS
 	
 	public Base getEngineBase() {
 		return this.engineBase;
 	}
 	
 	public JTextField getNbAgents() {
 		return this.nbAgents;
 	}
 	
 	public static Point2D.Float getStartingPoint() {
 		return BaseSprite.startingBase.getCenter();
 	}
 	
 	public static Point2D.Float getEndingPoint() {
 		return BaseSprite.endingBase.getCenter();
 	}
 	
 	public static Base getStartingBase() {
 		return BaseSprite.startingBase;
 	}
 	
 	public static Base getEndingBase() {
 		return BaseSprite.endingBase;
 	}
 
 	public static boolean isAStartingBase() {
 		return BaseSprite.startingBase == null ? false : true;
 	}
 	
 	public static boolean isAnEndingBase() {
 		return BaseSprite.endingBase == null ? false : true;
 	}
 }
