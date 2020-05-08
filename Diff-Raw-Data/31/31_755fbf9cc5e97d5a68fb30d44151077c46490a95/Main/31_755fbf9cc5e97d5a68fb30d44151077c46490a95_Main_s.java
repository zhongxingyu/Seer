 import com.pi4j.io.gpio.GpioPinDigitalOutput;
 
 import javax.xml.soap.Text;
 import java.net.Socket;
 import java.util.Scanner;
 public class Main {
 
     public static void main(String[] args) throws InterruptedException {
        /* //int Bild[][] = new int [19][11];
         GpioPinDigitalOutput out [] = new GpioPinDigitalOutput [10];
         System.out.println("** PiLED V0.1 **");
         out  = Ausgabe.init();
         //Scanner sc = new Scanner(System.in);
 
 
        // for (int l=1;l<100;l++){
            // Ausgabe.Ausgabe(Bild,out[1],out[4],out[5]);
         //}
 
      //Programme.Uhr();
        Socket Sok=Net.soket();
        while (true){
        //System.out.println(Net.Anfrage(Sok));
            Thread.sleep(100);
            String daten=Net.Anfrage(Sok);
            System.out.println(daten.substring(0,2));
            if (daten.contains("ON")){
                System.out.println("OOOONNN");
                int Bild[][] = new int [19][11];
                for (int x=1;x<19;x++){
                    for (int y=1;y<11;y++){
                        Bild[x][y]= 1;
 
 
                    }
                }
                Ausgabe.Ausgabe(Bild,out[1],out[4],out[5]);
 
            }else if(daten.contains("OF"))  {
                int Bild[][] = new int [19][11];
 
                        Ausgabe.Ausgabe(Bild,out[1],out[4],out[5]);
 
 
 
            }
        }
      */
        // Programme.Snake();
        Programme.ScrollingText(args[0],200,true);
         //IR_Controll.IR_Recive();
        //Programme.Uhr();
         while(true){
 
 
         }
     }
 }
