 package com.sessionfive.core.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.media.opengl.GLCanvas;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 
 public class CentralControlPaletteUI {
 
 	private final CentralControlPalette centralControlPalette;
 
 	private TranslucentPalette window;
 	private JButton choosePresentationButton;
 	private JComboBox layoutChoice;
 	private JComboBox animationChoice;
 	private JTextField layerText;
 	private final GLCanvas canvas;
 
 	private JSlider xRotationSlider;
 
 	private JSlider yRotationSlider;
 
 	private JSlider zRotationSlider;
 
 	public CentralControlPaletteUI(CentralControlPalette centralControlPalette,
 			GLCanvas canvas) {
 		this.centralControlPalette = centralControlPalette;
 		this.canvas = canvas;
 		window = new TranslucentPalette("Sessionfire - Central Control", false,
 				SwingUtilities.getWindowAncestor(canvas));
 		initComponents();
 		window.pack();
 		window.setLocation(100, 100);
 	}
 
 	public void show() {
 		window.showPalette();
 	}
 
 	public void setStatus(String status) {
 		window.setStatus(status);
 	}
 
 	private void initComponents() {
 		choosePresentationButton = new JButton("Choose Presentation...");
 		choosePresentationButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				centralControlPalette.choosePresentation(canvas,
 						(Layouter) layoutChoice.getSelectedItem(),
 						(AnimationFactory) animationChoice.getSelectedItem());
 
 				centralControlPalette.setRotation(xRotationSlider.getValue(),
 						yRotationSlider.getValue(), zRotationSlider.getValue());
 			}
 		});
 
 		JComponent contentPane = (JComponent) window.getEmbeddedContentPane();
 		contentPane.setLayout(new BorderLayout());
 
 		JPanel subContentPane = new JPanel();
 		subContentPane.setOpaque(false);
 		contentPane.add(subContentPane, BorderLayout.NORTH);
 
 		subContentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
 		subContentPane.setLayout(new GridLayout(8, 1, 15, 15));
 		subContentPane.add(choosePresentationButton);
 
 		DefaultComboBoxModel layoutModel = new DefaultComboBoxModel();
 		Layouter[] allLayouter = centralControlPalette.getLayouter();
 		for (Layouter layouter : allLayouter) {
 			layoutModel.addElement(layouter);
 		}
 		layoutChoice = new JComboBox(layoutModel);
 		layoutChoice.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object selectedLayouter = layoutChoice.getSelectedItem();
 				if (selectedLayouter != null) {
 					centralControlPalette
 							.changeLayout((Layouter) selectedLayouter);
 				}
 			}
 		});
 		subContentPane.add(layoutChoice);
 
 		DefaultComboBoxModel animationModel = new DefaultComboBoxModel();
 		AnimationFactory[] animationFactories = centralControlPalette
 				.getAnimators();
 		for (AnimationFactory animationFactory : animationFactories) {
 			animationModel.addElement(animationFactory);
 		}
 		animationChoice = new JComboBox(animationModel);
 		animationChoice.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object selectedAnimation = animationChoice.getSelectedItem();
 				if (selectedAnimation != null) {
 					centralControlPalette
 							.changeAnimation((AnimationFactory) selectedAnimation);
 				}
 			}
 		});
 		subContentPane.add(animationChoice);
 
 		JButton backgroundChooser = new JButton("Choose Background Color...");
 		subContentPane.add(backgroundChooser);
 		backgroundChooser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				chooseBackground();
 			}
 		});
 
 		layerText = new JTextField(centralControlPalette.getLayerText());
 		subContentPane.add(layerText);
 		layerText.getDocument().addDocumentListener(new DocumentListener() {
 			public void removeUpdate(DocumentEvent e) {
 				centralControlPalette.setLayerText(layerText.getText());
 			}
 
 			public void insertUpdate(DocumentEvent e) {
 				centralControlPalette.setLayerText(layerText.getText());
 			}
 
 			public void changedUpdate(DocumentEvent e) {
 				centralControlPalette.setLayerText(layerText.getText());
 			}
 		});
 
 		xRotationSlider = new JSlider(0, 360, 0);
 		yRotationSlider = new JSlider(0, 360, 0);
 		zRotationSlider = new JSlider(0, 360, 0);
 		subContentPane.add(xRotationSlider);
 		subContentPane.add(yRotationSlider);
 		subContentPane.add(zRotationSlider);
 
 		ChangeListener rotationSliderListener = new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				centralControlPalette.setRotation(xRotationSlider.getValue(),
 						yRotationSlider.getValue(), zRotationSlider.getValue());
 			}
 		};
 
 		xRotationSlider.addChangeListener(rotationSliderListener);
 		yRotationSlider.addChangeListener(rotationSliderListener);
 		zRotationSlider.addChangeListener(rotationSliderListener);
 		
 		PanelExtension[] extensions = centralControlPalette.getExtensionPanels();
 		if(extensions.length > 0) {
 			JPanel panel = new JPanel();
 			panel.setOpaque(false);
 			panel.setDoubleBuffered(false);
 			panel.setBackground(new Color(0, 0, 0, 0));
 
			panel.setLayout(new GridLayout(0, 1));
 			
 			for (PanelExtension panelExtension : extensions) {
 				if (panelExtension.getPanel() != null) {
 					panel.add(panelExtension.getPanel());
 				}
 			}
 			
 			contentPane.add(panel, BorderLayout.SOUTH);
 		}
 		
 	}
 
 	protected void chooseBackground() {
 		Color newColor = JColorChooser.showDialog(window,
 				"Choose Background Color", centralControlPalette
 						.getBackgroundColor());
 
 		if (newColor != null) {
 			centralControlPalette.setBackgroundColor(newColor);
 		}
 	}
 
 }
