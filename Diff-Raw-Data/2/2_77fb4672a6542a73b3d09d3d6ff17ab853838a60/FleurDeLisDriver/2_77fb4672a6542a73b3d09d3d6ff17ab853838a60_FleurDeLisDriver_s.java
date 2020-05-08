 package com.simcc800;
 
 import java.io.*;
 import java.util.Calendar;
 
 // Oh. Fleur de Lis.
 public class FleurDeLisDriver {
 	static short threadFlags;
 	// Some I/O related stuff
 	static boolean lcdoffshift0flag = false;
 	static public boolean timer0started = false;
 	static public boolean timer0waveoutstart = false;
 	static public boolean timer1started = false;
 	static public int prevtimer0value = 0;
 	private static final int RAM_SIZE = 0x10002;
 	static public byte fixedram0000[] = new byte[RAM_SIZE]; // This array keeps fixedran0000, size=64K
 	static private byte[] zp40cache = new byte[0x40];
 	static byte keypadmatrix[][] = new byte[8][8];
 	static private byte[] brom_file;
 	static private byte[] norflash_file;
 	
 	private static final int MAP0000 = 0;
 	private static final int MAP2000 = 1;
 	private static final int MAP4000 = 2;
 	private static final int MAP6000 = 3;
 	private static final int MAP8000 = 4;
 	private static final int MAPA000 = 5;
 	private static final int MAPC000 = 6;
 	private static final int MAPE000 = 7;
 	
 	// I/O port
 	private static final int IO00_BANK_SWITCH= 0x00;
 	private static final int IO01_INT_ENABLE = 0x01;
 	private static final int IO02_TIMER0_VAL = 0x02;
 	private static final int IO04_GENERAL_CTRL=0x04;
 	private static final int IO06_LCD_CONFIG = 0x06;
 	private static final int IO08_PORT0_DATA = 0x08;
 	private static final int IO09_PORT1_DATA = 0x09;
 	private static final int IO0A_ROA        = 0x0A;
 	private static final int IO0B_LCD_CTRL   = 0x0B;
 	private static final int IO0C_LCD_CTRL   = 0x0C;
 	private static final int IO0D_VOLUME_ID  = 0x0D;
 	
 	static private final int IO15_PORT1_DIR  = 0x15;
 	
 	static public void keymatrixChange(int idx_y, int idx_x, boolean is_pressed) {
 		if(is_pressed==true) keypadmatrix[idx_y][idx_x] = 1;
 		else keypadmatrix[idx_y][idx_x] = 0;
 		
 		System.out.println("0908: "+fixedram0000[0x09] + fixedram0000[0x08]);
 		updateKeypadRegisters();
 	}
 	
 	// Pointers to memory
 	static AddressN[] pmemmap = new AddressN[8]; // This line creates 8 REFERENCES, not actual java.lang.Object's
 	static AddressN[] may4000ptr = new AddressN[1]; // Java does not have pointer.
 	static AddressN[] norbankheader = new AddressN[0x10]; // ##@@!!
 	static AddressN[] volume0array  = new AddressN[0x100];
 	static AddressN[] volume1array  = new AddressN[0x100];
 	static AddressN[] bbsbankheader = new AddressN[0x10]; // ##@@!!
 	
 	static public void writeByte(int address, byte data) {
 		int row = address >> 0xD;
 		/*
 		Address addr = pmemmap[row].add(address & 0x1FFF);
 		if(addr instanceof BROMAddress) {
 			brom_file[((BROMAddress) addr).offset] = data; // Ah, we're writing BROM?
 		} else if(addr instanceof NORFlashAddress) {
 			norflash_file[((NORFlashAddress) addr).offset] = data;
 		} else if(addr instanceof SRAMAddress) {
 			fixedram0000[((SRAMAddress) addr).offset] = data;
 		} else {
 			assert(false);
 		}*/
 		
 		
 		// ##@@!!
 		AddressN addr = pmemmap[row];
 		int offset = pmemmap[row].offset + (address&0x1FFF);
 		if(addr.type == AddressType.BROM) {
 			brom_file[offset] = data; // Ah, we're writing into BROM?
 		} else if(addr.type == AddressType.NORFLASH) {
 			norflash_file[offset] = data;
 		} else if(addr.type == AddressType.SRAM) {
 			fixedram0000[offset] = data;
 		} else { 
 			assert(false);
 		}
 	}
 	
 	static public byte getByte(int address) {
 		int row = address >> 0xD;
 		byte ret = 0x00;
 		/*
 		Address addr = pmemmap[row].add(address & 0x1FFF);
 		if(addr instanceof BROMAddress) {
 			ret = brom_file[((BROMAddress) addr).offset];
 		} else if(addr instanceof NORFlashAddress) {
 			ret = norflash_file[((NORFlashAddress) addr).offset];
 		} else if(addr instanceof SRAMAddress) {
 			ret = fixedram0000[((SRAMAddress) addr).offset];
 		} else {
 			assert(false);
 		}
 		return ret;
 		*/
 		
 		AddressN addr = pmemmap[row];
 		int offset = addr.offset + (address & 0x1FFF);
 		if(addr.type == AddressType.BROM) {
 			ret = brom_file[offset];
 		} else if(addr.type == AddressType.NORFLASH) {
 			ret = norflash_file[offset];
 		} else if(addr.type == AddressType.SRAM) {
 			ret = fixedram0000[offset];
 		} else {
 			assert(false);
 		}
 		return ret;
 	}
 	
 	static public short getWord(int address) {
 		int row = address >> 0xD;
 		short ret = 0x0000;
 
 		/*
 		Address addr = pmemmap[row].add(address & 0x1FFF);
 		if(addr instanceof BROMAddress) {
 			int offset_low = ((BROMAddress) addr).offset
 				,offset_high = (offset_low==0xFFFF)?0:offset_low+1;
 			short low = (short)(((short)brom_file[offset_low]) & 0x00FF) // Too cumbersome!
 				, high = (short)(((short)brom_file[offset_high]) & 0x00FF);// No unsigned in Java?
 			ret = (short) ((high << 8) + low);
 		} else if(addr instanceof NORFlashAddress) {
 			int offset_low = ((NORFlashAddress) addr).offset,
 				offset_high = (offset_low==0xFFFF)?0:offset_low+1;
 			short low = (short)(((short)norflash_file[offset_low]) & 0x00FF),
 				 high = (short)(((short)norflash_file[offset_high]) & 0x00FF);
 			ret = (short) ((high << 8) + low);
 		} else if(addr instanceof SRAMAddress) {
 			int offset_low = ((SRAMAddress) addr).offset,
 				offset_high = (offset_low==0xFFFF)?0:offset_low+1;
 			short low = (short)(((short)fixedram0000[offset_low]) & 0x00FF),
 				high = (short)(((short)fixedram0000[offset_high]) & 0x00FF);
 			ret = (short) ((high << 8) + low);
 		} else {
 			assert(false);
 		}
 		return ret;
 		*/
 		AddressN addr = pmemmap[row];
 		int offset = addr.offset + (address & 0x1FFF);
 		if(addr.type == AddressType.BROM) {
 			int offset_low = offset, offset_high = (offset_low==0xFFFF) ? 0:offset_low+1;
 			short low = (short)(((short)brom_file[offset_low]) & 0x00FF) // Too cumbersome!
 					,high = (short)(((short)brom_file[offset_high]) & 0x00FF);// No unsigned in Java?
 			ret = (short) ((high << 8) + low);
 		} else if(addr.type == AddressType.NORFLASH) {
 			int offset_low = offset, offset_high = (offset_low==0xFFFF) ? 0:offset_low+1;
 			short low = (short)(((short)norflash_file[offset_low]) & 0x00FF),
 					 high = (short)(((short)norflash_file[offset_high]) & 0x00FF);
 			ret = (short) ((high << 8) + low);
 		} else if(addr.type == AddressType.SRAM) {
 			int offset_low = offset, offset_high = (offset_low==0xFFFF) ? 0:offset_low+1;
 			short low = (short)(((short)fixedram0000[offset_low]) & 0x00FF),
 					high = (short)(((short)fixedram0000[offset_high]) & 0x00FF);
 			ret = (short) ((high << 8) + low);
 		} else {
 			assert(false);
 		}
 		return ret;
 	}
 	
 	public FleurDeLisDriver(byte[] _brom, byte[] _nor) {
 		brom_file = _brom;
 		norflash_file = _nor;
 		
 		
 		// Taken from TNekoDriver's constructor.
 		for(int i=0; i<256; i++) {
 			volume0array[i] = new AddressN(i*0x8000, AddressType.BROM); // ##@@!!
 			volume1array[i] = new AddressN((i+256)*0x8000, AddressType.BROM); // ##@@!!
 		}
 		for(int i=0; i<16; i++) {
 			norbankheader[i] = new AddressN(i*0x8000, AddressType.NORFLASH); // ##@@!!
 		}
 		
 		may4000ptr[0] = new AddressN(0x0000, AddressType.SRAM);
 		for(int i=0; i<0x10; i++) {
 			bbsbankheader[i] = new AddressN(0x0000, AddressType.SRAM);
 		}
 		
 		memInitialize();
 	}
 	
 	// This guy is called from outside
 	static public void checkTimebaseAndEnableIRQnEXIE1() {
 		if((fixedram0000[IO04_GENERAL_CTRL] & 0x0F)!=0) {
 			threadFlags |= 0x10;
 			fixedram0000[IO01_INT_ENABLE] |= 0x8;
 		}
 	}
 	
 	// Also, this guy is called from outside
 	static public void turnOff2HzNMIMaskAddIRQFlag() {
 		if((fixedram0000[IO04_GENERAL_CTRL]&0xF)!=0) {
 			threadFlags |= 0x10;
 			fixedram0000[IO01_INT_ENABLE] |= 0x10; // Turn off 2Hz NMI.
 		}
 	}
 	
 	// Reset case
 	static public void resetCPU() {
 		fixedram0000[IO01_INT_ENABLE] |= 0x1;
 		fixedram0000[IO02_TIMER0_VAL] |= 0x1;
 		threadFlags &= 0xFF7F;
 		CPU.regs.pc = getWord(0xFFFC);
 	}
 	
 	static private void fillC000BIOSBank(AddressN[] array) {
 		AddressN_assign(bbsbankheader[0], array[0]);
 		if((fixedram0000[0x0D] & 1)!=0) {
 			AddressN_assignAdd(bbsbankheader[1], norbankheader[0], 0x2000);
 		} else {
 			bbsbankheader[1].set(0x4000, AddressType.SRAM);
 		}
 		AddressN_assignAdd(bbsbankheader[2], array[0], 0x4000);
 		AddressN_assignAdd(bbsbankheader[3], array[0], 0x6000);
 		for(int i=0; i<3; i++) {
 			AddressN_assign(bbsbankheader[i*4 + 4], array[i+1]); // ##@@!!
 			AddressN_assignAdd(bbsbankheader[i*4 + 5], array[i+1], 0x2000); // ##@@!!
 			AddressN_assignAdd(bbsbankheader[i*4 + 6], array[i+1], 0x4000); // ##@@!!
 			AddressN_assignAdd(bbsbankheader[i*4 + 7], array[i+1], 0x6000); // ##@@!!
 		}
 	}
 	
 	static private void switch4000ToBFFF(int bank) {
 		if((bank != 0) || ((fixedram0000[0x0A] & 0x80) != 0)) {
 			AddressN_assign(pmemmap[MAP4000], may4000ptr[0]); // ##@@!!
 			AddressN_assignAdd(pmemmap[MAP6000], may4000ptr[0], 0x2000); // ##@@!!
 		} else {
 			if((fixedram0000[0x0D] & 1) != 0) {
 				// Volume1, 3;
 				// 4000 - 7FFF is page 0 of NOR
 				pmemmap[MAP4000].set(0, AddressType.NORFLASH);// = new NORFlashAddress(0); // ##@@!!
 				pmemmap[MAP6000].set(0x2000, AddressType.NORFLASH);
 			} else {
 				// Volume, 2;
 				// 4000 - 5FFF is RAM;
 				// 6000 - 7FFF is mirror of 4000 - 5FFF
 				pmemmap[MAP4000].set(0x4000, AddressType.SRAM);
 				pmemmap[MAP6000].set(0x4000, AddressType.SRAM);
 			}
 		}
 		AddressN_assignAdd(pmemmap[MAP8000], may4000ptr[0], 0x4000);
 		AddressN_assignAdd(pmemmap[MAPA000], may4000ptr[0], 0x6000);
 	}
 	
 	private void memReset() {
 		
 		// 1. Taken from MemReset()
 		pmemmap[MAP0000] = new AddressN(0x0000, AddressType.SRAM);
 		pmemmap[MAP2000] = new AddressN(0x2000, AddressType.SRAM);
 		pmemmap[MAP4000] = new AddressN(0x4000, AddressType.SRAM);
 		pmemmap[MAP6000] = new AddressN(0x6000, AddressType.SRAM);
 		pmemmap[MAP8000] = new AddressN(0x8000, AddressType.SRAM);
 		pmemmap[MAPA000] = new AddressN(0xA000, AddressType.SRAM);
 		pmemmap[MAPC000] = new AddressN(0xC000, AddressType.SRAM);
 		pmemmap[MAPE000] = new AddressN(0xE000, AddressType.SRAM);
 		
 		
 		// 2. Taken from InitInternalAddrs()
 		// 2.1: FillC000BIOSBank
 		fillC000BIOSBank(volume0array);
 		AddressN_assign(pmemmap[MAPC000], bbsbankheader[0]);
 		AddressN_assign(may4000ptr[0], volume0array[0]); //##@@!!
 		AddressN_assignAdd(pmemmap[MAPE000], volume0array[0], 0x2000);
 		// 2.2: switch4000toBFFF
 		switch4000ToBFFF(0);
 		fixedram0000[0x0C] = 0x28;
 		
 		// 3. CPU initialize and set PS
 	}
 	
 	static private void initRAM0IO() {
 		fixedram0000[0x1B] = 0; // PWM Data
 		fixedram0000[0x01] = 0; // Interrupt Enable
 		fixedram0000[0x04] = 0; // General Ctrl
 		fixedram0000[0x05] = 0; // Clock Ctrl
 		threadFlags = 0;
 		fixedram0000[0x08] = 0; // Port0 Data
 		fixedram0000[0x00] = 0; // Bank Switch
 		fixedram0000[0x09] = 0; // Port1 Data
 	}
 	
 	private void memInitialize() {
 		for(int i=0; i<RAM_SIZE; i++) fixedram0000[i] = 0x00;
 		memReset();
 		initRAM0IO();
 		for(int y=0; y<8; y++) {
 			for(int x=0; x<8; x++) 
 				keypadmatrix[y][x]=0;
 		}
 		syncMachineTime();
 	}
 	
 	public static void syncMachineTime() {
 		Calendar c = Calendar.getInstance();
 		int year = c.get(Calendar.YEAR);
 		int month= c.get(Calendar.MONTH);
 		int day  = c.get(Calendar.DAY_OF_MONTH);
 		int hour = c.get(Calendar.HOUR_OF_DAY);
 		int min  = c.get(Calendar.MINUTE);
 		int sec  = c.get(Calendar.SECOND);
 		int wk   = c.get(Calendar.DAY_OF_WEEK);
 		
 		fixedram0000[1132] = (byte)(year-1881);
		fixedram0000[1133] = (byte)(month-1);
 		fixedram0000[1134] = (byte)(day-1);
 		fixedram0000[1129] = (byte)(hour);
 		fixedram0000[1130] = (byte)(min);
 		fixedram0000[1131] = (byte)(sec*2);
 	}
 	
 	public static void updateKeypadRegisters() {
 		byte port1control = fixedram0000[0x15];
 		byte port0control = (byte) (fixedram0000[0x0F] & (byte)0xF0);
 		byte port1controlbit = 1; // I don't know what I'm doing
 		byte tmpdest0 = 0, tmpdest1 = 0;
 		byte port1data = fixedram0000[0x09], 
 			 port0data = fixedram0000[0x08];
 		for(int y=0; y<8; y++) {
 			// y = port10 to port17!!
 			boolean ysend = ((port1control & port1controlbit) != 0);
 			byte xbit = 1;
 			for(int x=0; x<8; x++) {
 				// x = port00 to port07!
 				byte port0controlbit;
 				if(x<2) {
 					port0controlbit = (byte)(xbit << 4);
 				} else if (x<4) {
 					port0controlbit = (byte)0x40;
 				} else {
 					port0controlbit = (byte)0x80;
 				}
 				if(y<2 && (port1data==0x02 || port1data==0x01)) {
 					// Ah, I really have no idea
 					// how he hacked the simulator.
 					if(ysend==true) {
 						if((keypadmatrix[y][x]!=1)
 							&& ((port1data & port1controlbit)!=0)
 							&& ((port0control & port0controlbit)==0)) {
 							tmpdest0 |= xbit;
 						}
 					} else {
 						if((keypadmatrix[y][x]!=1)
 							&& ((port0data & xbit)!=0)
 							&& ((port0control & port0controlbit)!=0)) {
 							tmpdest1 |= xbit;
 						}
 					}
 				} else if(keypadmatrix[y][x] != 2) {
 					if(ysend == true){
 						if((keypadmatrix[y][x]!=0)
 							&& ((port1data & port1controlbit)!=0)
 							&& ((port0control & port0controlbit)==0)) {
 							tmpdest0 |= xbit;
 						}
 					} else {
 						if((keypadmatrix[y][x]!=0)
 							&& ((port0data & xbit)!=0)
 							&& ((port0control & port0controlbit)!=0)) {
 							tmpdest1 |= xbit;
 						}
 					}
 				}
 				xbit = (byte)(xbit << 1);
 			}
 			port1controlbit = (byte)(port1controlbit << 1);
 		}
 		if(port1control != (byte)0xFF) {
 			// Port 1 should clean .. $*%$$^*#$)(^
 			port1data &= port1control;
 		}
 		if(port1control != (byte)0xF0) {
 			byte port0mask = (byte)(((port0control>>4)&0xFF));
 			port0mask = (byte)(port0mask & 0x03);
 			if((port0control & 0x40) != 0) {
 				port0mask |= 0x0C;
 			}
 			if((port0control & (byte)0x80) != 0) {
 				port0mask |= (byte)0xF0;
 			}
 			port0data &= port0mask;
 		}
 		port0data |= tmpdest0;
 		port1data |= tmpdest1;
 		
 		fixedram0000[0x09/*Port 1 data*/] = port1data;
 		fixedram0000[0x08/*Port 0 data*/] = port0data;
 	}
 	
 	// Copying from 's code.
 	public enum AddressType {
 		BROM, NORFLASH, SRAM
 	};
 	public interface Address {
 		public Address add(int _delta);
 		public int getOffset();
 	}
 	public abstract class BasicAddress {
 		public AddressType type;
 		public int offset; // Offset from the beginning of the whatever storage device
 		public BasicAddress(int _offset) { offset = _offset; }
 		public int getOffset() { return offset; }
 	}
 	
 	public class AddressN {
 		public AddressType type;
 		public int offset;
 		public AddressN(int _offset) {
 			type = AddressType.SRAM;
 		}
 		public AddressN(int _offset, AddressType _type) {
 			offset=_offset; type=_type; 
 		}
 		public Address add(int delta) { return null; }
 		public int getOffset() { return offset; }
 //		public void assign(AddressN array) { offset=array.offset; type=array.type; }
 //		public void assignAdd(AddressN array, int delta) {
 //			offset = array.offset+delta;
 //			type = array.type;
 //		}
 		public void set(int _offset, AddressType _type) {
 			offset = _offset; type = _type;
 		}
 	}
 	
 	public static void AddressN_assign(AddressN to, AddressN from) {
 		to.offset = from.offset; to.type = from.type;
 	}
 	public static void AddressN_assignAdd(AddressN to, AddressN from, int delta) {
 		to.offset = from.offset + delta;
 		to.type = from.type;
 	}
 	
 	// #######################
 	// I/O read/write stuff
 	// #######################
 	
 	public static byte ioread(int idx) {
 		switch(idx) {
 		case 0x00: {
 			byte r = fixedram0000[IO00_BANK_SWITCH];
 			return r;
 		}
 		
 		case 0x04: { // Read04StopTimer0
 			timer0started = false;
 			if(timer0waveoutstart == true) timer0waveoutstart=false;
 			return fixedram0000[0x04];
 		}
 		case 0x05: { // Read05StartTimer0
 			timer0started = true;
 			if(fixedram0000[0x02] == (byte)0x3F) {
 				timer0waveoutstart = true;
 			}
 			prevtimer0value = fixedram0000[0x02];
 			return fixedram0000[0x05]; // Follow rulez by GGV
 		}
 		case 0x06: { // Read06StopTimer1
 			threadFlags |= 0x02;
 			// TODO What is mayGenralnClockCtrlValue in Banxian's code?
 			return fixedram0000[0x06];
 		}
 		
 		case 0x07: { // Read07StartTime1
 			threadFlags &= 0xFFFD;
 			return fixedram0000[0x07];
 		}
 		
 		case 0x08: { // Read08Port0
 			updateKeypadRegisters();
 			return fixedram0000[IO08_PORT0_DATA];
 		}
 		
 		case 0x09: { // Read09Port1
 			updateKeypadRegisters();
 			return fixedram0000[IO09_PORT1_DATA];
 		}
 		
 		
 		
 		default:
 			return fixedram0000[idx];
 		}
 	}
 	
 	/*
 	final IORead[] ioread = {
 		read00BankSwitch, // 0x00
 		nullRead, // 0x01
 		nullRead, // 0x02
 		nullRead, // 0x03
 		read04StopTimer0, // 0x04
 		read05StartTimer0, // 0x05
 		read06StopTimer1, // 0x06
 		read07StartTimer1, // 0x07
 		read08Port0, // TODO 0x08
 		read09Port1, // TODO 0x09
 		nullRead, nullRead, nullRead, nullRead, nullRead, nullRead,  // 0x0A-0x0F
 		nullRead, nullRead, nullRead, nullRead, // 0x10 - 0x13
 		nullRead, nullRead, nullRead, nullRead, // 0x14 - 0x17
 		nullRead, nullRead, nullRead, nullRead, // 0x18 - 0x1B
 		nullRead, nullRead, nullRead, nullRead, // 0x1C - 0x1F
 		nullRead, nullRead, nullRead, nullRead, // 0x20 - 0x23
 		nullRead, nullRead, nullRead, nullRead, // 0x24 - 0x27
 		nullRead, nullRead, nullRead, nullRead, // 0x28 - 0x2B
 		nullRead, nullRead, nullRead, nullRead, // 0x2C - 0x2F
 		nullRead, nullRead, nullRead, nullRead, // 0x30 - 0x33
 		nullRead, nullRead, nullRead, nullRead, // 0x34 - 0x37
 		nullRead, nullRead, nullRead, nullRead, // 0x38 - 0x3B
 		nullRead, nullRead, nullRead, nullRead, // 0x3C - 0x3F
 	};*/
 	
 	public static void iowrite(int pos, int value) {
 		switch(pos) {
 		case 0x00: // Write00BankSwitch
 		{
 			int bank=value&0xFF;
 			if((fixedram0000[0x0A] & 0x80)==0x80) { // ROA == 1
 				// NOR flash has 16 banks, each 32KB. Totalling 512KB.
 				char nor_bank = (char)(bank & 0xF);
 //				may4000ptr[0] = norbankheader[nor_bank];
 				AddressN_assign(may4000ptr[0], norbankheader[nor_bank]); // ##@@!!
 				switch4000ToBFFF(nor_bank);
 			} else { // ROA == 0
 				if((fixedram0000[0x0D] & 1) != 0) {
 					// Volume ID, ($0D), is 1.
 //					may4000ptr[0] = volume1array[bank];
 					AddressN_assign(may4000ptr[0], volume1array[bank]); // ##@@!!
 					switch4000ToBFFF(bank);
 				} else {
 //					may4000ptr[0] = volume0array[bank];
 					AddressN_assign(may4000ptr[0],volume0array[bank]); // ##@@!!
 					switch4000ToBFFF(bank);
 				}
 			}
 			// Last, update the value of memory address $00
 			fixedram0000[0x00] = (byte)(bank);
 			break;
 		}
 		
 		case 0x02: // Write02Timer0Value
 		{
 			if(timer0started) {
 				prevtimer0value = value;
 			}
 			fixedram0000[IO02_TIMER0_VAL] = (byte)value;
 			break;
 		}
 
 		case 0x05: {
 			// I can't really follow Banxian's code now.
 			if((fixedram0000[0x05] & 0x8) != 0) {
 				if((value & 0xF) == 0) {
 					lcdoffshift0flag = true;
 				}
 			}
 			fixedram0000[0x05] = (byte)value;
 			break;
 		}
 		
 		case 0x06: {
 			int t = ((fixedram0000[0x0C] & 0x03) << 12);
 			t |= (value << 4);
 			t = t & 0x0000FFFF;
 			fixedram0000[0x09] &= 0x000000FE;
 			fixedram0000[0x06] = (byte) (value&0xFF);
 			break;
 		}
 		
 		case 0x08: {
 			fixedram0000[IO08_PORT0_DATA/*Port0 data*/] = (byte) value;
 			byte xbit = 1;
 			byte row6data = 0, row7data = 0;
 			for(int x=0; x<8; x++) {
 				if((keypadmatrix[1][x] != 1)) {
 					row6data |= xbit;
 				}
 				if(keypadmatrix[0][x] != 1) {
 					row7data |= xbit;
 				}
 				xbit = (byte)(xbit << 1);
 			}
 			// Workaround... for what?
 			if(row6data == (byte)0xFF) {
 				row6data = 0;
 			}
 			if(row7data == (byte)0xFF) {
 				row7data = 0;
 			}
 			if(row6data == value || value == 0 || row7data == (byte)(0xFB)) {
 				fixedram0000[IO0B_LCD_CTRL] &= 0xFE;
 			}else { fixedram0000[IO0B_LCD_CTRL] |= 0x01; }
 			updateKeypadRegisters();
 			break;
 		}
 			
 		case 0x09: //Write09Port1
 		{
 			fixedram0000[IO09_PORT1_DATA] = (byte)value;
 			// Cosplay the emulator!!!!
 			byte xbit = 1, row6data = 0, row7data = 0;
 			for(int x=0; x<8; x++) {
 				if(keypadmatrix[1][x] != 1) row6data |= xbit;
 				if(keypadmatrix[0][x] != 1) row7data |= xbit;
 				xbit = (byte) (xbit << 1);
 			}
 			// Again the workaround
 			if(row6data == (byte)0xFF) row6data = 0;
 			if(row7data == (byte)0xFF) row7data = 0;
 			byte port0bit01 = (byte) (fixedram0000[IO08_PORT0_DATA] & 0x03);
 			if(value == 0) {
 				fixedram0000[IO0B_LCD_CTRL] = port0bit01;
 				if((row6data != (byte)0xFF) || (row7data != (byte)0xFF)) {
 					fixedram0000[IO0B_LCD_CTRL] = (byte)(port0bit01 ^ 0x03);
 				}
 				if(row7data == (byte)0xFD) fixedram0000[IO0B_LCD_CTRL] &= 0xFE;
 			}
 			// Signed and unsigned; pay attention here.
 			else if(((byte)value == (byte)0xFD) || (byte)value == (byte)0xFE) {
 				fixedram0000[IO0B_LCD_CTRL] = port0bit01;
 				if(row7data == value) {
 					fixedram0000[IO0B_LCD_CTRL] = (byte)(port0bit01 ^ 0x3);
 				}
 			}
 			else if((byte)value==0x03) {
 				if(fixedram0000[IO15_PORT1_DIR]==(byte)0xFC) updateKeypadRegisters();
 				fixedram0000[IO0B_LCD_CTRL] = port0bit01;
 				if(row7data == 0xFB) { fixedram0000[IO0B_LCD_CTRL] = (byte)(port0bit01 ^ 0x3); }
 			}
 			else if((byte)value==0x02) {
 				fixedram0000[IO08_PORT0_DATA] = row6data;
 			} else if((byte)value==0x01) {
 				fixedram0000[IO08_PORT0_DATA] = row7data;
 			}
 			else {
 				updateKeypadRegisters();
 			}
 			break;
 		}
 		
 		case 0x0A: // Write0AROABBS
 		{
 			// BBS means "BIOS Bank Switch"
 			if(value != (int)(fixedram0000[0x0A]&0xFF)) {
 				int bank;
 				if((value & 0x80)!=0) {
 					// ROA <- 1, RAM or NOR flash.
 					bank = fixedram0000[0x00] & 0x0000000F;
 					AddressN_assign(may4000ptr[0], norbankheader[bank]);
 				} else { // Should not write to ROM.
 					bank = fixedram0000[0x00] & 0xFF;
 					if((fixedram0000[0x0D]/*Volume ID*/&1) != 0) {
 						AddressN_assign(may4000ptr[0], volume1array[bank]);
 					} else {
 						AddressN_assign(may4000ptr[0], volume0array[bank]);
 					}
 				}
 				fixedram0000[0x0A] = (byte)value;
 				switch4000ToBFFF(bank&0xFF);
 				AddressN_assign(pmemmap[MAPC000], bbsbankheader[value & 0x0F]);
 			}
 			break;
 		}
 		
 		case 0x0C: // Write0CTimer01Control
 		{
 			int t = ((value & 0x3) << 12) & 0x0000FFFF;
 			t = t | (fixedram0000[IO06_LCD_CONFIG] << 4);
 			fixedram0000[IO0C_LCD_CTRL] = (byte)value;
 			break;
 		}
 		
 		case 0x0D: // Write0DVolumeIDLCDSegCtrl
 		{
 			if((value ^ fixedram0000[IO00_BANK_SWITCH] & 1) != 0) { // Assiciativity: Calculate '&' then '^'
 				// Bit 0 changed.
 				byte bank = fixedram0000[IO00_BANK_SWITCH];
 				if((value&1)!= 0) { // Volume 1, 3.
 					fillC000BIOSBank(volume1array);
 //					may4000ptr[0] = volume1array[bank&0xFF]; // Signed and unsigned
 					AddressN_assign(may4000ptr[0], volume1array[bank&0xFF]); // ##@@!!
 //					pmemmap[MAPE000] = volume1array[0].add(0x2000);
 					AddressN_assignAdd(pmemmap[MAPE000], volume1array[0], 0x2000);
 				} else { // Volume 0, 2.
 					fillC000BIOSBank(volume0array);
 //					may4000ptr[0] = volume0array[bank&0xFF]; // Signed and unsigned
 					AddressN_assign(may4000ptr[0], volume0array[bank&0xFF]); // ##@@!!
 //					pmemmap[MAPE000] = volume0array[0].add(0x2000);
 					AddressN_assignAdd(pmemmap[MAPE000], volume0array[0], 0x2000); // ##@@!!
 				}
 				byte roabbs = fixedram0000[IO0A_ROA];
 				if((roabbs&0x80) != 0) {
 					bank = (byte)(bank & 0x0F);
 //					may4000ptr[0] = norbankheader[bank];
 					AddressN_assign(may4000ptr[0], norbankheader[bank]); // ##@@!!
 				}
 //				pmemmap[MAPC000] = bbsbankheader[roabbs & 0x0F];
 				AddressN_assign(pmemmap[MAPC000], bbsbankheader[roabbs&0x0F]); // ##@@!!
 				switch4000ToBFFF(bank&0xFF); // Signed and unsigned
 			}
 			fixedram0000[IO0D_VOLUME_ID] = (byte)value;
 			break;
 		}
 		
 		case 0x0F: // Write0FZeroPageBankSwitch
 		{
 
 			byte oldzpbank = (byte) (fixedram0000[0x0F] & 0x07);
 			byte newzpbank = (byte) (value & 7);
 			if(oldzpbank != newzpbank) {
 				if(oldzpbank != 0) {
 					int old_zpptr = getZeroPagePointer(oldzpbank);
 					for(int i=0; i<64; i++) 
 						fixedram0000[old_zpptr+i] = fixedram0000[0x40+i];
 					if(newzpbank != 0) {
 						// GetZeroPagePointer(oldzpbank);
 						// Simply, we won't use the return value of 0
 						//    that might from GetZeroPagePointer.
 						int zp_ptr = getZeroPagePointer(newzpbank);
 						for(int i=0; i<64; i++) 
 							fixedram0000[0x40 + i] = fixedram0000[zp_ptr+i];
 					} else {
 						for(int i=0; i<64; i++)
 							fixedram0000[0x40 + i] = zp40cache[i];
 					}
 				} else {
 					for(int i=0; i<64; i++) zp40cache[i] = fixedram0000[i+0x40];
 					int zp_ptr = getZeroPagePointer(newzpbank);
 					for(int i=0; i<64; i++) fixedram0000[0x40+i] = fixedram0000[zp_ptr+i];
 				}
 			}
 			fixedram0000[0x0F/*ZP_BSW*/] = (byte)value;
 		}
 		
 		case 0x15: // Write15ControlPort1
 		{
 			fixedram0000[IO15_PORT1_DIR/*Port 1 direction*/] = (byte)value;
 			updateKeypadRegisters();
 			break;
 		}
 		
 		case 0x20: // Write20JG
 		{
 			if(value == (byte)0x80) fixedram0000[0x20] = 0;
 			else fixedram0000[0x20] = (byte)value;
 			break;
 		}
 		
 		default:
 			fixedram0000[pos] = (byte)value;
 			break;
 		}
 	}
 	
 	private static int getZeroPagePointer(byte bank) {
 		int ret = 0;
 		if(bank >= 4) {
 			ret = ((bank+4) << 6) & 0x0000FFFF;
 			/**  bank = 4    5    6    7
 			 *   ret  = 512  576  640  704
 			 *   ret  = 200h 240h 280h 2C0h
 			 */
 		} else {
 			ret = 0;
 		}
 		return ret;
 	}
 	/*// Eliminated!
 	final IOWrite[] iowrite = {
 		write00BankSwitch, // 0x00 - Bank Switch
 		nullWrite, // 0x01
 		write02Timer0Value, // TODO 0x02 - Timer0Value
 		nullWrite, // 0x03
 		nullWrite, // 0x04
 		write05ClockCtrl, // TODO 0x05 - Clock Control
 		write06LCDStartAddr, // TODO 0x06 - LCD Buffer address
 		nullWrite, // 0x07
 		write08Port0, // TODO 0x08 - IO Port 0
 		write09Port1, // TODO 0x09 - IO Port 1
 		write0AROABBS, // 0x0A - ROABBS
 		nullWrite, // 0x0B
 		write0CTimer01Control, // TODO 0x0C - Timer 01 Control
 		write0DVolumeIDLCDSegCtrl, // TODO 0x0D - Volume ID LCD Seg Control ?
 		nullWrite, // 0x0E
 		write0FZeroPageBankSwitch, // TODO 0x0F, Write 0 page bank switch
 		nullWrite, // 0x10
 		nullWrite, // 0x11
 		nullWrite, // 0x12
 		nullWrite, // 0x13
 		nullWrite, // 0x14
 		write15ControlPort1, // 0x15 Control Port 1
 		nullWrite, // 0x16
 		nullWrite, // 0x17
 		nullWrite, // 0x18
 		nullWrite, // 0x19
 		nullWrite, // 0x1A
 		nullWrite, // 0x1B
 		nullWrite, // 0x1C
 		nullWrite, // 0x1D
 		nullWrite, // 0x1E
 		nullWrite, // 0x1F
 		write20JG, // 0x20 JG
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x21 - 0x24
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x25 - 0x28
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x29 - 0x2C
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x2D - 0x30
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x31 - 0x34
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x35 - 0x38
 		nullWrite, nullWrite, nullWrite, nullWrite, // 0x39 - 0x3C
 		nullWrite, nullWrite, nullWrite,  // 0x3D - 0x3F
 	}; */
 }
