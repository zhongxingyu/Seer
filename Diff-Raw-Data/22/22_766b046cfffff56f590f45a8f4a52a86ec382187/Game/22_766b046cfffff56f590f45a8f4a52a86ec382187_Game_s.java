 package fap_java;
 
 import animations.Animation;
 
 import animations.PauseCountDown;
 
 import characters.*;
 
 import gui.Constants;
 
 import gui.Fapplication;
 import gui.GameSave;
 import gui.NeedingFocus;
 
 import gui.PlayerSelect;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 
 import java.awt.Graphics2D;
 
 import java.util.ArrayList;
 
 import java.util.HashMap;
 
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import npcs.NPCWMBlocking;
 import npcs.NPCWMStarting;
 
 import npcs.actions.ADisplayMessage;
 import npcs.actions.ASetSwitch;
 import npcs.actions.AStartGame;
 import npcs.actions.ATestSwitch;
 import npcs.actions.Action;
 
 public class Game extends JPanel implements NeedingFocus {
 
     /**
      * The map of this Game. Contains a grid of cells and a set of methods to play with them
      */
     private transient CMap map;
     /**
      * The thread permanently refreshes the game. Updates healthpoints, positions of players, animations, etc...
      */
     private transient TheThread thread;
     /**
      * This contains all the player of this game. See also fap_java.Player
      */
     private transient ArrayList<Player> players = new ArrayList<Player>();
     /**
      * The Key Listener that will handle player displacements and pause
      */
     private transient KListener kl;
     /**
      * The scorebars manager
      */
     private transient ScoreBar scoreHandler;
     /**
      * Contains every team of the game
      */
     private transient ArrayList<Team> teams = new ArrayList<Team>();
     
     /**
      * How much points does it take to win the game
      */
     private int victScore;
     /**
      * How much percent of total cells of the map does it take to win the game
      */
     private double victTile;    // in percent
     /**
      * Time before automatically losing the game
      */
     private int victTime; // in sec
     
     /**
      * A boolean indicating if the game is still on or if someone won
      */
     private boolean gameEnded;
     
     /**
      * The Objects of the game, such as arrows
      */
     private ArrayList<Element> objects = new ArrayList<Element>();
     /**
      * The animations of the game, such as explosions
      */
     private ArrayList<Animation> anims = new ArrayList<Animation>();
     
     //Parameters to be given when starting a new game
     /**
      * A String indicating wich player is in the game and their skills. Ex : "1,8,0,4"
      */
     private String whoIsPlaying; // This is linked with skill, here player 1 is magician
     /**
      * The no of team the corresponding player is in. Ex : "0,1,1,0"
      */
     private String wichTeam; // Here player no2 is in team no0
     /**
      * Indicating wich player is FSM. Ex : "0,1,1,0"
      */
     private String isFSM;   // 0 for player, other for FSM level
     /**
      * Wich controler (set of keys) controls this player if it's not a FSM
      */
     private String controlers;
     /**
      * If the starting points of the map are mixed or fixed
      */
     private boolean randStart;
     
     /* ----- Adventure Related stuff */
     /**
      * Tells if the game is in adventure mode
      * 0 : battle mode (conquest and stuff) launched from versus
      * 1 : battle mode launched from adventure
      * 2 : full adventure map (town without battle)
      */
     private int adv;
     
     private GameSave gameSave;
     
     private ArrayList<NPC> listNPCs = new ArrayList<NPC>();
     
     private boolean pauseNPC;
 
     /**
      * Initializes a game. extends JPanel so it draws everything that is game-related. It initalizes the teams, 
      * the players, the map, the cells, the scores and victory condition, and handle everything.
      * Permanently refreshed by TheThread.
      * @param whoIsPlaying A String indicating wich player is in the game and their skills. Ex : "1,8,0,4"
      * @param wichTeam The no of team the corresponding player is in. Ex : "0,1,1,0"
      * @param controlers Wich controler (set of keys) controls this player if it's not a FSM
      * @param isFSM Indicating wich player is FSM. Ex : "0,1,1,0"
      * @param randStart If the starting points of the map are mixed or fixed
      * @param nmap The number of file the map is in. Maps are stored in "resources/Maps/map"+nmap+".xml"
      * @param victScore How much points does it take to win the game
      * @param victTile How much percent of total cells of the map does it take to win the game
      * @param victTime Time before automatically losing the game
      */
     public Game(String whoIsPlaying, String wichTeam, String controlers, String isFSM, boolean randStart, int nmap, int victScore, double victTile, int victTime, int adv) {
         this.victScore = victScore;
         this.victTile = victTile;
         this.victTime = victTime;
         this.whoIsPlaying = whoIsPlaying;
         this.wichTeam = wichTeam;
         this.isFSM = isFSM;
         this.randStart = randStart;
         this.controlers = controlers;
         this.adv = adv;
         
         initGame(nmap);
         
         initTeams();
         
         initPlayers();
         
         // Init scores
         if(adv < 2){
             scoreHandler = new ScoreBar(this);
         }
     }
 
     /**
      * Initializes a game from the Versus menu. cf other Game constructor
      * @param playerSelect
      * @param randStart
      * @param mapNumber
      * @param victScore
      * @param victTile
      * @param victTime
      */
     public Game(ArrayList<PlayerSelect> playerSelect, boolean randStart, int mapNumber, int victScore, double victTile, int victTime, int adv) {
         this.victScore = victScore;
         this.victTile = victTile;
         this.victTime = victTime;
         initGame(mapNumber);
         this.randStart = randStart;
         this.adv = adv;
         
         initTeams(playerSelect);
         initPlayers(playerSelect);
         
         if(adv < 2){
             scoreHandler = new ScoreBar(this);
         }
     
     }
     
     /**
      * Initializes a full adventure map (whitout battling and stuff), such as a town, qhere the player can peacefully
      * walk and talk to NPCs.
      * @param nmap The ID of the map where it's played
      */
     public Game(int nmap){
         this("1","0","0","0",false,nmap,0,0,0,2);
         // Detect World Map
         initListNPCs(nmap);
     }
     
     /**
      * Init common game-related stuff, such as the map, thread, and pauses the game
      * @param nmap The number of the file the map is in
      */
     public void initGame(int nmap){
         // Panel related stuff 
         this.setLayout(null);
         this.setBackground(Color.white);
         
         // Parse the map
         map = new CMap(this,nmap);
         map = XMLparser.parseMap(nmap,this);
         
         //Parse ParamTable
         XMLparser.parseParams();
         this.setSize(Constants.frameDimension);
         
         // Initialize thread
         thread = new TheThread(this);
         thread.setRunning(false);
         new Thread(this.thread).start();
         thread.setRunning(true);
         
         gameEnded = false;
         
         if(adv > 0){
             this.gameSave = Tools.loadGame();
         }
         
         //Pause it (so it can be automatically un-paused afterwards)
         //if(adv < 2){
             pauseGame();
         //}
             
             this.listNPCs = new ArrayList<NPC>();
     }
 
     /**
      * Paint the panel. takes care of background, map, players and other components
      * @param g Graphical thing that no one really understands
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         // Background
         Graphics2D g2d = (Graphics2D)g;
         
         int w = this.getWidth();
         int h = this.getHeight();
 
         // Paint a gradient from top to bottom
         GradientPaint gp = new GradientPaint(0, 0, Constants.top, 0, h, Constants.bottom);
 
         g2d.setPaint(gp);
         g2d.fillRect(0, 0, w, h);
         // --- End BG
         
         // Repaint the map
         map.paintComponent(g);
         if(adv <2){
             // Repaint the scoreHandler
             this.scoreHandler.paintComponent(g);
         }
         // Repaint the objects (such as arrows)
         for(int j=0;j<objects.size();j++){
             objects.get(j).paintComponent(g);
         }
         
         
         /*
         // Paint black screen if the game is paused
         if(!thread.getRunning() && !pauseNPC){
             g.drawImage(Graph.guimg.get("pauseScreen"), 0, 0,this.getWidth(),this.getHeight(), this);
         }
         */
         // Paint the animations (warps, explosions, bitches,...)
         for(int j=0;j<anims.size();j++){
             if(thread.getRunning() || anims.get(j) instanceof PauseCountDown){
                 anims.get(j).paintComponent(g);
             }
         }
     }
 
     public ArrayList<Player> getPlayers() {
         return players;
     }
 
     public CMap getMap() {
         return map;
     }
 
     public TheThread getThread() {
         return thread;
     }
 
     /**
      * Refreshes healthpoints for each and every cell by calling the speciefied method
      * Also activate the cell each player is on (killing it, etc...)
      */
     public void refreshHealthPoints() {
         if (adv < 2) {
             ArrayList<Cell> myMap = map.getMyMap();
             for (int j = 0; j < myMap.size(); j++) {
                 Cell c = myMap.get(j);
                 c.refreshHealthPoints(this);
             }
 
             for (int i = 0; i < players.size(); i++) {
                 Player p = players.get(i);
                 Cell c = p.getCurrent();
                 c.activateCell(p);
             }
         }
     }
 
     /**
      * Checks is the designated cell is occupied (someone stands on it)
      * @param c The cell that needs to be tested
      * @return the player that stand on it (or null)
      */
     public Player isOccupied(Cell c) {
         Player p = null;
         // Check if the cell is indeed in the map first
         if (c != null && map.containsCell(c) != -1) {
             // Check for each player
             for (int i = 0; i < players.size(); i++) {
                 Player q = players.get(i);
                 // Check position
                 if (q.getI() == c.getI() && q.getJ() == c.getJ()) {
                     p = q;
                     break;      // Escape !
                 }
             }
         }
         return p;
     }
 
     /**
      * Updates the number of cell each team has
      */
     public void updateCellsByOwner() {
         // Reset numbers
         for (int j = 0; j < teams.size(); j++) {
             Team te = teams.get(j);
             te.setNCells(0);
         }
         // Read the map
         ArrayList<Cell> cells = map.getMyMap();
         for (int i = 0; i < cells.size(); i++) {
             Cell c = cells.get(i);
             // Get the owner
             if (c.getOwner() != null) {
                 // Update owner's value
                 Team te = c.getOwner();
                 te.setNCells(te.getNCells() + 1);
             }
         }
 
     }
 
 
     public ScoreBar getScoreHandler() {
         return scoreHandler;
     }
 
     public int getRWidth() {
         return this.getWidth();
     }
 
     /**
      * Initializes teams with the wichTeam string
      */
     private void initTeams() {
         // read the string
         for (int i = 0; i < wichTeam.length(); i += 2) {
             int idT = Integer.parseInt(""+wichTeam.charAt(i));
             // Create new team if needed
             if(idT>=teams.size()){
                 Team te = new Team();
                 teams.add(idT, te);
             }
         }
     }
     
     /**
      * Initializes teams with the values given by the versus menus
      * @param playerSelect the players selected in versus menus
      */
     private void initTeams(ArrayList<PlayerSelect> playerSelect) {
         // Read info
         for (int i = 0; i < playerSelect.size(); i ++) {
             int idT = playerSelect.get(i).getTeam();
             // Create team if needed
             if(idT>=teams.size()){
                 Team te = new Team();
                 teams.add(idT, te);
             }
         }
     }
 
     public void setTeams(ArrayList<Team> teams) {
         this.teams = teams;
     }
 
     public ArrayList<Team> getTeams() {
         return teams;
     }
 
     /**
      * Tells the objects of the game to execute. Ex : make the arrow move
      */
     public void computeObjects() {
         // Get objects
         for(int j=0;j<objects.size();j++){
             Element e = objects.get(j);
             // Make them execute action
             if(e instanceof Arrow){
                 Arrow a = (Arrow)e;
                 a.effect();
             }
         }
     }
     
     /**
      * Executes the animations
      */
     public void computeAnimations(){
         // Get animations
         for(int j=0;j<anims.size();j++){
             Animation a = anims.get(j);
             // Execute them
             if(a.isRunning()){
                 a.executeAnimation();
             }
         }
     }
     
     /**
      * Tells the players to handle their keys. This method makes them move according to what key is pressed
      */
     public void playerHandleKeys(){
         for(int i=0;i<players.size();i++){
             players.get(i).handleKeys();
         }
     }
     
     /**
      * Add object to the Game
      * @param e the object
      */
     public void addObject(Element e){
         objects.add(e);
     }
     
     /**
      * Add animation to the Game
      * @param e the animation
      */
     public void addAnim(Animation e){
         anims.add(e);
     }
     
     /**
      * Remove an existing object from the game
      * @param e the object
      */
     public void deleteObject(Element e){
         if(objects.contains(e)){
             objects.remove(e);
         }
     }
     
     /**
      * Remove an existing animation from the game
      * @param e the animation
      */
     public void deleteAnim(Animation e){
         if(anims.contains(e)){
             anims.remove(e);
         }
     }
 
     /**
      * Tells the FSMs to compute states, move, etc...
      */
     public void executeFSMs() {
         for(int i=0;i<players.size();i++){
             Player p = players.get(i);
             // If this player has a FSM, tell it to execute
             if(p.getFsm() != null){
                 p.getFsm().executeMethod();
             }
         }
     }
 
     /**
      * Initializes the Key Listener of the game
      */
     private void initKListener() {
         kl = new KListener(this);
         this.addKeyListener(kl);
     }
     
     private void deleteKListener(){
         this.removeKeyListener(kl);
         kl = new KListener(this);
     }
 
     /**
      * Request the focus so that the K listener works
      */
     public void initFocus() {
         initKListener();
         this.setFocusable(true);
         this.initListNPCs(map.getFileID());
         requestFocus();
     }
     
     public void releaseFocus(){
         deleteKListener();
         thread.setRunning(false);
         this.setFocusable(false);
     }
     
     /**
      * Tests if the victory was reached by a Team. if yes, returns it
      * @return the winner team (or null)
      */
     public Team testVictory() {
         Team p= null;
         //test for each player
         for (int i = 0; i < teams.size(); i++) {
             Team te = teams.get(i);
             //place here victory condition
             int score = te.getScore();
             int tilesOwned = te.getNCells();
             int totalTile = map.getTakableCells().size();
             //pass the time test
             //if(victTime!=0 && dateG <= victTime*fpsa){
             //Pass the score test
             if (score != 0 && score >= victScore) {
                 //Pass the tile test
                 if (((double)tilesOwned) / totalTile >= victTile) {
                     p = te;
                 }
             }
             //}
             if (victTime != 0 && this.getThread().getCount() > victTime * 1000) {
                 System.out.println("Time out !");
                 endGame(null);
             }
         }
         return p;
     }
     
     /**
      * Pauses or un-pauses the game
      */
     public void pauseGame(){
         pauseGame(false);
     }
     
     public void pauseGame(boolean isNPC){
         this.pauseNPC = isNPC;
         // If running, pause
         if(thread.getRunning()){
             thread.setRunning(false);
             this.repaint();
             // Display pause
             PauseScreen ps = new PauseScreen(false, this);
            if(adv > 0){
                ps.setAdvMode(true);
             }
            this.addObject(ps);
         }
         // Else un-pause if the game is still not finished
         else if(!gameEnded && !isNPC){
             // display countdown
             for(int j=0;j<objects.size();j++){
                 Element e = objects.get(j);
                 if(e instanceof PauseScreen){
                     ((PauseScreen)e).setResuming(true);
                 }
             }
             new PauseCountDown(400,150,Params.pauseDuration,thread);
         }
         else if(isNPC){
             
             removePauseScreen();
             thread.setRunning(true);
         }
     }
     
     public void removePauseScreen(){
         for(int j=0;j<objects.size();j++){
             Element e = objects.get(j);
             if(e instanceof PauseScreen){
                 ((PauseScreen) e).setResuming(true);
                 deleteObject(e);
             }
         }
     }
     
     /**
      * Ends the game and displays winner
      * @param winner the team who won (or null, that would mean tie or lost (for adventure))
      */
     public void endGame(Team winner){
         pauseGame();
         PauseScreen victoryScreen = null;
         for(int j=0;j<objects.size();j++){
             Element e = objects.get(j);
             if(e instanceof PauseScreen){
                 victoryScreen = (PauseScreen) e;
             }
         }
         victoryScreen.setDisplayVictory(true);
         victoryScreen.setWinner(winner);
         gameEnded = true;
         // versus mode
         if(adv == 0){
             victoryScreen.setAdvMode(false);
             if(winner == null){
                 System.out.println("Tie !");
                 //victoryScreen.setMessage("Match Nul !");
             }
             else{
                 victoryScreen.setMessage("Fin du match !");
                 //System.out.println("Winner : "+winner);
             }
         }
         else{
             victoryScreen.setAdvMode(true);
             // adventure mode : if the winner isn't the player, then display lost
             Team thePlayer = teams.get(0);
             if(thePlayer == winner){
                 // Change and save gameDatas
                 this.gameSave = Tools.loadGame();
                 Map<Integer,Integer> mapValues = gameSave.getMapValues();
                 mapValues.put(map.getFileID(),2);
                 Tools.saveGame(this.gameSave);
                 Fapplication.getWorldMap().initWorldMap();
                 // display victory : (wich will have its own back to world map)
                 victoryScreen.setMessage("Victoire !");
                 //System.out.println("Yay, Victory !");
             }
             else{
                 // Display defeat :(wich will have its own back to world map)
                 victoryScreen.setMessage("Perdu...");
                 victoryScreen.setWinner(null);
                 //System.out.println("You lost");
                 // Don't change anything
             }
             //new AStartGame(Fapplication.getWorldMap()).execute(null);
         }
         this.repaint();
     }
     
     /**
      * Initializes the players according to the WhoIsPlaying String and friends (isFSM, etc...)
      */
     private void initPlayers() {
         for (int i = 0; i < whoIsPlaying.length(); i += 2) {
             //Note : the number in "isPlaying" is also the character of the player. If 0, the player is disabled
             int charac = Integer.parseInt(""+whoIsPlaying.charAt(i));
             int controler = Integer.parseInt(""+controlers.charAt(i));
             boolean isPlaying = charac!=0;
             if (isPlaying) {
                 // Initializes ID
                 int pid = i / 2;
                 // Initializes startCell
                 Cell c = getStartCell(pid);
                 // Get team (previously created)
                 Team team = teams.get(Integer.parseInt(""+wichTeam.charAt(i)));
                 // Parse fsm level if there is
                 int ai = Integer.parseInt(""+isFSM.charAt(i));
                 // Create player
                 Player p = generatePlayer(charac,pid,c,team,ai,controler,this);
                 // Add it
                 players.add(p);
             }
         }
     }
     
     /**
      * Initializes the players according to the datas received from the versus menus
      */
     private void initPlayers(ArrayList<PlayerSelect> playerSelect) {
         // Get info
         for (int i = 0; i < playerSelect.size(); i++) {
             PlayerSelect ps = playerSelect.get(i);
             //Note : the number in "isPlaying" is also the character of the player. If 0, the player is disabled
             int charac = ps.getPc();
             int pid = players.size();
             Cell c = getStartCell(pid);
             Team team = teams.get(ps.getTeam());
             int ai = ps.getIsFSM();
             Player p = generatePlayer(charac, pid, c, team, ai, ps.getControler(),this);
 
             players.add(p);
         }
     }
     
     /**
      * Get a start cell for the designated player id
      * @param pid the id of the player to be placed
      * @return the start Cell for this player
      */
     private Cell getStartCell(int pid){
         ArrayList<Cell> startCellsAL = map.getStartCells();
         Cell c;
         // If the start cells are randomly mixed
         if (randStart) {
             // Get a cell from the list
             if (startCellsAL.size() > 0) {
                 int rand = Tools.randRange(0, startCellsAL.size() - 1);
                 c = startCellsAL.get(rand);
                 startCellsAL.remove(rand);
             }
             // if no more, find randomly a cell
             /*
              * This should not be used ! All map should have 8 start cells defined (but this is safe)
              */
             else {
                 ArrayList<Cell> takable = map.getTakableCells();
                 do {
                     c = takable.get(Tools.randRange(0, takable.size() - 1));
                 } while (this.isOccupied(c) != null);
             }
         } else {
             // The start cells are for a designated player, get him his cell
             c = startCellsAL.get(pid);
         }
         return c;
     }
     
     /**
      * Generate a player according to diverses parameters
      * @param charac The class (skill, character) of the player
      * @param pid An ID
      * @param c The start cell
      * @param team His team
      * @param ai His Artificial Intelligence level (0 if human)
      * @param controler His keyboard set
      * @return The created player
      */
     public static Player generatePlayer(int charac, int pid, Cell c, Team team, int ai, int controler, Game game){
         Player p;
         switch(charac){
             case 1:
                 p = new Knight(pid, c, game,team,ai,controler);
                 break;
             case 3:
                 p = new Miner(pid, c, game,team,ai,controler);
                 break;
             case 4:
                 p = new Warlock(pid, c, game,team,ai,controler);
                 break;
             case 5:
                 p = new Archer(pid, c, game,team,ai,controler);
                 break;
             case 6:
                 p = new Vampire(pid, c, game,team,ai,controler);
                 break;
             case 7:
                 p = new NoCharacter(pid, c, game,team,ai,controler);
                 break;
             case 8:
                 p = new Magician(pid, c, game,team,ai,controler);
                 break;
             case 9:
                 p = new Booster(pid, c, game,team,ai,controler);
                 break;
         default:
             p = new Knight(pid, c, game,team,ai,controler);
             break;
         }
         return p;
     }
     
 // The game, you just lost
 
     public void setAdv(int adv) {
         this.adv = adv;
     }
 
     public int getAdv() {
         return adv;
     }
 
     public void initWorldMap() {
         this.listNPCs = new ArrayList<NPC>();
         // Linking maps to Cells
         Map<Integer,Dimension> mapList = new HashMap<Integer,Dimension>();
         mapList.put(20, new Dimension(18,11));
         mapList.put(21, new Dimension(18,10));
         mapList.put(22, new Dimension(17,9));
         mapList.put(23, new Dimension(17,10));
         mapList.put(24, new Dimension(16,10));
         mapList.put(25, new Dimension(18,9));
     
         Map<Integer,Game> gameList = new HashMap<Integer,Game>();
         gameList.put(25, new Game(25));
         gameList.put(20, new Game("1,1","0,1","0,1","0,2",false,20,1000,0,0,1));
 
         // Get list of conquered cells
         this.computeWorldMap();
         ArrayList<Integer> listJustAvailableCells = getListOfWMCells(1);
         ArrayList<Integer> listConqueredCells = getListOfWMCells(2);
         ArrayList<Integer> listNotAvailableCells = getListOfWMCells(0);
         /*System.out.println("Just Av : "+listJustAvailableCells);
         System.out.println("Not Av : "+listNotAvailableCells);
         System.out.println("Conquered : "+listConqueredCells);*/
         // Create NPCs to cover the designated cells
         for(int i=0;i<listNotAvailableCells.size();i++){
             int mapID = listNotAvailableCells.get(i);
             Dimension indexes = mapList.get(mapID);
             Cell pos = this.map.getCell((int)indexes.getWidth(), (int)indexes.getHeight());
             this.addNPC(new NPCWMBlocking(pos));
         }
         
         for(int i=0;i<listJustAvailableCells.size();i++){
             int mapID = listJustAvailableCells.get(i);
             //if(Tools.intTableContains(Constants.listAdvMaps, mapID)){
                 Dimension indexes = mapList.get(mapID);
                 Cell pos = this.map.getCell((int)indexes.getWidth(), (int)indexes.getHeight());
                 this.addNPC(new NPCWMStarting(pos,false,this,gameList.get(mapID)));
             //}
         }
         
         for(int i=0;i<listConqueredCells.size();i++){
             int mapID = listConqueredCells.get(i);
             //if(Tools.intTableContains(Constants.listAdvMaps, mapID)){
                 Dimension indexes = mapList.get(mapID);
                 Cell pos = this.map.getCell((int)indexes.getWidth(), (int)indexes.getHeight());
                 this.addNPC(new NPCWMStarting(pos,true,this,gameList.get(mapID)));
             //}
         }
     }
     
     public void computeWorldMap(){
         //Load game :
         this.gameSave = Tools.loadGame();
         // Init map parents
         Map<Integer,ArrayList<Integer>> mapParents = new HashMap<Integer,ArrayList<Integer>>();
         for(int i=0;i<Constants.listAdvMaps.length;i++){
             mapParents.put(Constants.listAdvMaps[i], new ArrayList<Integer>());
         }
         // Fill map parents
         /*mapParents[20] = [0];
         mapParents[21] = [0];
         mapParents[22] = [1,21,20];
         mapParents[23] = [1,21,22];
         mapParents[24] = [2,23,20];
         mapParents[25] = [0];*/
         
         mapParents.get(22).add(20);
         
         mapParents.get(23).add(21);
         mapParents.get(23).add(22);
         
         mapParents.get(24).add(23);
         // This line doesn't do much, does it ?
         mapParents.get(24).add(20);
         
         // Get conquered cell
         ArrayList<Integer> listConqueredCells = getListOfWMCells(2);
         Map<Integer, Integer> mapValues = gameSave.getMapValues();
         for (int i = 0; i < Constants.listAdvMaps.length; i++) {
             int mapID = Constants.listAdvMaps[i];
             int mapConquest = mapValues.get(mapID);
             ArrayList<Integer> parents = mapParents.get(mapID);
             boolean available = true;
             if (parents != null && parents.size() > 0) {
                 for (int j = 0; j < parents.size(); j++) {
                     if (!listConqueredCells.contains(parents.get(j))) {
                         available = false;
                         break;
                     }
                 }
             }
             if(available && mapConquest == 0){
                 gameSave.getMapValues().put(mapID, 1);
             }
         }
         Tools.saveGame(this.gameSave);
     }
     
     public ArrayList<Integer> getListOfWMCells(int value){
         Map<Integer, Integer> mapValues = gameSave.getMapValues();
         ArrayList<Integer> list = new ArrayList<Integer>();
         for (int i = 0; i < Constants.listAdvMaps.length; i++) {
             int mapConquest = mapValues.get(Constants.listAdvMaps[i]);
             //System.out.println(mapConquest);
             if(mapConquest == value){
                 list.add(Constants.listAdvMaps[i]);
             }
         }
         return list;
     }
 
     public void setGameSave(GameSave gameSave) {
         this.gameSave = gameSave;
     }
 
     public GameSave getGameSave() {
         return gameSave;
     }
 
     public void setListNPCs(ArrayList<NPC> listNPCs) {
         this.listNPCs = listNPCs;
     }
 
     public ArrayList<NPC> getListNPCs() {
         return listNPCs;
     }
 
     public void addNPC(NPC e){
         if(!this.listNPCs.contains(e)){
             listNPCs.add(e);
         }
     }
 
     public void initListNPCs(int nmap) {
         this.pauseNPC = false;
         if(this.listNPCs.size()>0){
             for(int i=0;i<listNPCs.size();i++){
                 listNPCs.get(i).reInit();
             }
         }
         else{
             
         }
             //TODO Initialize the list of all NPCs according to the nmap
             switch(nmap){
             case 0:
                 initWorldMap();
                 break;
             case 25:
                 System.out.println("init 25");
                 ArrayList<Action> list = new ArrayList<Action>();
                 list.add(new ASetSwitch(0,true));
                 list.add(new ADisplayMessage("What ? Cha didn't activate da test switch ? I'ma doing it right now !"));
                 NPC npc = new NPC(map.getCell(10, 10),false,false,Graph.getBasicCellImage(),this,0,0,list);
                 this.addNPC(npc);
                 
                 
                 ADisplayMessage ad = new ADisplayMessage("Ya do not interrest-a me");
                 ArrayList<Action> list2 = new ArrayList<Action>();
                 list2.add(new ATestSwitch(0,ad));
                 list2.add(new ADisplayMessage("Ye have da switch !! Gloria !"));
                 NPC npc2 = new NPC(map.getCell(15, 8),false,false,Graph.getBasicCellImage(),this,0,0,list2);
                 this.addNPC(npc2);
                 break;
             default:
                 //this.listNPCs = new ArrayList<NPC>();
                 System.out.println("Couldn't find NPC list for map no "+nmap);
                 break;
             }
     }
 
     public void setPauseNPC(boolean pauseNPC) {
         this.pauseNPC = pauseNPC;
     }
 
     public boolean isPauseNPC() {
         return pauseNPC;
     }
 
     public void setObjects(ArrayList<Element> objects) {
         this.objects = objects;
     }
 
     public ArrayList<Element> getObjects() {
         return objects;
     }
 
     public void setKl(KListener kl) {
         this.kl = kl;
     }
 
     public KListener getKl() {
         return kl;
     }
 }
