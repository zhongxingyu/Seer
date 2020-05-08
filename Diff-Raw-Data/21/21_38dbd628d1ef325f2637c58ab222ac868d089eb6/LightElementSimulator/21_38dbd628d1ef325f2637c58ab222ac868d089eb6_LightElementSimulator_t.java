 package com.partyrock.gui;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import com.partyrock.LightMaster;
 import com.partyrock.element.ElementController;
 import com.partyrock.element.ElementSimulator;
 
 /**
  * The GUI Simulator for all elements. This does not need to be registered as a listener for element updating because
  * it's constantly re-rendered
  * @author Matthew
  * 
  */
 public class LightElementSimulator extends Shell {
 
 	private LightMaster master;
 	private final int TIMER_INTERVAL = 33;
 	private final Display display;
 	private ElementController dragged = null;
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		try {
 			Display display = Display.getDefault();
 			LightElementSimulator shell = new LightElementSimulator(null);
 			shell.open();
 			shell.layout();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the shell.
 	 * @param display
 	 */
 	public LightElementSimulator(final LightWindow window) {
 		super(window.getDisplay(), SWT.SHELL_TRIM);
 
 		display = window.getDisplay();
 		this.master = window.getMaster();
 		setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		final Canvas canvas = new Canvas(this, SWT.NONE);
 		canvas.addMouseMoveListener(new MouseMoveListener() {
 			public void mouseMove(MouseEvent e) {
 				simMouseMove(e);
 			}
 		});
 		canvas.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent e) {
 				simMouseDown(e);
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 				simMouseUp(e);
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				simMouseDoubleClick(e);
 			}
 		});
 		canvas.addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				renderSimulator(e);
 			}
 		});
 
 		createContents();
 
 		Runnable runnable = new Runnable() {
 			public void run() {
 				// Catch when the canvas may have been disposed
 				if (canvas.isDisposed()) {
 					return;
 				}
 
 				canvas.redraw();
 				display.timerExec(TIMER_INTERVAL, this);
 			}
 		};
 
 		display.timerExec(TIMER_INTERVAL, runnable);
 	}
 
 	/**
 	 * Create contents of the shell.
 	 */
 	protected void createContents() {
 		setText("Party Simulator");
 		setSize(700, 500);
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	/**
 	 * Does all of the rendering for the simulator. Called ~30 times/second (30 FPS)
 	 * @param e The PaintEvent to get the GC from
 	 */
 	public void renderSimulator(PaintEvent e) {
 		GC gc = e.gc;
 
 		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
 
 		gc.fillRectangle(0, 0, e.width, e.height);
 
 		for (ElementController c : master.getElements()) {
 			c.getSimulator().render(gc);
 		}
 	}
 
 	/**
 	 * Handles when the mouse is pressed down, so works on dragging
 	 * @param e The MouseEvent for mouseDown
 	 */
 	public void simMouseDown(MouseEvent e) {
 		ArrayList<ElementController> elements = master.getElements();
 
 		for (int a = elements.size() - 1; a >= 0; a--) {
 			ElementController element = elements.get(a);
 			if (element.getSimulator().mouseDown(e)) {
 				dragged = element;
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Handles double clicking to compress or not compress
 	 */
 	public void simMouseDoubleClick(MouseEvent e) {
 		ArrayList<ElementController> elements = master.getElements();
 
 		for (int a = elements.size() - 1; a >= 0; a--) {
 			ElementSimulator sim = elements.get(a).getSimulator();
 			if (sim.mouseDown(e)) {
 				sim.setCollapsed(!sim.isCollapsed());
				master.getLocationManager().unsavedChanges();
 				break;
 			}
 		}
 	}
 
 	/**
 	 * MouseUp, so handles stopping selection
 	 * @param e The MouseEvent for mouseUp
 	 */
 	public void simMouseUp(MouseEvent e) {
 		dragged = null;
 	}
 
 	/**
 	 * Handles adjusting the position if possible
 	 * @param e The MouseEvent for dragging
 	 */
 	public void simMouseMove(MouseEvent e) {
 		if (dragged != null) {
 			dragged.getSimulator().setPos(e.x, e.y);
			master.getLocationManager().unsavedChanges();
 		}
 	}
 }
