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
 package org.jabox.model;
 
 import java.io.Serializable;
 
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.persistence.domain.BaseEntity;
 import org.jabox.apis.ConnectorConfig;
 import org.jabox.apis.cis.CISConnectorConfig;
 import org.jabox.apis.cqm.CQMConnectorConfig;
 import org.jabox.apis.its.ITSConnectorConfig;
 import org.jabox.apis.rms.RMSConnectorConfig;
 import org.jabox.apis.scm.SCMConnectorConfig;
 
 public class DefaultConfiguration extends BaseEntity implements Serializable {
 	public static final String TRUE = "true";
 
 	public static final String FALSE = "false";
 
 	private static final long serialVersionUID = 970298449487373906L;
 
 	private DeployerConfig _its;
 
 	private DeployerConfig _cis;
 
 	private DeployerConfig _rms;
 
 	private DeployerConfig _scm;
 
 	private DeployerConfig _cqm;
 
 	public String isDefault(final ConnectorConfig item) {
 		if (item == null) {
 			return FALSE;
 		}
 
 		String name = item.getServer().getName();
		if (_its != null && name.equals(_its.getId())) {
 			return TRUE;
 		} else if (_cis != null && name.equals(_cis.getServer().getName())) {
 			return TRUE;
 		} else if (_rms != null && name.equals(_rms.getServer().getName())) {
 			return TRUE;
 		} else if (_scm != null && name.equals(_scm.getServer().getName())) {
 			return TRUE;
 		} else if (_cqm != null && name.equals(_cqm.getServer().getName())) {
 			return TRUE;
 		}
 
 		return FALSE;
 	}
 
 	public String isDefault(final ListItem<ConnectorConfig> listItem) {
 		if (listItem == null) {
 			return FALSE;
 		}
 		return isDefault(listItem.getModelObject());
 	}
 
 	/**
 	 * If given parameter is already default disable, else set it as default.
 	 * 
 	 * @param config
 	 */
 	public void switchDefault(final ConnectorConfig config) {
 		if (ITSConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			if (TRUE.equals(isDefault(config))) {
 				_its = null;
 			} else {
 				_its = (DeployerConfig) config;
 			}
 		} else if (CISConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			if (TRUE.equals(isDefault(config))) {
 				_cis = null;
 			} else {
 				_cis = (DeployerConfig) config;
 			}
 		} else if (RMSConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			if (TRUE.equals(isDefault(config))) {
 				_rms = null;
 			} else {
 				_rms = (DeployerConfig) config;
 			}
 		} else if (SCMConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			if (TRUE.equals(isDefault(config))) {
 				_scm = null;
 			} else {
 				_scm = (DeployerConfig) config;
 			}
 		} else if (CQMConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			if (TRUE.equals(isDefault(config))) {
 				_cqm = null;
 			} else {
 				_cqm = (DeployerConfig) config;
 			}
 		}
 
 	}
 
 	public ITSConnectorConfig getIts() {
 		return (ITSConnectorConfig) _its;
 	}
 
 	public CISConnectorConfig getCis() {
 		return (CISConnectorConfig) _cis;
 	}
 
 	public SCMConnectorConfig getScm() {
 		return (SCMConnectorConfig) _scm;
 	}
 
 	public CQMConnectorConfig getCqm() {
 		return (CQMConnectorConfig) _cqm;
 	}
 
 	public RMSConnectorConfig getRms() {
 		return (RMSConnectorConfig) _rms;
 	}
 
 	/**
 	 * Returns the Default ConnectorConfig for the type of the passed
 	 * ConnectorConfig instance.
 	 * 
 	 * @param config
 	 *            the ConnectorConfig instance to retrieve the type from.
 	 */
 	public ConnectorConfig getDefault(final ConnectorConfig config) {
 		if (config == null) {
 			return null;
 		}
 
 		if (ITSConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			return _its;
 		} else if (CISConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			return _cis;
 		} else if (RMSConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			return _rms;
 		} else if (SCMConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			return _scm;
 		} else if (CQMConnectorConfig.class.isAssignableFrom(config.getClass())) {
 			return _cqm;
 		}
 
 		return null;
 	}
 }
