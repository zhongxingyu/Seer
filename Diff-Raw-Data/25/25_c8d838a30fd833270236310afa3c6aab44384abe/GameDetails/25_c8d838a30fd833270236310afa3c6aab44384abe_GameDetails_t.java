 package net.threedoubloons.legendaryrandomiser;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import net.threedoubloons.legendaryrandomiser.data.CardType;
 import net.threedoubloons.legendaryrandomiser.data.Hero;
 import net.threedoubloons.legendaryrandomiser.data.Mastermind;
 import net.threedoubloons.legendaryrandomiser.data.Scheme;
 import net.threedoubloons.legendaryrandomiser.data.Sets;
 import net.threedoubloons.legendaryrandomiser.data.Villain;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class GameDetails implements Parcelable {
 	private static Random r = new Random();
 	private final static int[] numVillainsPerPlayer = {0, 1, 2, 3, 3, 4};
 	private final static int[] numHenchmenPerPlayer = {0, 1, 1, 1, 2, 2};
 	private final static int[] numHeroesPerPlayer = {0, 3, 5, 5, 5, 6};
 	private final static int[] numBystanderPerPlayer = {0, 1, 2, 8, 8, 12};
 
 	private EnumSet<Sets> activeSets = Sets.make(Sets.CoreSet);
 	private int numPlayers = 2;
 	private int numVillains = 2;
 	private int numHenchmen = 1;
 	private int numHeroes = 1;
 	private Mastermind mastermind = Mastermind.none;
 	private Scheme scheme = Scheme.none;
 	private ArrayList<Villain> villains = new ArrayList<Villain>();
 	private ArrayList<Villain> henchmen = new ArrayList<Villain>();
 	private ArrayList<Hero> heroes = new ArrayList<Hero>();
 	private ArrayList<Hero> reservedHeroes = new ArrayList<Hero>();
 	private transient HashMap<CardType, Integer> villainDeckContents = new HashMap<CardType, Integer>();
 	private transient HashMap<CardType, Integer> heroDeckContents = new HashMap<CardType, Integer>();
 	private transient ArrayList<String> notes = new ArrayList<String>();
 	private transient ArrayList<String> errors = new ArrayList<String>();
 	private boolean useAdvancedRules = false;
 	private ArrayList<Villain> reservedVillains = new ArrayList<Villain>();
 	
 	public GameDetails() {
 		initialiseLists();
 	}
 
 	public GameDetails(GameDetails other) {
 		super();
 		this.activeSets = other.activeSets;
 		this.numPlayers = other.numPlayers;
 		this.numVillains = other.numVillains;
 		this.numHenchmen = other.numHenchmen;
 		this.numHeroes = other.numHeroes;
 		this.mastermind = other.mastermind;
 		this.scheme = other.scheme;
 		this.villains = new ArrayList<Villain>(other.villains);
 		this.henchmen = new ArrayList<Villain>(other.henchmen);
 		this.heroes = new ArrayList<Hero>(other.heroes);
 		this.useAdvancedRules = other.useAdvancedRules;
 	}
 	
 	public GameDetails(Parcel in) {
 		activeSets = Sets.make(in.readLong());
 		numPlayers = in.readInt();
 		
 		mastermind = Mastermind.get(in.readString());
 		scheme = Scheme.get(in.readString());
 		
 		applyPlayerCount();
 		applyScheme();
 		
 		int arrSize = in.readInt();
 		villains = new ArrayList<Villain>(arrSize);
 		for (int i = 0; i < arrSize; ++i) {
 			villains.add(Villain.get(in.readString()));
 		}
 		
 		arrSize = in.readInt();
 		henchmen = new ArrayList<Villain>(arrSize);
 		for (int i = 0; i < arrSize; ++i) {
 			henchmen.add(Villain.get(in.readString()));
 		}
 
 		arrSize = in.readInt();
 		heroes = new ArrayList<Hero>(arrSize);
 		for (int i = 0; i < arrSize; ++i) {
 			heroes.add(Hero.get(in.readString()));
 		}
 
 		arrSize = in.readInt();
 		reservedHeroes = new ArrayList<Hero>(arrSize);
 		for (int i = 0; i < arrSize; ++i) {
 			reservedHeroes.add(Hero.get(in.readString()));
 		}
 		
 		useAdvancedRules = (in.readByte() != 0);
 	}
 
 	public void setActiveSets(long newSets) {
 		activeSets = Sets.make(newSets);
 		initialiseLists();
 	}
 	
 	public int getNumPlayers() {
 		return numPlayers;
 	}
 
 	public void setNumPlayers(int numPlayers) {
 		this.numPlayers = numPlayers;
 	}
 	
 	public void setUseAdvancedRules(boolean useAdvancedRules) {
 		this.useAdvancedRules = useAdvancedRules;
 	}
 
 	public int getNumVillains() {
 		return numVillains;
 	}
 
 	public void setNumVillains(int numVillains) {
 		this.numVillains = numVillains;
 	}
 
 	public int getNumHenchmen() {
 		return numHenchmen;
 	}
 
 	public void setNumHenchmen(int numHenchmen) {
 		this.numHenchmen = numHenchmen;
 	}
 	
 	public int getNumHeroes() {
 		return numHeroes;
 	}
 
 	public void setNumHeroes(int numHeroes) {
 		this.numHeroes = numHeroes;
 	}
 
 	public final Mastermind getMastermind() {
 		return mastermind;
 	}
 	
 	public final Collection<Villain> getVillains() {
 		return villains;
 	}
 	
 	public final Collection<Villain> getHenchmen() {
 		return henchmen;
 	}
 	
 	public final Collection<Hero> getHeroes() {
 		return heroes;
 	}
 	
 	public final Collection<String> getNotes() {
 		return notes;
 	}
 	
 	public final Collection<String> getErrors() {
 		return errors;
 	}
 	
 	public int getNumNotes() {
 		return notes.size() + errors.size();
 	}
 	
 	public final Collection<Map.Entry<CardType, Integer>> getVillainsDeckContents() {
 		return villainDeckContents.entrySet();
 	}
 	
 	public void setVillainDeckContentsForCardType(CardType type, int number) {
 		villainDeckContents.put(type, number);
 	}
 	
 	public final Collection<Map.Entry<CardType, Integer>> getHeroDeckContents() {
 		return heroDeckContents.entrySet();
 	}
 	
 	public void setHeroDeckContentsForCardType(CardType type, int number) {
 		heroDeckContents.put(type, number);
 	}
 	
 	public void randomiseAll() {
 		notes.clear();
 		errors.clear();
 		addRandomMastermind();
 		applyPlayerCount();
 		addRandomScheme();
 		applyScheme();
 		addAlwaysLeads();
 		addVillains();
 		addHenchmen();
 		addHeroes();
 		checkInconsistencies();
 	}
 
 	private void checkInconsistencies() {
 		if (villains.size() < numVillains) {
 			addError("Too few villains");
 		}
 		
 		if (henchmen.size() < numHenchmen) {
 			addError("Too few henchmen");
 		}
 		
 		if (heroes.size() < numHeroes) {
 			addError("Too few heroes");
 		}
 	}
 
 	public void initialiseLists() {
 		Hero.initialiseAllList(activeSets);
 		Mastermind.initialiseAllList(activeSets);
 		Scheme.initialiseAllList(activeSets);
 		Villain.initialiseAllList(activeSets);
 	}
 
 	public void addRandomMastermind() {
 		if (mastermind != Mastermind.none) return;
 		int mPosition = r.nextInt(Mastermind.getAll().size());
 		mastermind = Mastermind.getAll().get(mPosition);
 	}
 	
 	private void applyPlayerCount() {
 		numVillains = numVillainsPerPlayer[numPlayers];
 		numHenchmen = numHenchmenPerPlayer[numPlayers];
 		setNumHeroes(numHeroesPerPlayer[numPlayers]);
 		setVillainDeckContentsForCardType(CardType.bystander, numBystanderPerPlayer[numPlayers]);
 		if (numPlayers == 1 && !useAdvancedRules) {
 			setVillainDeckContentsForCardType(CardType.masterStrike, 1);
 		} else {
 			setVillainDeckContentsForCardType(CardType.masterStrike, 5);
 		}
 		
 		if (numPlayers == 1) {
 			addNote("Use only 3 henchman cards");
 		}
 	}
 	
 	public void addRandomScheme() {
 		if (scheme != Scheme.none) return;
 		int mPosition;
 		Scheme scheme;
 		boolean isAcceptable;
 		do {
 			mPosition = r.nextInt(Scheme.getAll().size());
 			scheme = Scheme.getAll().get(mPosition);
 			isAcceptable = numPlayers == 1 ? scheme.isSPAcceptable() : true; 
 		} while (!isAcceptable);
 		this.scheme = scheme;
 	}
 	
 	private void applyScheme() {
 		scheme.applyScheme(this);
 	}
 	
 	public boolean addAlwaysLeads() {
 		if (numPlayers == 1) {
 			return true;
 		}
 		
 		Villain preferred = getMastermind().getAlwaysLeads();
 		if (preferred == null) {
 			return false;
 		}
 		
 		if (preferred.isHenchman()) {
 			return addPreferredHenchman(preferred);
 		} else {
 			return addPreferredVillain(preferred);
 		}
 	}
 	
 	public boolean addPreferredVillain(Villain preferred) {
 		if (villains.contains(preferred)) {
 			return true;
 		}
 		
 		if (villains.size() >= numVillains) {
 			return false;
 		}
 		
 		villains.add(preferred);
 		return true;
 	}
 	
 	public boolean addPreferredHenchman(Villain preferred) {
 		if (henchmen.contains(preferred)) {
 			return true;
 		}
 		
 		if (henchmen.size() >= numHenchmen) {
 			return false;
 		}
 		
 		henchmen.add(preferred);
 		return true;
 	}
 
 	private void addVillains() {
 		if (numVillains >= Villain.getAll().size()) {
 			villains = new ArrayList<Villain>(Villain.getAll());
 		} else {
 			while (villains.size() < numVillains) {
 				addRandomVillain();
 			}
 		}
 	}
 	
 	private void addRandomVillain() {
 		Villain v = getRandomVillain();
 		villains.add(v);
 	}
 
 	public Villain getRandomVillain() {
 		int mPosition;
 		Villain v;
 		do {
 			mPosition = r.nextInt(Villain.getAll().size());
 			v = Villain.getAll().get(mPosition);
 		} while (villains.contains(v) || reservedVillains.contains(v));
 		return v;
 	}
 
 	private void addHenchmen() {
 		if (numHenchmen >= Villain.getAllHenchmen().size()) {
 			henchmen = new ArrayList<Villain>(Villain.getAllHenchmen());
 		} else {
 			while (henchmen.size() < numHenchmen) {
 				addRandomHenchman();
 			}
 		}
 	}
 	
 	private void addRandomHenchman() {
 		Villain v = getRandomHenchman();
 		henchmen.add(v);
 	}
 
 	public Villain getRandomHenchman() {
		List<Villain> allHenchmen = Villain.getAllHenchmen();
		if (allHenchmen.size() == 0) {
			return Villain.none;
		}
		
 		int vPosition;
 		Villain v;
 		do {
			vPosition = r.nextInt(allHenchmen.size());
			v = allHenchmen.get(vPosition);
 		} while (henchmen.contains(v) || reservedVillains.contains(v));
 		return v;
 	}
 
 	private void addHeroes() {
 		if (numHeroes >= Hero.getAll().size()) {
 			heroes = new ArrayList<Hero>(Hero.getAll());
 		} else {
 			while (heroes.size() < numHeroes) {
 				addRandomHero();
 			}
 		}
 	}
 	
 	public Hero getRandomHero() {
 		int vPosition;
 		Hero h;
 		do {
 			vPosition = r.nextInt(Hero.getAll().size());
 			h = Hero.getAll().get(vPosition);
 		} while (heroes.contains(h) || reservedHeroes.contains(h));
 		
 		return h;
 	}
 	
 	public void reserveHero(Hero h) {
 		reservedHeroes.add(h);
 	}
 	
 	public void reserveVillain(Villain h) {
 		reservedVillains.add(h);
 	}
 	
 	public void reserveHenchman(Villain h) {
 		reservedVillains.add(h);
 	}
 	
 	private void addRandomHero() {
 		heroes.add(getRandomHero());
 	}
 
 	public final Scheme getScheme() {
 		return scheme;
 	}
 	
 	public void addNote(String note) {
 		notes.add(note);
 	}
 	
 	public void addError(String note) {
 		errors.add(note);
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeLong(Sets.unmake(activeSets));
 		dest.writeInt(numPlayers);
 		
 		dest.writeString(mastermind.name());
 		dest.writeString(scheme.name());
 		
 		dest.writeInt(villains.size());
 		for (Villain v : villains) {
 			dest.writeString(v.name());
 		}
 		
 		dest.writeInt(henchmen.size());
 		for (Villain h : henchmen) {
 			dest.writeString(h.name());
 		}
 
 		dest.writeInt(heroes.size());
 		for (Hero h : heroes) {
 			dest.writeString(h.name());
 		}
 
 		dest.writeInt(reservedHeroes.size());
 		for (Hero h : reservedHeroes) {
 			dest.writeString(h.name());
 		}
 		
 		dest.writeByte((byte)(useAdvancedRules ? 1 : 0));
 	}
 	
 	 public static final Parcelable.Creator<GameDetails> CREATOR
 		     = new Parcelable.Creator<GameDetails>() {
 		 public GameDetails createFromParcel(Parcel in) {
 		     return new GameDetails(in);
 		 }
 		
 		 public GameDetails[] newArray(int size) {
 		     return new GameDetails[size];
 		 }
 	 };
 }
