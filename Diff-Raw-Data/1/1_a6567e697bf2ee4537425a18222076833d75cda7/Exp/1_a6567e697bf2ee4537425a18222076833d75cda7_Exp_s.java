 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.dawnfirerealms.legends.library.level;
 
 
 /**
  * @author YoshiGenius
  */
 class Exp {
     private double exp;
     
     public Exp(double exp) {
         this.exp = exp;
     }
 
     public Exp() {
         this.exp = 0;
     }
 
     public Exp setExp(double exp) {
         this.exp = exp;
         return this;
     }
 
     public Exp addExp(double exp) {
         this.exp += exp;
         return this;
     }
 
     public Exp removeExp(double exp) {
         this.exp -= exp;
         return this;
<<<<<<< HEAD
     }
 
     public double getExp() {
         return exp;
     }
 
     public static Exp max(Exp exp1, Exp exp2) {
         return new Exp(Math.max(exp1.exp, exp2.exp));
     }
     
     public static Exp min(Exp exp1, Exp exp2) {
         return new Exp(Math.min(exp1.exp, exp2.exp));
     }
     
     public static boolean areEqual(Exp exp1, Exp exp2) {
         return (exp1.exp == exp2.exp);
     }
 
 }
