 package org.eclipse.dltk.ruby.core;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.dltk.core.ScriptContentDescriber;
 
 public class RubyContentDescriber extends ScriptContentDescriber {
 	protected static Pattern[] header_patterns = { Pattern.compile(
			"^#!.*ruby.*", Pattern.MULTILINE) }; //$NON-NLS-1$
 
 	public int describe(Reader contents, IContentDescription description)
 			throws IOException {
 		if (checkPatterns(contents, header_patterns, null)) {
 			if (description != null) {
 				description.setProperty(DLTK_VALID, TRUE);
 			}
 			return VALID;
 		}
 		return INDETERMINATE;
 	}
 }
