 package edu.illinois.gitsvn.infra.collectors;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.illinois.gitsvn.infra.DataCollector;
 
 import org.eclipse.jgit.errors.IncorrectObjectTypeException;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.errors.StopWalkException;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.gitective.core.filter.commit.CommitFilter;
 
 /**
  * Collector of issue references from commit messages
  * @author sshmarkatiuk
  *
  */
 public class IssuesCollector extends CommitFilter implements DataCollector {
 
 	private Set<String> issues;
 	private static final String ISSUES_PATTERN_REGEX = "(((fix|complet|clos)(es|ed|ing)? (for )?)?(fix|bug|issue|ticket|task|feature)s? ?(#?[0-9]+)(( and |,( )?| )(#?[0-9]+))*|#[0-9]+)"; 
 
 	private Matcher numberMatcher, m;
 	private List<Matcher> issueMatchers = new ArrayList<Matcher>();
 	private List<String> numberMatches, issueMatches;
 	private List<List<String>> matchesListsList = new ArrayList<List<String>>();
 	private Pattern numberPattern = Pattern.compile("[0-9]+");
 	private List<Pattern> issuePatterns = new ArrayList<Pattern>();
 
 	
 	public IssuesCollector() {
 		issuePatterns.add(
 			// Numbers with a hash symbol are usually used as issue references
 			// Capital letters followed by dash and sequence of digits are typical JIRA issue references
			Pattern.compile("(#|[A-Z]{3,}-)[0-9]+", Pattern.CASE_INSENSITIVE)
 		);
 		issuePatterns.add(
 			// Numbers preceded by 'fix' word or its variations are usually used
 			// as issue references 
 			Pattern.compile("(fix|complet|clos)(es|ed|ing)? ?(for )?(#?[0-9]+)(( and |,( )?| )(#?[0-9]+))*", Pattern.CASE_INSENSITIVE)
 		);
 		issuePatterns.add(
 			// Numbers preceded by 'bug', 'issue', 'ticket', etc words or their variations 
 			// are usually used as issue references
 			Pattern.compile("((fix|complet|clos)(es|ed|ing)? ?(for )?)?((fix|bug|issue|ticket|task|feature)s? ?)(#?[0-9]+)(( and |,( )?| )(#?[0-9]+))*", Pattern.CASE_INSENSITIVE)
 		);
 	}
 
 	/**
 	 * Method extracts issue numbers from string using simple heuristic implemented
 	 * as a set of regular expressions
 	 * 
 	 * @param String line - commit message to extract issue numbers from
 	 * @return list of issue numbers
 	 */
 	private Set<String> findIssues(String line) {
 		Set<String> result;
 		String match;
 		String[] matchArr;
 		
 		numberMatcher = numberPattern.matcher(line);
 		for(Pattern p: issuePatterns) {
 			issueMatchers.add(p.matcher(line));
 		}
 		numberMatches = new ArrayList<String>();
 		matchesListsList = new ArrayList<List<String>>();
 		result = new HashSet<String>();
 		
 		while(numberMatcher.find()){
 			numberMatches.add(numberMatcher.group());
 		}
 		for(int i = 0; i < issueMatchers.size(); i++) {
 			m = issueMatchers.get(i);
 			issueMatches = new ArrayList<String>();
 		    while(m.find()){
 		    	issueMatches.add(m.group());
 			}
 		    matchesListsList.add(issueMatches);
 		}
 		
 		for(String num : numberMatches) {
 			for(Iterator<List<String>> listIterator = matchesListsList.iterator(); listIterator.hasNext();) {
 				for (Iterator<String> iter = listIterator.next().iterator(); iter.hasNext();) {
 					match = (String) iter.next();
 					// need to match whole numbers, that's why string is split by non-digit characters
 					matchArr = match.split("\\D+");
 					for(int j = 1; j < matchArr.length; j++) {
 						if(matchArr[j].equals(num)) {
 							result.add(num);
 							break;
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	@Override
 	public String name() {
 		return "Issues number";
 	}
 
 	@Override
 	public String getDataForCommit() {
 		return Integer.toString(issues.size());
 	}
 
 	@Override
 	public boolean include(RevWalk walker, RevCommit cmit)
 			throws StopWalkException, MissingObjectException,
 			IncorrectObjectTypeException, IOException {
 		
 		issues = findIssues(cmit.getFullMessage());
 		
 		return cmit.getFullMessage().matches(ISSUES_PATTERN_REGEX);
 	}
 
 }
