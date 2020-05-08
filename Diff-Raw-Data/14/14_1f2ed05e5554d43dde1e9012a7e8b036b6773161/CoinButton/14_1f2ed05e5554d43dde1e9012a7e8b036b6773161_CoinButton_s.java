 
 
 
 
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 import machineTest.*;
 
 @SuppressWarnings("serial")
 public class CoinButton extends JButton implements MouseListener{
 	private int value;
 	private TransitionStateManager tsManager;
 	private VendingMachine vm;
 	
 	public CoinButton(int value, ImageIcon icon, int x, int y, TransitionStateManager tsManager, VendingMachine vm){
 		this.tsManager = tsManager;
 		this.value = value;
 		this.vm = vm;
 		this.setBounds(x, y, 40, 40);
 		this.setBorderPainted(false);
 		this.setContentAreaFilled(false);
 		this.setIcon(icon);
 		addMouseListener(this);
 	}
 	public void mouseClicked(MouseEvent e){
 		try{
 			if (vm.ioIOVR() || vm.ioSOVR()){
 				String s;
				if (vm.ioIOVR()){
					s = "over 1000yen";
 				} else{
					s = "over 100coins";
 				}
 				System.out.println("error: " + s);
 				vm.ioACK();
 				AlertWindow alertWindow = new AlertWindow(s);
 				alertWindow.setVisible(true);
 				tsManager.retOverCoins(value);
 			} else{
 				switch (value){
 				case 50:
 					vm.ioC50();
 					vm.ioSTB();
 					break;
 				case 100:
 					vm.ioC100();
 					vm.ioSTB();
 					break;
 				case 500:
 					vm.ioC500();
 					vm.ioSTB();
 					break;
 				}
 			}
 		}catch(Exception e1){
 					System.out.println("Error in mouseClicked()");
 				}
 				tsManager.setCoin(value);
 	}
 
 	public void mouseEntered(MouseEvent e){
 	}
 
 	public void mouseExited(MouseEvent e){
 	}
 
 	public void mouseReleased(MouseEvent e){
 	}
 
 	public void mousePressed(MouseEvent e){
 	}
 }
