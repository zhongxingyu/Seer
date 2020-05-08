 package core;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class GUI extends JFrame {
 
 	private GraphicsRenderer parent;
 
 	BufferedImage buffer;
 
 	private JPanel buttonPanel, graphicsPanel, outputPanel;
 	private JTextArea textOutputArea;
 	private JButton loadButton, resetButton;
 	private JSlider ambiantSlider, intensitySlider;
 
 	public GUI(GraphicsRenderer parent){
 		this.parent = parent;
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		setSize(500, 500);
 
 		buttonPanel = new JPanel();
 		graphicsPanel = new JPanel();
 		outputPanel = new JPanel();
 		textOutputArea = new JTextArea();
 		JScrollPane scrollPane = new JScrollPane();
 		loadButton = new JButton("Load Model");
 		resetButton = new JButton("Reset");
 		ambiantSlider = new JSlider(0, 100);
 		ambiantSlider.setPreferredSize(new Dimension(75,20));
 		ambiantSlider.setToolTipText("Ambiant Light");
 		intensitySlider = new JSlider(0, 100);
 		intensitySlider.setPreferredSize(new Dimension(75,20));
 		intensitySlider.setToolTipText("Light Intensity");
 
 		buttonPanel.setBackground(Color.LIGHT_GRAY);
 		graphicsPanel.setBackground(Color.WHITE);
 		outputPanel.setBackground(Color.LIGHT_GRAY);
 
 		ActionListener aListener = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				GUIActionPerformed(e);
 			}
 		};
 		ChangeListener sliderChange = new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				GUISliderChange(e);
 			}
 		};
 		MouseListener mListener = new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e){
 				GUIMouseAction(e,"Press");
 			}
 			@Override
 			public void mouseReleased(MouseEvent e){
 				GUIMouseAction(e,"Release");
 			}
 		};
 		MouseMotionListener mMotionListener = new MouseAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent e){
 				GUIMouseAction(e,"Drag");
 			}
 		};
 
 		loadButton.addActionListener(aListener);
 		resetButton.addActionListener(aListener);
 		ambiantSlider.addChangeListener(sliderChange);
 		intensitySlider.addChangeListener(sliderChange);
 		graphicsPanel.addMouseListener(mListener);
 		graphicsPanel.addMouseMotionListener(mMotionListener);
 
 		buttonPanel.add(loadButton);
 		buttonPanel.add(resetButton);
 		buttonPanel.add(ambiantSlider);
 		buttonPanel.add(intensitySlider);
 		scrollPane.add(textOutputArea);
 
 		this.add(buttonPanel, BorderLayout.NORTH);
 		this.add(graphicsPanel, BorderLayout.CENTER);
 		this.add(scrollPane, BorderLayout.SOUTH);
 		setVisible(true);
 	}
 
 	protected void GUISliderChange(ChangeEvent e) {
 		if(e.getSource() instanceof JSlider){
 			if(e.getSource() == ambiantSlider){
 				double level = ((double)ambiantSlider.getValue())/100;
				System.out.println("Ambiant = "+level);
 				parent.setAmbiant(level,level,level);
 			}else if(e.getSource() == intensitySlider){
 				double level = ((double)intensitySlider.getValue())/100;
				System.out.println("Intensity = "+level);
 				parent.setIntensity(level,level,level);
 			}
 		}
 		repaint();
 	}
 
 	protected void GUIActionPerformed(ActionEvent e) {
 		if(e.getSource() instanceof JButton){
 			this.parent.buttonPressed(((JButton)e.getSource()).getText());
 		}
 		repaint();
 	}
 
 	protected void GUIMouseAction(MouseEvent e, String event){
 		if(e.getButton() == MouseEvent.BUTTON1){
 			if(event.equals("Press")){
 				parent.mousePressed(e);
 			}else if(event.equals("Release")){
 				parent.mouseReleased(e);
 			}
 		}else if(event.equals("Drag")){
 			parent.mouseDragged(e);
 		}
 		repaint();
 	}
 
 	/**
 	 * Renders a graphical output to be displayed
 	 */
 	private void updateBuffer() {
 		buffer = new BufferedImage(graphicsPanel.getWidth(),
 				graphicsPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = buffer.createGraphics();
 		g2.setColor(Color.WHITE);
 		g2.fillRect(0, 0, graphicsPanel.getWidth(), graphicsPanel.getHeight());
 		g2.setColor(Color.black);
 		parent.draw(g2);
 		g2.dispose();
 	}
 
 	/**
 	 * Updates Graphics and displays the current buffer image onto the window
 	 */
 	public void paint(Graphics g) {
 		updateBuffer();
 		Graphics2D g2 = (Graphics2D) graphicsPanel.getGraphics();
 		if (buffer != null) {
 			g2.drawImage(buffer, 0, 0, this);
 		}
 		buttonPanel.repaint();
 		outputPanel.repaint();
 	}
 
 
 
 }
