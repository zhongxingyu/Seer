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
 
 package RESOLVE.Main.Concepts.Standard.Integer_Template;
 
 import RESOLVE.*;
 import RESOLVE.Main.Concepts.Standard.Boolean_Template.*;
 
 public class Std_Integer_Realiz extends RESOLVE_BASE implements Integer_Template{
 
   Std_Boolean_Realiz Std_Boolean_Fac = new Std_Boolean_Realiz();
 
   public class Integer implements Integer_Template.Integer {
     public int val;
 
     Integer() {
       val = 0;
     }
 
     Integer(int i) {
       val = i;
     }
 
 // getRep is special case, this will never be called
     public Object getRep() {
       return this;
     }
 
 // setRep is special case, this will never be called
     public void setRep(Object o) {
     }
 
     public RType initialValue() {
       return new Integer();
     }
 
     public String toString() {
       return new java.lang.Integer(val).toString();
     }
   }
 
   public Boolean_Template.Boolean Is_Zero(Integer_Template.Integer i) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i).val == 0);
   }
 
   public Boolean_Template.Boolean Is_Not_Zero(Integer_Template.Integer i) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i).val != 0);
   }
 
   public void Increment(Integer_Template.Integer i) {
    ((Std_Integer_Realiz.Integer)i).val = ((Std_Integer_Realiz.Integer)i).val + 1;
   }
 
   public void Decrement(Integer_Template.Integer i) {
     ((Std_Integer_Realiz.Integer)i).val = ((Std_Integer_Realiz.Integer)i).val - 1;
   }
 
   public Boolean_Template.Boolean Are_Equal(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val == ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Boolean_Template.Boolean Are_Not_Equal(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val != ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Boolean_Template.Boolean Less_Or_Equal(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val <= ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Boolean_Template.Boolean Less(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val < ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Boolean_Template.Boolean Greater(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val > ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Boolean_Template.Boolean Greater_Or_Equal(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return Std_Boolean_Fac.createBoolean(((Std_Integer_Realiz.Integer)i1).val >= ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Sum(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val + ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Difference(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val - ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Product(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val * ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Power(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     int v1 = ((Std_Integer_Realiz.Integer)i1).val;
     int v2 = ((Std_Integer_Realiz.Integer)i2).val;
     int r = 1;
     for(int i = 0; i < v2; i++) {
         r = r * v1;
     }
     return new Integer(r);
   }
 
   public void Divide(Integer_Template.Integer i, Integer_Template.Integer j, Integer_Template.Integer q) {
    ((Std_Integer_Realiz.Integer)i).val = (((Std_Integer_Realiz.Integer)i).val / ((Std_Integer_Realiz.Integer)j).val);
   }
 
   public Integer_Template.Integer Rem(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val % ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Mod(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     if(((Std_Integer_Realiz.Integer)i1).val < 0){
 		((Std_Integer_Realiz.Integer)i1).val = ((Std_Integer_Realiz.Integer)i1).val + ((Std_Integer_Realiz.Integer)i2).val;
 	}
     return new Integer(((Std_Integer_Realiz.Integer)i1).val % ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Quotient(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val / ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Div(Integer_Template.Integer i1, Integer_Template.Integer i2) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val / ((Std_Integer_Realiz.Integer)i2).val);
   }
 
   public Integer_Template.Integer Negate(Integer_Template.Integer i1) {
     return new Integer(-((Std_Integer_Realiz.Integer)i1).val);
   }
 
   public Integer_Template.Integer Replica(Integer_Template.Integer i1) {
     return new Integer(((Std_Integer_Realiz.Integer)i1).val);
   }
 
   public void Read(Integer_Template.Integer i) {
 	((Std_Integer_Realiz.Integer)i).val = TextIO.getInt();
   } 
 
   public void Write(Integer_Template.Integer i) {
 	TextIO.put(((Std_Integer_Realiz.Integer)i).val);
   } 
 
   public void Write_Line(Integer_Template.Integer i) {
 	TextIO.putln(((Std_Integer_Realiz.Integer)i).val);
   } 
 
   public Integer Max_Int() {
 	  return new Integer(java.lang.Integer.MAX_VALUE);
   }
   
   public Integer Min_Int() {
 	  return new Integer(java.lang.Integer.MIN_VALUE);
   }
 
   public void Clear(Integer_Template.Integer i) {
 
   }
 
   public Integer_Template.Integer createInteger() {
     return new Integer();
   }
 
   public Integer_Template.Integer createInteger(int i) {
     return new Integer(i);
   } 
 
 } 
