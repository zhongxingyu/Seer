 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    cbrand - Bug 377475 - Fix AbstractCustomFeature.execute and canExecute
  *
  * </copyright>
  *
  *******************************************************************************/
 /*
  * Created on 12.12.2005
  */
 package org.eclipse.graphiti.features.custom;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.impl.AbstractFeature;
 import org.eclipse.graphiti.internal.util.T;
 
 /**
  * The Class AbstractCustomFeature.
  */
 public abstract class AbstractCustomFeature extends AbstractFeature implements ICustomFeature {
 
 	/**
 	 * Creates a new {@link AbstractCustomFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public AbstractCustomFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public String getDescription() {
 		return ""; //$NON-NLS-1$
 	}
 
 	public boolean canExecute(ICustomContext context) {
 		return false;
 	}
 
 	@Override
 	public boolean isAvailable(IContext context) {
 		return true;
 	}
 
 	public final boolean canExecute(IContext context) {
 		final String SIGNATURE = "canExecute(IContext)"; //$NON-NLS-1$
 		boolean info = T.racer().info();
 		if (info) {
 			T.racer().entering(AbstractCustomFeature.class, SIGNATURE, new Object[] { context });
 		}
 		boolean ret = false;
 		if (context instanceof ICustomContext) {
 			ret = canExecute((ICustomContext) context);
 		} else {
 			T.racer().error(SIGNATURE, new IllegalArgumentException("ICustomContext expected")); //$NON-NLS-1$
 		}
 		if (info) {
 			T.racer().exiting(AbstractCustomFeature.class, SIGNATURE, ret);
 		}
 		return ret;
 	}
 
	public void execute(IContext context) {
 		final String SIGNATURE = "execute(IContext)"; //$NON-NLS-1$
 		boolean info = T.racer().info();
 		if (info) {
 			T.racer().entering(AbstractCustomFeature.class, SIGNATURE, new Object[] { context });
 		}
 		if (context instanceof ICustomContext) {
 			ICustomContext customContext = (ICustomContext) context;
 			execute(customContext);
 		} else {
 			T.racer().error(SIGNATURE, new IllegalArgumentException("ICustomContext expected")); //$NON-NLS-1$
 		}
 		if (info) {
 			T.racer().exiting(AbstractCustomFeature.class, SIGNATURE);
 		}
 	}
 
 	public String getImageId() {
 		return null;
 	}
 
 }
