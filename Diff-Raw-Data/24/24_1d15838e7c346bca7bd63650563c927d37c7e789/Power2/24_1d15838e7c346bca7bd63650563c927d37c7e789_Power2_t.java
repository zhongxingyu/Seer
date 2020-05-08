 /*
 # This file Power2.java is part of xversi-java-parallel, a board game called reversi or othello.
 #
 # Copyright (c) 2002-2006 Chung Shin Yee <cshinyee@gmail.com>
 #
 #       http://cshinyee.blogspot.com/index.html
 #
 # This program is free software; you can redistribute it and/or
 # modify it under the terms of the GNU General Public License as
 # published by the Free Software Foundation; either version 2 of the
 # License, or (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program; if not, write to the Free Software
 # Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 # USA.
 #
 # The GNU General Public License is contained in the file COPYING.
 #
 */
 
 import java.io.Serializable ;
 
 
 /* providing look up for values of 2^power */
 public class Power2 implements Serializable {
 
private final static long serialVersionUID = 2006L ;

 private final static int POWER_SIZE = 64 ;
 private static long[] power = new long[POWER_SIZE] ;
 
 public static void Init() {
 
   int i ;
   long count = 1 ;
 
   for(i = 0 ; i < POWER_SIZE ; i++) {
     power[i] = count ;
     count *= 2 ;
   }
 
 }
 
 public static long GetPower(int n) {
   
   if(n >= 0 && n <= POWER_SIZE) 
     return power[n] ;
   else
     return 0 ;
 
 }
 
 }
