 /**
  * 
  * CabConverter: A Cabrillo generation tool for MacLoggerDX
  * Original concept and author: B. Scott Andersen (NE1RD)
  */
 package com.bsandersen.CabConverter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 /**
  * This class provides a simple ADIF parser suitable for extracting
  * data from MacLoggerDX exported .ADIF files. 
  * 
  * @author B. Scott Andersen
  * 
  * CabConverter by B. Scott Andersen (NE1RD) is licensed under a 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  */
 public class ADIFparser {	
 	/**
 	 * This method provides access to the reference to this Singleton object 
 	 */
 	public static ADIFparser getInstance() {
 		return me;
 	}
 	
 	/*
 	 * This is a Singleton object. This private member holds a reference
 	 * to the created object.
 	 */
 	private static ADIFparser me;
 	
 	/*
 	 * in: The file pointer used by getNextToken().
 	 * tokens: the tokens on this line
 	 * tokenIndex: index into the tokens[] array
 	 * tokenThisLine: zero based tokens array has this many tokens from this line
 	 * lineIndex: index into the current line
 	 * line: the characters read from the file
 	 */
 	private BufferedReader in;
 	private String tokens[] = new String[200];
 	private int tokenIndex;
 	private int tokensThisLine;
 	private int lineIndex;
 	private String line;
 	
 	/*
 	 * Collection as a linked list
 	 */
 	ADIFrecord adifHead = null;
 	ADIFrecord adifTail = null;
 	
 	/**
 	 * Constructor
 	 */
 	public ADIFparser() {
 		me = this;
 	}
 
 	/**
 	 * This method opens the file indicated, parses its records, and creates
 	 * a list of QSOs that can be walked by the contest processing part of
 	 * the program.
 	 * 
 	 * @param The file to open as selected by the user.
 	 */
 	public void parseADIFfile(File f) {		
 
 		
 		// Do away with anything we had previously.
 		// This also allocates the collection area.
 		// emptyLog();
 		
 		// Open the specified file
 		try {
 			in = new BufferedReader(new FileReader(f));
 			
 			LogViewer logViewer = LogViewer.getInstance();
 			logViewer.removeAllRows();
 			logViewer.refresh();
 			
 			// Lose the ones we had before
 			adifHead = null;
 			adifTail = null;
 			
 			processFile(logViewer);
 			
 			logViewer.refresh();
 		} catch (IOException e) {
 			System.err.println("Error opening ADIF file:" + e);
 			in = null;	// Make sure we don't do something stupid
 			return;
 		}
 		
 	}
 	
 	/**
 	 * Accessor for the colletion of ADIF records.
 	 * @return The the collection of ADIF records parsed from the file
 	 */
 	public ADIFrecord getAdifRecords() {
 		return adifHead;
 	}
 	
 	/*
 	 * An open file and tokenizer are active. This method walks through
 	 * the tokens, collecting the information from each record in the 
 	 * file, and updates the UI and collection of ADIF records as it
 	 * goes.
 	 * @param logViewer Reference to the log viewer pane UI element.
 	 */
 	private void processFile(LogViewer logViewer) {
 		String tok;
 		line = "";
 		lineIndex = 0;
 		tokenIndex = 0;
 		tokensThisLine = 0;
 		
 		// We need to walk past the header.
 		while ((tok = getNextToken()) != null) {
 			if (tok.equalsIgnoreCase("<EOH>")) {
 				break;
 			}
 		}
 		
 		// The QSOs begin immediately after the header. Each QSO
 		// ends with a <EOR> token.
 		
 		String		call = "";					// Callsign of the contacted station
 		String		date = "";					// Date in the format YYYYMMDD
 		String		time = "";					// Time in the format hhmmss
 		String		frequency = "";				// In megahertz
 		String		mode = "";					// String like SSB, USB, LSB, CW, etc. (Not in a convenient Cabrillo form)
 		String		rstSent = "";				// String like 59
 		String		rstReceived = "";			// String like 59
 		String		serialNumberSent = "";		// Serial number sent
 		String		serialNumberReceived = "";	// Serial number sent
 		String		exchangeSent = "";			// String part of the exchange, if any
 		String		exchangeReceived = "";		// String part of the exchange, if any
 		
 		while ((tok = getNextToken()) != null) {
 			if (tok.equalsIgnoreCase("<EOR>")) {
 				ADIFrecord r = new ADIFrecord(call, date, time, frequency, mode, rstSent, rstReceived, 
 							serialNumberSent, serialNumberReceived, exchangeSent, exchangeReceived);
 				logViewer.addQSO(r);
 				addADIF(r);
 					
 				// Reset everything back to empty strings so we can collect another
 				// record with a clean slate.
 				call = "";
 				date = "";
 				time = "";
 				frequency = "";
 				mode = "";
 				rstSent = "";
 				rstReceived = "";
 				serialNumberSent = "";
 				serialNumberReceived = "";
 				exchangeSent = "";
 				exchangeReceived = "";
 			} else if ((tok.length() >= 11) && (tok.substring(0, 11).equalsIgnoreCase("<SRX_STRING"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					exchangeReceived = new String(tok);
 				}
 			} else if ((tok.length() >= 11) && (tok.substring(0, 11).equalsIgnoreCase("<STX_STRING"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					exchangeSent = new String(tok);
 				}
 			} else if ((tok.length() >= 4) && (tok.substring(0, 4).equalsIgnoreCase("<SRX"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					serialNumberReceived = new String(tok);
 				}
 			} else if ((tok.length() >= 4) && (tok.substring(0, 4).equalsIgnoreCase("<STX"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					serialNumberSent = new String(tok);
 				}
 			} else if ((tok.length() >= 5) && (tok.substring(0, 5).equalsIgnoreCase("<FREQ"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					frequency = new String(tok);
 				}
 			} else if ((tok.length() >= 5) && (tok.substring(0, 5).equalsIgnoreCase("<CALL"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					call = new String(tok);
 				}
 			} else if ((tok.length() >= 5) && (tok.substring(0, 5).equalsIgnoreCase("<MODE"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					mode = new String(tok);
 				}
 			} else if ((tok.length() >= 8) && (tok.substring(0, 8).equalsIgnoreCase("<TIME_ON"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					time = new String(tok);
 					time = time.substring(0, 4);
 				}
 			} else if ((tok.length() >= 9) && (tok.substring(0, 9).equalsIgnoreCase("<QSO_DATE"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					String s = new String(tok);
 					if (s.length() > 7) {
 						date = new String(s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8));
 					} else {
 						date = s; // This is probably wrong, but what can we do?
 					}
 				}
 			} else if ((tok.length() >= 9) && (tok.substring(0, 9).equalsIgnoreCase("<RST_SENT"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					rstSent = new String(tok);
 				}
 			} else if ((tok.length() >= 9) && (tok.substring(0, 9).equalsIgnoreCase("<RST_RCVD"))) {
 				tok = getNextToken();
 				if (tok != null) {
 					rstReceived = new String(tok);
 				}
 			}
 		}
 		
 	}  // End parseADIFfile
 	
 	/*
 	 * This retrieves the next token. If necessary, we go back to the
 	 * input file to get more data.
 	 */
 	private String getNextToken() {
 		if (tokenIndex < tokensThisLine) {
 			return tokens[tokenIndex++];
 		} else {
 			// We may need to loop here since there could be blank lines
 			// or because we are on the first line (which is a comment line
 			// just saying the ADIF was produced by MLDX.
 			
 			while (in != null) {
 				// We need data from the file. Get it.
 				processLine();
 				if (tokenIndex < tokensThisLine) {
 					return tokens[tokenIndex++];
 				} else {
 					// We found no tokens. Loop and try again.
 				}
 			} // while
 			// We fell out of the loop looking for the next token.
 			// There are no more tokens. Return null to say this.
 			return null;
 		} // else
 	} // getNextToken()
 	
 	/*
 	 * Read a line from the ADIF file, get it parsed into tokens.
 	 */
 	private void processLine() {
 		if (in != null) {
 			try {
 				line = in.readLine();
 				if (line == null) {					
 					in.close();
 					in = null;
 				} else {
 					parseLine();
 				}
 			} catch (IOException e) {
 				System.err.println("Error reading from ADIF file:" + e);
 			}
 		} // if (in != null)
 	} // processLine
 	
 	/*
 	 * This method takes the line read from the file and creates the tokens
 	 */
 	private void parseLine() {
 		line.trim();	// Leading white and trailing white space can go
 		int len = line.length();
 		lineIndex = 0;
 		tokenIndex = 0;
 		tokensThisLine = 0;
 		
 		// Walk the string and extract the tokens. Don't recognize a the
 		// start of a token? Walk by it. Look again.
 		while (lineIndex < len) {
 			if (line.charAt(lineIndex) == '<') {
 				int tokStart = lineIndex;
 				while ((lineIndex < len) && (line.charAt(lineIndex) != '>')) {
 					lineIndex++;
 				}
 				if (line.charAt(lineIndex) == '>') {
 					tokens[tokensThisLine++] = line.substring(tokStart, ++lineIndex);
 					tokStart = lineIndex;
 					while ((lineIndex < len) && (line.charAt(lineIndex) != '<')) {
 						lineIndex++;
 					}
 					
 					// If there is really another token on the line add it
 					if (lineIndex > tokStart) {
 						tokens[tokensThisLine++] = line. substring(tokStart, lineIndex);
 					}
 				} else {
 					// This is strange. We had an open angle bracket, but no close one.
 					// We only get here if we are at the end of the line. We'll ignore
 					// the open angle bracket, get a new line, and try again.
 				}
 			} else {
 				lineIndex++;
 			} // else
 		} // while
 	} // parseLine
 	
 	/*
 	 * Add this ADIF record to our collection
 	 */
 	private void addADIF(ADIFrecord r) {
 		if (adifHead == null) {
 			adifHead = r;
 			adifTail = r;
 		} else {
 			adifTail.setNext(r);
 			adifTail = r;
 		}
 	}
 }
