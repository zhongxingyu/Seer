 package tttmiddleware.interfaces;
 
 public interface TTTFactory {
     Board getBoard(String type);
    Player getPlayer(String type, String mark);
     Game getGame(String id, Board board, Player player1, Player player2);
 }
 
 // org.andrewzures.tttmiddleware.interfaces.TTTFactory factory = (org.andrewzures.tttmiddleware.interfaces.TTTFactory)org.jruby.Ruby.currentRuby.executeScriptlet("require 'jfactory'; JFactory,new");
