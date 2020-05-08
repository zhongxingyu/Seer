 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.7 $
 * $Date: 2007-09-25 12:22:21 $
 * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.view.document;
 
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.Shape;
 
 import javax.swing.JLabel;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.ComponentView;
 import javax.swing.text.Element;
 import javax.swing.text.Position;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.otrunk.view.OTViewContainerPanel;
 
 public class OTDocumentObjectView extends ComponentView
 {
 	AbstractOTDocumentView docView;
     OTViewContainerPanel viewContainerPanel;
 	
     /**
      * A new object view will be created each time the text of the 
      * document is updated.  So this object needs to be careful
      * about the listeners it adds to things.  
      * One option would be to cache the panels used to display
      * the objects in documentView, and that way new views don't
      * need to be created each time the text is changed.
      * 
      * @param elem
      * @param doc
      * @param docView
      */
     public OTDocumentObjectView(Element elem, AbstractOTDocumentView docView)
     {
     	super(elem);
     	this.docView = docView;
        viewContainerPanel = docView.createtViewContainerPanel();
     }
 
     protected Component createComponent() 
     {
     	AttributeSet attr = getElement().getAttributes();
     	String refId = (String) attr.getAttribute("refid");
     	String editStr = (String) attr.getAttribute("editable");
     	String viewId = (String) attr.getAttribute("viewid");
 
     	// By convention this attribute should only be used by OTXHTMLViews
     	// which need to change the mode of view.  If this is used in the 
     	// actual text of a document it will prevent the mode abstraction
     	// from working.    Most like you should use the viewid attribute
     	// instead
     	String modeStr = (String) attr.getAttribute("mode");
     	
     	if(editStr != null && editStr.equalsIgnoreCase("false"))
     	{
     		System.err.println("Warning: the editable attribute in object elements in documents is no longer supported");
     	}
     	
     	if(refId != null && refId.length() > 0) {
         	OTObject childObject = docView.getReferencedObject(refId);        	
         	if(childObject == null) {
         		return new JLabel("Bad OTID: " + refId);
         	}
 
         	OTViewEntry viewEntry = null;
         	if(viewId != null && viewId.length() > 0) {
         		viewEntry = (OTViewEntry)
         			docView.getReferencedObject(viewId);
         	}
 
         	if(modeStr != null){
         		if(modeStr.length() == 0){
         			modeStr = null;
         		}
         		viewContainerPanel.setViewMode(modeStr);
         	}
         	
         	viewContainerPanel.setCurrentObject(childObject, 
         			viewEntry);
 
         	// CHECKME we will probably have problems when this panel
         	// changes sizes after its content is loaded
     		return viewContainerPanel;
     	}
     	
     	return null;       	
     }
 
     
     /**
      * This method is to fix a bug in the HTMLEditorKit
      * It was taken from the way an ImageView is implemented in 
      * javax.swing.text.html.
      * The ComponentView implementation of this method returns:
      *   startPos with a forward bias if the x pos is left of the
      * mid point.
      *   endPos with a backward bias if the x pos is right of the 
      * mid point.
      * The mouse handler in the editor kit looks up these positions.
      * It then uses the returned pos to get the appropriate document
      * element.  Apparently the elementLookup method really returns
      * the element AFTER the requested pos.  So if this method returned
      * the endPos then that doesn't give the correct value for
      * elementLookup.   In some cases the editor kit is smart and
      * checks the bias.  In this case it subtracts 1 so the correct
      * element is found.  But in the "clicking" case it doesn't subtract
      * 1.
      * So because we can't change the editor kit we'll change this method
      * instead.
      */
     public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) 
     {
     	Rectangle alloc = (Rectangle) a;
     	if (x < alloc.x + alloc.width) {
     		bias[0] = Position.Bias.Forward;
     		return getStartOffset();
     	}
     	bias[0] = Position.Bias.Backward;
     	return getEndOffset();
     }        
 }
