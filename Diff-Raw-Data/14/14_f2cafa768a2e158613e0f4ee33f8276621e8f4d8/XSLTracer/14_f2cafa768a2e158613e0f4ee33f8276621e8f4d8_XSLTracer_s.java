 package ru.imobilco;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.stream.StreamResult;
 
 import com.icl.saxon.Controller;
 import com.icl.saxon.FeatureKeys;
 import com.icl.saxon.ParameterSet;
 import com.icl.saxon.PreparedStyleSheet;
 import com.icl.saxon.StyleSheet;
 import com.icl.saxon.trace.TraceListener;
 
 public class XSLTracer extends StyleSheet {
 
 	private File tracerOutput;
 	private static ByteArrayOutputStream traceStream = new ByteArrayOutputStream();
 	private static ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
 	private static PrintStream writer = System.err;
 	private static PrintStream outputStream = System.out;
 	
 	private String templateUrl = "";
 	private String sourceUrl = "";
 
 	public static void main(String args[]) throws java.lang.Exception {
 		String name = " java ru.imobilco.XSLTracer";
 		XSLTracer tracer = new XSLTracer();
 		ArrayList<String> nativeArgs = new ArrayList<String>();
 
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].equals("-to")) {
 				if (args.length < i + 2)
 					tracer.badUsage(name, "No tracer output file");
 				
 				tracer.setTracerOutput(args[++i]);
 			} else {
 				nativeArgs.add(args[i]);
 			}
 		}
 
 		// create a clean copy of args so underlying StyleSheet initializer
 		// won't thow errors about unknown params
 		String[] cleanArgs = new String[nativeArgs.size()];
 		for (int i = 0; i < nativeArgs.size(); i++) {
 			cleanArgs[i] = nativeArgs.get(i);
 		}
 
 		// the real work is delegated to another routine so that it can be used
 		// in a subclass
 		tracer.doMain(cleanArgs, new XSLTracer(), name);
 	}
 	
 	public void setTracerOutput() {
 		JSONTraceListener traceListener = new JSONTraceListener();
 		try {
 			writer = new PrintStream(traceStream, true, "utf-8");
 			traceListener.setOutputDestination(writer);
 			
 			factory.setAttribute(FeatureKeys.TRACE_LISTENER, traceListener);
 			factory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void setTracerOutput(String fileName) {
 		tracerOutput = new File(fileName);
 		setTracerOutput();
 	}
 
 	/**
 	 * Set base template URL used for transformation. You should set this 
 	 * property right before calling <code>makeTraceDocument</code>, because
 	 * this result will be printed in result document
 	 * @param templateUrl
 	 */
 	public void setTemplateUrl(String templateUrl) {
 		this.templateUrl = templateUrl;
 	}
 
 	public String getTemplateUrl() {
 		return templateUrl;
 	}
 
 	/**
 	 * Set source XML URL used for transformation. You should set this 
 	 * property right before calling <code>makeTraceDocument</code>, because
 	 * this result will be printed in result document
 	 * @param sourceUrl
 	 */
 	public void setSourceUrl(String sourceUrl) {
 		this.sourceUrl = sourceUrl;
 	}
 
 	public String getSourceUrl() {
 		return sourceUrl;
 	}
 
 	/**
 	 * Process a single file using a supplied stylesheet
 	 */
 	public void processFile(Source source, Templates sheet, File outputFile,
 			ParameterSet params) throws TransformerException {
 		
 		Transformer instance = sheet.newTransformer();
 		((Controller) instance).setParams(params);
 		
 		setSourceUrl(source.getSystemId());
 		setTemplateUrl(((PreparedStyleSheet) sheet).getStyleSheetDocument().getSystemId());
 
 		String result = makeTraceDocument(source, sheet);
 		saveResult(result);
 	}
 	
 	private void saveResult(String result) {
 		if (tracerOutput != null) {
 			if (!tracerOutput.exists()) {
 				try {
 					tracerOutput.createNewFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			try {
 				FileWriter fstream = new FileWriter(tracerOutput);
 				BufferedWriter out = new BufferedWriter(fstream);
 				out.write(result);
 				out.close();
 				fstream.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			// no file supplied, print result into output stream
 			outputStream.print(result);
 		}
 	}
 	
 	/**
 	 * Process a single file using a supplied stylesheet and returns tracing
 	 * document either as a successfull or faild transformation. The output
 	 * result can be saved to the document or outputted to the user
 	 * @param source
 	 * @param sheet
 	 * @return
 	 * @throws TransformerConfigurationException 
 	 */
 	public String makeTraceDocument(Source source, Templates sheet) throws TransformerConfigurationException {
 		TraceListener tracer = (TraceListener) factory.getAttribute(FeatureKeys.TRACE_LISTENER);
 		
 		if (tracer != null && tracer instanceof JSONTraceListener) {
 			((JSONTraceListener) tracer).setBaseFiles(source.getSystemId(),
 					((PreparedStyleSheet) sheet).getStyleSheetDocument()
 							.getSystemId());
 		}
 
 		Transformer instance = sheet.newTransformer();
 
 		// force transformer to output XML result
 		instance.setOutputProperty(OutputKeys.ENCODING, "utf8");
 		instance.setOutputProperty(OutputKeys.METHOD, "xml");
 		instance.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 		instance.setOutputProperty(OutputKeys.INDENT, "no");
 
 		Result result = new StreamResult(resultStream);
 
 		try {
 			instance.transform(source, result);
 			return outputTraceDocument();
 		} catch (Exception err) {
 			return outputErrorDocument(err);
 		}
 	}
 	
 	/**
 	 * Returns HTML template content as string for <code>MessageFormat</code>
 	 * class
 	 * @return
 	 */
 	private String getTemplate() {
 		InputStream template = this.getClass().getResourceAsStream("/template.html");
 		try {
 			return convertStreamToString(template);
 		} catch (IOException e) {
 			return "";
 		}
 	}
 	
 	private String outputTraceDocument() {
 		return MessageFormat.format(getTemplate(), "", 
 				escapeHTML(resultStream.toString()), traceStream.toString(),
 				getTemplateUrl(), getSourceUrl());
 	}
 	
 	private String outputErrorDocument(Exception e) {
 		// contruct error message
 		String error = "<div id=\"xt-global-error\">" +
 				"<h2>Error</h2>" +
 				"<div id=\"xt-global-error-content\">" +
 				"<p>The XSL traces cannot be initialized because of the following server error:</p>" +
 				"<div class=\"xt-error\">" +
				e.toString() +
 				"</div></div></div>";
 		
 		return MessageFormat.format(getTemplate(), error, "", "{}", "", "");
 	}
 
 	public static String convertStreamToString(InputStream is) throws IOException {
 		/*
 		 * To convert the InputStream to String we use the
 		 * BufferedReader.readLine() method. We iterate until the BufferedReader
 		 * return null which means there's no more data to read. Each line will
 		 * appended to a StringBuilder and returned as String.
 		 */
 		if (is != null) {
 			StringBuilder sb = new StringBuilder();
 			String line;
 
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 				while ((line = reader.readLine()) != null) {
 					sb.append(line).append("\n");
 				}
 			} finally {
 				is.close();
 			}
 			return sb.toString();
 		} else {
 			return "";
 		}
 	}
 	
 	public static String escapeHTML(String string) {
 		StringBuffer sb = new StringBuffer(string.length());
 		int len = string.length();
 		char c;
 		for (int i = 0; i < len; i++) {
 			c = string.charAt(i);
 			if (c == '&')
 				sb.append("&amp;");
 			else if (c == '<')
 				sb.append("&lt;");
 			else if (c == '>')
 				sb.append("&gt;");
 			else {
 				sb.append(c);
 			}
 		}
 		return sb.toString();
 	}
 }
