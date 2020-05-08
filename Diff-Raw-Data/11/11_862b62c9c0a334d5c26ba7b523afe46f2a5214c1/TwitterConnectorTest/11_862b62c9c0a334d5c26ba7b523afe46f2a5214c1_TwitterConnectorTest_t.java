 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.connectors.twitter.test;
 
 import static org.junit.Assert.fail;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bonitasoft.connectors.twitter.TwitterConnector;
 import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorValidationException;
 import org.bonitasoft.engine.exception.BonitaException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public abstract class TwitterConnectorTest {
 
 	protected static final Logger LOG = Logger
 			.getLogger(TwitterConnectorTest.class.getName());
 
 	@Before
 	public void initialize() throws Exception {
 		if (TwitterConnectorTest.LOG.isLoggable(Level.WARNING)) {
 			TwitterConnectorTest.LOG.warning("======== Starting test: "
 					+ this.getClass().getName() + "() ==========");
 		}
 	}
 
 	@After
 	public void addLogEnding() throws Exception {
 		if (TwitterConnectorTest.LOG.isLoggable(Level.WARNING)) {
 			TwitterConnectorTest.LOG.warning("======== Ending test: "
 					+ this.getClass().getName() + " ==========");
 		}
 	}
 
 	protected abstract Class<? extends Connector> getConnectorClass();
 
 	//
 	// @Test(expected = ConnectorValidationException.class)
 	// public void validateConnector() throws BonitaException {
 	// Connector.validate();
 	// Assert.assertTrue(Connector.validateConnector(getConnectorClass())
 	// .isEmpty())
 	// assertTrue();
 	// }
 
 	@Test(expected = ConnectorValidationException.class)
 	public void setWrappedSmtpPortWithLessThanRange() throws BonitaException {
 		final Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("proxyHost", "");
 		parameters.put("proxyPort", -1);
 		final TwitterConnector connector = getTwitterConnector(parameters);
 		connector.validateInputParameters();
 	}
 
 	@Test(expected = ConnectorValidationException.class)
 	public void setWrappedSmtpPortWithGreaterThanRange() throws BonitaException {
 		final Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("proxyHost", "");
 		parameters.put("proxyPort", 65536);
 		final TwitterConnector connector = getTwitterConnector(parameters);
 		connector.validateInputParameters();
 	}
 
 	@Test(expected = ConnectorValidationException.class)
 	public void setSmtpPortWithLessThanRange() throws BonitaException {
 		final Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("proxyPort", -1);
 		parameters.put("proxyHost", "");
 		final TwitterConnector connector = getTwitterConnector(parameters);
 		connector.validateInputParameters();
 	}
 
 	@Test(expected = ConnectorValidationException.class)
 	public void setSmtpPortWithGreaterThanRange() throws BonitaException {
 		final Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("proxyPort", 65536);
 		parameters.put("proxyHost", "");
 		final TwitterConnector connector = getTwitterConnector(parameters);
 		connector.validateInputParameters();
 	}
 
 	@Ignore
 	@Test
 	public void execute() {
 		final Map<String, Object> emptyMap = Collections.emptyMap();
 		final TwitterConnector connector = getTwitterConnector(emptyMap);
 		try {
 			connector.execute();
 		} catch (final Exception e) {
 			e.printStackTrace();
 			fail("Impossible! A Tweet must be sent");
 		}
 	}
 
 	public Map<String, Object> getTwitterConnectorParameters() {
 		final Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("consumerKey", "zerzer");
 		parameters.put("consumerSecret", "mljpoi");
 		parameters.put("accesToken",
 				"7MyWkyU2RuQxeNGTqn8AItTWuAIb9juhN3mhgBmUKn8");
 		parameters.put("accessTokenSecret",
 				"zTf1uz5RZR9XmuAdPnMwk4PfnLRoI1o4gyNU7wSlQ");
 		parameters.put("status", "poafjaofj");
 		parameters.put("proxyHost", "myproxy.bonitasoft.com");
 		parameters.put("proxyPort", 8080);
 
 		return parameters;
 	}
 
 	public abstract TwitterConnector getTwitterConnector(
 			Map<String, Object> parameters);
 }
