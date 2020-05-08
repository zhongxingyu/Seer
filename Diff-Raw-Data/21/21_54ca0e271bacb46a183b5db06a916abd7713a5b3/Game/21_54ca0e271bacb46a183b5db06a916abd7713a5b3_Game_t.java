 package fap_java;
 
 import animations.Animation;
 
 import animations.PauseCountDown;
 
 import characters.*;
 
 import gui.Constants;
 
 import gui.FAPanel;
 import gui.NeedingFocus;
 
 import gui.PlayerSelect;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 
 import java.awt.Graphics2D;
 
 import java.awt.KeyboardFocusManager;
 
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 import pathFinder.pathFinder;
 
 public class Game extends JPanel implements NeedingFocus {
 
     private transient CMap map;
     private transient TheThread thread;
     private transient ArrayList<Player> players = new ArrayList<Player>();
     private transient KListener kl;
     private transient ScoreBar scoreHandler;
     private transient ArrayList<Team> teams = new ArrayList<Team>();
     
     private int victScore;
     private double victTile;    // in percent
     private int victTime; // in sec
     
     private boolean gameEnded;
     
     private ArrayList<Element> objects = new ArrayList<Element>();
     private ArrayList<Animation> anims = new ArrayList<Animation>();
     
     //Parameters to be given when starting a new game
 
     private String whoIsPlaying; // This is linked with skill, here player 1 is magician
     private String wichTeam; // Here player n2 is in team n0
     private String isFSM;   // 0 for player, other for FSM level
     private String controlers;
     private boolean randStart;
 
     public Game(String whoIsPlaying, String wichTeam, String controlers, String isFSM, boolean randStart, int nmap, int victScore, double victTile, int victTime) {
         this.victScore = victScore;
         this.victTile = victTile;
         this.victTime = victTime;
         this.whoIsPlaying = whoIsPlaying;
         this.wichTeam = wichTeam;
         this.isFSM = isFSM;
         this.randStart = randStart;
         this.controlers = controlers;
         
         initGame(nmap);
         
         initTeams();
         
         initPlayers();
 
         scoreHandler = new ScoreBar(this);
     }
 
     public Game(ArrayList<PlayerSelect> playerSelect, boolean randStart, int mapNumber, int victScore, double victTile, int victTime) {
         this.victScore = victScore;
         this.victTile = victTile;
         this.victTime = victTime;
         initGame(mapNumber);
         this.randStart = randStart;
         
         initTeams(playerSelect);
         initPlayers(playerSelect);
         
         scoreHandler = new ScoreBar(this);
     }
     
     private void initGame(int nmap){
         this.setLayout(null);
         this.setBackground(Color.white);
         map = new CMap(this);
         map = XMLparser.parseMap(nmap,this);
         //Parse ParamTable
         XMLparser.parseParams();
         this.setSize(Constants.frameDimension);
         
         thread = new TheThread(this);
         thread.setRunning(false);
         new Thread(this.thread).start();
         thread.setRunning(true);
         
         gameEnded = false;
         
         pauseGame();
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         map.paintComponent(g);
         for(int j=0;j<objects.size();j++){
             objects.get(j).paintComponent(g);
         }
         this.scoreHandler.paintComponent(g);
         
         // Paint black screen if the game is paused
         if(!thread.getRunning()){
             g.drawImage(Graph.guimg.get("pauseScreen"), 0, 0,this.getWidth(),this.getHeight(), this);
         }
         
         for(int j=0;j<anims.size();j++){
             if(!thread.getRunning() || anims.get(j) instanceof PauseCountDown){
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
 
     public void refreshHealthPoints() {
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
 
     public Player isOccupied(Cell c) {
         Player p = null;
         if (c != null && map.containsCell(c) != -1) {
             for (int i = 0; i < players.size(); i++) {
                 Player q = players.get(i);
                 if (q.getI() == c.getI() && q.getJ() == c.getJ()) {
                     p = q;
                     break;
                 }
             }
         }
         return p;
     }
 
 
     public void updateCellsByOwner() {
         for (int j = 0; j < teams.size(); j++) {
             Team te = teams.get(j);
             te.setNCells(0);
         }
         ArrayList<Cell> cells = map.getMyMap();
         for (int i = 0; i < cells.size(); i++) {
             Cell c = cells.get(i);
             if (c.getOwner() != null) {
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
 
     private void initTeams() {
         for (int i = 0; i < wichTeam.length(); i += 2) {
             int idT = Integer.parseInt(""+wichTeam.charAt(i));
             if(idT>=teams.size()){
                 Team te = new Team();
                 teams.add(idT, te);
             }
         }
     }
     
     private void initTeams(ArrayList<PlayerSelect> playerSelect) {
         for (int i = 0; i < playerSelect.size(); i ++) {
             int idT = playerSelect.get(i).getTeam();
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
 
     public void computeObjects() {
         for(int j=0;j<objects.size();j++){
             Element e = objects.get(j);
             if(e instanceof Arrow){
                 Arrow a = (Arrow)e;
                 a.effect();
             }
         }
     }
     
     public void computeAnimations(){
         for(int j=0;j<anims.size();j++){
             Animation a = anims.get(j);
             if(a.isRunning()){
                 a.executeAnimation();
             }
         }
     }
     
     public void playerHandleKeys(){
         for(int i=0;i<players.size();i++){
             players.get(i).handleKeys();
         }
     }
     
     public void addObject(Element e){
         objects.add(e);
     }
     
     public void addAnim(Animation e){
         anims.add(e);
     }
     
     public void deleteObject(Element e){
         if(objects.contains(e)){
             objects.remove(e);
         }
     }
     
     public void deleteAnim(Animation e){
         if(anims.contains(e)){
             anims.remove(e);
         }
     }
 
     public void executeFSMs() {
         for(int i=0;i<players.size();i++){
             Player p = players.get(i);
             if(p.getFsm() != null){
                 p.getFsm().executeMethod();
             }
         }
     }
 
 
     private void initKListener() {
         kl = new KListener(this);
         this.addKeyListener(kl);
     }
 
     public void initFocus() {
         initKListener();
         this.setFocusable(true);
         requestFocus();
     }
     
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
     
     public void pauseGame(){
         if(thread.getRunning()){
             thread.setRunning(false);
             this.repaint();
             // Display pause
         }
         else if(!gameEnded){
             // display countdown
             Animation countDown = new PauseCountDown(400,150,Params.pauseDuration,thread);
         }
     }
     
     public void endGame(Team winner){
         pauseGame();
         gameEnded = true;
         // versus mode
         if(winner == null){
             System.out.println("Tie !");
         }
         else{
             System.out.println("Winner : "+winner);
         }
         // adventure mode : if the winner isn't the player, then display lost
     }
     
     private void initPlayers() {
         ArrayList<Cell> startCellsAL = map.getStartCells();
         for (int i = 0; i < whoIsPlaying.length(); i += 2) {
             //Note : the number in "isPlaying" is also the character of the player. If 0, the player is disabled
             int charac = Integer.parseInt(""+whoIsPlaying.charAt(i));
             int controler = Integer.parseInt(""+controlers.charAt(i));
             boolean isPlaying = charac!=0;
             if (isPlaying) {
                 int pid = i / 2;
                 Cell c = getStartCell(pid);
                 Team team = teams.get(Integer.parseInt(""+wichTeam.charAt(i)));
                 int ai = Integer.parseInt(""+isFSM.charAt(i));
                 Player p = generatePlayer(charac,pid,c,team,ai,controler);
                 players.add(p);
             }
         }
     }
 
     private void initPlayers(ArrayList<PlayerSelect> playerSelect) {
         for (int i = 0; i < playerSelect.size(); i ++) {
             PlayerSelect ps = playerSelect.get(i);
             //Note : the number in "isPlaying" is also the character of the player. If 0, the player is disabled
             int charac = ps.getPc();
             int pid = players.size();
                 Cell c = getStartCell(pid);
                 Team team = teams.get(ps.getTeam());
                 int ai = ps.getIsFSM();
                 Player p = generatePlayer(charac,pid,c,team,ai,ps.getControler());
                 
                 players.add(p);
         }
     }
     
     private Cell getStartCell(int pid){
         ArrayList<Cell> startCellsAL = map.getStartCells();
         Cell c;
         if (randStart) {
             if(startCellsAL.size()>0){
                 int rand = Tools.randRange(0, startCellsAL.size() - 1);
                 c = startCellsAL.get(rand);
                 startCellsAL.remove(rand);
             }
             else{
                 ArrayList<Cell> takable = map.getTakableCells();
                 do{
                 c = takable.get(Tools.randRange(0, takable.size()-1));
                 }
                 while(this.isOccupied(c) != null);
             }
         } else {
             c = startCellsAL.get(pid);
         }
         return c;
     }
     
     private Player generatePlayer(int charac, int pid, Cell c, Team team, int ai, int controler){
         Player p;
         switch(charac){
             case 1:
                 p = new Knight(pid, c, this,team,ai,controler);
                 break;
             case 3:
                 p = new Miner(pid, c, this,team,ai,controler);
                 break;
             case 4:
                 p = new Warlock(pid, c, this,team,ai,controler);
                 break;
             case 5:
                 p = new Archer(pid, c, this,team,ai,controler);
                 break;
             case 6:
                 p = new Vampire(pid, c, this,team,ai,controler);
                 break;
             case 7:
                 p = new NoCharacter(pid, c, this,team,ai,controler);
                 break;
             case 8:
                 p = new Magician(pid, c, this,team,ai,controler);
                 break;
             case 9:
                 p = new Booster(pid, c, this,team,ai,controler);
                 break;
         default:
             p = new Knight(pid, c, this,team,ai,controler);
             break;
         }
         return p;
     }
 }
