 /**
  * Copyright (c) 2005, 2011, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.business.types.impl;
 
 import static org.eclipse.uomo.core.impl.DataHelper.BDT_DELIM;
 
 import org.eclipse.uomo.business.Messages;
 import org.eclipse.uomo.business.types.BDTHelper;
 import org.eclipse.uomo.business.types.BDTypeException;
 import org.eclipse.uomo.business.types.IBDType;
 import org.eclipse.uomo.business.types.IMarket;
 import org.eclipse.uomo.core.ISymbol;
 
 /**
  * Stock Ticker - composed of market, symbol and country codes - at least one of
  * market and country will usually be present - if both are missing, we use the
  * default country
  * 
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  */
 public class StockTicker implements IBDType, ISymbol {
 	IMarket m_market = null;
 	final String m_sym;
 	String m_country = ""; //$NON-NLS-1$
 
 	/**
 	 * Stock Ticker constructor using market;sym;country, where at least one of market
 	 * and country will usually be present - if both are missing, we use the
 	 * default country
 	 * 
 	 * @throws BDTypeException
 	 */
 	public StockTicker(String s) throws BDTypeException {
 		super();
 
 		int sp = s.indexOf(BDT_DELIM);
 		if (sp == -1)
			throw new BDTypeException(Messages.StockTicker_1 + s);
 		else {
 			String tmpMarket = s.substring(0, sp);
 			if (sp > 0) {
 				try {
 					m_market = Market.get(tmpMarket);
 				} // verify market is valid (added Oct. 3)
 				catch (BDTypeException e) {
					throw new BDTypeException(Messages.StockTicker_2
 							+ s);
 				}
 			}
 
 			String s2 = s.substring(sp + 1);
 			sp = s2.indexOf(BDT_DELIM);
 			if (sp == -1)
 				m_sym = s2;
 			else {
 				m_sym = s2.substring(0, sp);
 				m_country = s2.substring(sp + 1);
 				if (!m_country.equals("") //$NON-NLS-1$
 						&& !(BDTHelper.getCountries().containsKey(m_country)))
 					throw new BDTypeException(
							Messages.StockTicker_4 + s);
 			}
 		}
 
 		if (m_sym.equals("")) //$NON-NLS-1$
			throw new BDTypeException(Messages.StockTicker_6
 					+ s);
 
 		if (m_market.equals("") && m_country.equals("")) //$NON-NLS-1$ //$NON-NLS-2$
 			m_country = BDTHelper.DEFAULT_COUNTRY;
 	}
 
 	/**
 	 * Get country String from object
 	 */
 	public String getCountry() {
 		return m_country;
 	}
 
 	/**
 	 * Get market String from object
 	 */
 	public IMarket getMarket() {
 		return m_market;
 	}
 
 	/**
 	 * Get symbol String from object
 	 */
 	public String getSymbol() {
 		return m_sym;
 	}
 
 	/**
 	 * Convert object to String
 	 * 
 	 * @return java.lang.String
 	 */
 	public String serialize() {
 
 		String s1 = ""; //$NON-NLS-1$
 
 		if (m_market != null && !m_market.equals("")) //$NON-NLS-1$
 			s1 = m_market.getName();
 
 		s1 = s1 + BDT_DELIM + m_sym;
 
 		if (!m_country.equals("")) //$NON-NLS-1$
 			s1 = s1 + BDT_DELIM + m_country;
 
 		return s1;
 	}
 
 	/**
 	 * Create a String from this object
 	 * 
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return serialize();
 	}
 }
