 package fr.ujm.tse.info4.pgammon.test.gui;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 
 import fr.ujm.tse.info4.pgammon.gui.DeButton;
 import fr.ujm.tse.info4.pgammon.gui.TriangleCaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 import fr.ujm.tse.info4.pgammon.models.DeSixFaces;
 
 public class TestDesignPlateau {
 	
 	
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("Test Design");
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(800,600);
 		
 		Container panel = frame.getContentPane();
 		panel.setLayout(new FlowLayout());
 
 		panel.setBackground(new Color(0x00000));
 		
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.NOIR,3,3), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,4,4), CouleurCase.NOIR);
 			panel.add(t);
 		}
 
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,5,5), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,6,6), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,7,7), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,8,8), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,9,9), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.BLANC,10,10), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.NOIR,11,1), CouleurCase.BLANC);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.NOIR,12,12), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
 			TriangleCaseButton t = new TriangleCaseButton(new Case(CouleurCase.NOIR,0,13), CouleurCase.NOIR);
 			panel.add(t);
 		}
 		{
 			DeSixFaces dsf = new DeSixFaces(CouleurCase.BLANC,1);
 			dsf.utiliser();
 			DeButton d = new DeButton(dsf);
			d.utiliser();
 			panel.add(d);
 		}
 		frame.setVisible(true);
 	}
 }
