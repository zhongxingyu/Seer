 package org.jboss.tools.switchyard.reddeer.debug;
 
import org.jboss.reddeer.swt.impl.toolbar.WorkbenchToolItem;
import org.jboss.reddeer.swt.matcher.WithMnemonicTextMatcher;
import org.jboss.reddeer.swt.matcher.WithRegexMatcher;
 
public class TerminateButton extends WorkbenchToolItem {
 
 	public TerminateButton() {
		super(new WithMnemonicTextMatcher(new WithRegexMatcher("Terminate.*")));
 	}
 
 }
