 package com.example.spaceshipgame.renderer;
 
 import android.content.Context;
 import android.graphics.Canvas;
 
 import com.example.spaceshipgame.model.*;
 
 public class MainRenderer {
 	Context context;
 	
 	public MainRenderer(Context context_) {
		context=context_;
 	}
 	
 	public void render(Canvas canvas, GameState gameState) {
 		for(Player player : gameState.players){
 			for(Element element: player.elements){
 				ElementRenderer elementRenderer = ElementRendererFactory.getElementRenderer(element);
 				elementRenderer.render(canvas, element, context);
 			}
 		}	
 	}
 }
