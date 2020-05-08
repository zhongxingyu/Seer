 package com.vesalaakso.rbb.model;
 
 import java.util.Properties;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.tiled.GroupObject;
 
 import com.vesalaakso.rbb.model.exceptions.MapException;
 
 /**
  * Represents a single GroupObject, in an easier-to-digest way.
  * 
  * @author Vesa Laakso
  */
 public class TileMapObject {
 
 	/** The index of this object */
 	public final int index;
 	/** The name of this object */
 	public final String name;
 	/** The type of this object */
 	public final TileMapObjectType type;
 	/** The object type of this object */
 	public final GroupObject.ObjectType objectType;
 	/** The x-coordinate of this object */
 	public final int x;
 	/** The y-coordinate of this object */
 	public final int y;
 	/** The width of this object */
 	public final int width;
 	/** The height of this object */
 	public final int height;
 	/** the properties of this object */
 	public final Properties props;
 
 	/** The underlying GroupObject */
 	private final GroupObject groupObject;
 
 	/**
 	 * Constructs a new object based on the given <code>GroupObject</code>.
 	 * 
 	 * @param obj
 	 *            the GroupObject to base this new object on
 	 * @throws MapException
 	 *             if the type of this object was unknown.
 	 */
 	public TileMapObject(GroupObject obj) throws MapException {
 		groupObject = obj;
 
 		index = obj.index;
 		name = obj.name;
 		try {
			type = TileMapObjectType.valueOf(obj.name.toUpperCase());
 		}
 		catch (IllegalArgumentException e) {
 			throw new MapException("Unknown object type: " + obj.type, e);
 		}
 		objectType = obj.getObjectType();
 		x = obj.x;
 		y = obj.y;
 		width = obj.width;
 		height = obj.height;
 		props = obj.props;
 	}
 
 	/**
 	 * Returns the <code>Polygon</code> used to construct a polygon shape, if
 	 * this shape is indeed a polygon.
 	 * 
 	 * @return <code>Polygon</code> constructing this object
 	 * @throws MapException
 	 *             if the shape was not a polygon
 	 */
 	public Polygon getPolygon() throws MapException {
 		if (objectType != GroupObject.ObjectType.POLYGON) {
 			String str = String.format(
 					"The object \"%s\" at (%d, %d) was not a polygon!",
 					name, x, y);
 			throw new MapException(str);
 		}
 
 		try {
 			return groupObject.getPolygon();
 		}
 		catch (SlickException e) {
 			throw new MapException(String.format(
 					"Couldn't get polygon for the object \"%s\" at (%d, %d)",
 					name, x, y), e);
 		}
 	}
 	
 	/**
 	 * Gets the <code>String</code> representation of this
 	 * <code>TileMapObject</code>.
 	 */
 	@Override
 	public String toString() {
 		String str = "TileMapObject={"
 				+ "index:" + index
 				+ ",name:" + name
 				+ ",type:" + type
 				+ ",objectType:" + objectType
 				+ ",x:" + x
 				+ ",y:" + y
 				+ ",width:" + width
 				+ ",height:" + height
 				+ ",props:" + props
 				+ "}";
 		return str;
 	}
 }
