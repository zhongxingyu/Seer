 package dab.gui.auxpanels;
 
 import dab.engine.simulator.UserCommands;
 import dab.engine.newsim.interfaces.ReactorView;
 import dab.engine.utilities.Percentage;
import java.awt.Color;
 import java.awt.Dimension;
import java.awt.Font;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.plaf.basic.BasicSliderUI;
 
 /**
  * A slider for the ButtonPanel that controls the height of ControlRods
  * in the system.
  * @author Team Haddock
  * 
  *
  */
 public class ControlRodSlider extends JSlider{
 
     ReactorView reactor;
     /**
      * 
      * @param
      * @param
      */
     
     public ControlRodSlider(ReactorView reac) {
         super(JSlider.VERTICAL, 0, 100, 0);
         this.reactor = reac;
         setOpaque(false);
         setMajorTickSpacing(25);
         setPaintTicks(true);
         setPaintLabels(true);
        setFont(new Font("Bookman Old Style", Font.BOLD, 14));
        setForeground(Color.WHITE);
        
         setUI(new MySliderUI(this));
         addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (!getValueIsAdjusting()){
                     reactor.moveControlRods(new Percentage(getValue()));
                 }
             }
         });
         
     }
     
     public void update() {
         setValue((int)(reactor.controlRodPosition().ratio() * 100));
     }
     
     /**
      *
      * A private class used to overwrite the natural look of a basic slider
      */
     private class MySliderUI extends BasicSliderUI {
         Image knobImage;
         MySliderUI( JSlider aSlider ) {
             super( aSlider );
             try {
                 this.knobImage = ImageIO.read( new File("resources/controlPanel/orangeSlider.png") );
             } catch ( IOException e ) {
                 e.printStackTrace();
             }
         }
         @Override
         public void scrollByUnit(int direction)
         {
 
         }
         @Override
         public void paintThumb(Graphics g)  {
             // overwrite the default slider icon
             g.drawImage( this.knobImage, thumbRect.x, thumbRect.y, 30, 10, null );
             repaint();
         }
     }
 }
