 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Mar 28, 2009
  */
 package com.soartech.simjr.scenario;
 
 import java.io.File;
 
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoableEdit;
 
 import org.jdom.Element;
 import org.jdom.xpath.XPath;
 
 /**
  * @author ray
  */
 public class TerrainElement implements ModelElement
 {
     public static final String ORIGIN = TerrainElement.class.getCanonicalName() + ".origin";
     
     private final Model model;
     private final XPath terrainPath;
     private final XPath latPath;
     private final XPath lonPath;
     private final TerrainImageElement image;
     private final TerrainTypeElement terrainType;
     
     public static Element buildDefault(Model model)
     {
         final Element root = model.newElement("terrain");
         
         final Element origin = model.newElement("origin");
         origin.setAttribute("latitude", "0.0", Model.NAMESPACE);
         origin.setAttribute("longitude", "0.0", Model.NAMESPACE);
         origin.setAttribute("altitude", "0.0", Model.NAMESPACE);
         root.addContent(origin);
         
         return root;
     }
 
     public static TerrainElement attach(Model model)
     {
         return new TerrainElement(model);
     }
     
     private TerrainElement(Model model)
     {
         this.model = model;
         
         this.terrainPath = this.model.newXPath("/simjr:scenario/simjr:terrain");
         this.latPath = this.model.newXPath("/simjr:scenario/simjr:terrain/simjr:origin/@simjr:latitude");
         this.lonPath = this.model.newXPath("/simjr:scenario/simjr:terrain/simjr:origin/@simjr:longitude");
         
         this.image = TerrainImageElement.attach(this);
         this.terrainType = TerrainTypeElement.attach(this);
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getElement()
      */
     public Element getElement()
     {
         return (Element) this.model.getNode(terrainPath, null);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getModel()
      */
     public Model getModel()
     {
         return model;
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getParent()
      */
     public ModelElement getParent()
     {
         return null;
     }
 
     public double getOriginLatitude()
     {
         return this.model.getDouble(latPath, null);
     }
  
     public double getOriginLongitude()
     {
         return this.model.getDouble(lonPath, null);
     }
     
     public void relativizePaths(File base)
     {
         image.relativizePaths(base);
         terrainType.relativizePaths(base);
     }
     
     public UndoableEdit setOrigin(double latDegrees, double lonDegrees)
     {
         final double oldLat = getOriginLatitude();
         final double oldLon = getOriginLongitude();
         boolean latChanged = model.setText(latPath, null, Double.toString(latDegrees), null);
         boolean lonChanged = model.setText(lonPath, null, Double.toString(lonDegrees), null);
         
         UndoableEdit edit = null;
         if(latChanged || lonChanged)
         {
             model.fireChange(new ModelChangeEvent(model, this, ORIGIN));
            
             edit = new ChangeTerrainOriginEdit(oldLat, oldLon);
            edit.addEdit(image.getLocation().setLatitude(latDegrees));
            edit.addEdit(image.getLocation().setLongitude(lonDegrees));
         }
         return edit;
     }
     
     public TerrainImageElement getImage()
     {
         return image;
     }
     
     public TerrainTypeElement getTerrainType()
     {
     	return terrainType;
     }
     
     private class ChangeTerrainOriginEdit extends AbstractUndoableEdit
     {
         private static final long serialVersionUID = -8050360351980227293L;
 
         private final double oldLat, oldLon;
         private final double newLat = getOriginLatitude(), newLon = getOriginLongitude();
         
         public ChangeTerrainOriginEdit(double oldLat, double oldLon)
         {
             this.oldLat = oldLat;
             this.oldLon = oldLon;
         }
 
         /* (non-Javadoc)
          * @see javax.swing.undo.AbstractUndoableEdit#redo()
          */
         @Override
         public void redo() throws CannotRedoException
         {
             super.redo();
             setOrigin(newLat, newLon);
         }
 
         /* (non-Javadoc)
          * @see javax.swing.undo.AbstractUndoableEdit#undo()
          */
         @Override
         public void undo() throws CannotUndoException
         {
             super.undo();
             setOrigin(oldLat, oldLon);
         }
     }
 }
