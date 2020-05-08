 /**
  * 
  */
 package org.gololang.gldt.core.services;
 
 import javax.inject.Singleton;
 
 import org.eclipse.xtext.common.services.DefaultTerminalConverters;
 import org.eclipse.xtext.conversion.IValueConverter;
 import org.eclipse.xtext.conversion.ValueConverter;
 import org.eclipse.xtext.conversion.ValueConverterException;
 import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
 import org.eclipse.xtext.nodemodel.INode;
 
 /**
  *
  */
 @Singleton
 public class GoloTerminalConverter extends DefaultTerminalConverters {
 	
 	
 	private class LongConverter extends AbstractLexerBasedConverter<Long> {
 
 		private static final String LONG_SUFFIX = "_L";
 		
 		public Long toValue(String string, INode node)
 				throws ValueConverterException {
 			if (string.endsWith(LONG_SUFFIX)) {
 				string = string.substring(0, string.length() - LONG_SUFFIX.length());
 			}
 			return Long.parseLong(string);
 		}
 
 		@Override
 		protected String toEscapedString(Long value) {
 			return value.toString() + LONG_SUFFIX;
 		}
 	}
 	
 	private final IValueConverter<Long> LONG_NUMBER = new LongConverter();
 	
 	@ValueConverter(rule = "LONG_NUMBER")
 	public IValueConverter<Long> LONG_NUMBER() {
 		return LONG_NUMBER;
 	}
 	
 	private class DoubleConverter extends AbstractLexerBasedConverter<Double> {
 
 		public Double toValue(String string, INode node)
 				throws ValueConverterException {
 			return Double.parseDouble(string);
 		}
 	}
 	
 	private final IValueConverter<Double> FLOATING_NUMBER = new DoubleConverter();
 	
 	@ValueConverter(rule = "FLOATING_NUMBER")
 	public IValueConverter<Double> FLOATING_NUMBER() {
 		return FLOATING_NUMBER;
 	}
 	
 	private class FloatConverter extends AbstractLexerBasedConverter<Float> {
 
 		public Float toValue(String string, INode node)
 				throws ValueConverterException {
 			return Float.parseFloat(string);
 		}
 	}
 	
 	private final IValueConverter<Float> FLOAT = new FloatConverter();
 	
 	@ValueConverter(rule = "FLOAT")
 	public IValueConverter<Float> FLOAT() {
 		return FLOAT;
 	}
 }
