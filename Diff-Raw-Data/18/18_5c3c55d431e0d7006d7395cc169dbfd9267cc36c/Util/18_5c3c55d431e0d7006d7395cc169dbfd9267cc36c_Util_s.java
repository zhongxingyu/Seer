 package de.hs_rm.cs.vs.dsm.generator;
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.common.util.EList;
 
 import de.hs_rm.cs.vs.dsm.flow.StreamDefinition;
 
 /**
  *  
  * 
  */
 public class Util {
 	/** Initialize the only instance of the class */ 
 	private static final Util mInstance = new Util();
 	/***/
 	private Network mNetwork = new Network();
 	
 	/**
 	 * The constructor is private in order to guarantee that the class is
 	 * only initialized once. 
 	 */
 	private Util(){}
 	
 	/**
 	 * The method returns an instance of the class
 	 *
 	 * @return The only instance of the class unit
 	 */
 	public static Util getInstance(){
 		return mInstance;
 	}
 
 	public String createOperator(String pOperator, String pName){
 		// TODO: Is it necessary to add operator at this point to the network?
 		
 		//
 		return pName + " = fm:create_operator_of_type(\"" + pName + "\", \"" + pOperator + "\");\n";
 	}
 	
 	public String connectOperator(final String pIn, final String pIndirection, final String pOut, final String pOutDirection){
 		// TODO: Add the operators to the network
 		
 		// Return the connection operator string
 		return "fm:connect_operators(" + pOut + ", \"" + pOutDirection + "\"," + 
 				pIn + ", \"" + pIndirection + "\");\n";
 	}
 	
 	public String createBarrier(final String pStream, final String pWindowType, final String pValue){
 		// The result string
 		String result = "";
 		// If the window type is of type time
 		if(pWindowType.equals("time")){
 			result += Util.getInstance().createParameter(pStream, "step_size_num", pValue);
 			result += Util.getInstance().createParameter(pStream, "window_type", "time");
 		// If the window type is of type elements
 		}else if(pWindowType.equals("elements")){
 			// TODO
 		// If the window type is of type now
 		}else if(pWindowType.equals("now")){
 			// TODO
 		// If the window type is a marker, which probably requires a seperate method
 		}else{
 			//TODO
 		}
 		return result;
 	}
 	
 	/**
 	 * The method creates a parameter for an operator. Every parameter of an operator consists of
 	 * an key/value pair. For example
 	 * 
 	 *	avg:set_parameter("window_type", "num");
 	 * 
 	 * where for a stream 'avg' a parameter 'window_type' is set to 'num'
 	 * 
 	 * @return A string which contains a set_parameter operation
 	 */
 	public String createParameter(final String pStream, final String pKey, final String pValue){
 		return pStream + ":set_parameter(" + "\"" + pKey + "\"" + ", " + "\"" + pValue + "\"" + ");\n";
 	}
 	
 	public String connectOperator(final ArrayList<String> pIn, final String pIndirection, final ArrayList<String> pOut, final String pOutDirection){
 		return "TODO \n";
 	}
 	
 	public String connectOperator(final String pIn, final String pIndirection, final ArrayList<String> pOut, final String pOutDirection){
 		String result = "";
 		
 		if(pOut.size() > 1){
 			for(int i = 0; i < pOut.size(); i++){
 				// The operator has only one input stream and thus we only use the first element in the list
 				result += Util.getInstance().connectOperator(pIn, "in",  pOut.get(i), "out" + i);
 			}
 		}else{
 			// This should be not necessary, but just in case
 			if(pOut.size() == 1){
 				result += Util.getInstance().connectOperator(pIn, "in",  pOut.get(0), "out");
 			}
 		}
 		
 		return result;
 	}
 	
 	
 	public ArrayList<String> getStreamFrom(EList<StreamDefinition> pStreams){
 		ArrayList<String> result = new ArrayList<String>();
 		
 		/*
 		for(int i = 0; i < pStreams.size(); i++){
 			result.add(pStreams.get(i).getName());
 		}
 		*/
 		
 		return result;
 	}
 	
 	/**
 	 * The method returns a string which contains the operators
 	 * which should be started.
 	 * 
 	 * @return A list with operators which should be started
 	 */
 	public String getStartOperators(){
 		return Util.getInstance().createOperators("fm:start(");
 	}
 	
 	/**
 	 * The method returns a string which contains the operators
 	 * which should be stopped.
 	 * 
 	 * @return A list with operators which should be stopped
 	 */
 	public String getStopOperators(){
 		return Util.getInstance().createOperators("fm:stop(");
 	}
 	
 	/**
 	 * The method builds a string which starts or stops the operators 
 	 * in LUA. Actually the method is 'capable' of using every 
 	 * preamble, but there are only two types of operation which have 
 	 * one parameter with a operator. One operation is the 
 	 * 
 	 * 	fm:start();
 	 * 
 	 * method and the other one is the
 	 * 
 	 * 	fm:stop();
 	 * 
 	 * method.
 	 *
 	 * @param pSetting The preamble which will be added before the operator
 	 * 
 	 * @return A string which consists of starting or stopping operators in 
 	 *  the network
 	 */
 	private String createOperators(final String pSetting){
 		// Initialize result string
 		String result = "";
 		// Iterate over the stream list in the network
 		for(int i = 0; i < this.mNetwork.getStreams().size(); i++){
 			// Add the streams to the result string
 			result += pSetting + this.mNetwork.getStreams().get(i) + ");\n";
 		}
 		// Return the result
 		return result;
 	}
 }
