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
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.Collection;
 import java.util.Currency;
 import java.util.Iterator;
 
 import org.apache.commons.lang.builder.*;
 
 /**
  * Money.
  * 
  * @author Claus Nielsen
  */
 public class Money {
 
 	public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.UNNECESSARY;
 
 	private BigDecimal amount;
 	private Currency currency;
 
 	private Money() {
 	}
 
 	public Money(BigDecimal amount, Currency currency) {
 		this(amount, currency, DEFAULT_ROUNDING_MODE);
 	}
 
 	public Money(BigDecimal amount, Currency currency, RoundingMode roundingMode) {
 		int scale = currency.getDefaultFractionDigits();
 		if (scale < 0)
 			scale = 6;
 		this.amount = amount.setScale(scale, roundingMode);
 		this.currency = currency;
 	}
 
 	/**
 	 * Getter required for object relational mapping.
 	 * 
 	 * @return BigDecimal amount
 	 */
 	BigDecimal getAmount() {
 		return amount;
 	}
 
 	/**
 	 * Get the currency code of this Money.
 	 * 
 	 * @return String with the currency code.
 	 */
 	public String getCurrencyCode() {
 		return currency.getCurrencyCode();
 	}
 
 	/**
 	 * Check that another Money instance is in the same currency.
 	 * 
 	 * @param other
 	 */
 	private void checkCurrency(Money other) {
 		if (other.currency != currency)
 			throw new IllegalArgumentException(
 					"Amounts must have same currency.");
 	}
 
 	/**
 	 * Summarize a collection of Money.
 	 * 
 	 * All the money must have the same currency.
 	 * 
 	 * @param monies
 	 * @return Money - sum. Null if the collection is empty or null.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if there are Money with different currencies in the
 	 *             collection.
 	 */
 	public static Money sum(Collection<Money> monies) {
 		if (monies == null || monies.size() == 0)
 			return null;
 		Iterator<Money> iter = monies.iterator();
 		Money money = iter.next();
 		BigDecimal sum = money.amount;
 		Currency currency = money.currency;
 		while (iter.hasNext()) {
 			money = iter.next();
 			if (money.currency != currency) {
 				throw new IllegalArgumentException(
 						"Amounts must have same currency.");
 			}
 			sum = sum.add(money.amount);
 		}
 		return new Money(sum, currency);
 	}
 
 	/**
 	 * Add two monetary amounts.
 	 * 
 	 * Returns a Money whose value is (this + augend), and whose currency is the
 	 * same as the currency of the two added amounts.
 	 * 
 	 * @param augend
 	 * @return Money - sum of this and augend.
 	 */
 	public Money add(Money augend) {
 		checkCurrency(augend);
 		return new Money(amount.add(augend.amount), currency);
 	}
 
 	/**
 	 * Subtract an amount from this.
 	 * 
 	 * Returns a Money whose value is (this - subtrahend), and whose currency is
 	 * the same as the currency of this and subtrahend.
 	 * 
 	 * @param subtrahend
 	 * @return Money - sum of this and subtrahend.
 	 */
 	public Money subtract(Money subtrahend) {
 		checkCurrency(subtrahend);
 		return new Money(amount.subtract(subtrahend.amount), currency);
 	}
 
 	/**
 	 * Divide evenly into parts.
 	 * 
 	 * Divides an amount into parts of approximately the same size while
 	 * ensuring that the sum of all the parts equals the whole.
 	 * <p>
 	 * Parts of unequal size will be distributed evenly in the returned array.
 	 * <p>
 	 * For example, if asked to divede 20 into 6 parts the result will be {3.33,
 	 * 3.34, 3.33, 3.33, 3.34, 3.33}
 	 * 
 	 * @param parts -
 	 *            number of parts
 	 * @return Money[] with the parts
 	 */
 	public Money[] divideEvenlyIntoParts(int parts) {
 		Money[] res = new Money[parts];
 		final BigDecimal bdParts = new BigDecimal(parts);
 		final MathContext mc = MathContext.DECIMAL128;
 		BigDecimal sumOfPreviousParts = BigDecimal.ZERO;
 		for (int i = 0; i < parts; i++) {
 			Money part = new Money();
 			BigDecimal sumOfParts = amount.multiply(new BigDecimal(i + 1));
 			sumOfParts = sumOfParts.divide(bdParts, mc);
 			sumOfParts = sumOfParts.setScale(amount.scale(), mc
 					.getRoundingMode());
 			part.amount = sumOfParts.subtract(sumOfPreviousParts);
 			sumOfPreviousParts = sumOfParts;
 			part.currency = currency;
 			res[i] = part;
 		}
 		return res;
 	}
 
 	/**
 	 * Returns a Money whose value is (-this).
 	 * 
 	 * @return -this.
 	 */
 	public Money negate() {
 		Money newMoney = new Money();
 		newMoney.amount = amount.negate();
 		newMoney.currency = currency;
 		return newMoney;
 	}
 
 	/**
 	 * Returns a Money whose value is the absolute value of this.
 	 * 
 	 * @return abs(this)
 	 */
 	public Money abs() {
 		if (amount.compareTo(BigDecimal.ZERO) >= 0)
 			return this;
 		return negate();
 	}
 
 	public boolean isZero() {
 		return (amount.compareTo(BigDecimal.ZERO) == 0);
 	}
 
 	public boolean isNegative() {
 		return (amount.compareTo(BigDecimal.ZERO) < 0);
 	}
 
 	public boolean isPositive() {
 		return (amount.compareTo(BigDecimal.ZERO) > 0);
 	}
 
 	/**
 	 * Compare.
 	 * 
 	 * Compares this Money with the specified Money. Two Money This method is
 	 * provided in preference to individual methods for each of the six boolean
 	 * comparison operators (<, ==, >, >=, !=, <=). The suggested idiom for
 	 * performing these comparisons is: (x.compareTo(y) <op> 0), where <op> is
 	 * one of the six comparison operators.
 	 * 
 	 * @param val
 	 *            Money to which this Money is to be compared.
 	 * @return -1, 0, or 1 as this Money is less than, equal to, or greater than
 	 *         val.
 	 */
 	public int compareTo(Money val) {
 		checkCurrency(val);
 		return amount.compareTo(val.amount);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final Money other = (Money) obj;
 		if (other.currency != currency)
 			return false;
 		return amount.compareTo(other.amount) == 0;
 	}
 
 	@Override
 	public int hashCode() {
 		return amount.hashCode();
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 
 }
