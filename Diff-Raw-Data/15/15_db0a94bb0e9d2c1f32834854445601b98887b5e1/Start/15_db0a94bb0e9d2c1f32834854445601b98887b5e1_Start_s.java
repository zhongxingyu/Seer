 import aes.AES;
 import aes.SquareAttack;
 import gui.MainFrame;
 
 import javax.swing.*;
 import java.util.Arrays;
 
 public class Start {
 
     public static void main(String[] args) {
 
        //This code will be deleted when the GUI will be made
        /*SquareAttack.generateKey();

        for (int i = 0; i < 16; i++) {
            byte[][] texts = SquareAttack.generate256Texts(i);

            byte[][] enc = SquareAttack.encryptTexts(texts);

            byte rec = SquareAttack.attackKeyByte(enc, i);

            System.out.println(rec);
        }

        System.out.println(Arrays.deepToString(AES.getRoundKey(4))); */

         JFrame mainFrame = new MainFrame();
 
     }
 
 }
