 package team.GunsPlus;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.Packet42RemoveMobEffect;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Snowball;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.MaterialData;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.Enum.EffectType;
 import team.GunsPlus.Enum.Projectile;
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Util;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Task;
 
 public class GunUtils {
 
 		public static boolean holdsGun(SpoutPlayer p) {
 			ItemStack is = p.getItemInHand();
 			for(Gun g : GunsPlus.allGuns){
 				SpoutItemStack sis = new SpoutItemStack(g);
 				if(is.getTypeId()==sis.getTypeId()&&is.getDurability()==sis.getDurability()){
 					return true;
 				}
 			}
 			return false;
 		}
 
 		public static void shootProjectile(SpoutPlayer sp , Projectile pro){
 			double speed = pro.getSpeed();
 			if(pro.equals(Projectile.ARROW)){
 				Arrow a = sp.launchProjectile(Arrow.class);
 				a.setVelocity(sp.getLocation().getDirection().multiply(speed));
 			}else if(pro.equals(Projectile.FIREBALL)){
 				Fireball fb = sp.launchProjectile(Fireball.class);
 				fb.setYield(0f);
 				fb.setIsIncendiary(false);
 				fb.setDirection(sp.getLocation().getDirection());
 				fb.setVelocity(sp.getLocation().getDirection().multiply(speed));
 			}else if(pro.equals(Projectile.SNOWBALL)){
 				Snowball sb = sp.launchProjectile(Snowball.class);
 				sb.setVelocity(sp.getLocation().getDirection().multiply(speed));
 			}else if(pro.equals(Projectile.EGG)){
 				Egg egg = sp.launchProjectile(Egg.class);
 				egg.setVelocity(sp.getLocation().getDirection().multiply(speed));
 			}else if(pro.equals(Projectile.ENDERPEARL)){
 				EnderPearl ep = sp.launchProjectile(EnderPearl.class);
 				ep.setVelocity(sp.getLocation().getDirection().multiply(speed));
 			}
 		}
 
 		public static HashMap<LivingEntity, Integer> getTargets(SpoutPlayer p, Gun g ){
 			HashMap<LivingEntity, Integer> targets = new HashMap<LivingEntity, Integer>();
 			Location loc = p.getEyeLocation();
 			HashMap<LivingEntity,Integer > e = null;
 			for(int i=0; i<=(Util.isZooming(p)?(g.getValue("SPREAD_IN")/2):(g.getValue("SPREAD_OUT")/2)); i+=3){
 				loc = p.getEyeLocation();
 				loc.setYaw(loc.getYaw()+Util.getRandomInteger(i, Math.round(i*g.getValue("RANDOMFACTOR"))));
 				e = getTargetEntities(loc, p,g);
 				targets.putAll(e);
 				loc = p.getEyeLocation();
 				loc.setYaw(loc.getYaw()-Util.getRandomInteger(i, i+Util.getRandomInteger(i, Math.round(i*g.getValue("RANDOMFACTOR")))));
 				e = getTargetEntities(loc, p,g);
 				targets.putAll(e);
 				loc = p.getEyeLocation();
 				loc.setPitch(loc.getPitch()+Util.getRandomInteger(i, i+Util.getRandomInteger(i, Math.round(i*g.getValue("RANDOMFACTOR")))));
 				e = getTargetEntities(loc,p, g);
 				targets.putAll(e);
 				loc = p.getEyeLocation();
 				loc.setPitch(loc.getPitch()-Util.getRandomInteger(i, i+Util.getRandomInteger(i, Math.round(i*g.getValue("RANDOMFACTOR")))));
 				e = getTargetEntities(loc,p, g);
 				targets.putAll(e);
 			}
 			return targets;
 		}
 
 		public static HashMap<LivingEntity, Integer> getTargetEntities(Location loc, SpoutPlayer sp ,Gun g) {
 			HashMap<LivingEntity, Integer> targets = new HashMap<LivingEntity, Integer>();
 			BlockIterator bitr = new BlockIterator(loc,0d, (int) g.getValue("RANGE"));
 			Block b;
 			Location l;
 			int bx, by, bz;
 			double ex, ey, ez;
 			while (bitr.hasNext()) {
 				b = bitr.next();
 				bx = b.getX();
 				by = b.getY();
 				bz = b.getZ();
 				Set<LivingEntity> entities = new HashSet<LivingEntity>();
 				Location lb = new Location(b.getWorld(), bx, by, bz);
 				if(!Util.isTransparent(lb.getBlock()))break;
 				for (Entity e : Util.getNearbyEntities(lb, 0.4, 0.4, 0.4)) {
 					if (e instanceof LivingEntity) {
 						entities.add((LivingEntity) e);
 					}
 				}
 
 				for (LivingEntity e : entities) {
 					if(e==sp);
 					l = e.getLocation();
 					ex = l.getX();
 					ey = l.getY();
 					ez = l.getZ();
 					
 					double changedamage = (int) Math.ceil((float)g.getValue("CHANGEDAMAGE")*loc.toVector().distance(l.toVector()));
 					
 					if(Util.is1x1x2(e)){
 						if ((((bx - .5) <= ex) && (ex <= (bx + 1.5)))&&(((bz - .5) <= ez) && (ez <= (bz + 1.5)))&&(((by - 1) <= ey) && (ey <= by+2.65)))
 							targets.put(e, (int) ((int) g.getValue("DAMAGE")+changedamage));
 					}else if(Util.is1x1x1(e)){
 						if ((((bx - .9) <= ex) && (ex <= (bx + 1.9)))&&(((bz - .9) <= ez) && (ez <= (bz + 1.9)))&&(((by - 1) <= ey) && (ey <= by+1.3))) 
 							targets.put(e, (int) (int) ((int) g.getValue("DAMAGE")+changedamage));
 					}else if(Util.is2x2x1(e)){
 						if ((((bx - .5) <= ex) && (ex <= (bx + 1.5)))&&(((bz - .5) <= ez) && (ez <= (bz + 1.5)))&&(((by - 1) <= ey) && (ey <= by+1.2))) 
 							targets.put(e, (int) ((int) g.getValue("DAMAGE")+changedamage));
 					}else{
 						if ((((bx - .75) <= ex) && (ex <= (bx + 1.75)))&&(((bz - .75) <= ez) && (ez <= (bz + 1.75)))&&(((by - 1) <= ey) && (ey <= by+2.55)))
 							targets.put(e, (int) ((int) g.getValue("DAMAGE")+changedamage));
 					}
 					l = e.getEyeLocation();
 					ex = l.getX();
 					ey = l.getY();
 					ez = l.getZ();
 					if(Util.is1x1x2(e)){
 						if ((((bx - .5) <= ex) && (ex <= (bx + 1.5)))&&(((bz - .5) <= ez) && (ez <= (bz + 1.5)))&&(((by - 1) <= ey) && (ey <= by))) 
 							targets.put(e, (int) ((int) g.getValue("HEADSHOTDAMAGE")+changedamage));
 					}else if(Util.is1x1x1(e)){
 						if ((((bx - .9) <= ex) && (ex <= (bx + 1.9)))&&(((bz - .9) <= ez) && (ez <= (bz + 1.9)))&&(((by - 1) <= ey) && (ey <= by))) 
 							targets.put(e, (int) ((int) g.getValue("HEADSHOTDAMAGE")+changedamage));
 					}else if(Util.is2x2x1(e)){
 						if ((((bx - .5) <= ex) && (ex <= (bx + 1.5)))&&(((bz - .5) <= ez) && (ez <= (bz + 1.5)))&&(((by - 1) <= ey) && (ey <= by))) 
 							targets.put(e, (int) ((int) g.getValue("HEADSHOTDAMAGE")+changedamage));
 					}else{
 						if ((((bx - .75) <= ex) && (ex <= (bx + 1.75)))&&(((bz - .75) <= ez) && (ez <= (bz + 1.75)))&&(((by - 1) <= ey) && (ey <= by))) 
 							targets.put(e, (int)((int) g.getValue("HEADSHOTDAMAGE")+changedamage));
 					}
 				}
 			}
 			return targets;
 		}
 
 		@SuppressWarnings("deprecation")
 		public static void removeAmmo(ArrayList<ItemStack> ammo ,SpoutPlayer p){
 			if(ammo==null) return;
 			HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 			Inventory inv = p.getInventory();
 			ItemStack ammoStack = null; 
 			for(ItemStack theStack: ammo){
 				invAll = inv.all(theStack.getTypeId());
 				for(int j = 0; j<inv.getSize();j++){
 					if(invAll.containsKey(j)){
 						ItemStack hi = invAll.get(j);
 						if(hi.getTypeId()==theStack.getTypeId()&&hi.getDurability()==theStack.getDurability()){
 							ammoStack = hi;
 							break;
 						}
 					}
 				}
 			}
 			if(ammoStack == null){
 				return;
 			}
 			if(ammoStack.getAmount() >1){
 				ammoStack.setAmount(ammoStack.getAmount()-1);
 			}else{
 				inv.remove(ammoStack);
 			}
 			p.updateInventory();
 		}
 
 		public static boolean checkInvForAmmo(SpoutPlayer p, ArrayList<ItemStack> ammo) {
 			if(ammo==null) return true;
 			HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 			Inventory inv = p.getInventory();
 			for(ItemStack theStack : ammo){
 				invAll = inv.all(theStack.getTypeId());
 				for(int j = 0; j<inv.getSize();j++){
 					if(invAll.containsKey(j)){
 						ItemStack hi = invAll.get(j);
 						if(hi.getTypeId()==theStack.getTypeId()&&hi.getDurability()==theStack.getDurability()){
 							return true;
 						}
 					}
 				}
 			}
 			return false;
 		}
 
 		public static int getAmmoCount(SpoutPlayer p, ArrayList<ItemStack> ammo){
 			HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 			int counter = 0;
 			if(ammo==null)return counter;
 			Inventory inv = p.getInventory();
 			for(ItemStack theStack : ammo){
 				invAll = inv.all(theStack.getTypeId());
 				for(int j = 0; j<inv.getSize();j++){
 					if(invAll.containsKey(j)){
 						ItemStack hi = invAll.get(j);
 						if(hi.getTypeId()==theStack.getTypeId()&&hi.getDurability()==theStack.getDurability()){
 							counter+=hi.getAmount();
 						}
 					}
 				}
 			}
 			return counter;
 		}
 
 		public static void performEffects(ArrayList<EffectType> effects, Set<LivingEntity> targets, SpoutPlayer player, Gun gun){
 			Location loc_tar, loc_sp;
 			for(LivingEntity tar : targets){
 				if(tar.equals(player)){
 					continue;
 				}
 				loc_tar = tar.getEyeLocation();
 				loc_sp = player.getEyeLocation();
 				
 				for(EffectType eff : effects){
 					switch(eff.getSection()){
 						case TARGETLOCATION:
 							switch(eff){
 							case EXPLOSION:
 								loc_tar.getWorld().createExplosion(loc_tar, (Integer) eff.getArgument("SIZE"));
 								break;
 							case LIGHTNING:
 								loc_tar.getWorld().strikeLightning(loc_tar);
 								break;
 							case SMOKE:
 								loc_tar.getWorld().playEffect(loc_tar, Effect.SMOKE, (Integer)eff.getArgument("DENSITY"));
 								break;
 							case SPAWN:
 								Location l1 = loc_tar;
 								l1.setY(loc_tar.getY()+1);
 								loc_tar.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 								break;
 							case FIRE:
 								loc_tar.getWorld().playEffect(loc_tar, Effect.MOBSPAWNER_FLAMES, (Integer) eff.getArgument("STRENGTH"));
 								break;
 							case PLACE:
 								loc_tar.getBlock().setTypeId((Integer) eff.getArgument("BLOCK"));
 //								BlockIterator bi = new BlockIterator(player.getWorld(), loc_sp.toVector(), loc_tar.toVector(), 0, (int) gun.getValue("RANGE"));
 //								Block last = null, b = null;
 //								boolean loop=true;
 //								while(bi.hasNext()&&loop){
 //									last = b;
 //									b = bi.next();
 //									if(!Util.isTransparent(b)){
 //										last.setTypeId((Integer) eff.getArgument("BLOCK"));
 //										loop=false;
 //									}
 //								}
 								break;
 							case BREAK:
 								if(MaterialData.getBlock(loc_tar.getBlock().getTypeId()).getHardness()<(Integer) eff.getArgument("POTENCY")){
 									loc_tar.getBlock().setTypeId(0);
 								}
 								break;
 							}
 							break;
 						case SHOOTERLOCATION:
 							switch(eff){
 							case EXPLOSION:
 								loc_sp.getWorld().createExplosion(loc_sp, (Integer) eff.getArgument("SIZE"));
 								break;
 							case LIGHTNING:
 								loc_sp.getWorld().strikeLightning(loc_sp);
 								break;
 							case SMOKE:
 								loc_sp.getWorld().playEffect(loc_sp, Effect.SMOKE, (Integer)eff.getArgument("DENSITY"));
 								break;
 							case SPAWN:
 								Location l1 = loc_sp;
 								l1.setY(loc_tar.getY()+1);
 								loc_sp.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 								break;
 							case FIRE:
 								loc_sp.getWorld().playEffect(loc_sp, Effect.MOBSPAWNER_FLAMES, (Integer) eff.getArgument("STRENGTH"));
 								break;
 							case PLACE:
 								loc_sp.getBlock().setTypeId((Integer) eff.getArgument("BLOCK"));
 //								BlockIterator bi = new BlockIterator(player.getWorld(), loc_sp.toVector(), loc_tar.toVector(), 0, (int) gun.getValue("RANGE"));
 //								Block last = null, b = null;
 //								boolean loop=true;
 //								while(bi.hasNext()&&loop){
 //									last = b;
 //									b = bi.next();
 //									if(!Util.isTransparent(b)){
 //										last.setTypeId((Integer) eff.getArgument("BLOCK"));
 //										loop=false;
 //									}
 //								}
 								break;
 							case BREAK:
 								if(MaterialData.getBlock(loc_tar.getBlock().getTypeId()).getHardness()<(Integer) eff.getArgument("POTENCY")){
 									loc_tar.getBlock().setTypeId(0);
 								}
 								break;
 							}
 							break;
 						case TARGETENTITY:
 							switch(eff){
 								case FIRE:
 									tar.setFireTicks((Integer) eff.getArgument("DURATION"));
 									break;
 								case PUSH:
 									Vector v1 = loc_sp.getDirection();
 									v1.multiply((Double)eff.getArgument("SPEED"));
 									tar.setVelocity(v1);
 									break;
 								case DRAW:
 									Vector v2 = loc_sp.getDirection();
 									v2.multiply((Double)eff.getArgument("SPEED")*-1);
 									tar.setVelocity(v2);
 									break;
 								case POTION:
 									System.out.println(""+tar);
 									tar.addPotionEffect(new PotionEffect(PotionEffectType.getById((Integer) eff.getArgument("ID")), (Integer)eff.getArgument("DURATION"), (Integer) eff.getArgument("STRENGTH")),true);
 									break;
 								}
 							break;
 						case SHOOTER:
 							switch(eff){
 							case FIRE:
 								player.setFireTicks((Integer) eff.getArgument("DURATION"));
 								break;
 							case PUSH:
 								Vector v1 = loc_sp.getDirection();
 								v1.multiply((Double)eff.getArgument("SPEED"));
 								player.setVelocity(v1);
 								break;
 							case DRAW:
 								Vector v2 = loc_sp.getDirection();
 								v2.multiply((Double)eff.getArgument("SPEED")*-1);
 								player.setVelocity(v2);
 								break;
 							case POTION:
 								player.addPotionEffect(new PotionEffect(PotionEffectType.getById((Integer) eff.getArgument("ID")), (Integer)eff.getArgument("DURATION"), (Integer) eff.getArgument("STRENGTH")),true);
 								break;
 							}
 						break;
 						case FLIGHTPATH:
 							BlockIterator bi = new BlockIterator(loc_sp, gun.getValue("RANGE"));
 							boolean loop = true;
 							switch(eff){
 							case FIRE:
 								while(bi.hasNext()){
 									Block b = bi.next();
 									b.getWorld().playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, (Integer) eff.getArgument("STRENGTH"));
 								}
 								break;
 							case EXPLOSION:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b))
 									b.getWorld().createExplosion(b.getLocation(), (Integer) eff.getArgument("SIZE"));
 									else loop=false;
 								}
 								break;
 							case LIGHTNING:
 								loop=true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b))
 									b.getWorld().strikeLightning(b.getLocation());
 									else loop=false;
 								}
 								break;
 							case SMOKE:
 								while(bi.hasNext()){
 									Block b = bi.next();
 									b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, (Integer) eff.getArgument("DENSITY"));
 								}
 								break;
 							case SPAWN:
 								loop=true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									Location l1 = b.getLocation();
 									l1.setY(loc_tar.getY()+1);
 									if(Util.isTransparent(b))
 									b.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 									else loop=false;
 								}
 								break;
 							case PLACE:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b))
 									b.setTypeId((Integer)eff.getArgument("BLOCK"));
 									else loop=false;
 								}
 								break;
 							case BREAK:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(MaterialData.getBlock(b.getTypeId()).getHardness()<(Integer)eff.getArgument("POTENCY")){
 										b.setTypeId(0);
 									}else{
 										loop=false;
 									}
 								}
 								break;
 							}
 							break;
 					}
 				}
 			}
 		}
 
 		public static void performRecoil(GunsPlus plugin, SpoutPlayer p, float recoil){
 			Task t1 = new Task(plugin, p, recoil){
 				public void run() {
 					SpoutPlayer p = (SpoutPlayer) this.getArg(0);
 					Location l = p.getLocation();
 					l.setPitch(l.getPitch() - this.getFloatArg(1)/2);
 					p.teleport(l);
 				}
 			};
 			t1.startRepeating(3, 1, false);
 			Task t2 = new Task(plugin, t1){
 				public void run(){
 					Task t = (Task)this.getArg(0);
 					t.stop();
 				}
 			};
 			t2.startDelayed(5);
 			Task t3 = new Task(plugin, p, recoil){
 				public void run() {
 					SpoutPlayer p = (SpoutPlayer) this.getArg(0);
 					Location l = p.getLocation();
 					l.setPitch(l.getPitch() + this.getFloatArg(1)/3);
 					p.teleport(l);
 				}
 			};
 			t3.startRepeating(6, 1, false);
 			Task t4 = new Task(plugin, t3){
 				public void run(){
 					Task t = (Task)this.getArg(0);
 					t.stop();
 				}
 			};
 			t4.startDelayed(9);
 		}
 
 		public static void performKnockBack(SpoutPlayer p, float knockback){
 			Location loc = p.getLocation();
 			if(loc.getPitch()>5){
 				loc.setPitch(0);
 			}else if(loc.getPitch()<-5){
 				loc.setPitch(0);
 			}
 			Vector pdir = Util.getDirection(loc);
 			Vector v = pdir;
 			v.setX(v.getX()*(knockback/100)*-1);
 			v.setZ(v.getZ()*(knockback/100)*-1);
 			p.setVelocity(v);
 		}
 
 		public static Gun getGunInHand(SpoutPlayer p) {
 			ItemStack is = p.getItemInHand();
 
 			if(holdsGun(p)){
 				for(Gun g:GunsPlus.allGuns){
 					SpoutItemStack sis = new SpoutItemStack(g);
 					if(is.getTypeId()==sis.getTypeId()&&is.getDurability()==sis.getDurability()){
 						return g;
 					}
 				}
 			}
 			return null;
 		}
 
 		public static Gun getGun(ItemStack item){
 			for(Gun g:GunsPlus.allGuns){
 				SpoutItemStack sis = new SpoutItemStack(g);
 				if(item.getTypeId()==sis.getTypeId()&&item.getDurability()==sis.getDurability()){
 					return g;
 				}
 			}
 			return null;
 		}
 
 		public static void zoomOut(SpoutPlayer p){
 //			PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, 0, 100);
 //			p.addPotionEffect(pe, true);
 			SpoutPlayer sp = (SpoutPlayer) p;
 			//CAN be used still!!!
 			CraftPlayer cp = (CraftPlayer) p;
 			
 			try {
 	            Field field = EntityLiving.class.getDeclaredField("effects");
 	            field.setAccessible(true);
 	            @SuppressWarnings("rawtypes")
 				HashMap effects = (HashMap)field.get(cp.getHandle());
 	            effects.remove(2);
 	            EntityPlayer player = cp.getHandle();
 	            player.netServerHandler.sendPacket(new Packet42RemoveMobEffect(player.id, new MobEffect(2, 0, 0)));
 	            cp.getHandle().getDataWatcher().watch(8, Integer.valueOf(0));
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 			sp.getMainScreen().removeWidget(GunsPlus.zoomTextures.get(p));
 			GunsPlus.zoomTextures.remove(p);
 		}
 		
 		public static void zoomIn(GunsPlus plugin, SpoutPlayer p, GenericTexture zTex,int  zoomfactor){
 //			SpoutPlayer sp = (SpoutPlayer) p;
 //			PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, 24000, zoomfactor);
 //			p.addPotionEffect(pe, true);
 			//CAN be used still!!!
 			SpoutPlayer sp = (SpoutPlayer) p;
 			CraftPlayer cp = (CraftPlayer) p;
 			
 			cp.getHandle().addEffect(new MobEffect(2, 24000, zoomfactor));
 			try{
 				Field field;
 			field = EntityLiving.class.getDeclaredField("effects");
 	        field.setAccessible(true);
 	        @SuppressWarnings("rawtypes")
 			HashMap effects = (HashMap)field.get(cp.getHandle());
 	        effects.remove(2);
 			}catch(NoSuchFieldException e){
 				e.printStackTrace();
 			}catch(IllegalAccessException e1){
 				e1.printStackTrace();
 			}
 			
 			if(!(zTex==null)){
 				GenericTexture t = zTex;
 				t.setHeight(sp.getMainScreen().getHeight()).setWidth(sp.getMainScreen().getWidth());
 				sp.getMainScreen().attachWidget(plugin, t);
 				GunsPlus.zoomTextures.put(sp, t);
 			}
 		}
 		
 		public static boolean isHudEnabled(Gun g){
 			if((Boolean)g.getObject("HUDENABLED")) return true;
 			return false;
 		}
 
 		public static boolean isGun(ItemStack i){
 			for(Gun g:GunsPlus.allGuns){
 				SpoutItemStack sis = new SpoutItemStack(g);
 				if(i.getTypeId()==sis.getTypeId()&&i.getDurability()==sis.getDurability()){
 					return true;
 				}
 			}
 			return false;
 		}
 }
