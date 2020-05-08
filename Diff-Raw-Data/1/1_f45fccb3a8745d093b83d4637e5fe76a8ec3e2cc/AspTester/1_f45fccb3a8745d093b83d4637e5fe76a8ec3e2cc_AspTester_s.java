 package de.egore911.aspparser;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 
 public class AspTester {
 
 	private static final Pattern p = Pattern.compile("<!--#INCLUDE (FILE|VIRTUAL)=\"(.*)\"-->",
 			Pattern.CASE_INSENSITIVE);
 
 	private static String getFileContent(Path file, Map<String, Path> virtuals, Deque<String> inclusionStack) throws IOException {
 		StringBuilder buffer = new StringBuilder();
 		buffer.append("<!-- ");
 		for (String s : inclusionStack) {
 			buffer.append(s);
 			buffer.append(" -> ");
 		}
 		buffer.append(file.getFileName().toString());
 		inclusionStack.addLast(file.getFileName().toString());
 		buffer.append(" -->\n");
 
 		if (!Files.exists(file)) {
 			String desiredFile = file.getFileName().toString().toLowerCase();
 			DirectoryStream<Path> dir = Files.newDirectoryStream(file.toAbsolutePath().getParent());
 			for (Path f : dir) {
 				if (f.getFileName().toString().toLowerCase().equals(desiredFile)) {
 					file = f;
 					break;
 				}
 			}
 		}
 
 		try (BufferedReader reader = Files.newBufferedReader(file,
 				Charset.forName("ISO-8859-15"))) {
 			String line;
 			while ((line = reader.readLine()) != null) {
 				buffer.append(line).append('\n');
 			}
 			String content = buffer.toString();
 
 			Matcher matcher = p.matcher(content);
 			StringBuffer sb = new StringBuffer();
 			while (matcher.find()) {
 				String type = matcher.group(1);
 				Path includeFile;
 				if ("VIRTUAL".equalsIgnoreCase(type)) {
 					String fileToInclude = matcher.group(2);
 					includeFile = null;
 					for (Map.Entry<String, Path> x : virtuals.entrySet()) {
 						if (fileToInclude.startsWith(x.getKey())) {
 							String replace = fileToInclude.replace(x.getKey(), "");
 							if (replace.startsWith("/")) {
 								replace = replace.substring(1);
 							}
 							includeFile = x.getValue().resolve(replace);
 							break;
 						}
 					}
 					if (includeFile == null) {
 						throw new IllegalArgumentException("No valid virtual for " + fileToInclude);
 					}
 				} else {
 					String fileToInclude = matcher.group(2);
 					includeFile = file.resolveSibling(fileToInclude);
 				}
 				String fileContentToInclude = getFileContent(includeFile, virtuals, inclusionStack);
 				matcher.appendReplacement(sb, "");
 				sb.append(fileContentToInclude);
 			}
 			matcher.appendTail(sb);
 
 			sb.append("<!-- Done: ");
 			inclusionStack.removeLast();
 			for (String s : inclusionStack) {
 				sb.append(s);
 				sb.append(" -> ");
 			}
 			sb.append(file.getFileName().toString());
 			sb.append(" -->\n");
 			sb.append("<% %>"); // FIXME: remove this temporary workaround!
 
 			content = sb.toString();
 			return content;
 		}
 	}
 
 	public static void main(String[] args) throws IOException,
 			RecognitionException {
 
 		if (args.length < 1) {
 			System.err.println(AspTester.class.getSimpleName() + " usage <dir> (<virtuals>)");
 			System.err.println("	<dir>     the directory to verify");
 			System.err.println("	<virtual> a list of virtuals to resolve");
 			System.err.println("	          (e.g. '/lib1,/var/asp/lib1;/lib2,/var/asp/lib2");
 			System.exit(1);
 		}
 
 		String dir = args[0];
 
 		FileSystem fileSystem = FileSystems.getDefault();
 
 		final Map<String, Path> virtuals;
 		if (args.length > 1) {
 			virtuals = new HashMap<>();
 			String[] virtualsInput = args[1].split(";");
 			for (String virtualInput : virtualsInput) {
 				String[] v = virtualInput.split(",");
 				Path path = fileSystem.getPath(v[1]).toAbsolutePath();
 				if (!Files.exists(path)) {
 					throw new IllegalArgumentException("The path " + path + " could not be found");
 				}
 				if (virtuals.put(v[0], path) != null) {
 					throw new IllegalArgumentException("Virtual " + v[0] + " assigned multiple times");
 				}
 			}
 		} else {
 			virtuals = Collections.emptyMap();
 		}
 
 		Path path = fileSystem.getPath(dir).toAbsolutePath();
 		System.out.println("Analyzing all *.asp files below " + path);
 		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
 
 			@Override
 			public FileVisitResult visitFile(Path file,
 					BasicFileAttributes attrs) throws IOException {
 
 				if (file.toString().toLowerCase(Locale.ENGLISH)
 						.endsWith(".asp")) {
 
 					System.out.print(file);
 
 					String content = getFileContent(file, virtuals, new LinkedList<String>());
 
 					try (FileOutputStream fos = new FileOutputStream("tmp.asp")) {
 						fos.write(content.getBytes());
 					};
 
 					CharStream charStream = new ANTLRLowercaseStringStream(content);
 					AspLexer lexer = new AspLexer(charStream);
 					List<String> lexerErrorMessages = new ArrayList<>();
 					lexer.setErrorMessages(lexerErrorMessages);
 					TokenStream tokenStream = new CommonTokenStream(lexer);
 					AspParser parser = new AspParser(tokenStream);
 					List<String> parserErrorMessages = new ArrayList<>();
 					parser.setErrorMessages(parserErrorMessages);
 					try {
 						parser.file();
 					} catch (RecognitionException e) {
 						e.printStackTrace();
 					}
 					if (lexerErrorMessages.isEmpty() && parserErrorMessages.isEmpty()) {
 						System.out.println(" [ok]");
 					} else {
 						System.err.println(" [failed]");
 						for (String errorMessage : lexerErrorMessages) {
 							System.err.println(errorMessage);
 						}
 						for (String errorMessage : parserErrorMessages) {
 							System.err.println(errorMessage);
 						}
 						System.err.println(lexerErrorMessages.size() + " errors in the lexer, " + parserErrorMessages.size() + " in the parser");
 					}
 
 				}
 
 				return FileVisitResult.CONTINUE;
 			}
 
 		});
 
 	}
 
 }
