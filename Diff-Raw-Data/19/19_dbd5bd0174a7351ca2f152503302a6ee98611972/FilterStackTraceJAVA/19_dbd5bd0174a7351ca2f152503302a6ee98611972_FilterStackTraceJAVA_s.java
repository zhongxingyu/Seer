 /**
  * Slightly modified version of InfoZilla's FilterStackTraceJAVA.java, for use 
  * by the Eggnet Social-Technical Network project.
  * 
  * Credit : InfoZilla Tool
  * <a href='http://groups.csail.mit.edu/pag/pubs/bettenburg-msr-2008.pdf'>
  * Extracting Structural Information From Bug Reports
  * </a>
  * @authors Nicolas Bettenburg, Rahul Premraj, Thomas Zimmermann, Sunghun Kim
  */
 package extractor.filters;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import models.extractor.stacktrace.StackTrace;
 
 import comm.RegExHelper;
 
 public class FilterStackTraceJAVA implements IFilter
 {
 	private FilterTextRemover	textRemover;
	public static String		JAVA_EXCEPTION			= "^(([\\w<>\\$_]+\\.)+[\\w<>\\$_]+(Error|Exception){1}(\\s|:))";
	public static String		JAVA_REASON				= "(:?.*?)(at\\s+([\\w<>\\$_]+\\.)+[\\w<>\\$_]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)";
	public static String		JAVA_TRACE				= "(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
 	public static String		JAVA_CAUSE				= "(Caused by:).*?(Exception|Error)(.*?)(\\s+at.*?\\(.*?:\\d+\\))+";
 	public static String		JAVA_STACKTRACE			= JAVA_EXCEPTION + JAVA_REASON + JAVA_TRACE;
 	private static Pattern		pattern_stacktrace_java	= Pattern.compile(JAVA_STACKTRACE, 40);
 	private static Pattern		pattern_cause_java		= Pattern.compile(JAVA_CAUSE, 40);
 
 	private List<MatchResult> findStackTraces(CharSequence s)
 	{
 		List<MatchResult> stacktraces = new ArrayList<MatchResult>();
 
 		for (MatchResult r : RegExHelper.findMatches(pattern_stacktrace_java, s))
 		{
 			stacktraces.add(r);
 		}
 
 		for (MatchResult r : RegExHelper.findMatches(pattern_cause_java, s))
 		{
 			stacktraces.add(r);
 		}
 		return stacktraces;
 	}
 
 	private StackTrace createCause(String stackTraceMatchGroup)
 	{
 		String exception = "";
 		String reason = "";
 		List foundFrames = new ArrayList();
 
 		String causeException = "(Caused by:)(.*?(Error|Exception){1})(.*?)(at\\s+([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
 		Pattern causeEPattern = Pattern.compile(causeException, 40);
 
 		Matcher exceptionMatcher = causeEPattern.matcher(stackTraceMatchGroup);
 		if (exceptionMatcher.find())
 		{
 			MatchResult matchResult = exceptionMatcher.toMatchResult();
 
 			exception = matchResult.group(2).trim();
 			reason = matchResult.group(4).trim();
 
 			String regexFrames = "(^\\s*?at\\s+(([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.*?\\)$))";
 			Pattern patternFrames = Pattern.compile(regexFrames, 40);
 
 			for (MatchResult framesMatch : RegExHelper.findMatches(patternFrames, matchResult.group(5)))
 			{
 				foundFrames.add(framesMatch.group(2).replaceAll("[\n\r]", ""));
 			}
 		}
 		StackTrace trace = new StackTrace(exception, reason, foundFrames);
 		trace.setCause(true);
 
 		return trace;
 	}
 
 	private StackTrace createTrace(String stackTraceMatchGroup)
 	{
 		String exception = "";
 		String reason = "";
 		List foundFrames = new ArrayList();
 
		String traceException = "(([\\w<>\\$_]+\\.)+[\\w<>\\$_]+(Error|Exception){1})(.*?)(at\\s+([\\w<>\\$_\n\r]+\\.)+[\\w<>\\$_\n\r]+\\s*\\(.+?\\.java(:)?(\\d+)?\\)(\\s*?at\\s+([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.+?\\.java(:)?(\\d+)?\\))*)";
		Pattern tracePattern = Pattern.compile(traceException, 40);
 
 		Matcher exceptionMatcher = tracePattern.matcher(stackTraceMatchGroup);
 		if (exceptionMatcher.find())
 		{
 			MatchResult matchResult = exceptionMatcher.toMatchResult();
 
 			exception = matchResult.group(1).trim();
 			reason = matchResult.group(4).trim();
 
 			String regexFrames = "(^\\s*?at\\s+(([\\w<>\\$_\\s]+\\.)+[\\w<>\\$_\\s]+\\s*\\(.*?\\)$))";
 			Pattern patternFrames = Pattern.compile(regexFrames, 40);
 
 			for (MatchResult framesMatch : RegExHelper.findMatches(patternFrames, matchResult.group(5)))
 			{
 				foundFrames.add(framesMatch.group(2).replaceAll("[\n\r]", ""));
 			}
 		}
 		StackTrace trace = new StackTrace(exception, reason, foundFrames);
 		trace.setCause(false);
 
 		return trace;
 	}
 
 	private List<StackTrace> getStackTraces(CharSequence inputSequence)
 	{
 		List stackTraces = new ArrayList();
 
 		int[] possibleStart = findExceptions(inputSequence);
 		MatchResult match;
 		for (int i = 0; i < possibleStart.length - 1; i++)
 		{
 			CharSequence region = inputSequence.subSequence(possibleStart[i], possibleStart[(i + 1)] - 1);
 			List matches = findStackTraces(region);
 
 			for (Iterator localIterator = matches.iterator(); localIterator.hasNext();)
 			{
 				match = (MatchResult) localIterator.next();
 				String matchText = match.group();
 
 				int traceStart = inputSequence.toString().indexOf(matchText);
 				int traceEnd = traceStart + matchText.length() + 1;
 				this.textRemover.markForDeletion(traceStart, traceEnd);
 				if ((traceStart == 0) && (traceEnd == 0))
 				{
 					System.out.println("Critical Error in Stacktrace Filter! Could not find start and End!");
 				}
 				if (matchText.trim().startsWith("Caused by:"))
 				{
 					StackTrace cause = createCause(matchText);
 
 					cause.setTraceStart(traceStart);
 					cause.setTraceEnd(traceEnd);
 					stackTraces.add(cause);
 				}
 				else
 				{
 					StackTrace trace = createTrace(matchText);
 
 					trace.setTraceStart(traceStart);
 					trace.setTraceEnd(traceEnd);
 					stackTraces.add(trace);
 				}
 			}
 
 		}
 
 		if (possibleStart.length > 0)
 		{
 			CharSequence region = inputSequence.subSequence(possibleStart[(possibleStart.length - 1)], inputSequence
 					.length());
 			List<MatchResult> matches = findStackTraces(region);
 
 			for (MatchResult matchRes : matches)
 			{
 				String matchText = matchRes.group();
 
 				int traceStart = inputSequence.toString().lastIndexOf(matchText);
 				int traceEnd = traceStart + matchText.length();
 				this.textRemover.markForDeletion(traceStart, traceEnd);
 				if ((traceStart == 0) && (traceEnd == 0))
 				{
 					System.out.println("Critical Error in Stacktrace Filter! Could not find start and End!");
 				}
 
 				if (matchText.trim().startsWith("Caused by:"))
 				{
 					StackTrace cause = createCause(matchText);
 
 					cause.setTraceStart(traceStart);
 					cause.setTraceEnd(traceEnd);
 					stackTraces.add(cause);
 				}
 				else
 				{
 					StackTrace trace = createTrace(matchText);
 
 					trace.setTraceStart(traceStart);
 					trace.setTraceEnd(traceEnd);
 					stackTraces.add(trace);
 				}
 			}
 		}
 
 		return stackTraces;
 	}
 
 	private final int[] findExceptions(CharSequence s)
 	{
 		List exceptionList = new ArrayList();
 
 		Pattern exceptionPattern = Pattern.compile(JAVA_EXCEPTION, 40);
 
 		for (MatchResult r : RegExHelper.findMatches(exceptionPattern, s))
 		{
 			if (exceptionList.size() > 0)
 			{
 				String newRegion = s.subSequence(((Integer) exceptionList.get(exceptionList.size() - 1)).intValue(),
 						r.start()).toString();
 				if (newRegion.split("[\n\r]").length >= 20)
 					exceptionList.add(new Integer(r.start()));
 			}
 			else
 			{
 				exceptionList.add(new Integer(r.start()));
 			}
 
 		}
 
 		if (exceptionList.size() == 0)
 		{
 			exceptionList.add(new Integer(0));
 		}
 
 		int[] results = new int[exceptionList.size()];
 		for (int i = 0; i < exceptionList.size(); i++)
 		{
 			results[i] = ((Integer) exceptionList.get(i)).intValue();
 		}
 		return results;
 	}
 
 	public String getOutputText()
 	{
 		return this.textRemover.doDelete();
 	}
 
 	public List<StackTrace> runFilter(String inputText)
 	{
 		this.textRemover = new FilterTextRemover(inputText);
 
 		List foundStackTraces = getStackTraces(inputText);
 
 		return foundStackTraces;
 	}
 }
