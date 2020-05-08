 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.apis;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.jabox.apis.cis.CISConnector;
 import org.jabox.apis.cis.CISConnectorConfig;
 import org.jabox.apis.cqm.CQMConnector;
 import org.jabox.apis.cqm.CQMConnectorConfig;
 import org.jabox.apis.its.ITSConnector;
 import org.jabox.apis.its.ITSConnectorConfig;
 import org.jabox.apis.rms.RMSConnector;
 import org.jabox.apis.rms.RMSConnectorConfig;
 import org.jabox.apis.scm.SCMConnector;
 import org.jabox.apis.scm.SCMConnectorConfig;
 
 import com.google.inject.Inject;
 
 public class Manager implements IManager {
 
 	private final Set<ITSConnector> _itsConnectors;
 	private final Set<CISConnector> _cisConnectors;
 	private final Set<SCMConnector> _scmConnectors;
 	private final Set<RMSConnector> _rmsConnectors;
 	private final Set<CQMConnector> _cqmConnectors;
 
 	@Inject
 	public Manager(Set<ITSConnector> its, Set<CISConnector> cis,
 			Set<SCMConnector> scm, Set<RMSConnector> rms, Set<CQMConnector> cqm) {
 		this._itsConnectors = its;
 		this._cisConnectors = cis;
 		this._scmConnectors = scm;
 		this._rmsConnectors = rms;
 		this._cqmConnectors = cqm;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.jabox.apis.IManager#getConnectorInstance(org.jabox.apis.ConnectorConfig
 	 * )
 	 */
 	public Connector getConnectorInstance(final ConnectorConfig connectorConfig) {
 		return getEntry(connectorConfig.getPluginId());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.jabox.apis.IManager#getScmConnectorInstance(org.jabox.apis.scm.
 	 * SCMConnectorConfig)
 	 */
 	public SCMConnector<SCMConnectorConfig> getScmConnectorInstance(
 			SCMConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		for (SCMConnector connectorInstance : _scmConnectors) {
			if (config.getPluginId().equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.jabox.apis.IManager#getItsConnectorInstance(org.jabox.apis.its.
 	 * ITSConnectorConfig)
 	 */
 	public ITSConnector<ITSConnectorConfig> getItsConnectorInstance(
 			ITSConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		for (ITSConnector connectorInstance : _itsConnectors) {
			if (config.getPluginId().equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.jabox.apis.IManager#getRmsConnectorInstance(org.jabox.apis.rms.
 	 * RMSConnectorConfig)
 	 */
 	public RMSConnector getRmsConnectorInstance(RMSConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		for (RMSConnector connectorInstance : _rmsConnectors) {
			if (config.getPluginId().equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.jabox.apis.IManager#getCisConnectorInstance(org.jabox.apis.cis.
 	 * CISConnectorConfig)
 	 */
 	public CISConnector getCisConnectorInstance(CISConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		for (CISConnector connectorInstance : _cisConnectors) {
			if (config.getPluginId().equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.jabox.apis.IManager#getCqmConnectorInstance(org.jabox.apis.cqm.
 	 * CQMConnectorConfig)
 	 */
 	public CQMConnector getCqmConnectorInstance(CQMConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		for (CQMConnector connectorInstance : _cqmConnectors) {
			if (config.getPluginId().equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Connector getEntry(String pluginId) {
 		List<Connector> connectors = getAllConnectors();
 
 		if (pluginId == null) {
 			return null;
 		}
 
 		for (Connector connectorInstance : connectors) {
 			if (pluginId.equals(connectorInstance.getId())) {
 				return connectorInstance;
 			}
 		}
 		return null;
 	}
 
 	private List<Connector> getAllConnectors() {
 		List<Connector> connectors = new ArrayList<Connector>();
 		connectors.addAll(_itsConnectors);
 		connectors.addAll(_scmConnectors);
 		connectors.addAll(_rmsConnectors);
 		connectors.addAll(_cisConnectors);
 		connectors.addAll(_cqmConnectors);
 		return connectors;
 	}
 
 	@Override
 	public List<? extends String> getIds(
 			Class<? extends Connector> connectorClass) {
 		List<Connector> connectors = getAllConnectors();
 		List<String> ids = new ArrayList<String>();
 		for (Connector connector : connectors) {
 			if (connectorClass.isAssignableFrom(connector.getClass())) {
 				ids.add(connector.getId());
 			}
 		}
 		return ids;
 	}
 
 }
