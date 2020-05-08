 package org.kalibro.core.persistence.database;
 
 import static org.junit.Assert.*;
 import static org.kalibro.core.model.ConfigurationFixtures.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.model.Configuration;
 import org.kalibro.core.model.MetricConfiguration;
 import org.kalibro.core.model.MetricConfigurationFixtures;
 
 public abstract class MetricConfigurationDatabaseTest extends DatabaseTestCase {
 
 	private Configuration configuration;
 
 	private ConfigurationDatabaseDao configurationDao;
 	private MetricConfigurationDatabaseDao dao;
 
 	@Before
 	public void setUp() {
 		configuration = simpleConfiguration();
 		configurationDao = daoFactory.getConfigurationDao();
 		dao = daoFactory.getMetricConfigurationDao();
 		configurationDao.save(configuration);
 	}
 
 	@Test(timeout = INTEGRATION_TIMEOUT)
 	public void shouldRetrieveSavedMetricConfiguration() {
 		MetricConfiguration ditConfiguration = MetricConfigurationFixtures.configuration("dit");
 		String configurationName = configuration.getName();
 		dao.save(ditConfiguration, configurationName);
 
 		String metricName = ditConfiguration.getMetric().getName();
 		MetricConfiguration retrieved = dao.getMetricConfiguration(configurationName, metricName);
 		assertNotSame(ditConfiguration, retrieved);
 		assertDeepEquals(ditConfiguration, retrieved);
 	}
 
 	@Test(timeout = INTEGRATION_TIMEOUT)
 	public void shouldRemoveConfigurationByName() {
 		final MetricConfiguration cboConfiguration = MetricConfigurationFixtures.configuration("cbo");
 		final String configurationName = configuration.getName();
 		final String metricName = cboConfiguration.getMetric().getName();
 
 		assertDeepEquals(cboConfiguration, dao.getMetricConfiguration(configurationName, metricName));
 
 		dao.removeMetricConfiguration(configurationName, metricName);
 		String message = "Metric '" + metricName + "' not found in configuration '" + configurationName + "'";
 		checkKalibroException(new Task() {
 
 			@Override
 			public void perform() {
 				dao.getMetricConfiguration(configurationName, metricName);
 			}
 		}, message);
 	}
 }
