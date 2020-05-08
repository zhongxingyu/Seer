 import java.util.ArrayList;
 
 public class Block
 {
   public ArrayList<Instruction> instructions;
   public ArrayList<Block> successors;
   public ArrayList<Block> predecessors;
   
   public String name;
 
   public Block()
   {
     name = "exit";
     instructions = new ArrayList<Instruction>();
     successors = new ArrayList<Block>();
     predecessors = new ArrayList<Block>();
   }
   
   public String toString(){
     
    return name + ":\n\t" + subTreeString();
   }
   
   private String subTreeString()
   {
     String str = "";
     for(Block b : successors)
     {
      str += b.toString();
     }
     //System.out.println("finished subtree");
     
     return str;
   }
 }
