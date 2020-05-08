 /*
  * This softare is released under the new BSD 2006 license.
  * 
  * Note the new BSD license is equivalent to the MIT License, except for the
  * no-endorsement final clause.
  * 
  * Copyright (c) 2007, Clemson University
  * 
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer. 
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution. 
  *   * Neither the name of the Clemson University nor the names of its
  *     contributors may be used to endorse or promote products derived from
  *     this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * This sofware has been developed by past and present members of the
  * Reusable Sofware Research Group (RSRG) in the School of Computing at
  * Clemson University.  Contributors to the initial version are:
  * 
  *     Steven Atkinson
  *     Greg Kulczycki
  *     Kunal Chopra
  *     John Hunt
  *     Heather Keown
  *     Ben Markle
  *     Kim Roche
  *     Murali Sitaraman
  */
 
 package RESOLVE.Main.Concepts.Standard.Character_Template;
 
 import RESOLVE.*;
 import RESOLVE.Main.Concepts.Standard.Boolean_Template.*;
 import RESOLVE.Main.Concepts.Standard.Integer_Template.*;
 
 public class Std_Character_Realiz extends RESOLVE_BASE implements Character_Template{
 
   Std_Boolean_Realiz Std_Boolean_Fac = new Std_Boolean_Realiz();
   Std_Integer_Realiz Std_Integer_Fac = new Std_Integer_Realiz();
 
   public class Character implements Character_Template.Character {
     public char val;
 
     Character() {
       val = ' ';
     }
 
     Character(char c) {
       val = c;
     }
 
 // getRep is special case, this will never be called
     public Object getRep() {
       return this;
     }
 
 // setRep is special case, this will never be called
     public void setRep(Object o) {
     }
 
     public RType initialValue() {
       return new Character();
     }
 
     public String toString() {
       return new java.lang.Character(val).toString();
     }
   }
 
 // this operation here only for the use of Std_Char_Str_Realiz to convert a Character to Char_Str
   public String Make_String(Character_Template.Character c) {
     return new java.lang.Character(((Std_Character_Realiz.Character)c).val).toString();
   }
 
   public Character_Template.Character Replica(Character_Template.Character c) {
     return new Character(((Std_Character_Realiz.Character)c).val);
   }
 
   public Character_Template.Character createCharacter() {
     return new Character();
   }
 
   public Character_Template.Character createCharacter(char c) {
     return new Character(c);
   } 
 
   public Integer_Template.Integer  Char_to_Int(Character_Template.Character  c) {
     return Std_Integer_Fac.createInteger(((Std_Character_Realiz.Character)c).val );
   }
 
   public Boolean_Template.Boolean Are_Equal(Character_Template.Character c1, 
                                             Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(((Std_Character_Realiz.Character)c1).val == ((Std_Character_Realiz.Character)c2).val);
   }
 
   public Boolean_Template.Boolean Are_Not_Equal(Character_Template.Character c1, 
                                             Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(!(((Std_Character_Realiz.Character)c1).val == ((Std_Character_Realiz.Character)c2).val));
   }
 
   public Boolean_Template.Boolean Less(Character_Template.Character c1,
 					    Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(((Std_Character_Realiz.Character)c1).val < ((Std_Character_Realiz.Character)c2).val);
   }
 
   public Boolean_Template.Boolean Less_Or_Equal(Character_Template.Character c1,
 					    Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(((Std_Character_Realiz.Character)c1).val <= ((Std_Character_Realiz.Character)c2).val);
   }
 
   public Boolean_Template.Boolean Greater(Character_Template.Character c1,
 					    Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(((Std_Character_Realiz.Character)c1).val > ((Std_Character_Realiz.Character)c2).val);
   }
 
   public Boolean_Template.Boolean Greater_Or_Equal(Character_Template.Character c1,
 					    Character_Template.Character c2) {
     return Std_Boolean_Fac.createBoolean(((Std_Character_Realiz.Character)c1).val >= ((Std_Character_Realiz.Character)c2).val);
   } 
 
   public void Read(Character_Template.Character c) {
 	((Std_Character_Realiz.Character)c).val = TextIO.getChar();
	TextIO.getln();
   } 
 
   public void Write(Character_Template.Character c) {
 	TextIO.put(((Std_Character_Realiz.Character)c).val);
   } 
 
   public void Write_Line(Character_Template.Character c) {
 	TextIO.putln(((Std_Character_Realiz.Character)c).val);
   } 
 
 }
