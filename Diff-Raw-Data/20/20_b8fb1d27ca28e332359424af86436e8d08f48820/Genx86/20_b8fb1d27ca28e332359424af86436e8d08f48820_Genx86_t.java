 package zinara.code_generator;
 
 import zinara.ast.*;
 import zinara.ast.instructions.*;
 import zinara.ast.expression.*;
 import zinara.exceptions.InvalidArchitectureException;
 import zinara.exceptions.InvalidCodeException;
 import zinara.exceptions.TypeClashException;
 import zinara.parser.sym;
 import zinara.symtable.SymTable;
 import zinara.symtable.SymValue;
 import zinara.ast.type.*;
 
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class Genx86{
     private String[] regs;
     private String[] dwordRegs;
     private String[] byteRegs;
     private String stackp;
     private String framep;
     private String stackAlig; //Alineamiento de la pila
     private String global_offset;
     
     private int n_regs;
     private int next_reg;
     private int bits;
     private int labelCounter = 0;
     private int word_size;
 
     private BufferedWriter file;
 
     private String save;
     private String restore;
 
     public Genx86(int bits, String fileName)
 	throws InvalidArchitectureException, IOException{
 	if (bits == 64){
 	    regs = new String[14];
 	    dwordRegs = new String[14];
 	    byteRegs = new String[14];
 
 	    regs[0] = "rax";
 	    regs[1] = "rbx";
 	    regs[2] = "rcx";
 	    regs[3] = "rdx";
 	    regs[4] = "rsi";
 	    regs[5] = "rdi";
 	    regs[6] = "r8";
 	    regs[7] = "r9";
 	    regs[8] = "r10";
 	    regs[9] = "r11";
 	    regs[10] = "r12";
 	    regs[11] = "r13";
 	    regs[12] = "r14";
 	    regs[13] = "r15";
 
 	    dwordRegs[0] = "eax";
 	    dwordRegs[1] = "ebx";
 	    dwordRegs[2] = "ecx";
 	    dwordRegs[3] = "edx";
 	    dwordRegs[4] = "esi";
 	    dwordRegs[5] = "edi";
 	    dwordRegs[6] = "r8d";
 	    dwordRegs[7] = "r9d";
 	    dwordRegs[8] = "r10d";
 	    dwordRegs[9] = "r11d";
 	    dwordRegs[10] = "r12d";
 	    dwordRegs[11] = "r13d";
 	    dwordRegs[12] = "r14d";
 	    dwordRegs[13] = "r15d";
 
 	    byteRegs[0] = "al";
 	    byteRegs[1] = "bl";
 	    byteRegs[2] = "cl";
 	    byteRegs[3] = "dl";
 	    byteRegs[4] = "sil";
 	    byteRegs[5] = "dil";
 	    byteRegs[6] = "r8b";
 	    byteRegs[7] = "r9b";
 	    byteRegs[8] = "r10b";
 	    byteRegs[9] = "r11b";
 	    byteRegs[10] = "r12b";
 	    byteRegs[11] = "r13b";
 	    byteRegs[12] = "r14b";
 	    byteRegs[13] = "r15b";
 
 	    stackp = "rsp";
 	    framep = "rbp";
 	    stackAlig = "8";
 	    
 	    n_regs = 14;
 	    word_size = 8;
 	}
 	else if (bits == 32){
 	    regs = new String[6];
 	    byteRegs = new String[6];
 
 	    regs[0] = "eax";
 	    regs[1] = "ebx";
 	    regs[2] = "ecx";
 	    regs[3] = "edx";
 	    regs[4] = "esi";
 	    regs[5] = "edi";
 
 	    byteRegs[0] = "al";
 	    byteRegs[1] = "bl";
 	    byteRegs[2] = "cl";
 	    byteRegs[3] = "dl";
 	    byteRegs[4] = "sil";
 	    byteRegs[5] = "dil";
 
 	    stackp = "esp";
 	    framep = "ebp";
 	    stackAlig = "4";
 
 	    n_regs = 6;
 	    word_size = 4;
 	}
 	else
 	    throw new InvalidArchitectureException(bits); 
 	
 	global_offset = "glob";
 
 	next_reg = 0;
 	this.bits = bits;
 	save = null;
 	restore = null;
 
 	file = new BufferedWriter(new FileWriter(new File(fileName)));
 
 	file.write("%include \"asm_io.inc\"\n");
 	    
 	if (this.bits == 64)
 	    this.file.write("BITS 64\n");
     }
 
     public void generateProgram(Program program)
 	throws IOException,InvalidCodeException,TypeClashException {
 	// Main
 	program.getMain().register = 0;
 	program.getMain().getCode().nextInst = "halt";
 
 	program.tox86(this);
 
 	// Exits the program
 	writeLabel(program.getMain().getCode().nextInst);
 	exitSyscall(0);
 	closeFile();
     }
 
     public void generateHeader(SymTable symtable)
 	throws IOException,InvalidCodeException {
 	String identifier;
 	SymValue symValue;
 
 	// Espacio para variable globales
  	Iterator keyIt = symtable.keySet().iterator();
 	int total_size = 0;
 	while(keyIt.hasNext()) {
 	    identifier = (String)keyIt.next();
 	    symValue = symtable.getSymbol(identifier);
 	    if (symValue.type.size() == 0){continue;}
 
 	    symValue.setArea(global_offset());
 	    symValue.setOffset("+"+Integer.toString(total_size));
 	    total_size += symValue.type.size();
 	}
 
 	// Luego para variables del Main
 	// La tabla del main siempre queda al final de la lista de tablas hijo
 	SymTable mainSymTable = symtable.getSon(symtable.getSons().size()-1);
 	total_size = mainSymTable.reserve_mem_main(total_size,global_offset);
 
 	// .ASM HEADER
 	//  El espacio para variables, texto de las funciones
 	//  y el comienzo del texto del main se crean aca
 	write(reserve_space_str(global_offset, total_size));
 	write(main_header_str());
     }
 
     //Reserva tanta memoria como le pidan. data_size es la cantidad
     //de bytes. 
     public String reserve_space_str(String label, int data_size){
 	String code = "";
 
 	code += "section .bss\n   "+label+": resb "+data_size+"\n"; 
 	code += "section .rodata\n";
 	code += "   int_format  db  \"%i\",0\n";
 	code += "   float_format  db  \"%f\",0\n";
 	code += "   string_format  db  \"%s\",0\n";
 	code += "   write_format  db  \"w\",0\n";
 	return code;
     }
 
     public String main_header_str(){
 	return "section .text\n   global main\nextern printf\nmain:\n";
     }
 
     public int stack_align(){
 	return Integer.parseInt(this.stackAlig);
     }
 
     public String newLabel() {
 	return "zn" + labelCounter++;
     }
 
     
     public int word_size() { return word_size; }
     
     private String regId(int regNumber) {
 	return regs[regNumber % n_regs];
     }
 
     public String dwordRegId(int regNumber) {
 	return dwordRegs[regNumber % n_regs];
     }
 
     public String byteRegId(int regNumber) {
 	return byteRegs[regNumber % n_regs];
     }
 
     public String intRegName(int regNumber){
 	if (this.bits == 32){
 	    return regId(regNumber);
 	}
 	else
 	    return dwordRegId(regNumber);	    
     }
 
     public String realRegName(int regNumber){
 	return regId(regNumber);
     }
 
     public String charRegName(int regNumber){
 	return byteRegId(regNumber);
     }
 
     public String boolRegName(int regNumber){
 	if (this.bits == 32){
 	    return regId(regNumber);
 	}
 	else
 	    return dwordRegId(regNumber);	    
     }
 
     public String addrRegName(int regNumber){
 	    return regId(regNumber);
     }
 
     public String regName(int r, Type type) throws InvalidCodeException{
 	if (type instanceof IntType)
 	    return intRegName(r);
 	else if (type instanceof FloatType)
 	    return realRegName(r);
 	else if (type instanceof BoolType)
 	    return boolRegName(r);
 	else if (type instanceof CharType)
 	    return charRegName(r);
 	else if ((type instanceof ListType)||
 		 (type instanceof DictType)||
		 (type instanceof VariantType)||
 		 (type instanceof StringType))
 	    return addrRegName(r);
 	else
 	    throw new InvalidCodeException("No se tienen registros para el tipo "+type);
     }
 
     public void write(String thing) throws IOException{
 	file.write(thing);
     }
 
     public void writeLabel(String label) throws IOException {
 	file.write(label + ":\n");
     }
 
     public void closeFile() throws IOException{
 	file.close();
     }
 
     public String stack_pointer(){
 	return this.stackp;
     }
 
     public String frame_pointer(){
 	return this.framep;
     }
 
     public String global_offset(){
 	return global_offset;
     }
 
 
     public String get_reg(int reg){
 	return regs[reg%n_regs];
     }
 
     //Push
     public String push (String thing){
 	String code = "";
 
 	if (this.bits == 32)
 	    return "push "+thing+"\n";
 	else{
 	    code += sub("rsp",stackAlig);
 	    code += mov("[rsp]",thing);
 	    return code;
 	}
     }
 
     //Push con resta arbitraria
     public String push (String thing, int size){
 	String code = "";
 
 	code += sub(this.stackp,Integer.toString(size));
 	code += mov("["+this.stackp+"]",thing);
 	return code;
     }
 
     //Push de 1 byte (word)
     public String pushb (String thing){
 	String code = "";
 
 	code += sub(this.stackp,"1");
 	code += mov("["+this.stackp+"]",thing);
 	return code;
     }
 
     //Push de 32 bits (dword)
     public String pushw (String thing){
 	String code = "";
 
 	if (this.bits == 32)
 	    return "push "+thing+"\n";
 	else{
 	    code += sub("rsp","4");
 	    code += mov("[rsp]",thing);
 	    return code;
 	}
     }
 
     //Push de 64 bits (quad-word)
     public String pushq (String thing) throws InvalidCodeException{
 	String code = "";
 	if (this.bits == 64){
 	    code += sub("rsp",stackAlig);
 	    code += mov("[rsp]",thing);
 	    return code;
  	}
 	else{
 	    throw new InvalidCodeException("pushq en 32 bits");
 	}
     }
 
     // Pop, la cantidad de bits es la misma
     //que la del destino (thing). Si nasm no
     //puede deducir la cantidad de bits,
     //va a dar error. En ese caso usar el otro pop.
     //(Esta funcion esta sobrecargada)
     public String pop (String thing){
 	String code = "";
 
 	if (this.bits == 32)
 	    return "pop "+thing+"\n";
 	else{
 	    code += mov(thing,"[rsp]");
 	    code += add("rsp",this.stackAlig);
 	    return code;
 	}
     }
 
     // Pop que especifica la cantidad de bits
     //que se deben mover. Si los bits son vacios
     //nasm deduce cuantos se deben mover, si puede.
     // Independiente de la arquitectura
     public String pop (String thing, String size_mod)
 	throws InvalidCodeException{
 	String size;
 	if (size_mod.compareTo("qword")==0){
 	    if (this.bits != 64)
 		throw new InvalidCodeException("pop de un qword en 32 bits");
 	    size = "8";
 	}
 	else if (size_mod.compareTo("dword")==0){ 
 	    size = "4";
 	}
 	else if (size_mod.compareTo("word")==0){
 	    size = "2";
 	}
 	else if (size_mod.compareTo("byte")==0){
 	    size = "1";
 	}
 	else
 	    throw new InvalidCodeException("modificador de tamano invalido");
     
 	String code = "";
 	code += mov(thing,"["+this.stackp+"]",size_mod);
 	code += add(this.stackp,size);
 	return code;
     }
 
     public String push(String t, Type type) throws InvalidCodeException{
 	if (type instanceof IntType)
 	    return pushInt(t);
 	else if (type instanceof FloatType)
 	    return pushReal(t);
 	else if (type instanceof BoolType)
 	    return pushInt(t);
 	// else if (type instanceof CharType)
 	//     return pushChar(t);
 	else if ((type instanceof ListType)||
 		 (type instanceof DictType)||
 		 (type instanceof StringType))
 	    return pushAddr(t);
 	else
 	    throw new InvalidCodeException("No se tiene push para el tipo "+type);
     }
 
     public String pushInt (String in){
 	if (this.bits == 32)
 	    return push(in);
 	else
 	    return pushw(in);
     }
 
     public String pushReal (String real) throws InvalidCodeException{
 	if (this.bits == 32)
 	    return pushFloat(real);
 	else
 	    return pushDouble(real);
     }
 
     public String pushFloat (String flo){
 	if (this.bits == 32)
 	    return push(flo);
 	else
 	    return pushw(flo);
     }
 
     public String pushDouble (String dou) throws InvalidCodeException{
 	if (this.bits == 64)
 	    return push(dou);
 	else{
 	    throw new InvalidCodeException("pushDouble en 32 bits");
 	}
     }
 
     // public String pushChar (String ch){
     // 	    return push(ch,"byte");
     // }
 
     public String pushAddr (String addr){
 	return push(addr);
     }
 
     public String pop(String t, Type type) throws InvalidCodeException{
 	if (type instanceof IntType)
 	    return popInt(t);
 	else if (type instanceof FloatType)
 	    return popReal(t);
 	else if (type instanceof BoolType)
 	    return popInt(t);
 	else if (type instanceof CharType)
 	    return popChar(t);
 	else if ((type instanceof ListType)||
 		 (type instanceof DictType)||
 		 (type instanceof StringType))
 	    return popAddr(t);
 	else
 	    throw new InvalidCodeException("No se tiene pop para el tipo "+type);
     }
 
     public String popInt (String in)
 	throws InvalidCodeException{
 	    return pop(in,"dword");
     }
 
     public String popReal (String real)
 	throws InvalidCodeException{
 	if (this.bits == 32)
 	    return popFloat(real);
 	else
 	    return popDouble(real);
     }
 
     public String popFloat (String flo)
 	throws InvalidCodeException{
 	    return pop(flo,"dword");
     }
 
     public String popDouble (String dou)
 	throws InvalidCodeException{
 	if (this.bits == 64)
 	    return pop(dou,"qword");
 	else{
 	    throw new InvalidCodeException("popDouble en 32 bits");
 	}
     }
 
     public String popChar (String ch)
 	throws InvalidCodeException{
 	return pop(ch,"byte");
     }
 
     public String popAddr (String addr)
 	throws InvalidCodeException{
 	if (this.bits == 32)
 	    return pop(addr,"dword");
 	else
 	    return pop(addr,"qword");
     }
 
     // Usando este mov, nasm deducira la cantidad de
     //bytes a mover a partir del contexto.
     public String mov(String dst, String orig){
 	return "mov "+dst+","+orig+"\n";
     }
 
     // Usando este mov, se le especifica explicitamente
     //a nasm la cantidad de bytes (byte,word,dword o qword).
     private String mov(String dst, String orig, String size_mod){
 	return "mov "+size_mod+" "+dst+","+orig+"\n";
     }
 
     public String movInt(String dst, String orig){
 	return mov(dst,orig,"dword");
     }
 
     public String movReal(String dst, String orig){
 	if (this.bits == 32)
 	    return mov(dst,orig,"dword");
 	else
 	    return mov(dst,orig,"qword");	    
     }
 
     public String movBool(String dst, String orig){
 	return mov(dst,orig,"dword");
     }
 
     public String movChar(String dst, String orig){
 	return mov(dst,orig,"byte");
     }
 
     public String movAddr(String dst, String orig){
 	if (this.bits == 32)
 	    return mov(dst,orig,"dword");
 	else
 	    return mov(dst,orig,"qword");
     }
 
     public String mov(String dst, String orig, Type type)
 	throws InvalidCodeException{
 	if (type instanceof IntType)
 	    return movInt(dst,orig);
 	else if (type instanceof FloatType)
 	    return movReal(dst,orig);
 	else if (type instanceof BoolType)
 	    return movBool(dst,orig);
 	else if (type instanceof CharType)
 	    return movChar(dst,orig);
 	else if ((type instanceof ListType)||
 		 (type instanceof DictType)||
 		 (type instanceof StringType))
 	    return movAddr(dst,orig);
 	else
 	    throw new InvalidCodeException("No se tiene mov para el tipo "+type);
     }
 
     public String fst(String dst, String size){
 	return "fst "+size+" "+dst+"\n";
     }
 
     public String fstp(String dst, String size){
 	return "fstp "+size+" "+dst+"\n";
     }
 
     public String fld(String orig, String size){
 	return "fld "+size+" "+orig+"\n";
     }
 
     public String fst(String dst){
 	return "fst "+dst+"\n";
     }
 
     public String fstp(String dst){
 	return "fstp "+dst+"\n";
     }
 
     public String fld(String orig){
 	return "fld "+orig+"\n";
     }
 
     public String fist(String dst){
 	return "fist "+dst+"\n";
     }
 
     public String fild(String orig){
 	return "fild "+orig+"\n";
     }
 
     public String fxch(){
 	return "fxch\n";
     }
 
     public String fxchR(String st){
 	return "fxch "+st+"\n";
     }
 
     public String ffree(String st){
 	return "ffree "+st+"\n";
     }
 
     public String fninit(){
 	return "fninit\n";
     }
 
     //Suma de enteros
     public String add(String dst, String op2){
 	return "add "+dst+","+op2+"\n";
     }
 
     //Resta de enteros
     public String sub(String dst, String op2){
 	return "sub "+dst+","+op2+"\n";
     }
     
     //Multiplicacion de enteros
     public String imul(String dst, String op2){
 	return "imul "+dst+","+op2+"\n";
     }
 
     //Division de enteros, el resultado queda en dividend
     public String idiv(String dividend, String diviser)
 	throws InvalidCodeException{
 	String code = "";
 	String ax = regId(0);
 	String bx = regId(1);
 	String dx = regId(3);
 
 	code += push(ax); //Parte baja del dividendo
 	code += push(bx); //Divisor
 	code += push(dx); //Parte alta del dividendo
 
 	//Aqui pongo los argumentos en los registros
 	code += pushInt(dividend);
 	code += pushInt(diviser);
 	code += popInt("ebx");
 	code += popInt("eax");
 
 	code += mov(dx,"0h");
 	//Limpio la parte alta porque no se va a usar
 
 	code += "idiv ebx\n";
 	code += pop(dx);//Restauro registros 
 	code += pop(bx);//Restauro registros 
 
 	//If en caso de que el registro donde debe quedar la division
 	//esa el mismo eax, no lo puedo sobreescribir
 	if (dividend.compareTo("eax") == 0)
 	    code += add(this.stackp,this.stackAlig);
 	else{
 	    code += mov(dividend,"eax");
 	    code += pop(ax);
 	}
 	return code;
     }
 
     //Modulo de enteros, el resultado queda en dividend
     public String imod(String dividend, String diviser){
 	String code = "";
 
 	code += pushw("eax"); //Parte baja del dividendo
 	code += pushw("ebx"); //Divisor
 	code += pushw("edx"); //Parte alta del dividendo
 
 	//Aqui pongo los argumentos en los registros
 	code += pushw(dividend);
 	code += pushw(diviser);
 	code += pop("ebx");
 	code += pop("eax");
 
 	code += mov("edx","0h","dword");
 	//Limpio la parte alta porque no se va a usar
 
 	code += "idiv ebx\n";
 	code += pop("edx");//Restauro registros 
 	code += pop("ebx");//Restauro registros 
 
 	//If en caso de que el registro donde debe quedar la division
 	//esa el mismo eax, no lo puedo sobreescribir
 	if (dividend.compareTo("edx") == 0)
 	    code += add(this.stackp,this.stackAlig);
 	else{
 	    code += mov(dividend,"edx");
 	    code += pop("edx");
 	}
 	return code;
     }
 
     //Suma de flotantes
     //Para que esto funcione dst y op2 deben ser registros
     public String fadd(String dst, String op2){
 	String code = "";
 	String size;
 	
 	if (this.bits == 32)
 	    size = "dword";
 	else
 	    size = "qword";
 	
 	code += mov("["+this.stackp+"]",dst);
 	code += fld("["+this.stackp+"]",size);
 	code += mov("["+this.stackp+"]",op2);
 	code += fld("["+this.stackp+"]",size);
 	code += "fadd st0,st1\n";
 	code += fstp("["+this.stackp+"]",size);
 	code += mov(dst,"["+this.stackp+"]");
 	code += fninit();
 	
 	return code;
     }
 
     //Resta de flotantes
     public String fsub(String dst, String op2){
 	String code = "";
 	String size;
 	
 	if (this.bits == 32)
 	    size = "dword";
 	else
 	    size = "qword";
 	
 	code += mov("["+this.stackp+"]",dst);
 	code += fld("["+this.stackp+"]",size);
 	code += mov("["+this.stackp+"]",op2);
 	code += fld("["+this.stackp+"]",size);
 	code += "fsub st0,st1\n";
 	code += fstp("["+this.stackp+"]",size);
 	code += mov(dst,"["+this.stackp+"]");
 	code += fninit();
 	
 	return code;
     }
     
     //Multiplicacion de flotantes
     public String fmul(String dst, String op2){
 	String code = "";
 	String size;
 	
 	if (this.bits == 32)
 	    size = "dword";
 	else
 	    size = "qword";
 	
 	code += mov("["+this.stackp+"]",dst);
 	code += fld("["+this.stackp+"]",size);
 	code += mov("["+this.stackp+"]",op2);
 	code += fld("["+this.stackp+"]",size);
 	code += "fmul st0,st1\n";
 	code += fstp("["+this.stackp+"]",size);
 	code += mov(dst,"["+this.stackp+"]");
 	code += fninit();
 	
 	return code;
     }
 
     //Multiplicacion de flotantes
     public String fdiv(String dst, String op2){
 	String code = "";
 	String size;
 	
 	if (this.bits == 32)
 	    size = "dword";
 	else
 	    size = "qword";
 	
 	code += mov("["+this.stackp+"]",dst);
 	code += fld("["+this.stackp+"]",size);
 	code += mov("["+this.stackp+"]",op2);
 	code += fld("["+this.stackp+"]",size);
 	code += "fdiv st0,st1\n";
 	code += fstp("["+this.stackp+"]",size);
 	code += mov(dst,"["+this.stackp+"]");
 	code += fninit();
 	
 	return code;
     }
 
     public String and(String a, String b){
 	return "and "+a+","+b+"\n";
     }
 
     public String or(String a, String b){
 	return "or "+a+","+b+"\n";
     }
 
     public String xor(String a, String b){
 	return "xor "+a+","+b+"\n";
     }
 
     public String fcom(){
 	return "ficom\n";
     }
 
     public String ficom(String integer,String size){
 	return "ficom "+size+" "+integer+"\n";
     }
 
     public String cmp(String a, String b){
 	return "cmp "+a+","+b+"\n";
     }
 
     public String compare(String a, String b, Type at, Type bt)
 	throws InvalidCodeException{
 	String code = "";
 	String size;
 	if (this.bits == 32)
 	    size = "dword";
 	else
 	    size = "qword";
 
 	if (at instanceof IntType){
 	    if (bt instanceof IntType)
 		return cmp(a,b);
 	    else if (bt instanceof FloatType){
 		code += fld(b,size);
 		code += ficom(a,size);
 		code += fninit();
 		return code;
 	    }
 	}
 	else if (at instanceof FloatType){
 	    if (bt instanceof IntType){
 		code += fld(a,size);
 		code += ficom(b,size);
 		code += fninit();
 		return code;
 	    }
 	    else if (bt instanceof FloatType){
 		code += fld(a,size);
 		code += fld(b,size);
 		code += fcom();
 		code += fninit();
 		return code;
 	    }
 	}
 	else
 	    throw new InvalidCodeException("No se pueden comparar "+
 					   at+
 					   "contra "+
 					   bt);
 
 	return "";
     }
     
     public String jump(String label){
         return "jmp " + label + "\n";
     }
     
     public String jz(String label){
 	return "jz "+label+"\n";
     }
 
     public String jnz(String label){
 	return "jnz "+label+"\n";
     }
 
     public String je(String label){
 	return "je "+label+"\n";
     }
 
     public String jne(String label){
 	return "je "+label+"\n";
     }
 
     public String jg(String label){
 	return "jg "+label+"\n";
     }
 
     public String jge(String label){
 	return "jge "+label+"\n";
     }
 
     public String jl(String label){
 	return "jl "+label+"\n";
     }
 
     public String jle(String label){
 	return "jle "+label+"\n";
     }
 
     public String call(String label){
 	return "call "+label+"\n";
     }
 
     public String ret(){
 	return "ret\n";
     }
 
     public String syscall(){
 	if (this.bits == 32)
 	    return "int 80h\n";
 	else
 	    return "syscall\n";
     }
 
     public String save(int reg) throws InvalidCodeException{
 	if (reg > n_regs)
 	    if (this.bits == 32)
 		return pushw(regId(reg));
 	    else
 		return pushq(regId(reg));
 	else
 	    return "";
     }
 
     public String restore(int reg) throws InvalidCodeException{
 	if (reg > n_regs)
 	    if (this.bits == 32)
 		return pop(regId(reg),"dword");
 	    else
 		return pop(regId(reg),"qword");
 	else
 	    return "";
     }
 
     public String save_print_regs() throws InvalidCodeException{
 	String code = "";
 	if (this.bits == 32){
 	    code += pushw("eax");//Codigo del syscall
 	    code += pushw("ebx");//Descriptor
 	    code += pushw("ecx");//Direccion del string
 	    code += pushw("edx");//Cantidad a imprimir
 	}
 	else{
 	    code += pushq("rax");//Codigo del syscall
 	    code += pushq("rdi");//Descriptor
 	    code += pushq("rsi");//Direccion del string
 	    code += pushq("rdx");//Cantidad a imprimir
 	    code += pushq("rcx");//Registro que destruye el kernel
 	    code += pushq("r11");//Registro que destruye el kernel
 	}
 	return code;
     }
 
     public String restore_print_regs(){
 	String code = "";
 	if (this.bits == 32){
 	    code += pop("edx");
 	    code += pop("ecx");
 	    code += pop("ebx");
 	    code += pop("eax");
 	}
 	else{
 	    code += pop("r11");
 	    code += pop("rcx");
 	    code += pop("rdx");
 	    code += pop("rsi");
 	    code += pop("rdi");
 	    code += pop("rax");
 	}
 	return code;
     }
 
     //El llamado guarda los registros rbx y r12-r15 para x86_64
     //El llamado guarda los registros eax,ecx y edx para x86
     public String save_regs_callee()
 	throws InvalidCodeException{
 	String code = "";
 
 	if (this.bits == 32){
 	    //eax
 	    code += push(regs[0]);
 
 	    //ecx
 	    code += push(regs[2]);
 
 	    //edx
 	    code += push(regs[3]);
 	}
 	else{
 	    //rbx
 	    code += push(regs[1]);
 	    
 	    //r12 - r15
 	    for(int i=10; i<=13 ; ++i){
 		code += push(regs[i]);
 	    }
 	}
 	
 	return code;
     }
 
     //El llamado guarda los registros rbx y r12-r15 para x86_64
     //El llamado guarda los registros eax,ecx y edx para x86
     public String restore_regs_callee()
 	throws InvalidCodeException{
 	String code = "";
 
 	if (this.bits == 32){
 	    //edx
 	    code += pop(regs[3]);
 	    
 	    //ecx
 	    code += pop(regs[2]);
 	    
 	    //eax
 	    code += pop(regs[0]);
 	}
 	else{	    
 	    //r12 - r15
 	    for(int i=13; i >= 10; --i){
 		code += pop(regs[i]);
 	    }
 
 	    //rbx
 	    code += pop(regs[1]);
 	}
 	
 	return code;	
     }
 
     /*El llamador guarda los registros rax, rcx, rdx, rsi, rdi
       y r8-r11 para x86_64.*/
     //El llamador solo guarda los registros ebx, esi y edi para x86
     public String save_regs_caller(int reg)
 	throws InvalidCodeException{
 	String code = "";
 
 	if (this.bits == 32){
 	    //ebx
 	    if (reg > 1)
 		code += push(regs[1]);
 
 	    //esi
 	    if (reg > 4)
 		code += push(regs[4]);
 
 	    //edi
 	    if (reg > 5)
 		code += push(regs[5]);
 	}
 	else{
 	    //rax
 	    if (reg > 0) 
 		code += push(regs[0]);
 
 	    //rcx - r11
 	    for(int i=2; i<reg && i<=9 ; ++i){
 		code += push(regs[i]);
 	    }
 	}
 	
 	return code;
     }
 
     /*El llamador guarda los registros rax, rcx, rdx, rsi, rdi
       y r8-r11 para x86_64.*/
     //El llamador solo guarda los registros ebx, esi y edi para x86
     public String restore_regs_caller(int reg)
 	throws InvalidCodeException{
 	String code = "";
 
 	if (this.bits == 32){
 	    //ebx
 	    if (reg > 5)
 		code += pop(regs[5]);
 
 	    //esi
 	    if (reg > 4)
 		code += pop(regs[4]);
 
 	    //edi
 	    if (reg > 1)
 		code += pop(regs[1]);
 	}
 	else{	    
 	    //rcx - r11
 	    for(int i=min(9,reg); i >= 2; --i){
 		code += pop(regs[i]);
 	    }
 
 	    //rax
 	    if (reg > 0) 
 		code += pop(regs[0]);
 	}
 	
 	return code;	
     }
 
     //El addr, si es un label, debe venir sin corchetes
     public String setup_print(String addr, String desc, String bytes){
 	String code = "";
 
 	if (this.bits == 32){
 	    code += mov("eax","4"); //Codigo de sys_write 32 bits
 	    code += mov("ebx",desc); //Descriptor a donde escribir (1=stdout)
 	    code += mov("ecx",addr); //Direccion donde comienza el valor a imprimir
 	    code += mov("edx",bytes); //Cantidad de bytes que se van a imprimir
 	}
 	else{
 	    code += mov("rax","1"); //Codigo de sys_write 64 bits
 	    code += mov("rdi",desc); //Descriptor a donde escribir (1=stdout)
 	    code += mov("rsi",addr); //Direccion donde comienza el valor a imprimir
 	    code += mov("rdx",bytes); //Cantidad de bytes que se van a imprimir
 	}
 	return code;
     }
 
     public void exitSyscall(int exit_code) throws IOException {
 	String code = "\n";
 	String e_code = Integer.toString(exit_code);
 
 	if (this.bits == 64){
 	    code += mov("rax","60");
 	    code += mov("rdi",e_code);
 	}
 	else{
 	    code += mov("eax","1");
 	    code += mov("ebx",e_code);
 	}
 	code += syscall();
 
 	write(code);
     }
 
     public String toASCII(char C){
 	return DataTranslator.toASCII(C);
     }
 
     public String toReal(double F){
 	if (bits == 32)
 	    return DataTranslator.toReal((float)F);
 	else
 	    return DataTranslator.toReal(F);
     }
 
     public String toReal(float F){
 	return DataTranslator.toReal(F);
     }
 
     public int min(int a,int b){
 	if (a>b)
 	    return b;
 	else
 	    return a;
     }
 }
