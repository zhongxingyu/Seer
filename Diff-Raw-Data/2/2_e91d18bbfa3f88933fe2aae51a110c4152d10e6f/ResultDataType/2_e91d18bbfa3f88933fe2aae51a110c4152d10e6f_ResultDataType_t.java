 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.dataintegrity.db.hibernate;
 
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.usertype.UserType;
 import org.openmrs.module.dataintegrity.IntegrityCheckUtil;
 import org.openmrs.module.dataintegrity.QueryResult;
 
 /**
  * Hibernate's Custom Type for {@link QueryResult}
  */
 public class ResultDataType implements UserType, Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int[] TYPES = new int[] { Types.VARCHAR };
 
 	/**
 	 * @see org.hibernate.usertype.UserType#returnedClass()
 	 */
 	public Class returnedClass() {
 		return QueryResult.class;
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#sqlTypes()
 	 */
 	public int[] sqlTypes() {
 		return TYPES;
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#isMutable()
 	 */
 	public boolean isMutable() {
 		return true;
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
 	 */
 	public Object deepCopy(Object value) throws HibernateException {
 		return new QueryResult((QueryResult) value);
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable,
 	 *      java.lang.Object)
 	 */
 	public Object assemble(Serializable cached, Object owner)
 			throws HibernateException {
 		return deepCopy(cached);
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
 	 */
 	public Serializable disassemble(Object value) throws HibernateException {
 		return (Serializable) deepCopy(value);
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public Object replace(Object original, Object target, Object owner)
 			throws HibernateException {
 		return deepCopy(original);
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet,
 	 *      java.lang.String[], java.lang.Object)
 	 * @see QueryResult#valueOf(String)
 	 */
 	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
 			throws HibernateException, SQLException {
		String value = (String) rs.getString(names[0]);
 		if (value != null)
 			return QueryResult.valueOf(value);
 		else
 			return null;
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
 	 *      java.lang.Object, int)
 	 * @see IntegrityCheckUtil#serialize(LocalizedString)
 	 */
 	public void nullSafeSet(PreparedStatement ps, Object value, int index)
 			throws HibernateException, SQLException {
 		if (value != null) {
 			if (value instanceof java.lang.String) {
 				// only in query mode(e.g., Expression.like('queryResult',
 				// value)), the type of value will be String
 				// use this tricky here, in order to support Hibernate's
 				// easy-reading query mode, such as
 				// Expression.like(propertyName, value)
 				ps.setString(index, value.toString());
 			} else {
 				// only when create/update an OpenmrsMetadata object, the type
 				// of value will be LocalizedString
 				ps.setString(index, IntegrityCheckUtil.serializeResult((QueryResult) value));
 			}
 		} else {
 			ps.setString(index, null);
 		}
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	public boolean equals(Object a, Object b) throws HibernateException {
 		if (a == b)
 			return true;
 		if (a != null && b != null) {
 			if (!(a instanceof QueryResult) || !(b instanceof QueryResult))
 				return false;
 			else
 				return a.equals(b);
 		}
 		return false;
 	}
 
 	/**
 	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
 	 */
 	public int hashCode(Object x) throws HibernateException {
 		return x.hashCode();
 	}
 }
