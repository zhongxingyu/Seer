 // Copyright 2008 Peter William Birch <birchb@genyis.org>
 //
 // This software may be used and distributed according to the terms
 // of the Genyris License, in the file "LICENSE", incorporated herein by reference.
 //
 package org.genyris.io;
 
 import java.io.IOException;
 import org.genyris.exception.GenyrisException;
 
 
 public class StdioInStream implements InStream {
 
     private int _nextByte;
     private boolean _gotByte;
 
     public StdioInStream() {
         _gotByte = false;
     }
 
     public void unGet(char x) throws LexException {
         throw new LexException("StdioStream: unGet not implemented.");
     }
 
 
    public char readNext() throws LexException {
         if(!_gotByte) {
            throw new LexException("StdioInStream: readNext() called before hasData()");
         }
         _gotByte = false;
         return (char)_nextByte;
     }
 
     public boolean hasData() {
         try {
         	if(_gotByte) {
         		return true;
         	}
             _nextByte = System.in.read();
             _gotByte = true;
         }
         catch (IOException e) {
             _gotByte = false;
             return false;
         }
         if( _nextByte == -1 ) {
             _gotByte = false;
             return false;
         }
         else {
             return true;
         }
     }
 
     public void close() throws GenyrisException {}
 
 	public void resetAfterError() {
 	}
 
 }
