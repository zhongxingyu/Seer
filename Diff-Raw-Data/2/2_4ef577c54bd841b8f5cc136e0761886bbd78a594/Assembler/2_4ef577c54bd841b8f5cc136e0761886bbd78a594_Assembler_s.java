 //  2009 Spring CSE 141 Project #1
 //  Assembler Framework
 //  Written by Hung-Wei Tseng
 
 import java.io.*;
 import java.util.*;
 
 class MemoryEntry {
   String data;
   int address;
 }
 
 class Memory {
   int size, j;
   MemoryEntry[] entries;
 
   Memory() {
     entries = new MemoryEntry[1024];
     for (int j = 0; j < 1024; j++)
       entries[j] = new MemoryEntry();
     size = 0;
   }
 
   Memory(int n) {
     entries = new MemoryEntry[n];
     for (int j = 0; j < n; j++)
       entries[j] = new MemoryEntry();
     size = 0;
   }
 
   public void add(String data, int address) {
     entries[size].data = data;
     entries[size++].address = address;
   }
 
   public String find(int address) {
     for (j = 0; j < size; j++)
       if (address == entries[j].address)
         return entries[j].data;
     return null;
   }
 
   public void print() {
     for (j = 0; j < size; j++)
       System.out.println(Integer.toHexString(entries[j].address) + "\t"
           + entries[j].data);
   }
 
   public String dump() {
     String output = "";
     String[] zeros = { "", "0", "00", "000", "0000", "00000", "000000",
         "0000000", "00000000", "000000000" };
     for (j = 0; j < size; j++) {
       String tempOutput = entries[j].data.substring(
           entries[j].data.lastIndexOf("0x") + 2, entries[j].data.length());
       if (j < size - 1)
         output += zeros[9 - tempOutput.length()] + tempOutput + ",\n";
       else
         output += zeros[9 - tempOutput.length()] + tempOutput;
     }
     return output;
   }
 
   public int leng() {
     return size;
   }
 
 }
 
 class Operand {
   public String name;
   public int offset;
 
   Operand() {
     name = "";
     offset = 0;
   }
 
   Operand(String i_name, int i_offset) {
     name = i_name;
     offset = i_offset;
   }
 
   public int extractRegisterNumber() {
     if (name.startsWith("$"))
       return Integer.valueOf(
           name.substring(name.lastIndexOf("$") + 1, name.length())).intValue();
     else
       return -1;
   }
 
   public long extractImmediate() {
     // if(name.startsWith("0x"))
     // return
     // Integer.valueOf(name.substring(name.lastIndexOf("0x")+2,name.length()),16).intValue();
     if (name.startsWith("0x"))
       return Long.valueOf(
           name.substring(name.lastIndexOf("0x") + 2, name.length()), 16)
           .longValue();
     else
       return Integer.MIN_VALUE;
   }
 
   public String getOperandType() {
     if (name.startsWith("$")) {
       return "register";
     } else if (name.startsWith("0x")) {
       return "immediate";
     } else {
       return "label";
     }
   }
 }
 
 class Instruction {
   public String operator;
   public Operand operands[];
 
   Instruction(String i_operator, Operand i_operands[]) {
     operator = i_operator;
     operands = new Operand[i_operands.length];
     for (int i = 0; i < i_operands.length; i++) {
       operands[i] = new Operand();
       operands[i].name = i_operands[i].name;
       operands[i].offset = i_operands[i].offset;
     }
   }
 
   Instruction(String sourceCodeLine) {
     StringTokenizer st = new StringTokenizer(sourceCodeLine, " ,\t");
     int numberOfTokens = st.countTokens();
     if (numberOfTokens > 0) // The first argument is operator
     {
       operator = st.nextToken();
       numberOfTokens--;
       operands = new Operand[numberOfTokens];
       for (int i = 0; i < numberOfTokens; i++) {
         operands[i] = new Operand();
         operands[i].name = st.nextToken();
         if (operands[i].name.lastIndexOf("(") >= 0) {
           operands[i].offset = Integer.valueOf(
               operands[i].name.substring(0, operands[i].name.lastIndexOf("(")))
               .intValue();
           operands[i].name = operands[i].name.substring(
               operands[i].name.lastIndexOf("(") + 1,
               operands[i].name.lastIndexOf(")"));
         }
       }
     }
   }
 
   public void print() {
     String output = "";
     for (int i = 0; i < operands.length; i++) {
       output += i + ":" + operands[i].name + " " + operands[i].offset + "\t";
     }
     System.out.println(operator + "\t" + output);
   }
 
 }
 
 public abstract class Assembler {
   Assembler() {
   }
 
   Assembler(String[] args) throws IOException {
     sourceFile = new BufferedReader(new FileReader(args[0]));
     out_code = new BufferedWriter(new FileWriter(args[1] + "_i.coe"));
     out_data = new BufferedWriter(new FileWriter(args[1] + "_d.coe"));
   }
 
   public BufferedReader sourceFile;
   public BufferedWriter out_code, out_data;
   /* keywords of your asseblemly language, and of course, you may override it. */
   public String[] keywords;
   /* memory table */
   public Memory memory = new Memory();
   /* instructions */
   public Instruction instructions[] = new Instruction[1024];
   /* number of scanned instructions */
   public int instructionCount = 0;
   /* where are we now. */
   public int currentCodeSection = 0; // 0 for text, 1 for data
   /* The next program counter */
   public long programCounter = 0;
   /* The next data memory address */
   public int dataMemoryAddress = 0;
   /* The number of lines scanned */
   int currentSourceCodeline = 0;
 
   // Get the next line from input file
   public String getNextInputLine() throws IOException {
     if (sourceFile == null)
       System.out.println("The source code file handler is not initialized");
 
     if (out_code == null)
       System.out.println("The output code file handler is not initialized");
 
     if (out_data == null)
       System.out.println("The output memory file handler is not initialized");
 
     while (sourceFile.ready()) {
       currentSourceCodeline++;
       // get the next line.
       String sourceCodeLine = sourceFile.readLine().trim();
       // get rid of the comments
       if (sourceCodeLine.startsWith("//")) {
         continue;
       }
       if (sourceCodeLine.indexOf("//") != -1) {
         sourceCodeLine = sourceCodeLine.substring(0,
             sourceCodeLine.indexOf("//")).trim();
       }
       // trim the leading spaces and return the source code line.
       sourceCodeLine = sourceCodeLine.trim();
       /* remove the comments */
       if (sourceCodeLine.length() == 0) {
         continue;
       }
       return sourceCodeLine;
     }
     return null;
   }
 
   // Check if the input line contains a keyword
   boolean isKeyword(String sourceCodeLine) {
     if (sourceCodeLine.startsWith("."))
       return true;
     else
       return false;
   }
 
   // Extract the input line with the keywords stored in keywords array
   String extractKeyword(String sourceCodeLine) {
     for (int i = 0; i < keywords.length; i++) {
       if (sourceCodeLine.startsWith(keywords[i])) {
         return keywords[i];
       }
     }
     outputErrorMessage("Hey! The line does not contain any keyword!");
     return null;
   }
 
   // check if the input contains a label
   boolean isLabel(String sourceCode) {
     if (sourceCode.lastIndexOf(":") >= 0)
       return true;
     else
       return false;
   }
 
   // extract the label from a source code input
   String extractLabel(String sourceCode) {
     if (sourceCode.lastIndexOf(":") >= 0) {
       String label = sourceCode.substring(0, sourceCode.lastIndexOf(":"));
       if (label.length() != 0)
         return label;
       else
         return null;
     } else
       return null;
   }
 
   // process the instruction
   Instruction processInstruction(String sourceCode) {
     Instruction instruction = new Instruction(sourceCode);
     return instruction;
   }
 
   // process the data.
   void processData(String sourceCode) {
     if (sourceCode.startsWith(".word")) {
       StringTokenizer st = new StringTokenizer(sourceCode, " ,\t");
       int numberOfRemainingTokens = st.countTokens();
       /* Fill the words into memory */
       while (numberOfRemainingTokens > 0) {
         numberOfRemainingTokens--;
         String data = st.nextToken();
         if (data.startsWith(".word"))
           continue;
         memory.add(data, dataMemoryAddress);
         dataMemoryAddress++;
       }
     }
     /* Process the .fill keyword */
     else if (sourceCode.startsWith(".fill")) {
       StringTokenizer st = new StringTokenizer(sourceCode, " ,\t");
       int numberOfRemainingData;
       if (st.countTokens() != 3) {
         outputErrorMessage("Error: .fill should be in the form of .fill n data");
       }
       String data = st.nextToken();
       if (data.startsWith(".fill")) {
         int numberOfRemainingElements = Integer.valueOf(st.nextToken())
             .intValue();
         String dataToFill = st.nextToken();
         for (int i = 0; i < numberOfRemainingElements; i++) {
           memory.add(dataToFill, dataMemoryAddress);
           dataMemoryAddress++;
         }
       }
     }
   }
 
   // The static function returns the operand type.
   public static String getOperandType(String operand) {
     if (operand.startsWith("$")) {
       return "register";
     } else if (operand.startsWith("0x")) {
       return "immediate";
     } else {
       return "label";
     }
   }
 
   public void outputErrorMessage(String errorMessage) {
     System.out.println("Line " + currentSourceCodeline + ": " + errorMessage);
   }
 
   // You need to override it if you have new keywords!
   void processAdditionalKeywords(String sourceCode) {
     outputErrorMessage("Sorry, we don't know how to process it");
     return;
   }
 
   // The student has to implement it for processing the labels.
   abstract void processLabel(String sourceCode);
 
   /**
    * The student has to implement it for generating the machine codes. In
    * general, one instruction translates to a single line of machine code, but a
    * psuedo instruction may generate multiple lines of machine code (one string
    * with several newlines in it)
    */
   abstract String generateCode(Instruction instruction);
 
   /**
    * The student has to implement it for updating the program counter For most
    * instructions this is simply pc++, but pseudo-instructions are a bit more
    * complicated
    * 
    * If the pseudo-instruction expands to 3 real instructions, then you should
    * increment pc by 3 so that all your addresses following this instructions
    * will be correct.
    */
   abstract void updateProgramCounter(Instruction instruction);
 
   // The student has to implement it for initializing some of their own
   // variables and data structures.
   abstract void initialization() throws IOException;
 
   /**
    * The student has to implement it for replacing the labels used in
    * instructions
    */
   abstract void replaceInstructionLabel(Instruction instruction);
 
   /**
    * The student has to implement it for replacing the labels used in memory For
    * example, consider the code LineNum 10 Foo: .word 0x27
    * 
    * 17 Foo_ptr: Foo
    * 
    * Here, Foo_ptr is a label that points to Foo; Foo (on line 17) needs to be
    * replaced with its address so that Foo_ptr is actually a ptr (to Foo)
    */
   abstract void replaceMemoryLabel();
 
   /* The core of our assembler */
   public void AssembleCode(String[] arg) throws IOException {
     if (arg.length < 2) {
       System.out
           .println("Usage: java Assembler input_filename output_file_prefix ");
       return;
     }
     String keywordString = ".text .word .data .fill";
     keywords = keywordString.split(" ");
     initialization();
     // Pass 1: Scan the source code line
     String sourceCodeLine = getNextInputLine();
     while (sourceCodeLine != null) {
       if (isKeyword(sourceCodeLine)) {
         /* Extract the keyword from scanned source code */
         String keyword = extractKeyword(sourceCodeLine);
         if (keyword == null) {
           outputErrorMessage("Error! It's not a valid keyword!");
         }
         /* Change the current code section to text */
         else if (keyword.equalsIgnoreCase(".text"))
           currentCodeSection = 0;
         /* Change the current code section to data */
         else if (keyword.equalsIgnoreCase(".data"))
           currentCodeSection = 1;
         else if (keyword.equalsIgnoreCase(".word")
             || keyword.equalsIgnoreCase(".fill")) {
           processData(sourceCodeLine);
         } else {
           processAdditionalKeywords(sourceCodeLine);
         }
       } else if (isLabel(sourceCodeLine)) {
         String label = extractLabel(sourceCodeLine);
         if (label != null)
           processLabel(label);
         else
           outputErrorMessage("The input line does not contains a label");
       } else {
         instructions[instructionCount] = processInstruction(sourceCodeLine);
         updateProgramCounter(instructions[instructionCount]);
         instructionCount++;
       }
       sourceCodeLine = getNextInputLine();
     }
     // Pass 2: Replace labels and output the code and memory.
     // output code
     programCounter = 0;
     out_code
        .write("MEMORY_INITIALIZATION_RADIX=16;\nMEMORY_INITIALIZATION_VECTOR=\n");
     for (int i = 0; i < instructionCount; i++) {
       replaceInstructionLabel(instructions[i]);
       String tempOutput = generateCode(instructions[i]);
       if (i < instructionCount - 1) {
         out_code.write(tempOutput + ",\n");
       } else
         out_code.write(tempOutput);
     }
     // replace labels in data field.
     replaceMemoryLabel();
     // output the memory states.
     out_data
         .write("MEMORY_INITIALIZATION_RADIX=16;\nMEMORY_INITIALIZATION_VECTOR=\n");
     out_data.write(memory.dump());
     out_code.close();
     out_data.close();
   }
 }
