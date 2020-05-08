 package Ingame;
 
 import Utilities.Utilizer;
 import model.HeroSystem.HeroMoveThread;
 import model.HeroSystem.HeroStatus;
 import model.Skills.SkillStatus;
 import model.Skills.SkillThread;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 3/5/13
  * Time: 11:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MapListener implements MouseListener,MouseMotionListener {
     GameMap panel;
     Point pressed;
     public MapListener(GameMap panel)
     {
         this.panel=panel;
     }
     @Override
     public void mouseClicked(MouseEvent e) {
 
 
         //To change body of implemented methods use File | Settings | File Templates.
         GameMap temp= panel;
         temp.setStatus("selected");
         Cell selectCell= new Cell();
         Point curPos=e.getPoint();
         int x=(curPos.x + panel.scrollX)/ Utilizer.TILE_SIZE;
         selectCell.setColPos(x);
         x=x*Utilizer.TILE_SIZE;
         selectCell.setX(x);
         int y=(curPos.y + panel.scrollY)/Utilizer.TILE_SIZE;
         selectCell.setRowPos(y);
         y=y*Utilizer.TILE_SIZE;
         selectCell.setY(y);
         temp.setSelectedCell(selectCell);
        panel.requestFocus();
         if(e.isMetaDown()){
             panel.getHero().setIsChosen(false);
         }
         //To set Hero
         if(selectCell.getRowPos()==panel.hero.getRow() && selectCell.getColPos()==panel.hero.getCol()){
             panel.hero.setIsChosen(true);
             panel.hero.setStatus(HeroStatus.standing);
             panel.hero.resetPath();
         }
         else if(panel.hero.getIsChosen() && panel.hero.getStatus().equals(HeroStatus.standing))
         {
             panel.getHero().setStatus(HeroStatus.moving);
             panel.getHero().setShortestPathSelect(panel.getHero().getShortestpathHover());
             HeroMoveThread t=new HeroMoveThread(panel.getHero(), panel);
             t.start();
 
         }
         else if(panel.getHero().getIsChosen() && panel.getHero().getStatus().equals(HeroStatus.attacking) && panel.getHero().getCurrentSkill().getStatus().equals(SkillStatus.before) ){
             panel.getHero().getCurrentSkill().setStatus(SkillStatus.after);
             SkillThread t=new SkillThread(panel,panel.getHero().getCurrentSkill());
             t.start();
         }
         panel.repaint();
 
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
         pressed=e.getPoint();
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
 
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
 
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
         Point dragged= e.getPoint();
         int x=dragged.x-pressed.x;
         int y=dragged.y-pressed.y;
         if( y>0 ){
            panel.decreaseScrollY();
         }
         if(y <0){
            panel.increaseScrollY();
         }
         if(x>0){
             panel.decreaseScrollX();
         }
         if(x<0){
             panel.increaseScrollX();
         }
         panel.repaint();
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
         GameMap temp=(GameMap) panel;
         Cell rangeCell= new Cell();
         Point curPos=e.getPoint();
         int x=(curPos.x + panel.scrollX) / Utilizer.TILE_SIZE;
         rangeCell.setColPos(x);
         x=x*Utilizer.TILE_SIZE;
         rangeCell.setX(x);
         int y=(curPos.y+panel.scrollY)/Utilizer.TILE_SIZE;
         rangeCell.setRowPos(y);
         y=y*Utilizer.TILE_SIZE;
         rangeCell.setY(y);
         temp.setRangedCell(rangeCell);
 
         //set hero movement
         if(panel.hero.getIsChosen())
         {
             panel.hero.calculateShortestPath(rangeCell);
         }
         panel.repaint();
     }
 
 
 
 
 
 
 }
