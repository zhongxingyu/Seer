 /**
  * programming contest dufus
  */
 package com.barracuda;
 
 import org.apache.xmlrpc.webserver.WebServer;
 import org.apache.xmlrpc.server.PropertyHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcServer;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.lang.Math;
 import java.util.*;
 
 public class ContestServer {
     
     private int[][] board = new int[7][7];
    private int[][] own = new int[7][7];

    
     private static Log log = LogFactory.getLog(ContestServer.class);
 
     public Boolean ping() {
         log.info("ping");
         return true;
     }
 
     public int init_game(Map state) {
         log.info("init_game");
         log.info(state.toString());
         
        board = get_board(state, "board", 7, 7);
         log.info("Board: ");
         debug_table(board);
         
        own = get_board(state, "owned_squares", 2, 0);
        debug_table(own);
        
         return 0;
     }
 
     public Integer get_bid(List<Integer> offer, Map state) {
         log.info("get_bid");
         log.info(offer.toString());
         return new Integer(4);
     }
 
     public Integer make_choice(List<Integer> offer, Map state) {
         log.info("make_choice");
         return offer.get(0);
     }
 
     public int move_result(Map result) {
         log.info("move_result");
         log.info(result.toString());
         return 0;
     }
 
     public int game_result(Map result) {
         log.info("game_result");
         log.info(result.toString());
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
        int port = 9998;
         WebServer webServer = new WebServer(port);
         XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         phm.setVoidMethodEnabled(true);
 
         // class key = null for no prefix on method names
         phm.addHandler(null, ContestServer.class);
         xmlRpcServer.setHandlerMapping(phm);
 
         log.info("starting XML-RPC server at /RPC2 on port " + port);
         webServer.start();
         
     }
     
     public void debug_table(int[][] b) {
      
         String output = "" + '\n';
         for (int[] row : b) {
             for (int cell : row) {
                 output += cell + ", ";
             }
             output += '\n';
         }
         log.info(output);
     }
     
     //EFFECT: Returns 7x7 board
    public int[][] get_board(Map state, String type, int x, int y) {
         
        int[][] temp_board = new int[x][y];
         
        Object[] all = (Object[]) state.get(type);
        for( int i = 0; i < x; i++) {
             Object[] row = (Object[]) all[i];
            for( int j = 0; j < y; j++) {
                 temp_board[i][j] = (Integer) row[j];
             }
         }
         return temp_board;
     }
 }
