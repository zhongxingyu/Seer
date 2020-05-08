 package org.eclipse.iee.editor.core.utils.symbolic;
 
 import java.io.StringWriter;
 
 import org.apache.log4j.Logger;
 import org.matheclipse.core.eval.EvalEngine;
 import org.matheclipse.core.eval.EvalUtilities;
 import org.matheclipse.core.eval.TeXUtilities;
 import org.matheclipse.core.expression.F;
 import org.matheclipse.core.form.output.OutputFormFactory;
 import org.matheclipse.core.form.output.StringBufferWriter;
 import org.matheclipse.core.interfaces.IExpr;
 
 public class SymbolicEngine {
 
 	private EvalUtilities fUtil;
 
 	private static final Logger logger = Logger.getLogger(SymbolicEngine.class);
 
 	public SymbolicEngine() {
 		F.initSymbols(null);
 		fUtil = new EvalUtilities();
 	}
 	
 	public static boolean validate(String text) {
 
 		try {
 
 			if (getTeX(text) == "") {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 
 	public String getOutput(String expression) {
 		String output = "";
 
 		IExpr result = null;
 		StringBufferWriter buf = new StringBufferWriter();
 
		logger.debug("expression: " + expression);
		
 		try {
 			result = fUtil.evaluate(expression);
 
 			OutputFormFactory.get().convert(buf, result);
 
 			output = buf.toString();
 
 			logger.debug("result: " + output);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}
 
		return output.replaceAll("->", "=");
 	}
 
 	public static String getTeX(String expression) {
 		/* Convert to TeX */
 		EvalEngine engine = new EvalEngine();
 		TeXUtilities texUtil = new TeXUtilities(engine);
 
 		StringWriter stw = new StringWriter();
 		texUtil.toTeX(expression, stw);
 		String output = stw.toString();
 
 		return output;
 	}
 
 }
