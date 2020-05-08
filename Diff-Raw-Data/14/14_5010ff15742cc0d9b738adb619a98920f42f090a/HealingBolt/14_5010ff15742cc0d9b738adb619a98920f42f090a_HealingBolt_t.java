 package com.github.thelonedevil.TLDSpells.Spells;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.spout.api.entity.Entity;
 import org.spout.api.entity.Player;
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.event.entity.EntityInteractEntityEvent;
 import org.spout.api.geo.discrete.Point;
 import org.spout.vanilla.component.entity.misc.Health;
 import org.spout.vanilla.component.entity.misc.Level;
 import org.spout.vanilla.component.entity.substance.projectile.Snowball;
 import org.spout.vanilla.data.effect.GeneralEffect;
 import org.spout.vanilla.data.effect.type.SmokeEffect;
 import org.spout.vanilla.event.world.PlayParticleEffectEvent;
 
 import com.github.thelonedevil.TLDSpells.TLDSpellsPlugin;
 import com.github.thelonedevil.TLDSpells.event.PlayerCastSpellEvent;
 
 public class HealingBolt extends Bolt implements Listener {
 	private final TLDSpellsPlugin plugin;
 
 	public HealingBolt(TLDSpellsPlugin instance) {
 		this.plugin = instance;
 	}
 
 	private HashMap<UUID, Player> list = new HashMap<UUID, Player>();
 
 	public void cast(Player p) {
 		Entity s = Bolt.cast(p, Snowball.class); //TODO replace Snowball.class with real healthbolt class
 		list.put(s.getUID(), p);
 		PlayerCastSpellEvent event = new PlayerCastSpellEvent(p, this);
 		plugin.getEngine().getEventManager().callEvent(event);
 	}
 
 	@EventHandler
	public void healthbolt(EntityInteractEntityEvent event) {
 		UUID e = event.getEntity().getUID();
 		int level = list.get(e).get(Level.class).getLevel();
 		int basedamage = 4;
 		int damage = level * basedamage;
 		damage(event, damage);
 
 	}
 
 	void damage(EntityInteractEntityEvent event, int amount) {
 		UUID e = event.getEntity().getUID();
 		if (list.containsKey(e)) {
 			Entity hit = event.getInteracted();
 				hit.get(Health.class).heal(amount);
 				Point loc = hit.getPhysics().getPosition();
 				GeneralEffect effect = new SmokeEffect(1);
 				PlayParticleEffectEvent particle = new PlayParticleEffectEvent(loc, effect, 0);
 				plugin.getEngine().getEventManager().callEvent(particle);
 			
 		}
 	}
 }
