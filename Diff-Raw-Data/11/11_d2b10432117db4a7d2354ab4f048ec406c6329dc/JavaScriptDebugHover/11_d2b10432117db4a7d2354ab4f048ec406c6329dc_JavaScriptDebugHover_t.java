 package org.eclipse.dltk.javascript.internal.debug.ui;
 
import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.debug.ui.ScriptDebugModelPresentation;
 import org.eclipse.dltk.internal.debug.ui.ScriptDebugHover;
import org.eclipse.dltk.internal.javascript.typeinference.FakeField;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class JavaScriptDebugHover extends ScriptDebugHover{
 
 	protected ScriptDebugModelPresentation getModelPresentation() {
 		return new JavaScriptDebugModelPresentation();
 	}
 
 	public void setPreferenceStore(IPreferenceStore store) {
 		
 	}
	protected String getFieldProperty(IField field) {
		if( field instanceof FakeField ) {
			return ((FakeField)field).getSnippet();
		}
		return super.getFieldProperty(field);
	}
 }
