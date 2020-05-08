 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.apache.commons.lang.CharUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ho.yaml.Yaml;
 
 public class Automata {
 
 	String q;
 	String finals;
 	String finalsAndReset;
 	Map<String, Map<String, String>> delta;
 	Map<String, Object> automata;
 
 	ArrayList<Character> ascii = new ArrayList<Character>();
 	ArrayList<Character> AZ = new ArrayList<Character>();
 	ArrayList<Character> Az = new ArrayList<Character>();
 	ArrayList<Character> numero = new ArrayList<Character>();
 	@SuppressWarnings("unchecked")
 	public Automata() throws Exception{
 
 		// Levantamos desde el archivo de configuracion del aut�mata
 		File automataConfig = new File("automataJAVA.yaml");
 		automata = Yaml.loadType(automataConfig, HashMap.class);
 
 		// Setea el estado inicial
 		q = (String) automata.get("initial");
 
 		// Setea los estados finales
 		finals = (String) automata.get("finals");
 		//estados finales que vuelven un caracter atras.
 		finalsAndReset = (String) automata.get("finalsAndReset");
 		
 		// Setea la funcion delta
 		delta = new HashMap<String, Map<String, String>>();
 
 		// TODO: Nombre / comentario
 		get();
 
 		HashMap<String, Map<String, String>> deltaTemp = (HashMap<String, Map<String, String>>) automata.get("delta");
 		for (Entry<String, Map<String, String>> entry : deltaTemp.entrySet()){
 		    
 			Map<String, String> temp = entry.getValue();
 			Map<String, String> temp2 = new HashMap<String, String>();
 			for (Entry<String, String> entry2 : temp.entrySet()){
 			//TODO PASAR PARAMETRO MAGICO
 				if(false){	
 				if ("ASCII".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						temp2.put(CharUtils.toString(character), entry2.getValue());
 					}
 				}else if ("ASCII-".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						if (!StringUtils.contains("<>",character)){
 							temp2.put(CharUtils.toString(character), entry2.getValue());
 						}
 					}
 				}else if ("ASCII--".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						if (!StringUtils.contains("-",character)){
 							temp2.put(CharUtils.toString(character), entry2.getValue());
 						}
 					}
 				}else if ("ASCII---".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						if (!StringUtils.contains("->",character)){
 							temp2.put(CharUtils.toString(character), entry2.getValue());
 						}
 					}
 				}else if ("AZ".equals(entry2.getKey())){
 					for (Iterator iterator = AZ.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						temp2.put(CharUtils.toString(character), entry2.getValue());
 					}
 				}else if ("enter".equals(entry2.getKey())){
 					temp2.put("\n", entry2.getValue());
 				}else{
 					temp2.put(entry2.getKey(), entry2.getValue());
 				}
 			}else{
 				if ("letra".equals(entry2.getKey())){
 					for (Iterator iterator = Az.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						temp2.put(CharUtils.toString(character), entry2.getValue());
 					}
 				}else if ("numero".equals(entry2.getKey())){
 					for (Iterator iterator = numero.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						temp2.put(CharUtils.toString(character), entry2.getValue());
 					}
 				}else if ("caracter".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						temp2.put(CharUtils.toString(character), entry2.getValue());
 					}
 				}else if ("enterytab".equals(entry2.getKey())){
 					temp2.put("\n", entry2.getValue());
 					temp2.put("\t", entry2.getValue());
				}else if ("caracter-".equals(entry2.getKey())){
 					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
 						Character character = (Character) iterator.next();
 						if (!StringUtils.contains("\"",character)){
 							temp2.put(CharUtils.toString(character), entry2.getValue());
 						}
 					}
				}else if("caracter--".equals(entry2.getKey())){
					for (Iterator iterator = ascii.iterator(); iterator.hasNext();) {
						Character character = (Character) iterator.next();
						if (!StringUtils.contains("'",character)){
							temp2.put(CharUtils.toString(character), entry2.getValue());
						}
					}
 				}else{
 					temp2.put(entry2.getKey(), entry2.getValue());
 				}
 			}
 			}
 
 			delta.put(entry.getKey(), temp2);
 		}
 		System.out.println(delta);
 	}
 
 	// TODO: Nombre
 	public AutomataResponse process(String input, int since) throws Exception {
 
 		AutomataResponse response = new AutomataResponse();
 
 		// Setea el estado inicial
 		q = (String) automata.get("initial");
 
 		int i = since;
 		for (; i < input.length()-1; i++) {
 
 			// Calcula el delta
 			String c = CharUtils.toString(input.charAt(i));
 			q = delta(q,c);
 			if ("q0".equals(q)){
 				since = i;
 			}
 			/* 
 			 * Cuando termin� en un estado
 			 * que no es final -> Error
 			 */
 			if(q == null){
 				response.set(since, i+1, input.substring(since, i+1), true, "Error desde # hasta $ - "+q+" No es estado final");
 				return response;
 			}
 
 			if(isFinalAndReset(q)){
 				i= i-1;
 				break;
 			}
 			
 			/*
 			 * Lleg� a estado final. 
 			 * Resetea el estado a inicial
 			 */
 			if (isFinalState(q)){
 				break;
 			}
 		}
 
 		/* 
 		 * Cuando termin� en un estado
 		 * que no es final -> Excepci�n
 		 */
		if(("qe".equals(q)) || (!isFinalState(q) && !isFinalAndReset(q))){
 			response.set(since, i+1, input.substring(since, i+1), true, "Error desde # hasta $ - "+q+" No es estado final");
 			return response;
 		}
 
 		response.set(since, i+1, input.substring(since, i+1), false, null);
 		return response;
 	}
 
 	private boolean isFinalAndReset(String q2) {
 		return StringUtils.contains(finalsAndReset, q);
 	}
 
 	private boolean isFinalState(String q) {
 		return StringUtils.contains(finals, q);
 	}
 
 	private String delta(String q, String c) {
 		System.out.println(q+c); //TODO: Sacar
 		return delta.get(q).get(c);
 	}
 
 	// TODO: Nombre
 	private void get() {
 		for (int i = 33; i < 127 ; i++){
 			Character a = new Character((char) i);
 			ascii.add(a);	
 		}
 		for (int i = 65; i < 91 ; i++){
 			Character a = new Character((char) i);
 			AZ.add(a);	
 		}
 		for (int i = 65; i < 91 ; i++){
 			Character a = new Character((char) i);
 			Az.add(a);	
 		}
 		for (int i = 97; i < 123 ; i++){
 			Character a = new Character((char) i);
 			Az.add(a);	
 		}
 		for (int i = 48; i < 58 ; i++){
 			Character a = new Character((char) i);
 			numero.add(a);	
 		}
 	}
 }
