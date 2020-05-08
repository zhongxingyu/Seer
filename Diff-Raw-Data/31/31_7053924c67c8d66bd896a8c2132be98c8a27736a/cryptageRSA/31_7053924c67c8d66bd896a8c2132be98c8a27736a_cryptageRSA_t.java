 import processing.core.*; 
 import processing.xml.*; 
 
 import java.awt.*; 
 import java.applet.*; 
 import java.awt.event.*; 
 import java.math.BigInteger; 
 import java.util.Random; 
 
 import java.applet.*; 
 import java.awt.Dimension; 
 import java.awt.Frame; 
 import java.awt.event.MouseEvent; 
 import java.awt.event.KeyEvent; 
 import java.awt.event.FocusEvent; 
 import java.awt.Image; 
 import java.io.*; 
 import java.net.*; 
 import java.text.*; 
 import java.util.*; 
 import java.util.zip.*; 
 import java.util.regex.*; 
 
 public class cryptageRSA extends PApplet {
 
 /*///////////////////////////////////////////////////////////////////////////////////
  *
  * 08.2010 C\u00e9cile P-L for Fuscia, ccl.picard@gmail.com
  * CRYPTOGRAPHIE
  * Interface p\u00e9dagogique sur le codage/d\u00e9codage de messages avec cl\u00e9s RSA
  * Interface r\u00e9alis\u00e9e avec ControlP5 (bas\u00e9e sur l'example ControlP5TextfieldAdvanced)
  *
  */                                                                                                                                                                                                                                                                                                                                            // /////////////////////////////////////////////////////////////////////////////////
 
 
 
 
 
 
 
 // Param\u00e8tres pour la m\u00e9thode RSA
 int pq_size;
 static int prime_certainty = 20;
 BigInteger p, q, n, e, d, A;
 BigInteger EncMessBits;
 
 // Param\u00e8tres de l'interface
 PFont pfont;
 int space;
 int myOr = color(255, 100, 0);
 int myGreen = color(0, 100, 30);
 int myGreenA = color(0, 200, 100);
 int myRed = color(255, 0, 0);
 int myBlue = color(120);  // (120,140,150);
 int myBlueA = color(100);  // (120,165,175);
 
 rectButton[] T0 = new rectButton[4];
 TextButton[] T1 = new TextButton[4];
 rectButton[] T2 = new rectButton[3];
 rectButton[] T3 = new rectButton[5];
 TextButton[] T4 = new TextButton[4];
 TextButton[] T5 = new TextButton[2];   // infos
 TextButton T6;  // = new TextButton; //masquer
 boolean locked = false;
 boolean info = false;
 
 String[] ListN1 = { "1. G\u00e9n\u00e8re   P  et  Q", "2. Calcule N", "3. G\u00e9n\u00e8re E", "4. Calcule D" };
 String[] ListN2 = { "Cl\u00e9 priv\u00e9e = ", "Cl\u00e9 publique = ", "Cl\u00e9 publique = " };
 String[] ListN3 = { "Traduction  du  message  en  chiffres", "Encryptage du message", "E N V O I   du message encrypt\u00e9", "D\u00e9cryptage du message" };
 String[] ListN4 = { "Tu es Alice et tu es charg\u00e9 de g\u00e9n\u00e9rer deux 'cl\u00e9s', \n" +
                     "une 'cl\u00e9 publique' et 'une cl\u00e9 priv\u00e9e'.\n" +
                     "Tu ne transmettras que la cl\u00e9 publique \u00e0 Bob. \n" +
                     "Bob encryptera son message secret \u00e0 l'aide de la cl\u00e9 publique,\n" +
                     "et seul toi pourras le d\u00e9crypter au moyen de la cl\u00e9 priv\u00e9e!",
                     "Tu es Bob. Tu vas recevoir une 'cl\u00e9', dite 'publique', \n" +
                     "qui te permettra d'encrypter un message secret.\n" +
                     "Transmet ton message encrypt\u00e9 \u00e0 Alice qui essayera de le d\u00e9crypter! " };
 
 /*"Tu es Alice. Nous te proposons d'exp\u00e9rimenter le CODAGE et le DECODAGE de messages.\n" +
  *  "Tout d'abord, tu dois g\u00e9n\u00e9rer une 'cl\u00e9 publique' et 'une cl\u00e9 priv\u00e9e'.\n" +
  *  "Tu divulgueras ensuite la cl\u00e9 publique \u00e0 Bob, et tu garderas la cl\u00e9 priv\u00e9e pr\u00e9cieusement. \n" +
  *  "Bob encryptera son message secret \u00e0 l'aide de la cl\u00e9 publique.\n" +
  *  "Seul toi pourras d\u00e9crypter le message au moyen de la cl\u00e9 priv\u00e9e!",
  *  "Tu es Bob. Nous te proposons d'exp\u00e9rimenter le CODAGE et le DECODAGE de messages.\n" +
  *  "Tu vas recevoir une 'cl\u00e9', dite 'publique', qui te permettra d'encrypter un message secret.\n" +
  *  "Apres encryptage du message, transmet-le \u00e0 Alice qui essayera de le d\u00e9crypter! " };*/
 String lastInput = new String();
 
 // Ce qui est lanc\u00e9 une fois, au d\u00e9part
 public void setup() {
   frame = new Frame();
 
   size(900, 650);
   space = 50;
   frameRate(30);
   pfont = createFont("Arial Bold", 12, true); // use true/false for smooth/no-smooth
 
   p = new BigInteger("0");
   q = new BigInteger("0");
   n = new BigInteger("0");
   e = new BigInteger("0");
   d = new BigInteger("0");
   A = new BigInteger("0");
 
   this.frame.setTitle("Alice");   // interface principale: celle d'Alice
   for(int i = 0; i < T1.length; i++) {
     T0[i] = new rectButton(space, height / 2 - height / 4 + i *40, 160, 25, myGreen);
     T1[i] = new TextButton(width / 2 - (space + 160), height / 2 - height / 4 + i *40, 160, 25, color(255), myGreen, myGreenA, ListN1[i]);
   }
   for(int i = 0; i < T2.length; i++) {
     if(i == 2)
       T2[i] = new rectButton(width / 2 + space, height / 2 - height / 4, 200, 25, myRed);
     else
       T2[i] = new rectButton(((i + 1) % 2) *space + PApplet.parseInt ((i + 1) / 2) *(width / 2 - (space + 160)) - i *40, height / 2 + height / 20, 160 + i *40, 25, myRed);
     T2[i].setText(ListN2[i]);
   }
   for(int i = 0; i < T3.length; i++) {
     if(i < 3) {
       if(i == 0)
         T3[i] = new rectButton(width / 2 + space, height / 2 - height / 6 + i *90, width / 2 - (space * 2), 30, myBlue);
       else
         T3[i] = new rectButton(width / 2 + space, height / 2 - height / 6 + i *90 + PApplet.parseInt (i / 2) *30, width / 2 - (space * 2), 60, myBlue);
     } else
       T3[i] = new rectButton(space, height / 2 - height / 4 + PApplet.parseInt (i / 2) *30 + i *90, width / 2 - (space * 2), 30 + (i % 2) *30, myBlue);
   }
   for(int i = 0; i < T4.length; i++) {
     if(i < 2)
       T4[i] = new TextButton(width - (space + 250), height / 2 - height / 6 + 55 + i *120, 250, 25, color(255), myBlue, myBlueA, ListN3[i]);
     // T4[i] = new rectButton(width/2+100, height/2-height/8 +i*90, width/2-200, 30, myBlue);
     else if(i == 2)
       T4[i] = new TextButton(width - (space + 250), height / 2 - height / 6 + 115 + i *90, 250, 25, color(255), myRed, myOr, ListN3[i]);
     else
       T4[i] = new TextButton(width / 2 - (space + 250), height / 2 - height / 6 + 60 + i *90, 250, 25, color(255), myBlue, myBlueA, ListN3[i]);
     // T4[i] = new rectButton(100, height/2-height/8 +i*90, width/2-200, 30, myBlue);
   }
   for(int i = 0; i < T5.length; i++)
     T5[i] = new TextButton(10 + i *width / 2, 12, 70, 22, color(255), color(i * 153), myOr, "- info -");
   T6 = new TextButton(width / 2 - space - 170, height / 2 + height / 10, 170, 25, color(255), myRed, myOr, "Masquer toute information");
 
   lastInput = "Ecrit ton message, il s'inscrira ici ";
   T3[0].setText(lastInput);
 }
 // Ce qui est effectu\u00e9 tout au long de l'animation
 public void draw() {
   background(0);
   fill(153);
   rect(width / 2 + 1, 0, width / 2, height);
 
   update(mouseX, mouseY);
   for(int i = 0; i < T1.length; i++) {
     T0[i].display();
     T1[i].display();
   }
   for(int i = 0; i < T2.length; i++)
     T2[i].display();
   for(int i = 0; i < T3.length; i++)
     T3[i].display();
   for(int i = 0; i < T4.length; i++)
     T4[i].display();
   for(int i = 0; i < T5.length; i++)
     T5[i].display();
   T6.display();
 }
 public void keyPressed() {
   if(!(key == CODED)) {
     if(lastInput.length() > 62)
       lastInput = lastInput.substring(0, 62);
     if(key == BACKSPACE) {
       if(lastInput.length() > 1) {
         lastInput = lastInput.substring(0, lastInput.length() - 2);
         T3[0].setText(lastInput);
         T3[0].display();
       }
     } else {
       // println((T3[0].value).length());
       lastInput = lastInput + key;
       T3[0].setText(lastInput);
       T3[0].display();
     }
   }
   if(key == '1') {
     BigInteger[] myKeys = new BigInteger[3];
     myKeys = createKeys();
     println("Private Key is: " + myKeys[0] + " // Public Key is: " + myKeys[1] + " " + myKeys[2]);
   }
   if(key == '2') {
     BigInteger[] myKeys = new BigInteger[3];
     myKeys = createKeys();
     BigInteger EncMess;
     EncMess = encrypt("oh oh je suis le p\u00e8re Noel!!!", myKeys[1], myKeys[2]);
     println("Public Key is: " + myKeys[1] + " " + myKeys[2] + " // encrypted message: " + EncMess);
   }
   if(key == '3') {
     BigInteger[] myKeys = new BigInteger[3];
     myKeys = createKeys();
     BigInteger EncMess;
     String decryptedMessage;
     EncMess = encrypt("oh oh je suis le p\u00e8re Noel!!!", myKeys[1], myKeys[2]);
     decryptedMessage = decrypt(EncMess, myKeys);
     println("Public Key is: " + myKeys[1] + " " + myKeys[2] + " // encrypted message: " + EncMess);
     println("Private Key is: " + myKeys[0] + " // decrypted message: " + decryptedMessage);
   }
 }
 // Update les \u00e9tats des boutons
 public void update(int x, int y) {
   for(int i = 0; i < T5.length; i++) {
     T5[i].update();
     if(T5[i].over)
       myInfo(ListN4[i], 0 + i * width / 2, 110 - i * 40);
   }
   if(locked == false) {
     for(int i = 0; i < T1.length; i++)
       T1[i].update();
     for(int i = 0; i < T4.length; i++)
       T4[i].update();
     T6.update();
   } else
     locked = false;
   if(mousePressed) {
     // Actions pour g\u00e9n\u00e9ration des cl\u00e9s
     for(int i = 0; i < T1.length; i++) {
       if(T1[i].pressed()) {
         T1[i].select = true;
         if(i == 0) {
           reset();
           T0[i].setText("  P = " + p + "      Q = " + q);
         } else if(i == 1) {
           if(!(p == A))
             calculate_n();
           T0[i].setText("  N = " + n);
         } else if(i == 2) {
           if(!(p == A))
             launch_e();
           T0[i].setText("  E = " + e);
         } else if(i == 3)
           if(!(e == A)) {
             launch_d();
             T0[i].setText("  D = " + d);
             T2[0].setText(ListN2[0] + d);
             T2[1].setText(ListN2[1] + "(" + n + "; " + e + ")");
             T2[2].setText(ListN2[2] + "(" + n + "; " + e + ")");
           }
       }
       for(int j = 0; j < T1.length - 1; j++)
         if(!(j == i))
           T1[j].select = false;
     }
     // Masquage des infos
     if(T6.pressed()) {
       T6.select = true;
       for(int i = 0; i < T0.length; i++)
         T0[i].setText("");
       T2[0].setText("");
     }
     // Actions pour l'encryptage du message
     for(int i = 0; i < T4.length; i++) {
       if(T4[i].pressed()) {
         T4[i].select = true;
         if(i == 0)
           translate_m();
         else if(i == 1) {
           if(!(n.equals(A)))
             encrypt_m();
         } else if(i == 2) {
           if(!(n.equals(A))) {
             send_m();
             lastInput = "";
             T3[0].setText(lastInput);
             T2[0].setText(ListN2[0] + "");
             // T3[0].display();
           }
         } else if(i == 3)
           if(!(n.equals(A)))
             decrypt_m();
       }
       for(int j = 0; j < T4.length - 1; j++)
         if(!(j == i))
           T4[j].select = false;
     }
   }
 }
 // Fenetre informative
 public void myInfo(String sinfo, int ix, int isize) {
   fill(255);
   rect(ix, 20, width / 2, isize);
   fill(myOr);
   text(sinfo, ix + 20, 40);
 }
 class Button
 {
   int x, y;
   int L, h;
   int basecolor, highlightcolor; // , selectcolor;
   int currentcolor, fcolor;
   String value;
   boolean over = false;
   boolean pressed = false;
   boolean select = false;
 
   public void update() {
     if(over())
       currentcolor = highlightcolor;
     else
       currentcolor = basecolor;
   }
   public boolean pressed() {
     if(over) {
       locked = true;
       return true;
     } else {
       locked = false;
       return false;
     }
   }
   public boolean over() {
     return true;
   }
   public boolean overText(int x, int y, int width, int height) {
     if((mouseX >= x) && (mouseX <= x + width) &&
        (mouseY >= y - height / 2) && (mouseY <= y + height / 2))
       return true;
     else
       return false;
   }
 }
 
 class rectButton extends Button
 {
   rectButton(int ix, int iy, int iL, int ih, int icolor) {
     x = ix;
     y = iy;
     L = iL;
     h = ih;
     value = "null";
     currentcolor = icolor;
   }
 
   public void display() {
     noStroke();
     fill(currentcolor);
     rect(x, y - 17, L, h);
     if(!(value.equals("null"))) {
       fill(255);
       textFont(pfont);
       if(value.length() < 45)
         text(" " + value.substring(0, value.length()), x, y);
       else {
         textFont(pfont, 10);
         if(PApplet.parseInt (value.length() / 58) == 0)
           text(" " + value.substring(0, value.length()), x, y);
         else {
           int count = 0;
           for(int i = 0; i < PApplet.parseInt (value.length() / 58); i++) {
             text(" " + value.substring(i * 58, (i + 1) * 58 - 1), x, y + i * 15);
             count = i + 1;
           }
           text(" " + value.substring(count * 58, value.length()), x, y + count * 15);
         }
       }
     }
   }
   public void setText(String iText) {
     value = iText;
   }
 }
 
 class TextButton extends Button
 {
   TextButton(int ix, int iy, int iL, int ih, int ifcolor, int icolor, int ihighlight, String itext) {
     x = ix;
     y = iy;
     L = iL;
     h = ih;
     fcolor = ifcolor;
     highlightcolor = ihighlight;
     basecolor = icolor;
     currentcolor = basecolor;
     value = itext;
   }
 
   public boolean over() {
     if(overText(x, y, L, h)) {
       over = true;
       return true;
     } else {
       over = false;
       return false;
     }
   }
   public void display() {
     if(x < width / 2)
       stroke(153);
     else
       stroke(190);
     strokeWeight(0.8f);
 
     fill(currentcolor);
     rect(x, y - 17, L, h);
 
     fill(fcolor);
     textFont(pfont);
 
     text(value, x + 10, y);
     noStroke();
   }
 }
 
 /**
  *
  * Fonctions g\u00e9n\u00e9rales de l'application
  *
  */
 
 // //////////////////////////////////////////////////
 // / Actions des controlleurs
 
 public void reset() {
   pq_size = PApplet.parseInt (random(4, 10));
 
   p = new BigInteger(pq_size + 1, prime_certainty, new Random());
   q = new BigInteger(pq_size - 1, prime_certainty, new Random());
   if(p == null)
     p = new BigInteger(pq_size + 1, prime_certainty, new Random());
   if(q == null)
     q = new BigInteger(pq_size - 1, prime_certainty, new Random());
 }
 public void calculate_n() {
   // Calculer n = p\u00d7q
   n = p.multiply(q);
 }
 public void launch_e() {
   e = generate_e(p, q, 16);
 }
 public void launch_d() {
   // Calculer d selon:
   // Il existe un relatif entier m, tel que e \u00d7 d + m \u00d7 (p - 1)(q - 1) = 1
   // d est la cl\u00e9 priv\u00e9e
   d = calculate_d(p, q, e);
 }
 public void translate_m() {
  BigInteger MessBits = newBigInteger(lastInput.getBytes());
   T3[1].setText(MessBits + " ");
 }
 public void encrypt_m() {
  BigInteger MessBits = newBigInteger(lastInput.getBytes());
   EncMessBits = encrypt(MessBits, e, n);
   T3[2].setText(EncMessBits + " ");
 }
 public void decrypt_m() {
   BigInteger DecMessBits = decrypt(EncMessBits, d, n);
   String decryptedMessage = new String(DecMessBits.toByteArray());
   T3[4].setText(decryptedMessage + " ");
 }
 public void send_m() {
   T3[3].setText(EncMessBits + " ");
 }
 // //////////////////////////////////////////////////
 // Fonctions pour la m\u00e9thode RSA
 
 public static BigInteger generate_e(BigInteger p, BigInteger q, int bitsize) {
   BigInteger e, phi_pq;
 
   e = new BigInteger("0");
   phi_pq = q.subtract(new BigInteger("1"));
   phi_pq = phi_pq.multiply(p.subtract(new BigInteger("1")));
 
   int i = 0;
   do {
     e = (new BigInteger(bitsize, 0, new Random())).setBit(0);
     i = i + 1;
   } while(i < 100 && (e.gcd(phi_pq).compareTo(new BigInteger("1")) != 0));
   return e;
 }
 public static BigInteger calculate_d(BigInteger p, BigInteger q, BigInteger e) {
   BigInteger d, phi_pq;
 
   phi_pq = q.subtract(new BigInteger("1"));
   phi_pq = phi_pq.multiply(p.subtract(new BigInteger("1")));
 
   d = e.modInverse(phi_pq);
   return d;
 }
 public static BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n) {
   BigInteger c, bitmask;
   c = new BigInteger("0");
   int i = 0;
   bitmask = (new BigInteger("2")).pow(n.bitLength() - 1).subtract(new BigInteger("1"));
   while(m.compareTo(bitmask) == 1) {
     c = m.and (bitmask).modPow(e, n).shiftLeft(i * n.bitLength()).or (c);
     m = m.shiftRight(n.bitLength() - 1);
     i = i + 1;
   }
   c = m.modPow(e, n).shiftLeft(i * n.bitLength()).or (c);
   return c;
 }
 public static BigInteger decrypt(BigInteger c, BigInteger d, BigInteger n) {
   BigInteger m, bitmask;
   m = new BigInteger("0");
   int i = 0;
   bitmask = (new BigInteger("2")).pow(n.bitLength()).subtract(new BigInteger("1"));
   while(c.compareTo(bitmask) == 1) {
     m = c.and (bitmask).modPow(d, n).shiftLeft(i * (n.bitLength() - 1)).or (m);
     c = c.shiftRight(n.bitLength());
     i = i + 1;
   }
   m = c.modPow(d, n).shiftLeft(i * (n.bitLength() - 1)).or (m);
 
   return m;
 }
 /** Cr\u00e9e une cl\u00e9 priv\u00e9e D et le couple de cl\u00e9 publiques (E, N).
  * @return Un tableau de 3 entiers avec les cl\u00e9s keys[] = {D, E, N};
  */
 public static BigInteger[] createKeys() {
   BigInteger[] Keys = new BigInteger[3];
   
   int pqSize = (int) (4 + 6 * Math.random());
   BigInteger p_ = new BigInteger(pqSize + 1, prime_certainty, new Random());
   BigInteger q_ = new BigInteger(pqSize - 1, prime_certainty, new Random());
   if(p_ == null)
     p_ = new BigInteger(pqSize + 1, prime_certainty, new Random());
   if(q_ == null)
     q_ = new BigInteger(pqSize - 1, prime_certainty, new Random());
   BigInteger n_ = p_.multiply(q_);
   BigInteger e_ = cryptageRSA.generate_e(p_, q_, 16);
   BigInteger d_ = cryptageRSA.calculate_d(p_, q_, e_);
   
   Keys[0] = d_;
   Keys[1] = e_;
   Keys[2] = n_;
   
   return Keys;
 }
   /** Encrypte un message \u00e0 l'aide de cl\u00e9s.
    * @param m Le message \u00e0 encrypter.
    * @param E cl\u00e9 publique.
    * @param N cl\u00e9 publique.
    * @return Le message encrypt\u00e9 sous forme d'une suite de chiffres.
    */
   public static BigInteger encrypt(String m, BigInteger E, BigInteger N) {
     BigInteger EncMessBits = null;
 
    BigInteger MessBits = newBigInteger(m.getBytes());
     EncMessBits = cryptageRSA.encrypt(MessBits, E, N);
 
     return EncMessBits;
   }
   /** D\u00e9crypte un message \u00e0 l'aide de cl\u00e9s.
    * @param m Le message encrypt\u00e9 sous forme de chiffres.
    * @param k cl\u00e9s, publique et priv\u00e9e.
    * @return Le message d\u00e9crypt\u00e9.
    */
   public static String decrypt(BigInteger m, BigInteger[] k) {
     String decryptedMessage = null;
 
     BigInteger DecMessBits = cryptageRSA.decrypt(m, k[0], k[2]);
     decryptedMessage = new String(DecMessBits.toByteArray());
 
     return decryptedMessage;
   }

// Construction prot\u00e9g\u00e9e d'un entier
private static BigInteger newBigInteger(byte[] value) {
  try {
    return new BigInteger(value);
  } catch(Exception e) {
    return new BigInteger("0");
  }
}
   static public void main(String args[]) {
     PApplet.main(new String[] { "--bgcolor=#DFDFDF", "cryptageRSA" });
   }
 }
