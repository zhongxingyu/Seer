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
 package org.mgenterprises.java.bukkit.gmcfps.Core.BasicCommands;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.GameManager;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class CommandRegisterer {
     private JavaPlugin plugin;
     private GameManager gm;
     
     public CommandRegisterer(JavaPlugin plugin, GameManager gm){
         this.plugin = plugin;
         this.gm = gm;
        init();
     }
     
    private void init(){
         GameManagementCommands gmc = new GameManagementCommands(gm);
         ScoreCommands sc = new ScoreCommands(gm);
         plugin.getCommand(Commands.JOIN.toString()).setExecutor(gmc);
         plugin.getCommand(Commands.LEAVE.toString()).setExecutor(gmc);
         
         plugin.getCommand(Commands.TOP.toString()).setExecutor(sc);
         plugin.getCommand(Commands.SCORE.toString()).setExecutor(sc);
         
         plugin.getCommand(Commands.GAME.toString()).setExecutor(gmc);
     }
 }
