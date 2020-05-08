 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.util.PythonInterpreter;
import java.lang.System;
 
 public class YayFactory {
 
     public YayFactory() {
	System.setProperty("python.cachedir.skip", "false");
	PythonInterpreter interpreter = new PythonInterpreter();
         interpreter.exec("from yay_gui import YayGui");
         
        interpreter.exec("YayGui()");
        
     }
 
     private PyObject jyYayClass;
}
