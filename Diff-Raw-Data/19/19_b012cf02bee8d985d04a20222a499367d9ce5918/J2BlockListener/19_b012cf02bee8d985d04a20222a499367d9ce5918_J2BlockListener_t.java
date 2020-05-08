 
 package com.J2;
 
 
 import org.bukkit.entity.*;
 import org.bukkit.ChatColor;
 import org.bukkit.block.*;
 import org.bukkit.Material;
 import org.bukkit.event.block.*;
 
 
 
 
 
 
 /**
  * J2 block listener
  * @author mbaxter
  */
 public class J2BlockListener extends BlockListener {
     private final J2Plugin j2;
 
     public J2BlockListener(final J2Plugin plugin) {
         this.j2 = plugin;
     }
 
 
     @Override
     public void onBlockCanBuild(BlockCanBuildEvent event) {
         Material mat = event.getMaterial();
 
         if (mat.equals(Material.CACTUS)) {
             event.setBuildable(true);
         }
     }
     
     @Override
     public void onBlockDamage(BlockDamageEvent event){
     	
     	if(event.getPlayer().equals(j2.OneByOne)){
             j2.OneByOne=null;
             event.getPlayer().sendMessage("Boom");
             int x=event.getBlock().getX();
             int z=event.getBlock().getZ();
             int y=event.getBlock().getY()+1;
             while(y<128){
            	event.getBlock().getWorld().getBlockAt(x, y, z).setTypeId(0);
             	y++;
             }
     	}
     	
     	if (event.getDamageLevel()==BlockDamageLevel.BROKEN)
 		{
 			BlockRow changed;
 			Block smacked = event.getBlock();
 			Player player = event.getPlayer();
 			changed = new BlockRow(player.getDisplayName(),smacked.getTypeId(),0,smacked.getX(),smacked.getY(),smacked.getZ());
 			//if(!event.isCancelled())
 			
 			BlockLogger.bqueue.offer(changed);
 		}
     	
     }
     
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
     	Player player=event.getPlayer();
     	Block blockPlaced=event.getBlockPlaced();
     	int type=blockPlaced.getTypeId();
     	
     	BlockRow test;
 		
 		test = new BlockRow(player.getDisplayName(),0,type,blockPlaced.getX(),blockPlaced.getY(),blockPlaced.getZ());
 		BlockLogger.bqueue.offer(test);
     	
     	if(j2.getPerm().playerLevel(player)==1 && j2.isOnSuperBlacklist(type)){
     		player.sendMessage(ChatColor.RED+"Even trusted have limits. Can't place that block type");
     		event.setCancelled(true);
     		return;
     	}
     	if(j2.getPerm().playerLevel(player)==0 && (j2.isOnRegularBlacklist(type)||j2.isOnSuperBlacklist(type))){
     		player.sendMessage(ChatColor.RED+"You need to be trusted or higher to place that block type");
     		event.setCancelled(true);
     		return;
     	}
     	double x = player.getLocation().getX() - (blockPlaced.getX() + 0.5D);
         double y = player.getLocation().getY() - (blockPlaced.getY() + 0.5D);
         double z = player.getLocation().getZ() - (blockPlaced.getZ() + 0.5D);
         double dist = x*x + y*y + z*z;
         if(dist>400.0D) {
             j2.getChat().msgByLvlPlus(2, player.getName()+" placed a block over 20 away. *thwump*");
             j2.log.info(player.getName() + " placed a block too far away");
             player.kickPlayer("Detected as using a hack. Just rejoin if you aren't :)");
             event.setCancelled(true);
         }
     }
     
     public void onBlockRightClick(BlockRightClickEvent event)
 	{
     	Player player = event.getPlayer();
 		if(event.getItemInHand().getTypeId() == 284 && j2.getPerm().playerLevel(player) ==2)
 		{
 			this.j2.blogger.showBlockHistory(event.getPlayer(), event.getBlock()) ;
 		}
 		//System.out.println("Item type id ="+event.getItemInHand().getTypeId() );
 		
 	}
     
     /*public boolean onBlockBreak(Player player, Block block) {
     	/if(j2.mc2){
     		Block loc=j2.locationCheck(player,block,false);
     		if(loc!=null){
     			String[] bob = new String[4];
     			bob[0]="/ban";
     			bob[1]=player.getName();
     			bob[2]="0";
     			bob[3]="Destruction of nature ("+etc.getDataSource().getItem(block.getType())+" "+block.getX()+" "+block.getY()+" "+block.getZ()+"). http://forums.joe.to for unban";
     			j2.callBan("BobTheNaturalist", bob);
 
     			j2.log.info("NatureBan at "+player.getX()+" "+player.getY()+" "+player.getZ());
     			//queue=loc;
     			return true;
     		}
     	}
     	if(player.equals(j2.OneByOne)){
             j2.OneByOne=null;
             player.sendMessage("Boom");
             int x=block.getX();
             int z=block.getZ();
             int y=block.getY()+1;
             while(y<128){
             	etc.getServer().setBlockAt(0, x, y, z);
             	y++;
             }
     	}
     	return false;
     }*/
     
     /*public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand){
     	int type=0;
     	if(blockPlaced!=null)
     		type=blockPlaced.getType();
     	else{
     		type=itemInHand;
     		blockPlaced=new Block(type,)
     	}
 
     	if(j2.banBuckets && (type==325 || type==326 || type==327 ) && !player.canIgnoreRestrictions()){
 
     		String[] bob = new String[4];
     		bob[0]="/ban";
     		bob[1]=player.getName();
     		bob[2]="0";
     		bob[3]=etc.getDataSource().getItem(type)+" usage ("+blockPlaced.getX()+" "+blockPlaced.getY()+" "+blockPlaced.getY()+"). http://forums.joe.to for unban";
     		j2.callBan("BobTheBucketeer", bob);
     		j2.log.info("BucketBan at "+player.getX()+" "+player.getY()+" "+player.getZ());
     		queue=new Block( 19, blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ());
     		//j2.log.info(String.valueOf(blockPlaced.getX())+String.valueOf(blockPlaced.getY())+String.valueOf(blockPlaced.getZ()));
     		Block temp1=new Block(3,queue.getX(),queue.getY()-1,queue.getZ());
     		Block temp2=new Block(3,queue.getX(),queue.getY(),queue.getZ()-1);
     		Block temp3=new Block(3,queue.getX(),queue.getY(),queue.getZ()+1);
     		Block temp4=new Block(3,queue.getX()-1,queue.getY(),queue.getZ());
     		Block temp5=new Block(3,queue.getX()+1,queue.getY(),queue.getZ());
     		etc.getServer().setBlock(temp1);
     		etc.getServer().setBlock(temp2);
     		etc.getServer().setBlock(temp3);
     		etc.getServer().setBlock(temp4);
     		etc.getServer().setBlock(temp5);
     		return true;
     	}
     	/* Goodbye, faithful boatman
 		if(j2.banBuckets && (type==333 )){
 
     		String[] bob = new String[4];
     		bob[0]="/ban";
     		bob[1]=player.getName();
     		bob[2]="0";
     		bob[3]="boat usage. http://forums.joe.to for unban";
     		j2.callBan("BobTheBoatman", bob);
     		//etc.getServer().setBlockAt(12, blockPlaced.getX(), blockPlaced.getY()+1, blockPlaced.getZ());
     		j2.log.info("BoatBan at "+blockPlaced.getX()+" "+blockPlaced.getY()+" "+blockPlaced.getZ());
     	}
     	
     	if(!(player.isAdmin()||player.isInGroup("admins")) && ((j2.isOnRegularBlacklist(type) && !player.canIgnoreRestrictions()) || j2.isOnSuperBlacklist(type))){
     		player.sendMessage(ChatColor.RED+"You cannot place "+etc.getDataSource().getItem(type)+".");
     		queue=new Block( 19, blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ());
     		return true;
     	}
     	return false;
     }*/
     
     
     /*public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {
     	int type=blockPlaced.getType();
     	if(j2.mc2){
     		Block loc=j2.locationCheck(player,blockPlaced,true);
     		if(loc!=null){
     			String[] bob = new String[4];
     			bob[0]="/ban";
     			bob[1]=player.getName();
     			bob[2]="0";
     			bob[3]="Violation of nature ("+etc.getDataSource().getItem(blockPlaced.getType())+" "+player.getX()+" "+player.getY()+" "+player.getZ()+"). http://forums.joe.to for unban";
     			j2.callBan("BobTheNaturalist", bob);
     			j2.log.info("NatureBan at "+player.getX()+" "+player.getY()+" "+player.getZ());
     			return true;
     		}
     	}
     	if(player.canIgnoreRestrictions() && !j2.isJ2Admin(player) && j2.isOnSuperBlacklist(type)){
     		player.sendMessage(ChatColor.RED+"Even trusted have limits. Can't place that block type");
     		return true;
     	}
     	if(!player.canIgnoreRestrictions() && (j2.isOnRegularBlacklist(type)||j2.isOnSuperBlacklist(type))){
     		player.sendMessage(ChatColor.RED+"You need to be trusted or higher to place that block type");
     		return true;
     	}
     	double x = player.getX() - (blockPlaced.getX() + 0.5D);
         double y = player.getY() - (blockPlaced.getY() + 0.5D);
         double z = player.getZ() - (blockPlaced.getZ() + 0.5D);
         double dist = x*x + y*y + z*z;
         if(dist>400.0D) {
             j2.msgByCmd("/admin", player.getName()+" placed a block over 20 away. *thwump*");
             j2.log.info(player.getName() + " placed a block too far away");
             player.kick("Detected as using a hack. Just rejoin if you aren't :)");
             return true;
         }
     	//player.sendMessage("placed "+String.valueOf(type));
     	return false;
     }*/
 
     /*public boolean onItemUse(Player player, Block blockPlaced, Block blockClicked, Item item){
     	int type=item.getItemId();
     	  
     	if(player.canIgnoreRestrictions() && !j2.isJ2Admin(player) && j2.isOnSuperBlacklist(type)){
     		player.sendMessage(ChatColor.RED+"Even trusted have limits. Can't use this.");
     		return true;
     	}
     	if(!player.canIgnoreRestrictions() && (j2.isOnRegularBlacklist(type)||j2.isOnSuperBlacklist(type))){
     		player.sendMessage(ChatColor.RED+"You need to be trusted or higher to use this.");
     		return true;
     	}
     	/*if(j2.mc2 && (type==259 || type==290 || type==291 || type==292 || type==293 || type==294 
     			|| type==295 || type==321 || type==323 || type==234 || type==235 || type==236 
     			|| type==237 || type==238 || type==239 || type==330 || type==331 || type==333 
     			|| type==342 || type==343)){
     		Block derp=new Block(0,Double.valueOf(player.getX()).intValue(),Double.valueOf(player.getY()).intValue(),Double.valueOf(player.getZ()).intValue());
     		Block loc=j2.locationCheck(player,derp,true);
     		if(loc!=null){
     			String[] bob = new String[4];
     			bob[0]="/ban";
     			bob[1]=player.getName();
     			bob[2]="0";
     			bob[3]="Violation of nature ("+etc.getDataSource().getItem(type)+" "+player.getX()+" "+player.getY()+" "+player.getZ()+"). http://forums.joe.to for unban";
     			j2.callBan("BobTheNaturalist", bob);
     			j2.log.info("NatureBan at "+player.getX()+" "+player.getY()+" "+player.getZ());
     			return true;
     		}
     	}
     	//player.sendMessage("used "+String.valueOf(type));
     	return false;
     }
     
     /*public boolean onBlockDestroy(Player player, Block block){
     	/*if(queue!=null){
     		Block temp=new Block(0,queue.getX(),queue.getY(),queue.getZ());
     		etc.getServer().setBlock(queue);
     		etc.getServer().setBlock(temp);
 
     		queue=null;
     	}
     	if(j2.mc2 && block.getX()==45 && block.getY()==63 && block.getZ()==-10 && block.getType()==77){
     		Date curTime=new Date();
     		long timeNow=curTime.getTime()/1000;
     		if(timeNow>(piggeh+2)||j2.isJ2Admin(player)){
     			etc.getServer().dropItem(46, 64, -12, 319);
     			etc.getServer().dropItem(46, 64, -13, 319);
     			etc.getServer().dropItem(46, 64, -14, 319);
     			etc.getServer().dropItem(46, 64, -15, 319);
     			etc.getServer().dropItem(47, 64, -12, 319);
     			etc.getServer().dropItem(47, 64, -13, 319);
     			etc.getServer().dropItem(47, 64, -14, 319);
     			etc.getServer().dropItem(47, 64, -15, 319);
     			etc.getServer().dropItem(48, 64, -12, 319);
     			etc.getServer().dropItem(48, 64, -13, 319);
     			etc.getServer().dropItem(48, 64, -14, 319);
     			etc.getServer().dropItem(48, 64, -15, 319);
     			etc.getServer().dropItem(46, 64, -12, 319);
     			etc.getServer().dropItem(46, 63, -13, 319);
     			etc.getServer().dropItem(46, 63, -14, 319);
     			etc.getServer().dropItem(46, 63, -15, 319);
     			etc.getServer().dropItem(47, 63, -12, 319);
     			etc.getServer().dropItem(47, 63, -13, 319);
     			etc.getServer().dropItem(47, 63, -14, 319);
     			etc.getServer().dropItem(47, 63, -15, 319);
     			etc.getServer().dropItem(48, 63, -12, 319);
     			etc.getServer().dropItem(48, 63, -13, 319);
     			etc.getServer().dropItem(48, 63, -14, 319);
     			etc.getServer().dropItem(48, 63, -15, 319);
     			Mob mob = new Mob("Pig", new Location(47,64,-14));
     			mob.spawn();
     			player.sendMessage(Colors.Red+"RECEIVE BACON");
     			piggeh=timeNow;
     			}
     		return true;
     	}
     	return false;
     }*/
     
     //long piggeh=0;
 }
