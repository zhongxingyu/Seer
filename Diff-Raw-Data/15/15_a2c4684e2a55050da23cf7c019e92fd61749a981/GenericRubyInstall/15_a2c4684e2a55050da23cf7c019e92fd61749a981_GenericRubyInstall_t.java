 package org.eclipse.dltk.ruby.internal.launching;
 
 import org.eclipse.dltk.launching.AbstractInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.IInterpreterRunner;
 
 public class GenericRubyInstall extends AbstractInterpreterInstall {
 
 	public GenericRubyInstall(IInterpreterInstallType type, String id) {
 		super(type, id);
 	}
 
 	public IInterpreterRunner getInterpreterRunner(String mode) {
 		return new RubyInterpreterRunner(this);
 	}

	public String getBuiltinModuleContent(String name) {
		if( name.equals("types.rb")) {
			return "class Object\n end\nclass Class\n end\n";
		}
		else if( name.equals("functions.rb") ) {
			return "def function\nend\n";
		}
		return null;
	}

	public String[] getBuiltinModules() {
		return new String[] {"types.rb", "functions.rb"};
	}
	
 }
