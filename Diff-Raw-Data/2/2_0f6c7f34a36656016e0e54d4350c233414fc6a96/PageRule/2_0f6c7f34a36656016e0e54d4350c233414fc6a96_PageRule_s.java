 package net.inervo.TedderBot.NewPageSearch;
 
 /*
  * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
  * Inervo Networks nor the names of its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.inervo.Wiki.WikiFetcher;
 
 public class PageRule {
 	private static final int DEFAULT_SCORE = 10;
 	private static final int DEFAULT_THRESHOLD = 10;
 
 	private WikiFetcher fetcher = null;
 	private String searchName = null;
 	private String pageName = null;
 	private int threshold = DEFAULT_THRESHOLD;
 
 	private List<MatchRule> patterns = new ArrayList<MatchRule>();
 	private List<String> errors = new LinkedList<String>();
 
 	public PageRule( WikiFetcher fetcher, String pageName, String searchName ) throws Exception {
 		this.fetcher = fetcher;
 		this.searchName = searchName;
 		this.pageName = pageName;
 		parseRule( pageName );
 	}
 
 	private void parseRule( String pageName ) throws Exception {
 
 		Pattern rexLine = Pattern.compile( "^\\s*([\\-\\d]*)\\s*\\/(.*?)\\/\\s*(\\,\\s*.*\\s*)?$" );
 		Pattern defaultScorePattern = Pattern.compile( "^\\s*@@(\\d+)@@\\s*$" );
 		Pattern categoryPattern = Pattern.compile( "^\\s*(\\d*)\\s*\\$\\$(.*)\\$\\$\\s*$" );
 
 		String input = null;
 		try {
 			input = fetcher.getPageText( pageName );
 		} catch ( Exception ex ) {
 			throw new Exception( "failed fetching rule at " + pageName + ". Original exception: " + ex.getMessage() );
 		}
 		// print( "text: |" + input + "|" );
 		Scanner s = new Scanner( input ).useDelimiter( "\\n" );
 
 		while ( s.hasNext() ) {
 			String line = stripComments( s.next() );
 
 			Matcher rexMatcher = rexLine.matcher( line );
 			Matcher scoreMatcher = defaultScorePattern.matcher( line );
 			Matcher categoryMatcher = categoryPattern.matcher( line );
 
 			MatchRule rule = new MatchRule();
 
 			if ( rexMatcher.matches() ) {
 				String scoreString = rexMatcher.group( 1 );
 				rule.score = DEFAULT_SCORE;
 				if ( scoreString.length() > 0 ) {
 					rule.score = Integer.parseInt( scoreString );
 				}
 
 				safeSetPattern( rule, rexMatcher.group( 2 ) );
 
 				if ( rexMatcher.groupCount() >= 3 ) {
 					String inhibitors = rexMatcher.group( 3 );
 					// print( "inhib: " + inhibitors );
 					parseInhibitors( rule, inhibitors );
 				}
 				// if ( rexMatcher.groupCount() > 2 ) {
 				// String extra = rexMatcher.group( 3 );
 				// if ( extra.length() > 0 ) {
 				// print( "extra: |" + extra + "|, whole line: " + line );
 				// }
 				// }
 
 				// print( "match: " + rule.pattern + " / " + rule.score );
 				if ( rule.isValidPattern() ) {
 					patterns.add( rule );
 				}
 			} else if ( scoreMatcher.matches() ) {
 				String score = scoreMatcher.group( 1 );
 				threshold = Integer.parseInt( score );
 			} else if ( categoryMatcher.matches() ) {
 				// print( "category: " + line + " count: " + categoryMatcher.groupCount() );
 				String scoreString = categoryMatcher.group( 1 );
 				rule.score = DEFAULT_SCORE;
 				if ( scoreString.length() > 0 ) {
 					rule.score = Integer.parseInt( scoreString );
 				}
 
 				safeSetPattern( rule, categoryMatcher.group( 2 ) );
 
 				if ( rule.isValidPattern() ) {
 					patterns.add( rule );
 				}
 			} else if ( line.trim().length() > 0 ) {
 				// print( "no match: |" + line + "|" );
 				errors.add( "no match: " + line );
 			}
 		}
 
 		// pattern:
 
 	}
 
 	private void safeSetPattern( MatchRule rule, String pattern ) {
 		if ( pattern.contains( "\\p{" ) ) {
 			errors.add( "skipped pattern, character sets are poorly supported, pattern: " + pattern );
 			return;
 		}
 
 		try {
 			rule.setPattern( pattern );
 
 		} catch ( PatternSyntaxException ex ) {
 			// catch and rethrow with more context.
 			errors.add( "failed compiling rule, error was " + ex.getDescription() + ", pattern: " + ex.getPattern() );
 			return;
 		}
 	}
 
 	private void safeAddInhibitor( MatchRule rule, String pattern ) {
 		if ( pattern.contains( "\\p{" ) ) {
 			errors.add( "skipped inhibitor pattern, character sets are poorly supported, pattern: <nowiki>" + pattern + "</nowiki>" );
 			return;
 		}
 
 		try {
 			rule.addInhibitor( pattern );
 
 		} catch ( PatternSyntaxException ex ) {
 			// catch and rethrow with more context.
 			errors.add( "failed compiling inhibitor rule in [[" + pageName + "]], error was " + ex.getDescription() + ", pattern: <nowiki>" + ex.getPattern()
 					+ "</nowiki>" );
 			return;
 		}
 	}
 
 	private String stripComments( String next ) {
 		return next.replaceAll( "<!--.*?-->", "" );
 	}
 
 	private void parseInhibitors( MatchRule rule, String inhibitors ) {
 		if ( inhibitors == null || inhibitors.length() == 0 ) {
 			return;
 		}
 		// List<Pattern> list = new ArrayList<Pattern>();
 
 		while ( true ) {
 			Pattern inhibitorPattern = Pattern.compile( "^\\,\\s*\\/(.*?)\\/\\s*$" );
 			Matcher matcher = inhibitorPattern.matcher( inhibitors );
 			if ( !matcher.matches() ) {
 				break;
 			}
 
 			safeAddInhibitor( rule, matcher.group( 1 ) );
 
 			inhibitors = matcher.replaceFirst( "" );
 		}
 	}
 
 	public static class MatchRule {
 
 		private List<Pattern> ignore;
 		private Pattern pattern;
 		protected int score;
 
 		public MatchRule( String pattern ) {
 			setPattern( pattern );
 		}
 
 		public void addInhibitor( String pattern ) {
 			if ( ignore == null ) {
 				ignore = new ArrayList<Pattern>();
 			}
 
 			if ( pattern != null && pattern.length() > 0 ) {
 				ignore.add( Pattern.compile( pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE ) );
 			}
 		}
 
 		public MatchRule() {
 		}
 
 		public boolean isValidPattern() {
 			if ( pattern != null && pattern.toString() != null && pattern.toString().length() > 0 ) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public void setPattern( Pattern pattern ) {
 			this.pattern = pattern;
 		}
 
 		public void setPattern( String pattern ) {
 			this.pattern = Pattern.compile( pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
 		}
 
 		public List<Pattern> getIgnore() {
 			return ignore;
 		}
 
 		public Pattern getPattern() {
 			return pattern;
 		}
 
 		public int getScore() {
 			return score;
 		}
 
 	}
 
 	/** getters and setters **/
 
 	/**
 	 * search name. For instance, 'Oregon'.
 	 */
 	public String getSearchName() {
 		return searchName;
 	}
 
 	/**
 	 * list of match rules
 	 * 
 	 * @return
 	 */
 	public List<MatchRule> getPatterns() {
 		return patterns;
 	}
 
 	/**
 	 * list of errors created on parsing the rules
 	 * 
 	 * @return
 	 */
 	public List<String> getErrors() {
 		return errors;
 	}
 
 	/**
 	 * configuration page, such as 'User:AlexNewArtBot/Oregon'
 	 * 
 	 * @return
 	 */
 	public String getRulePage() {
 		return pageName;
 	}
 
 	/**
 	 * where we are placing errors for this configuration, such as 'User:TedderBot/NewPageSearch/Oregon/errors'
 	 * 
 	 * @return
 	 */
 	public String getErrorPage() {
 		return "User:TedderBot/NewPageSearch/" + searchName + "/errors";
 	}
 
 	/**
 	 * where we are placing archives for this configuration, such as 'User:TedderBot/NewPageSearch/Oregon/errors'
 	 * 
 	 * @return
 	 */
 	public String getArchivePage() {
		return "User:TedderBot/NewPageSearch/" + searchName + "/archive";
 	}
 
 	/**
 	 * where That Other Bot placed archives, such as 'User:AlexNewArtBot/Oregon/archive'
 	 * 
 	 * @return
 	 */
 	public String getOldArchivePage() {
 		return "User:AlexNewArtBot/" + searchName + "/archive";
 	}
 
 	/**
 	 * where we are putting search results, such as 'User:AlexNewArtBot/OregonSearchResult'
 	 * 
 	 * @return
 	 */
 	public String getSearchResultPage() {
 		return "User:AlexNewArtBot/" + searchName + "SearchResult";
 	}
 
 	/**
 	 * How many points is the threshold for inclusion. Default is 10, is embedded on rule page like @@20@@.
 	 * 
 	 * @return
 	 */
 	public int getThreshold() {
 		return threshold;
 	}
 
 }
