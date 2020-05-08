 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
 *  
  *  (C) Liam Byrne, 2008 - 09.
  */
 
 package gui;
 
 import java.awt.Dimension;
import java.awt.Insets;
 import java.awt.Toolkit;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import logic.Game;
 
 
 public class Application {
    
    public static void main(String... args) {
       boolean debugTimes = false;
       boolean debugPath = false;
       if(args.length > 0) {
          List<String> argsList = Arrays.asList(args);
          if(argsList.contains("--debugTimes")) {
             debugTimes = true;
          }
          if(argsList.contains("--debugPath")) {
             debugPath = true;
          }
          if(argsList.contains("--debugAll")) {
             debugTimes = true;
             debugPath = true;
          }
       }
       JFrame frame = new JFrame("Pac Defence");
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setResizable(false);      
       new Game(frame, debugTimes, debugPath);
       frame.pack();
       Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
       // Centres the frame on screen
       frame.setLocation((d.width - frame.getWidth())/2, (d.height - frame.getHeight())/2);
       frame.setVisible(true);
      // Need this to set the frame to the right size under openjdk...
      Insets ins = frame.getInsets();
      frame.setSize(Game.WIDTH + ins.left + ins.right, Game.HEIGHT + ins.top + ins.bottom);
    }
 
 }
