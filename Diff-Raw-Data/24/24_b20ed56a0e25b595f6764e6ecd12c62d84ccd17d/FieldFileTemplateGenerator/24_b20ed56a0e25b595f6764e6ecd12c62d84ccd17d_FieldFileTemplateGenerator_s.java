 package com.golddigger.utils;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.golddigger.templates.GameTemplate;
 import com.golddigger.templates.generators.TemplateGenerator;
 
 public class FieldFileTemplateGenerator implements TemplateGenerator{
 
 	private File dir;
 
 	public void setDirectory(File directory){
 		this.dir = directory;
 	}
 
 	@Override
 	public GameTemplate[] generate() {
 		List<GameTemplate> templates = new ArrayList<GameTemplate>();
 		for (File file : dir.listFiles()){
 			try {
 				String text = load(file);
 				templates.add(LegacyTemplateParser.parse(text));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return templates.toArray(new GameTemplate[]{});
 	}
 	
 	private String load(File file) throws IOException{
 		System.out.println("loading: "+file.getCanonicalPath());
		String s;
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
 	}
 }
