 /**
  * This file is part of the Money library.
 * Copyright (C) 2007, Claus Nielsen, clanie@clanie.dk
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package dk.clanie.money;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Currency;
 
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.engine.SessionImplementor;
 import org.hibernate.type.Type;
 import org.hibernate.usertype.CompositeUserType;
 
 public class MoneyCompositeUserType implements CompositeUserType {
 
 	@SuppressWarnings("unchecked")
 	public Class returnedClass() {
 		return Money.class;
 	}
 
 	public boolean isMutable() {
 		return false;
 	}
 
 	public Object deepCopy(Object value) throws HibernateException {
 		return value;
 	}
 
 	public Serializable disassemble(Object value, SessionImplementor session)
 			throws HibernateException {
 		return (Serializable) value;
 	}
 
 	public Object assemble(Serializable cached, SessionImplementor session,
 			Object owner) throws HibernateException {
 		return cached;
 	}
 
 	public Object replace(Object original, Object target,
 			SessionImplementor session, Object owner) throws HibernateException {
 		return original;
 	}
 
 	public boolean equals(Object x, Object y) throws HibernateException {
 		if (x == y)
 			return true;
 		if (x == null || y == null)
 			return false;
 		return x.equals(y);
 	}
 
 	public int hashCode(Object x) throws HibernateException {
 		return x.hashCode();
 	}
 
 	public Object nullSafeGet(ResultSet rs, String[] names,
 			SessionImplementor session, Object owner)
 			throws HibernateException, SQLException {
 		BigDecimal amount = rs.getBigDecimal(names[0]);
 		if (rs.wasNull())
 			return null;
 		String currencyCode = rs.getString(names[1]);
 		Currency currency = Currency.getInstance(currencyCode);
 		return new Money(amount, currency);
 	}
 
 	public void nullSafeSet(PreparedStatement st, Object value, int index,
 			SessionImplementor session) throws HibernateException, SQLException {
 		if (value == null) {
 			st.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
 			st.setNull(index + 1, Hibernate.CURRENCY.sqlType());
 		} else {
 			Money money = (Money) value;
 			st.setBigDecimal(index, money.getAmount());
 			st.setString(index + 1, money.getCurrencyCode());
 		}
 	}
 
 	public String[] getPropertyNames() {
 		return new String[] { "amount", "currency" };
 	}
 
 	public Type[] getPropertyTypes() {
 		return new Type[] { Hibernate.BIG_DECIMAL, Hibernate.CURRENCY };
 	}
 
 	public Object getPropertyValue(Object component, int property)
 			throws HibernateException {
 		Money money = (Money) component;
 		if (property == 0)
 			return money.getAmount();
 		else
 			return money.getCurrencyCode();
 	}
 
 	public void setPropertyValue(Object component, int property, Object value)
 			throws HibernateException {
 		throw new UnsupportedOperationException("Immutable Money");
 	}
 
 }
