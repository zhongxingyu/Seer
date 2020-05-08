 /*************************************************************************
  * 
  *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
  *  
  *  The Contents of this file are made available subject to
  *  the terms of European Union Public License (EUPL) version 1.1
  *  as published by the European Community.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the EUPL.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  EUPL for more details.
  *
  *  You should have received a copy of the EUPL along with this
  *  program.  If not, see:
  *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
  *
  ************************************************************************/
 
 package it.plio.ext.oxsit.ooo.ui;
 
 import it.plio.ext.oxsit.Utilities;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import com.sun.star.awt.FontFamily;
 import com.sun.star.awt.FontPitch;
 import com.sun.star.awt.FontSlant;
 import com.sun.star.awt.FontWeight;
 import com.sun.star.awt.FontWidth;
 import com.sun.star.awt.XControl;
 import com.sun.star.awt.XControlModel;
 import com.sun.star.awt.XFixedText;
 import com.sun.star.awt.XTextComponent;
 import com.sun.star.awt.XWindow;
 import com.sun.star.beans.PropertyVetoException;
 import com.sun.star.beans.UnknownPropertyException;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.WrappedTargetException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XEventListener;
 import com.sun.star.uno.UnoRuntime;
 
 /**
  * contains the data description of the tree node, for example
  * parts of the certificate to display, along with status and similia
  * 
  * @author beppe
  *
  */
 public class TreeNodeDescriptor implements XComponent  {
 	public enum TreeNodeType {
 		SIGNATURE, /* general state of the signature:
 		state: valid/not valid
 		short description of signee: name and Italian fiscal code
 		a confirmation of identity check */		
 		CERTIFICATE, /*	 */
 		VERSION, /* */
 		SERIAL_NUMBER, /* */
 		ISSUER, /* */
 		VALID_FROM, /* */
 		VALID_TO, /* */
 		SUBJECT,
 		SUBJECT_ALGORITHM,
 		PUBLIC_KEY,
 		SIGNATURE_ALGORITHM,
 		THUMBPRINT_SHA1,
 		THUMBPRINT_MD5,
 		EXTENSIONS_NON_CRITICAL,
 			QC_STATEMENTS,
 			AUTHORITY_INFORMATION_ACCESS,
 			X509V3_CERTIFICATE_POLICIES,
 			X509V3_SUBJECT_DIRECTORY_ATTRIBUTES,
 			X509V3_ISSUER_ALTERNATIVE_NAME,
 			X509V3_AUTHORITY_KEY_IDENTIFIER,
 			X509V3_SUBJECT_KEY_IDENTIFIER,
 			X509V3_CRL_DISTRIBUTION_POINTS,
 		EXTENSIONS_CRITICAL,
 			X509V3_KEY_USAGE,
 		CERTIFICATION_PATH,
 	};
 	
 	/**
 	 * defines the state of the node, for display purposes
 	 * at every state is associated a graphic element for display
 	 * or none, if this is the case  
 	 * @author beppec56
 	 *
 	 */
 	public enum NodeState {
 		NODE_NONE, //no graphic indication needed
 		NODE_OK,
 		NODE_WARNING,
 		NODE_ERROR,
 		NODE_BROKEN
 	};
 
 	private SignatureStateInDocument m_aSignatureState = null;
 	private TreeNodeType m_nType;
 	private LinkedList<XControl>	m_aControlsElements;
 
 	public TreeNodeDescriptor(TreeNodeType nType, SignatureStateInDocument aSignatureState) {
 		// TODO Auto-generated constructor stub
 		m_nType = nType;
 		m_aSignatureState = aSignatureState;
 		m_aControlsElements = new LinkedList<XControl>();
 	}
 
 	public TreeNodeType getType() {
 		return m_nType;
 	}
 
 	public void addEventListener(XEventListener arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void removeEventListener(XEventListener arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public SignatureStateInDocument  getCertificateData() {
 		return m_aSignatureState;
 	}
 
 	/**
 	 * enable (or disable ) the data of this node according to bWhat
 	 * scan the list of display elements and disable the XWindows element, if present
 	 * the first character of every string in SIGNATURE and CERTIFICATE element types
 	 * is interpreted as a command for the character rendering
 	 */
 	public void EnableDisplay(boolean bWhat) {
 
 		//if bWhat == false, then disable
 		//else prepare data in element according to type, then enable
 		// the window
 		if(bWhat == false) {
 			for(ListIterator<XControl> aIterator = m_aControlsElements.listIterator();  aIterator.hasNext();) {
 				XControl xTFControl = aIterator.next();
 				XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
 				if(xaWNode != null )					
 					xaWNode.setVisible(bWhat);
 			}
 		}
 		else {
 			String[] aStrings;
 			if(m_nType ==  TreeNodeType.SIGNATURE) 
 				aStrings = m_aSignatureState.getCertStrings(m_nType);
 			else
 				aStrings = m_aSignatureState.m_aCert.getCertStrings(m_nType);
 			boolean bSetFixedPitch = false;
 			//check the type, according to it print the data
 			switch(m_nType){
 			case SIGNATURE:
 			case CERTIFICATE:
 			{
 				//ouput them
 				int StringIndex = 0;
 				for(ListIterator<XControl> aIterator = m_aControlsElements.listIterator();
 											aIterator.hasNext(); ) {
 					XControl xTFControl = aIterator.next();
 					XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
 					if(xaWNode != null ) {
 						//check the interface, should be the one for fixed text
 						XFixedText aFText =  (XFixedText)UnoRuntime.queryInterface( XFixedText.class, xTFControl );
 						if(aFText != null) {
 							if(StringIndex < aStrings.length) {
 								String sStr = aStrings[StringIndex];
 								if(sStr != null && sStr.length() > 0 ) {
 									//grab the font width property
 //									Utilities.showInterfaces(xTFControl);
 									XPropertySet xTFModelPSet = (XPropertySet) UnoRuntime.queryInterface(
 											XPropertySet.class, xTFControl.getModel());
 // DEBUG: print properties:
 	/*								Utilities.showProperties(this, xTFModelPSet);*/
 									//detect the first char
 									String sFirst = aStrings[StringIndex].substring(0,1);
 									// set the width accordingly 
 									try {
 										if(sFirst.equalsIgnoreCase("b"))
 											xTFModelPSet.setPropertyValue("FontWeight", new Float(FontWeight.BOLD));
 										else
 											xTFModelPSet.setPropertyValue("FontWeight", new Float(FontWeight.NORMAL));
 									} catch (UnknownPropertyException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									} catch (PropertyVetoException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									} catch (IllegalArgumentException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									} catch (WrappedTargetException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 									
 									// set the string
 									aFText.setText(sStr.substring(1));
 								}
 								else
 									aFText.setText("");
 								StringIndex++;
 								xaWNode.setVisible(bWhat);
 							}
 						}
 						else {
 							XTextComponent aText =  (XTextComponent)UnoRuntime.queryInterface( XTextComponent.class, xTFControl );
 							if( aText != null) // then it's the background  control in background color
 								xaWNode.setVisible(true); //match if edit control
 							else
 								xaWNode.setVisible(false);
 						}
 					}
 				}
 			}
 			break;
 			case SERIAL_NUMBER:
 			case PUBLIC_KEY:
 			case THUMBPRINT_SHA1:
 			case THUMBPRINT_MD5:
 				bSetFixedPitch = true;
 			case VERSION:
 			case ISSUER:
 			case VALID_FROM:
 			case VALID_TO:
 			case SUBJECT:
 			case SUBJECT_ALGORITHM:
 			case SIGNATURE_ALGORITHM:
 			case X509V3_KEY_USAGE:
 			case QC_STATEMENTS:
 			case AUTHORITY_INFORMATION_ACCESS:
 			case X509V3_CERTIFICATE_POLICIES:
 			case X509V3_SUBJECT_DIRECTORY_ATTRIBUTES:
 			case X509V3_ISSUER_ALTERNATIVE_NAME:
 			case X509V3_AUTHORITY_KEY_IDENTIFIER:
 			case X509V3_SUBJECT_KEY_IDENTIFIER:
 			case X509V3_CRL_DISTRIBUTION_POINTS:
 				
 			{
 				//prints only a single line in the multiline window, so we expect only one element
 				ListIterator<XControl> aIterator = m_aControlsElements.listIterator();
 				XControl xTFControl = aIterator.next();
 				XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
 				if(xaWNode != null ) {
 					// first set the font style
 					//grab the model
 					XControl aControl =  (XControl)UnoRuntime.queryInterface( XControl.class, xTFControl );
 					XControlModel aControlModel = aControl.getModel();
 //					Utilities.showInterfaces(xTFControl);
 //					Utilities.showServiceProperties(aControlModel);
 //					com.sun.star.awt.FontDescriptor font;
 					try {
 						XPropertySet xTFModelPSet = (XPropertySet) UnoRuntime.queryInterface(
 								XPropertySet.class, aControlModel);
 						
 /*						font = (com.sun.star.awt.FontDescriptor) xTFModelPSet
 								.getPropertyValue("FontDescriptor");
 */						// strangely enough, it seems that FontPitch alone doesn't work
 						if (bSetFixedPitch){
 //							font.Name = new String("Courier");
 //							xTFModelPSet.setPropertyValue("FontHeight", new Float(12.0));
 							xTFModelPSet.setPropertyValue("FontName", "Courier");
 //							font.Pitch = FontPitch.FIXED; //doesn't work
 						}
 						else {
 //							xTFModelPSet.setPropertyValue("FontHeight", new Float(12.0));
 //							xTFModelPSet.setPropertyValue("FontWidth", new Short((short)FontWidth.EXPANDED));
 							xTFModelPSet.setPropertyValue("FontName", "Verdana");
 //							xTFModelPSet.setPropertyValue("FontName", "Helvetica");
 //							xTFModelPSet.setPropertyValue("FontName", "Times");
 						}
 							
 //							font.Name = new String("Helvetica");
 //							font.Pitch = FontPitch.VARIABLE; //doesn't work
 						
 //						font.Family = FontFamily.SYSTEM;
 
 //						font.Weight = (float)120.0;//100.0 = standard, 200.0 = double weight
 //						font.Slant = FontSlant.ITALIC;//works
 //						System.out.println("font: " + font.Pitch);
 
 //						xTFModelPSet.setPropertyValue("FontDescriptor", font);
 					} catch (UnknownPropertyException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (PropertyVetoException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (WrappedTargetException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					//check the interface, should be the one for fixed text
 					XTextComponent aText =  (XTextComponent)UnoRuntime.queryInterface( XTextComponent.class, xTFControl );
 					if( aText != null ) {
 						 //match if edit control
 						aText.setText("  ");												
 						xaWNode.setVisible(true);
 						if( aStrings.length > 0) {
 							aText.setText(aStrings[0]);
 						}
 					}
 					else {
 						xaWNode.setVisible(false);
 					}
 				}
 			}
 			break;
 
 			default:
 				;//do nothing
 			}
 		}
 	}
 
 	public LinkedList<XControl> getList() {
 		return m_aControlsElements;
 	}
 }
