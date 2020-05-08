 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package nl.frensjan.osgi.autoconf;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.log.LogService;
 
 import aQute.bnd.annotation.component.Activate;
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.Deactivate;
 import aQute.bnd.annotation.component.Reference;
 import aQute.bnd.annotation.metatype.Configurable;
 
 @Component(immediate = true, designateFactory = Config.class)
 public class AutoConfigurator implements ServiceListener {
 	// logger (defaults to a system.out directed custom logger)
 	private LogService logger = new PrintStreamLogger(System.out);
 
 	// the service for creating, updating and deleting the configurations
 	private ConfigurationAdmin configAdmin;
 
 	// the context for e.g. service lookup
 	private BundleContext context;
 
 	// configuration for auto configuration
 	private Config config;
 
 	// the managed configurations
 	private Configuration singletonConfig = null;
 	private final Map<ServiceReference, Configuration> managedConfigs = new HashMap<>();
 
 	@Activate
 	public synchronized void activate(BundleContext context, Map<String, Object> props)
 			throws InvalidSyntaxException {
 		this.context = context;
 
 		// parse configuration
 		this.config = Configurable.createConfigurable(Config.class, props);
 
 		// listen for changes in matching services
 		context.addServiceListener(this, this.config.filter());
 
 		// lookup services matching to new filter
 		Set<ServiceReference> matchingServices = this.matchingServices();
 
 		try {
 			switch (this.config.multiplicity()) {
 			case SINGLETON: {
 				// create the singleton configuration
 				this.updateSingletonConfiguration(matchingServices);
 				break;
 			}
 			case ONE_FOR_EACH: {
 				// create configurations for all matched services
 				for (ServiceReference ref : matchingServices) {
 					this.createManagedConfiguration(ref);
 				}
 				break;
 			}
 			}
 		} catch (Exception e) {
 			this.logger.log(LogService.LOG_ERROR, "Couldn't create configuration", e);
 		}
 	}
 
 	@Deactivate
 	public synchronized void deactivate(BundleContext context) {
 		context.removeServiceListener(this);
 
 		for (Configuration managedConfiguration : this.managedConfigs.values()) {
 			this.deleteConfiguration(managedConfiguration);
 		}
 
 		this.managedConfigs.clear();
 
 		this.deleteSingletonConfiguration();
 	}
 
 	@Reference(optional = true)
 	public void setLogger(LogService logger) {
 		this.logger = logger;
 	}
 
 	@Reference
 	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
 		this.configAdmin = configAdmin;
 	}
 
 	@Override
 	public synchronized void serviceChanged(ServiceEvent event) {
 		ServiceReference ref = event.getServiceReference();
 
 		switch (this.config.multiplicity()) {
 		case SINGLETON: {
 			try {
 				this.updateSingletonConfiguration(this.matchingServices());
 			} catch (Exception e) {
 				this.logger.log(LogService.LOG_ERROR, "Unable to process service changed event", e);
 			}
 			break;
 		}
 		case ONE_FOR_EACH: {
 			try {
 				switch (event.getType()) {
 				case ServiceEvent.REGISTERED:
 					this.createManagedConfiguration(ref);
 					break;
 				case ServiceEvent.MODIFIED:
 					this.updateManagedConfiguration(ref);
 					break;
 				case ServiceEvent.UNREGISTERING:
 					this.deleteManagedConfiguration(ref);
 					break;
 				}
 			} catch (Exception e) {
 				this.logger.log(LogService.LOG_ERROR, "Unable to process service changed event", e);
 			}
 			break;
 		}
 		}
 	}
 
 	private void updateSingletonConfiguration(Set<ServiceReference> matchingServices)
 			throws IOException, ParseException {
 		try {
 			Properties props = this.createProperties(this.config.configuration(),
 					new AggregatePropertyProvider(this.matchingServices()));
 
 			if (this.singletonConfig == null) {
 				this.singletonConfig = this.createConfiguration(props);
 			} else {
 				this.singletonConfig.update(props);
 			}
 		} catch (InvalidSyntaxException e) {
 			this.logger.log(LogService.LOG_ERROR, "Invalid filter syntax", e);
 		}
 	}
 
 	private void deleteSingletonConfiguration() {
 		if (this.singletonConfig != null) {
 			this.deleteConfiguration(this.singletonConfig);
 		}
 	}
 
 	private Configuration createManagedConfiguration(ServiceReference ref) throws IOException,
 			ParseException {
 		try {
 			Properties props = this.createProperties(this.config.configuration(),
 					new BasicPropertyProvider(ref));
 
 			Configuration managedConfig = this.createConfiguration(props);
 			this.managedConfigs.put(ref, managedConfig);
 
 			return managedConfig;
 		} catch (ParseException e) {
 			this.logger.log(LogService.LOG_ERROR, "Couldn't parse the config spec", e);
 			throw e;
 		}
 	}
 
 	private void updateManagedConfiguration(ServiceReference ref) throws IOException,
 			ParseException {
 		Configuration managedConfiguration = this.managedConfigs.get(ref);
 
 		try {
 			Properties newProps = this.createProperties(this.config.configuration(),
 					new BasicPropertyProvider(ref));
 			managedConfiguration.update(newProps);
 		} catch (ParseException e) {
 			this.logger.log(LogService.LOG_ERROR, "Couldn't parse the configuration", e);
 			throw e;
 		} catch (Exception e) {
 			this.logger.log(LogService.LOG_ERROR, "Couldn't update the configuration", e);
 			throw e;
 		}
 	}
 
 	private void deleteManagedConfiguration(ServiceReference ref) {
 		Configuration managedConfiguration = this.managedConfigs.remove(ref);
 
 		if (managedConfiguration != null) {
 			this.deleteConfiguration(managedConfiguration);
 		}
 	}
 
 	private Configuration createConfiguration(Properties props) throws IOException {
 		Configuration managedConfiguration = null;
 
 		String pid = this.config.targetPid();
 		String location = this.config.targetLocation();
 		if (location != null && location.length() == 0) {
 			location = null;
 		}
 
 		if (this.config.factory()) {
 			managedConfiguration = this.configAdmin.createFactoryConfiguration(pid, location);
 		} else {
 			managedConfiguration = this.configAdmin.getConfiguration(pid, location);
 		}
 
 		managedConfiguration.update(props);
 
 		this.logger.log(LogService.LOG_DEBUG, "Created configuration");
 		return managedConfiguration;
 	}
 
 	private void deleteConfiguration(Configuration configuration) {
 		try {
 			configuration.delete();
 		} catch (IllegalStateException | IOException e) {
 			this.logger.log(LogService.LOG_INFO, "unable to delete managed configuration");
 		}
 	}
 
 	/**
 	 * Create properties from the given property lines. Any references (in the
 	 * format of {name}) are resolved as properties of the given service
 	 * reference.
 	 * 
 	 * @param propertyLines
 	 *            The keys and values as array of strings in the format of
 	 *            key=value.
 	 * @param serviceReference
 	 *            The service reference used to resolve references in the
 	 *            properties
 	 * @throws ParseException
 	 *             Thrown if the property lines aren't correctly formatted.
 	 */
 	private Properties createProperties(String[] propertyLines, PropertyProvider valueProvider)
 			throws ParseException {
 		Properties props = new Properties();
 		for (String prop : propertyLines) {
 			String[] keyValue = prop.split("=", 2);
 			if (keyValue.length != 2) {
 				throw new ParseException(
 						String.format("property %s is not in the format key=value", prop), 0);
 			}
 
 			String key = keyValue[0];
 			String value = keyValue[1];
 
 			int openIdx = value.indexOf('{');
 			int closeIdx = value.indexOf('}');
 
 			// if the value does not contain a reference, or there is no service
 			// registration which can provide the values, use it 'as is'
 			if (openIdx == -1 || closeIdx == -1 || valueProvider == null) {
 				props.setProperty(key, value);
 			}
 
 			// if value is only a reference, get the value from the
 			// serviceReference (which is the context for the reference) as an
 			// object (instead of copying it as a string)
 			else if (openIdx == 0 && closeIdx == value.length() - 1) {
 				String ref = value.substring(1, value.length() - 1);
 				props.put(key, valueProvider.getProperty(ref));
 			}
 
 			// otherwise the value contains multiple references, build a
 			// string from that by replacing all references with the value from
 			// the serviceReference (which is the context for the property
 			// reference)
 			else {
 				Matcher matcher = Pattern.compile("\\{(.*)\\}").matcher(value);
 				StringBuilder valueBuilder = new StringBuilder();
 
 				// find all references and resolve them with values from the
 				// serviceReference (which is the context for the references)
 				int pos = 0;
 				for (; pos < value.length() - 1 && matcher.find(pos); pos = matcher.end() + 1) {
 					// append the text before the reference
 					String text = value.substring(pos, matcher.start());
 					valueBuilder.append(text);
 
 					// get the reference value and append it
 					String ref = matcher.group(1);
 					Object v = valueProvider.getProperty(ref);
 					valueBuilder.append(v);
 				}
 
 				// append the tail
				if (pos - 1 < value.length()) {
 					valueBuilder.append(value.substring(pos - 1));
 				}
 
 				// build the value and set the property
 				props.setProperty(key, valueBuilder.toString());
 			}
 		}
 
 		return props;
 	}
 
 	private Set<ServiceReference> matchingServices() throws InvalidSyntaxException {
 		return this.getServices(this.context, this.config.filter());
 	}
 
 	private Set<ServiceReference> getServices(BundleContext context, String filter)
 			throws InvalidSyntaxException {
 		ServiceReference[] refs = context.getAllServiceReferences(null, filter);
 
 		if (refs != null) {
 			Set<ServiceReference> refsSet = new HashSet<>();
 			refsSet.addAll(Arrays.asList(refs));
 			return refsSet;
 		} else {
 			return new HashSet<>(0);
 		}
 	}
 }
