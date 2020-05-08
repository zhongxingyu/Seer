 package carnero.netmap.model;
 
 import carnero.netmap.common.Util;
 
 import java.util.HashMap;
 
 public class BtsCache {
 
 	private static final HashMap<String, Bts> mCache = new HashMap<String, Bts>();
 
 	public static void add(Bts bts) {
 		synchronized (mCache) {
 			mCache.put(Bts.getId(bts), bts);
 		}
 	}
 
 	public static Bts get(int lac, int cid, int type) {
 		if (lac < 0 || cid < 0) {
 			return null;
 		}
 
 		final String id = Bts.getId(lac, cid);
 
 		if (mCache.containsKey(id)) {
 			final Bts bts = mCache.get(id);
 
 			int lvlOld = Util.getNetworkLevel(bts.type);
 			int lvlNew = Util.getNetworkLevel(type);
 			if (lvlNew > lvlOld) {
 				bts.type = type;
 			}
 
 			return bts;
 		} else {
 			final Bts bts = new Bts(lac, cid, type);
 			add(bts);
 
			int lvlOld = Util.getNetworkLevel(bts.type);
			int lvlNew = Util.getNetworkLevel(type);
			if (lvlNew > lvlOld) {
				bts.type = type;
			}

 			return bts;
 		}
 	}
 }
