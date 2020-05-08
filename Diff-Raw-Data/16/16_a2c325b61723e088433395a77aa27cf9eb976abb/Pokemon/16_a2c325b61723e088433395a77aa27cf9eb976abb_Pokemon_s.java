 package com.podts.pokemon.server.pokemon;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.podts.pokemon.server.Identifiable;
 import com.podts.pokemon.server.Namable;
 import com.podts.pokemon.server.Ownable;
 import com.podts.pokemon.server.Owner;
 import com.podts.pokemon.server.io.Cachable;
 import com.podts.pokemon.server.player.Player;
 
 public class Pokemon implements Namable, Identifiable, Ownable, Cachable {
 	
 	private final static Map<Integer,Pokemon> allpokemon = new HashMap<Integer, Pokemon>();
 	private static long nextid;
 	
 	public static Collection<Pokemon> getAllPokemon() {
 		synchronized (Pokemon.class) {
 			return allpokemon.values();
 		}
 	}
 	
 	public static Pokemon getPokemon(int id) {
 		synchronized (Pokemon.class) {
 			return allpokemon.get(id);
 		}
 	}
 	
 	public static int GCPokemon() {
 		int cleared = 0;
 		long start = System.currentTimeMillis();
		for (Pokemon p : getAllPokemon()) {
			if (!p.getKeep()) {
				if (p.getLastGrab()+p.getCacheTime() < start) {
					p.uncache();
					cleared++;
					continue;
 				}
 			}
 		}
 		return cleared;
 	}
 	
 	public static final void init() {
 		//set nextid from iobackend;
 		synchronized (Pokemon.class) {
 			
 		}
 	}
 	
 	private static long getNextID() {
 		synchronized (Pokemon.class) {
 			nextid++;
 			return nextid-1;
 		}
 	}
 	
 	private final long id;
 	private final PokemonType type;
 	private Gender gender;
 	private Owner owner;
 	private long cachetime, lastgrab = System.currentTimeMillis();
 	private boolean cache;
 	private String name;
 	
 	@Override
 	public final long getID() {
 		return id;
 	}
 	
 	public final PokemonType getType() {
 		return type;
 	}
 	
 	public Gender getGender() {
 		return gender;
 	}
 	
 	public void setGender(Gender g) {
 		gender = g;
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public Owner getOwner() {
 		return owner;
 	}
 
 	@Override
 	public void setOwner(Owner o) {
 		if (o == null)
 			return;
 		if (o instanceof Player)
 			owner = o;
 	}
 
 	@Override
 	public long getLastGrab() {
 		return lastgrab;
 	}
 
 	@Override
 	public void updateGrab() {
 		lastgrab = System.currentTimeMillis();
 	}
 
 	@Override
 	public void uncache() {
 		// TODO remove pokemon from RAM.
 		
 	}
 	
 	@Override
 	public boolean getKeep() {
 		return cache;
 	}
 	
 	@Override
 	public void setKeep(boolean keep) {
 		this.cache = keep;
 	}
 	
 	public Pokemon(PokemonType pokemon, Gender gender) {
 		id = getNextID();
 		type = pokemon;
 		setGender(gender);
 	}
 
 	@Override
 	public long getCacheTime() {
 		return cachetime;
 	}
 
 	@Override
 	public void setCacheTime(long time) {
 		cachetime = time;
 	}
 	
 }
