 /*
 Processor.java
 Michael Black, 6/10
 
 Simulates the x86 processor
 */
 package simulator;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Processor
 {
 public ProcessorGUICode processorGUICode;
 private int instructionCount=0;
 private Computer computer;
 
 
 //registers
 public Register eax, ebx, edx, ecx, esi, edi, esp, ebp, eip;
 public Register cr0, cr2, cr3, cr4;
 
 public Segment cs, ds, ss, es, fs, gs;
 public Segment idtr, gdtr, ldtr, tss;
 
 //flags
 public Flag carry, parity, auxiliaryCarry, zero, sign, trap, interruptEnable, direction, overflow, interruptEnableSoon, ioPrivilegeLevel1, ioPrivilegeLevel0, nestedTask, alignmentCheck, idFlag;
 
 //protection level
 public int current_privilege_level;
 
 public int interruptFlags;
 
 //devices
 public IOPorts ioports;
 public InterruptController interruptController;
 
 private boolean addressDecoded=false;
 private boolean haltMode=false;
 public int lastInterrupt=-1;
 
 public LinearMemory linearMemory;
 
 public Processor(Computer computer)
 {
 	this.computer=computer;
 	this.ioports=computer.ioports;
 	interruptController=null;
 
 	processorGUICode=null;
 	
 	linearMemory=new LinearMemory(computer);
 
 	cs=new Segment(Segment.CS,computer.physicalMemory);
 	ds=new Segment(Segment.DS,computer.physicalMemory);
 	ss=new Segment(Segment.SS,computer.physicalMemory);
 	es=new Segment(Segment.ES,computer.physicalMemory);
 	fs=new Segment(Segment.FS,computer.physicalMemory);
 	gs=new Segment(Segment.GS,computer.physicalMemory);
 
 	idtr=new Segment(Segment.IDTR,computer.physicalMemory);
 //	idtr=new Segment(Segment.IDTR,linearMemory);
  	idtr.setDescriptorValue(0);
 	gdtr=new Segment(Segment.GDTR,computer.physicalMemory);
 //	gdtr=new Segment(Segment.GDTR,linearMemory);
 	gdtr.setDescriptorValue(0);
 	ldtr=new Segment(Segment.LDTR,linearMemory);
 	ldtr.setDescriptorValue(0);
 	tss=new Segment(Segment.TSS,linearMemory);
 	tss.setDescriptorValue(0);
 
 	eax=new Register(Register.EAX,0);
 	ebx=new Register(Register.EBX,0);
 	ecx=new Register(Register.ECX,0);
 	edx=new Register(Register.EDX,0);
 	esp=new Register(Register.ESP,0);
 	ebp=new Register(Register.EBP,0);
 	esi=new Register(Register.ESI,0);
 	edi=new Register(Register.EDI,0);
 	eip=new Register(Register.EIP,0x0000fff0);
 
 	cr0=new Register(Register.CR0,0);
 	cr2=new Register(Register.CR2,0);
 	cr3=new Register(Register.CR3,0);
 	cr4=new Register(Register.CR4,0);
 
 	carry=new Flag(Flag.CARRY);
 	parity=new Flag(Flag.PARITY);
 	auxiliaryCarry=new Flag(Flag.AUXILIARYCARRY);
 	zero=new Flag(Flag.ZERO);
 	sign=new Flag(Flag.SIGN);
 	trap=new Flag(Flag.OTHER);
 	interruptEnable=new Flag(Flag.OTHER);
 	direction=new Flag(Flag.OTHER);
 	overflow=new Flag(Flag.OVERFLOW);
 	interruptEnableSoon=new Flag(Flag.OTHER);
 	ioPrivilegeLevel1=new Flag(Flag.OTHER);
 	ioPrivilegeLevel0=new Flag(Flag.OTHER);
 	nestedTask=new Flag(Flag.OTHER);
 	alignmentCheck=new Flag(Flag.OTHER);
 	idFlag=new Flag(Flag.OTHER);
 	setCPL(0);
 //	current_privilege_level=0;
 
 	fetchQueue=new FetchQueue();
 
 	initializeDecoder();
 
 	
 	reset();
 }
 
 public String saveState()
 {
 	String state="";
 	state+=cs.saveState()+":";
 	state+=ss.saveState()+":";
 	state+=ds.saveState()+":";
 	state+=es.saveState()+":";
 	state+=fs.saveState()+":";
 	state+=gs.saveState()+":";
 	state+=idtr.saveState()+":";
 	state+=gdtr.saveState()+":";
 	state+=ldtr.saveState()+":";
 	state+=tss.saveState()+":";
 
 	state+=eax.saveState()+":";
 	state+=ebx.saveState()+":";
 	state+=ecx.saveState()+":";
 	state+=edx.saveState()+":";
 	state+=esp.saveState()+":";
 	state+=ebp.saveState()+":";
 	state+=esi.saveState()+":";
 	state+=edi.saveState()+":";
 	state+=eip.saveState()+":";
 	state+=cr0.saveState()+":";
 	state+=cr2.saveState()+":";
 	state+=cr3.saveState()+":";
 	state+=cr4.saveState()+":";
 
 	state+=carry.saveState()+":";
 	state+=parity.saveState()+":";
 	state+=auxiliaryCarry.saveState()+":";
 	state+=zero.saveState()+":";
 	state+=sign.saveState()+":";
 	state+=trap.saveState()+":";
 	state+=interruptEnable.saveState()+":";
 	state+=direction.saveState()+":";
 	state+=overflow.saveState()+":";
 	state+=interruptEnableSoon.saveState()+":";
 	state+=ioPrivilegeLevel1.saveState()+":";
 	state+=ioPrivilegeLevel0.saveState()+":";
 	state+=nestedTask.saveState()+":";
 	state+=alignmentCheck.saveState()+":";
 	state+=idFlag.saveState()+":";
 	state+=interruptFlags;
 
 	return state;
 }
 
 public void loadState(String state)
 {
 	String[] states=state.split(":");
 	cs.loadState(states[0]); ss.loadState(states[1]); ds.loadState(states[2]); es.loadState(states[3]); fs.loadState(states[4]); gs.loadState(states[5]); idtr.loadState(states[6]); gdtr.loadState(states[7]); ldtr.loadState(states[8]); tss.loadState(states[9]);
 	eax.loadState(states[10]); ebx.loadState(states[11]); ecx.loadState(states[12]); edx.loadState(states[13]); esp.loadState(states[14]); ebp.loadState(states[15]); esi.loadState(states[16]); edi.loadState(states[17]); eip.loadState(states[18]); cr0.loadState(states[19]); cr2.loadState(states[20]); cr3.loadState(states[21]); cr4.loadState(states[22]);
 	carry.loadState(states[23]); parity.loadState(states[24]); auxiliaryCarry.loadState(states[25]); zero.loadState(states[26]); sign.loadState(states[27]); trap.loadState(states[28]); interruptEnable.loadState(states[29]); direction.loadState(states[30]); overflow.loadState(states[31]); interruptEnableSoon.loadState(states[32]); ioPrivilegeLevel1.loadState(states[33]); ioPrivilegeLevel0.loadState(states[34]); nestedTask.loadState(states[35]); alignmentCheck.loadState(states[36]); idFlag.loadState(states[37]);
 	interruptFlags=new Scanner(states[38]).nextInt();
 }
 
 public void reset()
 {
 	linearMemory=new LinearMemory(computer);
 
 	eax.setValue(0);
 	ebx.setValue(0);
 	ecx.setValue(0);
 	edx.setValue(0);
 	esp.setValue(0);
 	ebp.setValue(0);
 	esi.setValue(0);
 	edi.setValue(0);
 	eip.setValue(0x0000fff0);
 
 	carry.clear();
 	parity.clear();
 	auxiliaryCarry.clear();
 	zero.clear();
 	sign.clear();
 	trap.clear();
 	interruptEnable.clear();
 	direction.clear();
 	overflow.clear();
 	interruptEnableSoon.clear();
 	ioPrivilegeLevel1.clear();
 	ioPrivilegeLevel0.clear();
 	nestedTask.clear();
 
 	interruptFlags=0;
 
 	cs.setValue(0xf000);
 	ds.setValue(0);
 	ss.setValue(0);
 	es.setValue(0);
 	fs.setValue(0);
 	gs.setValue(0);
 
 
 	parity.set();
 	zero.set();
 
 	setCR0(0x60000010);
 	setCR2(0);
 	setCR3(0);
 	setCR4(0);
 /*	cr0.setValue(0x60000010);
 	cr2.setValue(0);
 	cr3.setValue(0);
 	cr4.setValue(0);*/
 	
 	haltMode=false;
 }
 
 public void setInterruptController(InterruptController interruptController)
 {
 	this.interruptController=interruptController;
 }
 
 //executes a single instruction
 public void executeAnInstruction()
 {
 	processorGUICode=null;
 //	if (computer.processorGUI!=null || computer.memoryGUI!=null || computer.registerGUI!=null)
 //	{
 		if (computer.debugMode || computer.updateGUIOnPlay || computer.trace!=null)
 			processorGUICode=new ProcessorGUICode();
 //	}
 
 	if (haltMode)
 	{ 
 		if (processorGUICode!=null) processorGUICode.push(GUICODE.EXECUTE_HALT);
 		try{Thread.sleep(10);}catch(InterruptedException e){}
 		return;
 	}
 
 	if(processorGUICode!=null) processorGUICode.pushFetch(eip.getValue());
 
 	fetchQueue.fetch();
 	decodeInstruction(cs.getDefaultSize());
 	executeInstruction();
 
 	if(processorGUICode!=null) 
 	{
 		if (computer.trace!=null) computer.trace.addProcessorCode(processorGUICode);
 		processorGUICode.updateMemoryGUI();
 		processorGUICode.updateGUI();
 	}
 }
 
 public void printRegisters()
 {
 //	System.out.printf("IP: %x, AX: %x, BX: %x, CX: %x, DX: %x, SI: %x, DI: %x, SP: %x, BP: %x, CS: %x, CSbase: %x, SS: %x, DS: %x, ES: %x, FLAGS: %x\n",eip.getValue(),eax.getValue(),ebx.getValue(),ecx.getValue(),edx.getValue(),esi.getValue(),edi.getValue(),esp.getValue(),ebp.getValue(),cs.getValue(),cs.getBase(),ss.getValue(),ds.getValue(),es.getValue(),getFlags());
 	System.out.printf("IP: %x\n",eip.getValue());
 	System.out.printf("@ AX: %x, BX: %x, CX: %x, DX: %x, SI: %x, DI: %x, SP: %x, BP: %x, CS: %x, SS: %x, DS: %x, ES: %x\n",eax.getValue(),ebx.getValue(),ecx.getValue(),edx.getValue(),esi.getValue(),edi.getValue(),esp.getValue(),ebp.getValue(),cs.getValue(),ss.getValue(),ds.getValue(),es.getValue());
 
 }
 
 public void printMicrocode(int[] code)
 {
 	System.out.print("Microcode: ");
 	for(int i=0; i<code.length; i++)
 		System.out.printf("%x ",code[i]);
 	System.out.println();
 }
 
 public void setCR0(int value)
 {
 	value|=0x10;
 	if (value==cr0.getValue()) return;
 	int changedBits=cr0.getValue()^value;
 	cr0.setValue(value);
 	if (isModeReal())
 	{
 		setCPL(0);
 		cs.memory=computer.physicalMemory;
 		ss.memory=computer.physicalMemory;
 		ds.memory=computer.physicalMemory;
 		es.memory=computer.physicalMemory;
 		fs.memory=computer.physicalMemory;
 		gs.memory=computer.physicalMemory;
 		
 //		current_privilege_level=0;
 		if(processorGUICode!=null) processorGUICode.push(GUICODE.MODE_REAL);
 		
 //		System.out.println("Switching to Real Mode");
 //		System.out.printf("New segment bases: CS: %x, SS: %x, DS: %x, ES: %x, FS: %x, GS: %x\n",cs.getBase(),ss.getBase(),ds.getBase(),es.getBase(),fs.getBase(),gs.getBase());
 	}
 	else
 	{
 		cs.memory=linearMemory;
 		ss.memory=linearMemory;
 		ds.memory=linearMemory;
 		es.memory=linearMemory;
 		fs.memory=linearMemory;
 		gs.memory=linearMemory;
 		if(processorGUICode!=null) processorGUICode.push(GUICODE.MODE_PROTECTED);
 //		System.out.println("Switching to Protected Mode");
 //		System.out.printf("New segment bases: CS: %x, SS: %x, DS: %x, ES: %x, FS: %x, GS: %x\n",cs.getBase(),ss.getBase(),ds.getBase(),es.getBase(),fs.getBase(),gs.getBase());
 	}
 	if ((value&0x2)!=0)
 		panic("implement CR0 monitor coprocessor");
 	if ((value&0x4)!=0)
 		panic("implement CR0 fpu emulation");
 	if ((value&0x8)!=0)
 		panic("implement CR0 task switched");
 	if ((value&0x20)!=0)
 		panic("implement CR0 numeric error");
 	if ((value&0x40000)!=0)
 		panic("implement CR0 alignment mask");
 	if ((value&0x20000000)==0)
 		panic("implement CR0 writethrough");
 	if ((changedBits&0x10000)!=0)
 	{
 		panic("implement CR0 write protect");
 		linearMemory.setWriteProtectUserPages((value&0x10000)!=0);
 	}
 	if ((changedBits&0x40000000)!=0)
 	{
 		//page caching
 		linearMemory.setPagingEnabled((value&0x80000000)!=0);
 		linearMemory.setPageCacheEnabled((value&0x40000000)==0);
 	}	
 	if ((changedBits&0x80000000)!=0)
 	{
 		//paging
 		linearMemory.setPagingEnabled((value&0x80000000)!=0);
 		linearMemory.setPageCacheEnabled((value&0x40000000)==0);
 	}
 }
 public void setCR2(int value)
 {
 	cr2.setValue(value);
 	if (value!=0)
 		panic("setting CR2 to "+value);
 }
 public void setCR3(int value)
 {
 	cr3.setValue(value);
 	linearMemory.setPageDirectoryBaseAddress(value);
 	linearMemory.setPageCacheEnabled((value&0x10)==0);
 //	if ((value&0x8)==0)
 //		panic("implement CR3 writes transparent");
 }
 public void setCR4(int value)
 {
 	cr4.setValue((cr4.getValue()&~0x5f)|(value&0x5f));
 	linearMemory.setGlobalPagesEnabled((value&0x80)!=0);
 	linearMemory.setPageSizeExtensionsEnabled((value&0x10)!=0);
 }
 
 public boolean isModeReal()
 {
 	if((cr0.getValue()&1)==0) return true;
 	return false;
 }
 
 private void setCPL(int cpl)
 {
 	current_privilege_level=cpl;
 	linearMemory.setSupervisor(cpl==0);
 }
 
 //models a register
 //public class Register extends MonitoredRegister
 public class Register
 {
 	static final int EAX=100, EBX=101, ECX=102, EDX=103, ESI=104, EDI=105, ESP=106, EBP=107, CR0=108, CR2=109, CR3=110, CR4=111, EIP=112;
 	int id;
 	int value;
 	
 	public String saveState()
 	{
 		String state="";
 		state+=id+" "+value;
 		return state;
 	}
 	public void loadState(String state)
 	{
 		Scanner loader=new Scanner(state);
 		id=loader.nextInt(); value=loader.nextInt();
 	}	
 
 	public Register(int id)
 	{
 		this.id=id;
 		this.value=0;
 //		super(0);
 	}
 	public Register(int id, int value)
 	{
 		this.id=id;
 		this.value=value;
 //		super(value);
 	}
 	public int getValue()
 	{
 //		super.updateGUI();
 		if(processorGUICode!=null) processorGUICode.pushRegister(id,0,value);
 		return value;
 	}
 	public void setValue(int value)
 	{
 //		super.updateGUI();
 		if(processorGUICode!=null) processorGUICode.pushRegister(id,1,value);
 		this.value=value;
 	}
 	public short getLower16Value()
 	{
 		return (short)(getValue()&0xffff);
 	}
 	public byte getLower8Value()
 	{
 		return (byte)(getValue()&0xff);
 	}
 	public byte getUpper8Value()
 	{
 		return (byte)((getValue()>>8)&0xff);
 	}
 	public void setLower16Value(int v)
 	{
 		setValue((getValue()&0xffff0000)|(0x0000ffff&v));
 	}
 	public void setLower8Value(int v)
 	{
 		setValue((getValue()&0xffffff00)|(0x000000ff&v));
 	}
 	public void setUpper8Value(int v)
 	{
 		setValue((getValue()&0xffff00ff)|(0x0000ff00&(v<<8)));
 	}
 }
 
 //public class Flag extends MonitoredFlag
 public class Flag
 {
 	int type;
 
 	public static final int AUXILIARYCARRY=100, CARRY=101, ZERO=102, SIGN=103, PARITY=104, OVERFLOW=105, OTHER=106;
 
 	public static final int AC_XOR = 1;
 	public static final int AC_BIT4_NEQ = 2;
 	public static final int AC_LNIBBLE_MAX = 3;
 	public static final int AC_LNIBBLE_ZERO = 4;
 	public static final int AC_LNIBBLE_NZERO = 5;
 	public static final int OF_NZ = 1;
 	public static final int OF_NOT_BYTE = 2;
 	public static final int OF_NOT_SHORT = 3;
 	public static final int OF_NOT_INT = 3;
 	public static final int OF_LOW_WORD_NZ = 5;
 	public static final int OF_HIGH_BYTE_NZ = 6;
 	public static final int OF_BIT6_XOR_CARRY = 7;
 	public static final int OF_BIT7_XOR_CARRY = 8;
 	public static final int OF_BIT14_XOR_CARRY = 9;
 	public static final int OF_BIT15_XOR_CARRY = 10;
 	public static final int OF_BIT30_XOR_CARRY = 11;
 	public static final int OF_BIT31_XOR_CARRY = 12;
 	public static final int OF_BIT7_DIFFERENT = 13;
 	public static final int OF_BIT15_DIFFERENT = 14;
 	public static final int OF_BIT31_DIFFERENT = 15;
 	public static final int OF_MAX_BYTE = 16;
 	public static final int OF_MAX_SHORT = 17;
  	public static final int OF_MAX_INT = 18;
  	public static final int OF_MIN_BYTE = 19;
 	public static final int OF_MIN_SHORT = 20;
 	public static final int OF_MIN_INT = 21;
 	public static final int OF_ADD_BYTE = 22;
 	public static final int OF_ADD_SHORT = 23;
 	public static final int OF_ADD_INT = 24;
 	public static final int OF_SUB_BYTE = 25;
 	public static final int OF_SUB_SHORT = 26;
 	public static final int OF_SUB_INT = 27;
 	public static final int CY_NZ = 1;
 	public static final int CY_NOT_BYTE = 2;
 	public static final int CY_NOT_SHORT = 3;
 	public static final int CY_NOT_INT = 4;
 	public static final int CY_LOW_WORD_NZ = 5;
 	public static final int CY_HIGH_BYTE_NZ = 6;
 	public static final int CY_NTH_BIT_SET = 7;
 	public static final int CY_GREATER_FF = 8;
 	public static final int CY_TWIDDLE_FF = 9;
 	public static final int CY_TWIDDLE_FFFF = 10;
 	public static final int CY_TWIDDLE_FFFFFFFF = 11;
 	public static final int CY_SHL_OUTBIT_BYTE = 12;
 	public static final int CY_SHL_OUTBIT_SHORT = 13;
 	public static final int CY_SHL_OUTBIT_INT = 14;
 	public static final int CY_SHR_OUTBIT = 15;
 	public static final int CY_LOWBIT = 16;
 	public static final int CY_HIGHBIT_BYTE = 17;
 	public static final int CY_HIGHBIT_SHORT = 18;
 	public static final int CY_HIGHBIT_INT = 19;
 	public static final int CY_OFFENDBIT_BYTE = 20;
 	public static final int CY_OFFENDBIT_SHORT = 21;
 	public static final int CY_OFFENDBIT_INT = 22;
 
 	private boolean value;
 	private int v1, v2, v3, method;
 	private long v1long;
 
 	public String saveState()
 	{
 		String state="";
 		state+=type+" "+(value?1:0)+" "+v1+" "+v2+" "+v3+" "+method+" "+v1long;
 		return state;
 	}
 	public void loadState(String state)
 	{
 		Scanner loader=new Scanner(state);
 		type=loader.nextInt(); value=loader.nextInt()==1; v1=loader.nextInt(); v2=loader.nextInt(); v3=loader.nextInt(); method=loader.nextInt(); v1long=loader.nextLong();
 	}
 	
 	public Flag(int type)
 	{
 //		super(false);
 		this.type=type;
 	}
 	public void clear()
 	{
 		if(processorGUICode!=null) processorGUICode.pushFlag(type,0);
 		value=false;
 //		super.updateGUI();
 	}
 	public void set()
 	{
 		if(processorGUICode!=null) processorGUICode.pushFlag(type,1);
 		value=true;
 //		super.updateGUI();
 	}
 	public boolean read()
 	{
 //		super.updateGUI();
 		if(processorGUICode!=null) processorGUICode.pushFlag(type,2);
 		return value;
 	}
 
 	public void toggle()
 	{
 		if (value)
 			clear();
 		else
 			set();
 	}
 	public void set(boolean value)
 	{
 		if (value)
 			set();
 		else
 			clear();
 	}
 
 	private void calculate()
 	{
 		switch(type)
 		{
 			case AUXILIARYCARRY: calculateAuxiliaryCarry(); break;
 			case ZERO: calculateZero(); break;
 			case OVERFLOW: calculateOverflow(); break;
 			case PARITY: calculateParity(); break;
 			case SIGN: calculateSign(); break;
 			case CARRY: calculateCarry(); break;
 		}
 	}
 
 	private void calculateAuxiliaryCarry()
 	{
 		if (method==AC_XOR)
 			set((((v1^v2)^v3)&0x10)!=0);
 		else if (method==AC_LNIBBLE_MAX)
 			set((v1&0xf)==0xf);
 		else if (method==AC_LNIBBLE_ZERO)
 			set((v1&0xf)==0x0);
 		else if (method==AC_BIT4_NEQ)
 			set((v1&0x8)!=(v2&0x8));
 		else if (method==AC_LNIBBLE_NZERO)
 			set((v1&0xf)!=0);
 	}
 
 	private void calculateParity()
 	{
 		set((Integer.bitCount(v1&0xff)&0x1)==0);
 	}
 
 	private void calculateOverflow()
 	{
 		if (method==OF_ADD_BYTE)
 			set(((v2&0x80)==(v3&0x80))&&((v2&0x80)!=(v1&0x80)));
 		else if (method==OF_ADD_SHORT)
 			set(((v2&0x8000)==(v3&0x8000))&&((v2&0x8000)!=(v1&0x8000)));
 		else if (method==OF_ADD_INT)
 			set(((v2&0x80000000)==(v3&0x80000000))&&((v2&0x80000000)!=(v1&0x80000000)));
 		else if (method==OF_SUB_BYTE)
 			set(((v2&0x80)!=(v3&0x80))&&((v2&0x80)!=(v1&0x80)));
 		else if (method==OF_SUB_SHORT)
 			set(((v2&0x8000)!=(v3&0x8000))&&((v2&0x8000)!=(v1&0x8000)));
 		else if (method==OF_SUB_INT)
 			set(((v2&0x80000000)!=(v3&0x80000000))&&((v2&0x80000000)!=(v1&0x80000000)));
 		else if (method==OF_MAX_BYTE)
 			set(v1==0x7f);
 		else if (method==OF_MIN_BYTE)
 			set(v1==(byte)0x80);
 		else if (method==OF_MAX_SHORT)
 			set(v1==0x7fff);
 		else if (method==OF_MIN_SHORT)
 			set(v1==(short)0x8000);
 		else if (method==OF_MAX_INT)
 			set(v1==0x7fffffff);
 		else if (method==OF_MIN_INT)
 			set(v1==0x80000000);
 		else if (method==OF_BIT6_XOR_CARRY)
 			set(((v1&0x40)!=0)^carry.read());
 		else if (method==OF_BIT7_XOR_CARRY)
 			set(((v1&0x80)!=0)^carry.read());
 		else if (method==OF_BIT14_XOR_CARRY)
 			set(((v1&0x4000)!=0)^carry.read());
 		else if (method==OF_BIT15_XOR_CARRY)
 			set(((v1&0x8000)!=0)^carry.read());
 		else if (method==OF_BIT30_XOR_CARRY)
 			set(((v1&0x40000000)!=0)^carry.read());
 		else if (method==OF_BIT31_XOR_CARRY)
 			set(((v1&0x80000000)!=0)^carry.read());
 		else if (method==OF_BIT7_DIFFERENT)
 			set((v1&0x80)!=(v2&0x80));
 		else if (method==OF_BIT15_DIFFERENT)
 			set((v1&0x8000)!=(v2&0x8000));
 		else if (method==OF_BIT31_DIFFERENT)
 			set((v1&0x80000000)!=(v2&0x80000000));
 		else if (method==OF_NZ)
 			set(v1!=0);
 		else if (method==OF_NOT_BYTE)
 			set(v1!=(byte)v1);
 		else if (method==OF_NOT_SHORT)
 			set(v1!=(short)v1);
 		else if (method==OF_NOT_INT)
 			set(v1long!=(int)v1long);
 		else if (method==OF_LOW_WORD_NZ)
 			set((v1&0xffff)!=0);
 		else if (method==OF_HIGH_BYTE_NZ)
 			set((v1&0xff00)!=0);
 	}
 
 	private void calculateCarry()
 	{
 		if (method==CY_TWIDDLE_FF)
 			set((v1&(~0xff))!=0);
 		else if (method==CY_TWIDDLE_FFFF)
 			set((v1&(~0xffff))!=0);
 		else if (method==CY_TWIDDLE_FFFFFFFF)
 			set((v1long&(~0xffffffffl))!=0);
 		else if (method==CY_SHR_OUTBIT)
 			set(((v1>>>(v2-1))&0x1)!=0);
 		else if (method==CY_SHL_OUTBIT_BYTE)
 			set(((v1<<(v2-1))&0x80)!=0);
 		else if (method==CY_SHL_OUTBIT_SHORT)
 			set(((v1<<(v2-1))&0x8000)!=0);
 		else if (method==CY_SHL_OUTBIT_INT)
 			set(((v1<<(v2-1))&0x80000000)!=0);
 		else if (method==CY_NZ)
 			set(v1!=0);
 		else if (method==CY_NOT_BYTE)
 			set(v1!=(byte)v1);
 		else if (method==CY_NOT_SHORT)
 			set(v1!=(short)v1);
 		else if (method==CY_NOT_INT)
 			set(v1long!=(int)v1long);
 		else if (method==CY_LOW_WORD_NZ)
 			set((v1&0xffff)!=0);
 		else if (method==CY_HIGH_BYTE_NZ)
 			set((v1&0xff00)!=0);
 		else if (method==CY_NTH_BIT_SET)
 			set((v1&(1<<v2))!=0);
 		else if (method==CY_GREATER_FF)
 			set(v1>0xff);
 		else if (method==CY_LOWBIT)
 			set((v1&0x1)!=0);
 		else if (method==CY_HIGHBIT_BYTE)
 			set((v1&0x80)!=0);
 		else if (method==CY_HIGHBIT_SHORT)
 			set((v1&0x8000)!=0);
 		else if (method==CY_HIGHBIT_INT)
 			set((v1&0x80000000)!=0);
 		else if (method==CY_OFFENDBIT_BYTE)
 			set((v1&0x100)!=0);
 		else if (method==CY_OFFENDBIT_SHORT)
 			set((v1&0x10000)!=0);
 		else if (method==CY_OFFENDBIT_INT)
 			set((v1long&0x100000000l)!=0);
 	}
 
 	private void calculateZero()
 	{
 		set(v1==0);
 	}
 
 	private void calculateSign()
 	{
 		set(v1<0);
 	}
 
 	public void set(int d1, int d2, int d3, int m)
 	{
 		v1=d1;
 		v2=d2;
 		v3=d3;
 		method=m;
 		calculate();
 	}
 
 	public void set(int d1, int d2, int m)
 	{
 		v1=d1;
 		v2=d2;
 		method=m;
 		calculate();
 	}
 
 	public void set(int d1, int m)
 	{
 		v1=d1;
 		method=m;
 		calculate();
 	}
 
 	public void set(long d1, int m)
 	{
 		v1long=d1;
 		method=m;
 		calculate();
 	}
 
 	public void set(int d1)
 	{
 		v1=d1;
 		calculate();
 	}
 }
 
 //models a segment register
 //public class Segment extends MonitoredRegister
 public class Segment
 {
 	public static final int CS=100,SS=101,DS=102,ES=103,FS=104,GS=105,IDTR=106,GDTR=107,LDTR=108,TSS=109;
 
 	private int value;
 	private int id;
 	private int base;
 	private long limit;
 	private MemoryDevice memory;
 
 	long descriptor;
 	boolean granularity,defaultSize,present,system;
 	int rpl,dpl;
 
 	public String saveState()
 	{
 		String state="";
 		state+=value+" "+id+" "+base+" "+limit+" "+descriptor+" "+(granularity?1:0)+" "+(defaultSize?1:0)+" "+(present?1:0)+" "+(system?1:0)+" "+rpl+" "+dpl;
 		return state;
 	}
 	public void loadState(String state)
 	{
 		Scanner loader=new Scanner(state);
 		value=loader.nextInt(); id=loader.nextInt(); base=loader.nextInt(); limit=loader.nextLong();
 		descriptor=loader.nextLong(); granularity=loader.nextInt()==1; defaultSize=loader.nextInt()==1; present=loader.nextInt()==1; system=loader.nextInt()==1;
 		rpl=loader.nextInt(); dpl=loader.nextInt();
 	}
 	
 	public Segment(int id, MemoryDevice memory)
 	{
 //		super(0);
 		this.id=id;
 		this.memory=memory;
 		limit=0xffff;
 	}
 	
 	public void setDescriptorValue(int value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushSegment(id,1,value);
 		this.value=value;
 		this.base=value;
 		this.limit=0xffff;
 //		super.updateGUI();
 	}
 	public void setDescriptorValue(int value, int limit)
 	{
 		if(processorGUICode!=null) processorGUICode.pushSegment(id,1,value);
 		this.value=value;
 		this.base=value;
 		this.limit=limit;
 //		super.updateGUI();
 	}
 	public void setValue(int value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushSegment(id,1,value);
 		if(isModeReal())
 			setRealValue(value);
 		else
 			setProtectedValue(value);
 //		super.updateGUI();
 	}
 	public int getValue()
 	{
 		if(processorGUICode!=null) processorGUICode.pushSegment(id,0,value);
 //		super.updateGUI();
 		return value;
 	}
 	public void setRealValue(int value)
 	{
 		this.value=value;
 		this.base=(0xffff0 & (value<<4));
 		defaultSize=false;
 	}
 	public void setProtectedValue(int value)
 	{
 		//first get the descriptor
 		boolean sup=linearMemory.isSupervisor;
 		linearMemory.setSupervisor(true);
 		if ((value&4)==0)
 		{
 			descriptor=gdtr.loadQuadWord(value&0xfff8);
 		}
 		else
 		{
 			if (ldtr==null)
 			{
 				System.out.println("LDTR is null");
 				throw GENERAL_PROTECTION;
 			}
 			descriptor=ldtr.loadQuadWord(value&0xfff8);
 		}
 		setProtectedValue(value,descriptor);
 		linearMemory.setSupervisor(sup);
 	}
 
 	public void setProtectedValue(int value, long descriptor)
 	{
 		this.value=value;
 		this.descriptor=descriptor;
 
 		granularity=(descriptor&0x80000000000000l)!=0;
 		if(granularity)
 			limit=((descriptor<<12)&0xffff000l)|((descriptor>>>20)&0xf0000000l)|0xffl;
 		else
 			limit=(descriptor&0xffffl)|((descriptor>>>32)&0xf0000l);
 		base=(int)((0xffffffl & (descriptor>>16))|((descriptor>>32)&0xffffffffff000000l));
 		rpl=value&0x3;
 		dpl=(int)((descriptor>>45)&0x3);
 		defaultSize=(descriptor&(1l<<54))!=0;
 		present=(descriptor&(1l<<47))!=0;
 		system=(descriptor&(1l<<44))!=0;
 		
 		if (id==CS && !defaultSize)
 			System.out.println("operating in 16 bit protected mode "+descriptor+" "+value);
 		else if (id==CS)
 			System.out.println("operating in 32 bit protected mode "+descriptor+" "+value);
 	}
 
 	public int getBase()
 	{
 		return base;
 	}
 	public int getLimit()
 	{
 		return (int)limit;
 	}
 	//16 or 32?
 	public boolean getDefaultSize()
 	{
 		return defaultSize;
 	}
 	public int address(int offset)
 	{
 //		return (0xffff0&base)+(0xffff&offset);
 		return base+offset;
 	}
 	public int physicalAddress(int offset)
 	{
 		if(memory==computer.physicalMemory)
 			return base+offset;
 		return linearMemory.virtualAddressLookup(base+offset);
 	}
 	public byte loadByte(int offset)
 	{
 		byte memvalue=memory.getByte(address(offset));
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,value,0,address(offset),memvalue);
 		return memvalue;
 	}
 	public short loadWord(int offset)
 	{
 		short memvalue=memory.getWord(address(offset));
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,value,0,address(offset),memvalue);
 		return memvalue;
 	}
 	public int loadDoubleWord(int offset)
 	{
 		int memvalue=memory.getDoubleWord(address(offset));
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,value,0,address(offset),memvalue);
 		return memvalue;
 	}
 	public long loadQuadWord(int offset)
 	{
 		long memvalue=memory.getQuadWord(address(offset));
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,value,0,address(offset),memvalue);
 		return memvalue;
 	}
 	public void storeByte(int offset, byte value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,this.value,1,address(offset),value);
 		memory.setByte(address(offset),value);
 		fetchQueue.flush();
 	}
 	public void storeWord(int offset, short value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,this.value,1,address(offset),value);
 		memory.setWord(address(offset),value);
 		fetchQueue.flush();
 	}
 	public void storeDoubleWord(int offset, int value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,this.value,1,address(offset),value);
 		memory.setDoubleWord(address(offset),value);
 		fetchQueue.flush();
 	}
 	public void storeQuadWord(int offset, long value)
 	{
 		if(processorGUICode!=null) processorGUICode.pushMemory(id,this.value,1,address(offset),value);
 		memory.setQuadWord(address(offset),value);
 		fetchQueue.flush();
 	}
 }
 
 public int getFlags()
 {
 	int result=0x2;
 	if(carry.read()) result|=0x1;
 	if(parity.read()) result|=0x4;
 	if(auxiliaryCarry.read()) result|=0x10;
 	if(zero.read()) result|=0x40;
 	if(sign.read()) result|=0x80;
 	if(trap.read()) result|=0x100;
 	if(interruptEnable.read()) result|=0x200;
 	if(direction.read()) result|=0x400;
 	if(overflow.read()) result|=0x800;
 	if(ioPrivilegeLevel0.read()) result|=0x1000;
 	if(ioPrivilegeLevel1.read()) result|=0x2000;
 	if(nestedTask.read()) result|=0x4000;
 	if(alignmentCheck.read()) result|=0x40000;
 	if(idFlag.read()) result|=0x200000;
 	return result;
 }
 
 public void setFlags(int code)
 {
 	carry.set((code&1)!=0);
 	parity.set((code&(1<<2))!=0);
 	auxiliaryCarry.set((code&(1<<4))!=0);
 	zero.set((code&(1<<6))!=0);
 	sign.set((code&(1<<7))!=0);
 	trap.set((code&(1<<8))!=0);
 	interruptEnable.set((code&(1<<9))!=0);
 	interruptEnableSoon.set((code&(1<<9))!=0);
 	direction.set((code&(1<<10))!=0);
 	overflow.set((code&(1<<11))!=0);
 	ioPrivilegeLevel0.set((code&(1<<12))!=0);
 	ioPrivilegeLevel1.set((code&(1<<13))!=0);
 	nestedTask.set((code&(1<<14))!=0);
 	alignmentCheck.set((code&(1<<18))!=0);
 	idFlag.set((code&(1<<21))!=0);
 }
 
 public void handleProcessorException(Processor_Exception e)
 {
 	if (e.vector==PAGE_FAULT.vector)
 	{
 		setCR2(linearMemory.lastPageFaultAddress);
 		System.out.println("A page fault exception just happened: "+e.vector);
 	}
 	//REMOVE THIS LINE WHEN I'M CONFIDENT THAT EXCEPTIONS WORK
 	panic("A processor exception happened "+e.vector);
 	System.out.println("A Processor Exception just happened: "+e.vector);
 	if(processorGUICode!=null) processorGUICode.push(GUICODE.EXCEPTION,e.vector);
     handleInterrupt(e.vector);
 }
 
 public void waitForInterrupt()
 {
 	System.out.println("Halting machine (probably a PANIC happened)");
 	haltMode=true;
 }
 
 //call this periodically to accept incoming interrupts
 public void processInterrupts()
 {
 	//if disable interrupt mode, just ignore
 	if(!interruptEnable.read())
 	{
 		interruptEnable.set(interruptEnableSoon.read());
 		return;
 	}
 	//is there a hardware interrupt outstanding?
 	if((interruptFlags & 0x1) == 0)
 		return;
 		//turn off the flag
 	interruptFlags &= ~0x1;
 		//get the interrupt
 	haltMode=false;
 	lastInterrupt=interruptController.cpuGetInterrupt();
 	handleInterrupt(lastInterrupt);
 }
 
 //tell the cpu that there is a hardware interrupt ready
 public void raiseInterrupt()
 {
 //	System.out.println("raiseInterrupt called");
 	interruptFlags |= 0x1;
 }
 	//tell the cpu that there is no longer a hardware interrupt ready
 public void clearInterrupt()
 {
 	interruptFlags &= ~ 0x1;
 }
 	//deal with a real mode interrupt
 public void handleInterrupt(int vector)
 {
 	int newIP=0, newSegment=0;
 	long descriptor=0;
 
 	if (isModeReal())
 	{
 		//get the new CS:IP from the IVT
 		vector=vector*4;
 		newIP = 0xffff & idtr.loadWord(vector);
 		newSegment = 0xffff & idtr.loadWord(vector+2);		
 		//save the flags on the stack
 		short sesp = (short) esp.getValue();
 		sesp-=2;
 		int eflags = getFlags() & 0xffff;
 		ss.storeWord(sesp & 0xffff, (short)eflags);
 			//disable interrupts
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 			//save CS:IP on the stack
 		sesp-=2;
 		ss.storeWord(sesp&0xffff, (short)cs.getValue());
 		sesp-=2;
 		ss.storeWord(sesp&0xffff, (short)eip.getValue());
 		esp.setValue((0xffff0000 & esp.getValue()) | (sesp & 0xffff));
 			//change CS and IP to the ISR's values
 		cs.setValue(newSegment);
 		eip.setValue(newIP);
 	}
 	else
 	{
 		//get the new CS:EIP from the IDT
 		boolean sup=linearMemory.isSupervisor;
 		linearMemory.setSupervisor(true);
 		vector=vector*8;
 		descriptor=idtr.loadQuadWord(vector);
 		int segIndex=(int)((descriptor>>16)&0xffff);
 		if ((segIndex&4)!=0)
 			descriptor=ldtr.loadQuadWord(segIndex&0xfff8);
 		else
 			descriptor=gdtr.loadQuadWord(segIndex&0xfff8);
 		linearMemory.setSupervisor(sup);
 		
 		int dpl=(int)((descriptor>>45)&0x3);
 		newIP = (int)(((descriptor>>32)&0xffff0000)|(descriptor&0x0000ffff));
 		newSegment = (int)((descriptor>>16)&0xffff);
 		
 		if (dpl<current_privilege_level)
 		{
 			int stackAddress=dpl*8+4;
 			int newSS=0xffff&(tss.loadWord(stackAddress+4));
 			int newSP=tss.loadDoubleWord(stackAddress);
 			int oldSS=ss.getValue();
 			int oldSP=esp.getValue();
 			ss.setValue(newSS);
 			esp.setValue(newSP);
 	
 			//save SS:ESP on the stack
 			int sesp1 = esp.getValue();
 			sesp1-=4;
 			ss.storeDoubleWord(sesp1, oldSS);
 			sesp1-=4;
 			ss.storeDoubleWord(sesp1, oldSP);
 			esp.setValue(sesp1);
 		}
 		
 		//save the flags on the stack
 		int sesp=esp.getValue();
 		int eflags = getFlags();
 		ss.storeDoubleWord(sesp, eflags);
 			//disable interrupts
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 			//save CS:IP on the stack
 		sesp-=4;
 		ss.storeDoubleWord(sesp, cs.getValue());
 		sesp-=4;
 		ss.storeDoubleWord(sesp, eip.getValue());
 		esp.setValue(sesp);
 			//change CS and IP to the ISR's values
 		cs.setProtectedValue(newSegment,descriptor);
 		eip.setValue(newIP);
 		setCPL(dpl);
 //		current_privilege_level=dpl;
 	}
 
 	if(processorGUICode!=null) processorGUICode.push(GUICODE.HARDWARE_INTERRUPT,vector);
 	//System.out.printf("Hardware interrupt happened %x\n",vector);
 }
 
 public FetchQueue fetchQueue;
 
 public class FetchQueue
 {
 	private static final int PREFETCH_QUANTITY=40;
 	private static final int MAX_INST_LENGTH=16;
 
 	private byte[] bytearray;
 	private int[] iparray;
 	private boolean dofetch;
 
 	int counter;
 	int ilength;
 
 	public void fetch()
 	{
 		ilength=0;
 
 		if (!dofetch)
 		{
 			if (iparray[counter]!=eip.getValue())
 				dofetch=true;
 			if (counter>PREFETCH_QUANTITY-MAX_INST_LENGTH)
 				dofetch=true;
 		}
 		if (dofetch)
 		{
 			counter=0;
 			int pc=eip.getValue();
 			for (int i=0; i<PREFETCH_QUANTITY; i++)
 			{
 				bytearray[i]=cs.loadByte(pc+i);
 				iparray[i]=pc+i;
 			}
 			dofetch=false;
 		}
 	}
 
 	public void flush()
 	{
 		dofetch=true;
 	}
 
 	public FetchQueue()
 	{
 		bytearray=new byte[PREFETCH_QUANTITY];
 		iparray=new int[PREFETCH_QUANTITY];
 		counter=0;
 		ilength=0;
 		dofetch=true;
 	}
 	public byte readByte()
 	{
 		return bytearray[counter];
 	}
 	public byte readByte(int i)
 	{
 		return bytearray[counter+i];
 	}
 	public int instructionLength()
 	{
 		return ilength;
 	}
 	public void advance(int i)
 	{
 		if (computer.trace!=null)
 		{
 			for (int j=0; j<i; j++)
 				computer.trace.postInstructionByte(bytearray[counter+j]);
 		}
 		counter+=i;
 		ilength+=i;
 		if (counter>bytearray.length)
 			panic("Reached end of fetch queue");
 	}
 }
 
 private MICROCODE[] code = new MICROCODE[100];
 private int[] icode = new int[100];
 private int icodeLength=0;
 private int icodesHandled=0;
 public int codeLength=0;
 public int codesHandled=0;
 
 public static int[][][] operandTable;
 
 public void executeInstruction()
 {
 	//move IP to the next instruction
 	int pc = eip.getValue();
 	pc=pc+getInstructionLength();
 	eip.setValue(pc);
 
 /*	//check whether we're out of range
 	if ((pc & 0xffff0000) != 0)
 	{
 		eip.setValue(eip.getValue() - getInstructionLength());
 		throw GENERAL_PROTECTION;
 	}
 */
 	executeMicroInstructions();
 }
 
 public void executeMicroInstructions()
 {
 	//internal registers
 	int reg0=0, reg1=0, reg2=0, addr=0;
 	long reg0l=0;
 	Segment seg=null;
 	boolean condition=false;
 	int displacement=0;
 
 	codesHandled=0;
 	icodesHandled=0;
 
 	MICROCODE microcode;
 	boolean op32,addr32;
 
 	try
 	{
 	while (codesHandled < codeLength)
 	{
 	microcode=getCode();
 	op32=isCode(MICROCODE.PREFIX_OPCODE_32BIT);
 	addr32=isCode(MICROCODE.PREFIX_ADDRESS_32BIT);
 
 	if(computer.debugMode)
 	{
 		System.out.println(code[codesHandled]);
 		System.out.printf("reg0=%x, reg1=%x, addr=%x ",reg0,reg1,addr);
 		System.out.println(microcode+" op32="+" addr32="+addr32);
 	}
 
 	switch (microcode)
 	{
 	//reads and writes
 	case LOAD0_AX: reg0 = eax.getValue() & 0xffff; break;
 	case LOAD0_BX: reg0 = ebx.getValue() & 0xffff; break;
 	case LOAD0_CX: reg0 = ecx.getValue() & 0xffff; break;
 	case LOAD0_DX: reg0 = edx.getValue() & 0xffff; break;
 	case LOAD0_SP: reg0 = esp.getValue() & 0xffff; break;
 	case LOAD0_BP: reg0 = ebp.getValue() & 0xffff; break;
 	case LOAD0_SI: reg0 = esi.getValue() & 0xffff; break;
 	case LOAD0_DI: reg0 = edi.getValue() & 0xffff; break;
 	case LOAD0_EAX: reg0 = eax.getValue(); break;
 	case LOAD0_EBX: reg0 = ebx.getValue(); break;
 	case LOAD0_ECX: reg0 = ecx.getValue(); break;
 	case LOAD0_EDX: reg0 = edx.getValue(); break;
 	case LOAD0_ESP: reg0 = esp.getValue(); break;
 	case LOAD0_EBP: reg0 = ebp.getValue(); break;
 	case LOAD0_ESI: reg0 = esi.getValue(); break;
 	case LOAD0_EDI: reg0 = edi.getValue(); break;
 	case LOAD0_AL: reg0 = eax.getValue() & 0xff; break;
 	case LOAD0_AH: reg0 = (eax.getValue()>>8) & 0xff; break;
 	case LOAD0_BL: reg0 = ebx.getValue() & 0xff; break;
 	case LOAD0_BH: reg0 = (ebx.getValue()>>8) & 0xff; break;
 	case LOAD0_CL: reg0 = ecx.getValue() & 0xff; break;
 	case LOAD0_CH: reg0 = (ecx.getValue()>>8) & 0xff; break;
 	case LOAD0_DL: reg0 = edx.getValue() & 0xff; break;
 	case LOAD0_DH: reg0 = (edx.getValue()>>8) & 0xff; break;
 	case LOAD0_CS: reg0 = cs.getValue() & 0xffff; break;
 	case LOAD0_SS: reg0 = ss.getValue() & 0xffff; break;
 	case LOAD0_DS: reg0 = ds.getValue() & 0xffff; break;
 	case LOAD0_ES: reg0 = es.getValue() & 0xffff; break;
 	case LOAD0_FS: reg0 = fs.getValue() & 0xffff; break;
 	case LOAD0_GS: reg0 = gs.getValue() & 0xffff; break;
 	case LOAD0_FLAGS: reg0 = getFlags() & 0xffff; break;
 	case LOAD0_EFLAGS: reg0 = getFlags(); break;
 	case LOAD0_IB: reg0 = getiCode() & 0xff; break;
 	case LOAD0_IW: reg0 = getiCode() & 0xffff; break;
 	case LOAD0_ID: reg0 = getiCode(); break;
 	case LOAD0_ADDR: reg0=addr; break;
 	case LOAD0_CR0: reg0 = cr0.getValue(); break;
 	case LOAD0_CR2: reg0 = cr2.getValue(); break;
 	case LOAD0_CR3: reg0 = cr3.getValue(); break;
 	case LOAD0_CR4: reg0 = cr4.getValue(); break;
 
 	case LOAD1_AX: reg1 = eax.getValue() & 0xffff; break;
 	case LOAD1_BX: reg1 = ebx.getValue() & 0xffff; break;
 	case LOAD1_CX: reg1 = ecx.getValue() & 0xffff; break;
 	case LOAD1_DX: reg1 = edx.getValue() & 0xffff; break;
 	case LOAD1_SP: reg1 = esp.getValue() & 0xffff; break;
 	case LOAD1_BP: reg1 = ebp.getValue() & 0xffff; break;
 	case LOAD1_SI: reg1 = esi.getValue() & 0xffff; break;
 	case LOAD1_DI: reg1 = edi.getValue() & 0xffff; break;
 	case LOAD1_EAX: reg1 = eax.getValue(); break;
 	case LOAD1_EBX: reg1 = ebx.getValue(); break;
 	case LOAD1_ECX: reg1 = ecx.getValue(); break;
 	case LOAD1_EDX: reg1 = edx.getValue(); break;
 	case LOAD1_ESP: reg1 = esp.getValue(); break;
 	case LOAD1_EBP: reg1 = ebp.getValue(); break;
 	case LOAD1_ESI: reg1 = esi.getValue(); break;
 	case LOAD1_EDI: reg1 = edi.getValue(); break;
 	case LOAD1_AL: reg1 = eax.getValue() & 0xff; break;
 	case LOAD1_AH: reg1 = (eax.getValue()>>8) & 0xff; break;
 	case LOAD1_BL: reg1 = ebx.getValue() & 0xff; break;
 	case LOAD1_BH: reg1 = (ebx.getValue()>>8) & 0xff; break;
 	case LOAD1_CL: reg1 = ecx.getValue() & 0xff; break;
 	case LOAD1_CH: reg1 = (ecx.getValue()>>8) & 0xff; break;
 	case LOAD1_DL: reg1 = edx.getValue() & 0xff; break;
 	case LOAD1_DH: reg1 = (edx.getValue()>>8) & 0xff; break;
 	case LOAD1_CS: reg1 = cs.getValue() & 0xffff; break;
 	case LOAD1_SS: reg1 = ss.getValue() & 0xffff; break;
 	case LOAD1_DS: reg1 = ds.getValue() & 0xffff; break;
 	case LOAD1_ES: reg1 = es.getValue() & 0xffff; break;
 	case LOAD1_FS: reg1 = fs.getValue() & 0xffff; break;
 	case LOAD1_GS: reg1 = gs.getValue() & 0xffff; break;
 	case LOAD1_FLAGS: reg1 = getFlags() & 0xffff; break;
 	case LOAD1_EFLAGS: reg1 = getFlags(); break;
 	case LOAD1_IB: reg1 = getiCode() & 0xff; break;
 	case LOAD1_IW: reg1 = getiCode() & 0xffff; break;
 	case LOAD1_ID: reg1 = getiCode(); break;
 
 	case LOAD2_IB: reg2 = getiCode() & 0xff; break;
 	case LOAD2_AL: reg2 = eax.getValue() & 0xff; break;
 	case LOAD2_AX: reg2 = eax.getValue() & 0xffff; break;
 	case LOAD2_EAX: reg2 = eax.getValue(); break;
 	case LOAD2_CL: reg2 = ecx.getValue() & 0xff; break;
 	
 	case STORE0_AX: eax.setLower16Value(0xffff & reg0); break;
 	case STORE0_BX: ebx.setLower16Value(0xffff & reg0); break;
 	case STORE0_CX: ecx.setLower16Value(0xffff & reg0); break;
 	case STORE0_DX: edx.setLower16Value(0xffff & reg0); break;
 	case STORE0_SP: esp.setLower16Value(0xffff & reg0); break;
 	case STORE0_BP: ebp.setLower16Value(0xffff & reg0); break;
 	case STORE0_SI: esi.setLower16Value(0xffff & reg0); break;
 	case STORE0_DI: edi.setLower16Value(0xffff & reg0); break;
 	case STORE0_EAX: eax.setValue(reg0); break;
 	case STORE0_EBX: ebx.setValue(reg0); break;
 	case STORE0_ECX: ecx.setValue(reg0); break;
 	case STORE0_EDX: edx.setValue(reg0); break;
 	case STORE0_ESP: esp.setValue(reg0); break;
 	case STORE0_EBP: ebp.setValue(reg0); break;
 	case STORE0_ESI: esi.setValue(reg0); break;
 	case STORE0_EDI: edi.setValue(reg0); break;
 	case STORE0_AL: eax.setLower8Value(0xff & reg0); break;
 	case STORE0_AH: eax.setUpper8Value(0xff & reg0); break;
 	case STORE0_BL: ebx.setLower8Value(0xff & reg0); break;
 	case STORE0_BH: ebx.setUpper8Value(0xff & reg0); break;
 	case STORE0_CL: ecx.setLower8Value(0xff & reg0); break;
 	case STORE0_CH: ecx.setUpper8Value(0xff & reg0); break;
 	case STORE0_DL: edx.setLower8Value(0xff & reg0); break;
 	case STORE0_DH: edx.setUpper8Value(0xff & reg0); break;
 	case STORE0_CS: cs.setValue(0xffff & reg0); break;
 	case STORE0_SS: ss.setValue(0xffff & reg0); break;
 	case STORE0_DS: ds.setValue(0xffff & reg0); break;
 	case STORE0_ES: es.setValue(0xffff & reg0); break;
 	case STORE0_FS: fs.setValue(0xffff & reg0); break;
 	case STORE0_GS: gs.setValue(0xffff & reg0); break;
 	case STORE0_FLAGS: setFlags(0xffff & reg0); break;
 	case STORE0_EFLAGS: setFlags(reg0); break;
 	case STORE0_CR0: setCR0(reg0); break;
 	case STORE0_CR2: setCR2(reg0); break;
 	case STORE0_CR3: setCR3(reg0); break;
 	case STORE0_CR4: setCR4(reg0); break;
 
 	case STORE1_AX: eax.setLower16Value(0xffff & reg1); break;
 	case STORE1_BX: ebx.setLower16Value(0xffff & reg1); break;
 	case STORE1_CX: ecx.setLower16Value(0xffff & reg1); break;
 	case STORE1_DX: edx.setLower16Value(0xffff & reg1); break;
 	case STORE1_SP: esp.setLower16Value(0xffff & reg1); break;
 	case STORE1_BP: ebp.setLower16Value(0xffff & reg1); break;
 	case STORE1_SI: esi.setLower16Value(0xffff & reg1); break;
 	case STORE1_DI: edi.setLower16Value(0xffff & reg1); break;
 	case STORE1_EAX: eax.setValue(reg1); break;
 	case STORE1_EBX: ebx.setValue(reg1); break;
 	case STORE1_ECX: ecx.setValue(reg1); break;
 	case STORE1_EDX: edx.setValue(reg1); break;
 	case STORE1_ESP: esp.setValue(reg1); break;
 	case STORE1_EBP: ebp.setValue(reg1); break;
 	case STORE1_ESI: esi.setValue(reg1); break;
 	case STORE1_EDI: edi.setValue(reg1); break;
 	case STORE1_AL: eax.setLower8Value(0xff & reg1); break;
 	case STORE1_AH: eax.setUpper8Value(0xff & reg1); break;
 	case STORE1_BL: ebx.setLower8Value(0xff & reg1); break;
 	case STORE1_BH: ebx.setUpper8Value(0xff & reg1); break;
 	case STORE1_CL: ecx.setLower8Value(0xff & reg1); break;
 	case STORE1_CH: ecx.setUpper8Value(0xff & reg1); break;
 	case STORE1_DL: edx.setLower8Value(0xff & reg1); break;
 	case STORE1_DH: edx.setUpper8Value(0xff & reg1); break;
 	case STORE1_CS: cs.setValue(0xffff & reg1); break;
 	case STORE1_SS: ss.setValue(0xffff & reg1); break;
 	case STORE1_DS: ds.setValue(0xffff & reg1); break;
 	case STORE1_ES: es.setValue(0xffff & reg1); break;
 	case STORE1_FS: fs.setValue(0xffff & reg1); break;
 	case STORE1_GS: gs.setValue(0xffff & reg1); break;
 	case STORE1_FLAGS: setFlags(0xffff & reg1); break;
 	case STORE1_EFLAGS: setFlags(reg1); break;
 
 	case LOAD0_MEM_BYTE:	reg0 = 0xff & seg.loadByte(addr); break;
 	case LOAD1_MEM_BYTE:	reg1 = 0xff & seg.loadByte(addr); break;
 	case STORE0_MEM_BYTE:	seg.storeByte(addr,(byte)reg0); break;
 	case STORE1_MEM_BYTE:	seg.storeByte(addr,(byte)reg1); break;
 	case LOAD0_MEM_WORD:	reg0 = 0xffff & seg.loadWord(addr); break;
 	case LOAD1_MEM_WORD:	reg1 = 0xffff & seg.loadWord(addr); break;
 	case STORE0_MEM_WORD:	seg.storeWord(addr,(short)reg0); break;
 	case STORE1_MEM_WORD:	seg.storeWord(addr,(short)reg1); break;
 	case LOAD0_MEM_DOUBLE:	reg0 = seg.loadDoubleWord(addr); break;
 	case LOAD1_MEM_DOUBLE:	reg1 = seg.loadDoubleWord(addr); break;
 	case STORE0_MEM_DOUBLE:	seg.storeDoubleWord(addr,reg0); break;
 	case STORE1_MEM_DOUBLE:	seg.storeDoubleWord(addr,reg1); break;
 
 	//addressing
 	case ADDR_AX:	addr += (short)eax.getValue(); break;
 	case ADDR_AL:	addr += ((0xff)&eax.getValue()); break;
 	case ADDR_BX:	addr += (short)ebx.getValue(); break;
 	case ADDR_CX:	addr += (short)ecx.getValue(); break;
 	case ADDR_DX:	addr += (short)edx.getValue(); break;
 	case ADDR_SP:	addr += (short)esp.getValue(); break;
 	case ADDR_BP:	addr += (short)ebp.getValue(); break;
 	case ADDR_SI:	addr += (short)esi.getValue(); break;
 	case ADDR_DI:	addr += (short)edi.getValue(); break;
 	case ADDR_EAX:	addr += eax.getValue(); break;
 	case ADDR_EBX:	addr += ebx.getValue(); break;
 	case ADDR_ECX:	addr += ecx.getValue(); break;
 	case ADDR_EDX:	addr += edx.getValue(); break;
 	case ADDR_ESP:	addr += esp.getValue(); break;
 	case ADDR_EBP:	addr += ebp.getValue(); break;
 	case ADDR_ESI:	addr += esi.getValue(); break;
 	case ADDR_EDI:	addr += edi.getValue(); break;
 
 	case ADDR_2EAX:	addr += (eax.getValue()<<1); break;
 	case ADDR_2EBX:	addr += (ebx.getValue()<<1); break;
 	case ADDR_2ECX:	addr += (ecx.getValue()<<1); break;
 	case ADDR_2EDX:	addr += (edx.getValue()<<1); break;
 	case ADDR_2EBP:	addr += (ebp.getValue()<<1); break;
 	case ADDR_2ESI:	addr += (esi.getValue()<<1); break;
 	case ADDR_2EDI:	addr += (edi.getValue()<<1); break;
 
 	case ADDR_4EAX:	addr += (eax.getValue()<<2); break;
 	case ADDR_4EBX:	addr += (ebx.getValue()<<2); break;
 	case ADDR_4ECX:	addr += (ecx.getValue()<<2); break;
 	case ADDR_4EDX:	addr += (edx.getValue()<<2); break;
 	case ADDR_4EBP:	addr += (ebp.getValue()<<2); break;
 	case ADDR_4ESI:	addr += (esi.getValue()<<2); break;
 	case ADDR_4EDI:	addr += (edi.getValue()<<2); break;
 
 	case ADDR_8EAX:	addr += (eax.getValue()<<3); break;
 	case ADDR_8EBX:	addr += (ebx.getValue()<<3); break;
 	case ADDR_8ECX:	addr += (ecx.getValue()<<3); break;
 	case ADDR_8EDX:	addr += (edx.getValue()<<3); break;
 	case ADDR_8EBP:	addr += (ebp.getValue()<<3); break;
 	case ADDR_8ESI:	addr += (esi.getValue()<<3); break;
 	case ADDR_8EDI:	addr += (edi.getValue()<<3); break;
 
 	case ADDR_IB:	displacement=((byte)getiCode()); addr += displacement; break;
 	case ADDR_IW:	displacement= (short)getiCode(); addr += displacement; break;
 	case ADDR_ID:	displacement= getiCode(); addr += displacement; break;
 
 	case ADDR_MASK_16:	addr=addr&0xffff; break;
 
 	case LOAD_SEG_CS:	seg = cs; addr=0; break;
 	case LOAD_SEG_SS:	seg = ss; addr=0; break;
 	case LOAD_SEG_DS:	seg = ds; addr=0; break;
 	case LOAD_SEG_ES:	seg = es; addr=0; break;
 	case LOAD_SEG_FS:	seg = fs; addr=0; break;
 	case LOAD_SEG_GS:	seg = gs; addr=0; break;
 
 	//operations
 
 	//control
 	case OP_JMP_FAR:	jump_far(reg0, reg1); break;
 	case OP_JMP_ABS:	eip.setValue(reg0); break;
 	case OP_CALL:		call(reg0, op32, addr32); break;
 	case OP_CALL_FAR:	call_far(reg0, reg1, op32, addr32); break;
 	case OP_CALL_ABS:	call_abs(reg0, op32, addr32); break;
 	case OP_RET:	ret(op32, addr32); break;
 	case OP_RET_IW:	ret_iw((short)reg0,op32,addr32); break;
 	case OP_RET_FAR:	ret_far(op32,addr32); break;
 	case OP_RET_FAR_IW:	ret_far_iw((short)reg0,op32,addr32); break;
 	case OP_ENTER:	enter(reg0, reg1,op32,addr32); break;
 	case OP_LEAVE:	leave(op32,addr32); break;
 	case OP_JMP_08:	jump_08((byte) reg0); break;
 	case OP_JMP_16_32:	if (!op32) { jump_16((short) reg0);  } else { jump_32(reg0); } break;
 	case OP_JO:	if (overflow.read()) { condition=true; jump_08((byte) reg0); } break;
 	case OP_JO_16_32:	if (!op32) { if (overflow.read()) { condition=true; jump_16((short) reg0);} } else { if (overflow.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JNO:	if (!overflow.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNO_16_32:	if (!op32) { if (!overflow.read()) { condition=true; jump_16((short) reg0);} } else { if (!overflow.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JC:	if (carry.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JC_16_32:	if (!op32) { if (carry.read()) { condition=true; jump_16((short) reg0);} } else { if (carry.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JNC:	if (!carry.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNC_16_32:	if (!op32) { if (!carry.read()) { condition=true; jump_16((short) reg0);} } else { if (!carry.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JZ:	if (zero.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JZ_16_32:	if (!op32) { if (zero.read()) { condition=true; jump_16((short) reg0);} } else { if (zero.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JNZ:	if (!zero.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNZ_16_32:	if (!op32) { if (!zero.read()) { condition=true; jump_16((short) reg0);} } else { if (!zero.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JS:	if (sign.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JS_16_32:	if (!op32) { if (sign.read()) { condition=true; jump_16((short) reg0);} } else { if (sign.read()) { condition=true;  jump_32(reg0);} } break;
 	case OP_JNS:	if (!sign.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNS_16_32:	if (!op32) { if (!sign.read()) { condition=true; jump_16((short) reg0);} } else { if (!sign.read())  { condition=true; jump_32(reg0);} } break;
 	case OP_JP:	if (parity.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JP_16_32:	if (!op32) { if (parity.read()) { condition=true; jump_16((short) reg0);} } else { if (parity.read())  { condition=true; jump_32(reg0);} } break;
 	case OP_JNP:	if (!parity.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNP_16_32:	if (!op32) { if (!parity.read()) { condition=true; jump_16((short) reg0);} } else { if (!parity.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JA:	if ((!carry.read()) && (!zero.read())) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JA_16_32:	if (!op32) { if ((!carry.read()) && (!zero.read())) { condition=true; jump_16((short) reg0);} } else { if ((!carry.read()) && (!zero.read()))  { condition=true; jump_32(reg0);} } break;
 	case OP_JNA:	if (carry.read() || zero.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNA_16_32:	if (!op32) { if (carry.read() || zero.read()) { condition=true; jump_16((short) reg0);} } else { if (carry.read() || zero.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JL:	if (sign.read()!=overflow.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JL_16_32:	if (!op32) { if (sign.read()!=overflow.read()) { condition=true; jump_16((short) reg0);} } else { if (sign.read()!=overflow.read())  { condition=true; jump_32(reg0);} } break;
 	case OP_JNL:	if (sign.read()==overflow.read()) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNL_16_32:	if (!op32) { if (sign.read()==overflow.read()) { condition=true; jump_16((short) reg0);} } else { if (sign.read()==overflow.read()) { condition=true; jump_32(reg0);} } break;
 	case OP_JG:	if ((!zero.read()) && (sign.read()==overflow.read())) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JG_16_32:	if (!op32) { if ((!zero.read()) && (sign.read()==overflow.read())) { condition=true; jump_16((short) reg0);} } else { if ((!zero.read()) && (sign.read()==overflow.read()))  { condition=true; jump_32(reg0);} } break;
 	case OP_JNG:	if (zero.read() || (sign.read()!=overflow.read())) { condition=true; jump_08((byte) reg0);} break;
 	case OP_JNG_16_32:	if (!op32) { if (zero.read() || (sign.read()!=overflow.read())) { condition=true; jump_16((short) reg0);} } else { if (zero.read() || (sign.read()!=overflow.read())) { condition=true; jump_32(reg0);} } break;
 
 	case OP_INT:	intr(reg0,op32,addr32); break;
 	case OP_INT3:	intr(reg0,op32,addr32); break;
 	case OP_IRET:	reg0 = iret(op32,addr32); break;
 
 	//input/output	
 	case OP_IN_08:	reg0 = 0xff & ioports.ioPortReadByte(reg0); break;
 	case OP_IN_16_32:	if (!op32) reg0 = 0xffff & ioports.ioPortReadWord(reg0);
 				else reg0 = ioports.ioPortReadLong(reg0); 
 				break;
 	case OP_OUT_08:	ioports.ioPortWriteByte(reg0, reg1); break;
 	case OP_OUT_16_32:	if (!op32) ioports.ioPortWriteWord(reg0, reg1);
 				else ioports.ioPortWriteLong(reg0, reg1); 
 				break;
 
 	//arithmetic
 	case OP_INC:	reg0=reg0+1; break;
 	case OP_DEC:	reg0=reg0-1; break;
 	case OP_ADD:	reg2=reg0; reg0=reg2+reg1; break;
 	case OP_ADC:	reg2=reg0; reg0=reg2+reg1+(carry.read()? 1:0); break;
 	case OP_SUB:	reg2=reg0; reg0=reg2-reg1; break;
 	case OP_CMP:	reg2=reg0; reg0=reg2-reg1; break;
 	case OP_SBB:	reg2=reg0; reg0=reg2-reg1-(carry.read()? 1:0); break;
 	case OP_AND:	reg0=reg0&reg1; break;
 	case OP_TEST:	reg0=reg0&reg1; break;
 	case OP_OR:	reg0=reg0|reg1; break;
 	case OP_XOR:	reg0=reg0^reg1; break;
 	case OP_ROL_08:	reg2=reg1&0x7; reg0=(reg0<<reg2)|(reg0>>>(8-reg2)); break;
 	case OP_ROL_16_32:	if(!op32) {reg2=reg1&0xf; reg0=(reg0<<reg2)|(reg0>>>(16-reg2)); }
 				else {reg1=reg1&0x1f; reg0=(reg0<<reg1)|(reg0>>>(32-reg1)); }
 				break;
 	case OP_ROR_08:	reg1=reg1&0x7; reg0=(reg0>>>reg1)|(reg0<<(8-reg1)); break;
 	case OP_ROR_16_32:	if (!op32) {reg1=reg1&0xf; reg0=(reg0>>>reg1)|(reg0<<(16-reg1)); }
 				else {reg1=reg1&0x1f; reg0=(reg0>>>reg1)|(reg0<<(32-reg1));} 
 				break;
 	case OP_RCL_08:	reg1=reg1&0x1f; reg1=reg1%9; reg0=reg0|(carry.read()? 0x100:0); reg0=(reg0<<reg1)|(reg0>>>(9-reg1)); break;
 	case OP_RCL_16_32:	if(!op32){reg1=reg1&0x1f; reg1=reg1%17; reg0=reg0|(carry.read()? 0x10000:0); reg0=(reg0<<reg1)|(reg0>>>(17-reg1)); }
 				else {reg1=reg1&0x1f; reg0l=(0xffffffffl&reg0)|(carry.read()? 0x100000000l:0); reg0=(int)(reg0l=(reg0l<<reg1)|(reg0l>>>(33-reg1))); }
 				break;
 	case OP_RCR_08:	reg1=reg1&0x1f; reg1=reg1%9; reg0=reg0|(carry.read()? 0x100:0); reg2=(carry.read()^((reg0&0x80)!=0)? 1:0); reg0=(reg0>>>reg1)|(reg0<<(9-reg1)); break;
 	case OP_RCR_16_32:	
 		if(!op32) { reg1=reg1&0x1f; reg1=reg1%17; reg2=(carry.read()^((reg0&0x8000)!=0)? 1:0); reg0=reg0|(carry.read()? 0x10000:0); reg0=(reg0>>>reg1)|(reg0<<(17-reg1)); }
 		else {reg1=reg1&0x1f; reg0l=(0xffffffffl&reg0)|(carry.read()? 0x100000000l:0); reg2=(carry.read()^((reg0&0x80000000)!=0)? 1:0); reg0=(int)(reg0l=(reg0l>>>reg1)|(reg0l<<(33-reg1))); }
 		break;
 	case OP_SHL:	reg1 &= 0x1f; reg2 = reg0; reg0 <<= reg1; break;
 	case OP_SHR:	reg1=reg1&0x1f; reg2=reg0; reg0=reg0>>>reg1; break;
 	case OP_SAR_08:	reg1=reg1&0x1f; reg2=reg0; reg0=((byte)reg0)>>reg1; break;
 	case OP_SAR_16_32:	if (!op32) {reg1=reg1&0x1f; reg2=reg0; reg0=((short)reg0)>>reg1; }
 				else { reg1=reg1&0x1f; reg2=reg0; reg0=reg0>>reg1; }
 				break;
 	case OP_NOT:	reg0=~reg0; break;
 	case OP_NEG:	reg0=-reg0; break;
 	case OP_MUL_08:	mul_08(reg0); break;
 	case OP_MUL_16_32:	if(!op32) mul_16(reg0); else mul_32(reg0); break;
 	case OP_IMULA_08:	imula_08((byte)reg0); break;
 	case OP_IMULA_16_32:	if(!op32) imula_16((short)reg0); else imula_32(reg0); break;
 	case OP_IMUL_16_32:	if(!op32) reg0=imul_16((short)reg0, (short)reg1); else reg0=imul_32(reg0,reg1); break;
 
 	case OP_DIV_08:	div_08(reg0); break;
 	case OP_DIV_16_32:	if (!op32) div_16(reg0); else div_32(reg0); break;
 	case OP_IDIV_08:	idiv_08((byte)reg0); break;
 	case OP_IDIV_16_32:	if (!op32) idiv_16((short)reg0); else idiv_32(reg0); break;
 
 	case OP_SCASB:
 	case OP_SCASW:
 		{
 			int a;
 			if (!addr32)
 				a=edi.getValue()&0xffff;
 			else
 				a=edi.getValue();
 			int i,n;
 			if (microcode==MICROCODE.OP_SCASB)
 			{
 				i=0xff&es.loadByte(a);
 				n=1;
 			}
 			else if (!op32)
 			{
 				i=0xffff&es.loadWord(a);
 				n=2;
 			}
 			else
 			{
 				i=es.loadDoubleWord(a);
 				n=4;
 			}
 			if(direction.read())
 				a-=n;
 			else
 				a+=n;
 			if (!addr32)
 				edi.setValue((edi.getValue()&~0xffff)|(a&0xffff));
 			else
 				edi.setValue(a);
 			reg2=reg0;
 			if (microcode==MICROCODE.OP_SCASW && op32)
 				reg0=(int)((0xffffffffl&reg0)-(0xffffffffl&i));
 			else
 				reg0=reg0-i;
 			reg1=i;
 		}
 		break;
 
 	case OP_REPNE_SCASB:
 	case OP_REPE_SCASB:
 	case OP_REPNE_SCASW:
 	case OP_REPE_SCASW:
 		{
 			int count,a;
 			if(!op32)
 				count=ecx.getValue()&0xffff;
 			else
 				count=ecx.getValue();
 			if (!addr32)
 				a=edi.getValue()&0xffff;
 			else
 				a=edi.getValue();
 			boolean used=count!=0;
 			int input=0;
 			while(count!=0)
 			{
 				if (microcode==MICROCODE.OP_REPNE_SCASB || microcode==MICROCODE.OP_REPE_SCASB)
 				{
 					input=0xff&es.loadByte(a);
 					count--;
 					if(direction.read())
 						a-=1;
 					else
 						a+=1;
 				}
 				else if (!op32)
 				{
 					input=0xffff&es.loadWord(a);
 					count--;
 					if(direction.read())
 						a-=2;
 					else
 						a+=2;
 				}
 				else
 				{
 					input=es.loadDoubleWord(a);
 					count--;
 					if(direction.read())
 						a-=4;
 					else
 						a+=4;
 				}
 				if (microcode==MICROCODE.OP_REPNE_SCASB || microcode==MICROCODE.OP_REPNE_SCASW)
 				{
 					if(reg0==input) break;
 				}
 				else
 				{
 					if(reg0!=input) break;
 				}
 			}
 			if (!op32)
 				ecx.setValue((ecx.getValue()&~0xffff)|(count&0xffff));
 			else
 				ecx.setValue(count);
 			if (!addr32)
 				edi.setValue((edi.getValue()&~0xffff)|(a&0xffff));
 			else
 				edi.setValue(a);
 			reg2=reg0;
 			reg1=input;
 			reg0=used? 1:0;
 		}
 		break;
 
 	case OP_CMPSB:
 	case OP_CMPSW:
 	{
 		int addrOne,addrTwo;
 		if (!addr32)
 		{
 			addrOne=esi.getValue()&0xffff;
 			addrTwo=edi.getValue()&0xffff;
 		}
 		else
 		{
 			addrOne=esi.getValue();
 			addrTwo=edi.getValue();
 		}
 		int dataOne;
 		int dataTwo;
 		int n;
 		if (microcode==MICROCODE.OP_CMPSB)
 		{
 			dataOne=0xff&seg.loadByte(addrOne);
 			dataTwo=0xff&es.loadByte(addrTwo);
 			n=1;
 		}
 		else if (!op32)
 		{
 			dataOne=0xffff&seg.loadWord(addrOne);
 			dataTwo=0xffff&es.loadWord(addrTwo);
 			n=2;
 		}
 		else
 		{
 			dataOne=seg.loadDoubleWord(addrOne);
 			dataTwo=es.loadDoubleWord(addrTwo);
 			n=4;
 		}
 		if(direction.read())
 		{
 			addrOne-=n;
 			addrTwo-=n;
 		}
 		else
 		{
 			addrOne+=n;
 			addrTwo+=n;
 		}
 
 		if(!addr32)
 		{
 			esi.setValue((esi.getValue()&~0xffff)|(addrOne&0xffff));
 			edi.setValue((edi.getValue()&~0xffff)|(addrTwo&0xffff));
 		}
 		else
 		{
 			esi.setValue(addrOne);
 			edi.setValue(addrTwo);
 		}
 		reg2=dataOne;
 		reg1=dataTwo;
 		if(microcode==MICROCODE.OP_CMPSW && op32)
 			reg0=(int)((0xffffffffl&dataOne)-(0xffffffffl&dataTwo));
 		else
 			reg0=dataOne-dataTwo;
 	}
 	break;
 
 	case OP_REPNE_CMPSB:
 	case OP_REPE_CMPSB:
 	case OP_REPNE_CMPSW:
 	case OP_REPE_CMPSW:
 	{
 		int count,addrOne,addrTwo;
 		if(!op32)
 			count=ecx.getValue()&0xffff;
 		else
 			count=ecx.getValue();
 		if(!addr32)
 		{
 			addrOne=esi.getValue()&0xffff;
 			addrTwo=edi.getValue()&0xffff;
 		}
 		else
 		{
 			addrOne=esi.getValue();
 			addrTwo=edi.getValue();
 		}
 		boolean used=count!=0;
 		int dataOne=0;
 		int dataTwo=0;
 		while(count!=0)
 		{
 			if(microcode==MICROCODE.OP_REPE_CMPSB || microcode==MICROCODE.OP_REPNE_CMPSB)
 			{
 				dataOne=0xff&seg.loadByte(addrOne);
 				dataTwo=0xff&es.loadByte(addrTwo);
 				count--;
 				if(direction.read())
 				{
 					addrOne-=1;
 					addrTwo-=1;
 				}
 				else
 				{
 					addrOne+=1;
 					addrTwo+=1;
 				}
 			}
 			else if(!op32)
 			{
 				dataOne=0xffff&seg.loadWord(addrOne);
 				dataTwo=0xffff&es.loadWord(addrTwo);
 				count--;
 				if(direction.read())
 				{
 					addrOne-=2;
 					addrTwo-=2;
 				}
 				else
 				{
 					addrOne+=2;
 					addrTwo+=2;
 				}
 			}
 			else
 			{
 				dataOne=seg.loadDoubleWord(addrOne);
 				dataTwo=es.loadDoubleWord(addrTwo);
 				count--;
 				if(direction.read())
 				{
 					addrOne-=4;
 					addrTwo-=4;
 				}
 				else
 				{
 					addrOne+=4;
 					addrTwo+=4;
 				}
 			}
 			if (microcode==MICROCODE.OP_REPE_CMPSB || microcode==MICROCODE.OP_REPE_CMPSW)
 			{
 				if(dataOne!=dataTwo) break;
 			}
 			else
 			{
 				if(dataOne==dataTwo) break;
 			}
 		}
 		if(!op32)
 			ecx.setValue((ecx.getValue()&~0xffff)|(count&0xffff));
 		else
 			ecx.setValue(count);
 		if(!addr32)
 		{
 			esi.setValue((esi.getValue()&~0xffff)|(addrOne&0xffff));
 			edi.setValue((edi.getValue()&~0xffff)|(addrTwo&0xffff));
 		}
 		else
 		{
 			esi.setValue(addrOne);
 			edi.setValue(addrTwo);
 		}
 		reg0=used? 1:0;
 		reg1=dataTwo;
 		reg2=dataOne;
 	}
 	break;
 
 	case OP_BSF: reg0 = bsf(reg1, reg0); break;
 	case OP_BSR: reg0 = bsr(reg1, reg0); break;
 			
 	case OP_BT_MEM: bt_mem(reg1, seg, addr); break;
 	case OP_BTS_MEM: bts_mem(reg1, seg, addr); break;
 	case OP_BTR_MEM: btr_mem(reg1, seg, addr); break;
 	case OP_BTC_MEM: btc_mem(reg1, seg, addr); break;
 			
 	case OP_BT_16_32:  if (!op32) {reg1 &= 0xf;} else {reg1&=0x1f;}  carry.set(reg0, reg1, Flag.CY_NTH_BIT_SET); break;
 	case OP_BTS_16_32: if (!op32) {reg1 &= 0xf;} else {reg1&=0x1f;}  carry.set(reg0, reg1, Flag.CY_NTH_BIT_SET); reg0 |= (1 << reg1); break;
 	case OP_BTR_16_32: if (!op32) {reg1 &= 0xf;} else {reg1&=0x1f;}  carry.set(reg0, reg1, Flag.CY_NTH_BIT_SET); reg0 &= ~(1 << reg1); break;
 	case OP_BTC_16_32: if (!op32) {reg1 &= 0xf;} else {reg1&=0x1f;}  carry.set(reg0, reg1, Flag.CY_NTH_BIT_SET); reg0 ^= (1 << reg1); break;
 			
 	case OP_SHLD_16_32: 
 	if (!op32)
 	{
 		int i = reg0;
 		reg2 &= 0x1f;
 		if (reg2 < 16)
 		{
 			reg0 = (reg0 << reg2) | (reg1 >>> (16 - reg2));
 			reg1 = reg2;
 			reg2 = i;
 		}
 		else
 		{
 			i = (reg1 & 0xFFFF) | (reg0 << 16);
 			reg0 = (reg1 << (reg2 - 16)) | ((reg0 & 0xFFFF) >>> (32 - reg2));
 			reg1 = reg2 - 15;
 			reg2 = i >> 1;
 		}
 	}
 	else
 	{
 		int i = reg0;
 		reg2 &= 0x1f;
 		if (reg2!=0)
 			reg0=(reg0<<reg2)|(reg1>>>(32-reg2));
 		reg1=reg2;
 		reg2=i;
 	} break;
 
 	case OP_SHRD_16_32:
 	if (!op32) 
 	{
 		int i = reg0;
 		reg2 &= 0x1f;
 		if (reg2 < 16)
 		{
 			reg0 = (reg0 >>> reg2) | (reg1 << (16 - reg2));
 			reg1 = reg2;
 			reg2 = i;
 		}
 		else
 		{
 			i = (reg0 & 0xFFFF) | (reg1 << 16);
 			reg0 = (reg1 >>> (reg2 - 16)) | (reg0 << (32 - reg2));
 			reg1 = reg2;
 			reg2 = i;
 		}
 	}
 	else
 	{
 		int i = reg0;
 		reg2 &= 0x1f;
 		if(reg2 != 0)
 			reg0=(reg0>>>reg2)|(reg1<<(32-reg2));
 		reg1=reg2;
 		reg2=i;
 	} break;
 
 	case OP_CWD: if(!op32) { if ((eax.getValue() & 0x8000) == 0) edx.setValue(edx.getValue() & 0xffff0000); else edx.setValue(edx.getValue() | 0x0000ffff); }
 
 			else { if ((eax.getValue() & 0x80000000) == 0) edx.setValue(0); else edx.setValue(-1); }
 			break;
 
 	case OP_AAA: aaa(); break;
 	case OP_AAD: aad(reg0); break;
 	case OP_AAM: reg0 = aam(reg0); break;
 	case OP_AAS: aas(); break;
 
 	case OP_DAA: daa(); break;
 	case OP_DAS: das(); break;
 
 	case OP_BOUND: 
 	{
 		if(!op32)
 		{
 			short lower = (short)reg0;
 			short upper = (short)(reg0 >> 16);
 			short index = (short)reg1;
 			if ((index < lower) || (index > (upper + 2)))
 				throw BOUND_RANGE;
 		}
 	} break;
 
 
 	//flag instructions
 	case OP_CLC:	carry.clear(); break;
 	case OP_STC:	carry.set(); break;
 	case OP_CLI:	interruptEnable.clear(); interruptEnableSoon.clear(); break;
 	case OP_STI:	interruptEnableSoon.set(); break;
 	case OP_CLD:	direction.clear(); break;
 	case OP_STD:	direction.set(); break;
 	case OP_CMC:	carry.toggle(); break;
 
 	case OP_SETO:	reg0 = overflow.read() ? 1 : 0; break;
 	case OP_SETNO:	reg0 = overflow.read() ? 0 : 1; break;
 	case OP_SETC:	reg0 = carry.read() ? 1 : 0; break; 
 	case OP_SETNC:	reg0 = carry.read() ? 0 : 1; break; 
 	case OP_SETZ:	reg0 = zero.read() ? 1 : 0; break; 
 	case OP_SETNZ:	reg0 = zero.read() ? 0 : 1; break; 
 	case OP_SETNA:	reg0 = carry.read() || zero.read() ? 1 : 0; break;
 	case OP_SETA:	reg0 = carry.read() || zero.read() ? 0 : 1; break;
 	case OP_SETS:	reg0 = sign.read() ? 1 : 0; break; 
 	case OP_SETNS:	reg0 = sign.read() ? 0 : 1; break; 
 	case OP_SETP:	reg0 = parity.read() ? 1 : 0; break; 
 	case OP_SETNP:	reg0 = parity.read() ? 0 : 1; break; 
 	case OP_SETL:	reg0 = sign.read() != overflow.read() ? 1 : 0; break;
 	case OP_SETNL:	reg0 = sign.read() != overflow.read() ? 0 : 1; break;
 	case OP_SETNG:	reg0 = zero.read() || (sign.read() != overflow.read()) ? 1 : 0; break;
 	case OP_SETG:	reg0 = zero.read() || (sign.read() != overflow.read()) ? 0 : 1; break;
 	case OP_SALC:	reg0 = carry.read() ? -1 : 0; break;
 	case OP_CMOVO:	if(overflow.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNO:	if(!overflow.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVC:	if(carry.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNC:	if(!carry.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVZ:	if(zero.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNZ:	if(!zero.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNA:	if((carry.read()||(zero.read()))) { condition=true; reg0=reg1; } break;
 	case OP_CMOVA:	if(!(carry.read()||(zero.read()))) { condition=true; reg0=reg1; } break;
 	case OP_CMOVS:	if(sign.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNS:	if(!sign.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVP:	if(parity.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNP:	if(!parity.read()) { condition=true; reg0=reg1; } break;
 	case OP_CMOVL:	if((sign.read() != overflow.read())) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNL:	if(!(sign.read() != overflow.read())) { condition=true; reg0=reg1; } break;
 	case OP_CMOVNG:	if((zero.read() || (sign.read() != overflow.read()))) { condition=true; reg0=reg1; } break;
 	case OP_CMOVG:	if(!(zero.read() || (sign.read() != overflow.read()))) { condition=true; reg0=reg1; } break;
 
 	case OP_LAHF: lahf(); break;
 	case OP_SAHF: sahf(); break;
 
 	//stack instructions
 	case OP_POP:
 		if(!op32 && !addr32)
 		{
 			reg0 = ss.loadWord(esp.getValue()&0xffff);
 			esp.setValue((esp.getValue()&~0xffff) | ((esp.getValue()+2)&0xffff));
 		}
 		else if (!op32 && addr32)
 		{
 			reg0 = ss.loadWord(esp.getValue());
 			esp.setValue((esp.getValue()+2));
 		}
 		else if (op32 && !addr32)
 		{
 			reg0 = ss.loadDoubleWord(esp.getValue()&0xffff);
 			esp.setValue((esp.getValue()&~0xffff) | ((esp.getValue()+4)&0xffff));
 		}
 		else
 		{
 			reg0 = ss.loadDoubleWord(esp.getValue()&0xffffffff);
 			esp.setValue((esp.getValue()&~0xffff) | ((esp.getValue()+4)&0xffff));
 		}
 		if (code[codesHandled]==MICROCODE.STORE0_SS)
 			interruptEnable.clear();
 		break;
 
 	case OP_POPF:
 		if (isModeReal())
 		{
 			if (!op32 && !addr32)
 			{
 				reg0 = ss.loadWord(esp.getValue()&0xffff);
 				esp.setValue((esp.getValue()&~0xffff)|((esp.getValue()+2)&0xffff));
 			}
 			else if (op32 && !addr32)
 			{
 				reg0 = (getFlags()&0x20000)|(ss.loadDoubleWord(esp.getValue()&0xffff)&~0x1a0000);
 				esp.setValue((esp.getValue()&~0xffff)|((esp.getValue()+4)&0xffff));
 			}
 			else if (!op32 && addr32)
 			{
 				reg0 = ss.loadWord(esp.getValue());
 				esp.setValue(esp.getValue()+2);
 			}
 			else if (op32 && addr32)
 			{
 				reg0 = (getFlags()&0x20000)|(ss.loadDoubleWord(esp.getValue())&~0x1a0000);
 				esp.setValue(esp.getValue()+4);
 			}
 		}
 		else
 		{
 			int flagPrivilegeLevel=(ioPrivilegeLevel1.value?2:0)+(ioPrivilegeLevel0.value?1:0);
 			if (!op32 && !addr32)
 			{
 				reg0 = ss.loadWord(esp.getValue()&0xffff);
 				esp.setValue((esp.getValue()&~0xffff)|((esp.getValue()+2)&0xffff));
 				
 				if (current_privilege_level>0)
 				{
 					if (current_privilege_level>flagPrivilegeLevel)
 						reg0=(getFlags()&0x3200)|(reg0&~0x3200);
 					else
 						reg0=(getFlags()&0x3000)|(reg0&~0x3000);
 				}
 			}
 			else if (!op32 && addr32)
 			{
 				reg0 = ss.loadWord(esp.getValue());
 				esp.setValue(esp.getValue()+2);
 				
 				if (current_privilege_level>0)
 				{
 					if (current_privilege_level>flagPrivilegeLevel)
 						reg0=(getFlags()&0x3200)|(reg0&~0x3200);
 					else
 						reg0=(getFlags()&0x3000)|(reg0&~0x3000);
 				}
 			}
 			else if (op32 && !addr32)
 			{
 				reg0 = ss.loadDoubleWord(esp.getValue()&0xffff);
 				esp.setValue((esp.getValue()&~0xffff)|((esp.getValue()+4)&0xffff));
 				if (current_privilege_level>0)
 				{
 					if (current_privilege_level>flagPrivilegeLevel)
 						reg0=(getFlags()&0x23200)|(reg0&~(0x23200|0x180000));
 					else
 						reg0=(getFlags()&0x23000)|(reg0&~(0x23000|0x180000));
 				}
 				else
 					reg0=(getFlags()&0x20000)|(reg0&~(0x20000|0x180000));
 			}
 			else if (op32 && addr32)
 			{
 				reg0 = ss.loadDoubleWord(esp.getValue());
 				esp.setValue(esp.getValue()+4);
 				if (current_privilege_level>0)
 				{
 					if (current_privilege_level>flagPrivilegeLevel)
 						reg0=(getFlags()&0x23200)|(reg0&~(0x23200|0x180000));
 					else
 						reg0=(getFlags()&0x23000)|(reg0&~(0x23000|0x180000));
 				}
 				else
 					reg0=(getFlags()&0x20000)|(reg0&~(0x20000|0x180000));
 			}
 		}
 		break;
 
 	case OP_PUSH:	if(!op32) push_16((short)reg0,addr32); else push_32(reg0,addr32); break;
 	case OP_PUSHF:	if(!op32) push_16((short)reg0,addr32); else push_32(~0x30000&reg0,addr32); break;
 	case OP_PUSHA: if(!op32) pusha(); else pushad(); break;
 	case OP_POPA: if(!op32) popa(); else popad(); break;
 
 	case OP_SIGN_EXTEND: if(!op32) reg0 = 0xffff & ((byte)reg0); else reg0 = ((short)reg0); break;
 	case OP_SIGN_EXTEND_8_16: reg0 = 0xffff & ((byte)reg0); break;
 	case OP_SIGN_EXTEND_8_32: if(op32) reg0 = ((byte)reg0); break;
 	case OP_SIGN_EXTEND_16_32: if(op32) reg0 = ((short)reg0); break;
 
 	case OP_INSB: ins(reg0,1,addr32); break;
 	case OP_INSW: ins(reg0,op32?4:2,addr32); break;
 	case OP_REP_INSB: rep_ins(reg0,1,addr32); break;
 	case OP_REP_INSW: rep_ins(reg0,op32?4:2,addr32); break;
 
 	case OP_LODSB: lods(seg,1,addr32); break;
 	case OP_LODSW: lods(seg,op32?4:2,addr32); break;
 	case OP_REP_LODSB: rep_lods(seg,1,addr32); break;
 	case OP_REP_LODSW: rep_lods(seg,op32?4:2,addr32); break;
 
 	case OP_MOVSB: movs(seg,1,addr32); break;
 	case OP_MOVSW: movs(seg,op32?4:2,addr32); break;
 	case OP_REP_MOVSB: rep_movs(seg,1,addr32); break;
 	case OP_REP_MOVSW: rep_movs(seg,op32?4:2,addr32); break;
 
 	case OP_OUTSB: outs(reg0, seg, 1,addr32); break;
 	case OP_OUTSW: outs(reg0, seg, op32?4:2,addr32); break;
 	case OP_REP_OUTSB: rep_outs(reg0, seg, 1,addr32); break;
 	case OP_REP_OUTSW: rep_outs(reg0, seg, op32?4:2,addr32); break;
 			
 	case OP_STOSB: stos(reg0,1,addr32); break;
 	case OP_STOSW: stos(reg0,op32?4:2,addr32); break;
 	case OP_REP_STOSB: rep_stos(reg0,1,addr32); break;
 	case OP_REP_STOSW: rep_stos(reg0,op32?4:2,addr32); break;
 
 	case OP_LOOP_CX: 
 			if (!op32) { ecx.setValue((ecx.getValue() & ~0xffff) | ((ecx.getValue() - 1) & 0xffff)); if ((0xffff & ecx.getValue()) != 0) jump_08((byte)reg0); }
 			else {ecx.setValue(ecx.getValue()-1); if (ecx.getValue() != 0) jump_08((byte)reg0); }break;
 	case OP_LOOPZ_CX: if(!op32){ecx.setValue((ecx.getValue() & ~0xffff) | ((ecx.getValue() - 1) & 0xffff)); if (((0xffff & ecx.getValue()) != 0) && zero.read()) jump_08((byte)reg0); }
 			else { ecx.setValue(ecx.getValue()-1); if ((ecx.getValue() != 0) && zero.read()) jump_08((byte)reg0); } break;
 	case OP_LOOPNZ_CX: if(!op32){ecx.setValue((ecx.getValue() & ~0xffff) | ((ecx.getValue() - 1) & 0xffff)); if (((0xffff & ecx.getValue()) != 0) && !zero.read()) jump_08((byte)reg0);}
 			else{ecx.setValue(ecx.getValue()-1); if ((ecx.getValue() != 0) && !zero.read()) jump_08((byte)reg0);} break;
 
 	case OP_JCXZ: if(!op32) jcxz((byte)reg0); else jecxz((byte)reg0); break;
 		
 	case OP_HALT: waitForInterrupt(); break;
 
 	case OP_CPUID: cpuid(); break;
 
 	case OP_LGDT:	
 		gdtr.memory=linearMemory; 
 		gdtr.setDescriptorValue(op32? reg1:(reg1&0x00ffffff),reg0); 
 		System.out.printf("New GDT starts at %x\n", gdtr.getBase()); 
 		break;
 	case OP_LIDT:
 		idtr.memory=linearMemory;
 		idtr.setDescriptorValue(op32? reg1:(reg1&0x00ffffff),reg0); 
 		break;
 	case OP_SGDT:	if (op32) reg1=gdtr.getBase(); else reg1=gdtr.getBase()&0x00ffffff; reg0=gdtr.getLimit(); break;
 	case OP_SIDT:	if (op32) reg1=idtr.getBase(); else reg1=idtr.getBase()&0x00ffffff; reg0=gdtr.getLimit(); break;
 
 	case OP_SMSW:	reg0 = cr0.getValue() & 0xffff; break;
 	case OP_LMSW:	setCR0((cr0.getValue()&~0xf)|(reg0&0xf)); break;
 
 	case OP_SLDT:	reg0=0xffff&ldtr.getBase(); break;
 	case OP_STR:	reg0=0xffff&tss.getBase(); break;
 	case OP_LLDT:	ldtr.setProtectedValue(reg0&~0x4); System.out.printf("New LDT starts at %x\n",ldtr.getBase()); break;
 	case OP_LTR:	tss.setProtectedValue(reg0); System.out.printf("New TSS starts at %x\n",tss.getBase()); break;
 	
 	case OP_CLTS:	setCR3(cr3.getValue()&~0x4); break;
 
 	case OP_RDTSC:	long tsc = rdtsc(); reg0=(int)tsc; reg1=(int)(tsc>>>32); break;
 
 	case OP_NOP: break;
 	case OP_MOV: break;
 	case OP_FLOAT_NOP: break;
 
 	//prefices
 	case PREFIX_LOCK: case PREFIX_REPNE: case PREFIX_REPE: case PREFIX_CS: case PREFIX_SS: case PREFIX_DS: case PREFIX_ES: case PREFIX_FS: case PREFIX_GS: case PREFIX_OPCODE_32BIT: case PREFIX_ADDRESS_32BIT: break;
 
 	//flag setting commands
 	case FLAG_BITWISE_08:	bitwise_flags((byte)reg0); break;
 	case FLAG_BITWISE_16:	bitwise_flags((short)reg0); break;
 	case FLAG_BITWISE_32:	bitwise_flags(reg0); break;
 	case FLAG_SUB_08:	sub_flags_08(reg0, reg2, reg1); break;
 	case FLAG_SUB_16:	sub_flags_16(reg0, reg2, reg1); break;
 	case FLAG_SUB_32:	sub_flags_32(reg0l, reg2, reg1); break;
 	case FLAG_REP_SUB_08:	rep_sub_flags_08(reg0, reg2, reg1); break;
 	case FLAG_REP_SUB_16:	rep_sub_flags_16(reg0, reg2, reg1); break;
 	case FLAG_REP_SUB_32:	rep_sub_flags_32(reg0, reg2, reg1); break;
 	case FLAG_ADD_08:	add_flags_08(reg0, reg2, reg1); break;
 	case FLAG_ADD_16:	add_flags_16(reg0, reg2, reg1); break;
 	case FLAG_ADD_32:	add_flags_32(reg0l, reg2, reg1); break;
 	case FLAG_ADC_08:	adc_flags_08(reg0, reg2, reg1); break;
 	case FLAG_ADC_16:	adc_flags_16(reg0, reg2, reg1); break;
 	case FLAG_ADC_32:	adc_flags_32(reg0l, reg2, reg1); break;
 	case FLAG_SBB_08:	sbb_flags_08(reg0, reg2, reg1); break;
 	case FLAG_SBB_16:	sbb_flags_16(reg0, reg2, reg1); break;
 	case FLAG_SBB_32:	sbb_flags_32(reg0l, reg2, reg1); break;
 	case FLAG_DEC_08:	dec_flags_08((byte)reg0); break;
 	case FLAG_DEC_16:	dec_flags_16((short)reg0); break;
 	case FLAG_DEC_32:	dec_flags_32(reg0); break;
 	case FLAG_INC_08:	inc_flags_08((byte)reg0); break;
 	case FLAG_INC_16:	inc_flags_16((short)reg0); break;
 	case FLAG_INC_32:	inc_flags_32(reg0); break;
 	case FLAG_SHL_08:	shl_flags_08((byte)reg0, (byte)reg2, reg1); break;
 	case FLAG_SHL_16:	shl_flags_16((short)reg0, (short)reg2, reg1); break;
 	case FLAG_SHL_32:	shl_flags_32(reg0, reg2, reg1); break;
 	case FLAG_SHR_08:	shr_flags_08((byte)reg0, reg2, reg1); break;
 	case FLAG_SHR_16:	shr_flags_16((short)reg0, reg2, reg1); break;
 	case FLAG_SHR_32:	shr_flags_32(reg0, reg2, reg1); break;
 	case FLAG_SAR_08:	sar_flags((byte)reg0, (byte)reg2, reg1); break;
 	case FLAG_SAR_16:	sar_flags((short)reg0, (short)reg2, reg1); break;
 	case FLAG_SAR_32:	sar_flags(reg0, reg2, reg1); break;
 	case FLAG_RCL_08:	rcl_flags_08(reg0, reg1); break;
 	case FLAG_RCL_16:	rcl_flags_16(reg0, reg1); break;
 	case FLAG_RCL_32:	rcl_flags_32(reg0l, reg1); break;
 	case FLAG_RCR_08:	rcr_flags_08(reg0, reg1, reg2); break;
 	case FLAG_RCR_16:	rcr_flags_16(reg0, reg1, reg2); break;
 	case FLAG_RCR_32:	rcr_flags_32(reg0l, reg1, reg2); break;
 	case FLAG_ROL_08:	rol_flags_08((byte)reg0, reg1); break;
 	case FLAG_ROL_16:	rol_flags_16((short)reg0, reg1); break;
 	case FLAG_ROL_32:	rol_flags_32(reg0, reg1); break;
 	case FLAG_ROR_08:	ror_flags_08((byte)reg0, reg1); break;
 	case FLAG_ROR_16:	ror_flags_16((short)reg0, reg1); break;
 	case FLAG_ROR_32:	ror_flags_32(reg0, reg1); break;
 	case FLAG_NEG_08:	neg_flags_08((byte)reg0); break;
 	case FLAG_NEG_16:	neg_flags_16((short)reg0); break;
 	case FLAG_NEG_32:	neg_flags_32(reg0); break;
 
 	case FLAG_NONE:		break;
 	case FLAG_FLOAT_NOP:	break;
 
 	//errors
 	case OP_BAD: panic("Bad microcode");
 	case OP_UNIMPLEMENTED: panic("Unimplemented microcode");
 	
 	default: System.out.println(microcode); panic("Unhandled microcode");
 	
 	}
 
 	if(processorGUICode!=null) processorGUICode.pushMicrocode(microcode,reg0,reg1,reg2,addr,displacement,condition);
 	}
 	}
 	catch (Processor_Exception e)
 	{
 		handleProcessorException(e);
 	}
 
 }
 
 private void jump_08(byte offset)
 {
 	int pc = eip.getValue();
 	pc=pc+offset;
 	eip.setValue(pc);
 
 	//check whether we're out of range
 /*	if ((pc & 0xffff0000) != 0)
 	{
 		eip.setValue(eip.getValue() - offset);
 		throw GENERAL_PROTECTION;
 	}
 */
 }
 
 private void jump_16(short offset)
 {
 	eip.setValue((eip.getValue()+offset)&0xffff);
 }
 
 private void jump_32(int offset)
 {
 	int pc = eip.getValue();
 	pc=pc+offset;
 	eip.setValue(pc);
 
 /*	//check whether we're out of range
 	if ((pc & 0xffff0000) != 0)
 	{
 		eip.setValue(eip.getValue() - offset);
 		throw GENERAL_PROTECTION;
 	}
 */
 }
 
 private void call(int target, boolean op32, boolean addr32)
 {
 	int sp=esp.getValue();
 	int pc=eip.getValue();
 //	if (((sp&0xffff)<2)&&((sp&0xffff)>0))
 //		throw STACK_SEGMENT;
 	if (!op32 && !addr32)
 	{
 		int offset = (sp-2)&0xffff;
 		ss.storeWord(offset, (short)pc);
 		esp.setValue((sp & 0xffff0000) | offset);
 		eip.setValue((pc+target)&0xffff);
 	}
 	else if (op32 && !addr32)
 	{
 		int offset = (sp-4)&0xffff;
 		ss.storeDoubleWord(offset, pc);
 		esp.setValue((sp & 0xffff0000) | offset);
 		eip.setValue(pc+target);
 	}
 	else if (!op32 && addr32)
 	{
 		int offset = (sp-2);
 		ss.storeWord(offset, (short)pc);
 		esp.setValue(offset);
 		eip.setValue((pc+target)&0xffff);
 	}
 	else
 	{
 		int offset = (sp-4);
 		ss.storeDoubleWord(offset, pc);
 		esp.setValue(offset);
 		eip.setValue(pc+target);
 	}
 }
 
 private void ret(boolean op32, boolean addr32)
 {
 	int sp=esp.getValue();
 	if(!op32 && !addr32)
 	{
 		eip.setValue(ss.loadWord(sp&0xffff)&0xffff);
 		esp.setValue((sp&~0xffff)|((sp+2)&0xffff));
 	}
 	else if (op32 && !addr32)
 	{
 		eip.setValue(ss.loadDoubleWord(sp&0xffff));
 		esp.setValue((sp&~0xffff)|((sp+4)&0xffff));
 	}
 	else if (!op32 && addr32)
 	{
 		eip.setValue(ss.loadWord(sp)&0xffff);
 		esp.setValue(sp+2);
  	}
 	else
 	{
 		eip.setValue(ss.loadDoubleWord(sp));
 		esp.setValue(sp+4);
 	}
 }
 
 private void ret_iw(short data, boolean op32, boolean addr32)
 {
 	ret(op32,addr32);
 	int sp=esp.getValue();
 	if (!addr32)
 		esp.setValue((sp&~0xffff)|((sp+data)&0xffff));
 	else
 		esp.setValue(sp+data);
 }
 
 private void ret_far(boolean op32, boolean addr32)
 {
 	int sp=esp.getValue();
 	if (!op32 && !addr32)
 	{
 		eip.setValue(ss.loadWord(sp&0xffff)&0xffff);
 		cs.setValue(ss.loadWord((sp+2)&0xffff)&0xffff);
 		esp.setValue((sp&~0xffff)|((sp+4)&0xffff));
 	}
 	else if (!op32 && addr32)
 	{
 		eip.setValue(ss.loadWord(sp)&0xffff);
 		cs.setValue(ss.loadWord((sp+2))&0xffff);
 		esp.setValue(sp+4);
 	}
 	else if (op32 && !addr32)
 	{
 		eip.setValue(ss.loadWord(sp&0xffff));
 		cs.setValue(ss.loadWord((sp+4)&0xffff));
 		esp.setValue((sp&~0xffff)|((sp+8)&0xffff));
 	}
 	else
 	{
 		eip.setValue(ss.loadWord(sp));
 		cs.setValue(ss.loadWord((sp+4)));
 		esp.setValue(sp+8);
 	}
 	if (!isModeReal())
 	{
 		if (cs.rpl<current_privilege_level)
 			throw GENERAL_PROTECTION;
 //		current_privilege_level=cs.rpl;
 		setCPL(cs.rpl);
 	}
 }
 
 private void ret_far_iw(short data, boolean op32, boolean addr32)
 {
 	ret_far(op32,addr32);
 	int sp=esp.getValue();
 	if (!addr32)
 		esp.setValue((sp&~0xffff)|((sp+data)&0xffff));
 	else
 		esp.setValue(sp+data);
 }
 
 private void push_16(short data, boolean addr32)
 {
 	int sp=esp.getValue();
 
 	if (((sp&0xffff)<2)&&((sp&0xffff)>0))
 		throw STACK_SEGMENT;
 
 	if (!addr32)
 	{
 		int offset=(sp-2)&0xffff;
 		ss.storeWord(offset,data);
 		esp.setValue((sp&~0xffff)|offset);
 	}
 	else
 	{
 		int offset=(sp-2);
 		ss.storeWord(offset,data);
 		esp.setValue(offset);
 	}
 }
 
 private void push_32(int data, boolean addr32)
 {
 	int sp=esp.getValue();
 
 	if (((sp&0xffff)<4)&&((sp&0xffff)>0))
 		throw STACK_SEGMENT;
 
 	if (!addr32)
 	{
 		int offset=(sp-4)&0xffff;
 		ss.storeDoubleWord(offset,data);
 		esp.setValue((sp&~0xffff)|offset);
 	}
 	else
 	{
 		int offset=(sp-4);
 		ss.storeDoubleWord(offset,data);
 		esp.setValue(offset);
 	}
 }
 
 private void jump_far(int ip, int segment)
 {
 	eip.setValue(ip);
 	cs.setValue(segment);
 	if (!isModeReal())
 	{
 		if (cs.dpl<current_privilege_level)
 			throw GENERAL_PROTECTION;
 		cs.rpl=current_privilege_level;
 	}
 }
 
 private void call_far(int ip, int segment, boolean op32, boolean addr32)
 {
 	int sp=esp.getValue();
 
 	if (!op32 && !addr32)
 		if (((sp&0xffff)<4)&&((sp&0xffff)>0))
 			throw STACK_SEGMENT;
 	else if (!op32 && addr32)
 		if (sp<4 && sp>0)
 			throw STACK_SEGMENT;
 	else if (op32 && !addr32)
 		if (((sp&0xffff)<8)&&((sp&0xffff)>0))
 			throw STACK_SEGMENT;
 	else
 		if (sp<8 && sp>0)
 			throw STACK_SEGMENT;
 
 	if (!op32 && !addr32)
 	{
 		ss.storeWord((sp-2)&0xffff, (short)cs.getValue());
 		ss.storeWord((sp-4)&0xffff, (short)eip.getValue());
 		esp.setValue((sp&~0xffff)|((sp-4)&0xffff));
 	}
 	else if (!op32 && addr32)
 	{
 		ss.storeWord((sp-2), (short)cs.getValue());
 		ss.storeWord((sp-4), (short)eip.getValue());
 		esp.setValue(sp-4);
 	}
 	else if (op32 && !addr32)
 	{
 		ss.storeDoubleWord((sp-4)&0xffff, 0xffff&cs.getValue());
 		ss.storeDoubleWord((sp-8)&0xffff, eip.getValue());
 		esp.setValue((sp&~0xffff)|((sp-8)&0xffff));
 	}
 	else
 	{
 		ss.storeDoubleWord((sp-4), 0xffff&cs.getValue());
 		ss.storeDoubleWord((sp-8), eip.getValue());
 		esp.setValue(sp-8);
 	}
 
 	eip.setValue(ip);
 	cs.setValue(segment);
 	if (!isModeReal())
 	{
 		if (cs.dpl<current_privilege_level)
 			throw GENERAL_PROTECTION;
 		cs.rpl=current_privilege_level;
 	}
 }
 
 private void jcxz(byte offset)
 {
 	if ((ecx.getValue() & 0xffff) == 0) jump_08(offset);
 }
 
 private void jecxz(byte offset)
 {
 	if (ecx.getValue() == 0) jump_08(offset);
 }
 
 private void enter(int frameSize, int nestingLevel, boolean op32, boolean addr32)
 {
 if (op32||addr32) { panic("Implement enter 32"); }
 
 	nestingLevel %= 32;
 
 	int tempESP = esp.getValue();
 	int tempEBP = ebp.getValue();
 
 	tempESP = (tempESP & ~0xffff) | ((tempESP - 2) & 0xffff);
 	ss.storeWord(tempESP & 0xffff, (short)tempEBP);
 
 	int frameTemp = tempESP & 0xffff;
 
 	if (nestingLevel != 0) 
 	{
 		while (--nestingLevel != 0) 
 		{
 			tempEBP = (tempEBP & ~0xffff) | ((tempEBP - 2) & 0xffff);
 			tempESP = (tempESP & ~0xffff) | ((tempESP - 2) & 0xffff);
 			ss.storeWord(tempESP & 0xffff, ss.loadWord(tempEBP & 0xffff));
 		}
 		tempESP = (tempESP & ~0xffff) | ((tempESP - 2) & 0xffff);
 		ss.storeWord(tempESP & 0xffff, (short)frameTemp);
 	}
 	ebp.setValue((tempEBP & ~0xffff) | (frameTemp & 0xffff));
         esp.setValue((tempESP & ~0xffff) | ((tempESP - frameSize -2*nestingLevel) & 0xffff));
 }
 
 private void leave(boolean op32, boolean addr32)
 {
 	if (!op32 && !addr32)
 	{
 		int tempESP = (esp.getValue() & ~0xffff) | (ebp.getValue() & 0xffff);
 		int tempEBP = (ebp.getValue() & ~0xffff) | (ss.loadWord(tempESP & 0xffff) & 0xffff);
 //		if (((tempESP & 0xffff) > 0xffff) || ((tempESP & 0xffff) < 0)) {
 // 		    throw GENERAL_PROTECTION;	
 //		}
 		esp.setValue((tempESP & ~0xffff) | ((tempESP + 2) & 0xffff));
 		ebp.setValue(tempEBP);
 	}
 	else if (op32 && !addr32)
 	{
 		int tempESP = (ebp.getValue() & 0xffff);
 		int tempEBP = ss.loadDoubleWord(tempESP);
 		esp.setValue((tempESP & ~0xffff) | ((tempESP+4) & 0xffff));
 		ebp.setValue(tempEBP);
 	}
 	else if (!op32 && addr32)
 	{
 		int tempESP=ebp.getValue()&0xffff;
 		int tempEBP=ss.loadWord(tempESP)&0xffff;
 		esp.setValue((esp.getValue()&~0xffff)|(tempESP+2));
 		ebp.setValue((ebp.getValue()&~0xffff)|(tempEBP));
 	}
 	else if (op32 && addr32)
 	{
 		int tempESP=ebp.getValue();
 		int tempEBP=ss.loadDoubleWord(tempESP);
 		esp.setValue(tempESP+4);
 		ebp.setValue(tempEBP);
 	}
 }
 
 private void pusha()
 {
 	int offset = esp.getValue()&0xffff;
 //	if ((offset<16)&&((offset&1)==1)&&(offset<6))
 //		throw GENERAL_PROTECTION;
 	int temp = esp.getValue();
 
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) eax.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) ecx.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) edx.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) ebx.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) temp);
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) ebp.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) esi.getValue());
 	offset -= 2;
 	ss.storeWord(offset & 0xffff, (short) edi.getValue());
         
 	esp.setValue((temp & ~0xffff) | (offset & 0xffff));
 }
 
 public void pushad()
 {
 	int offset = esp.getValue()&0xffff;
 //	if ((offset<32)&&(offset>0))
 //		throw GENERAL_PROTECTION;
 	int temp = esp.getValue();
 
 	offset -= 4;
 	ss.storeDoubleWord(offset, eax.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, ecx.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, edx.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, ebx.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, temp);
 	offset -= 4;
 	ss.storeDoubleWord(offset, ebp.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, esi.getValue());
 	offset -= 4;
 	ss.storeDoubleWord(offset, edi.getValue());
         
 	esp.setValue((temp & ~0xffff) | (offset));
 }
 
 private void popa()
 {
 	int offset = 0xffff & esp.getValue();
 
 	edi.setValue((edi.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 	esi.setValue((esi.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 	ebp.setValue((ebp.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 4;	//skip SP	
 	ebx.setValue((ebx.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 	edx.setValue((edx.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 	ecx.setValue((ecx.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 	eax.setValue((eax.getValue() & ~0xffff) | (0xffff & ss.loadWord(0xffff & offset)));
 	offset += 2;
 		
 	esp.setValue((esp.getValue() & ~0xffff) | (offset & 0xffff));
 }
 
 public void popad()
 {
 	int offset = 0xffff & esp.getValue();
 
 	edi.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 	esi.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 	ebp.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 8;	//skip SP	
 	ebx.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 	edx.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 	ecx.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 	eax.setValue(ss.loadDoubleWord(0xffff & offset));
 	offset += 4;
 		
 	esp.setValue((esp.getValue() & ~0xffff) | (offset & 0xffff));
 }
 
 private final void call_abs(int target, boolean op32, boolean addr32)
 {
 	int sp=esp.getValue();
 
 	if (!op32 && !addr32)
 	{
 		if (((sp & 0xffff) < 2) && ((sp & 0xffff) > 0))
 			throw STACK_SEGMENT;
 		ss.storeWord((sp - 2) & 0xffff, (short)eip.getValue());
 		esp.setValue(sp-2);
 	}
 	else if (!op32 && addr32)
 	{
 		if (sp< 2 && sp > 0)
 			throw STACK_SEGMENT;
 		ss.storeWord((sp - 2), (short)eip.getValue());
 		esp.setValue((sp&0xffff0000)|((sp-2)&0xffff));
 	}
 	else if (op32 && !addr32)
 	{
 		if (((sp & 0xffff) < 4) && ((sp & 0xffff) > 0))
 			throw STACK_SEGMENT;
 		ss.storeDoubleWord((sp - 4) & 0xffff, eip.getValue());
 		esp.setValue((sp&0xffff0000)|((sp-4)&0xffff));
 	}
 	else
 	{
 		if (sp < 4 && sp > 0)
 			throw STACK_SEGMENT;
 		ss.storeDoubleWord((sp - 4), eip.getValue());
 		esp.setValue(sp-4);
 	}	
 	eip.setValue(target);
 }
 
 private void intr(int vector, boolean op32, boolean addr32)
 {
 	if (vector == 0)
 		panic("INT 0 called");
 	if (((esp.getValue()&0xffff)<6)&&((esp.getValue()&0xffff)>0))
 		panic("No room on stack for interrupt");
 
 	if (!isModeReal())
 	{
 		intr_protected(vector,op32,addr32);
 		return;
 	}
 	
 	if (!op32 && !addr32)
 	{
 		esp.setValue((esp.getValue() & 0xffff0000) | (0xffff & (esp.getValue() - 2)));
 	        int flags = getFlags() & 0xffff;
 	        ss.storeWord(esp.getValue() & 0xffff, (short)flags);
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 		trap.clear();
 		esp.setValue((esp.getValue()&0xffff0000)|(0xffff&(esp.getValue()-2)));
 		ss.storeWord(esp.getValue()&0xffff,(short)cs.getValue());
 		esp.setValue((esp.getValue()&0xffff0000)|(0xffff&(esp.getValue()-2)));
 		ss.storeWord(esp.getValue()&0xffff,(short)eip.getValue());
 		eip.setValue(0xffff & idtr.loadWord(4*vector));
 		cs.setValue(0xffff & idtr.loadWord(4*vector+2));
 	}
 	else if (op32 && !addr32)
 	{
 		esp.setValue((esp.getValue() & 0xffff0000) | (0xffff & (esp.getValue() - 4)));
 	        int flags = getFlags();
 	        ss.storeDoubleWord(esp.getValue() & 0xffff, flags);
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 		trap.clear();
 		esp.setValue((esp.getValue()&0xffff0000)|(0xffff&(esp.getValue()-4)));
 		ss.storeDoubleWord(esp.getValue()&0xffff,cs.getValue());
 		esp.setValue((esp.getValue()&0xffff0000)|(0xffff&(esp.getValue()-4)));
 		ss.storeDoubleWord(esp.getValue()&0xffff,eip.getValue());
 		eip.setValue(idtr.loadDoubleWord(8*vector));
 		cs.setValue(0xffff & idtr.loadDoubleWord(8*vector+4));
 	}
 	else if (!op32 && addr32)
 	{
 		esp.setValue(esp.getValue()-2);
 	        int flags = getFlags() & 0xffff;
 	        ss.storeWord(esp.getValue(), (short)flags);
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 		trap.clear();
 		esp.setValue(esp.getValue()-2);
 		ss.storeWord(esp.getValue(),(short)cs.getValue());
 		esp.setValue(esp.getValue()-2);
 		ss.storeWord(esp.getValue(),(short)eip.getValue());
 		eip.setValue(0xffff & idtr.loadWord(4*vector));
 		cs.setValue(0xffff & idtr.loadWord(4*vector+2));
 	}
 	else if (op32 && addr32)
 	{
 		esp.setValue(esp.getValue()-4);
 	        int flags = getFlags();
 	        ss.storeDoubleWord(esp.getValue(), flags);
 		interruptEnable.clear();
 		interruptEnableSoon.clear();
 		trap.clear();
 		esp.setValue(esp.getValue()-4);
 		ss.storeDoubleWord(esp.getValue(),cs.getValue());
 		esp.setValue(esp.getValue()-4);
 		ss.storeDoubleWord(esp.getValue(),eip.getValue());
 		eip.setValue(idtr.loadDoubleWord(8*vector));
 		cs.setValue(0xffff & idtr.loadDoubleWord(8*vector+4));
 	}
 
 //	System.out.printf("Software Interrupt %x\n",vector);
 	if(processorGUICode!=null) processorGUICode.push(GUICODE.SOFTWARE_INTERRUPT,vector);
 }
 
 
 private void intr_protected(int vector,boolean op32, boolean addr32)
 {
 	int newIP=0, newCS=0;
 	int dpl;
 	long descriptor=0;
 
 	if (!op32 || !addr32)
 		panic("Only handling 32 bit mode protected interrupts");
 	
 	//get the new CS:EIP from the IDT
 	boolean sup=linearMemory.isSupervisor;
 	linearMemory.setSupervisor(true);
 	vector=vector*8;
 	descriptor=idtr.loadQuadWord(vector);
 	int segIndex=(int)((descriptor>>16)&0xffff);
 	long newSegmentDescriptor=0;
 	if ((segIndex&4)!=0)
 		newSegmentDescriptor=ldtr.loadQuadWord(segIndex&0xfff8);
 	else
 		newSegmentDescriptor=gdtr.loadQuadWord(segIndex&0xfff8);
 	linearMemory.setSupervisor(sup);
 	
 	dpl=(int)((newSegmentDescriptor>>45)&0x3);
 	newIP = (int)(((descriptor>>32)&0xffff0000)|(descriptor&0x0000ffff));
 	newCS = (int)((descriptor>>16)&0xffff);
 
 	int sesp = esp.getValue();
 	if (dpl<current_privilege_level)
 	{	
 		//calculate new stack segment
 		int stackAddress=dpl*8+4;
 		int newSS=0xffff&(tss.loadWord(stackAddress+4));
 		int newSP=tss.loadDoubleWord(stackAddress);
 
 		int oldSS=ss.getValue();
 		int oldSP=esp.getValue();
 		ss.setValue(newSS);
 		esp.setValue(newSP);
 		//save SS:ESP on the stack
 		sesp=newSP;
 		sesp-=4;
 		ss.storeDoubleWord(sesp, oldSS);
 		sesp-=4;
 		ss.storeDoubleWord(sesp, oldSP);
 	}
 	
 	//save the flags on the stack
 	sesp-=4;
     int flags = getFlags();
     ss.storeDoubleWord(esp.getValue(), flags);
 		//disable interrupts
 	interruptEnable.clear();
 	interruptEnableSoon.clear();
 		//save CS:IP on the stack
 	sesp-=4;
 	ss.storeDoubleWord(sesp, cs.getValue());
 	sesp-=4;
 	ss.storeDoubleWord(sesp, eip.getValue());
 	esp.setValue(sesp);
 		//change CS and IP to the ISR's values
 	
 	cs.setProtectedValue(newCS,descriptor);
 	eip.setValue(newIP);
 	setCPL(dpl);
 //	current_privilege_level=dpl;
 }
 
 
 public int iret(boolean op32, boolean addr32)
 {
 	if (!isModeReal())
 	{
 		return iret_protected(op32,addr32);
 	}
 	
 	int flags=0;
 	if (!op32 && !addr32)
 	{
 		eip.setValue(ss.loadWord(esp.getValue()&0xffff)&0xffff);
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+2)&0xffff));
 		cs.setValue(ss.loadWord(esp.getValue()&0xffff)&0xffff);
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+2)&0xffff));
 		flags=(ss.loadWord(esp.getValue()&0xffff)&0xffff);
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+2)&0xffff));
 	}
 	else if (op32 && !addr32)
 	{
 		eip.setValue(ss.loadDoubleWord(esp.getValue()&0xffff));
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+4)&0xffff));
 		cs.setValue(ss.loadDoubleWord(esp.getValue()&0xffff)&0xffff);
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+4)&0xffff));
 		flags=(ss.loadDoubleWord(esp.getValue()&0xffff));
 		esp.setValue((esp.getValue()&0xffff0000)|((esp.getValue()+4)&0xffff));
 	}
 	else if (!op32 && addr32)
 	{
 		eip.setValue(ss.loadWord(esp.getValue())&0xffff);
 		esp.setValue(esp.getValue()+2);
 		cs.setValue(ss.loadWord(esp.getValue())&0xffff);
 		esp.setValue(esp.getValue()+2);
 		flags=(ss.loadWord(esp.getValue())&0xffff);
 		esp.setValue(esp.getValue()+2);
 	}
 	else
 	{
 		eip.setValue(ss.loadDoubleWord(esp.getValue()));
 		esp.setValue(esp.getValue()+4);
 		cs.setValue(ss.loadDoubleWord(esp.getValue())&0xffff);
 		esp.setValue(esp.getValue()+4);
 		flags=(ss.loadDoubleWord(esp.getValue()));
 		esp.setValue(esp.getValue()+4);
 	}
 
 //	System.out.println("Returning from interrupt");
 	return flags;
 }
 
 private int iret_protected(boolean op32, boolean addr32)
 {
 	int flags=0;
 	
 	eip.setValue(ss.loadDoubleWord(esp.getValue()));
 	esp.setValue(esp.getValue()+4);
 	cs.setValue(ss.loadDoubleWord(esp.getValue())&0xffff);
 	esp.setValue(esp.getValue()+4);
 	flags=(ss.loadDoubleWord(esp.getValue()));
 	esp.setValue(esp.getValue()+4);
 	
 	if (cs.rpl>current_privilege_level)
 	{
 		int newSP=ss.loadDoubleWord(esp.getValue());
 		esp.setValue(esp.getValue()+4);
 		int newSS=ss.loadDoubleWord(esp.getValue());
 		esp.setValue(newSP);
 		ss.setValue(newSS);
 		setCPL(cs.rpl);
 //		current_privilege_level=cs.rpl;
 	}
 	return flags;
 }
 
 private void ins(int port, int b, boolean addr32)
 {
 	if (!addr32)
 	{
 		int addr = edi.getValue() & 0xffff;
 		if (b==1)
 			es.storeByte(addr & 0xffff, (byte)ioports.ioPortReadByte(port));
 		else if (b==2)
 			es.storeWord(addr & 0xffff, (short)ioports.ioPortReadWord(port));
 		else
 			es.storeDoubleWord(addr & 0xffff, ioports.ioPortReadLong(port));
 		if (direction.read()) 
 			addr -= b;
 		else
 			addr += b;
 	
 		edi.setValue((edi.getValue()&~0xffff)|(addr&0xffff));
 	}
 	else
 	{
 		int addr = edi.getValue();
 		if (b==1)
 			es.storeByte(addr, (byte)ioports.ioPortReadByte(port));
 		else if (b==2)
 			es.storeWord(addr, (short)ioports.ioPortReadWord(port));
 		else
 			es.storeDoubleWord(addr, ioports.ioPortReadLong(port));
 		if (direction.read()) 
 			addr -= b;
 		else
 			addr += b;
 
 		edi.setValue(addr);
 	}
 }
 
 private void rep_ins(int port, int b, boolean addr32)
 {
 	if (!addr32)
 	{
 		int count = ecx.getValue() & 0xffff;
 		int addr = edi.getValue() & 0xffff;
 	
 		while (count != 0) 
 		{
 			if (b==1)
 				es.storeByte(addr & 0xffff, (byte)ioports.ioPortReadByte(port));		
 			else if (b==2)
 				es.storeWord(addr & 0xffff, (short)ioports.ioPortReadWord(port));		
 			else
 				es.storeDoubleWord(addr & 0xffff, ioports.ioPortReadLong(port));		
 			count--;
 			if (direction.read())
 				addr -= b;
 			else
 				addr += b;
 		}
 		ecx.setValue((ecx.getValue() & ~0xffff) | (count & 0xffff));
 		edi.setValue((edi.getValue() & ~0xffff) | (addr & 0xffff));
 	}
 	else
 	{
 		int count = ecx.getValue();
 		int addr = edi.getValue();
 	
 		while (count != 0) 
 		{
 			if (b==1)
 				es.storeByte(addr, (byte)ioports.ioPortReadByte(port));		
 			else if (b==2)
 				es.storeWord(addr, (short)ioports.ioPortReadWord(port));		
 			else
 				es.storeDoubleWord(addr, ioports.ioPortReadLong(port));		
 			count--;
 			if (direction.read())
 				addr -= b;
 			else
 				addr += b;
 		}
 		ecx.setValue(count);
 		edi.setValue(addr);
 	}
 }
 
 
 private void outs(int port, Segment seg, int b, boolean addr32)
 {
 	if (!addr32)
 	{
 		int addr = esi.getValue() & 0xffff;
 	
 		if (b==1)
 			ioports.ioPortWriteByte(port, 0xff & seg.loadByte(addr&0xffff));
 		else if (b==2)
 			ioports.ioPortWriteWord(port, 0xffff & seg.loadWord(addr&0xffff));
 		else
 			ioports.ioPortWriteLong(port, seg.loadDoubleWord(addr&0xffff));
 	
 		if (direction.read()) 
 			addr -= b;
 		else
 			addr += b;
 
 if (esi.getValue()==0x3ffff) System.out.println("outs addr16 "+esi.getValue()+" "+addr);
 		esi.setValue((esi.getValue()&~0xffff)|(addr&0xffff));
 	}
 	else
 	{
 		int addr = esi.getValue();
 		
 		if (b==1)
 			ioports.ioPortWriteByte(port, 0xff & seg.loadByte(addr));
 		else if (b==2)
 			ioports.ioPortWriteWord(port, 0xffff & seg.loadWord(addr));
 		else
 			ioports.ioPortWriteLong(port, seg.loadDoubleWord(addr));
 	
 		if (direction.read()) 
 			addr -= b;
 		else
 			addr += b;
 	
 if (esi.getValue()==0x3ffff) System.out.println("outs addr32 "+esi.getValue()+" "+addr);
 		esi.setValue(addr);		
 	}
 }
 
 private void rep_outs(int port, Segment seg, int b, boolean addr32)
 {
 	if (!addr32)
 	{
 		int count = ecx.getValue() & 0xffff;
 		int addr = esi.getValue() & 0xffff;
 	
 		while (count != 0) 
 		{
 			if (b==1)
 				ioports.ioPortWriteByte(port, 0xff & seg.loadByte(addr&0xffff));
 			else if (b==2)
 				ioports.ioPortWriteWord(port, 0xffff & seg.loadWord(addr&0xffff));
 			else
 				ioports.ioPortWriteLong(port, seg.loadDoubleWord(addr&0xffff));
 			count--;
 			if (direction.read())
 				addr -= b;
 			else
 				addr += b;
 		}
 		ecx.setValue((ecx.getValue() & ~0xffff) | (count & 0xffff));
 		esi.setValue((esi.getValue() & ~0xffff) | (addr & 0xffff));
 	}
 	else
 	{
 		int count = ecx.getValue();
 		int addr = esi.getValue();
 
 		while (count != 0) 
 		{
 			if (b==1)
 				ioports.ioPortWriteByte(port, 0xff & seg.loadByte(addr));
 			else if (b==2)
 				ioports.ioPortWriteWord(port, 0xffff & seg.loadWord(addr));
 			else
 				ioports.ioPortWriteLong(port, seg.loadDoubleWord(addr));
 			count--;
 			if (direction.read())
 				addr -= b;
 			else
 				addr += b;
 		}
 		ecx.setValue(count);
 		esi.setValue(addr);		
 	}
 }
 
 private void lods(Segment seg, int b, boolean addr32)
 {
 	int addr;
 	if(!addr32)
 		addr = esi.getValue() & 0xffff;
 	else
 		addr = esi.getValue();
 	if (b==1)
 		eax.setValue((eax.getValue()&~0xff)|(0xff&seg.loadByte(addr)));
 	else if (b==2)
 		eax.setValue((eax.getValue()&~0xffff)|(0xffff&seg.loadWord(addr)));
 	else
 		eax.setValue(seg.loadDoubleWord(addr));
 	if(direction.read())
 		addr-=b;
 	else
 		addr+=b;
 	if(!addr32)
 		esi.setValue((esi.getValue()&~0xffff)|(addr&0xffff));
 	else
 		esi.setValue(addr);
 }
 
 private void rep_lods(Segment seg, int b, boolean addr32)
 {
 	int count,addr,data;
 	if(!addr32)
 	{
 		count = ecx.getValue()&0xffff;
 		addr = esi.getValue()&0xffff;
 	}
 	else
 	{
 		count=ecx.getValue();
 		addr=esi.getValue();
 	}
 	if(b==1)
 		data = eax.getValue()&0xff;
 	else if (b==2)
 		data = eax.getValue()&0xffff;
 	else
 		data = eax.getValue();
 
 	while(count!=0)
 	{
 		if(!addr32)
 		{
 			if(b==1)
 				data=0xff&seg.loadByte(addr&0xffff);
 			else if (b==2)
 				data=0xffff&seg.loadWord(addr&0xffff);
 			else
 				data=seg.loadDoubleWord(addr&0xffff);
 		}
 		else
 		{
 			if(b==1)
 				data=0xff&seg.loadByte(addr);
 			else if (b==2)
 				data=0xffff&seg.loadWord(addr);
 			else
 				data=seg.loadDoubleWord(addr);
 		}
 		count--;
 		if (direction.read())
 			addr-=b;
 		else
 			addr+=b;
 	}
 	if (b==1)
 		eax.setValue((eax.getValue()&~0xff)|data);
 	else if (b==2)
 		eax.setValue((eax.getValue()&~0xffff)|data);
 	else
 		eax.setValue(data);
 	if(!addr32)
 	{
 		ecx.setValue((ecx.getValue()&~0xffff)|(count&0xffff));
 		esi.setValue((esi.getValue()&~0xffff)|(addr&0xffff));
 	}
 	else
 	{
 		ecx.setValue(count);
 		esi.setValue(addr);
 	}
 }
 
 private void movs(Segment seg, int b, boolean addr32)
 {
 	int inaddr,outaddr;
 	if(!addr32)
 	{
 		inaddr = edi.getValue() & 0xffff;
 		outaddr = esi.getValue() & 0xffff;
 	}
 	else
 	{
 		inaddr = edi.getValue();
 		outaddr = esi.getValue();
 	}
 	if (b==1)
 		es.storeByte(inaddr, seg.loadByte(outaddr));
 	else if (b==2)
 		es.storeWord(inaddr, seg.loadWord(outaddr));
 	else
 		es.storeDoubleWord(inaddr, seg.loadDoubleWord(outaddr));
 	if(direction.read())
 	{
 		inaddr-=b;
 		outaddr-=b;
 	}
 	else
 	{
 		inaddr+=b;
 		outaddr+=b;
 	}
 	if(!addr32)
 	{
 		esi.setValue((esi.getValue()&~0xffff)|(outaddr&0xffff));
 		edi.setValue((edi.getValue()&~0xffff)|(inaddr&0xffff));
 	}
 	else
 	{
 		esi.setValue(outaddr);
 		edi.setValue(inaddr);
 	}
 }
 
 private void rep_movs(Segment seg, int b, boolean addr32)
 {
 	int count,inaddr,outaddr;
 	if(!addr32)
 	{
 		count = ecx.getValue()&0xffff;
 		outaddr = esi.getValue()&0xffff;
 		inaddr = edi.getValue()&0xffff;
 	}
 	else
 	{
 		count=ecx.getValue();
 		outaddr=esi.getValue();
 		inaddr=edi.getValue();
 	}
 
 	while(count!=0)
 	{
 		if(!addr32)
 		{
 			if(b==1)
 				es.storeByte(inaddr&0xffff, seg.loadByte(outaddr&0xffff));
 			else if (b==2)
 				es.storeWord(inaddr&0xffff, seg.loadWord(outaddr&0xffff));
 			else
 				es.storeDoubleWord(inaddr&0xffff, seg.loadDoubleWord(outaddr&0xffff));
 		}
 		else
 		{
 			if(b==1)
 				es.storeByte(inaddr, seg.loadByte(outaddr));
 			else if (b==2)
 				es.storeWord(inaddr, seg.loadWord(outaddr));
 			else
 				es.storeDoubleWord(inaddr, seg.loadDoubleWord(outaddr));
 		}
 		count--;
 		if (direction.read())
 		{
 			inaddr-=b;
 			outaddr-=b;
 		}
 		else
 		{
 			inaddr+=b;
 			outaddr+=b;
 		}
 	}
 	if(!addr32)
 	{
 		ecx.setValue((ecx.getValue()&~0xffff)|(count&0xffff));
 		esi.setValue((esi.getValue()&~0xffff)|(outaddr&0xffff));
 		edi.setValue((edi.getValue()&~0xffff)|(inaddr&0xffff));
 	}
 	else
 	{
 		ecx.setValue(count);
 		esi.setValue(outaddr);
 		edi.setValue(inaddr);
 	}
 }
 
 private void stos(int data, int b, boolean addr32)
 {
 	int addr;
 	if(!addr32)
 		addr = edi.getValue() & 0xffff;
 	else
 		addr = edi.getValue();
 	if (b==1)
 		es.storeByte(addr,(byte)data);
 	else if (b==2)
 		es.storeWord(addr,(short)data);
 	else
 		es.storeDoubleWord(addr,data);
 	if(direction.read())
 		addr-=b;
 	else
 		addr+=b;
 	if(!addr32)
 		edi.setValue((edi.getValue()&~0xffff)|(addr&0xffff));
 	else
 		edi.setValue(addr);
 
 }
 
 private void rep_stos(int data, int b, boolean addr32)
 {
 	int count,addr;
 	if(!addr32)
 	{
 		count = ecx.getValue()&0xffff;
 		addr = edi.getValue()&0xffff;
 	}
 	else
 	{
 		count=ecx.getValue();
 		addr=edi.getValue();
 	}
 	while(count!=0)
 	{
 		if(!addr32)
 		{
 			if(b==1)
 				es.storeByte(addr&0xffff,(byte)data);
 			else if (b==2)
 				es.storeWord(addr&0xffff,(short)data);
 			else
 				es.storeDoubleWord(addr&0xffff,data);
 		}
 		else
 		{
 			if(b==1)
 				es.storeByte(addr,(byte)data);
 			else if (b==2)
 				es.storeWord(addr,(short)data);
 			else
 				es.storeDoubleWord(addr,data);
 		}
 		count--;
 		if (direction.read())
 			addr-=b;
 		else
 			addr+=b;
 	}
 	if(!addr32)
 	{
 		ecx.setValue((ecx.getValue()&~0xffff)|(count&0xffff));
 		edi.setValue((edi.getValue()&~0xffff)|(addr&0xffff));
 	}
 	else
 	{
 		ecx.setValue(count);
 		edi.setValue(addr);
 	}
 }
 
 private void mul_08(int data)
 {
 	int x = eax.getValue()&0xff;
 	int result = x*data;
 	eax.setValue((eax.getValue()&0xffff0000)|(result&0xffff));
 	overflow.set(result, Flag.OF_HIGH_BYTE_NZ);
 	carry.set(result, Flag.CY_HIGH_BYTE_NZ);
 }
 
 private void mul_16(int data)
 {
 	int x = eax.getValue()&0xffff;
 	int result = x*data;
 	eax.setValue((eax.getValue()&0xffff0000)|(result&0xffff));
 	result=result>>16;
 	edx.setValue((edx.getValue()&0xffff0000)|(result&0xffff));
 	overflow.set(result, Flag.OF_LOW_WORD_NZ);
 	carry.set(result, Flag.CY_LOW_WORD_NZ);
 }
 
 private void mul_32(int data)
 {
 	long x = eax.getValue()&0xffffffffl;
 	long y = 0xffffffffl&data;
 	long result=x*y;
 	eax.setValue((int)result);
 	result=result>>>32;
 	edx.setValue((int)result);
 	overflow.set((int)result, Flag.OF_NZ);
 	carry.set((int)result, Flag.CY_NZ);
 }
 
 private void imula_08(byte data)
 {
 	byte x = (byte)eax.getValue();
 	int result = x*data;
 	eax.setValue((eax.getValue()&~0xffff)|(result&0xffff));
 	overflow.set(result, Flag.OF_NOT_BYTE);
 	carry.set(result, Flag.CY_NOT_BYTE);
 }
 
 private void imula_16(short data)
 {
 	short x = (short)eax.getValue();
 	int result = x*data;
 	eax.setValue((eax.getValue()&~0xffff)|(result&0xffff));
 	edx.setValue((edx.getValue()&~0xffff)|(result>>>16));
 	overflow.set(result, Flag.OF_NOT_SHORT);
 	carry.set(result, Flag.CY_NOT_SHORT);
 }
 
 private void imula_32(int data)
 {
 	long eaxvalue = (long)eax.getValue();
 	long y=(long)data;
 	long result=eaxvalue*y;
 	eax.setValue((int)result);
 	edx.setValue((int)(result>>>32));
 	overflow.set(result,Flag.OF_NOT_INT);
 	carry.set(result,Flag.CY_NOT_INT);
 }
 
 private int imul_16(short data0, short data1)
 {
 	int result = data0*data1;
 	overflow.set(result, Flag.OF_NOT_SHORT);
 	carry.set(result, Flag.CY_NOT_SHORT);
 	return result;
 }
 
 private int imul_32(int data0, int data1)
 {
 	long result = ((long)data0)*((long)data1);
 	overflow.set(result, Flag.OF_NOT_SHORT);
 	carry.set(result, Flag.CY_NOT_SHORT);
 	return (int)result;
 }
 
 private void div_08(int data)
 {
 	if(data==0)
 		throw DIVIDE_ERROR;
 
 	int x = (eax.getValue()&0xffff);
 	int result=x/data;
 	if (result>0xff)
 		throw DIVIDE_ERROR;
 	int remainder = (x%data)<<8;
 	eax.setValue((eax.getValue()&~0xffff)|(0xff&result)|(0xff00&remainder));
 }
 
 private void div_16(int data)
 {
 	if(data==0)
 		throw DIVIDE_ERROR;
 
 	long x = (edx.getValue()&0xffffl);
 	x<<=16;
 	x|=(eax.getValue()&0xffffl);
 	long result=x/data;
 	if (result>0xffffl)
 		throw DIVIDE_ERROR;
 	long remainder = (x%data);
 	eax.setValue((eax.getValue()&~0xffff)|(int)(result&0xffff));
 	edx.setValue((edx.getValue()&~0xffff)|(int)(remainder&0xffff));
 }
 
 private void div_32(int data)
 {
 	long d = 0xffffffffl&data;
 	if(d==0) throw DIVIDE_ERROR;
 	long t = (long)edx.getValue();
 	t<<=32;
 	t|=(0xffffffffl&eax.getValue());
 	long r2=t&1;
 	long n2=t>>>1;
 	long q2=n2/d;
 	long m2=n2%d;
 	long q=q2<<1;
 	long r=(m2<<1)+r2;
 	q+=(r/d);
 	r%=d;
 	if (q>0xffffffffl) throw DIVIDE_ERROR;
 	eax.setValue((int)q);
 	edx.setValue((int)r);
 }
 
 private void idiv_08(byte data)
 {
 	if(data==0)
 		throw DIVIDE_ERROR;
 
 	short x = (short)eax.getValue();
 	int result=x/(short)data;
 	int remainder = (x%data);
 	if((result>Byte.MAX_VALUE)||(result<Byte.MIN_VALUE))
 		throw DIVIDE_ERROR;
 	eax.setValue((eax.getValue()&~0xffff)|(0xff&result)|((0xff&remainder)<<8));
 }
 
 private void idiv_16(short data)
 {
 	if(data==0)
 		throw DIVIDE_ERROR;
 
 	int x = (edx.getValue()<<16) | (eax.getValue()&0xffff);
 	int result=x/(int)data;
 	int remainder = (x%data);
 	if((result>Short.MAX_VALUE)||(result<Short.MIN_VALUE))
 		throw DIVIDE_ERROR;
 	eax.setValue((eax.getValue()&~0xffff)|(0xffff&result));
 	edx.setValue((edx.getValue()&~0xffff)|(0xffff&remainder));
 }
 
 private void idiv_32(int data)
 {
 	if(data==0) throw DIVIDE_ERROR;
 	long t=(0xffffffffl&edx.getValue())<<32;
 	t|=(0xffffffffl&eax.getValue());
 	long result=t/data;
 	if(result>Integer.MAX_VALUE || result<Integer.MIN_VALUE) throw DIVIDE_ERROR;
 	long r = t%data;
 	eax.setValue((int)result);
 	edx.setValue((int)r);
 }
 
 private void btc_mem(int offset, Segment seg, int addr)
 {
 	addr+=(offset>>>3);
 	offset&=0x7;
	byte data=seg.loadByte(addr);
 	seg.storeByte(addr,(byte)(data^(1<<offset)));
 	carry.set(data,offset,Flag.CY_NTH_BIT_SET);
 }
 
 private void bts_mem(int offset, Segment seg, int addr)
 {
 	addr+=(offset>>>3);
 	offset&=0x7;
	byte data=seg.loadByte(addr);
 	seg.storeByte(addr,(byte)(data|(1<<offset)));
 	carry.set(data,offset,Flag.CY_NTH_BIT_SET);
 }
 
 private void btr_mem(int offset, Segment seg, int addr)
 {
 	addr+=(offset>>>3);
 	offset&=0x7;
	byte data=seg.loadByte(addr);
 	seg.storeByte(addr,(byte)(data&~(1<<offset)));
 	carry.set(data,offset,Flag.CY_NTH_BIT_SET);
 }
 
 private void bt_mem(int offset, Segment seg, int addr)
 {
 	addr+=(offset>>>3);
 	offset&=0x7;
	byte data=seg.loadByte(addr);
 	carry.set(data,offset,Flag.CY_NTH_BIT_SET);
 }
 
 private int bsf(int source, int initial)
 {
 	if (source == 0)
 	{
 		zero.set();
 		return initial;
 	}
 	else
 	{
 		zero.clear();
 		int y;
 		int i=source;
 		if (i==0) return 32;
 		int n=31;
 		y=i<<16; if (y!=0) {n-=16; i=y;}
 		y=i<<8; if (y!=0) {n-=8; i=y;}
 		y=i<<4; if (y!=0) {n-=4; i=y;}
 		y=i<<2; if (y!=0) {n-=2; i=y;}
 		return n-((i<<1)>>>31);
 	}
 }
 
 private int bsr(int source, int initial)
 {
 	if (source==0)
 	{
 		zero.set();
 		return initial;
 	}
 	else
 	{
 		zero.clear();
 		int i=source;
 		if (i == 0) return -1;
 		int n=1;
 		if (i >>> 16 == 0) { n += 16; i <<= 16; }
 		if (i >>> 24 == 0) { n += 8; i <<= 8; }
 		if (i >>> 28 == 0) { n += 4; i <<= 4; }
 		if (i >>> 30 == 0) { n += 2; i <<= 2; }
 		n-=i>>>31;
 		return 31-n;
 	}
 }
     
 private void aaa()
 {
 	if (((eax.getValue() & 0xf) > 0x9) || auxiliaryCarry.read()) 
 	{
 		int alCarry = ((eax.getValue() & 0xff) > 0xf9) ? 0x100 : 0x000;
 		eax.setValue((0xffff0000 & eax.getValue()) | (0x0f & (eax.getValue() + 6)) | (0xff00 & (eax.getValue() + 0x100 + alCarry)));
 		auxiliaryCarry.set();
 		carry.set();
 	}
 	else
 	{
 		auxiliaryCarry.clear();
 		carry.clear();
 		eax.setValue(eax.getValue()&0xffffff0f);
 	}
 }
 
 private void aad(int base)
 {
 	int tl = (eax.getValue() & 0xff);
 	int th = ((eax.getValue() >> 8) & 0xff);
 
 	int ax1 = th * base;
 	int ax2 = ax1 + tl;
 
         eax.setValue((eax.getValue() & ~0xffff) | (ax2 & 0xff));
 
 	zero.set((byte)ax2);
 	parity.set((byte)ax2);
 	sign.set((byte)ax2);
 	auxiliaryCarry.set(ax1, ax2, Flag.AC_BIT4_NEQ);
 	carry.set(ax2, Flag.CY_GREATER_FF);
 	overflow.set(ax2, tl, Flag.OF_BIT7_DIFFERENT);
 }
 
 private int aam(int base)
 {
 	int tl = 0xff & eax.getValue();
 	if (base == 0) 
 		throw DIVIDE_ERROR;
 	int ah = 0xff & (tl / base);
 	int al = 0xff & (tl % base);
 	eax.setValue(eax.getValue() & ~0xffff);
 	eax.setValue(eax.getValue() | (al | (ah << 8)));
 	auxiliaryCarry.clear();
 	return (byte) al;
 }
 
 private void aas()
 {
 	if (((eax.getValue() & 0xf) > 0x9) || auxiliaryCarry.read()) 
 	{
 		int alBorrow = (eax.getValue() & 0xff) < 6 ? 0x100 : 0x000;
 		eax.setValue((0xffff0000 & eax.getValue()) | (0x0f & (eax.getValue() - 6)) | (0xff00 & (eax.getValue() - 0x100 - alBorrow)));
 		auxiliaryCarry.set();
 		carry.set();
 	}
 	else 
 	{
 		auxiliaryCarry.clear();
 		carry.clear();
 		eax.setValue(eax.getValue()&0xffffff0f);
 	}
 }
 
 private void daa()
 {
 	int al = eax.getValue() & 0xff;
 	boolean newCF;
 	if (((eax.getValue() & 0xf) > 0x9) || auxiliaryCarry.read())
 	{
         	al += 6;
 		auxiliaryCarry.set();
 	}
 	else
 		auxiliaryCarry.clear();
 
 	if (((al & 0xff) > 0x9f) || carry.read()) 
 	{
 		al += 0x60;
 		newCF = true;
 	} 
 	else
 		newCF = false;
 
 	eax.setValue((eax.getValue()&~0xff)|(0xff&al));
 
 	overflow.clear();
 	zero.set((byte)al);
 	parity.set((byte)al);
 	sign.set((byte)al);
 	carry.set(newCF);
 }
 
 private void das()
 {
 	boolean tempCF = false;
 	int tempAL = 0xff & eax.getValue();
  	if (((tempAL & 0xf) > 0x9) || auxiliaryCarry.read()) 
 	{
 		auxiliaryCarry.set();
 		eax.setValue((eax.getValue() & ~0xff) | ((eax.getValue() - 0x06) & 0xff));
 		tempCF = (tempAL < 0x06) || carry.read();
 	}
 	
         if ((tempAL > 0x99) || carry.read()) 
 	{
 		eax.setValue( (eax.getValue() & ~0xff) | ((eax.getValue() - 0x60) & 0xff));
 		tempCF = true;
 	}
 
  	overflow.clear();
 	zero.set((byte)eax.getValue());
 	parity.set((byte)eax.getValue());
 	sign.set((byte)eax.getValue());
 	carry.set(tempCF);
 }
 
 private void lahf()
 {
 	int result = 0x0200;
 	if (sign.read()) result|=0x8000;
 	if (zero.read()) result|=0x4000;
 	if (auxiliaryCarry.read()) result|=0x1000;
 	if (parity.read()) result|=0x0400;
 	if (carry.read()) result|=0x0100;
 	eax.setValue((eax.getValue()&0xffff00ff)|result);
 }
 
 private void sahf()
 {
 	int ah = (eax.getValue()&0xff00);
 	carry.set(0!=(ah&0x0100));
 	parity.set(0!=(ah&0x0400));
 	auxiliaryCarry.set(0!=(ah&0x1000));
 	zero.set(0!=(ah&0x4000));
 	sign.set(0!=(ah&0x8000));
 }
 
 private long rdtsc()
 {
 	return computer.clock.getTime();
 }
 
 private void cpuid()
 {
 	switch(eax.getValue())
 	{
 		case 0x00:
 			eax.setValue(0x02);
 			//this spells out "GenuineIntel"
 			ebx.setValue(0x756e6547);
 			edx.setValue(0x49656e69);
 			ecx.setValue(0x6c65746e);
 			break;
 		case 0x01:
 			eax.setValue(0x633);	//use Pentium II model 8 stepping 3 until I can find specs for an older proc.
 			ebx.setValue(8<<8);
 			ecx.setValue(0);
 			int features=0;
 			features|=0;	//no features at all
 
 //		    features |= 0x01; //Have an FPU;
 		    features |= 0x00; //Have no FPU;
 //	    features |= (1<< 8);  // Support CMPXCHG8B instruction
 //	    features |= (1<< 4);  // implement TSC
 //	    features |= (1<< 5);  // support RDMSR/WRMSR
 	    //features |= (1<<23);  // support MMX
 	    //features |= (1<<24);  // Implement FSAVE/FXRSTOR instructions.
 //	    features |= (1<<15);  // Implement CMOV instructions.
 	    //features |= (1<< 9);   // APIC on chip
 	    //features |= (1<<25);  // support SSE
 	    features |= (1<< 3);  // Support Page-Size Extension (4M pages)
 	    features |= (1<<13);  // Support Global pages.
 	    //features |= (1<< 6);  // Support PAE.
 //	    features |= (1<<11);  // SYSENTER/SYSEXIT
 
 			edx.setValue(features);
 			break;
 		case 0x02:
 			eax.setValue(0x410601);
 			ebx.setValue(0);
 			ecx.setValue(0);
 			edx.setValue(0);
 			break;
 	}
 }
 
 private void bitwise_flags(int result)
 {
 	overflow.clear();
 	carry.clear();
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 }
 
 private void arithmetic_flags_08(int result, int operand1, int operand2)
 {
 	zero.set((byte)result);
 	parity.set(result);
 	sign.set((byte)result);
 	carry.set(result, Flag.CY_TWIDDLE_FF);
 	auxiliaryCarry.set(operand1, operand2, result, Flag.AC_XOR);
 }
 
 private void arithmetic_flags_16(int result, int operand1, int operand2)
 {
 	zero.set((short)result);
 	parity.set(result);
 	sign.set((short)result);
 	carry.set(result, Flag.CY_TWIDDLE_FFFF);
 	auxiliaryCarry.set(operand1, operand2, result, Flag.AC_XOR);
 }
 
 private void arithmetic_flags_32(long result, int operand1, int operand2)
 {
 	zero.set((int)result);
 	parity.set((int)result);
 	sign.set((int)result);
 	carry.set(result, Flag.CY_TWIDDLE_FFFFFFFF);
 	auxiliaryCarry.set(operand1, operand2, (int)result, Flag.AC_XOR);
 }
 
 private void add_flags_08(int result, int operand1, int operand2)
 {
 	arithmetic_flags_08(result, operand1, operand2);
 	overflow.set(result, operand1 , operand2, Flag.OF_ADD_BYTE);
 }
 
 private void add_flags_16(int result, int operand1, int operand2)
 {
 	arithmetic_flags_16(result, operand1, operand2);
 	overflow.set(result, operand1 , operand2, Flag.OF_ADD_SHORT);
 }
 
 private void add_flags_32(long result, int operand1, int operand2)
 {
 	long res = (0xffffffffl & operand1) + (0xffffffffl & operand2);
 	arithmetic_flags_32(res, operand1, operand2);
 	overflow.set((int)res, operand1 , operand2, Flag.OF_ADD_INT);
 }
 
 private void adc_flags_08(int result, int operand1, int operand2)
 {
 	if (carry.read() && (operand2 == 0xff))
 	{
 		arithmetic_flags_08(result, operand1, operand2);
 		overflow.clear();
 		carry.set();
 	}
 	else
 	{
 		overflow.set(result, operand1, operand2, Flag.OF_ADD_BYTE);
 		arithmetic_flags_08(result, operand1, operand2);
 	}
 }
 
 private void adc_flags_16(int result, int operand1, int operand2)
 {
 	if (carry.read() && (operand2 == 0xffff))
 	{
 		arithmetic_flags_16(result, operand1, operand2);
 		overflow.clear();
 		carry.set();
 	}
 	else
 	{
 		overflow.set(result, operand1, operand2, Flag.OF_ADD_SHORT);
 		arithmetic_flags_16(result, operand1, operand2);
 	}
 }
 
 private void adc_flags_32(long result, int operand1, int operand2)
 {
 	long res = (0xffffffffl & operand1) + (0xffffffffl & operand2) + (carry.read()? 1l:0l);
 
 	if (carry.read() && (operand2 == 0xffffffff))
 	{
 		arithmetic_flags_32(res, operand1, operand2);
 		overflow.clear();
 		carry.set();
 	}
 	else
 	{
 		overflow.set((int)res, operand1, operand2, Flag.OF_ADD_INT);
 		arithmetic_flags_32(res, operand1, operand2);
 	}
 }
 
 private void sub_flags_08(int result, int operand1, int operand2)
 {
 	arithmetic_flags_08(result, operand1, operand2);
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_BYTE);
 }
 
 private void sub_flags_16(int result, int operand1, int operand2)
 {
 	arithmetic_flags_16(result, operand1, operand2);
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_SHORT);
 }
 
 private void sub_flags_32(long result, int operand1, int operand2)
 {
 	long res = (0xffffffffl&operand1) - (0xffffffffl&operand2);
 	arithmetic_flags_32(res, operand1, operand2);
 	overflow.set((int)res, operand1, operand2, Flag.OF_SUB_INT);
 }
 
 private void rep_sub_flags_08(int used, int operand1, int operand2)
 {
 	if (used == 0)
 		return;
 	int result = operand1 - operand2;
 	arithmetic_flags_08(result, operand1, operand2);
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_BYTE);
 }
 
 private void rep_sub_flags_16(int used, int operand1, int operand2)
 {
 	if (used == 0)
 		return;
 	int result = operand1 - operand2;
 	arithmetic_flags_16(result, operand1, operand2);
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_SHORT);
 }
 
 private void rep_sub_flags_32(int used, int operand1, int operand2)
 {
 	if (used == 0)
 		return;
 	long res = (0xffffffffl&operand1) - (0xffffffffl&operand2);
 	arithmetic_flags_32(res, operand1, operand2);
 	overflow.set((int)res, operand1, operand2, Flag.OF_SUB_INT);
 }
 
 private void sbb_flags_08(int result, int operand1, int operand2)
 {
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_BYTE);
 	arithmetic_flags_08(result, operand1, operand2);
 }
 
 private void sbb_flags_16(int result, int operand1, int operand2)
 {
 	overflow.set(result, operand1, operand2, Flag.OF_SUB_SHORT);
 	arithmetic_flags_16(result, operand1, operand2);
 }
 
 private void sbb_flags_32(long result, int operand1, int operand2)
 {
 	long res = (0xffffffffl&operand1) - (0xffffffffl&operand2) - (carry.read()? 1l:0l);
 	overflow.set((int)res, operand1, operand2, Flag.OF_SUB_INT);
 	arithmetic_flags_32(res, operand1, operand2);
 }
 
 private void dec_flags_08(byte result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MAX_BYTE);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_MAX);
 }
 
 private void dec_flags_16(short result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MAX_SHORT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_MAX);
 }
 
 private void dec_flags_32(int result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MAX_INT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_MAX);
 }
 
 private void inc_flags_08(byte result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MIN_BYTE);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_ZERO);
 }
 
 private void inc_flags_16(short result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MIN_SHORT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_ZERO);
 }
 
 private void inc_flags_32(int result)
 {
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 	overflow.set(result, Flag.OF_MIN_INT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_ZERO);
 }
 
 private void shl_flags_08(byte result, byte initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHL_OUTBIT_BYTE);
 		if (count == 1)
 			overflow.set(result, Flag.OF_BIT7_XOR_CARRY);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void shl_flags_16(short result, short initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHL_OUTBIT_SHORT);
 		if (count == 1)
 			overflow.set(result, Flag.OF_BIT15_XOR_CARRY);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void shl_flags_32(int result, int initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHL_OUTBIT_INT);
 		if (count == 1)
 			overflow.set(result, Flag.OF_BIT31_XOR_CARRY);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void shr_flags_08(byte result, int initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHR_OUTBIT);
 		if (count == 1)
 			overflow.set(result, initial, Flag.OF_BIT7_DIFFERENT);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void shr_flags_16(short result, int initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHR_OUTBIT);
 		if (count == 1)
 			overflow.set(result, initial, Flag.OF_BIT15_DIFFERENT);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void shr_flags_32(int result, int initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHR_OUTBIT);
 		if (count == 1)
 			overflow.set(result, initial, Flag.OF_BIT31_DIFFERENT);
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void sar_flags(int result, int initial, int count)
 {
 	if (count > 0) 
 	{
 		carry.set(initial, count, Flag.CY_SHR_OUTBIT);
 		if (count == 1)
 			overflow.clear();
 		zero.set(result);
 		parity.set(result);
 		sign.set(result);
 	}
 }
 
 private void rol_flags_08(byte result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_LOWBIT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT7_XOR_CARRY);
 	}
 }
 
 private void rol_flags_16(short result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_LOWBIT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT15_XOR_CARRY);
 	}
 }
 
 private void rol_flags_32(int result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_LOWBIT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT31_XOR_CARRY);
 	}
 }
 
 private void ror_flags_08(byte result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_HIGHBIT_BYTE);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT6_XOR_CARRY);
 	}
 }
 
 private void ror_flags_16(short result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_HIGHBIT_SHORT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT14_XOR_CARRY);
 	}
 }
 
 private void ror_flags_32(int result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_HIGHBIT_INT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT30_XOR_CARRY);
 	}
 }
 
 private void rcl_flags_08(int result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_BYTE);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT7_XOR_CARRY);
 	}
 }
 
 private void rcl_flags_16(int result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_SHORT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT15_XOR_CARRY);
 	}
 }
 
 private void rcl_flags_32(long result, int count)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_INT);
 		if (count==1)
 			overflow.set(result, Flag.OF_BIT31_XOR_CARRY);
 	}
 }
 
 private void rcr_flags_08(int result, int count, int over)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_BYTE);
 		if (count==1)
 			overflow.set(over>0);
 	}
 }
 
 private void rcr_flags_16(int result, int count, int over)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_SHORT);
 		if (count==1)
 			overflow.set(over>0);
 	}
 }
 
 private void rcr_flags_32(long result, int count, int over)
 {
 	if (count>0)
 	{
 		carry.set(result, Flag.CY_OFFENDBIT_INT);
 		if (count==1)
 			overflow.set(over>0);
 	}
 }
 
 private void neg_flags_08(byte result)
 {
 	carry.set(result, Flag.CY_NZ);
 	overflow.set(result, Flag.OF_MIN_BYTE);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_NZERO);
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 }
 
 private void neg_flags_16(short result)
 {
 	carry.set(result, Flag.CY_NZ);
 	overflow.set(result, Flag.OF_MIN_SHORT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_NZERO);
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 }
 
 private void neg_flags_32(int result)
 {
 	carry.set(result, Flag.CY_NZ);
 	overflow.set(result, Flag.OF_MIN_INT);
 	auxiliaryCarry.set(result, Flag.AC_LNIBBLE_NZERO);
 	zero.set(result);
 	parity.set(result);
 	sign.set(result);
 }
 
 
 
 
 
 
 public void initializeDecoder()
 {
 	//assemble the table of instruction inputs and outputs by instrution
 	//this will speed processing
 
 	//29 different sources / destinations, + 1 miscellaneous
 	int types=30;
 	operandTable = new int[0x200][types][4];
 	for (int i=0; i<types; i++)
 	{
 		for (int j=0; j<inputTable0[i].length; j++)
 		{
 			int inst = inputTable0[i][j];
 			if (inst>=0xf00)
 				inst-=0xe00;
 			operandTable[inst][i][0]=1;
 		}
 		for (int j=0; j<inputTable1[i].length; j++)
 		{
 			int inst = inputTable1[i][j];
 			if (inst>=0xf00)
 				inst-=0xe00;
 			operandTable[inst][i][1]=1;
 		}
 		for (int j=0; j<outputTable0[i].length; j++)
 		{
 			int inst = outputTable0[i][j];
 			if (inst>=0xf00)
 				inst-=0xe00;
 			operandTable[inst][i][2]=1;
 		}
 		for (int j=0; j<outputTable1[i].length; j++)
 		{
 			int inst = outputTable1[i][j];
 			if (inst>=0xf00)
 				inst-=0xe00;
 			operandTable[inst][i][3]=1;
 		}
 	}
 }
 
 //this routine will eventually be turned into a normal exception
 public void panic(String reason)
 {
 	System.out.println("PANIC: "+reason);
 	System.out.println("icount = "+computer.icount);
 	//System.exit(0);
 }
 
 //call this to start the decoding
 public void decodeInstruction(boolean is32bit)
 {
 	codeLength=0;
 	icodeLength=0;
 	addressDecoded=false;
 
 	if(is32bit)
 	{
 		pushCode(MICROCODE.PREFIX_OPCODE_32BIT);
 		pushCode(MICROCODE.PREFIX_ADDRESS_32BIT);
 	}
 
 	decodePrefix(is32bit);
 	decodeOpcode();
 	if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 		replaceFlags1632();
 }
 
 //returns microinstruction codes
 public MICROCODE[] getCodes()
 {
 	return code;
 }
 
 //returns the length of the instruction in bytes
 public int getInstructionLength()
 {
 	return fetchQueue.instructionLength();
 }
 
 //add a new microcode to the sequence
 private void pushCode(MICROCODE code)
 {
 	this.code[codeLength++]=code;
 }
 
 private void pushCode(int code)
 {
 	this.icode[icodeLength++]=code;
 }
 
 public MICROCODE getCode()
 {
 	if(codesHandled>=codeLength)
 		panic("No more codes to read");
 	return this.code[codesHandled++];
 }
 
 private int getLastiCode()
 {
 	if (icodesHandled==0)
 		return this.icode[0];
 	return this.icode[icodesHandled-1];
 }
 
 private int getiCode()
 {
 	if(icodesHandled>=icodeLength)
 		panic("No more icodes to read");
 	return this.icode[icodesHandled++];
 }
 
 //is a particular microcode already in the array?
 private boolean isCode(MICROCODE code)
 {
 	for (int i=0; i<codeLength; i++)
 		if (this.code[i]==code)
 			return true;
 	return false;
 }
 
 private void removeCode(MICROCODE code)
 {
 	int i;
 	for (i=0; i<codeLength; i++)
 		if (this.code[i]==code)
 			break;
 	if (i==codeLength)
 		return;
 	for (int j=i; j<codeLength-1; j++)
 		this.code[j]=this.code[j+1];
 	codeLength--;
 }
 
 private void decodePrefix(boolean is32bit)
 {
 	int prefix;
 
 	//keep decoding prefices until no more
 	while(true)
 	{
 		prefix = (0xff & fetchQueue.readByte());
 
 		switch(prefix)
 		{
 			//Group 1, page 34
 			case 0xf0: pushCode(MICROCODE.PREFIX_LOCK); break;
 			case 0xf2: pushCode(MICROCODE.PREFIX_REPNE); break;
 			case 0xf3: pushCode(MICROCODE.PREFIX_REPE); break;
 			//Group 2, page 34
 			case 0x2e: pushCode(MICROCODE.PREFIX_CS); break;
 			case 0x36: pushCode(MICROCODE.PREFIX_SS); break;
 			case 0x3e: pushCode(MICROCODE.PREFIX_DS); break;
 			case 0x26: pushCode(MICROCODE.PREFIX_ES); break;
 			case 0x64: pushCode(MICROCODE.PREFIX_FS); break;
 			case 0x65: pushCode(MICROCODE.PREFIX_GS); break;
 			case 0x66:
 				if(!is32bit)
 					pushCode(MICROCODE.PREFIX_OPCODE_32BIT);
 				else
 					removeCode(MICROCODE.PREFIX_OPCODE_32BIT);
 				break;
 			case 0x67:
 				if(!is32bit)
 					pushCode(MICROCODE.PREFIX_ADDRESS_32BIT);
 				else
 					removeCode(MICROCODE.PREFIX_ADDRESS_32BIT);
 				break;
 			case 0xd8: case 0xd9: case 0xda: case 0xdb: case 0xdc: case 0xdd: case 0xde: case 0xdf:
 				return;
 //				panic("Floating point not implemented");
 			default:
 				//this isn't a prefix - it's the opcode
 				return;
 		}
 		//move beyond the prefix byte
 		fetchQueue.advance(1);
 	}
 }
 
 private void decodeOpcode()
 {
 	//the opcode is either one or two bytes
 	//(intel manual mentions three, but I'll ignore that for now)
 
 	int opcode;
 	int modrm;
 	boolean hasmodrm;
 	int hasimmediate, hasdisplacement;
 	int tableindex=0;
 	int displacement;
 	long immediate;
 	int sib;
 
 	opcode=0xff & fetchQueue.readByte();
 	fetchQueue.advance(1);
 
 	tableindex=opcode;
 
 	//0x0f means a second byte
 	if (opcode==0x0f)
 	{
 		opcode=(opcode<<8) | (0xff & fetchQueue.readByte());
 		fetchQueue.advance(1);
 		tableindex=(opcode&0xff)+0x100;
 	}
 	else if (opcode>=0xd8 && opcode<=0xdf)
 	{
 		//floating point: throw away next byte
 		fetchQueue.advance(1);
 	}
 	if(computer.debugMode)
 		System.out.printf("opcode %x\n",opcode);
 
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_OPCODE_32BIT)) processorGUICode.push(GUICODE.DECODE_PREFIX,"op32");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_ADDRESS_32BIT)) processorGUICode.push(GUICODE.DECODE_PREFIX,"addr32");
 	if (processorGUICode!=null && !isCode(MICROCODE.PREFIX_OPCODE_32BIT)) processorGUICode.push(GUICODE.DECODE_PREFIX,"op16");
 	if (processorGUICode!=null && !isCode(MICROCODE.PREFIX_ADDRESS_32BIT)) processorGUICode.push(GUICODE.DECODE_PREFIX,"addr16");
 	if (processorGUICode!=null && (isCode(MICROCODE.PREFIX_REPE)||isCode(MICROCODE.PREFIX_REPNE))) processorGUICode.push(GUICODE.DECODE_PREFIX,"rep");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_CS)) processorGUICode.push(GUICODE.DECODE_PREFIX,"cs");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_SS)) processorGUICode.push(GUICODE.DECODE_PREFIX,"ss");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_DS)) processorGUICode.push(GUICODE.DECODE_PREFIX,"ds");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_ES)) processorGUICode.push(GUICODE.DECODE_PREFIX,"es");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_FS)) processorGUICode.push(GUICODE.DECODE_PREFIX,"fs");
 	if (processorGUICode!=null && isCode(MICROCODE.PREFIX_GS)) processorGUICode.push(GUICODE.DECODE_PREFIX,"gs");
 
 	if(processorGUICode!=null) processorGUICode.push(GUICODE.DECODE_OPCODE,opcode);
 
 	hasmodrm=(modrmTable[tableindex]==1);
 	
 	//the next byte is the mod r/m byte, if the instruction has one
 	if (hasmodrm)
 	{
 		modrm = (0xff & fetchQueue.readByte());
 		fetchQueue.advance(1);
 
 		if(processorGUICode!=null) processorGUICode.push(GUICODE.DECODE_MODRM,modrm);
 	}
 	else
 		modrm=-1;
 
 	//the next byte might be the sib
 	sib=-1;
 	if (modrm!=-1 && isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 	{
 		if (sibTable[modrm]==1)
 		{
 			sib=(0xff&fetchQueue.readByte());
 			fetchQueue.advance(1);
 
 			if(processorGUICode!=null) processorGUICode.push(GUICODE.DECODE_SIB,sib);
 		}
 	}
 
 	//get the displacement, if any
 	hasdisplacement=0;
 	if(hasDisplacementTable[tableindex]==1)
 	{
 		if(!isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 		{
 			if ((modrm & 0xc0)==0 && (modrm & 0x7)==0x6)
 				hasdisplacement=2;
 			else if ((modrm & 0xc0)==0x40)
 				hasdisplacement=1;
 			else if ((modrm & 0xc0)==0x80)
 				hasdisplacement=2; 
 		}
 		else
 		{
 			if ((modrm & 0xc0)==0 && (modrm & 0x7)==0x5)
 				hasdisplacement=4;
 			else if ((modrm & 0xc0)==0 && (modrm & 0x7)==0x4 && (sib!=-1) && (sib & 0x7)==0x5)
 				hasdisplacement=4;
 			else if ((modrm & 0xc0)==0x40)
 				hasdisplacement=1;
 			else if ((modrm & 0xc0)==0x80)
 				hasdisplacement=4; 
 		}
 	}
 
 	displacement=0;
 
 	//handle the special a0-a3 case
 	if(hasDisplacementTable[tableindex]==4)
 	{
 		if(!isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 			hasdisplacement=2;
 		else
 			hasdisplacement=4;
 	}
 
 	if (hasdisplacement==1)
 	{
 		displacement=(0xff & fetchQueue.readByte());
 		fetchQueue.advance(1);
 	}
 	else if (hasdisplacement==2)
 	{
 		displacement=(0xff & fetchQueue.readByte());
 		fetchQueue.advance(1);
 		displacement=(displacement&0xff)|(0xff00 & (fetchQueue.readByte()<<8));
 		fetchQueue.advance(1);
 	}
 	else if (hasdisplacement==4)
 	{
 		displacement=(0xff & fetchQueue.readByte());
 		fetchQueue.advance(1);
 		displacement=(displacement&0xff)|(0xff00 & (fetchQueue.readByte()<<8));
 		fetchQueue.advance(1);
 		displacement=(displacement&0xffff)|(0xff0000 & (fetchQueue.readByte()<<16));
 		fetchQueue.advance(1);
 		displacement=(displacement&0xffffff)|(0xff000000 & (fetchQueue.readByte()<<24));
 		fetchQueue.advance(1);
 	}
 
 	if (hasdisplacement>0)
 		if(processorGUICode!=null) processorGUICode.push(GUICODE.DECODE_DISPLACEMENT,displacement);
 
 	//get the immediate
 	//get the displacement, if any
 	hasimmediate=hasImmediateTable[tableindex];
 
 	//since we're in 16-bit mode, change the 32-bit to 20-bit
 	if (!isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 	{
 		if(hasimmediate==4)
 			hasimmediate=2;
 		else if(hasimmediate==6)
 			hasimmediate=4;
 	}
 
 	//the una instructions (f6 and f7) may or may not have an immediate, depending on modrm
 	if(opcode==0xf6 && (modrm&0x38)==0)
 		hasimmediate=1;
 	else if(opcode==0xf7 && (modrm&0x38)==0 && !isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 			hasimmediate=2;
 	else if(opcode==0xf7 && (modrm&0x38)==0 && isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 			hasimmediate=4;
 
 	immediate=0;
 	if (hasimmediate>=1)
 	{
 		immediate=fetchQueue.readByte();
 		fetchQueue.advance(1);
 	}
 	if (hasimmediate>=2)
 	{
 		immediate = (immediate&0xff)|(0xff00&(fetchQueue.readByte()<<8));
 		fetchQueue.advance(1);
 	}
 	if (hasimmediate>=3)
 	{
 		immediate = (immediate&0xffff)|(0xff0000&(fetchQueue.readByte()<<16));
 		fetchQueue.advance(1);
 	}
 	if (hasimmediate>=4)
 	{
 		immediate = (immediate&0xffffff)|(0xff000000&(fetchQueue.readByte()<<24));
 		fetchQueue.advance(1);
 	}
 	if (hasimmediate>=6)
 	{
 		immediate = (immediate&0xffffffffl)|(((fetchQueue.readByte()&0xffl)|((fetchQueue.readByte(1)<<8)&0xff00l))<<32);
 		fetchQueue.advance(2);
 	}
 
 	if (hasimmediate>0)
 		if(processorGUICode!=null) processorGUICode.push(GUICODE.DECODE_IMMEDIATE,(int)immediate);
 
 	decodeOperands(opcode, modrm, sib, displacement, immediate, true);
 	decodeOperation(opcode, modrm);
 	decodeOperands(opcode, modrm, sib, displacement, immediate, false);
 	decodeFlags(opcode, modrm);
 }
 
 private void decodeOperands(int opcode, int modrm, int sib, int displacement, long immediate, boolean inputOperands)
 {
 	//30 different operand types
 	int types=30;
 	int opcodeLookup = opcode;
 	if (opcodeLookup>=0xf00)
 		opcodeLookup-=0xe00;
 	
 	//step through load 0, load 1, store 0, store 1
 	int start=inputOperands? 0:2;
 	int end=inputOperands? 2:4;
 	for (int operand=start; operand<end; operand++)
 	{
 		for (int type=0; type<types; type++)
 		{
 			if(operandTable[opcodeLookup][type][operand]==0)
 				continue;
 
 			if (type==0)
 				effective_byte(modrm,sib,displacement,operand);
 			else if (type==1)
 			{
 				if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					effective_double(modrm,sib,displacement,operand);
 				else
 					effective_word(modrm,sib,displacement,operand);
 			}
 			else if (type==2)
 				register_byte(modrm,operand);
 			else if (type==3)
 			{
 				if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					register_double(modrm,operand);
 				else
 					register_word(modrm,operand);
 			}
 			else if (type==4)
 			{
 				if (operand==0)
 					pushCode(MICROCODE.LOAD0_IB);
 				else if (operand==1)
 					pushCode(MICROCODE.LOAD1_IB);
 				pushCode((int)immediate);
 			}
 			else if (type==5)
 			{
 				if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					if (operand==0)
 						pushCode(MICROCODE.LOAD0_ID);
 					else if (operand==1)
 						pushCode(MICROCODE.LOAD1_ID);
 					pushCode((int)immediate);
 				}
 				else
 				{
 					if (operand==0)
 						pushCode(MICROCODE.LOAD0_IW);
 					else if (operand==1)
 						pushCode(MICROCODE.LOAD1_IW);
 					pushCode((int)immediate);
 				}
 			}
 			else if (type>=6 && type<=28)
 			{
 				MICROCODE c = operandRegisterTable[operand*23+type-6];
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					if (c==MICROCODE.LOAD0_AX) c=MICROCODE.LOAD0_EAX;
 					else if (c==MICROCODE.LOAD0_BX) c=MICROCODE.LOAD0_EBX;
 					else if (c==MICROCODE.LOAD0_CX) c=MICROCODE.LOAD0_ECX;
 					else if (c==MICROCODE.LOAD0_DX) c=MICROCODE.LOAD0_EDX;
 					else if (c==MICROCODE.LOAD0_SI) c=MICROCODE.LOAD0_ESI;
 					else if (c==MICROCODE.LOAD0_DI) c=MICROCODE.LOAD0_EDI;
 					else if (c==MICROCODE.LOAD0_SP) c=MICROCODE.LOAD0_ESP;
 					else if (c==MICROCODE.LOAD0_BP) c=MICROCODE.LOAD0_EBP;
 					else if (c==MICROCODE.LOAD0_FLAGS) c=MICROCODE.LOAD0_EFLAGS;
 					else if (c==MICROCODE.LOAD1_AX) c=MICROCODE.LOAD1_EAX;
 					else if (c==MICROCODE.LOAD1_BX) c=MICROCODE.LOAD1_EBX;
 					else if (c==MICROCODE.LOAD1_CX) c=MICROCODE.LOAD1_ECX;
 					else if (c==MICROCODE.LOAD1_DX) c=MICROCODE.LOAD1_EDX;
 					else if (c==MICROCODE.LOAD1_SI) c=MICROCODE.LOAD1_ESI;
 					else if (c==MICROCODE.LOAD1_DI) c=MICROCODE.LOAD1_EDI;
 					else if (c==MICROCODE.LOAD1_SP) c=MICROCODE.LOAD1_ESP;
 					else if (c==MICROCODE.LOAD1_BP) c=MICROCODE.LOAD1_EBP;
 					else if (c==MICROCODE.LOAD1_FLAGS) c=MICROCODE.LOAD1_EFLAGS;
 					else if (c==MICROCODE.STORE0_AX) c=MICROCODE.STORE0_EAX;
 					else if (c==MICROCODE.STORE0_BX) c=MICROCODE.STORE0_EBX;
 					else if (c==MICROCODE.STORE0_CX) c=MICROCODE.STORE0_ECX;
 					else if (c==MICROCODE.STORE0_DX) c=MICROCODE.STORE0_EDX;
 					else if (c==MICROCODE.STORE0_SI) c=MICROCODE.STORE0_ESI;
 					else if (c==MICROCODE.STORE0_DI) c=MICROCODE.STORE0_EDI;
 					else if (c==MICROCODE.STORE0_SP) c=MICROCODE.STORE0_ESP;
 					else if (c==MICROCODE.STORE0_BP) c=MICROCODE.STORE0_EBP;
 					else if (c==MICROCODE.STORE0_FLAGS) c=MICROCODE.STORE0_EFLAGS;
 					else if (c==MICROCODE.STORE1_AX) c=MICROCODE.STORE1_EAX;
 					else if (c==MICROCODE.STORE1_BX) c=MICROCODE.STORE1_EBX;
 					else if (c==MICROCODE.STORE1_CX) c=MICROCODE.STORE1_ECX;
 					else if (c==MICROCODE.STORE1_DX) c=MICROCODE.STORE1_EDX;
 					else if (c==MICROCODE.STORE1_SI) c=MICROCODE.STORE1_ESI;
 					else if (c==MICROCODE.STORE1_DI) c=MICROCODE.STORE1_EDI;
 					else if (c==MICROCODE.STORE1_SP) c=MICROCODE.STORE1_ESP;
 					else if (c==MICROCODE.STORE1_BP) c=MICROCODE.STORE1_EBP;
 					else if (c==MICROCODE.STORE1_FLAGS) c=MICROCODE.STORE1_EFLAGS;
 				}
 				pushCode(c);
 			}
 			else
 				decodeIrregularOperand(opcode, modrm, sib, displacement, immediate, operand);
 			break;
 		}
 	}
 
 	//a few instructions have a third input
 	if (inputOperands && (opcode==0xfa4 || opcode==0xfa5 || opcode==0xfac || opcode==0xfad || opcode==0xfb0 || opcode==0xfb1))
 	{
 		switch(opcode)
 		{
 		case 0xfa4: case 0xfac:
 			pushCode(MICROCODE.LOAD2_IB);
 			pushCode((int)immediate);
 			break;
 		case 0xfa5: case 0xfad:
 			pushCode(MICROCODE.LOAD2_CL);
 			break;
 		case 0xfb0:
 			pushCode(MICROCODE.LOAD2_AL);
 			break;
 		case 0xfb1:
 			if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				pushCode(MICROCODE.LOAD2_EAX);
 			else
 				pushCode(MICROCODE.LOAD2_AX);
 			break;
 		}
 	}
 }
 
 private void effective_byte(int modrm, int sib, int displacement, int operand)
 {
 	if ((modrm & 0xc7)>=0xc0 && (modrm & 0xc7)<=0xc7)
 		pushCode(modrmRegisterTable[operand*9 + (modrm&7)]);
 	else
 	{
 		decode_memory(modrm, sib, displacement);
 		pushCode(modrmRegisterTable[operand*9+8]);
 	}
 }
 
 private void effective_word(int modrm, int sib, int displacement, int operand)
 {
 	if ((modrm & 0xc7)>=0xc0 && (modrm & 0xc7)<=0xc7)
 		pushCode(modrmRegisterTable[36 + operand*9 + (modrm&7)]);
 	else
 	{
 		decode_memory(modrm, sib, displacement);
 		pushCode(modrmRegisterTable[36 + operand*9+8]);
 	}
 }
 
 private void effective_double(int modrm, int sib, int displacement, int operand)
 {
 	if ((modrm & 0xc7)>=0xc0 && (modrm & 0xc7)<=0xc7)
 		pushCode(modrmRegisterTable[72 + operand*9 + (modrm&7)]);
 	else
 	{
 		decode_memory(modrm, sib, displacement);
 		pushCode(modrmRegisterTable[72 + operand*9+8]);
 	}
 }
 
 private void register_byte(int modrm, int operand)
 {
 	pushCode(modrmRegisterTable[operand*9+((modrm&0x38)>>3)]);
 }
 
 private void register_word(int modrm, int operand)
 {
 	pushCode(modrmRegisterTable[36+operand*9+((modrm&0x38)>>3)]);
 }
 
 private void register_double(int modrm, int operand)
 {
 	pushCode(modrmRegisterTable[72+operand*9+((modrm&0x38)>>3)]);
 }
 
 private boolean isAddressDecoded()
 {
 	if(addressDecoded)
 		return true;
 	addressDecoded=true;
 	return false;
 }
 
 private void store0_Rd(int modrm)
 {
 	switch(modrm&0xc7)
 	{
 		case 0xc0: pushCode(MICROCODE.STORE0_EAX); break;
 		case 0xc1: pushCode(MICROCODE.STORE0_ECX); break;
 		case 0xc2: pushCode(MICROCODE.STORE0_EDX); break;
 		case 0xc3: pushCode(MICROCODE.STORE0_EBX); break;
 		case 0xc4: pushCode(MICROCODE.STORE0_ESP); break;
 		case 0xc5: pushCode(MICROCODE.STORE0_EBP); break;
 		case 0xc6: pushCode(MICROCODE.STORE0_ESI); break;
 		case 0xc7: pushCode(MICROCODE.STORE0_EDI); break;
 		default: panic("Bad store0 Rd");
 	}
 }
 
 private void load0_Rd(int modrm)
 {
 	switch(modrm&0xc7)
 	{
 		case 0xc0: pushCode(MICROCODE.LOAD0_EAX); break;
 		case 0xc1: pushCode(MICROCODE.LOAD0_ECX); break;
 		case 0xc2: pushCode(MICROCODE.LOAD0_EDX); break;
 		case 0xc3: pushCode(MICROCODE.LOAD0_EBX); break;
 		case 0xc4: pushCode(MICROCODE.LOAD0_ESP); break;
 		case 0xc5: pushCode(MICROCODE.LOAD0_EBP); break;
 		case 0xc6: pushCode(MICROCODE.LOAD0_ESI); break;
 		case 0xc7: pushCode(MICROCODE.LOAD0_EDI); break;
 		default: panic("Bad store0 Rd");
 	}
 }
 
 private void load0_Cd(int modrm)
 {
 	switch(modrm&0x38)
 	{
 		case 0x00: pushCode(MICROCODE.LOAD0_CR0); break;
 		case 0x10: pushCode(MICROCODE.LOAD0_CR2); break;
 		case 0x18: pushCode(MICROCODE.LOAD0_CR3); break;
 		case 0x20: pushCode(MICROCODE.LOAD0_CR4); break;
 		default: panic("Bad load0 Cd");
 	}
 }
 
 private void store0_Cd(int modrm)
 {
 	switch(modrm&0x38)
 	{
 		case 0x00: pushCode(MICROCODE.STORE0_CR0); break;
 		case 0x10: pushCode(MICROCODE.STORE0_CR2); break;
 		case 0x18: pushCode(MICROCODE.STORE0_CR3); break;
 		case 0x20: pushCode(MICROCODE.STORE0_CR4); break;
 		default: panic("Bad load0 Cd");
 	}
 }
 
 private void decode_memory(int modrm, int sib, int displacement)
 {
 	if(isAddressDecoded()) return;
 
 	if (isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 	{
 		//first figure out which segment to access
 		if (isCode(MICROCODE.PREFIX_CS))
 			pushCode(MICROCODE.LOAD_SEG_CS);
 		else if (isCode(MICROCODE.PREFIX_SS))
 			pushCode(MICROCODE.LOAD_SEG_SS);
 		else if (isCode(MICROCODE.PREFIX_DS))
 			pushCode(MICROCODE.LOAD_SEG_DS);
 		else if (isCode(MICROCODE.PREFIX_ES))
 			pushCode(MICROCODE.LOAD_SEG_ES);
 		else if (isCode(MICROCODE.PREFIX_FS))
 			pushCode(MICROCODE.LOAD_SEG_FS);
 		else if (isCode(MICROCODE.PREFIX_GS))
 			pushCode(MICROCODE.LOAD_SEG_GS);
 		else if ((modrm&0xc7)==0x45 || (modrm&0xc7)==0x55)
 			pushCode(MICROCODE.LOAD_SEG_SS);
 		else if ((modrm&0xc7)==0x04 || (modrm&0xc7)==0x44 || (modrm&0xc7)==0x84) { }
 		else
 			pushCode(MICROCODE.LOAD_SEG_DS);
 
 		if ((modrm & 0x7)==0)
 			pushCode(MICROCODE.ADDR_EAX);
 		else if ((modrm & 0x7)==1)
 			pushCode(MICROCODE.ADDR_ECX);
 		else if ((modrm & 0x7)==2)
 			pushCode(MICROCODE.ADDR_EDX);
 		else if ((modrm & 0x7)==3)
 			pushCode(MICROCODE.ADDR_EBX);
 		else if ((modrm & 0x7)==4)
 		{
 			decodeSIB(modrm, sib, displacement);
 		}
 		else if ((modrm & 0x7)==5 && (modrm & 0xc0)==0x00)
 		{
 			pushCode(MICROCODE.ADDR_ID);
 			pushCode(displacement);
 		}
 		else if ((modrm & 0x7)==5)
 			pushCode(MICROCODE.ADDR_EBP);
 		else if ((modrm & 0x7)==6)
 			pushCode(MICROCODE.ADDR_ESI);
 		else if ((modrm & 0x7)==7)
 			pushCode(MICROCODE.ADDR_EDI);
 
 		//now add the displacement
 		if ((modrm & 0xc0)==0x40)
 		{
 			pushCode(MICROCODE.ADDR_IB);
 			pushCode(displacement);
 		}
 		else if ((modrm & 0xc0)==0x80)
 		{
 			pushCode(MICROCODE.ADDR_ID);
 			pushCode(displacement);
 		}
 	}
 	else
 	{
 		//first figure out which segment to access
 		if (isCode(MICROCODE.PREFIX_CS))
 			pushCode(MICROCODE.LOAD_SEG_CS);
 		else if (isCode(MICROCODE.PREFIX_SS))
 			pushCode(MICROCODE.LOAD_SEG_SS);
 		else if (isCode(MICROCODE.PREFIX_DS))
 			pushCode(MICROCODE.LOAD_SEG_DS);
 		else if (isCode(MICROCODE.PREFIX_ES))
 			pushCode(MICROCODE.LOAD_SEG_ES);
 		else if (isCode(MICROCODE.PREFIX_FS))
 			pushCode(MICROCODE.LOAD_SEG_FS);
 		else if (isCode(MICROCODE.PREFIX_GS))
 			pushCode(MICROCODE.LOAD_SEG_GS);
 		else if ((modrm&0xc7)==0x02 || (modrm&0xc7)==0x03 || (modrm&0xc7)==0x42 || (modrm&0xc7)==0x43 || (modrm&0xc7)==0x46 || (modrm&0xc7)==0x82 || (modrm&0xc7)==0x83 || (modrm&0xc7)==0x86)
 			pushCode(MICROCODE.LOAD_SEG_SS);
 		else
 			pushCode(MICROCODE.LOAD_SEG_DS);
 
 		if ((modrm & 0x7)==0 || (modrm & 0x7)==1 || (modrm & 0x7)==7)
 			pushCode(MICROCODE.ADDR_BX);
 		else if ((modrm & 0x7)==2 || (modrm & 0x7)==3)
 			pushCode(MICROCODE.ADDR_BP);
 		else if ((modrm & 0x7)==4)
 			pushCode(MICROCODE.ADDR_SI);
 		else if ((modrm & 0x7)==5)
 			pushCode(MICROCODE.ADDR_DI);
 		else if ((modrm & 0xc0)!=0)
 			pushCode(MICROCODE.ADDR_BP);
 		else
 		{
 			pushCode(MICROCODE.ADDR_IW);
 			pushCode(displacement);
 		}
 		//in some cases SI or DI is added
 		if ((modrm & 0x7)==0 || (modrm & 0x7)==2)
 			pushCode(MICROCODE.ADDR_SI);
 		else if ((modrm & 0x7)==1 || (modrm & 0x7)==3)
 			pushCode(MICROCODE.ADDR_DI);
 
 		//now add the displacement
 		if ((modrm & 0xc0)==0x40)
 		{
 			pushCode(MICROCODE.ADDR_IB);
 			pushCode(displacement);
 		}
 		else if ((modrm & 0xc0)==0x80)
 		{
 			pushCode(MICROCODE.ADDR_IW);
 			pushCode(displacement);
 		}
 		pushCode(MICROCODE.ADDR_MASK_16);
 	}
 }
 
 private void decodeSIB(int modrm, int sib, int displacement)
 {
 	if (isCode(MICROCODE.PREFIX_CS)) pushCode(MICROCODE.LOAD_SEG_CS);
 	else if (isCode(MICROCODE.PREFIX_SS)) pushCode(MICROCODE.LOAD_SEG_SS);
 	else if (isCode(MICROCODE.PREFIX_DS)) pushCode(MICROCODE.LOAD_SEG_DS);
 	else if (isCode(MICROCODE.PREFIX_ES)) pushCode(MICROCODE.LOAD_SEG_ES);
 	else if (isCode(MICROCODE.PREFIX_FS)) pushCode(MICROCODE.LOAD_SEG_FS);
 	else if (isCode(MICROCODE.PREFIX_GS)) pushCode(MICROCODE.LOAD_SEG_GS);
 	else
 	{
 		if ((sib&0x7)==0x4)
 			pushCode(MICROCODE.LOAD_SEG_SS);
 		else if ((sib&0x7)==0x5)
 		{
 			if ((modrm&0xc0)==0)
 				pushCode(MICROCODE.LOAD_SEG_SS);
 			else
 				pushCode(MICROCODE.LOAD_SEG_DS);
 		}
 		else
 			pushCode(MICROCODE.LOAD_SEG_DS);
 	}
 	if ((sib&0x7)==0) pushCode(MICROCODE.ADDR_EAX);
 	else if ((sib&0x7)==1) pushCode(MICROCODE.ADDR_ECX);
 	else if ((sib&0x7)==2) pushCode(MICROCODE.ADDR_EDX);
 	else if ((sib&0x7)==3) pushCode(MICROCODE.ADDR_EBX);
 	else if ((sib&0x7)==4) pushCode(MICROCODE.ADDR_ESP);
 	else if ((sib&0x7)==6) pushCode(MICROCODE.ADDR_ESI);
 	else if ((sib&0x7)==7) pushCode(MICROCODE.ADDR_EDI);
 	else if ((modrm&0xc0)!=0) pushCode(MICROCODE.ADDR_EBP);
 	else
 	{
 		pushCode(MICROCODE.ADDR_ID);
 		pushCode(displacement);
 	}
 
 	switch(sib&0xf8)
 	{
 		case 0x00:	pushCode(MICROCODE.ADDR_EAX); break;
 		case 0x08:	pushCode(MICROCODE.ADDR_ECX); break;
 		case 0x10:	pushCode(MICROCODE.ADDR_EDX); break;
 		case 0x18:	pushCode(MICROCODE.ADDR_EBX); break;
 		case 0x28:	pushCode(MICROCODE.ADDR_EBP); break;
 		case 0x30:	pushCode(MICROCODE.ADDR_ESI); break;
 		case 0x38:	pushCode(MICROCODE.ADDR_EDI); break;
 		case 0x40:	pushCode(MICROCODE.ADDR_2EAX); break;
 		case 0x48:	pushCode(MICROCODE.ADDR_2ECX); break;
 		case 0x50:	pushCode(MICROCODE.ADDR_2EDX); break;
 		case 0x58:	pushCode(MICROCODE.ADDR_2EBX); break;
 		case 0x68:	pushCode(MICROCODE.ADDR_2EBP); break;
 		case 0x70:	pushCode(MICROCODE.ADDR_2ESI); break;
 		case 0x78:	pushCode(MICROCODE.ADDR_2EDI); break;
 		case 0x80:	pushCode(MICROCODE.ADDR_4EAX); break;
 		case 0x88:	pushCode(MICROCODE.ADDR_4ECX); break;
 		case 0x90:	pushCode(MICROCODE.ADDR_4EDX); break;
 		case 0x98:	pushCode(MICROCODE.ADDR_4EBX); break;
 		case 0xa8:	pushCode(MICROCODE.ADDR_4EBP); break;
 		case 0xb0:	pushCode(MICROCODE.ADDR_4ESI); break;
 		case 0xb8:	pushCode(MICROCODE.ADDR_4EDI); break;
 		case 0xc0:	pushCode(MICROCODE.ADDR_8EAX); break;
 		case 0xc8:	pushCode(MICROCODE.ADDR_8ECX); break;
 		case 0xd0:	pushCode(MICROCODE.ADDR_8EDX); break;
 		case 0xd8:	pushCode(MICROCODE.ADDR_8EBX); break;
 		case 0xe8:	pushCode(MICROCODE.ADDR_8EBP); break;
 		case 0xf0:	pushCode(MICROCODE.ADDR_8ESI); break;
 		case 0xf8:	pushCode(MICROCODE.ADDR_8EDI); break;
 	}
 }
 
 private void decodeSegmentPrefix()
 {
 	if (isCode(MICROCODE.PREFIX_CS))
 		pushCode(MICROCODE.LOAD_SEG_CS);
 	else if (isCode(MICROCODE.PREFIX_SS))
 		pushCode(MICROCODE.LOAD_SEG_SS);
 	else if (isCode(MICROCODE.PREFIX_DS))
 		pushCode(MICROCODE.LOAD_SEG_DS);
 	else if (isCode(MICROCODE.PREFIX_ES))
 		pushCode(MICROCODE.LOAD_SEG_ES);
 	else if (isCode(MICROCODE.PREFIX_FS))
 		pushCode(MICROCODE.LOAD_SEG_FS);
 	else if (isCode(MICROCODE.PREFIX_GS))
 		pushCode(MICROCODE.LOAD_SEG_GS);
 	else
 		pushCode(MICROCODE.LOAD_SEG_DS);
 }
 
 private void decodeO(int modrm, int displacement)
 {
 	//first figure out which segment to access
 	if (isCode(MICROCODE.PREFIX_CS))
 		pushCode(MICROCODE.LOAD_SEG_CS);
 	else if (isCode(MICROCODE.PREFIX_SS))
 		pushCode(MICROCODE.LOAD_SEG_SS);
 	else if (isCode(MICROCODE.PREFIX_DS))
 		pushCode(MICROCODE.LOAD_SEG_DS);
 	else if (isCode(MICROCODE.PREFIX_ES))
 		pushCode(MICROCODE.LOAD_SEG_ES);
 	else if (isCode(MICROCODE.PREFIX_FS))
 		pushCode(MICROCODE.LOAD_SEG_FS);
 	else if (isCode(MICROCODE.PREFIX_GS))
 		pushCode(MICROCODE.LOAD_SEG_GS);
 	else if ((modrm&0xc7)==0x02 || (modrm&0xc7)==0x03 || (modrm&0xc7)==0x42 || (modrm&0xc7)==0x43 || (modrm&0xc7)==0x46 || (modrm&0xc7)==0x82 || (modrm&0xc7)==0x83 || (modrm&0xc7)==0x86)
 		pushCode(MICROCODE.LOAD_SEG_SS);
 	else
 		pushCode(MICROCODE.LOAD_SEG_DS);
 
 	if(isAddressDecoded()) return;
 
 	if (isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 		pushCode(MICROCODE.ADDR_ID);
 	else
 	{
 		pushCode(MICROCODE.ADDR_IW);
 		pushCode(MICROCODE.ADDR_MASK_16);
 	}
 	pushCode(displacement);
 }
 
 private void decodeIrregularOperand(int opcode, int modrm, int sib, int displacement, long immediate, int operand)
 {
 //input 0
 /*{ 0xc8, 0x62, 0xa0, 0xa1, 0xa4, 0xa5, 0xa6, 0xa7, 0xac, 0xad, 0xfa3, 0xfab, 0xfb3, 0xfbb, 0x6e, 0x6f, 0xd7, 0xf00, 0xf01, 0xf20, 0xf21, 0xf23, 0xfba, 0xfc8, 0xfc9, 0xfca, 0xfcb, 0xfcc, 0xfcd, 0xfce, 0xfcf}*/
 
 	if (operand==0)
 	{
 		switch(opcode)
 		{
 			case 0x8e: case 0xfb7: case 0xfbf:
 				effective_word(modrm, sib, displacement, operand);
 				break;
 			case 0xec: case 0xed: case 0xee: case 0xef: case 0x6c: case 0x6d:
 				pushCode(MICROCODE.LOAD0_DX);
 				break;
 			case 0xc2: case 0xca:
 				pushCode(MICROCODE.LOAD0_IW);
 				pushCode((int)(immediate));
 				break;
 			case 0xea: case 0x9a:	//far jump or call
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					pushCode(MICROCODE.LOAD0_ID);
 					pushCode((int)immediate);
 				}
 				else
 				{
 					pushCode(MICROCODE.LOAD0_IW);
 					pushCode((int)(0xffff & immediate));
 				}
 				break;
 			case 0x8d:	//lea
 				decode_memory(modrm, sib, displacement);
 				pushCode(MICROCODE.LOAD0_ADDR);
 				break;
 			case 0x8c:	//store to segment register
 			switch(modrm&0x38)
 			{
 				case 0x00: pushCode(MICROCODE.LOAD0_ES); break;
 				case 0x08: pushCode(MICROCODE.LOAD0_CS); break;
 				case 0x10: pushCode(MICROCODE.LOAD0_SS); break;
 				case 0x18: pushCode(MICROCODE.LOAD0_DS); break;
 				case 0x20: pushCode(MICROCODE.LOAD0_FS); break;
 				case 0x28: pushCode(MICROCODE.LOAD0_GS); break;
 //				default: panic("Bad segment operand");
 			} break;
 			case 0xa0:
 				decodeO(modrm, displacement);
 				pushCode(MICROCODE.LOAD0_MEM_BYTE);
 				break;
 			case 0xa1:
 				decodeO(modrm, displacement);
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					pushCode(MICROCODE.LOAD0_MEM_DOUBLE);
 				else
 					pushCode(MICROCODE.LOAD0_MEM_WORD);
 				break;
 			case 0xa4: case 0xa5: case 0xa6: case 0xa7: case 0xac: case 0xad:
 				decodeSegmentPrefix();
 				break;
 			case 0x6e: case 0x6f:
 				pushCode(MICROCODE.LOAD0_DX);
 				decodeSegmentPrefix();
 				break;
 			case 0xc8:
 				pushCode(MICROCODE.LOAD0_IW);
 //				pushCode((int)(0xffffl & (immediate>>>16)));
 				pushCode((int)(0xffffl & immediate));
 				break;
 			case 0xd7:
 				if(isCode(MICROCODE.PREFIX_CS))
 					pushCode(MICROCODE.LOAD_SEG_CS);
 				else if(isCode(MICROCODE.PREFIX_SS))
 					pushCode(MICROCODE.LOAD_SEG_SS);
 				else if(isCode(MICROCODE.PREFIX_ES))
 					pushCode(MICROCODE.LOAD_SEG_ES);
 				else if(isCode(MICROCODE.PREFIX_FS))
 					pushCode(MICROCODE.LOAD_SEG_FS);
 				else if(isCode(MICROCODE.PREFIX_GS))
 					pushCode(MICROCODE.LOAD_SEG_GS);
 				else
 					pushCode(MICROCODE.LOAD_SEG_DS);
 				if (isCode(MICROCODE.PREFIX_ADDRESS_32BIT))
 				{
 					pushCode(MICROCODE.ADDR_EBX);
 					pushCode(MICROCODE.ADDR_AL);
 				}	
 				else
 				{
 					pushCode(MICROCODE.ADDR_BX);
 					pushCode(MICROCODE.ADDR_AL);
 					pushCode(MICROCODE.ADDR_MASK_16);
 				}
 				pushCode(MICROCODE.LOAD0_MEM_BYTE);
 				break;
 			case 0xf00:
 				if ((modrm&0x38)==0x10 || (modrm&0x38)==0x18 || (modrm&0x38)==0x20 || (modrm&0x38)==0x28)
 					effective_word(modrm,sib,displacement,operand);
 				break;
 			case 0xf01:
 				if ((modrm&0x38)==0x10 || (modrm&0x38)==0x18 || (modrm&0x38)==0x30)
 					effective_word(modrm,sib,displacement,operand);
 				else if ((modrm&0x38)==0x38)
 					decode_memory(modrm,sib,displacement);
 				break;
 			case 0xf20:
 				load0_Cd(modrm); break;
 			case 0xf22:
 				load0_Rd(modrm); break;
 			case 0xfab: case 0xfa3: case 0xfbb: case 0xfb3:
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					switch(modrm&0xc7)
 					{
 					case 0xc0: pushCode(MICROCODE.LOAD0_EAX); break;
 					case 0xc1: pushCode(MICROCODE.LOAD0_ECX); break;
 					case 0xc2: pushCode(MICROCODE.LOAD0_EDX); break;
 					case 0xc3: pushCode(MICROCODE.LOAD0_EBX); break;
 					case 0xc4: pushCode(MICROCODE.LOAD0_ESP); break;
 					case 0xc5: pushCode(MICROCODE.LOAD0_EBP); break;
 					case 0xc6: pushCode(MICROCODE.LOAD0_ESI); break;
 					case 0xc7: pushCode(MICROCODE.LOAD0_EDI); break;
 					default: decode_memory(modrm, sib, displacement); break;
 					}
 				}
 				else
 				{
 					switch(modrm&0xc7)
 					{
 					case 0xc0: pushCode(MICROCODE.LOAD0_AX); break;
 					case 0xc1: pushCode(MICROCODE.LOAD0_CX); break;
 					case 0xc2: pushCode(MICROCODE.LOAD0_DX); break;
 					case 0xc3: pushCode(MICROCODE.LOAD0_BX); break;
 					case 0xc4: pushCode(MICROCODE.LOAD0_SP); break;
 					case 0xc5: pushCode(MICROCODE.LOAD0_BP); break;
 					case 0xc6: pushCode(MICROCODE.LOAD0_SI); break;
 					case 0xc7: pushCode(MICROCODE.LOAD0_DI); break;
 					default: decode_memory(modrm, sib, displacement); break;
 					}					
 				}
 				break;
 			default:
 				panic("Need to decode irregular input 0 operand: "+opcode);
 		}
 	}
 //input 1
 /*{0xc8, 0xf6, 0xf7, 0x62, 0xc4, 0xc5, 0xfb2, 0xfb4, 0xfb5, 0xfba}*/
 	else if (operand==1)
 	{
 		switch(opcode)
 		{
 			case 0xc8:	//enter
 				pushCode(MICROCODE.LOAD1_IB);
 //				pushCode((int)(0xffl & immediate));
 				pushCode((int)(0xffl & (immediate>>>24)));
 				break;
 			case 0xea: case 0x9a:	//far jump or call
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					pushCode(MICROCODE.LOAD1_IW);
 					pushCode((int)(immediate>>>32));
 				}
 				else
 				{
 					pushCode(MICROCODE.LOAD1_IW);
 					pushCode((int)(immediate>>>16));
 				}
 				break;
 			case 0xf6:	//una
 				if((modrm&0x38)==0)
 				{
 					pushCode(MICROCODE.LOAD1_IB);
 					pushCode((int)immediate);
 				} break;
 			case 0xf7:	//una
 				if(isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					if((modrm&0x38)==0)
 					{
 						pushCode(MICROCODE.LOAD1_ID);
 						pushCode((int)immediate);
 					}
 				}
 				else
 				{
 					if((modrm&0x38)==0)
 					{
 						pushCode(MICROCODE.LOAD1_IW);
 						pushCode((int)immediate);
 					}
 				} 
 				break;
 			case 0xff:	//various jumps
 				if((modrm&0x38)==0x18 || (modrm&0x38)==0x28)
 				{
 					pushCode(MICROCODE.ADDR_IB);
 					if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 						pushCode(4);
 					else
 						pushCode(2);
 					pushCode(MICROCODE.LOAD1_MEM_WORD);
 				} break;
 			case 0xd0: case 0xd1:
 				pushCode(MICROCODE.LOAD1_IB);
 				pushCode(1);
 				break;
 			case 0xc4: case 0xc5: case 0xfb2: case 0xfb4: case 0xfb5:
 				pushCode(MICROCODE.ADDR_IB);
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					pushCode(4);
 				else
 					pushCode(2);
 				pushCode(MICROCODE.LOAD1_MEM_WORD);
 				break;
 			case 0xf01:
 				if ((modrm&0x38)==0x10 || (modrm&0x38)==0x18)
 				{
 					pushCode(MICROCODE.ADDR_ID);
 					pushCode(2);
 					pushCode(MICROCODE.LOAD1_MEM_DOUBLE);
 				}	
 				break;
 			case 0xfab: case 0xfa3: case 0xfbb: case 0xfb3:
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 				{
 					switch(modrm&0x38)
 					{
 					case 0x00: pushCode(MICROCODE.LOAD1_EAX); break;
 					case 0x08: pushCode(MICROCODE.LOAD1_ECX); break;
 					case 0x10: pushCode(MICROCODE.LOAD1_EDX); break;
 					case 0x18: pushCode(MICROCODE.LOAD1_EBX); break;
 					case 0x20: pushCode(MICROCODE.LOAD1_ESP); break;
 					case 0x28: pushCode(MICROCODE.LOAD1_EBP); break;
 					case 0x30: pushCode(MICROCODE.LOAD1_ESI); break;
 					case 0x38: pushCode(MICROCODE.LOAD1_EDI); break;
 					}
 				}
 				else
 				{
 					switch(modrm&0x38)
 					{
 					case 0x00: pushCode(MICROCODE.LOAD1_AX); break;
 					case 0x08: pushCode(MICROCODE.LOAD1_CX); break;
 					case 0x10: pushCode(MICROCODE.LOAD1_DX); break;
 					case 0x18: pushCode(MICROCODE.LOAD1_BX); break;
 					case 0x20: pushCode(MICROCODE.LOAD1_SP); break;
 					case 0x28: pushCode(MICROCODE.LOAD1_BP); break;
 					case 0x30: pushCode(MICROCODE.LOAD1_SI); break;
 					case 0x38: pushCode(MICROCODE.LOAD1_DI); break;
 					}					
 				}
 				break;
 			default:
 				panic("Need to decode irregular input 1 operand: "+opcode);
 		}
 	}
 //output 0
 /*{0x8e, 0xa2, 0xd0, 0xd1, 0xf6, 0xf7, 0xf00, 0xf01, 0xf20, 0xf21, 0xf22, 0xf23, 0xf31, 0xf32, 0xfab, 0xfb3, 0xfbb, 0xfba, 0xfc8, 0xfc9, 0xfca, 0xfcb, 0xfcc, 0xfcd, 0xfce, 0xfcf}*/
 	else if (operand==2)
 	{
 		switch(opcode)
 		{
 			case 0x8e:	//store to segment register
 			switch(modrm&0x38)
 			{
 				case 0x00: pushCode(MICROCODE.STORE0_ES); break;
 				case 0x08: pushCode(MICROCODE.STORE0_CS); break;
 				case 0x10: pushCode(MICROCODE.STORE0_SS); break;
 				case 0x18: pushCode(MICROCODE.STORE0_DS); break;
 				case 0x20: pushCode(MICROCODE.STORE0_FS); break;
 				case 0x28: pushCode(MICROCODE.STORE0_GS); break;
 				default: panic("Bad segment operand");
 			} break;
 			case 0xff:
 			if((modrm&0x38)==0x00 || (modrm&0x38)==0x08)
 			{
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					effective_double(modrm,sib,displacement,2);
 				else
 					effective_word(modrm,sib,displacement,2);
 			} break;
 			case 0xf6:
 			if((modrm&0x38)==0x10 || (modrm&0x38)==0x18)
 			{
 				effective_byte(modrm,sib,displacement,2);
 			} break;
 			case 0xf7:
 			if((modrm&0x38)==0x10 || (modrm&0x38)==0x18)
 			{
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					effective_double(modrm,sib,displacement,2);
 				else
 					effective_word(modrm,sib,displacement,2);
 			} break;
 			case 0xa2:
 				decodeO(modrm, displacement);
 				pushCode(MICROCODE.STORE0_MEM_BYTE);
 				break;
 			case 0xa3:
 				decodeO(modrm, displacement);
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					pushCode(MICROCODE.STORE0_MEM_DOUBLE);
 				else
 					pushCode(MICROCODE.STORE0_MEM_WORD);
 				break;
 			case 0x80: case 0x82:
 			if((modrm&0x38)!=0x38)
 				effective_byte(modrm,sib,displacement,2);
 			break;
 			case 0x81: case 0x83:
 			if((modrm&0x38)!=0x38)
 			{
 				if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					effective_double(modrm,sib,displacement,2);
 				else
 					effective_word(modrm,sib,displacement,2);
 			}
 			break;
 			case 0xfab: case 0xfb3: case 0xfbb:
				if((modrm&0xc0)!=0xc0)
 				{
 					if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 						effective_double(modrm,sib,displacement,2);
 					else
 						effective_word(modrm,sib,displacement,2);
 				}
 				break;
 			case 0xf00:
 				if ((modrm&0x38)==0)
 					effective_word(modrm,sib,displacement,2);
 				else if ((modrm&0x38)==0x8)
 				{
 					if (isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 					{
 						switch(modrm&0xc7)
 						{
 							case 0xc0: pushCode(MICROCODE.STORE0_EAX); break;
 							case 0xc1: pushCode(MICROCODE.STORE0_ECX); break;
 							case 0xc2: pushCode(MICROCODE.STORE0_EDX); break;
 							case 0xc3: pushCode(MICROCODE.STORE0_EBX); break;
 							case 0xc4: pushCode(MICROCODE.STORE0_ESP); break;
 							case 0xc5: pushCode(MICROCODE.STORE0_EBP); break;
 							case 0xc6: pushCode(MICROCODE.STORE0_ESI); break;
 							case 0xc7: pushCode(MICROCODE.STORE0_EDI); break;
 							default: decode_memory(modrm,sib,displacement); pushCode(MICROCODE.STORE0_MEM_WORD); break;
 						}
 					}
 					else
 						effective_word(modrm,sib,displacement,2);
 				}
 				break;
 			case 0xf01:
 				if ((modrm&0x38)==0x00 || (modrm&0x38)==0x08 || (modrm&0x38)==0x20)
 					effective_word(modrm,sib,displacement,2);
 				break;
 			case 0xf20:
 				store0_Rd(modrm); break;
 			case 0xf22:
 				store0_Cd(modrm); break;
 			case 0xf31:
 				pushCode(MICROCODE.STORE0_EAX); break;				
 			default:
 				panic("Need to decode irregular output 0 operand: "+opcode);
 		}
 
 	}
 //output 1
 /*{0xf01, 0xf31, 0xf32}*/
 	else
 	{
 		switch(opcode)
 		{
 			case 0xf01:
 				if ((modrm&0x38)==0x0 || (modrm&0x38)==0x08)
 				{
 					pushCode(MICROCODE.ADDR_ID);
 					pushCode(2);
 					pushCode(MICROCODE.STORE1_MEM_DOUBLE);
 				}	
 				break;
 			case 0xf31:
 				pushCode(MICROCODE.STORE0_EDX); break;
 			default:
 				panic("Need to decode irregular output 1 operand: "+opcode);
 		}
 
 	}
 
 }
 
 private void decodeFlags(int opcode, int modrm)
 {
 	int tableindex=opcode;
 	if(tableindex>=0x0f00)
 		tableindex-=0xe00;
 	MICROCODE code=flagTable[tableindex];
 	if (code==MICROCODE.FLAG_ROTATE_08)
 	{
 		int val=((modrm & 0x38)>>3);
 		code=rotationTable[val];
 		pushCode(code);
 	}
 	else if (code==MICROCODE.FLAG_ROTATE_16)
 	{
 		int val=((modrm & 0x38)>>3);
 		code=rotationTable[8+val];
 		pushCode(code);
 	}
 	else if (code==MICROCODE.FLAG_80_82)
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00: pushCode(MICROCODE.FLAG_ADD_08); break;
 			case 0x08: pushCode(MICROCODE.FLAG_BITWISE_08); break;
 			case 0x10: pushCode(MICROCODE.FLAG_ADC_08); break;
 			case 0x18: pushCode(MICROCODE.FLAG_SBB_08); break;
 			case 0x20: pushCode(MICROCODE.FLAG_BITWISE_08); break;
 			case 0x28: pushCode(MICROCODE.FLAG_SUB_08); break;
 			case 0x30: pushCode(MICROCODE.FLAG_BITWISE_08); break;
 			case 0x38: pushCode(MICROCODE.FLAG_SUB_08); break;
 		}
 	}
 	else if (code==MICROCODE.FLAG_81_83)
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00: pushCode(MICROCODE.FLAG_ADD_16); break;
 			case 0x08: pushCode(MICROCODE.FLAG_BITWISE_16); break;
 			case 0x10: pushCode(MICROCODE.FLAG_ADC_16); break;
 			case 0x18: pushCode(MICROCODE.FLAG_SBB_16); break;
 			case 0x20: pushCode(MICROCODE.FLAG_BITWISE_16); break;
 			case 0x28: pushCode(MICROCODE.FLAG_SUB_16); break;
 			case 0x30: pushCode(MICROCODE.FLAG_BITWISE_16); break;
 			case 0x38: pushCode(MICROCODE.FLAG_SUB_16); break;
 		}
 	}
 	else if (code==MICROCODE.FLAG_REP_SUB_08)
 	{
 		if (isCode(MICROCODE.PREFIX_REPNE)||isCode(MICROCODE.PREFIX_REPE))
 			pushCode(MICROCODE.FLAG_REP_SUB_08);
 		else
 			pushCode(MICROCODE.FLAG_SUB_08);
 	}
 	else if (code==MICROCODE.FLAG_REP_SUB_16)
 	{
 		if (isCode(MICROCODE.PREFIX_REPNE)||isCode(MICROCODE.PREFIX_REPE))
 			pushCode(MICROCODE.FLAG_REP_SUB_16);
 		else
 			pushCode(MICROCODE.FLAG_SUB_16);
 	}
 	else if (code==MICROCODE.FLAG_F6)
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00: pushCode(MICROCODE.FLAG_BITWISE_08); break;
 			case 0x18: pushCode(MICROCODE.FLAG_NEG_08); break;
 		}
 	}
 	else if (code==MICROCODE.FLAG_F7)
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00: pushCode(MICROCODE.FLAG_BITWISE_16); break;
 			case 0x18: pushCode(MICROCODE.FLAG_NEG_16); break;
 		}
 	}
 /*	else if (code==MICROCODE.FLAG_8F)
 	{
 //		panic("Need to implement flags for instruction 0x8F");
 		if(!isCode(MICROCODE.STORE0_SP))
 			pushCode(MICROCODE.STORE1_ESP);
 	}*/
 	else if (code==MICROCODE.FLAG_FE)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.FLAG_INC_08); break;
 			case 0x08:	pushCode(MICROCODE.FLAG_DEC_08); break;
 		}
 	}
 	else if (code==MICROCODE.FLAG_FF)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.FLAG_INC_16); break;
 			case 0x08:	pushCode(MICROCODE.FLAG_DEC_16); break;
 		}
 	}
 	else if (code==MICROCODE.FLAG_UNIMPLEMENTED)
 		panic("Unimplemented flag code "+opcode);
 	else if (code==MICROCODE.FLAG_BAD)
 		panic("Invalid flag code "+opcode);
 	else
 	{
 		pushCode(code);
 	}
 }
 
 private void decodeOperation(int opcode, int modrm)
 {
 	int tableindex=opcode;
 	if(tableindex>=0x0f00)
 		tableindex-=0xe00;
 	MICROCODE code=opcodeTable[tableindex];
 
 	//a few codes need to be decoded further
 	if (code==MICROCODE.OP_CBW)	//CBW: 0x98
 	{
 		if(!isCode(MICROCODE.PREFIX_OPCODE_32BIT))
 		{
 			pushCode(MICROCODE.LOAD0_AL);
 			pushCode(MICROCODE.OP_SIGN_EXTEND_8_16);
 			pushCode(MICROCODE.STORE0_AX);
 		}
 		else
 		{
 			pushCode(MICROCODE.LOAD0_AX);
 			pushCode(MICROCODE.OP_SIGN_EXTEND_16_32);
 			pushCode(MICROCODE.STORE0_EAX);
 		}
 	}
 	else if (code==MICROCODE.OP_BT_16_32)
 	{
 		if ((modrm&0xc7)==0)
 			pushCode(MICROCODE.OP_BT_MEM);
 		else
 			pushCode(code);
 	}
 	else if (code==MICROCODE.OP_BTC_16_32)
 	{
 		if ((modrm&0xc7)==0)
 			pushCode(MICROCODE.OP_BTC_MEM);
 		else
 			pushCode(code);
 	}
 	else if (code==MICROCODE.OP_BTR_16_32)
 	{
 		if ((modrm&0xc7)==0)
 			pushCode(MICROCODE.OP_BTR_MEM);
 		else
 			pushCode(code);
 	}
 	else if (code==MICROCODE.OP_BTS_16_32)
 	{
 		if ((modrm&0xc7)==0)
 			pushCode(MICROCODE.OP_BTS_MEM);
 		else
 			pushCode(code);
 	}
 	else if (code==MICROCODE.OP_ROTATE_08)	//rotates: 0xc0, 0xd0, 0xd2
 	{
 		int val=((modrm & 0x38)>>3);
 		code=rotationTable[16+val];
 		pushCode(code);
 	}
 	else if (code==MICROCODE.OP_ROTATE_16_32)	//rotates: 0xc1, 0xd1, 0xd3
 	{
 		int val=((modrm & 0x38)>>3);
 		code=rotationTable[24+val];
 		pushCode(code);
 	}
 	else if (code==MICROCODE.OP_80_83)	//imm: 80, 81, 82, 83
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_ADD); break;
 			case 0x08:	pushCode(MICROCODE.OP_OR); break;
 			case 0x10:	pushCode(MICROCODE.OP_ADC); break;
 			case 0x18:	pushCode(MICROCODE.OP_SBB); break;
 			case 0x20:	pushCode(MICROCODE.OP_AND); break;
 			case 0x28:	pushCode(MICROCODE.OP_SUB); break;
 			case 0x30:	pushCode(MICROCODE.OP_XOR); break;
 			case 0x38:	pushCode(MICROCODE.OP_SUB); break;
 		}
 	}
 	else if (code==MICROCODE.OP_F6)	//una
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_AND); break;
 			case 0x10:	pushCode(MICROCODE.OP_NOT); break;
 			case 0x18:	pushCode(MICROCODE.OP_NEG); break;
 			case 0x20:	pushCode(MICROCODE.OP_MUL_08); break;
 			case 0x28:	pushCode(MICROCODE.OP_IMULA_08); break;
 			case 0x30:	pushCode(MICROCODE.OP_DIV_08); break;
 			case 0x38:	pushCode(MICROCODE.OP_IDIV_08); break;
 //			default:	panic("Bad UNA F6");
 		}
 	}
 	else if (code==MICROCODE.OP_F7)	//una
 	{
 		switch(modrm&0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_AND); break;
 			case 0x10:	pushCode(MICROCODE.OP_NOT); break;
 			case 0x18:	pushCode(MICROCODE.OP_NEG); break;
 			case 0x20:	pushCode(MICROCODE.OP_MUL_16_32); break;
 			case 0x28:	pushCode(MICROCODE.OP_IMULA_16_32); break;
 			case 0x30:	pushCode(MICROCODE.OP_DIV_16_32); break;
 			case 0x38:	pushCode(MICROCODE.OP_IDIV_16_32); break;
 //			default:	panic("Bad UNA F7");
 		}
 	}
 	else if (code==MICROCODE.OP_FE)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_INC); break;
 			case 0x08:	pushCode(MICROCODE.OP_DEC); break;
 		}
 	}
 	else if (code==MICROCODE.OP_FF)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_INC); break;
 			case 0x08:	pushCode(MICROCODE.OP_DEC); break;
 			case 0x10:	pushCode(MICROCODE.OP_CALL_ABS); break;
 			case 0x18:	pushCode(MICROCODE.OP_CALL_FAR); break;
 			case 0x20:	pushCode(MICROCODE.OP_JMP_ABS); break;
 			case 0x28:	pushCode(MICROCODE.OP_JMP_FAR); break;
 			case 0x30:	pushCode(MICROCODE.OP_PUSH); break;
 			default:	//panic("Invalid operation FF code");
 		}
 	}
 	else if (code==MICROCODE.OP_F00)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_SLDT); break;
 			case 0x08:	pushCode(MICROCODE.OP_STR); break;
 			case 0x10:	pushCode(MICROCODE.OP_LLDT); break;
 			case 0x18:	pushCode(MICROCODE.OP_LTR); break;
 			case 0x20:	pushCode(MICROCODE.OP_VERR); break;
 			case 0x28:	pushCode(MICROCODE.OP_VERW); break;
 			default:	panic("Invalid operation F00 code: "+(modrm&0x38)); break;
 		}
 	}
 	else if (code==MICROCODE.OP_F01)
 	{
 		switch(modrm & 0x38)
 		{
 			case 0x00:	pushCode(MICROCODE.OP_SGDT); break;
 			case 0x08:	pushCode(MICROCODE.OP_SIDT); break;
 			case 0x10:	pushCode(MICROCODE.OP_LGDT); break;
 			case 0x18:	pushCode(MICROCODE.OP_LIDT); break;
 			case 0x20:	pushCode(MICROCODE.OP_SMSW); break;
 			case 0x30:	pushCode(MICROCODE.OP_LMSW); break;
 			default:	panic("Invalid operation F01 code"); break;
 		}
 	}
 	else if (code==MICROCODE.OP_BAD)
 	{
 		panic("Invalid instruction "+opcode);
 		return;
 	}
 	else if (code==MICROCODE.OP_UNIMPLEMENTED)
 	{
 //		pushCode(MICROCODE.OP_UNIMPLEMENTED);
 		panic("Unimplemented instruction "+opcode);
 		return;
 	}
 	else
 	{
 		//handle repeat codes
 		if(isCode(MICROCODE.PREFIX_REPE) || isCode(MICROCODE.PREFIX_REPNE))
 			switch(opcode)
 			{
 				case 0x6c: pushCode(MICROCODE.OP_REP_INSB); break;
 				case 0x6d: pushCode(MICROCODE.OP_REP_INSW); break;
 				case 0x6e: pushCode(MICROCODE.OP_REP_OUTSB); break;
 				case 0x6f: pushCode(MICROCODE.OP_REP_OUTSW); break;
 				case 0xa4: pushCode(MICROCODE.OP_REP_MOVSB); break;
 				case 0xa5: pushCode(MICROCODE.OP_REP_MOVSW); break;
 				case 0xaa: pushCode(MICROCODE.OP_REP_STOSB); break;
 				case 0xab: pushCode(MICROCODE.OP_REP_STOSW); break;
 				case 0xac: pushCode(MICROCODE.OP_REP_LODSB); break;
 				case 0xad: pushCode(MICROCODE.OP_REP_LODSW); break;
 				case 0xa6: 
 					if(isCode(MICROCODE.PREFIX_REPE)) pushCode(MICROCODE.OP_REPE_CMPSB); 
 					else pushCode(MICROCODE.OP_REPNE_CMPSB);
 					break;
 				case 0xa7: 
 					if(isCode(MICROCODE.PREFIX_REPE)) pushCode(MICROCODE.OP_REPE_CMPSW); 
 					else pushCode(MICROCODE.OP_REPNE_CMPSW);
 					break;
 				case 0xae: 
 					if(isCode(MICROCODE.PREFIX_REPE)) pushCode(MICROCODE.OP_REPE_SCASB); 
 					else pushCode(MICROCODE.OP_REPNE_SCASB);
 					break;
 				case 0xaf: 
 					if(isCode(MICROCODE.PREFIX_REPE)) pushCode(MICROCODE.OP_REPE_SCASW); 
 					else pushCode(MICROCODE.OP_REPNE_SCASW);
 					break;
 			}
 		//normal instructions: repush
 		else
 			pushCode(code);
 	}
 	if(processorGUICode!=null) processorGUICode.pushInstruction(this.code[codeLength-1],opcode);
 }
 
 private void replaceFlags1632()
 {
 	for (int i=0; i<codeLength; i++)
 	{
 		if (code[i]==MICROCODE.FLAG_BITWISE_16) code[i]=MICROCODE.FLAG_BITWISE_32;
 		else if (code[i]==MICROCODE.FLAG_ADD_16) code[i]=MICROCODE.FLAG_ADD_32;
 		else if (code[i]==MICROCODE.FLAG_ADC_16) code[i]=MICROCODE.FLAG_ADC_32;
 		else if (code[i]==MICROCODE.FLAG_SUB_16) code[i]=MICROCODE.FLAG_SUB_32;
 		else if (code[i]==MICROCODE.FLAG_SBB_16) code[i]=MICROCODE.FLAG_SBB_32;
 		else if (code[i]==MICROCODE.FLAG_SHL_16) code[i]=MICROCODE.FLAG_SHL_32;
 		else if (code[i]==MICROCODE.FLAG_SHR_16) code[i]=MICROCODE.FLAG_SHR_32;
 		else if (code[i]==MICROCODE.FLAG_SAR_16) code[i]=MICROCODE.FLAG_SAR_32;
 		else if (code[i]==MICROCODE.FLAG_ROL_16) code[i]=MICROCODE.FLAG_ROL_32;
 		else if (code[i]==MICROCODE.FLAG_ROR_16) code[i]=MICROCODE.FLAG_ROR_32;
 		else if (code[i]==MICROCODE.FLAG_RCL_16) code[i]=MICROCODE.FLAG_RCL_32;
 		else if (code[i]==MICROCODE.FLAG_RCR_16) code[i]=MICROCODE.FLAG_RCR_32;
 		else if (code[i]==MICROCODE.FLAG_NEG_16) code[i]=MICROCODE.FLAG_NEG_32;
 		else if (code[i]==MICROCODE.FLAG_REP_SUB_16) code[i]=MICROCODE.FLAG_REP_SUB_32;
 		else if (code[i]==MICROCODE.FLAG_INC_16) code[i]=MICROCODE.FLAG_INC_32;
 		else if (code[i]==MICROCODE.FLAG_DEC_16) code[i]=MICROCODE.FLAG_DEC_32;
 	}
 }
 
 public final Processor_Exception DIVIDE_ERROR = new Processor_Exception(0x00);
 public final Processor_Exception DEBUG = new Processor_Exception(0x01);
 public final Processor_Exception BREAKPOINT = new Processor_Exception(0x03);
 public final Processor_Exception OVERFLOW = new Processor_Exception(0x04);
 public final Processor_Exception BOUND_RANGE = new Processor_Exception(0x05);
 public final Processor_Exception UNDEFINED = new Processor_Exception(0x06);
 public final Processor_Exception NO_FPU = new Processor_Exception(0x07);
 public final Processor_Exception DOUBLE_FAULT = new Processor_Exception(0x08);
 public final Processor_Exception FPU_SEGMENT_OVERRUN = new Processor_Exception(0x09);
 public final Processor_Exception TASK_SWITCH = new Processor_Exception(0x0a);
 public final Processor_Exception NOT_PRESENT = new Processor_Exception(0x0b);
 public final Processor_Exception STACK_SEGMENT = new Processor_Exception(0x0c);
 public final Processor_Exception GENERAL_PROTECTION = new Processor_Exception(0x0d);
 public final Processor_Exception PAGE_FAULT = new Processor_Exception(0x0e);
 public final Processor_Exception FLOATING_POINT = new Processor_Exception(0x10);
 public final Processor_Exception ALIGNMENT_CHECK = new Processor_Exception(0x11);
 public final Processor_Exception MACHINE_CHECK = new Processor_Exception(0x12);
 public final Processor_Exception SIMD_FLOATING_POINT = new Processor_Exception(0x13);
 
 public class Processor_Exception extends RuntimeException
 {
 	private static final long serialVersionUID = 1L;
 	int vector;
 
 	public Processor_Exception(int vector)
 	{
 		this.vector=vector;
 	}
 }
 
 
 
 public void constructProcessorGUICode()
 {
 	processorGUICode=new ProcessorGUICode();
 }
 
 public class ProcessorGUICode
 {
 	private String addressString;
 
 	ArrayList<GUICODE> code;
 	ArrayList<String> name;
 	ArrayList<String> value;
 	
 	int instructionNumber;
 //	int displacement;
 
 	public ProcessorGUICode()
 	{
 		code=new ArrayList<GUICODE>();
 		name=new ArrayList<String>();
 		value=new ArrayList<String>();
 		
 		addressString="";
 		instructionNumber=instructionCount++;
 	}
 
 	public void updateMemoryGUI()
 	{
 		if (computer.memoryGUI==null) return;
 
 try{
 		for (int i=0; i<code.size(); i++)
 		{
 			int j;
 			if (code.get(i)==null) return;
 			switch(code.get(i))
 			{
 				case CS_MEMORY_READ:	computer.memoryGUI.codeRead(Integer.parseInt(name.get(i),16)); break;
 				case CS_MEMORY_WRITE:	computer.memoryGUI.codeWrite(Integer.parseInt(name.get(i),16)); break;
 				case SS_MEMORY_READ:	computer.memoryGUI.stackRead(Integer.parseInt(name.get(i),16)); break;
 				case SS_MEMORY_WRITE:
 					for (j=0; j<i; j++)
 						if (code.get(j)==GUICODE.DECODE_INPUT_OPERAND_0)
 							break;
 					if (j<i)
 					{
 						int size=1;
 						if (!name.equals("") && name.get(j).charAt(0)=='e') size=4;
 						else if (name.get(j).equals("ax")||name.get(j).equals("bx")||name.get(j).equals("cx")||name.get(j).equals("dx")||name.get(j).equals("sp")||name.get(j).equals("bp")||name.get(j).equals("si")||name.get(j).equals("di")||name.get(j).equals("cs")||name.get(j).equals("ss")||name.get(j).equals("ds")||name.get(j).equals("es")||name.get(j).equals("fs")||name.get(j).equals("gs")||name.get(j).equals("ip")) size=2;
 						computer.memoryGUI.stackWrite(Integer.parseInt(name.get(i),16),name.get(j),size); break;
 					}
 					computer.memoryGUI.stackWrite(Integer.parseInt(name.get(i),16)); break;
 				case DS_MEMORY_READ:	computer.memoryGUI.dataRead(Integer.parseInt(name.get(i),16)); break;
 				case DS_MEMORY_WRITE:	computer.memoryGUI.dataWrite(Integer.parseInt(name.get(i),16)); break;
 				case ES_MEMORY_READ: case FS_MEMORY_READ: case GS_MEMORY_READ:
 					computer.memoryGUI.extraRead(Integer.parseInt(name.get(i),16)); break;
 				case ES_MEMORY_WRITE: case FS_MEMORY_WRITE: case GS_MEMORY_WRITE:
 					computer.memoryGUI.extraWrite(Integer.parseInt(name.get(i),16)); break;
 				case IDTR_MEMORY_READ:	computer.memoryGUI.interruptRead(Integer.parseInt(name.get(i),16)); break;
 				case IDTR_MEMORY_WRITE:	computer.memoryGUI.interruptWrite(Integer.parseInt(name.get(i),16)); break;
 			}
 		}
 }catch(Exception e){}
 		computer.memoryGUI.updateIP(cs.address(eip.getValue()));
 	}
 
 	public void updateGUI()
 	{
 		if (computer.processorGUI==null) return;
 		if (!computer.debugMode && !computer.updateGUIOnPlay) return;
 
 		System.out.println("Instruction "+instructionNumber+":");
 
 		for (int i=0; i<code.size(); i++)
 		{
 			if (computer.processorGUI!=null) computer.processorGUI.applyCode(code.get(i),name.get(i),value.get(i));
 
 			System.out.print(code.get(i));
 			System.out.print(" ");
 			if (!name.get(i).equals(""))
 				System.out.print(name.get(i)+" ");
 			if (!value.get(i).equals(""))
 				System.out.print(value.get(i));
 			System.out.println();
 
 		}
 
 	}
 
 	public String constructName()
 	{
 		boolean o0=false, i0=false;
 		String n="";
 		for (int i=0; i<code.size(); i++)
 		{
 			if (code.get(i)==GUICODE.DECODE_INSTRUCTION)
 			{
 				n=name.get(i);
 				break;
 			}
 		}
 		for (int i=0; i<code.size(); i++)
 		{
 			if (code.get(i)==GUICODE.DECODE_OUTPUT_OPERAND_0)
 			{
 				n=n+" "+name.get(i);
 				o0=true;
 				break;
 			}
 		}
 		for (int i=0; i<code.size(); i++)
 		{
 			if (code.get(i)==GUICODE.DECODE_INPUT_OPERAND_0)
 			{
 				i0=true;
 				if(o0)
 					n=n+" =";
 				if (name.get(i).equals("immediate"))
 					n=n+" "+Integer.toHexString(getLastiCode());
 				else
 					n=n+" "+name.get(i);
 				break;
 			}
 		}
 		for (int i=0; i<code.size(); i++)
 		{
 			if (code.get(i)==GUICODE.DECODE_INPUT_OPERAND_1)
 			{
 				if(i0)
 					n=n+",";
 				else if (o0)
 					n=n+" =";
 
 				if (name.get(i).equals("immediate"))
 					n=n+" "+Integer.toHexString(getLastiCode());
 				else
 					n=n+" "+name.get(i);
 				break;
 			}
 		}
 		for (int i=0; i<code.size(); i++)
 		{
 			if (code.get(i)==GUICODE.DECODE_INPUT_OPERAND_2)
 			{
 				if(i0)
 					n=n+",";
 				else if (o0)
 					n=n+" =";
 
 				if (name.get(i).equals("immediate"))
 					n=n+" "+Integer.toHexString(getLastiCode());
 				else
 					n=n+" "+name.get(i);
 				break;
 			}
 		}
 		return n;
 	}
 
 	public void push(GUICODE guicode)
 	{
 		code.add(guicode);
 		name.add("");
 		value.add("");
 	}
 	public void push(GUICODE guicode, String name)
 	{
 		code.add(guicode);
 		this.name.add(name);
 		value.add("");
 	}
 	public void push(GUICODE guicode, String name, int value)
 	{
 		code.add(guicode);
 		this.name.add(name);
 		this.value.add(Integer.toHexString(value));
 	}
 	public void push(GUICODE guicode, int name, int value)
 	{
 		code.add(guicode);
 		this.name.add(Integer.toHexString(name));
 		this.value.add(Integer.toHexString(value));
 	}
 	public void push(GUICODE guicode, int name)
 	{
 		code.add(guicode);
 		this.name.add(Integer.toHexString(name));
 		this.value.add("");
 	}
 	public void pushFetch(int ip)
 	{
 		push(GUICODE.FETCH,ip);
 	}
 	public void pushFlag(int type, int value)
 	{
 		String name="";
 		switch(type)
 		{
 			case Flag.AUXILIARYCARRY: name="auxiliary carry"; break;
 			case Flag.CARRY: name="carry"; break;
 			case Flag.SIGN: name="sign"; break;
 			case Flag.PARITY: name="parity"; break;
 			case Flag.ZERO: name="zero"; break;
 			case Flag.OVERFLOW: name="overflow"; break;
 			default: name="other"; break;
 		}
 		if (value==0)
 			push(GUICODE.FLAG_CLEAR,name);
 		else if (value==1)
 			push(GUICODE.FLAG_SET,name);
 //		else
 //			push(GUICODE.FLAG_READ,name);
 	}
 
 	public void pushRegister(int id, int access, int value)
 	{
 		String name="";
 		switch(id)
 		{
 			case Register.EAX: name="eax"; break;
 			case Register.EBX: name="ebx"; break;
 			case Register.ECX: name="ecx"; break;
 			case Register.EDX: name="edx"; break;
 			case Register.ESI: name="esi"; break;
 			case Register.EDI: name="edi"; break;
 			case Register.ESP: name="esp"; break;
 			case Register.EBP: name="ebp"; break;
 			case Register.EIP: name="eip"; break;
 			case Register.CR0: name="cr0"; break;
 			case Register.CR2: name="cr2"; break;
 			case Register.CR3: name="cr3"; break;
 			case Register.CR4: name="cr4"; break;
 		}
 		if (access!=0)
 			push(GUICODE.REGISTER_WRITE,name,value);
 //		else
 //			push(GUICODE.REGISTER_READ,name,value);
 	}
 
 	public void pushSegment(int id, int access, int value)
 	{
 		String name="";
 		switch(id)
 		{
 			case Segment.CS: name="cs"; break;
 			case Segment.SS: name="ss"; break;
 			case Segment.DS: name="ds"; break;
 			case Segment.ES: name="es"; break;
 			case Segment.FS: name="fs"; break;
 			case Segment.GS: name="gs"; break;
 			case Segment.IDTR: name="idtr"; break;
 			case Segment.GDTR: name="gdtr"; break;
 			case Segment.LDTR: name="ldtr"; break;
 			case Segment.TSS: name="tss"; break;
 		}
 		if (access!=0)
 			push(GUICODE.REGISTER_WRITE,name,value);
 //		else
 //			push(GUICODE.REGISTER_READ,name,value);
 	}
 
 	public void pushMemory(int segid, int segvalue, int access, int address, short value)
 	{
 		pushMemory(segid,segvalue,access,address,(byte)(value));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>8));
 	}
 
 	public void pushMemory(int segid, int segvalue, int access, int address, int value)
 	{
 		pushMemory(segid,segvalue,access,address,(byte)(value));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>8));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>16));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>24));
 	}
 
 	public void pushMemory(int segid, int segvalue, int access, int address, long value)
 	{
 		pushMemory(segid,segvalue,access,address,(byte)(value));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>8));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>16));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>24));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>32));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>40));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>48));
 		pushMemory(segid,segvalue,access,address,(byte)(value>>>56));
 	}
 
 	public void pushMemory(int segid, int segvalue, int access, int address, byte value)
 	{
 		pushSegment(segid,0,segvalue);
 		if(access==0)
 		{
 			switch(segid)
 			{
 				case Segment.CS: push(GUICODE.CS_MEMORY_READ,address,value); break;
 				case Segment.SS: push(GUICODE.SS_MEMORY_READ,address,value); break;
 				case Segment.DS: push(GUICODE.DS_MEMORY_READ,address,value); break;
 				case Segment.ES: push(GUICODE.ES_MEMORY_READ,address,value); break;
 				case Segment.FS: push(GUICODE.FS_MEMORY_READ,address,value); break;
 				case Segment.GS: push(GUICODE.GS_MEMORY_READ,address,value); break;
 				case Segment.IDTR: push(GUICODE.IDTR_MEMORY_READ,address,value); break;
 				case Segment.GDTR: push(GUICODE.GDTR_MEMORY_READ,address,value); break;
 				case Segment.LDTR: push(GUICODE.LDTR_MEMORY_READ,address,value); break;
 				case Segment.TSS: push(GUICODE.TSS_MEMORY_READ,address,value); break;
 			}
 		}
 		else
 		{
 			switch(segid)
 			{
 				case Segment.CS: push(GUICODE.CS_MEMORY_WRITE,address,value); break;
 				case Segment.SS: push(GUICODE.SS_MEMORY_WRITE,address,value); break;
 				case Segment.DS: push(GUICODE.DS_MEMORY_WRITE,address,value); break;
 				case Segment.ES: push(GUICODE.ES_MEMORY_WRITE,address,value); break;
 				case Segment.FS: push(GUICODE.FS_MEMORY_WRITE,address,value); break;
 				case Segment.GS: push(GUICODE.GS_MEMORY_WRITE,address,value); break;
 				case Segment.IDTR: push(GUICODE.IDTR_MEMORY_WRITE,address,value); break;
 				case Segment.GDTR: push(GUICODE.GDTR_MEMORY_WRITE,address,value); break;
 				case Segment.LDTR: push(GUICODE.LDTR_MEMORY_WRITE,address,value); break;
 				case Segment.TSS: push(GUICODE.TSS_MEMORY_WRITE,address,value); break;
 			}
 		}
 	}
 
 	public void pushInstruction(MICROCODE microcode, int opcode)
 	{
 		String name="";
 
 		switch(microcode)
 		{
 			case OP_MOV: name="mov"; break;
 			case OP_JMP_FAR: name="jmp far"; break;
 			case OP_JMP_ABS: name="jmp abs"; break;
 			case OP_JMP_08:  name="jmp"; break;
 			case OP_JMP_16_32:  name="jmp"; break;
 			case OP_CALL_FAR: name="call far"; break;
 			case OP_CALL_ABS: name="call abs"; break;
 			case OP_CALL: name="call"; break;
 			case OP_RET: name="ret"; break;
 			case OP_RET_IW: name="ret iw"; break;
 			case OP_RET_FAR: name="ret far"; break;
 			case OP_RET_FAR_IW: name="ret far iw"; break;
 			case OP_JO: name="jo"; break;
 			case OP_JO_16_32: name="jo"; break;
 			case OP_JNO: name="jno"; break;
 			case OP_JNO_16_32: name="jno"; break;
 			case OP_JC: name="jc"; break;
 			case OP_JC_16_32: name="jc"; break;
 			case OP_JNC: name="jnc"; break;
 			case OP_JNC_16_32: name="jnc"; break;
 			case OP_JS: name="js"; break;
 			case OP_JS_16_32: name="js"; break;
 			case OP_JNS: name="jns"; break;
 			case OP_JNS_16_32: name="jns"; break;
 			case OP_JZ: name="jz"; break;
 			case OP_JZ_16_32: name="jz"; break;
 			case OP_JNZ: name="jnz"; break;
 			case OP_JNZ_16_32: name="jnz"; break;
 			case OP_JP: name="jp"; break;
 			case OP_JP_16_32: name="jp"; break;
 			case OP_JNP: name="jnp"; break;
 			case OP_JNP_16_32: name="jnp"; break;
 			case OP_JA: name="ja"; break;
 			case OP_JA_16_32: name="ja"; break;
 			case OP_JNA: name="jna"; break;
 			case OP_JNA_16_32: name="jna"; break;
 			case OP_JL: name="jl"; break;
 			case OP_JL_16_32: name="jl"; break;
 			case OP_JNL: name="jnl"; break;
 			case OP_JNL_16_32: name="jnl"; break;
 			case OP_JG: name="jg"; break;
 			case OP_JG_16_32: name="jg"; break;
 			case OP_JNG: name="jng"; break;
 			case OP_JNG_16_32: name="jng"; break;
 			case OP_LOOP_CX:  name="loop cx"; break;
 			case OP_LOOPZ_CX:  name="loopz cx"; break;
 			case OP_LOOPNZ_CX: name="loopnz cx"; break;
 			case OP_JCXZ: name="jcxz"; break;
 			case OP_IN_08: name="in"; break;
 			case OP_IN_16_32: name="in"; break;
 			case OP_INSB: name="insb"; break;
 			case OP_INSW: name="insw"; break;
 			case OP_REP_INSB: name="rep insb"; break;
 			case OP_REP_INSW: name="rep insw"; break;
 			case OP_OUT_08: name="out"; break;
 			case OP_OUT_16_32: name="out"; break;
 			case OP_OUTSB: name="outsb"; break;
 			case OP_OUTSW: name="outsw"; break;
 			case OP_REP_OUTSB: name="rep outsb"; break;
 			case OP_REP_OUTSW: name="rep outsw"; break;
 			case OP_INT: name="int"; break;
 			case OP_INT3: name="int"; break;
 			case OP_IRET: name="iret"; break;
 			case OP_INC: name="inc"; break;
 			case OP_DEC: name="dec"; break;
 			case OP_ADD: name="add"; break;
 			case OP_ADC: name="adc"; break;
 			case OP_SUB: name="sub"; break;
 			case OP_CMP: name="cmp"; break;
 			case OP_SBB: name="sbb"; break;
 			case OP_AND: name="and"; break;
 			case OP_TEST: name="test"; break;
 			case OP_OR: name="or"; break;
 			case OP_XOR: name="xor"; break;
 			case OP_ROL_08: name="rol"; break;
 			case OP_ROL_16_32: name="rol"; break;
 			case OP_ROR_08: name="ror"; break;
 			case OP_ROR_16_32: name="ror"; break;
 			case OP_RCL_08: name="rcl"; break;
 			case OP_RCL_16_32: name="rcl"; break;
 			case OP_RCR_08: name="rcr"; break;
 			case OP_RCR_16_32: name="rcr"; break;
 			case OP_SHL: name="shl"; break;
 			case OP_SHR: name="shr"; break;
 			case OP_SAR_08: name="sar"; break;
 			case OP_SAR_16_32: name="sar"; break;
 			case OP_NOT: name="not"; break;
 			case OP_NEG: name="neg"; break;
 			case OP_MUL_08: name="mul"; break;
 			case OP_MUL_16_32: name="mul"; break;
 			case OP_IMULA_08: name="imula"; break;
 			case OP_IMULA_16_32: name="imula"; break;
 			case OP_IMUL_16_32: name="imul"; break;
 			case OP_DIV_08: name="div"; break;
 			case OP_DIV_16_32: name="div"; break;
 			case OP_IDIV_08: name="idiv"; break;
 			case OP_IDIV_16_32: name="idiv"; break;
			case OP_BT_MEM: name="bt mem"; break;
			case OP_BTS_MEM: name="bts mem"; break;
			case OP_BTR_MEM: name="btr mem"; break;
			case OP_BTC_MEM: name="btc mem"; break;
 			case OP_BT_16_32: name="bt"; break;
 			case OP_BTS_16_32: name="bts"; break;
 			case OP_BTR_16_32: name="btr"; break;
 			case OP_BTC_16_32: name="btc"; break;
 			case OP_SHLD_16_32: name="shld"; break;
 			case OP_SHRD_16_32: name="shrd"; break;
 			case OP_AAA: name="aaa"; break;
 			case OP_AAD: name="aad"; break;
 			case OP_AAM: name="aam"; break;
 			case OP_AAS: name="aas"; break;
 			case OP_CWD: name="cwd"; break;
 			case OP_DAA: name="daa"; break;
 			case OP_DAS: name="das"; break;
 			case OP_BOUND: name="bound"; break;
 			case OP_SIGN_EXTEND: name="cdw"; break;
 			case OP_SIGN_EXTEND_8_16: name="cdw"; break;
 			case OP_SIGN_EXTEND_8_32: name="cdw"; break;
 			case OP_SIGN_EXTEND_16_32: name="cdw"; break;
 			case OP_SCASB: name="scasb"; break;
 			case OP_SCASW: name="scasw"; break;
 			case OP_REPNE_SCASB: name="repne scasb"; break;
 			case OP_REPE_SCASB: name="repe scasb"; break;
 			case OP_REPNE_SCASW: name="repne scasw"; break;
 			case OP_REPE_SCASW: name="repe scasw"; break;
 			case OP_CMPSB: name="cmpsb"; break;
 			case OP_CMPSW: name="cmpsw"; break;
 			case OP_REPNE_CMPSB: name="repne cmpsb"; break;
 			case OP_REPE_CMPSB: name="repe cmpsb"; break;
 			case OP_REPNE_CMPSW: name="repne cmpsw"; break;
 			case OP_REPE_CMPSW: name="repe cmpsw"; break;
 			case OP_LODSB: name="lodsb"; break;
 			case OP_LODSW: name="lodsw"; break;
 			case OP_REP_LODSB: name="rep lodsb"; break;
 			case OP_REP_LODSW: name="rep lodsw"; break;
 			case OP_STOSB: name="stosb"; break;
 			case OP_STOSW: name="stosw"; break;
 			case OP_REP_STOSB: name="rep stosb"; break;
 			case OP_REP_STOSW: name="rep stosw"; break;
 			case OP_MOVSB: name="movsb"; break;
 			case OP_MOVSW: name="movsw"; break;
 			case OP_REP_MOVSB: name="rep movsb"; break;
 			case OP_REP_MOVSW: name="rep movsw"; break;
 			case OP_CLC: name="clc"; break;
 			case OP_STC: name="stc"; break;
 			case OP_CLI: name="cli"; break;
 			case OP_STI: name="sti"; break;
 			case OP_CLD: name="cld"; break;
 			case OP_STD: name="std"; break;
 			case OP_CMC: name="cmc"; break;
 			case OP_SETO: name="seto"; break;
 			case OP_SETNO: name="setno"; break;
 			case OP_SETC: name="setc"; break;
 			case OP_SETNC: name="setnc"; break;
 			case OP_SETZ: name="setz"; break;
 			case OP_SETNZ: name="setnz"; break;
 			case OP_SETA: name="seta"; break;
 			case OP_SETNA: name="setna"; break;
 			case OP_SETL: name="setl"; break;
 			case OP_SETNL: name="setnl"; break;
 			case OP_SETG: name="setg"; break;
 			case OP_SETNG: name="setng"; break;
 			case OP_SETS: name="sets"; break;
 			case OP_SETNS: name="setns"; break;
 			case OP_SETP: name="setp"; break;
 			case OP_SETNP: name="setnp"; break;
 			case OP_SALC: name="salc"; break;
 			case OP_LAHF: name="lahf"; break;
 			case OP_SAHF: name="sahf"; break;
 			case OP_CMOVO: name="cmovo"; break;
 			case OP_CMOVNO: name="cmovno"; break;
 			case OP_CMOVC: name="cmovc"; break;
 			case OP_CMOVNC: name="cmovnc"; break;
 			case OP_CMOVZ: name="cmovz"; break;
 			case OP_CMOVNZ: name="cmovnz"; break;
 			case OP_CMOVP: name="cmovp"; break;
 			case OP_CMOVNP: name="cmovnp"; break;
 			case OP_CMOVS: name="cmovs"; break;
 			case OP_CMOVNS: name="cmovns"; break;
 			case OP_CMOVL: name="cmovl"; break;
 			case OP_CMOVNL: name="cmovnl"; break;
 			case OP_CMOVG: name="cmovg"; break;
 			case OP_CMOVNG: name="cmovng"; break;
 			case OP_CMOVA: name="cmova"; break;
 			case OP_CMOVNA: name="cmovna"; break;
 			case OP_POP: name="pop"; break;
 			case OP_POPF: name="popf"; break;
 			case OP_POPA: name="popa"; break;
 			case OP_LEAVE: name="leave"; break;
 			case OP_PUSH: name="push"; break;
 			case OP_PUSHF: name="pushf"; break;
 			case OP_PUSHA: name="pusha"; break;
 			case OP_ENTER: name="enter"; break;
 			case OP_LGDT: name="lgdt"; break;
 			case OP_LIDT: name="lidt"; break;
 			case OP_LLDT: name="lldt"; break;
 			case OP_LMSW: name="lmsw"; break;
 			case OP_LTR: name="ltr"; break;
 			case OP_SGDT: name="sgdt"; break;
 			case OP_SIDT: name="sidt"; break;
 			case OP_SLDT: name="sldt"; break;
 			case OP_SMSW: name="smsw"; break;
 			case OP_STR: name="str"; break;
 			case OP_CLTS: name="clts"; break;
 			case OP_CPUID: name="cpuid"; break;
 			case OP_HALT: name="halt"; break;
 			case OP_RDTSC: name="rdtsc"; break;
 			default: name="nop"; break;
 		}
 
 		push(GUICODE.DECODE_INSTRUCTION, name);
 	}
 
 	public void pushMicrocode(MICROCODE microcode, int reg0, int reg1, int reg2, int addr, int displacement, boolean condition)
 	{
 		String name="";
 		switch(microcode)
 		{
 			case LOAD_SEG_CS: addressString="cs:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"cs"); break;
 			case LOAD_SEG_SS: addressString="ss:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"ss"); break;
 			case LOAD_SEG_DS: addressString="ds:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"ds"); break;
 			case LOAD_SEG_ES: addressString="es:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"es"); break;
 			case LOAD_SEG_FS: addressString="fs:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"fs"); break;
 			case LOAD_SEG_GS: addressString="gs:"; push(GUICODE.DECODE_SEGMENT_ADDRESS,"gs"); break;
 
 			case ADDR_AX: addressString+="ax+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"ax"); break;
 			case ADDR_BX: addressString+="bx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bx"); break;
 			case ADDR_CX: addressString+="cx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"cx"); break;
 			case ADDR_DX: addressString+="dx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"dx"); break;
 			case ADDR_SP: addressString+="sp+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"sp"); break;
 			case ADDR_BP: addressString+="bp+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bp"); break;
 			case ADDR_SI: addressString+="si+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"si"); break;
 			case ADDR_DI: addressString+="di+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"di"); break;
 			case ADDR_EAX: addressString+="eax+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"ax"); break;
 			case ADDR_EBX: addressString+="ebx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bx"); break;
 			case ADDR_ECX: addressString+="ecx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"cx"); break;
 			case ADDR_EDX: addressString+="edx+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"dx"); break;
 			case ADDR_ESP: addressString+="esp+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"sp"); break;
 			case ADDR_EBP: addressString+="ebp+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bp"); break;
 			case ADDR_ESI: addressString+="esi+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"si"); break;
 			case ADDR_EDI: addressString+="edi+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"di"); break;
 			case ADDR_2EAX: addressString+="eax*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"ax"); break;
 			case ADDR_2EBX: addressString+="ebx*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bx"); break;
 			case ADDR_2ECX: addressString+="ecx*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"cx"); break;
 			case ADDR_2EDX: addressString+="edx*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"dx"); break;
 			case ADDR_2EBP: addressString+="ebp*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bp"); break;
 			case ADDR_2ESI: addressString+="esi*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"si"); break;
 			case ADDR_2EDI: addressString+="edi*2+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"di"); break;
 			case ADDR_4EAX: addressString+="eax*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"ax"); break;
 			case ADDR_4EBX: addressString+="ebx*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bx"); break;
 			case ADDR_4ECX: addressString+="ecx*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"cx"); break;
 			case ADDR_4EDX: addressString+="edx*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"dx"); break;
 			case ADDR_4EBP: addressString+="ebp*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bp"); break;
 			case ADDR_4ESI: addressString+="esi*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"si"); break;
 			case ADDR_4EDI: addressString+="edi*4+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"di"); break;
 			case ADDR_8EAX: addressString+="eax*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"ax"); break;
 			case ADDR_8EBX: addressString+="ebx*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bx"); break;
 			case ADDR_8ECX: addressString+="ecx*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"cx"); break;
 			case ADDR_8EDX: addressString+="edx*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"dx"); break;
 			case ADDR_8EBP: addressString+="ebp*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"bp"); break;
 			case ADDR_8ESI: addressString+="esi*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"si"); break;
 			case ADDR_8EDI: addressString+="edi*8+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"di"); break;
 			case ADDR_IB: addressString+=Integer.toHexString(displacement)+"+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"displacement",displacement); break;
 			case ADDR_IW: addressString+=Integer.toHexString(displacement)+"+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"displacement",displacement); break;
 			case ADDR_ID: addressString+=Integer.toHexString(displacement)+"+"; push(GUICODE.DECODE_MEMORY_ADDRESS,"displacement",displacement); break;
 
 			case LOAD2_AX: name="ax"; push(GUICODE.DECODE_INPUT_OPERAND_2,name,reg2); break;
 			case LOAD2_EAX: name="eax"; push(GUICODE.DECODE_INPUT_OPERAND_2,name,reg2); break;
 			case LOAD2_AL: name="al"; push(GUICODE.DECODE_INPUT_OPERAND_2,name,reg2); break;
 			case LOAD2_CL: name="cl"; push(GUICODE.DECODE_INPUT_OPERAND_2,name,reg2); break;
 			case LOAD2_IB: name="immediate"; push(GUICODE.DECODE_INPUT_OPERAND_2,name,reg2); break;
 
 			case LOAD0_AX: name="ax"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_EAX: name="eax"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_AL: name="al"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_AH: name="ah"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_BX: name="bx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_EBX: name="ebx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_BL: name="bl"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_BH: name="bh"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CX: name="cx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_ECX: name="ecx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CL: name="cl"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CH: name="ch"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_DX: name="dx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_EDX: name="edx"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_DL: name="dl"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_DH: name="dh"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_SI: name="si"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_ESI: name="esi"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_DI: name="di"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_EDI: name="edi"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_SP: name="sp"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_ESP: name="esp"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_BP: name="bp"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_EBP: name="ebp"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CS: name="cs"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_DS: name="ds"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_SS: name="ss"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_ES: name="es"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_FS: name="fs"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_GS: name="gs"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_FLAGS: case LOAD0_EFLAGS: name="flags"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_ADDR: name="["+addressString.substring(0,addressString.length()-1)+"]"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_IB: case LOAD0_IW: case LOAD0_ID: name="immediate"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CR0: name="cr0"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CR2: name="cr2"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CR3: name="cr3"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_CR4: name="cr4"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD0_MEM_BYTE: case LOAD0_MEM_WORD: case LOAD0_MEM_DOUBLE: name="memory["+addressString.substring(0,addressString.length()-1)+"]"; push(GUICODE.DECODE_INPUT_OPERAND_0,name,reg0); break;
 			case LOAD1_AX: name="ax"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_EAX: name="eax"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_AL: name="al"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_AH: name="ah"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_BX: name="bx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_EBX: name="ebx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_BL: name="bl"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_BH: name="bh"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_CX: name="cx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_ECX: name="ecx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_CL: name="cl"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_CH: name="ch"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_DX: name="dx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_EDX: name="edx"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_DL: name="dl"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_DH: name="dh"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_SI: name="si"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_ESI: name="esi"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_DI: name="di"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_EDI: name="edi"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_SP: name="sp"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_ESP: name="esp"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_BP: name="bp"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_EBP: name="ebp"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_CS: name="cs"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_DS: name="ds"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_SS: name="ss"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_ES: name="es"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_FS: name="fs"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_GS: name="gs"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_FLAGS: case LOAD1_EFLAGS: name="flags"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_IB: case LOAD1_IW: case LOAD1_ID: name="immediate"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 			case LOAD1_MEM_BYTE: case LOAD1_MEM_WORD: case LOAD1_MEM_DOUBLE: name="memory["+addressString.substring(0,addressString.length()-1)+"]"; push(GUICODE.DECODE_INPUT_OPERAND_1,name,reg1); break;
 
 			case STORE0_AX: name="ax"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_EAX: name="eax"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_AL: name="al"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_AH: name="ah"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_BX: name="bx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_EBX: name="ebx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_BL: name="bl"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_BH: name="bh"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CX: name="cx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_ECX: name="ecx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CL: name="cl"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CH: name="ch"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_DX: name="dx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_EDX: name="edx"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_DL: name="dl"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_DH: name="dh"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_SI: name="si"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_ESI: name="esi"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_DI: name="di"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_EDI: name="edi"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_SP: name="sp"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_ESP: name="esp"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_BP: name="bp"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_EBP: name="ebp"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CS: name="cs"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_DS: name="ds"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_SS: name="ss"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_ES: name="es"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_FS: name="fs"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_GS: name="gs"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_FLAGS: case STORE0_EFLAGS: name="flags"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CR0: name="cr0"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CR2: name="cr2"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CR3: name="cr3"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_CR4: name="cr4"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			case STORE0_MEM_BYTE: case STORE0_MEM_WORD: case STORE0_MEM_DOUBLE: name="memory["+addressString.substring(0,addressString.length()-1)+"]"; push(GUICODE.DECODE_OUTPUT_OPERAND_0,name,reg0); break;
 			
 			case STORE1_AX: name="ax"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_EAX: name="eax"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_AL: name="al"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_AH: name="ah"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_BX: name="bx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_EBX: name="ebx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_BL: name="bl"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_BH: name="bh"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_CX: name="cx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_ECX: name="ecx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_CL: name="cl"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_CH: name="ch"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_DX: name="dx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_EDX: name="edx"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_DL: name="dl"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_DH: name="dh"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_SI: name="si"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_ESI: name="esi"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_DI: name="di"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_EDI: name="edi"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_SP: name="sp"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_ESP: name="esp"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_BP: name="bp"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_EBP: name="ebp"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_CS: name="cs"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_DS: name="ds"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_SS: name="ss"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_ES: name="es"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_FS: name="fs"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_GS: name="gs"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_FLAGS: case STORE1_EFLAGS: name="flags"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 			case STORE1_MEM_BYTE: case STORE1_MEM_WORD: case STORE1_MEM_DOUBLE: name="memory["+addressString.substring(0,addressString.length()-1)+"]"; push(GUICODE.DECODE_OUTPUT_OPERAND_1,name,reg1); break;
 
 
 			case OP_JMP_FAR: 
 			case OP_JMP_ABS:
 				push(GUICODE.EXECUTE_JUMP_ABSOLUTE,reg0); break;
 			case OP_JMP_08: 
 			case OP_JMP_16_32: 
 				push(GUICODE.EXECUTE_JUMP,reg0); break;
 			case OP_CALL_FAR:
 			case OP_CALL_ABS:
 				push(GUICODE.EXECUTE_CALL_STACK,reg0); push(GUICODE.EXECUTE_JUMP_ABSOLUTE,reg0); break;
 			case OP_CALL:
 				push(GUICODE.EXECUTE_CALL_STACK,reg0); push(GUICODE.EXECUTE_JUMP,reg0); break;
 			case OP_RET:
 			case OP_RET_IW:
 			case OP_RET_FAR:
 			case OP_RET_FAR_IW:
 				push(GUICODE.EXECUTE_RETURN); break;
 
 			case OP_JO:
 			case OP_JO_16_32:
 			case OP_JNO:
 			case OP_JNO_16_32:
 			case OP_JC:
 			case OP_JC_16_32:
 			case OP_JNC:
 			case OP_JNC_16_32:
 			case OP_JS:
 			case OP_JS_16_32:
 			case OP_JNS:
 			case OP_JNS_16_32:
 			case OP_JZ:
 			case OP_JZ_16_32:
 			case OP_JNZ:
 			case OP_JNZ_16_32:
 			case OP_JP:
 			case OP_JP_16_32:
 			case OP_JNP:
 			case OP_JNP_16_32:
 			case OP_JA:
 			case OP_JA_16_32:
 			case OP_JNA:
 			case OP_JNA_16_32:
 			case OP_JL:
 			case OP_JL_16_32:
 			case OP_JNL:
 			case OP_JNL_16_32:
 			case OP_JG:
 			case OP_JG_16_32:
 			case OP_JNG:
 			case OP_JNG_16_32:
 			case OP_LOOP_CX: 
 			case OP_LOOPZ_CX: 
 			case OP_LOOPNZ_CX:
 			case OP_JCXZ:
  
 				push(GUICODE.EXECUTE_CONDITION);
 				if(condition)
 					push(GUICODE.EXECUTE_JUMP,reg0);
 				break;
 
 			case OP_IN_08:
 			case OP_IN_16_32:
 			case OP_INSB:
 			case OP_INSW:
 			case OP_REP_INSB:
 			case OP_REP_INSW:
 				push(GUICODE.EXECUTE_PORT_READ); break;
 			case OP_OUT_08:
 			case OP_OUT_16_32:
 			case OP_OUTSB:
 			case OP_OUTSW:
 			case OP_REP_OUTSB:
 			case OP_REP_OUTSW:
 				push(GUICODE.EXECUTE_PORT_WRITE); break;
 
 			case OP_INT:
 			case OP_INT3:
 				push(GUICODE.EXECUTE_INTERRUPT,reg0); break;
 
 			case OP_IRET:
 				push(GUICODE.EXECUTE_INTERRUPT_RETURN); break;
 
 			case OP_INC:
 			case OP_DEC:
 			case OP_NOT:
 			case OP_NEG:
 			case OP_SHRD_16_32:
 			case OP_AAA:
 			case OP_AAD:
 			case OP_AAM:
 			case OP_AAS:
 			case OP_CWD:
 			case OP_DAA:
 			case OP_DAS:
 			case OP_BOUND:
 			case OP_SIGN_EXTEND:
 			case OP_SIGN_EXTEND_8_16:
 			case OP_SIGN_EXTEND_8_32:
 			case OP_SIGN_EXTEND_16_32:
 				push(GUICODE.EXECUTE_ARITHMETIC_1_1); push(GUICODE.EXECUTE_FLAG); break;
 
 			case OP_ADD:
 			case OP_ADC:
 			case OP_SUB:
 			case OP_CMP:
 			case OP_SBB:
 			case OP_AND:
 			case OP_TEST:
 			case OP_OR:
 			case OP_XOR:
 			case OP_ROL_08:
 			case OP_ROL_16_32:
 			case OP_ROR_08:
 			case OP_ROR_16_32:
 			case OP_RCL_08:
 			case OP_RCL_16_32:
 			case OP_RCR_08:
 			case OP_RCR_16_32:
 			case OP_SHL:
 			case OP_SHR:
 			case OP_SAR_08:
 			case OP_SAR_16_32:
 			case OP_MUL_08:
 			case OP_MUL_16_32:
 			case OP_IMULA_08:
 			case OP_IMULA_16_32:
 			case OP_IMUL_16_32:
 			case OP_DIV_08:
 			case OP_DIV_16_32:
 			case OP_IDIV_08:
 			case OP_IDIV_16_32:
 			case OP_SHLD_16_32:
 			case OP_BT_MEM:
 			case OP_BTS_MEM:
 			case OP_BTR_MEM:
 			case OP_BTC_MEM:
 			case OP_BT_16_32:
 			case OP_BTS_16_32:
 			case OP_BTR_16_32:
 			case OP_BTC_16_32:
 				push(GUICODE.EXECUTE_ARITHMETIC_2_1); push(GUICODE.EXECUTE_FLAG); break;
 
 			case OP_SCASB:
 			case OP_SCASW:
 			case OP_REPNE_SCASB:
 			case OP_REPE_SCASB:
 			case OP_REPNE_SCASW:
 			case OP_REPE_SCASW:
 			case OP_CMPSB:
 			case OP_CMPSW:
 			case OP_REPNE_CMPSB:
 			case OP_REPE_CMPSB:
 			case OP_REPNE_CMPSW:
 			case OP_REPE_CMPSW:
 				push(GUICODE.EXECUTE_MEMORY_COMPARE); break;
 
 			case OP_LODSB:
 			case OP_LODSW:
 			case OP_REP_LODSB:
 			case OP_REP_LODSW:
 			case OP_STOSB:
 			case OP_STOSW:
 			case OP_REP_STOSB:
 			case OP_REP_STOSW:
 			case OP_MOVSB:
 			case OP_MOVSW:
 			case OP_REP_MOVSB:
 			case OP_REP_MOVSW:
 				push(GUICODE.EXECUTE_MEMORY_TRANSFER); break;
 
 			case OP_CLC:
 			case OP_STC:
 			case OP_CLI:
 			case OP_STI:
 			case OP_CLD:
 			case OP_STD:
 			case OP_CMC:
 			case OP_SETO:
 			case OP_SETNO:
 			case OP_SETC:
 			case OP_SETNC:
 			case OP_SETZ:
 			case OP_SETNZ:
 			case OP_SETA:
 			case OP_SETNA:
 			case OP_SETL:
 			case OP_SETNL:
 			case OP_SETG:
 			case OP_SETNG:
 			case OP_SETS:
 			case OP_SETNS:
 			case OP_SETP:
 			case OP_SETNP:
 			case OP_SALC:
 			case OP_LAHF:
 			case OP_SAHF:
 				push(GUICODE.EXECUTE_FLAG); break;
 
 			case OP_CMOVO:
 			case OP_CMOVNO:
 			case OP_CMOVC:
 			case OP_CMOVNC:
 			case OP_CMOVZ:
 			case OP_CMOVNZ:
 			case OP_CMOVP:
 			case OP_CMOVNP:
 			case OP_CMOVS:
 			case OP_CMOVNS:
 			case OP_CMOVL:
 			case OP_CMOVNL:
 			case OP_CMOVG:
 			case OP_CMOVNG:
 			case OP_CMOVA:
 			case OP_CMOVNA:
 				push(GUICODE.EXECUTE_CONDITION);
 				if(condition)
 					push(GUICODE.EXECUTE_TRANSFER);
 				break;
 
 			case OP_POP:
 			case OP_POPF:
 			case OP_POPA:
 			case OP_LEAVE:
 				push(GUICODE.EXECUTE_POP);
 				break;
 			case OP_PUSH:
 			case OP_PUSHF:
 			case OP_PUSHA:
 			case OP_ENTER:
 				push(GUICODE.EXECUTE_PUSH);
 				break;
 
 			case OP_LGDT:
 			case OP_LIDT:
 			case OP_LLDT:
 			case OP_LMSW:
 			case OP_LTR:
 			case OP_SGDT:
 			case OP_SIDT:
 			case OP_SLDT:
 			case OP_SMSW:
 			case OP_STR:
 			case OP_CPUID:
 			case OP_RDTSC:
 				push(GUICODE.EXECUTE_TRANSFER);
 				break;
 
 			case OP_HALT:
 				push(GUICODE.EXECUTE_HALT);
 				break;
 		}
 	}
 }
 
 enum MICROCODE 
 {
 PREFIX_LOCK, PREFIX_REPNE, PREFIX_REPE, PREFIX_CS, PREFIX_SS, PREFIX_DS, PREFIX_ES, PREFIX_FS, PREFIX_GS, PREFIX_OPCODE_32BIT, PREFIX_ADDRESS_32BIT,
 
 LOAD0_AX, LOAD0_AL, LOAD0_AH, LOAD0_BX, LOAD0_BL, LOAD0_BH, LOAD0_CX, LOAD0_CL, LOAD0_CH, LOAD0_DX, LOAD0_DL, LOAD0_DH, LOAD0_SP, LOAD0_BP, LOAD0_SI, LOAD0_DI, LOAD0_CS, LOAD0_SS, LOAD0_DS, LOAD0_ES, LOAD0_FS, LOAD0_GS, LOAD0_FLAGS, LOAD1_AX, LOAD1_AL, LOAD1_AH, LOAD1_BX, LOAD1_BL, LOAD1_BH, LOAD1_CX, LOAD1_CL, LOAD1_CH, LOAD1_DX, LOAD1_DL, LOAD1_DH, LOAD1_SP, LOAD1_BP, LOAD1_SI, LOAD1_DI, LOAD1_CS, LOAD1_SS, LOAD1_DS, LOAD1_ES, LOAD1_FS, LOAD1_GS, LOAD1_FLAGS, STORE0_AX, STORE0_AL, STORE0_AH, STORE0_BX, STORE0_BL, STORE0_BH, STORE0_CX, STORE0_CL, STORE0_CH, STORE0_DX, STORE0_DL, STORE0_DH, STORE0_SP, STORE0_BP, STORE0_SI, STORE0_DI, STORE0_CS, STORE0_SS, STORE0_DS, STORE0_ES, STORE0_FS, STORE0_GS, STORE1_AX, STORE1_AL, STORE1_AH, STORE1_BX, STORE1_BL, STORE1_BH, STORE1_CX, STORE1_CL, STORE1_CH, STORE1_DX, STORE1_DL, STORE1_DH, STORE1_SP, STORE1_BP, STORE1_SI, STORE1_DI, STORE1_CS, STORE1_SS, STORE1_DS, STORE1_ES, STORE1_FS, STORE1_GS, STORE1_FLAGS, LOAD0_MEM_BYTE, LOAD0_MEM_WORD, STORE0_MEM_BYTE, STORE0_MEM_WORD, LOAD1_MEM_BYTE, LOAD1_MEM_WORD, STORE1_MEM_BYTE, STORE1_MEM_WORD, STORE1_ESP, STORE0_FLAGS, LOAD0_IB, LOAD0_IW, LOAD1_IB, LOAD1_IW, LOAD_SEG_CS, LOAD_SEG_SS, LOAD_SEG_DS, LOAD_SEG_ES, LOAD_SEG_FS, LOAD_SEG_GS, LOAD0_ADDR, LOAD0_EAX, LOAD0_EBX, LOAD0_ECX, LOAD0_EDX, LOAD0_ESI, LOAD0_EDI, LOAD0_EBP, LOAD0_ESP, LOAD1_EAX, LOAD1_EBX, LOAD1_ECX, LOAD1_EDX, LOAD1_ESI, LOAD1_EDI, LOAD1_EBP, LOAD1_ESP, STORE0_EAX, STORE0_EBX, STORE0_ECX, STORE0_EDX, STORE0_ESI, STORE0_EDI, STORE0_ESP, STORE0_EBP, STORE1_EAX, STORE1_EBX, STORE1_ECX, STORE1_EDX, STORE1_ESI, STORE1_EDI, STORE1_EBP, LOAD0_MEM_DOUBLE, LOAD1_MEM_DOUBLE, STORE0_MEM_DOUBLE, STORE1_MEM_DOUBLE, LOAD0_EFLAGS, LOAD1_EFLAGS, STORE0_EFLAGS, STORE1_EFLAGS, LOAD0_ID, LOAD1_ID, LOAD0_CR0, LOAD0_CR2, LOAD0_CR3, LOAD0_CR4, STORE0_CR0, STORE0_CR2, STORE0_CR3, STORE0_CR4,
 
 LOAD2_AL, LOAD2_CL, LOAD2_AX, LOAD2_EAX, LOAD2_IB,
 
 FLAG_BITWISE_08, FLAG_BITWISE_16, FLAG_BITWISE_32, FLAG_SUB_08, FLAG_SUB_16, FLAG_SUB_32, FLAG_REP_SUB_08, FLAG_REP_SUB_16, FLAG_REP_SUB_32, FLAG_ADD_08, FLAG_ADD_16, FLAG_ADD_32, FLAG_ADC_08, FLAG_ADC_16, FLAG_ADC_32, FLAG_SBB_08, FLAG_SBB_16, FLAG_SBB_32, FLAG_DEC_08, FLAG_DEC_16, FLAG_DEC_32, FLAG_INC_08, FLAG_INC_16, FLAG_INC_32, FLAG_SHL_08, FLAG_SHL_16, FLAG_SHL_32, FLAG_SHR_08, FLAG_SHR_16, FLAG_SHR_32, FLAG_SAR_08, FLAG_SAR_16, FLAG_SAR_32, FLAG_RCL_08, FLAG_RCL_16, FLAG_RCL_32, FLAG_RCR_08, FLAG_RCR_16, FLAG_RCR_32, FLAG_ROL_08, FLAG_ROL_16, FLAG_ROL_32, FLAG_ROR_08, FLAG_ROR_16, FLAG_ROR_32, FLAG_NEG_08, FLAG_NEG_16, FLAG_NEG_32, FLAG_ROTATE_08, FLAG_ROTATE_16, FLAG_ROTATE_32, FLAG_UNIMPLEMENTED, FLAG_8F, FLAG_NONE, FLAG_BAD, FLAG_80_82, FLAG_81_83, FLAG_FF, FLAG_F6, FLAG_F7, FLAG_FE, FLAG_FLOAT_NOP,
 
 ADDR_AX, ADDR_BX, ADDR_CX, ADDR_DX, ADDR_SP, ADDR_BP, ADDR_SI, ADDR_DI, ADDR_IB, ADDR_IW, ADDR_AL, ADDR_EAX, ADDR_EBX, ADDR_ECX, ADDR_EDX, ADDR_ESP, ADDR_EBP, ADDR_ESI, ADDR_EDI, ADDR_ID, ADDR_MASK_16, ADDR_2EAX, ADDR_2EBX, ADDR_2ECX, ADDR_2EDX, ADDR_2EBP, ADDR_2ESI, ADDR_2EDI, ADDR_4EAX, ADDR_4EBX, ADDR_4ECX, ADDR_4EDX, ADDR_4EBP, ADDR_4ESI, ADDR_4EDI, ADDR_8EAX, ADDR_8EBX, ADDR_8ECX, ADDR_8EDX, ADDR_8EBP, ADDR_8ESI, ADDR_8EDI,
 
 OP_JMP_FAR, OP_JMP_ABS, OP_CALL, OP_CALL_FAR, OP_CALL_ABS, OP_RET, OP_RET_IW, OP_RET_FAR, OP_RET_FAR_IW, OP_ENTER, OP_LEAVE, OP_JMP_08, OP_JMP_16_32, OP_JO, OP_JO_16_32, OP_JNO, OP_JNO_16_32, OP_JC, OP_JC_16_32, OP_JNC, OP_JNC_16_32, OP_JZ, OP_JZ_16_32, OP_JNZ, OP_JNZ_16_32, OP_JS, OP_JS_16_32, OP_JNS, OP_JNS_16_32, OP_JP, OP_JP_16_32, OP_JNP, OP_JNP_16_32, OP_JA, OP_JA_16_32, OP_JNA, OP_JNA_16_32, OP_JL, OP_JL_16_32, OP_JNL, OP_JNL_16_32, OP_JG, OP_JG_16_32, OP_JNG, OP_JNG_16_32, OP_INT, OP_INT3, OP_IRET, OP_IN_08, OP_IN_16_32, OP_OUT_08, OP_OUT_16_32, OP_INC, OP_DEC, OP_ADD, OP_ADC, OP_SUB, OP_SBB, OP_AND, OP_TEST, OP_OR, OP_XOR, OP_ROL_08, OP_ROL_16_32, OP_ROR_08, OP_ROR_16_32, OP_RCL_08, OP_RCL_16_32, OP_RCR_08, OP_RCR_16_32, OP_SHR, OP_SAR_08, OP_SAR_16_32, OP_NOT, OP_NEG, OP_MUL_08, OP_MUL_16_32, OP_IMULA_08, OP_IMULA_16_32, OP_IMUL_16_32, OP_BSF, OP_BSR, OP_BT_MEM, OP_BTS_MEM, OP_BTR_MEM, OP_BTC_MEM, OP_BT_16_32, OP_BTS_16_32, OP_BTR_16_32, OP_BTC_16_32, OP_SHLD_16_32, OP_SHRD_16_32, OP_CWD, OP_CDQ, OP_AAA, OP_AAD, OP_AAM, OP_AAS, OP_DAA, OP_DAS, OP_BOUND, OP_CLC, OP_STC, OP_CLI, OP_STI, OP_CLD, OP_STD, OP_CMC, OP_SETO, OP_SETNO, OP_SETC, OP_SETNC, OP_SETZ, OP_SETNZ, OP_SETA, OP_SETNA, OP_SETS, OP_SETNS, OP_SETP, OP_SETNP, OP_SETL, OP_SETNL, OP_SETG, OP_SETNG, OP_SALC, OP_CMOVO, OP_CMOVNO, OP_CMOVC, OP_CMOVNC, OP_CMOVZ, OP_CMOVNZ, OP_CMOVS, OP_CMOVNS, OP_CMOVP, OP_CMOVNP, OP_CMOVA, OP_CMOVNA, OP_CMOVL, OP_CMOVNL, OP_CMOVG, OP_CMOVNG, OP_LAHF, OP_SAHF, OP_POP, OP_POPF, OP_PUSH, OP_PUSHA, OP_POPA, OP_SIGN_EXTEND_8_16, OP_SIGN_EXTEND_8_32, OP_SIGN_EXTEND_16_32, OP_SIGN_EXTEND, OP_INSB, OP_INSW, OP_REP_INSB, OP_REP_INSW, OP_LODSB, OP_LODSW, OP_REP_LODSB, OP_REP_LODSW, OP_MOVSB, OP_MOVSW, OP_REP_MOVSB, OP_REP_MOVSW, OP_OUTSB, OP_OUTSW, OP_REP_OUTSB, OP_REP_OUTSW, OP_STOSB, OP_STOSW, OP_REP_STOSB, OP_REP_STOSW, OP_LOOP_CX, OP_LOOPZ_CX, OP_LOOPNZ_CX, OP_JCXZ, OP_HALT, OP_CPUID, OP_NOP, OP_MOV, OP_CMP, OP_BAD, OP_UNIMPLEMENTED, OP_ROTATE_08, OP_ROTATE_16_32, OP_INT0, OP_CBW, OP_PUSHF, OP_SHL, OP_80_83, OP_FF, OP_F6, OP_F7, OP_DIV_08, OP_DIV_16_32, OP_IDIV_08, OP_IDIV_16_32, OP_SCASB, OP_SCASW, OP_REPE_SCASB, OP_REPE_SCASW, OP_REPNE_SCASB, OP_REPNE_SCASW, OP_CMPSB, OP_CMPSW, OP_REPE_CMPSB, OP_REPE_CMPSW, OP_REPNE_CMPSB, OP_REPNE_CMPSW, OP_FE, OP_FLOAT_NOP, OP_SGDT, OP_SIDT, OP_LGDT, OP_LIDT, OP_SMSW, OP_LMSW, OP_F01, OP_F00, OP_SLDT, OP_STR, OP_LLDT, OP_LTR, OP_VERR, OP_VERW, OP_CLTS, OP_RDTSC
 }
 enum GUICODE
 {
 //processor action codes
 FETCH, DECODE_PREFIX, DECODE_INSTRUCTION, DECODE_OPCODE, DECODE_MODRM, DECODE_SIB, DECODE_INPUT_OPERAND_0, DECODE_INPUT_OPERAND_1, DECODE_INPUT_OPERAND_2, DECODE_OUTPUT_OPERAND_0, DECODE_OUTPUT_OPERAND_1, DECODE_IMMEDIATE, DECODE_DISPLACEMENT, HARDWARE_INTERRUPT, SOFTWARE_INTERRUPT, EXCEPTION, DECODE_MEMORY_ADDRESS, DECODE_SEGMENT_ADDRESS,
 
 EXECUTE_JUMP, EXECUTE_JUMP_ABSOLUTE, EXECUTE_RETURN, EXECUTE_CALL_STACK, EXECUTE_CONDITION, EXECUTE_PORT_READ, EXECUTE_PORT_WRITE, EXECUTE_INTERRUPT, EXECUTE_INTERRUPT_RETURN, EXECUTE_ARITHMETIC_1_1, EXECUTE_ARITHMETIC_2_1, EXECUTE_MEMORY_COMPARE, EXECUTE_MEMORY_TRANSFER, EXECUTE_FLAG, EXECUTE_TRANSFER, EXECUTE_PUSH, EXECUTE_POP, EXECUTE_HALT,
 
 //mode codes
 MODE_REAL, MODE_PROTECTED,
 
 //storage codes
 FLAG_SET, FLAG_CLEAR, FLAG_READ, REGISTER_READ, REGISTER_WRITE, PORT_READ, PORT_WRITE, CS_MEMORY_READ, CS_MEMORY_WRITE, SS_MEMORY_READ, SS_MEMORY_WRITE, DS_MEMORY_READ, DS_MEMORY_WRITE, ES_MEMORY_READ, ES_MEMORY_WRITE, FS_MEMORY_READ, FS_MEMORY_WRITE, GS_MEMORY_READ, GS_MEMORY_WRITE, IDTR_MEMORY_READ, IDTR_MEMORY_WRITE, GDTR_MEMORY_READ, GDTR_MEMORY_WRITE, LDTR_MEMORY_READ, LDTR_MEMORY_WRITE, TSS_MEMORY_READ, TSS_MEMORY_WRITE,
 };
 
 //decode tables
 
 
 public static final MICROCODE[] rotationTable = new MICROCODE[]
 {
 MICROCODE.FLAG_ROL_08, MICROCODE.FLAG_ROR_08, MICROCODE.FLAG_RCL_08, MICROCODE.FLAG_RCR_08, MICROCODE.FLAG_SHL_08, MICROCODE.FLAG_SHR_08, MICROCODE.FLAG_SHL_08, MICROCODE.FLAG_SAR_08,
 MICROCODE.FLAG_ROL_16, MICROCODE.FLAG_ROR_16, MICROCODE.FLAG_RCL_16, MICROCODE.FLAG_RCR_16, MICROCODE.FLAG_SHL_16, MICROCODE.FLAG_SHR_16, MICROCODE.FLAG_SHL_16, MICROCODE.FLAG_SAR_16,
 MICROCODE.OP_ROL_08, MICROCODE.OP_ROR_08, MICROCODE.OP_RCL_08, MICROCODE.OP_RCR_08, MICROCODE.OP_SHL, MICROCODE.OP_SHR, MICROCODE.OP_SHL, MICROCODE.OP_SAR_08,
 MICROCODE.OP_ROL_16_32, MICROCODE.OP_ROR_16_32, MICROCODE.OP_RCL_16_32, MICROCODE.OP_RCR_16_32, MICROCODE.OP_SHL, MICROCODE.OP_SHR, MICROCODE.OP_SHL, MICROCODE.OP_SAR_16_32
 };
 
 public static final int[][] inputTable0 = new int[][]
 {
 //effective byte
 {0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x38, 0x84, 0x86, 0x8a, 0xfb6, 0xfbe, 0x80, 0x82, 0xc0, 0xfb0, 0xd0, 0xd2, 0xf6, 0xfe},
 //effective word
 {0x01, 0x09, 0x11, 0x19, 0x21, 0x29, 0x31, 0x39, 0x85, 0x87, 0x8b, 0x81, 0xc1, 0x83, 0x69, 0x6b, 0xc4, 0xc5, 0xfb2, 0xfb4, 0xfb5, 0xfa4, 0xfac, 0xfa5, 0xfad, 0xfb1, 0xd1, 0xd3, 0xfb1, 0xff, 0xf7},
 //register byte
 {0x88, 0x02, 0x0a, 0x12, 0x1a, 0x22, 0x2a, 0x32, 0x3a, 0xfc0},
 //register word
 {0x89, 0x03, 0x0b, 0x13, 0x1b, 0x23, 0x2b, 0x33, 0x3b, 0xfaf, 0xfbc, 0xfbd, 0xfc1, 0xf40, 0xf41, 0xf42, 0xf43, 0xf44, 0xf45, 0xf46, 0xf47, 0xf48, 0xf49, 0xf4a, 0xf4b, 0xf4c, 0xf4d, 0xf4e, 0xf4f},
 //immediate byte
 {0xc6, 0xb0, 0xb1, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xe4, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f, 0xcd, 0xd4, 0xd5, 0xe0, 0xe1, 0xe2, 0xe3, 0xeb, 0xe5, 0xe6, 0xe7},
 //immediate word
 {0xc7, 0x68, 0x6a, 0xe8, 0xe9, 0xf80, 0xf81, 0xf82, 0xf83, 0xf84, 0xf85, 0xf86, 0xf87, 0xf88, 0xf89, 0xf8a, 0xf8b, 0xf8c, 0xf8d, 0xf8e, 0xf8f, 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf},
 //AX
 {0x05, 0x0d, 0x15, 0x1d, 0x25, 0x2d, 0x35, 0x3d, 0xa9, 0x40, 0x48, 0x50, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0xa3, 0xab, 0xaf},
 //AL
 {0x04, 0x0c, 0x14, 0x1c, 0x24, 0x2c, 0x34, 0x3c, 0xa8, 0xa2, 0xaa, 0xae},{},
 //BX
 {0x43, 0x4b, 0x53},{},{},
 //CX
 {0x41, 0x49, 0x51, 0xf30, 0xf32},{},{},
 //DX
 {0x42, 0x4a, 0x52},{},{},
 //SP
 {0x44, 0x4c, 0x54},
 //BP
 {0x45, 0x4d, 0x55},
 //SI
 {0x46, 0x4e, 0x56},
 //DI
 {0x47, 0x4f, 0x57},
 //CS
 {0x0e},
 //SS
 {0x16},
 //DS
 {0x1e},
 //ES
 {0x06},
 //FS
 {0xfa0},
 //GS
 {0xfa8},
 //flags
 {0x9c},
 //special
 {0x9a, 0xea, 0xc8, 0x62, 0x8c, 0xa0, 0xa1, 0xa4, 0xa5, 0xa6, 0xa7, 0xac, 0xad, 0xfa3, 0xfab, 0xfb3, 0xfbb, 0x6e, 0x6f, 0xd7, 0xf00, 0xf01, 0xf20, 0xf21, 0xf22, 0xf23, 0xfba, 0xfc8, 0xfc9, 0xfca, 0xfcb, 0xfcc, 0xfcd, 0xfce, 0xfcf, 0x8d, 0xec, 0xed, 0xee, 0xef, 0x8e, 0xfb7, 0xfbf, 0x6c, 0x6d, 0xca, 0xc2}
 };
 
 public static final int[][] inputTable1 = new int[][]
 {
 //effective byte
 {0x02, 0x0a, 0x12, 0x1a, 0x22, 0x2a, 0x32, 0x3a, 0xfc0},
 //effective word
 {0x03, 0x0b, 0x13, 0x1b, 0x23, 0x2b, 0x33, 0x3b, 0xfaf, 0xfbc, 0xfbd, 0xfc1, 0xf40, 0xf41, 0xf42, 0xf43, 0xf44, 0xf45, 0xf46, 0xf47, 0xf48, 0xf49, 0xf4a, 0xf4b, 0xf4c, 0xf4d, 0xf4e, 0xf4f},
 //register byte
 {0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x38, 0x84, 0x86, 0xfb0},
 //register word
 {0x01, 0x09, 0x11, 0x19, 0x21, 0x29, 0x31, 0x39, 0x85, 0x87, 0x62, 0xfa4, 0xfac, 0xfa5, 0xfad, 0xfb1},
 //immediate byte
 {0x04, 0x0c, 0x14, 0x1c, 0x24, 0x2c, 0x34, 0x3c, 0xa8, 0x80, 0x82, 0xc0, 0xc1},
 //immediate word
 {0x69, 0x6b, 0x05, 0x0d, 0x15, 0x1d, 0x25, 0x2d, 0x35, 0x3d, 0xa9, 0x81, 0x83},
 //AX
 {0xef, 0xe7},
 //AL
 {0xee, 0xe6},{},
 //BX
 {0x93},{},{},
 //CX
 {0x91},
 //CL
 {0xd2, 0xd3},{},
 //DX
 {0x92, 0xf30},{},{},
 //SP
 {0x94},
 //BP
 {0x95},
 //SI
 {0x96},
 //DI
 {0x97},{},{},{},{},{},{},{},
 //special
 {0x9a, 0xea, 0xc8, 0xf6, 0xf7, 0x62, 0xc4, 0xc5, 0xfb2, 0xfb4, 0xfb5, 0xfba, 0xff, 0xd1, 0xd0, 0xf01, 0xfa3, 0xfab, 0xfb3, 0xfbb}
 };
 
 
 public static final int[][] outputTable0 = new int[][]
 {
 //effective byte
 {0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x88, 0xc0, 0xc6, 0xfe, 0xf90, 0xf91, 0xf92, 0xf93, 0xf94, 0xf95, 0xf96, 0xf97, 0xf98, 0xf99, 0xf9a, 0xf9b, 0xf9c, 0xf9d, 0xf9e, 0xf9f, 0xfb0, 0xfc0, 0xd0, 0xd2},
 //effective word
 {0x01, 0x09, 0x11, 0x19, 0x21, 0x29, 0x31, 0x89, 0x21, 0x29, 0x31, 0x89, 0xc7, 0xc1, 0x8f, 0xd1, 0xd3, 0x8c, 0xfa4, 0xfa5, 0xfac, 0xfad, 0xfc1, 0xfb1},
 //register byte
 {0x86, 0x02, 0x0a, 0x12, 0x1a, 0x22, 0x2a, 0x32, 0x8a},
 //register word
 {0x87, 0x03, 0x0b, 0x13, 0x1b, 0x23, 0x2b, 0x33, 0x69, 0x6b, 0x8b, 0x8d, 0xf40, 0xf41, 0xf42, 0xf43, 0xf44, 0xf45, 0xf46, 0xf47, 0xf48, 0xf49, 0xf4a, 0xf4b, 0xf4c, 0xf4d, 0xf4e, 0xf4f, 0xfaf, 0xfb6, 0xfb7, 0xfbc, 0xfbd, 0xfbe, 0xfbf, 0xfb2, 0xfb4, 0xfb5, 0x62, 0xc4, 0xc5},{},{},
 //AX
 {0x05, 0x0d, 0x15, 0x1d, 0x25, 0x2d, 0x35, 0xb8, 0xe5, 0x40, 0x48, 0x58, 0xed, 0xa1},
 //AL
 {0xec, 0x04, 0x0c, 0x14, 0x1c, 0x24, 0x2c, 0x34, 0xe4, 0xb0, 0xa0, 0xd6, 0xd7},
 //AH
 {0xb4},
 //BX
 {0x43, 0x4b, 0x5b, 0xbb, 0x93},
 //BL
 {0xb3},
 //BH
 {0xb7},
 //CX
 {0x41, 0x49, 0x59, 0xb9, 0x91},
 //CL
 {0xb1},
 //CH
 {0xb5},
 //DX
 {0x42, 0x4a, 0x5a, 0xba, 0x92},
 //DL
 {0xb2},
 //DH
 {0xb6},
 //SP
 {0x44, 0x4c, 0x5c, 0xbc, 0x94},
 //BP
 {0x45, 0x4d, 0x5d, 0xbd, 0x95},
 //SI
 {0x46, 0x4e, 0x5e, 0xbe, 0x96},
 //DI
 {0x47, 0x4f, 0x5f, 0xbf, 0x97},{},
 //SS
 {0x17},
 //DS
 {0x1f},
 //ES
 {0x07},
 //FS
 {0xfa1},
 //GS
 {0xfa9},
 //flags
 {0x9d},
 //special
 {0x8e, 0xa2, 0xa3, 0xd0, 0xd1, 0xf6, 0xf7, 0xff, 0xf00, 0xf01, 0xf20, 0xf21, 0xf22, 0xf23, 0xf31, 0xf32, 0xfab, 0xfb3, 0xfbb, 0xfba, 0xfc8, 0xfc9, 0xfca, 0xfcb, 0xfcc, 0xfcd, 0xfce, 0xfcf, 0x80, 0x81, 0x82, 0x83}
 };
 
 public static final int[][] outputTable1 = new int[][]
 {
 //effective byte
 {0x86},
 //effective word
 {0x87},
 {},{},{},{},
 //AX
 {0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97},
 {},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},
 //SS
 {0xfb2},
 //DS
 {0xc5},
 //ES
 {0xc4},
 //FS
 {0xfb4},
 //GS
 {0xfb5},{},
 //special
 {0xf01, 0xf31, 0xf32}
 };
 
 public static final MICROCODE[] operandRegisterTable = new MICROCODE[]
 {
 MICROCODE.LOAD0_AX, MICROCODE.LOAD0_AL, MICROCODE.LOAD0_AH, MICROCODE.LOAD0_BX, MICROCODE.LOAD0_BL, MICROCODE.LOAD0_BH, MICROCODE.LOAD0_CX, MICROCODE.LOAD0_CL, MICROCODE.LOAD0_CH, MICROCODE.LOAD0_DX, MICROCODE.LOAD0_DL, MICROCODE.LOAD0_DH, MICROCODE.LOAD0_SP, MICROCODE.LOAD0_BP, MICROCODE.LOAD0_SI, MICROCODE.LOAD0_DI, MICROCODE.LOAD0_CS, MICROCODE.LOAD0_SS, MICROCODE.LOAD0_DS, MICROCODE.LOAD0_ES, MICROCODE.LOAD0_FS, MICROCODE.LOAD0_GS, MICROCODE.LOAD0_FLAGS, MICROCODE.LOAD1_AX, MICROCODE.LOAD1_AL, MICROCODE.LOAD1_AH, MICROCODE.LOAD1_BX, MICROCODE.LOAD1_BL, MICROCODE.LOAD1_BH, MICROCODE.LOAD1_CX, MICROCODE.LOAD1_CL, MICROCODE.LOAD1_CH, MICROCODE.LOAD1_DX, MICROCODE.LOAD1_DL, MICROCODE.LOAD1_DH, MICROCODE.LOAD1_SP, MICROCODE.LOAD1_BP, MICROCODE.LOAD1_SI, MICROCODE.LOAD1_DI, MICROCODE.LOAD1_CS, MICROCODE.LOAD1_SS, MICROCODE.LOAD1_DS, MICROCODE.LOAD1_ES, MICROCODE.LOAD1_FS, MICROCODE.LOAD1_GS, MICROCODE.LOAD1_FLAGS, MICROCODE.STORE0_AX, MICROCODE.STORE0_AL, MICROCODE.STORE0_AH, MICROCODE.STORE0_BX, MICROCODE.STORE0_BL, MICROCODE.STORE0_BH, MICROCODE.STORE0_CX, MICROCODE.STORE0_CL, MICROCODE.STORE0_CH, MICROCODE.STORE0_DX, MICROCODE.STORE0_DL, MICROCODE.STORE0_DH, MICROCODE.STORE0_SP, MICROCODE.STORE0_BP, MICROCODE.STORE0_SI, MICROCODE.STORE0_DI, MICROCODE.STORE0_CS, MICROCODE.STORE0_SS, MICROCODE.STORE0_DS, MICROCODE.STORE0_ES, MICROCODE.STORE0_FS, MICROCODE.STORE0_GS, MICROCODE.STORE0_FLAGS, MICROCODE.STORE1_AX, MICROCODE.STORE1_AL, MICROCODE.STORE1_AH, MICROCODE.STORE1_BX, MICROCODE.STORE1_BL, MICROCODE.STORE1_BH, MICROCODE.STORE1_CX, MICROCODE.STORE1_CL, MICROCODE.STORE1_CH, MICROCODE.STORE1_DX, MICROCODE.STORE1_DL, MICROCODE.STORE1_DH, MICROCODE.STORE1_SP, MICROCODE.STORE1_BP, MICROCODE.STORE1_SI, MICROCODE.STORE1_DI, MICROCODE.STORE1_CS, MICROCODE.STORE1_SS, MICROCODE.STORE1_DS, MICROCODE.STORE1_ES, MICROCODE.STORE1_FS, MICROCODE.STORE1_GS, MICROCODE.STORE1_FLAGS
 };
 
 public static final MICROCODE[] modrmRegisterTable = new MICROCODE[]
 {
 MICROCODE.LOAD0_AL, MICROCODE.LOAD0_CL, MICROCODE.LOAD0_DL, MICROCODE.LOAD0_BL, MICROCODE.LOAD0_AH, MICROCODE.LOAD0_CH, MICROCODE.LOAD0_DH, MICROCODE.LOAD0_BH, MICROCODE.LOAD0_MEM_BYTE,
 MICROCODE.LOAD1_AL, MICROCODE.LOAD1_CL, MICROCODE.LOAD1_DL, MICROCODE.LOAD1_BL, MICROCODE.LOAD1_AH, MICROCODE.LOAD1_CH, MICROCODE.LOAD1_DH, MICROCODE.LOAD1_BH, MICROCODE.LOAD1_MEM_BYTE,
 MICROCODE.STORE0_AL, MICROCODE.STORE0_CL, MICROCODE.STORE0_DL, MICROCODE.STORE0_BL, MICROCODE.STORE0_AH, MICROCODE.STORE0_CH, MICROCODE.STORE0_DH, MICROCODE.STORE0_BH, MICROCODE.STORE0_MEM_BYTE,
 MICROCODE.STORE1_AL, MICROCODE.STORE1_CL, MICROCODE.STORE1_DL, MICROCODE.STORE1_BL, MICROCODE.STORE1_AH, MICROCODE.STORE1_CH, MICROCODE.STORE1_DH, MICROCODE.STORE1_BH, MICROCODE.STORE1_MEM_BYTE,
 MICROCODE.LOAD0_AX, MICROCODE.LOAD0_CX, MICROCODE.LOAD0_DX, MICROCODE.LOAD0_BX, MICROCODE.LOAD0_SP, MICROCODE.LOAD0_BP, MICROCODE.LOAD0_SI, MICROCODE.LOAD0_DI, MICROCODE.LOAD0_MEM_WORD,
 MICROCODE.LOAD1_AX, MICROCODE.LOAD1_CX, MICROCODE.LOAD1_DX, MICROCODE.LOAD1_BX, MICROCODE.LOAD1_SP, MICROCODE.LOAD1_BP, MICROCODE.LOAD1_SI, MICROCODE.LOAD1_DI, MICROCODE.LOAD1_MEM_WORD,
 MICROCODE.STORE0_AX, MICROCODE.STORE0_CX, MICROCODE.STORE0_DX, MICROCODE.STORE0_BX, MICROCODE.STORE0_SP, MICROCODE.STORE0_BP, MICROCODE.STORE0_SI, MICROCODE.STORE0_DI, MICROCODE.STORE0_MEM_WORD,
 MICROCODE.STORE1_AX, MICROCODE.STORE1_CX, MICROCODE.STORE1_DX, MICROCODE.STORE1_BX, MICROCODE.STORE1_SP, MICROCODE.STORE1_BP, MICROCODE.STORE1_SI, MICROCODE.STORE1_DI, MICROCODE.STORE1_MEM_WORD,
 MICROCODE.LOAD0_EAX, MICROCODE.LOAD0_ECX, MICROCODE.LOAD0_EDX, MICROCODE.LOAD0_EBX, MICROCODE.LOAD0_ESP, MICROCODE.LOAD0_EBP, MICROCODE.LOAD0_ESI, MICROCODE.LOAD0_EDI, MICROCODE.LOAD0_MEM_DOUBLE,
 MICROCODE.LOAD1_EAX, MICROCODE.LOAD1_ECX, MICROCODE.LOAD1_EDX, MICROCODE.LOAD1_EBX, MICROCODE.LOAD1_ESP, MICROCODE.LOAD1_EBP, MICROCODE.LOAD1_ESI, MICROCODE.LOAD1_EDI, MICROCODE.LOAD1_MEM_DOUBLE,
 MICROCODE.STORE0_EAX, MICROCODE.STORE0_ECX, MICROCODE.STORE0_EDX, MICROCODE.STORE0_EBX, MICROCODE.STORE0_ESP, MICROCODE.STORE0_EBP, MICROCODE.STORE0_ESI, MICROCODE.STORE0_EDI, MICROCODE.STORE0_MEM_DOUBLE,
 MICROCODE.STORE1_EAX, MICROCODE.STORE1_ECX, MICROCODE.STORE1_EDX, MICROCODE.STORE1_EBX, MICROCODE.STORE1_ESP, MICROCODE.STORE1_EBP, MICROCODE.STORE1_ESI, MICROCODE.STORE1_EDI, MICROCODE.STORE1_MEM_DOUBLE
 };
 
 //indexed by the opcode value
 //tells whether the instruction has a displacement, and if so, how many bytes
 //a displacement is an immediate added to the memory address
 public static final int[] hasDisplacementTable = new int[]
 {
 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1,
 
 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1,
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 };
 
 //indexed by the opcode value
 //tells whether the opcode is followed by an immediate, and if so, how many bytes
 public static final int[] hasImmediateTable = new int[]
 {
 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0,
 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0,
 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0,
 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 1, 1, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
 1, 4, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4,
 1, 1, 2, 0, 0, 0, 1, 4, 3, 0, 2, 0, 0, 1, 0, 0,
 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 6, 1, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
 };
 
 //this list, indexed by the opcode value, tells whether the opcode is followed by a MODR/M byte
 private static int[] modrmTable = new int[]
 {
 1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,
 1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,
 1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,
 1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,1,1,0,0,0,0,0,1,0,1,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,
 1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,
 
 1,1,1,1,0,0,0,0,0,0,0,1,0,0,0,0,
 1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,
 
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 0,0,0,1,1,1,0,0,0,0,0,1,1,1,1,1,
 1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
 };
 
 //this list, indexed by the opcode, tells whether the instruction has an SIB byte
 //the SIB is only relevent in 32-bit opcode mode
 private static int[] sibTable = new int[]
 {
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
 };
 
 //gives the flag changes when indexed by the opcode value
 public static final MICROCODE[] flagTable = new MICROCODE[]
   {
   MICROCODE.FLAG_ADD_08, MICROCODE.FLAG_ADD_16, MICROCODE.FLAG_ADD_08, MICROCODE.FLAG_ADD_16, MICROCODE.FLAG_ADD_08, MICROCODE.FLAG_ADD_16, //0-5
   MICROCODE.FLAG_NONE, //6
   MICROCODE.FLAG_NONE, //7
   MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, //8-d
   MICROCODE.FLAG_NONE, //e
   MICROCODE.FLAG_BAD, //f
   MICROCODE.FLAG_ADC_08, MICROCODE.FLAG_ADC_16, MICROCODE.FLAG_ADC_08, MICROCODE.FLAG_ADC_16, MICROCODE.FLAG_ADC_08, MICROCODE.FLAG_ADC_16, //10-15
   MICROCODE.FLAG_NONE, //16
   MICROCODE.FLAG_NONE, //17
   MICROCODE.FLAG_SBB_08, MICROCODE.FLAG_SBB_16, MICROCODE.FLAG_SBB_08, MICROCODE.FLAG_SBB_16, MICROCODE.FLAG_SBB_08, MICROCODE.FLAG_SBB_16, //18-1d
   MICROCODE.FLAG_NONE, //1e
   MICROCODE.FLAG_NONE, //1f
   MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, //20-25
   MICROCODE.FLAG_BAD, //26
   MICROCODE.FLAG_NONE, //27
   MICROCODE.FLAG_SUB_08, //28
   MICROCODE.FLAG_SUB_16, //29
   MICROCODE.FLAG_SUB_08, //2a
   MICROCODE.FLAG_SUB_16, //2b
   MICROCODE.FLAG_SUB_08, //2c
   MICROCODE.FLAG_SUB_16, //2d
   MICROCODE.FLAG_BAD, //2e
   MICROCODE.FLAG_NONE, //2f
   MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, //30-35
   MICROCODE.FLAG_BAD, //36
   MICROCODE.FLAG_NONE, //37
   MICROCODE.FLAG_SUB_08, //38
   MICROCODE.FLAG_SUB_16, //39
   MICROCODE.FLAG_SUB_08, //3a
   MICROCODE.FLAG_SUB_16, //3b
   MICROCODE.FLAG_SUB_08, //3c
   MICROCODE.FLAG_SUB_16, //3d
   MICROCODE.FLAG_BAD, //3e
   MICROCODE.FLAG_NONE, //3f
   MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, MICROCODE.FLAG_INC_16, //40-47
   MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, MICROCODE.FLAG_DEC_16, //48-4f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //50-57
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //58-5b
   MICROCODE.FLAG_NONE, //5c
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //5d-5f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //60-63
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //64-7f
   MICROCODE.FLAG_80_82, MICROCODE.FLAG_81_83, MICROCODE.FLAG_80_82, MICROCODE.FLAG_81_83, //80-83
   MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, //84-85
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //86-8e
   MICROCODE.FLAG_NONE, //8f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //90-a5
   MICROCODE.FLAG_REP_SUB_08, MICROCODE.FLAG_REP_SUB_16, //a6-a7
   MICROCODE.FLAG_BITWISE_08, MICROCODE.FLAG_BITWISE_16, //a8-a9
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //aa-ad
   MICROCODE.FLAG_REP_SUB_08, MICROCODE.FLAG_REP_SUB_16, //ae-af
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //b0-bf
   MICROCODE.FLAG_ROTATE_08, MICROCODE.FLAG_ROTATE_16, //c0-c1
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //c2-ce
   MICROCODE.STORE0_FLAGS, //cf
   MICROCODE.FLAG_ROTATE_08, MICROCODE.FLAG_ROTATE_16, MICROCODE.FLAG_ROTATE_08, MICROCODE.FLAG_ROTATE_16, //d0-d3
   MICROCODE.FLAG_BITWISE_08, //d4
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //d5-d7
   MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, MICROCODE.FLAG_FLOAT_NOP, //d8-df
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //e0-ef
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f0-f3
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE,  //f4-f5
   MICROCODE.FLAG_F6, MICROCODE.FLAG_F7, //f6-f7
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //f8-fd
   MICROCODE.FLAG_FE, MICROCODE.FLAG_FF, //fe-ff
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f00-f05
   MICROCODE.FLAG_NONE, //f06
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f07-f08
   MICROCODE.FLAG_NONE, //f09
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f0a-f1f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //f20-f23
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f24-f2f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //f30-f32
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f33-f3f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //f40-f4f
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //f50-f7f
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //f80-fa0
   MICROCODE.FLAG_NONE, //fa1
   MICROCODE.FLAG_NONE, //fa2
   MICROCODE.FLAG_NONE, 
   MICROCODE.FLAG_SHL_16, MICROCODE.FLAG_SHL_16, //fa4-fa5
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //fa6-fa7
   MICROCODE.FLAG_NONE, //fa8
   MICROCODE.FLAG_NONE, //fa9
   MICROCODE.FLAG_BAD, //faa
   MICROCODE.FLAG_NONE, //fab
   MICROCODE.FLAG_SHR_16, MICROCODE.FLAG_SHR_16, //fac-fad
   MICROCODE.FLAG_BAD, //fae
   MICROCODE.FLAG_NONE, //faf
   MICROCODE.FLAG_UNIMPLEMENTED, MICROCODE.FLAG_UNIMPLEMENTED, //fb0-fb1
   MICROCODE.FLAG_NONE, //fb2
   MICROCODE.FLAG_NONE, //fb3
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //fb4-fb7
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //fb8-fb9
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //fba-fbb
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //fbc-fbf
   MICROCODE.FLAG_ADD_08, MICROCODE.FLAG_ADD_16, //fc0-fc1
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, //fc2-fc7
   MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, MICROCODE.FLAG_NONE, //fc8-fcf
   MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD, MICROCODE.FLAG_BAD //fd0-fdf
   };
 
 //gives the opcode microcode when indexed by the opcode value
 public static final MICROCODE[] opcodeTable = new MICROCODE[]
 {
 MICROCODE.OP_ADD, MICROCODE.OP_ADD, MICROCODE.OP_ADD, MICROCODE.OP_ADD, MICROCODE.OP_ADD, MICROCODE.OP_ADD,	//0x00-0x05
 MICROCODE.OP_PUSH, //0x06
 MICROCODE.OP_POP, //0x07
 MICROCODE.OP_OR, MICROCODE.OP_OR, MICROCODE.OP_OR, MICROCODE.OP_OR, MICROCODE.OP_OR, MICROCODE.OP_OR, //0x08-0x0d
 MICROCODE.OP_PUSH, //0xe
 MICROCODE.OP_BAD, //0xe-0xf
 MICROCODE.OP_ADC, MICROCODE.OP_ADC, MICROCODE.OP_ADC, MICROCODE.OP_ADC, MICROCODE.OP_ADC, MICROCODE.OP_ADC, //0x10-0x15
 MICROCODE.OP_PUSH, //0x16
 MICROCODE.OP_POP, //0x17
 MICROCODE.OP_SBB, MICROCODE.OP_SBB, MICROCODE.OP_SBB, MICROCODE.OP_SBB, MICROCODE.OP_SBB, MICROCODE.OP_SBB, //0x18-0x1d
 MICROCODE.OP_PUSH, //0x1e
 MICROCODE.OP_POP, //0x1f
 MICROCODE.OP_AND, MICROCODE.OP_AND, MICROCODE.OP_AND, MICROCODE.OP_AND, MICROCODE.OP_AND, MICROCODE.OP_AND, //0x20-0x25
 MICROCODE.OP_BAD, //0x26
 MICROCODE.OP_DAA, //0x27
 MICROCODE.OP_SUB, MICROCODE.OP_SUB, MICROCODE.OP_SUB, MICROCODE.OP_SUB, MICROCODE.OP_SUB, MICROCODE.OP_SUB, //0x28-0x2d
 MICROCODE.OP_BAD, //0x2e
 MICROCODE.OP_DAS, //0x2f
 MICROCODE.OP_XOR, MICROCODE.OP_XOR, MICROCODE.OP_XOR, MICROCODE.OP_XOR, MICROCODE.OP_XOR, MICROCODE.OP_XOR, //0x30-0x35
 MICROCODE.OP_BAD, //0x36
 MICROCODE.OP_AAA, //0x37
 MICROCODE.OP_CMP, MICROCODE.OP_CMP, MICROCODE.OP_CMP, MICROCODE.OP_CMP, MICROCODE.OP_CMP, MICROCODE.OP_CMP, //0x38-0x3d
 MICROCODE.OP_BAD, //0x3e
 MICROCODE.OP_AAS, //0x3f
 MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, MICROCODE.OP_INC, //0x40-0x47
 MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, MICROCODE.OP_DEC, //0x48-0x4f
 MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, MICROCODE.OP_PUSH, //0x50-0x57
 MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, MICROCODE.OP_POP, //0x58-0x5f
 MICROCODE.OP_PUSHA, //0x60
 MICROCODE.OP_POPA, //0x61
 MICROCODE.OP_BOUND, //0x62
 MICROCODE.OP_MOV, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0x63-0x67
 MICROCODE.OP_PUSH, //0x68
 MICROCODE.OP_IMUL_16_32, //0x69
 MICROCODE.OP_PUSH, //0x6a
 MICROCODE.OP_IMUL_16_32, //0x6b
 MICROCODE.OP_INSB, //0x6c
 MICROCODE.OP_INSW, //0x6d
 MICROCODE.OP_OUTSB, //0x6e
 MICROCODE.OP_OUTSW, //0x6f
 MICROCODE.OP_JO, MICROCODE.OP_JNO, MICROCODE.OP_JC, MICROCODE.OP_JNC, MICROCODE.OP_JZ, MICROCODE.OP_JNZ, MICROCODE.OP_JNA, MICROCODE.OP_JA, MICROCODE.OP_JS, MICROCODE.OP_JNS, MICROCODE.OP_JP, MICROCODE.OP_JNP, MICROCODE.OP_JL, MICROCODE.OP_JNL, MICROCODE.OP_JNG, MICROCODE.OP_JG, //0x70-0x7f
 MICROCODE.OP_80_83, MICROCODE.OP_80_83, MICROCODE.OP_80_83, MICROCODE.OP_80_83, //0x80-0x83
 MICROCODE.OP_TEST, MICROCODE.OP_TEST, //0x84-0x85
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0x86-0x8e
 MICROCODE.OP_POP, //0x8f
 MICROCODE.OP_NOP, //0x90
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0x91-0x97
 MICROCODE.OP_CBW, //0x98
 MICROCODE.OP_CWD, //0x99
 MICROCODE.OP_CALL_FAR, //0x9a
 MICROCODE.OP_FLOAT_NOP, //0x9b
 MICROCODE.OP_PUSHF, //0x9c
 MICROCODE.OP_POPF, //0x9d
 MICROCODE.OP_SAHF, //0x9e
 MICROCODE.OP_LAHF, //0x9f
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0xa0-0xa3
 MICROCODE.OP_MOVSB, //0xa4
 MICROCODE.OP_MOVSW, //0xa5
 MICROCODE.OP_CMPSB, //0xa6
 MICROCODE.OP_CMPSW, //0xa7
 MICROCODE.OP_TEST, MICROCODE.OP_TEST, //0xa8-0xa9
 MICROCODE.OP_STOSB, //0xaa
 MICROCODE.OP_STOSW, //0xab
 MICROCODE.OP_LODSB, //0xac
 MICROCODE.OP_LODSW, //0xad
 MICROCODE.OP_SCASB, //0xae
 MICROCODE.OP_SCASW, //0xaf
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0xb0-0xbf
 MICROCODE.OP_ROTATE_08, //0xc0
 MICROCODE.OP_ROTATE_16_32, //0xc1
 MICROCODE.OP_RET_IW, //0xc2
 MICROCODE.OP_RET, //0xc3
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0xc4-0xc7
 MICROCODE.OP_ENTER, //0xc8
 MICROCODE.OP_LEAVE, //0xc9
 MICROCODE.OP_RET_FAR_IW, //0xca
 MICROCODE.OP_RET_FAR, //0xcb
 MICROCODE.OP_INT3, //0xcc
 MICROCODE.OP_INT, //0xcd
 MICROCODE.OP_INT0, //0xce
 MICROCODE.OP_IRET, //0xcf
 MICROCODE.OP_ROTATE_08, //0xd0
 MICROCODE.OP_ROTATE_16_32, //0xd1
 MICROCODE.OP_ROTATE_08, //0xd2
 MICROCODE.OP_ROTATE_16_32, //0xd3
 MICROCODE.OP_AAM, //0xd4
 MICROCODE.OP_AAD, //0xd5
 MICROCODE.OP_SALC, //0xd6
 MICROCODE.OP_MOV, //0xd7
 MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, MICROCODE.OP_FLOAT_NOP, //0xd8-0xdf
 MICROCODE.OP_LOOPNZ_CX, MICROCODE.OP_LOOPZ_CX, MICROCODE.OP_LOOP_CX, MICROCODE.OP_JCXZ, //0xe0-0xe3
 MICROCODE.OP_IN_08, MICROCODE.OP_IN_16_32, //0xe4-0xe5
 MICROCODE.OP_OUT_08, MICROCODE.OP_OUT_16_32, //0xe6-0xe7
 MICROCODE.OP_CALL, //0xe8
 MICROCODE.OP_JMP_16_32, //0xe9
 MICROCODE.OP_JMP_FAR, //0xea
 MICROCODE.OP_JMP_08, //0xeb
 MICROCODE.OP_IN_08, MICROCODE.OP_IN_16_32, //0xec-0xed
 MICROCODE.OP_OUT_08, MICROCODE.OP_OUT_16_32, //0xee-0xef
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf0-0xf3
 MICROCODE.OP_HALT, //0xf4
 MICROCODE.OP_CMC, //0xf5
 MICROCODE.OP_F6, MICROCODE.OP_F7, //0xf6-f7
 MICROCODE.OP_CLC, MICROCODE.OP_STC, MICROCODE.OP_CLI, MICROCODE.OP_STI, MICROCODE.OP_CLD, MICROCODE.OP_STD, //0xf8-0xfd
 MICROCODE.OP_FE, MICROCODE.OP_FF, //0xfe-0xff
 MICROCODE.OP_F00, MICROCODE.OP_F01, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf00-0xf05
 MICROCODE.OP_CLTS, //0xf06
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf07-0xf08
 MICROCODE.OP_NOP, //0xf09
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf0a-0xf1e
 MICROCODE.OP_NOP, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0xf1f-0xf23
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf20-0xf2f
 MICROCODE.OP_UNIMPLEMENTED, //0xf30
 MICROCODE.OP_RDTSC, //0xf31
 MICROCODE.OP_UNIMPLEMENTED, //0xf32
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf33-0xf3f
 MICROCODE.OP_CMOVO, MICROCODE.OP_CMOVNO, MICROCODE.OP_CMOVC, MICROCODE.OP_CMOVNC, MICROCODE.OP_CMOVZ, MICROCODE.OP_CMOVNZ, MICROCODE.OP_CMOVNA, MICROCODE.OP_CMOVA, MICROCODE.OP_CMOVS, MICROCODE.OP_CMOVNS, MICROCODE.OP_CMOVP, MICROCODE.OP_CMOVNP, MICROCODE.OP_CMOVL, MICROCODE.OP_CMOVNL, MICROCODE.OP_CMOVNG, MICROCODE.OP_CMOVG, //0xf40-0xf4f
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xf50-0xf7f
 MICROCODE.OP_JO_16_32, MICROCODE.OP_JNO_16_32, MICROCODE.OP_JC_16_32, MICROCODE.OP_JNC_16_32, MICROCODE.OP_JZ_16_32, MICROCODE.OP_JNZ_16_32, MICROCODE.OP_JNA_16_32, MICROCODE.OP_JA_16_32, MICROCODE.OP_JS_16_32, MICROCODE.OP_JNS_16_32, MICROCODE.OP_JP_16_32, MICROCODE.OP_JNP_16_32, MICROCODE.OP_JL_16_32, MICROCODE.OP_JNL_16_32, MICROCODE.OP_JNG_16_32, MICROCODE.OP_JG_16_32, //0xf80-0xf8f
 MICROCODE.OP_SETO, MICROCODE.OP_SETNO, MICROCODE.OP_SETC, MICROCODE.OP_SETNC, MICROCODE.OP_SETZ, MICROCODE.OP_SETNZ, MICROCODE.OP_SETNA, MICROCODE.OP_SETA, MICROCODE.OP_SETS, MICROCODE.OP_SETNS, MICROCODE.OP_SETP, MICROCODE.OP_SETNP, MICROCODE.OP_SETL, MICROCODE.OP_SETNL, MICROCODE.OP_SETNG, MICROCODE.OP_SETG, //0xf90-0xf9f
 MICROCODE.OP_PUSH, //0xfa0
 MICROCODE.OP_POP, //0xfa1
 MICROCODE.OP_CPUID, //0xfa2
 MICROCODE.OP_BT_16_32, //0xfa3
 MICROCODE.OP_SHLD_16_32, MICROCODE.OP_SHLD_16_32, //0xfa4-0xfa5
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xfa6-0xfa7
 MICROCODE.OP_PUSH, //0xfa8
 MICROCODE.OP_POP, //0xfa9
 MICROCODE.OP_BAD, //0xfaa
 MICROCODE.OP_BTS_16_32, //0xfab
 MICROCODE.OP_SHRD_16_32, MICROCODE.OP_SHRD_16_32, //0xfac-0xfad
 MICROCODE.OP_BAD, MICROCODE.OP_IMUL_16_32, //0xfae-0xfaf
 MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, //0xfb0-0xfb1
 MICROCODE.OP_MOV, //0xfb2
 MICROCODE.OP_BTR_16_32, //0xfb3
 MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, MICROCODE.OP_MOV, //0xfb4-0xfb7
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_UNIMPLEMENTED, //0xfb8-0xfba
 MICROCODE.OP_BTC_16_32, //0xfbb
 MICROCODE.OP_BSF, //0xfbc
 MICROCODE.OP_BSR, //0xfbd
 MICROCODE.OP_SIGN_EXTEND, //0xfbe
 MICROCODE.OP_SIGN_EXTEND_16_32, //0xfbf
 MICROCODE.OP_ADD, MICROCODE.OP_ADD, //0xfc0-0xfc1
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, //0xfc2-0xfc7
 MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, MICROCODE.OP_UNIMPLEMENTED, //0xfc8-0xfcf
 MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD, MICROCODE.OP_BAD //0xfd0-0xfff
 };
 
 }
