 package pure_mp3;
 
 /**
  * Write a description of class PlayList here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 import java.awt.Color;
 import java.awt.dnd.DropTarget;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 //import java.io.File;
 //import java.io.FileNotFoundException;
 //import java.io.FileReader;
 //import java.io.FilenameFilter;
 import java.util.Random;
 //import java.io.IOException;
 
 import javax.swing.DefaultListModel;
 import javax.swing.DropMode;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 //import javax.swing.event.ListDataEvent;
 //import javax.swing.event.ListDataListener;
 public class PlayList extends JScrollPane
 {
 	private static final long serialVersionUID = 2385007980763532219L;
     private JList list;
     private DropTarget dropTarget;
     private DefaultListModel model;
     private int current;
     
     public PlayList()
     {
         super();
         setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         
         Global.setPlayList(this);
         current = 0;        
         list = new JList();
         list.setLocation(0,0);
         model = new MyListModel(this);
         
         list.setModel(model);
         list.setDragEnabled(true);
         list.setDropMode(DropMode.INSERT);
         list.setTransferHandler(new ListMoveTransferHandler());
         list.setBackground(Color.WHITE);
         list.setCellRenderer(new PlayListRenderer());
         
         list.addMouseListener(new MouseAdapter() 
         {
             public synchronized void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2)
                 {
                 	try
                 	{
                 		playSelected(((JList)e.getSource()).locationToIndex(e.getPoint()));
                 	}
                 	catch(ArrayIndexOutOfBoundsException ex)
                 	{
                 	}
                 }
              }
         });
         
         list.addKeyListener(new KeyListener() 
         {
         	public void keyPressed(KeyEvent event)
         	{
         		int selectAfter = 0;
         		if(event.getKeyCode() == KeyEvent.VK_DELETE)
         		{
         			int selected[] = list.getSelectedIndices();
         			if(selected.length >= 0)
         			{
         				selectAfter = selected[0];
         				if(selectAfter < 0)
             			{
             				selectAfter = 0;
             			}
         			}
         			int killCount = 0;
         			for(int i = 0; i < selected.length; i++)
         			{
 	        			if(selected[i] == current && Global.player.isPlaying())
 	        			{
 	        				prev();
 	//        				boolean paused = Global.player.isPaused();
 	//        				Global.player.stop();
 	//        				next();
 	//        				Global.player.playpause();
 	//        				if(paused)
 	//        				{
 	//        					Global.player.playpause();
 	//        				}
 	        			}
 	        			if(selected[i] != -1)
 	        			{
 	        				model.remove(selected[i] - killCount);
 	        				killCount++;	        				
 	        			}	        			
         			}
         			list.setSelectedIndex(selectAfter);
         			if(list.isSelectionEmpty())
         			{
         				list.setSelectedIndex(0);
         			}
         			list.ensureIndexIsVisible(list.getSelectedIndex());
         			if(current > selected[selected.length-1])
         			{
         				current = current - selected[selected.length-1];
         			}
         		}
         	}  
         	
         	public void keyReleased(KeyEvent event)
         	{
         		//Do nothing.
         	}  
         	
         	public void keyTyped(KeyEvent event)
         	{
         		//Do nothing.
         	}
         });
         
         list.setDropTarget(new DropTarget(Global.playList.getList(),new PlayListDropTargetListener()));
 
         list.setSelectedIndex(current);
         add(list);        
         setViewportView(list);
     }    
     
     public void next()
     {
         setCurrent(current+1);
     }
     
     public void prev()
     {
         setCurrent(current-1);
     }
     
     public void random()
     {
     	Random random = new Random();
     	if(model.getSize() > 0)
     	{
     		setCurrent(random.nextInt(model.getSize())-1);
     	}
     }
     
     public void playSelected(int index)
     {
         setCurrent(index);
         Global.player.stop();
        Global.player.playpause(true);
     }
     
     public void setCurrent(int xCurrent)
     {
     	if(model.getSize() > 0)
     	{
 	        if(xCurrent < 0)
 	        {
 	            current = model.getSize()-1;
 	        }
 	        else if(xCurrent+1 <= model.getSize() && xCurrent > 0)
 	        {
 	            current = xCurrent;
 	        }
 	        else
 	        {
 	            current = 0;
 	        }
 	        if(model.get(current)!=null)
 	        {
 	            list.setSelectedIndex(current);
 	            list.ensureIndexIsVisible(current);	
 	        }
     	}
     }
     
     public void setDropTargetActive(boolean isActive)
     {
     	dropTarget.setActive(isActive);
     }
     
     
     public int getCurrent()
     {
         return current;
     }
     
     public int getNumberOfSongs()
     {
     	return model.getSize();
     }
     
     public void checkCurrent(int xCurrent)
     {
     	System.out.println("checking Current");
     	Song currentSong = Global.player.getCurrentSong();
     	if(this.getCurrentSong().getSource()!=currentSong.getSource() && Global.player.isPlaying())
     	{
     		setCurrent(xCurrent);
     	}
     }
     
     public void checkCurrentNegative()
     {
     	if(current == -1 && model.getSize() > 0)
     	{
     		next();
     	}
     }
     
     public Song getCurrentSong()
     {
     	if(model.getSize() > 0)
     	{
     		return (Song)model.get(current);
     	}
     	return null;
     }
     
     public String getArtist()
     {
     	if(model.getSize() > 0)
     	{
 	        if(model.get(current)!=null)
 	        {
 	            return ((Song)model.get(current)).getArtist();
 	        }
 	        else
 	        {
 	            return "";
 	        }
     	}
     	return "";
     }
     
     public String getTitle()
     {
     	if(model.getSize() > 0)
     	{
 	        if(model.get(current)!=null)
 	        {
 	            return ((Song)model.get(current)).getTitle();
 	        }
 	        else
 	        {
 	            return "";
 	        }
     	}
     	return "";
     }
     
     public String getAlbum()
     {
     	if(model.getSize() > 0)
     	{
 	        if(model.get(current)!=null)
 	        {
 	            return ((Song)model.get(current)).getAlbum();
 	        }
 	        else
 	        {
 	            return "";
 	        }
     	}
     	return "";
     }
     
     public String getLength()
     {
     	if(model.getSize() > 0)
     	{
 	        if(model.get(current)!=null)
 	        {
 	            return ((Song)model.get(current)).getLength();
 	        }
 	        else
 	        {
 	            return "";
 	        }
     	}
     	return "";
     }
     
     public void addSong(Song song)
     {
     	System.out.println(song.getSource().toString());
     	model.ensureCapacity(model.getSize()+1);
     	if(song != null)
     	{
     		model.addElement(song);
     	}
     }
     
     public void addSongAt(Song song, int position)
     {
     	System.out.println(song.getSource().toString() + " at " + position);
     	model.ensureCapacity(model.getSize()+1);
     	if(song != null)
     	{
     		model.add(position,song);
     	}
     }
     
     public void removeAllElements()
     {
     	model.removeAllElements();
     	current = -1;
     }
     
     public DefaultListModel getModel()
     {
     	return model;
     }
     
     public JList getList()
     {
     	return list;
     }
     
     public int getModelSize()
     {
     	return model.getSize();
     }
     
 }
