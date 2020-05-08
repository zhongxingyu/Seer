 package fr.vinsnet.compteurtarot;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.LayoutInflater;
 import fr.vinsnet.compteurtarot.activities.AddRoundActivity;
 import fr.vinsnet.compteurtarot.activities.ChooseContactActivity;
 import fr.vinsnet.compteurtarot.activities.DisplayScoreActivity;
 import fr.vinsnet.compteurtarot.activities.strategies.calcutateresult.MeloResultStrategy;
 import fr.vinsnet.compteurtarot.activities.strategies.calcutateresult.ResultStrategy;
 import fr.vinsnet.compteurtarot.activities.strategies.fillround.FillRoundStrategy;
 import fr.vinsnet.compteurtarot.activities.strategies.fillround.FivePlayersFillRoundStrategy;
 import fr.vinsnet.compteurtarot.activities.strategies.fillround.FourPlayersFillRoundStrategy;
 import fr.vinsnet.compteurtarot.activities.strategies.fillround.ThreePlayersFillRoundStrategy;
 import fr.vinsnet.compteurtarot.adapters.AddButtonMenuItem;
 import fr.vinsnet.compteurtarot.dao.GameDao;
 import fr.vinsnet.compteurtarot.dao.raw.RawDaoFactory;
 import fr.vinsnet.compteurtarot.model.Bid;
 import fr.vinsnet.compteurtarot.model.Game;
 import fr.vinsnet.compteurtarot.model.Player;
 import fr.vinsnet.compteurtarot.model.Round;
 import fr.vinsnet.compteurtarot.parcels.GameParcel;
 import fr.vinsnet.compteurtarot.parcels.RoundParcel;
 import fr.vinsnet.compteurtarot.views.ContactView;
 
 public class Utils {
 
 	public static final String CURRENT_GAME_PARCEL_NAME = "fr.vinsnet.CompteurTarot.currentGame";
 
 	private static final String CURRENT_ROUND_PARCEL_NAME = "fr.vinsnet.CompteurTarot.currentRound";
 	
 	public static final int CONTACT_VIEW_ID = R.layout.view_contact;
 
 	public static final int BIDDING_SPINNER_VIEW_ID = R.layout.view_bidding_spinner;
 
 	public static final int DIALOG_CHOOSE_PLAYER = R.layout.dialog_choose_player;
 
 	public static final int NEW_ROUND_REQUEST_CODE = 1;
 
 	private static final String TAG = "UTILS";
 
 
 	
 	public static void startNewGame(Context c) {
 		Game currentGame = new Game();
 		Intent intent = new Intent(c, ChooseContactActivity.class);
 		intent.putExtra(CURRENT_GAME_PARCEL_NAME, new GameParcel(currentGame));
 		c.startActivity(intent);
 	}
 
 	public static void displayGame(Context c, Game game) {
 		Intent intent = new Intent(c, DisplayScoreActivity.class);
 		intent.putExtra(CURRENT_GAME_PARCEL_NAME, new GameParcel(game));
 		c.startActivity(intent);
 	}
 
 	public static Game getCurrentGame(Activity a) {
 		Game g  = getCurrentGame(a.getIntent());
		g.loadFuturePlayerWithGamePlayer();
 		g.loadFuturBid(getBidList(a));
 		return g;
 	}
 	
 	public static Game getCurrentGame(Intent intent) {
 		return ((GameParcel) intent.getExtras()
 				.getParcelable(CURRENT_GAME_PARCEL_NAME)).getGame();
 	}
 	
 	public static Round getCurrentRound(Activity a, List<Player> players) {
 		Round r =  getCurrentRound(a.getIntent(),players);
 		r.loadWithBids(getBidList(a));
 		return r;
 	}
 	
 	public static Round getCurrentRound(Intent intent, List<Player> players){
 		Round round = ((RoundParcel) intent.getExtras()
 				.getParcelable(CURRENT_ROUND_PARCEL_NAME)).getRound();
 		 round.loadWithPlayers(players);
 		return round;
 	}
 
 	public static void displayNewRound(Activity c, Game game,Round round) {
 		Intent intent = new Intent(c, AddRoundActivity.class);
 		//intent.setFlags(intent.)
 		
 		intent.putExtra(CURRENT_GAME_PARCEL_NAME, new GameParcel(game));
 		intent.putExtra(CURRENT_ROUND_PARCEL_NAME, new RoundParcel(round));
 		c.startActivityForResult(intent, NEW_ROUND_REQUEST_CODE);
 
 	}
 
 
 
 	public static FillRoundStrategy getCurrentScoreStrategy(Activity a,
 			FillRoundStrategy.Actionable actionable) {
 		Game game = getCurrentGame(a);
 		Round round = getCurrentRound(a,game.getPlayers());
 		switch (game.getPlayers().size()) {
 		case 3:
 			return new ThreePlayersFillRoundStrategy(game, round, actionable);
 		case 4:
 			return new FourPlayersFillRoundStrategy(game, round, actionable);
 		case 5:
 			return new FivePlayersFillRoundStrategy(game, round, actionable);
 		case 6:
 			throw new RuntimeException("Not Yet Implemented");
 		default:
 			throw new RuntimeException("erreur interne");
 		}
 
 		// return null;
 	}	
 
 
 	public static ContactView getNewPlayerView(Activity context,Player p) {
 
 		LayoutInflater inflater = (LayoutInflater)context.getSystemService
 			      (Context.LAYOUT_INFLATER_SERVICE);
 		ContactView view = (ContactView)inflater.inflate(Utils.CONTACT_VIEW_ID,null);
 		view.setPlayer(p);
 		return view;
 	}
 
 	public static GameDao getGameDao(Context context) {
 		return RawDaoFactory.getInstance().getGameDao(context);
 	}
 
 	public static ResultStrategy getNewDefaultResultStrategy(Round round,List<Player> players) {
 		//$return new WikipediaResultStrategy(round);
 		return new MeloResultStrategy(round,players);
 	}
 
 	public static List<AddButtonMenuItem> getAddButtonItems(AddRoundActivity a) {
 		List<AddButtonMenuItem> list = new ArrayList<AddButtonMenuItem>();
 		if (a.getCurrentScoreStrategy().getCurrentRound()
 				.getPetitAuBout() == null&&a.getCurrentScoreStrategy().isTeamCompleted()) {
 			list.add(new AddButtonMenuItem("Petit au bout", AddButtonMenuItem.PETIT_AU_BOUT));
 		}
 		list.add(new AddButtonMenuItem("Jeu blanc", AddButtonMenuItem.JEU_BLANC));
 		list.add(new AddButtonMenuItem("Ajouter une poignée", AddButtonMenuItem.POIGNEE));
 		list.add(new AddButtonMenuItem("Pénalité !", AddButtonMenuItem.PENALITE));
 		list.add(new AddButtonMenuItem("Grand chelem", AddButtonMenuItem.CHELEM));
 		
 		return list;
 	}
 
 	public static void updateRoundInIntent(Activity context,Round round) {
 		Intent intent = context.getIntent();
 		if(round!=null){
 			intent.putExtra(CURRENT_ROUND_PARCEL_NAME, new RoundParcel(round));
 		}else{
 			intent.removeExtra(CURRENT_GAME_PARCEL_NAME);
 		}
 		
 	}
 
 	public static List<Bid> getBidList(Context context) {
 		// TODO read Bids config From DB
 		// TODO only the name in configuration files
 		
 		Resources res = context.getResources();
 		String[] bidNames = res.getStringArray(R.array.bidName);
 		int[] bidValues = res.getIntArray(R.array.bidValue);
 		int[] bidMultiply = res.getIntArray(R.array.bidMultiply);
 
 		List<Bid> bids = new ArrayList<Bid>(bidNames.length);
 		for (int i = 0; i < bidNames.length; i++) {
 			Bid b = Bid.instanciateFromBidType(i + 1);
 			b.setName(bidNames[i]);
 			b.setValue(bidValues[i]);
 			b.setMultiply(bidMultiply[i]);
 			bids.add(b);
 		}
 		return bids;
 	}
 
 
 
 	
 }
