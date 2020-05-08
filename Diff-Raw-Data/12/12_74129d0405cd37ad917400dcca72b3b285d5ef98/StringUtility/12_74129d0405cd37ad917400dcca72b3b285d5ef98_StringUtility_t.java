 package jp.ac.osaka_u.ist.sdl.mpanalyzer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.CLineLexer;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.JavaLineLexer;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.LineLexer;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STATEMENT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;
 import yoshikihigo.commentremover.CommentRemover;
 
 public class StringUtility {
 
 	public static List<String> removeLineHeader(final List<String> beforeText)
 			throws IOException {
 		final List<String> afterText = new ArrayList<String>();
 		for (final String line : beforeText) {
 			afterText.add(removeLineHeader(line));
 		}
 		return afterText;
 	}
 
 	public static String removeLineHeader(final String line) {
 		if (line.startsWith("+") || line.startsWith("-")) {
 			return line.substring(1);
 		} else {
 			return line;
 		}
 	}
 
 	public static List<String> addLineHeader(final List<String> beforeText,
 			final String prefix) throws IOException {
 		final List<String> afterText = new ArrayList<String>();
 		for (final String line : beforeText) {
 			afterText.add(prefix + line);
 		}
 		return afterText;
 	}
 
 	public static String addLineHeader(final String line, final String prefix) {
 		final StringBuilder builder = new StringBuilder();
 		builder.append(prefix);
 		builder.append(line);
 		return builder.toString();
 	}
 
 	public static List<String> removeIndent(final List<String> beforeText)
 			throws IOException {
 		final List<String> afterText = new ArrayList<String>();
 		for (final String line : beforeText) {
 			afterText.add(removeIndent(line));
 		}
 		return afterText;
 	}
 
 	public static String removeIndent(final String line) {
 		int startIndex = 0;
 		for (int index = 0; index < line.length(); index++) {
 			if ((' ' != line.charAt(index)) && ('\t' != line.charAt(index))) {
 				startIndex = index;
 				break;
 			}
 		}
 		return line.substring(startIndex);
 	}
 
 	public static List<String> removeSpaceTab(final List<String> beforeText)
 			throws IOException {
 		final List<String> afterText = new ArrayList<String>();
 		for (final String line : beforeText) {
 			afterText.add(removeSpaceTab(line));
 		}
 		return afterText;
 	}
 
 	public static String removeSpaceTab(final String line) {
 		final StringBuilder newLine = new StringBuilder();
 		for (int index = 0; index < line.length(); index++) {
 			final char c = line.charAt(index);
 			if (' ' != c && '\t' != c) {
 				newLine.append(c);
 			}
 		}
 		return newLine.toString();
 	}
 
 	public static List<String> removeNewLine(final List<String> beforeText)
 			throws IOException {
 		final StringBuilder newLine = new StringBuilder();
 		for (final String line : beforeText) {
 			newLine.append(line);
 		}
 		final List<String> afterText = new ArrayList<String>();
 		afterText.add(newLine.toString());
 		return afterText;
 	}
 
 	public static List<String> trim(final List<String> beforeText)
 			throws IOException {
 		final List<String> afterText = new ArrayList<String>();
 		for (final String line : beforeText) {
 			boolean blankLine = true;
 			for (int index = 0; index < line.length(); index++) {
 				final char c = line.charAt(index);
 				if (' ' != c && '\t' != c) {
 					blankLine = false;
 					break;
 				}
 			}
 			if (!blankLine) {
 				afterText.add(line);
 			}
 		}
 		return afterText;
 	}
 
 	public static List<Statement> splitToStatements(final String text) {
 		final List<Statement> statements = new ArrayList<Statement>();
 		final String[] lines = text.split(System.getProperty("line.separator"));
 		for (final String line : lines) {
 			final List<Token> tokens = new ArrayList<Token>();
 			tokens.add(new STATEMENT(line));
 			final Statement statement = new Statement(tokens);
 			statements.add(statement);
 		}
 
 		// final List<Statement> statements = new ArrayList<Statement>();
 		// int startIndex = 0;
 		// int endIndex = 0;
 		// for (int index = 0; index < text.length(); index++) {
 		// if (';' == text.charAt(index) || '{' == text.charAt(index)
 		// || '}' == text.charAt(index)) {
 		// endIndex = index + 1;
 		// final String subText = text.substring(startIndex, endIndex);
 		// final List<Token> tokens = new ArrayList<Token>();
 		// tokens.add(new Token(subText, TokenType.STATEMENT, 0));
 		// final Statement statement = new Statement(tokens);
 		// statements.add(statement);
 		// }
 		// }
 		return statements;
 	}
 
 	public static String[] splitToLines(final String text) {
 		if (null == text) {
 			return new String[0];
 		}
 		return text.split(System.getProperty("line.separator"));
 	}
 
 	public static List<Statement> splitToStatements(final String text,
 			final String language) {
		
		if(text.isEmpty()){
			return new ArrayList<Statement>();
		}

		if(text.startsWith("-")){
			return new ArrayList<Statement>();
		}
		
 		final String[] args = new String[8];
 		args[0] = "-l";
 		args[1] = language;
 		args[2] = "-i";
		args[3] = text;
 		args[4] = "-q";
 		args[5] = "-a";
 		args[6] = "-d";
 		args[7] = "-e";
 		final CommentRemover remover = new CommentRemover();
 		remover.perform(args);
 		final String nonCommentText = remover.result;
 
 		final LineLexer lexer = language.equalsIgnoreCase("java") ? new JavaLineLexer()
 				: new CLineLexer();
 		final List<Token> tokens = lexer.lexFile(nonCommentText);
 		final List<Statement> statements = Statement.getStatements(tokens);
 
 		return statements;
 	}
 
 	public static int getLOC(final String text) {
 		return splitToLines(text).length;
 	}
 
 	public static boolean isImport(final List<String> text) throws IOException {
 		for (final String line : text) {
 			if (!line.startsWith("import ") && !line.startsWith("package ")) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean isInclude(final List<String> text) throws IOException {
 		for (final String line : text) {
 			if (!line.startsWith("#include")) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean isJavaFile(final String path) {
 		return path.endsWith(".java");
 	}
 
 	public static boolean isCFile(final String path) {
 		return path.endsWith(".c") || path.endsWith(".cc")
 				|| path.endsWith(".cpp") || path.endsWith(".cxx");
 	}
 
 	public static String convertListToString(final List<String> list) {
 		final StringBuilder builder = new StringBuilder();
 		for (final String line : list) {
 			builder.append(line);
 			builder.append(System.getProperty("line.separator"));
 		}
 		return builder.toString();
 	}
 
 	public static List<String> convertStringToList(final String text)
 			throws IOException {
 
 		final List<String> list = new ArrayList<String>();
 		final BufferedReader reader = new BufferedReader(new StringReader(text));
 
 		while (true) {
 			final String line = reader.readLine();
 			if (null == line) {
 				break;
 			}
 			list.add(line);
 		}
 
 		reader.close();
 		return list;
 	}
 
 	public static String getSQLITELiteral(final String text) {
 		final StringBuilder builder = new StringBuilder();
 		builder.append("\'");
 		for (int index = 0; index < text.length(); index++) {
 			final char c = text.charAt(index);
 			if ('\'' == c) {
 				builder.append('\'');
 			}
 			builder.append(c);
 		}
 
 		builder.append("\'");
 		return builder.toString();
 	}
 
 	public static String getFilePath(final String diffHeaderLine) {
 		if (diffHeaderLine.startsWith("Index: ")) {
 			return diffHeaderLine.substring("Index: ".length());
 		} else {
 			return null;
 		}
 	}
 
 	public static String getDateString(final Date date) {
 		final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		return df.format(date);
 	}
 
 	public static Date getDateObject(final String date) {
 		try {
 			final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			return df.parse(date);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String removeTime(final String date) {
 		return date.indexOf(' ') > 0 ? date.substring(0, date.indexOf(' '))
 				: date;
 	}
 }
