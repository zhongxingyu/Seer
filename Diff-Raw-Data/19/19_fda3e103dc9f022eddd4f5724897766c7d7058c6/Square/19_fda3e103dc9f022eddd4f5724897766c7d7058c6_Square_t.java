 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class Square extends JPanel implements MouseListener {
 	private static final Color defaultColor = new Color(0, 0, 0);
 	private static final Color activeColor = new Color(127, 127, 127);
 	private static int MAX_POWER = 1000;
 	private static double MAX_PULSE_PERCENT = 0.50;
 	private static double PULSE_STEP_UP_PERCENT = 0.10;
 	
 	private Timer timer;
 	private boolean charging, pulsing;
 	private boolean pulseUp, toggle;
 	private int timerCycles;
 	private int power, pulse;
 	private double pulseUpTo;
 	private int row, column;
 	
 	private PulseDelegate pulseDelegate;
 	
 	public Square(int r, int c) {
 		power = 0;
 		
 		row = r;
 		column = c;
 		pulseUp = true;
 		
 		setBackground(defaultColor);
 		addMouseListener(this);
 	}
 	
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 		//Update the power color
 		int red = (int)scale(power + pulse, 0, MAX_POWER, defaultColor.getRed(), activeColor.getRed());
 		int green = (int)scale(power + pulse, 0, MAX_POWER, defaultColor.getGreen(), activeColor.getGreen());
 		int blue = (int)scale(power + pulse, 0, MAX_POWER, defaultColor.getBlue(), activeColor.getBlue());
 		
 		if (red > 255) red = 255; else if (red < 0) red = 0;
 		if (green > 255) green = 255; else if (green < 0) green = 0;
 		if (blue > 255) blue = 255; else if (blue < 0) blue = 0;
 		
 		setBackground(new Color(red, green, blue));
 	}
 
 	public void mouseClicked(MouseEvent arg0) { }
 	public void mouseEntered(MouseEvent arg0) {	}
 	public void mouseExited(MouseEvent arg0) { }
 	
 	public void mousePressed(MouseEvent arg0) {
 		timerCycles = 0;
 		charging = true;
 	}
 	
 	public void mouseReleased(MouseEvent arg0) {
 		updatePower();
 		pulsing = true;
 		pulseUp = true;
 		charging = false;
 	}
 	
 	public void updatePower() {
 		power = (int) scale(timerCycles, 0, BoardPanel.CYCLE_DURATION, 0, MAX_POWER);
 		repaint();
 	}
 	
 	public void pulse() {
 		if (power != 0) {
 			if (pulseUp) {
 				pulse += power * PULSE_STEP_UP_PERCENT;
 			} else {
 				pulse -= power * PULSE_STEP_UP_PERCENT;
 			}
 		} else {
 			if (pulseUp) {
 				pulse += Math.ceil(pulse * PULSE_STEP_UP_PERCENT);
 			} else {
 				pulse -= Math.ceil(pulse * PULSE_STEP_UP_PERCENT);
 			}
 		}
 		
 		if (!toggle && power != 0 && pulse / power > MAX_PULSE_PERCENT) {
 			pulseUp = false;
			pulseDelegate.pulseOccurred(this);
 		} else if (toggle && pulse >= pulseUpTo) {
 			pulseUp = false;
			pulseDelegate.pulseOccurred(this);
 		}
 		
 		if (pulse <= 0 && !toggle) {
 			pulse = 0;
 			pulsing = false;
 			pulseUp = true;
 		} else if (pulse + power <= 0 && toggle) {
 			power = 0;
 			pulse = 0;
 			pulsing = false;
 			pulseUp = true;
 			toggle = false;
 		}
 		repaint();
 	}
 	
 	public void timerFired() {
 		timerCycles++;
 		
 		if (charging) {
 			updatePower();
 		}
 		
 		if (pulsing) {
 			pulse();
 		}
 	}
 	
 	private static double scale(double current, double currentMin, double currentMax, double targetMin, double targetMax) {
 		double scaled = targetMin + (targetMax - targetMin) * ((current - currentMin) / (currentMax - currentMin));
 		
 		return scaled;
 	}
 	
 	public void toggleWithPulse(int pulse) {
 		if (power == 0) {
 			this.pulse = 0;
 			this.power = pulse;
 			pulseUpTo = pulse;
 			toggle = true;
 			pulsing = true;
 			pulseUp = true;
 		}
 	}
 	
 	public void setPulseDelegate(PulseDelegate pd) {
 		pulseDelegate = pd;
 	}
 	
 	public int getRow() {
 		return row;
 	}
 	
 	public int getColumn() {
 		return column;
 	}
 	
 	public int getPower() {
 		return power;
 	}
 	
 	public boolean isPulsing() {
 		return pulsing;
 	}
 }
