 package com.jpii.navalbattle.gui;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.gui.listeners.Focus;
 import com.jpii.navalbattle.renderer.Helper;
 
 public class BaseWindow extends com.jpii.gamekit.gui.BaseWindow{
 	private static final long serialVersionUID = 1L;
 	
 	public BaseWindow(){
 		super();
 	}
 	
 	public BaseWindow(int arg0, int arg1){
 		super(arg0, arg1);
 	}
 	
 	public void setDefaults(){
 		this.setIconImage(Helper.GUI_WINDOW_ICON);
 		setTitle("NavalBattle");
 		addWindowListener(Constants.closer);
 		addKeyListener(Constants.keys);
 		addFocusListener(new Focus(this));
		super.setDefaults();
 	}
 	
 }
