 package team.GunsPlus;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import team.GunsPlus.API.Event.Gun.GunFireEvent;
 import team.GunsPlus.API.Event.Gun.GunReloadEvent;
 import team.GunsPlus.API.Event.Gun.GunZoomInEvent;
 import team.GunsPlus.API.Event.Gun.GunZoomOutEvent;
 import team.GunsPlus.Block.Tripod;
 import team.GunsPlus.Enum.Projectile;
 import team.GunsPlus.Gui.HUD;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Util.GunUtils;
 import team.GunsPlus.Util.LivingShooter;
 import team.GunsPlus.Util.PlayerUtils;
 import team.GunsPlus.Util.Shooter;
 import team.GunsPlus.Util.Task;
 import team.GunsPlus.Util.Util;
 
 public class GunsPlusPlayer extends LivingShooter {
 
 	private SpoutPlayer player;
 	private HUD hud;
 	private GenericTexture zoomtexture;
 	private boolean zooming = false;
 
 	public GunsPlusPlayer(SpoutPlayer sp, HUD h) {
 		player = sp;
 		hud = h;
 		for (Gun g : GunsPlus.allGuns)
 		this.resetFireCounter(g);
 	}
 	
 	public LivingEntity getLivingEntity(){
 		return getPlayer();
 	}
 
 	public boolean isZooming() {
 		return zooming;
 	}
 
 	public void setZooming(boolean zooming) {
 		this.zooming = zooming;
 	}
 
 	public GenericTexture getZoomtexture() {
 		return zoomtexture;
 	}
 
 	public void setZoomtexture(GenericTexture zoomtexture) {
 		this.zoomtexture = zoomtexture;
 	}
 
 	public HUD getHUD() {
 		return hud;
 	}
 
 	public SpoutPlayer getPlayer() {
 		return player;
 	}
 
 	@Override
 	public Location getLocation(){
 		return getPlayer().getEyeLocation();
 	}
 	
 	public void zoom(Gun g) {
 		if(!player.hasPermission("gunsplus.zoom.all")) {
 			if(!player.hasPermission("gunsplus.zoom." + g.getName().toLowerCase().replace(" ", "_")))
 				return;
 		}
 		if (Util.enteredTripod(getPlayer()) && Tripod.forcezoom)
 			return;
 		if (!g.getProperties().containsKey("LOADEDZOOMTEXTURE")) {
 			GenericTexture zoomtex = new GenericTexture((String) g.getProperty("ZOOMTEXTURE"));
 			zoomtex.setAnchor(WidgetAnchor.SCALE).setX(0).setY(0)
 					.setPriority(RenderPriority.Low);
 			g.getProperties().put("LOADEDZOOMTEXTURE", zoomtex);
 		}
 		if (!isZooming()) {
 			GunUtils.zoomIn(GunsPlus.plugin, this, (GenericTexture) g.getProperty("LOADEDZOOMTEXTURE"), (Integer) g
 					.getProperty("ZOOMFACTOR"));
 			setZooming(true);
 			PlayerUtils.sendNotification(getPlayer(), g.getName(), "Zoomed in!",
 						new ItemStack(Material.SULPHUR), 2000);
 			Bukkit.getPluginManager().callEvent(new GunZoomInEvent(this.getPlayer(), g));
 		} else {
 			GunUtils.zoomOut(this);
 			setZooming(false);
 			PlayerUtils.sendNotification(getPlayer(), g.getName(), "Zoomed out!",
 					new ItemStack(Material.SULPHUR), 2000);
 			Bukkit.getPluginManager().callEvent(new GunZoomOutEvent(this.getPlayer(), g));
 		}
 	}
 
 	@SuppressWarnings({ "deprecation", "unchecked" })
 	@Override
 	public void fire(Gun g) {
 		if(isFireing()) return;
 		setFireing(true);
 		if(!player.hasPermission("gunsplus.fire.all")) {
 			if(!player.hasPermission("gunsplus.fire." + g.getName().toLowerCase().replace(" ", "_"))){ 
 				setFireing(false);
 				return;
 			}
 		}
 		Inventory inv = getPlayer().getInventory();
 		if(!GunUtils.isShootable(g)&&!GunUtils.isMountable(g)){
 			PlayerUtils.sendNotification(getPlayer(), "This gun is ready for", "the scrap heap!", new ItemStack(Material.IRON_INGOT), 2000);
 			setFireing(false);
 			return;
 		}else if(GunUtils.isShootable(g)&&!GunUtils.isMountable(g)&&Util.enteredTripod(getPlayer())){
 			PlayerUtils.sendNotification(getPlayer(), "Use this gun ", "only outside a tripod!", new SpoutItemStack(g), 2000);
 			setFireing(false);
 			return;
 		}else if(!GunUtils.isShootable(g)&&GunUtils.isMountable(g)&&!Util.enteredTripod(getPlayer())){
 			PlayerUtils.sendNotification(getPlayer(), "Enter a tripod to", "use this heavy gun!", new SpoutItemStack(g), 2000);
 			setFireing(false);
 			return;
 		}
 		if(Util.enteredTripod(getPlayer()))
 			inv = Util.getTripodDataOfEntered(getPlayer()).getInventory();
 		if (!GunUtils.checkInvForAmmo(inv, (ArrayList<ItemStack>)g.getProperty("AMMO"))){
 			setFireing(false);
 			return;
 		}
 		if (isReloading()){
 			setFireing(false);
 			return;
 		}
 		else if (isDelaying()){
 			setFireing(false);
 			return;
 		}
 		else {
 			Ammo usedAmmo = GunUtils.getFirstCustomAmmo(inv,(ArrayList<ItemStack>)g.getProperty("AMMO"));
 			HashMap<LivingEntity, Integer> targets_damage = new HashMap<LivingEntity, Integer>(GunUtils.getTargets(player.getEyeLocation(), g, isZooming()));
 			
 			if(targets_damage.isEmpty()){
 				Location from = Util.getBlockInSight(this.getPlayer().getEyeLocation(), 2, 5).getLocation();
 				GunUtils.shootProjectile(from, this.getPlayer().getEyeLocation().getDirection().toLocation(getLocation().getWorld()),
 						(Projectile) g.getProperty("PROJECTILE"));
 			}
 			for (LivingEntity tar : targets_damage.keySet()) {
 				if (tar.equals(this.getPlayer())) {
 					continue;
 				}
 				int damage = targets_damage.get(tar);
 				Location from = Util.getBlockInSight(this.getPlayer().getEyeLocation(), 2, 5).getLocation();
 				GunUtils.shootProjectile(from, tar.getEyeLocation(),(Projectile) g.getProperty("PROJECTILE"));
				if (damage < 0)
 					PlayerUtils.sendNotification(this.getPlayer(), "Headshot!",
							"with a " + GunUtils.getRawGunName(g),
							new ItemStack(Material.ARROW), 2000);
				targets_damage.put(tar, Math.abs(damage));
				damage = targets_damage.get(tar);
 				if (Util.getRandomInteger(1, 100) <= (Integer) g.getProperty("CRITICAL")) {
 					PlayerUtils.sendNotification(this.getPlayer(), "Critical!",
 							"with a " + GunUtils.getRawGunName(g),
 							new ItemStack(Material.DIAMOND_SWORD), 2000);
 					damage = tar.getHealth() + 1000;
 				}
 				if (usedAmmo != null) {
 					damage += usedAmmo.getDamage();
 				}
				tar.damage(damage, tar);
 			}
 			
 			GunUtils.performEffects(this, new HashSet<LivingEntity>(targets_damage.keySet()), g);
 
 			if (!(g.getProperty("SHOTSOUND") == null)) {
 				if ((Integer)g.getProperty("SHOTDELAY") < 5
 						&& Util.getRandomInteger(0, 100) < 35) {
 					Util.playCustomSound(GunsPlus.plugin, getLocation(),
 							(String) g.getProperty("SHOTSOUND"),
 							(Integer) g.getProperty("SHOTSOUNDVOLUME"));
 				} else {
 					Util.playCustomSound(GunsPlus.plugin, getLocation(),
 							(String) g.getProperty("SHOTSOUND"),
 							(Integer) g.getProperty("SHOTSOUNDVOLUME"));
 				}
 
 			}
 			
 			GunUtils.removeAmmo(inv, (ArrayList<ItemStack>) g.getProperty("AMMO"));
 			Bukkit.getServer().getPluginManager().callEvent(new GunFireEvent(getPlayer(),g));
 			getPlayer().updateInventory();
 			
 			setFireCounter(g, getFireCounter(g) + 1);
 			recoil(g);
 			
 			if (GunsPlus.autoreload && getFireCounter(g) >= ((Number) g.getProperty("SHOTSBETWEENRELOAD")).intValue())
 					reload(g);
 			if ((Integer) g.getProperty("SHOTDELAY") > 0)
 				delay(g);
 			
 			setFireing(false);
 		}
 		
 	}
 	
 	@Override
 	public void reload(Gun g) {
 		if(!player.hasPermission("gunsplus.reload.all")) {
 			if(!player.hasPermission("gunsplus.reload." + g.getName().toLowerCase().replace(" ", "_")))
 				return;
 		}
 		if (getFireCounter(g) == 0)
 			return;
 		PlayerUtils.sendNotification(getPlayer(),
 				GunUtils.getRawGunName(g), "Reloading...",
 				new ItemStack(Material.WATCH), 2000);
 		Bukkit.getPluginManager().callEvent(new GunReloadEvent(this.getPlayer(),g));
 		if (isReloadResetted()) {
 			setOnReloadingQueue();
 		}
 		if (isOnReloadingQueue()) {
 			Task reloadTask = new Task(GunsPlus.plugin, this, g) {
 				public void run() {
 					Shooter s = (Shooter) this.getArg(0);
 					Gun g = (Gun) this.getArg(1);
 					s.resetReload();
 					s.resetFireCounter(g);
 				}
 			};
 			reloadTask.startTaskDelayed((Integer) g.getProperty("RELOADTIME"));
 			setReloading();
 			if (!(g.getProperty("RELOADSOUND") == null)) {
 				Util.playCustomSound(GunsPlus.plugin, getLocation(),
 						(String) g.getProperty("RELOADSOUND"),
 						(Integer) g.getProperty("RELOADSOUNDVOLUME"));
 			}
 			return;
 		} else if (isReloading()) {
 			return;
 		}
 	}
 	
 	@Override
 	public void delay(Gun g){
 		if(isDelayResetted()){
 			setOnDelayingQueue();
 		}
 		if(isOnDelayingQueue()){
 			Task t = new Task(GunsPlus.plugin ,this){
 				public void run() {
 					Shooter s = (Shooter) this.getArg(0);
 					s.resetDelay();
 				}
 			};
 			t.startTaskDelayed((Integer) g.getProperty("SHOTDELAY"));
 			setDelaying();
 		}else if(isDelaying()){
 			return;
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof GunsPlusPlayer) {
 			GunsPlusPlayer gp = (GunsPlusPlayer) o;
 			if (gp.getPlayer().equals(this.getPlayer())) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public void recoil(Gun gun) {
 		if (!Util.enteredTripod(getPlayer())) {
 			if (((Number)gun.getProperty("KNOCKBACK")).floatValue() > 0)
 				PlayerUtils.performKnockBack(getPlayer(),
 						((Number) gun.getProperty("KNOCKBACK")).floatValue());
 			if (((Number)gun.getProperty("RECOIL")).floatValue() > 0)
 				PlayerUtils.performRecoil(getPlayer(), (Float) gun.getProperty("RECOIL"));
 		}
 	}
 }
