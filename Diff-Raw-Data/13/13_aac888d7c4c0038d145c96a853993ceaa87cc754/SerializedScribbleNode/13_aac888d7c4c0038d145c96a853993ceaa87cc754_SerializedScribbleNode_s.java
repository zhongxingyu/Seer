 package boardnodes;
 
 import java.awt.Rectangle;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 public class SerializedScribbleNode extends SerializedBoardElt {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8416218992109699688L;
	public LinkedList<List<ColoredPoint>> drawnArea;
	public LinkedList<List<ColoredPoint>> undrawnArea;
 	public Stack<ScribbleNodeEdit> undos;
 	public Stack<ScribbleNodeEdit> redos;
 	public Rectangle bounds;
 	
 	public SerializedScribbleNode() {
 		type = BoardEltType.SCRIBBLE;
 	}
 }
