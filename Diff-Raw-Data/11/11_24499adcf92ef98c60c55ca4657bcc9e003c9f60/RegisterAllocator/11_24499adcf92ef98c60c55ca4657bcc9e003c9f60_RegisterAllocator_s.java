 import java.util.*;
 
 public class RegisterAllocator
 {
   private ArrayList<FuncBlock> blocks;
   private ArrayList<String> colors;
   
   
   public RegisterAllocator(ArrayList<FuncBlock> blocks)
   {
     this.blocks = blocks;
     this.colors = createColors();
   }
   
   public void color()
   {
     for(FuncBlock b : blocks)
     { 
       ArrayList<Block> blist = new ArrayList<Block>();
       makeList(blist, b);
       localInfo(blist);
       
       while(globalInfo(blist));
       InterferenceGraph ig = new InterferenceGraph();
       populateNodes(blist, ig);
       // debug
       /*System.out.println("Graph Nodes ~~> " + b.name);
       for(Node n : ig.nodes)
         System.out.println(n.reg.sparcName);*/
       
       interGraph(blist, ig);
       /*for(Node n : ig.nodes) {
         System.out.println(n.reg.sparcName);
         System.out.print("\t");
         for(Node edge : n.edges) {
           System.out.print(edge.reg.sparcName);
         }
         System.out.println();
       }*/
       colorGraph(b, ig);
       // debug
       /*System.out.println("Graph Nodes (colored) ~~> " + b.name);
       for(Node n : ig.nodes)
         System.out.println(n.reg.sparcName);*/
     }
     //globalInfo(b);
     //liveSet(b);
   }
   
   private void makeList(ArrayList<Block> blist, Block b)
   {
      if(!b.genkill)
      {
        blist.add(b);
        b.genkill = true;
        for(Block bs : b.successors)
        {
          makeList(blist, bs);
        }
      }
   }
   
   private void localInfo(ArrayList<Block> blist)
   {
        for(Block b : blist){
          b.createLocalInfo();
          /*System.out.println("GEN for " + b.name);
          System.out.println(b.gen);
          System.out.println("KILL for " + b.name);
          System.out.println(b.kill);*/
        }
   }
   
   private boolean globalInfo(ArrayList<Block> blist)
   {
     boolean change = false;
     for(Block b : blist)
     {
       change = change || b.createGlobalInfo();
       /*System.out.println("Liveout for " + b.name);
       System.out.println(b.liveOut);*/
     }
     return change;
   }
   
   private TreeSet<Register> getAllRegs(ArrayList<Block> blist)
   {
     TreeSet<Register> allRegs = new TreeSet<Register>(new RegisterComparator());
     for(Block b : blist)
     {
       for(Instruction i : b.instructions)
       {
         allRegs.addAll(i.getSources());
         allRegs.addAll(i.getDests());
       }
     }
     return allRegs;
   }
   
   private void populateNodes(ArrayList<Block> blist, InterferenceGraph ig)
   {
     for(Register r : getAllRegs(blist))
     {
       /*System.out.println("adding " + r.sparcName + " as node in graph");*/
       ig.nodes.add(new Node(r));
     }
   }
   
   private void interGraph(ArrayList<Block> blist, InterferenceGraph ig)
   {
     for(Block b : blist)
     {
        b.createInterGraph(ig);
     }
   }
   
   private void colorGraph(Block b, InterferenceGraph ig)
   {
     Stack<Node> nstack = new Stack<Node>();
     // destruct graph
     while(!ig.nodes.isEmpty())
     {
       Node selected = ig.nodes.get(0);
       selected.removeEdges();
       ig.nodes.remove(selected);
       nstack.push(selected);
     }
     // reconstruct graph
     while(!nstack.isEmpty())
     {
       Node selected = nstack.pop();
       ig.addAndColorNode(selected, colors);
     }
   }
   
   private ArrayList<String> createColors()
   {
     ArrayList<String> colors = new ArrayList<String>();
     for(int i = 0; i < 8; i++)
     {
       colors.add(new String("%l" + i));
     }
    for(int i = 0; i < 6; i++)
     {
       colors.add(new String("%i" + i));
     }
    for(int i = 0; i < 8; i++)
     {
       colors.add(new String("%g" + i));
     }
    for(int i = 0; i < 6; i++)
     {
       colors.add(new String("%o" + i));
    }
     return colors;
   }
 }
