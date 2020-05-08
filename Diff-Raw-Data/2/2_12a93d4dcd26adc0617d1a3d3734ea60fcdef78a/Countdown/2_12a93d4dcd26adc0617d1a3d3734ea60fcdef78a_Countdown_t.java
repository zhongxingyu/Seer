 
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
     private Integer[] numbers = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 25, 50, 75, 100};
     private ArrayList<Integer> numbersToUse;
     private Integer target;
     private Timer tim = new Timer();
     private String channel;
     private AbstractSolver runningThread;
     private String lastPuzzle = null;
 
     @Override
     public String getHelp() {
         return "Plays the countdown numbers game. !countdown starts the game, in which you must try and calculate the target result using the 6 given numbers in 30 seconds. Once the time is up, use !solution to get a solution.";
     }
 
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         if (message.startsWith("!countdown"))
         {
             // game running? decline request
             if (GameRunning)
             {
                 super.bot.Message(channel, "A game is currently in progress.");
             }
             else
             {
                 GameRunning = true;
                 this.channel = channel;
 
                 // start a new game
                 ArrayList<Integer> list = new ArrayList<Integer>(Arrays.asList(numbers));
                 numbersToUse = new ArrayList<Integer>(6);
 
                 StringBuffer buffer = new StringBuffer();
 
                 for (int i = 0; i < 6; i++)
                 {
                     numbersToUse.add(list.remove(rng.nextInt(list.size())));
                     buffer.append(numbersToUse.get(i));
 
                     if (i < 5)
                         buffer.append(", ");
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
         else if (message.startsWith("!solution"))
         {
             if (GameRunning)
             {
                 super.bot.Message(channel, "A game is currently in progress.");
             }
            else if (runningThread == null || runningThread.GetResult() == null)
             {
                 super.bot.Message(channel, "No games played.");
             }
             else
             {
                 SolverResult res = runningThread.GetResult();
                 super.bot.Message(channel, res.Solution + " Tested " + res.SolutionsTested + " solutions and took " + res.TimeTaken + "ms");
             }
         }
     }
 }
