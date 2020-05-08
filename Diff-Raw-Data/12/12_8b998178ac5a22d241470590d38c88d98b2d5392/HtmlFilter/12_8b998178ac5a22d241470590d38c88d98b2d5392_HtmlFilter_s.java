 package ch.fhnw.apsifilter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.annotation.CheckReturnValue;
 import javax.annotation.Nonnull;
 
 import org.jsoup.nodes.Document;
 
 import ch.fhnw.apsifilter.filter.AttributeWhitelistFilter;
 import ch.fhnw.apsifilter.filter.ProtocolFilter;
 import ch.fhnw.apsifilter.filter.TagWhitelistFilter;
 import ch.fhnw.apsifilter.filter.css.CssInlineFilter;
 import ch.fhnw.apsifilter.filter.css.CssLinkFilter;
 import ch.fhnw.apsifilter.filter.css.CssStyleAttributeFilter;
 
 public final class HtmlFilter {
 
 	public static void main(String[] args) {
 		if(!SecurityHelper.checkAdmin()) {
 			printAdminError();
 			return;
 		}
 		
 		if(args.length != 1) {
 			printUsage();
 			return;
 		}
 		
 		final HtmlFilter htmlFilter = new HtmlFilter();
 		final String cleanHtml = htmlFilter.filter(args[0]);
 		
 		System.out.println(cleanHtml);
 	}
 	
 	private static void printUsage() {
		System.out.println("Usage: java HtmlFilter <inputfile> <outputfile>");
 	}
 	
 	private static void printAdminError() {
		System.out.println("Authentication failed: This program has to run with administrator/root rights");
 	}
 	
 	private final Pipe pipe;
 	
 	private HtmlFilter() {
 		pipe = Pipe.createPipe();
 		pipe.addFilter(TagWhitelistFilter.createDefault());
 		pipe.addFilter(AttributeWhitelistFilter.createDefault());
 		pipe.addFilter(ProtocolFilter.createDefault());
 		pipe.addFilter(CssStyleAttributeFilter.createDefault());
 		pipe.addFilter(CssInlineFilter.createDefault());
 		pipe.addFilter(CssLinkFilter.createDefault());
 	}
 	
 	@CheckReturnValue
 	private String filter(@Nonnull String filename) {
 		try {
 			String htmlText = readHtmlFromFile(filename);
 			Document d = pipe.filter(htmlText);
 			
 			return d.html();
 			
 		} catch (FileNotFoundException ex) {
 			System.err.println("Error accessing file: " + ex.getMessage());
 			return "";
 		}
 	}
 	
 	@CheckReturnValue
 	private String readHtmlFromFile(@Nonnull String filename) throws FileNotFoundException {
 		final File file = new File(filename);
 		if(!file.exists()) throw new FileNotFoundException("Unable to find file.");
 		if(!file.canRead()) throw new FileNotFoundException("Could not read file.");
 
 		try(final BufferedReader in = new BufferedReader(new FileReader(file))){
 			final StringBuilder builder = new StringBuilder();
 			final char[] buf = new char[1024];
 			
 			int cnt;
 			while((cnt = in.read(buf)) != -1)
 				builder.append(buf, 0, cnt);
 	
 			return builder.toString();
 		} catch(IOException ex) {
 			throw new FileNotFoundException("Error reading file: " + ex.getMessage());
 		}
 	}
 }
