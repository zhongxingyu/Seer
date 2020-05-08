 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.utils.migration;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import se.toxbee.sleepfighter.utils.reflect.ROJava6Exception;
 import se.toxbee.sleepfighter.utils.reflect.ReflectionUtil;
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 
 /**
  * {@link IMigrationExecutor} is an abstraction of executing migrations.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Dec 22, 2013
  */
 public abstract class IMigrationExecutor<U, T extends IMigration<U>> {
 	/**
 	 * Returns the {@link Class} of the {@link IMigration} subinterface.
 	 *
 	 * @return the clazz.
 	 */
 	protected abstract Class<T> clazz();
 
 	/**
 	 * Returns the {@link Class}es of all defined migrations.
 	 *
 	 * @return an array of the migrations.
 	 */
 	protected abstract Class<?>[] definedMigrations();
 
 	/**
 	 * Called when execution of migration fails.
 	 *
 	 * @param e the MigrationException that caused failure.
 	 * @return false.
 	 */
 	protected boolean fail( IMigrationException e ) {
 		return false;
 	}
 
 	/**
 	 * Applies all migrations between originVersion & targetVersion.
 	 *
 	 * @param util the util object to pass to {@link IMigration#applyMigration(Object)}
 	 * @param originVersion the version we're coming from.
 	 * @param targetVersion the version we're moving to.
 	 * @throws IMigrationException If there was some reflection-error or if a migration failed.
 	 */
 	protected void apply( U util, int originVersion, int targetVersion ) throws IMigrationException {
 		// Find & apply all migraters.
 		for ( T m : this.assemble( originVersion, targetVersion ) ) {
 			m.applyMigration( util );
 		}
 	}
 
 	/**
 	 * Assembles any migraters available that are applicable.
 	 *
 	 * @param originVersion the version we're coming from.
 	 * @param targetVersion the version we're moving to.
 	 * @return collection of all migraters, in ascending version order.
 	 * @throws IMigrationException If there was some reflection-error.
 	 */
 	protected Collection<T> assemble( int originVersion, int targetVersion ) throws IMigrationException {
 		// Find all migraters.
 		Map<Integer, T> migraters = this.makeMigraters( originVersion, targetVersion );
 
 		// Filter out ones to skip.
 		this.filter( migraters, originVersion, targetVersion );
 
 		return migraters.values();
 	}
 
 	/**
 	 * Filters out any {@link IMigration} that are unnecessary.
 	 *
 	 * @param migraters the migraters to filter.
 	 * @param originVersion the version we're coming from.
 	 * @param targetVersion the version we're moving to.
 	 */
 	protected void filter( Map<Integer, T> migraters, int originVersion, int targetVersion ) {
 		// Let the migraters report what versions to skip.
 		Set<Integer> skipVersions = Sets.newHashSet();
 		for ( T m : migraters.values() ) {
 			Collection<Integer> skip = m.skipVersions( originVersion, targetVersion );
 			if ( skip != null ) {
 				skipVersions.addAll( skip );
 			}
 		}
 
 		// Remove any migraters to skip.
 		for ( int v : skipVersions ) {
 			migraters.remove( v );
 		}
 	}
 
 	/**
 	 * Assembles any migraters available that are above originVersion.
 	 *
 	 * @param originVersion the version we're coming from.
 	 * @return a map of version -> migraters.
 	 * @throws IMigrationException If there was some reflection-error.
 	 */
 	protected Map<Integer, T> makeMigraters( int originVersion, int targetVersion ) throws IMigrationException {
 		Map<Integer, T> migs = Maps.newTreeMap( Ordering.natural() );
 
 		Class<?>[] clazzes = this.definedMigrations();
 		for ( Class<?> _clazz : clazzes ) {
 			// Skip the class if the version is not appropriate.
 			int version = StringUtils.getDigitsIn( _clazz.getSimpleName() );
 			if ( originVersion >= version ) {
 				continue;
 			}
 
 			// Load the class, skip if not a migrater.
 			Class<? extends T> clazz = ReflectionUtil.asSubclass( _clazz, this.clazz() );
 			if ( clazz == null ) {
 				continue;
 			}
 
 			// Time to construct the migrater.
 			T migrater;
 			try {
 				migrater = ReflectionUtil.newInstance( clazz );
 			} catch ( ROJava6Exception e ) {
 				throw new IMigrationException( "Couldn't construct migrater", e, version );
 			}
 
 			int v = migrater.versionCode();
 
 			// Double check to ensure migrater version is appropriate.
			if ( originVersion > v ) {
 				continue;
 			}
 
 			// Finally, we've got a migrater.
 			migs.put( v, migrater );
 		}
 
 		return migs;
 	}
 }
