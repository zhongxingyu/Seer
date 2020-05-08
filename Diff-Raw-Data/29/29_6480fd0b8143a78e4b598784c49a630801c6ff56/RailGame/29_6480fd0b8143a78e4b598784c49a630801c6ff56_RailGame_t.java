 package edu.uwm.cs552;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observer;
 
 import javax.swing.SwingUtilities;
 
 /**
  * All the information about a game.
  */
 public final class RailGame {
 
   public static Color[] playerColors = { Color.BLUE, Color.GREEN.darker(),
       Color.MAGENTA.darker(), Color.YELLOW.darker(), Color.DARK_GRAY, };
 
   public static final double STARTUP = 50;
 
   private final HexBoard board;
   private final Player[] players;
   private Map<HexEdge, Rail> rails;
 
   /**
    * Create a game using the given board with teh given number of players
    * 
    * @param b board to use
    * @param numPlayers number of players to allocate
    */
   public RailGame(HexBoard b, int numPlayers) {
     if (b == null) {
       throw new IllegalArgumentException("Game requires a non-null board");
     }
     if (numPlayers <= 0 || numPlayers > playerColors.length) {
       throw new IllegalArgumentException("bad number of players: " + numPlayers);
     }
     board = b;
     players = new Player[numPlayers];
     for (int i = 0; i < players.length; ++i) {
       players[i] = new Player(this, i + 1, playerColors[i], STARTUP);
     }
     rails = new HashMap<HexEdge, Rail>();
   }
 
   // TODO: Add methods so people can ask things about the board
   // or about players' activities on the board. Also write
   // a method to top up time.
 
   /**
    * Draw all players' actions according to a particular player's perspective.
    * We assume the game board is already drawn. If the observer is null, then
    * use the omniscient perspective, which sees everything.
    * 
    * @param g graphics context to be used, must be a graphics2D object
    * @param scale scale to draw board at
    * @param observer perspective to take
    */
   public void draw(Graphics g, double scale, Player observer) {
     for (Map.Entry<HexEdge, Rail> rail : rails.entrySet()) {
       g.setColor(rail.getValue().getPlayer().getColor());
       rail.getValue().draw(g, scale, rail.getKey());
     }
 
     if (observer != null) {
       doDraw(g, scale, observer);
     }
     else {
       for (Player player : players) {
         doDraw(g, scale, player);
       }
     }
 
     for (Player player : players)
       player.getTrain().draw(g, scale);
   }
 
   private void doDraw(Graphics g, double scale, Player player) {
     Color oldColor = g.getColor();
     g.setColor(player.getColor());
 
     for (HexCoordinate coordinate : player.getAllTrainIntent())
       g.fillPolygon(coordinate.toPolygon(scale));
     for (HexEdge edge : player.getAllProposedTrack())
       RailState.PROPOSED.draw((Graphics2D) g, edge, scale);
 
     g.setColor(oldColor);
   }
 
   public Terrain getTerrain(HexCoordinate coordinate) {
     return board.getTerrain(coordinate);
   }
 
   public Rail getRail(HexEdge edge) {
     return rails.get(edge);
   }
 
   // Fake being an observable
 
   public void addObserver(Observer arg0) {
     for (Player p : players) {
       p.addObserver(arg0);
     }
   }
 
   public void deleteObserver(Observer arg0) {
     for (Player p : players) {
       p.deleteObserver(arg0);
     }
   }
 
   public void deleteObservers() {
     for (Player p : players) {
       p.deleteObservers();
     }
   }
 
   public void updateTurnState() {
     int playerCount = 0;
     for (Player p : players) {
       if (p.getTurns() < 10)
         playerCount++;
     }
 
     if (playerCount > (players.length / 2))
       for (Player p : players)
         p.setTurns(p.getTurns() + 1);
   }
 
   public void doTurn(Player player) {

    doTrainMove(player);
    doAdvanceTrackConstruction(player);

    updateTurnState();

    for (Player p : players)
      p.notifyObservers();
  }

  private void doAdvanceTrackConstruction(Player player) {
     for (Map.Entry<HexEdge, Rail> entry : rails.entrySet()) {
       if (entry.getValue().getPlayer().equals(player))
         entry.getValue().advanceConstruction();
     }
 
     HexEdge newRail = player.getNextProposedTrack();
     while (rails.get(newRail) != null && newRail != null)
       newRail = player.getNextProposedTrack();
 
     if (newRail != null) {
       // TODO : tax player for cost of rail
       player.setCash(player.getCash() - 1);
       rails.put(newRail, new Rail(player));
     }
  }
 
  private void doTrainMove(Player player) {
     int moveStatus = -1;
     HexCoordinate intent = null;
     do {
       intent = player.getNextTrainIntent();
       moveStatus = player.getTrain().canMove(intent);
     } while (intent != null && moveStatus < 0);
 
     if (moveStatus == 0)
       player.putBackTrainIntent(intent);
     else if (moveStatus == 1)
       player.getTrain().move(intent);
   }
 
   public static void main(String[] args) {
     if (args.length != 2) {
       System.err
           .println("RailGame requires two arguments: game-board and number of players");
       System.exit(1);
     }
     final HexBoard board = new HexBoard();
     try {
       board.read(new BufferedReader(new FileReader(args[0])));
       final int numPlayers = Integer.parseInt(args[1]);
       final RailGame game = new RailGame(board, numPlayers);
       SwingUtilities.invokeLater(new Runnable() {
         public void run() {
           PlayerFrame main = new PlayerFrame(game, null, board);
           main.setVisible(true);
           for (int i = 0; i < numPlayers; ++i) {
             new PlayerFrame(game, game.getPlayer(i), board).setVisible(true);
           }
         }
       });
     } catch (Exception e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1);
     }
   }
 
   protected Player getPlayer(int i) {
     return players[i];
   }
 
 }
