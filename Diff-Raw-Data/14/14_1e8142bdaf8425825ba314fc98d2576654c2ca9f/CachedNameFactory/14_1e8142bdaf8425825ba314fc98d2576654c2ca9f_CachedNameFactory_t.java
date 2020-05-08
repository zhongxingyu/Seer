 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 
 public class CachedNameFactory implements NameFactory {
 	
 	private final NameFactory factory;
 	private final Map<InetAddress, String> nameCache = new HashMap<InetAddress, String>();
 	
 	public CachedNameFactory(NameFactory factory) {
 		this.factory = factory;
 	}
 	
 	public CachedNameFactory(NameFactory factory,
 			Map<InetAddress, String> nameCache) {
 		this(factory);
 		
 		this.nameCache.putAll(nameCache);
 	}
 	
 	public CachedNameFactory(NameFactory factory, File nameCacheFile)
 			throws IOException {
 		this(factory);
 		
		InputStream inputStream = new FileInputStream(nameCacheFile);
 		Properties staticNames = new Properties();
		staticNames.load(inputStream);
		inputStream.close();
 		
 		for (Entry<Object, Object> entry : staticNames.entrySet()) {
 			nameCache.put(Inet4Address.getByName((String) entry.getKey()),
 					(String) entry.getValue());
 		}
 	}
 	
 	@Override
 	public String getName(InetAddress address) {
 		String name = nameCache.get(address);
 		
 		if (name == null) {
 			name = factory.getName(address);
 			nameCache.put(address, name);
 		}
 		
 		return name;
 	}
 	
 }
