 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.syr.bytecast.jimple.impl;
 
 import edu.syr.bytecast.amd64.api.instruction.IInstruction;
 import edu.syr.bytecast.jimple.api.IJimple;
import edu.syr.bytecast.jimple.beans.*;
 import java.util.List;
 
 /**
  *
  * @author QSA
  */
 public class Jimple implements IJimple{
 
   @Override
  public void createJimple(List<IInstruction> inst_List, 
            List<ParsedInstructionsSet> parsed_List) { 
        for (ParsedInstructionsSet pis : parsed_List) {
            //do something
            System.out.println("somethin");
    }            
  }  
 }
