 package ch.zhaw.headtracker.gui;
 
 import ch.zhaw.headtracker.Algorithm;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class ControlPanel extends JFrame implements ChangeListener
 {
 	private final JSlider filterThresholdSlider;
 	private final JTextField filterThresholdTextField;
 
 	private final JSlider minimumSlider;
 	private final JTextField minimumTextField;
 
 	private final JSlider maximumSlider;
 	private final JTextField maximumTextField;
 
 	private final JSlider contrastSlider;
 	private final JTextField contrastTextField;
 
 	public ControlPanel()
 	{
 		super("Control Panel");
 
 		setSize(400, 300);
 		setLayout(null);
 
 		JLabel filterThresholdLabel = new JLabel("Filter Threshold:");
 		filterThresholdLabel.setLocation(20, 20);
 		filterThresholdLabel.setSize(200, 20);
 		add(filterThresholdLabel);
 
 		filterThresholdSlider = new JSlider(0, 50);
 		filterThresholdSlider.setValue(Algorithm.filterThreshold);
 		filterThresholdSlider.setLocation(150, 20);
 		filterThresholdSlider.setSize(150, 20);
 		filterThresholdSlider.addChangeListener(this);
 		add(filterThresholdSlider);
 
 		filterThresholdTextField = new JTextField(Algorithm.filterThreshold + "");
 		filterThresholdTextField.setLocation(300, 20);
 		filterThresholdTextField.setSize(80, 20);
 		add(filterThresholdTextField);
 
 		JLabel minimumLabel = new JLabel("Minimum:");
 		minimumLabel.setLocation(20, 50);
 		minimumLabel.setSize(200, 20);
 		add(minimumLabel);
 
		minimumSlider = new JSlider(0, 50);
 		minimumSlider.setValue(Algorithm.minimum);
 		minimumSlider.setLocation(150, 50);
 		minimumSlider.setSize(150, 20);
 		minimumSlider.addChangeListener(this);
 		add(minimumSlider);
 
 		minimumTextField = new JTextField(Algorithm.minimum + "");
 		minimumTextField.setLocation(300, 50);
 		minimumTextField.setSize(80, 20);
 		add(minimumTextField);
 
 		JLabel maximumLabel = new JLabel("Maximum:");
 		maximumLabel.setLocation(20, 80);
 		maximumLabel.setSize(200, 20);
 		add(maximumLabel);
 
		maximumSlider = new JSlider(0, 50);
 		maximumSlider.setValue(Algorithm.maximum);
 		maximumSlider.setLocation(150, 80);
 		maximumSlider.setSize(150, 20);
 		maximumSlider.addChangeListener(this);
 		add(maximumSlider);
 
 		maximumTextField = new JTextField(Algorithm.maximum + "");
 		maximumTextField.setLocation(300, 80);
 		maximumTextField.setSize(80, 20);
 		add(maximumTextField);
 
 		JLabel contrastLabel = new JLabel("Contrast Threshold:");
 		contrastLabel.setLocation(20, 110);
 		contrastLabel.setSize(200, 20);
 		add(contrastLabel);
 		
 		contrastSlider = new JSlider(0, 255);
 		contrastSlider.setValue(Algorithm.contrastThreshold);
 		contrastSlider.setLocation(150, 110);
 		contrastSlider.setSize(150, 20);
 		contrastSlider.addChangeListener(this);
 		add(contrastSlider);
 
 		contrastTextField = new JTextField(Algorithm.contrastThreshold + "");
 		contrastTextField.setLocation(300, 110);
 		contrastTextField.setSize(80, 20);
 		add(contrastTextField);
 
 		final JCheckBox showOriginalCheckBox = new JCheckBox("Show original", Algorithm.showOriginal);
 		showOriginalCheckBox.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent actionEvent)
 			{
 				Algorithm.showOriginal = showOriginalCheckBox.isSelected();
 			}
 		});
 		showOriginalCheckBox.setLocation(20, 140);
 		showOriginalCheckBox.setSize(200, 20);
 		add(showOriginalCheckBox);
 
 
 		setVisible(true);
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent changeEvent)
 	{
 		if (changeEvent.getSource().equals(filterThresholdSlider))
 		{
 			Algorithm.filterThreshold = filterThresholdSlider.getValue();
 			filterThresholdTextField.setText(Algorithm.filterThreshold + "");
 		}
 
 		if (changeEvent.getSource().equals(minimumSlider))
 		{
 			Algorithm.minimum = minimumSlider.getValue();
 			minimumTextField.setText(Algorithm.minimum + "");
 		}
 
 		if (changeEvent.getSource().equals(maximumSlider))
 		{
 			Algorithm.maximum = maximumSlider.getValue();
 			maximumTextField.setText(Algorithm.maximum + "");
 		}
 
 		if (changeEvent.getSource().equals(contrastSlider))
 		{
 			Algorithm.contrastThreshold = contrastSlider.getValue();
 			contrastTextField.setText(Algorithm.contrastThreshold + "");
 		}
 	}
 }
