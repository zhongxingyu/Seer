 package malice;
 
 import malice.commands.ArrayDeclarationCommand;
 import malice.commands.ConditionalCommand;
 import malice.commands.FunctionCallCommand;
 import malice.commands.InputCommand;
 import malice.commands.ThroughCommand;
 import malice.commands.WhileNotCommand;
 import malice.symbols.SymbolTable;
 import malice.symbols.Register;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import malice.commands.Command;
 import malice.commands.CommandVisitor;
 import malice.commands.ConditionalBranch;
 import malice.commands.DecrementCommand;
 import malice.commands.FunctionReturnCommand;
 import malice.commands.IncrementCommand;
 import malice.commands.SpeakCommand;
 import malice.commands.VariableAssignmentCommand;
 import malice.commands.VariableDeclarationCommand;
 import malice.expressions.ArithmeticExpression;
 import malice.expressions.BooleanExpression;
 import malice.expressions.CharacterExpression;
 import malice.expressions.Expression;
 import malice.expressions.StringExpression;
 import malice.functions.LookingGlassFunction;
 import malice.functions.Parameter;
 import malice.functions.RoomFunction;
 import malice.symbols.MemoryLocation;
 import malice.symbols.Storage;
 import malice.symbols.Type;
 
 public class CodeGenerator implements CommandVisitor {
 
     private List<Command> commands;
     private List<RoomFunction> rooms;
     private List<LookingGlassFunction> lookingGlasses;
     private SymbolTable symbolTable;
     private List<String> assemblyCommands;
     private Queue<Register> freeRegisters;
     private Set<String> freeMemoryLocationVariables;
     private Set<String> memoryLocationVariables;
     private int nextFreeMemoryAddress;
     private int nextComparisonNumber;
     private int nextConditionalLabel;
     private int nextStringName;
     private int nextBoolComparisonNumber;
     private int nextWhileNotNumber;
     private Map<String, String> stringNames;
     private String scope;
 
     public CodeGenerator(List<Command> commands, List<RoomFunction> rooms,
             List<LookingGlassFunction> lookingGlasses, SymbolTable symbolTable) {
         this.commands = commands;
         this.rooms = rooms;
         this.lookingGlasses = lookingGlasses;
         this.symbolTable = symbolTable;
 
         assemblyCommands = new ArrayList<String>();
         freeRegisters = new LinkedList<Register>();
         freeRegisters.addAll(Arrays.asList(Register.values()));
         freeMemoryLocationVariables = new HashSet<String>();
         memoryLocationVariables = new HashSet<String>();
         nextFreeMemoryAddress = 0;
         nextComparisonNumber = 0;
         nextConditionalLabel = 0;
         nextStringName = 0;
         nextBoolComparisonNumber = 0;
         nextWhileNotNumber = 0;
         stringNames = new HashMap<String, String>();
         scope = "";
     }
 
     public List<String> generateCode() {
         assemblyCommands.add("extern malloc");
         assemblyCommands.add("");
         assemblyCommands.add("segment .text");
         assemblyCommands.add("");
         assemblyCommands.add("global main");
         assemblyCommands.add("main:");
 
         for (Command command : commands) {
             command.acceptVisitor(this);
         }
 
         assemblyCommands.add("mov rax, 1");
         assemblyCommands.add("int 0x80");
 
         
         for (RoomFunction room : rooms) {
             generateRoomCode(room);
         }
         
         //TODO - looking glasses
 
         addPrint();
         makeDataSegment();
 
         return assemblyCommands;
     }
 
     @Override
     public void visitArrayDeclaration(ArrayDeclarationCommand command) {
         //TODO - visitArrayDeclaration
         //TODO - need to allocate memory and stuff
     }
 
     @Override
     public void visitConditional(ConditionalCommand command) {
         int conditionalLabel = nextConditionalLabel;
         nextConditionalLabel++;
 
         List<ConditionalBranch> branches = command.getBranches();
 
         Storage destStorage = allocateStorage();
         for (int i = 0; i < branches.size() - 1; i++) {
             if (branches.get(i).getCondition() == null) {
                 break;
             }
 
             generateBooleanExpressionCode(destStorage, branches.get(i).getCondition());
 
             assemblyCommands.add("mov rax, " + destStorage);
             assemblyCommands.add("cmp rax, 1");
             assemblyCommands.add("je cond_" + conditionalLabel + "_" + i);
         }
         freeStorage(destStorage);
 
         for (int i = branches.size() - 1; i >= 0; i--) {
             assemblyCommands.add("cond_" + conditionalLabel + "_" + i + ":");
 
             for (Command aCommand : branches.get(i).getCommands()) {
                 aCommand.acceptVisitor(this);
             }
 
             if (i != 0) {
                 // Don't need to jump if this is the last branch
                 assemblyCommands.add("jmp cond_" + conditionalLabel + "_end");
             }
         }
 
         assemblyCommands.add("cond_" + conditionalLabel + "_end:");
     }
 
     @Override
     public void visitDecrement(DecrementCommand command) {
         Storage storage = symbolTable.getVariableStorage(command.getVariableName(), "");
         assemblyCommands.add("mov rax, " + storage);
         assemblyCommands.add("dec rax");
         assemblyCommands.add("mov " + storage + ", rax");
     }
 
     @Override
     public void visitFunctionCall(FunctionCallCommand command) {
         Storage destStorage = allocateStorage();
         for (ArithmeticExpression parameter : command.getParameters()) {
             generateExpressionCode(destStorage, parameter);
             assemblyCommands.add("mov rax, " + destStorage);
             assemblyCommands.add("push rax");
         }
         freeStorage(destStorage);
         
         assemblyCommands.add("call " + command.getFunctionName());
     }
 
     @Override
     public void visitFunctionReturn(FunctionReturnCommand command) {
         if (FunctionReturnCommand.Type.EXPRESSION == command.getType()) {
             // number
             Storage destStorage = allocateStorage();
             generateExpressionCode(destStorage, (ArithmeticExpression) command.getExpression());
             assemblyCommands.add("mov rax, " + destStorage);
         } else {
             // character
             //TODO - return character
         }
 
         assemblyCommands.add("ret");
     }
 
     @Override
     public void visitIncrement(IncrementCommand command) {
         Storage storage = symbolTable.getVariableStorage(command.getVariableName(), scope);
         assemblyCommands.add("mov rax, " + storage);
         assemblyCommands.add("inc rax");
         assemblyCommands.add("mov " + storage + ", rax");
     }
 
     @Override
     public void visitInput(InputCommand command) {
         ArithmeticExpression exp = command.getInputDestination();
 
         assemblyCommands.add("call read");
         assemblyCommands.add("call string_to_int");
        assemblyCommands.add("mov " + symbolTable.getVariableStorage(exp.getVariableName(), scope) + ", rax");
     }
 
     @Override
     public void visitSpeak(SpeakCommand command) {
         Storage destStorage = allocateStorage();
         Expression exp = command.getExpression();
 
         if (exp instanceof ArithmeticExpression) {
             ArithmeticExpression arithmeticExpression = (ArithmeticExpression) exp;
             if (ArithmeticExpression.Type.VARIABLE == arithmeticExpression.getType()
                     && Type.sentence == symbolTable.getVariableType(arithmeticExpression.getVariableName(), scope)) {
                 assemblyCommands.add("mov eax, 4");
                 assemblyCommands.add("mov ebx, 1");
                 assemblyCommands.add("mov ecx, " + symbolTable.getVariableStorage(arithmeticExpression.getVariableName(), scope));
                 assemblyCommands.add("mov edx, 100");
                 assemblyCommands.add("int 0x80");
                 return;
             }
 
             generateExpressionCode(destStorage, arithmeticExpression);
             assemblyCommands.add("mov rax, " + destStorage);
             assemblyCommands.add("call print_int");
         } else if (exp instanceof StringExpression) {
             stringNames.put("msg_" + nextStringName, ((StringExpression) exp).getString());
 
             assemblyCommands.add("mov eax, 4");
             assemblyCommands.add("mov ebx, 1");
             assemblyCommands.add("mov ecx, " + "msg_" + nextStringName);
             assemblyCommands.add("mov edx, 100");
             assemblyCommands.add("int 0x80");
 
             nextStringName++;
         } else if (exp instanceof CharacterExpression) {
             stringNames.put("msg_" + nextStringName, ((CharacterExpression) exp).getCharacter() + "");
 
             assemblyCommands.add("mov eax, 4");
             assemblyCommands.add("mov ebx, 1");
             assemblyCommands.add("mov ecx, " + "msg_" + nextStringName);
             assemblyCommands.add("mov edx, 1");
             assemblyCommands.add("int 0x80");
 
             nextStringName++;
         } else if (exp instanceof BooleanExpression) {
             BooleanExpression boolExp = (BooleanExpression) exp;
             generateBooleanExpressionCode(destStorage, boolExp);
             
             assemblyCommands.add("mov rax, " + destStorage);
             assemblyCommands.add("call print_int");
         }
     }
 
     @Override
     public void visitThrough(ThroughCommand command) {
         //TODO - visitThrough
     }
 
     @Override
     public void visitVariableAssignment(VariableAssignmentCommand command) {
         Storage storage = symbolTable.getVariableStorage(command.getVariableName(), scope);
         if (storage == Register.NONE) {
             storage = allocateStorage();
             symbolTable.setVariableStorage(command.getVariableName(), storage, scope);
         }
 
         Expression exp = command.getExpression();
         if (exp.isArithmeticExpression()) {
             generateExpressionCode(storage, (ArithmeticExpression) exp);
         } else if (exp instanceof StringExpression) {
             generateExpressionCode(storage, (StringExpression) exp);
         } else {
             generateExpressionCode(storage, (CharacterExpression) exp);
         }
     }
 
     @Override
     public void visitVariableDeclaration(VariableDeclarationCommand command) {
     }
 
     @Override
     public void visitWhileNot(WhileNotCommand command) {
         ConditionalBranch branch = command.getBranch();
         Storage destStorage = allocateStorage();
         
         int currentWhileNotNumber = nextWhileNotNumber;
         nextWhileNotNumber++;
         
         assemblyCommands.add("while_not_" + currentWhileNotNumber + ":");
         
         generateBooleanExpressionCode(destStorage, branch.getCondition());
 
         assemblyCommands.add("mov rax, " + destStorage);
         assemblyCommands.add("cmp rax, 1");
         assemblyCommands.add("je while_not_end_" + currentWhileNotNumber);
         
         for (Command aCommand : branch.getCommands()) {
             aCommand.acceptVisitor(this);
         }
         
         assemblyCommands.add("jmp while_not_" + currentWhileNotNumber);
         assemblyCommands.add("while_not_end_" + currentWhileNotNumber + ":");
     }
 
     private void generateExpressionCode(Storage destStorage, CharacterExpression exp) {
         //assemblyCommands.add("mov " + Register.rax + ", " + destStorage);
         assemblyCommands.add("mov " + destStorage + ", " + (int) exp.getCharacter());
     }
 
     private void generateExpressionCode(Storage destStorage, StringExpression exp) {
         stringNames.put("msg_" + nextStringName, exp.getString());
 
         assemblyCommands.add("mov rax, msg_" + nextStringName);
         assemblyCommands.add("mov " + destStorage + ", rax");
 
         nextStringName++;
     }
 
     private void generateExpressionCode(Storage destStorage, ArithmeticExpression exp) {
         if (ArithmeticExpression.Type.BINOP == exp.getType()) {
             // binOp
             Storage leftStorage = allocateStorage();
             Storage rightStorage = allocateStorage();
 
             generateExpressionCode(leftStorage, exp.getLeftExpr());
             generateExpressionCode(rightStorage, exp.getRightExpr());
             generateBinOpCode(exp.getBinOp(), leftStorage, rightStorage);
 
             assemblyCommands.add("mov " + Register.rax + ", " + leftStorage);
 
             freeStorage(leftStorage);
             freeStorage(rightStorage);
         } else {
             if (!exp.isImmediateValue()) {
                 // variable
                 assemblyCommands.add("mov " + Register.rax + ", " + symbolTable.getVariableStorage(exp.getVariableName(), scope));
             } else {
                 // immediate value
                 assemblyCommands.add("mov " + Register.rax + ", " + exp.getImmediateValue());
             }
 
             String unaryOperators = exp.getUnaryOperators();
             for (int i = 0; i < unaryOperators.length(); i++) {
                 if ('~' == unaryOperators.charAt(i)) {
                     assemblyCommands.add("not " + Register.rax);
                 } else {
                     assemblyCommands.add("imul " + Register.rax + ", -1");
                 }
             }
         }
         assemblyCommands.add("mov " + destStorage + ", " + Register.rax);
     }
 
     private void generateBinOpCode(char binOp, Storage destStorage, Storage moreStorage) {
         switch (binOp) {
             case '+':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("add rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 //assemblyCommands.add("mov " + moreStorage + ", rbx");
                 break;
             case '-':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("sub rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 break;
             case '&':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("and rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 //assemblyCommands.add("mov " + moreStorage + ", rbx");
                 break;
             case '*':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("imul rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 //assemblyCommands.add("mov " + moreStorage + ", rbx");
                 break;
             case '%':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("idiv rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 break;
             case '/':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("mov  rdx, 0");
                 assemblyCommands.add("idiv rbx");
                 assemblyCommands.add("mov " + destStorage + ", rdx");
                 break;
             case '|':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("mov  rdx, 0");
                 assemblyCommands.add("or  rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 //assemblyCommands.add("mov " + moreStorage + ", rbx");
                 break;
             case '^':
                 assemblyCommands.add("mov rax, " + destStorage);
                 assemblyCommands.add("mov rbx, " + moreStorage);
                 assemblyCommands.add("xor rax, rbx");
                 assemblyCommands.add("mov " + destStorage + ", rax");
                 //assemblyCommands.add("mov " + moreStorage + ", rbx");
                 break;
 
         }
         //Need to check if moreStorage is being used in the symbol table
         //Use symboltable.usesStorage(moreStorage)
         if (symbolTable.usesStorage(moreStorage, scope)) {
             freeStorage(moreStorage);
         }
         //Not sure
     }
 
     private void generateBooleanExpressionCode(Storage destStorage, BooleanExpression exp) {
         assemblyCommands.add("");
         switch (exp.getType()) {
             case BINOP:
                 Storage leftStorage = allocateStorage();
                 Storage rightStorage = allocateStorage();
 
                 //Evaluate both sides of the comparison
                 generateBooleanExpressionCode(leftStorage, exp.getLeftExpr());
                 generateBooleanExpressionCode(rightStorage, exp.getRightExpr());
 
                 //Move the values into registers
                 assemblyCommands.add("mov rax, " + leftStorage);
                 //TODO - short circuit
                 assemblyCommands.add("mov rbx, " + rightStorage);
 
                 if (exp.getOp().equals("&&")) {
                     assemblyCommands.add("and rax, rbx");
                     assemblyCommands.add("mov " + destStorage + ", rax");
                 } else if (exp.getOp().equals("||")) {
                     assemblyCommands.add("or rax, rbx");
                     assemblyCommands.add("mov " + destStorage + ", rax");
                 } else if (exp.getOp().equals("==")) {
                     assemblyCommands.add("cmp rax, rbx");
                     assemblyCommands.add("je cmp_pos_" + nextBoolComparisonNumber);
                     assemblyCommands.add("mov rax, 0");
                     assemblyCommands.add("jmp cmp_end_" + nextBoolComparisonNumber);
                     assemblyCommands.add("cmp_pos_" + nextBoolComparisonNumber + ":");
                     assemblyCommands.add("mov rax, 1");
                     assemblyCommands.add("cmp_end_" + nextBoolComparisonNumber + ":");
                     assemblyCommands.add("mov " + destStorage + ", rax");
                     nextBoolComparisonNumber++;
                 } else if (exp.getOp().equals("!=")) {
                     assemblyCommands.add("cmp rax, rbx");
                     assemblyCommands.add("jne cmp_pos_" + nextBoolComparisonNumber);
                     assemblyCommands.add("mov rax, 0");
                     assemblyCommands.add("jmp cmp_end_" + nextBoolComparisonNumber);
                     assemblyCommands.add("cmp_pos_" + nextBoolComparisonNumber + ":");
                     assemblyCommands.add("mov rax, 1");
                     assemblyCommands.add("cmp_end_" + nextBoolComparisonNumber + ":");
                     assemblyCommands.add("mov " + destStorage + ", rax");
                     nextBoolComparisonNumber++;
                 }
 
                 assemblyCommands.add("mov " + destStorage + ", rax");
 
                 assemblyCommands.add("");
                 break;
             case COMPARISON:
                 generateComparisonCode(destStorage, exp.getOp(), exp.getLeftAritExpr(), exp.getRightAritExpr());
                 break;
             case FUNCTION:
                 //TODO
                 break;
         }
 
 
     }
 
     private void generateComparisonCode(Storage destStorage, String binop, ArithmeticExpression left, ArithmeticExpression right) {
         Storage leftStorage = allocateStorage();
         Storage rightStorage = allocateStorage();
 
         //Evaluate both sides of the comparison
         generateExpressionCode(leftStorage, left);
         generateExpressionCode(rightStorage, right);
 
         //Move the values into registers
         assemblyCommands.add("");
         assemblyCommands.add("mov rax, " + leftStorage);
         assemblyCommands.add("mov rbx, " + rightStorage);
 
         nextComparisonNumber += 1;
         String comparisonLabel = "test_" + nextComparisonNumber;
 
         assemblyCommands.add("cmp rax, rbx");
 
 
         if (binop.equals("==")) {
             assemblyCommands.add("je " + comparisonLabel);
         } else if (binop.equals("!=")) {
             assemblyCommands.add("jne " + comparisonLabel);
         } else if (binop.equals(">")) {
             assemblyCommands.add("jg " + comparisonLabel);
         } else if (binop.equals("<")) {
             assemblyCommands.add("jl " + comparisonLabel);
         } else if (binop.equals(">=")) {
             assemblyCommands.add("jge " + comparisonLabel);
         } else if (binop.equals("<=")) {
             assemblyCommands.add("jle " + comparisonLabel);
         }
 
         String endLabel = "endtest_" + nextComparisonNumber;
 
         assemblyCommands.add("mov " + Register.rax + ", 0");
         assemblyCommands.add("mov " + destStorage + ", " + Register.rax);
         assemblyCommands.add("jmp " + endLabel);
         assemblyCommands.add(comparisonLabel + ":");
         assemblyCommands.add("mov " + Register.rax + ", 1");
         assemblyCommands.add("mov " + destStorage + ", " + Register.rax);
         assemblyCommands.add(endLabel + ":");
         assemblyCommands.add("");
     }
 
     private void generateRoomCode(RoomFunction room) {
         scope = room.getName();
         
         List<Parameter> parameters = room.getParameters();
         List<Storage> storages = new ArrayList<Storage>();
         
         assemblyCommands.add("room_" + room.getName() + ":");
         for (int i = parameters.size() - 1; i >= 0; i--) {
             Storage storage = symbolTable.getVariableStorage(parameters.get(i).getName(), scope);
             if (storage == Register.NONE) {
                 storage = allocateStorage();
                 symbolTable.setVariableStorage(parameters.get(i).getName(), storage, scope);
             }
             storages.add(0, storage);
             assemblyCommands.add("pop rax");
             assemblyCommands.add("mov " + storage + ", rax");
         }
         
         for (Command command : room.getCommands()) {
             command.acceptVisitor(this);
         }
     }
 
     private void generateLookingGlassCode() {
     }
 
     private Storage allocateStorage() {
         /*if (freeRegisters.size() > 0) {
         Register register = freeRegisters.remove();
         return register;
         }*/
 
         if (!freeMemoryLocationVariables.isEmpty()) {
             Iterator<String> iter = freeMemoryLocationVariables.iterator();
             String memoryLocationVariable = iter.next();
             iter.remove();
 
             return new MemoryLocation(memoryLocationVariable);
         }
 
         String memoryLocationVariable;
         do {
             memoryLocationVariable = "mem_" + nextFreeMemoryAddress;
             nextFreeMemoryAddress += 1;
         } while (symbolTable.containsVariable(memoryLocationVariable, scope));
 
         //Keep a list of all memory locations
         memoryLocationVariables.add(memoryLocationVariable);
 
         return new MemoryLocation(memoryLocationVariable);
     }
 
     private void freeStorage(Storage storage) {
         freeMemoryLocationVariables.add(((MemoryLocation) storage).getLocationName());
     }
 
     private void makeDataSegment() {
         assemblyCommands.add("");
         assemblyCommands.add("segment .data");
         for (String x : memoryLocationVariables) {
             assemblyCommands.add(x + " dq " + 0);
         }
 
         assemblyCommands.add("bufferaddr dq 0");
         assemblyCommands.add("octetbuffer dq 0");
         assemblyCommands.add("noofdigits dq 0");
         assemblyCommands.add("originalvalue dq 0");
         assemblyCommands.add("isnegative dq 0");
 
         for (Map.Entry<String, String> entry : stringNames.entrySet()) {
             String sentence = entry.getValue();
 
             boolean newLine = false;
             if (sentence.endsWith("\n")) {
                 newLine = true;
                 sentence = sentence.substring(0, sentence.length() - 2);
                 System.out.println("NEWLINE");
             }
 
             assemblyCommands.add(entry.getKey() + " db \"" + sentence + "\"" + (newLine ? ", 0xA" : ""));
             assemblyCommands.add("len_" + entry.getKey() + " equ $-" + entry.getKey());
         }
 
         assemblyCommands.add("segment .bss");
         assemblyCommands.add("number resb 100");
     }
 
     private void addPrint() {
         //Shouldn't need to preserve registers, but just to be safe.
         assemblyCommands.add("");
         assemblyCommands.add("");
 
         assemblyCommands.add("print:");
         assemblyCommands.add("push rax");
         assemblyCommands.add("push rbx ;Preserve registers");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("mov [octetbuffer], rcx ;rcx contains the printable value, put in buffer");
         assemblyCommands.add("mov rcx, octetbuffer ;give rcx the address of the buffer");
         assemblyCommands.add("mov rbx, 1 ;set to STOUT");
         assemblyCommands.add("mov rdx, 1 ;set length to 1");
         assemblyCommands.add("mov rax, 4 ;set interrupt to sys_write");
         assemblyCommands.add("int 0x80 ;interrupt the OS");
         assemblyCommands.add("pop rdx");
         assemblyCommands.add("pop rbx ;Restore registers");
         assemblyCommands.add("pop rax");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("read: ;reads in 1 character from stdin");
         assemblyCommands.add("push rax");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("mov rax, 3");
         assemblyCommands.add("mov rbx, 0");
         assemblyCommands.add("mov rcx, number");
         assemblyCommands.add("mov rdx, 100");
         assemblyCommands.add("int 0x80");
         assemblyCommands.add("pop rdx");
         assemblyCommands.add("pop rbx");
         assemblyCommands.add("pop rax");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("print_loop: ;prints the contents of the buffer");
         assemblyCommands.add("mov rax, 0");
         assemblyCommands.add("begin:");
         assemblyCommands.add("cmp rax, 100");
         assemblyCommands.add("jg end");
         assemblyCommands.add("mov rbx, number");
         assemblyCommands.add("mov rcx, [rbx + rax]");
 
         assemblyCommands.add("");
         assemblyCommands.add("cmp rcx, 0");
         assemblyCommands.add("je end");
         assemblyCommands.add("call print");
         assemblyCommands.add("add rax, 1");
         assemblyCommands.add("jmp begin");
         assemblyCommands.add("end:");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("int_to_string:");
         assemblyCommands.add("push rax");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("push rcx");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("power: ;rax to the power of rbx");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("push rcx");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("cmp rbx, 0");
         assemblyCommands.add("jne not0");
         assemblyCommands.add("mov rax, 1");
         assemblyCommands.add("jmp endfor");
         assemblyCommands.add("not0: ");
         assemblyCommands.add("mov rdx, rax");
         assemblyCommands.add("mov rcx, 1 ;loop counter");
         assemblyCommands.add("for:");
         assemblyCommands.add("add rcx, 1 ;increment loop counter");
         assemblyCommands.add("cmp rcx, rbx ;compare loop counter and rbx");
         assemblyCommands.add("jg endfor ;exit loop if the counter is more than rbx");
         assemblyCommands.add("imul rax, rdx");
         assemblyCommands.add("jmp for");
         assemblyCommands.add("endfor:");
         assemblyCommands.add("pop rdx");
         assemblyCommands.add("pop rcx");
         assemblyCommands.add("pop rbx");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("number_of_digits: ;puts the number of digits of the number in rax in rax");
         assemblyCommands.add("push rcx");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("mov rcx, rax ;rcx contains the value");
         assemblyCommands.add("mov rbx, 1 ;loop counter");
         assemblyCommands.add("for2:");
         assemblyCommands.add("mov rax, 10");
         assemblyCommands.add("call power");
         assemblyCommands.add("cmp rcx, rax");
         assemblyCommands.add("jle endfor2");
         assemblyCommands.add("add rbx, 1");
         assemblyCommands.add("jmp for2");
         assemblyCommands.add("endfor2:");
         assemblyCommands.add("mov rax, rbx");
         assemblyCommands.add("pop rbx");
         assemblyCommands.add("pop rcx");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("print_int: ;Prints a number in rax");
         assemblyCommands.add("push rax");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("push rcx");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("push rsi");
         assemblyCommands.add("push rbp");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rbp, 0");
         assemblyCommands.add("mov [isnegative], rbp");
         assemblyCommands.add("cmp rax, 0");
         assemblyCommands.add("jge continue");
         assemblyCommands.add("imul rax, -1");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rcx, '-'");
         assemblyCommands.add("call print");
 
         assemblyCommands.add("");
         assemblyCommands.add("continue:");
         assemblyCommands.add("mov rdx, rax");
         assemblyCommands.add("mov [originalvalue], rax");
         assemblyCommands.add("call number_of_digits");
         assemblyCommands.add("mov rcx, rax");
         assemblyCommands.add("mov [noofdigits], rax");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rbp, 0 ;loop counter");
         assemblyCommands.add("loop:");
         assemblyCommands.add("mov rbx, [noofdigits]");
         assemblyCommands.add("sub rbx, rbp ");
         assemblyCommands.add("dec rbx ;rbx = (no of chars - loop counter - 1)");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rax, 10");
         assemblyCommands.add("call power ;rax = 10^(no of chars - loop counter -1)");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rsi, [originalvalue] ;rsi contains the value");
         assemblyCommands.add("mov rbx, rax ;rbx = 10^(no of chars - loop counter - 1)");
         assemblyCommands.add("mov rax, rsi");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rdx, 0 ");
         assemblyCommands.add("idiv rbx ;rbx contains the number with the left trimmed");
         assemblyCommands.add("mov rsi, rax ;rsi now contains the thing above");
 
         assemblyCommands.add("");
         assemblyCommands.add("cmp rbp, 0");
         assemblyCommands.add("je firsttime");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rax, 10");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rbx, rax");
         assemblyCommands.add("mov rax, rsi ;rdx now contains the digit");
         assemblyCommands.add("mov rdx, 0");
         assemblyCommands.add("idiv rbx");
         assemblyCommands.add("mov rsi, rdx");
 
         assemblyCommands.add("");
         assemblyCommands.add("firsttime:");
         assemblyCommands.add("mov rcx, rsi");
         assemblyCommands.add("add rcx, 48");
         assemblyCommands.add("call print");
 
         assemblyCommands.add("");
         assemblyCommands.add("add rbp, 1");
         assemblyCommands.add("cmp rbp, [noofdigits]");
         assemblyCommands.add("jl loop");
         assemblyCommands.add("endbigloop:");
 
         assemblyCommands.add("");
         assemblyCommands.add("pop rbp");
         assemblyCommands.add("pop rsi ");
         assemblyCommands.add("pop rdx ;restore registers");
         assemblyCommands.add("pop rcx");
         assemblyCommands.add("pop rbx");
         assemblyCommands.add("pop rax");
         assemblyCommands.add("ret");
 
         assemblyCommands.add("");
         assemblyCommands.add("string_to_int: ;put the buffer address in rax and it shall be replaced with the value");
         assemblyCommands.add("push rbx");
         assemblyCommands.add("push rcx");
         assemblyCommands.add("push rsi ;preserve registers");
         assemblyCommands.add("push rdx");
         assemblyCommands.add("push rbp");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rsi, number ;buffer address");
         assemblyCommands.add("mov rax, 0 ;return value");
         assemblyCommands.add("mov rbx, 0 ;loop counter");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rdx, 0");
         assemblyCommands.add("mov dl, byte[rsi]");
         assemblyCommands.add("cmp rdx, '-' ");
         assemblyCommands.add("je neg ;check to see if its negative");
         assemblyCommands.add("jmp stringloop");
 
         assemblyCommands.add("");
         assemblyCommands.add("neg:");
         assemblyCommands.add("mov rbp, 1");
         assemblyCommands.add("mov [isnegative], rbp ;set isnegative to true");
 
         assemblyCommands.add("");
         assemblyCommands.add("stringloop:");
         assemblyCommands.add("cmp rbx, 100");
         assemblyCommands.add("jg endstringloop");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rdx, 0");
         assemblyCommands.add("mov dl, byte [rsi+rbx]");
 
         assemblyCommands.add("");
         assemblyCommands.add("cmp rdx, 58 ");
         assemblyCommands.add("jge notnumber");
         assemblyCommands.add("cmp rdx, 47");
         assemblyCommands.add("jle notnumber");
         assemblyCommands.add("jmp justright");
         assemblyCommands.add("notnumber:");
         assemblyCommands.add("add rbx, 1");
         assemblyCommands.add("jmp stringloop");
 
         assemblyCommands.add("");
         assemblyCommands.add("justright:");
         assemblyCommands.add("sub rdx, 48 ;convert char to int");
         assemblyCommands.add("imul rax, 10 ;times the current return by 10");
         assemblyCommands.add("add rax, rdx ;add the next digit onto the end");
         assemblyCommands.add("add rbx, 1 ;increment loop counter");
         assemblyCommands.add("jmp stringloop");
         assemblyCommands.add("endstringloop:");
 
         assemblyCommands.add("");
         assemblyCommands.add("mov rbp, [isnegative]");
         assemblyCommands.add("cmp rbp, 0");
         assemblyCommands.add("je return");
         assemblyCommands.add("imul rax, -1 ;make number negative");
 
         assemblyCommands.add("");
         assemblyCommands.add("return:");
         assemblyCommands.add("pop rbp");
         assemblyCommands.add("pop rdx");
         assemblyCommands.add("pop rsi");
         assemblyCommands.add("pop rcx");
         assemblyCommands.add("pop rbx");
         assemblyCommands.add("ret");
     }
 }
