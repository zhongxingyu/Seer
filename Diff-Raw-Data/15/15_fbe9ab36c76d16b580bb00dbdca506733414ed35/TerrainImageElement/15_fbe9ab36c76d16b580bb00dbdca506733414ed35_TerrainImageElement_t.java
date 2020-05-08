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
 
 import com.soartech.simjr.util.UndoTools;
 
 /**
  * @author ray
  */
 public class TerrainImageElement implements ModelElement
 {
     public static final String ADDED = TerrainImageElement.class.getCanonicalName() + ".added";
    public static final String REMOVED = TerrainImageElement.class.getCanonicalName() + ".removed";
     public static final String HREF = TerrainImageElement.class.getCanonicalName() + ".href";
     public static final String METERS_PER_PIXEL = TerrainImageElement.class.getCanonicalName() + ".metersPerPixel";
     public static boolean isProperty(String p)
     {
         return p.startsWith(TerrainImageElement.class.getCanonicalName());
     }
     
     private final TerrainElement parent;
     private final XPath imagePath;
     private final XPath imageHref;
     private final XPath metersPerPixel;
     private final LocationElement location;
     
 
     public static TerrainImageElement attach(TerrainElement parent)
     {
         return new TerrainImageElement(parent);
     }
     
     private TerrainImageElement(TerrainElement parent)
     {
         this.parent = parent;
         
         this.imagePath = this.parent.getModel().newXPath("/simjr:scenario/simjr:terrain/simjr:image");
         this.imageHref = this.parent.getModel().newXPath("/simjr:scenario/simjr:terrain/simjr:image/@simjr:href");
         this.metersPerPixel = this.parent.getModel().newXPath("/simjr:scenario/simjr:terrain/simjr:image/@simjr:metersPerPixel");
         
         this.location = new LocationElement(this);
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getElement()
      */
     public Element getElement()
     {
         return (Element) getModel().getNode(imagePath, null);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getModel()
      */
     public Model getModel()
     {
         return getParent().getModel();
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.scenario.ModelElement#getParent()
      */
     public ModelElement getParent()
     {
         return parent;
     }
 
     public boolean hasImage()
     {
         return getModel().getNode(imagePath, null) != null;
     }
     
     public UndoableEdit clearImage()
     {
         UndoableEdit edit = null;
         Element imageElement = (Element) getModel().getNode(imagePath, null);
         if(imageElement != null)
         {
             edit = new ClearEdit();
             imageElement.getParentElement().removeContent(imageElement);
             getModel().fireChange(new ModelChangeEvent(getModel(), this, REMOVED));
         }
         return edit;
     }
     
     public String getImageHref()
     {
         return getModel().getText(imageHref, null);
     }
     
     /**
      * @return the image file as an absolute path, or null if no image is set
      */
     public File getImageFile()
     {
         if(!hasImage())
         {
             return null;
         }
         final File href = new File(getImageHref());
         if(href.isAbsolute())
         {
             return href;
         }
         final File modelFile = getModel().getFile();
         
         return modelFile != null ? new File(modelFile.getParent(), href.toString()) : href;
     }
     
     public UndoableEdit setImageHref(String href)
     {
         final UndoableEdit create = getOrCreateImageElement("", 1.0);
         final UndoableEdit set;
         final String oldHref = getImageHref();
         if(getModel().setText(imageHref, null, href, new ModelChangeEvent(getModel(), this, HREF)))
         {
             set = new SetImageHrefEdit(oldHref);
         }
         else
         {
             set = null;
         }
         return UndoTools.createCompound(create, set);
     }
     
     public double getImageMetersPerPixel()
     {
         return hasImage()? getModel().getDouble(metersPerPixel, null) : 1.0;
     }
     
     public UndoableEdit setImageMetersPerPixel(double metersPerPixel)
     {
         final UndoableEdit create = getOrCreateImageElement("", 1.0);
         final UndoableEdit set;
         final double oldValue = getImageMetersPerPixel();
         if(getModel().setText(this.metersPerPixel, null, Double.toString(metersPerPixel), new ModelChangeEvent(getModel(), this, METERS_PER_PIXEL)))
         {
             set = new SetMetersPerPixelEdit(oldValue);
         }
         else
         {
             set = null;
         }
         return UndoTools.createCompound(create, set);
     }
     
     public LocationElement getLocation()
     {
         return location;
     }

    // FIXME: To get rid of the bug where map location for the image is not written out
    // or read in properly (it is currently always zero in the xml), we need a setLocation
    // function. This doesn't seem to work though. Need to wade through all that undo/redo
    // and xpath code to figure out how to set the location properly.
//    public UndoableEdit setLocation(double latDegrees, double lonDegrees, double altitude)
//    {
//        return location.setLocation(latDegrees, lonDegrees, altitude);        
//    }
     
     private UndoableEdit getOrCreateImageElement(String href, double metersPerPixel)
     {
         final UndoableEdit edit;
         Element imageElement = (Element) getModel().getNode(imagePath, null);
         if(imageElement == null)
         {
             imageElement = buildImageElement(href, metersPerPixel);
             getParent().getElement().addContent(imageElement);
             getModel().fireChange(new ModelChangeEvent(getModel(), this, ADDED));
             
             edit = new CreateEdit();
         }
         else
         {
             edit = null;
         }
         return edit;
     }
     
     private Element buildImageElement(String href, double metersPerPixel)
     {
         Element root = getModel().newElement("image");
         root.setAttribute("href", href, Model.NAMESPACE);
         root.setAttribute("metersPerPixel", Double.toString(metersPerPixel), Model.NAMESPACE);
         root.addContent(LocationElement.buildDefault(getModel()));
         return root;
     }
 
     private class SetImageHrefEdit extends AbstractUndoableEdit
     {
         private static final long serialVersionUID = 3822586829878438892L;
         final String newHref = getImageHref();
         final String oldHref;
 
         public SetImageHrefEdit(String oldHref)
         {
             this.oldHref = oldHref;
         }
         
         @Override
         public void redo() throws CannotRedoException
         {
             super.redo();
             setImageHref(newHref);
         }
         @Override
         public void undo() throws CannotUndoException
         {
             super.undo();
             setImageHref(oldHref);
         }
     }
     
     private class SetMetersPerPixelEdit extends AbstractUndoableEdit
     {
         private static final long serialVersionUID = 1341665266098789971L;
         final double newValue = getImageMetersPerPixel();
         final double oldValue;
 
         public SetMetersPerPixelEdit(double oldValue)
         {
             this.oldValue = oldValue;
         }
         
         @Override
         public void redo() throws CannotRedoException
         {
             super.redo();
             setImageMetersPerPixel(newValue);
         }
 
         @Override
         public void undo() throws CannotUndoException
         {
             super.undo();
             setImageMetersPerPixel(oldValue);
         }
     }
        
     private class CreateEdit extends AbstractUndoableEdit
     {
         private static final long serialVersionUID = 2557435858151443282L;
         final Element element = getElement();
         final Element parent = element.getParentElement();
         final int index = parent.indexOf(element);
 
         @Override
         public void redo() throws CannotRedoException
         {
             super.redo();
             parent.addContent(index, element);
             getModel().fireChange(new ModelChangeEvent(getModel(), TerrainImageElement.this, ADDED));
         }
 
         @Override
         public void undo() throws CannotUndoException
         {
             super.undo();
             parent.removeContent(element);
             getModel().fireChange(new ModelChangeEvent(getModel(), TerrainImageElement.this, REMOVED));
         }
     }
 
     private class ClearEdit extends AbstractUndoableEdit
     {
         private static final long serialVersionUID = 2266025921911874109L;
         final String oldHref = getImageHref();
         final double oldMetersPerPixel = getImageMetersPerPixel();
 
         @Override
         public void redo() throws CannotRedoException
         {
             super.redo();
             clearImage();
         }
 
         @Override
         public void undo() throws CannotUndoException
         {
             super.undo();
             getOrCreateImageElement(oldHref, oldMetersPerPixel);
         }
     }
 }
