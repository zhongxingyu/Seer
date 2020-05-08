 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinSourceElementRequestor;
 import org.eclipse.dltk.ruby.internal.parser.RubySourceElementParser;
 
 public class RubyMixin implements IMixinParser {
	
	public final static String INSTANCE_SUFFIX = "%"; //suffix for instance classes 
	
	public final static String VIRTUAL_SUFFIX = "%v"; //suffix for virtual classes
	
 	private IMixinRequestor requestor;
	private RubySourceElementParser parser = new RubySourceElementParser(null, null);
	public void parserSourceModule(char[] contents, boolean signature, ISourceModule module) {
		MixinSourceElementRequestor sourceRequestor = new MixinSourceElementRequestor( requestor, signature, module );
 		parser.setRequirestor(sourceRequestor);
 		parser.parseSourceModule(contents, null);
 	}
 
 	public void setRequirestor(IMixinRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
