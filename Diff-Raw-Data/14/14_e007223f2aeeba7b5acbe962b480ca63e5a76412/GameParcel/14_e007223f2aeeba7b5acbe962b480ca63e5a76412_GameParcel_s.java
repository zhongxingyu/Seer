 package fr.vinsnet.compteurtarot.parcels;
 
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import fr.vinsnet.compteurtarot.model.Game;
 import fr.vinsnet.compteurtarot.model.Player;
 import fr.vinsnet.compteurtarot.model.Round;
 
 public class GameParcel implements Parcelable {
 
 private Game game;
 
 public GameParcel(Game game) {
 	this.game=game;
 }
 	
 public int describeContents() {
 	return 0;
 }
 
 public void writeToParcel(Parcel dest, int flags) {
 	writeStartTime(dest);
 	writePlayerList(dest);
 	writeRoundList(dest);
 }
 
 private void writeRoundList(Parcel dest) {
 	List<Round> rounds = game.getRounds();
 	dest.writeInt(rounds.size());
 	for(Round r : rounds){
 		new RoundParcel(r).writeToParcel(dest,0);
 	}
 }
 
 private void writePlayerList(Parcel dest) {
 	List<Player> players = game.getPlayers();
 	dest.writeInt(players.size());
 	for(Player p : players){
 		new PlayerParcel(p).writeToParcel(dest,0);
 	}
 	
 }
 
 private void writeStartTime(Parcel dest) {
 	dest.writeLong(game.getStartTime().getTime());
	
 }
 
 protected static GameParcel gameFromParcel(Parcel in){
 		Game game = new Game();
 		GameParcel parcel = new GameParcel(game);
 		parcel.readStartTime(in);
 		parcel.readPlayerList(in);
 		parcel.readRoundList(in);
 		return parcel;
 	}
 
 
 
 private  void readRoundList( Parcel in) {
 	int size = in.readInt();
 	for(int i=0;i<size;i++){
 		game.addRound(RoundParcel.roundFromParcel(in).getRound());
 	}
 
 	
 }
 
 private  void readPlayerList( Parcel in) {
 	int size = in.readInt();
 	for(int i=0;i<size;i++){
 		game.addPlayer(PlayerParcel.playerFromParcel(in).getPlayer());
 	}
 	
 }
 
 private  void readStartTime( Parcel in) {
 	game.setStartTime(in.readLong());
 	
 }
 
 
 
 public static final Parcelable.Creator<GameParcel> CREATOR = new Parcelable.Creator<GameParcel>()
 {
    // @Override
     public GameParcel createFromParcel(Parcel source)
     {
         return gameFromParcel(source);
     }
 
     //@Override
     public GameParcel[] newArray(int size)
     {
     	
     	return new GameParcel[size];
     }
 };
 
 public Game getGame() {
 	return game;
 }
 
 }
