 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SubcalcData;
 
 import SubcalcData.MainMenu;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author g96930
  */
 public class SubCalc extends CalcData {
 
     public void userInput() {
 
         ipOct1 = Integer.parseInt(MainMenu.ipTxt1.getText());
         ipOct2 = Integer.parseInt(MainMenu.ipTxt2.getText());
         ipOct3 = Integer.parseInt(MainMenu.ipTxt3.getText());
         ipOct4 = Integer.parseInt(MainMenu.ipTxt4.getText());
 
         mOct1 = Integer.parseInt(MainMenu.subTxt1.getText());
         mOct2 = Integer.parseInt(MainMenu.subTxt2.getText());
         mOct3 = Integer.parseInt(MainMenu.subTxt3.getText());
         mOct4 = Integer.parseInt(MainMenu.subTxt4.getText());
 
 
         if (ipOct1 <= 255 && ipOct1 >= 0
                 && ipOct2 >= 0 && ipOct2 <= 255
                 && ipOct3 >= 0 && ipOct3 <= 255
                 && ipOct4 >= 0 && ipOct4 <= 255) {
 
             if ((ipOct1 <= 126 && ipOct1 >= 0) && (mOct1 == 255)) {
                 aNet();
             } else if ((ipOct1 <= 191 && ipOct1 >= 128) && (mOct1 == 255 && mOct2 == 255)) {
                 bNet();
             } else if (mOct1 == 255 && mOct2 == 255 && mOct3 == 255) {
                 cNet();
             }
         } else {
             JOptionPane.showMessageDialog(null, "Please enter a proper IP Address.");
         }
     }
 
     public void aNet() {
 
        hostBits = (int) Math.pow(2, (maskCalculate(mOct2)
                + maskCalculate(mOct3) + maskCalculate(mOct4))) - 2;
 
         System.out.println("a net");
 
         MainMenu.hostsTxt.setText(Integer.toString(hostBits));
         MainMenu.ipClasstxt.setText("A");
 
         broadcastCalc(mOct2);
 
         for (int i = 0; i < ipRange.size(); i++) {
 
 
             System.out.println("ipRange = " + ipRange.get(i));
             System.out.println("ipOct2 = " + ipOct2);
 
             if (ipRange.get(i) < ipOct2 && i > 1) {
                 System.out.println("IN THE LOOP!");
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipRange.get(i - 1) + ".0.1 - "
                         + ipOct1 + "." + ipRange.get(i) + ".255.254");
 
             } else if (ipRange.get(i) < ipOct2) {
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.1 - "
                         + ipOct1 + "." + ipRange.get(i) + ".255.254");
 
             } else if (ipRange.size() == 2) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.1 - "
                         + ipOct1 + "." + ipRange.get(i) + ".255.254");
 
             }
 
         }
 
     }
 
     public void bNet() {
 
        hostBits = (int) Math.pow(2, (maskCalculate(mOct3) + maskCalculate(mOct4))) - 2;
 
         System.out.println("b net");
 
         broadcastCalc(mOct3);
         for (int i = 0; i < ipRange.size(); i++) {
 
             if (ipRange.get(i) < ipOct3 && i > 1) {
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipRange.get(i) + ".0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + (ipRange.get(i - 1) + 1) + ".1 - "
                         + ipOct1 + "." + ipOct2 + "." + ipRange.get(i) + ".254");
 
             } else if (ipRange.get(i) < ipOct3) {
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipRange.get(i) + ".0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + (ipRange.get(i) + 1) + ".1 - "
                         + ipOct1 + "." + ipOct2 + "." + ipRange.get(i) + ".254");
             } else if (ipRange.size() == 2) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.0");
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipRange.get(i) + ".0.1 - "
                         + ipOct1 + "." + ipOct2 + "." + ipRange.get(i) + ".254");
 
 
             }
         }
         MainMenu.hostsTxt.setText(Integer.toString(hostBits));
         MainMenu.ipClasstxt.setText(
                 "B");
     }
 
     public void cNet() {
 
        hostBits = (int) Math.pow(2, maskCalculate(mOct4)) - 2;
 
         System.out.println("c net");
 
         broadcastCalc(mOct4);
         for (int i = 0; i < ipRange.size(); i++) {
 
             if (ipRange.get(i) < ipOct4 && i > 1 && i < ipRange.size()) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + ipRange.get(i));
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "."
                         + (ipRange.get(i) + 1) + " - "
                         + ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + (ipRange.get(i + 1) - 2));
                 System.out.println("c1");
 
             } else if (ipRange.size() == 2) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + ipRange.get(i));
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "."
                         + (ipRange.get(i) + 1) + " - "
                         + ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + (ipRange.get(i + 1) - 2));
                 System.out.println("c2");
 
             } else if (ipRange.get(i) < ipOct4) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + ipRange.get(i));
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "."
                         + (ipRange.get(i) + 1) + " - "
                         + ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + (ipRange.get(i + 1) - 2));
                 System.out.println("c2");
             } else if (ipRange.get(i) == ipOct4) {
 
                 MainMenu.netIdTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + ipRange.get(i));
                 MainMenu.ipRangeTxt.setText(ipOct1 + "." + ipOct2 + "." + ipOct3 + "."
                         + (ipRange.get(i) + 1) + " - "
                         + ipOct1 + "." + ipOct2 + "." + ipOct3 + "." + (ipRange.get(i + 1) - 2));
                 System.out.println("c3");
             }
         }
         MainMenu.hostsTxt.setText(Integer.toString(hostBits));
         MainMenu.ipClasstxt.setText("C");
     }
 }
