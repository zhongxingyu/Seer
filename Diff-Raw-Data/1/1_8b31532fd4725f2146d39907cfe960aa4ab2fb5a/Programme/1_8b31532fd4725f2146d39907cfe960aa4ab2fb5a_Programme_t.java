 import com.pi4j.io.gpio.GpioPinDigitalOutput;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Daniel
  * Date: 19.08.13
  * Time: 15:29
  * To change this template use File | Settings | File Templates.
  */
 public class Programme {
     int Length;
     int Bild[][];
 
 
     public static void ScrollingText(String Text, int Speed, Boolean Loop) throws InterruptedException {
 
         GpioPinDigitalOutput out[] = new GpioPinDigitalOutput[10];
         out = Ausgabe.init();
         int xPos = 0;
         int ScrollingText[][] = new int[(Text.length() * 11) + 19][10];
         int Bild[][] = new int[19][11];
         Programme Prog = new Programme();
 
         for (int a = 0; a < Text.length(); a++) {
             Prog = Zeichen(ScrollingText, xPos, 0, Text.charAt(a));
             xPos = xPos + Prog.Length + 1;
             ScrollingText = Prog.Bild;
         }
         if (Loop) {
             for (int x = 1; x < 19; x++) {
                 for (int y = 1; y < 11; y++) {
                     ScrollingText[x + xPos + 3][y - 1] = ScrollingText[x][y - 1];
                 }
             }
         }
 
         do {
             for (int a = 0; a < xPos + 3; a++) {
                 for (int x = 1; x < 19; x++) {
                     for (int y = 1; y < 11; y++) {
                         Bild[x][y] = ScrollingText[x - 1 + a][y - 1];
                     }
                 }
                 Ausgabe.Ausgabe(Bild, out[1], out[4], out[5]);
                 Thread.sleep(Speed);
             }
 
         } while (Loop);
 
     }
 
 
 

     public static Programme Zeichen(int Bild[][], int xPos, int yPos, char Asci) throws InterruptedException {
         //System.out.println("**PiLed Textausgabe**");
         //GpioPinDigitalOutput out[] = new GpioPinDigitalOutput[10];
         //out = Ausgabe.init();
         //int Asci=0;
         //while(true){
         //Asci++;
         //    if(Asci>91) Asci=1;
         String Zeichen[] = new String[100];
         //int Bild[][] = new int[19][11];
         int index = 0;
         int count = 0;
 
 
         try {
             BufferedReader in = new BufferedReader(new FileReader("Schrift.txt"));
             String zeile = null;
             while ((zeile = in.readLine()) != null) {
                 index++;
                 Zeichen[index] = zeile;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         for (int y = 1; y < 10; y++) {
             for (int x = 1; x < (Zeichen[Asci - 33].length() / 9) + 1; x++) {
                 if ((Zeichen[Asci - 33].substring(count, count + 1)).equals("1")) {
                     Bild[x + xPos][y + yPos] = 1;
                 }
                 //System.out.println(Bild[x][y]);
                 count++;
             }
         }
 
         //int BildA[][] = new int[19][11];
         //BildA[1][1]=1;
 
         //Ausgabe.Ausgabe(Bild, out[1], out[4], out[5]);
         //    Thread.sleep(500);
         Programme Prog = new Programme();
         Prog.Bild = Bild;
         Prog.Length = Zeichen[Asci - 33].length() / 9;
         return Prog;
     }
 
     public static void Snake() throws InterruptedException {
         int length = 1;
         int Appel = 1;
         int AppelX = 0;
         int AppelY = 0;
         int directionX = 1;
         int directionY = 0;
         int X = 5;
         int Y = 5;
         int SnakeX[] = new int[50];
         int SnakeY[] = new int[50];
 
         GpioPinDigitalOutput out[] = new GpioPinDigitalOutput[10];
         out = Ausgabe.init();
 
         Socket Sok = Net.soket();
 
         while (true) {
             Thread.sleep(1000);
             int Bild[][] = new int[19][11];
 
             if (Appel == 0) {
                 AppelX = (int) Math.random() * 18;
                 AppelY = (int) Math.random() * 10;
                 Appel = 1;
             }
 
             if ((SnakeX[0] == AppelX) && (SnakeY[0] == AppelY)) {
                 length++;
                 Appel = 0;
 
             }
 
 
             String daten = (Net.Anfrage(Sok)).substring(0, 2);
             System.out.print(daten + "  ");
             System.out.print(X);
             System.out.print("  ");
             System.out.println(Y);
 
             if (daten.contains("UP")) directionY = -1;
             if (daten.contains("DO")) directionY = +1;
             if (daten.contains("LE")) directionX = -1;
             if (daten.contains("RI")) directionX = +1;
 
             for (int i = length; i > 1; i--) {
                 SnakeX[i - 1] = SnakeX[i - 2];
                 SnakeY[i - 1] = SnakeY[i - 2];
             }
             SnakeX[0] = SnakeX[0] + directionX;
             SnakeY[0] = SnakeY[0] + directionY;
 
             //if ((X > 0) & (Y > 0) & (X < 19) & (Y < 11)) {
             for (int i = 0; i < length; i++)
                 Bild[SnakeX[i]][SnakeY[i]] = 1;
             //}
             if (Appel == 1) Bild[AppelX][AppelY] = 1;
             Ausgabe.Ausgabe(Bild, out[1], out[4], out[5]);
         }
 
     }
 
     public static void Uhr() throws InterruptedException {
         //Zahlen:
         int Zahlen[][][] =
                 {{{0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1}}, {{0, 1, 2, 2, 2, 2, 2, 2, 2}, {2, 1, 0, 1, 2, 3, 4, 5, 6}}, {{0, 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 1, 2}, {0, 0, 0, 1, 2, 3, 3, 3, 4, 5, 6, 6, 6,}},
                         {{0, 1, 2, 2, 2, 2, 1, 0, 2, 2, 2, 1, 0}, {0, 0, 0, 1, 2, 3, 3, 3, 4, 5, 6, 6, 6}}, {{0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2}, {0, 1, 2, 3, 3, 0, 1, 2, 3, 4, 5, 6}},
                         {{0, 1, 2, 0, 0, 0, 1, 2, 2, 2, 2, 1, 0}, {0, 0, 0, 1, 2, 3, 3, 3, 4, 5, 6, 6, 6}}, {{0, 1, 2, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 1}, {0, 0, 0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 3}},
                         {{0, 1, 2, 2, 2, 2, 2, 2, 2}, {0, 0, 0, 1, 2, 3, 4, 5, 6}}, {{0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 1}, {0, 0, 0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 3}},
                         {{0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 1}, {0, 0, 0, 1, 2, 3, 4, 5, 6, 6, 6, 3, 2, 1, 3}}};
 
         GpioPinDigitalOutput out[] = new GpioPinDigitalOutput[10];
         out = Ausgabe.init();
         //ocket Sok=Net.soket();
         //Boolean quitt=true;
         while (true) {
             // quitt=!Net.Anfrage(Sok).contains("exit");
 
             int Bild[][] = new int[19][11];
             SimpleDateFormat hh = new SimpleDateFormat();
             SimpleDateFormat mm = new SimpleDateFormat();
             SimpleDateFormat ss = new SimpleDateFormat();
             hh.applyPattern("hh");
             mm.applyPattern("mm");
             ss.applyPattern("ss");
             Thread.sleep(10);
             //ss.format(new Date()
             int h1 = Integer.parseInt(hh.format(new Date()).substring(0, 1));
             int h2 = Integer.parseInt(hh.format(new Date()).substring(1, 2));
             int m1 = Integer.parseInt(mm.format(new Date()).substring(0, 1));
             int m2 = Integer.parseInt(mm.format(new Date()).substring(1, 2));
             int sek = Integer.parseInt(ss.format(new Date()));
 
             for (int i = 0; i < Zahlen[h1][0].length; i++) {
                 Bild[1 + Zahlen[h1][0][i]][(Zahlen[h1][1][i]) + 2] = 1;
             }
 
             for (int i = 0; i < Zahlen[h2][0].length; i++) {
                 Bild[5 + Zahlen[h2][0][i]][(Zahlen[h2][1][i]) + 2] = 1;
             }
 
             for (int i = 0; i < Zahlen[m1][0].length; i++) {
                 Bild[12 + Zahlen[m1][0][i]][(Zahlen[m1][1][i]) + 2] = 1;
             }
 
             for (int i = 0; i < Zahlen[m2][0].length; i++) {
                 Bild[16 + Zahlen[m2][0][i]][(Zahlen[m2][1][i]) + 2] = 1;
             }
             Bild[9][4] = 1;
             Bild[10][4] = 1;
             Bild[9][6] = 1;
             Bild[10][6] = 1;
 
             Bild[9][7] = 1;
             Bild[10][7] = 1;
             Bild[9][3] = 1;
             Bild[10][3] = 1;
 
             for (int i = 0; i < (18 * sek / 55); i++) {
                 Bild[i][10] = 1;
             }
             Ausgabe.Ausgabe(Bild, out[1], out[4], out[5]);
             //System.out.println(h2);
 
 
         }
 
 
     }
 
 
 }
