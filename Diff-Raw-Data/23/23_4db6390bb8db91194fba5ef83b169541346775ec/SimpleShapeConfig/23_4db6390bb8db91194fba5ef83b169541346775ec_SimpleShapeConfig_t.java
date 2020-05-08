 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.testcells.common.cell.config;
 
 import org.jdesktop.wonderland.common.cell.config.CellConfig;
 
 /**
  * Configuration for the SimpleShapeCell
  *
  * @author paulby
  */
 public class SimpleShapeConfig extends CellConfig {
 
    public enum Shape { BOX, CONE, CYLINDER, SPHERE, TEAPOT };
 
     private Shape shape;
 
    private float mass;

     public SimpleShapeConfig(Shape shape) {
         this.shape = shape;
     }
 
    public SimpleShapeConfig(Shape shape, float mass) {
        this.shape = shape;
	this.mass = mass;
    }

     public Shape getShape() {
         return shape;
     }

    public float getMass() {
        return mass;
    }
 }
