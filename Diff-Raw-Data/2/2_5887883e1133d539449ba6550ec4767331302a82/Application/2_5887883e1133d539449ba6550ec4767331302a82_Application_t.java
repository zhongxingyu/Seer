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
  *  (C) Liam Byrne, 2008 - 10.
  */
 
 package gui;
 
 import jargs.CmdLineParser;
 import jargs.CmdLineParser.Option;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 
 import logic.Game;
 import logic.MyExecutor;
 
 
 public class Application {
    
    public static void main(String... args) {
       CmdLineParser parser = new CmdLineParser();
       
       Option debugTimesOption = parser.addBooleanOption('d', "debugTimes");
       Option debugPathOption = parser.addBooleanOption("debugPath");
      Option threadsOption = parser.addIntegerOption('t', "threads");
       
       try {
          parser.parse(args);
       } catch(CmdLineParser.OptionException e) {
          System.err.println(e.getMessage());
          System.exit(1);
       }
       
       boolean debugTimes = (Boolean) parser.getOptionValue(debugTimesOption, false);
       boolean debugPath = (Boolean) parser.getOptionValue(debugPathOption, false);
       int numThreads = (Integer) parser.getOptionValue(threadsOption, 0);
       
       // Let a negative or zero value imply to use the default
       if(numThreads > 0) {
          MyExecutor.setNumThreads(numThreads);
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
    }
 
 }
