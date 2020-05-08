 package team.GunsPlus;
 
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
 import team.GunsPlus.Enum.FireBehavior;
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
 	private int counter = 0;
 
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
 
 	public Location getLocation(){
 		return getPlayer().getLocation();
 	}
 	
 	public void zoom(Gun g) {
 		if(!player.hasPermission("gunsplus.zoom.all")) {
 			if(!player.hasPermission("gunsplus.zoom." + g.getName().toLowerCase().replace(" ", "_")))
 				return;
 		}
 		if (Util.enteredTripod(getPlayer()) && GunsPlus.forcezoom)
 			return;
 		if (!g.getObjects().containsKey("ZOOMTEXTURE")) {
 			GenericTexture zoomtex = new GenericTexture((String) g
 					.getResources().get("ZOOMTEXTURE"));
 			zoomtex.setAnchor(WidgetAnchor.SCALE).setX(0).setY(0)
 					.setPriority(RenderPriority.Low);
 			g.getObjects().put("ZOOMTEXTURE", zoomtex);
 		}
 		if (!isZooming()) {
 			GunUtils.zoomIn(GunsPlus.plugin, this, (GenericTexture) g
 					.getObjects().get("ZOOMTEXTURE"), (int) g
 					.getValue("ZOOMFACTOR"));
 			setZooming(true);
 			if (GunsPlus.notifications)
 				(getPlayer()).sendNotification(g.getName(), "Zoomed in!",
 						Material.SULPHUR);
 			Bukkit.getPluginManager().callEvent(new GunZoomInEvent(this.getPlayer(), g));
 		} else {
 			GunUtils.zoomOut(this);
 			setZooming(false);
 			if (GunsPlus.notifications)
 				(getPlayer()).sendNotification(g.getName(), "Zoomed out!",
 						Material.SULPHUR);
 			Bukkit.getPluginManager().callEvent(new GunZoomOutEvent(this.getPlayer(), g));
 		}
 	}
 
 	@SuppressWarnings("deprecation")
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
 			PlayerUtils.sendNotification(getPlayer(), "Use this gun ", "only outside a tripod!", new SpoutItemStack(GunsPlus.tripod), 2000);
 			setFireing(false);
 			return;
 		}else if(!GunUtils.isShootable(g)&&GunUtils.isMountable(g)&&!Util.enteredTripod(getPlayer())){
 			PlayerUtils.sendNotification(getPlayer(), "Enter a tripod to", "use this heavy gun!", new SpoutItemStack(g), 2000);
 			setFireing(false);
 			return;
 		}
 		if(Util.enteredTripod(getPlayer()))
 			inv = Util.getTripodDataOfEntered(getPlayer()).getInventory();
 		if (!GunUtils.checkInvForAmmo(inv, g.getAmmo())){
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
 		else if (isOutOfAmmo(g)){
 			setFireing(false);
 			return;
 		}
 		else {
 			FireBehavior f = (FireBehavior) g.getObject("FIREBEHAVIOR");
 			counter = f.getBurst().getShots();
 			Ammo usedAmmo = GunUtils.getFirstCustomAmmo(inv, g.getAmmo());
 			HashMap<LivingEntity, Integer> targets_damage = new HashMap<LivingEntity, Integer>(GunUtils.getTargets(player.getEyeLocation(), g, isZooming()));
 			Task fireTask = new Task(GunsPlus.plugin, g,  this, usedAmmo, targets_damage){
 				public void run(){
 					Gun g = (Gun) this.getArg(0);
 					GunsPlusPlayer gpp = (GunsPlusPlayer) this.getArg(1);
 					@SuppressWarnings("unchecked")
 					HashMap<LivingEntity, Integer> targets_damage = (HashMap<LivingEntity, Integer>)this.getArg(3);
 					Ammo usedAmmo = (Ammo) this.getArg(2);
 					if(targets_damage.isEmpty()){
						Location from = Util.getBlockInSight(gpp.getPlayer().getEyeLocation(), 2, 5).getLocation();
						GunUtils.shootProjectile(from, gpp.getPlayer().getEyeLocation().getDirection().toLocation(getLocation().getWorld()),
 								(Projectile) g.getObject("PROJECTILE"));
 					}
 					for (LivingEntity tar : targets_damage.keySet()) {
						if (tar.equals(gpp.getPlayer())) {
 							continue;
 						}
 						int damage = targets_damage.get(tar);
						Location from = Util.getBlockInSight(gpp.getPlayer().getEyeLocation(), 2, 5).getLocation();
 						GunUtils.shootProjectile(from, tar.getEyeLocation(),(Projectile) g.getObject("PROJECTILE"));
 						if (damage < 0)
							PlayerUtils.sendNotification(gpp.getPlayer(), "Headshot!",
 									"with a " + GunUtils.getRawGunName(g),
 									new ItemStack(Material.ARROW), 2000);
 						targets_damage.put(tar, Math.abs(damage));
 						damage = targets_damage.get(tar);
 						if (Util.getRandomInteger(1, 100) <= g.getValue("CRITICAL")) {
							PlayerUtils.sendNotification(gpp.getPlayer(), "Critical!",
 									"with a " + GunUtils.getRawGunName(g),
 									new ItemStack(Material.DIAMOND_SWORD), 2000);
 							damage = tar.getHealth() + 1000;
 						}
 						if (usedAmmo != null) {
 							damage += usedAmmo.getDamage();
 						}
						tar.damage(damage, tar);
 					}
 					
 					GunUtils.performEffects(gpp, getLocation(), new HashSet<LivingEntity>(targets_damage.keySet()), g);
 		
 					if (!(g.getResource("SHOTSOUND") == null)) {
 						if (g.getValue("SHOTDELAY") < 5
 								&& Util.getRandomInteger(0, 100) < 35) {
 							Util.playCustomSound(GunsPlus.plugin, getLocation(),
 									g.getResource("SHOTSOUND"),
 									(int) g.getValue("SHOTSOUNDVOLUME"));
 						} else {
 							Util.playCustomSound(GunsPlus.plugin, getLocation(),
 									g.getResource("SHOTSOUND"),
 									(int) g.getValue("SHOTSOUNDVOLUME"));
 						}
 		
 					}
 					--counter;
 					if(counter<=0) {
 						this.stopTask();
 					}
 				}
 			};
 			

			fireTask.startTaskRepeating(f.getBurst().getDelay(), false);
 			
 			Task other = new Task(GunsPlus.plugin, this, inv, g, fireTask){
 				public void run(){
 					Task fire = (Task) this.getArg(3);
 					if(!fire.isTaskRunning()){
 						Inventory inv = (Inventory) this.getArg(1);
 						Gun g = (Gun) this.getArg(2);
 						GunsPlusPlayer gpp = (GunsPlusPlayer) this.getArg(0);
 						
 						GunUtils.removeAmmo(inv, g.getAmmo());
 						Bukkit.getServer().getPluginManager().callEvent(new GunFireEvent(getPlayer(),g));
 						getPlayer().updateInventory();
 						
 						setFireCounter(g, getFireCounter(g) + 1);
 						recoil(g);
 						
 						if (GunsPlus.autoreload && getFireCounter(g) >= g.getValue("SHOTSBETWEENRELOAD"))
 								reload(g);
 						if ((int) g.getValue("SHOTDELAY") > 0)
 							delay(g);
 						gpp.setFireing(false);
 						this.stopTask();
 					}
 				}
 			};
 			other.startTaskRepeating(f.getBurst().getDelay(), false);
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
 			reloadTask.startTaskDelayed((int) g.getValue("RELOADTIME"));
 			setReloading();
 			if (!(g.getResource("RELOADSOUND") == null)) {
 				Util.playCustomSound(GunsPlus.plugin, getLocation(),
 						g.getResource("RELOADSOUND"),
 						(int) g.getValue("RELOADSOUNDVOLUME"));
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
 			t.startTaskDelayed((int) g.getValue("SHOTDELAY"));
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
 			if (gun.getValue("KNOCKBACK") > 0)
 				PlayerUtils.performKnockBack(getPlayer(),
 						gun.getValue("KNOCKBACK"));
 			if (gun.getValue("RECOIL") > 0)
 				PlayerUtils.performRecoil(getPlayer(), gun.getValue("RECOIL"));
 		}
 	}
 }
