 package logisim.scripts;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import com.cburch.logisim.gui.generic.LFrame;
 
 import org.python.core.PyException;
 import org.python.core.PyInteger;
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 public class ScriptHandler {
 	private String script;
 	
 	public ScriptHandler(String script){
 		this.script=script;
 	}
 	
 	
 	public boolean containsError(){
 		boolean errorResult=false;
 		PythonInterpreter interp = new PythonInterpreter();
 		try{
 			interp.exec(script);
 		}
 		catch(Exception e){
 			errorResult=true;
 			LFrame helpFrame = new LFrame();
			JLabel textArea=new JLabel(" An error in the script has been found: \n"
					+e.toString());
 			helpFrame.setTitle("Logisim Script Error");
 			helpFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			helpFrame.setSize(300, 100);
			helpFrame.getContentPane().add(textArea);
 			helpFrame.setVisible(true);
 		}
 		return errorResult;
 		
 	}
 }
 	
