 package me.hammale.Sewer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericOverlayScreen;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericScreen;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.InGameHUD;
 import org.getspout.spoutapi.gui.InGameScreen;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 public class SewerCommandListener implements CommandExecutor {
     
 	private final SewerGenerator sewergen = new SewerGenerator();
 	private final bridge bridge = new bridge();
 	private final Grave grave = new Grave();
 	private final spiderNest nest = new spiderNest();
 	private final CaveIn cavein = new CaveIn();
 	public ArrayList<Integer> distance = new ArrayList<Integer>();
 	public Location end;
 	
 	@SuppressWarnings("unused")
 	private Sewer plugin;
  
 	public SewerCommandListener(Sewer plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		
 		Player p = null;
 		if (sender instanceof Player) {
 			p = (Player) sender;
 		}
 		
 		if (command.getName().equalsIgnoreCase("sewer")) {
 			
 			if(args.length == 1){
 			String arg = args[0];
 			if(arg.equalsIgnoreCase("create")){		
 			if (p == null) {
 				sender.sendMessage("[Sewers] This command can only be run by a player!");
 			} else if (p.isOp()) {
 				p.sendMessage(ChatColor.GREEN + "Generating Sewer...");
 				Block b = p.getLocation().getBlock();
 				b.setType(Material.AIR);
 				Material m = (Material.SMOOTH_BRICK);
 				BlockFace bf = BlockFace.NORTH;
 				Block set2 = b;
 				//sewergen.start1(set2, m, bf);
 				sewergen.apreview(set2, m, bf);
 				//nest.nest1(set2, m, bf);
 				//bridge.nsbridge(set2, m, bf);
 				//cavein.cavein1(set2, m, bf);
 				//grave.grave(set2, m, bf);
 				p.sendMessage(ChatColor.GREEN + "Sewer Complete!");
 										
 			return true;
 			}
 			else {
 				sender.sendMessage("This command can opnly be run by an OP!");
 			}
 		}
 			
 			if(arg.equalsIgnoreCase("locate")){
 				if (p == null) {
 					sender.sendMessage("This command can only be run by a player!");
 				} else if (p.isOp()) {
 					p.sendMessage(ChatColor.GREEN + "Locating Sewer...");
 					
 					Location l = findSewer(p.getWorld(), p);
 					p.sendMessage(ChatColor.GREEN + "Closest sewer located @: " + ChatColor.BLUE + "X:" + l.getX()+ ChatColor.GREEN + "," + ChatColor.YELLOW + " Y:"+ l.getY()+ ChatColor.GREEN + "," + ChatColor.RED + " Z:" + l.getZ());
 					
 					DisplayArrows(p);
 					
 				return true;
 				}
 				else {
 					sender.sendMessage("This command can opnly be run by an OP!");
 				}
 			}
 			
 	}
 }
 		return false;
 }		
 	public Location findSewer(World w, Player p){
 		
 		try{
 			  // Open the file that is the first 
 			  // command line parameter
 			  FileInputStream fstream = new FileInputStream("plugins/Sewers/sewers.txt");
 			  // Get the object of DataInputStream
 			  DataInputStream in = new DataInputStream(fstream);
 			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			  String strLine;
 			  //Read File Line By Line
 			  int far;
 			  while ((strLine = br.readLine()) != null){
 			  // Print the content on the console
 			  String delims = ",";
 			  String[] cords = strLine.split(delims);
 
 			  int x = Integer.parseInt(cords[0]);
 			  int y = Integer.parseInt(cords[1]);
 			  int z = Integer.parseInt(cords[2]);
 			  
 				  Location l = w.getBlockAt(x, y, z).getLocation();
 				  double dis = p.getEyeLocation().distance(l);
 				  far = (int)dis;
 				  
 				  if(dis > far){
 					  end = l;
 				  }
 			  }
 			  in.close();
 			  return end;
 			    }catch (Exception e){//Catch exception if any
 			  System.err.println("Error: " + e.getMessage());
 			  }
 		return null;		
 	}
 	
	@SuppressWarnings("deprecation")
 	public void DisplayArrows(Player p){
 
 
 		//GenericTexture texture = new GenericTexture();		
 		//texture.setUrl("http://iconkits.com/images/vip/aerozone_arrow_small_preview.png");
 
 		SpoutPlayer player = (SpoutPlayer) p;
 		//Screen screen = player.getMainScreen();
 		
 //		GenericContainer box = new GenericContainer();
 //		box.setWidth(48).setHeight(48);
 //		box.setAnchor(WidgetAnchor.CENTER_CENTER);
 //		
 //		texture.setVisible(true);
 //		
 //		GenericPopup popup = new GenericPopup();
 //		box.addChild(texture);
 //		popup.attachWidget(plugin, box);
 		
 		InGameHUD hud = player.getMainScreen();
         GenericContainer generalBox = new GenericContainer();
         GenericTexture images = new GenericTexture();
        generalBox.setAnchor(WidgetAnchor.CENTER_LEFT);
        
         images.setUrl("http://iconkits.com/images/vip/aerozone_arrow_small_preview.png");
         try {
          URL urlimage = new URL("http://iconkits.com/images/vip/aerozone_arrow_small_preview.png");
         }
         catch (MalformedURLException e1) {
           e1.printStackTrace();
         }
 
         generalBox.setWidth(48).setHeight(48);
 
         generalBox.setX(48).setY(48);
         images.setWidth(48).setHeight(48);
         images.setVisible(true);
         generalBox.addChild(images);
         hud.attachWidget(plugin, generalBox);
         player.getMainScreen().attachWidget(hud);
 		
 		}	
 }
 
 
