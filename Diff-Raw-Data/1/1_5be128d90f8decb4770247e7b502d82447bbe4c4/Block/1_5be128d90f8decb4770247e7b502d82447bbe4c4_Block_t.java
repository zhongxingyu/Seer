 import java.util.*;
 
 public class Block
 {
   public static int counter = 0;
   
   public ArrayList<Instruction> instructions;
   public ArrayList<Block> successors;
   public ArrayList<Block> predecessors;
   public TreeSet<Register> gen;
   public TreeSet<Register> kill;
   public TreeSet<Register> liveOut;
   
   public String name;
   
   boolean visited;
   boolean genkill;
 
   public Block()
   {
     genkill = false;
     visited = false;
     //name = "exit";
     instructions = new ArrayList<Instruction>();
     successors = new ArrayList<Block>();
     predecessors = new ArrayList<Block>();
     RegisterComparator rc = new RegisterComparator();
     gen = new TreeSet<Register>(rc);
     kill = new TreeSet<Register>(rc);
     liveOut = new TreeSet<Register>(rc);
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
   
   public void printTreeReverse(){
     System.out.println(name + ":" + subTreeStringReverse() + "\n");
     for(Block b : predecessors)
     {
       if(!b.visited){
         b.visited = true;
         b.printTreeReverse();
       }
     }
   }
   
   private String subTreeStringReverse()
   {
     String str = "";
     for(Block b : predecessors)
     {
       str += (b.name + ", ");
     }
     //System.out.println("finished subtree");
     
     return str;
   }
   
   
   
   public String getInstructions(boolean sparc)
   {
     String rstring = "";
     rstring += name + ":\n";
     for(Instruction i : instructions)
     {
       String inststr;
       inststr = (sparc ? i.toSparc() : i.toString());
       rstring += "  " + inststr + "\n";
     }
     for(Block b : successors)
     {
       if(!b.visited){
         b.visited = true;
         rstring += b.getInstructions(sparc);
       }
     }
     return rstring;
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
   
   public void createLocalInfo(){
     for(Instruction i : instructions)
     {
       for(Register src : i.getSources())
       {
         /*if(src == null)
           System.out.println(i.toSparc() + " messed up");*/
         if(!kill.contains(src))
         {
           gen.add(src);
         }
       }
       for(Register dest : i.getDests())
       {
         /*if(dest == null)
           System.out.println(i.toSparc() + " messed up");*/
         kill.add(dest);
       }
     }
     //System.out.println(name);
     //System.out.println("gen: " + gen);
     //System.out.println("kill: " + kill);
   }
   
   public boolean createGlobalInfo()
   {
     TreeSet<Register> ret = new TreeSet<Register>(new RegisterComparator());
     TreeSet<Register> liveoutm = new TreeSet<Register>(new RegisterComparator());
     for(Block b : successors){
       liveoutm = new TreeSet<Register>(new RegisterComparator());
       liveoutm.addAll(b.liveOut);
       liveoutm.removeAll(b.kill);
       liveoutm.addAll(b.gen);
       ret.addAll(liveoutm);
     }
     return liveOut.addAll(ret);
   }
   
   public void addAllNodes(InterferenceGraph ig)
   {
     // create nodes
     TreeSet<Register> allregs = getAllRegs();
     for(Register r : allregs)
     {
      if(!r.sparcName.contains("%"))
       ig.addNode(new Node(r));
     }
   }
   
   public void createInterGraph(InterferenceGraph ig)
   {
     //System.out.println("creating graph for " + name);
     // compute live set and interference graph
     for(int i = instructions.size() - 1; i >= 0; i--)
     {
       for(Register dest : instructions.get(i).getDests())
       {
         liveOut.remove(dest);
         Node destnode = ig.nodeForRegister(dest);
         //System.out.println("Register " + dest + " conflicts with " + liveOut + "in " + instructions.get(i));
         for(Register r : liveOut)
         {
           destnode.addEdgeTo(ig.nodeForRegister(r));
         }
       }
       for(Register src : instructions.get(i).getSources())
       {
         liveOut.add(src);
       }
     }
   }
   
   private TreeSet<Register> getAllRegs()
   {
     TreeSet<Register> allregs = new TreeSet<Register>(new RegisterComparator());
     for(Instruction i : instructions)
     {
       //System.out.println("sdadsd " + i.toSparc() + " ASDASDASD");
       allregs.addAll(i.getSources());
       allregs.addAll(i.getDests());
     }
     allregs.addAll(liveOut);
     return allregs;
   }
 }
