 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.ui.eclipse;
 
 import java.awt.Color;
 
 import de.tuilmenau.ics.fog.ui.Decorator;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This  class is used to decorate HRM managed nodes
  */
 public class NodeDecorator implements Decorator
 {
 	/**
 	 * Stores the path to the image, which should be used to decorate the node
 	 */
 	private String mNodeDecorationImage = "";
 	
 	/**
 	 * Stores the text, which should be used to decorate the node
 	 */
 	private String mNodeDecorationText = "";
 
 	/**
 	 * Constructor
 	 */
 	public NodeDecorator()
 	{
 		
 	}
 	
 	/**
 	 * Sets the new text for node decoration
 	 * 
 	 * @param pNewText the new text
 	 */
 	public void setText(String pNewText)
 	{
 		mNodeDecorationText = pNewText;
 
		Logging.log(this, "DECORATION - new text: " + mNodeDecorationText);
 	}
 	
 	/**
 	 * Sets the new image for node decoration
 	 * 
 	 * @param pLevel the hierarchy level. which is used to derive the correct image for node decoration
 	 */
 	public void setImage(int pLevel)
 	{
 		if (pLevel >= 0)
 //			try {
 				mNodeDecorationImage = "de.tuilmenau.ics.fog.routing.hierarchical.Coordinator_Level" + Integer.toString(pLevel) + ".gif"; //Resources.locateInPlugin(PLUGIN_ID, PATH_ICONS, "Coordinator_Level" + Integer.toString(pLevel) + ".gif");
 //			} catch (FileNotFoundException tExc) {
 //				Logging.warn(this, "Unable to determine the path of a decoration image", tExc);
 //				mNodeDecorationImage = null;
 //			}
 		else
 			mNodeDecorationImage = null;
 		
		Logging.log(this, "DECORATION - new image: " + mNodeDecorationImage);
 	}
 	
 	/**
 	 * Defines the decoration color for the node
 	 * 
 	 * @return color for the node or null if no specific color is available
 	 */
 	@Override
 	public Color getColor()
 	{
 		return null;
 	}
 	
 	
 	/**
 	 * Defines the decoration text for the node
 	 * 
 	 * @return text for the node or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return mNodeDecorationText;
 	}
 	
 	/**
 	 * Defines the decoration image for the node
 	 *  
 	 * @return file name of image for the node or null if no specific image is available
 	 */
 	@Override
 	public String getImageName()
 	{
 		if (mNodeDecorationImage != "")
 			return mNodeDecorationImage;
 		else
 			return null;
 	}
 }
