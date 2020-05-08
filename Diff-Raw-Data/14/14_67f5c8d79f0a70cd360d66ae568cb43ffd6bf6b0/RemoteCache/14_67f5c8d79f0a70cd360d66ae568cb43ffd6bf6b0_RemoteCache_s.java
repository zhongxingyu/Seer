 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 
 public class RemoteCache implements ICache
 {
 	CacheServer cache;
 	HashBalancer hb;
 	
	public RemoteCache(String[] serverNames, int localId, ICache localCache)
 	{
 		try {
 			cache = new CacheServer(localCache);
			Naming.rebind(serverNames[localId], cache);
 			hb = new HashBalancer(serverNames);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void clear()
 	{
 		for (String server : hb.getServers())
 		{
 			try {
 				((ICache)Naming.lookup(server)).clear();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public String get(String key)
 	{
 		return getCacheServer(key).get(key);
 	}
 	
 	public void set(String key, String val)
 	{
 		getCacheServer(key).set(key, val);
 	}
 	
 	private ICache getCacheServer(String key)
 	{
 		try {
 			return (ICache)Naming.lookup(hb.getServerForKey(key));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 }
