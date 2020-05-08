 package at.tuwien.sbc.feeder.gui.components;
 
 import java.awt.Color;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.JTextField;
 
 public class DateTextField extends JTextField implements FocusListener {
     
     private SimpleDateFormat df;
     
     private String pattern = "HH.dd";
     
     public DateTextField() {
         super();
         this.df = new SimpleDateFormat(pattern);
         this.addFocusListener(this);
         this.setForeground(Color.BLACK);
         this.setText(this.df.toPattern());
         this.setToolTipText("Pattern: " + this.df.toPattern());
     }
     
     public DateTextField(int columns) {
         super(columns);
         this.df = new SimpleDateFormat(pattern);
         this.addFocusListener(this);
         this.setForeground(Color.BLACK);
         this.setText(this.df.toPattern());
         this.setToolTipText("Pattern: " + this.df.toPattern());
     }
     
     public DateTextField(String text) {
         super(text);
         this.df = new SimpleDateFormat(pattern);
         this.addFocusListener(this);
         this.setForeground(Color.BLACK);
         this.setText(this.df.toPattern());
         this.setToolTipText("Pattern: " + this.df.toPattern());
     }
     
     public DateTextField(String text, int columns) {
         super(text, columns);
         this.df = new SimpleDateFormat(pattern);
         this.addFocusListener(this);
         this.setForeground(Color.BLACK);
         this.setText(this.df.toPattern());
         this.setToolTipText("Pattern: " + this.df.toPattern());
     }
 
     public void focusGained(FocusEvent evt) {
         this.selectAll();
     }
 
     public void focusLost(FocusEvent evt) {
         this.validateField();
     }
 
     private void validateField() {
         String input = this.getText();
         
         try {
             this.df.parse(input);
            this.setBackground(Color.BLACK);
         } catch (Exception e) {
             this.setForeground(Color.RED);
         }
     }
 
     public void setDateFormat(SimpleDateFormat df) {
         this.df = df;
     }
 
     public SimpleDateFormat getDateFormat() {
         return df;
     }
     
     public Date getDate() throws ParseException {
         return this.df.parse(this.getText());
     }
     
     
 }
