 package com.bluesky.visualprogramming.ui.diagram;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.apache.batik.dom.events.DOMMouseEvent;
 import org.apache.batik.dom.svg.SVGOMGElement;
 import org.apache.batik.dom.svg.SVGOMPoint;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.svg.SVGLocatable;
 import org.w3c.dom.svg.SVGMatrix;
 import org.w3c.dom.svg.SVGStylable;
 import org.w3c.dom.svg.SVGTransform;
 
 import com.bluesky.visualprogramming.core.Field;
 import com.bluesky.visualprogramming.ui.SVGMainWindow;
 import com.bluesky.visualprogramming.ui.dialog.ObjectPropertyDialog;
 import com.bluesky.visualprogramming.ui.svg.SVGUtils;
 import com.bluesky.visualprogramming.ui.svg.SvgElementType;
 import com.bluesky.visualprogramming.ui.svg.SvgScene;
 import com.bluesky.visualprogramming.ui.svg.TransformIndex;
 
 public class SVGDiagramPanel extends JPanel {
 	static Logger logger = Logger.getLogger(SVGDiagramPanel.class);
 	/**
 	 * the dragged target
 	 */
 	public EventTarget currentDraggingElement;
 
 	/**
 	 * selected element for popup menu
 	 */
 	private Element currentElement;
 
 	// private SVGOMPoint cursorOffset;
 	// offset of mouse and shape left-top point.
 	public float dragOffsetX;
 	public float dragOffsetY;
 
 	private JSVGCanvas canvas;
 	private SvgScene scene;
 
 	private SVGMainWindow mainWindow;
 	private JPopupMenu popupMenu;
 
 	public SVGDiagramPanel(SVGMainWindow mainWindow, JPopupMenu popupMenu) {
 		this.mainWindow = mainWindow;
 		this.popupMenu = popupMenu;
 
 		canvas = new JSVGCanvas();
 		canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
 
 		setLayout(new BorderLayout());
 
 		add("Center", canvas);
 	}
 
 	/**
 	 * set scene(document) so that onload event will happen. resue a document
 	 * won't trigger onload event.
 	 * 
 	 * @param scene
 	 */
 	public void setScene(SvgScene scene) {
 		this.scene = scene;
 		canvas.setDocument(scene.getDocument());
 		setVisible(true);
 
 	}
 
 	public void addMouseListener(org.w3c.dom.events.EventTarget target) {
 		target.addEventListener("mousedown", new EventListener() {
 
 			@Override
 			public void handleEvent(Event evt) {
 				currentDraggingElement = evt.getCurrentTarget();
 				DOMMouseEvent elEvt = (DOMMouseEvent) evt;
 				int nowToX = elEvt.getClientX();
 				int nowToY = elEvt.getClientY();
 
 				// if (logger.isDebugEnabled())
 				// logger.debug(String.format("client xy: %d,%d", nowToX,
 				// nowToY));
 
 				Element ele = (Element) currentDraggingElement;
 
 				SVGOMPoint screenPt = new SVGOMPoint(nowToX, nowToY);
 
 				long objectId = SVGUtils.getObjectId(ele);
 
 				Element objBorder = scene.getElement(objectId,
 						SvgElementType.Border);
 
 				// elem -> screen
 				SVGMatrix mat = ((SVGLocatable) objBorder).getScreenCTM();
 
 				mat = mat.inverse(); // screen -> elem
 				SVGOMPoint svgPt = (SVGOMPoint) screenPt.matrixTransform(mat);
 
				SVGOMPoint startPosition = SVGUtils.getStartPoint(objBorder);
 
				dragOffsetX = svgPt.getX() - startPosition.getX();
				dragOffsetY = svgPt.getY() - startPosition.getY();
 
 			}
 		}, false);
 
 		target.addEventListener("mouseup", new EventListener() {
 
 			@Override
 			public void handleEvent(Event evt) {
				
				Element ele = (Element) currentDraggingElement;
				long objId = SVGUtils.getObjectId(ele);
				Element border = scene.getElement(objId, SvgElementType.Border);
				// System.out.println(objId);
				CSSStyleDeclaration style = ((SVGStylable) border).getStyle();
				style.removeProperty("stroke-dasharray");
				 
 				currentDraggingElement = null;
 
 			}
 		}, false);
 
 		target.addEventListener("mousemove", new EventListener() {
 
 			@Override
 			public void handleEvent(Event evt) {
 				if (currentDraggingElement == null)
 					return;
 
 				Element ele = (Element) currentDraggingElement;
 				long objId = SVGUtils.getObjectId(ele);
 				Element border = scene.getElement(objId, SvgElementType.Border);
 				// System.out.println(objId);
 				CSSStyleDeclaration style = ((SVGStylable) border).getStyle();
				style.setProperty("stroke-dasharray", "27.94599915,27.94599915", "");
				 
 				SVGOMGElement object = (SVGOMGElement) scene.getElement(objId,
 						SvgElementType.Object);
 				SVGTransform transform = scene.getTransform(objId,
 						TransformIndex.Offset);
 
 				DOMMouseEvent elEvt = (DOMMouseEvent) evt;
 				int nowToX = elEvt.getClientX();
 				int nowToY = elEvt.getClientY();
 
 				// if (logger.isDebugEnabled())
 				// logger.debug(String.format("client xy: %d,%d", nowToX,
 				// nowToY));
 
 				// convert it to a point for use with the Matrix
 				SVGOMPoint pt = new SVGOMPoint(nowToX, nowToY);
 				// Get the items screen coordinates, and apply the
 				// transformation
 				// elem -> screen
 				SVGMatrix mat = ((SVGLocatable) border).getScreenCTM();
 
 				mat = mat.inverse(); // screen -> elem
 				SVGOMPoint droppt = (SVGOMPoint) pt.matrixTransform(mat);
 				if (transform.getType() == SVGTransform.SVG_TRANSFORM_TRANSLATE) {
 					// if (logger.isDebugEnabled())
 					// logger.debug(String.format("drop xy: %f,%f",
 					// droppt.getX(), droppt.getY()));
 
 					float oldX = transform.getMatrix().getE();
 					float oldY = transform.getMatrix().getF();
 
 					// if (logger.isDebugEnabled())
 					// logger.debug(String.format("old xy: %f,%f", oldX, oldY));
 
 					float tranlsateX = droppt.getX() + oldX - dragOffsetX;
 					float tranlsateY = droppt.getY() + oldY - dragOffsetY;
 
 					// if (logger.isDebugEnabled())
 					// logger.debug(String.format("translate xy: %f,%f",
 					// tranlsateX, tranlsateY));
 
 					transform.setTranslate(tranlsateX, tranlsateY);
 
 					// update field.area
 					Field field = (Field) ele.getUserData("field");
 
 					field.getArea().x = (int) tranlsateX;
 					field.getArea().y = (int) tranlsateY;
 
 				}
 
 			}
 		}, false);
 
 		target.addEventListener("click", new EventListener() {
 
 			@Override
 			public void handleEvent(Event evt) {
 				DOMMouseEvent mouseEvent = (DOMMouseEvent) evt;
 				currentElement = (Element) evt.getCurrentTarget();
 				if (mouseEvent.getButton() == 2) {
 					popupMenu.show(canvas, mouseEvent.getClientX(),
 							mouseEvent.getClientY());
 				}
 
 			}
 		}, false);
 	}
 
 	public void addPopupMenuListener(org.w3c.dom.events.EventTarget target) {
 
 		target.addEventListener("click", new EventListener() {
 
 			@Override
 			public void handleEvent(Event evt) {
 				DOMMouseEvent mouseEvent = (DOMMouseEvent) evt;
 
 				if (mouseEvent.getButton() == 2) {
 					popupMenu.show(canvas, mouseEvent.getClientX(),
 							mouseEvent.getClientY());
 				}
 			}
 		}, false);
 	}
 
 	public JSVGCanvas getCanvas() {
 		return canvas;
 	}
 
 	public void setCanvas(JSVGCanvas canvas) {
 		this.canvas = canvas;
 	}
 
 	public SvgScene getScene() {
 		return scene;
 	}
 
 	public Element getCurrentElement() {
 		return currentElement;
 	}
 
 	public Field getCurrentField() {
 		return (Field) currentElement.getUserData("field");
 	}
 
 	public void reload() {
 		mainWindow.reloadDiagram();
 	}
 }
