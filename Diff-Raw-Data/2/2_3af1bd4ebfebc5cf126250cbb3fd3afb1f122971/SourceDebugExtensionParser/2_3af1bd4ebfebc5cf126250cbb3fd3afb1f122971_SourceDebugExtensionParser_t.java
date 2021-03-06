 /*******************************************************************************
  * Copyright (c) 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jdi.internal;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sun.jdi.AbsentInformationException;
 
 /**
  * 
  */
 public class SourceDebugExtensionParser {
 	
 	private static class Lexer {
 		
 		static final int UNKNOWN=        0;
 		static final int SMAP=           1;
 		static final int NON_ASTERISK_STRING= 2;
 		static final int NUMBER=         3;
 		static final int CR=             4;
 		static final int ASTERISK_CHAR=  5;
 		static final int ASTERISK_C=     6;
 		static final int ASTERISK_E=     7;
 		static final int ASTERISK_F=     8;
 		static final int ASTERISK_L=     9;
 		static final int ASTERISK_O=    10;
 		static final int ASTERISK_S=    11;
 		static final int ASTERISK_V=    12;
 		static final int WHITE_SPACE=   13;
 		static final int COLON=         14;
 		static final int COMMA=        15;
 		static final int SHARP=         16;
 		static final int PLUS=          17;
 		
 		private char[] fSmap;
 		private int fPointer;
 		private char fChar;
 		
 		private char[] fLexem;
 		private int fLexemType;
 		
 		private boolean fEOF;
 		
 		public Lexer (String smap) {
 			fSmap= smap.toCharArray();
 			fLexemType= UNKNOWN;
 			fPointer= -1;
 			nextChar();
 		}
 		
 		/**
 		 * Compute the next lexem.
 		 * 
 		 * @return the type of the next lexem.
 		 */
 		public int nextLexem() throws AbsentInformationException {
 			if (fEOF) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.0")); //$NON-NLS-1$
 			} else {
 				startWith();
 			}
 			return fLexemType;
 		}
 
 		
 		private char nextChar() {
 			if (++fPointer == fSmap.length) {
 				fEOF= true;
 				return '\000';
 			}
 			fChar= fSmap[fPointer];
 			return fChar;
 		}
 		
 		private void startWith() throws AbsentInformationException  {
 			switch (fChar) {
 				case '\n':
 				case '\r':
 					startWithCR();
 					break;
 				case '*':
 					startWithAsterisk();
 					break;
 				case ':':
 					fLexem= new char[] {':'};
 					fLexemType= COLON;
 					nextChar();
 					break;
 				case ',':
 					fLexem= new char[] {','};
 					fLexemType= COMMA;
 					nextChar();
 					break;
 				case '#':
 					fLexem= new char[] {'#'};
 					fLexemType= SHARP;
 					nextChar();
 					break;
 				case '+':
 					fLexem= new char[] {'+'};
 					fLexemType= PLUS;
 					nextChar();
 					break;
 				default:
 					startWithOtherChar();
 					break;
 			}
 		}
 
 		/**
 		 * 
 		 */
 		private void startWithOtherChar() {
 			int lexemStart= fPointer;
 			consumeWhiteSpace();
 			if (fChar >= '0' && fChar <= '9') { // a number
 				number(lexemStart);
 			} else {
 				nonAsteriskString(lexemStart);
 			}
 		}
 
 		/**
 		 * @param lexemStart
 		 */
 		private void nonAsteriskString(int lexemStart) {
 			while (fChar != '\n' && fChar != '\r' && !fEOF) {
 				nextChar();
 			}
 			int length= fPointer - lexemStart;
 			fLexem= new char[length];
 			System.arraycopy(fSmap, lexemStart, fLexem, 0, length);
 			if (length == 4 && fLexem[0] == 'S' && fLexem[1] == 'M' && fLexem[2] == 'A' && fLexem[3] == 'P') {
 				fLexemType= SMAP;
 			} else {
 				fLexemType= NON_ASTERISK_STRING;
 			}
 		}
 
 		/**
 		 * @param lexemStart
 		 */
 		private void number(int lexemStart) {
 			while (fChar >= '0' && fChar <= '9') {
 				nextChar();
 			}
 			consumeWhiteSpace();
 			fLexemType= NUMBER;
 			int length= fPointer - lexemStart;
 			fLexem= new char[length];
 			System.arraycopy(fSmap, lexemStart, fLexem, 0, length);
 		}
 
 		/**
 		 * 
 		 */
 		private void startWithAsterisk() throws AbsentInformationException {
 			nextChar();
 			if (fEOF) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.1")); //$NON-NLS-1$
 			}
 			switch (fChar) {
 				case 'C':
 					fLexemType= ASTERISK_C;
 					break;
 				case 'E':
 					fLexemType= ASTERISK_E;
 					break;
 				case 'F':
 					fLexemType= ASTERISK_F;
 					break;
 				case 'L':
 					fLexemType= ASTERISK_L;
 					break;
 				case 'O':
 					fLexemType= ASTERISK_O;
 					break;
 				case 'S':
 					fLexemType= ASTERISK_S;
 					break;
 				case 'V':
 					fLexemType= ASTERISK_V;
 					break;
 				default:
 					fLexemType= ASTERISK_CHAR;
 					break;
 			}
 			fLexem= new char[] {'*', fChar};
 			nextChar();
 		}
 
 		/**
 		 * 
 		 */
 		private void startWithCR() {
 			if (fChar == '\r') {
 				if (nextChar() == '\n') {
 					fLexem= new char[] {'\r', '\n'};
 					nextChar();
 				} else {
 					fLexem= new char[] {'\r'};
 				}
 			} else {
 				fLexem= new char[] {fChar};
 				nextChar();
 			}
 			fLexemType= CR;
 		}
 
 		/**
 		 * 
 		 */
 		private void consumeWhiteSpace() {
 			while (fChar == ' ' || fChar == '\t') {
 				nextChar();
 			}
 		}
 
 		/**
 		 * @return the value of the current lexem.
 		 */
 		public char[] lexem() {
 			return fLexem;
 		}
 		
 		/**
 		 * @return the type of the current lexem.
 		 */
 		public int lexemType() {
 			return fLexemType;
 		}
 		
 	}
 	
 	/**
 	 * The reference type to which this source debug extension is associated.
 	 */
 	private ReferenceTypeImpl fReferenceType;
 	
 	private List fDefinedStrata;
 	
 	// parser data;
 	private ReferenceTypeImpl.Stratum fCurrentStratum;
 	private boolean fFileSectionDefinedForCurrentStratum;
 	private boolean fLineSectionDefinedForCurrentStratum;
 	private int fCurrentLineFileId;
 
 
 	public static void parse(String smap, ReferenceTypeImpl referenceType) throws AbsentInformationException {
 		new SourceDebugExtensionParser(referenceType).parseSmap(smap);
 	}
 	
 	/**
 	 * SourceDebugExtension constructor.
 	 */
 	private SourceDebugExtensionParser(ReferenceTypeImpl referenceType) {
 		fReferenceType= referenceType;
 		fDefinedStrata= new ArrayList();
 		fDefinedStrata.add(VirtualMachineImpl.JAVA_STRATUM_NAME);
 	}
 	
 	/**
 	 * 
 	 */
 	private void parseSmap(String smap) throws AbsentInformationException {
 		Lexer lexer= new Lexer(smap);
 		parseHeader(lexer);
 		parseSections(lexer);
 		if (!fDefinedStrata.contains(fReferenceType.defaultStratum())) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.2")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseHeader(Lexer lexer) throws AbsentInformationException {
 		int lexemType= lexer.nextLexem();
 		if (lexemType != Lexer.SMAP) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.3")); //$NON-NLS-1$
 		}
 		if (lexer.nextLexem() != Lexer.CR) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.4")); //$NON-NLS-1$
 		}
 		if (isAsteriskLexem(lexer.nextLexem())) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.5")); //$NON-NLS-1$
 		}
 		fReferenceType.setOutputFileName(getNonAsteriskString(lexer));
 		if (isAsteriskLexem(lexer.lexemType())) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.6")); //$NON-NLS-1$
 		}
 		fReferenceType.setDefaultStratumId(getNonAsteriskString(lexer));
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseSections(Lexer lexer) throws AbsentInformationException {
 		while (lexer.lexemType() != Lexer.ASTERISK_E) {
 			parseStratumSection(lexer);
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseStratumSection(Lexer lexer) throws AbsentInformationException {
 		if (lexer.lexemType() != Lexer.ASTERISK_S) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.7")); //$NON-NLS-1$
 		}
 		if (isAsteriskLexem(lexer.nextLexem())) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.8")); //$NON-NLS-1$
 		}
 		String stratumId= getNonAsteriskString(lexer);
 		if (fDefinedStrata.contains(stratumId)) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.9"), new String[] {stratumId})); //$NON-NLS-1$
 		}
 		fCurrentStratum= new ReferenceTypeImpl.Stratum(stratumId);
 		fFileSectionDefinedForCurrentStratum= false;
 		fLineSectionDefinedForCurrentStratum= false;
 		int lexemType= lexer.lexemType();
 		while (lexemType != Lexer.ASTERISK_E && lexemType != Lexer.ASTERISK_S) {
 			switch (lexemType) {
 				case Lexer.ASTERISK_F:
 					if (fFileSectionDefinedForCurrentStratum) {
 						throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.10"), new String[] {stratumId})); //$NON-NLS-1$
 					}
 					parseFileSection(lexer);
 					fFileSectionDefinedForCurrentStratum= true;
 					break;
 				case Lexer.ASTERISK_L:
 					if (fLineSectionDefinedForCurrentStratum) {
 						throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.11"), new String[] {stratumId})); //$NON-NLS-1$
 					}
 					parseLineSection(lexer);
 					fLineSectionDefinedForCurrentStratum= true;
 					break;
 				case Lexer.ASTERISK_V:
 					parseVendorSection(lexer);
 					break;
 				case Lexer.ASTERISK_CHAR:
 					parseFutureSection(lexer);
 					break;
 				default:
 					throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.12"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 			}
 			lexemType= lexer.lexemType();
 		}
 		if (!fFileSectionDefinedForCurrentStratum) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.13"), new String[] {stratumId})); //$NON-NLS-1$
 		}
 		if (!fLineSectionDefinedForCurrentStratum) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.14"), new String[] {stratumId})); //$NON-NLS-1$
 		}
 		fDefinedStrata.add(stratumId);
 		fReferenceType.addStratum(fCurrentStratum);
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseFileSection(Lexer lexer) throws AbsentInformationException {
 		if (lexer.nextLexem() != Lexer.CR) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.15"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 		}
 		lexer.nextLexem();
 		while (!isAsteriskLexem(lexer.lexemType())) {
 			parseFileInfo(lexer);
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseFileInfo(Lexer lexer) throws AbsentInformationException {
 		int lexemType= lexer.lexemType();
 		if (lexemType == Lexer.NUMBER) {
 			int fileId= integerValue(lexer.lexem());
 			if (isAsteriskLexem(lexer.nextLexem())) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.16")); //$NON-NLS-1$
 			}
 			fCurrentStratum.addFileInfo(fileId, getNonAsteriskString(lexer));
 		} else if (lexemType == Lexer.PLUS) {
 			if (lexer.nextLexem() != Lexer.NUMBER) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.17")); //$NON-NLS-1$
 			}
 			int fileId= integerValue(lexer.lexem());
 			if (isAsteriskLexem(lexer.nextLexem())) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.18")); //$NON-NLS-1$
 			}
 			String fileName= getNonAsteriskString(lexer);
			if (isAsteriskLexem(lexer.lexemType())) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.19")); //$NON-NLS-1$
 			}
 			fCurrentStratum.addFileInfo(fileId, fileName, getNonAsteriskString(lexer));
 		} else {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.20"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseLineSection(Lexer lexer) throws AbsentInformationException {
 		fCurrentLineFileId= 0;
 		if (lexer.nextLexem() != Lexer.CR) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.21"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 		}
 		lexer.nextLexem();
 		while (!isAsteriskLexem(lexer.lexemType())) {
 			parseLineInfo(lexer);
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseLineInfo(Lexer lexer) throws AbsentInformationException {
 		if (lexer.lexemType() != Lexer.NUMBER) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.22")); //$NON-NLS-1$
 		}
 		int inputStartLine= integerValue(lexer.lexem());
 		int lexemType= lexer.nextLexem();
 		if (lexemType == Lexer.SHARP) {
 			if (lexer.nextLexem() != Lexer.NUMBER) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.23")); //$NON-NLS-1$
 			}
 			fCurrentLineFileId= integerValue(lexer.lexem());
 			lexemType= lexer.nextLexem();
 		}
 		int repeatCount;
 		if (lexemType == Lexer.COMMA) {
 			if (lexer.nextLexem() != Lexer.NUMBER) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.24")); //$NON-NLS-1$
 			}
 			repeatCount= integerValue(lexer.lexem());
 			lexemType= lexer.nextLexem();
 		} else {
 			repeatCount= 1;
 		}
 		if (lexemType != Lexer.COLON) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.25")); //$NON-NLS-1$
 		}
 		if (lexer.nextLexem() != Lexer.NUMBER) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.26")); //$NON-NLS-1$
 		}
 		int outputStartLine= integerValue(lexer.lexem());
 		lexemType= lexer.nextLexem();
 		int outputLineIncrement;
 		if (lexemType == Lexer.COMMA) {
 			if (lexer.nextLexem() != Lexer.NUMBER) {
 				throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.27")); //$NON-NLS-1$
 			}
 			outputLineIncrement= integerValue(lexer.lexem());
 			lexemType= lexer.nextLexem();
 		} else {
 			outputLineIncrement= 1;
 		}
 		if (lexemType != Lexer.CR) {
 			throw new AbsentInformationException(JDIMessages.getString("SourceDebugExtensionParser.28")); //$NON-NLS-1$
 		}
 		lexer.nextLexem();
 		fCurrentStratum.addLineInfo(inputStartLine, fCurrentLineFileId, repeatCount, outputStartLine, outputLineIncrement);
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseVendorSection(Lexer lexer) throws AbsentInformationException {
 		if (lexer.nextLexem() != Lexer.CR) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.29"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 		}
 		lexer.nextLexem();
 		while (!isAsteriskLexem(lexer.lexemType())) {
 			// do nothing in this case, just consume the lexems.
 			getNonAsteriskString(lexer);
 		}
 	}
 
 	/**
 	 * @param lexer
 	 */
 	private void parseFutureSection(Lexer lexer) throws AbsentInformationException {
 		if (lexer.nextLexem() != Lexer.CR) {
 			throw new AbsentInformationException(MessageFormat.format(JDIMessages.getString("SourceDebugExtensionParser.30"), new String[] {new String(lexer.lexem())})); //$NON-NLS-1$
 		}
 		lexer.nextLexem();
 		while (!isAsteriskLexem(lexer.lexemType())) {
 			// do nothing in this case, just consume the lexems.
 			getNonAsteriskString(lexer);
 		}
 	}
 
 	private String getNonAsteriskString(Lexer lexer) throws AbsentInformationException {
 		StringBuffer string= new StringBuffer();
 		int lexemType= lexer.lexemType();
 		while (lexemType != Lexer.CR) {
 			string.append(lexer.lexem());
 			lexemType= lexer.nextLexem();
 		}
 		lexer.nextLexem();
 		// remove the leading white spaces
 		int i= -1;
 		for (char c= string.charAt(++i); c == ' ' || c == '\t'; c= string.charAt(++i));
 		return string.delete(0, i).toString();
 	}
 	
 	private int integerValue(char[] lexem) {
 		int i= 0;
 		char c= lexem[0];
 		while (c == ' ' || c == '\t') {
 			c= lexem[++i];
 		}
 		int value= 0;
 		while (c >= '0' && c <= '9') {
 			value= value * 10 + c - '0';
 			if (++i == lexem.length) {
 				break;
 			}
 			c= lexem[i];
 		}
 		return value;
 	}
 
 	private boolean isAsteriskLexem(int lexemType) {
 		switch (lexemType) {
 			case Lexer.ASTERISK_C:
 			case Lexer.ASTERISK_E:
 			case Lexer.ASTERISK_F:
 			case Lexer.ASTERISK_L:
 			case Lexer.ASTERISK_O:
 			case Lexer.ASTERISK_S:
 			case Lexer.ASTERISK_V:
 			case Lexer.ASTERISK_CHAR:
 			return true;
 			default:
 				return false;
 		}
 	};
 
 }
