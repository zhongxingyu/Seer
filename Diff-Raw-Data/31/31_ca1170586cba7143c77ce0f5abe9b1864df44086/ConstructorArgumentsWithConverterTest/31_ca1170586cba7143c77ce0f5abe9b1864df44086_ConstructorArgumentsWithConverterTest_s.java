 package com.xebia.xoc;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 import com.xebia.xoc.config.ClassMapperConfig;
import com.xebia.xoc.conversion.ConversionException;
import com.xebia.xoc.conversion.Converter;
 
 public class ConstructorArgumentsWithConverterTest {
 	
 	public static class Source {
 	
 		private int a;
 		private String s;
 		
 		public int getA() {
 			return a;
 		}
 		
 		public void setA(int a) {
 			this.a = a;
 		}
 
 		public String getS() {
 			return s;
 		}
 
 		public void setS(String s) {
 			this.s = s;
 		}
 		
 		
 	}
 	
 	public static class Target {
 		
 		private final int b;
 		private final Integer d;
 		
 		public Target(int b, Integer d) {
 			this.b = b;
 			this.d = d;
 		}
 
 		public int getB() {
 			return b;
 		}
 		
 		public Integer getD() {
 			return d;
 		}
 
 	}
 
 	@Test
 	public void shouldCreateMapper() {
 		ClassMapperConfig config = new ClassMapperConfig()//
 				.constructorArg(0).from("a").add()//
 				.constructorArg(1).from("s").add();
 		ClassMapper<Source, Target> mapper = new ConfigurableMapper().withMapper(config, Source.class, Target.class);
 		assertThat(mapper, is(notNullValue()));
 		Source source = new Source();
 		source.setA(12);
 		source.setS("13");
 		Target target = mapper.map(source);
 		assertThat(target, is(notNullValue()));
 		assertThat(target.getB(), is(source.getA()));
 		assertThat(target.getD(), is(Integer.parseInt(source.getS())));
 	}
 	
	private static class MyMapper implements ClassMapper<Object, Target> {
		
		public Converter arg0Converter;

		@Override
		public Target map(Object source) {
			try {
				return new Target(((Source) source).getA(), (Integer) arg0Converter.convert(((Source) source).getS()));
			} catch (ConversionException e) {
				return null;
			}
		}
		
	}
	
 }
