 package factory;
 
 import java.awt.Dimension;
 import model.Model;
 import commands.ICommand;
 import exceptions.FormattingException;
 
 
 public class ParserTest {
     private static final String COMMAND_REGEX = "[a-zA-z_]+(\\?)?";
 
     public static void main (String[] args) {
 
        Parser parser = new Parser(new Model(new Dimension(600, 400)));
         try {
             String command = "sum 10 quotient 10 2 ]";
             ICommand main = parser.parse(command);
             System.out.println(main);
         }
         catch (FormattingException e) {
             e.printStackTrace();
         }
     }
 }
