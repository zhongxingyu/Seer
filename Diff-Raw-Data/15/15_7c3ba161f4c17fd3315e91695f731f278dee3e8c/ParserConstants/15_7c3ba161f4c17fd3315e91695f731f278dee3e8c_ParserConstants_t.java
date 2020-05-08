 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parser;
 
 /**
  *
  * @author Timo Vesalainen
  */
 public interface ParserConstants
 {
     /**
      * Name of local variable 0 = this
      */
     static final String THIS = "this";
     /**
      * Name of local variable containing InputReader
      */
     static final String INPUTREADER = "$inputReader";
     /**
      * Name of local variable containing stack pointer
      */
     static final String SP = "$sp";
     /**
      * Name of local variable containing current token number
      */
     static final String CURTOK = "$curTok";
     /**
      * Name of local variable containing read token number
      */
     static final String TOKEN = "$token";
     /**
      * Name of local variable containing current type (= ObjectType ordinal)
      */
     static final String CURTYPE = "$curType";
     /**
      * Name of local variable containing state stack (=int[])
      */
     static final String STATESTACK = "$stateStack";
     /**
      * Name of local variable containing type stack (=int[])
      */
     static final String TYPESTACK = "$typeStack";
     /**
      * Name of local variable containing value stack (= Object[])
      */
     static final String VALUESTACK = "$valueStack";
     /**
      * Name of prefix for current type (= ObjectType toString())
      */
     static final String CUR = "$cur";
     /**
      * Input method prefix
      */
     static final String INPUT = "$input";
     /**
      * Lookahead input method prefix
      */
     static final String LAINPUT = "$laInput";
     /**
      * Name of local variable containing lookahead state
      */
     static final String LASTATE = "$laState";
     /**
      * Name of local variable containing lookahead token number
      */
     static final String LATOKEN = "$laToken";
     /**
      * Name of local variable containing lookahead length
      */
     static final String LALENGTH = "$laLength";
     /**
      * Error token number
      */
     static final int ERROR = -1;
     /**
      * Eof token number
      */
     static final int EOF = 0;
     
     //static final String READER = "$reader";
     /**
      * Name of local variable 1. The original input object from which the InputReader is constructed.
      */
     static final String IN = "$in";
     /**
      * Prefix of generated parse method
      */
     static final String PARSEMETHODPREFIX = "$parse-";
 
     /**
      * Name of getToken method
      */
     static final String GETTOKEN = "getToken";
     static final String RULE = "$rule";
     static final String GETRULE = "getRule";
     static final String GETEXPECTED = "getExpected";
     static final String ARG = "$arg";
     /**
      * 
      */
     static final String SOURCESTACK = "$sourceStack";
     static final String LINESTACK = "$lineStack";
     static final String COLUMNSTACK = "$columnStack";
     static final String OFFSETSTACK = "$columnStack";
     /**
      * Name of the local variable containing thrown exception
      */
    static final String THROWABLE = "$throwable";
 }
