 package com.ionmarkgames.ys.client;
 
 import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.ionmarkgames.ys.client.ui.MessagePanel;
 import com.ionmarkgames.ys.client.ui.NameInqueryPanel;
 import com.ionmarkgames.ys.client.ui.UICallback;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class YS implements EntryPoint, UICallback<String> {
 
 	private RootPanel playArea = RootPanel.get("PlayArea");
 	private GameController controller;
 	
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
     	RootPanel.get("Loading").setVisible(false);
         playArea.setPixelSize(1020, 720); // 51 x 38
         NameInqueryPanel getName = new NameInqueryPanel(this);
        
        HTML pelcoLogo = new HTML("<div class='pelco_logo'>Pel Company ltd</div><div class='pelco_quote'><i>\"There's work to be done.\" - John Pel</i></div><br/><br/>");
        
        playArea.add(pelcoLogo);
         playArea.add(getName);
     }
     
     public void start() {
         playArea.clear();
         controller.nextLevel();
     }
 
 	public void done(String result) {
 	    controller = new GameController();
 		controller.setPlayerName(result);
 		playArea.clear();
 		
 		MessagePanel msg = new MessagePanel("Hello there " +controller.getPlayerName()+ " and welcome to the PelCo initiation home page! <br/><br/>#I will be your handler for the duration of these \"simulations\".  You may call me Broadus.<br/><br/>#I am sure you are eager to begin so lets dive right in.....<br/><br/>#First off you will notice that during the \"simulation\" your avatar will appear as <img src='images/brain.gif' />.<br/><br/>  #We apologize for this crude representation.  <br/>Plans are currently under way to present you as something more appealing.  <br/>Unfortunately due to temporal and monetary constraints these plans will most likely never come to fruition.", new UICallback<Boolean>() {
 			public void done(Boolean obj) {
 				start();
 			}
 
 			public void failed() {
 			}
 		});
 		msg.animate();
 	}
 
 	public void failed() {
 		// TODO: handle error?
 	}
 }
