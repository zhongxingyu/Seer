 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 import java.io.Reader;
 import java.io.BufferedReader;
 import java.util.regex.Pattern;
 import java.util.Random;
 import java.util.InputMismatchException;
 import java.util.NoSuchElementException;
 
 /**
  * Implementation of Dr Fossum's Stack Machine.
  *
  * @author Edward M. Banner
  *
  */
 public class StackMachine {
 
     // memory consists of stack at high memory and opcodes/data at low memory
    public static Memory mem;
     public static boolean TRACE;  // TRACE mode not implemented
    public static Scanner in;  // for READ and REAC opcodes
     public static boolean oldStyle;  // support legacy opcode numbers
     public static int DEBUG = 0;
 
    public StackMachine() {
        mem = new Memory(16384);
        in = new Scanner(System.in);
    }

     /**
      * Execute the executable file on the Stack Machine.
      *
      * @param file the executable file
      *
      */
     public void run(String file) throws IOException {
         // base address starts somewhere between 15 and 1000 exclusive
         int baseAddr = new Random().nextInt(984) + 16;
         if (DEBUG == 1) {  System.out.println("Base address: " + baseAddr); }
 
         // insert opcodes and data into the stack machine and perform
         // relocation process
         int entryPoint = initializeMemory(baseAddr, file);
         if (DEBUG == 1) { System.out.println("Entry point: " + entryPoint); }
         int PC = entryPoint;
         
         while (true) { // execute opcodes
             PC = executeOpcode(PC);
         }
     }
 
     /**
      * Parses the header of the SXX executable and loads the opcodes and data
      * into memory.
      *
      * @param baseAddr address where the first opcode is inserted
      * @param file     SXX executable
      *
      */
     public static int initializeMemory(int baseAddr, String file) {
 
         int nextFreeAddr = baseAddr;  // keep the base address around
         Scanner sc = null;
         try {  // open up a new scanner on the source file
             sc = new Scanner(new BufferedReader(new FileReader(file)));
         } catch (Exception e) {
             errorAndExit("ERROR: " + e.getMessage());
         }
                                                                           
         // ############## BEGIN PARSE HEADER ##################
         // a Header object's purpose is to parse the header of the SXX
         // program and hold useful information
         Header header = new Header(sc); 
         // parse the header and give us back the scanner where the header ends
         sc = header.parseHeader();
         // the header will tell us if we're using old style opcode numbering
         oldStyle = header.oldStyle;
 
         if (0 > header.length || // make sure the length is in range
                 header.length > mem.memorySize-baseAddr) {
             System.err.println("Illegal length: Out of range");
             System.err.println("  " + header.length);
             System.exit(1);
         }
         if (DEBUG == 1) { System.out.println("Length: " + header.length); }
         // ################ END PARSE HEADER #################
         
         // ############ BEGIN INSERTING OPCODES ##############
         String opcode = null;
         // keep track of how many words have been inserted into memory
         int words = 0; 
         int currFreeAddr;
         // insert opcodes until you reach the relocation section
         while (sc.hasNextLine() && words <= header.length) {
             // read the next opcode and trim off whitespace
             opcode = sc.nextLine().trim();
 
             if (isCommentOrBlankLine(opcode)) {
                 continue;
             } else if (opcode.charAt(0) == '%') {
                 break;  // move onto the relocation process
             } else {
                 // keep current address around so we can tell after how many
                 // memory cells we consumed
                 currFreeAddr = nextFreeAddr;
                 // insert the opcode/data into the stack machine
                 // or skip over memory cells if the opcode begins with `:'
                 nextFreeAddr = insertOpcode(nextFreeAddr, opcode);
                 // update the number of words that have been inserted into
                 // memory
                 words += nextFreeAddr - currFreeAddr;
             }
         } 
         // ############### END INSERTING OPCODES ################
 
         // ############# BEGIN RELOCATION PROCESS ###############
         while (sc.hasNextLine()) {  
             opcode = sc.nextLine().trim();
 
             if (isCommentOrBlankLine(opcode)) {
                 continue;
             } else if (opcode.charAt(0) == '%') {
                 break;  // we're done reading the SXX program
             } else{
                 insertRelocation(baseAddr, opcode);
             }
         } // ############## END RELOCATION PROCESS ################
 
         try { }
         finally {  // close the scanner
             if (sc != null)
                 sc.close();
         }
         return baseAddr + header.entry;  // actual entry point into memory
     }
     
     public static int insertOpcode(int nextFreeAddr, String instr) {
         // inserts the opcode of the next instruction or skips over a number of
         // memory cells if a line staring with `:' is encountered
                                                                                 
         String number = "^-?(\\d)+$";
         String colonInstruction = "^:(\\d)+$";
                                                                                 
         if (Pattern.matches(number, instr)) {
             // we now know that we have a digit
             if (DEBUG == 1) { System.out.println("mem["+nextFreeAddr+"]="+instr); }
             mem.putContents(nextFreeAddr, Integer.parseInt(instr));
             nextFreeAddr++;
         } else if (Pattern.matches(colonInstruction, instr)) {
             if (DEBUG == 1) { System.out.println("BSS "+instr.split(":")[1]); }
             nextFreeAddr += Integer.parseInt(instr.split(":")[1]);
         } else {
             System.err.println("ERROR: Not a valid line:");
             System.err.println("  " + instr);
             System.exit(1);
         }
 
         return nextFreeAddr;
     }
 
     public static void insertRelocation(int baseAddr, String address) {
         // insert into the current memory address the following:
         //     *(baseAddr+addresss) + baseAddr
         
         String number = "^-?(\\d)+$";
         if (! Pattern.matches(number, address)) {
             System.err.println("ERROR: Not a relocation address:");
             System.err.println("  " + address);
             System.exit(1);
         } else {
             int addr = Integer.parseInt(address) + baseAddr;
             if (DEBUG == 1) { System.out.println("relocating memory at " + addr); }
             mem.putContents(addr, mem.getContents(addr)+baseAddr);
         }
     }
 
     /**
      * Execute the opcode pointed at by PC.
      *
      * @param PC the program counter
      *
      */
     public static int executeOpcode(int PC) {
         int opcode = mem.getContents(PC);
         PC++;  // increment PC immediately
         if (oldStyle == true && opcode > 22) {
             opcode++; // support legacy opcode numbering convention
         }
         
         // values to be used in opcode computations
         int addr, value, num, temp; 
         addr = value = num = 0;
 
         if (opcodeRequiresParameter(opcode)) {
             // if it's an instruction that needs an `addr' or `value'
             // parameter, save that argument
             addr = value = mem.getContents(PC);
             PC++;  // increment PC again
         }
 
         switch(opcode) {  // find out the opcode and execute it
             case BKPT:   // 0
                 /* unconditionally enter the sxx debugger */
                 System.err.println("BKPT not implemented");
                 System.exit(1);
                 break;
             case PUSH:   // 1
                 /* push(*addr); */
                 if (DEBUG == 1) { System.out.println("PUSH " + addr); }
                 mem.push(mem.getContents(addr));
                 break;
             case PUSHV:  // 2
                 /* push(value); */
                 if (DEBUG == 1) { System.out.println("PUSHV " + value); }
                 mem.push(value);
                 break;
             case PUSHS:  // 3
                 /* push(*pop()); */
                 if (DEBUG == 1) { System.out.println("PUSHS"); }
                 num = mem.pop();
                 mem.push(mem.getContents(num));
                 break;
             case PUSHX:  // 4
                 /* push(*(pop()+addr)); */
                 if (DEBUG == 1) { System.out.println("PUSHX " + addr); }
                 num = mem.pop()+addr;
                 mem.push(mem.getContents(num));
                 break;
             case POP:    // 5
                 /* *addr=pop(); */
                 if (DEBUG == 1) { System.out.println("POP " + addr); }
                 temp = mem.pop();
                 mem.putContents(addr, temp);
                 break;
             case POPS:   // 6
                 /* temp=pop(); *pop()=temp; */
                 if (DEBUG == 1) { System.out.println("POPS"); }
                 temp = mem.pop();
                 num = mem.pop();
                 mem.putContents(num, temp);
                 break;
             case POPX:   // 7
                 /* temp=pop(); *(pop()+addr)=temp; */
                 if (DEBUG == 1) { System.out.println("POPX " + addr); }
                 temp = mem.pop();
                 num = mem.pop()+addr;
                 mem.putContents(num, temp);
                 break;
             case DUPL:   // 8
                 /* push(*SP); */
                 if (DEBUG == 1) { System.out.println("DUPL"); }
                 mem.push(mem.getContents(mem.getSP()));
                 break;
             case SWAP:   // 9
                 /* temp=*SP; *SP=*(SP+1); *(SP+1)=temp; */
                 if (DEBUG == 1) { System.out.println("SWAP"); }
                 temp = mem.getContents(mem.getSP());
                 mem.putContents(mem.getSP(), mem.getContents(mem.getSP()+1));
                 mem.putContents(mem.getSP()+1, temp);
                 break;
             case OVER:   // 10
                 /* push(*(SP+1)); */
                 if (DEBUG == 1) { System.out.println("OVER"); }
                 mem.push(mem.getContents(mem.getSP()+1));
                 break;
             case DROP:   // 11
                 /* SP++; */
                 if (DEBUG == 1) { System.out.println("DROP"); }
                 mem.setSP(mem.getSP()+1);
                 //mem.getSP()++;
                 break;
             case ROT:    // 12
                 /* temp=*SP; *SP=*(SP+2); *(SP+2)=*(SP+1); *(SP+1)=temp; */
                 if (DEBUG == 1) { System.out.println("ROT"); }
                 temp = mem.getContents(mem.getSP());
                 mem.putContents(mem.getSP(), mem.getContents(mem.getSP()+2));
                 mem.putContents(mem.getSP()+2, mem.getContents(mem.getSP()+1));
                 mem.putContents(mem.getSP()+1, temp);
                 break;
             case TSTLT:  // 13
                 /* TSTLT       --> temp=pop(); push((temp<0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTLT"); }
                 temp = mem.pop();
                 mem.push( (temp < 0) ? 1 : 0 );
                 break;
             case TSTLE:  // 14
                 /* TSTLE       --> temp=pop(); push((temp<=0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTLE"); }
                 temp = mem.pop();
                 mem.push( (temp <= 0) ? 1 : 0 );
                 break;
             case TSTGT:  // 15
                 /* temp=pop(); push((temp>0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTGT"); }
                 temp = mem.pop();
                 mem.push( (temp > 0) ? 1 : 0 );
                 break;
             case TSTGE:  // 16
                 /* temp=pop(); push((temp>=0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTGE"); }
                 temp = mem.pop();
                 mem.push( (temp >= 0) ? 1 : 0 );
                 break;
             case TSTEQ:  // 17
                 /* temp=pop(); push((temp==0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTEQ"); }
                 temp = mem.pop();
                 mem.push( (temp == 0) ? 1 : 0);
                 break;
             case TSTNE:  // 18
                 /* temp=pop(); push((temp!=0)?1:0); */
                 if (DEBUG == 1) { System.out.println("TSTNE"); }
                 temp = mem.pop();
                 mem.push( (temp != 0) ? 1 : 0 );
                 break;
             case BNE:    // 19
                 /* if (pop()!=0) PC=addr; */
                 if (DEBUG == 1) { System.out.println("BNE " + addr); }
                 if (mem.pop() != 0) {
                     PC = addr;
                 }
                 break;
             case BEQ:    // 20
                 /* if (pop()==0) PC=addr; */
                 if (DEBUG == 1) { System.out.println("BEQ " + addr); }
                 if (mem.pop() == 0) {
                     PC = addr;
                 }
                 break;
             case BR:     // 21
                 /* PC=addr; */
                 if (DEBUG == 1) { System.out.println("BR " + addr); }
                 PC = addr;
                 break;
             case CALL:   // 22
                 /* push(PC); PC=addr; */
                 if (DEBUG == 1) { System.out.println("CALL " + addr); }
                 mem.push(PC);
                 PC = addr;
                 break;
             case CALLS:  // 23
                 /* temp=pop(); push(PC); PC=temp; */
                 if (DEBUG == 1) { System.out.println("CALLS"); }
                 temp = mem.pop();
                 mem.push(PC);
                 PC = temp;
                 break;
             case RETURN: // 24
                 /* PC=pop(); */
                 if (DEBUG == 1) { System.out.println("RETURN"); }
                 PC = mem.pop();
                 break;
             case RETN:   // 25
                 /* temp=pop(); SP += value; PC=temp; */
                 if (DEBUG == 1) { System.out.println("RETN " + value); }
                 temp = mem.pop();
                 //if (DEBUG == 1) { System.out.println("RETN:temp = " + temp); }
                 mem.setSP(mem.getSP()+value);
                 //mem.getSP() += value;
                 PC = temp;
                 //if (DEBUG == 1) { printDebug(opcode, length, PC, temp); }
                 break;
             case HALT:   // 26
                 /* halt program execution */
                 if (DEBUG == 1) { System.out.println("HALT"); }
                 System.exit(0);
                 break;
             case ADD:    // 27
                 /* temp=pop(); push( pop() + temp ); */
                 if (DEBUG == 1) { System.out.println("ADD"); }
                 temp = mem.pop();
                 mem.push(mem.pop()+temp);
                 break;
             case SUB:    // 28
                 /* temp=pop(); push( pop() - temp ); */
                 if (DEBUG == 1) { System.out.println("SUB"); }
                 temp = mem.pop();
                 mem.push(mem.pop()-temp);
                 break;
             case MUL:    // 29
                 /* temp=pop(); push( pop() * temp ); */
                 if (DEBUG == 1) { System.out.println("MUL"); }
                 temp = mem.pop();
                 mem.push(mem.pop() * temp);
                 break;
             case DIV:    // 30
                 /* temp=pop(); push( pop() / temp ); */
                 if (DEBUG == 1) { System.out.println("DIV"); }
                 temp = mem.pop();
                 try {
                     mem.push(mem.pop() / temp);
                 } catch (ArithmeticException e) {
                     errorAndExit("ERROR: Attempt to divide by zero");
                 }
                 break;
             case MOD:    // 31
                 /* temp=pop(); push( pop() % temp ); */
                 if (DEBUG == 1) { System.out.println("DIV"); }
                 temp = mem.pop();
                 try {
                     mem.push(mem.pop() % temp);
                 } catch (ArithmeticException e) {
                     errorAndExit("ERROR: Attempt to mod by zero");
                 }
                 break;
             case OR:     // 32
                 /* temp=pop(); push( pop() || temp ); */
                 if (DEBUG == 1) { System.out.println("OR"); }
                 temp = mem.pop();
                 mem.push( (mem.pop() != 0 || temp != 0) ? 1 : 0 );
                 break;
             case AND:    // 33
                 /* temp=pop(); push( pop() && temp ); */
                 if (DEBUG == 1) { System.out.println("AND"); }
                 temp = mem.pop();
                 mem.push( (mem.pop() != 0 && temp != 0) ? 1 : 0 );
                 break;
             case XOR:    // 34
                 /* temp=pop(); push( pop() xor temp ); [see below] */
                 if (DEBUG == 1) { System.out.println("XOR"); }
                 temp = mem.pop();
                 mem.push( (mem.pop() != 0 ^ temp != 0) ? 1 : 0 );
                 break;
             case NOT:    // 35
                 /* push( !pop() ); */
                 if (DEBUG == 1) { System.out.println("NOT"); }
                 mem.push( !(mem.pop() != 0) ? 1 : 0 );
                 break;
             case NEG:    // 36
                 /* push( -pop() ); */
                 if (DEBUG == 1) { System.out.println("NEG"); }
                 mem.push( (-1)*mem.pop());
                 break;
             case ADDX:   // 37
                 /* push( pop()+addr ); */
                 if (DEBUG == 1) { System.out.println("ADDX " + addr); }
                 mem.push(mem.pop() + addr);
                 break;
             case ADDSP:  // 38
                 /* SP += value; */
                 if (DEBUG == 1) { System.out.println("ADDSP " + value); }
                 mem.setSP(mem.getSP()+value);
                 //mem.getSP() += value;
                 break;
             case READ:   // 39
                 temp = Read.READ(in);
                 if (DEBUG == 1) { System.out.println("READ ["+temp+"]"); }
                 mem.push(temp);
                 break;
             case PRINT:  // 40
                 /* print pop() in %d format */
                 temp = mem.pop();
                 if (DEBUG == 1) { System.out.println("PRINT ["+temp+"]"); }
                 System.out.print(temp);
                 break;
             case READC:  // 41
                 /* read temp in %c format; push(temp); */
                 temp = Read.READC(in);
                 if (DEBUG == 1) { System.out.println("READC ["+temp+"]"); }
                 mem.push(temp);
                 break;
             case PRINTC: // 42
                 /* print pop() in %c format */
                 temp = mem.pop();
                 if (DEBUG == 1) { System.out.println("PRINTC ["+temp+"]"); }
                 System.out.print((char)temp);
                 break;
             case TRON:   // 43
                 /* turn on trace feature */
                 if (DEBUG == 1) { System.out.println("TRON"); }
                 TRACE = true;
                 break;
             case TROFF:  // 44
                 /* turn off trace feature */
                 if (DEBUG == 1) { System.out.println("TROFF"); }
                 TRACE = false;
                 break;
             case DUMP:   // 45
                 /* temp=pop(); dump memory from pop() to temp; */
                 if (DEBUG == 1) { System.out.println("DUMP"); }
                 temp = mem.pop();
                 dump(mem.pop(), temp);
                 break;
             default:
                 System.err.println("ERROR: Invalid opcode");
                 System.err.println("  " + opcode);
                 System.exit(1);
         }
 
         return PC; // return the new PC
     }
 
     /**
      * Dump memory from pop to temp.
      *
      * @param pop  beginning address
      * @param temp ending address
      *
      */
     public static void dump(int pop, int temp) {
         if (pop < temp || 0 > pop || pop > mem.memorySize-1
                 || 0 > temp || temp > mem.memorySize-1) {
             errorAndExit("ERROR: Illegal dump range");
         }
         for (; pop >= temp; pop--)
             System.out.println(mem.getContents(pop));
     }
 
     /**
      * Returns true if opcode requires a parameter.
      *
      * @ param opcode the opcode
      *
      */
     public static boolean opcodeRequiresParameter(int opcode) {
         // informs the caller whether the current opcode needs to look at the
         // next location in memory for an argument to the instruction
         boolean requiresParameter;
 
         switch (opcode) {
             case PUSH:
             case PUSHV:
             case PUSHX:
             case POP:
             case POPX:
             case BNE:
             case BEQ:
             case BR:
             case CALL:
             case RETN:
             case ADDX:
             case ADDSP:
                 requiresParameter = true;
                 break;
             default:
                 requiresParameter = false;
                 break;
         }
 
         return requiresParameter;
     }
 
     private static void printInstructions(int length, int PC) {
         for (int pointer = 16+length-1; pointer >= 16; pointer--)
             System.out.format("%2d| %2s%s\n", pointer, mem.getContents(pointer), 
                     (pointer == PC) ? " <-- PC" : "");
         System.out.println();
     }
 
     private static boolean isCommentOrBlankLine(String line) {
         if (line.isEmpty() || line.charAt(0) == '#')
             return true;
         else
             return false;
     }
 
     private static void errorAndExit(String error) {
         System.err.println(error);
         System.exit(1);
     }
 
     private static void printDebug(int opcode, int length, int PC, int temp) {
         System.out.println("About to execute opcode: " + opcode);
         mem.reveal();
         System.out.println("\nInstructions:");
         printInstructions(length, PC-1);
         System.out.format("PC: %d  temp: %d%n%n", PC-1, temp); 
     }
 
 
     /* mneumonics and their opcodes */
     public static final int BKPT   =   0;
     public static final int PUSH   =   1;
     public static final int PUSHV  =   2;
     public static final int PUSHS  =   3;
     public static final int INDIR  =   3;
     public static final int PUSHX  =   4;
     public static final int POP    =   5;
     public static final int POPS   =   6;
     public static final int POPX   =   7;
     public static final int DUPL   =   8;
     public static final int SWAP   =   9;
     public static final int OVER   =  10;
     public static final int DROP   =  11;
     public static final int ROT    =  12;
     public static final int TSTLT  =  13;
     public static final int TSTLE  =  14;
     public static final int TSTGT  =  15;
     public static final int TSTGE  =  16;
     public static final int TSTEQ  =  17;
     public static final int TSTNE  =  18;
     public static final int BNE    =  19;
     public static final int BT     =  19;
     public static final int BEQ    =  20;
     public static final int BF     =  20;
     public static final int BR     =  21;
     public static final int CALL   =  22;
     public static final int CALLS  =  23;
     public static final int RETURN =  24;
     public static final int POPPC  =  24;
     public static final int RETN   =  25;
     public static final int HALT   =  26;
     public static final int ADD    =  27;
     public static final int SUB    =  28;
     public static final int MUL    =  29;
     public static final int DIV    =  30;
     public static final int MOD    =  31;
     public static final int OR     =  32;
     public static final int AND    =  33;
     public static final int XOR    =  34;
     public static final int NOT    =  35;
     public static final int NEG    =  36;
     public static final int ADDX   =  37;
     public static final int ADDSP  =  38;
     public static final int READ   =  39;
     public static final int PRINT  =  40;
     public static final int READC  =  41;
     public static final int PRINTC =  42;
     public static final int TRON   =  43;
     public static final int TROFF  =  44;
     public static final int DUMP   =  45;
 
 
     public static void main(String[] args) throws IOException {
         String file = args[0];
         new StackMachine().run(file);
     }

 }
