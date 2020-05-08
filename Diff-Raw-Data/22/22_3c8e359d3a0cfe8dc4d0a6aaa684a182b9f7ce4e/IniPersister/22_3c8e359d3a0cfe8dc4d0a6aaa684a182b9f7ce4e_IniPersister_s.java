 package lib.config.base.configuration.persist.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import lib.config.base.configuration.Configuration;
 import lib.config.base.configuration.ConfigurationException;
 import lib.config.base.configuration.ConfigurationList;
 import lib.config.base.configuration.impl.BasicConfiguration;
 import lib.config.base.configuration.persist.AbstractPersister;
 
 import org.ini4j.InvalidFileFormatException;
 import org.ini4j.Profile.Section;
 import org.ini4j.Wini;
 
 /**
  * Chose to go with this over apache commons as it is much lighter and does not have dependencies on
  * apaches logging system.
  * 
 * @author BT7662
  *
  * @param <E>
  */
 public class IniPersister<E extends Configuration> implements
 		AbstractPersister<E> {
 
 	@Override
 	public void write(ConfigurationList<E> configuration, File file)
 			throws ConfigurationException {
 		
 		Wini ini = new Wini();
 
 		List<E> configs = configuration.getConfigurations();
 
 		for(E curr : configs) {
 			String id = curr.getId(); // section id
 			
 			Section section = ini.add(id);
 			for(String key : curr.getKeys()) {
 				section.add(key, curr.getProperty(key));
 			}
 		}
 		
 		try {
 			ini.store(file);
 		} catch (IOException e) {
 			throw new ConfigurationException(e);
 		}
 	}
 
 	@Override
 	public ConfigurationList<E> read(File toLoad) throws ConfigurationException {
 
 		try {
 			Wini ini = new Wini(toLoad);
 			Set<Entry<String, Section>> sections = ini.entrySet();
 
 			List<E> configs = new ArrayList<E>();
 
 			for (Entry<String, Section> entry : sections) {
 				
 				String id = entry.getKey();
 
 				E newConfig = (E) new BasicConfiguration();
 				newConfig.setId(id);
 
 				Section section = entry.getValue();
 
 				for (String key : section.keySet()) {
 					String value = section.get(key);
 					newConfig.setProperty(key, value);
 				}
 				
 				configs.add(newConfig);
 			}
 
 			ConfigurationList<E> list = new ConfigurationList<E>();
 			list.setConfigurations(configs);
 
 			return list;
 
 		} catch (InvalidFileFormatException e) {
 			throw new ConfigurationException(e);
 		} catch (IOException e) {
 			throw new ConfigurationException(e);
 		}
 
 	}
 
 }
