 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.internal.deltacloud.ui.wizards;
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClient;
 
 public class CloudTypeValidator implements IValidator {
 
 	@Override
 	public IStatus validate(Object value) {
 		if (value != null
 				&& !DeltaCloudClient.DeltaCloudType.UNKNOWN.equals(value)) {
 			return ValidationStatus.ok();
 		} else {
			return ValidationStatus.error(WizardMessages.getString("IllegalCloudUrl.msg")); //$NON-NLS-1$
 		}
 	}
 }
