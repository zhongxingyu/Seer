 /**
  * 
  */
 package com.example.kindle.winningfour.gui;
 
 import java.awt.Component;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import com.amazon.kindle.kindlet.KindletContext;
 import com.amazon.kindle.kindlet.event.KindleKeyCodes;
 import com.example.kindle.sm.State;
 import com.example.kindle.utils.DialogHelper;
 import com.example.kindle.utils.KeyboardHelper;
 import com.example.kindle.winningfour.App;
 import com.example.kindle.winningfour.AppResources;
 import com.example.kindle.winningfour.options.AppOptions;
 
 /**
  *
  */
 public class OptionsPage extends State
 {
 	OptionsPage(final KindletContext context, final ImagePanel parent, final String name)
 	{
 		super(name);
 		this.context = context;
 		this.active = false;
 		this.focusListener = new FocusListener()
 		{
 			public void focusLost(FocusEvent e)
 			{
 				OptionsPage.this.active = false;
 			}
 			
 			public void focusGained(FocusEvent e)
 			{
 				if (!OptionsPage.this.active)
 				{
 					OptionsPage.this.active = true;
 					App.pager.back();
 				}
 			}
 		};
 	}
 
 	public void enter()
 	{
 		super.enter();
 		
 		this.focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
 		this.focused.addFocusListener(this.focusListener);
 		
 		this.context.setTextOptionPane(App.opts.getTextOptionPane());
 
 		App.opts.revert();
 		
 		KeyboardHelper.simulateKey(this.focused, KindleKeyCodes.VK_TEXT);
 	}
 
 	public void leave()
 	{
 		super.leave();
 		this.focused.removeFocusListener(this.focusListener);
 
 		int opstat = App.opts.getStatus();
 		if (opstat == AppOptions.STATUS_DISPLAY_CHANGES)
 		{
 			App.opts.commit();
			App.gamer.getView().reset();
 		}
 		else if (opstat == AppOptions.STATUS_GAME_CHANGES)
 		{
 			if (!App.gamer.isStopped())
 			{
 				DialogHelper.confirm(App.bundle.getString(AppResources.KEY_CONFIRM_OPTIONS),
 						new Runnable() {
 							public void run() {
 								App.gamer.stop();
 								App.opts.commit();
 							}},
 						new Runnable() {
 							public void run() {
 								App.opts.revert();
 							}});
 			}
 			else
 			{
 				App.opts.commit();
 			}
 		}
 
 		this.context.setTextOptionPane(null);
 	}
 
 	public void destroy()
 	{
 		App.log("OptionsPage::destroy");
 
 		super.destroy();
 		
 		this.context = null;
 		
 		if (this.focusListener != null && this.focused != null)
 		{
 			this.focused.removeFocusListener(this.focusListener);
 			this.focusListener = null;
 			this.focused = null;
 		}
 
 		App.log("OptionsPage::destroy done");
 	}
 
 	private KindletContext context;
 	private boolean active;
 	private Component focused;
 	private FocusListener focusListener;
 }
