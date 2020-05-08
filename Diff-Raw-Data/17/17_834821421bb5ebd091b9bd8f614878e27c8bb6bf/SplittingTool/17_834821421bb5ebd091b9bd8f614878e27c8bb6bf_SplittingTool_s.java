 package what.sp_parser;
 
 
 
 import what.sp_config.ConfigWrap;
 
 /**
  * 
  * The SplittingTool splits the <code>str</code> from its 
  * <code>parsingTask</code> and enters the splitted raw parts into its <code>DataEntry</code>.
  * It requires the date to be at the first place, then the IP, then all other informations and the statement as the last argument,
  * because the statement can contain commas and endOfLines.
  * It's used statically, because it's just a utility-class which doesn't need objects.
  * 
  * @author Alex
  *
  */
 public class SplittingTool {
 		
 	
 	private SplittingTool() {
 		//private constructor, because utility-class
 	}
 	
 	
 	/**
 	 * This is the main-method of the SplittingTool. It starts all "splitters" and returns true if they all finished working successfully
 	 * @param pt the ParsingTask, whos line will be splitted
 	 * @return true if everything worked correctly
 	 */
 	protected static boolean split(ParsingTask pt) {
 		
 		return (splitTime(pt) && splitOthers(pt) && splitStatement(pt));
	
 	}
 
 	/**
 	 * This method uses the string-compare from sp_config.StringMapRow to look in the whole string for keywords which identify
 	 * that the statement gets data from a certain table. If nothing is found it sets the value to "other".
 	 * @param pt - the ParsingTask
 	 * @return true if no problem occurred
 	 */
 	private static boolean splitStatement(ParsingTask pt) {
 				
 		ConfigWrap conf = pt.getPm().getConfig();
 		return conf.getEntryAt(conf.getSize() - 1).split(pt.getDe(), pt.getStr(), conf.getSize() - 2);
 				
 	}
 
 	/**
 	 * This method splits the time-component of a certain string and puts it into dataEntry.info(0-5).
 	 * @param pt - the ParsingTask
 	 * @return true if no problem occurred
 	 */
 	protected static boolean splitTime(ParsingTask pt) {
 		
 		DataEntry de = pt.getDe();
 		String[] str = pt.getSplitStr();
 		
 		
 		for (int i = 0; i < 6; i++) {
 			
 			if (!pt.getPm().getConfig().getEntryAt(i).getCategory().toUpperCase().equals("TIME")) {
 				return false;
 			}
 			if (!pt.getPm().getConfig().getEntryAt(i).split(de, str[i], i)) {
 				return false;
 			}
 		}		
 		
 		return true;		
 	}
 	
 	/**
 	 * This method splits all other parts of the currect string. It uses the strategy-pattern, which means, that it uses the method
 	 * split which all RowEntry-Subclasses contain. So it will be splitted differently depending on which RowEntry cw.getEntryAt(i) actually is.
 	 * @param pt - the ParsingTask
 	 * @return true , because there is no correctness-check involved
 	 */
 	private static boolean splitOthers(ParsingTask pt) {
 		
 		DataEntry de = pt.getDe();
 		String [] str = pt.getSplitStr();
 		ConfigWrap cw = pt.getPm().getConfig();
 		
 		
 		for (int i = 7; i < pt.getPm().getConfig().getSize() - 1; i++) {
 			cw.getEntryAt(i).split(de, str[i], i - 1);
 		}
 		return true;		
 	}
 	
 	
 }
 	
