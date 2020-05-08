 package fr.ujm.tse.info4.pgammon.test.gui;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 
 import fr.ujm.tse.info4.pgammon.gui.TriangleCaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 
 public class TestDesignPlateau {
 	
 	
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("Test Design");
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(800,600);
 		
 		Container panel = frame.getContentPane();
 		panel.setLayout(new FlowLayout());
 
 		panel.setBackground(new Color(0x00000));
 		
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.NOIR,3), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,4), CouleurCase.NOIR);
 			panel.add(t);
 		}
 
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,5), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,6), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,7), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,8), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,9), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.BLANC,10), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.NOIR,11), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12,CouleurCase.NOIR,12), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
			TriangleCaseButton t = new TriangleCaseButton(new Case(12), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		frame.setVisible(true);
 	}
 }
