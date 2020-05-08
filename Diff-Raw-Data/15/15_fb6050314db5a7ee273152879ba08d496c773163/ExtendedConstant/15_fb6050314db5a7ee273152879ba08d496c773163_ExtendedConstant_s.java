 package jscl.math.function;
 
 import jscl.AngleUnits;
 import jscl.JsclMathEngine;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.solovyev.common.definitions.IBuilder;
 import org.solovyev.common.math.MathEntity;
 
 /**
  * User: serso
  * Date: 11/7/11
  * Time: 12:06 PM
  */
 public class ExtendedConstant implements Comparable<ExtendedConstant>, IConstant {
 
 	@NotNull
 	private Constant constant;
 
 	@Nullable
 	private String value;
 
 	@Nullable
 	private String javaString;
 
 	@Nullable
 	private String description;
 
 	public static final class Builder implements IBuilder<ExtendedConstant> {
 		@NotNull
 		private Constant constant;
 
 		@Nullable
 		private String value;
 
 		@Nullable
 		private String javaString;
 
 		@Nullable
 		private String description;
 
 		public Builder(@NotNull Constant constant, @Nullable Double value) {
 			this(constant, value == null ? null : String.valueOf(value));
 		}
 
 		public Builder(@NotNull Constant constant, @Nullable String value) {
 			this.constant = constant;
 			this.value = value;
 		}
 
 		public Builder setJavaString(@Nullable String javaString) {
 			this.javaString = javaString;
 			return this;
 		}
 
 		public Builder setDescription(@Nullable String description) {
 			this.description = description;
 			return this;
 		}
 
 		@NotNull
 		@Override
 		public ExtendedConstant create() {
 			final ExtendedConstant result = new ExtendedConstant();
 
 			result.constant = constant;
 			result.value = value;
 			result.javaString = javaString;
 			result.description = description;
 
 			return result;
 		}
 	}
 
 	ExtendedConstant() {
 	}
 
 	ExtendedConstant(@NotNull Constant constant,
 					 @Nullable Double value,
 					 @Nullable String javaString) {
 		this.constant = constant;
 		this.value = value == null ? null : String.valueOf(value);
 		this.javaString = javaString;
 	}
 
 	@NotNull
 	@Override
 	public String getName() {
 		return this.constant.getName();
 	}
 
 	@Override
 	public boolean isSystem() {
 		return this.constant.isSystem();
 	}
 
 	@NotNull
 	@Override
 	public Integer getId() {
 		return constant.getId();
 	}
 
 	@Override
 	public boolean isIdDefined() {
 		return constant.isIdDefined();
 	}
 
 	@Override
 	public void setId(@NotNull Integer id) {
 		constant.setId(id);
 	}
 
 	@Override
 	public void copy(@NotNull MathEntity that) {
 		this.constant.copy(that);
 
 		if (that instanceof IConstant) {
 			this.description = ((IConstant) that).getDescription();
 			this.value = ((IConstant) that).getValue();
 		}
 
 		if (that instanceof ExtendedConstant) {
 			this.javaString = ((ExtendedConstant) that).javaString;
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		ExtendedConstant that = (ExtendedConstant) o;
 
 		if (!constant.equals(that.constant)) return false;
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		return constant.hashCode();
 	}
 
 	@Override
 	@NotNull
 	public Constant getConstant() {
 		return constant;
 	}
 
 	@Override
 	@Nullable
 	public String getDescription() {
 		return description;
 	}
 
 	@Override
 	public boolean isDefined() {
 		return value != null;
 	}
 
 	@Override
 	@Nullable
 	public String getValue() {
		return getValue0();
 	}
 
 	@Override
 	public Double getDoubleValue() {
 		Double result = null;
 		if (value != null) {
 			try {
 				result = getDoubleValue0();
 			} catch (NumberFormatException e) {
 				// do nothing - string is not a double
 			}
 		}
 		return result;
 	}
 
 	private Double getDoubleValue0() {
 		Double result;
 
 		if (Constant.PI_CONST.getName().equals(getName())) {
 			result = AngleUnits.rad.transform(JsclMathEngine.instance.getDefaultAngleUnits(), Double.valueOf(value));
 		} else {
 			result = Double.valueOf(value);
 		}
 
 		return result;
 	}
 
	private String getValue0() {
		String result;

		if (Constant.PI_CONST.getName().equals(getName())) {
			result = String.valueOf(AngleUnits.rad.transform(JsclMathEngine.instance.getDefaultAngleUnits(), Double.valueOf(value)));
		} else {
			result = value;
		}

		return result;
	}

 	@Override
 	@NotNull
 	public String toJava() {
 		if (javaString != null) {
 			return javaString;
 		} else if (value != null) {
 			return String.valueOf(value);
 		} else {
 			return constant.getName();
 		}
 	}
 
 	@Override
 	public int compareTo(ExtendedConstant o) {
 		return this.constant.compareTo(o.getConstant());
 	}
 }
