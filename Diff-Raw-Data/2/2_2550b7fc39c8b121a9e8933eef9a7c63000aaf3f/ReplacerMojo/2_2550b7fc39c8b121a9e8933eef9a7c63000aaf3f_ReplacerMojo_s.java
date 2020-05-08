 package com.google.code.maven_replacer_plugin;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import com.google.code.maven_replacer_plugin.file.FileUtils;
 import com.google.code.maven_replacer_plugin.include.FileSelector;
 
 
 /**
  * Goal replaces token with value inside file
  * 
  * @goal replace
  * 
  * @phase compile
  */
 public class ReplacerMojo extends AbstractMojo {
 	private final FileUtils fileUtils;
 	private final TokenReplacer tokenReplacer;
 	private final ReplacerFactory replacerFactory;
 	private final TokenValueMapFactory tokenValueMapFactory;
 	private final FileSelector fileSelector;
 	private final PatternFlagsFactory patternFlagsFactory;
 
 	/**
 	 * File to check and replace tokens
 	 * 
 	 * @parameter expression=""
 	 */
 	private String file;
 
 	/**
 	 * List of included files pattern in ant format. Cannot use with outputFile.
 	 * 
 	 * @parameter expression=""
 	 */
 	private List<String> includes;
 
 	/**
 	 * List of excluded files pattern in ant format. Cannot use with outputFile.
 	 * 
 	 * @parameter expression=""
 	 */
 	private List<String> excludes;
 
 	/**
 	 * Token
 	 * 
 	 * @parameter expression=""
 	 */
 	private String token;
 
 	/**
 	 * Token file
 	 * 
 	 * @parameter expression=""
 	 */
 	private String tokenFile;
 
 	/**
 	 * Ignore missing files
 	 * 
 	 * @parameter expression=""
 	 */
 	private boolean ignoreMissingFile;
 
 	/**
 	 * Value to replace token with
 	 * 
 	 * @parameter expression=""
 	 */
 	private String value;
 
 	/**
 	 * Value file to read value to replace token with
 	 * 
 	 * @parameter expression=""
 	 */
 	private String valueFile;
 
 	/**
 	 * Token uses regex
 	 * 
 	 * @parameter expression=""
 	 */
 	private boolean regex = true;
 
 	/**
 	 * Output to another file
 	 * 
 	 * @parameter expression=""
 	 */
 	private String outputFile;
 
 	/**
 	 * Map of tokens and respective values to replace with
 	 * 
 	 * @parameter expression=""
 	 */
 	private String tokenValueMap;
 	
 	/**
 	 * List of regex flags. 
	 * Must contain one of:
 	 * * CANON_EQ
 	 * * CASE_INSENSITIVE
 	 * * COMMENTS
 	 * * DOTALL
 	 * * LITERAL
 	 * * MULTILINE
 	 * * UNICODE_CASE
 	 * * UNIX_LINES
 	 * 
 	 * @parameter expression=""
 	 */
 	private List<String> regexFlags;
 
 	public ReplacerMojo() {
 		super();
 		this.fileUtils = new FileUtils();
 		this.tokenReplacer = new TokenReplacer();
 		this.replacerFactory = new ReplacerFactory(fileUtils, tokenReplacer);
 		this.tokenValueMapFactory = new TokenValueMapFactory(fileUtils);
 		this.fileSelector = new FileSelector();
 		this.patternFlagsFactory = new PatternFlagsFactory();
 	}
 
 	public ReplacerMojo(FileUtils fileUtils, TokenReplacer tokenReplacer,
 			ReplacerFactory replacerFactory, TokenValueMapFactory tokenValueMapFactory,
 			FileSelector fileSelector, PatternFlagsFactory patternFlagsFactory) {
 		super();
 		this.fileUtils = fileUtils;
 		this.tokenReplacer = tokenReplacer;
 		this.replacerFactory = replacerFactory;
 		this.tokenValueMapFactory = tokenValueMapFactory;
 		this.fileSelector = fileSelector;
 		this.patternFlagsFactory = patternFlagsFactory;
 	}
 
 	public void execute() throws MojoExecutionException {
 		try {
 			if (ignoreMissingFile && fileUtils.fileNotExists(file)) {
 				getLog().info("Ignoring missing file");
 				return;
 			}
 
 			Replacer replacer = replacerFactory.create();
 			List<ReplacerContext> contexts = getContexts();
 
 			if (includes == null || includes.isEmpty()) {
 				replaceContents(replacer, contexts, file, getOutputFile(file));
 				return;
 			}
 
 			for (String file : fileSelector.listIncludes(includes, excludes)) {
 				replaceContents(replacer, contexts, file, getOutputFile(file));
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void replaceContents(Replacer replacer, List<ReplacerContext> contexts,
 			String inputFile, String outputFile) throws IOException {
 		getLog().info("Replacing content in " + inputFile);
 		replacer.replace(contexts, regex, inputFile, getOutputFile(inputFile), 
 				patternFlagsFactory.buildFlags(regexFlags));
 	}
 
 	private List<ReplacerContext> getContexts() throws IOException {
 		if (tokenValueMap == null) {
 			ReplacerContext context = new ReplacerContext(fileUtils, token, value);
 			context.setTokenFile(tokenFile);
 			context.setValueFile(valueFile);
 			return Arrays.asList(context);
 		}
 		return tokenValueMapFactory.contextsForFile(tokenValueMap);
 	}
 
 	private String getOutputFile(String file) {
 		if (outputFile == null) {
 			return file;
 		}
 
 		getLog().info("Outputting to: " + outputFile);
 		if (fileUtils.fileNotExists(file)) {
 			fileUtils.ensureFolderStructureExists(outputFile);
 		}
 		return outputFile;
 	}
 
 	public void setRegex(boolean regex) {
 		this.regex = regex;
 	}
 
 	public void setFile(String file) {
 		this.file = file;
 	}
 
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public void setTokenFile(String tokenFile) {
 		this.tokenFile = tokenFile;
 	}
 
 	public void setValueFile(String valueFile) {
 		this.valueFile = valueFile;
 	}
 
 	public void setIgnoreMissingFile(boolean ignoreMissingFile) {
 		this.ignoreMissingFile = ignoreMissingFile;
 	}
 
 	public void setOutputFile(String outputFile) {
 		this.outputFile = outputFile;
 	}
 
 	public void setTokenValueMap(String tokenValueMap) {
 		this.tokenValueMap = tokenValueMap;
 	}
 }
