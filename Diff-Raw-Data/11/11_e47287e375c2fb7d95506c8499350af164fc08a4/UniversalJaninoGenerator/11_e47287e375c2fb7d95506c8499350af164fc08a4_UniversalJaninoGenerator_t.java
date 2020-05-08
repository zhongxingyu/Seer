 package ratson.genimageexplorer.generators.janino;
 
 import java.lang.reflect.InvocationTargetException;
 
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
 import org.codehaus.janino.ScriptEvaluator;
 
 import ratson.genimageexplorer.generators.Function;
 import ratson.genimageexplorer.generators.FunctionFactory;
 import ratson.genimageexplorer.generators.Renderer;
 import ratson.genimageexplorer.gui.dialogs.EditScriptDialog;
 
 public class UniversalJaninoGenerator implements FunctionFactory {
 
 	private ScriptReference script = new ScriptReference();
 	
 	private CodePreprocessor preprocessor = new CodePreprocessor();
 	
 	ScriptEvaluator evaluator = null;
 
 	private boolean scriptCompiled = false;
 	
 	class Func extends Function{
 		public float[] resultContainer = new float[1];
 
 		@Override
 		public float evaluate(double x, double y) {
 			if (evaluator == null)
 				return Renderer.BLACK_VALUE;
 			
 			try {
 				evaluator.evaluate(new Object[]{
 						x,y,resultContainer,new Integer(maxIters), new Double(p1), new Double(p2), new Double(p3)	
 				});
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 				return Renderer.BLACK_VALUE;
 			}
 			return resultContainer[0];
 		}
 		
 	}
 	/**Converts formula from script language to java.
 	 * 
 	 * @param formula
 	 * @return
 	 */
 	private String prepareFormula(String formula){
 
 		String prefix =
 			"double f=0;";
 		String postfix = 
 			"__result_container[0] = (float)f;";
 		
 		StringBuffer buff = new StringBuffer(prefix);
 		buff.append(preprocessor.preprocess(formula));
 		buff.append(postfix);
 		
 		
 		System.out.println("processed formula:");
 		System.out.println(buff.toString());
 		
 		return buff.toString();
 			
 	}
 	
 	public void doEditScript(){
 		EditScriptDialog esd=new EditScriptDialog(null, true);
 		esd.setScript(script);
 		esd.setVisible(true);
 		
 		scriptCompiled = false;
 		
 	}
 	private synchronized boolean compileScript(){
 		if (script == null){//resetting script;
 			evaluator = null;
 			scriptCompiled  = true;
 			return false;
 		}
 		try {
 			evaluator = null;
 			evaluator = new ScriptEvaluator(
 					prepareFormula(script.script),
 					void.class,
 					new String[]{"x","y","__result_container","maxIters","p1","p2","p3"},
 					new Class[]{double.class, double.class, float[].class, int.class, 
 						double.class, double.class, double.class}
 					);
 			
 		} catch (CompileException e) {
 			System.err.println(e.getMessage());
 			return false;
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return false;
		} catch (ScanException e) {
			System.err.println(e.getMessage());
			return false;
 		}
 		
 		scriptCompiled = true;//mark script as compiled
 		
 		return true;
 				
 	}
 	
 	private double p1=0,p2=0,p3=0;
 	
 	private int maxIters = 100;
 	
 	public int getMaxIter() {
 		return maxIters;
 	}
 	public void setMaxIter(int maxIters) {
 		this.maxIters = maxIters;
 	}
 	
 
 	public double getP1() {
 		return p1;
 	}
 	public double getP2() {
 		return p2;
 	}
 	public double getP3() {
 		return p3;
 	}
 	public void setP1(double p1) {
 		this.p1 = p1;
 	}
 	public void setP2(double p2) {
 		this.p2 = p2;
 	}
 	public void setP3(double p3) {
 		this.p3 = p3;
 	}
 	
 	public Function get() {
 		if (! scriptCompiled){
 			if (!compileScript()){
 				System.err.println("Script compilation failed.");
 			}
 		}
 		return new Func();
 	}
 
 }
