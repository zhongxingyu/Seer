 /**
  * Copyright (c) 2012, Team Votefuckers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy 
  * of this software and associated documentation files (the "Software"), to deal 
  * in the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
  * THE SOFTWARE.
  */
 
 package net.votefucker.nkvoter;
 
 import net.votefucker.nkvoter.applet.ConsoleApplet;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Scanner;
 import java.io.*;
 import java.net.URL;
 import java.util.NoSuchElementException;
 import net.votefucker.nkvoter.core.PollDaddyVoteStrategyFactory;
 import net.votefucker.nkvoter.core.VoteDispatcher;
 import net.votefucker.nkvoter.core.VoteEngine;
 import net.votefucker.nkvoter.core.listeners.BasicListener;
 import net.votefucker.nkvoter.io.SocketFactory;
 import net.votefucker.nkvoter.io.impl.NormalSocketFactory;
 import net.votefucker.nkvoter.io.impl.ProxySocketFactory;
 import net.votefucker.nkvoter.io.impl.TorSocketFactory;
 import net.votefucker.nkvoter.task.TaskManager;
 import net.votefucker.nkvoter.task.impl.DispatchVotesTask;
 import net.votefucker.nkvoter.task.impl.PulseEngineTask;
 
 
 public final class Main {
     
     /**
      * The maximum amount of votes.
      */
     private static final int MAXIMUM_VOTES = 50;
     
     /**
      * The delay between dumping the maximum amount of votes.
      */
     private static final long DELAY_BETWEEN_DUMPS = 10 * 60 * 1000;
     
     /**
      * The version of NKVoter.
      */
     private static final Version VERSION = new Version(1, 2, 3);
     
     private static VoteEngine engine;
     private static  PollDaddyVoteStrategyFactory strategyFactory;
     private static TaskManager taskManager;
     private static BasicListener listener;
     public static ConsoleApplet voteConsole;
     
     /**
      * The main entry point of the program.
      * 
      * @param args  The command line arguments.
      */
     
     public static void main(String [] args) throws Exception {
         
         
         System.out.println("" 
                          + " _   _ _  __ __     _____ _____ _____ ____                        \n"
                          + "| \\ | | |/ / \\ \\   / / _ |_   _| ____|  _ \\    Created by     \n"
                          + "|  \\| | ' /   \\ \\ / | | | || | |  _| | |_) |   Team VoteFuckers\n"
                          + "| |\\  | . \\    \\ V /| |_| || | | |___|  _ <                    \n"
                          + "|_| \\_|_|\\_\\    \\_/  \\___/ |_| |_____|_| \\_\\               \n"
                          + "                                                                  \n"
                          + "CREDITS to Kim Jong Un, Sini, Bla, Onon, Brother, Pholey, John,   \n"
                          + "           Orion, TheFeel, Drunkenevil, #opfuckmorsy              \n"
                          + "                                                                  \n"
                          + "(" + VERSION + ")                                                 \n"
                          + "==================================================================");
         System.out.println("NOTICE: THIS PROGRAM WILL SLEEP FOR 10 MINUTES BETWEEN VOTE BURSTS");
         
             boolean useNormal = true;
             boolean useTor = voteConsole.isTor();
             System.out.println(useTor);
         
         if(useTor)
         {
             useNormal = false;
         }
         
         strategyFactory = new PollDaddyVoteStrategyFactory();
         engine = NKVoter.getSingleton().getEngine();
         taskManager = NKVoter.getSingleton().getTaskManager();
         listener = new BasicListener();
         
         
         if(useTor) {
             TorSocketFactory socketFactory = new TorSocketFactory();
             VoteDispatcher dispatcher = new VoteDispatcher(socketFactory, strategyFactory.createStrategy("KJU"));
             
             setupDispatchTasks("TOR", socketFactory);
         }
 
         if(useNormal) {
             NormalSocketFactory socketFactory = new NormalSocketFactory();
             VoteDispatcher dispatcher = new VoteDispatcher(socketFactory, strategyFactory.createStrategy("KJU"));
             
             setupDispatchTasks("NORMAL", socketFactory);
         }
         
        
         
         taskManager.submit(new PulseEngineTask(DELAY_BETWEEN_DUMPS, engine));
     }
     
     private static void setupDispatchTasks(String dispatcher_type, SocketFactory sockf)
     {
         String[] candidates = {"KJU", "Jon", "Undoc", "Stephen", "Gabrielle", "Aung", "Christie", "Hillary", "AiWeiwei", "Morsi", "Assad", "ELJames", "Goodell", "Adelson", "Fluke"};
         String[] candidates_anew = {"KJU", "Jon", "Undoc", "Gabrielle", "Aung", "Stephen", "Christie", "Hillary", "AiWeiwei", "Morsi", "Assad", "ELJames", "Goodell", "Adelson"};
         int[] votesPerCandidate = {50, 45, 40, 35, 30, 25, 23, 21, 19, 16, 15, 13, 11, 9, 4};
          try {
             URL url = new URL("http://www.stullig.com/nkfiles/numbers.txt");
             Scanner s = new Scanner(url.openStream());
             String txt = s.nextLine();
             String txtVotes[] = txt.split(",");
             votesPerCandidate = new int[txtVotes.length];
             for(int i=0; i<votesPerCandidate.length; i++) {
                 try {
                 votesPerCandidate[i] = Integer.parseInt(txtVotes[i]);
                 }catch(NumberFormatException en) {}
             }
         
          }catch(IOException ex){
              System.out.println("Wasn't able to retrieve votesPerCandidate values from the server; using defaults.");
          }catch(NoSuchElementException nse){
              System.out.println("Wasn't able to retrieve votesPerCandidate values from the server; using defaults.");
          }
         
         for(int i = 0; i < candidates_anew.length; ++i)
         {
             VoteDispatcher dispatcher = new VoteDispatcher(sockf, strategyFactory.createStrategy(candidates_anew[i]));
             engine.add(dispatcher);
 
             DispatchVotesTask task = new DispatchVotesTask(DELAY_BETWEEN_DUMPS, dispatcher, votesPerCandidate[i]);
             task.addWorkerListener(listener);
             taskManager.submit(task);
         }
     }
 }
