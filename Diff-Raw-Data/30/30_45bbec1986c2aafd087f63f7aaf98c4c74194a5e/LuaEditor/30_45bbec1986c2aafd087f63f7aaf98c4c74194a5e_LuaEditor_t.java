 package com.rytong.ui.editor.lua;
 
 import org.eclipse.ui.editors.text.TextEditor;
 
 import com.rytong.ui.editor.ColorManager;
 
 /**
 * Rewrite of LuaEditor without using DLTK API. 
  * @author lu.jingbo@rytong.com
  *
  */
 public class LuaEditor extends TextEditor {
 
 	private ColorManager colorManager;
    private LuaScanner scanner;
 
 	public LuaEditor() {
 		super();
       colorManager = new LuaColorManager();
 		setSourceViewerConfiguration(new LuaSourceViewerConfiguration(this));
 	}
 
 	@Override
 	public void dispose() {
    	colorManager.dispose();
 		super.dispose();
 	}
     
 	public LuaScanner getLuaScanner() {
 		if (scanner == null)
 			scanner = new LuaScanner(colorManager);
 		return scanner;
 	}
 
 }
