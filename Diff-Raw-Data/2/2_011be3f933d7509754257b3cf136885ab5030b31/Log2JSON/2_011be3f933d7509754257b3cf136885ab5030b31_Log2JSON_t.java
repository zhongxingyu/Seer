 package uk.ac.ebi.age.log;
 
 import uk.ac.ebi.age.log.LogNode.Level;
 
 import com.pri.util.StringUtils;
 
 public class Log2JSON
 {
  
  public static String convert( LogNode nd )
  {
   StringBuilder sb = new StringBuilder();
   
   setLevels( nd );
   
   convertNode(nd, sb, 0);
   
   return sb.toString();
  }
  
  private static void convertNode( LogNode ln, StringBuilder sb, int lyr )
  {
   sb.append("{\n");
   sb.append(" level: \"").append(ln.getLevel().name()).append("\",\n");
   
   if(  ln.getMessage() != null )
   {
    sb.append(" message: \"");
   StringUtils.appendBackslashed(sb, ln.getMessage(),'"');
    sb.append("\",\n");
   }
   
   if( ln.getSubNodes() != null )
   {
    sb.append(" subnodes: [");
    
    for( LogNode snd : ln.getSubNodes() )
    {
     convertNode(snd,sb,lyr+1);
     sb.append(",\n");
    }
    
    sb.append("]\n");
   }
   
   sb.append("}");
  }
  
  private static void setLevels( LogNode ln )
  {
   if( ln.getSubNodes() == null )
    return;
   
   LogNode.Level maxLevel = Level.getMinLevel();
   
   for( LogNode snd : ln.getSubNodes() )
   {
    setLevels(snd);
    
    if( snd.getLevel().getPriority() > maxLevel.getPriority() )
     maxLevel = snd.getLevel();
   }
   
   ln.setLevel(maxLevel);
  }
  
 }
