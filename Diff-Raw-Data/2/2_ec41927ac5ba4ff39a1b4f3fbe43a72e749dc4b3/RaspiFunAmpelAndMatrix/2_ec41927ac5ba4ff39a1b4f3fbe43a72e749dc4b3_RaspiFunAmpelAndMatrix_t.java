 package net.slintes.raspi;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: slintes
  * Date: 28.12.12
  * Time: 15:43
  *
  * some fun with 3 LEDs and the Adafruit 8*8 bicolor LED matrix
  *
  */
 public class RaspiFunAmpelAndMatrix {
 
     private static final int I2C_BUS_NR = 1; // 0 for older Pi revisions
     private static int LED_PACK_ADDRESS = 0x0070;
 
     // see http://pi4j.com/usage.html#Pin_Numbering
     private static final int GPIO_PIN_RED = 4;
     private static final int GPIO_PIN_YELLOW = 5;
     private static final int GPIO_PIN_GREEN = 6;
 
     public static void main(String[] args) throws IOException {
 
         Adafruit8x8LEDMatrix leds = new Adafruit8x8LEDMatrix(I2C_BUS_NR, LED_PACK_ADDRESS);
         Ampel ampel = new Ampel(GPIO_PIN_RED, GPIO_PIN_YELLOW, GPIO_PIN_GREEN);
 
         while(true){
             for (int row = 0; row < 8; row++) {
                 for (int col = 0; col < 8; col++) {
 
                     int color = row % 3;
 
                     Adafruit8x8LEDMatrix.LedColor ledColor;
                     Ampel.State ampelState;
 
                     switch (color){
                         case 0: ledColor= Adafruit8x8LEDMatrix.LedColor.RED; ampelState = Ampel.State.RED; break;
                        case 1: ledColor= Adafruit8x8LEDMatrix.LedColor.YELLOW; ampelState = Ampel.State.YELLOW; break;
                         case 2: ledColor= Adafruit8x8LEDMatrix.LedColor.GREEN; ampelState = Ampel.State.GREEN; break;
                         default: ledColor= Adafruit8x8LEDMatrix.LedColor.OFF; ampelState = Ampel.State.OFF;
                     }
 
                     ampel.setState(ampelState);
 
                     leds.clear(false);
                     leds.setPixel(row, row % 2 == 0 ? col : 7-col, ledColor);
                     leds.writeDisplay();
 
                     try {
                         Thread.sleep(80);
                     } catch (InterruptedException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         }
     }
 }
