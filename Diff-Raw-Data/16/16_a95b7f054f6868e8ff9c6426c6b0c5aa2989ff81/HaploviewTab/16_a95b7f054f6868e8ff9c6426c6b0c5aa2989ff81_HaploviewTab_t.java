 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class HaploviewTab extends JPanel{
 
     //todo: this should be a little fancier and be able to access its principal components
     //todo: and maybe call their export functions, perhaps this should be an interface?
 
     public HaploviewTab(Component c){
         this.add(c);
         this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
     }


    public Component getComponent(){
        return getComponent(0);
    }

}
