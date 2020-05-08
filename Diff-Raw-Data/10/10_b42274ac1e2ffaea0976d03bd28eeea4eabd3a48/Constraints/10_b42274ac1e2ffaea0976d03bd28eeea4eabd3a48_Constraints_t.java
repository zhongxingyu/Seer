 package gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 
 /**
  *
  * @author Andreas J
  */
 public class Constraints extends GridBagConstraints{
 public Constraints(){
     super();
 }
 public void set(int grx,int gry,int grw,int grh,int ipx,int ipy, Insets ins,int anc){
   gridx=grx;
   gridy=gry;
   gridwidth=grw;
   gridheight=grh;
   ipadx=ipx;
   ipady=ipy;
   insets=ins;
   anchor= anc;
   
  }
  
 public void set(int grx,int gry,int grw,int grh,int ipx,int ipy){
   gridx=grx;
   gridy=gry;
  insets= new Insets(0,0,0,0);
   gridwidth=grw;
   gridheight=grh;
   ipadx=ipx;
   
  }
 public void set(int grx,int gry,int grw,int grh,Insets ins,int anc){
   gridx=grx;
   gridy=gry;
   gridwidth=grw;
   gridheight=grh;
   insets=ins;
    ipadx=0;
    ipady=0;
   anchor= anc;
   
  }
 }
