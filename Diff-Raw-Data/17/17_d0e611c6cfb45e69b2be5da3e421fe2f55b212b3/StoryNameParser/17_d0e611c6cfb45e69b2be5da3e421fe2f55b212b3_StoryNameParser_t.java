 package br.materdei.bdd.jbehave;
 
 import java.io.File;
 
 import br.materdei.bdd.model.ThreadLocalModel;
import br.materdei.bdd.util.FileUtil;
import java.util.regex.Pattern;
 
 public final class StoryNameParser {
 
 	private StoryNameParser() {
 		super();
 	}
 	
 	public static String parse(String storyPath) {
		storyPath = FileUtil.configPathSeparator(storyPath);
		String name = storyPath.substring(storyPath.lastIndexOf(File.separator));
		if (!name.equals(name.toLowerCase())) {
 			throw new RuntimeException("Formato de nome de arquivo inválido. O nome do arquivo deve ser todo minúsculo e separado com underline '_'.");
 		}
 		
 		File root = new File("");
		String seed = FileUtil.configPathSeparator(ThreadLocalModel.getJBehaveModel().getStoriesPath() + "/");
 		
		storyPath = storyPath.replaceFirst(Pattern.quote(root.getAbsolutePath() + File.separator), "");
		storyPath = storyPath.replaceFirst(Pattern.quote(seed), "");
		storyPath = storyPath.replaceAll(Pattern.quote(File.separator), ".");
 		storyPath = storyPath.replaceAll("(.story|.estoria)", "");		
 		
 		return convertToCamelCase(storyPath) + "Test";
 	}
 	
 	public static String convertToCamelCase(String text) {
 		String prefix = text.substring(0, text.lastIndexOf(".") + 1);
 		text = text.substring(text.lastIndexOf(".") + 1);
 		
 		String[] tokens = text.split("_");
 		StringBuilder builder = new StringBuilder();
 		
 		for (String token : tokens) {
 			token = String.valueOf(token.charAt(0)).toUpperCase() + token.substring(1);
 			builder.append(token);
 		}
 		
 		return prefix + builder.toString();
 	}
 }
