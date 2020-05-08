 package org.kalibro.service.entities;
 
 import static org.junit.Assert.*;
 import static org.kalibro.core.model.ConfigurationFixtures.*;
 import static org.kalibro.core.model.MetricConfigurationFixtures.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Test;
 import org.kalibro.DtoTestCase;
 import org.kalibro.core.model.CompoundMetricFixtures;
 import org.kalibro.core.model.MetricConfiguration;
 import org.kalibro.core.model.enums.Statistic;
 
 public class MetricConfigurationXmlTest extends DtoTestCase<MetricConfiguration, MetricConfigurationXml> {
 
 	@Override
 	protected MetricConfigurationXml newDtoUsingDefaultConstructor() {
 		return new MetricConfigurationXml();
 	}
 
 	@Override
 	protected Collection<MetricConfiguration> entitiesForTestingConversion() {
 		Collection<MetricConfiguration> configurations = simpleConfiguration().getMetricConfigurations();
 		ArrayList<MetricConfiguration> entities = new ArrayList<MetricConfiguration>(configurations);
 		entities.add(new MetricConfiguration(CompoundMetricFixtures.sc()));
 		return entities;
 	}
 
 	@Override
 	protected MetricConfigurationXml createDto(MetricConfiguration metricConfiguration) {
 		return new MetricConfigurationXml(metricConfiguration);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldTurnNullUnrequiredFieldsToDefault() {
 		MetricConfiguration configuration = configuration("dit");
		configuration.setWeight(null);
		configuration.setAggregationForm(null);
 		MetricConfiguration converted = createDto(configuration).convert();
 		assertDoubleEquals(1.0, converted.getWeight());
 		assertEquals(Statistic.AVERAGE, converted.getAggregationForm());
 	}
 }
