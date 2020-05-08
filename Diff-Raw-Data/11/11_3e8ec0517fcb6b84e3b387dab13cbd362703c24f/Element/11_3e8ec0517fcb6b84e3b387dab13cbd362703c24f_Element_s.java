 package temmental2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 abstract class Element {
 
 	protected Cursor cursor;
 	
 	Element(Cursor cursor) {
 		this.cursor = cursor.clone();
 	}
 	
 	abstract String getIdentifier();
 	
 	abstract Object writeObject(Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException;
 	
 	Object getInModel(Map<String, Object> map) throws TemplateException {
 		String varname = getIdentifier();
 		varname = varname.substring(1);
 		boolean optional = ! isRequired(varname);
 		if (optional) {
 			varname = varname.substring(0, varname.length()-1);
 			if (map.containsKey(varname)) {
 				return map.get(varname);
 			} else {
 				return null;
 			}
 		} else {
 			if (! map.containsKey(varname) || map.get(varname) == null) {
 				throw new TemplateException("Key '%s' is not present or has null value in the model map at position '%s'.", varname, cursor.getPosition());
 			} else {
 				return map.get(varname);
 			}
 		}
 	}
 	
 	static boolean isRequired(String varname) {
 		return varname != null && (varname.startsWith("'") || ! varname.endsWith("?"));
 	}
 	
 	List<Object> create_parameters_after_process(List<Object> parameters, Map<String, Object> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException, IOException {
 		List<Object> args = new ArrayList<Object>();
         for (int i = 0; i < parameters.size(); i++) {
         	Object parameter = parameters.get(i);
         	Object afterProcess;
         	/*if (parameter == null) {
         		throw new TemplateException("Unable to apply function: parameter #%d is null", i);     //FIXME
         	} */
         	if (parameter instanceof Element) {
         		afterProcess = ((Element) parameter).writeObject(functions, model, messages);
         	} else {
         		afterProcess = parameter;
         	}
         	if (afterProcess == null) {
                 Element pElem = (Element) parameter;
         		if (isRequired(pElem.getIdentifier())) {
         			// FIXME pas top le test
        			throw new TemplateException("Unable to apply function: null argument '%s[%s]' at position '%s'.",
                        pElem.getIdentifier(), (parameters.size() > 1 ? "\u2026" : ""), cursor.getPosition());
                 } else {
         			return null;
         		}
         	}
         	args.add(afterProcess);
         }
 		return args;
 	}
 
     protected final String pref(int d) {
         String p = "\t";
         for (int i=0; i<d; i++) {
             p += "\t";
         }
         return p;
     }
 
     public final String toString() {
         return repr(0, false);
     }
 
     public abstract String repr(int d, boolean displayPosition);
 
 }
