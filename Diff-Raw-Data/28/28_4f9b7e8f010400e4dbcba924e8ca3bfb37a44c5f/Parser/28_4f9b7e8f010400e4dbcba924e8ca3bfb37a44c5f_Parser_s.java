 /**
  * Encapsulates access to the input code.
  *
  * Encapsulates access to the input code. Reads an assembly language command,
  * parses it, and provides convenient access to the command's components
  * (fields and symbols). In addition, removes all white space and comments.
  *
  * @author Ben "the Pyrate" Mendis
  */
 
 package org.hacdc.tecs.project_6.assembler;
 
 import java.io.InputStream;
 
 public class Parser {
 	private final InputStream input;
 
 	/**
 	 * Constructor.
 	 *
 	 * @args input An InputStream containing the assembly language commands.
 	 */
 	public Parser (InputStream input) {
 		this.input = input;
 	}
 }
 
