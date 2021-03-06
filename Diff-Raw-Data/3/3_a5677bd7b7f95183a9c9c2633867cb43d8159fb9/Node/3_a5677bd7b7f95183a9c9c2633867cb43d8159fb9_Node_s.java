 package dk.itu.big_red.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 import org.eclipse.draw2d.geometry.PointList;
 import dk.itu.big_red.model.Control.Shape;
 import dk.itu.big_red.model.assistants.CloneMap;
 import dk.itu.big_red.model.interfaces.IChild;
 import dk.itu.big_red.model.interfaces.IControl;
 import dk.itu.big_red.model.interfaces.INode;
 import dk.itu.big_red.model.interfaces.IParent;
 import dk.itu.big_red.model.interfaces.IPort;
 import dk.itu.big_red.model.interfaces.ISite;
 import dk.itu.big_red.util.Utility;
 import dk.itu.big_red.util.geometry.Geometry;
 import dk.itu.big_red.util.geometry.Rectangle;
 
 /**
  * 
  * @author alec
  * @see INode
  */
 public class Node extends Container implements PropertyChangeListener, INode {
 	private ArrayList<Port> ports = new ArrayList<Port>();
 	
 	/**
 	 * Returns a new {@link Node} with the same {@link Control} as this one.
 	 */
 	@Override
 	protected Node newInstance() {
 		try {
 			return new Node(control);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public Node(Control control) {
 		control.addPropertyChangeListener(this);
 		this.control = control;
 		
 		ports = control.getPortsArray();
 		for (Port p : ports)
 			p.setParent(this);
 		
 		if (!control.isResizable())
 			super.setLayout(
 				getLayout().getCopy().setSize(control.getDefaultSize()));
 	}
 	
 	@Override
 	public Node clone(CloneMap m) {
 		Node n = (Node)super.clone(m);
 		n.setFillColour(getFillColour().getCopy());
 		n.setOutlineColour(getOutlineColour().getCopy());
 		if (m != null) {
 			/* Manually claim that the new Node's Ports are clones. */
 			for (Port i : getPorts()) {
 				for (Port j : n.getPorts()) {
 					if (i.getName().equals(j.getName())) {
 						m.setCloneOf(i, j);
 						break;
 					}
 				}
 			}
 		}
 		return n;
 	}
 	
 	private Control control = null;
 	
 	@Override
 	public boolean canContain(Layoutable child) {
 		Class<? extends Layoutable> c = child.getClass();
 		return (c == Node.class || c == Site.class);
 	}
 	
 	@Override
 	public void setLayout(Rectangle layout) {
 		if (!control.isResizable())
 			layout.setSize(getLayout().getSize());
 		fittedPolygon = null;
 		super.setLayout(layout);
 	}
 	
 	/**
 	 * Returns the {@link Control} of this Node.
 	 * @return a Control
 	 */
 	public Control getControl() {
 		return control;
 	}
 
 	public ArrayList<Port> getPorts() {
 		return ports;
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent arg) {
 		System.out.println(this + ": unexpected property change notification of type " + arg.getPropertyName());
 	}
 	
 	private PointList fittedPolygon = null;
 	
 	/**
 	 * Lazily creates and returns the <i>fitted polygon</i> for this Node (a
 	 * copy of its {@link Control}'s polygon, scaled to fit inside this Node's
 	 * layout).
 	 * 
 	 * <p>A call to {@link #setControl} or {@link #setLayout} will invalidate
 	 * the fitted polygon.
 	 * @return the fitted polygon
 	 */
 	public PointList getFittedPolygon() {
 		if (fittedPolygon == null)
 			if (getControl().getShape() == Shape.SHAPE_POLYGON)
 				fittedPolygon = Geometry.fitPolygonToRectangle(getControl().getPoints(), getLayout());
 		return fittedPolygon;
 	}
 
 	@Override
 	public IParent getIParent() {
 		return (IParent)getParent();
 	}
 
 	@Override
 	public Iterable<INode> getINodes() {
 		return Utility.only(children, INode.class);
 	}
 
 	@Override
 	public Iterable<? extends IPort> getIPorts() {
 		return ports;
 	}
 
 	@Override
 	public Iterable<ISite> getISites() {
 		return Utility.only(children, ISite.class);
 	}
 	
 	@Override
 	public Iterable<IChild> getIChildren() {
 		return Utility.only(children, IChild.class);
 	}
 	
 	@Override
 	public IControl getIControl() {
 		return control;
 	}
 }
