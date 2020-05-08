 package input;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 
 import javax.swing.JComponent;
 
 public class MouseModule extends InputDevice{
 	JComponent myComponent;
 	public final String myDevice = "MOUSE";
 	
 	public MouseModule(JComponent component, Input input){
 		super("Keyboard",input);
 		myComponent = component;
 		initialize();
 	}
 	
	private PositionObject makePositionObject(MouseEvent e, String source) {
		return new PositionObject(e.getX()/myComponent.getWidth(), e.getY()/myComponent.getHeight(),source,e.getWhen());
	}
	
 	private void initialize(){
 		myComponent.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
             public void mouseMoved (MouseEvent e) {
				notifyInputAction("Mouse_Move", makePositionObject(e, "Mouse_Move"));
             }
             
 			@Override
             public void mouseDragged (MouseEvent e) {
				notifyInputAction("Mouse_Drag", makePositionObject(e, "Mouse_Drag"));
             }
         });
         
 		myComponent.addMouseListener(new MouseListener() {
         	@Override
 			public void mouseClicked(MouseEvent e) {
 				String mouseSide = "";
 				switch(e.getButton()) {
 					case MouseEvent.BUTTON1:
 						mouseSide = "Left";
 						break;
					case MouseEvent.BUTTON3:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Click";
				notifyInputAction(inputSource, makePositionObject(e, inputSource));
         	}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 
 			@Override
 			public void mouseExited(MouseEvent e) {}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				String mouseSide = "";
 				switch(e.getButton()) {
 					case MouseEvent.BUTTON1:
 						mouseSide = "Left";
 						break;
					case MouseEvent.BUTTON3:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Down";
				notifyInputAction(inputSource, makePositionObject(e, inputSource));
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				String mouseSide = "";
 				switch(e.getButton()) {
 					case MouseEvent.BUTTON1:
 						mouseSide = "Left";
 						break;
					case MouseEvent.BUTTON3:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Up";
				notifyInputAction(inputSource, makePositionObject(e, inputSource));
 			}
         });
 	}
 }
