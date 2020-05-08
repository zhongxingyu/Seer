 package com.teamdev.students.calculator.swingui;
 
 import com.teamdev.students.calculator.implementation.Calculator;
 import com.teamdev.students.calculator.implementation.EvaluationException;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.math.BigDecimal;
 
 public class CalculatorFrame extends JFrame {
     private JTextField textExpression;
     private JTextField textResult;
     private Calculator calculator;
 
     public CalculatorFrame(Calculator calculator) {
         this.calculator = calculator;
         initializeComponents();
     }
 
     private void initializeComponents() {
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Calculator");
         setSize(new Dimension(226, 170));
         setResizable(false);
 
         JPanel panel = new JPanel(null);
         setContentPane(panel);
 
         JLabel labelExpression = new JLabel("Math Expression");
         labelExpression.setLocation(10, 10);
         labelExpression.setSize(200, 20);
 
         textExpression = new JTextField();
         textExpression.setLocation(10, 30);
         textExpression.setSize(new Dimension(200, 20));
 
         JButton buttonResult = new JButton("Calculate");
         buttonResult.setLocation(10, 60);
         buttonResult.setSize(new Dimension(200, 20));
         buttonResult.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 buttonResultClicked(e);
             }
         });
 
         JLabel labelResult = new JLabel("Result");
         labelResult.setLocation(10, 90);
         labelResult.setSize(200, 20);
 
         textResult = new JTextField();
         textResult.setLocation(10, 110);
         textResult.setSize(new Dimension(200, 20));
         textResult.setEditable(false);
         textResult.setBackground(Color.WHITE);
 
         panel.add(labelExpression);
         panel.add(textExpression);
         panel.add(buttonResult);
         panel.add(labelResult);
         panel.add(textResult);
 
         getRootPane().setDefaultButton(buttonResult);
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Dimension screenSize = toolkit.getScreenSize();
         setBounds(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2, getWidth(), getHeight());
         setVisible(true);
     }
 
     private void buttonResultClicked(ActionEvent event) {
        textResult.setText("");
         if (calculator != null) {
             try {
                 textExpression.setText(textExpression.getText().replaceAll(" ",""));
                 BigDecimal result = calculator.evaluate(textExpression.getText());
                 textResult.setText(result != null ? result.toString(): "NaN");
             } catch (EvaluationException ex) {
                 String info = ex.getMessage()
                         + "\nExpression: " + ex.getExpression()
                         + "\nPosition: " + ex.getErrorPosition();
                 JOptionPane.showMessageDialog(null, info);
                 textExpression.grabFocus();
                 textExpression.setCaretPosition(ex.getErrorPosition());
             }
         }
     }
 }
