 package com.ctco.louchansky.stringformatter.structures;
 
 import java.util.LinkedList;
 
 public class NewlineLinkedList<Character> extends LinkedList<Character> {
 
     public void tryToEmpty() {
         if (this.size() > 1 && this.getLast().equals('\n'))
             this.clear();
     }
 }
