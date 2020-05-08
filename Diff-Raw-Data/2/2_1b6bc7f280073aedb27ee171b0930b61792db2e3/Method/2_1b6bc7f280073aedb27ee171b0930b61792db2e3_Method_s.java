 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package edu.syr.bytecast.jimple.api;
 import edu.syr.bytecast.amd64.api.instruction.IInstruction;
 import edu.syr.bytecast.amd64.api.output.MemoryInstructionPair;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Fei Qi
  */
 
 // used to combine the method content and method info
 public class Method {
    private MethodInfo m_info;
     private List<MemoryInstructionPair> l_instruction;
 
     public MethodInfo getM_info() {
         return m_info;
     }
 
     public void setM_info(MethodInfo m_info) {
         this.m_info = m_info;
     }
 
     public List<MemoryInstructionPair> getL_instruction() {
         return l_instruction;
     }
 
     public void setL_instruction(List<MemoryInstructionPair> l_instruction) {
         this.l_instruction = l_instruction;
     }
 
 }
