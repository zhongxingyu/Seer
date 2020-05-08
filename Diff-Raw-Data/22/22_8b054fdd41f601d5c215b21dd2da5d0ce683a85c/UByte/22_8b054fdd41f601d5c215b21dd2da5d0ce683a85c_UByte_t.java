 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.interpreters.primitives;
 
 import org.trypticon.hex.interpreters.Value;
 
 /**
  * An unsigned byte value.
  *
  * @author trejkaz
  */
 public class UByte extends Number implements Value {
     private final byte value;
 
     public UByte(byte value) {
         this.value = value;
     }
 
     public byte getValue() {
         return value;
     }
 
     @Override
     public int intValue() {
        return value & 0xFF;
     }
 
     public long longValue() {
        return value & 0xFF;
     }
 
     @Override
     public float floatValue() {
         return value;
     }
 
     @Override
     public double doubleValue() {
         return value;
     }
 
     public int length() {
         return 1;
     }
 
     public String toString() {
         return String.valueOf(value & 0xFF);
     }
 }
