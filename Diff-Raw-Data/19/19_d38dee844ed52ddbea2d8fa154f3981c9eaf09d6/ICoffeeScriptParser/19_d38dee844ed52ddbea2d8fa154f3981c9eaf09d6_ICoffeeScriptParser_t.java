 package sidekick.coffeescript;
 
 import java.util.List;
 
/** Interface for CoffeeScriptParser.coffee/.js/.class */
 public interface ICoffeeScriptParser {
     public void parse(String source, Object rootNode, Object config);
 }
