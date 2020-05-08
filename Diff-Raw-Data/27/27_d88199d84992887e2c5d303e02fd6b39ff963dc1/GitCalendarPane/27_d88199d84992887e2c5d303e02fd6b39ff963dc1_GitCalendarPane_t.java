 package com.brotherlogic.memory.ui.calendar;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 
 import javax.swing.JFrame;
 
 import com.brotherlogic.memory.core.GitMemory;
 import com.brotherlogic.memory.core.Memory;
 import com.brotherlogic.memory.db.DBFactory;
 
 /**
  * UI which displays checkin thingy
  * 
  * @author simon
  * 
  */
 public class GitCalendarPane extends CalendarPane
 {
    public static void main(String[] args)
    {
       JFrame framer = new JFrame();
       GitCalendarPane pane = new GitCalendarPane();
 
       framer.setSize(500, 500);
       framer.setLocationRelativeTo(null);
       framer.getContentPane().add(pane);
       framer.setVisible(true);
 
       framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
    DateFormat df = DateFormat.getDateInstance();
 
    @Override
    protected String getRowSummary(final int row)
    {
       return null;
    }
 
    @Override
    protected void paintBox(Graphics g, Calendar date, int x, int y, int width, int height, int row,
          int day)
    {
       // Draw an x if we've checked some code in that day
       try
       {
          Collection<Memory> memories = DBFactory.buildInterface().retrieveMemories(date,
                GitMemory.class.getName());
 
          if (memories.size() > 0)
          {
             boolean found = false;
             for (Memory mem : memories)
             {
                System.out.println("HERE: " + mem);
               if (((GitMemory) mem).getBranch().equals("refs/heads/master"))
                   found = true;
             }
             if (found)
             {
                Color oldColor = g.getColor();
                g.setColor(Color.red);
                g.drawLine(x, y, x + width, y + height);
                g.drawLine(x + width, y, x, y + height);
                g.setColor(oldColor);
             }
          }
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 }
