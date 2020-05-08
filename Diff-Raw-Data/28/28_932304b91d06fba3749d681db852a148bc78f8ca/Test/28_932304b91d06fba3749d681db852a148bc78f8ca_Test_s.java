 package com.otika.test;
 
 import bim.Computer;
 import bim.Utilities;
 import java.awt.Font;
 import java.util.Locale;
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Hanynowsky <hanynowsky@gmail.com>
  */
 public class Test {
 
     public static void main(String[] args) {
         Utilities utility = new Utilities();
         Computer c = new Computer();
         // System.out.println(UIManager.getLookAndFeelDefaults().entrySet());
         // Utilities util = new Utilities();
         // System.out.println("Testing executed");
         //util.readIBMITABLE("Male", 1.49);
         // Test test = new Test();
         //test.calculateRange();
         // util.readIBMITABLE("Male", 1.50);
         // System.out.println("Here is Range: " + util.getIbmirange());
         //  c.computeBMR(58, 178, 31, "male");
         // c.computeTDEE("sedentary");
         // c.computeIdealWeight(178, "Hamwi", "female","mesomorph");
         //   Utilities utility = new Utilities();
         // String slaf = UIManager.getLookAndFeel().getName();
         // System.out.println(utility.prefs.get(utility.LAF_PREF, UIManager.getSystemLookAndFeelClassName()));
         // System.out.println(UIManager.getSystemLookAndFeelClassName());
         // String[] test = {"I am 0","I am 1","I am 2"};
         // System.out.println(c.computeBodyType("male", 153, 15, 6.5, true)); 
         // System.out.println(c.computeBodyFat(178, 75, 37, 85, "male"));
         // utils.playAudio("Obese",true);
         double d = 12.3265;
         System.out.println(new Locale(utility.prefs.get(utility.LOCALE_PREF, Locale.getDefault().toString())));
         System.out.println(Locale.getDefault().getLanguage() + "  <->  " + Locale.getDefault().getCountry());
         System.out.println(Locale.getDefault().toString());
         System.out.println(utility.prefs.get(utility.LOCALE_PREF, null).substring(0, 2) + "_" + utility.prefs.get(utility.LOCALE_PREF, null).substring(3, 5).toUpperCase());
 
         System.err.println(UIManager.getDefaults().get("Button.disabledText"));
         System.err.println(UIManager.getDefaults().get("Button.disabledForeground"));
         System.err.println(UIManager.getDefaults().get("Button.disabledShadow"));
         System.err.println(UIManager.getDefaults().get("Button.textShiftOffset"));
           System.err.println(UIManager.getDefaults().get("Button.shadow"));
         System.out.println(UIManager.getDefaults().keys().toString());
 
         //   UIManager.getDefaults().put("OptionPane.font", new Font("Droid Sans", Font.PLAIN, 11));
         // System.out.println(UIManager.getDefaults().getFont("OptionPane.font"));
 
     } // END OF MAIN
 
     public void calculateRange() {
     }
 } // END OF CLASS
