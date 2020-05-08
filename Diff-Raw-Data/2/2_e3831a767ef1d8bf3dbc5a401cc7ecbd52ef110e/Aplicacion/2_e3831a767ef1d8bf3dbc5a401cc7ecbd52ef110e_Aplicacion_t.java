package Imagen;

 import javax.swing.*;
 import java.awt.*;
 import java.net.URL;
 
 public class Aplicacion {
 
     public Aplicacion() {
     }
    
     public static void main(String[] args){
         JFrame frame=new JFrame();
         frame.setVisible(true);
         frame.add(new MiFrame());
               
     }
    
    
 }
 
 class MiFrame extends JPanel{
    
     Graphics p;
     Image img;
     JPanel panel=new JPanel();
     JTextField texto =new JTextField();
     URL url=this.getClass().getResource("/Tree.jpg");
     ImageIcon icono=new ImageIcon(url);
     
    
     public MiFrame(){
        texto.setColumns(20);
        this.add(texto);
        
     }
    
     public void paint(Graphics g){
    	 
     	g.drawImage(icono.getImage(), 0, 0, getWidth(), getHeight(),  
     			this);  
     			   
     			setOpaque(false);  
     			super.paint(g);  
     }
 }
