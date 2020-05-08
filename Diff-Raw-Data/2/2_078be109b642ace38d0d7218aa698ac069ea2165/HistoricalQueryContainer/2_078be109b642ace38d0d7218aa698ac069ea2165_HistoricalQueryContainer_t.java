 /**
  * 
  */
 package eu.play_project.dcep.distributedetalis.join;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Ningyuan Pan
  *
  */
 public class HistoricalQueryContainer {
 	final static String SELECT = "SELECT";
 	final static String WHERE = "WHERE";
 	final static String VALUES = "VALUES";
 	final static String GRAPH = "GRAPH";
 	final static String STREAM = " :stream ";
 	//private static PutGetProxyRegister proxyRegis = PutGetProxyRegister.getInstance();
 	
 	private final List<String> vvariables = new ArrayList<String>();
 	private final Map<String, List<String>> map;
 	private String query;
 	
 	private final Logger logger = LoggerFactory.getLogger(HistoricalQueryContainer.class);
 	
 	public HistoricalQueryContainer(String query, Map<String, List<String>> variableBindings){
 		if(query == null)
 			throw new IllegalArgumentException("Original query should not be null");
 		map = variableBindings;
 		if(map != null)
 			this.query = addVALUES(query);
 		else
 			this.query = query;
 	}
 	
 	public String getQuery(){
 		return query;
 	}
 	
 	/*
 	 * Add VALUES block into querys
 	 */
 	private String addVALUES(String oquery){
 		int count = 0, index = 0;
 		StringBuilder sparqlb = new StringBuilder(oquery);
 		index = oquery.indexOf(VALUES);
 		if(index != -1){
 			//TODO
 			throw new IllegalArgumentException("Original query already has VALUES block");
 		}
 		else {
 			index = oquery.indexOf(WHERE);
 			logger.debug("where index: "+index);
 		
 			// add vlues block in where block
 			if(index != -1){
 				while(index < oquery.length()){
 					if(oquery.charAt(index) == '{'){
 						count++;
 					}
 					else if(oquery.charAt(index) == '}'){
 						//count--;
 						if(count == 0){
 							break;
 						}
 					}
 					index++;
 				}
 				String vb = makeVALUES();
 				sparqlb.insert(index, vb);
 			}
 		}
 		return sparqlb.toString();
 	}
 	
 	/*
 	 * Make VALUES block using variables and its values
 	 */
 	private String makeVALUES(){
 		
 		StringBuilder ret = new StringBuilder();
 		if(makeVariableList()){
 			ret.append("\n VALUES (");
 			for(int i = 0; i < vvariables.size(); i++){
 				ret.append(vvariables.get(i));
 			}
 			ret.append(" ) {\n");
 			
 			ret = makeBody(ret, null, 0);
 			
 			ret.append("}");
 		}
 		return ret.toString();
 	}
 	
 	private boolean makeVariableList(){
 		boolean ret = false;
 		for(String variable : map.keySet()){
 			logger.debug("Add variable to list: " + variable);
 			vvariables.add(variable);
 			ret = true;
 		}
 		return ret;
 	}
 	
 	/*
 	 * Make VALUES body of all combinations of values
 	 */
 	private StringBuilder makeBody(StringBuilder ret, StringBuilder p, int depth){
 		StringBuilder path = p;
 		String pathMinusOne;
 		
 		if(depth == vvariables.size()){
 			path.append(") ");
 			ret.append(path);
 		}
 		else if(depth == 0){
 			path = new StringBuilder();
 			List<String> values = map.get(vvariables.get(depth));
 			if(values == null || values.isEmpty()){
 				path.append("( UNDEF ");
 				ret = makeBody(ret, path, depth+1);
 			}
 			else{
 				for(int i = 0; i < values.size(); i++){
 					path.delete(0, path.length());
 					path.append("( "+values.get(i)+" ");
 					ret = makeBody(ret, path, depth+1);
 				}
 			}
 		}
 		else{
 			pathMinusOne = path.toString();
 			logger.debug(vvariables.get(depth));
			logger.debug("{}", map.get(vvariables.get(depth)));
 			List<String> values = map.get(vvariables.get(depth));
 			if(values == null || values.isEmpty()){
 				path.append("UNDEF ");
 				ret = makeBody(ret, path, depth+1);
 			}
 			else{
 				for(int i = 0; i < values.size(); i++){
 					path.delete(0, path.length());
 					path.append(pathMinusOne);
 					path.append(values.get(i)+" ");
 					ret = makeBody(ret, path, depth+1);
 				}
 			}
 		}
 		return ret;
 	}
 	
 	/*private String getAimStream(){
 		String ret = null;
 		StringBuilder sb = new StringBuilder();
 		int index = 0, size = STREAM.length();
 		char c;
 		
 		index = oquery.indexOf(STREAM, index);
 		while(index != -1){
 			index += size;
 			c = oquery.charAt(index++);
 			while(c != ' '){
 				if(c != '<' && c != '>'){
 					sb.append(c);
 				}
 				c = oquery.charAt(index++);
 			}
 			ret = sb.toString();
 			logger.info("Aim stream: "+ret);
 			sb.delete(0, sb.length());
 			index = oquery.indexOf(STREAM, index);
 		}
 		return ret;
 	}*/
 
 }
