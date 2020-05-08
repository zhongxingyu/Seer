 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.enums;
 
 import static com.barchart.util.ascii.ASCII.*;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.util.enums.EnumByteOrdinal;
 import com.barchart.util.enums.EnumCodeByte;
 import com.barchart.util.math.MathExtra;
 import com.barchart.util.values.api.TimeValue;
 import com.barchart.util.values.provider.ValueBuilder;
 
 // TODO: Auto-generated Javadoc
 /**
  * ddf trade day codes.
  */
 public enum DDF_TradeDay implements EnumCodeByte, EnumByteOrdinal {
 
 	/*
 	 * NOTE: keep the monotonously increasing sequence or you will break ordinal
 	 */
 
 	D01(_1_), //
 	D02(_2_), //
 	D03(_3_), //
 	D04(_4_), //
 	D05(_5_), //
 	D06(_6_), //
 	D07(_7_), //
 	D08(_8_), //
 	D09(_9_), //
 	D10(_0_), //
 	D11(_A_), //
 	D12(_B_), //
 	D13(_C_), //
 	D14(_D_), //
 	D15(_E_), //
 	D16(_F_), //
 	D17(_G_), //
 	D18(_H_), //
 	D19(_I_), //
 	D20(_J_), //
 	D21(_K_), //
 	D22(_L_), //
 	D23(_M_), //
 	D24(_N_), //
 	D25(_O_), //
 	D26(_P_), //
 	D27(_Q_), //
 	D28(_R_), //
 	D29(_S_), //
 	D30(_T_), //
 	D31(_U_), //
 
 	// keep this item last
 
 	UNKNOWN(QUEST), //
 
 	;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(DDF_TradeDay.class);
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.enums.EnumByteOrdinal#ord()
 	 */
 	@Override
 	public final byte ord() {
 		return ord;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.enums.EnumCodeByte#code()
 	 */
 	@Override
 	public final byte code() {
 		return code;
 	}
 
 	/** byte sized ordinal. */
 	public final byte ord;
 
 	/** ddf feed encoding. */
 	public final byte code;
 
 	/** day of month number. */
 	public int day;
 
 	private DDF_TradeDay(final byte code) {
 		this.ord = (byte) ordinal();
 		this.code = code;
 		this.day = ordinal() + 1;
 	}
 
 	private final static DDF_TradeDay[] ENUM_VALUES = values();
 
 	/**
 	 * Values unsafe.
 	 *
 	 * @return the dD f_ trade day[]
 	 */
 	@Deprecated
 	public final static DDF_TradeDay[] valuesUnsafe() {
 		return ENUM_VALUES;
 	}
 
 	static {
 		// validate use of byte ord
 		MathExtra.castIntToByte(ENUM_VALUES.length);
 	}
 
 	/**
 	 * From ord.
 	 *
 	 * @param ord the ord
 	 * @return the dD f_ trade day
 	 */
 	public final static DDF_TradeDay fromOrd(final byte ord) {
 		return ENUM_VALUES[ord];
 	}
 
 	// TODO optimize
 	/**
 	 * From code.
 	 *
 	 * @param code the code
 	 * @return the dD f_ trade day
 	 */
 	public final static DDF_TradeDay fromCode(final byte code) {
 		for (final DDF_TradeDay known : ENUM_VALUES) {
 			if (known.code == code) {
 				return known;
 			}
 		}
 		return UNKNOWN;
 	}
 
 	/**
 	 * From millis utc.
 	 *
 	 * @param millisUTC the millis utc
 	 * @return the dD f_ trade day
 	 */
 	public static final DDF_TradeDay fromMillisUTC(final long millisUTC) {
 		final DateTime dateTime = new DateTime(millisUTC, DateTimeZone.UTC);
 		return fromDay(dateTime.getDayOfMonth());
 	}
 
 	/**
 	 * From day.
 	 *
 	 * @param day the day
 	 * @return the dD f_ trade day
 	 */
 	public static final DDF_TradeDay fromDay(final int day) {
 		/* NOTE: Date range is 1...31 ordinal range is 0...30 */
 		if (1 <= day & day <= 31) {
 			final int ordinal = day - 1;
 			return ENUM_VALUES[ordinal];
 		}
 		return UNKNOWN;
 	}
 
 	/**
 	 * Checks if is known.
 	 *
 	 * @return true, if is known
 	 */
 	public final boolean isKnown() {
 		return this != UNKNOWN;
 	}
 
 	// not used
 	static boolean isCurrent(final DDF_TradeDay one, final DDF_TradeDay two) {
 
 		if (!one.isKnown() || !two.isKnown()) {
 			return false;
 		}
 
 		return one == two;
 
 	}
 
 	// not used
 	static boolean isPrevious(final DDF_TradeDay one, final DDF_TradeDay two) {
 
 		if (!one.isKnown() || !two.isKnown()) {
 			return false;
 		}
 
 		final int diff = one.ord - two.ord;
 
 		if (diff == -1) {
 			return true;
 		}
 
 		if (diff > 0) {
 			return true;
 		}
 
 		return false;
 
 	}
 
 	//
 
 	/**
 	 * note: this is an approximation
 	 * 
 	 * depends on current invocation time
 	 * 
 	 * assumes DDF CST time zone of DDF_TradeDay.
 	 *
 	 * @return the time value
 	 */
 	public TimeValue tradeDate() {
 
 		// current date in chicago
 		final DateTime todayCST = new DateTime(ZONE_CST);
 
 		final TimeValue tradeDate = tradeDateFrom(this, todayCST);
 
 		return tradeDate;
 	}
 
 	private static final DateTimeZone ZONE_CST = DateTimeZone
 			.forID("America/Chicago");
 
 	private static final DateTimeZone ZONE_UTC = DateTimeZone.UTC;
 
 	/** longest market holiday duration */
 	private static final int HOLIDAY_THESHOLD = 5;
 
 	/**
 	 * recover full trade date from DDF day code and todays date
 	 * 
 	 * expressed in UTC zone
 	 * 
 	 * year, month, day : should be treated as local market trade date.
 	 *
 	 * @param tradeDay the trade day
 	 * @param todayDate the today date
 	 * @return the time value
 	 */
 	public static TimeValue tradeDateFrom(final DDF_TradeDay tradeDay,
 			final DateTime todayDate) {
 
 		// trading day of month reported by the feed
 		final int tradingDayNum = tradeDay.day;
 
 		// current day of month
 		final int currentDayNum = todayDate.getDayOfMonth();
 
 		// positive for same month if trading date is in the future
		// unless day enum is not a day in the month ???
 		final int difference = tradingDayNum - currentDayNum;
 
 		final boolean isSmall = Math.abs(difference) <= HOLIDAY_THESHOLD;
 		final boolean isLarge = !isSmall;
 
 		//
 
 		final boolean isSameMonthSameDay = (difference == 0);
 
 		final boolean isSameMonthPastDay = difference < 0 & isSmall;
 		final boolean isSameMonthNextDay = difference > 0 & isSmall;
 
 		final boolean isPastMonthPastDay = difference > 0 & isLarge;
 		final boolean isNextMonthNextDay = difference < 0 & isLarge;
 
 		//
 
 		DateTime generated;
 
 		try {
 			if (isSameMonthSameDay) {
 				generated = todayDate;
 			} else if (isSameMonthPastDay) {
 				generated = todayDate.withDayOfMonth(tradingDayNum);
 			} else if (isSameMonthNextDay) {
 				generated = todayDate.withDayOfMonth(tradingDayNum);
 			} else if (isPastMonthPastDay) {
 				generated = todayDate.minusMonths(1).withDayOfMonth(tradingDayNum);
 			} else if (isNextMonthNextDay) {
 				generated = todayDate.plusMonths(1).withDayOfMonth(tradingDayNum);
 			} else {
 				logger.error("should not happen");
 				generated = todayDate;
 			}
		} catch (final Exception e) {
 			generated = todayDate;
 		}
 
 		final DateTime result = new DateTime(//
 				generated.getYear(), //
 				generated.getMonthOfYear(), //
 				generated.getDayOfMonth(), //
 				0, 0, 0, 0, ZONE_UTC);
 
 		final long millisUTC = result.getMillis();
 
 		return ValueBuilder.newTime(millisUTC);
 
 	}
 
 }
