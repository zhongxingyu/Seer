 package mud.network.server.input.interpreter;
 
 /**
  *
 * @author Japhez
  */
 public class CommandHelpFile {
 
     private String category;
     private String syntax;
     private String usage;
 
     public CommandHelpFile(String category, String syntax, String usage) {
         this.category = category;
         this.syntax = syntax;
         this.usage = usage;
     }
 
     public String getCategory() {
         return category;
     }
 
     public String getSyntax() {
         return syntax;
     }
 
     public String getDescription() {
         return usage;
     }
 }
