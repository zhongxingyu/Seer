 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.symbol.enums;
 
 import static com.barchart.feed.ddf.symbol.enums.DDF_ExchangeKind.*;
 import static com.barchart.util.ascii.ASCII.*;
 
 import com.barchart.util.math.MathExtra;
 import com.barchart.util.values.api.Value;
 
 // TODO: Auto-generated Javadoc
 /**
  * dff market exchange channels.
  */
 public enum DDF_Exchange implements Value<DDF_Exchange> {
 
 	TEST(UNDER, FUTURE), //
 
 	Fix_Me_0(_0_, FUTURE), //
 	Fix_Me_1(_1_, FUTURE), //
 	Fix_Me_2(_2_, FUTURE), //
 	Fix_Me_3(_3_, FUTURE), //
 	Fix_Me_4(_4_, FUTURE), //
 	Fix_Me_5(_5_, FUTURE), //
 	Fix_Me_6(_6_, FUTURE), //
 	Fix_Me_7(_7_, FUTURE), //
 	Fix_Me_8(_8_, FUTURE), //
 	Fix_Me_9(_9_, FUTURE), //
 
 	AMEX(_A_, STOCK), //
 	AMEX_Book_Top(_a_, STOCK), //
 
 	CME_CBOT(_B_, FUTURE), //
 	CME_CBOT_XXX(_b_, FUTURE), //
 
 	ICE_US(_C_, FUTURE), //
 	CME_Special_Products(_c_, FUTURE), //
 
 	NASDAQ_OTC_BB(_D_, STOCK), //
 	NASDAQ_OTC_BB_XXX(_d_, STOCK), //
 
 	CME_COMEX(_E_, FUTURE), //
 	CME_COMEX_XXX(_e_, FUTURE), //
 
 	Mutual_Funds(_F_, INDEX), //
 	Money_Market_Funds(_f_, INDEX), //
 
 	CME_MGEX(_G_, FUTURE), //
 	CME_MGEX_XXX(_g_, FUTURE), //
 
 	Fix_Me_H(_H_, FUTURE), //
 	Fix_Me_H_XXX(_h_, FUTURE), //
 
 	Index_NO_DOW_NO_SP(_I_, INDEX), //
 	Index_NO_DOW_NO_SP_XXX(_i_, INDEX), //
 
 	CME_NYMEX(_J_, FUTURE), //
 	CME_NYMEX_Swaps(_j_, FUTURE), //
 
 	CME_KBOT(_K_, FUTURE), //
 	CME_KBOT_XXX(_k_, FUTURE), //
 
 	ICE_EU(_L_, FUTURE), //
 	ICE_EU_XXX(_l_, FUTURE), //
 
 	CME_Main(_M_, FUTURE), //
 	CME_Weather(_m_, FUTURE), //
 
 	NYSE(_N_, STOCK), //
 	NYSE_Book_Top(_n_, STOCK), //
 
 	Index_DOW(_O_, INDEX), //
 	Index_DOW_Full(_o_, INDEX), //
 
 	Index_SP(_P_, INDEX), //
 	Index_SP_XXX(_p_, INDEX), // XXX
 
 	NASDAQ(_Q_, STOCK), //
 	NASDAQ_Book_Top(_q_, STOCK), //
 
 	CBOE_Futures(_R_, FUTURE), //
	CBOE_Index(_r_, INDEX), //
 
 	Fix_Me_S(_S_, FUTURE), //
 	Fix_Me_S_XXX(_s_, FUTURE), //
 
 	Fix_Me_T(_T_, FUTURE), //
 	Fix_Me_T_XXX(_t_, FUTURE), //
 
 	NASDAQ_OTC_PinkSheets(_U_, STOCK), //
 	NASDAQ_OTC_PinkSheets_XXX(_u_, STOCK), //
 
 	Fix_Me_V(_V_, FUTURE), //
 	Fix_Me_V_XXX(_v_, FUTURE), //
 
 	ICE_Canada(_W_, FUTURE), //
 	ICE_Canada_XXX(_w_, FUTURE), //
 
 	Fix_Me_X(_X_, FUTURE), // XXX
 	Fix_Me_X_XXX(_x_, FUTURE), // XXX
 
 	NYSE_Metals(_Y_, FUTURE), //
 	NYSE_Metals_XXX(_y_, FUTURE), //
 
 	Futures_Books(_Z_, FUTURE), //
 	Futures_Books_XXX(_z_, FUTURE), //
 
 	Forex(DOLLAR, FOREX), //
 
 	//
 
 	UNKNOWN(QUEST, DDF_ExchangeKind.UNKNOWN), //
 
 	/** The ord. */
  ;
 
 	// ////////////////////////
 
 	/** byte sized enum ordinal */
 	public final byte ord;
 
 	/** ddf encoding of this enum. */
 	public final byte code;
 
 	/** ddf "kind" qualifier of this ddf exchange channel. */
 	public final DDF_ExchangeKind kind;
 
 	/** free style exchange description; can be used in full text search. */
 	public final String description;
 
 	// ////////////////////////
 
 	private DDF_Exchange(final byte code, final DDF_ExchangeKind kind) {
 
 		this.ord = (byte) ordinal();
 		this.code = code;
 		this.kind = kind;
 
 		this.description = description();
 	}
 
 	/** TODO use real human readable description as ENUM property */
 	private String description() {
 		return name().replaceAll("_", " ");
 	}
 
 	private final static DDF_Exchange[] ENUM_VALUES = values();
 
 	static {
 		// validate use of byte ord
 		MathExtra.castIntToByte(ENUM_VALUES.length);
 	}
 
 	/**
 	 * From code.
 	 *
 	 * @param code the code
 	 * @return the dD f_ exchange
 	 */
 	public final static DDF_Exchange fromCode(final byte code) {
 		for (final DDF_Exchange known : ENUM_VALUES) {
 			if (known.code == code) {
 				return known;
 			}
 		}
 		return UNKNOWN;
 	}
 
 	/**
 	 * From ord.
 	 *
 	 * @param ord the ord
 	 * @return the dD f_ exchange
 	 */
 	public final static DDF_Exchange fromOrd(final byte ord) {
 		return ENUM_VALUES[ord];
 	}
 
 	/**
 	 * Checks if is known.
 	 *
 	 * @return true, if is known
 	 */
 	public final boolean isKnown() {
 		return this != UNKNOWN;
 	}
 
 	boolean isNYSE() {
 		switch (this) {
 		case AMEX:
 		case AMEX_Book_Top:
 		case NYSE:
 		case NYSE_Book_Top:
 		case NYSE_Metals:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Checks if is nASDAQ.
 	 *
 	 * @return true, if is nASDAQ
 	 */
 	public final boolean isNASDAQ() {
 		switch (this) {
 		case NASDAQ:
 		case NASDAQ_Book_Top:
 		case NASDAQ_OTC_PinkSheets:
 		case NASDAQ_OTC_BB:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#freeze()
 	 */
 	@Override
 	public DDF_Exchange freeze() {
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#isFrozen()
 	 */
 	@Override
 	public boolean isFrozen() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#isNull()
 	 */
 	@Override
 	public boolean isNull() {
 		return this == UNKNOWN;
 	}
 
 }
