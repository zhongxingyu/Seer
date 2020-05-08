 package com.Top_Cat.CODMW.Killstreaks;
 
 import java.lang.reflect.Constructor;
import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import com.Top_Cat.CODMW.main;
 import com.Top_Cat.CODMW.sql.Stat;
 
 public enum Killstreaks {
     CLAYMORES(1, 3, claymore.class, Material.WALL_SIGN, Stat.CLAYMORES_ACHIEVED, 2),
     POWER_PORK(2, 5, pork.class, Material.GRILLED_PORK, Stat.PORK_ACHIEVED),
     INVULNERABLE_APPLE(3, 6, apple.class, Material.APPLE, Stat.APPLES_ACHIEVED),
     SENTRY(4, 7, sentry.class, Material.DISPENSER, Stat.SENTRIES_ACHIEVED),
     DOGS(5, 9, WolfPack.class, Material.BONE, Stat.DOGS_ACHIEVED),
     HELICOPTER(6, 11, chopper.class, Material.DIAMOND, Stat.CHOPPERS_ACHIEVED),
     ;
     
     final private int id;
     final private int kills;
     final private Class<? extends killstreak> k;
     final private Material m;
     final private Stat ach;
     final private int amm;
 
     private Killstreaks(int id, int kills, Class<? extends killstreak> k, Material m, Stat ach, int amm) {
         this.id = id;
         this.kills = kills;
         this.k = k;
         this.m = m;
         this.ach = ach;
         this.amm = amm;
     }
     
     private Killstreaks(int id, int kills, Class<? extends killstreak> k, Material m, Stat ach) {
     	this(id, kills, k, m, ach, 1);
     }
     
 	public int getId() {
 		return id;
 	}
 	
 	public int getKills() {
 		return kills;
 	}
 	
 	public Stat getStat() {
 		return ach;
 	}
 	
 	public int getAmm() {
 		return amm;
 	}
 	
 	public Class<? extends killstreak> getkClass() {
 		return k;
 	}
 	
 	public killstreak callIn(main instance, Player owner, Object[] args) {
 		try {
 			Constructor<? extends killstreak> c = k.getDeclaredConstructor(new Class[] {main.class, Player.class, Object.class});
 			c.setAccessible(true);
 			c.newInstance(instance, owner, args);
 			killstreak n = k.newInstance();
 			instance.ks.add(n);
 			return n;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Material getMat() {
 		return m;
 	}	
     
    public static ArrayList<Killstreaks> table = new ArrayList<Killstreaks>(); 
     static {
         for (Killstreaks i : Killstreaks.values()) {
            table.add(i.getId(), i);
         }
     }
 
     public static Killstreaks valueOf(int id) {
         return table.get(id);
     }
     
     public static HashMap<Material, Killstreaks> table2 = new HashMap<Material, Killstreaks>(); 
     static {
         for (Killstreaks i : Killstreaks.values()) {
             table2.put(i.getMat(), i);
         }
     }
 
     public static Killstreaks fromMaterial(Material m) {
         return table2.get(m);
     }
     
 }
