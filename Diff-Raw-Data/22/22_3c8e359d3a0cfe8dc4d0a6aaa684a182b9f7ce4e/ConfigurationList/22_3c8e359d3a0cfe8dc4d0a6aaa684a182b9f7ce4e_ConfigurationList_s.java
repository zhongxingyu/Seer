 package lib.config.base.configuration;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.simpleframework.xml.ElementList;
 import org.simpleframework.xml.Root;
 
 /**
  * Holds a list of configurations, so that multiple sets of key/value pairs can be
  * stored.
  * 
 * @author BT7662
  *
  * @param <E>
  */
 @Root
 public class ConfigurationList<E extends Configuration> {
 
 	@ElementList(entry="configurations", inline=true)
 	private List<E> list;
 
 	public ConfigurationList() {
 		
 	}
 	
 	public ConfigurationList(E...list) {
 		setConfigurations(list);
 	}
 	
 	public void setConfigurations(E... list) {
 		this.list = Arrays.asList(list);
 	}
 	
 	public void setConfigurations(List<E> list) {
 		this.list = list;
 	}
 	
 	
 	/**
 	 * Returns an array as the current serialization library produces
 	 * neater XML with arrays than lists.
 	 * 	 * 
 	 * @return
 	 */
 	public List<E> getConfigurations() {
 		return list;
 	}
 }
