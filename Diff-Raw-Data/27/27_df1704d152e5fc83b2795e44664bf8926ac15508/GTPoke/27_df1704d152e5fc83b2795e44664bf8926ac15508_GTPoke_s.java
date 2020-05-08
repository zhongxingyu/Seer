 package edu.gatech.CS2340.GrandTheftPoke;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.thoughtworks.xstream.XStream;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.GameMap;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlace;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Turn;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Player;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Rocket;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 import edu.gatech.CS2340.GrandTheftPoke.files.SaveGame;
 import edu.gatech.CS2340.GrandTheftPoke.screens.Battle;
 import edu.gatech.CS2340.GrandTheftPoke.screens.EncounterScreen;
 import edu.gatech.CS2340.GrandTheftPoke.screens.MainMenu;
 import edu.gatech.CS2340.GrandTheftPoke.screens.MapScreen;
 import edu.gatech.CS2340.GrandTheftPoke.screens.Market;
 import edu.gatech.CS2340.GrandTheftPoke.screens.MarketOld;
 import edu.gatech.CS2340.GrandTheftPoke.screens.MarketPlaceItemDemo;
 import edu.gatech.CS2340.GrandTheftPoke.screens.Name;
 import edu.gatech.CS2340.GrandTheftPoke.screens.SkillPoints;
 import edu.gatech.CS2340.GrandTheftPoke.screens.SplashScreen;
 import edu.gatech.CS2340.GrandTheftPoke.screens.StarterPokemon;
 import edu.gatech.CS2340.GrandTheftPoke.screens.TownScreen;
 
 public class GTPoke extends Game {
 	private String playerName = "";
 	private Player thePlayer;
 	private GlobalItemReference items;
 	private GameMap theMap;
 	private Turn controller;
 	private ArrayList<Person> gameActors;
 	private TextureAtlas atlas;
 	private static Texture ButtonSprite;
 	private static final int INITIAL_RANGE = 80;
 	private static final int INITIAL_CARRY = 30;
 	private static final int INITIAL_HEALTH = 100;
 	private XStream xstream;
 	private FileHandle saveFile;
 	private ArrayList<SaveGame> saves;
 	private ArrayList<String> savestrs;
 	private Screen nextScreen, prevScreen;
 
 	@Override
 	public void create() {
 		items = new GlobalItemReference();
 		xstream = new XStream();
 		saveFile = Gdx.files.local("saves\\savegame.xml");
 		atlas = new TextureAtlas(Gdx.files.internal("images//textures//packed//gtpoke.atlas"));
 		Pixmap map = new Pixmap(150, 600, Pixmap.Format.RGB565);
 		map.setColor(Color.GRAY);
 		map.fillRectangle(0, 0, 150, 600);
 		map.setColor(Color.BLACK);
 		map.fillRectangle(0, 200, 150, 200);
 		map.setColor(Color.RED);
 		map.fillRectangle(0, 400, 150, 200);
 		ButtonSprite = new Texture(map);
 		
 		gameActors = new ArrayList<Person>();
 		theMap = makeMap();
 		
 		setScreen(getSplashScreen());
 	}
 	
 	public TextureAtlas getTextures(){
 		return atlas;
 	}
 	
 	public Screen getNextScreen(){
 		return nextScreen;
 	}
 	
 	public Screen getPrevScreen(){
 		return prevScreen;
 	}
 	
 	public void newGame(){
 		theMap = makeMap();
 		thePlayer = new Player();
 		controller = new Turn(theMap, thePlayer, items);
 		prevScreen = getMainMenuScreen();
 		nextScreen = getNameScreen();
 		setScreen(getNameScreen());
 	}
 	
 	public String save() {
 		SaveGame save = new SaveGame(thePlayer, theMap, controller);
 		String savestr = xstream.toXML(save);
 		System.out.println(savestr);
 		//savestrs.add(savestr);
 		saveFile.writeString(savestr, false);
 		return savestr;
 	}
 
 	public void load(int index) {
 		String savestr = saveFile.readString();
 		System.out.println(savestr);
 		SaveGame game = (SaveGame) xstream.fromXML(savestr);
 		game.load(this);
 		setScreen(getCurrentTownScreenFromEncounter());
 	}
 
 	public void load(String gamestr) {
 		SaveGame game = (SaveGame) xstream.fromXML(gamestr);
 		game.load(this);
 	}
 
 	public void setPlayer(Player player) {
 		thePlayer = player;
 	}
 
 	public void setMap(GameMap map) {
 		theMap = map;
 	}
 	public void setController(Turn controller){
 		this.controller = controller;
 	}
 	public void clear() {
 		theMap = null;
 		thePlayer = null;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	public void render() {
 		super.render();
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 	}
 
 	public void setPlayerName(String playerName) {
 		thePlayer.setName(playerName);
 
 	}
 
 	public void createPlayer(Integer strength, Integer agility, Integer trade,
 			Integer stamina) {
 		thePlayer = new Player(playerName, strength, agility, trade, stamina,
 				INITIAL_HEALTH, INITIAL_RANGE, INITIAL_CARRY, theMap);
 		controller = new Turn(theMap, thePlayer, items);
 	}
 
 	public boolean equals(GTPoke game) {
 		return theMap.equals(game.getMap()) && thePlayer.equals(game.thePlayer);
 
 	}
 
 	public GameMap makeMap() {
 		return new GameMap(items);
 	}
 
 	public Town getCurrentTown() {
 		return thePlayer.getCurrent();
 	}
 
 	public static Texture getButtonSprite() {
 		return ButtonSprite;
 	}
 
 	public GlobalItemReference getItems() {
 		return items;
 	}
 
 	public Screen getSplashScreen() {
 		return new SplashScreen(this);
 	}
 
 	public Screen getMainMenuScreen() {
 		return new MainMenu(this);
 	}
 
 	public Screen getNameScreen() {
 		return new Name(this);
 	}
 
 	public Screen getMarketPlaceDemoScreen() {
 		return new MarketPlaceItemDemo(this);
 	}
 
 	public Screen getMarketScreen() {
 		return new Market(this);
 	}
 
 	public Screen getMarketScreen(MarketPlace theMarket) {
		return new MarketOld(this, theMarket);
 	}
 
 	public Screen getSkillPointsScreen() {
 		return new SkillPoints(this);
 	}
 
 	public Screen getStarterPokemonScreen() {
 		return new StarterPokemon(this);
 	}
 
 	public Screen getCurrentTownScreen() {
 		Person potentialEncounter = controller.takeATurn();
 		if (potentialEncounter != null) {
 			return new EncounterScreen(this, potentialEncounter);
 		}
 		return new TownScreen(this, thePlayer.getCurrent().getImage());
 	}
 	
 	public Button getButton(String button){
 		return new Button(	new TextureRegionDrawable(atlas.findRegion(button)), 
 							new TextureRegionDrawable(atlas.findRegion(button + "-down")));
 	}
 
 	public Screen getCurrentTownScreenFromEncounter() {
 		return new TownScreen(this, thePlayer.getCurrent().getImage());
 	}
 
 	public Screen getMapScreen() {
 		return new MapScreen(this);
 	}
 
 	public Player getPlayer() {
 		return thePlayer;
 	}
 
 	public GameMap getMap() {
 		return theMap;
 	}
 
 	public XStream getSerializer() {
 		return xstream;
 	}
 
 	public Screen getBattleScreen(Person myPerson) {
 		return new Battle(this, myPerson);
 	}
 }
