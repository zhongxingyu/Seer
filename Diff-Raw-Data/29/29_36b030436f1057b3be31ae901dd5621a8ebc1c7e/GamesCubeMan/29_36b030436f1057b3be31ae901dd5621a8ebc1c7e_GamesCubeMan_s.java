 package edu.berkeley.gcweb.gui.gamescubeman.XYZCube;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import netscape.javascript.JSObject;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;
 import edu.berkeley.gcweb.gui.gamescubeman.XYZCube.XYZCube.CubeVariation;
 
 @SuppressWarnings("serial")
 public class GamesCubeMan extends JApplet implements ChangeListener, ActionListener, CubeStateChangeListener {
 	private XYZCube cube;
 	private CubeCanvas cubeCanvas;
 	private Canvas3D canvas;
 	private JSlider scale, distance, gap, turningRate;
 	private JSpinner[] dimensions;
 	private JCheckBox antialiasing, colorChooserCheckBox, optionsCheckBox;
 	private JButton resetView, scramble, resetCube;
 	private JTextField stateField;
 	private JRadioButton[] variationButtons;
 	
 	private int size_x = 3, size_y = 3, size_z = 3;
 	private Color bg_color = Color.GRAY, fg_color = Color.WHITE;
 	private ArrayList<Face> legalFaces = null;
 	private boolean cubeRotations = true;
 	private boolean resizable = true;
 	private boolean focusIndicator = true;
 	private boolean drawAxis = false;
 	private void parseParameters() {
 		try {
 			size_x = Integer.parseInt(getParameter("size_x"));
 		} catch(Exception e) {}
 		try {
 			size_y = Integer.parseInt(getParameter("size_y"));
 		} catch(Exception e) {}
 		try {
 			size_z = Integer.parseInt(getParameter("size_z"));
 		} catch(Exception e) {}
 		try {
 			bg_color = Color.decode(getParameter("bg_color"));
 		} catch(Exception e) {}
 		try {
 			fg_color = Color.decode(getParameter("fg_color"));
 		} catch(Exception e) {}
 		cubeRotations = parseBoolean(getParameter("cube_rotations"), cubeRotations);
 		resizable = parseBoolean(getParameter("resizable"), resizable);
 		focusIndicator = parseBoolean(getParameter("focus_indicator"), focusIndicator);
 		drawAxis = parseBoolean(getParameter("draw_axis"), drawAxis);
 		try {
 			legalFaces = new ArrayList<Face>();
 			String faces = getParameter("legal_faces");
 			for(char f : faces.toCharArray())
 				legalFaces.add(Face.decodeFace(f));
 		} catch(Exception e) {
 			legalFaces = null;
 		}
 	}
 	
 	private Boolean parseBoolean(String s, boolean def) {
 		if(s == null)
 			return def;
 		if(s.equalsIgnoreCase("true"))
 			return true;
 		else if(s.equalsIgnoreCase("false"))
 			return false;
 		return def;
 	}
 	
 	private JSObject jso;
 	public void init() {
 		addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				canvas.requestFocusInWindow();
 			}
 			public void focusLost(FocusEvent e) {}
 		});
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					parseParameters();
 					
 					RollingJPanel options = new RollingJPanel();
 					options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
 					
 					cube = new XYZCube(size_x, size_y, size_z);
 					cube.setCubeRotations(cubeRotations);
 					cube.setLegalFaces(legalFaces);
 					
 					cube.addStateChangeListener(GamesCubeMan.this);
 					cubeCanvas = new CubeCanvas(cube, options);
 					canvas = cubeCanvas.getCanvas();
 					canvas.setFocusIndicator(focusIndicator);
 					canvas.setDrawAxis(drawAxis);
 					resetRotation();
 					
 					resetView = new JButton("Reset View");
 					resetView.setToolTipText(resetView.getText());
 					resetView.setMnemonic(KeyEvent.VK_R);
 					resetView.setFocusable(false);
 					resetView.addActionListener(GamesCubeMan.this);
 					
 					resetCube = new JButton("Reset Cube");
 					resetCube.setToolTipText(resetCube.getText());
 					resetCube.setFocusable(false);
 					resetCube.addActionListener(GamesCubeMan.this);
 					
 					scramble = new JButton("Scramble");
 					scramble.setToolTipText(scramble.getText());
 					scramble.setMnemonic(KeyEvent.VK_S);
 					scramble.setFocusable(false);
 					scramble.addActionListener(GamesCubeMan.this);
 					
 					colorChooserCheckBox = new JCheckBox("Choose colors", cubeCanvas.isColorEditing());
 					colorChooserCheckBox.setMnemonic(KeyEvent.VK_C);
 					colorChooserCheckBox.setFocusable(false);
 					colorChooserCheckBox.addActionListener(GamesCubeMan.this);
 					
 					optionsCheckBox = new JCheckBox("Options", false);
 					optionsCheckBox.setMnemonic(KeyEvent.VK_O);
 					optionsCheckBox.setFocusable(false);
 					optionsCheckBox.addActionListener(GamesCubeMan.this);
 					
 					distance = new JSlider(4, 200, (int) (cube.getCenter()[2]));
 					distance.setFocusable(false);
 					distance.addChangeListener(GamesCubeMan.this);
 					
 					antialiasing = new JCheckBox("Antialiasing", canvas.isAntialiasing());
 					antialiasing.setMnemonic(KeyEvent.VK_A);
 					antialiasing.setFocusable(false);
 					antialiasing.addActionListener(GamesCubeMan.this);
 					
 					options.add(sideBySide(new JLabel("Distance"), distance, antialiasing));
 					
 					scale = new JSlider(0, 10000, (int) canvas.getScale());
 					scale.setFocusable(false);
 					scale.addChangeListener(GamesCubeMan.this);
 					options.add(sideBySide(new JLabel("Scale"), scale));
 					
 					int[] dim = cube.getDimensions();
 					dimensions = new JSpinner[dim.length];
 					JPanel dims = new JPanel();
 					for(int ch = 0; ch < dimensions.length; ch++) {
 						dimensions[ch] = new JSpinner(new SpinnerNumberModel(dim[ch], 1, null, 1));
 						((JSpinner.NumberEditor)dimensions[ch].getEditor()).getTextField().setFocusable(false);
 						dimensions[ch].getEditor().setFocusable(false);
 						dimensions[ch].addChangeListener(GamesCubeMan.this);
 						if(ch != 0)
 							dims.add(new JLabel("x"));
 						dims.add(dimensions[ch]);
 					}
 					if(resizable)
 						options.add(dims);
 					
 					gap = new JSlider(0, 50, (int)(100*cube.getStickerGap()));
 					gap.setFocusable(false);
 					gap.addChangeListener(GamesCubeMan.this);
 					options.add(sideBySide(new JLabel("Gap"), gap));
 					
 					turningRate = new JSlider(1, cube.getMaxTurningRate(), cube.getTurningRate());
 					turningRate.setMajorTickSpacing(1);
 					turningRate.setMinorTickSpacing(1);
 					turningRate.setPaintTicks(true);
 					turningRate.setFocusable(false);
 					turningRate.addChangeListener(GamesCubeMan.this);
 					options.add(sideBySide(new JLabel("Speed"), turningRate));
 					
 					ButtonGroup g = new ButtonGroup();
 					CubeVariation[] vars = CubeVariation.values();
 					variationButtons = new JRadioButton[vars.length];
 					for(int i = 0; i < variationButtons.length; i++) {
 						variationButtons[i] = new JRadioButton(vars[i].toString(), vars[i] == cube.getCubeVariation());
 						variationButtons[i].setActionCommand(""+i);
 						variationButtons[i].setFocusable(false);
 						variationButtons[i].addActionListener(GamesCubeMan.this);
 						g.add(variationButtons[i]);
 					}
 					if(resizable)
 						options.add(sideBySide(variationButtons));
 					
 					JPanel topHalf = new JPanel(new BorderLayout());
 					
 					JPanel temp = new JPanel();
 					temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
 					temp.add(sideBySide(true, resetView, resetCube, scramble));
 					temp.add(sideBySide(false, colorChooserCheckBox, optionsCheckBox));
 					topHalf.add(temp, BorderLayout.PAGE_START);
 					
 					stateField = new JTextField(cube.getState());
//					topHalf.add(stateField, BorderLayout.CENTER);
 
 					JPanel pane = new JPanel(new BorderLayout());
 					setContentPane(pane);
 					
 					pane.add(topHalf, BorderLayout.PAGE_START);
 					pane.add(cubeCanvas, BorderLayout.CENTER);
 					canvas.requestFocusInWindow();
 					
 					setBG_FG(pane, bg_color, fg_color);
 				}
 			});
 			jso = JSObject.getWindow(this);
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setBG_FG(JComponent comp, Color bg, Color fg) {
 		if(comp instanceof JButton)
 			return;
 		comp.setBackground(bg);
 		comp.setForeground(fg);
 		for(Component child : comp.getComponents()) {
 			if(child instanceof JComponent)
 				setBG_FG((JComponent)child, bg, fg);
 			else if(!(child instanceof JButton)) {
 				child.setBackground(bg);
 				child.setForeground(fg);
 			}
 		}
 	}
 	
 	private JPanel sideBySide(JComponent... cs) {
 		return sideBySide(false, cs);
 	}
 	private JPanel sideBySide(boolean resize, JComponent... cs) {
 		JPanel p = new JPanel();
 		if(resize)
 			p.setLayout(new GridLayout(1, 0));
 		else
 			p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
 		p.setBackground(Color.WHITE);
 		for(JComponent c : cs)
 			p.add(c);
 		return p;
 	}
 
 	public void stateChanged(ChangeEvent e) {
 		Object src = e.getSource();
 		if(src == gap) {
 			cube.setStickerGap(gap.getValue() / 100.);
 		} else if(src == distance) {
 			double[] center = cube.getCenter();
 			center[2] = distance.getValue();
 			cube.setCenter(center[0], center[1], center[2]);
 		} else if(src == scale) {
 			canvas.setScale(scale.getValue());
 		} else if(e.getSource() == turningRate) {
 			cube.setTurningRate(turningRate.getValue());
 		} else if(src instanceof JSpinner) {
 			cube.setDimensions((Integer) dimensions[0].getValue(), (Integer) dimensions[1].getValue(), (Integer) dimensions[2].getValue());
 			cube.setCubeVariation(getSelectedVariation());
 		} 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == resetView)
 			resetRotation();
 		else if(e.getSource() == scramble)
 			cube.scramble();
 		else if(e.getSource() == resetCube)
 			cube.resetCube();
 		else if(e.getSource() == antialiasing)
 			canvas.setAntialiasing(antialiasing.isSelected());
 		else if(e.getSource() == colorChooserCheckBox)
 			cubeCanvas.setColorEditing(colorChooserCheckBox.isSelected());
 		else if(e.getSource() == optionsCheckBox)
 			cubeCanvas.setOptionsVisible(optionsCheckBox.isSelected());
 		else if(e.getSource() instanceof JRadioButton)
 			cube.setCubeVariation(getSelectedVariation());
 	}
 	private CubeVariation getSelectedVariation() {
 		for(int i = 0; i < variationButtons.length; i++)
 			if(variationButtons[i].isSelected())
 				return CubeVariation.values()[i];
 		return null;
 	}
 
 	private void resetRotation() {
 		cube.setRotation(new RotationMatrix(0, -45));
 		canvas.mousePressed(null);
 	}
 	
 	public String getBoardString() {
 		return cube.getState();
 	}
 	
 	public boolean doMove(String move) {
 		return cube.doTurn(move);
 	}
 
 	public void cubeStateChanged(XYZCube src, final FaceLayerTurn turn) {
 		if(jso != null) {
 			new Thread() {
 				public void run() {
 					//we do this in a separate thread because the call() method will
 					//hand for the javascript
 					jso.call("cubeStateChanged", new Object[] { turn });
 				}
 			}.start();
 		}
 		stateField.setText(src.getState());
 	}
 	
 	public static void main(String[] args) {
 		final GamesCubeMan a = new GamesCubeMan();
 		a.init();
 		a.setPreferredSize(new Dimension(400, 500));
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JFrame f = new JFrame();
 				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				JPanel pane = new JPanel();
 				f.setContentPane(pane);
 				f.add(a);
 				f.pack();
 				f.setVisible(true);
 				a.canvas.requestFocusInWindow();
 			}
 		});
 	}
 }
