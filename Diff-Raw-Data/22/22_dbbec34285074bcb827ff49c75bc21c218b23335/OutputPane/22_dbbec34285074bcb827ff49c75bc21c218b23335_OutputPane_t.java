 /**
  * 
  */
 package gui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import core.HillClimberImplementation;
 
 /**
  * @author Phillipp Ohl
  * @version 0.1
  */
 public class OutputPane {
 
 	private JFrame frame;
 	private JLabel labelX1;
 	private JLabel labelX2;
 	private JLabel labelX3;
 	private JLabel labelX4;
 	private JLabel labelX5;
 	private JLabel labelX6;
 	private JLabel labelX7;
 	private JLabel labelX8;
 	private JLabel labelSolutionX1;
 	private JLabel labelSolutionX2;
 	private JLabel labelSolutionX3;
 	private JLabel labelSolutionX4;
 	private JLabel labelSolutionX5;
 	private JLabel labelSolutionX6;
 	private JLabel labelSolutionX7;
 	private JLabel labelSolutionX8;
 	private JLabel currentSolution;
 	private JLabel currentSolutionValue;
 	private JLabel sizeOfParentSetLabel;
 	private JButton button;
 	private JTextField inputField;
 	private int sizeOfParentSet;
 	
 	/**
 	 * 
 	 */
 	public OutputPane() {
 		frame = new JFrame("Search Output");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setComponents();
 		showPane();
 	}
 
 	private void setComponents() {
 		sizeOfParentSetLabel = new JLabel("Specify the parent set's size: ");
 		labelX1 = new JLabel("x1 = ");
 		labelX2 = new JLabel("x2 = ");
 		labelX3 = new JLabel("x3 = ");
 		labelX4 = new JLabel("x4 = ");
 		labelX5 = new JLabel("x5 = ");
 		labelX6 = new JLabel("x6 = ");
 		labelX7 = new JLabel("x7 = ");
 		labelX8 = new JLabel("x8 = ");
 		currentSolution = new JLabel("Local minimum = ");	
 		
 		labelSolutionX1 = new JLabel();
 		labelSolutionX2 = new JLabel();
 		labelSolutionX3 = new JLabel();
 		labelSolutionX4 = new JLabel();
 		labelSolutionX5 = new JLabel();
 		labelSolutionX6 = new JLabel();
 		labelSolutionX7 = new JLabel();
 		labelSolutionX8 = new JLabel();
 		currentSolutionValue = new JLabel();
 		
 		inputField = new JTextField();
 		
 		button = new JButton("Start search");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				resetLabels();
				try {
					sizeOfParentSet = Integer.parseInt(inputField.getText());
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					showMissingInputValueDialog();
				}
 				HillClimberImplementation.startAlgorithm();
 	         }
 	    });
 			    
 		frame.setLayout(new GridLayout(11, 2));
 		
 		frame.getContentPane().add(sizeOfParentSetLabel);
 		frame.getContentPane().add(inputField);
 		frame.getContentPane().add(labelX1);
 		frame.getContentPane().add(labelSolutionX1);
 		frame.getContentPane().add(labelX2);
 		frame.getContentPane().add(labelSolutionX2);
 		frame.getContentPane().add(labelX3);
 		frame.getContentPane().add(labelSolutionX3);
 		frame.getContentPane().add(labelX4);
 		frame.getContentPane().add(labelSolutionX4);
 		frame.getContentPane().add(labelX5);
 		frame.getContentPane().add(labelSolutionX5);
 		frame.getContentPane().add(labelX6);
 		frame.getContentPane().add(labelSolutionX6);
 		frame.getContentPane().add(labelX7);
 		frame.getContentPane().add(labelSolutionX7);
 		frame.getContentPane().add(labelX8);
 		frame.getContentPane().add(labelSolutionX8);
 		frame.getContentPane().add(currentSolution);
 		frame.getContentPane().add(currentSolutionValue);
 		frame.getContentPane().add(button);
 	}
 	
 	private void showPane() {
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	public int getSizeOfParentSet() {
 		return sizeOfParentSet;
 	}
 	
 	public void setLabels(int label1, int label2, int label3, int label4, int label5, int label6, int label7, int label8) {
 		labelSolutionX1.setText(""+label1);
 		labelSolutionX2.setText(""+label2);
 		labelSolutionX3.setText(""+label3);
 		labelSolutionX4.setText(""+label4);
 		labelSolutionX5.setText(""+label5);
 		labelSolutionX6.setText(""+label6);
 		labelSolutionX7.setText(""+label7);
 		labelSolutionX8.setText(""+label8);
 	}
 	
 	public void setCurrentSolutionLabel(int currentSolutionLabel) {
 		currentSolutionValue.setText(""+currentSolutionLabel);
 	}
 	
 	public void resetLabels() {
 		labelSolutionX1.setText("");
 		labelSolutionX2.setText("");
 		labelSolutionX3.setText("");
 		labelSolutionX4.setText("");
 		labelSolutionX5.setText("");
 		labelSolutionX6.setText("");
 		labelSolutionX7.setText("");
 		labelSolutionX8.setText("");
 		currentSolutionValue.setText("");
 	}
 	
	public void showMissingInputValueDialog() {
		JOptionPane.showMessageDialog(frame,
			    "Please specify a feasible value.",
			    "Message",
			    JOptionPane.WARNING_MESSAGE);
	}
	
 	public void showLocalMinimumDialog() {
 		JOptionPane.showMessageDialog(frame,
 			    "Cannot reach a better local minimum.",
 			    "Message",
 			    JOptionPane.WARNING_MESSAGE);
 	}
 	
 	public void showGlobalMinimumDialog() {
 		JOptionPane.showMessageDialog(frame, "Reached global minimum.");
 	}
 }
