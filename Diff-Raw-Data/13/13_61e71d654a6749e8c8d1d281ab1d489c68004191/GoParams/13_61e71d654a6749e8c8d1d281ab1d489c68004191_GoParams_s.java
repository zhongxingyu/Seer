 package uci;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class GoParams {
 	enum SearchType{
 		/** plan time out*/
 		plan,
 		/** search only fixed depth*/
 		fixedDepth,
 		/** fixed search time*/
 		fixedTime,
 		/** search infinitely (until told to stop explicitly)*/
 		infinite,
 	}
 	
 	private final static Pattern whiteTimeSel = Pattern.compile("wtime\\s+(\\d+)");
 	private final static Pattern blackTimeSel = Pattern.compile("btime\\s+(\\d+)");
 	private final static Pattern whiteTimeIncSel = Pattern.compile("winc\\s+(\\d+)");
 	private final static Pattern blackTimeIncSel = Pattern.compile("binc\\s+(\\d+)");
 	private final static Pattern depthSel = Pattern.compile("depth\\s+(\\d+)");
 	private final static Pattern moveTimeSel = Pattern.compile("movetime\\s+(\\d+)");
 	
 	public GoParams(String goCommand) {
 		
 		//note, this will not be correct if the different search types are not
 		//mutually exclusive (for instance, if we allow fixed move time and fixed
 		//depth, where one or the other acts as a limiting factor)
 		
 		//note, no support for 'movestogo' parameter
 		
 		//note, no support for 'ponder' parameter
 		
 		Matcher temp;
 		if(goCommand.contains("infinite")){
 			type = SearchType.infinite;
 		} else if((temp = depthSel.matcher(goCommand)).find()){
 			type = SearchType.fixedDepth;
 			depth = Integer.parseInt(temp.group(1));
 		} else if((temp = moveTimeSel.matcher(goCommand)).find()){
 			type = SearchType.fixedTime;
 			moveTime = Integer.parseInt(temp.group(1));
 		} else{
 			type = SearchType.plan;
 			temp = whiteTimeSel.matcher(goCommand);
			time[0] = Integer.parseInt(temp.group(1));
 			
 			temp = blackTimeSel.matcher(goCommand);
			time[1] = Integer.parseInt(temp.group(1));
 			
 			temp = whiteTimeIncSel.matcher(goCommand);
			increment[0] = Integer.parseInt(temp.group(1));
 			
 			temp = blackTimeIncSel.matcher(goCommand);
			increment[1] = Integer.parseInt(temp.group(1));
 		}
 	}
 	
 	final SearchType type;
 	
 	boolean ponder = false;
 	
 	/** time remaining, indexed [player]*/
 	final int[] time = new int[2];
 	/** time increment per move, indexed [player]*/
 	final int[] increment = new int[2];
 	/** max time per move*/
 	int moveTime;
 	
 	/** max search depth*/
 	int depth;
 }
