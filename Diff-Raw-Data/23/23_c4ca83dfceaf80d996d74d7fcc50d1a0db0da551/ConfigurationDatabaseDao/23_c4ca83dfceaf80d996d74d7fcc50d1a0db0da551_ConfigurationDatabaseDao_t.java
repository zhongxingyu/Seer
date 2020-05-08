 package org.kalibro.core.persistence;
 
 import java.util.List;
 
 import org.kalibro.Configuration;
 import org.kalibro.core.model.Project;
 import org.kalibro.core.persistence.record.ConfigurationRecord;
 import org.kalibro.dao.ConfigurationDao;
 
 class ConfigurationDatabaseDao extends DatabaseDao<Configuration, ConfigurationRecord> implements ConfigurationDao {
 
 	protected ConfigurationDatabaseDao(RecordManager recordManager) {
 		super(recordManager, ConfigurationRecord.class);
 	}
 
 	@Override
 	public List<Configuration> all() {
 		return allOrderedByName();
 	}
 
 	@Override
 	public void save(Configuration configuration) {
 		ConfigurationRecord record = new ConfigurationRecord(configuration);
		record = save(record);
 		configuration.setId(record.convert().getId());
 	}
 
 	@Override
 	public List<String> getConfigurationNames() {
 		return getAllNames();
 	}
 
 	@Override
 	public boolean hasConfiguration(String configurationName) {
 		return hasEntity(configurationName);
 	}
 
 	@Override
 	public Configuration getConfiguration(String configurationName) {
 		return getByName(configurationName);
 	}
 
 	@Override
 	public Configuration getConfigurationFor(String projectName) {
 		Project project = new ProjectDatabaseDao(recordManager).getProject(projectName);
 		return getByName(project.getConfigurationName());
 	}
 
 	@Override
 	public void delete(Long configurationId) {
 		deleteById(configurationId);
 	}
 }
