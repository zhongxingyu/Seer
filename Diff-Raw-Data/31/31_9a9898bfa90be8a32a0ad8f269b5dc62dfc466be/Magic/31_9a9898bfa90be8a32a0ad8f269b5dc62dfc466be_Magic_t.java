 /*    Magic Builder
  * 	  A simple java program that grabs the latest prices and displays them per set.
     Copyright (C) 2013  Manuel Gonzales Jr.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
 
 package com.macleod2486.magicbuilder;
 
 import java.util.Scanner;
 
 public abstract class Magic 
 {
 	public static void main(String args[])
 	{
 		boolean executed;
 		String selection="";
 		
 		try
 		{
 			Scanner screen = new Scanner(System.in);
 			System.out.printf("Please make your selection\n1)Standard\n2)Extended\n3)Modern\n4)Legacy/Vintage\n");
 			selection=screen.next();
 			screen.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		if(selection.equals("1"))
 		{
 			Gatherer gather = new Gatherer();
 			executed=gather.gather(0);
 			if(executed)
 				gather.tcg();
 		}
 		else if(selection.equals("2"))
 		{
 			Gatherer gather = new Gatherer();
 			executed=gather.gather(1);
 			if(executed)
 				gather.tcg();
 		}
 		else if(selection.equals("3"))
 		{
 			Gatherer gather = new Gatherer();
 			executed=gather.gather(2);
 			if(executed)
 				gather.tcg();
 		}
 		else if(selection.equals("4"))
 		{
 			/*
 			 * Haven't quite worked out how to properly parse the info
 			 *  
 			 *  */
 			Gatherer gather = new Gatherer();
 			executed=gather.gatherAll();
 			if(executed)
 				gather.tcg();
 		}
 		
 		
 	}
 }
