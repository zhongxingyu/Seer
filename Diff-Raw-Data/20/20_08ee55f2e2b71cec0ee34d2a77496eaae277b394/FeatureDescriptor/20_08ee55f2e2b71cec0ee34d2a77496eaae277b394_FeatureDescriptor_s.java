 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.operations;
 
 import org.eclipse.equinox.internal.p2.metadata.TranslationSupport;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 
 /**
  * A description of a feature, providing id, name, etc.
  * 
  * @author David Green
  */
 public class FeatureDescriptor {
 	private final String id;
 
 	private final String simpleId;
 
 	private final String name;
 
 	private final String description;
 
 	private final String provider;
 
 	public FeatureDescriptor(IInstallableUnit iu) {
 		id = iu.getId();
 		simpleId = id.endsWith(AbstractProvisioningOperation.P2_FEATURE_GROUP_SUFFIX) ? id.substring(0, id.length()
 				- AbstractProvisioningOperation.P2_FEATURE_GROUP_SUFFIX.length()) : id;
 		name = getProperty(iu, IInstallableUnit.PROP_NAME);
 		description = getProperty(iu, IInstallableUnit.PROP_DESCRIPTION);
 		provider = getProperty(iu, IInstallableUnit.PROP_PROVIDER);
 	}
 
 	public FeatureDescriptor(String featureId) {
 		id = featureId.endsWith(AbstractProvisioningOperation.P2_FEATURE_GROUP_SUFFIX) ? featureId : featureId
 				+ AbstractProvisioningOperation.P2_FEATURE_GROUP_SUFFIX;
 		simpleId = id.substring(0, id.length() - AbstractProvisioningOperation.P2_FEATURE_GROUP_SUFFIX.length());
 		name = simpleId;
 		description = null;
 		provider = null;
 	}
 
 	private static String getProperty(IInstallableUnit candidate, String key) {
		String value = TranslationSupport.getInstance().getIUProperty(candidate, key);
 		return (value != null) ? value : ""; //$NON-NLS-1$
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getSimpleId() {
 		return simpleId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getProvider() {
 		return provider;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		FeatureDescriptor other = (FeatureDescriptor) obj;
 		if (id == null) {
 			if (other.id != null) {
 				return false;
 			}
 		} else if (!id.equals(other.id)) {
 			return false;
 		}
 		return true;
 	}
 }
