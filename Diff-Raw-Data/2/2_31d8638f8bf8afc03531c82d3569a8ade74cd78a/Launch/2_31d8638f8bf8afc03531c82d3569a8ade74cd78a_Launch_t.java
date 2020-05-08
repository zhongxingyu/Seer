 /*
  *  This file is part of pure.mp3.
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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Line;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.SourceDataLine;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 /**
  * Launching Class
  * @author Martin Braun
 */
 public class Launch 
 {
 	@SuppressWarnings("unused")
 	private static GUI gui;
 	
 	/**
 	 * Main Method for pure.mp3. Looks wheter there are mixers that are supported
 	 * and if they exist starts the program. At the first startup the User can choose
 	 * the Look And Feel of the Program (Metal or Nimbus, if neither is available,
 	 * the program uses the default LaF). This will be saved in puremp3/config.txt in the
 	 * current directory.
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main (String args[]) throws Exception
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
 			System.exit(-1);
 		}
 		if(!support)
 		{
 			JOptionPane.showMessageDialog(null,"Sound playback not supported properly!");
 		}
 		else
 		{
 			File config = new File("puremp3","config.txt");
 			Object choices[] = {"Metal","Nimbus"};			
 	 		System.out.println(System.getProperty("os.name"));
 	 		if(config.exists())
 	 		{
 	 			boolean success = false;
 	 			BufferedReader buffread = new BufferedReader(new FileReader(config));
 	 			String laf = buffread.readLine();
 	 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
     		    {
     		        if (laf.equals(info.getName())) 
     		        {
     		            UIManager.setLookAndFeel(info.getClassName());
     		            success = true;
     		            break;
     		        }
     		    }
 	        	if(!success)
 	            {
 	        		JOptionPane.showMessageDialog(null,"The Default LaF has been chosen, because your System doesn't support your choice.\nPlease update your config.txt");
 	        	}
 	 		}
 	 		else
 	 		{
 	 			PrintWriter writer = null;
 	 			int answer = JOptionPane.showOptionDialog(
 	 					null,
 	 					"Do you want to use Metal or Nimbus?", 
 	 					"Which Look and Feel do you want?" , 
 	 					JOptionPane.DEFAULT_OPTION, 
 	 					JOptionPane.QUESTION_MESSAGE, 
 	 					null, 
 	 					choices,
 	 					choices[0]);
 	 			try
 	 			{
 	 				new File("puremp3").mkdir();
 	 				writer = new PrintWriter(new FileWriter(config));
 	 			}
 	 			catch(Exception ex)
 	 			{
 	 				System.out.println("Failed Creating the PrintWriter");
 	 			}
 	        	if(answer == 0 || answer == 1)
 	        	{
 	        		String laf = "";
 	        		if(answer == 0)
 		        	{
 		        		laf = "Metal";
 		        	}
 		        	else
 		        	{
 		        		laf = "Nimbus";
 		        	}
 		        	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
 	    		    {
 	    		        if (laf.equals(info.getName())) 
 	    		        {
 	    		            UIManager.setLookAndFeel(info.getClassName());
	    		            writer.println(laf);
 		    	        	writer.flush();
 		    	        	writer.close();
 	    		            break;
 	    		        }
 	    		    }
 	        	}
 	        	else
 	        	{
 	        		JOptionPane.showMessageDialog(null,"The Default LaF has been chosen, because your System doesn't support your choice.\nPlease update your config.txt");
 	        	}
     	    }
 //	 		/*
 //	 		 * Load some User specified .jars 
 //	 		 */
 //	 		File pluginDirectory = new File("puremp3","plugins");
 //	 		if(pluginDirectory.exists())
 //	 		{
 //		 		List<URL> url = new ArrayList<URL>();
 //		 		for(String plugin: pluginDirectory.list())
 //		 		{
 //			 		if (plugin.contains(".jar")) {
 //			 		url.add(new URL("file", "localhost",
 //			 		pluginDirectory.getCanonicalPath() + File.separator
 //			 		+ plugin));
 //		 		} 
 //			 	else 
 //			 	{
 //			 		url.add(new URL("file", "localhost",
 //			 		pluginDirectory.getCanonicalPath() + File.separator
 //			 		+ plugin + File.separator));
 //		 		}
 //		 		}
 //		 		URLClassLoader ucl = new URLClassLoader(url.toArray(new URL[0]));
 //	 		}
 	 		gui = new GUI();
 		}
 	}
 
 }
