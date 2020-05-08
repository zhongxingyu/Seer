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
 			Log.info("HTTP error 404 - Not Found");
 			break;
 		default:
 			Log.fatal("Invalid http status code: " + this.httpStatusCode);
 			break;
 		}
 
 		try {
 			File bodyFile = generateBody(outputStream, requestResource);
 			byte[] body = getByteArray(bodyFile);
 
			// XXX issue, because interpreted files have different size
 			long size = body.length > bodyFile.length() ? body.length
 					: bodyFile.length();
 
 			byte[] header = getByteArray(generateHeader(size, requestResource));
 
 			HttpCache.getInstance().put(requestResource, header, body);
 
 			outputStream.write(header);
 			outputStream.write(body);
 		} catch (final IOException ex) {
 			Log.error("Can not write response / output stream! ", ex);
 		} catch (URISyntaxException e) {
 			Log.error("Can't read resource from JAR file.", e);
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
 
 		// TODO [dsc]
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
 		header = header + "Server: " + Configuration.getConfig().getServerName() + LINE_BREAK;
 		header = header + "Content-Length: " + bodyLength + LINE_BREAK;
 		header = header + "Content-Language: de" + LINE_BREAK;
 		header = header + "Connection: close" + LINE_BREAK;
 		if (this.httpStatusCode == 200 && !requestResource.isDirectory()) {
 			header = header + "Content-Type: "
					+ getContentType(requestResource)  + "; charset=utf-8"
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
 	 * @param outputStream
 	 * @param requestResource
 	 * @return file
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	private File generateBody(OutputStream outputStream, File requestResource)
 			throws IOException, URISyntaxException {
 
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
 			tempFile = new File(WebServer.class.getClassLoader()
 					.getResource("403.html").toURI());
 			break;
 		case 404:
 			tempFile = new File(WebServer.class.getClassLoader()
 					.getResource("404.html").toURI());
 			break;
 		case 500:
 			tempFile = new File(WebServer.class.getClassLoader()
 					.getResource("500.html").toURI());
 			break;
 		default:
 			throw new IllegalStateException("Invalid http status code.");
 		}
 		return tempFile;
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
 		File tempFile = File.createTempFile("directorylisting", ".html");
 		tempFile.deleteOnExit();
 
 		for (File file : requestResource.listFiles()) {
 			for (String item : Configuration.getConfig().getDirectoryIndex()) {
 				if (file.getName().equals(item)) {
 					return file;
 				}
 			}
 		}
 
 		PrintWriter tempFilePrintWriter = new PrintWriter(new BufferedWriter(
 				new FileWriter(tempFile)));
 		String style = "<style>"
 				+ "ul li:nth-child(2n) {background-color:#E6E6E6;} "
 				+ "li {list-style:none;}" + "a:visited {color:#0000FF;}"
 				+ "body {margin:0; padding-top:15px;}" + "</style>";
 
 		tempFilePrintWriter.print("<html><head>" + style + "</head><body><ul>");
 		// show directory info
 		tempFilePrintWriter.print("<h1>Directory: "
 				+ requestResource.toString().replaceFirst(
 						Configuration.getConfig().getWebRoot(), "") + "</h1>");
 		// add parent link
 		if (!Configuration.getConfig().getWebRoot()
 				.startsWith(requestResource.getAbsolutePath())) {
 			tempFilePrintWriter.print("<li><a href=\"..\">/..</a></li>");
 		}
 		// file listing
 		for (File file : requestResource.listFiles(new HiddenFilter())) {
 			if (file.isDirectory()) {
 				tempFilePrintWriter.print("<li><a href=\"" + file.getName()
 						+ "/\">" + file.getName() + "</a></li>");
 			} else if (file.isFile()) {
 				tempFilePrintWriter.print("<li><a href=\"" + file.getName()
 						+ "\">" + file.getName() + "</a></li>");
 			}
 		}
 		tempFilePrintWriter.print("</ul></body></html>");
 		tempFilePrintWriter.flush();
 		tempFilePrintWriter.close();
 		return tempFile;
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
 		// unknown content type - browser handle it
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
