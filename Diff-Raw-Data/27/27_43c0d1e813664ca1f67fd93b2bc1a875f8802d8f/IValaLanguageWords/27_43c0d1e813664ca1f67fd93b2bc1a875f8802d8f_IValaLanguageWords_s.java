 /* IValaLanguageWords.java
  *
  * Copyright (C) 2007  Johann Prieur <johann.prieur@gmail.com>
  * Copyright (C) 2011  Marco Trevisan (Treviño) <mail@3v1n0.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.editors.vala;
 
 public interface IValaLanguageWords {
 	//TODO add CCode
 	String[] keywords = { "#if",
 			  			  "#else",
 			  			  "#endif",
 						  "abstract",
 						  "as",
 						  "async",
 						  "base", 
 						  "break", 
 						  "case", 
 						  "catch", 
 						  "class", 
 						  "const", 
 						  "construct", 
 						  "continue", 
 						  "default",
 						  "delete",
 						  "dynamic",
 						  "do", 
 						  "else",
 						  "ensures",
 						  "enum",
 						  "errordomain",
 						  "extern",
 						  "inline",
 						  "internal",
 						  "finally", 
 						  "for", 
 						  "foreach", 
 						  "get", 
 						  "if", 
 						  "in", 
 						  "interface",
 						  "is", 
 						  "lock", 
 						  "namespace", 
 						  "new", 
 						  "out",
 						  "override",
 						  "owned",
 						  "params",
 						  "private",
 						  "protected", 
 						  "public", 
 						  "ref", 
 						  "return", 
 						  "set", 
 						  "sizeof",
 						  "static", 
 						  "struct", 
 						  "switch", 
 						  "this", 
 						  "throw", 
 						  "throws", 
 						  "try",
 						  "typeof", 
 						  "using",
 						  "unowned",
						  "var", 
 						  "virtual",
 						  "volatile",
 						  "weak", 
 						  "while",
 						  "yield",
 						  "yields" };
 	
 	String[] types = { "bool",
 					   "char",
 					   "delegate", 
 					   "double",
 					   "float", 
 					   "int",
 					   "int8",
 					   "int16",
 					   "int32",
 					   "int64",
 					   "long",
 					   "short",
 					   "signal",
 					   "size_t",
 					   "ssize_t",
 					   "string",
 					   "uchar",
 					   "uint",
 					   "uint8",
 					   "uint16",
 					   "uint32",
 					   "uint64",
 					   "ulong",
 					   "unichar",
 					   "ushort",
 					   "void" };
 	
 	String[] constants = { "false", 
 						   "null", 
 						   "true" };
 
 }
