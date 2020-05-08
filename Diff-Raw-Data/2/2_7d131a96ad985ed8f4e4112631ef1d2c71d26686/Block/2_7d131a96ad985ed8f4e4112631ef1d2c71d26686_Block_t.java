 import java.util.ArrayList;
 
 public class Block
 {
   public static int counter = 0;
   
   public ArrayList<Instruction> instructions;
   public ArrayList<Block> successors;
   public ArrayList<Block> predecessors;
   
   public String name;
   
   boolean visited;
 
   public Block()
   {
     visited = false;
    //name = "exit";
     instructions = new ArrayList<Instruction>();
     successors = new ArrayList<Block>();
     predecessors = new ArrayList<Block>();
   }
   
   public void printTree(){
     System.out.println(name + ":" + subTreeString() + "\n");
     for(Block b : successors)
     {
       if(!b.visited){
         b.visited = true;
         b.printTree();
       }
     }
   }
   
   public void printInstructions()
   {
     System.out.println(name + ":");
     for(Instruction i : instructions)
     {
       System.out.println(i);
     }
     for(Block b : successors)
     {
       if(!b.visited){
         b.visited = true;
         b.printInstructions();
       }
     }
   }
   
   public String toString(){
     return name + ":" + subTreeString() + "\n";
     
   }
   
   private String subTreeString()
   {
     String str = "";
     for(Block b : successors)
     {
       str += (b.name + ", ");
     }
     //System.out.println("finished subtree");
     
     return str;
   }
 }
