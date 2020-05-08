 package Countdown;
 
 import AndrewCassidy.PluggableBot.DefaultPlugin;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Andrew Cassidy
  */
 public class Countdown extends DefaultPlugin {
 
     private boolean GameRunning = false;
     private static final Random rng = new Random();
     private static final Integer[] numbers = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 25, 50, 75, 100 };
     private static final Integer[] bigNumbers = { 25, 50, 75, 100 };
     private static final Integer[] smallNumbers = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10 };
     private ArrayList<Integer> numbersToUse;
     private Integer target;
     private Timer tim = new Timer();
 //    private String channel;
     private AbstractSolver runningThread;
     private String lastPuzzle = null;
     private SolverResult lastResult = null;
 
     @Override
     public String getHelp() {
         return "Plays the countdown numbers game. !countdown starts the game, in which you must try and calculate the target result using the 6 given numbers in 30 seconds. Once the time is up, use !solution to get a solution.";
     }
 
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         String[] messageParts = message.split(" ", 2);
         if (messageParts[0] == null ? "!countdown" == null : messageParts[0].equals("!countdown"))
         {
             // game running? decline request
             if (GameRunning)
             {
                 super.bot.Message(channel, "A game is currently in progress.");
             }
             else
             {
                 // store the last solution if it exists
                if (runningThread != null && runningThread.GetResult() != null)
                 {
                     lastResult = runningThread.GetResult();
                 }
 
                 GameRunning = true;
 //                this.channel = channel;
 
                 // start a new game
 
                 int bigNums = -1;
                 if (messageParts.length == 2)
                 {
                     try {
                         bigNums = Integer.parseInt(messageParts[1]);
                         if (bigNums > 4 || bigNums < 0) throw new Exception();
                     }
                     catch (Exception e) {
                         bigNums = -1;
                         bot.Message(channel, "Invalid number of big numbers, must be between 0 and 4 inclusive. Using default random selection.");
                     }
                 }
 
                 StringBuffer buffer = new StringBuffer();
                 numbersToUse = new ArrayList<Integer>(6);
 
                 if (messageParts.length == 1 || bigNums == -1)
                 {
                     ArrayList<Integer> list = new ArrayList<Integer>(Arrays.asList(numbers));
 
                     for (int i = 0; i < 6; i++)
                     {
                         numbersToUse.add(list.remove(rng.nextInt(list.size())));
                         buffer.append(numbersToUse.get(i));
 
                         if (i < 5)
                             buffer.append(", ");
                     }
                 }
 
                 // otherwise number of big numbers to use
 
                 else
                 {
                     ArrayList<Integer> bigList = new ArrayList<Integer>(Arrays.asList(bigNumbers));
                     ArrayList<Integer> smallList = new ArrayList<Integer>(Arrays.asList(smallNumbers));
 
                     for (int i = 0; i < 6; i++)
                     {
                         if (i < bigNums)
                             numbersToUse.add(bigList.remove(rng.nextInt(bigList.size())));
                         else numbersToUse.add(smallList.remove(rng.nextInt(smallList.size())));
                         buffer.append(numbersToUse.get(i));
 
                         if (i < 5)
                             buffer.append(", ");
                     }
 
 
                 }
 
                 target = rng.nextInt(900) + 100;
 
                 buffer.append(" Target: ");
                 buffer.append(target);
 
                 lastPuzzle = buffer.toString();
 
                 bot.Message(channel, lastPuzzle);
 
                 final String channame = channel;
 
                 TimerTask task = new TimerTask() {
 
                     @Override
                     public void run() {
                         GameRunning = false;
                         bot.Message(channame, "Time's Up!");
                     }
                 };
                 
                 tim.schedule(task, 30000);
 
                 runningThread = new RandomiserSolver();
                 runningThread.Solve(numbersToUse, target);
             }
         }
         else if (messageParts[0] == null ? "!solution" == null : messageParts[0].equals("!solution"))
         {
             if (runningThread == null || runningThread.GetResult() == null)
             {
                 super.bot.Message(channel, "No games played.");
             }
             else if (GameRunning && lastResult == null)
             {
                 super.bot.Message(channel, "No games played.");
             }
             else if (GameRunning)
             {
                 super.bot.Message(channel, "Solution to the last game: " + lastResult.Solution + " Tested " + lastResult.SolutionsTested + " candidate solutions and took " + lastResult.TimeTaken + "ms");
             }
             else
             {
                 lastResult = runningThread.GetResult();
                 super.bot.Message(channel, "Solution to the last game: " + lastResult.Solution + " Tested " + lastResult.SolutionsTested + " candidate solutions and took " + lastResult.TimeTaken + "ms");
             }
         }
     }
 }
