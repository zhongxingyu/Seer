 package edu.neumont.learningChess.engine.persistence;
 
 import edu.neumont.learningChess.api.ChessGameState;
 import edu.neumont.learningChess.engine.GameStateInfo;
 import edu.neumont.learningChess.engine.SerializedChessGameState;
 
 public class PersistentGameStateCache {
 	private PersistentCache cache;
 	
 	private PersistentGameStateCache(String fileName) {
 		this.cache = PersistentCache.open(fileName);
 	}
 	
 	public static void create(String fileName, long keySize, long valueSize, long recordSize) {
 		try {
 			PersistentCache.create(fileName, keySize, valueSize, recordSize);
 			PersistentCache array = PersistentCache.open(fileName);
 			array.close();
 		} catch (Throwable e) {
 			throw new RuntimeException("The persistent array, " + fileName +", could not be created", e);
 		}
 	}
 	
 	public static void delete(String fileName) {
 		PersistentCache.delete(fileName);
 	}
 	
 	public static PersistentGameStateCache open(String fileName) {
 		PersistentGameStateCache tempCache = new PersistentGameStateCache(fileName);
 		return tempCache;
 	}
 	
 	public void close() {
 		cache.close();
 	}
 	
 	public void put(ChessGameState key, GameStateInfo value) {
 		cache.put(serialize(key), value.serialize());
 	}
 	
 	public GameStateInfo get(ChessGameState key) {
		if(cache.get(serialize(key)) == null)
			return new GameStateInfo(0,0);
 		return new GameStateInfo(cache.get(serialize(key)));
 	}
 	
 	private byte[] serialize(ChessGameState game) {
 		return SerializedChessGameState.serialize(game);
 	}
 }
