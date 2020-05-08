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
 }
