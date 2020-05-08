 package core;
 
 
 public class CheckTypes {
 	
 	private CheckTypes() {}
 	
 	
 	public static void checkIsBothInteger(Object o1, Object o2) {
 		checkIsInteger(o1);
 		checkIsInteger(o2);
 	}
 	
 	
 	public static void checkNotExp(Object o1) {
 		checkIsInteger(o1);
 	}
 	
 	public static void checkIsInteger(Object o1) {
 		Simbolo s1 = (Simbolo) o1;
 
 		if (s1.getType().equalsIgnoreCase("STRING")) {
 			LogHandler.showError(s1 + " deve ser do tipo Integer.");
 		} else if (s1.getType().equalsIgnoreCase("CONSTANT") && s1.getLexema().contains(".")) {
 			LogHandler.showError(s1 + " deve ser do tipo Integer.");
 		} else if (s1.getType().equalsIgnoreCase("IDENTIFIER")) {
 			Variavel v = TabelaSimbolos.getInstance().searchVar(s1);
 			Funcao f = TabelaSimbolos.getInstance().searchFun(s1);
 			
 			if (v != null ) {
 				if (!v.getType().equalsIgnoreCase("INT")) {
 					LogHandler.showError(s1.getLexema() + " deve ser do tipo Integer.");
 				}
 			} else if (f != null) {
 				if (!f.getType().equalsIgnoreCase("INT")) {
 					LogHandler.showError("Funcao " + s1.getLexema() + " no declarada.");
 				}
 			} else {
 				LogHandler.showError(s1 + " no foi declarado.");
 			}
 		}
 	}
 
 
 	public static void checkSwitchEx(Object ex){
 		Simbolo s = (Simbolo) ex;
 		if (s.getType().equalsIgnoreCase("CONSTANT") && s.getLexema().contains(".")){
 			LogHandler.showError("Expresso do SWITCH deve ser do tipo inteiro");
 		}
 		else if (s.getType().equalsIgnoreCase("STRING")){
 			LogHandler.showError("Expresso do SWITCH deve ser do tipo inteiro");
 		}
 		else if (s.getType().equalsIgnoreCase("IDENTIFIER")){
 			Variavel v = TabelaSimbolos.getInstance().searchVar(s);
 			if (v == null) {
 				LogHandler.showError("Variavel no foi criada: " + s.getLexema());
 			}
 			else if(!(v.getType().equalsIgnoreCase("INT"))){
 				LogHandler.showError("Expresso do SWITCH deve ser do tipo inteiro");
 			}
 		}
 	}
 	
 	public static void checkCaseEx(Object ex){
 		Simbolo s = (Simbolo) ex;
 		if (s.getType().equalsIgnoreCase("CONSTANT") && s.getLexema().contains(".")){
 			LogHandler.showError("CASE deve ser do tipo inteiro");
 		}
 		else if (s.getType().equalsIgnoreCase("STRING")){
 			LogHandler.showError("CASE deve ser do tipo inteiro");
 		}
 		else if (s.getType().equalsIgnoreCase("IDENTIFIER")){
 			Variavel v = TabelaSimbolos.getInstance().searchVar(s);
 			if (v == null) {
 				LogHandler.showError("CASE deve ser do tipo inteiro");
 			}
 			else if(!(v.getType().equalsIgnoreCase("INT"))){
 				LogHandler.showError("CASE deve ser do tipo inteiro");
 			}
 		}
 	}
 		
 	public static boolean checkAssignment(Object n1, Object n2) {
 		
 		if (!(n1 instanceof Simbolo) || !(n2 instanceof Simbolo)) {
 			return false;
 		}
 		
 		Simbolo s1 = (Simbolo) n1;
 		Simbolo s2 = (Simbolo) n2;
 		
 		// s1 deve ser um identificador
 		if (!(s1.getType().equalsIgnoreCase("IDENTIFIER"))) {
 			LogHandler.showError("First argument must be IDENTIFIER");
 			return false;
 		}
 		
 		Variavel v = TabelaSimbolos.getInstance().searchVar(s1);
 		
 		// Checa se v foi declarado
 		if (v == null) {
 			LogHandler.showError("Variavel no foi criada: " + s1.getLexema());
 			return false;
 		}
 		
 		// Se o lado direito for um Identificador, checa se ele existe
 		if (s2.getType().equalsIgnoreCase("IDENTIFIER")) {
 			Variavel v2 = TabelaSimbolos.getInstance().searchVar(s2);
 			
 			if (v2 == null) {
 				LogHandler.showError("Variavel no foi criada: " + s2.getLexema());
 				return false;
 			}
 			
 			if (v2.getValue() == null) {
 				LogHandler.showError("Variavel no foi inicializada: " + v2);
 				return false;
 			}
 			
 			// se existir, checa o tipo
 			if (!v.getType().equalsIgnoreCase(v2.getType())) {
 				LogHandler.showError("Variaveis de tipos diferentes: " + v + "," + v2);
 			}
 			
 			
 			if (v.getType().equalsIgnoreCase(("int"))) {
 				v.setValue(v2.getValue());
 				LogHandler.showInfo("Int with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("float")) {
 				v.setValue(v2.getValue());
 				LogHandler.showInfo("Float with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("double")) {
 				v.setValue(v2.getValue());
 				LogHandler.showInfo("Double with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("char")) {
 				
 				if (v2.getValue().toString().length() != 3) {
 					LogHandler.showError("Char invalid: " + v2.getValue().toString());
 				} else {
 					v.setValue(v2.getValue().toString().charAt(1));
 					
 					LogHandler.showInfo("Char with value: " + v.getLexema() + " = " + v.getValue().toString());
 				}
 			}
 			
 		} else {
 			if (v.getType().equalsIgnoreCase(("int"))) {
 				v.setValue(Integer.parseInt(s2.getLexema()));
 				LogHandler.showInfo("Int with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("float")) {
 				v.setValue(Float.parseFloat(s2.getLexema()));
 				LogHandler.showInfo("Float with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("double")) {
 				v.setValue(Double.parseDouble(s2.getLexema()));
 				LogHandler.showInfo("Double with value: " + v.getLexema() + " = " + v.getValue().toString());
 			} else if (v.getType().equalsIgnoreCase("char")) {
 				
 				if (s2.getLexema().length() != 3) {
 					LogHandler.showError("Char invalid: " + s2.getLexema());
 				} else {
 					v.setValue(s2.getLexema().charAt(1));
 					
 					LogHandler.showInfo("Char with value: " + v.getLexema() + " = " + v.getValue().toString());
 				}
 			}
 		}
 		
 		
 		try {
 			TabelaSimbolos.getInstance().getNewContexto().add(new Attr( new Variavel(v.getLexema(), v.getType(), v.getValue())));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 	
 	public static Object checkIfFuncaoExist(String namespace, String strParametros) {
 		
 		namespace = namespace.substring(0, namespace.indexOf("("));
 		
 		Funcao f = TabelaSimbolos.getInstance().searchFun(new Simbolo(namespace, null, null));
 		
 		// Nome de funo no existe;
 		if (f == null) {
 			LogHandler.showError("Funcao no existe: " + namespace);
 			return null;
 		}
 		
 		String[] par = null;
 		
 		if (strParametros.contains(",")) {
 			par = strParametros.split(",");
 		} else {
 			par = new String[0];
 		}
 		
 		Variavel varTemp = null;
 		
 		// Quantidade de parametros incorreto;
 		if (f.getParametros().size() != par.length) {
 			LogHandler.showError("Quantidade de parametros incorreto: " + f.getLexema());
 			return null;
 		}
 
 		for (int i = 0; i < par.length; i++) {
 			String temp =  par[i].substring(par[i].indexOf("(")).replace("(","").replace(")","");
 			
 			String id = par[i].substring(0, par[i].indexOf("(")).trim();
 			
 			if (temp.equalsIgnoreCase("CONSTANT") && (f.getParametros().get(i).getType().equalsIgnoreCase("CHAR"))) {
 				LogHandler.showError("Funo com parametro incorreto: " + id + "(" + temp + ") deveria ser " + f.getParametros().get(i).getType());
 			} else if (temp.equalsIgnoreCase("STRING") && !(f.getParametros().get(i).getType().equalsIgnoreCase("CHAR"))) {
 				LogHandler.showError("Funo com parametro incorreto: " + id + "(" + temp + ") deveria ser " + f.getParametros().get(i).getLexema() + ":" + f.getParametros().get(i).getType());
 			} else if (temp.equalsIgnoreCase("IDENTIFIER")) {
 				varTemp = TabelaSimbolos.getInstance().searchVar(id);
 				
 				// Varivel usada como parametro no existe;
 				if(varTemp == null) {
 					LogHandler.showError("Variavel no existe: " + id);
 					return null;
 				} 
 				
 				if (varTemp.getType().equalsIgnoreCase("CHAR") && !f.getParametros().get(i).getType().equalsIgnoreCase("CHAR")) {
 					LogHandler.showError("Funo com parametro incorreto: " + varTemp + " deveria ser " + f.getParametros().get(i).getType());
 				} else if (!varTemp.getType().equalsIgnoreCase("CHAR") && f.getParametros().get(i).getType().equalsIgnoreCase("CHAR")) {
 					LogHandler.showError("Funo com parametro incorreto: " + varTemp + " deveria ser " + f.getParametros().get(i).getType());
 				}
 			}
 		}
 		
 		
 		return (Object) f;
 	}
 	
 }
