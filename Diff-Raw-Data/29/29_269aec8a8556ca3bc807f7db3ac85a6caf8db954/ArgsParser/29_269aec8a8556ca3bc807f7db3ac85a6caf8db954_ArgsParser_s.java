 package vara.app.startupargs;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vara.app.startupargs.base.DefaultParameter;
 import vara.app.startupargs.base.Parameters;
 import vara.app.startupargs.exceptions.CatchOnException;
 import vara.app.startupargs.exceptions.OptionNotFoundException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
 * User: Grzegorz (vara) Warywoda
 * Date: 2010-05-12
 * Time: 15:16:11
 */
 
 /**
  * This class is a manager for all input command line arguments.
  * It is responsible for validation, delivered exception and parsing arguments.
  */
 public class ArgsParser {
 	private static final Logger log = LoggerFactory.getLogger(ArgsParser.class);
 
 	private static List<CatchOnException> exceptionCatchers = new ArrayList<CatchOnException>();
 
 	/**
 	 * This class tell to parser what to do when exception will be throw.
 	 * Regardless of your choice, all exception will be redirect to registered
 	 * listeners through <code>CatchOnException<code> object.
 	 * 
 	 * @see vara.app.startupargs.exceptions.CatchOnException
 	 *
 	 */
 	public enum ExceptionBehaviour{
 		/**
 		 * Do nothing
 		 */
 		IGNORE,
 		/**
 		 * When exception will be caught by parser perform as
 		 */
 		THROW,
 		/**
 		 * When exception will be caught by parser exit from program
 		 */
 		EXIT;
 
 		//TODO: For inner cases. "Throw" for only internal validation exceptions.  
 
 	}
 
 	static {
 		//check if the user didn't specify a variable
 		//NOTE:not checked/tested
 		String charForSeparator = System.getProperty("cmdline.values.separator","");
 
 		if(!charForSeparator.isEmpty()){
 			ArgsUtil.setArgumentValuesSeparator(charForSeparator);
 			log.info("User define new separator '{}' for combined input arguments.",ArgsUtil.getArgumentValuesSeparator());
 		}
 	}
 
 	/**
 	 * Variable is responsible for action behavior when any runtime exception will be throw
 	 * @see vara.app.startupargs.ArgsParser.ExceptionBehaviour 
 	 */
 	private static ExceptionBehaviour exceptionBehaviour = ExceptionBehaviour.EXIT;
 
 	/**
 	 * Get information of current set object  <code>ExceptionBehaviour<code>
 	 * Tell to parser what to do when exception will be generated.
 	 * Default behaviour is <code>ExceptionBehaviour.EXIT<code>
 	 *
 	 * @return type of <code>ExceptionBehaviour<code> object
 	 * @see #exceptionBehaviour
 	 * @see vara.app.startupargs.ArgsParser.ExceptionBehaviour
 	 *
 	 */
 	public static ExceptionBehaviour getExceptionBehaviour() {
 		return exceptionBehaviour;
 	}
 
 	/**
 	 * Tell to parser what to do when exception will be generated.
 	 * Default behaviour is <code>ExceptionBehaviour.EXIT<code>
 	 *
 	 * @param exceptionBehaviour
 	 * @see #exceptionBehaviour
 	 * @see vara.app.startupargs.ArgsParser.ExceptionBehaviour 
 	 */
 	public static void setExceptionBehaviour(ExceptionBehaviour exceptionBehaviour) {
 		ArgsParser.exceptionBehaviour = exceptionBehaviour;
 	}
 
 	/**
 	 * Set hook for all thrown exceptions by parser. In other words, allow
 	 * to monitoring exceptions while parsing arguments.
 	 *
 	 * @param catcher object to correspond with parser
 	 */
 	public static void setCatchOnException(CatchOnException catcher){
 		
 		if(!exceptionCatchers.contains(catcher)){
 			exceptionCatchers.add(catcher);
 		}else{
 			log.warn("Cant add CatchOnException object ! Object has been added before");
 		}
 	}
 
 	public static void removeCatchOnException(CatchOnException catcher){
 		//TODO : Add log inf
 		exceptionCatchers.remove(catcher);
 	}
 
 	/**
 	 * Function for handling eny runtime exception and it is responsible
 	 * to deliver to registered <code>CatchOnException</code>. Action behaviour
 	 * is mostly depend of variable <code> exceptionBehaviour </code>
 	 *
 	 *
 	 * @param exc exception has been thrown
 	 *
 	 * @see #exceptionBehaviour
 	 * @see vara.app.startupargs.exceptions.CatchOnException
 	 */
 	private static void deliverCaughtException(final Exception exc){
 		if(!exceptionCatchers.isEmpty()){
 			for (CatchOnException exceptionCatcher : exceptionCatchers) {
 				exceptionCatcher.caughtException(exc);
 			}
 		}
 
 		if(exceptionBehaviour == ExceptionBehaviour.THROW){
 
 			RuntimeException re;
 			if( !(exc instanceof RuntimeException)){
 				RuntimeException e = new RuntimeException(exc){
 					@Override
 					public String getLocalizedMessage() {
 						return exc.getLocalizedMessage();
 					}
 				};
 				re = e;
 			}else re = (RuntimeException)exc;
 
 			throw re;
 		}
 
 		if(exceptionBehaviour == ExceptionBehaviour.EXIT){
 			log.error("",exc);
 			//TODO: Check for special error code
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Helper class for encapsulating information about
 	 * layout of parameters in main container.
 	 */
 	private static class ParameterEntryHelper{
 		/**
 		 * Index in main list of command line arguments,
 		 * where 'symbol' parameter has been placed.
 		 */
 		private final int index;
 		private final String symbol;
 		private final int nArguments;
 
 		private boolean isCombined = false;
 		private String rawCombinedArguments = null;
 
 		private ParameterEntryHelper(int fromIndex, int numOfArguments,String symbol) {
 			this.index = fromIndex;
 
 			isCombined = ArgsUtil.isCombinedArgument(symbol);
 			if(isCombined){
 
 				if(numOfArguments != 0){
 					log.warn("Internal: numOfParams is > 0 when argument is CombinedArgument !!!");
 				}
 
 				ArgsUtil.CombinedArgument combArg = ArgsUtil.toCombinedArgObject(symbol);
 				symbol = combArg.getRawSymbol();
 				rawCombinedArguments = combArg.getArguments();
 
 			}
 
 			this.nArguments = numOfArguments;
 
 			this.symbol = symbol;
 		}
 
 		public String getSymbol() {
 			return symbol;
 		}
 
 		public int getIndex() {
 			return index;
 		}
 
 		public int getNumberOfArguments(){
 			return nArguments;
 		}
 
 		public String[] getArguments(List<String> rawArgumentList,String separator){
 			String[] retArray = new String[0];
 			if(separator == null) separator = ArgsUtil.getArgumentValuesSeparator();
			if(isCombined){
 				retArray = rawCombinedArguments.split(separator);
 			}
 			else if(nArguments>0){
 				int indexFrom = index+1;
 				retArray = rawArgumentList.subList(indexFrom,indexFrom + nArguments).toArray(retArray);
 			}
 
 			return retArray;
 		}
 
 		@Override
 		public String toString() {
 			String msg = "sym:"+symbol+" from:"+ index +" nParams:"+ nArguments + " isCombined:"+isCombined ;
 			if(isCombined) msg = msg + " combinedParams:"+ rawCombinedArguments;
 			return msg;
 		}
 	}
 
 	/**
 	 * Create table with symbol indexes. To test the argument has been recognized as a symbol see
 	 * {@link vara.app.startupargs.ArgsUtil#isSymbolParameter(String)}. 
 	 *
 	 * @param args container with all raw input arguments
 	 * @return table of indexes
 	 */
 	private static int [] getSymbolIndexes(List<String> args){
 
 		int len = args.size();
 		if(len == 0)	return new int[0];
 
 		int [] symbols = new int [len];
 		int offset=0;
 
 		for(int currentIndex = 0; currentIndex<len;currentIndex++){
 			String pretenderToSymbolParameter = args.get(currentIndex);
 			if(ArgsUtil.isSymbolParameter(pretenderToSymbolParameter)){
 				symbols[offset++] = currentIndex;
 			}
 		}
 
 		return Arrays.copyOf(symbols,offset);
 	}
 
 	/**
 	 * Create list with TODO:Finish me
 	 *
 	 * @param args container with all raw input arguments
 	 * @return container with <code>ParameterEntryHelper</code> objects
 	 */
 	private static List<ParameterEntryHelper> createParameterEntryHelpers(List<String> args){
 
 		List<ParameterEntryHelper> parameterHelper = new ArrayList();
 
 		int [] symbolIndexes = getSymbolIndexes(args);
 		int iArguments = args.size();
 		int length = symbolIndexes.length;
 
 		for (int i = 0; length>i;i++) {
 
 			int symbolIndex = symbolIndexes[i];
 			String symbol = args.get(symbolIndex);
 
 			if(log.isTraceEnabled())
 				log.trace("symbol index: {} <=> {}",symbolIndex,symbol);
 
 			//If you insert one prefix char '-' then stop parsing args
 			//TODO:fix me
 			if(symbol.length() == 1){
 				if(log.isDebugEnabled()) log.debug("Detected break-char '-' on {} index.", symbolIndex);
 				break;
 			}
 
 			int endIndex = i+1<length ? symbolIndexes[i+1] : iArguments;
 			int numOfParameters= endIndex - (symbolIndex+1);
 
 			parameterHelper.add(new ParameterEntryHelper(symbolIndex,numOfParameters, symbol));
 		}
 
 		return parameterHelper;
 	}
 
 	/**
 	 * TODO:Add description ,  (don't forget write about exceptions thrown in this method)
 	 * --symbol param ; -symbol=param; -symbol parm1 param2 ... ; -symbol=parameter1,parameter2...
 	 *
 	 * @param args container with command line arguments
 	 */
 	public static void parseParameters(List<String> args){
 		if(log.isDebugEnabled()){
 			log.debug("** PARSER: Start parse command line input arguments");
 			log.debug("CmdL args =>'{}'",optionValuesToString(args," "));
 		}
 
 		//make 100% sure that indexes on this list can't be changed
 		args = Collections.unmodifiableList(args);
 		List<ParameterEntryHelper> helpers = createParameterEntryHelpers(args);
 
 		for (ParameterEntryHelper entryHelper : helpers) {
 
 			String symbol =  ArgsUtil.recheck(entryHelper.getSymbol());
 			DefaultParameter optionHandler = (DefaultParameter)Parameters.getParameter(symbol);
 
 			if (optionHandler != null){
 
 				if( log.isDebugEnabled() ){
 					log.debug("Symbol information : {}",entryHelper);
 					log.debug("Found parameter class {}.",optionHandler.getClass().getSuperclass().getName());
 					log.debug("Expected number of params are :{} ",	optionHandler.getOptionValuesLength().toString2());
 				}
 
 				//Create list with arguments to pass internal arguments
 				//List<String> subListWithParameters = Collections.EMPTY_LIST;
 				
 				if(entryHelper.isCombined){
 					if(entryHelper.nArguments > 0){
						throw new OptionNotFoundException(args.get(entryHelper.index+1),"Missing prefix ?");
 					}
 				}
 
 				try {
 					String [] arguments = entryHelper.getArguments(args,optionHandler.getValueSeparator());
					//System.out.println("args:"+arguments.length +"{"+optionValuesToString(arguments,";")+"}");
 					optionHandler.handleOption(arguments);
 				}
 				catch( Exception exc ){	deliverCaughtException(exc); }
 
 				if( optionHandler.isExit() ){
 					System.exit(0);
 				}
 
 			}else{
 				deliverCaughtException(new OptionNotFoundException(entryHelper.symbol,"Unrecognized parameter"));
 			}
 		}//End For
 
 		if(log.isDebugEnabled()) log.debug("**PARSER: Parsing command line input arguments finished ! ");
 	}
 
 	public static void parseParameters(String ... args){
 		parseParameters(Arrays.asList(args));
 	}
 
 	private static String optionValuesToString(String[] v,String separator){
 		return optionValuesToString(Arrays.asList(v),separator);
 	}
 
 	private static String optionValuesToString(List<String> v,String separator){
 
 		StringBuffer sb = new StringBuffer(512);
 		int n = (v != null) ? v.size():0;
 		for (int i=0; i<n-1; i++,sb.append( separator )){
 			sb.append(v.get(i));
 		}
 		if(n>0)sb.append(v.get(n-1));
 		
 		return sb.toString();
 	}
 }
