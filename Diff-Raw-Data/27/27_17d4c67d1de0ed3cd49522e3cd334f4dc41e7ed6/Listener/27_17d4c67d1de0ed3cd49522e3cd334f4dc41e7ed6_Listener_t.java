 package org.ndaguan.micromanager.mmtracker;
 
 import ij.ImagePlus;
 import ij.gui.ImageCanvas;
 import ij.gui.ImageWindow;
import ij.gui.Roi;
 import ij.gui.Toolbar;
 
 import java.awt.Event;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
import org.json.JSONException;
 import org.micromanager.MMStudioMainFrame;
 import org.micromanager.utils.MMScriptException;
 
 public class Listener implements MouseListener, MouseMotionListener,KeyListener,ActionListener{
 
 	private MMStudioMainFrame gui_;
 	private ImageCanvas canvas_;
 	private int lastX_, lastY_;
 	private static boolean isRunning_ = false;
 	private static Listener instance_;
 
 	public Listener(MMStudioMainFrame gui) {
 		gui_ =  gui;
 	}
 	public static Listener getInstance() {
 		return instance_;
 	}
 	public static Listener getInstance(MMStudioMainFrame gui) {
 		if(instance_ == null)
 			instance_ = new Listener(gui);		
 		return instance_;
 	}
 	public void start (String acqName) throws MMScriptException  {
 		if (isRunning_)
 			return;
 
 		ImagePlus imageplus = gui_.getAcquisition(acqName).getAcquisitionWindow().getHyperImage();
 		if(imageplus != null){
 			attach (imageplus.getWindow());
 			isRunning_ = true;
 		}
 
 	}
 
 	public void stop() {
 		if (canvas_ != null) {
 			canvas_.removeMouseListener(this);
 			canvas_.removeMouseMotionListener(this);
 			canvas_.removeKeyListener(this);
 		}
 		isRunning_ = false;
 	}
 
 	public boolean isRunning() {
 		return isRunning_;
 	}
 
 	public void attach(ImageWindow win) {
 		if (win == null)
 			return;
 		if (isRunning_)
 			return;
 		canvas_ = win.getCanvas();
 		canvas_.addMouseListener(this);
 		canvas_.addMouseMotionListener(this);
 		canvas_.addKeyListener(this);
 	}
 
 
 	public void mousePressed(MouseEvent e) {
 		if (Toolbar.getInstance() == null)
 			return;
 		if ((e.getModifiers() & Event.META_MASK) != 0)  
 			return;
 		if (Toolbar.getToolId() != Toolbar.RECTANGLE)
 			return;
 
 		int x = e.getX();
 		int y = e.getY();
 		lastX_ = canvas_.offScreenX(x);
 		lastY_ = canvas_.offScreenY(y);
 		setFocusdRoi(new Point(lastX_,lastY_));
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		if ((e.getModifiers() & Event.META_MASK) != 0)  
 			return;
 		if (Toolbar.getInstance() == null)
 			return;
 		if (Toolbar.getToolId() != Toolbar.RECTANGLE)
 			return;
 
 		int x = e.getX();
 		int y = e.getY();
 		int cX = canvas_.offScreenX(x);
 		int cY = canvas_.offScreenY(y);	
 		moveFocusdRoi(cX - lastX_,cY - lastY_);  
 		lastX_ = cX;
 		lastY_ = cY;
 
 	} 
 	public void mouseClicked(MouseEvent e) {} 
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseMoved(MouseEvent e) {}
 
 	public void keyPressed(KeyEvent e) {
 		if(!e.isControlDown())
 			return;			
 		Rectangle rectangle = canvas_.getImage().getRoi().getBounds();
 
 		if(e.getKeyCode() == KeyEvent.VK_A){			
 			addRoi(rectangle);
 		}
 		if(e.getKeyCode() == KeyEvent.VK_D){
 			deleteRoi();
 		}
 		if(e.getKeyCode() == KeyEvent.VK_S){
 			selectRoiAsReference();
 		}
 		if(e.getKeyCode() == KeyEvent.VK_C){
 			showChartManager();
 		}
 
 	}
 	public void keyReleased(KeyEvent e) {}
 	public void keyTyped(KeyEvent arg0) {}
 
 
 	public void actionPerformed(ActionEvent e) {
 		String cmdString = "";
 		if(e.getSource().getClass().toString().equals("class javax.swing.JButton"))
 			cmdString = ((javax.swing.JButton)e.getSource()).getToolTipText();
 		if(e.getSource().getClass().toString().equals("class javax.swing.JMenuItem"))
 			cmdString = ((javax.swing.JMenuItem)e.getSource()).getToolTipText();
 		if(e.getSource().getClass().toString().equals("class javax.swing.JRadioButtonMenuItem"))
 			cmdString = ((javax.swing.JRadioButtonMenuItem)e.getSource()).getToolTipText();
 		switch(cmdString){
 		case "Capture under Live View(fast)":
 			liveCapture();
 			break;	
 		case "Live View":
 			liveView();
 			break;	
 		case "Calibrate":
 			calibrate();
 			break;
 		case "hide/show gui":
 			showGui();
 			break;
 		case "Add ROI":

			Roi ROI = canvas_.getImage().getRoi();
			if(ROI == null){
				MMT.logError("Select a ROI first!");
				return;
			}
			Rectangle rectangle = ROI.getBounds();
 			addRoi(rectangle);
 			break;
 		case "Delete ROI":
 			deleteRoi();
 			break;
 		case "Set Current ROI as Preferences":
 			selectRoiAsReference();
 			break;
 		case "Show Detail in Chart":
 			showChartManager();
 			break;
 		case "Set XY Orign":
 			SetXYOrign();
 			break;
 		case "Capture under Multi-DACQ(full)":
 			multiAcq();
 			break;
 		case "Preferences":
 			showPreferencesDialog();
 			break;
 		case "Auto Contrast":
 			setAutoContrast();
 			break;
 		case "MagnetManual":
 			ShowMagnetManualDialBox();
 			break;
 		case "Rectangle tool for select a ROI":
 			if (Toolbar.getInstance() == null)
 				return;
 
 			Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
 			break;
 		case "Hand tool for moving the xyStage":
 			if (Toolbar.getInstance() == null)
 				return;
 			Toolbar.getInstance().setTool(Toolbar.HAND);
 			break;
 
 		case "lock":
 			lockEveryThingButThis();
 			break;
 
 		}
 
 	}

 	//action
 	private synchronized void setFocusdRoi(Point point) {
 		Function.getInstance().setFocusdRoi(point);
 	}
 	private synchronized void SetXYOrign() {
 		Function.getInstance().SetXYOrign();
 	}
 
 	private synchronized void moveFocusdRoi(int dx, int dy) {
 		Function.getInstance().moveFocusdRoi(dx, dy);
 	}
 
 	private synchronized void addRoi(Rectangle rectangle) {
 		Function.getInstance().addRoi(rectangle);
 	}
 
 	private synchronized void deleteRoi() {
 		Function.getInstance().deleteRoi();
 	}
 
 	private void selectRoiAsReference() {
 		Function.getInstance().selectRoiAsReference();
 	}
 
 	private void liveView() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().liveCapture();
 		}
 		})).start();
 	}
 
 	private void calibrate() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().calibrate();
 		}
 		})).start();
 	}
 
 	private void multiAcq() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().multiAcq();
 		}
 		})).start();
 	}
 
 	private void showPreferencesDialog() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().showPreferencesDialog();
 		}
 		})).start();
 	}
 
 	private void showChartManager() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().showChartManager();
 		}
 		})).start();		
 	}
 
 	private void ShowMagnetManualDialBox() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().ShowMagnetManualDialBox();
 		}
 		})).start();		
 	}
 
 	private void setAutoContrast() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().setAutoContrast();
 		}
 		})).start();
 
 	}
 
 	private void liveCapture() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().liveCapture();
 		}
 		})).start();		
 	}
 
 	private void showGui() {
 		(new Thread(new Runnable() { @Override public void run() {
 			Function.getInstance().showGui();
 		}
 		})).start();			
 	}
 	private void  lockEveryThingButThis(){
 		Function.getInstance().lockEveryThingButThis();
 	}
 
 }
