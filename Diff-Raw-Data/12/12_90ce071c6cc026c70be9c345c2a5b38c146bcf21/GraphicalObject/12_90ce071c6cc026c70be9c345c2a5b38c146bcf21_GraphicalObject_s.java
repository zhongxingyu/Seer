 package jmathlib.core.graphics;
 
 import java.awt.*;
 import jmathlib.core.graphics.properties.*;
 
 /** implementation of a line object*/
 public abstract class GraphicalObject extends HandleObject
 {  
 
 	/** common properties to all objects*/
     public RadioProperty BusyActionP = new RadioProperty(this, "BusyAction", new String[] {"cancel", "queue"},"cancel");
 
     public StringProperty ButtonDownFcnP = new StringProperty(this, "ButtonDownFcn", "");
     public HandleObjectListProperty ChildrenP = new HandleObjectListProperty(this, "Children", -1);
     public BooleanProperty ClippingP = new BooleanProperty(this, "Clipping", true);
     // public CurrentFigureP
     public BooleanProperty DiaryP = new BooleanProperty(this, "Diary", true);
     public StringProperty  DiaryFileP = new StringProperty(this, "DiaryFile", "");
     public BooleanProperty EchoP = new BooleanProperty(this, "Echo", true);
     public StringProperty  ErrorMessageP     = new StringProperty(this, "ErrorMessage", "");
     public RadioProperty   FormatSpacingP    = new RadioProperty(this, "FormatSpacing", new String[] {"compact", "loose"},"compact");
     public BooleanProperty HandleVisibilityP = new BooleanProperty(this, "HandleVisibility", true);
     public BooleanProperty HitTestP          = new BooleanProperty(this, "HitTest", true);
     public BooleanProperty InterruptibleP    = new BooleanProperty(this, "Interruptible", true);
     public StringProperty  LanguageP         = new StringProperty(this, "Language", "");
     // public ParentP
     public BooleanProperty SelectedP           = new BooleanProperty(this, "Selected", true);
     public BooleanProperty SelectionHighlightP = new BooleanProperty(this, "SelectionHighlight", true);
     public BooleanProperty ShowHiddenHandlesP  = new BooleanProperty(this, "ShowHiddenHandles", false);
     public StringProperty  TagP                = new StringProperty(this, "Tag", "");
     public TypeProperty TypeP = new TypeProperty(this, "root");
     // private UserDataP
     public BooleanProperty VisibileP = new BooleanProperty(this, "Visible", true);
 	
 
     /** parent axes */
 	//HandleObjectListProperty Parent = new HandleObjectListProperty(this, "Parent",     1);
     protected GraphicalObject parent = null;
     
 	/** Origin of curves */
     public int xOrig;
     public int yOrig;
     public int zOrig;
 
 	/* Size of area to plot */
     public int width;
     public int height;
 
 	/* boundary values */
	protected double xmin;
	protected double xmax;
	protected double ymin;
	protected double ymax;
	protected double zmin;
	protected double zmax;
 
 	/* axes boundaries */
 	public double ax_xmin;
 	public double ax_xmax;
 	public double ax_ymin;
 	public double ax_ymax;
 	public double ax_zmin;
 	public double ax_zmax;
 
 	public Matrix3D mat  = new Matrix3D();		//?
 
 	public GraphicalObject()
 	{
 
 	}
 
 	public double getXMin()
 	{
 		return xmin;
 	}
 
 	public double getXMax()
 	{
 		return xmax;
 	}
 
 	public double getYMin()
 	{
 		return ymin;
 	}
 
 	public double getYMax()
 	{
 		return ymax;
 	}
 
 	public double getZMin()
 	{
 		return zmin;
 	}
 
 	public double getZMax()
 	{
 		return zmax;
 	}
 
 	/** Sets the area on the screen in which the line must be plotted */
 	public void setPlotArea(int _xOrig, int _yOrig, int _width, int _height)
 	{
 		xOrig  = _xOrig;
 		yOrig  = _yOrig;
 		width  = _width;
 		height = _height;
 	}
 
 	/** Sets the boundaries for the physical values of the line, this is used
 	  for graphs with multiple line */ 
 	public void setAxesBoundaries(double _xmin, double _xmax, double _ymin, double _ymax, double _zmin, double _zmax)
 	{
 		ax_xmin = _xmin;
 		ax_xmax = _xmax;
 		ax_ymin = _ymin;
 		ax_ymax = _ymax;
 		ax_zmin = _zmin;
 		ax_zmax = _zmax;
 	}
 	
 	public void setAxesBoundaries(double _xmin, double _xmax, double _ymin, double _ymax)
 	{
 		setAxesBoundaries(_xmin, _xmax, _ymin, _ymax, -0.5, 0.5);
 	}
 
 	public void paint(Graphics g)
 	{
 	    jmathlib.core.interpreter.Errors.throwMathLibException("GraphicalObject: paint");
 	}
 
     public void repaint()
     {
         jmathlib.core.interpreter.Errors.throwMathLibException("GraphicalObject: repaint");
     }
 
     // register a parent, so that the children can call methods of their parent
 	public void setParent(GraphicalObject parent)
 	{
 		//Parent.addElement(parent);
 	    this.parent = parent;
     }
 }
 
