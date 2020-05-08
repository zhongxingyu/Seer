 package xephyrus.sam.machines.penneysgame;
 
 import xephyrus.sam.core.CompletionListener;
 import xephyrus.sam.core.queue.MemoryProcessingQueue;
 
 public class PenneysGame
   implements CompletionListener<PenneysState,PenneysPayload>
 {
   static public void main (String[] args)
   {
     try
     {
       if (args.length < 2)
       {
         System.out.println("PenneysGame usage:");
         System.out.println("  java -jar penneysgame.jar {player1pattern} {player2pattern}\n");
         System.out.println("Each pattern can consist of any number of H's (for heads) or T's ");
         System.out.println("(for tails), however each pattern must be the same length.");
       }
       else
       {
         PenneysGame game = new PenneysGame();
         game.play(args[0],args[1]);
         game.waitWhilePlaying();
       }
     }
     catch (NoSuchMethodException e)
     {
       System.out.println("ERROR: " + e.toString());
     }
   }
 
   public PenneysGame ()
       throws NoSuchMethodException
   {
     _machine = new PenneysMachine();
     _machine.setProcessingQueue(new MemoryProcessingQueue<PenneysState,PenneysPayload>());
     _machine.addCompletionListener(this);
     _machine.start();
   }
 
   public void play (String player1Pattern, String player2Pattern)
   {
     System.out.println("Player 1's pattern: " + player1Pattern);
     System.out.println("Player 2's pattern: " + player2Pattern);
     System.out.println("\nPlaying...\n");
 
     _machine.process(new PenneysPayload(player1Pattern,player2Pattern));
   }
 
   public void waitWhilePlaying ()
   {
     try
     {
       _machine.join(10000L);
     }
     catch (InterruptedException ignore)
     {
     }
   }
 
   @Override
  public void notifyComplete (PenneysPayload payload, PenneysState lastState, Exception error)
   {
     if (error != null)
     {
       System.out.println("ERROR: " + error.toString());
     }
     else
     {
       System.out.println("Flip pattern was: " + payload.getFlips());
       System.out.println("Victor was: Player " + payload.getVictor());
     }
     _machine.requestStop();
   }
 
   private PenneysMachine _machine;
 }
