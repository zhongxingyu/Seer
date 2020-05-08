 package org.jboss.tools.switchyard.reddeer.debug;
 
import org.jboss.reddeer.swt.impl.toolbar.WorkbenchToolItem;
import org.jboss.reddeer.swt.matcher.WithMnemonicTextMatcher;
import org.jboss.reddeer.swt.matcher.WithRegexMatcher;
 
public class ResumeButton extends WorkbenchToolItem {
 
 	public ResumeButton() {
		super(new WithMnemonicTextMatcher(new WithRegexMatcher("Resume.*")));
 	}
 
 	@Override
 	public void click() {
 		if (!isEnabled()) {
 			throw new RuntimeException("Cannot click on 'Resume' button, it is NOT enabled!");
 		}
 		super.click();
 	}
 
 }
