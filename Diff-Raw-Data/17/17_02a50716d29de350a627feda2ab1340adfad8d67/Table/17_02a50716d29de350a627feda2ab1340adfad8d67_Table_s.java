 package py.edu.uca.fcyt.toluca.table;
 
 //import java.awt.*;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import py.edu.uca.fcyt.game.Card;
 import py.edu.uca.fcyt.game.ChatPanelContainer;
 import py.edu.uca.fcyt.toluca.RoomClient;
 import py.edu.uca.fcyt.toluca.TolucaConstants;
 import py.edu.uca.fcyt.toluca.event.RoomEvent;
 import py.edu.uca.fcyt.toluca.event.TableEvent;
 import py.edu.uca.fcyt.toluca.event.TableListener;
 import py.edu.uca.fcyt.toluca.event.TrucoEvent;
 import py.edu.uca.fcyt.toluca.event.TrucoListener;
 import py.edu.uca.fcyt.toluca.game.TrucoCard;
 import py.edu.uca.fcyt.toluca.game.TrucoGame;
 import py.edu.uca.fcyt.toluca.game.TrucoGameClient;
 import py.edu.uca.fcyt.toluca.game.TrucoPlay;
 import py.edu.uca.fcyt.toluca.game.TrucoPlayer;
 import py.edu.uca.fcyt.toluca.game.TrucoTeam;
 import py.edu.uca.fcyt.toluca.table.animation.Animator;
 
 /**
  * Maneja el juego de truco
  */
 public class Table implements PTableListener, ChatPanelContainer,
         ActionListener, WindowListener, ChangeListener {
     public static final int SIT = 0;
 
     public static final int PLAY = 1;
 
     public static final int PLAYCANTO = 2;
 
     public static final int CALL = 3;
 
     public static final int WAIT = 4;
 
     public static final int WATCH = 5;
 
     private int status; // estado actual
 
     private JFrame jFrame; // jFrame principal
 
     private TrucoTable trucoTable; // panel principal
 
     private PlayTable pTable; // mesa de juego
 
     private int playerCount; // cantidad de jugadores
 
     private CardManager cManager; // manejador de cartas
 
     private PopupTrucoPlays pTrucoPlays; // popup de jugadas
 
     private Vector players;
 
     private TrucoGameClient tGame; // juego de truco asociado
 
     private boolean host; // es o no el host
 
     private PlayerManager pManager; // manejador de jugadores
 
     /** El player que pertenece al login actual, o sea el "dueo" de este RoomClient */
     private TrucoPlayer playerLocal; 
 
     private int tableNumber; // n�mero de mesa
 
     private FaceManager fManager; // FaceManager asociado
 
     private Vector signs; // vector de se�as
 
     private Animator animator; // animador de objetos
 
     private Vector aPlays; // jugadas habilitadas
 
     protected int envidoPoints;
 
     private TTextAnimator ttAnimator;
 
     private TableFrame tFrame;
     
 
     // manejador de eventos de mesa
     protected TableEventManager tEventMan;
 
     // TrucoListener asociado
     protected TrListener trListener;
 
     private RoomClient room;
 
     private boolean esMiTurno;
 
     private int primerTurno = 0;
 
     public Vector getPlayers() {
         return players;
     }
 
     public int getTableNumber() {
         return tableNumber;
     }
 
     public void setTableNumber(int tableNumber) {
         this.tableNumber = tableNumber;
     }
 
     /** Crea un Table asociado con 'player' */
     public Table(TrucoPlayer player, boolean host) {
         playerLocal = player;
         this.host = host;
         players = new Vector(0, 1);
         playerCount = 6;
 
         // crea un nuevo manejador de eventos de tabla
         tEventMan = new TableEventManager(this);
 
         // crea el manejador de jugadores
         pManager = new PlayerManager(6);
     }
 
     /**
      * Inicializa los manejadores
      */
     public void initialize() {
         // inicializa el TrucoGame actual
         tGame = null;
 
         ttAnimator.clearAll();
         //ttAnimator.showPresentation();
 
         // establece el texto del bot�n
         trucoTable.buttons[TrucoTable.BUTTON_INICIAR_OK].setText("Iniciar");
         trucoTable.buttons[TrucoTable.BUTTON_INICIAR_OK].setEnabled(false);
 
         // inicializa el estado actual
         status = Table.SIT;
 
         // remueve manejador de cartas de la animaci�n
         animator.removeAnim(cManager);
 
         // remueve manejador de caritas
         animator.removeAnim(fManager);
 
         // inicializa el puntaje actual
         trucoTable.score.actualizarPuntaje(0, 0);
 
         // crea el manejador de caras y lo agrega al animador
         fManager = new FaceManager();
         animator.addAnim(fManager);
 
         // agrega 6 caritas sin nombre y las muestra
         fManager.addUnnamedFaces(6);
         fManager.hideFaces();
         fManager.pushGeneralPause(ttAnimator.getRemainingTime());
         fManager.showFaces();
 
         // crea el marco y lo agrega al animador
         tFrame = new TableFrame();
         animator.addAnim(tFrame);
     }
 
     public void initResources() {        
 
         // crea los objetos
         trucoTable = new TrucoTable();
         trucoTable.setTable(this);
         trucoTable.inicializar();
         pTable = trucoTable.getPlayTable();
         trListener = new TrListener(this);
         ttAnimator = new TTextAnimator();
 
         jFrame = new JFrame();
         jFrame.setTitle("Toluca: " + playerLocal.getName()
                 + (host ? " (host)" : ""));
         jFrame.getContentPane().add(trucoTable);
         jFrame.setSize(600, 500);
 
         animator = pTable.getAnimator();
         signs = new Vector(3, 0);
 
         // agrega los jugadores a la lista mostrada
         for (int i = 0; i < players.size(); i++)
             getJTrucoTable().addPlayer((TrucoPlayer) players.get(i));
         /*
          * trucoTable.getJpWatchers().addPlayer(((TrucoPlayer) players.get(i))
          * .getName());
          */
 
         animator.addAnim(ttAnimator);
 
         initialize();
 
         // agrega los nombres y las caritas de los players
         for (int i = 0; i < pManager.getPlayerCount(); i++) {
             Face face;
             TrucoPlayer player;
             face = fManager.getFace(i);
             player = pManager.getPlayer(i);
             if (player != null) {
                 face.setName(player.getName());
                 face
                         .loadFacesFromURL("/py/edu/uca/fcyt/toluca/images/faces/standard/");
             }
         }
 
         jFrame.addWindowListener(this);
         trucoTable.getChatPanel().setCpc(this);        
 
         Thread at = new Thread(animator, "animator");
         at.setPriority(Thread.MIN_PRIORITY);
         at.start();
     }
 
     public void showSystemMessage(String message) {
         TrucoPlayer trucoPlayer = new TrucoPlayer();
         trucoPlayer.setName("System");
         trucoTable.getChatPanel().showSystemMessage(message,
                 new String[] { "[", "]" });
 
     }
 
     /** Agrega el jugador 'player' a la mesa */
     public void addPlayer(TrucoPlayer player) {
         players.add(player);
         if (trucoTable != null)
             trucoTable.addPlayer(player);
         tEventMan.firePlayerJoined(player);
     }
 
     /**
      * Elimina al jugador 'player' de la mesa.
      * 
      * @return Verdadero si estaba en la mesa y se lo elimin�, de lo contrario
      *         falso.
      */
     public boolean removePlayer(TrucoPlayer player) {
         boolean ret;
         System.out.println(getClass().getName() + " removePlayer: ");
         ret = players.remove(player);
         System.out
                 .println(getClass().getName() + " removePlayer: ret = " + ret);
         trucoTable.removePlayer(player);
 
         if (ret && tGame != null && pManager.isSitted(player)) {
             System.out.println(getClass().getName()
                     + " removePlayer: tgame no es nulo y player sentado");
             System.out.println(getClass().getName()
                     + " removePlayer: deberia de mostrar el chat message");
             trucoTable.getChatPanel().showChatMessage(
                     new TrucoPlayer("[ System ]"),
                     "El juego ha finalizado debido a que el jugador "
                             + player.getName() + " lo ha abandonado.");
             initialize();
             tEventMan.fireGameFinished();
         }
 
         return ret;
     }
 
     /** Hecha a un jugador de la mesa */
     public void kickPlayer(TrucoPlayer player) {
         removePlayer(player);
         tEventMan.firePlayerKicked(player);
     }
 
     /** Retorna el PlayTable de esta mesa */
     PlayTable getPlayTable() {
         return pTable;
     }
 
     /** Retorna el TrucoListener de esta Table */
     public TrucoListener getTrucoListener() {
         return trListener;
     }
 
     /** Registra un TableListener */
     public void addTableListener(TableListener t) {
         tEventMan.addListener(t);
     }
 
     /** Registra un TrucoListener */
     public void addTrucoListener(TrucoListener t) {
         trListener.addListener(t);
     }
 
     /** Inicia el juego de esta mesa con Game 'game' */
     public void startGame(TrucoGameClient game) {
         long rem;
 
         // verificaciones
         Util.verifParam(playerLocal != null, "No hay jugador local");
         Util.verifParam(game != null, "TrucoGame nulo");
         Util.verifParam(players != null, "Vector de Players nulo");
 
         // actualiza el TrucoGame actual y carga su TrucoListener
         tGame = game;
         tGame.addTrucoListener(trListener);
 
         // reubica las caritas en pManager
         pManager.startGame();
 
         // crea un nuevo manejador de cartas y lo agrega al animador
         cManager = new CardManager(pManager.getPlayerCount());
         animator.addAnim(cManager);
 
         // quita todas las caritas
         fManager.pushGeneralPause(100);
         fManager.hideFaces();
         fManager.removeFaces();
 
         // agrega las caritas pero en el orden correcto
         fManager.addFaces(pManager, !pManager.isSitted(playerLocal));
         fManager.pushGeneralPause(100);
         fManager.hideFaces();
         rem = Math.max(cManager.getRemainigTime(), fManager.getRemaninigTime());
 
         fManager.pushGeneralPause(rem);
         fManager.showFaces();
 
         // limpia las se�as cargadas
         signs.clear();
 
         // manda al frente en el orden correcto
         animator.toTop(fManager);
         animator.toTop(ttAnimator);
         animator.toTop(tFrame);
 
         // avista que el juego inici� correctamente
         tEventMan.fireGameStarted();
     }
 
     /**
      * Sienta a un jugador en una silla
      * 
      * @param player
      *            Jugador
      * @param chair
      *            silla
      */
     public void sitPlayer(TrucoPlayer player, int chair) {
         //System.out.println("Mesa de " + getPlayer() + ": sentando a " +
         // player + " en la mesa " + chair);
         Face face;
         pManager.sitPlayer(player, chair);
 
         if (player.getName().equals(playerLocal.getName())) {
             playerLocal = player;
             pManager.setPlayerLocal(player);
         }
 
         if (fManager != null) {
             face = fManager.getFace(chair);
             face.setName(player.getName());
            face
                    .loadFacesFromURL("/py/edu/uca/fcyt/toluca/images/faces/standard/");
         }
 
         //		System.out.println("----------------- TABLA DE: " +
         // playerLocal.getName() + "-----------------");
         //		System.out.println("evenTeams: " + pManager.evenTeams());
         //		System.out.println("getActualChair: " + pManager.getActualChair());
         //		System.out.println("isSitted: " + pManager.isSitted(playerLocal));
 
         if (trucoTable != null)
             //			jtTable.buttons[TrucoTable.BUTTON_INICIAR_OK].setEnabled(pManager
             //					.evenTeams()
             //					&& (pManager.getActualChair() == 0)
             //					&& pManager.isSitted(playerLocal));
             trucoTable.enableAction(pManager, playerLocal);
         // avisa que le player se sent� correctamente
         tEventMan.firePlayerSit();
         StringBuffer sb = new StringBuffer();
         sb.append("Sentados: ");
         for (int i = 0; i < pManager.sittedPlayers.size(); i++) {            
             sb.append("silla(");
             sb.append(i);
             sb.append("): ");
             sb.append(pManager.sittedPlayers.get(i) != null ? pManager.sittedPlayers.get(i).toString() : ">> VACIA <<");
             sb.append("   ");
         }        
         TolucaConstants.log(TolucaConstants.CLIENT_DEBUG_LOG_LEVEL, sb.toString());
     }
 
     /**
      * Para a un jugador de la silla en donde est� sentado
      */
     public void standPlayer(int chair) {
         Face face;
         TrucoPlayer p;
 
         p = pManager.standPlayer(chair);
 
         if (fManager != null) {
             face = fManager.getFace(chair);
             face.setName("");
             face.loadFacesFromURL(null);
 
             trucoTable.buttons[TrucoTable.BUTTON_INICIAR_OK]
                     .setEnabled(pManager.evenTeams());
         }
 
         // avisa que el player se levant� correctamente
         tEventMan.firePlayerStanded(p);
     }
 
     /** Retorna el TrucoPlayer propietario de esta mesa */
     public TrucoPlayer getPlayer() {
         return playerLocal;
     }
 
     /**
      * Establece el estado actual de la mesa.
      * 
      * @param status
      *            nuevo estado de la mesa.
      */
     protected void setStatus(int status) {
         this.status = status;
     }
 
     /** Retorna el estado actual de la mesa. */
     protected int getStatus() {
         return status;
     }
 
     public void mouseClicked(int x, int y, MouseEvent e) {
         int pos;
         boolean getOut = false;
 
         if (status == SIT)
             sitIfClick(x, y);
         else {
             if (status == PLAYCANTO || status == PLAY) {
                 if (e.getButton() == MouseEvent.BUTTON1) {
                     Card card;
 
                     if (status == PLAYCANTO)
                         // si hay que responder alg�n canto,
                         // no jugar la carta
                         card = null;
                     else
                         // retorna la carta jugada si se jug�
                         card = cManager.playCardIfClick(x, y);
 
                     // si no se jug�...
                     if (card != null) {
                         setCursor(Cursor.WAIT_CURSOR);
                         tGame
                                 .play(new TrucoPlay(playerLocal,
                                         (byte) TrucoPlay.JUGAR_CARTA,
                                         (TrucoCard) card));
                     }
                 } else {
                     try {
                         new PopupTrucoPlays(this, aPlays, envidoPoints).show(
                                 pTable, e.getX(), e.getY());
                     } catch (NullPointerException ex) {
                     }
                 }
             }
 
             // abrir las cartas de alg�n jugador si
             // se presion� en ellas, si tampoco
             // ocurri� esto...
             pos = cManager.getClickCards(x, y);
             if (pos == -1) {
                 if (status != WATCH)
                     sendSignsIfClick(x, y);
             } else
                 showPlayed(pos);
         }
     }
 
     /**
      * Sienta o para al jugador actual en/de una silla si �sta fue
      * seleccionada
      */
     protected boolean sitIfClick(int x, int y) {
         int pos;
 
         // carga la posici�n clickeada
         pos = fManager.getPosIfClick(x, y);
 
         // si existe dicha posici�n...
         if (pos != -1) {
             TrucoPlayer player;
             int chair, localChair;
 
             // obtiene la silla en esa posici�n
             chair = pManager.getChair(pos);
 
             // obtiene el player sentado en esa silla
             player = pManager.getPlayer(chair);
 
             if (player == playerLocal) {
                 tEventMan.firePlayerStandRequest(chair);
                 return true;
             }
 
             // si hay otra persona sentada retornar con falso
             if (player != null)
                 return false;
 
             // obtiene la silla actual
             localChair = pManager.getChair(playerLocal);
 
             // si el TrucoPlayer actual ya est� sentado ret. con falso
             if (localChair != -1) {
                 tEventMan.firePlayerStandRequest(localChair);
                 tEventMan.firePlayerSitRequest(chair);
                 return true;
             }
 
             // dispara los eventos 'sitRequiested' de todos
             // los TableListeners registrados
             tEventMan.firePlayerSitRequest(chair);
             return true;
         }
 
         return false;
     }
 
     public void say(byte say) {
         String strPts;
         int intPts;
         boolean bad = true;
 
         setCursor(Cursor.WAIT_CURSOR);
         switch (say) {
         case TrucoPlay.CANTO_ENVIDO:
             intPts = envidoPoints;
             tGame.play(new TrucoPlay(playerLocal, (byte) say, intPts));
             break;
         default:
             tGame.play(new TrucoPlay(playerLocal, say));
             break;
         }
     }
 
     /**
      * Muestra las se�as a un cierto jugador si se clicke� en �l
      */
     protected boolean sendSignsIfClick(int x, int y) {
         int pos;
         TrucoPlayer player;
 
         // obtiene la posici�n de jugador al
         // cual se debe enviar las se�as
         pos = fManager.getPosIfClick(x, y);
 
         // si no se hizo click en ninguno, salir con falso
         if (pos == -1)
             return false;
 
         // si es la posici�n del contrincante, salir
         if (pos % 2 == 0)
             return false;
 
         // obtiene el jugador sentado en esa posici�n
         player = pManager.getPlayer(pManager.getChair(pos + 1));
 
         // si no hay se�as, informa a todos los
         // TableListeners registrados la se�a "seca"
         if (signs.size() == 0)
             tEventMan.fireSignSendRequest(player, Sign.SECA);
         // si las hay informa a todos los
         // TableListeners registrados las se�as
         else
             for (int i = 0; i < signs.size(); i++)
                 tEventMan.fireSignSendRequest(player, ((Integer) signs.get(i))
                         .intValue());
 
         return true;
     }
 
     /**
      * Muestra la se�a emitida por un cierto jugador
      */
     public void showSign(TableEvent event) {
         TrucoPlayer src, dest;
 
         // obtiene el TrucoPlayer emisor y el receptor
         src = event.getPlayer(0);
         dest = event.getPlayer(1);
 
         // muestra la se�a
         if (dest == playerLocal)
             fManager.showSign(pManager.getPos(src) - 1, event.getValue());
 
         // avisa que la se�a fue enviada correctamente
         tEventMan.fireSignSent(event.getPlayer(1));
     }
 
     /**
      * Agrega una se�a a emitir posteriormente
      */
     protected void addSign(int sign) {
         // verificaciones
         Util.verifParam(Sign.isSign(sign), "Se�a inv�lida");
 
         signs.add(new Integer(sign));
     }
 
     /**
      * Limpia las se�as
      */
     protected void clearSigns() {
         signs.clear();
     }
 
     public void sendChatMessage(TrucoPlayer player, String htmlMessage) {
         tEventMan.fireChatMessageRequested(player, htmlMessage);
     }
 
     public void showChatMessage(TrucoPlayer player, String htmlMessage) {
         int pos;
 
         // si el mensaje comienza con una barra, va s�lo para
         // el chat
         if (htmlMessage.startsWith("\\")) {
             htmlMessage = htmlMessage.substring(1, htmlMessage.length());
         } else if (pManager.isSitted(player) && fManager != null) {
             pos = pManager.getPos(player);
             fManager.pushText(pos, htmlMessage, false);
         }
 
         // muestra el mensaje de chat y
         // aviza que lleg� correctamente
         if (trucoTable != null)
             trucoTable.getChatPanel().showChatMessage(player, htmlMessage);
         tEventMan.fireChatMessageSent(player, htmlMessage);
     }
 
     public boolean isHost() {
         return host;
     }
 
     /** Retorna el animador */
     protected Animator getAnimator() {
         return animator;
     }
 
     /** Retorna el manejador de cartas */
     protected CardManager getCManager() {
         return cManager;
     }
 
     /** Retorna el manejador de jugadores */
     protected PlayerManager getPManager() {
         return pManager;
     }
 
     /** Retorna el manejador de caritas */
     protected FaceManager getFManager() {
         return fManager;
     }
 
     /**
      * Retorna el puntaje de un equipo
      * 
      * @param index
      *            �ndice del equipo
      */
     protected int getPoints(int index) {
         return tGame.getTeam(index).getPoints();
     }
 
     /**
      * Actualiza los puntajes de cada equipo
      * 
      * @param p0
      *            puntaje para el equipo 0
      * @param p1
      *            puntaje para el equipo 1
      */
     protected void updateScore(int p0, int p1) {
         trucoTable.score.actualizarPuntaje(p0, p1);
     }
 
     /**
      * Retorna el TrucoGame de esta mesa
      */
     public TrucoGame getTGame() {
         return tGame;
     }
 
     /**
      * Carga las jugadas habilitadas
      * 
      * @param aPlays
      *            Vector de Bytes de jugadas habilitadas
      */
     protected void setAllowedPlays(Vector aPlays) {
         this.aPlays = aPlays;
         loadPlays();
     }
 
     /**
      *  
      */
     private void loadPlays() {
         //getJTrucoTable().getJPbotonesJugadas().setVisible(false);
         getJTrucoTable().getJPbotonesJugadas().removeAll();
         if (esMiTurno && aPlays != null && !aPlays.isEmpty()) {
             byte play;
             String name;
 
             for (int i = 0; i < aPlays.size(); i++) {
                 play = ((Byte) aPlays.get(i)).byteValue();
                 name = Util.getPlayName(play);
 
                 if (envidoPoints != -1 && play == TrucoPlay.QUIERO
                         || play == TrucoPlay.CANTO_ENVIDO)
                     name = name + ", " + envidoPoints;
 
                 getJTrucoTable().getJPbotonesJugadas().add(
                         new ButtonPlayAction(this, play, name));
             }
             //getJTrucoTable().getJPbotonesJugadas().setVisible(true);
         }
         /*if (esPrimerTurno()) {
             //PP total - Ale
             jFrame.dispatchEvent(new ContainerEvent(getJTrucoTable(),
                     ContainerEvent.COMPONENT_RESIZED, getJTrucoTable()
                             .getJPbotonesJugadas()));
             primerTurno++;
         }*/
         getJTrucoTable().getJPbotonesJugadas().repaint();
     }
 
     /**
      * @return
      */
     private boolean esPrimerTurno() {
         //TODO No se porque entra dos veces asi q PP unicamente :((
         return primerTurno < 2;
     }
 
     public void actionPerformed(ActionEvent e) {
         String text, pName;
         JButton source;
         TrucoPlayer tPlayer;
 
         source = (JButton) e.getSource();
         text = source.getText();
         if (text.equals("Iniciar")) {
             source.setEnabled(false);
             tEventMan.fireGameStartRequest();
 
         } else if (text.equals("Ok")) {
             trucoTable.buttons[TrucoTable.BUTTON_INICIAR_OK].setEnabled(false);
             ttAnimator.clearAll();
             tGame.startHand(playerLocal);
         } else if (text.equals("Echar")) {
             new Thread() {
                 public void run() {
                     trucoTable.buttons[TrucoTable.BUTTON_HECHAR]
                             .setText("Confirmar");
                     Util.wait(this, 3000);
                     trucoTable.buttons[TrucoTable.BUTTON_HECHAR]
                             .setText("Echar");
                 }
             }.start();
         } else if (text.equals("Confirmar")) {
             trucoTable.buttons[TrucoTable.BUTTON_HECHAR].setText("Echar");
             //TODO Deprecada la hechada hasta nuevo aviso
             /*
              * pName = trucoTable.getJpWatchers().getSelection();
              * 
              * for (int i = 0; i < players.size(); i++) { tPlayer =
              * (TrucoPlayer) players.get(i); if (tPlayer != playerLocal &&
              * tPlayer.getName().equals(pName))
              * tEventMan.firePlayerKickRequest(tPlayer); }
              */
         } else if (text.equals("Ayuda")) {
             try {
                 getRoom().getRoomUING().getAppletContext().showDocument(new URL("http://www.truco.com.py/ayuda.html"), "_blank");
             } catch (MalformedURLException e1) {            
                 e1.printStackTrace(System.err);
             }
             /*ttAnimator.showIndications();
             new Thread() {
                 public void run() {
                     trucoTable.buttons[TrucoTable.BUTTON_AYUDA]
                             .setEnabled(false);
                     Util.wait(this, 20000);
                     trucoTable.buttons[TrucoTable.BUTTON_AYUDA]
                             .setEnabled(true);
                 }
             }.start();*/
         }
     }
 
     /**
      * Muestra las cartas de un player
      * 
      * @param chair
      *            Silla donde est� el player
      */
     private void showPlayed(int pos) {
         cManager.showPlayed(pos);
     }
 
     /**
      * Retorna la mesa de juego
      */
     public TrucoTable getJTrucoTable() {
         return trucoTable;
     }
 
     /**
      * Retorna el manejador de eventos de mesa
      */
     public TableEventManager getTEventMan() {
         return tEventMan;
     }
 
     /**
      * Retorna el {@link TrucoPlayer}sentado en una silla.
      * 
      * @param chair
      *            Indice de la silla.
      */
     public TrucoPlayer getPlayerInChair(int chair) {
         return pManager.getPlayer(chair);
     }
 
     /**
      * Retorna el {@link TrucoPlayer}sentado en una silla.
      * 
      * @param chair
      *            Indice de la silla.
      */
     public TrucoPlayer getPlayer(int chair) {
         return (TrucoPlayer) players.get(chair);
     }
 
     /** Retorna la cantidad de {@link TrucoPlayer}s agregados. */
     public int getPlayerCount() {
         return players.size();
     }
 
     /**
      * Crea un array de dos {@link TrucoTeam}s que se pueden posteriormente
      * utilizar para crear un {@link TrucoGame}
      * 
      * @return Un array de dos {@link TrucoTeam}s
      */
     public TrucoTeam[] createTeams() {
         TrucoTeam tTeams[];
         TrucoPlayer player;
 
         if (tGame != null) {
             tTeams = new TrucoTeam[] { tGame.getTeam(0), tGame.getTeam(1) };
 
             //			for (int i = 0; i < 2; i++)
             //			{
             //				System.out.println("TEAM: " + i);
             //				for (int j = 0; j < tTeams[i].getNumberOfPlayers(); j++)
             //					System.out.println("PLAYER: " + tTeams[i].getPlayerNumber(j));
             //			}
 
             return tTeams;
         }
 
         // se crean los teams
         tTeams = new TrucoTeam[] { new TrucoTeam("Rojo"), new TrucoTeam("Azul") };
 
         // se agregan los players a los teams
         for (int i = 0; i < pManager.getPlayerCount(); i++) {
             player = pManager.getPlayer(i);
             if (player != null)
                 tTeams[i % 2].addPlayer(player);
         }
 
         return tTeams;
     }
 
     public void windowClosing(WindowEvent e) {
         jFrame.remove(trucoTable);
         jFrame.dispose();
     }
 
     public void windowClosed(WindowEvent e) {
         tEventMan.firePlayerLeft();
         animator.stopAnimator();
     }
 
     /**
      * Retorna el {@link TrucoGame asociado}
      */
     public TrucoGame getTrucoGame() {
         return tGame;
     }
 
     public void windowOpened(WindowEvent e) {
     }
 
     public void windowIconified(WindowEvent e) {
     }
 
     public void windowDeiconified(WindowEvent e) {
     }
 
     public void windowActivated(WindowEvent e) {
     }
 
     public void windowDeactivated(WindowEvent e) {
     }
 
     public String getOrigin() {
         return String.valueOf(getTableNumber());
     }
 
     public void finish() {
         jFrame.dispose();
     }
 
     void flash(boolean on) {
         this.esMiTurno = on;
         tFrame.flash(on);
         loadPlays();        
     }
 
     protected TTextAnimator getTTextAnimator() {
         return ttAnimator;
     }
 
     protected void setCursor(int type) {
         if (pTable != null)
             pTable.setCursor(type);
     }
 
     public void stateChanged(ChangeEvent arg0) {
         animator.setDelay(1000 / trucoTable.actions.getValue());
     }
 
     public int getChair(TrucoPlayer p) {
         return pManager.getChair(p);
     }
 
     public TableBeanRepresentation getTableBeanRepresentation() {
         TableBeanRepresentation tableBean = new TableBeanRepresentation();
         tableBean.setId(getTableNumber());
         tableBean.setPlayer(getPlayer());
         return tableBean;
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see py.edu.uca.fcyt.game.ChatPanelContainer#sendChatMessage(py.edu.uca.fcyt.toluca.event.RoomEvent)
      */
     public void sendChatMessage(RoomEvent event) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * @param event
      */
     public void endOfGame(TrucoEvent event) {
         System.out.println(getClass().getName() + "End of game for player "
                 + getPlayer());
         getJTrucoTable().getJButton("Ayuda").setEnabled(true);
 
         if (getJTrucoTable() != null)
             getJTrucoTable().buttons[TrucoTable.BUTTON_INICIAR_OK]
                     .setEnabled(getPManager().evenTeams()
                             && (getPManager().getLocalChair() == 0)
                             && getPManager().isSitted(
                                     trListener.getAssociatedPlayer()));
 
         getJTrucoTable().getJButton("Ok").setText("Iniciar");
 
         getTEventMan().fireGameFinished();
         initialize();
         if (isHost())
             trucoTable.buttons[TrucoTable.BUTTON_INICIAR_OK].setEnabled(true);
 
     }
 
     /**
      * @return Returns the jFrame.
      */
     public JFrame getJFrame() {
         return jFrame;
     }
 
     /**
      * @param frame
      *            The jFrame to set.
      */
     public void setJFrame(JFrame frame) {
         jFrame = frame;
     }
 
     /**
      *  
      */
     public void selfKick() {
         if (!isHost()) {
             getJFrame().dispatchEvent(
                     new WindowEvent(getJFrame(), WindowEvent.WINDOW_CLOSING));
             System.out.println("Cerrando automaticamente la Mesa");
         }
     }
 
     /**
      * @param string
      * @return
      */
     public boolean isInside(String string) {
         boolean ret = false;
         Iterator iter = getPlayers().iterator();
         while (iter.hasNext()) {
             TrucoPlayer element = (TrucoPlayer) iter.next();
             if (element.getName().equals(string))
                 ret = true;
         }
         return ret;
     }
 
     /**
      * @param client
      */
     public void setRoom(RoomClient room) {
         this.room = room;
     }
 
     public RoomClient getRoom() {
         return room;
     }
 
     /**
      * @param playerClient
      */
     public void actualizarRanking(TrucoPlayer playerClient) {
         getJTrucoTable().getTableRanking().actualizarPuntaje(playerClient);
     }
 
     /**
      * @param team
      * @param val
      */
     public void addJugada(int team, String val) {
         String txt = getJTrucoTable().getJlSaying().getText();
         if (txt.trim().length() == 0) {
             txt = "<html>Canto: ";
         } else if (!txt.startsWith("<html>")) {
             txt = "<html>" + txt;
         }
         txt = txt + "<font color=\""
                 + (team == TrucoTeam.ROJO ? "ff0000" : "0000ff") + "\"> - "
                 + val + "</font>";
         getJTrucoTable().getJlSaying().setText(txt);
     }
 
     public void handStarted() {
         getJTrucoTable().getJlSaying().setText("Canto: ");
     }
 
     /**
      * @param b
      */
     public void setPrimerTurno(boolean b) {
         if (b)
             primerTurno = 0;
         else
             primerTurno = 2;
     }
 
     
 }
 
