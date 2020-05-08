 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.server.spatial.impl;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Vector3f;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author paulby
  */
 class SpaceManagerGridImpl implements SpaceManager {
 
    static final int SPACE_SIZE = 25; // Radius
 
     private HashMap<String, Space> spaces = new HashMap();
  
     // The spaces must overlap slightly so that the view does not land between 2 spaces
     // Fudge is only required during debugging, remove once the debug test in getEnclosingSpace is removed
     private static final float fudge = 1.0001f;
 
     public void initialize() {
     }
     
     /**
      * Return the space that encloses this point, if the space does not exist, create it
      * @param position
      * @return
      */
     public Iterable<Space> getEnclosingSpace(BoundingVolume volume) {
         ArrayList retList = new ArrayList();
 
         Vector3f point = volume.getCenter();
 
         int x = (int) (point.x / (SPACE_SIZE*2));
         int y = (int) (point.y / (SPACE_SIZE*2));
         int z = (int) (point.z / (SPACE_SIZE*2));
         
         if (point.x<0) x-=1;
         if (point.y<0) y-=1;
         if (point.z<0) z-=1;
 
         // Get the space that encloses the center of the volume
         Space sp = getEnclosingSpaceImpl(x,y,z);
         
         if (sp==null) {
             sp = createSpace(x, y, z);
 //            System.err.println("Created space "+sp.getName());
         }
 
         // Debug test
         if (!sp.getWorldBounds().contains(point))
             throw new RuntimeException("BAD ENCLOSING SPACE "+sp.getWorldBounds()+"  does not contain "+point+"   name "+getSpaceBindingName(x, y, z));
         
         retList.add(sp);
 
         // Now get all the other spaces within the volume
         float radius;
         if (volume instanceof BoundingBox) {
             radius = ((BoundingBox)volume).xExtent;
         } else if (volume instanceof BoundingSphere) {
             radius = ((BoundingSphere)volume).getRadius();
         } else
             throw new RuntimeException("Bounds not supported "+volume.getClass().getName());
 
         int step = (int) (radius / (SPACE_SIZE * 2));
 //        System.out.println("RADIUS "+radius+"  step "+step);
 //        System.out.println("Current "+x+", "+y+", "+z);
 //        System.err.println("In space "+getSpaceBindingName(x, y, z)+"   step="+step);
         // TODO this is brute force, is there a better way ?
         for(int xs=-step; xs<=step; xs++) {
             for(int ys=-step; ys<=step; ys++) {
                 for(int zs=-step; zs<=step; zs++) {
 //                    System.out.println("Checking "+(x+xs)+", "+(y+ys)+", "+(z+zs));
                     sp = getEnclosingSpaceImpl(x+xs, y+ys, z+zs);
                     if (sp==null){
                         // Create the space
                         sp = createSpace(x+xs, y+ys, z+zs);
 //                        System.out.println("Creating "+(x+xs)+", "+(y+ys)+", "+(z+zs)+"  "+sp.getWorldBounds());
                     }
                     if (sp!=null && sp.getWorldBounds().intersects(volume)) {
                         retList.add(sp);
                     }
                 }
             }
         }
 
         return retList;
     }
 
     private Space createSpace(int x, int y, int z) {
         
         Vector3f center = new Vector3f((x * SPACE_SIZE*2)+SPACE_SIZE, 
                                        (y * SPACE_SIZE*2)+SPACE_SIZE, 
                                        (z * SPACE_SIZE*2)+SPACE_SIZE);
         BoundingBox gridBounds = new BoundingBox(center, 
                                                  SPACE_SIZE * fudge, 
                                                  SPACE_SIZE * fudge, 
                                                  SPACE_SIZE * fudge);
 
         String bindingName = getSpaceBindingName(x, y, z);
         Space space = new Space(gridBounds, bindingName);
         synchronized(spaces) {
             spaces.put(bindingName, space);
         }
         
         return space;
     }
     
     /**
      * Return the space that encloses this point, or null if the space does not exist.
      * @param position
      * @return
      */
     private Space getEnclosingSpaceImpl(int x, int y, int z) {
         synchronized(spaces) {
             return spaces.get(getSpaceBindingName(x,y,z));
         }
     }
     
     private String getSpaceBindingName(int x, int y, int z) {
         return x+"_"+y+"_"+z;
     }
  
 }
