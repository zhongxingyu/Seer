 package fap_java;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 
 import java.util.Map;
 
 public class Cell extends Element{
     private int did;
     private int type;
     private Team owner;
     private boolean walkable;
     private double hp;
     private boolean height;
    private Player trap;
     private CMap map;
     private String addParam;
     
     public Cell(int i, int j, int type, int did) {
         this(i, j, type,"", did);
     }
     
     public Cell(int i, int j, int type, String param, int did) {
         super();
         
         this.setI(i);
         this.setJ(j);
         this.walkable = true;
         this.height = false;
         this.hp = 0;
         this.addParam = param;
         this.did = did;
         this.setType(type);
         this.trap = null;
         
     }
     
     public void paintComponent(Graphics g) {
         int x = CMap.giveTalePosition(this.getI(), this.getJ())[0];
         int y = CMap.giveTalePosition(this.getI(), this.getJ())[1] + CMap.OFFMAP;
         
         /*
         Graphics2D g2d = (Graphics2D)g;
         
         // Anti-aliasing enabled
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         
         g2d.drawImage(Graph.cells.get("normal"), x, y, CMap.TW * 2, CMap.TH * 2, null);
         
         // Anti-aliasing disabled (trying not to slow down the entire thing)
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         
         */
         
         if(owner == null){
             g.setColor(Color.BLACK);
         }
         else{
             g.setColor(owner.getColor());
         }
         if(type == 10){
             g.setColor(Color.GREEN);
         }
         if(!walkable){
             g.setColor(Color.pink);
         }
         g.drawRect(x, y, CMap.TW, CMap.TH);
         
         if(hp > 0){
             g.drawString("" + (int) hp, x + 5, y + 10);
         }
     }
 
     public void setWalkable(boolean walkable) {
         this.walkable = walkable;
     }
 
     public boolean isWalkable() {
         return walkable;
     }
     
     public String toString(){
         String s = "Cell at "+this.getI()+","+this.getJ();
         if(this.owner != null){
             s+=", owned by "+owner;
         }
         if(walkable){
             s+=", walkable.";
         }
         return s;
     }
     
     public boolean equals(Cell c){
         boolean b = false;
         if(this.getI() == c.getI() && this.getJ() == c.getJ()){
             b = true;
         }
         return b;
     }
 
     public void activateCell(Player p){
         if (owner!=p.getTeam() && type ==1) {
             // if not, tests if the tale is empty
             if (hp<=0) {
                     // The tale is empty, sets it as the property of the player, gives HP and draw the according map
                 if(walkable == false){
                     p.kickBack(); 
                 }
                 else{
                     owner = p.getTeam();
                     hp = p.getInitHP();
                 }
             } else {
                     // Else forces the healthpoints of the tale to decrease (Attack)
                     hp -= p.getDecLifeForced();
             }
         }
         //System.out.println(trap);
         if(trap != null){
            if(trap != p){ // Blast that guy
                 //System.out.println("blasting");
                 p.blast(Params.nBlastedTiles);
                 trap = null;
                 // Add animation
             }
             else{ // Disable the bomb
                 if(p.getGame().getThread().getCount() - p.getLastSkill() >= p.getSkillTime()){
                     //System.out.println("disabling");
                     trap = null;
                     p.setLastSkill(p.getGame().getThread().getCount());
                     //Add animation
                 }
             }
         }
     }
     
     public void refreshHealthPoints(Game game){
         //trace(_root.kco);
                 // Counts the neighbours of the same type of the tale
                 //Note : MyDMap != 8 is for lava floor and unstable cells
                 //var recovB:Boolean = myDMap[vi][vj] != 8 && healthPoints[i][0] !=1 && countNeighbours(myMap, vi, vj, healthPoints[i][0])>=nNeighboursConwell;
         if(owner != null){
             boolean recovB = (type == 1) && owner != null && map.countNeighbours(this)>=Params.nNeighboursConwell && type != 8;
                 if (recovB) {
                         // If the cell is wounded (under initHP HPs)
                         if (hp<owner.getFirstPlayer().getInitHP()) {
                                         // The HP will recover slowly up to initHP
                                         hp += owner.getFirstPlayer().getRecovLifeAuto();
                                 // between initHP and maxHP
                         } else if (hp<owner.getFirstPlayer().getMaxHP() || (hp<Params.higherMaxHP && type == 13)) {
                                 //_root["t"+i].onEnterFrame = function() {
                                         // The HP will very slowly increase up to the max limit
                                         double gainLifeFactor;
                                         if(type == 13){
                                                 gainLifeFactor = Params.gainLifeFactorMultiplier;
                                         }
                                         else{
                                                 gainLifeFactor = 1;
                                         }
                                         hp += owner.getFirstPlayer().getGainLife()*gainLifeFactor;
                                 //};
                         } else {
                                 // If the tale isn't lonely or anything, do nothing
                                 //delete _root["t"+i].onEnterFrame;
                         }
                 } else {
                         /* Only enabled when "GameOfLife" level 1 or more is on :
                         the goal here is to decrease the HP of the tale because it's alone.
                         Cells need to be in groups to survive
                         */
                         if (Params.gameOfLife==true && type != 2) {
                                         //_root["t"+i].onEnterFrame = function() {
                                                 // The HP will decrease until the cell is dead OR not alone anymore
                                                 hp -= owner.getFirstPlayer().getDecLifeAuto();
                                         //};
                         }
                 }
                 }
                 // Testing if the tale is a neutral tale with HP (Countdown Cell) :
                 if (type == 2) {
                                         // The HP will decrease until the cell is dead
                                         hp -= Params.decLifeCountDown;
                 }
                 // If a cell dies, it goes back to normal un-owned tale  
                 if (hp<=0) {
                         // if it's a timer-cell
                         if (type == 2) {
                             type = 20;
                             walkable = false;
                             // KICK BACK
                             Player p = game.isOccupied(this);
                             if(p!=null){
                                 p.kickBack();
                             }
                             //Change did
                         } else {
                                 //else it goes back to neutral
                         }
                         // set the changes in the different variables
                         hp = 0;
                         owner = null;
                 }
     }
 
     public void setOwner(Team owner) {
         this.owner = owner;
     }
 
     public Team getOwner() {
         return owner;
     }
 
     public void setType(int type) {
         if(type ==20){
             //this.type = 1;
             walkable = false;
             this.height = true;
         }
         else if(type == 19){
             //this.type = 1;
             walkable = false;
             this.height = false;
         }
         else if(type == 2){
             this.type = type;
             hp = Integer.parseInt(addParam);
         }
         else{
             this.type = type;
         }
     }
 
     public int getType() {
         return type;
     }
 
     public void setAddParam(String addParam) {
         this.addParam = addParam;
     }
 
     public String getAddParam() {
         return addParam;
     }
 
     public void setMap(CMap map) {
         this.map = map;
     }
 
     public CMap getMap() {
         return map;
     }
 
     public void setDid(int did) {
         this.did = did;
     }
 
     public int getDid() {
         return did;
     }
 
     public void setHp(double hp) {
         this.hp = hp;
     }
 
     public double getHp() {
         return hp;
     }
 
    public void setTrap(Player trap) {
         this.trap = trap;
     }
 
    public Player getTrap() {
         return trap;
     }
 
 }
