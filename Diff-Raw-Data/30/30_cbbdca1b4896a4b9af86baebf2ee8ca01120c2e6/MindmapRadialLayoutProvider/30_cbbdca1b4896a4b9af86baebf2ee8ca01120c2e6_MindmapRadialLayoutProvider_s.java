 /******************************************************************************
 * Copyright (c) 2006 Borland Software Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    Borland Software Corporation - initial API and implementation 
  ****************************************************************************/
 
 package org.eclipse.gmf.examples.mindmap.diagram.layout;
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.gmf.runtime.common.core.service.IOperation;
 import org.eclipse.gmf.runtime.diagram.ui.providers.internal.RadialProvider;
 import org.eclipse.gmf.runtime.diagram.ui.services.layout.ILayoutNode;
 import org.eclipse.gmf.runtime.diagram.ui.services.layout.ILayoutNodeOperation;
 import org.eclipse.gmf.runtime.diagram.ui.services.layout.LayoutType;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 
 
 /**
  * @author rgronback
  * Custom provider that lays out the Mindmap Diagram in a radial manner.
  */
 
 public class MindmapRadialLayoutProvider
     extends RadialProvider {
 
     public boolean provides(IOperation operation) {
         // enable this provider only on mindmap diagrams
         if (operation instanceof ILayoutNodeOperation) {
             Iterator nodes = ((ILayoutNodeOperation) operation)
                 .getLayoutNodes().listIterator();
             if (nodes.hasNext()) {
                 View node = ((ILayoutNode) nodes.next()).getNode();
                 Diagram container = node.getDiagram();
                 if (container == null
                     || !(container.getType().equals("Mindmap"))) //$NON-NLS-1$
                     return false;
             }
         } else {
             return false;
         }
         IAdaptable layoutHint = ((ILayoutNodeOperation) operation)
             .getLayoutHint();
         String layoutType = (String) layoutHint.getAdapter(String.class);
         return LayoutType.DEFAULT.equals(layoutType);
     }
 
 }
