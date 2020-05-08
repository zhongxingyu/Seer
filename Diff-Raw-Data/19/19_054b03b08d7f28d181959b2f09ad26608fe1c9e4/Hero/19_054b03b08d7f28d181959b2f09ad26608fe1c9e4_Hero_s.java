 package model.HeroSystem;
 import View.Ingame.Cell;
 import Utilities.Utilizer;
 import model.Character;
 import model.ItemSystem.Item;
 import model.Skills.Skill;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 3/17/13
  * Time: 9:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Hero extends Character implements Serializable {
     ArrayList<Skill> skills;
     int skillCount;
     ArrayList<Item> inventory;
     Cell[][] map;
     ArrayList<Cell> shortestpathHover,shortestPathSelect;
     int currentSkill;
     HeroStatus status;
     BufferedImage icon;
     private static ArrayList<Cell> range = new ArrayList<Cell>();
     protected Hero(int HP, int maxHP, int AP, int maxAP, int row, int col, BufferedImage[] image,BufferedImage avatar,String name,BufferedImage icon) {
         super(HP, maxHP, AP, maxAP, row, col, image,avatar,name);
         skills=new ArrayList<Skill>();
         inventory=new ArrayList<Item>();
         skillCount = 0;
         map=new Cell[Utilizer.MAP_ROWS][Utilizer.MAP_COLS];
         shortestpathHover=new ArrayList<Cell>();
         status=HeroStatus.standing;
         currentSkill = -1;
         this.icon = icon;
     }
     protected Hero(){
         super();
         skills=new ArrayList<Skill>();
         inventory=new ArrayList<Item>();
         skillCount = 0;
         map=new Cell[Utilizer.MAP_ROWS][Utilizer.MAP_COLS];
         shortestpathHover=new ArrayList<Cell>();
         status=HeroStatus.standing;
         currentSkill = -1;
     }
 
     public ArrayList<Cell> getShortestPathSelect() {
         return shortestPathSelect;
     }
 
     public void setShortestPathSelect(ArrayList<Cell> shortestPathSelect) {
         this.shortestPathSelect = shortestPathSelect;
     }
 
     public ArrayList<Cell> getShortestpathHover() {
         return shortestpathHover;
     }
 
     public void setShortestpathHover(ArrayList<Cell> shortestpathHover) {
         this.shortestpathHover = shortestpathHover;
     }
 
     public HeroStatus getStatus() {
         return status;
     }
 
     public void setStatus(HeroStatus status) {
         this.status = status;
     }
 
     @Override
     public void draw(Graphics g,int scrollX,int scrollY) {
         //To change body of implemented methods use File | Settings | File Templates.
         g.drawImage(getCurrentSprite(),getX()-scrollX,getY()-scrollY,getPanel());
         ArrayList<Cell> path=getShortestpathHover();
         if(isChosen && !path.isEmpty() && status==HeroStatus.standing){
             for(Iterator i=getShortestpathHover().iterator();i.hasNext();){
                 Cell temp=(Cell)i.next();
                 g.clearRect(temp.getColPos()*Utilizer.TILE_SIZE - scrollX, temp.getRowPos()*Utilizer.TILE_SIZE - scrollY,Utilizer.TILE_SIZE,Utilizer.TILE_SIZE);
                 g.drawImage(Utilizer.selectArray[0],temp.getColPos()*Utilizer.TILE_SIZE - scrollX,temp.getRowPos()*Utilizer.TILE_SIZE - scrollY,getPanel());
                 g.drawImage(Utilizer.selectArray[Utilizer.MAP[temp.getRowPos()][temp.getColPos()] - 1],temp.getColPos()*Utilizer.TILE_SIZE - scrollX,temp.getRowPos()*Utilizer.TILE_SIZE - scrollY,getPanel());
             }
         }
     }
 
     @Override
     public void nextSprite() {
         //To change body of implemented methods use File | Settings | File Templates.
         if(currentSprite % 4 == 3) currentSprite-=3;
         else currentSprite++;
     }
 
     @Override
     public void moveCharacter(int col,int row) {
         //To change body of implemented methods use File | Settings | File Templates.
         if(col- this.col > 0 && row==this.row)
         {
             dirX=12;
             dirY=0;
             distanceX= (col - this.col)*Utilizer.TILE_SIZE;
             distanceY = 0;
             currentSprite=8;
         }
         //move left
         if(col- this.col < 0 && row==this.row)
         {
             dirX=-12;
             dirY=0;
             distanceX= (this.col-col)*Utilizer.TILE_SIZE;
             distanceY = 0;
             currentSprite=4;
         }
         //move up
         if(col == this.col && row<this.row)
         {
             dirX=0;
             dirY=-12;
             distanceX= 0;
             distanceY = (this.row - row)*Utilizer.TILE_SIZE;
             currentSprite=12;
         }
         //move down
         if(col == this.col && row>this.row)
         {
             dirX=0;
             dirY=12;
             distanceX= 0;
             distanceY = (row - this.row)*Utilizer.TILE_SIZE;
             currentSprite=0;
         }
         this.col=col;
         this.row=row;
         resetPath();
     }
 
     @Override
     public void move() {
         //To change body of implemented methods use File | Settings | File Templates.
         x+=dirX;
         y+=dirY;
         distanceX-=Math.abs(dirX);
         distanceY-=Math.abs(dirY);
         setAP(getAP()-0.5);
     }
 
     @Override
     public void resetPath() {
         //To change body of implemented methods use File | Settings | File Templates.
         for(int i=0;i<map.length;i++)
             for(int j=0;j<map[0].length;j++)
                 map[i][j]=new Cell(i,j);
 
         map[row][col].setMinPath(0);
         PriorityQueue<Cell> queue=new PriorityQueue<Cell>();
         queue.add(map[row][col]);
         while(!queue.isEmpty()){
             Cell temp= queue.poll();
             int precost=temp.getMinPath()+1;
             if(temp.getRowPos()>0){
                 if(Utilizer.MOVEMAP[temp.getRowPos()-1][temp.getColPos()] == 0 && map[temp.getRowPos()-1][temp.getColPos()].getMinPath() > precost ){
                     //queue.remove(map[temp.getRowPos()-1][temp.getColPos()]);
                     map[temp.getRowPos()-1][temp.getColPos()].setMinPath(precost);
                     map[temp.getRowPos()-1][temp.getColPos()].setPrevious(temp);
                     queue.add(map[temp.getRowPos()-1][temp.getColPos()]);
                 }
             }
 
             if(temp.getRowPos()<map.length-1){
                 if(Utilizer.MOVEMAP[temp.getRowPos()+1][temp.getColPos()] == 0 && map[temp.getRowPos()+1][temp.getColPos()].getMinPath() > precost ){
                     //queue.remove(map[temp.getRowPos()+1][temp.getColPos()]);
                     map[temp.getRowPos()+1][temp.getColPos()].setMinPath(precost);
                     map[temp.getRowPos()+1][temp.getColPos()].setPrevious(temp);
                     queue.add(map[temp.getRowPos()+1][temp.getColPos()]);
                 }
             }
 
             if(temp.getColPos()>0){
                 if(Utilizer.MOVEMAP[temp.getRowPos()][temp.getColPos()-1] == 0 && map[temp.getRowPos()][temp.getColPos()-1].getMinPath() > precost ){
                     //queue.remove(map[temp.getRowPos()][temp.getColPos()-1]);
                     map[temp.getRowPos()][temp.getColPos()-1].setMinPath(precost);
                     map[temp.getRowPos()][temp.getColPos()-1].setPrevious(temp);
                     queue.add(map[temp.getRowPos()][temp.getColPos()-1]);
                 }
             }
 
             if(temp.getColPos()<map[0].length-1){
                 if(Utilizer.MOVEMAP[temp.getRowPos()][temp.getColPos()+1] == 0 && map[temp.getRowPos()][temp.getColPos()+1].getMinPath() > precost ){
                     //queue.remove(map[temp.getRowPos()][temp.getColPos()+1]);
                     map[temp.getRowPos()][temp.getColPos()+1].setMinPath(precost);
                     map[temp.getRowPos()][temp.getColPos()+1].setPrevious(temp);
                     queue.add(map[temp.getRowPos()][temp.getColPos()+1]);
                 }
             }
 
         }
     }
     public ArrayList<Cell> calculateShortestPath(Cell to){
         if(Utilizer.MOVEMAP[to.getRowPos()][to.getColPos()]==0){
             ArrayList<Cell> path=new ArrayList<Cell>();
             for(Cell i=map[to.getRowPos()][to.getColPos()];i!=null;i=i.getPrevious()) {
                 path.add(i);
             }
             Collections.reverse(path);
             shortestpathHover =path;
             return path;
         }
         return null;
     }
 
     public void addSkill(Skill skill){
         if(skillCount<3){
             skills.add(skill);
             skillCount++;
         }
     }
     public Skill getSkill(int index){
         return skills.get(index);
     }
 
     public int getCurrentSkillIndex() {
         return currentSkill;
     }
     public Skill getCurrentSkill(){
         return skills.get(currentSkill);
     }
     public void setCurrentSkill(int currentSkill) {
         this.currentSkill = currentSkill;
     }
 
     public void addItem(Item item){
         inventory.add(item);
     }
     public void removeItem(Item item){
         inventory.remove(item);
     }
     public void removeItemByIndex(int index){
         inventory.remove(index);
     }
     public ArrayList<Cell> calculateRange(int x, int y, double remaining){
         if(remaining == 0)
             return range;
 
         if(Utilizer.MOVEMAP[x][y] == 0)
             range.add(new Cell(x,y));
         else    return range;
 
         if(y+1 < 40)
             calculateRange(x, y + 1, remaining - 1); //up
         if(x-1 > 0)
             calculateRange(x - 1, y, remaining - 1); //left
         if(y-1 > 0)
             calculateRange(x, y - 1, remaining - 1); //down
         if(x+1 < 40)
             calculateRange(x + 1, y, remaining - 1); //right
         return range;
     }
     public void drawRange(Graphics g,int scrollX,int scrollY){
         ArrayList<Cell> cells=getRange();
         for(Cell cell:cells){
             g.clearRect(cell.getColPos()*Utilizer.TILE_SIZE - scrollX, cell.getRowPos()*Utilizer.TILE_SIZE - scrollY, Utilizer.TILE_SIZE, Utilizer.TILE_SIZE);
            g.drawImage(Utilizer.hoverArray[0], cell.getColPos()*Utilizer.TILE_SIZE - scrollX, cell.getRowPos()*Utilizer.TILE_SIZE - scrollY, panel);
            g.drawImage(Utilizer.hoverArray[Utilizer.MAP[cell.getRowPos()][cell.getColPos()] - 1], cell.getColPos()*Utilizer.TILE_SIZE - scrollX, cell.getRowPos()*Utilizer.TILE_SIZE - scrollY, panel);
         }
     }
     public ArrayList<Cell> getRange(){
         return range;
     }
     public void clearRange(){
         range.clear();
     }
 }
