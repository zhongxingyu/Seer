 package server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.Card;
 import model.Game;
 import model.GameProperties;
 import model.Play;
 import model.Player;
 
 public class Server
 {
     private ServerSocket serverSocket;
     private List<Socket> sockets;
 
     private int currentPlayerID;
     private List<Player> players;
     private Map<Integer, PrintWriter> outs;
 
     private Game game;
     private Timer drawingCardsTimer;
 
     public Server()
     {
         currentPlayerID = 101;
         players = new ArrayList<Player>();
         outs = new HashMap<Integer, PrintWriter>();
     }
 
     public void startServer(int port) throws IOException
     {
         serverSocket = new ServerSocket(port);
         sockets = new ArrayList<Socket>();
 
         new Thread()
         {
             public void run()
             {
                 try
                 {
                     while (true)
                     {
                         final Socket incoming = serverSocket.accept();
                         sockets.add(incoming);
 
                         final BufferedReader in = new BufferedReader(
                                 new InputStreamReader(incoming.getInputStream()));
 
                         new Thread()
                         {
                             Player player;
 
                             public void run()
                             {
                                 try
                                 {
                                     synchronized (Server.this)
                                     {
                                         player = makePlayer();
                                         if (player == null)
                                             return;
 
                                         players.add(player);
                                         outs.put(player.ID, new PrintWriter(
                                                 incoming.getOutputStream(),
                                                 true));
                                         if (game != null)
                                             game.addPlayer(player);
                                         announce("ADDPLAYER",
                                                 Integer.toString(player.ID),
                                                 player.name);
                                         message(player, "YOU",
                                                 Integer.toString(player.ID));
                                     }
 
                                     while (true)
                                     {
                                         String line = in.readLine();
                                         if (line == null)
                                             break;
                                         processMessage(player, parse(line));
                                     }
                                 }
                                 catch (Exception e)
                                 {
                                     e.printStackTrace();
                                 }
                                 finally
                                 {
                                     if (player != null)
                                     {
                                         players.remove(player);
                                         outs.remove(player);
                                         if (game != null)
                                             game.removePlayer(player);
                                         announce("REMOVEPLAYER",
                                                 Integer.toString(player.ID));
                                     }
                                 }
                             }
 
                             Player makePlayer() throws IOException
                             {
                                 /* HELLO [name] */
                                 String[] decoded = parse(in.readLine());
                                 if (decoded.length != 2
                                         || !decoded[0].equals("HELLO"))
                                 {
                                     return null;
                                 }
                                 else
                                 {
                                     return new Player(currentPlayerID++,
                                             decoded[1]);
                                 }
                             }
                         }.start();
                     }
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace();
                 }
                 finally
                 {
                     close();
                 }
             }
         }.start();
     }
 
     public void close()
     {
         try
         {
             serverSocket.close();
             for (Socket socket : sockets)
                 socket.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     protected synchronized void processMessage(Player player, String... data)
     {
         String command = data[0];
         List<String> params = Arrays.asList(data).subList(1, data.length);
 
         System.out.println("Server received request: " + command + " - "
                 + params);
 
         if (command.equals("STARTGAME"))
         {
             /* STARTGAME [properties] */
             game = new Game(GameProperties.decode(params));
             game.addPlayers(players);
             announce(data);
             // TODO ask other players to verify?
         }
         else if (command.equals("STARTROUND"))
         {
             /* STARTROUND */
             if (game.canStartNewRound())
             {
                 long randomSeed = System.currentTimeMillis();
                 game.startRound(randomSeed);
                 announce(command, Long.toString(randomSeed));
 
                 /* Start drawing */
                 drawingCardsTimer = new Timer();
                 drawingCardsTimer.schedule(new TimerTask()
                 {
                     public void run()
                     {
                         int currentPlayerID = game.getCurrentPlayer().ID;
                        if (game.canDrawFromDeck(currentPlayerID))
                         {
                             game.drawFromDeck(currentPlayerID);
                             announce("DRAW", Integer.toString(currentPlayerID));
                         }
                         else
                         {
                             drawingCardsTimer.cancel();
                         }
                     }
                 }, 1000, 250);
             }
             // TODO ask other players to verify?
         }
         else
         {
             Play play = new Play(player.ID, Card.decodeCards(params));
             if (command.equals("SHOW"))
             {
                 /* SHOW [cards] */
                 if (game.canShowCards(play))
                 {
                     game.showCards(play);
                     announce(buildMessage(command, player, params));
                 }
             }
             else if (command.equals("MAKEKITTY"))
             {
                 /* MAKEKITTY [cards] */
                 game.makeKitty(play);
                 announce(buildMessage(command, player, params));
             }
             else if (command.equals("PLAY"))
             {
                 /* PLAY [cards] */
                 if (game.isSpecialPlay(play) && !game.allowedSpecialPlay(play))
                 {
                     message(player, "NOTIFICATION", "Invalid special play.");
                     Card minCard = game.minCard(play);
                     play = new Play(player.ID, Arrays.asList(minCard));
                 }
                 game.play(play);
                 announce(buildMessage(command, player,
                         Card.encodeCards(play.getCards())));
             }
         }
     }
 
     protected String[] buildMessage(String command, Player player,
             List<String> data)
     {
         List<String> args = new ArrayList<String>();
         args.add(command);
         args.add(Integer.toString(player.ID));
         args.addAll(data);
         return args.toArray(new String[0]);
     }
 
     protected void message(Player player, String... args)
     {
         PrintWriter out = outs.get(player.ID);
         for (String arg : args)
             out.print(arg.replace(" ", "\1") + " ");
         out.println();
     }
 
     protected void announce(String... args)
     {
         System.out.println("Server announcing " + Arrays.toString(args));
         for (Player player : players)
             message(player, args);
     }
 
     protected String[] parse(String line)
     {
         String[] data = line.split(" ");
         String[] decoded = new String[data.length];
         for (int i = 0; i < data.length; i++)
             decoded[i] = data[i].replace("\1", " ");
         return decoded;
     }
 }
