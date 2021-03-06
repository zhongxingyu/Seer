 /*******************************************************************************
  * Copyright (c) 2008, 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.osgi.internal.permadmin;
 
 import java.io.*;
 import java.net.URL;
 import java.security.*;
 import java.util.*;
 import org.eclipse.osgi.framework.adaptor.PermissionStorage;
 import org.eclipse.osgi.framework.internal.core.*;
 import org.eclipse.osgi.internal.signedcontent.DNChainMatching;
 import org.osgi.framework.AdminPermission;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.service.condpermadmin.*;
 import org.osgi.service.permissionadmin.PermissionAdmin;
 import org.osgi.service.permissionadmin.PermissionInfo;
 
 public final class SecurityAdmin implements PermissionAdmin, ConditionalPermissionAdmin {
 	private static final PermissionCollection DEFAULT_DEFAULT;
 	static {
 		AllPermission allPerm = new AllPermission();
 		DEFAULT_DEFAULT = allPerm.newPermissionCollection();
 		if (DEFAULT_DEFAULT != null)
 			DEFAULT_DEFAULT.add(allPerm);
 	}
 
 	private static final String ADMIN_IMPLIED_ACTIONS = AdminPermission.RESOURCE + ',' + AdminPermission.METADATA + ',' + AdminPermission.CLASS + ',' + AdminPermission.CONTEXT;
 	private static final PermissionInfo[] EMPTY_PERM_INFO = new PermissionInfo[0];
 	/* @GuardedBy(lock) */
 	private final PermissionAdminTable permAdminTable = new PermissionAdminTable();
 	/* @GuardedBy(lock) */
 	private SecurityTable condAdminTable;
 	/* @GuardedBy(lock) */
 	private PermissionInfoCollection permAdminDefaults;
 	/* @GuardedBy(lock) */
 	private long timeStamp = 0;
 	/* @GuardedBy(lock) */
 	private long nextID = System.currentTimeMillis();
 	/* @GuardedBy(lock) */
 	private final PermissionStorage permissionStorage;
 	private final Object lock = new Object();
 	private final Framework framework;
 	private final PermissionInfo[] impliedPermissionInfos;
 	private final EquinoxSecurityManager supportedSecurityManager;
 
 	public SecurityAdmin(EquinoxSecurityManager supportedSecurityManager, Framework framework, PermissionStorage permissionStorage) throws IOException {
 		this.supportedSecurityManager = supportedSecurityManager;
 		this.framework = framework;
 		this.permissionStorage = new SecurePermissionStorage(permissionStorage);
 		this.impliedPermissionInfos = SecurityAdmin.getPermissionInfos(getClass().getResource(Constants.OSGI_BASE_IMPLIED_PERMISSIONS), framework);
 		String[] encodedDefaultInfos = permissionStorage.getPermissionData(null);
 		PermissionInfo[] defaultInfos = getPermissionInfos(encodedDefaultInfos);
 		if (defaultInfos != null)
 			permAdminDefaults = new PermissionInfoCollection(defaultInfos);
 		String[] locations = permissionStorage.getLocations();
 		if (locations != null) {
 			for (int i = 0; i < locations.length; i++) {
 				String[] encodedLocationInfos = permissionStorage.getPermissionData(locations[i]);
 				if (encodedLocationInfos != null) {
 					PermissionInfo[] locationInfos = getPermissionInfos(encodedLocationInfos);
 					permAdminTable.setPermissions(locations[i], locationInfos);
 				}
 			}
 		}
 		String[] encodedCondPermInfos = permissionStorage.getConditionalPermissionInfos();
 		if (encodedCondPermInfos == null)
 			condAdminTable = new SecurityTable(this, new SecurityRow[0]);
 		else {
 			SecurityRow[] rows = new SecurityRow[encodedCondPermInfos.length];
 			try {
 				for (int i = 0; i < rows.length; i++)
 					rows[i] = SecurityRow.createSecurityRow(this, encodedCondPermInfos[i]);
 			} catch (IllegalArgumentException e) {
 				// TODO should log
 				// bad format persisted in storage; start clean
 				rows = new SecurityRow[0];
 			}
 			condAdminTable = new SecurityTable(this, rows);
 		}
 	}
 
 	private static PermissionInfo[] getPermissionInfos(String[] encodedInfos) {
 		if (encodedInfos == null)
 			return null;
 		PermissionInfo[] results = new PermissionInfo[encodedInfos.length];
 		for (int i = 0; i < results.length; i++)
 			results[i] = new PermissionInfo(encodedInfos[i]);
 		return results;
 	}
 
 	boolean checkPermission(Permission permission, BundlePermissions bundlePermissions) {
 		// check permissions by location
 		PermissionInfoCollection locationCollection;
 		SecurityTable curCondAdminTable;
 		PermissionInfoCollection curPermAdminDefaults;
 		// save off the current state of the world while holding the lock
 		synchronized (lock) {
 			// get location the hard way to avoid permission check
 			locationCollection = permAdminTable.getCollection(bundlePermissions.getBundle().getBundleData().getLocation());
 			curCondAdminTable = condAdminTable;
 			curPermAdminDefaults = permAdminDefaults;
 		}
 		if (locationCollection != null)
 			return locationCollection.implies(permission);
 		// if conditional admin table is empty the fall back to defaults
 		if (curCondAdminTable.isEmpty())
 			return curPermAdminDefaults != null ? curPermAdminDefaults.implies(permission) : DEFAULT_DEFAULT.implies(permission);
 		// check the condition table	
 		int result = curCondAdminTable.evaluate(bundlePermissions, permission);
 		if ((result & SecurityTable.GRANTED) != 0)
 			return true;
 		if ((result & SecurityTable.DENIED) != 0)
 			return false;
 		if ((result & SecurityTable.POSTPONED) != 0)
 			return true;
 		return false;
 	}
 
 	public PermissionInfo[] getDefaultPermissions() {
 		synchronized (lock) {
 			if (permAdminDefaults == null)
 				return null;
 			return permAdminDefaults.getPermissionInfos();
 		}
 	}
 
 	public String[] getLocations() {
 		synchronized (lock) {
 			String[] results = permAdminTable.getLocations();
 			return results.length == 0 ? null : results;
 		}
 	}
 
 	public PermissionInfo[] getPermissions(String location) {
 		synchronized (lock) {
 			return permAdminTable.getPermissions(location);
 		}
 	}
 
 	public void setDefaultPermissions(PermissionInfo[] permissions) {
 		checkAllPermission();
 		synchronized (lock) {
 			if (permissions == null)
 				permAdminDefaults = null;
 			else
 				permAdminDefaults = new PermissionInfoCollection(permissions);
 			try {
 				permissionStorage.setPermissionData(null, getEncodedPermissionInfos(permissions));
 			} catch (IOException e) {
 				// log
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void checkAllPermission() {
 		SecurityManager sm = System.getSecurityManager();
 		if (sm != null)
 			sm.checkPermission(new AllPermission());
 	}
 
 	private static String[] getEncodedPermissionInfos(PermissionInfo[] permissions) {
 		if (permissions == null)
 			return null;
 		String[] encoded = new String[permissions.length];
 		for (int i = 0; i < encoded.length; i++)
 			encoded[i] = permissions[i].getEncoded();
 		return encoded;
 	}
 
 	public void setPermissions(String location, PermissionInfo[] permissions) {
 		checkAllPermission();
 		synchronized (lock) {
 			permAdminTable.setPermissions(location, permissions);
 			try {
 				permissionStorage.setPermissionData(location, getEncodedPermissionInfos(permissions));
 			} catch (IOException e) {
 				// TODO log
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void delete(SecurityRow securityRow, boolean firstTry) {
 		ConditionalPermissionUpdate update = newConditionalPermissionUpdate();
 		List rows = update.getConditionalPermissionInfos();
 		for (Iterator iRows = rows.iterator(); iRows.hasNext();) {
 			ConditionalPermissionInfo info = (ConditionalPermissionInfo) iRows.next();
 			if (securityRow.getName().equals(info.getName())) {
 				iRows.remove();
 				synchronized (lock) {
 					if (!update.commit()) {
 						if (firstTry)
 							// try again
 							delete(securityRow, false);
 					}
 				}
 				break;
 			}
 		}
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public ConditionalPermissionInfo addConditionalPermissionInfo(ConditionInfo[] conds, PermissionInfo[] perms) {
 		return setConditionalPermissionInfo(null, conds, perms, true);
 	}
 
 	public ConditionalPermissionInfo newConditionalPermissionInfo(String name, ConditionInfo[] conditions, PermissionInfo[] permissions, String decision) {
 		return new SecurityRowSnapShot(name, conditions, permissions, decision);
 	}
 
 	public ConditionalPermissionInfo newConditionalPermissionInfo(String encoded) {
 		return SecurityRow.createSecurityRowSnapShot(encoded);
 	}
 
 	public ConditionalPermissionUpdate newConditionalPermissionUpdate() {
 		synchronized (lock) {
 			return new SecurityTableUpdate(this, condAdminTable.getRows(), timeStamp);
 		}
 	}
 
 	public AccessControlContext getAccessControlContext(String[] signers) {
 		Enumeration infos = getConditionalPermissionInfos();
 		ArrayList securityRows = new ArrayList();
 		if (infos != null && infos.hasMoreElements()) {
 			// enumerate through all the rows
 			while (infos.hasMoreElements()) {
 				SecurityRow condPermInfo = (SecurityRow) infos.nextElement();
 				ConditionInfo[] condInfo = condPermInfo.internalGetConditionInfos();
 				boolean match = true;
 				// check that each condition is a signer condition
 				for (int i = 0; match && i < condInfo.length; i++) {
 					if (BundleSignerCondition.class.getName().equals(condInfo[i].getType())) {
 						String[] args = condInfo[i].getArgs();
 						String condSigners = args.length > 0 ? args[0] : null;
 						if (condSigners != null) {
 							match = false;
 							boolean negate = (args.length == 2) ? "!".equals(args[1]) : false; //$NON-NLS-1$
 							for (int j = 0; j < signers.length && !match; j++)
 								match = (negate ^ DNChainMatching.match(signers[i], condSigners));
 						} else {
 							match = false;
 						}
 					} else {
 						// contains a non signer condition; cannot match
 						match = false;
 					}
 				}
 				if (match)
 					// found match add the row; keeping the row order
 					securityRows.add(condPermInfo);
 			}
 		} else {
 			PermissionCollection defaultPermissions;
 			synchronized (lock) {
 				defaultPermissions = permAdminDefaults == null ? DEFAULT_DEFAULT : permAdminDefaults;
 			}
 			return new AccessControlContext(new ProtectionDomain[] {new ProtectionDomain(null, defaultPermissions)});
 		}
 		SecurityTable table = new SecurityTable(this, (SecurityRow[]) securityRows.toArray(new SecurityRow[securityRows.size()]));
 		return new AccessControlContext(new ProtectionDomain[] {new ProtectionDomain(null, table)});
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public ConditionalPermissionInfo getConditionalPermissionInfo(String name) {
 		synchronized (lock) {
 			return condAdminTable.getRow(name);
 		}
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public Enumeration getConditionalPermissionInfos() {
 		// could implement our own Enumeration, but we don't care about performance here.  Just do something simple:
 		synchronized (lock) {
 			SecurityRow[] rows = condAdminTable.getRows();
 			Vector vRows = new Vector(rows.length);
 			for (int i = 0; i < rows.length; i++)
 				vRows.add(rows[i]);
 			return vRows.elements();
 		}
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public ConditionalPermissionInfo setConditionalPermissionInfo(String name, ConditionInfo[] conds, PermissionInfo[] perms) {
 		return setConditionalPermissionInfo(name, conds, perms, true);
 	}
 
 	private ConditionalPermissionInfo setConditionalPermissionInfo(String name, ConditionInfo[] conds, PermissionInfo[] perms, boolean firstTry) {
 		ConditionalPermissionUpdate update = newConditionalPermissionUpdate();
 		List rows = update.getConditionalPermissionInfos();
 		ConditionalPermissionInfo infoBase = newConditionalPermissionInfo(name, conds, perms, ConditionalPermissionInfo.ALLOW);
 		int index = -1;
 		if (name == null) {
 			rows.add(infoBase);
 			index = rows.size() - 1;
 		} else {
 			for (int i = 0; i < rows.size() && index < 0; i++) {
 				ConditionalPermissionInfo info = (ConditionalPermissionInfo) rows.get(i);
 				if (name.equals(info.getName())) {
 					rows.set(i, infoBase);
 					index = i;
 				}
 			}
 			if (index < 0) {
 				rows.add(infoBase);
 				index = rows.size() - 1;
 			}
 		}
 		synchronized (lock) {
 			if (!update.commit()) {
 				if (firstTry)
 					// try again
 					setConditionalPermissionInfo(name, conds, perms, false);
 			}
 			return condAdminTable.getRow(index);
 		}
 	}
 
 	boolean commit(List rows, long updateStamp) {
 		checkAllPermission();
 		synchronized (lock) {
 			if (updateStamp != timeStamp)
 				return false;
 			SecurityRow[] newRows = new SecurityRow[rows.size()];
 			Collection names = new ArrayList();
 			for (int i = 0; i < newRows.length; i++) {
 				Object rowObj = rows.get(i);
 				if (!(rowObj instanceof ConditionalPermissionInfo))
 					throw new IllegalStateException("Invalid type \"" + rowObj.getClass().getName() + "\" at row: " + i); //$NON-NLS-1$//$NON-NLS-2$
 				ConditionalPermissionInfo infoBaseRow = (ConditionalPermissionInfo) rowObj;
 				String name = infoBaseRow.getName();
 				if (name == null)
 					name = generateName();
 				if (names.contains(name))
 					throw new IllegalStateException("Duplicate name \"" + name + "\" at row: " + i); //$NON-NLS-1$//$NON-NLS-2$
				newRows[i] = new SecurityRow(this, name, infoBaseRow.getConditionInfos(), infoBaseRow.getPermissionInfos(), infoBaseRow.getAccessDecision());
 			}
 			condAdminTable = new SecurityTable(this, newRows);
 			try {
 				permissionStorage.saveConditionalPermissionInfos(condAdminTable.getEncodedRows());
 			} catch (IOException e) {
 				// TODO log
 				e.printStackTrace();
 			}
 			timeStamp += 1;
 			return true;
 		}
 	}
 
 	/* GuardedBy(lock) */
 	private String generateName() {
 		return "generated_" + Long.toString(nextID++); //$NON-NLS-1$;
 	}
 
 	public EquinoxProtectionDomain createProtectionDomain(AbstractBundle bundle) {
 		PermissionInfoCollection impliedPermissions = getImpliedPermission(bundle);
 		PermissionInfo[] restrictedInfos = getFileRelativeInfos(SecurityAdmin.getPermissionInfos(bundle.getEntry("OSGI-INF/permissions.perm"), framework), bundle); //$NON-NLS-1$
 		PermissionInfoCollection restrictedPermissions = restrictedInfos == null ? null : new PermissionInfoCollection(restrictedInfos);
 		BundlePermissions bundlePermissions = new BundlePermissions(bundle, this, impliedPermissions, restrictedPermissions);
 		return new EquinoxProtectionDomain(bundlePermissions);
 	}
 
 	private PermissionInfoCollection getImpliedPermission(AbstractBundle bundle) {
 		if (impliedPermissionInfos == null)
 			return null;
 		// create the implied AdminPermission actions for this bundle
 		PermissionInfo impliedAdminPermission = new PermissionInfo(AdminPermission.class.getName(), "(id=" + bundle.getBundleId() + ")", ADMIN_IMPLIED_ACTIONS); //$NON-NLS-1$ //$NON-NLS-2$
 		PermissionInfo[] bundleImpliedInfos = new PermissionInfo[impliedPermissionInfos.length + 1];
 		System.arraycopy(impliedPermissionInfos, 0, bundleImpliedInfos, 0, impliedPermissionInfos.length);
 		bundleImpliedInfos[impliedPermissionInfos.length] = impliedAdminPermission;
 		return new PermissionInfoCollection(getFileRelativeInfos(bundleImpliedInfos, bundle));
 	}
 
 	private PermissionInfo[] getFileRelativeInfos(PermissionInfo[] permissionInfos, AbstractBundle bundle) {
 		if (permissionInfos == null)
 			return null;
 		PermissionInfo[] results = new PermissionInfo[permissionInfos.length];
 		for (int i = 0; i < permissionInfos.length; i++) {
 			results[i] = permissionInfos[i];
 			if ("java.io.FilePermission".equals(permissionInfos[i].getType())) { //$NON-NLS-1$
 				File file = new File(permissionInfos[i].getName());
 				if (!file.isAbsolute()) { // relative name
 					File target = bundle.getBundleData().getDataFile(permissionInfos[i].getName());
 					if (target != null)
 						results[i] = new PermissionInfo(permissionInfos[i].getType(), target.getPath(), permissionInfos[i].getActions());
 				}
 			}
 		}
 		return results;
 	}
 
 	public void clearCaches() {
 		PermissionInfoCollection[] permAdminCollections;
 		SecurityRow[] condAdminRows;
 		synchronized (lock) {
 			permAdminCollections = permAdminTable.getCollections();
 			condAdminRows = condAdminTable.getRows();
 		}
 		for (int i = 0; i < permAdminCollections.length; i++)
 			permAdminCollections[i].clearPermissionCache();
 		for (int i = 0; i < condAdminRows.length; i++)
 			condAdminRows[i].clearCaches();
 	}
 
 	EquinoxSecurityManager getSupportedSecurityManager() {
 		return supportedSecurityManager != null ? supportedSecurityManager : getSupportedSystemSecurityManager();
 	}
 
 	static private EquinoxSecurityManager getSupportedSystemSecurityManager() {
 		try {
 			EquinoxSecurityManager equinoxManager = (EquinoxSecurityManager) System.getSecurityManager();
 			return equinoxManager != null && equinoxManager.inCheckPermission() ? equinoxManager : null;
 		} catch (ClassCastException e) {
 			return null;
 		}
 	}
 
 	private static PermissionInfo[] getPermissionInfos(URL resource, Framework framework) {
 		if (resource == null)
 			return null;
 		PermissionInfo[] info = EMPTY_PERM_INFO;
 		DataInputStream in = null;
 		try {
 			in = new DataInputStream(resource.openStream());
 			ArrayList permissions = new ArrayList();
 			BufferedReader reader;
 			try {
 				reader = new BufferedReader(new InputStreamReader(in, "UTF8")); //$NON-NLS-1$
 			} catch (UnsupportedEncodingException e) {
 				reader = new BufferedReader(new InputStreamReader(in));
 			}
 
 			while (true) {
 				String line = reader.readLine();
 				if (line == null) /* EOF */
 					break;
 				line = line.trim();
 				if ((line.length() == 0) || line.startsWith("#") || line.startsWith("//")) /* comments *///$NON-NLS-1$ //$NON-NLS-2$
 					continue;
 
 				try {
 					permissions.add(new PermissionInfo(line));
 				} catch (IllegalArgumentException iae) {
 					/* incorrectly encoded permission */
 					if (framework != null)
 						framework.publishFrameworkEvent(FrameworkEvent.ERROR, framework.getBundle(0), iae);
 				}
 			}
 			int size = permissions.size();
 			if (size > 0)
 				info = (PermissionInfo[]) permissions.toArray(new PermissionInfo[size]);
 		} catch (IOException e) {
 			// do nothing
 		} finally {
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException ee) {
 				// do nothing
 			}
 		}
 		return info;
 	}
 }
