 /*
  * Copyright (c) 2005, the JUNG Project and the Regents of the University 
  * of California
  * All rights reserved.
  *
  * This software is open-source under the BSD license; see either
  * "license.txt" or
  * http://jung.sourceforge.net/license.txt for a description.
  *
  */
 package graph.annotations;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Point2D;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import dataStructures.SavedAnnotation;
 import dataStructures.Vertex;
 
 import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.visualization.MultiLayerTransformer;
 import edu.uci.ics.jung.visualization.RenderContext;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.annotations.Annotation;
 import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
 import edu.uci.ics.jung.visualization.picking.PickedState;
 
 /** 
  * VertexAnnotatingGraphMousePlugin<V,E> is an altered form of the
  * jung AnnotatingGraphMousePlugin. This plugin adds Text annotations in a layer
  * of the graph. It utilizes a VertexAnnotationManager
  * so that VertexAnnotation is used in place of the original Annotation class.
  * 
  * @param <V>
  * @param <E>
  */
 public class VertexAnnotatingGraphMousePlugin<V,E> extends AbstractGraphMousePlugin
     implements MouseListener, MouseMotionListener {
 
     /**
      * additional modifiers for the action of adding to an existing
      * selection
      */
     protected int additionalModifiers;
     
     /**
      * a Paintable to store all Annotations
      */
     protected VertexAnnotationManager annotationManager;
     
     /**
      * color for annotations
      */
     protected Color annotationColor = Color.cyan;
     
     /**
      * layer for annotations
      */
     protected VertexAnnotation.Layer layer = VertexAnnotation.Layer.LOWER;
     
     protected boolean fill;
     
     /**
      * holds rendering transforms
      */
     protected MultiLayerTransformer basicTransformer;
     
     /**
      * holds rendering settings
      */
     protected RenderContext<V,E> rc;
     
     /**
      * set to true when the AnnotationPaintable has been
      * added to the view component
      */
     protected boolean added = false;
     
 	/**
 	 * the picked Vertex, if any
 	 */
 	protected Vertex vertex;
 
 	/**
 	 * the picked Edge, if any
 	 */
 	protected E edge;
 	
 	private VisualizationViewer vv;
 		
     /**
 	 * create an instance with default settings
 	 */
 	public VertexAnnotatingGraphMousePlugin(VisualizationViewer vv) {
 	    this(vv, InputEvent.BUTTON1_MASK, 
 	    		InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
 	}
 
 	/**
 	 * create an instance with overides
 	 * @param selectionModifiers for primary selection
 	 * @param additionalModifiers for additional selection
 	 */
 	public VertexAnnotatingGraphMousePlugin(VisualizationViewer vv,
     		int selectionModifiers, int additionalModifiers) {
         super(selectionModifiers);
         this.rc = vv.getRenderContext();
         this.vv = vv;
         this.basicTransformer = rc.getMultiLayerTransformer();
         this.additionalModifiers = additionalModifiers;
         this.annotationManager = new VertexAnnotationManager(rc);
         this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
     }
     
     /**
      * @return Returns the lensColor.
      */
     public Color getAnnotationColor() {
         return annotationColor;
     }
 
     /**
      * @param lensColor The lensColor to set.
      */
     public void setAnnotationColor(Color lensColor) {
         this.annotationColor = lensColor;
     }
 
 	/**
 	 * selects a single vertex, or if one is selected and pop up button is
 	 * pressed, opens VertexAnnotationDialog to annotate that vertex
 	 * 
	 * @param e the event
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void mousePressed(MouseEvent e) {
 		// get the tools to discern the vertex
 		down = e.getPoint();
 		
 		VisualizationViewer<Vertex, E> vv = (VisualizationViewer) e.getSource();
 		
 		if(added == false) {
 			vv.addPreRenderPaintable(annotationManager.getLowerAnnotationPaintable());
 			vv.addPostRenderPaintable(annotationManager.getUpperAnnotationPaintable());
 			added = true;
 		}
 				
 		GraphElementAccessor<Vertex, E> pickSupport = vv.getPickSupport();
 		PickedState<Vertex> pickedVertexState = vv.getPickedVertexState();
 
 		// if these tools are not null, get the vertex selected
 		if (pickSupport != null && pickedVertexState != null && vv != null) {
 			Layout<Vertex, E> layout = vv.getGraphLayout();
 			Point2D ip = e.getPoint();
 			vertex = (Vertex) pickSupport.getVertex(layout, ip.getX(), ip.getY());
 			if (vertex != null) {
 				// if this vertex is not already selected, then just select it
 				if (pickedVertexState.isPicked(vertex) == false) {
 					pickedVertexState.clear();
 					pickedVertexState.pick(vertex, true);
 
 					// if it is a right-click, then pop up the
 					// Annotation Dialog
 				} else if (e.isPopupTrigger()) {
 					VertexAnnotation oldAnnotation = annotationManager
 							.getAnnotation((dataStructures.Vertex) vertex);
 					String oldAnnotationString = null;
 					if (oldAnnotation != null) {
 						oldAnnotationString = (String) oldAnnotation.getAnnotation();
 						annotationManager.remove(oldAnnotation);
 					} else {
 						oldAnnotationString = "";
 					}
 					
 					String annotationString = JOptionPane.showInputDialog(vv,"Annotation:", oldAnnotationString);
 					if(annotationString != null && annotationString.length() > 0) {
 		    			Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
 		    			VertexAnnotation annotation =
 		    				new VertexAnnotation(annotationString, layer, annotationColor, fill, p, vertex);
 		    			annotationManager.add(layer, annotation);
 		    		}
 				}
 			} else {
 				pickedVertexState.clear();
 			}
 
 		}
 	}
 
     /**
	 * Completes the process of adding an annotation.
 	 * 
 	 */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void mouseReleased(MouseEvent e) {
         VisualizationViewer<Vertex,E> vv = (VisualizationViewer)e.getSource();
     	if(e.isPopupTrigger()) {
 			if(vertex != null) {
 				VertexAnnotation oldAnnotation = annotationManager.getAnnotation(vertex);
 				String oldAnnotationString;
 				if(oldAnnotation != null) {
 					oldAnnotationString = (String) oldAnnotation.getAnnotation();
 					annotationManager.remove(oldAnnotation);
 				} else {
 					oldAnnotationString = "";
 				}
 				String annotationString = JOptionPane.showInputDialog(vv,"Annotation:", oldAnnotationString);
 	    		if(annotationString != null && annotationString.length() > 0) {
 	    			Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
 	    			VertexAnnotation annotation =
 	    				new VertexAnnotation(annotationString, layer, annotationColor, fill, p, vertex);
 	    			annotationManager.add(layer, annotation);
 	    		}
 			}
     		
     	}
         down = null;
         vv.repaint();
     }
     
     public void mouseDragged(MouseEvent e) {}
     
     public void mouseClicked(MouseEvent e) {}
 
     public void mouseEntered(MouseEvent e) {
         JComponent c = (JComponent)e.getSource();
         c.setCursor(cursor);
     }
 
     public void mouseExited(MouseEvent e) {
         JComponent c = (JComponent)e.getSource();
         c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 
     public void mouseMoved(MouseEvent e) {
     }
 
 	/**
 	 * @return the layer
 	 */
 	public Annotation.Layer getLayer() {
 		return layer;
 	}
 
 	/**
 	 * @param layer the layer to set
 	 */
 	public void setLayer(Annotation.Layer layer) {
 		this.layer = layer;
 	}
 
 	/**
 	 * @return the fill
 	 */
 	public boolean isFill() {
 		return fill;
 	}
 
 	/**
 	 * @param fill the fill to set
 	 */
 	public void setFill(boolean fill) {
 		this.fill = fill;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public VertexAnnotation getAnnotation(Vertex vertex) {
 		return annotationManager.getAnnotation(vertex);
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void addSavedAnnotation(SavedAnnotation annotation) {
 		if(added == false) {
 			vv.addPreRenderPaintable(annotationManager.getLowerAnnotationPaintable());
 			vv.addPostRenderPaintable(annotationManager.getUpperAnnotationPaintable());
 			added = true;
 		}
 		
 		VertexAnnotation newAnnotation =
 				new VertexAnnotation(annotation.getAnnotation(),
 						layer, annotationColor, fill, 
 						annotation.getLocation(), 
 						annotation.getVertex());
 		annotationManager.add(layer, newAnnotation);
 	}
 
  }
