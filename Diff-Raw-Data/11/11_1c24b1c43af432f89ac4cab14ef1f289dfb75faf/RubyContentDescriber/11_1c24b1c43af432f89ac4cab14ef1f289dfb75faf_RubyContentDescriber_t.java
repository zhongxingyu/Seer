 package org.eclipse.dltk.ruby.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.ITextContentDescriber;
 
 public class RubyContentDescriber implements ITextContentDescriber {
 
 	public int describe(Reader contents, IContentDescription description)
 			throws IOException {
 		return VALID;
 	}
 
 	public int describe(InputStream contents, IContentDescription description)
 			throws IOException {
 		return VALID;
 	}
 
 	public QualifiedName[] getSupportedOptions() {
 		return null;
 	}
 }
