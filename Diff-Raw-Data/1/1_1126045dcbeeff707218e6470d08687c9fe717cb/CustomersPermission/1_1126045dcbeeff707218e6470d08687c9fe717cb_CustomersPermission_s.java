 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.sample.app.common.model;
 
 import java.security.Permission;
 import java.util.StringTokenizer;
 
 /**
  * Permission for the Customer class to allow or disallow certain methods
  * 
  */
 public class CustomersPermission extends Permission {
 
 	private static final long serialVersionUID = -606601630674230084L;
 
 	private String actions;
 	private String[] actionList;
 
	@SuppressWarnings("unused")
 	private CustomersPermission() { // for hessian only
 		super(""); //$NON-NLS-1$
 	}
 
 	public CustomersPermission(String name, String actions) {
 		super(name);
 		this.actions = actions;
 		actionList = makeActionList(actions);
 	}
 
 	private String[] makeActionList(String actions) {
 		StringTokenizer st = new StringTokenizer(actions, ",", false); //$NON-NLS-1$
 		String[] list = new String[st.countTokens()];
 		int i = 0;
 		while (st.hasMoreTokens()) {
 			list[i++] = st.nextToken();
 		}
 		return list;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.security.Permission#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof CustomersPermission) {
 			CustomersPermission cp = (CustomersPermission) obj;
 			if (cp.getName().equals(this.getName())) {
 				String[] l2 = makeActionList(cp.getActions());
 				if (actionList.length == l2.length) {
 					for (int i = 0; i < actionList.length; i++) {
 						boolean found = false;
 						for (int x = 0; x < l2.length && !found; x++) {
 							if (actionList[i].equals(l2[x])) {
 								found = true;
 							}
 						}
 						if (!found) {
 							return false;
 						}
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.security.Permission#getActions()
 	 */
 	@Override
 	public String getActions() {
 		return actions;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.security.Permission#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.security.Permission#implies(java.security.Permission)
 	 */
 	@Override
 	public boolean implies(Permission permission) {
 		if (permission instanceof CustomersPermission) {
 			CustomersPermission cp = (CustomersPermission) permission;
 			if (getName().equals(cp.getName())) {
 				String[] l2 = makeActionList(cp.getActions());
 				if (l2.length <= actionList.length) {
 					for (int i = 0; i < l2.length; i++) {
 						boolean found = false;
 						for (int x = 0; x < actionList.length && !found; x++) {
 							if (l2[i].equals(actionList[x])) {
 								found = true;
 							}
 						}
 						if (!found) {
 							return false;
 						}
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 }
