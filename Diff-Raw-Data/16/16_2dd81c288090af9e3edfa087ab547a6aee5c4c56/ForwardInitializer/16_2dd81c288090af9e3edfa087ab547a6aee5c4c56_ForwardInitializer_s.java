 package factory;
 
 import java.util.List;
 import model.Model;
 import commands.Forward;
 import commands.ICommand;
 
 
 public class ForwardInitializer extends AbstractInitializer {
     public ForwardInitializer (Model model, Parser parser) {
         super(model, parser);
         setNumArgs(Forward.NUM_ARGS);
 
     }
 
     @Override
     protected ICommand instantiate (List<ICommand> parameters) {
         return new Forward(parameters, getModel().getTurtle());
     }
 
 }
