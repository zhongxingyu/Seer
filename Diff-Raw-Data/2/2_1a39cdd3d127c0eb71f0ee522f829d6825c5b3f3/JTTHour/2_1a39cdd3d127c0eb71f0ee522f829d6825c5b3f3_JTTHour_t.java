 package com.aragaer.jtt;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class JTTHour implements Parcelable {
 	public static final int QUARTERS = 4;
 	public static final int PARTS = 10; // split quarter to that much parts
 
 	public static final int HOUR_PARTS = QUARTERS * PARTS;
 	private static final int DAY_QUARTERS = 12 * QUARTERS;
 
 	public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
 			"辰", "巳", "午", "未", "申" };
 
 	public boolean isNight;
 	public int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
 	public int quarter; // 0 to 3
 	public int quarter_parts; // 0 to PARTS
 
 	public JTTHour(int num) {
 		this(num, QUARTERS / 2, 0);
 	}
 
 	public JTTHour(int n, int q, int f) {
 		this.setTo(n, q, f);
 	}
 
 	// Instead of reallocation, reuse existing object
 	public void setTo(int n, int q, int f) {
 		num = n;
 		isNight = n < 6;
 		quarter = q;
 		quarter_parts = f;
 	}
 
 	public static final Parcelable.Creator<JTTHour> CREATOR = new Parcelable.Creator<JTTHour>() {
 		public JTTHour createFromParcel(Parcel in) {
 			return new JTTHour(in);
 		}
 
 		public JTTHour[] newArray(int size) {
 			return new JTTHour[size];
 		}
 	};
 
 	private JTTHour(Parcel in) {
 		unwrap(in.readInt(), this);
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(wrap());
 	}
 
 	/*
 	 * Wrapped value is a single integer denoting the JTT
 	 * Wrapping:
 	 * Wrapped value is a number of hour parts passed since last sunset
 	 * To calculate it we use SHIFTED_QUARTER
 	 * SHIFTED_QUARTER is number of quarters passed since last sunset
 	 * It is "shifted" because at sunset quarter number is QUARTERS/2
 	 * SHIFTED_QUARTER = (HOUR * QUARTERS + QUARTER + QUARTER_SHIFT) % DAY_QUARTERS
 	 * WRAPPED = SHIFTED_QUARTER * PARTS + PART
 	 *
 	 * Unwrapping:
 	 * PART = WRAPPED % PARTS
 	 * SHIFTED_QUARTER = WRAPPED / PARTS
 	 * QUARTER = (SHIFTED_QUARTER + QUARTERS / 2) % QUARTERS
 	 * HOUR = (SHIFTED_QUARTER + QUARTERS / 2) / QUARTERS
 	 */
 
 	/* I'd do simple subtraction, but it can get bad near zero since % keeps sign */
 	private static final int QUARTER_SHIFT = DAY_QUARTERS - QUARTERS / 2;
 
 	public static int wrap(int n, int q, int p) {
 		final int shifted_q = (n * QUARTERS + q + QUARTER_SHIFT) % DAY_QUARTERS;
 		return shifted_q * PARTS + p;
 	}
 
 	/* wrap self */
 	public int wrap() {
 		return wrap(num, quarter, quarter_parts);
 	}
 
 	public static JTTHour unwrap(int wrapped) {
 		return unwrap(wrapped, null);
 	}
 
 	public static JTTHour unwrap(int wrapped, JTTHour reuse) {
 		if (reuse == null)
 			reuse = new JTTHour(0);
 		final int unshifted_q = wrapped / PARTS + QUARTERS / 2; // that is shifted_q + shift back
		reuse.setTo(unshifted_q / QUARTERS % 12, unshifted_q % QUARTERS, wrapped % PARTS);
 		return reuse;
 	}
 }
