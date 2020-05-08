 package edu.berkeley.gamesman.database.cache;
 
 import java.io.IOException;
 
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.database.Database;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.Quarto;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.hasher.DartboardHasher;
 import edu.berkeley.gamesman.hasher.QuartoMinorHasher;
 
 public class QuartoCache extends TierCache {
 	private final RecordRangeCache[][] upperCaches = new RecordRangeCache[16][16];
 	private final RecordRangeCache[] lowerCache = new RecordRangeCache[16];
 	private final DartboardHasher majorHasher;
 	private final QuartoMinorHasher minorHasher;
 	private final Quarto game;
 	private final long cacheMemory;
 	private final DatabaseHandle dh;
 
 	public QuartoCache(Quarto game, DartboardHasher majorHasher,
 			QuartoMinorHasher minorHasher, Database db, long availableMemory) {
 		super(db, availableMemory);
 		this.game = game;
 		this.majorHasher = majorHasher;
 		this.minorHasher = minorHasher;
 		for (int place = 0; place < 16; place++) {
 			for (int pieceNum = 0; pieceNum < 16; pieceNum++) {
 				upperCaches[place][pieceNum] = new RecordRangeCache(db);
 			}
 			lowerCache[place] = upperCaches[place][0];
 		}
 		cacheMemory = availableMemory / 256;
 		dh = db.getHandle(true);
 	}
 
 	@Override
 	public void fetchChildren(TierState position, int numChildren,
 			TierState[] children, int[] hints, Record[] values) {
 		for (int child = 0; child < numChildren; child++) {
 			int piece = hints[child] / 16;
 			int place = hints[child] % 16;
 			long childHash = game.stateToHash(children[child]);
 			boolean childFetched = fetchChild(children[child], values[child],
 					piece, place, childHash);
 			if (!childFetched) {
 				setCache(place, piece);
 				childFetched = fetchChild(children[child], values[child],
 						piece, place, childHash);
 				assert childFetched;
 			}
 		}
 	}
 
 	private void setCache(int place, int piece) {
 		boolean success;
 		success = setCacheThroughAll(place, cacheMemory);
 		if (!success)
 			success = setCacheThrough(place, cacheMemory);
 		if (!success)
 			setCacheThrough(place, piece, cacheMemory);
 	}
 
 	private boolean fetchChild(TierState childState, Record toStore, int piece,
 			int place, long childHash) {
 		if (fetchChild(childState, lowerCache[place], childHash, toStore))
 			return true;
 		else if (fetchChild(childState, upperCaches[place][piece], childHash,
 				toStore))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean fetchChild(TierState childState, RecordRangeCache cache,
 			long childHash, Record toStore) {
 		if (cache.containsRecord(childHash)) {
 			game.longToRecord(childState, cache.readRecord(childHash), toStore);
 			return true;
 		} else
 			return false;
 	}
 
 	private boolean setCacheThrough(int place, long availableMemory) {
 		int availableIntMemory = (int) Math.min(availableMemory,
 				Integer.MAX_VALUE);
 		int minorIndex = getMinorInsertionPlace(place);
 		long[] range = minorHasher.getCache(minorIndex,
 				db.recordsForBytes(availableIntMemory));
 		if (range == null)
 			return false;
 		else {
 			setCacheMinorRange(lowerCache[place], place, range[0],
 					(int) range[1], availableIntMemory);
 			return true;
 		}
 	}
 
 	private int getMinorInsertionPlace(int place) {
 		int nextFilled = place;
 		while (nextFilled < 16 && !game.placeList[nextFilled].hasPiece()) {
 			nextFilled++;
 		}
 		if (nextFilled >= 16)
 			return game.getTier();
 		else
 			return game.placeList[nextFilled].getMinorIndex();
 	}
 
 	private void setCacheMinorRange(RecordRangeCache cache, int place,
 			long minorRecordIndex, int numRecords, int ensureBytes) {
 		cache.ensureByteCapacity(ensureBytes, false);
 		long firstRecordIndex = game.hashOffsetForTier(game.getTier() + 1)
 				+ majorHasher.nextChild(' ', 'P', place)
 				* minorHasher.numHashesForTier(game.getTier() + 1)
 				+ minorRecordIndex;
 		cache.setRange(firstRecordIndex, numRecords);
 		try {
 			cache.readRecordsFromDatabase(db, dh, firstRecordIndex, numRecords);
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 
 	private void setCacheMajorRange(RecordRangeCache cache, int place,
 			long majorRecordIndex, int numMajorPlaces, int ensureBytes) {
 		cache.ensureByteCapacity(ensureBytes, false);
 		long nextTierNumHashes = minorHasher
 				.numHashesForTier(game.getTier() + 1);
 		long firstRecordIndex = game.hashOffsetForTier(game.getTier() + 1)
 				+ majorRecordIndex * nextTierNumHashes;
 		int numRecords = (int) (numMajorPlaces * nextTierNumHashes);
 		cache.setRange(firstRecordIndex, numRecords);
 		try {
 			cache.readRecordsFromDatabase(db, dh, firstRecordIndex, numRecords);
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 
 	private boolean setCacheThrough(int place, int piece, long availableMemory) {
 		int availableIntMemory = (int) Math.min(availableMemory,
 				Integer.MAX_VALUE);
 		int minorIndex = getMinorInsertionPlace(place);
 		long[] range = minorHasher.getCache(minorIndex, piece,
 				db.recordsForBytes(availableIntMemory));
 		if (range == null)
 			return false;
 		else {
 			setCacheMinorRange(upperCaches[place][piece], place, range[0],
 					(int) range[1], availableIntMemory);
 			return true;
 		}
 	}
 
 	private boolean setCacheThroughAll(int place, long availableMemory) {
 		int availableIntMemory = (int) Math.min(availableMemory,
 				Integer.MAX_VALUE);
 		long currentMajorChild = majorHasher.nextChild(' ', 'P', place);
 		int availableMajor = (int) (db.recordsForBytes(availableIntMemory) / minorHasher
 				.numHashesForTier());
 		if (availableMajor == 0)
 			return false;
 		else {
 			int addMajor = availableMajor * 2;
 			long lastMajorChild;
 			long majorHash = majorHasher.getHash();
			long remainingMajor = majorHasher.numHashes() - majorHash;
			addMajor = (int) Math.min(addMajor, remainingMajor);
 			do {
 				majorHasher.unhash(majorHash + addMajor - 1);
 				lastMajorChild = majorHasher.previousChild(' ', 'P', place);
 				addMajor /= 2;
 			} while (lastMajorChild - currentMajorChild + 1 > availableMajor);
 			majorHasher.unhash(majorHash);
 			setCacheMajorRange(lowerCache[place], place, currentMajorChild,
 					(int) (lastMajorChild - currentMajorChild + 1),
 					availableIntMemory);
 			return true;
 		}
 	}
 }
