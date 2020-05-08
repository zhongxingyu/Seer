 /*
  * The MIT License
  *
  * Copyright 2013 Manuel Gauto.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement;
 
 import org.bukkit.entity.Player;
 import org.mgenterprises.java.bukkit.gmcfps.Core.FPSCore;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class Game {
     private String name;
     private FPSCore core;
     private int maxSize = 25;
     private int scoreCap = 30;
     
    public Game(String name){
         this.name = name;
         this.core = new FPSCore(this);
     }
     
     public FPSCore getFPSCore(){
         return this.core;
     }
     
     public String getName(){
         return this.name;
     }
     
     public int getSize(){
         return this.core.getTeamManager().getAllPlayers().length;
     }
     
     public void setMaxSize(int numPlayers){
         this.maxSize = numPlayers;
     }
     
     public int getMaxSize(){
         return this.maxSize;
     }
     
     public boolean isJoinable(){
         if(getSize()<maxSize){
             return true;
         }
         else{
             return false;
         }
     }
     
     public boolean registerPlayer(Player p){
         if(isJoinable()){
             core.getTeamManager().registerPlayer(p);
             return true;
         }
         return false;
     }
     
     public void unregisterPlayer(Player p){
         core.getTeamManager().unregisterPlayer(p);
     }
     
     public int getScoreCap(){
         return this.scoreCap;
     }
     
     public void setScoreCap(int cap){
         this.scoreCap = cap;
     }
 }
