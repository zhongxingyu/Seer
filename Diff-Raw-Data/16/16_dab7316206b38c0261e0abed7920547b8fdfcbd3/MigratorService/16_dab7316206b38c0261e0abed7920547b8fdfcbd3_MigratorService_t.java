 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist.migrate;
 
 import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
 import static org.oobium.utils.literal.Map;
 import static org.oobium.utils.literal.e;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 
 import java.util.List;
 
 import org.oobium.app.AppService;
 import org.oobium.app.routing.Router;
 import org.oobium.app.workers.Worker;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.migrate.controllers.MigrateController;
 import org.oobium.persist.migrate.controllers.PurgeController;
 import org.oobium.persist.migrate.controllers.RedoController;
 import org.oobium.persist.migrate.controllers.RollbackController;
 import org.oobium.utils.Config;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 public abstract class MigratorService extends AppService {
 
 	public static final String SYS_PROP_MODE = "org.oobium.persist.migrate";
 	public static final String SYS_PROP_ACTION = "org.oobium.persist.migrate.action";
 	public static final String ACTIVE = "active";
 	public static final String DONE = "Migrate complete";
 
 	
 	private static MigratorService instance;
 	
 	public static MigratorService instance() {
 		return instance;
 	}
 	
 	public static boolean isActive() {
 		return ACTIVE.equals(System.getProperty(SYS_PROP_MODE));
 	}
 	
 	
 	protected String appName;
 	
 	protected String appVersion;
 	protected Config appConfig;
 	protected String migName;
 
 	protected String migVersion;
 	protected String schema;
 	protected Config migConfig;
 	
 	protected ServiceTracker msTracker;
 
 	public MigratorService() {
 		instance = this;
 	}
 	
 	/**
 	 * Add all of your migrations here. They will be run in the
 	 * order that they are added.
 	 * @param migrations
 	 */
 	public abstract void addMigrations(Migrations migrations);
 	
 	@Override
 	public void addRoutes(Config config, Router router) {
 		if(!isActive()) { // TODO this doesn't need to be an application for remote use...
 			router.addRoute(POST, "/migrate", 							MigrateController.class);
 			router.addRoute(POST, "/migrate/{name:\\w+}/{dir=up}", 		MigrateController.class);
 			router.addRoute(POST, "/migrate/{name:\\w+}/{dir=down}", 	MigrateController.class);
 			router.addRoute(POST, "/migrate/to/{name:\\w+}", 			MigrateController.class);
 			router.addRoute(POST, "/migrate/rollback", 					RollbackController.class);
 			router.addRoute(POST, "/migrate/rollback/{step=all}", 		RollbackController.class);
 			router.addRoute(POST, "/migrate/rollback/{step:\\d+}", 		RollbackController.class);
 			router.addRoute(POST, "/migrate/redo", 						RedoController.class);
 			router.addRoute(POST, "/migrate/redo/{step=all}", 			RedoController.class);
 			router.addRoute(POST, "/migrate/redo/{step:\\d+}", 			RedoController.class);
 			router.addRoute(POST, "/migrate/purge", 					PurgeController.class);
 		}
 	}
 
 	protected abstract void commit() throws Exception;
 	
 	private Migration createMigration(Class<? extends Migration> clazz, MigrationService service) throws InstantiationException, IllegalAccessException {
 		Migration migration = clazz.newInstance();
 		migration.setLogger(logger);
 		migration.setService(service);
 		return migration;
 	}
 	
 	private Bundle getBundle(String symbolicName) {
 		ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
 		if(ref == null) {
 			throw new IllegalStateException("Package Admin service is not present");
 		}
 		PackageAdmin admin = (PackageAdmin) getContext().getService(ref);
 		Bundle[] bundles = admin.getBundles(symbolicName, null);
 		if(bundles.length == 0) {
 			throw new IllegalStateException("bundle " + symbolicName + " is not present");
 		} else if(bundles.length == 1) {
 			return bundles[0];
 		} else {
 			throw new IllegalStateException("no more than 2 bundles of " + symbolicName + " may be present");
 		}
 	}
 	
 	public Config getConfig() {
 		return appConfig;
 	}
 
 	protected abstract String getCurrentMigration();
 	
 	protected abstract List<String> getMigrated();
 	
 	public MigrationService getMigrationService() {
 		if(msTracker == null) {
 			PersistService persistor = getPersistService();
 			String serviceName = persistor.getInfo().getMigrationService();
 
 			String str = "(&(" + Constants.OBJECTCLASS + "=" + MigrationService.class.getName() + ")" +
 							"(" + MigrationService.SERVICE + "=" + serviceName + "))";
 			Filter filter;
 			try {
 				filter = context.createFilter(str);
 			} catch(InvalidSyntaxException e) {
 				throw new IllegalArgumentException("invalid syntax for the filter: " + str, e);
 			}
 
 			msTracker = new ServiceTracker(getContext(), filter, null);
 			msTracker.open();
 		}
 		MigrationService service = (MigrationService) msTracker.getService();
 		if(service != null) {
 			service.setClient(getPersistClientName());
 			service.setPersistServices(getPersistServices());
 			return service;
 		}
 		throw new IllegalStateException("MigrationService is not present - Migration cannot proceed");
 	}
 	
 	@Override
 	public String getPersistClientName() {
 //		if(isActive()) {
 //			return appName + "_migrator";
 //		}
 		return appName + "_" + appVersion;
 	}
 	
 	@Override
 	protected Config loadConfiguration() {
 		return new Config(Map(
 				e(Config.PERSIST, appConfig.get(Config.PERSIST)),
 				e(Config.HOST, "localhost"),
 				e(Config.PORT, "5001")
 			));
 	}
 	
 	public synchronized String migrate() throws Exception {
 		return migrate(null);
 	}
 	
 	public synchronized String migrate(String to) throws Exception {
 		Migrations migrations = new Migrations();
 		addMigrations(migrations);
 		List<String> names = migrations.getNames();
 		List<String> migrated = getMigrated();
 		String current = getCurrentMigration();
 		int cix = names.indexOf(current);
 		int tix = (to == null) ? (names.size() - 1) : names.indexOf(to);
 		if(tix == cix) {
 			if(to == null) {
 				return "no migration to perform: already migrated to the last one";
 			} else {
 				return "no migration to perform: \"" + to + "\" is the current migration";
 			}
 		} else {
 			MigrationService mservice = getMigrationService();
 			if(cix == -1) {
 				try {
 					mservice.createDatastore();
 				} catch(Exception e) {
 					// discard
 				}
 			}
 			setAutoCommit(false);
 			String migratedName = null;
 			try {
 				if(cix < tix) { // migrate up
 					for(int i = cix + 1; i <= tix; i++) {
 						String name = names.get(i);
 						if(!migrated.contains(name)) {
 							Migration migration = createMigration(migrations.get(i), mservice);
 							migration.up();
 							commit();
 							setMigrated(name, true);
 						}
 						migratedName = name;
 					}
 				} else { // migrate down
 					for(int i = cix; i >= tix; i--) {
 						String name = names.get(i);
 						if(migrated.contains(name)) {
 							Migration migration = createMigration(migrations.get(i), mservice);
 							migration.down();
 							commit();
 							setMigrated(name, false);
 						}
 						migratedName = (i > 0) ? names.get(i-1) : null; 
 					}
 				}
 				return "migrated successfully";
 			} catch(Exception e) {
 				rollback();
 				throw e; // pass it upstream
 			} finally {
 				setCurrentMigration(migratedName);
 			}
 		}
 	}
 	
 	public synchronized String migrate(String name, boolean up) throws Exception {
 		Migrations migrations = new Migrations();
 		addMigrations(migrations);
 		try {
 			setAutoCommit(false);
 			MigrationService mservice = getMigrationService();
 			for(int i = 0; i < migrations.size(); i++) {
 				Migration migration = createMigration(migrations.get(i), mservice);
 				if(name.equals(migration.getClass().getSimpleName())) {
 					if(up) {
 						migration.up();
 						commit();
 						return "ran " + name + ".up() successfully";
 					} else {
 						migration.down();
 						commit();
 						return "ran " + name + ".down() successfully";
 					}
 				}
 			}
 		} catch(Exception e) {
 			rollback();
 			throw e; // pass it upstream
 		}
 		return "migration \"" + name + "\" does not exist";
 	}
 	
 	public synchronized String migratePurge() throws Exception {
 		getMigrationService().dropDatastore();
 		return "database purged";
 	}
 	
 	public synchronized String migrateRedo(int step) throws Exception {
 		Migrations migrations = new Migrations();
 		addMigrations(migrations);
 		List<String> names = migrations.getNames();
 		List<String> migrated = getMigrated();
 		String current = getCurrentMigration();
 		int cix = names.indexOf(current);
 		if(cix == -1) {
 			return "already at the beginning of the migrations";
 		} else {
 			if(step == -1) step = names.size();
 			MigrationService mservice = getMigrationService();
 			String migratedName = null;
 			try {
 				setAutoCommit(false);
 				int ix = cix;
 				for(int i = 0; i < step && ix >= 0; i++, ix--) {
 					String name = names.get(ix);
 					if(migrated.contains(name)) {
						Migration migration = createMigration(migrations.get(ix), mservice);
 						migration.down();
 						commit();
 						setMigrated(name, false);
 					}
 					migratedName = (ix > 0) ? names.get(ix-1) : null;
 				}
 				for(int i = ix + 1; i <= cix; i++) {
 					String name = names.get(i);
 					Migration migration = createMigration(migrations.get(i), mservice);
 					migration.up();
 					commit();
 					setMigrated(name, true);
 					migratedName = name;
 				}
 				return "migrated successfully";
 			} catch(Exception e) {
 				rollback();
 				throw e; // pass it upstream
 			} finally {
 				setCurrentMigration(migratedName);
 			}
 		}
 	}
 	
 	public synchronized String migrateRollback() throws Exception {
 		return migrateRollback(1);
 	}
 	
 	public synchronized String migrateRollback(int step) throws Exception {
 		Migrations migrations = new Migrations();
 		addMigrations(migrations);
 		List<String> names = migrations.getNames();
 		List<String> migrated = getMigrated();
 		String current = getCurrentMigration();
 		int cix = names.indexOf(current);
 		if(cix == -1) {
 			return "already at the beginning of the migrations";
 		} else {
 			if(step == -1) step = names.size();
 			MigrationService mservice = getMigrationService();
 			String migratedName = null;
 			try {
 				setAutoCommit(false);
 				int ix = cix;
 				for(int i = 0; i < step && ix >= 0; i++, ix--) {
 					String name = names.get(ix);
 					if(migrated.contains(name)) {
 						Migration migration = createMigration(migrations.get(i), mservice);
 						migration.down();
 						commit();
 						setMigrated(name, false);
 					}
 					migratedName = (ix > 0) ? names.get(ix-1) : null;
 				}
 				return "migrated successfully";
 			} catch(Exception e) {
 				rollback();
 				throw e; // pass it upstream
 			} finally {
 				setCurrentMigration(migratedName);
 			}
 		}
 	}
 	
 	protected abstract void rollback() throws Exception;
 	
 	protected abstract void setAutoCommit(boolean autoCommit) throws Exception;
 	
 	protected abstract void setCurrentMigration(String current) throws Exception;
 	
 	protected abstract void setMigrated(String name, boolean migrated) throws Exception;
 	
 	@Override
 	protected void setName(BundleContext context) throws Exception {
 		Bundle migBundle = context.getBundle();
 		migConfig = loadConfiguration(migBundle);
 		migName = migBundle.getSymbolicName();
 		Version version = migBundle.getVersion();
 		version = new Version(version.getMajor(), version.getMinor(), version.getMicro());
 		migVersion = version.toString();
 
 		name = migName + "_" + migVersion;
 		
 		String symbolicName = migBundle.getSymbolicName();
 		symbolicName = symbolicName.substring(0, symbolicName.lastIndexOf('.'));
 
 		Bundle appBundle = getBundle(symbolicName);
 
 		appConfig = loadConfiguration(appBundle);
 		appName = appBundle.getSymbolicName();
 		version = appBundle.getVersion();
 		version = new Version(version.getMajor(), version.getMinor(), version.getMicro());
 		appVersion = version.toString();
 	}
 
 	@Override
 	public void startWorkers() {
 		if(isActive()) {
 			
 			// we're using the tracker to make sure the migration service is loaded before starting...
 			//   this assumes only one migration service, which is no longer always the case...
 			
 			// instead - get list of migration services necessary, do a count down on them, and start when they're all active
 			//   can use package manager to make sure they're all installed first...
 			
 			final ServiceTracker tmp = new ServiceTracker(getContext(), MigrationService.class.getName(), new ServiceTrackerCustomizer() {
 				public Object addingService(ServiceReference reference) {
 					Worker worker = new Worker() {
 						protected void run() {
 							String action = System.getProperty(SYS_PROP_ACTION, "migrate");
 							String[] sa = action.split("/");
 							if("migrate".equals(sa[0])) {
 								try {
 									if(sa.length == 1) {
 										migrate();
 									} else {
 										if("purge".equals(sa[1])) {
 											migratePurge();
 										}
 										else if("redo".equals(sa[1])) {
 											if(sa.length == 2) {
 												migrateRedo(-1);
 											} else {
 												migrateRedo("all".equals(sa[2]) ? -1 : coerce(sa[2], 1));
 											}
 										}
 										else if("rollback".equals(sa[1])) {
 											if(sa.length == 2) {
 												migrateRollback();
 											} else {
 												migrateRollback("all".equals(sa[2]) ? -1 : coerce(sa[2], 1));
 											}
 										}
 										else if("to".equals(sa[1])) {
 											if(sa.length == 3) {
 												migrate(sa[2]);
 											} else {
 												throw new Exception();
 											}
 										}
 										else if(sa.length == 3) {
 											if("up".equals(sa[2])) {
 												migrate(sa[1], true);
 											}
 											else if("down".equals(sa[2])) {
 												migrate(sa[1], false);
 											}
 											else {
 												throw new Exception();
 											}
 										}
 										else {
 											throw new Exception();
 										}
 									}
 								} catch(Exception e) {
 									logger.error(e);
 								}
 							}
 							logger.info(DONE);
 							try {
 								getContext().getBundle(0).stop();
 							} catch(BundleException e) {
 								logger.error(e);
 							}
 						};
 					};
 					submit(worker);
 					return null;
 				}
 				public void modifiedService(ServiceReference reference, Object service) { }
 				public void removedService(ServiceReference reference, Object service) { }
 			});
 			tmp.open();
 		}
 	}
 
 	@Override
 	protected void teardown() {
 		instance = null;
 	}
 	
 }
