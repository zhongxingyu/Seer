 package logisim.scripts;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
import javax.swing.JPanel;
 
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
			int length=e.toString().length();
 			errorResult=true;
 			LFrame helpFrame = new LFrame();
			JPanel panel=new JPanel();
			JLabel textArea=new JLabel(" An error in the script has been found: "
					);
			JLabel errorTextArea=new JLabel(e.toString());
			panel.add(textArea);
			panel.add(errorTextArea);
 			helpFrame.setTitle("Logisim Script Error");
 			helpFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			helpFrame.setSize(length*6, 100);
			helpFrame.getContentPane().add(panel);
 			helpFrame.setVisible(true);
 		}
 		return errorResult;
 		
 	}
	
 }
 	
