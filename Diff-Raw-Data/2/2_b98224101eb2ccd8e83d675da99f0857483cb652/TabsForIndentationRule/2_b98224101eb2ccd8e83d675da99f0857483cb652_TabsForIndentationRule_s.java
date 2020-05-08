 package com.refactr.snitch.rules;
 
 import java.io.File;
 
 import com.refactr.snitch.SnitchResult;
 import com.refactr.snitch.Violation;
 
 public class TabsForIndentationRule extends AbstractRule {
 
 	@Override
 	public void check(final File f, final String line, final int i, final SnitchResult results) {
 		for (int j = 0; j < line.length(); j++) {
 			char c = line.charAt(j);
 			if (c == ' ') {
 				// don't complain about javadoc indentation
				if ((line.length() >= (j + 1)) && (line.charAt(j + 1) != '*')) {
 					results.addViolation(new Violation(f, i, "Line uses spaces for indentation"));
 				}
 				return;
 			} else if (c != '\t') {
 				return;
 			}
 		}
 	}
 }
