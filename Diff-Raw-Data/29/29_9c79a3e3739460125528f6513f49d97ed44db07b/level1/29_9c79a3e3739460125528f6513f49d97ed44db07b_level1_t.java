 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.warofcosmo.cosmo.levelpackage;
 
 import com.warofcosmo.cosmo.Board;
 import com.warofcosmo.cosmo.Enemy;
 import sun.audio.AudioStream;
 
 /**
  *
  * @author TOSHIBA
  */
 public class level1 extends AbstractLevel{
     private int _speed=1;
     private String _bgm;
     private AudioStream as;
     
     public level1(Board brd){
         super(brd,"bg2.png","project4.wav",0,0);   
     }
    
     @Override
     public void Move(){
         _dx=_dx-_speed;
         _dy=_dy-0;
      
         int px=_brd.getSize().width;
       
                      if(_dx-px<=-_length){
 			_speed=0;
 			_brd.stopBGM(as);
 			//_brd.playBGM(bs);
                        // _brd.LoadNext();
 		}
               /*
                 if(_dx == -300){
                      _brd.addEnemy(new Enemy(_brd,1,"er",".png",px,400,96,45));
                      _brd.addEnemy(new Enemy(_brd,3,"e3l",".png",px,300,32,16));
                      _brd.addEnemy(new Enemy(_brd,3,"e3l",".png",px,600,32,16));
                 }
                
                 if(_dx == -800){
                      _brd.addEnemy(new Enemy(_brd,3,"e3l",".png",px,200,32,16));
                      _brd.addEnemy(new Enemy(_brd,3,"e3l",".png",px,700,32,16));
                 }
                 if(_dx == -1000){
                      _brd.addEnemy(new Enemy(_brd,1,"er",".png",px,400,96,45));
                      _brd.addEnemy(new Enemy(_brd,1,"er",".png",px,300,96,45));
                      _brd.addEnemy(new Enemy(_brd,1,"er",".png",px,600,96,45));
                 }
                */
     }
 }
