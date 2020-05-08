 package uk.ac.ebi.age.log.impl;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.ebi.age.log.LogNode;
 
 public class BufferLogger
 {
  private LogNode rootNode;
  
  public BufferLogger()
  {
   rootNode = new LBNode( null, "" );
  }
  
  public LogNode getRootNode()
  {
   return rootNode;
  }
  
  public static void printBranch( LogNode node )
  {
   printBranch(node, new PrintWriter( System.out ), 0);
  }
  
  public static void printBranch( LogNode node, PrintWriter out  )
  {
   printBranch(node, out, 0);
  }
 
  
  private static void printBranch( LogNode node, PrintWriter out, int lvl )
  {
   for( int i=0; i < lvl; i++ )
    out.print("  ");
   
   if( node.getLevel() != null )
   {
    switch( node.getLevel() )
    {
     case DEBUG:
      out.print("DEBG: ");
      break;
     case INFO:
      out.print("INFO: ");
      break;
     case WARN:
      out.print("WARN: ");
      break;
     case ERROR:
      out.print("ERRR: ");
      break;
    }
    
    out.println(node.getMessage());
   }
   else
   {
    out.println(node.getMessage());
    
    if( node.getSubNodes() != null )
    {
     for(LogNode sbNode : node.getSubNodes() )
      printBranch(sbNode,out, lvl+1);
    }
   }
    
  }
 
  
 class LBNode implements LogNode
  {
   private String nodeMessage;
   private Level level;
   
   private List<LogNode> subNodes;
 
   LBNode( Level l, String msg )
   {
    nodeMessage = msg;
    level = l;
   }
   
   @Override
   public void log(Level lvl, String msg)
   {
    if( subNodes == null )
     subNodes = new ArrayList<LogNode>(10);
    
    subNodes.add(new LBNode(lvl, msg));
   }
 
   @Override
   public LogNode branch(String msg)
   {
    if( subNodes == null )
     subNodes = new ArrayList<LogNode>(10);
    
    LogNode nnd = new LBNode(null, msg);
    
    subNodes.add(nnd);
    
    return nnd;
   }
 
   @Override
   public void append(LogNode node)
   {
    if( subNodes == null )
     subNodes = new ArrayList<LogNode>(10);
    
    subNodes.add(node);
   }
   
   @Override
   public String getMessage()
   {
    return nodeMessage;
   }
 
   @Override
   public Level getLevel()
   {
    return level;
   }
 
   @Override
   public List<LogNode> getSubNodes()
   {
    return subNodes;
   }
 
 
   
  }
 }
