 package com.jexpect.matchers;
 
 import com.jexpect.ToBeBoolean;
 
 class ExpectToBeBoolean implements ToBeBoolean {
 
  private final boolean actual;
 
  ExpectToBeBoolean(boolean actual) {
    this.actual = actual;
   }
 
   @Override
   public void toBeTrue() {
    if (!actual) {
       throw new IllegalArgumentException();
     }
   }
 
   @Override
   public void toBeFalse() {
    if (actual) {
       throw new IllegalArgumentException();
     }
   }
 
   @Override
  public void toBe(boolean expected) {
    if (actual != expected) {
      throw new IllegalArgumentException();
    }
   }
 }
