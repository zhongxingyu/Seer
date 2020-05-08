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
 
import java.awt.BorderLayout;
 import java.awt.Dimension;
import java.awt.Graphics;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
import net.miginfocom.swing.MigLayout;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Line;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.SourceDataLine;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UnsupportedLookAndFeelException;
 //import javax.swing.UIManager;
 //import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.UIManager;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 import com.seaglasslookandfeel.SeaGlassLookAndFeel;
 
 public class GUI extends JFrame
 {
 	private static final long serialVersionUID = 2385007980763532219L;
     private MainPanel mainPanel;    
     private static GUI gui;
     
     public GUI()
     {
         super("pure.mp3");
         Global.setGUI(this);
         init();
     }
     
     private void init()
     {
         SwingUtilities.invokeLater(new Runnable(){
              public void run() 
              {
            	 	setMinimumSize(new Dimension(420,300));
                     mainPanel = new MainPanel();
                     setContentPane(mainPanel);
                     setJMenuBar(new Menu());
                     System.setProperty("apple.laf.useScreenMenuBar", "true");
                     System.setProperty("com.apple.mrj.application.apple.menu.about.name", "pure.mp3");
                     setDefaultCloseOperation(EXIT_ON_CLOSE);   
                     setResizable(true);
                    pack();
                    setSize(800,600); //Has to be done after the Window has been layouted to prevent a bug under Windows
                    setLocationRelativeTo(null);
                     setVisible(true); 
             }
         });
     }
     
     public static void main (String args[])
     {
     	boolean support = false;
 		System.out.println("Available Mixers:");
 		Mixer.Info[]	aInfos = AudioSystem.getMixerInfo();
 		for (int i = 0; i < aInfos.length; i++)			
 		{
 			Mixer mixer = AudioSystem.getMixer(aInfos[i]);
 			Line.Info lineInfo = new Line.Info(SourceDataLine.class);
 			if (mixer.isLineSupported(lineInfo))
 			{
 				System.out.println(aInfos[i].getName());
 				support = true;
 			}
 			else
 			{
 				System.out.println("Not supported: " + aInfos[i].getName());
 			}
 		}
 		if (aInfos.length == 0)
 		{
 			System.out.println("[No mixers available]");
 			System.exit(ERROR);
 		}
 		if(support)
 		{
 			try 
     	 	{
 				Object choices[] = {"Swing","SeaGlass"};
     	 		System.out.println(System.getProperty("os.name"));
     	 		try
     	 		{
     	 			BufferedReader buffread = new BufferedReader(new FileReader(new File("puremp3","config.txt")));
     	 			String laf = buffread.readLine();
     	 			if(laf.equals("Swing"))
     	 			{
     	 				UIManager.setLookAndFeel(new MetalLookAndFeel());
     	 			}
     	 			else if(laf.equals("SeaGlass"))
     	 			{
     	 				UIManager.setLookAndFeel(new SeaGlassLookAndFeel());
     	 			}
     	 			else
     	 			{
     	 				throw new Exception();
     	 			}
     	 		}
     	 		catch(Exception e)
     	 		{
     	 			PrintWriter writer = null;
     	 			int answer = JOptionPane.showOptionDialog(
     	 					null,
     	 					"Do you want to use Swing or SeaGlass?\nOn slow systems please use Swing!", 
     	 					"Which Look and Feel do you want?" , 
     	 					JOptionPane.DEFAULT_OPTION, 
     	 					JOptionPane.QUESTION_MESSAGE, 
     	 					null, 
     	 					choices,
     	 					choices[0]);
     	 			try
     	 			{
     	 				new File("puremp3").mkdir();
     	 				writer = new PrintWriter(new FileWriter(new File("puremp3","config.txt")));
     	 			}
     	 			catch(Exception ex)
     	 			{
     	 				System.out.println("Failed saving LookAndFeel!");
     	 			}
 		        	if(answer == 0)
 		        	{
 		    	        UIManager.setLookAndFeel(new MetalLookAndFeel());
 		    	        if(writer != null)
 		    	        {
 		    	        	writer.println("Swing");
 		    	        	writer.flush();
 		    	        	writer.close();
 		    	        }
 		        	}
 		        	else if(answer == 1)
 		        	{
 		        		UIManager.setLookAndFeel(new SeaGlassLookAndFeel());
 		        		if(writer != null)
 		    	        {
 		    	        	writer.println("SeaGlass");
 		    	        	writer.flush();
 		    	        	writer.close();
 		    	        }
 		        	}
 		        	else
 		        	{
 		        		System.exit(0);
 		        	}
     	 		}    	 		
     	    } 
     	    catch (UnsupportedLookAndFeelException e)
     	    {
     	       System.out.println("Something is wrong with your Look and Feel!!!");
     	    }
 			gui = new GUI();
 		}
 		else
 		{
 			System.out.println("Sound playback not supported properly!");
 			System.exit(ERROR);
 		}
     
 	}
    
    public void invalidate()
    {
    	super.invalidate();
    	System.out.println("ABC");
    }
 }
