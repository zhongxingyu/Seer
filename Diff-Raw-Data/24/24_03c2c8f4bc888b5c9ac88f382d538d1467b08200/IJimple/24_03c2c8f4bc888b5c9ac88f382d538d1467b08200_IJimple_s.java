 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.syr.bytecast.jimple.api;
 
 import edu.syr.bytecast.amd64.api.instruction.IInstruction;
 import java.util.List;

 /**
  *
  * @author QSA
  */
 public interface IJimple {
  void createJimple(List<IInstruction> inst_List);
 }
