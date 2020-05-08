 package at.fundev.oobe;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import at.fundev.oobe.model.Metadata;
 import at.fundev.oobe.model.PostEntry;
 import at.fundev.oobe.utils.EncodingDetector;
 
 public class TemplatablePackager extends Task implements Constants {
 	private String input;
 	
 	private String template;
 	
 	private String outputDir;
 	
 	private String metaDataFile = META_DATA_JSON_FILE;
 	
 	private <T> void process() throws FileNotFoundException, IOException {
 		if(getInput() == null || getTemplate() == null) {
 			return;
 		}
 		
 		Metadata md = Metadata.fromFile(metaDataFile);
 		
 		File inputFile = new File(getInput());
 		
 		if(inputFile.isDirectory()) {
 			File[] inputFiles = inputFile.listFiles(new FileFilter() {
 				@Override
 				public boolean accept(File file) {
 					return file.getName().endsWith(".html");
 				}
 			});
 			
 			for(File htmlFile : inputFiles) {
 				processInputFile(htmlFile, md);
 			}
 		} else {
 			processInputFile(inputFile, md);			
 		}
 	}
 	
 	private void processInputFile(File inputFile, Metadata md) throws IOException {
 		File templateFile = new File(getTemplate());
 		
 		if(!inputFile.exists()) {
 			throw new FileNotFoundException(String.format("The input file %s doesn't exist.", inputFile.getName()));
 		}
 		
 		if(!templateFile.exists()) {
 			throw new FileNotFoundException(String.format("The template file %s doesn't exist.", templateFile.getName()));
 		}
 		
 		String charset = EncodingDetector.of(inputFile).getEncoding();
 		charset = charset != null ? charset : EncodingDetector.DEFAULT_ENCODING; // default to UTF-8 and hope for the best
 		
 		Document inputDoc = Jsoup.parse(inputFile, charset);
 		Map<String, String> articleData = new HashMap<String, String>();
 		
 		Element contentElem = inputDoc.select("body").first();
 
 		System.out.println(contentElem.text());
 		
 		for(Element link : contentElem.select("img")) {
 			link.attr("src", "img/" + link.attr("src"));
 		}
 		
 		Element headerElem = contentElem.select(".maketitle").first();
 		Element titleElem = headerElem.select(".titleHead").first();
 		
		Element authorElem = headerElem.select(".author").first();
 		
 		Element dateElem = headerElem.select(".date").first();
 		
 		headerElem.remove();
 		
 		addIfNotNull(titleElem, TITLE_NAME, articleData);
 		addIfNotNull(authorElem, AUTHOR_NAME, articleData);
 		addIfNotNull(dateElem, DATE_NAME, articleData);
 		
 		fillOutTemplate(contentElem, articleData, inputFile, md);		
 	}
 	 
 	private String sanitizeSpecialChars(String input) {
 		
 		return (input == null) ? null : input.replace("ä", "&auml;")
 												.replace("Ä", "&Auml;")
 												.replace("ö", "&ouml;")
 												.replace("Ö", "&Ouml;")
 												.replace("ü", "&uuml;")
 												.replace("Ü", "&Uuml;")
 												.replace("ß", "&szlig;");
 	}
 	
 	private void addMetaData(Map<String, String> articleData, Metadata data, String path) throws IOException {
 		PostEntry entry = new PostEntry();
 		entry.setName(articleData.get(TITLE_NAME));
 		entry.setDate(articleData.get(DATE_NAME));
 		entry.setAuthor(articleData.get(AUTHOR_NAME));
 		entry.setFilePath(path);
 	
 		int index = data.getEntries() != null ? data.getEntries().indexOf(entry) : -1;
 
 		if(index != -1) {
 			data.getEntries().set(index, entry);
 		} else {
 			data.getEntries().add(entry);
 		}
 		
 		data.persist();
 	}
 	
 	private void addIfNotNull(Element value, String key, Map<String, String> parsedItems) {
 		if(value == null) {
 			return;
 		}
 		
 		parsedItems.put(key, value.text());
 	}
 	
 	private void fillOutTemplate(Element content, Map<String, String> articleData, File inputFile, Metadata md) throws IOException {
 		String fileName = inputFile.getName();
 		File outputFile = new File(getOutputDir(), fileName);
 	
 		addMetaData(articleData, md, outputFile.getAbsolutePath());
 		
 		if(content != null) {
 			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), EncodingDetector.DEFAULT_ENCODING));
 			String contentHtml = getTemplateText();
 			
 			for(String key : articleData.keySet()) {
 				contentHtml = contentHtml.replace(key, articleData.get(key));
 			}
 			
 			contentHtml = contentHtml.replace(CONTENT_NAME, sanitizeSpecialChars(content.html()));
 			
 			writer.write(contentHtml);
 			writer.flush();
 			writer.close();
 			
 			System.out.println("Written to output File: " + outputFile);
 		} else {
 			throw new IOException("No content element found in template file.");
 		}
 	}
 	
 	private String getTemplateText() throws IOException {
 		
 		File templateFile = new File(template);
 		String charset = EncodingDetector.of(templateFile).getEncoding();
 		charset = (charset == null) ? EncodingDetector.DEFAULT_ENCODING : charset;
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(templateFile), charset));
 		StringBuffer buf = new StringBuffer();
 		String cur = null;
 		
 		while((cur = reader.readLine()) != null) {
 			buf.append(cur);
 		}
 		
 		reader.close();
 		return buf.toString();
 	}
 	
 	public static void main(String[] args) {
 		if(args.length < 3) {
 			System.err.println("Usage: TemplatablePackager <input> <template> <outputDir>");
 			System.exit(1);
 		}
 		
 		String input = args[0];
 		String template = args[1];
 		String outputDir = args[2];
 		
 		TemplatablePackager templatePackager = new TemplatablePackager();
 		templatePackager.setInput(input);
 		templatePackager.setTemplate(template);
 		templatePackager.setOutputDir(outputDir);
 		
 		try {
 			templatePackager.process();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 	}
 	
 	@Override
 	public void execute() throws BuildException {
 		try {
 			process();
 		} catch(IOException e) {
 			throw new BuildException(e);
 		}
 	}
 
 	public String getInput() {
 		return input;
 	}
 
 	public void setInput(String input) {
 		this.input = input;
 	}
 
 	public String getTemplate() {
 		return template;
 	}
 
 	public void setTemplate(String template) {
 		this.template = template;
 	}
 
 	public String getOutputDir() {
 		return outputDir;
 	}
 
 	public void setOutputDir(String outputDir) {
 		this.outputDir = outputDir;
 	}
 }
