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
 	
 	private void initialize(){
 		myComponent.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
             public void mouseMoved (MouseEvent e) {
				notifyInputAction("Mouse_Move", new PositionObject(e.getX(), e.getY(),"Mouse_Move",e.getWhen()));
             }
             
 			@Override
             public void mouseDragged (MouseEvent e) {
				notifyInputAction("Mouse_Drag",new PositionObject(e.getX(), e.getY(),"Mouse_Drag",e.getWhen()));
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
					case MouseEvent.BUTTON2:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Click";
				notifyInputAction(inputSource, new PositionObject(e.getX(), e.getY(),inputSource,e.getWhen()));
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
					case MouseEvent.BUTTON2:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Down";
				notifyInputAction(inputSource,new PositionObject(e.getX(), e.getY(),inputSource,e.getWhen()));
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				String mouseSide = "";
 				switch(e.getButton()) {
 					case MouseEvent.BUTTON1:
 						mouseSide = "Left";
 						break;
					case MouseEvent.BUTTON2:
 						mouseSide = "Right";
 						break;
 				}
 				String inputSource = "Mouse_" + mouseSide + "_Up";
				notifyInputAction(inputSource,new PositionObject(e.getX(), e.getY(),inputSource,e.getWhen()));
 			}
         });
 	}
 }
