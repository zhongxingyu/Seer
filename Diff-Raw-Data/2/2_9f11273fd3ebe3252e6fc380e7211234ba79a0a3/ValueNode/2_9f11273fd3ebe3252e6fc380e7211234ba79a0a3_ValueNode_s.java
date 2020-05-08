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
 
 package de.weltraumschaf.registermachine.inter;
 
 /**
  * Super type for {@link ConstNode} and {@link VarNode}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 abstract class ValueNode extends AbstractNode {
 
     /**
      * Name of the variable.
      */
     private final String name;
     /**
      * Value of the variable.
      */
     private final Value value;
 
     /**
      * Not used outside package.
      *
      * @param name name of value.
      * @param value typed value.
     * @param type either {@value AstNode.Type#CONST} or {@value AstNode.Type#}
      */
     ValueNode(final String name, final Value value, final Type type) {
         super(type);
         this.name = name;
         this.value = value;
     }
 
     /**
      * Get the name.
      *
      * @return name as string
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Get the value.
      *
      * @return typed value object
      */
     public final Value getValue() {
         return value;
     }
 
 }
