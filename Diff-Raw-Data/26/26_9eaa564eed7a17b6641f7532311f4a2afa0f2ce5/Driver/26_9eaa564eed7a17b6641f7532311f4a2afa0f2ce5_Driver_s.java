 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 public class Driver {
 
     /* this variable `stack' can never be assigned to anything else */
     public static final Stack stack = new Stack(50);  
     /* assume for now that opcodes start at position 16 */
     public static int pc = 16;
     /* instrCount increments after every instruction is parsed, so we know where to put
      * the opcode--starts at the first instruction */
     public static int instrCount = pc;
     /* values used to help compute the values of instructions */
     public static int addr, value, temp, t1, t2;
     public static boolean TRACE;
     public static Scanner in = null;
 
     public static void main(String[] args) throws IOException {
 
         Scanner sc = null;
         String instruction = null;
 
         for (int i = 25; i <= 45; i++)
             stack.push(i);
 
         try {
             sc = new Scanner(new BufferedReader(new FileReader("test.esm")));
             in = new Scanner(System.in);
 
             while (sc.hasNextLine()) {
                 // grab the next instruction
                 instruction = sc.nextLine();
                 /* esm only supports entire commented lines at this point */
                 if (Pattern.matches("^;.*", instruction)) {
                     continue;
                 }
                 System.out.println(instruction);
                 // execute the next instruction
                 executeInstruction(instruction);
             }
         } finally {
             sc.close();
         }
 
        System.out.println(stack);
        System.out.println("SP: " + stack.SP);
     }
 
     public static void executeInstruction(String instr) {
         /* executes the current instruction in the provided program */
 
         instr = instr.trim();  /* remove leading & trailing whitespace from
                                   current instruction */
         if (instr.equals("BKPT")) {  /* 0 */
             /* BKPT        --> unconditionally enter the sxx debugger */
             pass(instr);
         } else if (Pattern.matches("PUSH (\\d+)", instr)) {  /* 1 */
             /* PUSH addr   --> push(*addr); */
             addr = getNum(instr);
             stack.push(stack.getContents(addr));
         } else if (Pattern.matches("PUSHV (\\d+)", instr)) {  /* 2 */
             /* PUSHV value --> push(value); */
             value = getNum(instr);
             stack.push(value);
         } else if (instr.equals("PUSHS")) {  /* 3 */
             /* PUSHS       --> push(*pop()); */
             stack.push(stack.getContents(stack.pop()));
         } else if (instr.equals("INDIR")) {  /* 3 */
             /* INDIR       --> is a synonym for PUSHS */
             stack.push(stack.getContents(stack.pop()));
         } else if (Pattern.matches("PUSHX (\\d+)", instr)) {  /* 4 */
             /* PUSHX addr  --> push(*(pop()+addr)); */
             addr = getNum(instr);
             stack.push(stack.getContents(stack.pop()+addr));
         } else if (Pattern.matches("POP (\\d+)", instr)) {  /* 5 */
             /* POP addr    --> *addr=pop(); */
             addr = getNum(instr);
             stack.putContents(addr, stack.pop());
         } else if (instr.equals("POPS")) {  /* 6 */
             /* POPS        --> temp=pop(); *pop()=temp; */
             temp = stack.pop();
             stack.putContents(stack.pop(), temp);
         } else if (Pattern.matches("POPX (\\d+)", instr)) {  /* 7 */
             /* POPX addr   --> temp=pop(); *(pop()+addr)=temp; */
             temp = stack.pop();
             addr = getNum(instr);
             stack.putContents(stack.pop()+addr, temp);
         } else if (instr.equals("DUPL")) {  /* 8 */
             /* DUPL        --> push(*SP); */
             stack.push(stack.getContents(stack.SP));
         } else if (instr.equals("SWAP")) {  /* 9 */
             /* SWAP        --> temp=*SP; *SP=*(SP+1); *(SP+1)=temp; */
             temp = stack.getContents(stack.SP);
             stack.putContents(stack.SP, stack.getContents(stack.SP+1));
             stack.putContents(stack.SP+1, temp);
         } else if (instr.equals("OVER")) {  /* 10 */
             /* OVER        --> push(*(SP+1)); */
             stack.push(stack.getContents(stack.SP+1));
         } else if (instr.equals("DROP")) {  /* 11 */
             /* DROP        --> SP++; */
             stack.SP++;
         } else if (instr.equals("ROT")) {  /* 12 */
             /* ROT         --> temp=*SP; *SP=*(SP+2); *(SP+2)=*(SP+1);
                                 *(SP+1)=temp; */
             temp = stack.getContents(stack.SP);
             stack.putContents(stack.SP, stack.getContents(stack.SP+2));
             stack.putContents(stack.SP+2, stack.getContents(stack.SP+1));
             stack.putContents(stack.SP+1, temp);
         } else if (instr.equals("TSTLT")) {  /* 13 */
             /* TSTLT       --> temp=pop(); push((temp<0)?1:0); */
             temp = stack.pop();
             stack.push( (temp < 0) ? 1 : 0 );
         } else if (instr.equals("TSTLE")) {  /* 14 */
             /* TSTLE       --> temp=pop(); push((temp<=0)?1:0); */
             temp = stack.pop();
             stack.push( (temp <= 0) ? 1 : 0 );
         } else if (instr.equals("TSTGT")) {  /* 15 */
             /* TSTGT       --> temp=pop(); push((temp>0)?1:0); */
             temp = stack.pop();
             stack.push( (temp > 0) ? 1 : 0 );
         } else if (instr.equals("TSTGE")) {  /* 16 */
             /* TSTGE       --> temp=pop(); push((temp>=0)?1:0); */
             temp = stack.pop();
             stack.push( (temp >= 0) ? 1 : 0 );
         } else if (instr.equals("TSTEQ")) {  /* 17 */
             /* TSTEQ       --> temp=pop(); push((temp==0)?1:0); */
             temp = stack.pop();
             stack.push( (temp == 0) ? 1 : 0);
         } else if (instr.equals("TSTNE")) {  /* 18 */
             /* TSTNE       --> temp=pop(); push((temp!=0)?1:0); */
             temp = stack.pop();
             stack.push( (temp != 0) ? 1 : 0 );
         } else if (Pattern.matches("BNE (\\d+)", instr)) {  /* 19 */
             /* BNE addr    --> if (pop()!=0) PC=addr; */
             addr = getNum(instr);
             if (stack.pop() != 0)
                 stack.PC = addr;
         } else if (Pattern.matches("BT (\\d+)", instr)) {  /* 19 */
             /* BT addr     --> is a synonym for BNE */
             addr = getNum(instr);
             if (stack.pop() != 0)
                 stack.PC = addr;
         } else if (Pattern.matches("BEQ (\\d+)", instr)) {  /* 20 */
             /* BEQ addr    --> if (pop()==0) PC=addr; */
             addr = getNum(instr);
             if (stack.pop() == 0)
                 stack.PC = addr;
         } else if (Pattern.matches("BF (\\d+)", instr)) {  /* 20 */
             /* BR addr     --> is a synonym for BEQ */
             addr = getNum(instr);
             if (stack.pop() == 0)
                 stack.PC = addr;
         } else if (Pattern.matches("BR (\\d+)", instr)) {  /* 21 */
             /* BR addr     --> PC=addr; */
             addr = getNum(instr);
             stack.PC = addr;
         } else if (Pattern.matches("CALL (\\d+)", instr)) {  /* 22 */
             /* CALL addr   --> push(PC); PC=addr; */
             stack.push(stack.PC);
             addr = getNum(instr);
             stack.PC = addr;
         } else if (instr.equals("CALLS")) {  /* 23 */
             /* CALLS       --> temp=pop(); push(PC); PC=temp; */
             temp = stack.pop();
             stack.push(stack.PC);
             stack.PC = temp;
         } else if (instr.equals("RETURN")) {  /* 24 */
             /* RETURN      --> PC=pop(); */
             stack.PC = stack.pop();
         } else if (instr.equals("POPPC")) {  /* 24 */
             /* POPPC       --> is a synonym for RETURN */
             stack.PC = stack.pop();
         } else if (Pattern.matches("RETN (\\d+)", instr)) {  /* 25 */
             /* RETN value  --> temp=pop(); SP += value; PC=temp; */
             temp = stack.pop();
             value = getNum(instr);
             stack.SP += value;
             stack.PC = temp;
         } else if (instr.equals("HALT")) {  /* 26 */
             /* HALT        --> halt program execution */
             System.out.println("Halting program execution");
             System.exit(0);
         } else if (instr.equals("ADD")) {  /* 27 */
             /* ADD         --> temp=pop(); push( pop() + temp ); */
             temp = stack.pop();
             stack.push(stack.pop()+temp);
         } else if (instr.equals("SUB")) {  /* 28 */
             /* SUB         --> temp=pop(); push( pop() - temp ); */
             temp = stack.pop();
             stack.push(stack.pop()-temp);
         } else if (instr.equals("MUL")) {  /* 29 */
             /* MUL         --> temp=pop(); push( pop() * temp ); */
             temp = stack.pop();
             stack.push(stack.pop() * temp);
         } else if (instr.equals("DIV")) {  /* 30 */
             /* DIV         --> temp=pop(); push( pop() / temp ); */
             temp = stack.pop();
             stack.push(stack.pop() / temp);
         } else if (instr.equals("MOD")) {  /* 31 */
             /* MOD         --> temp=pop(); push( pop() % temp ); */
             temp = stack.pop();
             stack.push(stack.pop() % temp);
         } else if (instr.equals("OR")) {  /* 32 */
             /* OR          --> temp=pop(); push( pop() || temp ); */
             temp = stack.pop();
             stack.push( (stack.pop() != 0 || temp != 0) ? 1 : 0 );
         } else if (instr.equals("AND")) {  /* 33 */
             /* AND         --> temp=pop(); push( pop() && temp ); */
             temp = stack.pop();
             stack.push( (stack.pop() != 0 && temp != 0) ? 1 : 0 );
         } else if (instr.equals("XOR")) {  /* 34 */
             /* XOR         --> temp=pop(); push( pop() xor temp ); [see below] */
             t1 = stack.pop();
             t2 = stack.pop();
             stack.push( (!(t1 != 0 && t2 != 0)
                     && (t1 != 0 || t2 != 0)) ? 1 : 0 );
         } else if (instr.equals("NOT")) {  /* 35 */
             /* NOT         --> push( !pop() ); */
             stack.push( !(stack.pop() != 0) ? 1 : 0 );
         } else if (instr.equals("NEG")) {  /* 36 */
             /* NEG         --> push( -pop() ); */
             stack.push( -1*stack.pop());
         } else if (Pattern.matches("ADDX (\\d+)", instr)) {  /* 37 */
             /* ADDX addr   --> push( pop()+addr ); */
             addr = getNum(instr);
             stack.push(stack.pop() + addr);
         } else if (Pattern.matches("ADDSP (\\d+)", instr)) {  /* 38 */
             /* ADDSP value --> SP += value; */
             value = getNum(instr);
             stack.SP += value;
         } else if (instr.equals("READ")) {  /* 39 */
             /* READ        --> read temp in %d format; push(temp); */
             temp = in.nextInt();
         } else if (instr.equals("PRINT")) {  /* 40 */
             /* PRINT       --> print pop() in %d format */
             System.out.println(stack.pop());
         } else if (instr.equals("READC")) {  /* 41 */
             /* READC       --> read temp in %c format; push(temp); */
             temp = in.nextLine().charAt(0);
             stack.push(temp);
         } else if (instr.equals("PRINTC")) {  /* 42 */
             /* PRINTC      --> print pop() in %c format */
             System.out.println((char)stack.pop());
         } else if (instr.equals("TRON")) {  /* 43 */
             /* TRON        --> turn on trace feature */
             TRACE = true;
         } else if (instr.equals("TROFF")) {  /* 44 */
             /* TROFF       --> turn off trace feature */
             TRACE = false;
         } else if (instr.equals("DUMP")) {  /* 45 */
             /* DUMP        --> temp=pop(); dump memory from pop() to temp; */
             temp = stack.pop();
             dump(stack.pop(), temp);
         }
     }
 
     public static void pass(String instr) {
         System.out.println("/\\/" + instr + "\\/\\");
     }
 
     public static int getNum(String instr) {
         return Integer.parseInt(instr.split(" ")[1]);
     }
 
     public static void dump(int pop, int temp) {
         for (; pop >= temp; pop--)
             System.out.println(stack.getContents(pop));
     }
 }
