 package fr.vinsnet.compteurtarot.dao.raw;
 
 import java.util.Date;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import fr.vinsnet.compteurtarot.dao.RoundDao;
 import fr.vinsnet.compteurtarot.model.Bid;
 import fr.vinsnet.compteurtarot.model.Game;
 import fr.vinsnet.compteurtarot.model.Player;
 import fr.vinsnet.compteurtarot.model.Round;
 import fr.vinsnet.compteurtarot.model.future.bid.FutureBid;
 import fr.vinsnet.compteurtarot.model.future.player.FuturePlayer;
 import fr.vinsnet.utils.ObjectWithId;
 
 public class RoundRawDao extends BaseRawDao implements RoundDao {
 
 	private static final String TAG = "RoundRawDao";
 	public static final String TABLE_NAME = "rounds";
 
 	private static final String KEY_ID = "id";
 	private static final String KEY_GAME_ID = "game_id";
 	private static final String KEY_CREATION_TIME = "creation_time";
 	private static final String KEY_UPDATE_TIME = "update_time";
 	private static final String KEY_BIDDING_TYPE = "bidding_type";
 	private static final String KEY_SCORE_TAKERS = "score";
 	private static final String KEY_NB_BOUTS_TAKERS = "nb_bouts";
 	private static final String KEY_TAKER_ID = "taker_id";
 	private static final String KEY_TAKER_CALLED_ID = "taker_called_id";
 	
 
 	private static final String ROUND_TABLE_CREATE = "CREATE TABLE "
 			+ TABLE_NAME + " (" + 
 			KEY_ID	+ " integer primary key autoincrement, " +
 			KEY_GAME_ID + " integer constraint fk_game references " + GameRawDao.TABLE_NAME +","+
 			KEY_CREATION_TIME + " integer, " +
 			KEY_UPDATE_TIME + " integer, " +
 			KEY_BIDDING_TYPE + " integer, " +
 			KEY_SCORE_TAKERS + " real, " +
 			KEY_NB_BOUTS_TAKERS + " integer, " +
 			KEY_TAKER_ID + " integer constraint fk_game references " + PlayerRawDao.TABLE_NAME +", "+
 			KEY_TAKER_CALLED_ID + " integer constraint fk_game references " + PlayerRawDao.TABLE_NAME + /*", "+
 			
 			*/" );";
 
 
 	public RoundRawDao(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		Log.v(TAG, "onCreate : "+ROUND_TABLE_CREATE);
 		
 		String request = ROUND_TABLE_CREATE + "#";
 		
 		Log.v(TAG, request);
 		
 		db.execSQL(ROUND_TABLE_CREATE);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		Log.v(TAG,"onUpgrade");
 		throw new RuntimeException("not yet implemented");
 
 		
 	}
 
 
 	@Override
 	public boolean updateOrCreate(ObjectWithId o, SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		Log.v(TAG,"mergeOrCreate");
 		return save((Round)o,db)>0;
 		
 	}
 	
 	protected long save(Round r, SQLiteDatabase db) {
 		long key ;
 		if(r.getId()==0){
 			r.setCreationTimestamp(new Date().getTime());//XXX laisser la base faire ca ?
 			key= db.insertOrThrow(TABLE_NAME, KEY_ID, getRoundBaseValues(r));
 			r.setId(key);
 		}else{
 			r.setUpdateTimestamp(new Date().getTime());//XXX laisser la base faire ca
 			/*int nbLineUpdated =*/ db.update(TABLE_NAME, getRoundBaseValues(r),KEY_ID+"=?",new String[]{""+r.getId()});
 
 		}
 		return r.getId();
 	}
 
 	protected ContentValues getRoundBaseValues(Round r) {
 		Bid b =r.getBidding();
 		float scoreTaker = r.getScoreTakers();
 		int nbBoutsTakers = r.getNbBoutsTakers();
 		List<Player> takers = r.getTakers();
 		Player t = takers.size()>=1?takers.get(0):null;
 		Player t2 = takers.size()>=2?takers.get(1):null;
 		ContentValues c = new ContentValues();
 		c.put(KEY_CREATION_TIME, r.getCreationTimestamp());
 		c.put(KEY_UPDATE_TIME, r.getUpdateTimestamp());
 		c.put(KEY_BIDDING_TYPE, b==null?0:b.getType());
 		c.put(KEY_SCORE_TAKERS, scoreTaker);
 		c.put(KEY_NB_BOUTS_TAKERS, nbBoutsTakers);
 		c.put(KEY_TAKER_ID,t==null?0:t.getId() );
 		c.put(KEY_TAKER_CALLED_ID,t2==null?0:t2.getId() );
 		Game g = r.getGame();
 		long gameId = -1;
 		if(g!=null){
 			gameId = g.getId();
 		}else{
 			Log.w(TAG,"saving round without game");
 		}
 		c.put(KEY_GAME_ID, gameId);
 		
 		return c;
 	}
 
 
 	protected Round loadRoundInfos(Round round,Cursor cursor, SQLiteDatabase db) {
 		Log.v(TAG,"loadGameInfos");
 		round.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
 		round.setCreationTimestamp(cursor.getLong(cursor.getColumnIndex(KEY_CREATION_TIME)));
 		round.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(KEY_UPDATE_TIME)));
		round.setbidding(new FutureBid(cursor.getColumnIndex(KEY_BIDDING_TYPE)));
 		round.setNbBoutsTakers(cursor.getInt(cursor.getColumnIndex(KEY_NB_BOUTS_TAKERS)));
 		round.setScoreTakers(cursor.getFloat(cursor.getColumnIndex(KEY_SCORE_TAKERS)));
		round.getTakers().add(new FuturePlayer(cursor.getLong(cursor.getColumnIndex(KEY_TAKER_ID))));
		round.getTakers().add(new FuturePlayer(cursor.getLong(cursor.getColumnIndex(KEY_TAKER_CALLED_ID))));
		//game is set when added to game;
 		Log.d(TAG,"loading round["+round.getId()+"] created at "+new Date(round.getCreationTimestamp()));
 		return round;
 	}
 
 	
 	public void loadRoundsForGame(Game game, SQLiteDatabase db) {
 		Log.v(TAG,"loadRoundForGame"); 
 		Cursor cursor = null;
 		try {
 			cursor=db.query(TABLE_NAME, new String[] { KEY_ID, KEY_CREATION_TIME,KEY_UPDATE_TIME,KEY_BIDDING_TYPE,KEY_NB_BOUTS_TAKERS,KEY_SCORE_TAKERS,KEY_TAKER_ID,KEY_TAKER_CALLED_ID }, KEY_GAME_ID
 					+ "=?", new String[] { "" + game.getId() }, null, null, KEY_CREATION_TIME
 					+ " asc ");
 
 			cursor.moveToFirst();
 
 			while(!cursor.isAfterLast()){
 				Round r = new Round();
 				loadRoundInfos(r, cursor, db);
 				game.addRound(r);
 				cursor.moveToNext();
 			}
 			
 		} catch (Throwable e) {
 			Log.w(TAG, e.getMessage());
 		} finally{
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 
 }
