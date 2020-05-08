 package de.th.wildau.dsc.sne.webserver;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 
 /**
  * The HttpWriter generates and writes the http response (header and body) into
  * the output stream.
  */
 public class HttpWriter {
 
 	private static final String LINE_BREAK = "\r\n";
 
 	private final int httpStatusCode;
 
 	/**
 	 * This constructor is only for caches. It writes the byte array directly.
 	 * The http Status Code is in this case <b>-1</b>.
 	 * 
 	 * @param outputStream
 	 * @param direct
 	 *            byte[]
 	 */
 	protected HttpWriter(OutputStream outputStream, byte[] direct) {
 
 		this.httpStatusCode = -1;
 
 		try {
 			outputStream.write(direct);
 		} catch (final IOException ex) {
 			Log.error("Can not write response / output stream! ", ex);
 		}
 	}
 
 	/**
 	 * Constructor which prepares the server http response.
 	 * 
 	 * @param httpStatusCode
 	 */
 	public HttpWriter(int httpStatusCode) {
 
 		this.httpStatusCode = httpStatusCode;
 	}
 
 	/**
 	 * This method writes the http response into the given output stream.
 	 * 
 	 * @param outputStream
 	 * @param requestResource
 	 */
 	public void write(OutputStream outputStream, File requestResource) {
 
 		switch (this.httpStatusCode) {
 		case -1:
 			Log.warn("HTTP -1 write output from cache");
 			break;
 		case 200:
 			Log.info("HTTP success 200 - OK");
 			break;
 		case 403:
 			Log.info("HTTP error 403 - Forbidden");
 			break;
 		case 404:
 			Log.info("HTTP error 404 - Page Not Found");
 			break;
 		case 500:
 			Log.info("HTTP error 500 - Internal Server Error");
 			break;
 		default:
 			Log.fatal("Invalid http status code: " + this.httpStatusCode);
 			break;
 		}
 
 		try {
 			File bodyFile = generateBody(requestResource);
 			byte[] body = getByteArray(bodyFile);
 
 			long bodySize;
 			// output length of interpreted script / normal files are different
 			if (isInterpretedFile(requestResource) == null) {
 				bodySize = bodyFile.length();
 			} else {
 				bodySize = body.length;
 			}
 
 			byte[] header = getByteArray(generateHeader(bodySize,
 					requestResource));
 
 			HttpCache.getInstance().put(requestResource, header, body);
 
 			outputStream.write(header);
 			outputStream.write(body);
 		} catch (final IOException ex) {
 			Log.error("Can not write response / output stream! ", ex);
 			failOver(outputStream);
 		} catch (URISyntaxException e) {
 			Log.error("Can't read resource from JAR file.", e);
 			failOver(outputStream);
 		}
 	}
 
 	/**
 	 * Internal help method which create a 500 error page.
 	 * 
 	 * @param outputStream
 	 */
 	private void failOver(OutputStream outputStream) {
 
 		File tempFile = createHtmlFile("500",
 				"Error 500 - Internal Server Error",
 				"<h1>Error 500 - Internal Server Error</h1>");
 		String header = generateHeader(tempFile.length(), null);
 		try {
 			outputStream.write(getByteArray(header));
 			outputStream.write(getByteArray(tempFile));
 		} catch (final Exception ex) {
 			Log.fatal("The fail over failed.", ex);
 		}
 	}
 
 	/**
 	 * Internal help method which converts a string (UTF-8) into a byte array.
 	 * 
 	 * @param string
 	 * @return byte[]
 	 * @throws UnsupportedEncodingException
 	 */
 	private byte[] getByteArray(String string)
 			throws UnsupportedEncodingException {
 
 		return new String(string.getBytes(), "UTF-8").getBytes();
 	}
 
 	/**
 	 * Internal help method which converts a file into a byte array.
 	 * 
 	 * @param file
 	 * @return byte[]
 	 * @throws IOException
 	 * @throws UnsupportedEncodingException
 	 */
 	private byte[] getByteArray(File file) throws IOException,
 			UnsupportedEncodingException {
 
 		ScriptLanguage scriptLanguage;
 		if ((scriptLanguage = isInterpretedFile(file)) != null) {
 			return new ScriptExecutor().execute(scriptLanguage, file)
 					.getBytes();
 		}
 
 		FileInputStream fis = new FileInputStream(file);
 		byte[] data = new byte[(int) file.length()];
 		fis.read(data);
 		fis.close();
 		return data;
 	}
 
 	/**
 	 * Internal help method which checks if the given file is a script file.
 	 * 
 	 * @param file
 	 * @return {@link ScriptLanguage} is supported or null
 	 */
 	private ScriptLanguage isInterpretedFile(File file) {
 
 		for (ScriptLanguage scriptLanguage : WebServer.supportedScriptLanguages) {
 			if (file.getName().toLowerCase()
 					.endsWith(scriptLanguage.getFileExtension())) {
 				return scriptLanguage;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Internal help method which generates the http header.
 	 * 
 	 * @param body
 	 * @param requestResource
 	 * @return String http header
 	 */
 	private String generateHeader(long bodyLength, File requestResource) {
 
 		String header = new String("HTTP/1.1 ");
 
 		switch (this.httpStatusCode) {
 		case 200:
 			header = header + "200 OK" + LINE_BREAK;
 			break;
 		case 403:
 			header = header + "403 Forbidden" + LINE_BREAK;
 			break;
 		case 404:
 			header = header + "404 File Not Found" + LINE_BREAK;
 			break;
 		default:
 			header = header + "500 Internal Server Error" + LINE_BREAK;
 			break;
 		}
 		header = header + "Server: "
 				+ Configuration.getConfig().getServerName() + LINE_BREAK;
 		header = header + "Content-Length: " + bodyLength + LINE_BREAK;
 		header = header + "Content-Language: de" + LINE_BREAK;
 		header = header + "Connection: close" + LINE_BREAK;
 		if (this.httpStatusCode == 200 && requestResource != null
 				&& !requestResource.isDirectory()) {
 			header = header + "Content-Type: "
 					+ getContentType(requestResource) + "; charset=utf-8"
 					+ LINE_BREAK;
 		} else {
 			header = header + "Content-Type: text/html; charset=utf-8"
 					+ LINE_BREAK;
 		}
 
 		// add empty line
 		header = header + LINE_BREAK;
 
 		return header;
 	}
 
 	/**
 	 * Internal help method which generates the http body.
 	 * 
 	 * @param requestResource
 	 * @return file
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	private File generateBody(File requestResource) throws IOException,
 			URISyntaxException {
 
 		File tempFile = null;
 
 		switch (this.httpStatusCode) {
 		case 200:
 			if (requestResource.isFile()) {
 				tempFile = requestResource;
 			} else if (requestResource.isDirectory()
 					&& requestResource.canRead()) {
 				tempFile = createDirectoryListing(requestResource);
 			}
 			break;
 		case 403:
 			if (Configuration.getConfig().getErrorPage403() != null) {
 				tempFile = new File(Configuration.getConfig().getErrorPage403());
 			} else {
 				tempFile = createHtmlFile("403", "Error 403 - Forbidden",
 						"<h1>Error 403 - Forbidden</h1>");
 			}
 			break;
 		case 404:
 			if (Configuration.getConfig().getErrorPage404() != null) {
 				tempFile = new File(Configuration.getConfig().getErrorPage404());
 			} else {
 				tempFile = createHtmlFile("404", "Error 404 - File Not Found",
 						"<h1>Error 404 - File Not Found</h1>");
 			}
 			break;
 		case 500:
 			if (Configuration.getConfig().getErrorPage403() != null) {
 				tempFile = new File(Configuration.getConfig().getErrorPage403());
 			} else {
 				throw new IllegalStateException("Missing error page 500.");
 			}
 			break;
 		default:
 			throw new IllegalStateException("Invalid http status code.");
 		}
 		return tempFile;
 	}
 
 	/**
 	 * @see #createHtmlFile(String, String, String, String)
 	 * @param fileName
 	 * @param title
 	 * @param body
 	 * @return
 	 */
 	private File createHtmlFile(String fileName, String title, String body) {
 		return createHtmlFile(fileName, title, "", body);
 	}
 
 	/**
 	 * Internal help method which creates a temporary html file.
 	 * 
 	 * @param fileName
 	 * @param title
 	 * @param body
 	 * @return temp file
 	 */
 	private File createHtmlFile(String fileName, String title, String style,
 			String body) {
 
 		try {
 			File tempFile = File.createTempFile(fileName, ".html");
 			tempFile.deleteOnExit();
 			PrintWriter tempFilePrintWriter = new PrintWriter(
 					new BufferedWriter(new FileWriter(tempFile)));
 			tempFilePrintWriter
 					.print("<html><head><meta charset=\"UTF-8\"><title>"
 							+ title + "</title><style>" + style
 							+ "</style></head><body>");
 			tempFilePrintWriter.print(body);
 			tempFilePrintWriter.print("</body></html>");
 			tempFilePrintWriter.flush();
 			tempFilePrintWriter.close();
 			return tempFile;
 		} catch (final IOException ex) {
 			Log.error("Can not create a temp file.", ex);
 			return null;
 		}
 	}
 
 	/**
 	 * Internal help method which creates the directory listing.
 	 * 
 	 * @param requestResource
 	 * @return File
 	 * @throws IOException
 	 */
 	private File createDirectoryListing(File requestResource)
 			throws IOException {
 
 		for (File file : requestResource.listFiles()) {
 			for (String item : Configuration.getConfig().getDirectoryIndex()) {
 				if (file.getName().equals(item)) {
 					return file;
 				}
 			}
 		}
 
 		String style = "ul li:nth-child(2n) {background-color:#E6E6E6;} "
 				+ "li {list-style:none;}" + "a:visited {color:#0000FF;}"
 				+ "body {margin:0; padding-top:15px;}";
 
 		String body = "<ul>";
 		// show directory info
 		body = body
 				+ "<h1>Directory: "
 				+ requestResource.toString().replaceFirst(
 						Configuration.getConfig().getWebRoot(), "") + "</h1>";
 
 		// add parent link
 		if (!Configuration.getConfig().getWebRoot()
 				.startsWith(requestResource.getAbsolutePath())) {
 			body = body + "<li><a href=\"..\">/..</a></li>";
 		}
 		// file listing
 		for (File file : requestResource.listFiles(new HiddenFilter())) {
 			if (file.isDirectory()) {
 				body = body + "<li><a href=\"" + file.getName() + "/\">"
 						+ file.getName() + "</a></li>";
 			} else if (file.isFile()) {
 				body = body + "<li><a href=\"" + file.getName() + "\">"
 						+ file.getName() + "</a></li>";
 			}
 		}
 		return createHtmlFile("directorylisting", requestResource.getName(),
 				style, body);
 	}
 
 	/**
 	 * Help method which finds the content type of the requestResource file.
 	 * 
 	 * @param requestResource
 	 * @return content type
 	 */
 	private String getContentType(File requestResource) {
 
 		Log.debug("method... HttpWriter.getContentType()");
 
 		try {
 			if (requestResource.isFile()) {
 				// fix script files has content type content/unknown
 				if (isInterpretedFile(requestResource) != null) {
 					return "text/html";
 				}
 				// general
 				return requestResource.toURI().toURL().openConnection()
 						.getContentType();
 			}
 		} catch (final MalformedURLException ex) {
 			Log.warn(ex.getMessage());
 		} catch (final IOException ex) {
 			Log.warn(ex.getMessage());
 		}
		// unknown content type - browser handles it
 		return "application/octet-stream";
 	}
 
 	/**
 	 * Getter which returns the current http status code.
 	 * 
 	 * @return http status code
 	 */
 	protected int getHttpCode() {
 		return this.httpStatusCode;
 	}
 }
