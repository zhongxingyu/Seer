 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.examples.extension.rcp.gef.logic.identifier;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gef.examples.logicdesigner.edit.GateEditPart;
 import org.eclipse.gef.examples.logicdesigner.figures.GateFigure;
 import org.eclipse.gef.examples.logicdesigner.model.Gate;
import org.eclipse.jubula.rc.rcp.e3.gef.identifier.ClassCountEditPartIdentifier;
import org.eclipse.jubula.rc.rcp.e3.gef.identifier.IEditPartIdentifier;
 
 
 /**
  * Identifier for Gate Edit Parts.
  *
  * @author BREDEX GmbH
  * @created Jun 18, 2009
  */
 public class GateEditPartIdentifier implements IEditPartIdentifier {
 
 	/** the Edit Part for which identifying information will be provided */
     private GateEditPart m_editPart;
     
     /** delegate for providing the identifier string */
     private ClassCountEditPartIdentifier m_delegate;
 
     /**
      * Constructor
      * 
      * @param editPart The Edit Part for which identifying information will be provided.
      */
     public GateEditPartIdentifier(GateEditPart editPart) {
         m_editPart = editPart;
         m_delegate = new ClassCountEditPartIdentifier(editPart);
     }
     
     /**
      * {@inheritDoc}
      */
     public Map<String, ConnectionAnchor> getConnectionAnchors() {
         Map<String, ConnectionAnchor> anchorMap = 
             new HashMap<String, ConnectionAnchor>();
         
         IFigure figure = m_editPart.getFigure();
         if (figure instanceof GateFigure) {
             GateFigure gateFigure = (GateFigure)figure;
 
             String [] terminals = 
                 new String [] {Gate.TERMINAL_A, Gate.TERMINAL_B};
             
             for (int i = 0; i < terminals.length; i++) {
                 anchorMap.put(terminals[i], 
                         gateFigure.getConnectionAnchor(terminals[i]));
             }
             
         }
         
         return anchorMap;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getIdentifier() {
         return m_delegate.getIdentifier();
     }
 
 }
