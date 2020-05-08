 package TablaSimbolos;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import Tipos.Tipo;
 import Tipos.TipoConstante;
 import Tipos.TipoError;
 import Tipos.TipoSubprograma;
 
 public class TablaSimbolos {
 	HashMap<String, TSElem> ts;
 	int proximaDirTs;
 	HashMap<String, TSElem> tsNivel2;
 	int proximaDirTsNivel2;
 	
 	Vector<String> palabrasReservadas;
 	
 	public TablaSimbolos(){
 		ts = new HashMap<String, TSElem>();
 		palabrasReservadas = new Vector<String>();
 		anyadePalabrasReservadas();
 		proximaDirTs = 2;
 		
 		
 		tsNivel2 = null;
 	}
 	
 	public void activaNivel2(){
 		tsNivel2 = new HashMap<String, TSElem>();
 		proximaDirTsNivel2 = 0;
 	}
 	
 	public void desactivaNivel2(){
 		tsNivel2 = null;
 	}
 	
 	public boolean nivel2Activado(){
 		return tsNivel2 != null;
 	}
 	
 	private void anyadePalabrasReservadas() {
 		palabrasReservadas.add("program");
 		palabrasReservadas.add("consts");
 		palabrasReservadas.add("const");
 		palabrasReservadas.add("tipos");
 		palabrasReservadas.add("tipo");
 		palabrasReservadas.add("vars");
 		palabrasReservadas.add("var");
 		palabrasReservadas.add("natural");
 		palabrasReservadas.add("integer");
 		palabrasReservadas.add("float");
 		palabrasReservadas.add("character");
 		palabrasReservadas.add("boolean");
 		palabrasReservadas.add("subprograms");
 		palabrasReservadas.add("subprogram");
 		palabrasReservadas.add("instructions");
 		palabrasReservadas.add("swap1");
 		palabrasReservadas.add("swap2");
 		palabrasReservadas.add("in");
 		palabrasReservadas.add("out");
 		palabrasReservadas.add("and");
 		palabrasReservadas.add("or");
 		palabrasReservadas.add("if");
 		palabrasReservadas.add("then");
 		palabrasReservadas.add("else");
 		palabrasReservadas.add("endif");
 		palabrasReservadas.add("while");
 		palabrasReservadas.add("do");
 		palabrasReservadas.add("endwhile");
 		palabrasReservadas.add("true");
 		palabrasReservadas.add("false");
 		palabrasReservadas.add("_");
 		palabrasReservadas.add("{");
 		palabrasReservadas.add("}");
 		palabrasReservadas.add("[");
 		palabrasReservadas.add("]");
 		palabrasReservadas.add(",");
 		palabrasReservadas.add("");
 		palabrasReservadas.add("@");
 		palabrasReservadas.add(":");
 		palabrasReservadas.add(";");
 		palabrasReservadas.add("=");
 		palabrasReservadas.add("(float)");
 		palabrasReservadas.add("(int)");
 		palabrasReservadas.add("(char)");
 		palabrasReservadas.add("(nat)");
 		palabrasReservadas.add("not");
 		palabrasReservadas.add("(");
 		palabrasReservadas.add(")");
 		palabrasReservadas.add("<");
 		palabrasReservadas.add(">");
 		palabrasReservadas.add("<=");
 		palabrasReservadas.add(">=");
 		palabrasReservadas.add("==");
 		palabrasReservadas.add("!=");
 		palabrasReservadas.add("+");
 		palabrasReservadas.add("-");
 		palabrasReservadas.add("*");
 		palabrasReservadas.add("/");
 		palabrasReservadas.add("%");
 		palabrasReservadas.add(">>");
 		palabrasReservadas.add("<<");
 	}
 	
 	
 	public void anyadeId(String id, String clase, Tipo tipo){
 		if (!nivel2Activado()){
 			if (clase.equals("tipo") || clase.equals("proc")){
 				TSElem elem = new TSElem(clase, -1, tipo);
 				ts.put(id, elem);
 			} else {
 				TSElem elem = new TSElem(clase, proximaDirTs, tipo);
 				ts.put(id, elem);
 				proximaDirTs = proximaDirTs + tipo.getTam();
 			}
 		} else {
 			if (clase.equals("tipo") || clase.equals("proc")){
 				TSElem elem = new TSElem(clase, -1, tipo);
 				tsNivel2.put(id, elem);
 			} else {
 				TSElem elem = new TSElem(clase, proximaDirTsNivel2, tipo);
 				tsNivel2.put(id, elem);
 				proximaDirTsNivel2 = proximaDirTsNivel2 + tipo.getTam();
 			}
 		}
 	}
 	
 	public void anyadeIdPrimerNivel(String id, String clase, Tipo tipo){
 		if (clase.equals("tipo") || clase.equals("proc")){
 			TSElem elem = new TSElem(clase, -1, tipo);
 			ts.put(id, elem);
 		} else {
 			TSElem elem = new TSElem(clase, proximaDirTs, tipo);
 			ts.put(id, elem);
 			proximaDirTs = proximaDirTs + tipo.getTam();
 		}
 	}
 	
 	public boolean existeId(String id){
 		if (nivel2Activado()) return tsNivel2.containsKey(id) || ts.containsKey(id);
 		return ts.containsKey(id);
 	}
 	
 	public boolean existeIdPrimerNivel(String id){
 		return ts.containsKey(id);
 	}
 	
 	public int getDir(String id) { //FIXME
 		if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getDir();
 		if (ts.containsKey(id)) return ts.get(id).getDir();
 		else return -1;
 	}
 	
 	public Tipo getTipo(String id) {
 		if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getTipo();
 		if (ts.containsKey(id)) return ts.get(id).getTipo();
 		else return new TipoError("terror");
 	}
 	
 	public String getClase(String id){
 		if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getClase();
 		if (ts.containsKey(id)) return ts.get(id).getClase();
 		else return null;
 	}
 	
 	public boolean esModoPvar(String idProc, String idVar){
 		if (!existeId(idProc)) return false;
 		else if (!esSubprograma(idProc)) return false;
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc)) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).esModoPvar(idVar);
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).esModoPvar(idVar);
 			else return false;
 		}
 	}
 	
 	public boolean esConstante(String id){
 		if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getClase().equals("const");
 		if (ts.containsKey(id)) return ts.get(id).getClase().equals("const");
 		else return false;
 	}
 	
 	public boolean esTipo(String id){
 		if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getClase().equals("tipo");
 		if (ts.containsKey(id)) return ts.get(id).getClase().equals("tipo");
 		else return false;
 	}
 	
 	public boolean esSubprograma(String id){
 		if (nivel2Activado() && tsNivel2.containsKey(id) && tsNivel2.get(id).getClase().equals("proc")) return true;
 		else if (ts.containsKey(id)) return ts.get(id).getClase().equals("proc");
 		else return false;
 	}
 	
 	public String dameValorCte(String id){
 		if (!existeId(id)) return null;
 		else if (!esConstante(id)) return null;
 		else if (nivel2Activado() && tsNivel2.containsKey(id)) return ((TipoConstante)tsNivel2.get(id).getTipo()).getValor();
 		else if (ts.containsKey(id)) return ((TipoConstante)ts.get(id).getTipo()).getValor();
 		else return null;
 	}
 	
 	public int dameDirComienzoProc(String id){
 		if (!existeId(id)) return -1;
 		else if (!esSubprograma(id)) return -1;
 		else if (nivel2Activado() && tsNivel2.containsKey(id) && tsNivel2.get(id).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(id).getTipo()).getDirComienzo();
 		else if (ts.containsKey(id)) return ((TipoSubprograma)ts.get(id).getTipo()).getDirComienzo();
 		else return -1;
 	}
 	
 	public int dameDespProc(String idProc, String idVar){
 		if (!existeId(idProc)) return -1;
 		else if (!esSubprograma(idProc)) return -1;
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc) && tsNivel2.get(idProc).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).getDespl(idVar);
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).getDespl(idVar);
 			else return -1;
 		}
 	}
 	
 	public int dameTamParametro(String idProc, String idVar){
 		if (!existeId(idProc)) return -1;
 		else if (!esSubprograma(idProc)) return -1;
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc) && tsNivel2.get(idProc).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).getTam(idVar);
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).getTam(idVar);
 			else return -1;
 		}
 	}
 	
 	public boolean existeParam(String idProc, String idVar){
 		if (!existeId(idProc)) return false;
 		else if (!esSubprograma(idProc)) return false;
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc) && tsNivel2.get(idProc).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).existeParametro(idVar);
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).existeParametro(idVar);
 			else return false;
 		}
 	}
 	
 	public Tipo dameTipoParam(String idProc, String idVar){
 		if (!existeId(idProc)) return new TipoError("terror");
 		else if (!esSubprograma(idProc)) return new TipoError("terror");
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc) && tsNivel2.get(idProc).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).getTipo(idVar);
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).getTipo(idVar);
 			else return new TipoError("terror");
 		}
 	}
 	
 	public int dameNumParametros(String idProc){
 		if (!existeId(idProc)) return -1;
 		else if (!esSubprograma(idProc)) return -1;
 		else {
 			if (nivel2Activado() && tsNivel2.containsKey(idProc) && tsNivel2.get(idProc).getClase().equals("proc")) return ((TipoSubprograma)tsNivel2.get(idProc).getTipo()).getNumParams();
 			if (ts.containsKey(idProc)) return ((TipoSubprograma)ts.get(idProc).getTipo()).getNumParams();
 			else return -1;
 		}
 	}
 	
 	
 
 	public boolean esParametroRepetido(String id) {
 		if (!nivel2Activado()) return false;
 		return tsNivel2.containsKey(id);
 	}
 	
 	public int dameTamDatosGlobales(){
 		return proximaDirTs-2;
 	}
 
 	public int getTam(String id){
 		if (!existeId(id)) return -1;
 		else if (nivel2Activado() && tsNivel2.containsKey(id)) return tsNivel2.get(id).getTipo().getTam();
 		else if (ts.containsKey(id)) return ts.get(id).getTipo().getTam();
 		else return -1;
 	}
 	
 	public boolean esPalabraReservada(String pal) {
 		return palabrasReservadas.contains(pal.toLowerCase());
 	}
 	
 	public String calculaCodigoAccesoVar(String id){
 		String cod = "";
 		if (!existeId(id)) return "errCodigoIden";
 		else if (nivel2Activado() && tsNivel2.containsKey(id)){
 			cod += "apila_dir(1)\n";
 			cod += "apila(" + tsNivel2.get(id).getDir() + ")\n";
 			cod += "suma\n";
 			if (tsNivel2.get(id).getClase().equals("pvar")) cod += "apila_ind\n";
 			return cod;
 		}
 		else if (ts.containsKey(id)){
 			cod += "apila(" + ts.get(id).getDir() + ")\n";
 			return cod;
 		}
 		else return "errCodigoIden";
 		
 	}
 
 	public String calculaCodigoPilaOP(String op) {
 		String res = "";
 		if(op.equals("<"))
 			res = "menor";
 		else if (op.equals(">"))
 			res = "mayor";
 		else if (op.equals("<="))
 			res = "menorIgual";
 		else if (op.equals(">="))
 			res = "mayorIgual";
 		else if (op.equals("=="))
 			res = "igual";
 		else if (op.equals("!="))
 			res = "distinto";
 		else if (op.equals("+"))
			res = "suma_swap";
 		else if (op.equals("-"))
			res = "resta_swap";
 		else if (op.equals("*"))
			res = "multiplica_swap";
 		else if (op.equals("/"))
			res = "divide_swap";
 		else if (op.equals("%"))
 			res = "modulo";
 		else if (op.equals("<<"))
 			res = "desplazamiento_Izq";
 		else if (op.equals(">>"))
 			res = "desplazamiento_Der";
 		else if (op.equals("(float)"))
 			res = "cast_float";
 		else if (op.equals("(int)"))
 			res = "cast_int";
 		else if (op.equals("(char)"))	
 			res = "cast_char";
 		else if (op.equals("(nat)"))
 			res = "cast_nat";
 		else if (op.equals("not"))
 			res = "not";
 																														
 		return res;
 		
 	}
 
 
 	
 }
