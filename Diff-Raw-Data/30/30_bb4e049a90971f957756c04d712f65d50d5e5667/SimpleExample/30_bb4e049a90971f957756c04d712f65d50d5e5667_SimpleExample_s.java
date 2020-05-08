 /**
  *
  * This file is part of the SJWidget library.
  * (c) 2005-2012 Bjorn Roche
  * Development of this library has been supported by Indaba Media (http://www.indabamusic.com)
  * and XO Audio (http://www.xoaudio.com)
  *
  * for copyright and sharing permissions, please see the COPYING.txt file which you should
  * have recieved with this file.
  *
  */
 
 import com.xowave.sjwidget.*;
 import javax.swing.*;
 
 import java.io.IOException;
 
 
 public class SimpleExample
 {
    public static void main( String[] args ) {
       javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
       });
    }
    private static void createAndShowGUI() {
       try {
          WidgetUtil.loadDescriptions("resources/WidgetDescriptions.xml");
       } catch( IOException ioe ) {
          throw new RuntimeException( ioe );
       }
 
       JFrame frame = new JFrame("SJWidget Example");
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
       // we're going to create a column of labels, buttons and stuff.
       SJPanel p = SJPanel.createVerticalIPanel(null);
       p.setBackground( java.awt.Color.BLACK ); //we can still use swing to set colors and so on
 
       p.add( new SJLabel( "example label" ) );
       p.add( new SJLabel( "example label with gradient" ) );
       p.add( new SJLabel( "label with icon" ) );
       SJButton b = new SJButton( "button with multiple icons" );
       b.setEnabled( true );
       b.setText( "Enabled" );
       p.add( b );
       b = new SJButton( "button with multiple icons" );
       b.setEnabled( false );
       b.setText( "Disabled" );
       p.add( b );
       p.add( new SJLabel( "label with custom font" ) );
 
       frame.setContentPane( p );
       frame.pack();
       frame.setVisible(true);
    }
 }
