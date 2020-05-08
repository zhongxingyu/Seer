 package com.ii2d.genthemall;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 
 public class GeneratorFactory {
 
 	public static Generator create(String templateText, OutputStream out,
 			Map<?, ?> binding) throws IOException {
 		return create(templateText, new OutputStreamWriter(out), binding);
 	}
 
 	public static Generator create(InputStream templateText, OutputStream out,
 			Map<?, ?> binding) throws IOException {
 		return create(IOUtils.toString(templateText), new OutputStreamWriter(
 				out), binding);
 	}
 
 	public static Generator create(String templateText, Writer out,
 			Map<?, ?> binding) {
 		Generator g = new Generator();
 		g.setBinding(binding);
 		g.setOut(out);
 		g.setTemplateText(templateText);
 		return g;
 	}
 
 }
