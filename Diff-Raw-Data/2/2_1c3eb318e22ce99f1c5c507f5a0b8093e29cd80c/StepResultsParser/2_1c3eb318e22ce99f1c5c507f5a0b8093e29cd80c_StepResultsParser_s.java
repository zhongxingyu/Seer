 package gov.va.mumps.debug.xtdebug;
 
 import gov.va.mumps.debug.xtdebug.vo.ReadResultsVO;
 import gov.va.mumps.debug.xtdebug.vo.StackVO;
 import gov.va.mumps.debug.xtdebug.vo.StepResultsVO;
 import gov.va.mumps.debug.xtdebug.vo.VariableVO;
 import gov.va.mumps.debug.xtdebug.vo.WatchVO;
 import gov.va.mumps.debug.xtdebug.vo.StepResultsVO.ResultReasonType;
 
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StepResultsParser {
 	
 	private static final Pattern captureTagLocation = Pattern.compile("^.*?[A-Z]+\\: *([\\w\\+\\^]+)\\s*.*$");
 	private static final Pattern stackCaller = Pattern.compile("^\\s*([%\\w\\d\\+\\^]*).*$");
 	
 	public StepResultsVO parse(String data) {
 		//System.out.println(data);
 		//results
 		ResultReasonType resultReason = null;
 		String routineName = null;
 		boolean complete = false;
 		LinkedList<StackVO> stack = new LinkedList<StackVO>();
 		int lineLocation = -1;
 		String locationAsTag = null;
 		String nextCommand = null;
 		String lastCommand = null;
 		LinkedHashSet<VariableVO> variables = new LinkedHashSet<VariableVO>(70);
 		String resultLine = null;
 		LinkedList<WatchVO> watchedVariables = new LinkedList<WatchVO>();
 		Integer maxChars = null;
 		Integer timeout = null;
 		boolean starRead = false;
 		boolean typeAhead = false;
 		ReadResultsVO readResult = null;
 		
 		//scanning logic
 		SectionType section = null;
 		Scanner scanner = new Scanner(data);
 		scanner = scanner.useDelimiter("\n");
 		String line;
 		
 		//parsing state
 		boolean readCommandFound = false;
 
 		while (scanner.hasNext()) {
 			line = scanner.next();
 			try {
 				String str = line.substring(8);
 				section = SectionType.valueOf(str);
 				
 				if (section == SectionType.VALUES) {
 					scanner.next();
 					assert scanner.next().equals("VALUES");
 					continue;
 				} else if (section == SectionType.READ) {
 					readCommandFound = true;
 				}
 			} catch (IllegalArgumentException e1) {
 			} catch (NullPointerException e2) {
 			} catch (IndexOutOfBoundsException e3) {
 			}
 			
 			switch (section) {
 			
 			case REASON:
				if (line.equals("DONE -- PROCESSING FINISHED"))
 					complete = true;
 				else if (line.startsWith("START:" )) {
 					resultReason = ResultReasonType.START;
 				} else if (line.startsWith("STEP MODE: ")) {
 					resultReason = ResultReasonType.STEP;
 					locationAsTag = captureTagLoc(line);
 				} else if (line.startsWith("BREAKPOINT:")) {
 					//BREAKPOINT: TST33+2^TSTROUT   <-- note: the breakpoint sent originaly was TSTROUT+72^TSTROUT... will not be able to tie my line breakpoints back until either 1) the rpc sends back the original 2) I send the offset of the last line label
 					resultReason = ResultReasonType.BREAKPOINT;
 					locationAsTag = captureTagLoc(line);
 				} else if (line.startsWith("WATCH ON VARIABLES:")) {
 					//WATCH ON VARIABLES: STACK5+2^TSTROUT AT +55^TSTROUT     S Q="a",R="b",S="c"
 					resultReason = ResultReasonType.WATCHPOINT;
 					locationAsTag = captureTagLoc(line);
 				} else if (line.startsWith("WRITE:")) {
 					//WRITE:  STACK5+3^TSTROUT  LINE: IM IN STACK2IM IN STACK5
 					resultReason = ResultReasonType.WRITE;
 					locationAsTag = captureTagLoc(line);
 				} else if(line.startsWith("READ:")) {
 					resultReason = ResultReasonType.READ;
 					locationAsTag = captureTagLoc(line);
 				} else if (line.startsWith("   NEXT COMMAND: ")) {
 					nextCommand = line.substring(17);
 				} else if (line.startsWith("   LAST COMMAND: ")) {
 					lastCommand = line.substring(17);
 				}
 				break;
 			case LOCATION:
 				if (line.startsWith("ROUTINE: "))
 					routineName = line.substring(9);
 				else if (line.startsWith("LINE: ")) {
 					if (line.substring(6).equals(""))
 						lineLocation = -1;
 					else
 						lineLocation = Integer.parseInt(line.substring(6));
 				}
 				break;
 			case STACK:				
 				if (line.indexOf('>') != -1) {
 					int gtLoc = line.indexOf('>');
 					Matcher m = stackCaller.matcher(line);
 					m.find();
 					String caller = m.group(1);
 					stack.add(new StackVO(
 							line.substring(gtLoc+2, line.length()),
 							caller.equals("") ? null : caller));
 				}
 				
 				break;
 			case VALUES:
 				variables.add(new VariableVO(line, scanner.next()));
 				break;
 			case WATCH:
 //				WATCH DATA
 //				Q = x^<UNDEFINED>
 //
 //				R = y^<UNDEFINED>
 				if (line.contains("=") && line.contains(" "))
 					watchedVariables.add(new WatchVO(line.substring(0, line.indexOf(" ")),
 							null, null));
 				//TODO: problem in api results. the API cannot tell me the correct prev and new values if they contain the "^" character
 				//setting null until API is fixed
 
 				
 				//this returns a list of vars that are (1) being watched (2) changed.
 				//it is possible for this to be more than 1, example. the NEW comand
 				//does not actually run seperatly for each parm, but once, wiping out
 				//all of the previous values in the symbol table. In which case multiple
 				//watchpoints are being hit actually.
 				break;
 			case READ:
 				if (line.startsWith("NUM CHARS:")) {
 					String maxCharsStr = line.substring(11);
 					try {
 						maxChars = Integer.parseInt(maxCharsStr);
 					} catch(IllegalArgumentException e) {
 					}
 				} else if (line.startsWith("TIMEOUT:")) {
 					String timeOutStr = line.substring(9);
 					try {
 						timeout = Integer.parseInt(timeOutStr);
 					} catch(IllegalArgumentException e) {
 					}
 				} else if (line.startsWith("STAR-READ:")) {
 					String starReadStr = line.substring(11);
 					try {
 						starRead = starReadStr.equals("1");
 					} catch(IllegalArgumentException e) {
 					}
 				} else if (line.startsWith("TYPE-AHEAD:")) {
 					String typeAheadStr = line.substring(12);
 					try {
 						typeAhead = typeAheadStr.equals("1");
 					} catch(IllegalArgumentException e) {
 					}
 				}
 				if (line.startsWith("LINE: ")) //grab whatever output that needs to be written
 					resultLine = line.substring(6);
 				break;
 			case WRITE:
 				if (line.startsWith("LINE: "))
 					resultLine = line.substring(6);
 			}
 
 		}
 		
 		assert(resultReason != null); //TODO: throw proper parsing exception
 		if (readCommandFound)
 			readResult = new ReadResultsVO(maxChars, timeout, starRead, typeAhead);
 		return new StepResultsVO(resultReason, complete, variables, 
 				routineName, lineLocation, 
 				locationAsTag, nextCommand, lastCommand, stack, resultLine,
 				watchedVariables, readResult); //TODO: instead of a pojo with a single level, having all variables, considering having a pojo with a tree level, one for each section maybe. more organized
 	}
 
 	private String captureTagLoc(String line) {
 		Matcher m = captureTagLocation.matcher(line);
 		m.find();
 		return m.group(1);
 	}
 
 	private enum SectionType {
 		REASON, WRITE, READ, VALUES, STACK, WATCH, LOCATION;
 	}
 	
 }
