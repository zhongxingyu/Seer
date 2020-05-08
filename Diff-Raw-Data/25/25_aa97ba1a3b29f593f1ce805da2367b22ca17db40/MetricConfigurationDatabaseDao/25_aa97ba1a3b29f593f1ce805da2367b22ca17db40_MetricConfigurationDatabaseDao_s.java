 package org.kalibro.core.persistence.database;
 
 import org.kalibro.KalibroException;
 import org.kalibro.core.model.Configuration;
 import org.kalibro.core.model.MetricConfiguration;
 import org.kalibro.core.persistence.dao.MetricConfigurationDao;
 import org.kalibro.core.persistence.database.entities.ConfigurationRecord;
 import org.kalibro.core.persistence.database.entities.MetricConfigurationRecord;
 
 class MetricConfigurationDatabaseDao extends DatabaseDao<MetricConfiguration, MetricConfigurationRecord> implements
 	MetricConfigurationDao {
 
 	private ConfigurationDatabaseDao configurationDao;
 
 	protected MetricConfigurationDatabaseDao(DatabaseManager databaseManager) {
 		super(databaseManager, MetricConfigurationRecord.class);
 		configurationDao = new ConfigurationDatabaseDao(databaseManager);
 	}
 
 	@Override
 	public void save(MetricConfiguration metricConfiguration, String configurationName) {
 		Configuration configuration = getConfiguration(configurationName);
		configuration.addMetricConfiguration(metricConfiguration);
 		databaseManager.save(newRecord(metricConfiguration, configuration));
 	}
 
 	@Override
 	public MetricConfiguration getMetricConfiguration(String configurationName, String metricName) {
 		Configuration configuration = getConfiguration(configurationName);
 		return findMetricConfiguration(configuration, metricName);
 	}
 
 	@Override
 	public void removeMetricConfiguration(String configurationName, String metricName) {
 		Configuration configuration = getConfiguration(configurationName);
 		MetricConfiguration metricConfiguration = findMetricConfiguration(configuration, metricName);
 		configuration.removeMetric(metricConfiguration.getMetric());
 		databaseManager.delete(newRecord(metricConfiguration, configuration));
 	}
 
 	private MetricConfigurationRecord newRecord(MetricConfiguration metricConfiguration, Configuration configuration) {
 		ConfigurationRecord configurationRecord = new ConfigurationRecord(configuration);
 		return new MetricConfigurationRecord(metricConfiguration, configurationRecord);
 	}
 
 	private Configuration getConfiguration(String configurationName) {
 		return configurationDao.getConfiguration(configurationName);
 	}
 
 	private MetricConfiguration findMetricConfiguration(Configuration configuration, String metricName) {
 		for (MetricConfiguration metricConfiguration : configuration.getMetricConfigurations())
 			if (metricConfiguration.getMetric().getName().equals(metricName))
 				return metricConfiguration;
 		String message = "Metric '" + metricName + "' not found in configuration '" + configuration.getName() + "'";
 		throw new KalibroException(message);
 	}
 }
