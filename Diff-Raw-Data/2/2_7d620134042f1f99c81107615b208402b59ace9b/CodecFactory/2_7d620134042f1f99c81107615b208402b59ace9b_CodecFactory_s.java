 package com.monits.packer;
 
 import java.lang.reflect.Field;
 
 import com.monits.packer.annotation.Unsigned;
 import com.monits.packer.annotation.UseCodec;
 import com.monits.packer.codec.Codec;
 import com.monits.packer.codec.ObjectCodec;
 import com.monits.packer.codec.UnsignedByteCodec;
 import com.monits.packer.codec.UnsignedIntCodec;
 import com.monits.packer.codec.UnsignedShortCodec;
 
 public class CodecFactory {
 	
 	public static <E> Codec<E> get(Class <E> clz) {
 		return new ObjectCodec<E>(clz);
 	}
 	
 	public static Codec<?> get(Field field) {
 		
 		boolean unsigned = field.isAnnotationPresent(Unsigned.class);
 
 		Class<?> type = field.getType();
 		if (field.isAnnotationPresent(UseCodec.class)) {
 			
 			UseCodec ann = field.getAnnotation(UseCodec.class);
 			try {
 				return (Codec<?>) ann.value().newInstance();
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			}
		} else if (type.equals(Long.class) || type.equals(long.class)) {
 			if (unsigned) {
 				return new UnsignedIntCodec();
 			}
 		} else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
 			if (unsigned) {
 				return new UnsignedShortCodec();
 			}
 		} else if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
 			if (unsigned) {
 				return new UnsignedByteCodec();
 			}
 		} else {
 			return new ObjectCodec(field.getType());
 		}
 		
 		return null;
 	}
 
 }
