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
 package org.jdesktop.wonderland.common.cell.state;
 
 import java.io.Serializable;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
 
 /**
  * The ColladaCellSetup class is the cell that renders a collada model cell in
  * world.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @XmlRootElement(name="model-cell-component")
 @ServerState
 public class ModelCellComponentServerState extends CellComponentServerState implements Serializable {
     
     /* The URI of the static deployedModelURL file */
     @XmlElement(name="deployedModelURL")
     public String deployedModelURL = null;
 
 
     /** Default constructor */
     public ModelCellComponentServerState() {
     }
 
 
 
     @Override
     @XmlTransient public String getServerComponentClassName() {
         return "org.jdesktop.wonderland.server.cell.ModelCellComponentMO";
     }
 
     public CellComponentServerState clone(CellComponentServerState state) {
         ModelCellComponentServerState ret = (ModelCellComponentServerState) state;
         if (ret == null)
             ret = new ModelCellComponentServerState();
 
         ret.deployedModelURL = this.deployedModelURL;
 
         return ret;
     }
 
     public CellComponentClientState setClientState(ModelCellComponentClientState state) {
         state.setDeployedModelURL(deployedModelURL);
 
         return state;
     }
 
     /**
      * Returns the deployedModelURL URI, this is the URI for the .dep file.
      *
      * @return The deployedModelURL URI specification
      */
     @XmlTransient public String getDeployedModelURL() {
         return this.deployedModelURL;
     }
 
     /**
      * Sets the deployedModelURL URI. If null, then this property will not be written
      * out to the file.
      *
      * @param deployedModelURL The deployedModelURL URI
      */
     public void setDeployedModelURL(String deployedModelURL) {
         this.deployedModelURL = deployedModelURL;
     }
 }
