 package org.laboumerde.pureunicodeconversion.converters;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 
 import org.apache.commons.lang3.StringUtils;
 import org.laboumerde.pureunicodeconversion.Constants;
 import org.laboumerde.pureunicodeconversion.interfaces.Converter;
 
 /**
  * @author laboumerde
  * Unicode codes  to native UTF8 converter.
  */
 public class UnicodeToNativeUTF8 implements Converter,Constants {
 	
 	@Override
 	public  String convert(String text){
 		for (int i = 0; i < UTF_CODE_TO_NATIVE_UTF8_CORRESPONDANCE_TABLE.length; i++) {
 			text = StringUtils.replace(text, UTF_CODE_TO_NATIVE_UTF8_CORRESPONDANCE_TABLE[i][1], UTF_CODE_TO_NATIVE_UTF8_CORRESPONDANCE_TABLE[i][0]);
 		}
 		return text;
 	}
 
 	@Override
 	public Charset getDefaultInputCharster() {
		return  StandardCharsets.US_ASCII;
 	}
 
 	@Override
 	public Charset getDefaultOutputCharset() {
 		return  StandardCharsets.UTF_8;
 	}
 }
