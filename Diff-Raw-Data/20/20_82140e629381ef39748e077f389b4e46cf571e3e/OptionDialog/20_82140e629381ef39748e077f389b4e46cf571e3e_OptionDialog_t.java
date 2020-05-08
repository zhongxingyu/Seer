 /*
 * MandelBrotViewer
 * Copyright (C) 2012 Pierre-Henri Symoneaux
 * 
 * This work is licensed under the Creative Commons 
 * Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
 * 
 * To view a copy of this license,
 * visit http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode .
 */
 
 package phsym.mandelbrot.dialogs;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import phsym.mandelbrot.ColorMap;
 import phsym.mandelbrot.FractalCanvas;
 import phsym.mandelbrot.ColorMap.ColorMode;
 
 
 /**
 *	@author Pierre-Henri Symoneaux
 */
 public class OptionDialog extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3775821772884902700L;
 	
 	private FractalCanvas canvas;
 	
 	private GridLayout layout;
 	
 	private JTextField iterations;
 	
	private JComboBox<ColorMode> colorMode;
 	
 	private JButton b_cancel;
 	private JButton b_OK;
 
 	public OptionDialog(final FractalCanvas canvas) {
 		super(canvas.getFrame(), "Settings", true);
 		this.canvas = canvas;
 		
 		setLocationRelativeTo(canvas);
 		setResizable(false);
 		
 		layout = new GridLayout(0, 2);
 		
 		setLayout(layout);
 		add(new JLabel("Iterations : "));
 		iterations = new JTextField(Integer.toString(canvas.getMax_iteration()));
 		
 		add(iterations);
 		
 		add(new JLabel("ColorMode : "));
 		
 		colorMode = new JComboBox<ColorMode>(ColorMap.getColorModes());
 		add(colorMode);
 		
 		b_cancel = new JButton("Cancel");
 		b_cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		add(b_cancel);
 		
 		b_OK = new JButton("OK");
 		b_OK.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				canvas.setMax_iteration(Integer.parseInt(iterations.getText()));
 				canvas.setColorMode((ColorMode) colorMode.getSelectedItem());
 				setVisible(false);
 			}
 		});
 		add(b_OK);
 		
 		pack();
 		validate();
 	}
 	
 	@Override
 	public void setVisible(boolean b) {
 		if(b)
 		{
 			iterations.setText(Integer.toString(canvas.getMax_iteration()));
 			colorMode.setSelectedItem(canvas.getColorMode());
 		}
 		super.setVisible(b);
 	}
 }
