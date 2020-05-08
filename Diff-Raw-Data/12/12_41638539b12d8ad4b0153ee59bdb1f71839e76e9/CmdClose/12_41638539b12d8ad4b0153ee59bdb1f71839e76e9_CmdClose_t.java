 package cmd;
 
 import model.HTMLBuffer;
 import model.Mediator;
 import model.Session;
 
 public class CmdClose extends Command
 {
     private Mediator mediator;
  
     public CmdClose(Mediator mediator)
     {
         this.mediator=mediator;
     }
     @Override
     public void execute(Context context)
     {
        mediator.close(context.getBuffer());
     }
 
 }
