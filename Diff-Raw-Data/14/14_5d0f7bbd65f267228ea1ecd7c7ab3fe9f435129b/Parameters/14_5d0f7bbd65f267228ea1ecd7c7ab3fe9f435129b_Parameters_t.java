 package vara.app.startupargs.base;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vara.app.startupargs.ArgsUtil;
 import vara.app.startupargs.defaultImpl.DefaultHelpParameter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Grzegorz (vara) Warywoda
  */
 public class Parameters {
 
 	private static final Logger log = LoggerFactory.getLogger(Parameters.class);
 
 	private final static List<AbstractParameter> parameters = new ArrayList();
 
 	public static enum InsertBehavior{
 		DontReplaceExisting,
 		ReplaceExisting;
 	}
 
 	static {
 		parameters.add(new DefaultHelpParameter("--help","-h"));
 	}
 
 	/**
 	 * Add new parameter to container.
 	 * If current parameter will be detected in container then not be added to the list.
 	 * Default behavior is <code>InsertBehavior.DontReplaceExisting</code>.
 	 * If you want changed this behavior use second method 
 	 * {@link #putParameter(AbstractParameter, vara.app.startupargs.base.Parameters.InsertBehavior)}
 	 *
 	 * @param parameter
 	 * @return true if parameter has been added to the container otherwise false.
 	 *
 	 * @see vara.app.startupargs.base.Parameters.InsertBehavior
 	 */
 	public static boolean putParameter(AbstractParameter parameter){
 		return putParameter(parameter,InsertBehavior.DontReplaceExisting);
 	}
 
 	/**
 	 * Add new parameter to container.
 	 * If current parameter will be detected in container then situation will depend of second parameter.
 	 *
 	 * @param parameter
 	 * @return true if parameter has benn added to the container otherwise false.
 	 */
 
 	public static boolean putParameter(AbstractParameter parameter,InsertBehavior behavior){
 
 		if(parameter!=null){
 
 			synchronized (parameters){
 
 				if(!parameters.contains(parameter)){
 
 					return parameters.add(parameter);
 				}else{
					if(log.isWarnEnabled())log.warn("Detected multiply parameter in container for '{}' behavior: '{}'",parameter,behavior);
 
 					switch (behavior) {
 
 						case DontReplaceExisting:	break;
 						case ReplaceExisting:
 
 							//Remove object parameter with same symbols but probably inner behavior.
 							//Make sure that the objects are different by additional checking instance.
 							//this block invoked only when parameter is in array.
 							//and index should be  less then zero.
 							//Added synchronize statement for race condition protection
 							int index = parameters.indexOf(parameter);
 
 							assert index!=-1;
 
 							AbstractParameter oldParam = parameters.get(index);
 
 							if(oldParam != parameter){
 								if(log.isDebugEnabled())log.debug("You tried add the same parameter to container with different instance. Parameters will be turned");
 
 								parameters.set(index,parameter);
 								return true;
 								
 							}else{
 								if(log.isDebugEnabled())log.debug("You tried add the same parameter to container. Operation canceled !");
 							}
 					}
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 * @param vap
 	 */
 	public static void putParameter(List<AbstractParameter> vap){
 		if(vap != null){
 			for (AbstractParameter ap : vap) {
 				putParameter(ap);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static void removeAll(){
 		synchronized (parameters){
 			parameters.clear();
 		}
 	}
 
 	/**
 	 *
 	 * @param symbol
 	 * @return
 	 */
 	public static AbstractParameter getParameter(String symbol){
 
 		EntryParameter entry  = new EntryParameter(symbol);
 		synchronized (parameters){
 
 			int index = parameters.lastIndexOf(entry);
 			if(index != -1) {
				if(log.isDebugEnabled())log.debug("Found parameter for '{}' on index {}",symbol,index);
 				return parameters.get(index);
 			}
 		}
		if(log.isDebugEnabled())log.debug("Parameter wasn't found for '{}'",symbol);
 		return null;
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	public static List<AbstractParameter> getAllParameters(){
 
 		List<AbstractParameter> allParams = null;
 		synchronized (parameters){
 
 			 allParams = new ArrayList(parameters);
 		}
 		return allParams;
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	public static int numberOfParameters(){
 		synchronized (parameters){
 			return parameters.size();
 		}
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	public static boolean isEmpty(){
 		synchronized (parameters){
 			return parameters.isEmpty();
 		}
 	}
 
 	/**
 	 * Object is helpful for easier to verify the identity of "h? == (h1 or h2)".
 	 * Where h? is represented by this object and right side by AbstractParameter.
 	 *
 	 */
 	private static class EntryParameter{
 
 		private int hashCode = -1;
 
 		private final String symbolName;
 
 
 		public EntryParameter(String symbolName){
 
 			if(symbolName == null) throw new NullPointerException("Input parameter must be non-null !");
 
 			this.symbolName = ArgsUtil.recheck(symbolName);
 
 			hashCode = generateHashCode();
 		}
 
 		public String getSymbolName() {
 			return symbolName;
 		}
 
 		protected int generateHashCode(){
 			int hashCode = 3;
 			hashCode = 97 * hashCode + (this.symbolName != null ? this.symbolName.hashCode() : 0);
 			return hashCode;
 		}
 
 		@Override
 		public int hashCode() {
 			new Throwable().printStackTrace();
 			return hashCode;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 
			if(log.isTraceEnabled()) log.trace("Equals {} with {}",this,obj);
 			boolean retVal = false;
 
 			if( !(obj instanceof DefaultParameter)) {
 				retVal =  false;
 			}else if(this == obj){
 				retVal = true;
 			}else if( ((DefaultParameter)obj).getSymbol().hashCode() == symbolName.hashCode() ||
 						((DefaultParameter)obj).getShortSymbol().hashCode() == symbolName.hashCode() ){
 				retVal = true;
 			}
			if(log.isTraceEnabled())log.trace("Result is: {}",retVal);
 			return  retVal;
 		}
 
 		@Override
 		public String toString() {
 			return getClass().getSimpleName()+"'"+ getSymbolName()+"'";
 		}
 	}
 }
