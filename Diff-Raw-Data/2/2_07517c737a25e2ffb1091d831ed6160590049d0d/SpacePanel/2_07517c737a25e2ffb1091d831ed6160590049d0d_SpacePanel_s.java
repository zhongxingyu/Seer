 import java.awt.Color;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class SpacePanel extends JPanel
 {
   public static final int DEFAULT_WINDOW_SIZE = 740;
   public static final int DEFAULT_SIZE = 35;
   private static final int SCALAR = 1;
   private static final int SHIFT  = DEFAULT_WINDOW_SIZE/2;
   private Player           player;
   private Location         currentLocation;
   private JLabel           locationName;
 
   public SpacePanel(Player player)
   {
     this.player = player;
     this.setBackground(Color.BLACK);
     locationName = new JLabel("");
     populateSpace();
   }
 
   public void populateSpace()
   {
     this.removeAll();
     this.setLayout(null);
     this.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
     currentLocation = player.getLoc();
     Location[] locations = currentLocation.getChild();
     locationName.setBounds(200, 0, 200, 30);
     
     JButton optionsButton = new JButton();
	optionsButton.setLocation(702, 0);
 	optionsButton.setSize(38, 38);
 	optionsButton.setIcon(new ImageIcon("Art" + File.separator + "OptionButton.png"));
 	optionsButton.setToolTipText("Options Menu");
 	optionsButton.addActionListener(new ActionListener(){
 		 public void actionPerformed(ActionEvent arg0)
 	      {
 			 UWOptionsPanel optionWindow = new UWOptionsPanel();
 				optionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				//optionWindow.setLocation(
 				//		(int) (this.getLocation().getX() + (this.getWidth() / 4)),
 				//		(int) (this.getLocation().getY() + (this.getHeight() / 3)));
 				optionWindow.setSize(400, 200);
 				optionWindow.setVisible(true);
 				optionWindow.setResizable(false);
 	      }
 	});
     
     JButton back = new JButton();
     back.setIcon(new ImageIcon("Art" + File.separator + "BackButton.png"));
     back.setOpaque(false);
     back.setBorderPainted(false);
     back.setContentAreaFilled(false);
     back.setToolTipText("Return to " + currentLocation.getParent());
     back.addActionListener(new ActionListener()
     {
 
       @Override
       public void actionPerformed(ActionEvent arg0)
       {
         player.setLoc(currentLocation.getParent());
         populateSpace();
       }
 
     });
     back.setLocation(0, 0);
     back.setSize(38, 38);
     back.setVisible(true);
     switch (currentLocation.whatAmI())
     {
     case 4:
       locationName.setText("Orbital");
       break;
     case 3:
       locationName.setText("Star System");
       break;
     case 2:
       locationName.setText("Sector");
       break;
     case 1:
       locationName.setText("Cluster");
       back.setVisible(false);
       break;
     default:
       locationName.setText("Error");
     }
     locationName.setBackground(Color.WHITE);
     this.add(locationName);
     this.add(optionsButton);
     this.add(back);
     if (locations != null)
     {
       
       for (int i = 0; i < locations.length; i++)
       {
         Location loc = locations[i];
         JButton b = new JButton();
 /*        if(currentLocation.whatAmI() == 1){
         	b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY() + SHIFT,
                     100, 100);
         	b.setIcon(new ImageIcon(loc.GetPic(75)));
         }else{
         	b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY() + SHIFT,
                     50, 50);
         	b.setIcon(new ImageIcon(loc.GetPic(DEFAULT_SIZE)));
         }*/
         b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY() + SHIFT, 50, 50);
         b.setIcon(new ImageIcon(loc.GetNavImage()));
         b.setBackground(Color.BLUE);
         LocationListener handler = new LocationListener(i);
         b.addActionListener(handler);
         b.setOpaque(false);
         b.setBorderPainted(false);
         b.setContentAreaFilled(false);
         b.setToolTipText("<HTML>"
         				+ "Name: " + loc.toString() 
         				+ "<BR />"
         				+ "Type: "
         				+ "<BR />"
         				+ "Location: "+ loc.getParent().toString() 
         				+ "</HTML>");
 
         this.add(b);
       }
       renderScene(locations);
       repaint();
     } else
     {
       
       TradingMenu menu = new TradingMenu(player);
       menu.setSize(650, 230);
       menu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       menu.setResizable(false);
       menu.setVisible(true);
       player.setLoc(currentLocation.getParent());
       populateSpace();
     }
   }
   public void renderScene(Location[] locations)
   {
 	  for(int j = 0; j < locations.length; j++)
 	  {
 		  Location loc = locations[j];
 		  JLabel p;
 		  if(loc.getClass() != Ring.class)
 		  {
 			  p = new JLabel(new ImageIcon(loc.GetPic(DEFAULT_SIZE)));
 			  p.setBackground(Color.BLACK);
 			  p.setOpaque(false);
 			  int length = loc.GetPic().getWidth();
 			  p.setBounds(	(int)(loc.GetX()+SHIFT-(length/3)),
 					  		(int)(loc.GetY()+SHIFT-(length/3)),
 					  		length, length);
 			  this.add(p);
 		  }
 	  }
 	  for(int j = 0; j < locations.length; j++) //Rings need to be rendered after planets
 	  {
 		  Location loc = locations[j];
 		  JLabel p;
 		  if(loc.getClass() == Ring.class)
 		  {
 			  int length = 250 + (j*60);
 			  p = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(loc.GetPic(),length)));
 			  p.setBackground(Color.BLACK);
 			  p.setOpaque(false);
 			  p.setBounds(	(int)(SHIFT-(length/2)),
 					  		(int)(SHIFT-(length/2)),
 					  		length, length);
 			  this.add(p);
 		  }
 	  }
 	  if (locations[0].getParent().whatAmI() == 1)
 	  {
 		  JLabel singularity = new JLabel(new ImageIcon(locations[0].getParent().GetCenterImage()));
 		  singularity.setBounds(SHIFT - 64, SHIFT - 64,	128, 128);
 		  this.add(singularity);
 	  }
 	  if (locations[0].getParent().whatAmI() == 3)
 	  {
 		  
 		  JLabel center = new JLabel(new ImageIcon(locations[0].GetCenterImage()));
 		  center.setBounds(	SHIFT - (locations[0].GetCenterImage().getWidth()/2), 
 				  			SHIFT - (locations[0].GetCenterImage().getHeight()/2), 
 				  			locations[0].GetCenterImage().getWidth(),
 				  			locations[0].GetCenterImage().getHeight());
 		  this.add(center);
 	  }
 	  else
 	  {
 		  JLabel center = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(locations[0].GetCenterImage(),DEFAULT_WINDOW_SIZE) ));
 		  center.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 		  this.add(center);
 	  }
 	  
 	  JLabel bg = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(locations[0].GetBGImage(),DEFAULT_WINDOW_SIZE)));
 	  bg.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 	  this.add(bg);
   }
 
   private class LocationListener implements ActionListener
   {
 
     private int index;          // The index of the child location
 
     public LocationListener(int index)
     {
       this.setIndex(index);
     }
 
     @Override
     public void actionPerformed(ActionEvent e)
     {
       player.setLoc(currentLocation.getChild(index));
       populateSpace();
     }
 
     public int getIndex()
     {
       return index;
     }
 
     public void setIndex(int index)
     {
       this.index = index;
     }
 
   }
 }
