 import java.io.*;
 import java.lang.*;
 import java.util.*;
 
 /* Wrapper to make it easier to call Qss */
 public class Parser {
 	private Qss qss = new Qss(new StringReader(""));
 	private Writer debugWriter = null;
 	private String debugEol = null;
 	private StringWriter errorWriter = null;
 
 	public Parser() {
 		setErrorWriter(new StringWriter(), "<BR>");
 	}
 
 	public boolean booleanVal(Evidence ev, String exp) {
 		return parse(ev, exp).booleanVal();
 	}
 
 	public String stringVal(Evidence ev, String exp) {
 		return parse(ev, exp).stringVal(false);
 	}
 
 	public String stringVal(Evidence ev, String exp, boolean showReserved) {
 		return parse(ev,exp).stringVal(showReserved);
 	}
 
 	public double doubleVal(Evidence ev, String exp) {
 		return parse(ev, exp).doubleVal();
 	}
 
 	public Datum parse(Evidence ev, String exp) {
 		debug(exp);
 
 		qss.ReInit(new StringReader(exp));
 		Datum ans = qss.parse(ev);
 
 		debug(null);
 
 		return ans;
 	}
 
 	public boolean hasErrors() {
 		if (errorWriter != null) {
 			return (errorWriter.getBuffer().length() > 0);
 		}
 		return false;
 	}
 
 	public String getErrors() {
 		if (errorWriter != null) {
 			String s = errorWriter.toString();
 			setErrorWriter(new StringWriter(),"<BR>");
 			return s;
 		}
 		return "";
 	}
 
 	public String parseJSP(Evidence ev, String msg) {
 		java.util.StringTokenizer st = new java.util.StringTokenizer(msg,"`",true);
 		StringBuffer sb = new StringBuffer();
 		String s;
 		boolean inside = false;
 
 		debug(msg);
 
 		while(st.hasMoreTokens()) {
 			s = st.nextToken();
 			if ("`".equals(s)) {
 				inside = (inside) ? false : true;
 				continue;
 			}
 			else {
 				if (inside) {
 					sb.append(stringVal(ev,s,true));	// so that see the *REFUSED*, etc as part of questions
 				}
 				else {
 					sb.append(s);
 				}
 			}
 		}
 		debug(null);
 
 		return sb.toString();
 	}
 
 	private void debug(String s) {
 		if (debugWriter != null) {
 			try {
 				debugWriter.write(((s != null) ? s : "") + debugEol);
 			}
 			catch (IOException e) { }
 		}
 	}
 
 	public boolean setErrorWriter(Writer err) { return setErrorWriter(err,null); }
 	public boolean setErrorWriter(Writer err, String eol) { 
 		if (err instanceof StringWriter) {
 			errorWriter = (StringWriter) err;
 		}
		return qss.setErrorWriter(err,eol);
 	}
 	
 	public boolean setDebugWriter(Writer debug) { return setDebugWriter(debug,"\n\r"); }
 	public boolean setDebugWriter(Writer debug, String eol) {
 		debugWriter = debug;
 		debugEol = eol;
 		return qss.setDebugWriter(debug,eol);
 	}
 }
