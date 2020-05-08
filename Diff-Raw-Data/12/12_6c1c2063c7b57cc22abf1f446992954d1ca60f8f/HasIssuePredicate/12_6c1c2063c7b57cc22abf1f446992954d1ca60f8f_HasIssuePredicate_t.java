 package com.atlassian.jira.ext.commitacceptance.server.evaluator.predicate;
 
 import java.util.Set;
 
 import com.atlassian.jira.ext.commitacceptance.server.exception.AcceptanceException;
 
 /**
  * A set of issues passed to this predicate should contain at least one issue. If it's not true,
  * <code>AcceptanceException</code> will be thrown.
  *
  * @author <a href="mailto:ferenc.kiss@midori.hu">Ferenc Kiss</a>
  * @version $Id$
  */
 public class HasIssuePredicate implements JiraPredicate {
 	public void evaluate(Set issues) {
 		if (issues.isEmpty()) {
			throw new AcceptanceException("Commit message must contain at least one valid issue key.");
 		}
 	}
 }
