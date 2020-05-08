 package net.marcuswhybrow.minecraft.law;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import net.marcuswhybrow.minecraft.law.interfaces.PrisonerContainer;
 import net.marcuswhybrow.minecraft.law.interfaces.Referenceable;
 import net.marcuswhybrow.minecraft.law.prison.PrisonCell;
 
 public abstract class Entity implements Referenceable, PrisonerContainer, Serializable {
 	private static final long serialVersionUID = -3452269165185206945L;
 	/** A map of prisoner names to prison cells */
 	private Map<String, PrisonCell> prisoners;
 	/** A map of prisoner names to prison cells, for players which are being released from prison */
 	private Map<String, PrisonCell> prisonersToRelease;
 	/** A map of prisoner names to prison cells, for players which are to be imprisoned */
 	private Map<String, PrisonCell> prisonersToImprison;
 	/** The in-game name of this entity */
 	private String name;
 	private PrisonerContainer parent;
 	private Set<PrisonerContainer> children;
 	
 	public Entity() {
 		this.prisoners = new HashMap<String, PrisonCell>();
 		this.prisonersToRelease = new HashMap<String, PrisonCell>();
 		this.prisonersToImprison = new HashMap<String, PrisonCell>();
 		this.name = null;
 		this.parent = null;
 		this.children = null;
 	}
 
 	@Override
 	public abstract boolean imprisonPlayer(String playerName);
 
 	@Override
 	public void imprisonPlayers(Set<String> playerNames) {
 		if (playerNames == null) {
 			throw new IllegalArgumentException("Player names set cannot be null");
 		}
 		
 		for (String playerName : playerNames) {
 			this.imprisonPlayer(playerName);
 		}
 	}
 
 	@Override
 	public void freePlayer(String playerName) {
 		PrisonCell cell = this.getPrisonerCell(playerName);
 		if (cell == null) {
 			return;
 		}
 		
 		cell.releasePrisoner(playerName);
 	}
 
 	@Override
 	public void freePlayers(Set<String> playerNames) {
 		if (playerNames == null) {
 			throw new IllegalArgumentException("Player names set cannot be null");
 		}
 		
 		for (String playerName : playerNames) {
 			this.freePlayer(playerName);
 		}
 	}
 
 	@Override
 	public void freePlayers() {
 		this.freePlayers(this.prisoners.keySet());
 	}
 
 	@Override
 	public Set<String> getPrisoners() {
 		return this.prisoners.keySet();
 	}
 	
 	@Override
 	public void setParentPrisonerContainer(PrisonerContainer prisonerContiainer) {
 		this.parent = prisonerContiainer;
 	}
 
 	@Override
 	public PrisonerContainer getParentPrisonerContainer() {
 		return this.parent;
 	}
 	
 	@Override
 	public void addChildPrisonerContainer(PrisonerContainer prisonerContiainer) {
 		this.children.add(prisonerContiainer);
 	}
 	
 	@Override
 	public void removeChildPrisonerContainer(PrisonerContainer prisonerContiainer) {
 		this.children.remove(prisonerContiainer);
 	}
 
 	@Override
 	public Set<PrisonerContainer> getChildPrisonerContainers() {
 		return this.children;
 	}
 
 	@Override
 	public void addPrisoner(String playerName, PrisonCell cell) {
 		if (this.parent != null) {
 			this.parent.addPrisoner(playerName, cell);
 		}
 		this.prisonersToImprison.put(playerName.toLowerCase(), cell);
 	}
 	
 	@Override
 	public void securePrisoner(String playerName) {
 		if (this.parent != null) {
 			this.parent.securePrisoner(playerName);
 		}
		PrisonCell cell = this.prisonersToImprison.get(playerName.toLowerCase());
 		if (cell != null) {
 			this.prisoners.put(playerName.toLowerCase(), cell);
 		}
 	}
 	
 	@Override
 	public void releasePrisoner(String playerName) {
 		if (this.parent != null) {
 			this.parent.releasePrisoner(playerName);
 		}
 		PrisonCell cell = this.prisoners.remove(playerName.toLowerCase());
 		if (cell != null) {
 			this.prisonersToRelease.put(playerName.toLowerCase(), cell);
 		}
 	}
 
 	@Override
 	public void removePrisoner(String playerName) {
 		if (this.parent != null) {
 			this.parent.removePrisoner(playerName);
 		}
 		this.prisoners.remove(playerName.toLowerCase());
 		this.prisonersToRelease.remove(playerName.toLowerCase());
 	}
 	
 	@Override
 	public boolean hasUnreleasedPrisoner(String playerName) {
 		return this.prisonersToRelease.containsKey(playerName.toLowerCase());
 	}
 	
 	@Override
 	public boolean hasUnsecuredPrisoner(String playerName) {
 		return this.prisonersToImprison.containsKey(playerName.toLowerCase());
 	}
 
 	@Override
 	public PrisonCell getPrisonerCell(String playerName) {
 		PrisonCell cell = this.prisonersToImprison.get(playerName.toLowerCase());
 		if (cell == null) {
 			cell = this.prisoners.get(playerName.toLowerCase());
 		}
 		if (cell == null) {
 			cell = this.prisonersToRelease.get(playerName.toLowerCase());
 		}
 		return cell;
 	}
 
 	@Override
 	public boolean hasPrisoner(String playerName) {
		return this.prisoners.containsKey(playerName.toLowerCase());
 	}
 	
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 	
 	@Override
 	public boolean hasPrisoners() {
 		if (this.prisoners.isEmpty() == false) {
 			return true;
 		}
 		if (this.prisonersToImprison.isEmpty() == false) {
 			return true;
 		}
 		if (this.prisonersToRelease.isEmpty() == false) {
 			return true;
 		}
 		return false;
 	}
 }
