 package com.boothj5.jarch.analyser;
 
 public class Violation {
 
     private final String message;
     private final String clazz;
     private final int lineNumber;
     private final String line;
 
     public Violation(final String message, final String clazz, final int lineNumber, final String line) {
         this.message = message;
         this.clazz = clazz;
         this.lineNumber = lineNumber;
         this.line = line;
     }
 
     public String getMessage() {
         return message;
     }
 
     public String getClazz() {
         return clazz;
     }
 
     public int getLineNumber() {
         return lineNumber;
     }
 
     public String getLine() {
         return line;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o == null) { return false; }
         if (!(o instanceof Violation)) { return false; }
 
         Violation other = (Violation) o;
         return this.message.equals(other.getMessage())
                 && this.clazz.equals(other.getClazz())
                 && this.lineNumber == other.getLineNumber()
                 && this.line.equals(other.getLine());
     }
 
     @Override
     public int hashCode() {
        fint hash = 1;
         hash = hash * 17 + this.message == null ? 0 : this.message.hashCode();
         hash = hash * 3 + this.clazz == null ? 0 : this.clazz.hashCode();
         hash = hash * 5 + this.lineNumber;
         hash = hash * 13 + this.line == null ? 0 : this.line.hashCode();
         return hash;
     }
 
 }
