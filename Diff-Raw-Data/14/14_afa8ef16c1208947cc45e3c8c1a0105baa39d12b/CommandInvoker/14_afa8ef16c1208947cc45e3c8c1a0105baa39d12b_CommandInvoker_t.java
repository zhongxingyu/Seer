 package cn.shenyanchao.ut.command.invoker;
 
 import cn.shenyanchao.ut.builder.CompilationUnitBuilder;
 import cn.shenyanchao.ut.command.AbstractCommand;
 
 /**
  * @author shenyanchao
  *         <p/>
  *         Date:  6/20/13
  *         Time:  7:25 PM
  */
 public class CommandInvoker {
 
     private AbstractCommand command;
 
    public CommandInvoker(AbstractCommand command) {
         this.command = command;
     }
 
     public CompilationUnitBuilder action() {
         return command.sendCommand();
     }
 }
