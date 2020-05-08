 package com.ctco.louchansky.stringformatter.structures;
 
 import java.util.Stack;
 
 public class NewlineStack<Character> extends Stack<Character> {
 
     public String toString() {
         NewlineStack<Character> out = new NewlineStack<Character>();
         out.addAll(this);
        String result = "";
         while (!out.isEmpty()) {
            result += out.pop();
         }
        return result;

     }
 
     public void addAllIfNotEmptyAndClear(NewlineLinkedList<Character> tempStorage) {
 
         tempStorage.tryToEmpty();
 
         if (!tempStorage.isEmpty()) {
             this.addAll(tempStorage);
             tempStorage.clear();
         }
 
     }
 
 }
