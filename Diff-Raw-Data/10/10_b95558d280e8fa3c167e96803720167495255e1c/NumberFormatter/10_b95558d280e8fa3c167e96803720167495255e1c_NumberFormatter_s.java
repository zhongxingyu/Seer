 package nz.net.dnh.eve.web;
 
 import java.text.NumberFormat;
 
 public class NumberFormatter {
 	private final NumberFormat format;
 	private String nullValue;
 
 	public NumberFormatter(final NumberFormat format) {
 		this(format, "");
 	}
 
 	public NumberFormatter(final NumberFormat format, final String nullValue) {
 		this.format = format;
 		setNullValue(nullValue);
 	}
 
	public String format(final Double value) {
		return (value == null ? this.nullValue : this.format.format(value));
	}

	public String format(final Long value) {


		return (value == null ? this.nullValue : this.format.format(value));
	}

 	public String format(final Object value) {
 		return (value == null ? this.nullValue : this.format.format(value));
 	}
 
 	/**
 	 * @return the nullValue
 	 */
 	public String getNullValue() {
 		return this.nullValue;
 	}
 
 	/**
 	 * @param nullValue the nullValue to set
 	 */
 	public void setNullValue(final String nullValue) {
 		this.nullValue = nullValue;
 	}
 }
