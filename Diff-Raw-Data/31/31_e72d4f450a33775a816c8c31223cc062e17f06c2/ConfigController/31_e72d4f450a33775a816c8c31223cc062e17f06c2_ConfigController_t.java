 package com.anthropomo.digitalrattle.controllers;
 
 import java.util.List;
 
 import com.anthropomo.digitalrattle.io.CommandFactory;
 import com.anthropomo.digitalrattle.io.ConfigEvent;
 import com.anthropomo.digitalrattle.io.ICommand;
 import com.anthropomo.digitalrattle.io.IEventPublisher;
 import com.anthropomo.digitalrattle.io.ScreenEvent.IScreenConsumer;
 
 public class ConfigController implements IScreenConsumer {
 	private int noteCols = 3; // With noteRows, determines number of squares in grid and therefore playable notes
 	private int noteRows = 5;
 	private int numNotes;
 	private int baseNote = 60; // Default: 60 Middle C
 	
 	private IEventPublisher eventPublisher;
 	private ConfigEvent config;
 		
     public ConfigController (CommandFactory factory, IEventPublisher eventPublisher) {
     	this.eventPublisher = eventPublisher;
         config = factory.getConfigEvent();
    	eventPublisher.registerSubscriber(this);
     }
     
     public void setConfig(float dpHeight, float dpWidth){
     	
     	int padding = 0;
     	
     	if (dpWidth >= 480) {  // Large  
             baseNote = 48;
             padding = 20;
         }
         
     	noteCols = (int) (dpWidth / (106+padding));
     	noteRows = (int) (dpHeight / (106+padding));
         numNotes = noteCols*noteRows;
         
         config.setConfig(baseNote, numNotes, noteRows, noteCols);
         
         eventPublisher.receiveCommand(config);
     }
     
 
 	@Override
 	public void onEvent(ICommand e) {
 		e.execute(this);
 	}
 
 
 	@Override
 	public void onScreenEvent(float screenHeight, float screenWidth, float screenHeightDp, float screenWidthDp) {
 		setConfig(screenHeightDp, screenWidthDp);
 	}
 }
