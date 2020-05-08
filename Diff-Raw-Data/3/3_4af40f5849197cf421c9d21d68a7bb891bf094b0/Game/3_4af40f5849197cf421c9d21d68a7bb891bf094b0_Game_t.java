 package edu.ycp.cs320.fokemon_webApp.shared.Player;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import edu.ycp.cs320.fokemon_webApp.client.RPC;
 import edu.ycp.cs320.fokemon_webApp.shared.ItemClasses.ItemDatabase;
 import edu.ycp.cs320.fokemon_webApp.shared.ItemClasses.ItemName;
 import edu.ycp.cs320.fokemon_webApp.shared.Login.Login;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.MoveDataBase;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.MoveName;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.PokeID;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.Pokemon;
 
 public class Game {
 	private static Player user;
 	private static Login login;
 	//private Battle battle;
 
 
 	public Game(){
 		Location loc=new Location(0, 0, 0);
 		
 		
 		user = new Player(200, "Cody F.", true, loc);
 		
 		user.getItems().add(ItemDatabase.generateItem(ItemName.SUPER_POTION,5));
 		user.getItems().add(ItemDatabase.generateItem(ItemName.HYPER_POTION,5));
 		user.getItems().add(ItemDatabase.generateItem(ItemName.REVIVE,5));
 		user.getItems().add(ItemDatabase.generateItem(ItemName.MASTER_BALL,5));
 		user.getItems().add(ItemDatabase.generateItem(ItemName.POKE_BALL,5));
 		
 		Pokemon Attacker = Pokemon.GeneratePokemon(PokeID.Charizard, 75);
 		Attacker.getInfo().setNickname("Charizizzle");
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Spore));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Bite));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Flamethrower));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Acid));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Hyper_Beam));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Hydro_Pump));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.SolarBeam));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Dragon_Rage));
 		Attacker.getMoves().add(MoveDataBase.generateMove(MoveName.Waterfall));
 		
 		Pokemon Attacker2 = Pokemon.GeneratePokemon(PokeID.Snorlax, 99);
 		Pokemon Attacker3 = Pokemon.GeneratePokemon(PokeID.Blastoise, 30);
 		user.getTeam().add(Attacker);
 		user.getTeam().add(Attacker2);
 		user.getTeam().add(Attacker3);
 		//setBattle(Battle.wildPokemonBattle(PokeID.Pikachu, 35));
 	}
 	public static void HealTeam(){
 		for(int i=0;i<user.getTeamSize();i++){
 			user.getTeam(i).getStats().fullHeal();
			for(int j=0;j<user.getTeam(i).getMoves().size();j++){
				user.getTeam(i).getMove(j).setCurPP(user.getTeam(i).getMove(j).getMaxPP());
			}
 		}
 	}
 	public Game(Player user){
 		Game.user=user;
 	}
 	
 	public Game(Player user, Login login){
 		Game.user = user;
 		Game.login = login;
 	}
 
 
 	public static Player getUser() {
 		return user;
 	}
 
 
 	public static void setUser(Player user) {
 		Game.user = user;
 	}
 	public static Login getLogin() {
 		return login;
 	}
 	public static void setLogin(Login login) {
 		Game.login = login;
 	}
 
 
 //	public Battle getBattle() {
 //		return battle;
 //	}
 //
 //
 //	public void setBattle(Battle battle) {
 //		this.battle = battle;
 //	}
 	
 	protected void saveProfile() {
 		RPC.loadProfile.saveProfile(login,user, new AsyncCallback<Player>() {
 			@Override
 			public void onSuccess(Player result) {
 				if (result != null) {
 					GWT.log("Save succeeded!");
 					Window.alert("Save Success");
 
 					//Window.alert("Player Name: " + result.getName());
 				} else {
 					GWT.log("Save Fail");
 					Window.alert("Save Fail");
 				}
 
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Save failure", caught);
 
 				Window.alert("Failure");
 
 			}
 		});
 	}
 	
 }
