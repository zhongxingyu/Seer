 import lejos.nxt.*;
 
 public class RobotTest {
 	private static int drawnMotorIndex = -1;
 	private static int drawnMotorSpeed = -1;
 	private static int drawnSelectionPosition = -1;
 	private static int drawnRotationSpeed = -1;
 
 	public static void main(String[] args) {
 		int currentMotorIndex = 0;
 		int currentMotorSpeed = 0;
 		int selectionPosition = 0;
 		while (true) {
 			updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 			if (Button.ESCAPE.isDown()) {
 				if (Motor.getInstance(currentMotorIndex).isMoving()) {
 					Motor.getInstance(currentMotorIndex).stop();
 
 					while (Button.ESCAPE.isDown()) {
 						Button.waitForAnyEvent(10000000);
 					}
 				} else {
 					break;
 				}
 			} else if (Button.RIGHT.isDown()) {
 				if (selectionPosition==0) {
 					currentMotorIndex = (currentMotorIndex+1)%3;
 					updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 					
 					while (Button.RIGHT.isDown()) {
 						Button.waitForAnyEvent(10000000);
 					}
 
 				} else if (selectionPosition==1) {
 					currentMotorSpeed = (currentMotorSpeed+1)%1000;
					Motor.A.setSpeed(currentMotorSpeed);
					Motor.B.setSpeed(currentMotorSpeed);
					Motor.C.setSpeed(currentMotorSpeed);
 					updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 
 					if (Button.waitForAnyEvent(700)==0) {
 						while (Button.waitForAnyEvent(200)==0) {
 							currentMotorSpeed = (currentMotorSpeed+1)%1000;
							Motor.A.setSpeed(currentMotorSpeed);
							Motor.B.setSpeed(currentMotorSpeed);
							Motor.C.setSpeed(currentMotorSpeed);
 							updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 						}
 					}
 
 				} else {
 					Motor.getInstance(currentMotorIndex).forward();
 				}
 			} else if (Button.LEFT.isDown()) {
 				if (selectionPosition==0) {
 					currentMotorIndex = (currentMotorIndex-1)%3;
 					updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 
 					while (Button.LEFT.isDown()) {
 						Button.waitForAnyEvent(10000000);
 					}
 
 				} else if (selectionPosition==1) {
 					currentMotorSpeed = (currentMotorSpeed-1)%1000;
					Motor.A.setSpeed(currentMotorSpeed);
					Motor.B.setSpeed(currentMotorSpeed);
					Motor.C.setSpeed(currentMotorSpeed);
 					updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 
 					if (Button.waitForAnyEvent(700)==0) {
 						while (Button.waitForAnyEvent(200)==0) {
 							currentMotorSpeed = (currentMotorSpeed-1)%1000;
							Motor.A.setSpeed(currentMotorSpeed);
							Motor.B.setSpeed(currentMotorSpeed);
							Motor.C.setSpeed(currentMotorSpeed);
 							updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 						}
 					}
 
 				} else {
 					Motor.getInstance(currentMotorIndex).backward();
 				}
 			} else if (Button.ENTER.isDown()) {
 				selectionPosition = (selectionPosition+1)%3;
 				updateDisplay(currentMotorIndex, currentMotorSpeed, selectionPosition);
 
				while (Button.ENTER.isDown()) {
					Button.waitForAnyEvent(10000000);
 				}
 			}
 		}
 	}
 
 	private static void updateDisplay(int motorIndex, int currentMotorSpeed, int selectionPosition) {
 		if (RobotTest.drawnMotorIndex!=motorIndex) {
 			if (motorIndex==0) {
 				LCD.drawString("A            ", 0, 0);
 			} else if (motorIndex==1) {
 				LCD.drawString("B            ", 0, 0);
 			} else if (motorIndex==2) {
 				LCD.drawString("C            ", 0, 0);
 			} else {
 				LCD.drawString("INVALID INDEX", 0, 0);
 			}
 
 			RobotTest.drawnMotorIndex = motorIndex;
 		}
 
 		if (RobotTest.drawnMotorSpeed!=currentMotorSpeed) {
 			LCD.drawInt(currentMotorSpeed, 0, 1);
 
 			RobotTest.drawnMotorSpeed = currentMotorSpeed;
 		}
 
 		LCD.drawString("Run", 0, 2);
 
 		if (RobotTest.drawnRotationSpeed!=Motor.getInstance(motorIndex).getRotationSpeed()) {
 			LCD.drawString("Actual speed:", 0, 4);
 			LCD.drawString("     ", 0, 5);
 			LCD.drawInt(Motor.getInstance(motorIndex).getRotationSpeed(), 0, 5);
 
 			RobotTest.drawnRotationSpeed = Motor.getInstance(motorIndex).getRotationSpeed();
 		}
 
 		if (RobotTest.drawnSelectionPosition!=selectionPosition) {
 			LCD.drawString(" ", 15, 0);
 			LCD.drawString(" ", 15, 1);
 			LCD.drawString(" ", 15, 2);
 			LCD.drawString("<", 15, selectionPosition);
 
 			RobotTest.drawnSelectionPosition = selectionPosition;
 		}
 	}
 
 }
