 package net.mtu.eggplant.app;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.mtu.eggplant.util.gui.GraphicsUtils;
 
 /**
    Nifty little class that converts a date as a long to a human readable string
 **/
 public class TimeConvert extends JPanel {
 
   /**
      args is ignored
   **/
  public static void main(final String[] args) {
     TimeConvert tc = new TimeConvert();
     GraphicsUtils.basicGUIMain(tc, false, "Time Convert");
   }
 
   public TimeConvert() {
     setLayout(new BorderLayout());
 
     final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS zzz");
     final JTextField time = new JTextField("MM/dd/yyyy HH:mm:ss.SSS zzz");
     
     final JTextField number = new JTextField();
     number.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent ae) {
           final Date d = new Date(Long.parseLong(number.getText()));
           time.setText(format.format(d));
         }
     });
 
     time.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent ae) {
           try {
             number.setText(String.valueOf(format.parse(time.getText()).getTime()));
           } catch(final ParseException pe) {
             number.setText("-1");
           }
         }
     });
     
     add(number, BorderLayout.NORTH);
     add(time, BorderLayout.CENTER);
   }
 
 }
