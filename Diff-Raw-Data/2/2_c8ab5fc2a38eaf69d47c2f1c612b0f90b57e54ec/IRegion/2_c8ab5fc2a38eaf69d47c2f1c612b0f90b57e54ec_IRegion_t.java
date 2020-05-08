 package org.dawb.common.ui.plot.region;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 /**
  * A selection region must conform to this interface. You can set its position, colour and transparency settings.
  * 
  * @author fcp94556
  */
 public interface IRegion {
 
 	/**
 	 * @return the name of the region
 	 */
 	public String getName();
 
 	/**
 	 * The name of the region
 	 * 
 	 * @param name
 	 */
 	public void setName(String name);
 
 	/**
 	 * @return the name of the region
 	 */
 	public Label getLabel();
 
 	/**
 	 * The name of the region
 	 * 
 	 * @param name
 	 */
 	public void setLabel(Label label);
 
 	
 	/**
 	 * @return the colour of the region
 	 */
 	public Color getRegionColor();
 
 	/**
 	 * The colour of the region
 	 * 
 	 * @param regionColor
 	 */
 	public void setRegionColor(Color regionColor);
 
 	/**
 	 * @return if true, position information should be shown in the region.
 	 */
 	public boolean isShowPosition();
 
 	/**
 	 * If position information should be shown in the region.
 	 * 
 	 * @param showPosition
 	 */
 	public void setShowPosition(boolean showPosition);
 
 	/**
 	 * Alpha transparency 0-255, 0-transparent, 255-opaque
 	 * 
 	 * @return
 	 */
 	public int getAlpha();
 
 	/**
 	 * Alpha transparency 0-255, 0-transparent, 255-opaque
 	 * 
 	 * @param alpha
 	 */
 	public void setAlpha(int alpha);
 
 	/**
 	 * @return true if visible
 	 */
 	public boolean isVisible();
 
 	/**
 	 * Visibility
 	 * 
 	 * @param visible
 	 */
 	public void setVisible(boolean visible);
 
 	/**
 	 * @return true if moveable
 	 */
 	public boolean isMobile();
 
 	/**
 	 * Moveable or not
 	 * 
 	 * @param mobile
 	 */
 	public void setMobile(boolean mobile);
 
 	/**
 	 * Get the region of interest (in coordinate frame of the axis that region is added to)
 	 */
 	public ROIBase getROI();
 
 	/**
 	 * Set the region of interest (in coordinate frame of the axis that region is added to)
 	 */
 	public void setROI(ROIBase roi);
 
 	/**
 	 * Add a listener which is notified when this region is resized or moved.
 	 * 
 	 * @param l
 	 */
 	public boolean addROIListener(final IROIListener l);
 
 	/**
 	 * Remove a ROIListener
 	 * 
 	 * @param l
 	 */
 	public boolean removeROIListener(final IROIListener l);
 
 	/**
 	 * Will be called to remove the region and clean up resources when the user
 	 * calls the removeRegion(...) method.
 	 */
 	public void remove();
 
 	// some missing colours from draw2d
 	static final Color darkYellow  = new Color(null, 128, 128, 0);
 	static final Color darkMagenta = new Color(null, 128, 0, 128);
 	static final Color darkCyan    = new Color(null, 0, 128, 128);
 
 	/**
 	 * Class packages types of regions, their default names, colours and indices.
 	 * @author fcp94556
 	 *
 	 */
 	public enum RegionType {
 		LINE("Line",               ColorConstants.cyan),
 		POLYLINE("Polyline",       ColorConstants.cyan),
 		BOX("Box",                 ColorConstants.green),
 		RING("Ring",               darkYellow), 
 		XAXIS("X-Axis",            ColorConstants.blue), 
 		YAXIS("Y-Axis",            ColorConstants.blue), 
 		SECTOR("Sector",           ColorConstants.red),
 		XAXIS_LINE("X-Axis Line",  ColorConstants.blue), 
 		YAXIS_LINE("Y-Axis Line",  ColorConstants.blue), 
 		FREE_DRAW("Free draw",     darkYellow),
 		POINT("Point",             darkMagenta),
 //		ELLIPSE("Ellipse",         ColorConstants.lightGreen),
 		ELLIPSEFIT("Ellipse fit",  ColorConstants.lightGreen);
 
 		private String name;
 		private Color defaultColor;
 		
 		public static List<RegionType> ALL_TYPES = new ArrayList<RegionType>(12);
 		static {
 			for (RegionType t : EnumSet.allOf(RegionType.class))
 				ALL_TYPES.add(t);
 		}
 
 		RegionType(String name, Color defaultColor) {
 			this.name = name;
 			this.defaultColor = defaultColor;
 		}
 
 		public int getIndex() {
 			return ALL_TYPES.indexOf(this);
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Color getDefaultColor() {
 			return defaultColor;
 		}
 
 		public static RegionType getRegion(int index) {
 			return ALL_TYPES.get(index);
 		}
 
 		public String getId() {
 			return IRegion.class.getPackage().getName()+"."+toString();
 		}
 	}
 
 	/**
 	 * return the line width used for drawing any lines (if any are drawn, otherwise 0).
 	 * @return
 	 */
 	public int getLineWidth();
 	
 	/**
 	 * set the line width used for drawing any lines (if any are drawn, otherwise does nothing).
 	 * @return
 	 */
 	public void setLineWidth(int i);
 
 	/**
 	 * The type of this region
 	 * @return
 	 */
 	public RegionType getRegionType();
 	
 	/**
 	 * return true if the mouse should be tracked. The region will mouse with this tracking.
 	 * WARNING Most regions will not respond to this setting.
 	 * 
 	 * @return
 	 */
 	public boolean isTrackMouse();
 	
 	/**
 	 * return true if the mouse should be tracked.
 	 * WARNING Most regions will not respond to this setting. AxisSelection does.
      *
 	 * @return
 	 */
 	public void setTrackMouse(boolean trackMouse);
 
 	/**
 	 * 
 	 * @return true if user region. If not a user region the region has been created programmatically
 	 * and has been marked as not editable to the user.
 	 */
 	public boolean isUserRegion();
 
 	/**
 	 *  If not a user region the region has been created programmatically
 	 * and has been marked as not editable to the user.
 	 * @param userRegion
 	 */
 	public void setUserRegion(boolean userRegion);
 	
 	/**
 	 * Add Mouse listener to the region if it supports it and if it is a draw2d region.
 	 */
 	public void addMouseListener(MouseListener l);
 	
 	
 	/**
 	 * Remove Mouse listener to the region if it supports it and if it is a draw2d region.
 	 */
 	public void removeMouseListener(MouseListener l);
 
 	
 	/**
 	 * Add Mouse motion listener to the region if it supports it and if it is a draw2d region.
 	 */
 	public void addMouseMotionListener(MouseMotionListener l);
 	
 	/**
 	 * Remove Mouse motion listener to the region if it supports it and if it is a draw2d region.
 	 */
 	public void removeMouseMotionListener(MouseMotionListener l);
 	
 	/**
 	 * This method will send the figure back to the start of its
 	 * parents child list. This results in it being underneath the other children.
 	 */
 	public void toBack();
 
 	
 	/**
 	 * This method will send the figure to the end of its
 	 * parents child list. This results in it being above the other children.
 	 */
 	public void toFront();
 
 	/**
 	 *
 	 * @return true if the region should be used in a mask operation.
 	 */
 	public boolean isMaskRegion();
 	
 	/**
 	 * Set the mask boolean. By default this will be false but when true the region
 	 * is marked as being part of mask operations.
 	 * 
 	 * @param isMaskRegion
 	 */
 	public void setMaskRegion(boolean isMaskRegion);
 	
 	
 	/**
 	 * Converts the x and y location in the axis coordinate to 
 	 * pixels and checks if the region is covering this location.
 	 * 
	 * NOT Thread safe!
	 * 
 	 * @param x
 	 * @param y
 	 */
 	public boolean containsPoint(double x, double y);
 }
