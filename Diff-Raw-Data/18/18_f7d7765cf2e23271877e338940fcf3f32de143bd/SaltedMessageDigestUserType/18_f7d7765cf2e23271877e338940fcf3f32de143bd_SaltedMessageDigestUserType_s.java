 /* Copyright 2008-2011 NetRadius, LLC
  * Licensed under the Simplified BSD License
  * You may obtain a copy of this license at http://netradius.com/opensource/simplified-bsd-license
  * http://netradius.com/opensource/hibernate-support
  */
 package com.netradius.hibernate.support;
 
 import com.netradius.commons.bitsnbytes.BitTwiddler;
 import org.hibernate.HibernateException;
 
 import java.security.SecureRandom;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Properties;
 
 /**
  * Extended version of MessageDigestUserType that uses a random salt in the message digest calculation.
  * This user type requires two columns to be defined. This first column will is where the message digest
  * is stored and the second column is where the randomly generated salt is stored. The size of the salt
  * generated can be overridden with the "saltSize" parameter. The salt is added to the end of the value
  * like "value{salt}" before it is digested.  In order to properly handle updates to entities using this
  * user type, you MUST have the "dynamic-update" property set to true on any entities that use this user
  * type.
  *
  * Do to a limitation in hibernate, if you need immediate access to the calculated message digest after an entity
  * has been saved or updated, you must re-query the value from your database.
  *
  * @author Erik R. Jensen
  */
 public class SaltedMessageDigestUserType extends MessageDigestUserType {
 
 	/**
 	 * Convenience constant which can be used as the value for the type property of the @Type annotation.
 	 */
 	public static final String TYPE = "com.netradius.hibernate.support.SaltedMessageDigestUserType";
 
 	/**
 	 * The parameter name, "saltSize", used to override the default salt size.
 	 */
 	public static final String PARAM_SALT_SIZE = "saltSize";
 
 	/**
 	 * The default salt size which is 16 bytes.
 	 */
 	public static final int DEFAULT_SALT_SIZE = 16;
 
 	/**
 	 * The random number generated used to generate the salt.
 	 */
 	protected SecureRandom rand = new SecureRandom();
 
 	/**
 	 * The size of the salt to generate.
 	 */
 	protected int saltSize;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setParameterValues(Properties properties) {
 		if (properties != null) {
 			super.setParameterValues(properties);
 			String tmp = properties.getProperty(PARAM_SALT_SIZE);
 			if (tmp != null) {
 				try {
 					saltSize = Integer.parseInt(tmp);
 				} catch (NumberFormatException x) {
 					throw new HibernateException("Invalid salt size " + saltSize + ". Salt size must be an integer.");
 				}
 			} else {
 				saltSize = DEFAULT_SALT_SIZE;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int[] sqlTypes() {
 		return new int[] {SQL_TYPE, SQL_TYPE};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void nullSafeSet(PreparedStatement ps, Object value, int index)
 			throws HibernateException, SQLException {
 		if (value == null) {
 			super.nullSafeSet(ps, value, index);
 			ps.setNull(index + 1, SQL_TYPE);
 		} else {
 			final String salt = generateSalt();
 			String str = new StringBuilder((String)value)
 					.append("{").append(salt).append("}").toString();
 			super.nullSafeSet(ps, str, index);
 			ps.setString(index + 1, salt);
 		}
 	}
 
 	/**
 	 * Generates the salt value.
 	 *
 	 * @return the generated salt
 	 */
 	protected String generateSalt() {
 		byte[] bytes = new byte[saltSize];
 		rand.nextBytes(bytes);
		return BitTwiddler.tohexstr(bytes);
 	}
 }
