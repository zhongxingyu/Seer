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
 package se.chalmers.dat255.sleepfighter.model.challenge;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import se.chalmers.dat255.sleepfighter.utils.model.IdProvider;
 
 import com.google.common.collect.Maps;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * ChallengeConfig models what challenge to produce, and with what settings.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 3, 2013
  */
 @DatabaseTable(tableName = "challenge_config")
 public class ChallengeConfig implements IdProvider {
 	public static final String SET_FOREIGN_COLUMN = "challenge_set_id";
 
 	/* --------------------------------
 	 * Fields.
 	 * --------------------------------
 	 */
 
 	@DatabaseField(canBeNull = false, columnName = SET_FOREIGN_COLUMN)
 	private int challengeSetId;
 
 	@DatabaseField(generatedId = true)
 	private int id;
 
 	@DatabaseField
 	private boolean enabled = true;
 
 	@DatabaseField
 	private ChallengeType type;
 
 	private Map<String, String> params;
 
 	/* --------------------------------
 	 * Constructors.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Constructs the config object with no parameters.
 	 */
 	public ChallengeConfig() {
 		this.params = Maps.newHashMap();
 	}
 
 	/**
 	 * Constructs the config object with type.
 	 *
 	 * @param type the type of the config.
 	 * @param enabled true if challenge is to be enabled. See {@link #setEnabled(boolean)}
 	 */
 	public ChallengeConfig( ChallengeType type, boolean enabled ) {
 		this( type, enabled, null );
 	}
 
 	/**
 	 * Constructs the config object with given its type, parameters and whether or not to enable it.
 	 *
 	 * @param type the type of the config.
 	 * @param enabled true if challenge is to be enabled. See {@link #setEnabled(boolean)}
 	 * @param params the initial parameters to use for config.
 	 */
 	public ChallengeConfig( ChallengeType type, boolean enabled, Map<String, String> params ) {
 		this.type = type;
 		this.enabled = enabled;
 
		this.params = this.params == null ? new HashMap<String, String>() : params;
 	}
 
 	/**
 	 * Copy constructor.
 	 *
 	 * @param rhs the config to copy from.
 	 */
 	public ChallengeConfig( ChallengeConfig rhs ) {
 		this( rhs.type, rhs.enabled, null );
 
 		for ( Entry<String, String> entry : rhs.params.entrySet() ) {
 			this.params.put( entry.getKey(), entry.getValue() );
 		}
 	}
 
 	/* --------------------------------
 	 * Public Interface.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Returns the id of the owner challenge set.
 	 *
 	 * @return the id of the set.
 	 */
 	public int getSetId() {
 		return this.challengeSetId;
 	}
 
 	@Override
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns the type of this config, used to factorize a challenge.
 	 *
 	 * @return the type.
 	 */
 	public ChallengeType getType() {
 		return this.type;
 	}
 
 	/**
 	 * Returns whether or not the specific challenge given by {@link #getType()} is enabled or not.
 	 *
 	 * @return true if it is enabled.
 	 */
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	/* --------------------------------
 	 * Public Accessors: Parameters.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Returns a settings parameter for this ChallengeConfig.
 	 *
 	 * @param key the setting key, challenge specific.
 	 * @return the value, challenge specific.
 	 */
 	public String getParam( String key ) {
 		return this.params.get( key );
 	}
 
 	/**
 	 * Returns an unmodifiable view of all parameters for this challenge config.
 	 *
 	 * @return the parameters.
 	 */
 	public Map<String, String> getParams() {
 		return Collections.unmodifiableMap( this.params );
 	}
 
 	/* ---------------------------------------
 	 * Protected Setters (Facade).
 	 * ---------------------------------------
 	 */
 
 	/**
 	 * Sets whether or not to enable challenge.
 	 *
 	 * @param enabled if true, challenge is enabled, otherwise it is disabled.
 	 * @return true if {@link #isEnabled()} will return the inverse value after calling {@link #setEnabled(boolean)}.
 	 */
 	protected boolean setEnabled( boolean enabled ) {
 		if ( this.enabled == enabled ) {
 			return false;
 		}
 
 		this.enabled = enabled;
 		return true;
 	}
 
 	/**
 	 * Sets a settings parameter for this ChallengeConfig.
 	 *
 	 * @param key the setting key, challenge specific.
 	 * @param value the value, challenge specific. null is converted to an empty string.
 	 * @return the old value, or null if not set before.
 	 */
 	protected String setParam( String key, String value ) {
 		return this.params.put( key, value );
 	}
 
 	/* --------------------------------
 	 * PERSISTENCE ONLY METHODS.
 	 * --------------------------------
 	 */
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets a {@link ChallengeParam}, bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * @param param the {@link ChallengeParam} to set.
 	 */
 	public void setFetched( ChallengeParam param ) {
 		this.params.put( param.getKey(), param.getValue() );
 	}
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets the set ID owning this config, bypassing any and all checks.</p>
 	 *
 	 * @param id the id of the owning set, to use.
 	 */
 	public void setFetchedSetId( int id ) {
 		this.challengeSetId = id;
 	}
 }
