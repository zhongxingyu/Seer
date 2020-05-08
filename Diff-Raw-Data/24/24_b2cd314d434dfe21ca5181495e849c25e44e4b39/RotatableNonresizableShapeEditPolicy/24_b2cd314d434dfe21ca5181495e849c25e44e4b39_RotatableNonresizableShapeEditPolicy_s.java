 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.edit.policies;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.draw2d.Border;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.Handle;
 import org.eclipse.gef.handles.AbstractHandle;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.RotatableShapeEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.internal.handles.RotateHandle;
 import org.eclipse.gmf.runtime.diagram.ui.internal.tools.RotateTracker;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.RGB;
 import org.fritzing.fritzing.diagram.part.FritzingDiagramEditorPlugin;
 
 /**
  * @generated NOT
  */
 public class RotatableNonresizableShapeEditPolicy extends RotatableShapeEditPolicy {
 	public static HashMap<Integer, Cursor> rotateCursors = new HashMap<Integer, Cursor>();
 	static RGB handleRGB = new RGB(0x51, 0x8a, 0x5b);
 	static Color fillColor = new Color(null, handleRGB);
 	static String cursorImageFileSuffix = ".gif";
 	
 	/**
 	 * @generated NOT
 	 */
 	public void setResizeDirections(int newDirections) {
 		// superclass sets these to all 4 directions in createSelectionHandles
 		// so get rid of them here
 		super.setResizeDirections(0);
 	}
 		
 	/**
 	 * @generated NOT
 	 */
 	protected List createSelectionHandles() {
 		List l = super.createSelectionHandles();
 		for (Object obj : l) {
 			if(obj instanceof AbstractHandle) {
 				AbstractHandle handle = (AbstractHandle) obj;
 				Border border = handle.getBorder();
 				if(border instanceof LineBorder) {
 					LineBorder lborder = (LineBorder) border;
 					lborder.setWidth(2);
 					lborder.setColor(fillColor);
 				}
 			}
 		}
 		
 		return l;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	protected Handle createRotationHandle(GraphicalEditPart owner, int direction) {
 		String prefix = "";
 		int hotX;
 		int hotY;
 		switch (direction) {
 		case PositionConstants.SOUTH_EAST:
 			hotY = 0;
 			hotX = 0;
			prefix = "SE";
 			break;
 		case PositionConstants.NORTH_EAST:
			prefix = "NE";
 			hotY = 1;
 			hotX = 0;
 			break;
 		case PositionConstants.SOUTH_WEST:
 			hotY = 0;
 			hotX = 1;
			prefix = "SW";
 			break;
 		case PositionConstants.NORTH_WEST:
 			hotY = 1;
 			hotX = 1;
			prefix = "NW";
 			break;
 		default:
 			hotY = 0;
 			hotX = 0;
			direction = PositionConstants.SOUTH_EAST;
			prefix = "SE";
 		}
 		
 		Integer key = new Integer(direction);
 		Cursor rotateCursor = rotateCursors.get(key);
 		if (rotateCursor == null) {
 			try {
				InputStream stream = FileLocator.openStream(FritzingDiagramEditorPlugin.getInstance().getBundle(), new Path("icons/cursors/" + prefix + cursorImageFileSuffix), false);
 				if (stream != null) {		
 					ImageData imageData = new ImageData(stream);
 					if (hotY == 1) hotY = imageData.height - 1;
 					if (hotX == 1) hotX = imageData.width - 1;
 					rotateCursor = new Cursor(null, imageData, hotX, hotY);
 					rotateCursors.put(key, rotateCursor);
 				}
 			}
 			catch (Exception ex ) {
 				FritzingDiagramEditorPlugin
 				.getInstance()
 				.logError( "Unable to create cursor: " + ex.getMessage(), ex);			
 			}
 			
 		}
 		
 		
 		RotateHandle handle = new RotateHandle(owner, direction) {
 			protected Color getFillColor() {
 				return fillColor;
 			}			
 		};
 				
 		if (rotateCursor != null) {
 			handle.setCursor(rotateCursor);
 		}
 					
 		handle.setDragTracker(new CustomCursorRotateTracker(owner, direction, rotateCursor));
 		
 				
 		return handle;
 				
 	}
 	
 	
 
 	/*
 	private boolean isRotationRequired(ChangeBoundsRequest request) {
 		return request instanceof RotateShapeRequest ?  ((RotateShapeRequest) request).shouldRotate() : false; 
 	}
 	
 	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
 		super.showChangeBoundsFeedback(request);
 		System.out.println("show change " + request.getClass().getName());	
 	}
 	
 	
 	protected Rectangle getInitialFeedbackBounds() {
 		System.out.println("get initial bounds ");	
 
 		return super.getInitialFeedbackBounds();	
 	}
 
 	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
 		ChangeBoundsRequest cbr = (ChangeBoundsRequest) request;
 		if (isRotationRequired(cbr)) {
 			Control control = Display.getCurrent().getCursorControl();
 			savedCursor = control.getCursor();
 			control.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT));
 			System.out.println("setting cursor? " + control.getToolTipText() + " " + request.getClass().getName());	
 		}
 	}
 	
 	public void eraseTargetFeedback(Request request) {
 		if (request instanceof ChangeBoundsRequest) {
 			ChangeBoundsRequest cbr = (ChangeBoundsRequest) request;
 			if (isRotationRequired(cbr)) {
 				Control control = Display.getCurrent().getCursorControl();
 				control.setCursor(savedCursor);
 			}
 		}
 	}
 	
 	*/
 }
 
 /**
  * @generated NOT
  */
 class CustomCursorRotateTracker extends RotateTracker
 {
 	int direction;
 	Cursor cursor;
 		
 	/**
 	 * @generated NOT
 	 */
 	public CustomCursorRotateTracker(GraphicalEditPart owner, int direction, Cursor cursor) {
 		super(owner, direction);
 		this.direction = direction;
 		this.cursor = cursor;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	protected Cursor getDefaultCursor() {
 		return cursor;
 	}
 	
 
 	protected boolean  handleButtonDown(int button) {
 		// 
 		boolean result = super.handleButtonDown(button);
 		if (button == 1) {
 			// tell it we're already dragging
 			
 			// setFlag makes movedPastThreshold return true
 			//setFlag(FLAG_PAST_THRESHOLD, true);
 			setFlag(1, true);
 			
 			handleDrag();
 			handleDragStarted();
 			handleDragInProgress();
 		}
 		return result;
 	}
 }
 
 
