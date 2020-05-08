 package emulator.cpu; 
 
 import java.util.Date;
 import emulator.wp.IO;
 import robot.Robot;
 import robot.MotorPart;
 import emulator.assembler.Assembler;
 import robot.ConsolePart;
 /**
  * Write a description of class Test here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Test
 {
     public static void testPrinterInput() throws java.lang.InterruptedException, java.io.IOException
     {   
         //Assembler a = new Assembler("/Users/jacob/Stacks/Dropbox/12th/APCS/FROIDZ/FROIDZ/java-src/emulator/cpu/Print.asm");
         //a.assemble();
         //a.write("/Users/jacob/Stacks/Dropbox/12th/APCS/FROIDZ/FROIDZ/java-src/emulator/cpu/Printer.tst");
         AVR cpu = new AVR("/Users/jacob/Stacks/Dropbox/12th/APCS/FROIDZ/FROIDZ/java-src/emulator/cpu/Printer.tst");     
         
         ConsolePart p = new ConsolePart();
         cpu.connectToSerial(p, 0);
         
        
        
         while (true)
         {
             cpu.act(10);
             Thread.sleep(10);
         }
     }
     
     public static void testPinConnector()
     {
         AVR cpu = new AVR("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/go.tst");
         
         PinPrinter p = new PinPrinter();
         
         cpu.connectToPWM(p, IO.OCR3AH);
         
         cpu.connectToPin(p.clockPin(), IO.PORTB, 0);
        
         for (int i = 0; i < 100; i++)
         {
             cpu.act(1);
             p.clock();
         }
     }
     
     
     public static void testUSARTFile()
     {
         Robot r = new Robot("Fred");
         
         MotorPart m = new MotorPart();
         m.setMaxSpeed(100); //added by Henry, allows Test to compile with changes to MotorPart
         m.setSerialPort(0);
         
         AVR cpu = new AVR("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/go.tst");
         
         r.setCPU(cpu);
         r.addPart(m);
         
         //Printer p = new Printer();
         
         //cpu.connectToSerial(p, IO.UDR1);
       
         cpu.act(100);
         
         //r.printSpeed();
     }
     
     public static void testUSART()
     {
         AVR cpu = new AVR();
         
         ConsolePart p = new ConsolePart();
         
         cpu.connectToSerial(p, IO.UDR1);
         
         cpu.proc.mem.flash[0] = Integer.parseInt("01000011000000000000000101001000", 2);
         cpu.proc.mem.flash[1] = Integer.parseInt("01000110000000001001110000000001", 2); 
         cpu.proc.mem.flash[2] = Integer.parseInt("01000011000000000000000101100101", 2);
         cpu.proc.mem.flash[3] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[4] = Integer.parseInt("01000011000000000000000101101100", 2);
         cpu.proc.mem.flash[5] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[6] = Integer.parseInt("01000011000000000000000101101100", 2);
         cpu.proc.mem.flash[7] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[8] = Integer.parseInt("01000011000000000000000101101111", 2);
         cpu.proc.mem.flash[9] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[10] = Integer.parseInt("01000011000000000000000100101100", 2);
         cpu.proc.mem.flash[11] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[12] = Integer.parseInt("01000011000000000000000100100000", 2);
         cpu.proc.mem.flash[13] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[14] = Integer.parseInt("01000011000000000000000101010111", 2);
         cpu.proc.mem.flash[15] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[16] = Integer.parseInt("01000011000000000000000101101111", 2);
         cpu.proc.mem.flash[17] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[18] = Integer.parseInt("01000011000000000000000101110010", 2);
         cpu.proc.mem.flash[19] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[20] = Integer.parseInt("01000011000000000000000101101100", 2);
         cpu.proc.mem.flash[21] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[22] = Integer.parseInt("01000011000000000000000101100100", 2);
         cpu.proc.mem.flash[23] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[24] = Integer.parseInt("01000011000000000000000100001101", 2);
         cpu.proc.mem.flash[25] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[26] = Integer.parseInt("01000011000000000000000100001101", 2);
         cpu.proc.mem.flash[27] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[26] = Integer.parseInt("01000011000000000000000100001010", 2);
         cpu.proc.mem.flash[27] = Integer.parseInt("01000110000000001001110000000001", 2);
         cpu.proc.mem.flash[28] = Integer.parseInt("01010011000000000000000000000000", 2);      
         
         cpu.act(100);
     }
 
     public static void main()
     {
         FROIDZCPU p = new AVR();
         
         p.act(3);
         
         System.out.println((int)(((ToastyProcessor)(p.getProcessor())).mem.sram[3]));
         System.out.println((int)(((ToastyProcessor)(p.getProcessor())).mem.io[0]));
     }
     
     public static void speedTest()
     {
         FROIDZCPU cpu = new AVR();
         cpu.proc.mem.flash[0] = Integer.parseInt("01010011000000000000000000000000", 2);
         long cycles = 0;
         Date t = new Date();
         long start = t.getTime();
         while(cycles < 600000000)
         {
             cpu.act(1000);
             cycles += 1000;
         }
         t = new Date();
         System.out.println((cycles / (t.getTime() - start)) / 1000 + " mHz");
     }
     
     public static AVR helloWorld()
     {
         AVR cpu = new AVR("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/cpu/helloworld.asm");
        
         return cpu;
     }
      
     public static void pinTest()
     {
         Pin<Boolean> p1 = new Pin(true);
         Pin<Boolean> p2 = new Pin(false);
         LED l1 = new LED();
         LED l2 = new LED();
         
         p1.connect(l1);
         p2.connect(l2);
         l1.connect(l2);
     }
     
     /*
     public static void addTest()
     {
         cpu.proc.flash[0] = 1124073729; // Put 1 into reg 1
         cpu.proc.flash[1] = 1124073986; // Put 2 into reg 2
         cpu.proc.flash[2] = 16974337; // Add reg 1 and reg 2, put result in reg 3
         this.act(3);
         System.out.println((int)proc.mem.registers[1] + " + " + (int)proc.mem.registers[2] + " = " + (int)proc.mem.registers[3]);
     }
     */
 }
