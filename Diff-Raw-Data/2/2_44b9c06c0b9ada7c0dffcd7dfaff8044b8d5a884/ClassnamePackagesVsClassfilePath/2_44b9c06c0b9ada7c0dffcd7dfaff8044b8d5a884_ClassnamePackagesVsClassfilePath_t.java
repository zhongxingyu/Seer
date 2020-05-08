 package net.ayld.facade.dependency.matcher.condition.impl;
 
 import java.io.File;
 import java.util.List;
 
 import com.google.common.base.Joiner;
 
 import net.ayld.facade.dependency.matcher.condition.MatchingCondition;
 import net.ayld.facade.model.ClassFile;
 import net.ayld.facade.model.ClassName;
 import net.ayld.facade.util.Tokenizer;
 
 public class ClassnamePackagesVsClassfilePath implements MatchingCondition{
 
 	@Override
 	public boolean satisfied(ClassName className, ClassFile classFile) {
 		final List<String> classPackagesAndName = Tokenizer.delimiter(".").tokenize(className.toString()).tokens();
 		final List<String> classPackagesNoName = classPackagesAndName.subList(0, classPackagesAndName.size() - 1);
 		
 		final String classPackages = Joiner.on("").join(classPackagesNoName);
 		final String classfileFullPath = classFile.toString();
 		
 		// FIXME this is incorrect as it would match
 		//
		// com.something.Bad
 		// to
 		// /home/user/extracted.jar/org/wrong/com/something/Bad.class
 		//
 		return classfileFullPath.replaceAll(File.separator, "").contains(classPackages);
 	}
 }
