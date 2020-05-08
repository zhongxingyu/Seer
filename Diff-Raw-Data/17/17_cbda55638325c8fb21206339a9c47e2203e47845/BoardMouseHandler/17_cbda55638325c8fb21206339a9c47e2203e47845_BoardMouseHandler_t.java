 package demo.client.local.game.tools;
 
 import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 
 import demo.client.local.game.controllers.BoardController;
 
 public class BoardMouseHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, DoubleClickHandler {
 
   private BoardController controller;
   private int lastCol;
   private int lastRow;
   private boolean mouseDown = false;
   private Element canvas;
 
   public BoardMouseHandler(BoardController controller, Element canvas) {
     this.controller = controller;
     this.canvas = canvas;
   }
 
   @Override
   public void onMouseDown(MouseDownEvent event) {
    if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
      lastCol = coordToIndex(event.getRelativeX(canvas));
      lastRow = coordToIndex(event.getRelativeY(canvas));
      mouseDown = true;
    }
   }
 
   @Override
   public void onMouseMove(MouseMoveEvent event) {
     if (mouseDown) {
       int newCol = coordToIndex(event.getRelativeX(canvas));
       if (newCol - lastCol != 0) {
         controller.setColMoveOnce(newCol - lastCol);
         lastCol = newCol;
       }
       int newRow = coordToIndex(event.getRelativeY(canvas));
       if (newRow - lastRow > 0) {
         controller.setRowMoveOnce(1);
         lastRow = newRow;
       }
     }
   }
 
   @Override
   public void onMouseUp(MouseUpEvent event) {
    if (event.getNativeButton() == NativeEvent.BUTTON_LEFT)
      mouseDown = false;
   }
 
   @Override
   public void onDoubleClick(DoubleClickEvent event) {
     controller.rotateOnce();
   }

   private static int coordToIndex(int x) {
     return x / Size.MAIN_BLOCK_SIZE;
   }
 
 }
