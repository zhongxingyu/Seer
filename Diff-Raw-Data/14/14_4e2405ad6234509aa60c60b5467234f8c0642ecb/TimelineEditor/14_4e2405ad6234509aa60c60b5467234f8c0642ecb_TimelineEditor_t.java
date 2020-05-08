 /*
  * This file is a part of CAST project.
  * (c) Copyright 2007, AGH University of Science & Technology
  * https://caribou.iisg.agh.edu.pl/trac/cast
  *
  * Licensed under the Eclipse Public License, Version 1.0 (the "License").
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 /*
  * File: TimelineEditor.java
  * Created: Feb 19, 2013
  * Author: mentel
  * $Id$
  */
 
 package pl.edu.agh.iisg.timeline.editor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Composite;
 
 import pl.edu.agh.iisg.timeline.editpart.TimelineRootEditPart;
 import pl.edu.agh.iisg.timeline.model.TimelineDiagram;
 import pl.edu.agh.iisg.timeline.view.TimelineEditPartsFactory;
 
 /**
  *
  * @author AGH CAST Team
  */
 public class TimelineEditor extends GraphicalEditor {
 
     public static final String ID = "pl.edu.agh.iisg.timeline.editor"; //$NON-NLS-1$
 
     private TimelineRootEditPart rootEditPart;
 
     /**
      * Constructor.
      *
      */
     public TimelineEditor() {
         setEditDomain(new DefaultEditDomain(this));
     }
 
     @Override
    protected void createGraphicalViewer(Composite parent) {
        GraphicalViewer viewer = new GraphicalViewerImpl();
        viewer.createControl(parent);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();
    }

    @Override
     protected void configureGraphicalViewer() {
         super.configureGraphicalViewer();
         configureGraphicalViewerEditParts();
         configureGraphicalViewerLookAndControls();
     }
 
     @Override
     protected void initializeGraphicalViewer() {
         TimelineDiagram diagram = ((TimelineEditorInput)getEditorInput()).getTimelineDiagram();
         getGraphicalViewer().setContents(diagram);
     }
 
     @Override
     public void doSave(IProgressMonitor monitor) {
     }
 
     private void configureGraphicalViewerEditParts() {
         GraphicalViewer viewer = getGraphicalViewer();
         viewer.setEditPartFactory(new TimelineEditPartsFactory());
         rootEditPart = new TimelineRootEditPart();
         viewer.setRootEditPart(rootEditPart);
     }
 
     private void configureGraphicalViewerLookAndControls() {
         getGraphicalViewer().getControl().setBackground(ColorConstants.white);
         addKeyControl();
         addMouseScroll();
     }
 
     private void addMouseScroll() {
         getGraphicalViewer().getControl().addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseScrolled(MouseEvent e) {
                 rootEditPart.scrollVertical(e.count);
             }
         });
     }
 
     private void addKeyControl() {
         getGraphicalViewer().getControl().addKeyListener(new KeyAdapter() {
             private static final int ARROW_STEP = 1;
 
             private static final int PAGE_STEP = 25;
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.ARROW_DOWN) {
                     rootEditPart.scrollVertical(-ARROW_STEP);
                 } else if (e.keyCode == SWT.ARROW_UP) {
                     rootEditPart.scrollVertical(ARROW_STEP);
                 } else if (e.keyCode == SWT.ARROW_RIGHT) {
                     rootEditPart.scrollHorizontal(-ARROW_STEP);
                 } else if (e.keyCode == SWT.ARROW_LEFT) {
                     rootEditPart.scrollHorizontal(ARROW_STEP);
                 } else if (e.keyCode == SWT.PAGE_DOWN) {
                     rootEditPart.scrollVertical(-PAGE_STEP);
                 } else if (e.keyCode == SWT.PAGE_UP) {
                     rootEditPart.scrollVertical(PAGE_STEP);
                 } else if (e.keyCode == SWT.HOME) {
                     rootEditPart.scrollVerticalToStart();
                 } else if (e.keyCode == SWT.END) {
                     rootEditPart.scrollVerticalToEnd();
                 }
             }
         });
     }
 
 }
