 package dk.itu.kf04.g4tw.model;
 
 import dk.itu.kf04.g4tw.model.tree.RoadRectangle;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 
 public class Road extends DijkstraEdge implements Serializable {
 
 	public final String name;
 	public final int id;
 	public final int type;
 	public final Point2D.Double from;
 	public final Point2D.Double to;
 	public double speed;
 	private double length;
 
 	/**
 	 * The rectangle enclosing this road.
 	 */
 	public final RoadRectangle rect;
 	
 	public Road(int id, String name, Point2D.Double f, Point2D.Double t, int type, double speed, double length) {
         this.id   = id;
 		this.name = name;
 		this.type = type;
 		this.from = f;
 		this.to = t;
 		this.speed = speed;
 		this.length = length;
 		
 		rect = new RoadRectangle(f, t);
 	}
     
     public int getId()        { return id; }
     
 	public double getLength() { return length; }
 
     public String toString() {
         return ("Name: "+name + "; From: ("+from.getX()+","+from.getY()+")" + " To: ("+to.getX()+","+to.getY()+")" + "; Type: "+type + "; Speed: "+speed + "; Length: "+length + ";");
     }
 
 	/**
 	 * This method converts a road into an XML element. The XML element can be inserted into a document
 	 * making searching and printing faster.
 	 *
 	 * @param doc The document used to create the elements
 	 * @return The road converted to an XML element.
 	 */
 	public Element toXML(Document doc)
 	{
 		Element road = doc.createElement("r");
 
 		Element idElement = doc.createElement("i");
 		idElement.appendChild(doc.createTextNode("" + id));
 		road.appendChild(idElement);
 
 		Element typeElement = doc.createElement("t");
 		typeElement.appendChild(doc.createTextNode("" + type));
 		road.appendChild(typeElement);
 
 		Element fxElement = doc.createElement("fx");
 		fxElement.appendChild(doc.createTextNode("" + from.x));
 		road.appendChild(fxElement);
 
 		Element fyElement = doc.createElement("fy");
 		fyElement.appendChild(doc.createTextNode("" + from.y));
 		road.appendChild(fyElement);
 
 		Element txElement = doc.createElement("tx");
 		txElement.appendChild(doc.createTextNode("" + to.x));
 		road.appendChild(txElement);
 
 		Element tyElement = doc.createElement("ty");
 		tyElement.appendChild(doc.createTextNode("" + to.y));
 		road.appendChild(tyElement);
 
 		return road;
 	}
 }
