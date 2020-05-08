 package com.ii2d.genthemall.util;
 
 import static com.ii2d.dbase.util.DStringUtils.replaceAll;
 import groovy.util.ConfigObject;
 
 import java.io.File;
 import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.codehaus.groovy.control.CompilationFailedException;
 
 import com.ii2d.dbase.util.DNameUtils;
 import com.ii2d.genthemall.Generator;
 import com.ii2d.genthemall.GeneratorFactory;
 import com.ii2d.genthemall.template.Template;
 
 public class GeneratorUtils {
 
 	/**
 	 * @see #generate(Template, ConfigObject, String)
 	 */
 	public static void generate(Template t, ConfigObject bindingData)
 			throws FileNotFoundException, IOException,
 			CompilationFailedException, ClassNotFoundException {
 		generate(t, bindingData, t.getPath());
 	}
 	
 	/**
 	 * 如果修改修改最终生成路径可以使用这个方法
 	 * @author Doni
 	 * @since 2012-9-28
 	 * @param t
 	 * @param bindingData
 	 * @param destPath
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws CompilationFailedException
 	 * @throws ClassNotFoundException
 	 */
 	public static void generate(Template t, ConfigObject bindingData, String destPath) throws FileNotFoundException, IOException, CompilationFailedException, ClassNotFoundException {
 		String path = replaceAll(destPath, DNameUtils.toReplaceMap(bindingData));
 		File file = new File(path);
 		if(!t.isOverridable() && file.exists()) {
 			// return, If template cannot override.
 			return;
 		}
 		FileUtils.touch(file);
		OutputStream out = new FileOutputStream(file);
		Generator g = GeneratorFactory.create(t.getTemplateText(), out, bindingData);
 		g.generate();
		out.close();
 	}
 }
