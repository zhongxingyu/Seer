 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.syr.bytecast.jimple.api;
 
 import edu.syr.bytecast.amd64.api.instruction.IInstruction;
 import java.util.List;
import edu.syr.bytecast.jimple.beans.ParsedInstructionsSet;
 /**
  *
  * @author QSA
  */
 public interface IJimple {
  void createJimple(List<IInstruction> inst_List, 
            List<ParsedInstructionsSet> parsed_List);
 }
