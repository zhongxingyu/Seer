 package BL2.common;
 
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.input.Keyboard;
 
 import BL2.common.ItemBandoiler.BandStor;
 
 import net.minecraft.src.*;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.ArrowLooseEvent;
 import net.minecraftforge.event.entity.player.ArrowNockEvent;
 
 public class ItemGun extends Item
 {
 	private String itemName;
 	
 	public static final String[] gunNames = new String[] {"", "Pistol", "SMG", "Assault Rifle", "Rocket Launcher", "Sniper", "Shotgun"};
 	public static final String[] Companies = new String[] {"Dahl", "Tediore", "Jakobs", "Maliwan", "Bandit", "Hyperion", "Vladof", "Torgue"};
 	
     public ItemGun(int par1)
     {
         super(par1);
         this.maxStackSize = 1;
         this.setCreativeTab(BL2Core.tabBL2);
         this.setHasSubtypes(true);
         this.setMaxDamage(100);
     }
 
     public void getSubItems(int i, CreativeTabs tabs, List l)
     {
         for (int j = 1; j < 7; j++)
         {
             ItemStack stack = new ItemStack(this, 1, j);
             //GunAtributes atr = new GunAtributes(stack);
             l.add(stack);
             //atr.save(stack);
         }
     }
 
     public int getIconFromDamage(int par1)
     {
         return par1 - 1;
     }
     
     public float getDamageForItemStack(ItemStack stack)
     {
     	GunAtributes atr = new GunAtributes(stack);
     	
    	if(atr.bulletsleft <= 1 && atr.reloadticker == 0){
     		return 1;
     	}
     	else if(atr.reloadticker > 0)
     	{
     		return (atr.reloadticker / ((float)atr.reloadtime));
     	}
     	else if(atr.bulletsleft > 1)
     	{
     		return (((atr.clipsize - 1) - (atr.bulletsleft - 1)) / ((float)atr.clipsize - 1));
     	}
     	return 0;
     }
     
     public String s(int par1){
     	String ifS = "";
 		if(par1 > 1){
 			ifS = "s";
 		}
     	return ifS;
     }
     
     public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
 	 {
     	par3List.clear();
     	GunAtributes atr = new GunAtributes(par1ItemStack);
     	float damage = atr.damage;
     	par3List.add(gunNames[par1ItemStack.getItemDamage()]);
     	par3List.add(Companies[atr.Company]);
     	par3List.add("DPS: " + getDPS(par1ItemStack) + " Hearts/second");
     	par3List.add("Damage: " + damage/2 + " Hearts");
 		par3List.add("Ammo: " + getBulletsLeftInfo(par1ItemStack));
 		par3List.add("Consumes " + atr.ammoPerShot + " bullet"+s(atr.ammoPerShot)+" per shot.");
 		par3List.add("Reload: " + (float) (atr.reloadtime / 20) + " seconds");
 		if(atr.explosive == true){
 			par3List.add("Bullets explode on impact.");
 		}
 	 }
 
     public int getBulletsLeft(ItemStack par1ItemStack){
     	int Bulletsleft = new GunAtributes(par1ItemStack).bulletsleft - 1;
     	if(Bulletsleft < 0){
     		return 0;
     	}
     	else
     	{
 		return Bulletsleft;
     	}
     }
     
     public float getDPS(ItemStack par1ItemStack){
     	GunAtributes atr = new GunAtributes(par1ItemStack);
     	float damage = (float)atr.damage/2;
     	float firetime = (float) atr.firetime;
     	float DPS = (float)((damage)*(20/firetime));
     	float RDPS = (float) ((double)Math.round(DPS * 100) / 100);
     	
 		return RDPS;
     }
     
     public String getBulletsLeftInfo(ItemStack par1ItemStack){
 		int BulletsLeft = getBulletsLeft(par1ItemStack);
 		String Info = null;
 		Info = BulletsLeft + "/" + (new GunAtributes(par1ItemStack).clipsize - 1);
 		return Info;
     }
     
     public float getReloadLeft(ItemStack par1ItemStack){
     	float Reloadleft = (float) (new GunAtributes(par1ItemStack).reloadticker - 1) /20;
     	if(Reloadleft < 0){
     		return 0;
     	}
     	else
     	{
 		return Reloadleft;
     	}
     }
     
     public String getReloadLeftInfo(ItemStack par1ItemStack){
 		float ReloadLeft = getReloadLeft(par1ItemStack);
 		String Info = null;
 		Info = ReloadLeft + "/" + (int) (new GunAtributes(par1ItemStack).reloadtime - 1) / 20;
 		return Info;
     }
     
     public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
     	 GunAtributes atr = new GunAtributes(par1ItemStack);
     	 atr.using = true;
     	 //System.out.println(System.currentTimeMillis() - atr.lasttick);
     	 atr.lasttick = (System.currentTimeMillis());
     	 atr.save(par1ItemStack);
     	 return par1ItemStack;
     }
     
     public boolean noAmmo(GunAtributes atr){
 		if(atr.bulletsleft >= 1){
 			return true;
 		}
     	return false;
     }
     
     public boolean fullAmmo(GunAtributes atr){
 		if(atr.bulletsleft == atr.clipsize){
 			return true;
 		}
     	return false;
     }
     
     public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
     {
         GunAtributes atr = new GunAtributes(par1ItemStack);
 
         //System.out.println(Keyboard.isKeyDown(19));
         
         //System.out.println(par3Entity instanceof EntityPlayer && ((EntityPlayer)par3Entity).getHeldItem() == par1ItemStack);
         //System.out.println(canReload(((EntityPlayer)par3Entity), ((EntityPlayer)par3Entity).getCurrentEquippedItem().getItemDamage(), par1ItemStack) == true);
         if(par3Entity instanceof EntityPlayer && ((EntityPlayer)par3Entity).getHeldItem() == par1ItemStack){
         	if (atr.bulletsleft <= atr.ammoPerShot && atr.reloadticker == 0 || (Keyboard.isKeyDown(19) && !fullAmmo(atr)))
 		    {
 				//atr.bulletsleft = 1;
 		        atr.reloadticker = atr.reloadtime;
 		        atr.save(par1ItemStack);
 		        
 		        return;
 		    }
         }
 
         //System.out.println(canReload(((EntityPlayer)par3Entity), ((EntityPlayer)par3Entity).getHeldItem().getItemDamage(), par1ItemStack) == true);
         
         if (par3Entity instanceof EntityPlayer && ((EntityPlayer)par3Entity).getHeldItem() == par1ItemStack && atr.reloadticker > 0)
         {
         	boolean var5 = ((EntityPlayer)par3Entity).capabilities.isCreativeMode;
         	
         	
         	if(var5 || canReload(((EntityPlayer)par3Entity), ((EntityPlayer)par3Entity).getHeldItem().getItemDamage(), par1ItemStack) == true){
         		
         		atr.reloadticker--;
 
 	            if (atr.reloadticker <= 0)
 	            {
 	            	int needed = bulletsNeeded(par1ItemStack);
 	            	for(int i=0;i < needed; i++){
 	            		
 	            		if(var5 || reload(((EntityPlayer)par3Entity), ((EntityPlayer)par3Entity).getHeldItem().getItemDamage(), par1ItemStack)){
 	            			atr.bulletsleft++;
 	            		}
 	            		//atr.bulletsleft++;
 	            	}
 	            }
         	}
 
             atr.save(par1ItemStack);
         }
         else
         {
         	atr.reloadticker = 0;
         	atr.save(par1ItemStack);
         }
         
         
 
         //System.out.println(par3Entity instanceof EntityPlayer && ((EntityPlayer)par3Entity).getHeldItem() == par1ItemStack);
         
         if (atr.using && (System.currentTimeMillis() - atr.lasttick) < 250 && par3Entity instanceof EntityPlayer && ((EntityPlayer)par3Entity).getHeldItem() == par1ItemStack)
         {
             if (atr.reloadticker > 0)
             {
                 return;
             }
 
             if (atr.fireticker < atr.firetime)
             {
                 atr.fireticker++;
                 atr.save(par1ItemStack);
                 return;
             }
             
 
             //System.out.println(atr.bulletsleft);
             boolean var5 = ((EntityPlayer)par3Entity).capabilities.isCreativeMode;
             
             //System.out.println(consumeBullet(((EntityPlayer)par3Entity), atr, par1ItemStack));
             
             if (var5 || consumeBullet(((EntityPlayer)par3Entity), atr, par1ItemStack))// || par3EntityPlayer.inventory.hasItem(Item.arrow.shiftedIndex))
             {
             	atr.fireticker = 0;
             	atr.bulletsleft -= atr.ammoPerShot;
             	
             	
             
                 EntityBullet var8 = new EntityBullet(par2World, ((EntityPlayer)par3Entity), atr.bulletspeed, atr.damage, atr.explosive, atr.explosivepower, atr.accuracy, atr.knockback);
                 par3Entity.rotationPitch -= atr.recoil * .75F;
 
                 //par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
 
                if (!par2World.isRemote)
                 {
                     par2World.spawnEntityInWorld(var8);
                 }
             }
 
             atr.save(par1ItemStack);
         }
     }
     
 //    public boolean reload(EntityPlayer player, int type, ItemStack is){
 //    	GunAtributes atr = new GunAtributes(is);
 //    	int t = atr.clipsize - atr.bulletsleft;
 //    	for(int i = 0; i < t; i++)
 //    	{
 //    		consumeBullet(player,atr,is);
 //    		return true;
 //    	}
 //		return false;
 //    }
     
     public int bulletsNeeded(ItemStack is){
     	GunAtributes atr = new GunAtributes(is);
     	return (atr.clipsize - atr.bulletsleft);
     }
 
     public boolean consumeBullet(EntityPlayer player, GunAtributes atr, ItemStack is)
     {
         ItemStack stack = null;
         if (atr.bulletsleft > 0){
             return true;
         }
 		return false;
     }
     
     public boolean canReload(EntityPlayer player, int type, ItemStack is)
     {
         ItemStack stack = null;
         
         for (int i = 0; i < 36; i++)
         {
             stack = player.inventory.getStackInSlot(i);
 
             if (stack != null && stack.getItemDamage() == type)
             {
                 if (stack.itemID == BL2Core.bandoiler.shiftedIndex)
                 {
                     ItemBandoiler.BandStor stor = new ItemBandoiler.BandStor(stack);
 
                     if(stor.bullets >= 1)
                     {
                         return true;
                     }
                 }
                 else if (stack.itemID == BL2Core.bullets.shiftedIndex)
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
     
     public boolean reload(EntityPlayer player, int type, ItemStack is)
     {
         ItemStack stack = null;
         GunAtributes atr = new GunAtributes(is);
 
         int needed = (atr.clipsize - atr.bulletsleft); 
         
         for (int i = 0; i < 36; i++)
         {
             stack = player.inventory.getStackInSlot(i);
 
             if (stack != null && stack.getItemDamage() == type)
             {
                 if (stack.itemID == BL2Core.bandoiler.shiftedIndex)
                 {
                     ItemBandoiler.BandStor stor = new ItemBandoiler.BandStor(stack);
 
                     if(stor.bullets >= 1)
                     {
                         stor.bullets--;
                         atr.bulletsleft++;
                         stor.save(stack);
                         atr.save(is);
                         return true;
                     }
                 }
                 else if (stack.itemID == BL2Core.bullets.shiftedIndex)
                 {
                     stack.stackSize--;
                     atr.bulletsleft++;
                     atr.save(is);
 
                     if (stack.stackSize <= 0)
                     {
                         player.inventory.setInventorySlotContents(i, null);
                     }
                     
                     return true;
                 }
             }
         }
 
         return false;
     }
     
     /*
     public boolean consumeBullet(EntityPlayer player, int type, ItemStack is)
     {
         ItemStack stack = null;
 
         for (int i = 0; i < 36; i++)
         {
             stack = player.inventory.getStackInSlot(i);
 
             if (stack != null && stack.getItemDamage() == type - 1)
             {
                 if (stack.itemID == BL2Core.bandoiler.shiftedIndex)
                 {
                     ItemBandoiler.BandStor stor = new ItemBandoiler.BandStor(stack);
 
                     if (stor.bullets > 0)
                     {
                         stor.bullets--;
                         stor.save(stack);
                         return true;
                     }
                 }
                 else if (stack.itemID == BL2Core.bullets.shiftedIndex)
                 {
                     stack.stackSize--;
 
                     if (stack.stackSize <= 0)
                     {
                         player.inventory.setInventorySlotContents(i, null);
                     }
                     
                     return true;
                 }
             }
         }
 
         return false;
     }
     */
 
     public String getTextureFile()
     {
         return "/BL2/textures/Items.png";
     }
     
     //pistol = 0, smg = 1, assault rifle = 2, rocket launcher = 3, sniper = 4, shotgun = 5
     
 
     public static void genAny(GunAtributes atr)
     {
     	float g = (float) Math.random() * 100;
      	
      	if(g < 17)
      	{
      		genPistol(atr);
      	}
      	else if(g < 33)
      	{
      		genSMG(atr);
      	}
      	else if(g < 50)
      	{
      		genAR(atr);
      	}
      	else if(g < 67)
      	{
      		genRocketLauncher(atr);
      	}
      	else if(g < 83)
      	{
      		genSniper(atr);
      	}
      	else
      	{
      		genShotgun(atr);
      	}
     }
     
     public static void genPistol(GunAtributes atr)
     {
 		atr.guntype = 1;
 		atr.ammotype = atr.guntype;
 		atr.clipsize *= .5;
 		
 	}
     public static void genSMG(GunAtributes atr)
     {
     	atr.guntype = 2;
     	atr.ammotype = atr.guntype;
 	    atr.accuracy *= 2;
 	}
     public static void genAR(GunAtributes atr)
     {
     	atr.guntype = 3;
     	atr.ammotype = atr.guntype;
 		atr.accuracy *= .75;
 		atr.firetime *= 1.5;
 	}
     public static void genRocketLauncher(GunAtributes atr)
     {
     	atr.guntype = 4;
     	atr.ammotype = atr.guntype;
     	atr.clipsize *= .1;
     	atr.bulletspeed *= .33;
     	atr.explosivepower *= 2;
     	atr.explosive = true;
 	}
     public static void genSniper(GunAtributes atr)
     {
     	float recoil;
     	
     	atr.guntype = 5;
     	atr.ammotype = atr.guntype;
     	atr.clipsize = (int) ((Math.random() * (15 - 5)) + 5) + 1;
     	
     	if(atr.clipsize <= 8)
     	{
     		atr.damage *= 3.5;
     		atr.recoil = 8;
     	}else
     	{
     		atr.damage *= 1.5;
     		atr.recoil = 4;
     	}
     	
     	recoil = (float) ((atr.damage/2)/1.5); 
     	
     	atr.reloadtime *= 2;
     	//atr.recoil *= recoil;
     	atr.accuracy = 1000F;
     	atr.firetime *= 5;
     }
 	public static void genShotgun(GunAtributes atr)
 	{
 		atr.guntype = 6;
 		atr.ammotype = atr.guntype;
 		atr.knockback *= 8;
 		atr.pellets = 5;
 		atr.clipsize *= .25;
 		atr.recoil *= 3;
 		atr.accuracy *= 4;
 	}
 
     public static ItemStack getRandomGun()
     {
         ItemStack re = new ItemStack(BL2Core.guns);
         GunAtributes atr = ((ItemGun)BL2Core.guns).new GunAtributes(re);
         /*
          * set the random atributes here :D
          *
          * to set fire time to a random number between x and y,
          * atr.firetime = (Math.random() * (y - x)) + x;
          */
         
         atr.Company = (int) Math.round(Math.random() * 7);
         atr.firetime = (int) Math.round((Math.random() * (15 - 5)) + 5);
         
       //Dahl = 0, Tediore = 1, Jakobs = 2, Maliwan = 3, Bandit = 4, Hyerion = 5, Vladof = 6, Torgue = 7
       //pistol = 0, smg = 1, assault rifle = 2, rocket launcher = 3, sniper = 4, shotgun = 5
         
         atr.firetime = (int) ((Math.random() * (5 - 2)) + 2);
         atr.explosivepower = (float) ((Math.random() * (3 - 2)) + 2);
         atr.clipsize = (int) ((Math.random() * (45 - 20)) + 20) + 1;
         atr.reloadtime = (int) ((Math.random() * (100 - 40)) + 40) + 1;
         //atr.ammoPerShot = (int) ((Math.random() * (3 - 1)) + 1);
         atr.damage = (int) ((Math.random() * (6 - 2)) + 2) + 1;
         
         
         float i = (float) Math.random() * 100;
         
         if(i < 30)
         {
         	atr.ammoPerShot = 2;
         	//chance = num
         }else
         {
         	atr.ammoPerShot = 1;
         	//chance = 100% - num x - 1
         }
         
         if(atr.Company == 0)
         {
         	float g = (float) Math.random() * 100;
         	
         	if(g < 33)
         	{
         		genPistol(atr);
         	}
         	else if(g < 66)
         	{
         		genAR(atr);
         	}
         	else
         	{
         		genRocketLauncher(atr);
         	}
         	atr.clipsize = 3+1;
         	atr.firetime = 1;
         	atr.ammoPerShot = 1;
         	atr.reloadtime = 20 + 1;
         }
          if(atr.Company == 1)
         { 
         	float g = (float) Math.random() * 100;
          	
          	if(g < 33)
          	{
          		genPistol(atr);
          	}
          	else if(g < 66)
          	{
          		genShotgun(atr);
          	}
          	else
          	{
          		genRocketLauncher(atr);
          	}
          	
         	atr.explosive = true;
         	atr.throwtoreload = true;
         }
         if(atr.Company == 2)
         {
         	float g = (float) Math.random() * 100;
         	
         	if(g < 25)
          	{
          		genPistol(atr);
          	}
          	else if(g < 50)
          	{
          		genShotgun(atr);
          	}
          	else if(g < 75)
          	{
          		genAR(atr);
          	}
          	else
          	{
          		genSniper(atr);
          	}
         	atr.damage *= 1.5;
         }
         if(atr.Company == 3)
         {
         	genAny(atr);
         	atr.damage *= .75;
         	atr.reloadtime *= .5;
         }
         //Dahl = 0, Tediore = 1, Jakobs = 2, Maliwan = 3, Bandit = 4, Hyerion = 5, Vladof = 6, Torgue = 7
         //pistol = 0, smg = 1, assault rifle = 2, rocket launcher = 3, sniper = 4, shotgun = 5
         if(atr.Company == 4)
         {
         	float g = (float) Math.random() * 100;
         	
         	if(g < 25)
          	{
          		genPistol(atr);
          	}
          	else if(g < 50)
          	{
          		genSMG(atr);
          	}
          	else if(g < 75)
          	{
          		genAR(atr);
          	}
          	else
          	{
          		genRocketLauncher(atr);
          	}
         	
         	atr.clipsize *= 2;
         }
         if(atr.Company == 5)
         {
         	float g = (float) Math.random() * 100;
         	
         	if(g < 33)
          	{
          		genPistol(atr);
          	}
          	else if(g < 67)
          	{
          		genSMG(atr);
          	}
          	else 
          	{
          		genSniper(atr);
          	}
         	atr.bulletspeed *= 1.5;
         }
         //Dahl = 0, Tediore = 1, Jakobs = 2, Maliwan = 3, Bandit = 4, Hyerion = 5, Vladof = 6, Torgue = 7
         //pistol = 0, smg = 1, assault rifle = 2, rocket launcher = 3, sniper = 4, shotgun = 5
         if(atr.Company == 6)
         {
         	float g = (float) Math.random() * 100;
         	
         	if(g < 25)
          	{
          		genPistol(atr);
          	}
          	else if(g < 50)
          	{
          		genSMG(atr);
          	}
          	else if(g < 75)
          	{
          		genAR(atr);
          	}
          	else
          	{
          		genSniper(atr);
          	}
         	atr.firetime = 1;
         	atr.clipsize *= 1.25;
         	atr.accuracy *= 0.75;
         }
         if(atr.Company == 7)
         {
         	genAny(atr);
         	atr.explosive = true;
         	atr.bulletspeed *= .5;
         }
         
 //        System.out.println("comp:" + atr.Company);
 //        System.out.println("type" +atr.guntype);
         
         atr.save(re);
         re.setItemDamage(atr.guntype);
         return re;
     }
 
     public class GunAtributes
     {
         /**
          * number of ticks between fires, should be >0
          */
         public int firetime = 2;
         public boolean explosive = false;
         public float explosivepower = 2.0F;
         public int clipsize = 31 + 1;
         public int ammoPerShot = 1;
         public int Company = 0; //Dahl = 0, Tediore = 1, Jakobs = 2, Maliwan = 3, Bandit = 4, Hyerion = 5, Vladof = 6, Torgue = 7
         public int reloadtime = 40 + 1;
         public float bulletspeed = 10;
         public int damage = 4;
         public int ammotype = 2; //pistol = 1, smg = 2, assault rifle = 3, rocket launcher = 4, sniper = 5, shotgun = 6
         public boolean throwtoreload = false;
         public int guntype; //pistol = 1, smg = 2, assault rifle = 3, rocket launcher = 4, sniper = 5, shotgun = 6
         public float accuracy = 1.0F;
         public float recoil = 1.5F;
         public int fireticker;
         public int bulletsleft;
         public int reloadticker = 1;
         public int pellets;
         public int knockback;
         
         public boolean using;
         public long lasttick;
 
         public GunAtributes(ItemStack it)
         {
             load(it);
         }
 
         public void save(ItemStack it)
         {
 	    boolean newTag = false;
             NBTTagCompound tag = it.getTagCompound();
             if(tag == null) {
             	tag = new NBTTagCompound();
             	newTag = true;
             }
             
             tag.setInteger("firetime", firetime);
             tag.setBoolean("explosive", explosive);
             tag.setFloat("explosivepower", explosivepower);
             tag.setInteger("clipsize", clipsize);
             tag.setInteger("ammoPerShot", ammoPerShot);
             tag.setInteger("Company", Company);
             tag.setInteger("reloadtime", reloadtime);
             tag.setFloat("bulletspeed", bulletspeed);
             tag.setInteger("damage", damage);
             tag.setInteger("ammotype", ammotype);
             tag.setBoolean("throwtoreload", throwtoreload);
             tag.setFloat("accuracy", accuracy);
             tag.setFloat("recoil", recoil);
             tag.setInteger("fireticker", fireticker);
             tag.setInteger("bulletsleft", bulletsleft);
             tag.setInteger("reloadticker", reloadticker);
             tag.setInteger("pellets", pellets);
             tag.setInteger("knockback", knockback);
             
             tag.setBoolean("using", using);
             tag.setLong("lasttick", lasttick);
 		    if(newTag)
 	            	it.setTagCompound(tag);
 	        }
 
         public void load(ItemStack it)
         {
             NBTTagCompound tag = it.getTagCompound();
 
             if (tag == null)
             {
                 return;
             }
 
             firetime = tag.getInteger("firetime");
             explosive = tag.getBoolean("explosive");
             explosivepower = tag.getFloat("explosivepower");
             clipsize = tag.getInteger("clipsize");
             ammoPerShot = tag.getInteger("ammoPerShot");
             Company = tag.getInteger("Company");
             reloadtime = tag.getInteger("reloadtime");
             bulletspeed = tag.getFloat("bulletspeed");
             damage = tag.getInteger("damage");
             ammotype = tag.getInteger("ammotype");
             throwtoreload = tag.getBoolean("throwtoreload");
             guntype = it.getItemDamage();
             accuracy = tag.getFloat("accuracy");
             recoil = tag.getFloat("recoil");
             fireticker = tag.getInteger("fireticker");
             bulletsleft = tag.getInteger("bulletsleft");
             reloadticker = tag.getInteger("reloadticker");
             pellets = tag.getInteger("pellets");
             knockback = tag.getInteger("knockback");
             
             using = tag.getBoolean("using");
             lasttick = tag.getLong("lasttick");
         }
     }
 }
