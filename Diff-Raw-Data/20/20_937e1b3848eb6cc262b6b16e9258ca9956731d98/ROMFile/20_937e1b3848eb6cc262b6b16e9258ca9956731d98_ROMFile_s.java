 package com.tnes;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ROMFile extends File {
 
 	private static final long serialVersionUID = 1L;
 	private Debugger debugger = Debugger.getInstance();
 	private int prgPageCount = 0, chrPageCount = 0;
 	private int romControl1 = 0, romControl2 = 0;
 	private int mapper = 0;
 	private List<List<Short>> prgROMPages = new ArrayList<List<Short>>();
 	private List<List<Short>> chrROMPages = new ArrayList<List<Short>>();
 	private int mirroring = 0;
 
 	public ROMFile(String pathname) {
 		super(pathname);
 
 		try {
 			FileInputStream stream = new FileInputStream(this);
 			byte[] fileType = new byte[4];
 			stream.read(fileType, 0, 4);
 			prgPageCount = stream.read();
 			chrPageCount = stream.read();
 			romControl1 = stream.read();
 			romControl2 = stream.read();
 			mapper = (romControl1 >> 4) | romControl2;
 			mirroring |= (romControl1 & 0x08) >> 2;
 			mirroring = (mirroring == 0) ? (romControl1 & 0x01) : mirroring;
 
 			// Load PRG-ROM and CHR-Pages into object variables
 			prgROMPages = new ArrayList<List<Short>>(prgPageCount);
 			chrROMPages = new ArrayList<List<Short>>(chrPageCount);
 
 			// Read in data for PRG-ROM pages
 			for (int i = 0; i < prgPageCount; i++) {
 				prgROMPages.add(i, new ArrayList<Short>(Constants.PRG_ROM_PAGE_SIZE));
 				for (int j = 0; j < Constants.PRG_ROM_PAGE_SIZE; j++) {
 					prgROMPages.get(i).add(j, (short) stream.read());
 					
 					if (prgROMPages.get(i).get(j) > 0xFF)
 						debugger.debugPrint("\nChar Value over 0xFF: " + prgROMPages.get(i).get(j));
 				}
 			}
 
 			// Read in data for PRG-ROM pages
 			for (int i = 0; i < chrPageCount; i++) {
 				chrROMPages.add(i, new ArrayList<Short>(Constants.CHR_ROM_PAGE_SIZE));
 				for (int j = 0; j < Constants.CHR_ROM_PAGE_SIZE; j++) {
 					chrROMPages.get(i).add(j, (short) stream.read());
 				}
 			}
 
 			stream.close();
 
 		} catch (FileNotFoundException e) {
 			debugger.debugPrint("Error opening ROM file: " + pathname);
 			debugger.debugPrint(e.getMessage());
 			debugger.readCommands();
 		} catch (IOException ioe) {
 			debugger.debugPrint("Error reading ROM file: " + pathname);
 			debugger.debugPrint(ioe.getMessage());
 			debugger.readCommands();
 		}
 
 		debugger.debugPrint(String.format("\nPRG-ROM Pages: %d", prgPageCount));
 		debugger.debugPrint(String.format("\nCHR-ROM Pages: %d", chrPageCount));
 		debugger.debugPrint(String.format("\nROM Control Byte #1: %s", Debugger.intToHex(romControl1)));
 		debugger.debugPrint(String.format("\nROM Control Byte #2: %s", Debugger.intToHex(romControl2)));
 		debugger.debugPrint(String.format("\nMapper: %d", mapper));
 		if (mirroring == 2) {
 			debugger.debugPrint("\nMirroring: Four Screen");
 		} else {
 			debugger.debugPrint(String.format("\nMirroring: %s", mirroring == 0 ? "Horizontal" : "Vertical"));
 		}
 
 	}
 
 	public int getPrgPageCount() {
 		return prgPageCount;
 	}
 
 	public int getChrPageCount() {
 		return chrPageCount;
 	}
 
 	public int getMapper() {
 		return mapper;
 	}
 
 	public int getROMControl1() {
 		return romControl1;
 	}
 
 	public int getROMControl2() {
 		return romControl2;
 	}
 
 	public List<List<Short>> getPrgROMPages() {
 		return prgROMPages;
 	}
 
 	public List<List<Short>> getChrROMPages() {
 		return chrROMPages;
 	}
 
 	public int getMirroring() {
 		return mirroring;
 	}
 
 }
