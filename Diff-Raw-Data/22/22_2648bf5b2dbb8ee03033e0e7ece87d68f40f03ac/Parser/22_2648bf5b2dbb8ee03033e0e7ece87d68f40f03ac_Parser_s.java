 /*
  * Parser.java Copyright (C) 2010 Stephanie Stroka 
  * 
  * This library is free software; you can redistribute it and/or modify it under 
  * the terms of the GNU Lesser General Public License as published by the 
  * Free Software Foundation; either version 2.1 of the License, or (at your option) 
  * any later version. 
  * 
  * This library is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
  * more details. 
  * 
  * You should have received a copy of the GNU Lesser General Public License along 
  * with this library; if not, write to the Free Software Foundation, Inc., 59 
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package catpiler.frontend.parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import catpiler.backend.codegeneration.CodeGenerator;
 import catpiler.backend.codegeneration.MemoryManager;
 import catpiler.backend.linker.Linker;
 import catpiler.frontend.exception.ParseException;
 import catpiler.frontend.exception.SyntaxException;
 import catpiler.frontend.parser.lib.ExtractFileHeader;
 import catpiler.frontend.parser.symboltable.Symboltable;
 import catpiler.frontend.parser.symboltable.SymboltableEntry;
 import catpiler.frontend.parser.symboltable.TypeItem;
 import catpiler.frontend.scanner.Scanner;
 import catpiler.frontend.scanner.keywords.A;
 import catpiler.frontend.scanner.keywords.ALL;
 import catpiler.frontend.scanner.keywords.AN;
 import catpiler.frontend.scanner.keywords.ANY;
 import catpiler.frontend.scanner.keywords.ArrayAccessOp;
 import catpiler.frontend.scanner.keywords.BIGGR;
 import catpiler.frontend.scanner.keywords.BOTH;
 import catpiler.frontend.scanner.keywords.CAN;
 import catpiler.frontend.scanner.keywords.CHAR;
 import catpiler.frontend.scanner.keywords.CHARZ;
 import catpiler.frontend.scanner.keywords.DIFF;
 import catpiler.frontend.scanner.keywords.DIFFRINT;
 import catpiler.frontend.scanner.keywords.DOWANT;
 import catpiler.frontend.scanner.keywords.DUZ;
 import catpiler.frontend.scanner.keywords.DerefOperator;
 import catpiler.frontend.scanner.keywords.EITHER;
 import catpiler.frontend.scanner.keywords.FAIL;
 import catpiler.frontend.scanner.keywords.FOUND;
 import catpiler.frontend.scanner.keywords.GIMMEH;
 import catpiler.frontend.scanner.keywords.GTFO;
 import catpiler.frontend.scanner.keywords.HAI;
 import catpiler.frontend.scanner.keywords.HAS;
 import catpiler.frontend.scanner.keywords.HOW;
 import catpiler.frontend.scanner.keywords.I;
 import catpiler.frontend.scanner.keywords.IF;
 import catpiler.frontend.scanner.keywords.IM;
 import catpiler.frontend.scanner.keywords.IN;
 import catpiler.frontend.scanner.keywords.IS;
 import catpiler.frontend.scanner.keywords.Identifier;
 import catpiler.frontend.scanner.keywords.Int;
 import catpiler.frontend.scanner.keywords.KTHXBYE;
 import catpiler.frontend.scanner.keywords.Keyword;
 import catpiler.frontend.scanner.keywords.MEBBE;
 import catpiler.frontend.scanner.keywords.MKAY;
 import catpiler.frontend.scanner.keywords.NO;
 import catpiler.frontend.scanner.keywords.NOOB;
 import catpiler.frontend.scanner.keywords.NOT;
 import catpiler.frontend.scanner.keywords.NOW;
 import catpiler.frontend.scanner.keywords.NUMBR;
 import catpiler.frontend.scanner.keywords.NUMBRZ;
 import catpiler.frontend.scanner.keywords.OF;
 import catpiler.frontend.scanner.keywords.OIC;
 import catpiler.frontend.scanner.keywords.ORLY;
 import catpiler.frontend.scanner.keywords.OUTTA;
 import catpiler.frontend.scanner.keywords.PRODUKT;
 import catpiler.frontend.scanner.keywords.QUOSHUNT;
 import catpiler.frontend.scanner.keywords.QuestionMark;
 import catpiler.frontend.scanner.keywords.R;
 import catpiler.frontend.scanner.keywords.RLY;
 import catpiler.frontend.scanner.keywords.SAEM;
 import catpiler.frontend.scanner.keywords.SAY;
 import catpiler.frontend.scanner.keywords.SMALLR;
 import catpiler.frontend.scanner.keywords.SO;
 import catpiler.frontend.scanner.keywords.STUFF;
 import catpiler.frontend.scanner.keywords.SUM;
 import catpiler.frontend.scanner.keywords.String;
 import catpiler.frontend.scanner.keywords.THATSIT;
 import catpiler.frontend.scanner.keywords.TIL;
 import catpiler.frontend.scanner.keywords.TROOF;
 import catpiler.frontend.scanner.keywords.TROOFZ;
 import catpiler.frontend.scanner.keywords.U;
 import catpiler.frontend.scanner.keywords.VISIBLE;
 import catpiler.frontend.scanner.keywords.WAI;
 import catpiler.frontend.scanner.keywords.WILE;
 import catpiler.frontend.scanner.keywords.WIN;
 import catpiler.frontend.scanner.keywords.YA;
 import catpiler.frontend.scanner.keywords.YOU;
 import catpiler.frontend.scanner.keywords.YR;
 import catpiler.utils.ErrorReporter;
 import catpiler.utils.StringLib;
 
 /**
  * The Parser checks whether the program follows
  * the specified EBNF.
  * This is a recursive descendant parser, which means that
  * it starts with the non-terminal start symbol and works
  * his way recursively down to the terminal symbols of the 
  * program.
  * 
  * @author Stephanie Stroka
  * 			(stephanie.stroka@sbg.ac.at)
  *
  */
 public class Parser {
 	
 	// junit test need scanner to be public
 	public Scanner s = null;
 	
 	int tmpSrcPointer = 0;
 	
 	// hack :(
 	int tmpSrcPointer2 = 0;
 	
 	public boolean parseTest = false;
 
 	private Symboltable symboltable;
 	
 	private CodeGenerator codeGenerator;
 	
 	private MemoryManager memoryManager;
 	
 	private int loadStmt;
 	
 	private boolean inFlowControl;
 	
 	private boolean inStruct;
 	
 	private int structOffset;
 	
 	private static boolean separateCompile;
 	
 	public static HashSet<java.lang.String> modules;
 	
 	public java.lang.String currentPath;
 	
 	public java.lang.String currentModule;
 	
 	private ExtractFileHeader extractFileHeader;
 	
 	private void init() {
 		memoryManager = new MemoryManager(codeGenerator);
 		if(symboltable == null) symboltable = new Symboltable("global", null);
 		symboltable.setLevel(0);
 		SymboltableEntry it = new SymboltableEntry();
 		it.setName("IT");
 		it.setCategory("reg");
 		it.setType(NOOB.tokenId);
 		it.setReg("$v1");
 		symboltable.put(it);
 		loadStmt = 0;
 		inFlowControl = false;
 		extractFileHeader = new ExtractFileHeader();
 		modules = new HashSet<java.lang.String>();
 	}
 	
 	public Parser() {
 		codeGenerator = new CodeGenerator(null);
 		init();
 	}
 	
 	public Parser(java.lang.String filename) {
 		java.lang.String outputName = filename.replace(".lol", ".cat");
 		codeGenerator = new CodeGenerator(outputName);
 		init();
 	}
 	
 	public SymboltableEntry currentSymboltableEntry;
 	
 	/**
 	 * Checks whether the keyword-token indicates the start of
 	 * a module-call <<CAN HAS identifier>>
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws ParseException
 	 */
 	public boolean isModuleImport(Keyword keyword) throws ParseException {
 		if(keyword instanceof CAN) {
 			if(lookAhead() instanceof HAS) {
 				adjustSrcPointer();
 				Keyword moduleKeyword = lookAhead();
 				if(moduleKeyword instanceof Identifier) {
 					adjustSrcPointer();
 				} else {
 					ErrorReporter.markError("Missing module");
 				}
 				
 				if(!separateCompile) {
 					Parser.loadSourcecode(currentPath + moduleKeyword.getAttribute() + ".lol");
 				}
 				
 			} else {
 				ErrorReporter.markError("Missing 'HAS' keyword");
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks whether the keyword-token indicates the start of 
 	 * a structure definition <<STUFF label ... THATSIT>>
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws ParseException
 	 */
 	public boolean isStruct(Keyword keyword) throws ParseException {
 		if(keyword instanceof STUFF) {
 			TypeItem type = new TypeItem();
 			Keyword id = lookAhead();
 			if(!isIdentifier(id)) {
 				ErrorReporter.markError("Missing structure label after 'STUFF'");
 			} else {
 				adjustSrcPointer();
 				// TODO add it to the list of types
 				type.setName(id.getAttribute());
 				symboltable.put(type);
 			}
 			
 			int lastLvl = symboltable.getLevel();
 			symboltable = new Symboltable(id.getAttribute(), symboltable);
 			symboltable.setLevel(lastLvl+1);
 			
 			Keyword next = null;
 			inStruct = true;
 			while(isVarInit(next = s.lookupToken()) || 
 					isVarDecl(next));
 			if(!(next instanceof THATSIT)) {
 				ErrorReporter.markError("Missing 'THATSIT' keyword");
 			}
 			inStruct = false;
 			// store the size of the record in another global data label
 			type.setSize(structOffset);
 			codeGenerator.storeData(id.getAttribute(), ".byte", new Integer(structOffset).toString());
 			
 			Set<java.lang.String> registers = symboltable.getRegInScopeLevel();
 			for(java.lang.String r : registers) {
 				codeGenerator.releaseRegister(r);
 			}
 			codeGenerator.nft = codeGenerator.nft - registers.size();
 			type.setSymbolTable(symboltable);
 			symboltable = symboltable.getOutterTable();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks whether the keyword-token indicates the start 
 	 * of a variable initialization <<I HAS A identifier>>
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws ParseException
 	 */
 	public boolean isVarInit(Keyword keyword) throws ParseException {
 		if(keyword instanceof I) {
 			if(!(lookAhead() instanceof HAS)) {
 				ErrorReporter.markError("Missing 'HAS' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			if(!(lookAhead() instanceof A)) {
 				ErrorReporter.markError("Missing 'A' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			Keyword id = lookAhead();
 			if(!(id instanceof Identifier)) {
 				ErrorReporter.markError("Missing variable");
 			} else {
 				adjustSrcPointer();
 				if(symboltable == null) {
 					symboltable = new Symboltable("main", null);
 					System.out.println("THIS SHOULD NEVER HAPPEN!!");
 				}
 
 				if(symboltable.get(id.getAttribute()) != null) {
 					ErrorReporter.markError("Variable " + id.getAttribute() + 
 							" has already been defined");
 				} else {
 					// found global variables -> stored in global .data segment
 					if(symboltable.getGlobalTable().equals(symboltable)) {
 						SymboltableEntry entry = new SymboltableEntry();
 						entry.setName(id.getAttribute());
 						entry.setType(NOOB.tokenId);
 						entry.setCategory("var");
 						entry.setAddress(id.getAttribute());
 					} else {
 						SymboltableEntry entry = new SymboltableEntry();
 						entry.setName(id.getAttribute());
 						entry.setType(NOOB.tokenId);
 						entry.setCategory("var");
 						if(!inStruct) {
 							/* I do not know the size for undefined types...
 							 * therefore I'll just create 1 word for a pointer 
 							 * (which then points to the actual data) */
 							int addr = new Integer(codeGenerator.getFp());
 							// set address relative to frame pointer
 							entry.setAddress(new Integer(codeGenerator.getSp() - addr).toString());
 							codeGenerator.decreaseSp(4);
 						} else {
 							// if we are currently defining a structure, the label gets 
 							// stored together with the memory offset in the global data segment
 							int addr = new Integer(codeGenerator.getFp());
 							entry.setAddress(new Integer(codeGenerator.getSp() - addr).toString());
 //							codeGenerator.decreaseSp(4);
 							codeGenerator.storeData(symboltable.getName()+"."+id.getAttribute(), 
 									".word", new Integer(structOffset).toString());
 							structOffset = structOffset + 4;
 							entry.setAddress(symboltable.getName()+"."+id.getAttribute());
 						}
 						symboltable.put(entry);
 					}
 				}
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks whether the whole program follows
 	 * the specified EBNF structure
 	 * The program may either start with a module-import,
 	 * a structure definition or directly with the main program.
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws ParseException
 	 */
 	public boolean isProgram(Keyword keyword) throws ParseException {
 		Keyword k = keyword;
 		while(isModuleImport(k)) {
 			k = s.lookupToken();
 		}
 		while(isStruct(k)) {
 			k = s.lookupToken();
 		}
 
 		
 		while(isVarInit(k) || isVarDecl(k)) {
 			k = s.lookupToken();
 		}
 		if(isMain(k)) {
 			k = s.lookupToken();
 		} 
 		while(isFunction(k))
 			k = s.lookupToken();
 		
 		return true;
 	}
 	
 	/**
 	 * Checks the consistency of the main part of the
 	 * program <<HAI ... KTHXBYE>>
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws SyntaxException
 	 * @throws ParseException
 	 */
 	public boolean isMain(Keyword keyword) throws ParseException {
 		if(keyword instanceof HAI) {
 			
 			int lastLvl = symboltable.getLevel();
 			symboltable = new Symboltable("main", symboltable);
 			symboltable.setLevel(lastLvl+1);
 			codeGenerator.put("main: ");
 			codeGenerator.setFPisSP();
 			
 			Keyword next = null;
 			while(isStatement(next = s.lookupToken()));
 			if(!(next instanceof KTHXBYE)) {
 				ErrorReporter.markError("Missing 'KTHXBYE' keyword");
 			}
 			
 			codeGenerator.setSPisFP();
 			// exit syscall:
 			codeGenerator.put("addi", "$v0", "$zero", "10");
 			codeGenerator.put("syscall");
 			
 			Set<java.lang.String> registers = symboltable.getRegInScopeLevel();
 			for(java.lang.String r : registers) {
 				codeGenerator.releaseRegister(r);
 			}
 			codeGenerator.nft = codeGenerator.nft - registers.size();
 			symboltable = symboltable.getOutterTable();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Checks whether the keyword-token indicates the 
 	 * start of a statement.
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws SyntaxException
 	 * @throws ParseException
 	 */
 	public boolean isStatement(Keyword keyword) throws ParseException {
 		if(isVarInit(keyword)) {
 			return true;
 		} else if(isVarDecl(keyword)) {
 			return true;
 		} else if(isVarAssign(keyword)) {
 			return true;
 		} else if(isPrint(keyword)) {
 			return true;
 		} else if(isGetline(keyword)) {
 			return true;
 		} else if(isFlowControl(keyword)) {
 			return true;
 		} else if(isOperation(keyword)) {
 			return true;
 		} else if(isExpr(keyword)) {
 			return true;
 		} else if(isFuncCall(keyword)) {
 			return true;
 		} else {
 			// search for the next known source keyword
			if(!(keyword instanceof KTHXBYE) ||
					!(keyword instanceof FOUND) ||
					!(keyword instanceof GTFO) ||
					!(keyword instanceof HOW) ||
					!(keyword instanceof OIC) ||
					!(keyword instanceof IF) ||
					!(keyword instanceof IM)) {
 				isStatement(s.lookupToken());
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Checks whether the given keyword-token indicates 
 	 * a function call
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws SyntaxException
 	 * @throws ParseException
 	 */
 	public boolean isFuncCall(Keyword keyword) throws ParseException {
 		if(keyword instanceof CAN) {
 			if(lookAhead() instanceof U) {
 				adjustSrcPointer();
 				
 				Keyword funcId = lookAhead();
 				if(!isIdentifier(funcId)) {
 					ErrorReporter.markError("Expected function name after 'CAN U' keyword");
 				}
 				adjustSrcPointer();
 				
 				Keyword k = s.lookupToken();
 				
 				StringBuilder argTypes = new StringBuilder();
 				ArrayList<SymboltableEntry> heapRef = new ArrayList<SymboltableEntry>();
 				
 				while(isOperation(k)) {
 					codeGenerator.clearArgs();
 					
 					k = s.lookupToken();
 					if(currentSymboltableEntry == null) {
 						currentSymboltableEntry = symboltable.get("IT");
 					}
 					
 					if(currentSymboltableEntry.getCategory().equals("heap") && 
 							currentSymboltableEntry.getLevel() != 0) {
 						heapRef.add(currentSymboltableEntry);
 						memoryManager.increaseReferenceCount(
 								currentSymboltableEntry.getHeap(), 
 								currentSymboltableEntry.getHeapsize());
 					}
 					/* store arguments in $a0 - $a3 */
 					if(currentSymboltableEntry.getCategory().equals("reg") && 
 							currentSymboltableEntry.getHeap() == null) {
 						codeGenerator.move2Args(currentSymboltableEntry.getReg());
 					} else if(currentSymboltableEntry.getHeap() != null) {
 						// again some hacking ... no time to think about something clever
 						java.lang.String reg = codeGenerator.loadAddress(currentSymboltableEntry.getHeap());
 						codeGenerator.move2Args(reg);
 					} else if(currentSymboltableEntry.getCategory().equals("const")) {
 						if(currentSymboltableEntry.getType().equals(NUMBR.tokenId)) {
 							java.lang.String reg = codeGenerator.loadImmediately(
 									currentSymboltableEntry.getAttribute());
 							codeGenerator.move2Args(reg);
 						} else if(currentSymboltableEntry.getType().equals(CHARZ.tokenId)) {
 							StringLib.storeString(currentSymboltableEntry);
 							heapRef.add(currentSymboltableEntry);
 							memoryManager.increaseReferenceCount(
 									currentSymboltableEntry.getHeap(), 
 									currentSymboltableEntry.getHeapsize());
 							codeGenerator.move2Args(currentSymboltableEntry.getReg());
 						}
 					} else if(currentSymboltableEntry.getCategory().equals("var")) {
 						java.lang.String reg = codeGenerator.loadWord(
 								currentSymboltableEntry.getAddress());
 						codeGenerator.move2Args(reg);
 					}
 					argTypes.append(" ");
 					argTypes.append(currentSymboltableEntry.getType());
 				}
 				
 				if(!(k instanceof QuestionMark)) {
 					ErrorReporter.markError("Expected '?' after function declaration");
 				}
 				
 				// linker must check whether function exists or not
 				codeGenerator.getFunctionCalls().add(funcId.getAttribute() + argTypes);
 				
 				/* combines one register with one stack-address */
 				HashMap<java.lang.String, java.lang.String> saveRegister = 
 					new HashMap<java.lang.String, java.lang.String>();
 				
 				if(!symboltable.getName().equals("main")) {
 					
 					// save $s0 - $s7 on the stack
 					for(int i=0; i<8; i++) {
 						java.lang.String address = new java.lang.String();
 						int addr = new Integer(codeGenerator.getFp());
 //						// set address relative to frame pointer
 						address = new Integer(codeGenerator.getSp() - addr).toString();
 						codeGenerator.decreaseSp(4);
 						codeGenerator.put("sw", "$s"+i, address + "($fp)");
 						saveRegister.put("$s"+i, address);
 					}
 				}
 				
 				/* combines one temporary register with one saver register */
 				HashMap<java.lang.String, SymboltableEntry> tmpRegister = 
 					new HashMap<java.lang.String, SymboltableEntry>();
 				
 				// save current symboltable entries in $s0 - $s7
 				codeGenerator.clearSavers();
 				Symboltable tmpTable = symboltable;
 				Symboltable globaleTable = symboltable.getGlobalTable();
 				while(tmpTable != globaleTable) {
 					for(java.lang.String var : tmpTable.getHashEntry().keySet()) {
 						SymboltableEntry ste = tmpTable.get(var);
 						if(ste.getCategory().equals("reg") && !ste.getName().equals("IT")) {
 							java.lang.String saver = codeGenerator.move2Savers(ste.getReg());
 							tmpRegister.put(ste.getReg(), ste);
 							codeGenerator.releaseRegister(ste.getReg());
 							ste.setReg(saver);
 						}
 					}
 					tmpTable = tmpTable.getOutterTable();
 				}
 				
 				// store return address in saver register
 				codeGenerator.put("move", "$s7", "$ra");
 				// store last frame pointer in saver register
 				codeGenerator.put("move", "$s6", "$fp");
 				
 				// call function
 				codeGenerator.put("jal", funcId.getAttribute());
 				
 				// restore frame pointer
 				codeGenerator.put("move", "$fp", "$s6");
 				// restore return address
 				codeGenerator.put("move", "$ra", "$s7");
 
 				/* move save register back to temp register */
 				for(java.lang.String treg : tmpRegister.keySet()) {
 					SymboltableEntry ste = tmpRegister.get(treg);
 					// treg = ste.getReg
 					codeGenerator.put("move", treg, ste.getReg());
 					ste.setReg(treg);
 				}
 				
 				/* move stack-content back to saver register */
 				for(java.lang.String savereg : saveRegister.keySet()) {
 					java.lang.String address = saveRegister.get(savereg);
 					codeGenerator.put("lw", savereg, address + "($fp)");
 				}
 				
 				for(SymboltableEntry ste : heapRef) {
 					memoryManager.decreaseReferenceCount(ste.getHeap(), ste.getHeapsize());
 				}
 				
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isPrint(Keyword keyword) throws ParseException {
 		if(keyword instanceof VISIBLE) {
 			
 			Keyword output = s.lookupToken();
 			if(isStringOp(output)) {
 				
 				if(currentSymboltableEntry.getName().equals("IT")) {
 					codeGenerator.put("addi", "$v0", "$zero", "4");
 					codeGenerator.put("move", "$a0", "$v1");
 					codeGenerator.put("lw", "$a0", "($a0)");
 				} else if(currentSymboltableEntry.getCategory().equals("heap")) {
 					if(currentSymboltableEntry.getReg() == null) {
 						codeGenerator.put("addi", "$v0", "$zero", "4");
 						codeGenerator.put("la", "$a0", currentSymboltableEntry.getHeap());
 						codeGenerator.put("lw", "$a0", "($a0)");
 					} else {
 						codeGenerator.put("addi", "$v0", "$zero", "4");
 						codeGenerator.put("move", "$a0", currentSymboltableEntry.getReg());
 						codeGenerator.put("lw", "$a0", "($a0)");
 					}
 				} else if(currentSymboltableEntry.getCategory().equals("reg")) {
 					codeGenerator.put("addi", "$v0", "$zero", "4");
 					codeGenerator.put("move", "$a0", currentSymboltableEntry.getReg());
 					codeGenerator.put("lw", "$a0", "($a0)");
 				} else if(currentSymboltableEntry.getCategory().equals("var")) {
 					/* address to heap is stored on stack 
 					 * -> add content of stack into register 
 					 * (this should be equivalent to loading the address)*/
 					codeGenerator.put("addi", "$v0", "$zero", "4");
 					// TODO: test whether relative address is ok
 					codeGenerator.put("lw", "$a0", currentSymboltableEntry.getAddress() + "($fp)");
 					codeGenerator.put("lw", "$a0", "($a0)");
 				} else if(currentSymboltableEntry.getCategory().equals("const")) {
 					StringLib.init(codeGenerator, memoryManager);
 					StringLib.storeString(currentSymboltableEntry);
 					codeGenerator.put("addi", "$v0", "$zero", "4");
 					codeGenerator.put("la", "$a0", currentSymboltableEntry.getHeap());
 					codeGenerator.put("lw", "$a0", "($a0)");
 //					// TODO: assign and load address
 //					int sizeOfStr = currentSymboltableEntry.getAttribute().length();
 //					
 //					int start = memoryManager.allocateMemory(sizeOfStr + 1);
 //	//				codeGenerator.decreaseSp(wordAligned);
 //					currentSymboltableEntry.setHeap("hp" + new Integer(start).toString());
 //					int blockAmount = 0;
 //					if(sizeOfStr % memoryManager.BLOCKSIZE != 0) {
 //						blockAmount = ((int) (sizeOfStr / memoryManager.BLOCKSIZE)) +1;
 //					} else {
 //						blockAmount = (int)( sizeOfStr / memoryManager.BLOCKSIZE);
 //					}
 //					currentSymboltableEntry.setHeapsize(blockAmount);
 //					currentSymboltableEntry.setCategory("heap");
 //					codeGenerator.put("la", "$a0", "hp" + new Integer(start).toString());
 				}
 			} else if(isNumOp(output)) {
 				
 				codeGenerator.put("addi", "$v0", "$zero", "1");
 				
 				if(currentSymboltableEntry.getCategory().equals("reg")) {
 					codeGenerator.put("move", "$a0", currentSymboltableEntry.getReg());
 				} else if(currentSymboltableEntry.getCategory().equals("var")) {
 					java.lang.String reg = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 					codeGenerator.put("move", "$a0", reg);
 					currentSymboltableEntry.setReg(reg);
 					currentSymboltableEntry.setCategory("reg");
 				} else if(currentSymboltableEntry.getCategory().equals("const")) {
 					java.lang.String reg = 
 							codeGenerator.loadImmediately(currentSymboltableEntry.getAttribute());
 					codeGenerator.put("move", "$a0", reg);
 					currentSymboltableEntry.setReg(reg);
 					currentSymboltableEntry.setCategory("reg");
 				}
 			} else {
 				ErrorReporter.markError("Expected a string");
 			}
 			codeGenerator.put("syscall");
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isGetline(Keyword keyword) throws ParseException {
 		if(keyword instanceof GIMMEH) {
 			Keyword lookahead = lookAhead();
 			if(isIdentifier(lookahead)) {
 				
 				adjustSrcPointer();
 				
 				SymboltableEntry ste = symboltable.get(lookahead.getAttribute());
 				if(ste != null) {
 					if(ste.getType().equals(NUMBR.tokenId)) {
 						codeGenerator.put("addi", "$v0", "$zero", "5");
 						codeGenerator.put("syscall");
 						if(ste.getCategory().equals("var")) {
 							java.lang.String reg = codeGenerator.loadWord(ste.getAddress());
 							ste.setReg(reg);
 							ste.setCategory("reg");
 						}
 						codeGenerator.put("move", ste.getReg(), "$v0");
 					} else if(ste.getType().equals(CHARZ.tokenId)) {
 						codeGenerator.put("addi", "$v0", "$zero", "8");
 						
 						if(ste.getCategory().equals("var")) {
 							java.lang.String reg = codeGenerator.loadWord(ste.getAddress());
 							ste.setReg(reg);
 						}
 						
 						if(ste.getHeap() == null) {
 							int size = 256;
 							int start = memoryManager.allocateMemory(size);
 							java.lang.String heap = "hp" + new Integer(start);
 							
 							int blocks;
 							if(size % memoryManager.BLOCKSIZE == 0) {
 								blocks = size / memoryManager.BLOCKSIZE;
 							} else {
 								blocks = size / memoryManager.BLOCKSIZE + 1;
 							}
 							ste.setHeapsize(blocks);
 							ste.setHeap(heap);
 						}
 						
 						java.lang.String temp = codeGenerator.getNextFreeTemporary();
 						codeGenerator.put("addi", "$a1", "$zero", new Integer(ste.getHeapsize() * memoryManager.BLOCKSIZE).toString());
 						codeGenerator.put("la", temp, ste.getHeap());
 						codeGenerator.put("lw", "$a0", "("+temp+")");
 						
 						codeGenerator.put("syscall");
 					} else {
 						ErrorReporter.markError("Cannot read variable of type " + ste.getType());
 					}
 					
 				} else {
 					ErrorReporter.markError("Variable " + 
 							lookahead.getAttribute() + " was not declared");
 				}
 			} else {
 				ErrorReporter.markError("Expected variable identifier");
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether the keyword indicates the start 
 	 * of an expression
 	 * 
 	 * @param keyword
 	 * @return
 	 * @throws SyntaxException
 	 * @throws ParseException
 	 */
 	public boolean isExpr(Keyword keyword) throws ParseException {
 		return (isBiExpr(keyword) || isInfExpr(keyword) );
 				//|| isBool(keyword) || isIdentifier(keyword));
 	}
 
 	/**
 	 * Checks whether the keyword indicates the start
 	 * of a boolean operation 
 	 * <<BOTH OF | EITHER OF expr AN expr>>
 	 * @param keyword
 	 * @return
 	 * @throws SyntaxException
 	 * @throws ParseException
 	 */
 	public boolean isBoolOp(Keyword keyword) throws ParseException {
 		if(keyword instanceof BOTH || keyword instanceof EITHER) {
 			if(!(lookAhead() instanceof OF)) {
 				if(keyword instanceof BOTH) {
 					return false;
 				} else {
 					ErrorReporter.markError("Missing 'OF' keyword");
 				}
 			} else {
 				adjustSrcPointer();
 			}
 			
 			java.lang.String op = null;
 			java.lang.String arg2 = null;
 			java.lang.String arg3 = null;
 			if(keyword instanceof BOTH)
 				op = new java.lang.String("and");
 			else if(keyword instanceof EITHER)
 				op = new java.lang.String("or");
 			
 			if(symboltable == null) { 
 				symboltable = new Symboltable("main", null);
 				System.out.println("THIS SHOULD NEVER HAPPEN");
 			}
 			
 			if(!isExpr(s.lookupToken())) {
 				ErrorReporter.markError("Expected Expression");
 			}
 			
 			if(!(currentSymboltableEntry == null || ErrorReporter.isError() == true)) {
 				
 				// store variable in register
 				if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("reg")) {
 					arg2 = currentSymboltableEntry.getReg();
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("var")) {
 					arg2 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 					loadStmt++;
 					// now set the attribute to the register name
 					currentSymboltableEntry.setReg(arg2);
 					currentSymboltableEntry.setCategory("reg");
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("const")) {
 					// switch arguments if one of them is a constant
 					arg2 = null;
 					op = op + "i";
 					
 					arg3 = currentSymboltableEntry.getAttribute();
 				} else {
 					ErrorReporter.markError("Compiler is missing the symbol category");
 				}
 			} else {
 				if(currentSymboltableEntry == null)
 					ErrorReporter.markError("Variable not inititalized");
 			}
 			
 			
 			if(!(lookAhead() instanceof AN)) {
 				ErrorReporter.markError("Missing 'AN' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!isExpr(s.lookupToken())) {
 				ErrorReporter.markError("Expected expression");
 			}
 			
 			
 			// found a symbol?
 			if(!(currentSymboltableEntry == null || ErrorReporter.isError() == true)) {
 				
 				// store variable in register
 				if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("reg")) {
 					if(arg2 == null) {
 						arg2 = currentSymboltableEntry.getReg();
 					} else {
 						arg3 = currentSymboltableEntry.getReg();
 					}
 					
 					currentSymboltableEntry = symboltable.get("IT");
 					currentSymboltableEntry.setType(TROOF.tokenId);
 					java.lang.String arg1 = currentSymboltableEntry.getReg();
 					codeGenerator.put(op, arg1, arg2, arg3);
 					
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("var")) {
 					// currentKeyword.getAddress() contains address - let's load it into a register
 					if(arg2 == null) {
 						arg2 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 						loadStmt++;
 						// now set the attribute to the register name
 						currentSymboltableEntry.setReg(arg2);
 						currentSymboltableEntry.setCategory("reg");
 					} else {
 						arg3 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 						loadStmt++;
 						// now set the attribute to the register name
 						currentSymboltableEntry.setReg(arg3);
 						currentSymboltableEntry.setCategory("reg");
 					}
 					
 					currentSymboltableEntry = symboltable.get("IT");
 					currentSymboltableEntry.setType(TROOF.tokenId);
 					java.lang.String arg1 = currentSymboltableEntry.getReg();
 					codeGenerator.put(op, arg1, arg2, arg3);
 					
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("const")) {
 					
 					// if both arguments are constants, don't generate any code 
 					if(arg2 == null) {
 						if(op.equals("andi")) {
 							java.lang.String arg1;
 							if(currentSymboltableEntry.getAttribute().equals("1")) {
 								if(arg3.equals("0")) {
 									arg1 = "0";
 								} else {
 									arg1 = "1";
 								}
 							} else {
 								arg1 = "0";
 							}
 							
 							currentSymboltableEntry = new SymboltableEntry();
 							currentSymboltableEntry.setName("");
 							currentSymboltableEntry.setType("TROOF");
 							currentSymboltableEntry.setCategory("const");
 							currentSymboltableEntry.setAttribute(arg1);
 							
 						} else if(op.equals("ori")) {
 							java.lang.String arg1;
 							if(currentSymboltableEntry.getAttribute().equals("1")) {
 								arg1 = "1";
 							} else if(currentSymboltableEntry.getAttribute().equals("0")) {
 								if(arg3.equals("0")) {
 									arg1 = "0";
 								} else {
 									arg1 = "1";
 								}
 							} else {
 								arg1 = "0";
 							}
 							
 							currentSymboltableEntry = new SymboltableEntry();
 							currentSymboltableEntry.setName("");
 							currentSymboltableEntry.setType("TROOF");
 							currentSymboltableEntry.setCategory("const");
 							currentSymboltableEntry.setAttribute(arg1);
 						}
 					} else {
 						op = op + "i";
 						if(currentSymboltableEntry.getAttribute().equals("1")) {
 							arg3 = "1";
 						} else {
 							arg3 = "0";
 						}
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(TROOF.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 					}
 				} else {
 					ErrorReporter.markError("Compiler is missing the symbol category");
 				}
 			} else {
 				if(currentSymboltableEntry == null)
 					ErrorReporter.markError("Variable not inititalized");
 			}
 			
 			return true;
 		} else if(keyword instanceof NOT) {
 			if(!isExpr(s.lookupToken())) {
 				ErrorReporter.markError("Expected expression");
 			}
 			
 			if(!ErrorReporter.isError()) {
 				if(currentSymboltableEntry.getCategory().equals("const")) {
 					if(currentSymboltableEntry.getAttribute().equals("1")) {
 						currentSymboltableEntry.setAttribute("0");
 					} else {
 						currentSymboltableEntry.setAttribute("1");
 					}
 				} else { 
 					java.lang.String arg1;
 					if(currentSymboltableEntry.getCategory().equals("var")) {
 						arg1 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 						loadStmt++;
 						currentSymboltableEntry.setReg(arg1);
 						currentSymboltableEntry.setCategory("reg");
 					} else {
 						arg1 = currentSymboltableEntry.getReg();
 					}
 					// arg3 = !arg3 == arg3 = nor(arg3,0)
 					codeGenerator.put("nor", arg1, currentSymboltableEntry.getReg(), "0");
 				}
 			}
 			
 			return true;
 		} else if(isBool(keyword)) {
 			if(!ErrorReporter.isError()) {
 				currentSymboltableEntry = new SymboltableEntry();
 				currentSymboltableEntry.setName("");
 				if(keyword.getAttribute().equals("1")) {
 					currentSymboltableEntry.setAttribute("1");
 				} else {
 					currentSymboltableEntry.setAttribute("0");
 				}
 				currentSymboltableEntry.setCategory("const");
 				currentSymboltableEntry.setType("TROOF");
 			}
 			return true;
 		} else if(isIdentifier(keyword)) {
 			
 			if(symboltable.exists(keyword.getAttribute())) {
 				SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 				if(!ste.getType().equals(TROOF.tokenId) && !ste.getType().equals(NOOB.tokenId)) {
 					//ErrorReporter.markError("Type mismatch for variable " + ste.getName());
 					return false;
 				} else if(ste.getType().equals(NOOB.tokenId)) {
 					ste.setType(TROOF.tokenId);
 				}
 				
 				if(!ErrorReporter.isError()) {
 					if(ste.getAttribute() == null || inFlowControl) {
 						// load variable if no value exists
 						java.lang.String reg;
 						if(ste.getCategory().equals("var")) {
 							reg = codeGenerator.loadWord(ste.getAddress());
 							loadStmt++;
 //							reg = codeGenerator.loadImmediately(0);
 							ste.setReg(reg);
 							ste.setCategory("reg");
 						} else {
 							reg = ste.getReg();
 						}
 						if(ste.getAttribute() != null) {
 							codeGenerator.put("addi", ste.getReg(), "$zero", ste.getAttribute());
 						}
 					} else {
 						ste.setCategory("const");
 					}
 					currentSymboltableEntry = ste;
 				}
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isGenExpr(Keyword keyword) throws ParseException {
 		Keyword lookahead = lookAhead();
 		if(keyword instanceof DIFFRINT || 
 			(keyword instanceof BOTH && lookahead instanceof SAEM) ) {
 			if(keyword instanceof BOTH) {
 				adjustSrcPointer();
 			}
 			if(!isOperation(s.lookupToken())) {
 				ErrorReporter.markError("Expected a value to compare");
 			}
 			
 			SymboltableEntry ste1 = currentSymboltableEntry;
 			
 			if(!(lookAhead() instanceof AN)) {
 				ErrorReporter.markError("Missing 'AN' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!isOperation(s.lookupToken())) {
 				ErrorReporter.markError("Expected a value to compare");
 			}
 			
 			SymboltableEntry ste2 = currentSymboltableEntry;
 			
 			if(!ErrorReporter.isError()) {
 				if(!ste1.getType().equals(ste2.getType())) {
 					ErrorReporter.markError("Type mismatch. Cannot compare " +
 							ste1.getType() + " and " + ste2.getType());
 				}
 				
 				if(ste1.getType().equals(NUMBR.tokenId) || 
 						ste1.getType().equals(CHAR.tokenId) ||
 						ste1.getType().equals(TROOF.tokenId)) {
 					// if both arguments are constants, we just compare them without creating code
 					if(ste1.getCategory().equals("const") && 
 							ste2.getCategory().equals("const")) {
 						currentSymboltableEntry = new SymboltableEntry();
 						currentSymboltableEntry.setName("");
 						currentSymboltableEntry.setCategory("const");
 						currentSymboltableEntry.setType(TROOF.tokenId);
 						if(keyword instanceof DIFFRINT) {
 							if(ste1.getAttribute().equals(ste2.getAttribute())) {
 								currentSymboltableEntry.setAttribute("0");
 								codeGenerator.put("addi", "$v1", "$zero", "0");
 							} else {
 								currentSymboltableEntry.setAttribute("1");
 								codeGenerator.put("addi", "$v1", "$zero", "1");
 							}
 						} else if(keyword instanceof BOTH) {
 							if(ste1.getAttribute().equals(ste2.getAttribute())) {
 								currentSymboltableEntry.setAttribute("1");
 								codeGenerator.put("addi", "$v1", "$zero", "1");
 							} else {
 								currentSymboltableEntry.setAttribute("0");
 								codeGenerator.put("addi", "$v1", "$zero", "0");
 							}
 						}
 					} else {
 						// load ste1 in register, if necessary
 						if(ste1.getCategory().equals("var")) {
 							java.lang.String reg = codeGenerator.loadWord(ste1.getAddress());
 							ste1.setReg(reg);
 							ste1.setCategory("reg");
 						} else if(ste1.getCategory().equals("const")) {
 							java.lang.String reg = codeGenerator.loadImmediately(ste1.getAttribute());
 							ste1.setReg(reg);
 							ste1.setCategory("reg");
 						}
 						
 						// load ste2 in register, if necessary
 						if(ste2.getCategory().equals("var")) {
 							java.lang.String reg = codeGenerator.loadWord(ste2.getAddress());
 							ste2.setReg(reg);
 							ste2.setCategory("reg");
 						} else if(ste2.getCategory().equals("const")) {
 							java.lang.String reg = codeGenerator.loadImmediately(ste2.getAttribute());
 							ste2.setReg(reg);
 							ste2.setCategory("reg");
 						}
 						
 						// generate code for != or == expression
 						java.lang.String brLabel1 = codeGenerator.getBranchLabel();
 						java.lang.String brLabel2 = codeGenerator.getBranchLabel();
 						if(keyword instanceof BOTH) {
 							codeGenerator.put("bne", ste1.getReg(), ste2.getReg(), brLabel1);
 							codeGenerator.put("addi", "$v1", "$zero", "1");
 							codeGenerator.put("j", brLabel2);
 							codeGenerator.put(brLabel1 + ":");
 							codeGenerator.put("add", "$v1", "$zero", "$zero");
 							codeGenerator.put("j", brLabel2);
 							codeGenerator.put(brLabel2 + ":");
 						} else if(keyword instanceof DIFFRINT) {
 							codeGenerator.put("beq", ste1.getReg(), ste2.getReg(), brLabel1);
 							codeGenerator.put("addi", "$v1", "$zero", "1");
 							codeGenerator.put("j", brLabel2);
 							codeGenerator.put(brLabel1 + ":");
 							codeGenerator.put("add", "$v1", "$zero", "$zero");
 							codeGenerator.put("j", brLabel2);
 							codeGenerator.put(brLabel2 + ":");
 						}
 					}
 				} else if(ste1.getType().equals(NUMBRZ.tokenId) ||
 						ste1.getType().equals(CHARZ.tokenId) ||
 						ste1.getType().equals(TROOFZ.tokenId)) {
 					// if both arguments are constants, we just compare them without creating code
 					if(ste1.getCategory().equals("const") && 
 							ste2.getCategory().equals("const")) {
 						currentSymboltableEntry = new SymboltableEntry();
 						currentSymboltableEntry.setName("");
 						currentSymboltableEntry.setCategory("const");
 						currentSymboltableEntry.setType(TROOF.tokenId);
 						if(keyword instanceof DIFFRINT) {
 							if(ste1.getAttribute().equals(ste2.getAttribute())) {
 								currentSymboltableEntry.setAttribute("0");
 								codeGenerator.put("addi", "$v1", "$zero", "0");
 							} else {
 								currentSymboltableEntry.setAttribute("1");
 								codeGenerator.put("addi", "$v1", "$zero", "1");
 							}
 						} else if(keyword instanceof BOTH) {
 							if(ste1.getAttribute().equals(ste2.getAttribute())) {
 								currentSymboltableEntry.setAttribute("1");
 								codeGenerator.put("addi", "$v1", "$zero", "1");
 							} else {
 								currentSymboltableEntry.setAttribute("0");
 								codeGenerator.put("addi", "$v1", "$zero", "0");
 							}
 						}
 					} else {
 						// TODO: compare strings, number-arrays and boolean-arrays
 						// - strings can be compared as in StringLib.strcmp()
 						// - number- and boolean arrays 
 						StringLib.init(codeGenerator, memoryManager);
 						StringLib.strCmp(ste1, ste2);
 					}
 				}
 			}
 			
 			return true;
 		} else {
 			if(keyword instanceof BOTH && 
 							!(lookAhead() instanceof SAEM || 
 									lookAhead() instanceof OF)) {
 				ErrorReporter.markError("Missing 'SAEM' OR 'OF' keyword");
 			}
 			return false;
 		}
 	}
 
 	public boolean isBool(Keyword keyword) {
 		if(keyword instanceof WIN) {
 			keyword.setAttribute("1");
 			return true;
 		} else if(keyword instanceof FAIL) {
 			keyword.setAttribute("0");
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isInfExpr(Keyword keyword) throws ParseException {
 		if(keyword instanceof ALL || keyword instanceof ANY) {
 			
 			if(!(lookAhead() instanceof OF)) {
 				ErrorReporter.markError("Missing 'OF' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!isBoolOp(s.lookupToken())) {
 				ErrorReporter.markError("Expected boolean variable");
 			}
 			
 			if(!(lookAhead() instanceof AN)) {
 				ErrorReporter.markError("Missing 'AN' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(isBoolOp(s.lookupToken()))) {
 				ErrorReporter.markError("Too few arguments");
 			}
 			
 			Keyword lookupTok = lookAhead();
 			while(!(lookupTok instanceof MKAY)) {
 				if(!(lookupTok instanceof AN)) {
 					ErrorReporter.markError("Missing 'AN' or 'MKAY' keyword");
 					break;
 				} else {
 					adjustSrcPointer();
 					if(!isBoolOp(s.lookupToken())) {
 						ErrorReporter.markError("Expected boolean variable");
 					}
 					lookupTok = lookAhead();
 				}
 			}
 			if(lookupTok instanceof MKAY) {
 				adjustSrcPointer();
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isBiExpr(Keyword keyword) throws ParseException {
 		if(isBoolOp(keyword) || isGenExpr(keyword)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isFlowControl(Keyword keyword) throws ParseException {
 		boolean ret = false;
 		inFlowControl = true;
 		ret = (isIf(keyword) || isLoop(keyword));
 		if(symboltable.getLevel() <= 1)
 			inFlowControl = false;
 		return ret;
 	}
 
 	public boolean isLoop(Keyword keyword) throws ParseException {
 		java.lang.String loopLabel = null;
 		if(keyword instanceof IM) {
 			
 			if(!(lookAhead() instanceof IN)) {
 				return false;
 			} else {
 				adjustSrcPointer();
 			}
 			
 			int lastLvl = symboltable.getLevel();
 			symboltable = new Symboltable("main", symboltable);
 			symboltable.setLevel(lastLvl+1);
 			boolean skipLoop = false;
 			boolean needFixUp = false;
 			int jumpFixup = 0; 
 			java.lang.String branchFixup = new java.lang.String();
 			
 			if(!(lookAhead() instanceof YR)) {
 				ErrorReporter.markError("Missing 'YR' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			Keyword id_loop = lookAhead();
 			if(!(id_loop instanceof Identifier)) {
 				ErrorReporter.markError("Loop does not have a name");
 				// search for loop end and quit
 				while(!(s.lookupToken() instanceof IM) && !(lookAhead() instanceof OUTTA));
 				
 				if(s.lookupToken() instanceof OUTTA);
 				if(s.lookupToken() instanceof YR);
 				if(isIdentifier(s.lookupToken()));
 				return true;
 			} else {
 				adjustSrcPointer();
 				loopLabel = id_loop.getAttribute();
 			}
 			
 			codeGenerator.put("j", loopLabel);
 			codeGenerator.put(loopLabel + ": ");
 			
 			Keyword tok = s.lookupToken();
 			if(tok instanceof YR) {
 				if(!(lookAhead() instanceof Identifier)) {
 					ErrorReporter.markError("Too few arguments");
 				} else {
 					adjustSrcPointer();
 				}
 				tok = s.lookupToken();
 			}
 			
 			jumpFixup = codeGenerator.getPc();
 			loadStmt = 0;
 			if(tok instanceof WILE || tok instanceof TIL) {
 				if(!isExpr(s.lookupToken())) {
 					ErrorReporter.markError("No limiting expression provided");
 				}
 				jumpFixup = jumpFixup + loadStmt;
 				loadStmt = 0;
 				// Expression, stored in $v1
 				// Branch on equal to end of loop (store loopoffset)
 				// Jump on end of loop to first expression
 				if(currentSymboltableEntry == null) {
 					currentSymboltableEntry = symboltable.get("IT");
 				} else if(currentSymboltableEntry.getCategory().equals("const")) {
 					if(currentSymboltableEntry.getAttribute().equals("0")) {
 						System.out.println("Skipping else if and else statements");
 						skipLoop = true;
 					}
 				} else {
 					branchFixup = codeGenerator.getBranchLabel();
 					
 					if(tok instanceof WILE) {
 						// if $v1 = 0 -> skip loop
 						codeGenerator.put("beq", "$zero", "$v1", branchFixup);
 					} else if(tok instanceof TIL) {
 						// if $v1 = 1 -> skip loop
 						codeGenerator.put("bgtz", "$v1", branchFixup);
 					}
 					needFixUp = true;
 				}
 				tok = s.lookupToken();
 			}
 			
 			// temporarily switch off code generation, if skipIf is true
 			// switching of code generation is realized by setting the 
 			// error flag to true, even if there is no error - 
 			// hope that does not create confusion
 			if(skipLoop && ErrorReporter.isError() == false) {
 				ErrorReporter.setError(true);
 			} else {
 				skipLoop = false;
 			}
 			
 			while(isStatement(tok)) {
 				tok = s.lookupToken();
 				if(currentSymboltableEntry == null)
 					currentSymboltableEntry = symboltable.get("IT");
 //				if(currentSymboltableEntry.isNeedLoad()) {
 //					if(currentSymboltableEntry.getCategory().equals("var")) {
 //						java.lang.String reg = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 //						currentSymboltableEntry.setReg(reg);
 //						currentSymboltableEntry.setCategory("reg");
 //					}
 //					codeGenerator.put(
 //							"addi", 
 //							currentSymboltableEntry.getReg(), 
 //							"$zero", 
 //							currentSymboltableEntry.getAttribute());
 //					currentSymboltableEntry.setNeedLoad(false);
 //				}
 			}
 			
 			if(skipLoop) {
 				ErrorReporter.setError(true);
 			}
 			
 			if(!(tok instanceof IM)) {
 				ErrorReporter.markError("Expected loop end");
 			}
 			
 			if(!(lookAhead() instanceof OUTTA)) {
 				ErrorReporter.markError("Missing 'OUTTA' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(lookAhead() instanceof YR)) {
 				ErrorReporter.markError("Missing 'YR' label");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			Keyword id = lookAhead();
 			if(!(id instanceof Identifier)) {
 				ErrorReporter.markError("No loop end label provided");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(loopLabel.equals(id.getAttribute()))) {
 				ErrorReporter.markError("Loop end label does " +
 					"not match the loop begin label");
 			}
 			
 			if(!ErrorReporter.isError()) {
 				codeGenerator.put("j", loopLabel);
 			}
 			if(needFixUp && !ErrorReporter.isError()) {
 				codeGenerator.put(branchFixup + ":");
 //				codeGenerator.fixUp(branchFixup, codeGenerator.getPc() - branchFixup);
 			}
 			
 			Set<java.lang.String> registers = symboltable.getRegInScopeLevel();
 			for(java.lang.String r : registers) {
 				codeGenerator.releaseRegister(r);
 			}
 			codeGenerator.nft = codeGenerator.nft - registers.size();
 			symboltable = symboltable.getOutterTable();
 			
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isIf(Keyword keyword) throws ParseException {
 		if(keyword instanceof ORLY) {
 			
 			boolean skipIf = false;
 			boolean skipElse = false;
 			boolean skipFollowing = false;
 			boolean needFixUp = false;
 			java.lang.String fixupLine = new java.lang.String();
 			java.lang.String jump2End = codeGenerator.getBranchLabel();
 			
 //			for(java.lang.String sym : symboltable.getHashEntry().keySet()) {
 //				SymboltableEntry ste = symboltable.getHashEntry().get(sym);
 //				if(ste.isNeedLoad()) {
 //					if(ste.getCategory().equals("var")) {
 //						java.lang.String reg = codeGenerator.loadWord(ste.getAddress());
 //						ste.setReg(reg);
 //						ste.setCategory("reg");
 //					} else {
 //						// store if currentSymboltableEntry != IT && category = reg
 //						if(ste.getCategory().equals("reg") &&
 //								!ste.getName().equals("IT")) {
 //							java.lang.String reg = codeGenerator.loadWord(ste.getAddress());
 //							if(ste.getAddress() == null) {
 //								/* I do not know the size for undefined types...
 //								 * therefore I'll just create 1 word for a pointer 
 //								 * (which then points to the actual data) */
 //								int addr = new Integer(codeGenerator.getFp());
 //								// set address relative to frame pointer
 //								ste.setAddress(new Integer(codeGenerator.getSp() - addr).toString());
 //								codeGenerator.decreaseSp(4);
 //							}
 //							codeGenerator.put("sw", ste.getReg(), ste.getAddress());
 //						}
 //					}
 //					
 //					if(ste.getAttribute() != null) {
 //						codeGenerator.put(
 //								"addi", 
 //								ste.getReg(), 
 //								"$zero", 
 //								ste.getAttribute());
 //					} else if(ste.getAttribute() == null) {
 //						codeGenerator.put(
 //								"move", "$v1", 
 //								ste.getReg());
 //					}
 //					
 //					ste.setNeedLoad(false);
 //				}
 //			}
 			
 			if(currentSymboltableEntry ==  null) {
 				currentSymboltableEntry = symboltable.get("IT");
 			}
 			if(currentSymboltableEntry.getName().equals("") && 
 					currentSymboltableEntry.getCategory().equals("const")) {
 				if(currentSymboltableEntry.getAttribute().equals("1")) {
 					System.out.println("Skipping else if and else statements");
 					skipElse = true;
 					skipFollowing = true;
 				} else {
 					System.out.println("Skipping if statement");
 					skipIf = true;
 				}
 			} else {
 				fixupLine = codeGenerator.getBranchLabel();
 				codeGenerator.put("beq", "$zero", "$v1", fixupLine);
 				needFixUp = true;
 			}
 			
 			if(!(lookAhead() instanceof YA)) {
 				ErrorReporter.markError("Missing 'YA' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(lookAhead() instanceof RLY)) {
 				ErrorReporter.markError("Missing 'RLY' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			int lastLvl = symboltable.getLevel();
 			symboltable = new Symboltable(symboltable.getName()+"_if", symboltable);
 			symboltable.setLevel(lastLvl+1);
 			
 			// temporarily switch off code generation, if skipIf is true
 			// switching of code generation is realized by setting the 
 			// error flag to true, even if there is no error - 
 			// hope that does not create confusion
 			if(skipIf && ErrorReporter.isError() == false) {
 				ErrorReporter.setError(true);
 			} else {
 				skipIf = false;
 			}
 			
 			Keyword k = null;
 			if(!isStatement(k = s.lookupToken())) {
 				ErrorReporter.markError("Expected statement after condition");
 			}
 			
 			while(isStatement(k = s.lookupToken()));
 			
 			if(!skipIf && !ErrorReporter.isError()) {
 				if(currentSymboltableEntry.getType().equals(NUMBR.tokenId) || 
 						currentSymboltableEntry.getType().equals(TROOF.tokenId) ||
 						currentSymboltableEntry.getType().equals(CHAR.tokenId)) {
 					if(!currentSymboltableEntry.getName().equals("IT") && currentSymboltableEntry.getCategory().equals("const")) {
 						java.lang.String result = currentSymboltableEntry.getAttribute();
 						currentSymboltableEntry = symboltable.get("IT");
 						codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", result);
 					}
 				} else if(currentSymboltableEntry.getType().equals(NUMBRZ.tokenId) || 
 						currentSymboltableEntry.getType().equals(TROOFZ.tokenId) ||
 						currentSymboltableEntry.getType().equals(CHARZ.tokenId) ||
 						symboltable.existsType(currentSymboltableEntry.getType())) {
 					if(currentSymboltableEntry.getCategory().equals("const")) {
 						StringLib.storeString(currentSymboltableEntry);
 					}
 				}
 //				if(currentSymboltableEntry.isNeedRefresh()) {
 //					if(currentSymboltableEntry.getCategory().equals("var")) {
 //						java.lang.String reg = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 //						currentSymboltableEntry.setReg(reg);
 //						currentSymboltableEntry.setCategory("reg");
 //					} else {
 //						// store if currentSymboltableEntry != IT && category = reg
 //						if(currentSymboltableEntry.getCategory().equals("reg") &&
 //								!currentSymboltableEntry.getName().equals("IT")) {
 //							java.lang.String reg = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 //							if(currentSymboltableEntry.getAddress() == null) {
 //								/* I do not know the size for undefined types...
 //								 * therefore I'll just create 1 word for a pointer 
 //								 * (which then points to the actual data) */
 //								int addr = new Integer(codeGenerator.getFp());
 //								// set address relative to frame pointer
 //								currentSymboltableEntry.setAddress(new Integer(codeGenerator.getSp() - addr).toString());
 //								codeGenerator.decreaseSp(4);
 //							}
 //							codeGenerator.put("sw", currentSymboltableEntry.getReg(), currentSymboltableEntry.getAddress());
 //						}
 //					}
 //					if(currentSymboltableEntry.getAttribute() != null) {
 //						codeGenerator.put(
 //								"addi", 
 //								currentSymboltableEntry.getReg(), 
 //								"$zero", 
 //								currentSymboltableEntry.getAttribute());
 //					} else if(currentSymboltableEntry.getAttribute() == null) {
 //						codeGenerator.put(
 //								"move", "$v1", 
 //								currentSymboltableEntry.getReg());
 //					}
 //					
 //					currentSymboltableEntry.setNeedRefresh(false);
 //				}
 				if(!skipFollowing) {
 					codeGenerator.put("j", jump2End);
 				}
 			}
 			
 			// switch code generation on again
 			if(skipIf) {
 				ErrorReporter.setError(false);
 			}
 			
 //				if(skipElse && error == false) {
 //					error = true;
 //				} else {
 //					skipElse = false;
 //				}
 			
 			Set<java.lang.String> registersIf = symboltable.getRegInScopeLevel();
 			for(java.lang.String r : registersIf) {
 				codeGenerator.releaseRegister(r);
 			}
 			codeGenerator.nft = codeGenerator.nft - registersIf.size();
 			symboltable = symboltable.getOutterTable();
 			
 			while(k instanceof MEBBE) {
 				
 				int lastLvl2 = symboltable.getLevel();
 				symboltable = new Symboltable(symboltable.getName()+"_elseif", symboltable);
 				symboltable.setLevel(lastLvl2+1);
 				
 				if(skipElse && ErrorReporter.isError() == false) {
 					ErrorReporter.setError(true);
 				} else {
 					skipElse = false;
 					System.out.println("Computing else statement");
 				}
 				
 				if(needFixUp && !ErrorReporter.isError()) {
 					codeGenerator.put(fixupLine + ":");
 				}
 				
 				if(!isExpr(s.lookupToken())) {
 					ErrorReporter.markError("Expected expression after 'MEBBE'");
 				}
 				
 //				if(!ErrorReporter.isError() && currentSymboltableEntry.isNeedRefresh()) {
 //					if(currentSymboltableEntry.getCategory().equals("reg")) {
 //						if(currentSymboltableEntry.getAttribute() != null) {
 //							codeGenerator.put(
 //									"addi", 
 //									currentSymboltableEntry.getReg(), 
 //									"$zero", 
 //									currentSymboltableEntry.getAttribute());
 //						} else if(currentSymboltableEntry.getAttribute() == null) {
 //							codeGenerator.put(
 //									"move", "$v1", 
 //									currentSymboltableEntry.getReg());
 //						}
 //					}
 //				}
 				
 				if(currentSymboltableEntry ==  null) {
 					currentSymboltableEntry = symboltable.get("IT");
 				}
 				if(!ErrorReporter.isError() && currentSymboltableEntry.getCategory().equals("const")) {
 					if(!skipFollowing) {
 						if(currentSymboltableEntry.getAttribute().equals("1")) {
 							skipFollowing = true;
 							System.out.println("Skipping the following statements");
 						} else {
 							skipElse = true;
 							ErrorReporter.setError(true);
 							System.out.println("Skipping this statement");
 						}
 					} else {
 						System.out.println("Skipped following statement");
 					}
 				}
 				
 				if(!isStatement(k = s.lookupToken())) {
 					ErrorReporter.markError("Expected statement after condition");
 				}
 				
 				while(isStatement(k = s.lookupToken()));
 				
 				if(!ErrorReporter.isError()) {
 					if(currentSymboltableEntry.getType().equals(NUMBR.tokenId) || 
 							currentSymboltableEntry.getType().equals(TROOF.tokenId) ||
 							currentSymboltableEntry.getType().equals(CHAR.tokenId)) {
 						if(!currentSymboltableEntry.getName().equals("IT") && currentSymboltableEntry.getCategory().equals("const")) {
 							java.lang.String result = currentSymboltableEntry.getAttribute();
 							currentSymboltableEntry = symboltable.get("IT");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", result);
 						}
 					} else if(currentSymboltableEntry.getType().equals(NUMBRZ.tokenId) || 
 							currentSymboltableEntry.getType().equals(TROOFZ.tokenId) ||
 							currentSymboltableEntry.getType().equals(CHARZ.tokenId) ||
 							symboltable.existsType(currentSymboltableEntry.getType())) {
 						if(currentSymboltableEntry.getCategory().equals("const")) {
 							StringLib.storeString(currentSymboltableEntry);
 						}
 					}
 					if(!skipFollowing) {
 //						codeGenerator.fixUp(jumpFixupLine, 
 //								codeGenerator.getPc() - jumpFixupLine);
 //						jumpFixupLine = codeGenerator.getPc();
 						codeGenerator.put("j", jump2End);
 					} else {
 						fixupLine = codeGenerator.getBranchLabel();
 						codeGenerator.put("j", fixupLine);
 						needFixUp = true;
 					}
 				}
 				
 				if(skipElse && !skipFollowing) {
 					ErrorReporter.setError(false);
 					skipElse = false;
 				} else if(skipFollowing) {
 					skipElse = true;
 				}
 				
 				Set<java.lang.String> registers = symboltable.getRegInScopeLevel();
 				for(java.lang.String r : registers) {
 					codeGenerator.releaseRegister(r);
 				}
 				codeGenerator.nft = codeGenerator.nft - registers.size();
 				symboltable = symboltable.getOutterTable();
 			}
 			if(k instanceof NO) {
 				
 				int lastLvl3 = symboltable.getLevel();
 				symboltable = new Symboltable(symboltable.getName()+"_else", symboltable);
 				symboltable.setLevel(lastLvl3+1);
 				
 				if(skipElse && ErrorReporter.isError() == false) {
 					ErrorReporter.setError(true);
 				} else {
 					skipElse = false;
 				}
 				
 				if(!(lookAhead() instanceof WAI)) {
 					ErrorReporter.markError("Missing 'WAI' keyword");
 				} else {
 					adjustSrcPointer();
 				}
 				
 				if(needFixUp && !ErrorReporter.isError()) {
 					codeGenerator.put(fixupLine + ":");
 				}
 				if(!isStatement(k = s.lookupToken())) {
 					ErrorReporter.markError("Expected statement after condition");
 				}
 				
 				while(isStatement(k = s.lookupToken()));
 				
 				if(!ErrorReporter.isError()) {
 					if(currentSymboltableEntry.getType().equals(NUMBR.tokenId) || 
 							currentSymboltableEntry.getType().equals(TROOF.tokenId) ||
 							currentSymboltableEntry.getType().equals(CHAR.tokenId)) {
 						if(!currentSymboltableEntry.getName().equals("IT") && currentSymboltableEntry.getCategory().equals("const")) {
 							java.lang.String result = currentSymboltableEntry.getAttribute();
 							currentSymboltableEntry = symboltable.get("IT");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", result);
 						}
 					} else if(currentSymboltableEntry.getType().equals(NUMBRZ.tokenId) || 
 							currentSymboltableEntry.getType().equals(TROOFZ.tokenId) ||
 							currentSymboltableEntry.getType().equals(CHARZ.tokenId) ||
 							symboltable.existsType(currentSymboltableEntry.getType())) {
 						if(currentSymboltableEntry.getCategory().equals("const")) {
 							StringLib.storeString(currentSymboltableEntry);
 						}
 					}
 
 					fixupLine = codeGenerator.getBranchLabel();
 					codeGenerator.put("j", fixupLine);
 					needFixUp = true;
 				}
 				
 				Set<java.lang.String> registers = symboltable.getRegInScopeLevel();
 				for(java.lang.String r : registers) {
 					codeGenerator.releaseRegister(r);
 				}
 				codeGenerator.nft = codeGenerator.nft - registers.size();
 				symboltable = symboltable.getOutterTable();
 			}
 			
 			if(needFixUp && !ErrorReporter.isError()) {
 				codeGenerator.put(fixupLine + ":");
 			}
 			
 			if(skipElse || skipFollowing) {
 				ErrorReporter.setError(false);
 			}
 			
 			if(!(k instanceof OIC)) {
 				ErrorReporter.markError("Missing 'OIC' keyword");
 			}
 			
 			if(needFixUp && !ErrorReporter.isError()) {
 //				codeGenerator.fixUp(fixupLine, codeGenerator.getPc() - fixupLine);
 				codeGenerator.put("j", jump2End);
 				codeGenerator.put(jump2End + ":");
 			}
 			
 //			symboltable = symboltable.getOutterTable();
 			
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isOperation(Keyword keyword) throws ParseException {
 		return isNumArray(keyword) || isBoolArray(keyword) ||
 				isNumOp(keyword) || isBoolOp(keyword) || isStringOp(keyword);
 	}
 
 	public boolean isStringOp(Keyword keyword) {
 		if(keyword instanceof String) {
 			if(!ErrorReporter.isError()) {
 				currentSymboltableEntry = new SymboltableEntry();
 				currentSymboltableEntry.setName("");
 				currentSymboltableEntry.setType(CHARZ.tokenId);
 				currentSymboltableEntry.setCategory("const");
 				currentSymboltableEntry.setAttribute(keyword.getAttribute());
 			}
 			return true;
 		} else if(isDerefIdentifier(keyword)) {
 			if(currentSymboltableEntry.getType().equals(CHARZ.tokenId))
 				return true;
 			else {
 				s.setSrcPointer(tmpSrcPointer2);
 				return false;
 			}
 		} else if(keyword instanceof Identifier) {
 			if(symboltable == null) {
 				symboltable = new Symboltable("main", null);
 				System.out.println("THIS SHOULD NEVER HAPPEN");
 			}
 			if(symboltable.exists(keyword.getAttribute())) {
 				SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 				if(!ste.getType().equals(CHARZ.tokenId) && !ste.getType().equals(NOOB.tokenId)) {
 					//ErrorReporter.markError("Type mismatch for variable " + ste.getName());
 					return false;
 				} else if(ste.getType().equals(NOOB.tokenId)) {
 					ste.setType(CHARZ.tokenId);
 				}
 				if(!ErrorReporter.isError()) {
 					if(ste.getAttribute() == null) {
 						// load variable if no value exists
 						java.lang.String reg;
 						if(ste.getCategory().equals("var")) {
 //							reg = codeGenerator.loadImmediately(0);
 							reg = codeGenerator.loadWord(ste.getAddress());
 							loadStmt++;
 							/* determine whether the loaded variable is in 
 							 * lowest scope and add register address, 
 							 * if this is the case.
 							 * The check must be done to ensure that the
 							 * variable gets loaded into a register if it is
 							 * defined in an upper scope, loaded in a lower scope
 							 * and used in another lower scope as well.
 							 */
 							if(symboltable.isInScopeLevel(ste.getName())) {
 								ste.setReg(reg);
 								ste.setCategory("reg");
 								currentSymboltableEntry = ste;
 							} else {
 								// add symbol to current (lowest) scope
 								SymboltableEntry tmpSte = new SymboltableEntry();
 								tmpSte.setName(ste.getName());
 								tmpSte.setReg(reg);
 								tmpSte.setCategory("reg");
 								tmpSte.setType(ste.getType());
 								symboltable.put(tmpSte);
 								currentSymboltableEntry = tmpSte;
 							}
 						} else {
 							currentSymboltableEntry = ste;
 						}
 					} else {
 //						ste.setCategory("const");
 //						ste.setNeedRefresh(true);
 						currentSymboltableEntry = ste;
 					}
 				}
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isNumOp(Keyword keyword) throws ParseException {
 		if(keyword instanceof SUM || keyword instanceof DIFF ||
 				keyword instanceof QUOSHUNT || keyword instanceof PRODUKT ||
 				keyword instanceof BIGGR || keyword instanceof SMALLR) {
 			
 			if(!(lookAhead() instanceof OF)) {
 				ErrorReporter.markError("Missing 'OF' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			java.lang.String op = null;
 			java.lang.String arg2 = null;
 			java.lang.String arg3 = null;
 			boolean arg2IsConst = false;
 			if(keyword instanceof SUM)
 				op = new java.lang.String("add");
 			else if(keyword instanceof DIFF || keyword instanceof BIGGR || keyword instanceof SMALLR)
 				op = new java.lang.String("sub");
 			else if(keyword instanceof PRODUKT)
 				op = new java.lang.String("mult");
 			else if(keyword instanceof QUOSHUNT)
 				op = new java.lang.String("div");
 			
 			if(symboltable == null) {
 				symboltable = new Symboltable("main", null);
 				System.out.println("THIS SHOULD NEVER HAPPEN");
 			}
 			
 			if(!isNumOp(s.lookupToken())) {
 				ErrorReporter.markError("Expected numerical operation or number");
 			}
 			
 			// look up identifier in the symboltable and store argument in register or
 			// calculate immediately
 			if(!(currentSymboltableEntry == null || ErrorReporter.isError() == true)) {
 			
 				// store variable in register
 				if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("reg")) {
 					if(currentSymboltableEntry.getAttribute() == null || inFlowControl) {
 						arg2 = currentSymboltableEntry.getReg();
 					} else {
 						currentSymboltableEntry.setCategory("const");
 						// switch arguments if one of them is a constant
 						op = op + "i";
 						arg3 = currentSymboltableEntry.getAttribute();
 						arg2IsConst = true;
 					}
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("var")) {
 					// currentKeyword.getAddress() contains address - let's load it into a register
 					arg2 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 					loadStmt++;
 					// now set the attribute to the register name
 					currentSymboltableEntry.setReg(arg2);
 					currentSymboltableEntry.setCategory("reg");
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("const")) {
 					// switch arguments if one of them is a constant
 					op = op + "i";
 					arg3 = currentSymboltableEntry.getAttribute();
 					arg2IsConst = true;
 				} else {
 					ErrorReporter.markError("Compiler is missing the symbol category");
 				}
 			} else {
 				if(currentSymboltableEntry == null)
 					ErrorReporter.markError("Variable not inititalized");
 			}
 			
 			// after the first parameter, we are expecting an AN keyword
 			if(!(lookAhead() instanceof AN)) {
 				ErrorReporter.markError("Missing 'AN' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 
 			// moving v1 to tmp-reg to avoid overriding after second numop
 			if(arg2 == null && arg3 != null && arg3.equals("$v1")) { 
 				java.lang.String reg = codeGenerator.getNextFreeTemporary();
 				codeGenerator.put("move", reg, arg3);
 				arg3 = reg;
 			} else if(arg3 == null && arg2 != null && arg2.equals("$v1")) {
 				java.lang.String reg = codeGenerator.getNextFreeTemporary();
 				codeGenerator.put("move", reg, arg2);
 				arg2 = reg;
 			}
 			
 			// ...and a second parameter
 			if(!isNumOp(s.lookupToken())) {
 				ErrorReporter.markError("Expected numerical operation or number");
 			}
 			
 			// found a symbol?
 			if(!(currentSymboltableEntry == null || ErrorReporter.isError() == true)) {
 				
 				if(currentSymboltableEntry.getAttribute() != null && !inFlowControl) {
 					currentSymboltableEntry.setCategory("const");
 				}
 				
 				// store variable in register
 				if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("reg")) {
 					// if the first parameter was a constant
 					if(arg2IsConst) {
 						// when operation is '-' or '/', we cannot make use of the commutative law
 						// thus, load the number into a register
 						if(op.equals("subi") || op.equals("divi")) {
 							arg2 = codeGenerator.loadImmediately(arg3);
 							op = op.substring(0, op.length()-1);
 							arg2IsConst = false;
 						}
 					}
 					// if the first parameter is still a constant...
 					if(arg2IsConst) {
 						
 						arg2 = currentSymboltableEntry.getReg();
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(NUMBR.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 						if(keyword instanceof BIGGR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero",  arg3);
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							codeGenerator.put(branchlabel + ":");
 						} else if(keyword instanceof SMALLR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", arg3);
 							codeGenerator.put(branchlabel + ":");
 						}
 					} else {
 						arg3 = currentSymboltableEntry.getReg();
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(NUMBR.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 						if(keyword instanceof BIGGR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg3, "0");
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							codeGenerator.put(branchlabel + ":");
 						} else if(keyword instanceof SMALLR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg3, "0");
 							codeGenerator.put(branchlabel + ":");
 						}
 					}
 					
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("var")) {
 					
 					// if the first parameter was a constant
 					if(arg2IsConst) {
 						// when operation is '-' or '/', we cannot make use of the commutative law
 						// thus, load the number into a register
 						if(op.equals("subi") || op.equals("divi")) {
 							arg2 = codeGenerator.loadImmediately(arg3);
 							op = op.substring(0, op.length()-1);
 							arg2IsConst = false;
 						}
 					}
 					
 					if(arg2IsConst) {
 						// currentKeyword.getAddress() contains address - let's load it into a register
 						arg2 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 						loadStmt++;
 						// now set the attribute to the register name
 						currentSymboltableEntry.setReg(arg2);
 						currentSymboltableEntry.setCategory("reg");
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(NUMBR.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 						if(keyword instanceof BIGGR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", arg3);
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							codeGenerator.put(branchlabel + ":");
 						} else if(keyword instanceof SMALLR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", arg3);
 							codeGenerator.put(branchlabel + ":");
 						}
 					} else {
 						arg3 = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 						loadStmt++;
 						// now set the attribute to the register name
 						currentSymboltableEntry.setReg(arg3);
 						currentSymboltableEntry.setCategory("reg");
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(NUMBR.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 						if(keyword instanceof BIGGR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg3, "0");							
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							codeGenerator.put(branchlabel + ":");
 						} else if(keyword instanceof SMALLR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg3, "0");
 							codeGenerator.put(branchlabel + ":");
 						}
 					}
 					
 				} else if(currentSymboltableEntry.getCategory() != null && 
 						currentSymboltableEntry.getCategory().equals("const")) {
 					
 					// if both arguments are constants, don't generate any code 
 					if(arg2IsConst) {
 						if(op.equals("addi")) {
 							Integer arg1 = new Integer(arg3) + 
 								new Integer(currentSymboltableEntry.getAttribute());
 							
 							currentSymboltableEntry = new SymboltableEntry();
 							currentSymboltableEntry.setName("");
 							currentSymboltableEntry.setType("NUMBR");
 							currentSymboltableEntry.setCategory("const");
 							currentSymboltableEntry.setAttribute(arg1.toString());
 							
 						} else if(op.equals("subi")) {
 							// the following is necessary to store the arg2 value for BIGGR/SMALLR comparison
 							arg2 = currentSymboltableEntry.getAttribute();
 							Integer arg1 = new Integer(arg3) - 
 								new Integer(currentSymboltableEntry.getAttribute());
 							
 							currentSymboltableEntry = new SymboltableEntry();
 							currentSymboltableEntry.setName("");
 							currentSymboltableEntry.setType("NUMBR");
 							currentSymboltableEntry.setCategory("const");
 							currentSymboltableEntry.setAttribute(arg1.toString());
 							
 						} else if(op.equals("divi")) {
 							if(currentSymboltableEntry.getAttribute().equals("0")) {
 								ErrorReporter.markError("Divison by 0");
 							} else {
 								Integer arg1 = new Integer(arg3) / 
 									new Integer(currentSymboltableEntry.getAttribute());
 								
 								currentSymboltableEntry = new SymboltableEntry();
 								currentSymboltableEntry.setName("");
 								currentSymboltableEntry.setType("NUMBR");
 								currentSymboltableEntry.setCategory("const");
 								currentSymboltableEntry.setAttribute(arg1.toString());
 								
 							}
						} else if(op.equals("muli")) {
 							Integer arg1 = new Integer(arg3) * 
 								new Integer(currentSymboltableEntry.getAttribute());
 							
 							currentSymboltableEntry = new SymboltableEntry();
 							currentSymboltableEntry.setName("");
 							currentSymboltableEntry.setType("NUMBR");
 							currentSymboltableEntry.setCategory("const");
 							currentSymboltableEntry.setAttribute(arg1.toString());
 							
 						}
 						
 						if(keyword instanceof BIGGR) {
 							if(new Integer(currentSymboltableEntry.getAttribute()) > 0) {
 								currentSymboltableEntry.setAttribute(arg3);
 							} else {
 								currentSymboltableEntry.setAttribute(arg2);
 							}
 						} else if(keyword instanceof SMALLR) {
 							if(new Integer(currentSymboltableEntry.getAttribute()) > 0) {
 								currentSymboltableEntry.setAttribute(arg2);
 							} else {
 								currentSymboltableEntry.setAttribute(arg3);
 							}
 						}
 //						currentSymboltableEntry.setNeedRefresh(true);
 					} else {
 						op = op + "i";
 						arg3 = currentSymboltableEntry.getAttribute();
 						
 						currentSymboltableEntry = symboltable.get("IT");
 						currentSymboltableEntry.setType(NUMBR.tokenId);
 						java.lang.String arg1 = currentSymboltableEntry.getReg();
 						codeGenerator.put(op, arg1, arg2, arg3);
 						
 						if(keyword instanceof BIGGR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", arg3);
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");
 							codeGenerator.put(branchlabel + ":");
 						} else if(keyword instanceof SMALLR) {
 							codeGenerator.put("bgtz", currentSymboltableEntry.getReg(), "3");
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), arg2, "0");							
 							java.lang.String branchlabel = codeGenerator.getBranchLabel();
 							codeGenerator.put("j", branchlabel);
 							codeGenerator.put("addi", currentSymboltableEntry.getReg(), "$zero", arg3);
 							codeGenerator.put(branchlabel + ":");
 						}
 					}
 				} else {
 					ErrorReporter.markError("Compiler is missing the symbol category");
 				}
 			} else {
 				if(currentSymboltableEntry == null)
 					ErrorReporter.markError("Variable not inititalized");
 			}
 			
 			return true;
 		} else if(isNum(keyword)) {
 			if(!ErrorReporter.isError()) {
 				currentSymboltableEntry = new SymboltableEntry();
 				currentSymboltableEntry.setName("");
 				currentSymboltableEntry.setType("NUMBR");
 				currentSymboltableEntry.setCategory("const");
 				currentSymboltableEntry.setAttribute(keyword.getAttribute());
 			}
 			return true;
 		} else if(isArrayAccess(keyword)) {
 			SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 			if(ste.getType().equals(NUMBRZ.tokenId))
 				return true;
 			else 
 				return false;
 		}  else if(isDerefIdentifier(keyword)) {
 			if(currentSymboltableEntry.getType().equals(NUMBR.tokenId))
 				return true;
 			else {
 				s.setSrcPointer(tmpSrcPointer2);
 				return false;
 			}
 		} else if(isIdentifier(keyword)) {
 			if(symboltable == null) {
 				symboltable = new Symboltable("main", null);
 				System.out.println("THIS SHOULD NEVER HAPPEN");
 			}
 			if(symboltable.exists(keyword.getAttribute())) {
 				SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 				if(!ste.getType().equals("NUMBR") && !ste.getType().equals("NOOB")) {
 					//ErrorReporter.markError("Type mismatch for variable " + ste.getName());
 					return false;
 				} else if(ste.getType().equals("NOOB")) {
 					ste.setType("NUMBR");
 				}
 				
 				if(!ErrorReporter.isError()) {
 //					if(ste.getAttribute() == null) {
 						// load variable if no value exists
 						java.lang.String reg;
 						if(ste.getCategory().equals("var")) {
 //							reg = codeGenerator.loadImmediately(0);
 							reg = codeGenerator.loadWord(ste.getAddress());
 							ste.setReg(reg);
 							ste.setCategory("reg");
 							loadStmt++;
 							/* determine whether the loaded variable is in 
 							 * lowest scope and add register address, 
 							 * if this is the case.
 							 * The check must be done to ensure that the
 							 * variable gets loaded into a register if it is
 							 * defined in an upper scope, loaded in a lower scope
 							 * and used in another lower scope as well.
 							 */
 //							if(symboltable.isInScopeLevel(ste.getName())) {
 //								ste.setReg(reg);
 //								ste.setCategory("reg");
 //								currentSymboltableEntry = ste;
 //							} else {
 								// add symbol to current (lowest) scope
 //								SymboltableEntry tmpSte = new SymboltableEntry();
 //								tmpSte.setName(ste.getName());
 //								tmpSte.setReg(reg);
 //								tmpSte.setCategory("reg");
 //								tmpSte.setType(ste.getType());
 //								symboltable.put(tmpSte);
 //								currentSymboltableEntry = tmpSte;
 //							}
 //						} else {
 						}
 						currentSymboltableEntry = ste;
 //						}
 //					} else {
 //						ste.setCategory("const");
 //						currentSymboltableEntry = ste;
 //					}
 				}
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isPointerArray(Keyword keyword) {
 		if(keyword instanceof Identifier) { 
 			SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 			if(ste != null) {
 				if(ste.getType().equals(NUMBRZ.tokenId) || 
 						ste.getType().equals(TROOFZ.tokenId) ||
 						ste.getType().equals(CHARZ.tokenId))
 					return true;
 				else
 					return false;
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 		}
 		return false;
 	}
 	
 	public boolean isBoolArray(Keyword keyword) {
 		if(keyword instanceof Identifier) { 
 			SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 			if(ste != null) {
 				if(ste.getType().equals(TROOFZ.tokenId))
 					return true;
 				else
 					return false;
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 		} else if(isBool(keyword)) {
 			if(isBool(lookAhead())) {
 				adjustSrcPointer();
 				while(isBool(lookAhead())) adjustSrcPointer();
 				return true;
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	public boolean isNumArray(Keyword keyword) {
 		if(keyword instanceof Identifier) { 
 			SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 			if(ste != null) {
 				if(ste.getType().equals(NUMBRZ.tokenId))
 					return true;
 				else
 					return false;
 			} else {
 				ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 			}
 		} else if(keyword instanceof Int) {
 			if(lookAhead() instanceof Int) {
 				adjustSrcPointer();
 				while(lookAhead() instanceof Int) adjustSrcPointer();
 				return true;
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isNum(Keyword keyword) {
 		return keyword instanceof Int;
 	}
 	
 	public boolean isVarAssign(Keyword keyword) throws ParseException {
 		boolean deref = isDerefIdentifier(keyword);
 		boolean arrayacc = false;
 		if(!deref)
 			arrayacc = isArrayAccess(keyword);
 		if(deref || arrayacc || isIdentifier(keyword)) {
 			if(symboltable == null) {
 				symboltable = new Symboltable("main", null);
 				System.out.println("THIS SHOULD NEVER HAPPEN");
 			}
 			
 			if(lookAhead() instanceof R) {
 				adjustSrcPointer();
 				
 				SymboltableEntry ste;
 				if(deref || arrayacc) {
 					ste = currentSymboltableEntry;
 				} else {
 					ste = symboltable.get(((Identifier) keyword).getAttribute());
 				}
 				
 				Keyword k = s.lookupToken();
 				if(!isMemAlloc(k) && !isOperation(k)) {
 					ErrorReporter.markError("Expected value");
 				}
 				
 				if(currentSymboltableEntry == null) {
 					currentSymboltableEntry = symboltable.get("IT");
 				}
 				
 				if(ste == null)
 					ErrorReporter.markError("Unknown variable " + ((Identifier) keyword).getAttribute());
 				if(ste != null) {
 					if(ste.getType().equals(NOOB.tokenId) || 
 							ste.getType().equals(currentSymboltableEntry.getType()) ||
 							currentSymboltableEntry.getName().equals("IT")) {
 					
 						if(ste.getCategory().equals("var")) {
 							// load variable
 							java.lang.String reg = codeGenerator.
 								loadWord(ste.getAddress());
 							ste.setCategory("reg");
 							ste.setReg(reg);
 						}
 						
 						if(ste.getCategory().equals("heap")) {
 							// loads address of heap-label
 							java.lang.String reg = codeGenerator.
 								loadAddress(ste.getHeap());
 							// loads the address of the heap start by loading the value behind the address of the heap-level
 							codeGenerator.put("lw", reg, "("+reg+")");
 							// load start address
 							ste.setCategory("reg");
 							ste.setReg(reg);
 						}
 						
 						if(currentSymboltableEntry.getCategory().equals("const")) {
 							if(currentSymboltableEntry.getAttribute() != null)
 								ste.setAttribute(currentSymboltableEntry.getAttribute());
 							else
 								ste.setAttribute("0");
 							if(currentSymboltableEntry.getType() != null)
 								ste.setType(currentSymboltableEntry.getType());
 							
 							if(ste.getType().equals(NUMBR.tokenId) || 
 									ste.getType().equals(TROOF.tokenId) ||
 									ste.getType().equals(CHAR.tokenId)) {
 								codeGenerator.put("addi", ste.getReg(), "$zero", ste.getAttribute());
 								if(ste.getName().equals("IT")) {
 									ste.setAttribute(null);
 								}
 							} else if(ste.getType().equals(CHARZ.tokenId)) {
 								StringLib.init(codeGenerator, memoryManager);
 								StringLib.storeString(ste);
 							}
 							
 						} else if(currentSymboltableEntry.getCategory().equals("heap") || 
 								currentSymboltableEntry.getCategory().equals("var")) {
 							if(currentSymboltableEntry.getCategory().equals("var") && currentSymboltableEntry.getReg() == null) {
 								java.lang.String reg = codeGenerator.loadWord(currentSymboltableEntry.getAddress());
 								currentSymboltableEntry.setReg(reg);
 								ste.setAddress(currentSymboltableEntry.getAddress());
 							} else if(currentSymboltableEntry.getCategory().equals("heap") && currentSymboltableEntry.getReg() == null) {
 								java.lang.String reg = codeGenerator.loadAddress(currentSymboltableEntry.getHeap());
 								currentSymboltableEntry.setReg(reg);
 								ste.setHeap(currentSymboltableEntry.getHeap());
 							}
 							codeGenerator.put("move", ste.getReg(), 
 									currentSymboltableEntry.getReg());
 							if(currentSymboltableEntry.getType() != null)
 								ste.setType(currentSymboltableEntry.getType());
 							ste.setCategory("reg");
 						} else if(currentSymboltableEntry.getCategory().equals("reg")) {
 							codeGenerator.put("move", ste.getReg(), 
 									currentSymboltableEntry.getReg());
 						} 
 						// if we got a dynamic type, we also have to store it on the heap
 						if(ste.getName().equals("IT") &&
 								ste.getAddress() != null) {
 							if(ste.getHeap() != null) {
 								// TODO: continue here!!!
 								java.lang.String reg = codeGenerator.loadAddress(ste.getHeap());
 								currentSymboltableEntry.setReg(reg);
 								codeGenerator.put("move", ste.getReg(), 
 										currentSymboltableEntry.getReg());
 							}
 							codeGenerator.put("sw", 
 									ste.getReg(), 
 									"(" + ste.getAddress() + ")");
 							ste.setAddress(null);
 						}
 					}
 				}
 //				currentSymboltableEntry = ste;
 				return true;
 			} else {
 				ErrorReporter.markError("Missing 'R' or 'IS NOW A' keywords after variable");
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isMemAlloc(Keyword keyword) {
 		if(keyword instanceof DOWANT) {
 			Keyword lookahead = lookAhead();
 			if(symboltable.existsType(lookahead.getAttribute())) {
 				adjustSrcPointer();
 				TypeItem type = symboltable.getType(lookahead.getAttribute());
 				int heap_id = memoryManager.allocateMemory(type.getSize());
 				java.lang.String heap_label = "hp" + new Integer(heap_id).toString();
 				SymboltableEntry ste = new SymboltableEntry();
 				ste.setHeap(heap_label);
 				ste.setCategory("heap");
 				ste.setType(type.getName());
 				currentSymboltableEntry = ste;
 			} else {
 				ErrorReporter.markError("Type " + lookahead.getAttribute() + " does not exist");
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	
 	private boolean checkTypeConversions(java.lang.String lastType, Keyword newType, Identifier identifier) {
 		TypeConversion.init(codeGenerator, symboltable, memoryManager);
 		if(isType(newType)) {
 			// IT can always be re-typed - leave responsibility 
 			// of type conversions to the programmer
 			if(identifier.getAttribute().equals("IT")) {
 				SymboltableEntry ste = symboltable.get("IT");
 				ste.setType(newType.getTokenID());
 				return true;
 			}
 			if(lastType == null) lastType = NOOB.tokenId;
 			if(!lastType.equals(NOOB.tokenId)) {
 				// change type of identifier from integer to...
 				if(lastType.equals(NUMBR.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						return false;
 					}
 					// ... int (no change)
 					else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						return true;
 					}
 					// ... int[]
 					else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						return false;
 					}
 					// ... boolean
 					else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						TypeConversion.int2bool(identifier);
 						return true;
 					}
 					// ... boolean[]
 					else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						return false;
 					}
 					// ... a char
 					else if(newType.getTokenID().equals(CHAR.tokenId)) {
 						TypeConversion.int2char(identifier);
 						return true;
 					} else if(newType.getTokenID().equals(CHARZ.tokenId)) {
 						if(!isNum(lookAhead())) {
 							ErrorReporter.markError("Missing size of char array");
 						} else {
 							// if the size has changed, assign new heap space and copy each word
 							int arraySize = new Integer(lookAhead().getAttribute());
 							TypeConversion.int2string(identifier, arraySize);
 						}
 						return true;
 					}
 				}
 				// change type of identifier from int[] to ...
 				else if(lastType.equals(NUMBRZ.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						TypeConversion.intarray2null(identifier);
 						return true;
 					} else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						if(!isNum(lookAhead())) {
 							ErrorReporter.markError("Missing size of char array");
 						} else {
 							// if the size has changed, assign new heap space and copy each word
 							int arraySize = new Integer(lookAhead().getAttribute());
 							int blocks = 0;
 							if(arraySize % memoryManager.BLOCKSIZE == 0)
 								blocks = arraySize / memoryManager.BLOCKSIZE;
 							else
 								blocks = ((int) arraySize / memoryManager.BLOCKSIZE) + 1;
 							SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 							if(ste != null) {
 								if(blocks > ste.getHeapsize()) {
 									java.lang.String heapStart = "hp" + 
 										new Integer(memoryManager.allocateMemory(blocks)).toString();
 									// load word aus ste.getHeap in reg
 									java.lang.String regFrom = codeGenerator.getNextFreeTemporary();
 									codeGenerator.put("la", regFrom, ste.getHeap());
 									java.lang.String regTo = codeGenerator.getNextFreeTemporary();
 									codeGenerator.put("la", regTo, heapStart);
 									for(int i=0; i<(ste.getHeapsize() * memoryManager.BLOCKSIZE); i=i+4) {
 										java.lang.String regTmp = codeGenerator.getNextFreeTemporary();
 										codeGenerator.put("lw", regTmp, new Integer(i).toString()+"(" + regFrom + ")");
 										codeGenerator.put("sw", regTmp, new Integer(i).toString()+"(" + regFrom + ")");
 									}
 									// store word aus reg in heapStart
 									ste.setHeap(heapStart);
 									ste.setHeapsize(blocks);
 								}
 							}
 						}
 						return true;
 					} else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						if(!isNum(lookAhead())) {
 							ErrorReporter.markError("Missing size of boolean array");
 						} else {
 							adjustSrcPointer();
 						}
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste.getCategory().equals("const")) {
 							// convert to bool[]
 							java.lang.String[] array = ste.getAttribute().split(" ");
 							StringBuilder newAttribute = new StringBuilder();
 							for(int i=0; i<array.length; i++) {
 								if(!array[i].isEmpty()) {
 									if(new Integer(array[i]) > 0) {
 										newAttribute.append("1 ");
 									} else {
 										newAttribute.append("0 ");
 									}
 								}
 							}
 							ste.setAttribute(ste.getAttribute().replaceAll(" ", ""));
 						} else {
 							// TODO: delete currently assigned heap mem and assign new heap??
 						}
 						return true;
 					} else if(newType.getTokenID().equals(CHAR.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(CHARZ.tokenId)) {
 						if(!isNum(lookAhead())) {
 							ErrorReporter.markError("Missing size of char array");
 						} else {
 							adjustSrcPointer();
 						}
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste.getCategory().equals("const")) {
 							// convert to string
 							ste.setAttribute(ste.getAttribute().replaceAll(" ", ""));
 						} else {
 							// convert each int into a character + 0byte
 						}
 					}
 				}
 				// change type of identifier from char to...
 				else if(lastType.equals(CHAR.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						return true;
 					} else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(CHAR.tokenId)) {
 						return true;
 					} else if(newType.getTokenID().equals(CHARZ.tokenId)) {
 						// assign new heap memory with blocksize = 1
 						// put character + 0byte into heap
 						// put heap-address into stack (is stack mem already allocated)
 						return true;
 					}
 				}
 				// change type of identifier from string to...
 				else if(lastType.equals(CHARZ.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste != null) {
 							java.lang.String heapstart = ste.getHeap();
 							// free memory
 							memoryManager.freeMemory(heapstart, ste.getHeapsize());
 						}
 						// erase heap and heapsize
 						ste.setHeap("");
 						ste.setHeapsize(0);
 						return true;
 					} else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						// check whether characters are really numbers
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste.getCategory().equals("const")) {
 							try {
 								new Integer(ste.getAttribute());
 							} catch (NumberFormatException e) {
 								ErrorReporter.markError("Cannot convert string to number");
 							}
 						}
 						return true;
 					} else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(CHAR.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(CHARZ.tokenId)) {
 						if(!isNum(lookAhead())) {
 							ErrorReporter.markError("Missing size of char array");
 						} else {
 							// if the size has changed, assign new heap space and copy each word
 							int arraySize = new Integer(lookAhead().getAttribute());
 							int blocks = 0;
 							if(arraySize % memoryManager.BLOCKSIZE == 0)
 								blocks = arraySize / memoryManager.BLOCKSIZE;
 							else
 								blocks = ((int) arraySize / memoryManager.BLOCKSIZE) + 1;
 							SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 							if(ste != null) {
 								if(blocks > ste.getHeapsize()) {
 									java.lang.String heapStart = "hp" + 
 										new Integer(memoryManager.allocateMemory(blocks)).toString();
 									// load word aus ste.getHeap in reg
 									java.lang.String regFrom = codeGenerator.getNextFreeTemporary();
 									codeGenerator.put("la", regFrom, ste.getHeap());
 									java.lang.String regTo = codeGenerator.getNextFreeTemporary();
 									codeGenerator.put("la", regTo, heapStart);
 									for(int i=0; i<(ste.getHeapsize() * memoryManager.BLOCKSIZE); i=i+4) {
 										java.lang.String regTmp = codeGenerator.getNextFreeTemporary();
 										codeGenerator.put("lw", regTmp, new Integer(i).toString()+"(" + regFrom + ")");
 										codeGenerator.put("sw", regTmp, new Integer(i).toString()+"(" + regFrom + ")");
 									}
 									// store word aus reg in heapStart
 									ste.setHeap(heapStart);
 									ste.setHeapsize(blocks);
 								}
 							}
 						}
 						return true;
 					}
 				}
 				// change type of identifier from boolean to...
 				else if(lastType.equals(TROOF.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						if(identifier.getAttribute().equals(WIN.tokenId))
 							identifier.setAttribute("1");
 						else if(identifier.getAttribute().equals(FAIL.tokenId))
 							identifier.setAttribute("0");
 						return true;
 					} else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						return false;
 					}
 					// ... boolean
 					else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						return true;
 					} else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						return false;
 					}
 					// ... a string/char
 					else if(newType.getTokenID().equals(CHAR.tokenId) || 
 							newType.getTokenID().equals(CHARZ.tokenId)) {
 						return false;
 					}
 				}
 				// change type of identifier from boolean array to...
 				else if(lastType.equals(TROOFZ.tokenId)) {
 					// ... NULL
 					if(newType.getTokenID().equals(NOOB.tokenId)) {
 						// set heap address to null
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste != null) {
 							java.lang.String heapstart = ste.getHeap();
 							// free memory
 							memoryManager.freeMemory(heapstart, ste.getHeapsize());
 						}
 						// erase heap and heapsize
 						ste.setHeap("");
 						ste.setHeapsize(0);
 						return true;
 					} else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 						SymboltableEntry ste = symboltable.get(identifier.getAttribute());
 						if(ste != null) {
 							if(ste.getCategory().equals("const")) {
 								java.lang.String[] values = ste.getAttribute().split(" ");
 								StringBuilder newAttribute = new StringBuilder();
 								for(java.lang.String s : values) {
 									if(s.equals(WIN.tokenId)) {
 										newAttribute.append("1 ");
 									} else if(s.equals(FAIL.tokenId)) {
 										newAttribute.append("0 ");
 									}
 								}
 							} else if(ste.getCategory().equals("var") || ste.getCategory().equals("reg")) {
 								
 							}
 						}
 						return true;
 					}
 					// ... boolean
 					else if(newType.getTokenID().equals(TROOF.tokenId)) {
 						return false;
 					} else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 						return true;
 					}
 					// ... a string/char
 					else if(newType.getTokenID().equals(CHAR.tokenId) || 
 							newType.getTokenID().equals(CHARZ.tokenId)) {
 						return false;
 					}
 				} else {
 					return false;
 				}
 			} 
 			// If the identifier was untyped, we change from NULL to...
 			else {
 				// ... NULL
 				if(newType.getTokenID().equals(NOOB.tokenId)) {
 					// do nothing
 				}
 				// ... int (no change)
 				else if(newType.getTokenID().equals(NUMBR.tokenId)) {
 					TypeConversion.null2int(identifier);
 				}
 				// ... int[]
 				else if(newType.getTokenID().equals(NUMBRZ.tokenId)) {
 					if(!isNum(lookAhead())) {
 						ErrorReporter.markError("Missing size of char array");
 					} else {
 						int arraySize = new Integer(lookAhead().getAttribute());
 						TypeConversion.null2intarray(identifier, arraySize);
 					}
 				}
 				// ... boolean
 				else if(newType.getTokenID().equals(TROOF.tokenId)) {
 					TypeConversion.null2bool(identifier);
 				}
 				// ... boolean[]
 				else if(newType.getTokenID().equals(TROOFZ.tokenId)) {
 					if(!isNum(lookAhead())) {
 						ErrorReporter.markError("Missing size of char array");
 					} else {
 						int arraySize = new Integer(lookAhead().getAttribute());
 						TypeConversion.null2boolarray(identifier, arraySize);
 					}
 				}
 				// ... a char
 				else if(newType.getTokenID().equals(CHAR.tokenId)) {
 					TypeConversion.null2char(identifier);
 				} 
 				// ... a string
 				else if(newType.getTokenID().equals(CHARZ.tokenId)) {
 					Keyword lookahead = lookAhead();
 					if(!isNum(lookahead)) {
 						ErrorReporter.markError("Missing size of char array");
 					} else {
 						adjustSrcPointer();
 						int arraySize = new Integer(lookahead.getAttribute());
 						TypeConversion.null2string(identifier, arraySize);
 					}
 				} else {
 					// newType = identifier
 					TypeItem type = symboltable.getType(newType.getAttribute());
 					TypeConversion.null2record(identifier, type.getSize());
 				}
 				return true;
 			}
 			return false;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isVarDecl(Keyword keyword) throws ParseException {
 		if(isIdentifier(keyword)) {
 			
 			if(lookAhead() instanceof IS) {
 				adjustSrcPointer();
 				
 				if(symboltable == null) {
 					symboltable = new Symboltable("main", null);
 					System.out.println("THIS SHOULD NEVER HAPPEN");
 				}
 				
 				if(!(lookAhead() instanceof NOW)) {
 					ErrorReporter.markError("Missing 'NOW' keyword");
 				} else {
 					adjustSrcPointer();
 				}
 				
 				if(!(lookAhead() instanceof A)) {
 					ErrorReporter.markError("Missing 'A' keyword");
 				} else {
 					adjustSrcPointer();
 				}
 				
 				Keyword typeKeyword = s.lookupToken();
 				
 				SymboltableEntry ste = symboltable.get(keyword.getAttribute());
 				java.lang.String lastType;
 				if(ste == null) {
 					ErrorReporter.markError("Unkown variable " + keyword.getAttribute());
 					ste = new SymboltableEntry();
 					ste.setName(keyword.getAttribute());
 					ste.setType(NOOB.tokenId);
 					lastType = NOOB.tokenId;
 				} else {
 					lastType = ste.getType();
 				}
 				
 //				if(typeKeyword instanceof CHARZ || typeKeyword instanceof NUMBRZ || typeKeyword instanceof TROOFZ) {
 //					if(!isNum(lookAhead())) {
 //						ErrorReporter.markError("Missing size of int array");
 //					} else {
 //						adjustSrcPointer();
 //					}
 //				}
 				
 				if(!checkTypeConversions(lastType, typeKeyword, (Identifier) keyword)) {
 					ErrorReporter.markError(
 							"Cannot convert " + lastType + 
 							" to " + typeKeyword.getAttribute());
 				} else {
 					ste.setType(typeKeyword.getTokenID());
 				}
 				
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isType(Keyword keyword) {
 		return (keyword instanceof CHAR ||
 				keyword instanceof NUMBR ||
 				keyword instanceof TROOF ||
 				keyword instanceof CHARZ ||
 				keyword instanceof NUMBRZ ||
 				keyword instanceof TROOFZ ||
 				symboltable.existsType(keyword.getAttribute()));
 	}
 
 	public boolean isIdentifier(Keyword keyword) {
 		return (keyword instanceof Identifier);
 	}
 	
 	/**
 	 * Checks whether identifier is followed by a .
 	 * This is only the case if identifier is of type 'array'
 	 * @param keyword
 	 * @return
 	 */
 	public boolean isArrayAccess(Keyword keyword) {
 		if(isIdentifier(keyword)) {
 			if(lookAhead() instanceof ArrayAccessOp) {
 				// left_hand is the symboltable entry for the array or record
 				SymboltableEntry left_hand = symboltable.get(keyword.getAttribute());
 				
 				if(!(left_hand.getType().equals(NUMBRZ.tokenId) || 
 						left_hand.getType().equals(CHARZ.tokenId) || 
 						left_hand.getType().equals(TROOFZ.tokenId))) {
 					ErrorReporter.markError("Array access on non-array type");
 				}
 				
 				adjustSrcPointer();
 				Keyword k = s.lookupToken();
 				boolean arrayAccess = isArrayAccess(k);
 				if(arrayAccess || isIdentifier(k)) {
 					// 1. load base address of array into register
 					// here we load the base address of the array or record
 					if(left_hand.getCategory().equals("heap")) {
 						java.lang.String reg = codeGenerator.getNextFreeTemporary();
 						codeGenerator.put("la", reg, left_hand.getHeap());
 						left_hand.setReg(reg);
 					} else if(left_hand.getCategory().equals("var")) {
 						java.lang.String reg = codeGenerator.getNextFreeTemporary();
 						codeGenerator.put("lw", reg, "("+left_hand.getAddress()+")");
 						left_hand.setReg(reg);
 					} 
 					// right_hand is the symboltable entry for the "right hand side" of the deref operator
 					SymboltableEntry right_hand;
 					
 					if(arrayAccess) {
 						// ... thus either the result of another deref operation, 
 						// which is stored in currentSymboltaleEntry
 						right_hand = currentSymboltableEntry;
 						// furthermore, we have to load the value of v1 
 						// (which should be an address at that time) into v1
 						java.lang.String tmp = codeGenerator.getNextFreeTemporary();
 						codeGenerator.put("lw", "$v1", "(" + tmp + ")");
 					} else {
 						// ... or the symboltable entry for the identifier 
 						right_hand = symboltable.get(k.getAttribute());
 					}
 					// keyword is an array
 					// 3. Calculate offset and add it to the base address
 					if(right_hand.getCategory().equals("const")) {
 						Integer val = new Integer(right_hand.getAttribute()) * 4;
 						java.lang.String offset = val.toString();
 						// adding the offset directly to the left_hand address 
 						// and store it in currentSymboltable-Reg v1
 						codeGenerator.put("addi", "$v1", left_hand.getReg(), offset);
 					} else {
 						if(right_hand.getCategory().equals("var")) {
 							java.lang.String reg = codeGenerator.loadWord(right_hand.getAddress());
 							right_hand.setReg(reg);
 							right_hand.setCategory("reg");
 						}
 						// calculating array start + offset 
 						// and store it in currentSymboltable-Reg v1
						codeGenerator.put("muli", right_hand.getReg(), right_hand.getReg(), "4");
 						codeGenerator.put("add", "$v1", left_hand.getReg(), right_hand.getReg());
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks whether identifier is followed by a ->
 	 * This is only the case if identifier is of a dynamic type.
 	 * The difference to ArrayAccess is, that it is evaluated from
 	 * left to right, i.e if we access the struct s1 of type Stuff, 
 	 * which contains an integer 'id' and a 'next' pointer to the 
 	 * next Stuff s2, and we want to evaluate s1->s2->id, then
 	 * we would evaluate (s1->s2)->id.
 	 * If we would have arrays instead, i.e a1.a2.id, we would
 	 * evaluate a1.(a2.id).
 	 * @param keyword
 	 * @return
 	 */
 	public boolean isDerefIdentifier(Keyword keyword) {
 		if(isIdentifier(keyword)) {
 			if(lookAhead() instanceof DerefOperator) {
 				// left_hand is the symboltable entry for the array or record
 				SymboltableEntry left_hand = symboltable.get(keyword.getAttribute());
 				
 				// 1. load base address of record into register
 				if(left_hand.getCategory().equals("heap")) {
 					java.lang.String reg = codeGenerator.getNextFreeTemporary();
 					codeGenerator.put("la", reg, left_hand.getHeap());
 					left_hand.setReg(reg);
 				} else if(left_hand.getCategory().equals("var")) {
 					java.lang.String reg = codeGenerator.getNextFreeTemporary();
 					codeGenerator.put("lw", reg, left_hand.getAddress());
 					left_hand.setReg(reg);
 				}
 				
 				tmpSrcPointer2 = s.getPointerBeforeToken();
 				while(lookAhead() instanceof DerefOperator) {
 					
 					if(!symboltable.existsType(left_hand.getType())) {
 						ErrorReporter.markError("Left hand of deref-operator " +
 								"is not known as a dynamic type");
 					}
 					
 					adjustSrcPointer();
 					Keyword k = s.lookupToken();
 					
 					if(isIdentifier(k)) {
 						
 						// keyword is a dynamic type
 						// the difference to an array is that the right-hand part is not an index, but
 						// the struct property name
 						// 2. at this state, we have loaded the base address of the record, 
 						//    now lets calculate the offset for our address.
 						//    The offset is already stored in the global label '<struct_label>.<right_hand_keyword>'
 						Symboltable tmpsymboltable = symboltable;
 						TypeItem structType = symboltable.getType(left_hand.getType());
 						symboltable = structType.getSymbolTable();
 						if(!symboltable.exists(k.getAttribute())) {
 							ErrorReporter.markError("The right-hand-side is not a structure property");
 							return true;
 						} else {
 							// right_hand is the symboltable entry for the "right hand side" of the deref operator
 							SymboltableEntry right_hand = symboltable.get(k.getAttribute());
 							
 							java.lang.String tmpReg1 = codeGenerator.getNextFreeTemporary();
 							java.lang.String tmpReg2 = codeGenerator.getNextFreeTemporary();
 							codeGenerator.put("la", tmpReg1, right_hand.getAddress());
 							codeGenerator.put("lw", tmpReg1, "(" + tmpReg1 + ")");
 							// calculating record base + offset 
 							// and store it in currentSymboltable-Reg v1
 							codeGenerator.put("add", "$v1", left_hand.getReg(), tmpReg1);
 							// loading address of calculated position
 							codeGenerator.put("lw", "$v1", "($v1)");
 							codeGenerator.put("move", tmpReg2, "$v1");
 //							if(right_hand.getType().equals(NUMBR.tokenId) || 
 //									right_hand.getType().equals(CHAR.tokenId) || 
 //									right_hand.getType().equals(TROOF.tokenId) ) {
 								// loading value of address of calculated position
 								codeGenerator.put("lw", "$v1", "($v1)");
 //							}
 							currentSymboltableEntry = symboltable.get("IT");
 							// set type to right hand symbol type
 							currentSymboltableEntry.setType(right_hand.getType());
 							// TODO: missusing address field here. Not sure about the consequences, 
 							//       but I need something to temporary store the address-register
 							//       to be able to save the value later. (in isVarAssign())
 							currentSymboltableEntry.setAddress(tmpReg2);
 							codeGenerator.releaseRegister(tmpReg1);
 						}
 						symboltable = tmpsymboltable;
 						left_hand = currentSymboltableEntry;
 					} else {
 						ErrorReporter.markError("Expected identifier after dereference operator");
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isFunction(Keyword keyword) throws ParseException {
 		if(keyword instanceof HOW) {
 			
 			if(!(lookAhead() instanceof DUZ)) {
 				ErrorReporter.markError("Missing 'DUZ' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(lookAhead() instanceof I)) {
 				ErrorReporter.markError("Missing 'I' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			Keyword funcId = lookAhead();
 			if(!(funcId instanceof Identifier)) {
 				ErrorReporter.markError("Expected function label");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			// save callers symboltable
 			Symboltable tmpSymboltable = symboltable;
 			// if the caller function was the main function, we store the
 			// content of the caller-symboltable in the $s-registers 
 			// (as long as there is enough space)
 			
 			codeGenerator.put(funcId.getAttribute() + ": ");
 			
 			
 			
 			// fp := sp
 			codeGenerator.setFPisSP();
 			
 			int lastLvl = tmpSymboltable.getGlobalTable().getLevel();
 			symboltable = new Symboltable(funcId.getAttribute(), tmpSymboltable.getGlobalTable());
 			symboltable.setLevel(lastLvl+1);
 			
 			codeGenerator.cleanUpTemporaries();
 			
 			Keyword tok = lookAhead();
 			StringBuilder argTypes = new StringBuilder();
 			if(tok instanceof YR) {
 				adjustSrcPointer();
 
 				Keyword type = lookAhead();
 				if(!isType(type)) {
 					
 					if(lookAhead() instanceof Identifier) {
 						ErrorReporter.markError("Expected type declaration for argument " + 
 								lookAhead().getAttribute());
 						adjustSrcPointer();
 					} else {
 						ErrorReporter.markError("Expected another argument");
 					}
 					
 				} else {
 					adjustSrcPointer();
 					argTypes.append(" ");
 					argTypes.append(type.getTokenID());
 					Keyword argument = lookAhead();
 					if(!(lookAhead() instanceof Identifier)) {
 						ErrorReporter.markError("Expected another argument");
 					} else {
 						adjustSrcPointer();
 					}
 					
 					java.lang.String tempreg = codeGenerator.getNextFreeTemporary();
 					codeGenerator.put("move", tempreg, "$a0");
 					SymboltableEntry symboltableEntry = new SymboltableEntry();
 					symboltableEntry.setReg(tempreg);
 					symboltableEntry.setCategory("reg");
 					symboltableEntry.setName(argument.getAttribute());
 					symboltableEntry.setType(type.getTokenID());
 					symboltable.put(symboltableEntry);
 				}
 				
 				int argCount = 1;
 				while((tok = lookAhead()) instanceof AN) {
 					adjustSrcPointer();
 					if(!(lookAhead() instanceof YR)) {
 						ErrorReporter.markError("Missing 'YR' keyword");
 					} else {
 						adjustSrcPointer();
 					}
 					Keyword nextType = lookAhead();
 					if(!isType(nextType)) {
 						if(lookAhead() instanceof Identifier) {
 							ErrorReporter.markError("Expected type declaration for argument " + 
 									lookAhead().getAttribute());
 							adjustSrcPointer();
 						} else {
 							ErrorReporter.markError("Expected another argument");
 						}
 						
 					} else {
 						adjustSrcPointer();
 						argTypes.append(" ");
 						argTypes.append(type.getTokenID());
 						Keyword argumentNext = lookAhead();
 						if(!(argumentNext instanceof Identifier)) {
 							ErrorReporter.markError("Expected another argument");
 						} else {
 							adjustSrcPointer();
 						}
 						
 						if(argCount < 4) {
 							java.lang.String tempreg = codeGenerator.getNextFreeTemporary();
 							codeGenerator.put("move", tempreg, "$a" + new Integer(argCount).toString());
 							SymboltableEntry symboltableEntry = new SymboltableEntry();
 							symboltableEntry.setReg(tempreg);
 							symboltableEntry.setCategory("reg");
 							symboltableEntry.setName(argumentNext.getAttribute());
 							symboltableEntry.setType(nextType.getTokenID());
 							symboltable.put(symboltableEntry);
 							argCount++;
 						} else {
 							// TODO: arguments were possibly stored on the stack. Retrieve them!
 						}
 					}
 				}
 			}
 			
 			codeGenerator.getFunctionDefs().add(funcId.getAttribute() + argTypes);
 			
 			adjustSrcPointer();
 			while(!(tok instanceof IF)) {
 				if(!(isStatement(tok) 
 						|| (tok instanceof GTFO)
 						|| (tok instanceof FOUND))) {
 					ErrorReporter.markError("Expected statement");
 				} else if(tok instanceof FOUND) {
 					if(!(lookAhead() instanceof YR)) {
 						ErrorReporter.markError("Missing 'YR' keyword");
 					} else {
 						adjustSrcPointer();
 					}
 					
 					if(!isOperation(s.lookupToken())) {
 						ErrorReporter.markError("Expected return value");
 					}
 					
 					if(currentSymboltableEntry == null) {
 						currentSymboltableEntry = symboltable.get("IT");
 					}
 					// stores return value in $v1
 					if(currentSymboltableEntry.getCategory().equals("const")) {
 						if(currentSymboltableEntry.getType().equals(NUMBR.tokenId) || 
 								currentSymboltableEntry.getType().equals(CHAR.tokenId))
 							codeGenerator.put("addi", "$v1", "$zero", currentSymboltableEntry.getAttribute());
 						else if(currentSymboltableEntry.getType().equals(CHARZ.tokenId)) {
 							StringLib.init(codeGenerator, memoryManager);
 							StringLib.storeString(currentSymboltableEntry);
 							codeGenerator.put("la", "$v1", currentSymboltableEntry.getHeap());
 							currentSymboltableEntry.setReg("$v1");
 							currentSymboltableEntry.setCategory("reg");
 						}
 					} else if(currentSymboltableEntry.getCategory().equals("var")) {
 						codeGenerator.put("lw", "$v1", currentSymboltableEntry.getAddress() + "($fp)");
 					} else {
 						codeGenerator.move2Return(currentSymboltableEntry.getReg());
 					}
 					// set the fp back: sp := fp
 					codeGenerator.setSPisFP();
 					
 //					// restore frame pointer
 //					codeGenerator.put("move", "$fp", "$s6");
 //					// restore return address
 //					codeGenerator.put("move", "$ra", "$s7");
 //					
 //					/* move stack-content back to saver register */
 //					for(java.lang.String savereg : saveRegister.keySet()) {
 //						java.lang.String address = saveRegister.get(savereg);
 //						codeGenerator.put("lw", savereg, address + "($fp)");
 //					}
 //					
 //					/* move save register back to temp register */
 //					for(java.lang.String treg : tmpRegister.keySet()) {
 //						SymboltableEntry ste = tmpRegister.get(treg);
 //						// treg = ste.getReg
 //						codeGenerator.put("move", treg, ste.getReg());
 //					}
 					
 					// returns to previous address					
 					codeGenerator.put("jr", "$ra");
 				} else if(tok instanceof GTFO) {
 					// set the fp back: sp := fp
 					codeGenerator.setSPisFP();
 					
 					// returns to previous address
 					codeGenerator.put("jr", "$ra");
 				}
 				tok = s.lookupToken();
 			}
 			
 			if(!(lookAhead() instanceof YOU)) {
 				ErrorReporter.markError("Missing 'YOU' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(lookAhead() instanceof SAY)) {
 				ErrorReporter.markError("Missing 'SAY' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			if(!(lookAhead() instanceof SO)) {
 				ErrorReporter.markError("Missing 'SO' keyword");
 			} else {
 				adjustSrcPointer();
 			}
 			
 			
 			// set the fp back: sp := fp
 			codeGenerator.setSPisFP();
 			
 			
 			codeGenerator.put("jr", "$ra");
 			symboltable = tmpSymboltable;
 			
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	public Keyword lookAhead() {
 		Keyword nextToken = s.lookupToken();
 		int i = s.getPointerBeforeToken();
 		tmpSrcPointer = s.getSrcPointer();
 		s.setSrcPointer(i);
 		return nextToken;
 	}
 	
 	public void adjustSrcPointer() {
 		s.setSrcPointer(tmpSrcPointer);
 	}
 
 	/**
 	 * This method triggers the whole parsing process
 	 * @param filename
 	 */
 	public static void loadSourcecode(java.lang.String filename) {
 		if(filename != null) {
 			Parser p = new Parser(filename);
 			StringBuilder sb = new StringBuilder();
 			if(filename.startsWith("/"))
 				sb.append("/");
 			java.lang.String[] path_split = filename.split("/");
 			int index = 0;
 			for(int i=0; i<path_split.length-1; i++) {
 				 sb.append(path_split[i]);
 				 sb.append("/");
 				 index = i+1;
 			}
 			p.currentPath = sb.toString();
 			p.currentModule = path_split[index];
 			
 			boolean needsCompilation = false;
 			File mainFile = new File(filename);
 			File mainFileCat = new File(filename.replace(".lol", ".cat"));
 			if(mainFileCat.canRead()) {
 				try {
 					java.lang.String timestamp;
 					java.util.Scanner filescanner = new java.util.Scanner(mainFileCat);
 					if(filescanner.hasNextLine()) {
 						timestamp = filescanner.nextLine().substring(1);
 						
 						if(!timestamp.equals(mainFile.lastModified())) {
 							needsCompilation = true;
 						}
 					} else {
 						needsCompilation = true;
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			} else {
 				needsCompilation = true;
 			}
 			
 			if(needsCompilation) {
 				mainFile.setLastModified(System.currentTimeMillis());
 				p.codeGenerator.setModuleTimestamp(new Long(mainFile.lastModified()).toString());
 				try {
 					java.util.Scanner filescanner = new java.util.Scanner(mainFile);
 					StringBuilder stringBuilder = new StringBuilder();
 					while(filescanner.hasNextLine()) {
 						stringBuilder.append(filescanner.nextLine() + "\n");
 					}
 					p.s = new Scanner(stringBuilder.toString());
 					p.isProgram(p.s.lookupToken());
 					p.codeGenerator.finalizeCG();
 					if(!ErrorReporter.isError()) {
 						System.out.println("Build Successfully.");
 					}
 				} catch (ParseException e) {
 					e.printStackTrace();
 				} catch (FileNotFoundException e) {
 					ErrorReporter.markError("File " + filename + " not found :(");
 				}
 			}
 		}
 	}
 	
 	public static void main(java.lang.String[] args) {
 		if(args[0] != null) {
 			
 			if(args[1] != null && args[1].equals("-s")) {
 				Parser.separateCompile = true;
 			} else {
 				Parser.separateCompile = false;
 			}
 			// -l = link only
 			if(args[1] != null && args.equals("-l")) {
 				Set<java.lang.String> sources = new HashSet<java.lang.String>();
 				for(int i=2; i<args.length; i++) {
 					sources.add(args[i]);
 				}
 				Linker.destination = "/home/sstroka/Desktop/main.asm";
 				Linker.linkSources(sources);
 			} else {
 				Parser.loadSourcecode(args[0]);
 				// link only if we do not want to just compile one file
 				// we then need to manually link the sources
 				if(!Parser.separateCompile) {
 					Linker.destination = "/home/sstroka/Desktop/main.asm";
 					Linker.linkSources(Parser.modules);
 				}
 			}
 		}
 	}
 }
