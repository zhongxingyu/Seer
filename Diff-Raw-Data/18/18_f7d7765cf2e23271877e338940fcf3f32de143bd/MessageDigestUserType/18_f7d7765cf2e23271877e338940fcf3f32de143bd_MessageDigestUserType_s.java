 /* Copyright 2008-2011 NetRadius, LLC
  * Licensed under the Simplified BSD License
  * You may obtain a copy of this license at http://netradius.com/opensource/simplified-bsd-license
  * http://netradius.com/opensource/hibernate-support
  */
 package com.netradius.hibernate.support;
 
 import com.netradius.commons.bitsnbytes.BitTwiddler;
 import org.hibernate.HibernateException;
 import org.hibernate.usertype.ParameterizedType;
 import org.hibernate.usertype.UserType;
 
 import java.io.Serializable;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.nio.charset.UnsupportedCharsetException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Properties;
 
 /**
  * This user type handles converting a string value to a hex encoded message digest. In order to properly handle
  * updates to entities using this user type, you MUST have the "dynamic-update" property set to true on any
  * entities that use this user type.
  *
  * Do to a limitation in hibernate, if you need immediate access to the calculated message digest after an entity
  * has been saved or updated, you must re-query the value from your database.
  *
  * @author Erik R. Jensen
  */
 public class MessageDigestUserType implements UserType, ParameterizedType {
 
 	/**
 	 * Convenience constant which can be used as the value for the type property of the @Type annotation.
 	 */
 	public static final String TYPE = "com.netradius.hibernate.support.MessageDigestUserType";
 
 	/**
 	 * The parameter name, "charset", used to override the default character set which is UTF8.
 	 */
 	public static final String PARAM_CHARSET = "charset";
 
 	/**
 	 * The default character set, "UTF8", which is used to convert the string value to bytes.
 	 */
 	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF8");
 
 	/**
 	 * The parameter name, "algorithm", used to override the default algorithm which is SHA1. 
 	 */
 	public static final String PARAM_ALGORITHM = "algorithm";
 
 	/**
 	 * The default algorithm, "SHA1", which is used to calculate the message digest.
 	 */
 	public static final String DEFAULT_ALGORITHM = "SHA1";
 
 	/**
 	 * The parameter name, "provider", which is used to select the message digest algorithm provider to use.
 	 * By default, there is no provider.
 	 */
 	public static final String PARAM_PROVIDER = "provider";
 
 	/**
 	 * The SQL type this user type maps to.
 	 */
 	public static final int SQL_TYPE = Types.VARCHAR;
 
 	/**
 	 * The message digest algorithm.
 	 */
 	protected String algorithm;
 
 	/**
 	 * The message digest provider.
 	 */
 	protected String provider;
 
 	/**
 	 * The character set to use.
 	 */
 	protected Charset charset;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setParameterValues(final Properties properties) {
 		if (properties != null) {
 			algorithm = properties.getProperty(PARAM_ALGORITHM);
 			provider = properties.getProperty(PARAM_PROVIDER);
 			String tmp = properties.getProperty(PARAM_CHARSET);
 			if (tmp != null) {
 				try {
 					charset = Charset.forName(tmp);
 				} catch (IllegalCharsetNameException x) {
 					throw new HibernateException("Unsupported character set " + tmp + ": " + x.getMessage(), x);
 				} catch (UnsupportedCharsetException x) {
 					throw new HibernateException("Unsupported character set " + tmp + ": " + x.getMessage(), x);
 				}
 			}
 		}
 		if (algorithm == null) {
 			algorithm = DEFAULT_ALGORITHM;
 		}
 		if (charset == null) {
 			charset = DEFAULT_CHARSET;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int[] sqlTypes() {
 		return new int[] {SQL_TYPE};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Class returnedClass() {
 		return String.class;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean equals(final Object o, final Object o1) throws HibernateException {
 		return o != null && o.equals(o1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int hashCode(final Object o) throws HibernateException {
 		return o.hashCode();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner)
 			throws HibernateException, SQLException {
 		final String val = rs.getString(names[0]);
 		return rs.wasNull() ? null : val;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void nullSafeSet(final PreparedStatement ps, final Object value, final int index)
 			throws HibernateException, SQLException {
 		if (value == null) {
 			ps.setNull(index, SQL_TYPE);
 		} else {
			ps.setString(index, BitTwiddler.tohexstr(digest(((String)value).getBytes(charset))));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected byte[] digest(final byte[] data) {
 		try {
 			final MessageDigest md = provider == null
 						? MessageDigest.getInstance(algorithm)
 						: MessageDigest.getInstance(algorithm, provider);
 			return md.digest(data);
 		} catch (NoSuchAlgorithmException x) {
 			final StringBuilder sb = new StringBuilder("Failed to find message digest algorithm ").append(algorithm);
 			if (provider != null) {
 				sb.append(" in provider ").append(provider);
 			}
 			sb.append(": ").append(x.getMessage());
 			throw new HibernateException(sb.toString(), x);
 		} catch (NoSuchProviderException x) {
 			throw new HibernateException("Failed to find message digest provider "
 					+ provider + ": " + x.getMessage(), x);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object deepCopy(final Object o) throws HibernateException {
 		return o;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isMutable() {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Serializable disassemble(final Object o) throws HibernateException {
 		return (Serializable)o;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object assemble(final Serializable serializable, final Object o) throws HibernateException {
 		return serializable;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
 		return original;
 	}
 }
