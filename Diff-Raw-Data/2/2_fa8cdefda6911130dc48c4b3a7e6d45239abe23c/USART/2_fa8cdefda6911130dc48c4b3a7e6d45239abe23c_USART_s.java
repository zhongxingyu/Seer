 package cpu; 
 
 import wp.*; 

 /**
  * Acts like a USART on an ATMEGA
  * 
  * @author Jacob Weiss
  * @version 0.0.1
  */
 public class USART extends Peripheral
 {
     public USART(Memory mem)
     {
         super(mem);
     }
     
     public void clock()
     {
         if ((this.mem.io[IO.UCSR1A] & 32) == 0)
         {
             System.out.print(this.mem.readIO(IO.UDR1));
         }
         this.mem.io[IO.UCSR1A] |= 32;
     }
 }
