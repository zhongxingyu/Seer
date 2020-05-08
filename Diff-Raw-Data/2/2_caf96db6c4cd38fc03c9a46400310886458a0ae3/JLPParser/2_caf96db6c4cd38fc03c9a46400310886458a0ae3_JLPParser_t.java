 /**
  * # JLPParser
  * @author Jonathan Bernard (jdb@jdb-labs.com)
  * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
  */
 package com.jdblabs.jlp;
 
 import com.jdblabs.jlp.ast.SourceFile;
 
 /**
  * JLPParser is a simple interface. It has one method to return a parsed
  * [`SourceFile`] given an input string. It may be expanded in the future to
  * be an abstract class implementing methods that take additional input for
  * convenience.
  *
 * [`SourceFile`]: jlp://jlp.jdb-labs.com/ast/SourceFile
  *
  * @org jlp.jdb-labs.com/JLPParser
  */
 public interface JLPParser {
     public SourceFile parse(String input); }
