 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package de.weltraumschaf.registermachine.asm;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 enum TokenType {
 
     /**
     * Literal strings, like: true, false, nil, a, zet42 etc.
      *
      * literal = "a" .. "Z" [ { "a" .. "Z" | "0" .. "9" } ]
      */
     LITERAL,
     /**
      * Everything defined in {@link de.weltraumschaf.registermachine.bytecode.OpCode}.
      *
      * opcode = "a" .. "z" [ { "a" .. "z" } ]
      */
     OPCODE,
     /**
      * Meta codes.
      *
      * meta = ".function" | ".var" | ".const" .
      */
     METACODE,
     /**
      * Everything between quotes.
      */
     STRING,
     /**
      * Numbers with a dot in it.
      */
     FLOAT,
     /**
      * Numbers without a dot in it.
      */
     INTEGER;
 
 }
