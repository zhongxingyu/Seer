 package descent.internal.compiler.parser;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.MathContext;
 
 
 public class real_t {
 	
 	// We'd like a math context of 80 bits, but...
 	private final static MathContext MATH_CONTEXT = MathContext.DECIMAL128;
 
 	public final static real_t ZERO = new real_t(BigDecimal.ZERO);
 	public final static real_t NAN = new real_t(Double.NaN);
 	public final static real_t POSITIVE_INFINITY = new real_t(Double.POSITIVE_INFINITY);
 	public final static real_t NEGATIVE_INFINITY = new real_t(Double.NEGATIVE_INFINITY);
 
 	public BigDecimal value;
 	public double nanOrInfinite;
 
 	public real_t(BigInteger value) {
 		this.value = new BigDecimal(value);
 	}
 
 	public real_t(integer_t value) {
 		this.value = new BigDecimal(value.bigIntegerValue());
 	}
 
 	public real_t(BigDecimal value) {
 		this.value = value;
 	}
 
 	public real_t(double doubleVal) {
 		if (Double.isInfinite(doubleVal) || Double.isNaN(doubleVal))
 			nanOrInfinite = doubleVal;
 		else
 			value = new BigDecimal(doubleVal);
 	}
 
 	public integer_t to_integer_t() {
 		// TODO consider nan?
 		if (value != null) { 
 			return new integer_t(value.toBigInteger());
 		} else {
 			return new integer_t((int) nanOrInfinite);
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof real_t)) {
 			return false;
 		}
 
 		real_t r = (real_t) obj;
 		if ((value != null) != (r.value != null)) {
 			return false;
 		}
 
 		if (value != null) {
 			// Read documentation of BigDecimal.equals: it compares
 			// also the scale :-(
 			return value.compareTo(r.value) == 0;
 		} else {
 			return (isNaN() && r.isNaN()) || 
 				(isPositiveInfinity() && r.isPositiveInfinity()) ||
 				(isNegativeInfinity() && r.isNegativeInfinity());
 		}
 	}
 
 	// FIXME Figure out the semantics with infinite and NaN values
 
 	public real_t negate() {
 		if (value != null) { 
 			return new real_t(value.negate(MATH_CONTEXT));
 		} else if (isNaN()) {
 			return NAN;
 		} else if (isPositiveInfinity()) {
 			return NEGATIVE_INFINITY;
 		} else {
 			return POSITIVE_INFINITY;
 		}
 	}
 
 	public real_t add(real_t other) {
 		if (value != null && other.value != null) {
 			return new real_t(value.add(other.value, MATH_CONTEXT));
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return NAN;
 			}
 			if (value != null) {
 				if (other.isPositiveInfinity()) {
 					return POSITIVE_INFINITY;
 				} else {
 					return NEGATIVE_INFINITY;
 				}
 			} else if (isPositiveInfinity()) {
 				if (other.value != null) {
 					return POSITIVE_INFINITY;
 				} else if (other.isPositiveInfinity()) {
 					return POSITIVE_INFINITY;
 				} else {
 					return NAN;
 				}
 			} else {
 				if (other.value != null) {
 					return NEGATIVE_INFINITY;
 				} else if (other.isNegativeInfinity()) {
 					return NEGATIVE_INFINITY;
 				} else {
 					return NAN;
 				}
 			}
 		}
 	}
 
 	public real_t subtract(real_t other) {
 		if (value != null && other.value != null) {
 			return new real_t(value.subtract(other.value, MATH_CONTEXT));
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return NAN;
 			}
 			if (value != null) {
 				if (other.isPositiveInfinity()) {
 					return NEGATIVE_INFINITY;
 				} else {
 					return POSITIVE_INFINITY;
 				}
 			} else if (isPositiveInfinity()) {
 				if (other.value != null) {
 					return POSITIVE_INFINITY;
 				} else if (other.isPositiveInfinity()) {
 					return NAN;
 				} else {
 					return POSITIVE_INFINITY;
 				}
 			} else {
 				if (other.value != null) {
 					return NEGATIVE_INFINITY;
 				} else if (other.isNegativeInfinity()) {
 					return NAN;
 				} else {
 					return NEGATIVE_INFINITY;
 				}
 			}
 		}
 	}
 	
 	// TODO multiply and divide: check 0.0 and -0.0
 
 	public real_t multiply(real_t other) {
 		if (value != null && other.value != null) {
 			return new real_t(value.multiply(other.value, MATH_CONTEXT));
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return NAN;
 			}
 			if (value != null) {
 				int comp = value.compareTo(BigDecimal.ZERO);
 				if (comp == 0) {
 					return NAN;
 				} else if (comp > 0) {
 					if (other.isPositiveInfinity()) {
 						return POSITIVE_INFINITY;
 					} else {
 						return NEGATIVE_INFINITY;
 					}
 				} else {
 					if (other.isPositiveInfinity()) {
 						return NEGATIVE_INFINITY;
 					} else {
 						return POSITIVE_INFINITY;
 					}
 				}
 			} else if (isPositiveInfinity()) {
 				if (other.value != null) {
 					int comp = other.value.compareTo(BigDecimal.ZERO);
 					if (comp == 0) {
 						return NAN;
 					} else if (comp > 0) {
 						return POSITIVE_INFINITY;
 					} else {
 						return NEGATIVE_INFINITY;
 					}
 				} else if (other.isPositiveInfinity()) {
 					return POSITIVE_INFINITY;
 				} else {
 					return NEGATIVE_INFINITY;
 				}
 			} else {
 				if (other.value != null) {
 					int comp = other.value.compareTo(BigDecimal.ZERO);
 					if (comp == 0) {
 						return NAN;
 					} else if (comp > 0) {
 						return NEGATIVE_INFINITY;
 					} else {
 						return POSITIVE_INFINITY;
 					}
 				} else if (other.isNegativeInfinity()) {
 					return POSITIVE_INFINITY;
 				} else {
 					return NEGATIVE_INFINITY;
 				}
 			}
 		}
 	}
 
 	public real_t divide(real_t other) {
 		if (value != null && other.value != null) {
 			if (other.value.compareTo(BigDecimal.ZERO) == 0) {
 				int cmp = value.compareTo(BigDecimal.ZERO);
 				if (cmp == 0) {
 					return NAN;
 				} else if (cmp > 0) {
 					return POSITIVE_INFINITY;
 				} else {
 					return NEGATIVE_INFINITY;
 				}
 			} else {
 				return new real_t(value.divide(other.value, MATH_CONTEXT));
 			}
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return NAN;
 			}
 			if (value != null) {
 				return ZERO;	
 			} else if (isPositiveInfinity()) {
 				if (other.value != null) {
 					int comp = other.value.compareTo(BigDecimal.ZERO);
 					if (comp == 0) {
 						return POSITIVE_INFINITY;
 					} else if (comp > 0) {
 						return POSITIVE_INFINITY;
 					} else {
 						return NEGATIVE_INFINITY;
 					}
 				} else {
 					return NAN;
 				}
 			} else {
 				if (other.value != null) {
 					int comp = other.value.compareTo(BigDecimal.ZERO);
 					if (comp == 0) {
 						return NEGATIVE_INFINITY;
 					} else if (comp > 0) {
 						return NEGATIVE_INFINITY;
 					} else {
 						return POSITIVE_INFINITY;
 					}
 				} else {
 					return NAN;
 				}
 			}
 		}
 	}
 
 	public real_t remainder(real_t other) {
 		if (value != null && other.value != null) {
 			return new real_t(value.remainder(other.value, MATH_CONTEXT));
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return NAN;
 			}
 			if (value != null) {
 				return this;	
 			} else {
 				return NAN;
 			}
 		}
 	}
 
 	public int compareTo(real_t other) {
 		if (value != null && other.value != null) {
 			return value.compareTo(other.value);
 		} else {
 			if (isNaN() || other.isNaN()) {
 				return -1;
 			}
 			if (value != null) {
 				if (other.isPositiveInfinity()) {
 					return -1;
 				} else {
 					return 1;
 				}
 			} else if (isPositiveInfinity()) {
 				if (other.value != null) {
 					return 1;
 				} else if (other.isPositiveInfinity()) {
 					return 0;
 				} else {
 					return 1;
 				}
 			} else {
 				if (other.value != null) {
 					return -1;
 				} else if (other.isNegativeInfinity()) {
 					return 0;
 				} else {
 					return -1;
 				}
 			}
 		}
 	}
 
 	public boolean isNaN() {
 		return value == null && Double.isNaN(nanOrInfinite);
 	}
 	
 	public boolean isInfinite() {
 		return value == null && Double.isInfinite(nanOrInfinite);
 	}
 	
 	public boolean isPositiveInfinity() {
 		return value == null && Double.isInfinite(nanOrInfinite) && nanOrInfinite > 0;
 	}
 	
 	public boolean isNegativeInfinity() {
 		return value == null && Double.isInfinite(nanOrInfinite) && nanOrInfinite < 0;
 	}
 	
 	public double floatValue() {
 		if (value != null) {
 			return value.floatValue();
 		} else {
 			return nanOrInfinite;
 		}
 	}
 	
 	public double doubleValue() {
 		if (value != null) {
 			return value.doubleValue();
 		} else {
 			return nanOrInfinite;
 		}
 	}
 	
 	public BigDecimal bigDecimalValue() {
 		return value;
 	}
 
 	@Override
 	public String toString() {
 		if (value != null) {
 			return value.toString();
 		} else if (isNaN()) {
 			return "nan";
 		} else {
 			return "infinity";
 		}
 	}
 
 	public real_t sin() {
 		double val = value.doubleValue();
 		return new real_t(Math.sin(val));
 	}
 	
 	public real_t cos() {
 		double val = value.doubleValue();
 		return new real_t(Math.cos(val));
 	}
 	
 	public real_t tan() {
 		double val = value.doubleValue();
 		return new real_t(Math.tan(val));
 	}
 	
 	public real_t sqrt() {
 		double val = value.doubleValue();
 		return new real_t(Math.sqrt(val));
 	}
 	
 	public real_t abs() {
 		double val = value.doubleValue();
 		return new real_t(Math.abs(val));
 	}
 
 	public static BigDecimal strtold(String string) {
 		String base = string;
 		
 		boolean isNegative = base.length() >= 1 && base.charAt(0) == '-';
 		if (isNegative) {
 			base = base.substring(1);
 		}
 		
 		boolean isHex = base.length() >= 2 &&
 			base.charAt(0) == '0' && (base.charAt(1) == 'x' || base.charAt(1) == 'X');
 		
 		if (isHex) {
 			int exponentIndex = base.indexOf('p');
 			if (exponentIndex == -1) {
 				exponentIndex = base.indexOf('P');
 			}
			if (exponentIndex != -1) {
				return new BigDecimal(Double.parseDouble(string));
			} else {
				return new BigDecimal(Double.parseDouble(string + "p0"));
 			}
 		} else {
 			return new BigDecimal(string);
 		}
 	}
 
 }
