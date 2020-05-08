 package net.axstudio.axparty.guessword;
 
 import java.util.Vector;
 
 import android.content.Context;
 import net.axstudio.axparty.guessword.Rule.PlayerType;
 
 public class Game
 {
 
 	class Player
 	{
 		String name;
 		String word;
 		PlayerType type;
 
 	}
 
 	Game(Context context)
 	{
 		mContext = context;
 	}
 
 	Context mContext;
 	Rule mRule;
 	WordLibEntry mWords;
 
 	Vector<Player> mPlayers = new Vector<Game.Player>();
 
 	// Vector<Player> mMajors = new Vector<Game.Player>();
 	// Vector<Player> mMinors = new Vector<Game.Player>();
 	// Vector<Player> mIdiots = new Vector<Game.Player>();
 
 	public void init(Rule rule, WordLibEntry words)
 	{
 		mRule = new Rule(rule.getData());
 		mWords = words;
 
 		for (int i = 0; i < mRule.getTotalPlayers(); ++i)
 		{
 			Player player = new Player();
 			player.name = String.format(
					mContext.getString(R.string.player_name), i+1);
 			mPlayers.add(player);
 
 		}
 
 	}
 
 	public void start()
 	{
 		String[] w = mWords.genWord();
 
 		Vector<Player> players = new Vector<Game.Player>(mPlayers);
 		for (int i = 0; i < players.size(); ++i)
 		{
 			int j = (int) Math.floor(Math.random() * players.size());
 			Player tmp = players.get(i);
 			players.set(i, players.get(j));
 			players.set(j, tmp);
 		}
 
 		// mMajors.clear();
 		for (PlayerType type : PlayerType.values())
 		{
 			for (int i = 0; i < mRule.getNumPlayersByType(type); ++i)
 			{
 				Player p = players.firstElement();
 				players.remove(0);
 				p.type = type;
 				p.word = w[type.ordinal()];
 			}
 		}
 	}
 
 	public Player[] getPlayers()
 	{
 		Player[] r = new Player[mPlayers.size()];
 		mPlayers.toArray(r);
 		return r;
 
 	}
 
 }
