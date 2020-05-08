 package pl.edu.agh.two.mud.server;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 
 public class ServiceRegistry implements IServiceRegistry {
 
	public Map<Service, IPlayer> map = new ConcurrentHashMap<Service, IPlayer>();
 
 	@Override
 	public Service getService(IPlayer player) {
 		for (Service service : map.keySet()) {
 			IPlayer p = map.get(service);
 			if (p.equals(player)) {
 				return service;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IPlayer getPlayer(Service service) {
 		return map.get(service);
 	}
 
 	@Override
 	public void bindPlayerToService(Service service, IPlayer player) {
 		map.put(service, player);
 	}
 
 	@Override
 	public Service getCurrentService() {
 		Thread thread = Thread.currentThread();
 		return (thread instanceof Service) ? (Service) thread : null;
 	}
 
 	@Override
 	public void unbindPlayer(IPlayer player) {
 		map.remove(getCurrentService());
 	}
 
 }
