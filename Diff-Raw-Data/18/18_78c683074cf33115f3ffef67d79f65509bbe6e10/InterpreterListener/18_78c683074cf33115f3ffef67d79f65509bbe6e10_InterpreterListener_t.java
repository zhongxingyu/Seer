 package columbia.plt.tt.typecheck;
 import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
 
 
 public interface InterpreterListener {
 
             public void info(String msg);
             public void error(String msg);
             public void error(String msg, Exception e);
            public void error(String msg, CommonTree t);
             public void error(String msg, Token t);
 
 
 }
