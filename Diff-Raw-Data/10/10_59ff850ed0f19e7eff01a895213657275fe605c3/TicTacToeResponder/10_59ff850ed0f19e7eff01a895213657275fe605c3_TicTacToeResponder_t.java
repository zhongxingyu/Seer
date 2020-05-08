 package com.cengage.apprentice.app.utils;
 
 import java.io.FileNotFoundException;
 
 import com.cengage.apprentice.app.main.OrionRequest;
 import com.cengage.apprentice.app.response.OrionResponse;
import com.cengage.apprentice.app.response.StatusCodeResponse;
 
 public class TicTacToeResponder implements Respondable {
    private static final String GAME_NEW = "/game/new";
     private static final String GAME_UPDATE = "/game/update";
     private TicTacToeController controller;
     private String rootDir;
 
     public TicTacToeResponder(TicTacToeController controller, String rootDir){
         this.controller = controller;
         this.rootDir = rootDir;
     }
 
     public OrionResponse respond(OrionRequest request)
             throws FileNotFoundException {
        if(request.getRoute().contains(GAME_NEW)){
             return controller.newGame();
         }
        return new StatusCodeResponse(500);
     }
 
 }
