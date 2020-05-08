 
 package com.vu.se.hm.net;
 
 import com.vu.se.hm.gui.HangmanEvent;
 import com.vu.se.hm.gui.HangmanEventListener;
 import com.vu.se.hm.service.*;
 import com.vu.se.hm.service.impl.WordGuesserImpl;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * GuesserServer. This is the class called by the host player.
  */
 public class GuesserServer implements WordGuesser, Runnable{
     private List listeners;
     private ConcurrentLinkedQueue queue;
     private WordGuesser guesser;
     private List players;
 
     
     public GuesserServer(String word){
         this.listeners = new ArrayList();
         this.guesser = new WordGuesserImpl(word);
         this.queue = new ConcurrentLinkedQueue();
         this.players = new ArrayList();
     }
     
     /**
      * addPlayer. this method is called for each potential player that we want the
      * server to listen for.
      */
     public void addPlayer(){
         SocketHandler temp = new SocketHandler(queue, 1234);
         (new Thread(temp)).start(); 
         players.add(temp);
     }
     
     /**
      * Close. this close all appropriate sockets for the server and individual sockethandlers;
      */
     public void close(){
         Iterator i = players.iterator();
         SocketHandler player;
         while(i.hasNext()){
             player = (SocketHandler) i.next();
             if(player.isConnected()){
                 player.close();
             }
         }
     }
     
     /**
      * sendData. this Class tells each socketHandler to send a pack to the respective player
      * @param packet 
      */
     public void sendData(HangmanPacket packet){
         packet.disguisedWord = guesser.getDisguisedWord();
         packet.missCount = guesser.getMissCount();
         Iterator i = players.iterator();
        SocketHandler player;
         while(i.hasNext()){
            player = (SocketHandler)i.next();
            if(player.isConnected()){
                player.sendData(packet);
            }
         }
     }
     
     /**
      * run(). Called when the thread is started. Continually checks queue for packets
      * then process them.
      */
     @Override
     public void run(){
         HangmanPacket packet;
         while(!isGameOver()){
             if(!queue.isEmpty()){
                 packet = (HangmanPacket)queue.remove();
                 guess(packet.letter); // Takes letter from player and Guesses that letter
                 fireEvent(packet.letter); //Update interface
                 sendData(packet); //Sends a return packet with updated information to each player
             }
         }
     }
     
     /**
      * addEventListener. Adds listeners who will respond when the server gets new data.
      * @param listener HangmanEventListner to add to this class
      */
     public synchronized void addEventListener(HangmanEventListener listener){
         listeners.add(listener);
     }
     
     /**
      * removeEventListener. 
      * @param listener HangmanEventListener to be removed from this class
      */
     public synchronized void removeEventListener(HangmanEventListener listener){
         listeners.remove(listener);
     }
     
     /**
      * fireEvent. This event is fired every time the server receives new guesses.
      * @param letter 
      */
     private synchronized void fireEvent(char letter){
         HangmanEvent event = new HangmanEvent(this, HangmanEvent.EventType.UPDATE, letter);
         Iterator i = listeners.iterator();
         while(i.hasNext()){
             ((HangmanEventListener) i.next()).handleHangmanEvent(event);
         }
     }
 
     @Override
     public String guess(char letter) {
         return guesser.guess(letter);
     }
 
     @Override
     public String getDisguisedWord() {
         return guesser.getDisguisedWord();
     }
 
     @Override
     public int getMissCount() {
         return guesser.getMissCount();
     }
 
     @Override
     public Set<Character> getLettersGuessed() {
         return guesser.getLettersGuessed();
     }
     
     @Override
     public String getSecretWord() {
             return guesser.getSecretWord();
     }
 
     @Override
     public boolean isFound() {
         return guesser.isFound();
     }
 
     @Override
     public int getGuessCount() {
         return guesser.getGuessCount();
     }
 
     @Override
     public boolean isGameOver() {
         return guesser.isGameOver();
     }
 
     @Override
     public boolean isWrongGuess() {
         return guesser.isWrongGuess();
     }
 }
