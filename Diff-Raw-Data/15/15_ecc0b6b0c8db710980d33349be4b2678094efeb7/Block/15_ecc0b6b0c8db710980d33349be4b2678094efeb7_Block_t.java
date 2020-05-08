 import java.util.ArrayList;
 
 public class Block
 {
   public ArrayList<Instruction> instructions;
   public ArrayList<Block> successors;
   public ArrayList<Block> predecessors;
   
  public String name;

   public Block()
   {
     instructions = new ArrayList<Instruction>();
     successors = new ArrayList<Block>();
     predecessors = new ArrayList<Block>();
   }
  
  public String toString(){
    return name;
  }
 }
