 package balle.bluetooth;
 
 import java.awt.Frame;
 import java.awt.TextField;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import balle.controller.BluetoothController;
 
 public class TestBluetooth extends Frame implements KeyListener {
 
 	/**
 	 * 
 	 */
 	TextField t1;
 	BluetoothController controller;
 	int currentWheelSpeed = 500;
	int currentTurnAngle = 180;
 
 	public TestBluetooth() {
 		super();
 		addKeyListener(this);
 		setSize(200, 100);
 		setVisible(true);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		
 		controller = new BluetoothController(new Communicator());
 	}
 
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyChar() == 'w')
 		{
 			System.out.println("Forward!");
 			controller.forward(currentWheelSpeed);
 		}
 		else if (e.getKeyChar() == 's')
 		{
 			System.out.println("Backward!");
 			controller.backward(currentWheelSpeed);
 		}		
 		else if (e.getKeyChar() == 'd')
 		{
 			System.out.println("Right");
 			controller.setWheelSpeeds(currentWheelSpeed, currentWheelSpeed * 3 / 4);
 		}
 		else if (e.getKeyChar() == 'a')
 		{
 			System.out.println("Left");
 			controller.setWheelSpeeds(currentWheelSpeed * 3 / 4, currentWheelSpeed);
 		}
 		else if (e.getKeyChar() == 'x')
 		{
 			System.out.println("Kick!");
 			controller.kick();
 		}
 		else if (e.getKeyChar() == 'q')
 		{
 			System.out.println("Stop!");
 			controller.stop();
 		}
 		else if (e.getKeyChar() == 'e')
 		{
 			currentWheelSpeed += 10;
 			if (currentWheelSpeed > controller.getMaximumWheelSpeed()) currentWheelSpeed = controller.getMaximumWheelSpeed();
 			System.out.println("Speed: " +  currentWheelSpeed);
 		}
 		else if (e.getKeyChar() == 'r')
 		{
 			currentWheelSpeed -= 10;
 			if (currentWheelSpeed < 0) currentWheelSpeed = 0;
 			System.out.println("Speed: " +  currentWheelSpeed);
 		}
		else if (e.getKeyChar() == 't') {
			currentTurnAngle += 5;
			System.out.println("Angle: " + currentTurnAngle);
		}
		else if (e.getKeyChar() == 'y') {
			currentTurnAngle -= 5;
			System.out.println("Angle: " + currentTurnAngle);
		}
		else if (e.getKeyChar() == ' '){
			controller.rotate(currentTurnAngle,180);
		}
 		else
 		{
 			System.out.println("pressed: " + e.getKeyChar());
 		}
 	}
 
 	public void keyReleased(KeyEvent e) {
 		return;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new TestBluetooth();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 		
 	}
 }
