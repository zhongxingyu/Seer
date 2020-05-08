 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.util.PythonInterpreter;
 
 public class YayFactory {
 
     public YayFactory() {
        PythonInterpreter interpreter = new PythonInterpreter();
         interpreter.exec("from yay_gui import YayGui");
        //jyYayClass = interpreter.get("YayGui()");
             interpreter.exec("YayGui()");
         
    // PyObject result = interpreter.eval("yay_gui.YayGui");
     }
 
     private PyObject jyYayClass;
}
