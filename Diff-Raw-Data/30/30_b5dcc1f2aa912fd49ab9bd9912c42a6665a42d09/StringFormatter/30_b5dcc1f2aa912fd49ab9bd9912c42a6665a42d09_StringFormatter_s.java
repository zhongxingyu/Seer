 package com.ctco.louchansky.stringformatter;
 
 import com.ctco.louchansky.stringformatter.structures.NewlineLinkedList;
 import com.ctco.louchansky.stringformatter.structures.NewlineStack;
 
 public class StringFormatter {
 
     private String data;
 
     public StringFormatter(String _data) {
         data = _data;
     }
 
     public String reverse() {
        return new StringBuffer(data).reverse().toString();
     }
 
     public String getString() {
         return data;
     }
 
     public char[] getStringChars() {
         return data.toCharArray();
     }
 
     public String formatNewlines() {
         if (!data.contains("\\\n"))
             return data;
 
         data = this.reverse();
 
         NewlineStack<Character> resultStack = new NewlineStack<Character>();
         NewlineLinkedList<Character> tempStorage = new NewlineLinkedList<Character>();
 
         for (Character c : this.getStringChars()) {
 
             if (c == '\\') {
 
                 tempStorage.push(c);
 
             } else if (c == '\n') {
 
                 resultStack.addAllIfNotEmptyAndClear(tempStorage);
                 tempStorage.push(c);
 
             } else {
 
                 resultStack.addAllIfNotEmptyAndClear(tempStorage);
                 resultStack.push(c);
 
             }
         }
 
         return resultStack.toString();
     }
 
 }
