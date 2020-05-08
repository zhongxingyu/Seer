 package de.zib.gndms.infra.util;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.HashSet;
 
 /**
  * Logging decision points decide wether a certain type of log message should be emitted.
  *
  * @author Stefan Plantikow<plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 29.07.2008 Time: 18:04:37
  */
 public interface LoggingDecisionPoint {
 	boolean shouldLog(@NotNull String token);
 
 	final class Parser {
 		private Parser() {}
 

		@SuppressWarnings({ "StaticMethodOnlyUsedInOneClass" })
        public static @Nullable Set<String> parseTokenSet(@NotNull String s) {
 			try {
 				final StringTokenizer tokenizer = new StringTokenizer(s, ",");
 				Set<String> set = new HashSet<String>(tokenizer.countTokens());
 				while (tokenizer.hasMoreTokens()) {
 					@NotNull String candidate = tokenizer.nextToken().trim();
 					if (candidate.length() > 0 && candidate.charAt(0) == '!')
 						;
 					else
 						set.add(candidate);
 				}
 				return set;
 			}
 			catch (RuntimeException e) {
 				return null;
 			}
 		}
 	}
 }
