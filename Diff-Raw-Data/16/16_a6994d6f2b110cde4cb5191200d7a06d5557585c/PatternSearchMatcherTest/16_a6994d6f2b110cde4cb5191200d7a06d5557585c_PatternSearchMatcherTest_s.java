 package org.gjt.sp.jedit.search;
 
 import static junit.framework.Assert.*;
 import static org.gjt.sp.jedit.search.MatchAssert.*;
 
 import org.gjt.sp.jedit.search.SearchMatcher.Match;
 import org.junit.Test;
 
 public class PatternSearchMatcherTest {
 	
 	/**
 	 * <p>{@code start} is used to signify whether or not the character
 	 * sequence (could be user selected) starts from a beginning of a
 	 * line. If it doesn’t start, ^ should not match the in the beginning
 	 * of the input (the first match must be skipped).</p>
 	 * @testcreated 2011-09-22
 	 * @testpriority medium
 	 */
 	@Test
 	public void trueStartMatchesFromBeginningOfTheLine() {
 	    final PatternSearchMatcher spm =
 	        new PatternSearchMatcher("^abbacd$", false);
 
 	    assertMatch(0, 6, 
 	    	spm.nextMatch("abbacd\nabbacd\n", true, true, true, false));
 	}
 	
 	/**
 	 * <p>Like {@link #trueStartMatchesFromBeginningOfTheLine()} but
 	 * expect it to match on the line after first LF.</p>
 	 * @testcreated 2011-09-28
 	 * @testpriority medium
 	 */
 	@Test
 	public void falseStartMatchesFromNextLine() {
 		final PatternSearchMatcher spm =
 	        new PatternSearchMatcher("^abbacd$", false);
 	
 	    assertMatch(7, 13, 
 	    	spm.nextMatch("abbacd\nabbacd\n", false, true, true, false));
 	}
 	
 	/**
 	 * <p>{@code end} is used to signify whether or not the character
 	 * sequence (could be user selected) ends to end of line.
 	 * If it doesn’t end, $ should not match the in the end
 	 * of the input (the last match must be skipped).</p>
 	 * @testcreated 2011-09-22
 	 * @testpriority medium
 	 */
 	@Test
 	public void trueEndMatchesToTheEnd() {
 	    final PatternSearchMatcher spm =
 	        new PatternSearchMatcher("^abbacd$", false);
 	
 	    assertMatch(7, 13, 
 	    	spm.nextMatch("abbacd\nabbacd\n", false, true, true, false));
 	}
 	
 	/**
 	 * <p>Like {@link #trueEndMatchesToTheEnd()} but
 	 * expect it to match on the line before the last LF.</p>
 	 * @testcreated 2011-09-28
 	 * @testpriority medium
 	 */
 	@Test
 	public void falseEndMatchesToTheEOLBeforeLast() {
 		final PatternSearchMatcher spm =
 	        new PatternSearchMatcher("^abbacd$", false);
 	
 	    assertMatch(0, 6, 
 	    	spm.nextMatch("abbacd\nabbacd\n", true, false, true, false));
 	}	
 	
 	/**
 	 * @testcreated 2011-09-28
 	 * @testpriority medium
 	 */
 	@Test
 	public void trueFirstTimeMatchesFirstMatch() {
 		final PatternSearchMatcher spm =
 			new PatternSearchMatcher("^.*$", false);
 		
 		assertMatch(0, 0,
 			spm.nextMatch("\nabbacd\nabbacd\n", true, true, true, false));
 	}
 
 	/**
 	 * @testcreated 2011-09-28
 	 * @testpriority medium
 	 */
 	@Test
 	public void falseFirstTimeMatchesSecondMatch() {
 		final PatternSearchMatcher spm =
 			new PatternSearchMatcher("^.*$", false);
 		
 		assertMatch(1, 7,
 				spm.nextMatch("\nabbacd\nabbacd\n", true, true, false, false));
 	}
 	
 	/**
 	 * <p>Validates the behaviour of {@code firstTime} with reverse
 	 * matching. When {@code firstTime} is {@code true} the last possible
 	 * match must be returned.</p>
 	 * @testcreated 2011-09-22
 	 * @testpriority medium
 	 */
 	@Test
 	public void trueFirstTimeMatchesLastReverseMatch() {
 		final PatternSearchMatcher spm =
 	        new PatternSearchMatcher("^.*$", false);
 		
 		//			              111 1111111 2 2
 		//             012345 6789012 3456789 0 1
 	    String text = "abbacd\nabbacd\nabbacd\n\n";
 		
 	    Match m = spm.nextMatch(text, true, true, true, true);
 		assertMatch(1, 1, m);
 	    assertReverseMatch(text, 21, 21, m);		
 	}
 	
 	/**
 	 * <p>Opposite to {@link #trueFirstTimeMatchesFirstMatch()}.</p>
 	 * @testcreated 2011-09-22
 	 * @testpriority medium
 	 */
 	@Test
 	public void falseFirstTimeMatchesSecondToLastReverseMatch() {
 		final PatternSearchMatcher spm =
 			new PatternSearchMatcher("^.*$", false);
 
 		//			              111 1111111 2 2
 		//             012345 6789012 3456789 0 1
 		String text = "abbacd\nabbacd\nabbacd\n\n";
 		
 		assertReverseMatch(text, 14, 20,
 			spm.nextMatch(text, true, true, false, true));
 	}
 	
 	/**
 	 * <p>Tests if matcher can handle chars outside of ASCII charset.</p>
 	 * @testcreated 2011-10-11
 	 * @testpriority low
 	 */
 	@Test
 	public void matchesNonAscii() {
         final String text = "aoääffЩ௫𐍈";
         
        final PatternSearchMatcher spm1 =
                 new PatternSearchMatcher("ää", false);
         
        final PatternSearchMatcher spm2 =
                new PatternSearchMatcher("Щ௫", false);
        
         assertMatch(2, 4, 
        		spm1.nextMatch(text, true, true, true, false));
         
        assertMatch(4,6, 
                spm2.nextMatch(text, true, true, true, false));
 	}
 	
 }
