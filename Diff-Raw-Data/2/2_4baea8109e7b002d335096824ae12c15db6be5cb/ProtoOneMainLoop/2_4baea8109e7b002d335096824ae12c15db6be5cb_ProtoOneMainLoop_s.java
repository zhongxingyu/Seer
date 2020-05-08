 /*
  * The Tokyo Project is hosted on Sourceforge:
  * http://sourceforge.net/projects/tokyo/
  * 
  * Copyright (c) 2005-2007 Eric Bréchemier
  * http://eric.brechemier.name
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  *
  */
 package net.sf.tokyo.prototype1;
 
 import net.sf.tokyo.ITokyoNaut;
 
 public class ProtoOneMainLoop implements ITokyoNaut
 {
   public static void main(String[] args)
   {
     if (args.length <3)
     {
       System.out.println("Usage: [ProtoOneMain] inCsvFilePath stylesheetFilePath outCsvFilePath");
       return;
     }
     String inCsvFilePath = args[0];
     String stylesheetFilePath = args[1];
     String outCsvFilePath = args[2];
     
     Object startRule = new int[]{2};
     ITokyoNaut mainLoop = new ProtoOneMainLoop();
     Object[] rules =
     {
       new ProtoOneLang(),
       new ProtoOneLang.InFileNaut(),
       new ProtoOneLang.InCsvNaut(),
       new ProtoOneLang.InSaxNaut(),
       new ProtoOneLang.XslTransformNaut(),
       new ProtoOneLang.OutSaxNaut(),
       new ProtoOneLang.OutCsvNaut(),
       new ProtoOneLang.OutFileNaut()
     };
     
     final int RULES_COUNT = 7;
     Object[] state = new Object[RULES_COUNT];
    Object[] data = new Object[RULES_COUNT];
     
     mainLoop.morph(rules,state,data);
   }
     
   public void morph(Object[] rules, Object[] state, Object[] data)
   {
     if (  !ProtoOneLang.URI.equals( rules[0].toString() )  )
       return;
     
     int[] currentRuleIndex = new int[1];
     state = new Object[] {currentRuleIndex};
     
     do
     {
       for (int i=1; i<rules.length; i++)
       {
         currentRuleIndex[0]=i;
         ITokyoNaut currentRule = (ITokyoNaut)rules[i];
         currentRule.morph(rules,state,data);
       }
     } while( data[data.length-1]!=null );
     
     return;
   }
   
 }
