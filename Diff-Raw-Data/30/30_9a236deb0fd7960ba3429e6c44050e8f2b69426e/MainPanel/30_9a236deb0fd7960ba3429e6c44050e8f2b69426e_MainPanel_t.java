  /**
  * @author Martin Braun
  *   
  * This file is part of pure.mp3.
  *
  *  pure.mp3 is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  pure.mp3 is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with pure.mp3.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pure_mp3;
 
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 
 import net.miginfocom.swing.MigLayout;
 public class MainPanel extends JPanel
 {
 	private static final long serialVersionUID = 2385007980763532219L;
     private PlayerMenu playerMenu;
     private Info info;
     private PlayList playList;
     private Player player;
     private JSlider progress;
     private Media media;
     
     public MainPanel()
     {
         setLayout(new MigLayout("insets 5 5 5 5, nogrid, nocache"));
         
         playerMenu = new PlayerMenu();
         add(playerMenu, "pos 5 5 n info.y2, id playerMenu");
         
         info = new Info();
         add(info,"x (playerMenu.x2 + 5), x2 (playList.x - 5), y 5, id info"); 
         
         playList = new PlayList();
         add(playList, "pos (66% - 5) 5 (100% - 5) (100% -5), id playList");
         
         progress = new Progress();
         add(progress,"pos 5 (info.y2 + 5) (playList.x - 5) n, id progress");
         
         media = new Media();
        add(media,"pos 5 (progress.y2 + 5) (playList.x - 5) (100% - 5), id media");
        repaint();
     }
     
     public PlayList getPlayList()
     {
     	return playList;
     }
     
     public Player getPlayer()
     {
     	return player;
     }
 }
