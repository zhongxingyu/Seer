 package org.codefaces.ui.internal.editors;
 
 public class SupportEditor extends BrowserEditor {
 	public static final String ID = "org.codefaces.ui.editor.supportEditor";
 
	private static final String SUPPORT_PAGE_URL = "http://codefaces.com/support/";
	
	public SupportEditor(){
 		super(SUPPORT_PAGE_URL);
 		setTitleToolTip("Contact us");
 	}
 }
