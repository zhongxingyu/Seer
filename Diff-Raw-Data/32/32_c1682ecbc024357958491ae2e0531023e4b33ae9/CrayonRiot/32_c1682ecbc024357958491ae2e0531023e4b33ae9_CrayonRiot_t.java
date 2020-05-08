 package Screen;
 
 import java.util.*;
 import java.awt.*;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class CrayonRiot extends JFrame {
 
 	public static final int FRAMETICK = 60;
 	 
 	public CrayonRiot(){
 		
 		setTitle("Crayon Riot");
         setSize(1024, 720);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		ScreenPanel sc = new ScreenPanel();
 		add(sc);
 		setVisible(true);
 		sc.run();
 	}
 	
 	public void gameLoop(){
 		
 	}
 
 	public static void main(String[] args) {
 		new CrayonRiot();
 	}
 
 }
