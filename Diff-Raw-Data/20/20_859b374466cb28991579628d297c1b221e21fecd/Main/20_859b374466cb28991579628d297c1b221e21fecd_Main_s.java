 package org.laboumerde.pureunicodeconversion;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.laboumerde.pureunicodeconversion.converters.DummyConverter;
 import org.laboumerde.pureunicodeconversion.converters.UnicodeToNativeUTF8;
 import org.laboumerde.pureunicodeconversion.interfaces.Converter;
 
 /**
  * @author laboumerde
  * Main access class
  */
 public class Main implements Constants {
 
 	public static void main(String[] args) {
 		if(args.length >= 3 && StringUtils.isNotEmpty(args[0]) && StringUtils.isNotEmpty(args[1])){
 			String conversionType = args[0];
 			String inputFile = args[1];
 			String outputFile = args[2];
 
 			Converter converter = null;
 			
 			//converter instanciation :
 			switch (conversionType) {
 			case CONV_UNICODE_CODES_TO_NATIVE_UTF8:
 				converter = new UnicodeToNativeUTF8();
 				break;
 			case CONV_DUMMY:
 				converter = new DummyConverter();
 				break;
 			default:
 				// for the moment default will be unicode codes to UTF8
 				converter = new UnicodeToNativeUTF8();
 				break;
 			}
 			
 			try {
 				Path path = Paths.get(inputFile);
 				
 				// FIXME - change code to add possibilite to change input
				List<String> inputLines = Files.readAllLines(path,  converter.getDefaultInputCharster());
 				String str = "";
 				System.out.println("Unicode to Native Conversion Starts...");
 				for (String string : inputLines) {
 					str +=  converter.convert(string) + "\n";
 				}
 				
 				//output !
 				FileOutputStream fos = new FileOutputStream(outputFile);
 				// FIXME - change code to add possibilite to change input
				Writer out = new OutputStreamWriter(fos, converter.getDefaultOutputCharset());
 				out.write(str);
 				out.close();
 				System.out.println("Unicode to Native Conversion Successful!");
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			System.err.println("Wrong parameter number. It should be : CONVERSION_TYPE inputFilePath outputFilePath");
 		}
 	}
 }
