 
 package gui.components;
 
 import java.awt.event.ActionEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 // Andrew fixed a minor bug in this class. Search "Andrew" (without quotes) to find it.
 
 /**
  * Class for animation of sensor
  */
 @SuppressWarnings("serial")
 public class GUISensor extends GuiComponent
 {
 	/**
 	 * ImageIcons for sensor
 	 */
 	public static final ImageIcon sensor = new ImageIcon("imageicons/sensorAnimated.gif");
 
 	public static final ImageIcon sensorOn = new ImageIcon("imageicons/ConveyorSensorRed.png");
 
 	public static final ImageIcon sensorOff = new ImageIcon("imageicons/ConveyorSensorGreen.png");
 
 	int counter = 8;
 
 	/**
 	 * The rectangle of the glass pane
 	 */
 	Rectangle2D glassRect;
 
 	ArrayList<GUIGlass> activePieces;
 
 	GUIGlass currentGlassPiece;
 
 	/** Index of the conveyor */
 	private Integer myIndex;
 
 	/** Whether the sensor is pressed */
 	private boolean pressed = false;
 
 	public Integer getIndex()
 	{
 		return myIndex;
 	}
 
 	public void setIndex(int index)
 	{
 		myIndex = index;
 	}
 
 	/**
 	 * Public constructor for GUISensor
 	 */
 	public GUISensor(Transducer t)
 	{
 		glassRect = new Rectangle2D.Double();
 		setIcon(sensorOff);
 		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
 		setupRect();
 		transducer = t;
 		transducer.register(this, TChannel.SENSOR);
 	}
 
 	/**
 	 * Moves the sensor's rectangle and sets its width and height to match that of the JLabel
 	 */
 	public void setupRect()
 	{
 		glassRect.setRect(getX(), getY(), getIcon().getIconWidth(), getIcon().getIconHeight());
 	}
 
 	/**
 	 * Getter for background rectangle
 	 * @return the background rectangle of the sensor
 	 */
 	public Rectangle2D getRect()
 	{
 		return glassRect;
 	}
 
 	/**
 	 * The actionPerformed will check if the sensor should be showing On or Off
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		checkGlassDetected();
 	}
 
 	/**
 	 * If the state is equal to false then it will check through
 	 * all active glass pieces to see if it interesects with the sensor
 	 * using guiX and guiY, if it finds one it will update its icon set its
 	 * state to true. if the state is true then it will wait for the current
 	 * piece of glass to move off the sensor then it will change it's icon back to
 	 * the red sensor icon.
 	 */
 	public void checkGlassDetected()
 	{
 		// Will update it's current list with the current roster of GuiGlassPieces
 		activePieces = parent.getActivePieces();
 		if (pressed == false)
 		{
 			// This is if the sensor is not pressed
 			// it will constantly check if it's intersecting
 			// with any guiglasspiece in the activeGlassPieces
 			// list.
 			for (int k = 0; k < this.activePieces.size(); k++)
 			{
 
 				// Will basically check a box that is range by range big around it's current guiX and guiY
 				//
 				if (activePieces.get(k).getCenterX()>=getCenterX()-10&&activePieces.get(k).getCenterX()<=getCenterX()+10&&activePieces.get(k).getCenterY()>=getCenterY()-10&&activePieces.get(k).getCenterY()<=getCenterY()+10)
 				{
 					setIcon(sensorOn);
 					// If one does intersect it will change it's icon to pressed(green)
 
 					// modifies it's current state to true - pressed
 					this.pressed = true;
 
 					// Sets the current piece to the piece that the sensor intersects with
 					this.currentGlassPiece = activePieces.get(k);
 
 					// Notifies the agent that the sensor has been pressed.
 					Object[] args = new Object[2];
 					args[0] = myIndex;
 					args[1] = k;
 
 					transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
 				}
 			}
 		}
 		else
 		{
 			// If it is already pressed then it will wait til
 			// it's current glass piece no longer intersects
 			// within a certain range and will reset it's icon
 			// to red(not pressed).
 
 			// Andrew fixed a bug on the line below.
 			// Before, if the glass stopped at the very edge of the sensor, the sensor would continuously flicker on and off.
			if (currentGlassPiece.getCenterX()<getCenterX()-10||currentGlassPiece.getCenterX()>getCenterX()+10||currentGlassPiece.getCenterY()<getCenterY()-10||currentGlassPiece.getCenterY()>getCenterY()+10)
 			{
 				// Redraws the sensor to it's red icon
 				setIcon(sensorOff);
 
 				// Resets the state to false: ie not pressed
 				pressed = false;
 				currentGlassPiece = null;
 
 				// Notifies the agent
 				Object[] args = new Object[1];
 				args[0] = myIndex;
 				transducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
 			}
 		}
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		// should never happen, since the sensors receive no messages from agent
 	}
 }
