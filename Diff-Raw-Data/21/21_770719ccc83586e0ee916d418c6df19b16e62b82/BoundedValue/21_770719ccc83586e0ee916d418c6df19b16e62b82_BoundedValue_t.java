 /*
  Copyright (C) 2012 William James Dyce
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package wjd.util;
 
import java.io.Serializable;

 /**
  * Value with a minimum and a maximum.
  *
  * @author wdyce
  * @since Nov 10, 2012
  */
public class BoundedValue implements Serializable
 {
   /* FUNCTIONS */
   
   private static float bound(float v, float min, float max)
   {
     return ((v > max) ? max : ((v < min) ? min : v));
   }
   
   /* ATTRIBUTES */
   private float balance, min, max;
   
   /* METHODS */
   
   // constructors
   public BoundedValue(float balance, float min, float max)
   {
     this.balance = balance;
     if(min > max)
     {
       this.min = max;
       this.max = min;
     }
     else
     {
       this.min = min;
       this.max = max;
     }
   }
   
   public BoundedValue(float balance, float max)
   {
     this(balance, 0, max);
   }
   
   public BoundedValue(float max)
   {
     this(0, 0, max);
   }
 
   // accessors
   
   public float value()
   {
     return balance;
   }
   
   public boolean isEmpty()
   {
     return (balance == min);
   }
   
   public boolean isFull()
   {
     return (balance == max);
   }
 
   // mutators
   
   public BoundedValue value(float _value)
   {
     balance = bound(_value, min, max);
     return this;
   }
   
   /**
    * 
    * @param amount how much we're trying to withdraw.
    * @return the amount which was successfully withdrawn.
    */
   public float tryWithdraw(float amount)
   {
     if(balance >= amount)
       balance -= amount;
     else
     {
       amount = balance;
       balance = 0;
     }
     // return the amount that was withdrawn
     return amount;
   }
   
   /**
    * 
    * @param amount how much we're trying to deposit.
    * @return the amount which was successfully deposited.
    */
   public float tryDeposit(float amount)
   {
     if(balance + amount <= max)
       balance += amount;
     else
     {
       amount = max - balance;
       balance = max;
     }
     // return the amount that was deposited
     return amount;
   }
   
   public float empty()
   {
     float temp = balance;
     balance = 0;
     return temp;
   }
   
   public float fill()
   {
     float temp = max-balance;
     balance = max;
     return temp;
   }
   
   /* OVERRIDES -- OBJECT */
   
   @Override
   public String toString()
   {
     return balance + " in (" + min + ',' + max + ')';
   }
 }
