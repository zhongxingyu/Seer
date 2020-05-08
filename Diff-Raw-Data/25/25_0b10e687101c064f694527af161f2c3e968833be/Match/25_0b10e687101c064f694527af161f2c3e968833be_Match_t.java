 /*
  * automaton
  *
  * Copyright (c) 2011 John Pritchard
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package automaton;
 
 /**
  * A greedy matching driver using {@link Compiled#run}.  The driver
  * halts at the first non matching state (as opposed to an unbounded
  * driver that searches to a matching state).
  * 
  * @author John Pritchard
  */
 public class Match 
     extends Object
     implements jauk.Match
 {
 
     protected final CharSequence chars;
 
     protected final int start;
 
     protected final int end;
 
 
     public Match(CharSequence chars, Compiled automaton) {
 	this(chars,automaton,0);
     }
     public Match(CharSequence chars, Compiled automaton, int ofs) {
 	super();
 	if (null != chars && null != automaton){
 	    this.chars = chars;
 
	    this.start = ofs;
 
 	    this.end = (automaton.run(chars,ofs)+1);
 	}
 	else
 	    throw new IllegalArgumentException();
     }
 
 
     public boolean satisfied(){
 	return (this.end > this.start);
     }
     public boolean terminal(){
 	return (this.end == this.chars.length());
     }
     public int next(){
 	return this.end;
     }
     public int start() {
         return this.start;
     }
     public int end() {
         return this.end;
     }
     public String group(){
	if (this.end > this.start)
	    return this.chars.subSequence(this.start, this.end).toString();
	else
	    return null;
     }
 }
