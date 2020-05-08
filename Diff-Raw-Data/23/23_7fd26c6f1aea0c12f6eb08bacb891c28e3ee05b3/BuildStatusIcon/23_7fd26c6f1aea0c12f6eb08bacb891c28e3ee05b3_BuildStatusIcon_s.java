 import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import javax.swing.ImageIcon;
 
 
 public class BuildStatusIcon implements ActionListener{
 
 	final String BLUE = "images/blue.png";
 	final String BLUE_ANIME = "images/blue_anime.gif";
 	final String RED = "images/red.png";
 	final String RED_ANIME = "images/red_anime.gif";
 	final String YELLOW = "images/yellow.png";
 	final String GREY = "images/grey.png";
 	
 	final int UNSTABLE = -1;
 	final int PENDING = 0;
 	final int SUCCESS = 1;
 	final int SUCCESS_BUILDING = 2;
 	final int FAILED = 3;
 	final int FAILED_BUILDING = 4;
 	
 	TrayIcon trayIcon;
 	String buildName;
 	final PopupMenu popup = new PopupMenu();
     MenuItem exitItem = new MenuItem("Exit");
     
     
 	public BuildStatusIcon(Build build) 
 	{
 		buildName = build.name;
 		  //Check the SystemTray is supported
         if (!SystemTray.isSupported())
         {
             System.out.println("SystemTray is not supported");
             return;
         }
         
         trayIcon = new TrayIcon(createImage("images/grey.png", buildName));
         this.setStatus(build.condition);
         trayIcon.setImageAutoSize(true);
         trayIcon.setToolTip(buildName);
         final SystemTray tray = SystemTray.getSystemTray();
        
         try
         {
         	exitItem.addActionListener(this);
         	
         	//popup.addSeparator();
         	popup.add(exitItem);
 
             trayIcon.setPopupMenu(popup);
             tray.add(trayIcon);
         } 
         catch (AWTException e)
         {
             System.out.println("TrayIcon could not be added.");
         }
 	}
 	
 	
     //Obtain the image URL
     protected static Image createImage(String path, String description)
     {
         URL imageURL = CiStatus.class.getResource(path);
         
         if (imageURL == null)
         {
             System.err.println("Resource not found: " + path);
             return null;
         } else {
             return (new ImageIcon(imageURL, description)).getImage();
         }
     }
     
     public void setStatus(int condition)
     {
     	Image image = createImage(YELLOW, "Unknown");
     	
     	switch(condition)
     	{
     	case UNSTABLE:
     		image = createImage(YELLOW, "Unstable");
     		break;
     	case SUCCESS_BUILDING:
     		image = createImage(BLUE_ANIME, "Unknown");
     		break;
     	case SUCCESS:
     		image = createImage(BLUE, "Unknown");
     		break;
     	case FAILED:
     		image = createImage(RED, "Unknown");
     		break;
     	case FAILED_BUILDING:
     		image = createImage(RED_ANIME, "Unknown");
     		break;	
     	case PENDING:
     		image = createImage(GREY, "Unknown");
     		break;
     	}
    	
     	trayIcon.setImage(image);
     }
     
     public String getBuildName()
     {
     	return buildName;
     }   
 
     private void close()
     {
     	System.exit(0);
     }
 
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		close();
 	}
 }
