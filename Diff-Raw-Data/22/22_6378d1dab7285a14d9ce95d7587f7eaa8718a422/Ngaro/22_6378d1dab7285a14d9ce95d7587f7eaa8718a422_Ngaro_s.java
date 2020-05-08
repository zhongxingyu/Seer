 package Retro;
 
 /**********************************************************************
  * Ngaro VM
  * Written by Charles Childers
  * Ported to J2ME by Martin Polak
  * Released into the public domain.
  *
  **********************************************************************/
 
 import javax.microedition.lcdui.*;
 import java.io.*;
 import javax.microedition.rms.*;
 
 public class Ngaro {
 
   private EvalForm evalForm;
 
   /**********************************************************************
    * Symbolic constants for each instruction.
    **********************************************************************/
   private static final int VM_NOP = 0;  private static final int VM_LIT = 1;  private static final int VM_DUP = 2;
   private static final int VM_DROP = 3; private static final int VM_SWAP = 4; private static final int VM_PUSH = 5;
   private static final int VM_POP = 6;  private static final int VM_CALL = 7; private static final int VM_JUMP = 8;
   private static final int VM_RETURN = 9; private static final int VM_GT_JUMP = 10; private static final int VM_LT_JUMP = 11;
   private static final int VM_NE_JUMP = 12; private static final int VM_EQ_JUMP = 13; private static final int VM_FETCH = 14;
   private static final int VM_STORE = 15; private static final int VM_ADD = 16; private static final int VM_SUB = 17;
   private static final int VM_MUL = 18; private static final int VM_DIVMOD = 19;  private static final int VM_AND = 20;
   private static final int VM_OR = 21;  private static final int VM_XOR = 22; private static final int VM_SHL = 23;
   private static final int VM_SHR = 24; private static final int VM_ZERO_EXIT = 25; private static final int VM_INC = 26;
   private static final int VM_DEC = 27; private static final int VM_IN = 28;  private static final int VM_OUT = 29;
   private static final int VM_WAIT = 30;
 
 /**********************************************************************
  * Some constants useful to us for dealing with the VM settings.
  **********************************************************************/
   private static final int STACK_DEPTH = 100;
   private static final int CYCLES_PER = 40;
 
 /**********************************************************************
  * Internal registers
  **********************************************************************/
   int sp = 0, rsp = 0, ip = 0;
   int trace = 0, run = 0;
   int [] data = new int[STACK_DEPTH];
   int [] address = new int[STACK_DEPTH];
   int [] ports = new int[1024];
   int interval;
 
 /**********************************************************************
  * Temporary vars for ANSI emulation
  **********************************************************************/
   StringBuffer escSeq = new StringBuffer("");
   boolean escape = false;
 
   public Ngaro(EvalForm evalForm) {
     this.evalForm = evalForm;
 	}
 
 /**********************************************************************
  * initVM()
  * Initialize the Ngaro VM
  **********************************************************************/
 public void initVM() {
   ip  = 0;
   sp  = 0;
   rsp = 0;
   ports[0] = 0;
   data[0] = 0;
 }
 
 /**********************************************************************
  * handleDevices()
  * This handles the simulated hardware devices. Specifically, it tries
  * to approximate a console interface using lcdui forms.
  **********************************************************************/
 public void handleDevices() {
   if (ports[0] == 1) return;
 
   /* Input */
   if (ports[0] == 0 && ports[1] == 1) {
     ports[0] = 1;
     ports[1] = this.evalForm.buffer.charAt(0);
     this.evalForm.buffer.deleteCharAt(0);
   }
 
   /* Output */
   if (ports[2] == 1) {
    if ((char) data[sp] == (char) 27) {
      escape = true;
     } else {
      if (escape == true) {
        escSeq.append((char) data[sp]);
        if (escSeq.toString().equals("[2J")) {
          this.evalForm.siOut.setText(" ");
          escSeq = new StringBuffer();
          escape = false;
        } else if (escSeq.toString().equals("[1;1H")) {
          this.evalForm.siOut.setText(" ");
          escSeq = new StringBuffer();
          escape = false;
        }
      } else {
         this.evalForm.siOut.setText(this.evalForm.siOut.getText() + (char) data[sp]);
      }
     }
     sp--;
     ports[2] = 0;
     ports[0] = 1;
   }
 
   /* Save image */
   if (ports[4] == 1) {
     evalForm.saving = true;
     RecordStore rstore = null;
     try {
       RecordStore.deleteRecordStore("image");
       rstore = RecordStore.openRecordStore("image", true);
       for (int j = 0; j < Retroforth.IMAGE_SIZE / 1000; j++) {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         DataOutputStream os = new DataOutputStream(output);
         for (int i = j * 1000; i < (j * 1000 + 1000); i++) {
           os.writeInt(Retroforth.image[i]);
         }
         byte[] bytes = output.toByteArray();
         rstore.addRecord(bytes, 0, bytes.length);
       }
     } catch (Exception e) {
       this.evalForm.siOut.setText(this.evalForm.siOut.getText() + e.toString());
     } finally {
       try {
         rstore.closeRecordStore();
       } catch (Exception e1) { }
     }
     ports[4] = 0;
     evalForm.saving = false;
   }
 
   /* Capabilities */
   if (ports[5] == -1) {
     ports[5] = Retroforth.IMAGE_SIZE;     /* ammount of memory */
     ports[0] = 1;
   }
   if (ports[5] == -2) {
     ports[5] = 0;                         /* address of framebuffer, 0 if none */
     ports[0] = 1;
   }
   if (ports[5] == -3) {
     ports[5] = 0;                         /* width of framebuffer */
     ports[0] = 1;
   }
   if (ports[5] == -4) {
     ports[5] = 0;                         /* height of framebuffer */
     ports[0] = 1;
   }
   if (ports[5] == -5) {
     ports[5] = sp;                        /* items on data stack */
     ports[0] = 1;
   }
   if (ports[5] == -6) {
     ports[5] = rsp;                       /* items on address stack */
     ports[0] = 1;
   }
 }
 /**********************************************************************
  * processOpcode()
  * This is the main piece of code in Ngaro. It looks up each opcode and
  * carries out the proper action. This is mostly a straight port of the
  * C implementation, so some optimization opportunities will probably
  * exist.
  **********************************************************************/
 public void processOpcode() {
   int x, y, z, op;
   op = Retroforth.image[ip];
   switch(op)
   {
     case VM_NOP:
       break;    
     case VM_LIT:
       sp++; ip++; data[sp] = Retroforth.image[ip];
       break;
     case VM_DUP:
       sp++; data[sp] = data[sp-1];
       break;    
     case VM_DROP:
       data[sp] = 0; sp--;
       break;    
     case VM_SWAP:
       x = data[sp];
       y = data[sp-1];
       data[sp] = y;
       data[sp-1] = x;
       break;    
     case VM_PUSH:
       rsp++;
       address[rsp] = data[sp];
       sp--;
       break;   
     case VM_POP:
       sp++;
       data[sp] = address[rsp];
       rsp--;
       break;    
     case VM_CALL:
       ip++; rsp++;
       address[rsp] = ip++;
       ip = Retroforth.image[ip-1] - 1;
       break;    
     case VM_JUMP:
       ip++;
       ip = Retroforth.image[ip] - 1;
       break;    
     case VM_RETURN:
       ip = address[rsp]; rsp--;
       break;    
     case VM_GT_JUMP:
       ip++;
       if (data[sp-1] > data[sp])
         ip = Retroforth.image[ip] - 1;
       sp = sp - 2;
       break;    
     case VM_LT_JUMP:
       ip++;
       if (data[sp-1] < data[sp])
         ip = Retroforth.image[ip] - 1;
       sp = sp - 2;
       break;    
     case VM_NE_JUMP:
       ip++;
       if (data[sp-1] != data[sp])
         ip = Retroforth.image[ip] - 1;
       sp = sp - 2;
       break;    
     case VM_EQ_JUMP:
       ip++;
       if (data[sp-1] == data[sp])
         ip = Retroforth.image[ip] - 1;
       sp = sp - 2;
       break;    
     case VM_FETCH:
       x = data[sp];
       data[sp] = Retroforth.image[x];
       break;    
     case VM_STORE:
       Retroforth.image[data[sp]] = data[sp-1];
       sp = sp - 2;
       break;    
     case VM_ADD:
       data[sp-1] += data[sp]; data[sp] = 0; sp--;
       break;    
     case VM_SUB:
       data[sp-1] -= data[sp]; data[sp] = 0; sp--;
       break;
     case VM_MUL:
       data[sp-1] *= data[sp]; data[sp] = 0; sp--;
       break;    
     case VM_DIVMOD:
       x = data[sp];
       y = data[sp-1];
       data[sp] = y / x;
       data[sp-1] = y % x;
       break;   
     case VM_AND:
       x = data[sp];
       y = data[sp-1];
       sp--;
       data[sp] = x & y;
       break;    
     case VM_OR:
       x = data[sp];
       y = data[sp-1];
       sp--;
       data[sp] = x | y;
       break;    
     case VM_XOR:
       x = data[sp];
       y = data[sp-1];
       sp--;
       data[sp] = x ^ y;
       break;    
     case VM_SHL:
       x = data[sp];
       y = data[sp-1];
       sp--;
       data[sp] = y << x;
       break;    
     case VM_SHR:
       x = data[sp];
       y = data[sp-1];
       sp--;
       data[sp] = y >>= x;
       break;    
     case VM_ZERO_EXIT:
       if (data[sp] == 0)
       {
         sp--;
         ip = address[rsp]; rsp--;
       }
       break;    
     case VM_INC:
       data[sp]++;
       break;    
     case VM_DEC:
       data[sp]--;
       break;    
     case VM_IN:
       x = data[sp];
       data[sp] = ports[x];
       ports[x] = 0;
       break;    
     case VM_OUT:
       ports[0] = 0;
       ports[data[sp]] = data[sp-1];
       sp = sp - 2;
       break;    
     case VM_WAIT:
       handleDevices();
       break;    
     default:
       this.evalForm.siOut.setText(this.evalForm.siOut.getText() + "ERROR: IP: " + ip + " op: " + Retroforth.image[ip] + "\n");
       ip = Retroforth.IMAGE_SIZE;
       run = 0;
   }
 }
 
 /**********************************************************************
  * runImage()
  **********************************************************************/
 public void runImage() {
   run = 1;
 }
 
 /**********************************************************************
  * startVM()
  **********************************************************************/
 public void startVM() {
   run = 1;
   processImage();
 }
 
 /**********************************************************************
  * processImage()
  * This runs through the image, calling processOpcode() for each 
  * instruction. It will only run if:
  *
  *  a) There is input in the input buffer
  *  b) An internal 'run' flag is set 
  *
  * For performance reasons up to CYCLES_PER instructions will be
  * executed by this code per call.
  **********************************************************************/
 public void processImage() {
   if (evalForm.buffer.length() <= 0 || run == 0)
   {
     run = 0;
     return;
   }
 
   for (int a = CYCLES_PER; a > 0; a--)
   {
    System.out.println(ip);
     processOpcode();
     ip++;
   }
 }
 }
