/**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis;
 
 import com.xith3d.scenegraph.Geometry;
 
 import javax.vecmath.Point3f;
 
 /**
  * A geometry parent class used by the application
  * to store geometry information relevant to the input
 * and output files supported within the program.
  *
  * Note that at this level this DVGeometry class is not
  * used by the 3D rendering engine, it is merely an
 * intermediary class used to assisst with the application
  * logic.
  */
 public abstract class DVGeometry
 {
 	/**
 	 * A string id associated with a particular geometry.
 	 * This is typically used if multiple shapes share a
 	 * common geometry, the id is used as a way of referencing
	 * the geometry between the shapes.
 	 *
 	 * Note that this isn't necessary programmatically, but
 	 * the input model files created by users can have an id
 	 * set manually when creating a file, so it is needed from
 	 * that sense (to save the file back as the user created it).
 	 */
 	String id = null;
 
 	/**
 	 * Returns the String id of the geometry. If no id
 	 * is set, null is returned.
 	 *
 	 * @return	the id of the shape
 	 */
 	public String getId()
 	{
 		return id;
 	}
 
 	/**
 	 * Sets the is for the geometry.
 	 * @param id	the geometry's id
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * Converts the object to a geometry shape supported by
 	 * the 3D rendering object.
 	 *
 	 * @param location	the coordinate of the geometry
 	 * @return			the geometry supported by the 3D rendering
 	 * 					engine
 	 */
 	public abstract Geometry toGeometry(Point3f location);
 
 	/**
 	 * Converts the object to a geometry shape supported by
 	 * the 3D rendering object.
 	 *
 	 * @param startPosition		the starting coordinates of the geometry
 	 * @param endPosition		then ending coordinates of the geometry
 	 * @return					the geometry supported by the 3D rendering
 	 * 							engine
 	 */
 	public abstract Geometry toGeometry(Point3f startPosition, Point3f endPosition);
 }
