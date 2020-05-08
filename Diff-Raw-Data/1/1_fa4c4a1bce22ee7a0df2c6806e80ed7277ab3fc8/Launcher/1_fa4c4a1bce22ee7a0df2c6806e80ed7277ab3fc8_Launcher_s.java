 package it.planetgeeks.mclauncher;
 
 import it.planetgeeks.mclauncher.frames.LauncherFrame;
 import it.planetgeeks.mclauncher.frames.MemoryFrame;
 import it.planetgeeks.mclauncher.frames.ProfileFrame;
 import it.planetgeeks.mclauncher.updater.LauncherUpdater;
 import it.planetgeeks.mclauncher.utils.LanguageUtils;
import it.planetgeeks.mclauncher.utils.Memory;
 import it.planetgeeks.mclauncher.utils.MemoryUtils;
 import it.planetgeeks.mclauncher.utils.Profile;
 import it.planetgeeks.mclauncher.utils.ProfilesUtils;
 
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 public class Launcher
 {
 	private static LauncherFrame launcherFrame;
 	private static ProfileFrame profileFrame;
 	private static MemoryFrame memoryFrame;
 	
     public static void main(String[]args)
     {
     	if(args.length > 0 && args[0].equals("start"))
     	{
     		LanguageUtils.loadLanguages();
         	loadLookAndFeel();
         	ProfilesUtils.loadProfiles();
         	MemoryUtils.loadMemories();
         	launcherFrame = new LauncherFrame();
         	launcherFrame.setVisible(true);
     	}
     	else
     	{
     		LauncherUpdater.startCheck();
     	}
     }
     
     private static void loadLookAndFeel()
     {
     	try
 		{
 			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
 			{
 				if ("Nimbus".equals(info.getName()))
 				{
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		}
 		catch (ClassNotFoundException ex)
 		{
 			java.util.logging.Logger.getLogger(LauncherFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		catch (InstantiationException ex)
 		{
 			java.util.logging.Logger.getLogger(LauncherFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		catch (IllegalAccessException ex)
 		{
 			java.util.logging.Logger.getLogger(LauncherFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		catch (UnsupportedLookAndFeelException ex)
 		{
 			java.util.logging.Logger.getLogger(LauncherFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
     }
     
     public static void openProfileEditor(Profile profile)
     {
     	profileFrame = new ProfileFrame(profile);
     	profileFrame.setVisible(true);
     }  
     
     public static void openMemoryEditor(int parent, Object extra)
     {
     	memoryFrame = new MemoryFrame(parent, extra);
     	memoryFrame.setVisible(true);
     }  
     
     public static LauncherFrame getLauncherFrame()
     {
     	return launcherFrame;
     }
 }
