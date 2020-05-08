 package com.heralli.ekonsehan;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JLabel;
 
 public class Main {
 	public static void main(String args[])
 	{
 		//SplashScreen test = new SplashScreen();
 		
 		MultipleChoiceQuestion q1 = new MultipleChoiceQuestion();
 		q1.setQuestion("Who is the father of Economics?");
 		q1.addChoice("a. Adam Smith"); 
 		q1.addChoice("b. Adam Sandler");
 		q1.addChoice("c. Alan Turing");
 		q1.addChoice("d. Alan K");
 		
 		MultipleChoiceQuestionView q1v = new MultipleChoiceQuestionView(q1);
 		MainWindow mainWin = new MainWindow();
		mainWin.getContentPane().setLayout(new BorderLayout());
		mainWin.getContentPane().add(new JLabel("Hello"),BorderLayout.WEST);
		mainWin.getContentPane().add(q1v,BorderLayout.CENTER);
 		mainWin.setVisible(true);
 		
 		
 	}
 }
