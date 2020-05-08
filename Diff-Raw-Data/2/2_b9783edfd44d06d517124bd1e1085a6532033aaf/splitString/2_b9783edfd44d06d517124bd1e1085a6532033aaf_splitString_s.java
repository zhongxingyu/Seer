 /*
  * Copyright (C) 2013
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 2 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details
  */
 
 public class splitString {
 	public static void main(String args[]){
 		String str = "2.2.0-10.0.59.20134.douglas.master.ev";
 
 		// dot is special code, require \\
 		String delimiter = "\\.";
 		String temp[] = str.split(delimiter);
 
        // Printing by delimiter
 		for(int i=0; i < temp.length; i++)
 			System.out.println(temp[i]);
 
 		if (temp[2].contains("-")) {
 			System.out.println(temp[2].split("-")[0]);
 		}
 	}
 }
