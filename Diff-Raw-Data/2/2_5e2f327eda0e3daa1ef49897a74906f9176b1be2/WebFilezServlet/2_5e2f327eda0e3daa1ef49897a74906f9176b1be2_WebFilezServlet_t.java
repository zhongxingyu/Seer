 package com.marakana.webfilez;
 
 import static com.marakana.webfilez.FileUtil.copy;
 import static com.marakana.webfilez.FileUtil.delete;
 import static com.marakana.webfilez.FileUtil.getUniqueFileInDirectory;
 import static com.marakana.webfilez.FileUtil.size;
 import static com.marakana.webfilez.FileUtil.sizeOfZip;
 import static com.marakana.webfilez.FileUtil.unzip;
 import static com.marakana.webfilez.FileUtil.zipDirectory;
 import static com.marakana.webfilez.FileUtil.zipFile;
 import static com.marakana.webfilez.FileUtil.zipFiles;
 import static com.marakana.webfilez.WebUtil.asParams;
 import static com.marakana.webfilez.WebUtil.generateETag;
 import static com.marakana.webfilez.WebUtil.getFileName;
 import static com.marakana.webfilez.WebUtil.getParentUriPath;
 import static com.marakana.webfilez.WebUtil.ifMatch;
 import static com.marakana.webfilez.WebUtil.ifModifiedSince;
 import static com.marakana.webfilez.WebUtil.ifNoneMatch;
 import static com.marakana.webfilez.WebUtil.ifUnmodifiedSince;
 import static com.marakana.webfilez.WebUtil.isHead;
 import static com.marakana.webfilez.WebUtil.isJson;
 import static com.marakana.webfilez.WebUtil.isMultiPartRequest;
 import static com.marakana.webfilez.WebUtil.isZip;
 import static com.marakana.webfilez.WebUtil.parseRange;
 import static com.marakana.webfilez.WebUtil.setContentLength;
 import static com.marakana.webfilez.WebUtil.setNoCacheHeaders;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.SocketException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import org.json.JSONException;
 import org.json.JSONWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.marakana.webfilez.FileUtil.PathHandler;
 import com.marakana.webfilez.WebUtil.Range;
 
 public final class WebFilezServlet extends HttpServlet {
 	private static final Pattern INVALID_SOURCE_PATH_PATTERN = Pattern.compile("(\\.\\.)");
 	private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("(\\.\\.)|/|\\\\");
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = LoggerFactory.getLogger(WebFilezServlet.class);
 	protected static final String MULTIPART_BOUNDARY = "mrkn_webfilez_boundary";
 
 	private Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
 
 	private int bufferSize;
 
 	private String defaultMimeType;
 
 	private String directoryMimeType;
 
 	private Path readmeFileName;
 
 	private long readmeFileMaxLength;
 
 	private Charset readmeFileCharset;
 
 	private String rootDir;
 
 	private Collection<SearchAndReplace> rewriteRules;
 
 	private Pattern basePathPattern;
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			final Context ctx = new InitialContext();
 			try {
 				final Params params = asParams((Context) ctx.lookup("java:comp/env/"));
 				this.bufferSize = params.getInteger("buffer-size", 4096);
 				this.directoryMimeType = params.getString(
 						"directory-mime-type", "x-directory/normal");
 				this.defaultMimeType = params.getString("default-mime-type",
 						"application/octet-stream");
 				this.readmeFileName = FileSystems.getDefault().getPath(
 						params.getString("readme-file-name", "README.html"));
 				this.readmeFileMaxLength = params.getInteger(
 						"readme-file-max-length", 5 * 1024 * 1024);
 				this.readmeFileCharset = Charset.forName(params.getString(
 						"readme-file-charset", "UTF-8"));
 				final String rootDir = params.getString("root-dir");
 				this.rootDir = rootDir == null ? "." : rootDir;
 				this.rewriteRules = SearchAndReplace.parse(params.getString("rewrite-rules"));
 				this.basePathPattern = Pattern.compile(params.getString(
 						"base-path-pattern", "^(/[^/]+/[0-9]+/files/).*"));
 				if (logger.isInfoEnabled()) {
 					logger.info("Initialized " + this);
 				}
 			} finally {
 				ctx.close();
 			}
 		} catch (NamingException e) {
 			throw new ServletException("Failed to init", e);
 		}
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		final String basePath = this.getBasePath(request, true);
 		String uri = request.getRequestURI();
 		Path file = this.getRequestFile(request);
 		if (fileExistsOrItIsTheBasePathAndIsCreated(file, uri, basePath)) {
 			if (Files.isDirectory(file)) {
 				if (uri.endsWith("/")) {
 					if (isZip(request)
 							|| "zip_download".equals(request.getParameter("_action"))) {
 						try {
 							this.handleZipDownloadRequest(request, response,
 									file);
 						} catch (IOException e) {
 							this.sendServerFailure(request, response,
 									"Failed to send ZIP of files in [" + file
 											+ "]", e);
 						}
 					} else {
 						try {
 							this.handleList(request, response, file, basePath);
 						} catch (JSONException e) {
 							this.sendServerFailure(request, response,
 									"Failed to send listing as JSON for ["
 											+ file + "]", e);
 						}
 					}
 				} else {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Adding trailing slash to [" + uri + "]");
 					}
 					response.sendRedirect(uri + "/");
 				}
 			} else if (Files.isRegularFile(file)) {
 				this.handleDownload(request, response, file);
 			} else {
 				this.sendServerFailure(request, response,
 						"Not a file or a directory [" + file
 								+ "] in response to [" + uri + "]");
 			}
 		} else {
 			if (!isHead(request)) {
 				// search for the closest folder that does exist
 				// until we get to directory root
 				while (uri != null && uri.startsWith(basePath)
 						&& !file.toString().equals(this.rootDir)) {
 					uri = getParentUriPath(uri);
 					file = file.getParent();
 					if (Files.exists(file)) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Attempting to recover. Redirecting to: "
 									+ uri);
 						}
 						response.sendRedirect(uri);
 						return;
 					} else {
 						if (logger.isTraceEnabled()) {
 							logger.trace("Trying to see if [" + uri
 									+ "] exists.");
 						}
 					}
 				}
 			}
 			this.refuseRequest(request, response,
 					HttpServletResponse.SC_NOT_FOUND, "No such file [" + file
 							+ "] in response to [" + uri + "]");
 		}
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		final Path file = getRequestFile(request);
 		if (logger.isDebugEnabled()) {
 			logger.debug("Processing request to delete [" + file + "]");
 		}
 		if (Files.exists(file)) {
 			final long lastModified = Files.getLastModifiedTime(file).toMillis();
 			final String etag = generateETag(Files.size(file), lastModified);
 			if (ifMatch(request, etag)
 					&& ifUnmodifiedSince(request, lastModified)) {
 				try {
 					delete(file);
 					if (logger.isDebugEnabled()) {
 						logger.debug("Deleted [" + file + "]");
 					}
 					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 				} catch (IOException e) {
 					this.sendServerFailure(request, response,
 							"Failed to delete [" + file + "]", e);
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_PRECONDITION_FAILED,
 						"Refusing to delete file for which precondition failed: ["
 								+ file + "]");
 			}
 		} else {
 			this.refuseRequest(request, response,
 					HttpServletResponse.SC_NOT_FOUND,
 					"Cannot delete a file that does not exist [" + file + "]");
 		}
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		final Path file = this.getRequestFile(request);
 		final String type = request.getContentType();
 		final boolean makeDirRequest = this.directoryMimeType.equals(type);
 		final int responseCode;
 		if (Files.exists(file)) {
 			if (makeDirRequest) {
 				if (Files.isDirectory(file)) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Directory already exists [" + file
 								+ "]. Ignoring.");
 					}
 				} else {
 					this.refuseRequest(request, response,
 							HttpServletResponse.SC_CONFLICT,
 							"Cannot create directory since a file with the same name already exists ["
 									+ file + "]");
 					return;
 				}
 			} else {
 				if (Files.isDirectory(file)) {
 					this.refuseRequest(request, response,
 							HttpServletResponse.SC_CONFLICT,
 							"Cannot create file since a directory with the same name already exists ["
 									+ file + "]");
 					return;
 				} else {
 					final long lastModified = Files.getLastModifiedTime(file).toMillis();
 					final String etag = generateETag(Files.size(file),
 							lastModified);
 					if (ifMatch(request, etag)
 							&& ifUnmodifiedSince(request, lastModified)) {
 						if (!this.handleSingleUpload(file, request, response)) {
 							return;
 						}
 					} else {
 						this.refuseRequest(request, response,
 								HttpServletResponse.SC_PRECONDITION_FAILED,
 								"Cannot modify file for which precondition failed: ["
 										+ file + "]");
 						return;
 					}
 				}
 			}
 			responseCode = HttpServletResponse.SC_OK;
 		} else {
 			if (makeDirRequest) {
 				Files.createDirectories(file);
 				if (logger.isDebugEnabled()) {
 					logger.debug("Created directory [" + file + "]");
 				}
 			} else {
 				if (!this.handleSingleUpload(file, request, response)) {
 					return;
 				}
 			}
 			responseCode = HttpServletResponse.SC_CREATED;
 		}
 		response.setStatus(responseCode);
 		try {
 			this.sendFileInfoResponse(file, response);
 		} catch (JSONException e) {
 			if (logger.isErrorEnabled()) {
 				logger.error(
 						"Failed to generate JSON response to PUT request file ["
 								+ file + "]", e);
 			}
 			response.resetBuffer();
 		}
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		final String action = request.getParameter("_action");
 		final String uri = request.getRequestURI();
 		final String basePath = this.getBasePath(request, true);
 		if (action == null) {
 			refuseBadRequest(
 					request,
 					response,
 					"No action specified for POST to "
 							+ request.getRequestURI());
 		} else {
 			final Path file = getRequestFile(request);
 			if (fileExistsOrItIsTheBasePathAndIsCreated(file, uri, basePath)) {
 				try {
 					if (Files.isDirectory(file)) {
 						switch (action) {
 						case "upload":
 							if (isMultiPartRequest(request)) {
 								this.handleUpload(file, request, response);
 							} else {
 								refuseBadRequest(request, response,
 										"Not a valid POST upload request to "
 												+ request.getRequestURI());
 							}
 							break;
 						case "zip":
 							this.handleZip(file, request, response);
 							break;
 						case "rename":
 							this.handleRename(file, request, response);
 							break;
 						case "copy":
 							this.handleCopy(file, request, response);
 							break;
 						case "move":
 						case "cut":
 							this.handleMove(file, request, response);
 							break;
 						default:
 							refuseBadRequest(
 									request,
 									response,
 									"Unsupported POST action [" + action
 											+ "] to directory "
 											+ request.getRequestURI());
 						}
 					} else if (Files.isRegularFile(file)) {
 						switch (action) {
 						case "unzip":
 							this.handleUnzip(file, request, response);
 							break;
 						case "rename":
 							this.handleRename(file, request, response);
 							break;
 						default:
 							refuseBadRequest(
 									request,
 									response,
 									"Unsupported POST action [" + action
 											+ "] to directory "
 											+ request.getRequestURI());
 						}
 					} else {
 						this.refuseRequest(request, response,
 								HttpServletResponse.SC_FORBIDDEN,
 								"Don't know how to handle POST to file " + file);
 					}
 				} catch (Exception e) {
 					this.sendServerFailure(request, response,
 							"Failed to handle POST " + request.getRequestURI()
 									+ " for [" + action + "]", e);
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND,
 						"Cannot handle a POST request for file that does not exist: "
 								+ file);
 			}
 		}
 	}
 
 	private void handleZipDownloadRequest(HttpServletRequest request,
 			HttpServletResponse response, Path dir) throws ServletException,
 			IOException {
 		if (logger.isTraceEnabled()) {
 			logger.trace("ZIP-downloading files in [" + dir + "]");
 		}
 		final List<Path> files = this.getFilesFromRequest(request, dir);
 		String filename = request.getParameter("filename");
 		if (files.isEmpty()) {
 			this.refuseBadRequest(request, response,
 					"Select at least one file to zip-download");
 		} else {
 			if (filename == null) {
 				filename = (files.size() == 1 ? files.get(0) : dir).getFileName()
 						+ ".zip";
 			}
 			response.setContentType("application/zip");
 			response.setHeader("Content-Disposition",
 					String.format("attachment; filename=\"%s\"", filename));
 			response.setHeader("Accept-Ranges", "none");
 			FileUtil.zipFiles(dir, files, response.getOutputStream());
 		}
 	}
 
 	private void handleList(HttpServletRequest request,
 			HttpServletResponse response, Path dir, String basePath)
 			throws IOException, ServletException, JSONException {
 		final String uri = request.getRequestURI();
 		final long lastModified = Files.getLastModifiedTime(dir).toMillis();
 		// TODO, we should probably find the most up-to-date file and use it for
 		// last-modified???
 		if (lastModified >= 0) {
 			response.setDateHeader("Last-Modified", lastModified);
 		}
 		setNoCacheHeaders(response);
 		if (isHead(request)) {
 			response.setHeader("ETag", generateETag(size(dir), lastModified));
 		} else if (isJson(request)) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Listing files in [" + dir + "]");
 			}
 			response.setContentType("application/json");
 			response.setHeader("Accept-Ranges", "none");
 			final ByteArrayOutputStream out = new ByteArrayOutputStream(8196);
 			long totalSize = 0;
 			Path readmeFile = null;
 			final Writer outWriter = new OutputStreamWriter(out,
 					DEFAULT_CHARSET);
 			final JSONWriter jsonWriter = new JSONWriter(outWriter);
 			jsonWriter.object();
 			jsonWriter.key("files").array();
 			try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
 				for (Path file : files) {
 					totalSize += writeFileInfoToJson(file, jsonWriter);
 					if (file.getFileName().equals(this.readmeFileName)) {
 						readmeFile = file;
 					}
 				}
 			}
 			jsonWriter.endArray();
 			response.setHeader("ETag", generateETag(totalSize, lastModified));
 			jsonWriter.key("uri").value(uri);
 			jsonWriter.key("name").value(dir.getFileName());
 			jsonWriter.key("type").value(this.directoryMimeType);
 			jsonWriter.key("size").value(totalSize);
 			jsonWriter.key("quota").value(this.getQuota(request));
 			jsonWriter.key("lastModified").value(Files.getLastModifiedTime(dir));
 			jsonWriter.key("writeAllowed").value(this.getWriteAllowed(request));
 			if (uri.length() > basePath.length() && uri.startsWith(basePath)) {
 				jsonWriter.key("parent").value(getParentUriPath(uri));
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("No parent present for uri [" + uri
 							+ "] and basePath [" + basePath + "]");
 				}
 			}
 			if (readmeFile != null) {
 				long readFileLength = Files.size(readmeFile);
 				if (readFileLength > this.readmeFileMaxLength) {
 					if (logger.isWarnEnabled()) {
 						logger.warn("README file [" + readmeFile + "] size ["
 								+ readFileLength + "] exceeds max size ["
 								+ this.readmeFileMaxLength + "]. Ignoring.");
 					}
 				} else {
 					try {
 						final String readme = new String(
 								Files.readAllBytes(readmeFile),
 								this.readmeFileCharset);
 						jsonWriter.key("readme").value(readme);
 					} catch (IOException e) {
 						if (logger.isWarnEnabled()) {
 							logger.warn("Failed to read the README file ["
 									+ readmeFile + "]. Ignoring.", e);
 						}
 					}
 				}
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("No README file present for uri [" + uri
 							+ "] and dir [" + dir + "]");
 				}
 			}
 			jsonWriter.endObject();
 			outWriter.flush();
 			if (logger.isTraceEnabled()) {
 				logger.trace("Writing listing of [" + out.size()
 						+ "] bytes for dir [" + dir + "]");
 			}
 			response.setContentLength(out.size());
 			out.writeTo(response.getOutputStream());
 		} else {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Sending HTML for listing ["
 						+ request.getRequestURI() + "]");
 			}
 			request.setAttribute("uri", uri);
 			request.setAttribute("dir", dir);
 			request.setAttribute("writeAllowed", this.getWriteAllowed(request));
 
 			request.getRequestDispatcher("/WEB-INF/jsp/webfilez.jsp").forward(
 					request, response);
 		}
 	}
 
 	private boolean isClientAbortException(IOException e) {
 		final Throwable cause = e.getCause();
 		return cause instanceof SocketException
 				&& "Broken pipe".equals(cause.getMessage());
 	}
 
 	private void sendFile(Path file, OutputStream out, Range range)
 			throws FileNotFoundException, IOException {
 		final long length = Files.size(file);
 		if (logger.isTraceEnabled()) {
 			logger.trace(String.format("Sending bytes %d-%d/%d of file %s",
 					range == null ? 0 : range.getStart(),
 					range == null ? max(length - 1, 0) : range.getEnd(),
 					length, file));
 		}
 		try (final InputStream in = Files.newInputStream(file)) {
 			if (range != null) {
 				final long skipped = in.skip(range.getStart());
 				if (skipped < range.getStart()) {
 					throw new IOException("Tried to skip [" + range.getStart()
 							+ "] but actually skipped [" + skipped + "] on ["
 							+ file + "]");
 				}
 			}
 			long bytesToRead = range == null ? length : range.getBytesToRead();
 			final byte[] buffer = new byte[this.bufferSize];
 			for (int bytesRead; bytesToRead > 0
 					&& (bytesRead = in.read(buffer, 0,
 							(int) min(buffer.length, bytesToRead))) > 0;) {
 				try {
 					out.write(buffer, 0, bytesRead);
 					bytesToRead -= bytesRead;
 				} catch (IOException e) {
 					if (isClientAbortException(e)) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Client aborted the connection while sending file ["
 									+ file + "]. Bailing out");
 						}
 						return;
 					} else {
 						throw e;
 					}
 				}
 			}
 			try {
 				out.flush();
 				if (logger.isTraceEnabled()) {
 					logger.trace("Sent " + file);
 				}
 			} catch (IOException e) {
 				if (isClientAbortException(e)) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Client aborted the connection while flushing file ["
 								+ file + "]. Bailing out");
 					}
 					return;
 				} else {
 					throw e;
 				}
 			}
 		}
 	}
 
 	private void handleDownload(HttpServletRequest request,
 			HttpServletResponse response, Path file) throws IOException,
 			ServletException {
 		if (!Files.isReadable(file)) {
 			this.refuseRequest(request, response,
 					HttpServletResponse.SC_FORBIDDEN,
 					"Cannot send file [" + file + "] for request URI ["
 							+ request.getRequestURI()
 							+ "]; file cannot be read");
 		} else {
 			final long length = Files.size(file);
 			final long lastModified = Files.getLastModifiedTime(file).toMillis();
 			final String eTag = generateETag(length, lastModified);
 			final String contentType = getMimeType(file);
 			if (lastModified >= 0) {
 				response.setDateHeader("Last-Modified", lastModified);
 			}
 			if (eTag != null) {
 				response.setHeader("ETag", eTag);
 			}
 			response.setHeader("Accept-Ranges", "bytes");
 			final List<Range> ranges = parseRange(request, response, eTag,
 					lastModified, length);
 			if (ranges == null) {
 				if (logger.isWarnEnabled()) {
 					logger.warn("Cannot handle download of [" + file
 							+ "]. Problem with ranges.");
 				}
 				return;
 			} else if (ifNoneMatch(request, eTag)
 					|| ifModifiedSince(request, lastModified)) {
 				if (ranges.isEmpty()) {
 					if (logger.isTraceEnabled()) {
 						logger.trace(request.getMethod()
 								+ " request for the entire "
 								+ request.getRequestURI() + " of type "
 								+ contentType);
 					}
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType(contentType);
 					setContentLength(response, length);
 					if (!isHead(request)) {
 						sendFile(file, response.getOutputStream(), null);
 					}
 				} else if (ranges.size() == 1) {
 					final Range range = ranges.get(0);
 					if (logger.isTraceEnabled()) {
 						logger.trace(request.getMethod() + " request for "
 								+ range.toContentRangeHeaderValue() + " of "
 								+ request.getRequestURI() + " of type "
 								+ contentType);
 					}
 					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
 					response.addHeader("Content-Range",
 							range.toContentRangeHeaderValue());
 					setContentLength(response, range.getBytesToRead());
 					response.setContentType(contentType);
 					if (!isHead(request)) {
 						sendFile(file, response.getOutputStream(), range);
 					}
 				} else if (ranges.size() > 1) {
 					if (logger.isTraceEnabled()) {
 						logger.trace(request.getMethod() + " request for "
 								+ ranges + " of " + request.getRequestURI()
 								+ " of type " + contentType);
 					}
 					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
 					response.setContentType("multipart/byteranges; boundary="
 							+ MULTIPART_BOUNDARY);
 					if (!isHead(request)) {
 						final ServletOutputStream out = response.getOutputStream();
 						for (Range range : ranges) {
 							// Writing MIME header.
 							out.println();
 							out.println("--" + MULTIPART_BOUNDARY);
 							out.println("Content-Type: " + contentType);
 							out.println("Content-Range: "
 									+ range.toContentRangeHeaderValue());
 							out.println();
 							sendFile(file, out, range);
 						}
 					}
 				}
 			} else {
 				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
 			}
 		}
 	}
 
 	private void handleUpload(Path dir, HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException,
 			JSONException {
 		final Collection<Part> parts = request.getParts();
 		if (logger.isTraceEnabled()) {
 			logger.trace("Uploading " + parts.size() + " file(s)");
 		}
 		response.setContentType("text/plain");
 		response.setCharacterEncoding("UTF-8");
 		final Collection<Path> uploadedFiles = new LinkedList<>();
 		final long quota = this.getQuota(request);
 		long usage = quota > 0 ? this.getUsage(this.getBasePath(request, true))
 				: 0;
 		for (Part part : parts) {
 			final String filename = getFileName(part);
 			final Path file = this.resolveSafe(dir, filename);
 			if (file == null) {
 				this.refuseBadRequest(request, response,
 						"Detected illegal/invalid filename [" + filename
 								+ "]. Aborting.");
 				return;
 			} else {
 				final long partSize = part.getSize();
 				if (quota > 0 && usage + partSize > quota) {
 					this.refuseOverQuotaRequest(request, response, "upload "
 							+ file, partSize, usage, quota);
 					return;
 				}
 				if (handleSingleUpload(part.getInputStream(), partSize, file,
 						request, response)) {
 					uploadedFiles.add(file);
 					usage += Files.size(file);
 				} else {
 					return;
 				}
 			}
 		}
 		sendFileInfoResponse(uploadedFiles, response);
 	}
 
 	private boolean handleSingleUpload(final Path target,
 			final HttpServletRequest request, final HttpServletResponse response)
 			throws ServletException, IOException {
 		InputStream in = null;
 		long contentLength = 0;
 		if (isMultiPartRequest(request)) {
 			final Collection<Part> parts;
 			try {
 				parts = request.getParts();
 			} catch (IOException e) {
 				Files.deleteIfExists(target);
 				this.refuseBadRequest(request, response,
 						"Failed to parse data parts from the client ["
 								+ request.getRemoteAddr()
 								+ "] while writing file [" + target
 								+ "]. Aborting.");
 				return false;
 			}
 			final String fileName = target.getFileName().toString();
 			if (parts.size() == 1) {
 				final Part part = parts.iterator().next();
 				final String partName = getFileName(part);
 				if (!fileName.equals(partName)) {
 					if (logger.isWarnEnabled()) {
 						logger.warn("Expecting filename [" + fileName
 								+ "] but got [" + partName + "]. Ignoring.");
 					}
 				}
 				contentLength = part.getSize();
 				in = part.getInputStream();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Uploading [" + contentLength
 							+ "] bytes from part to [" + target + "]");
 				}
 			} else {
 				this.refuseBadRequest(request, response,
 						"Expecting to upload one part to  [" + fileName
 								+ "] but got [" + parts.size() + "]. Aborting.");
 				return false;
 			}
 		}
 		if (in == null) {
 			contentLength = request.getContentLength();
 			if (contentLength > 0) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Uploading [" + contentLength
 							+ "] bytes from requst to [" + target + "]");
 				}
 				in = request.getInputStream();
 			}
 		}
 		final long quota = this.getQuota(request);
 		if (quota > 0) {
 			final long usage = this.getUsage(this.getBasePath(request, true));
 			if (usage + contentLength > quota) {
 				refuseOverQuotaRequest(request, response, "upload " + target,
 						contentLength, usage, quota);
 				return false;
 			}
 		}
 		Path parentDir = target.getParent();
 		if (!Files.exists(parentDir)) {
 			Files.createDirectories(parentDir);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Created parent directory for [" + target + "]");
 			}
 		}
		if (in == null || contentLength == 0) {
 			Files.createFile(target);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Created new file [" + target + "]");
 			}
 		} else {
 			this.handleSingleUpload(in, contentLength, target, request,
 					response);
 		}
 		return true;
 	}
 
 	private boolean handleSingleUpload(final InputStream sourceStream,
 			final long sourceLength, final Path target,
 			final HttpServletRequest request, final HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			try (final OutputStream out = Files.newOutputStream(target)) {
 				long bytesToRead = sourceLength;
 				byte[] buffer = new byte[this.bufferSize];
 				while (bytesToRead > 0) {
 					int numRead;
 					try {
 						numRead = sourceStream.read(buffer, 0,
 								(int) min(buffer.length, bytesToRead));
 						if (numRead == -1) {
 							break;
 						}
 						bytesToRead -= numRead;
 					} catch (IOException e) {
 						Files.deleteIfExists(target);
 						this.refuseBadRequest(request, response,
 								"Failed to read data from the client ["
 										+ request.getRemoteAddr()
 										+ "] while writing file [" + target
 										+ "]. Aborting.", e);
 						return false;
 					}
 					out.write(buffer, 0, numRead);
 				}
 				if (bytesToRead != 0) {
 					if (logger.isWarnEnabled()) {
 						logger.warn("Uploaded [" + (sourceLength - bytesToRead)
 								+ "] bytes to [" + target + "] but expected ["
 								+ sourceLength + "]. Ignoring.");
 					}
 				} else {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Uploaded [" + sourceLength
 								+ "] bytes to [" + target + "]");
 					}
 				}
 				return true;
 			}
 		} finally {
 			sourceStream.close();
 		}
 	}
 
 	private void handleZip(Path dir, HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException,
 			JSONException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to zip files in directory [" + dir
 					+ "]");
 		}
 		final Path zipFile;
 		final List<Path> files = this.getFilesFromRequest(request, dir);
 		if (files.size() == 1) {
 			final Path file = files.get(0);
 			zipFile = getUniqueFileInDirectory(dir,
 					file.getFileName().toString(), ".zip");
 			if (Files.isDirectory(file)) {
 				if (logger.isTraceEnabled()) {
 					logger.trace("Zipping directory [" + file + "] to ["
 							+ zipFile + "]");
 				}
 				zipDirectory(file, zipFile);
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("Zipping file [" + file + "] to [" + zipFile
 							+ "]");
 				}
 				zipFile(file, zipFile);
 			}
 		} else {
 			zipFile = getUniqueFileInDirectory(dir, "Archive", ".zip");
 			if (logger.isTraceEnabled()) {
 				logger.trace("Zipping files [" + files + "] to [" + zipFile
 						+ "]");
 			}
 			zipFiles(dir, files, zipFile);
 		}
 		this.sendFileInfoResponse(zipFile, response);
 	}
 
 	private void handleUnzip(Path file, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to unzip file [" + file + "]");
 		}
 		final long quota = this.getQuota(request);
 		if (quota > 0) {
 			final long usage = this.getUsage(this.getBasePath(request, true));
 			final long sizeOfZip = sizeOfZip(file);
 			if (sizeOfZip + usage > quota) {
 				refuseOverQuotaRequest(request, response, "unzip " + file,
 						sizeOfZip, usage, quota);
 				return;
 			}
 		}
 		final Path dir = file.getParent();
 		final Collection<Path> immediateCreatedFiles = new LinkedList<>();
 		unzip(file, dir, new PathHandler() {
 			@Override
 			public void handle(Path createdFile) throws IOException {
 				if (Files.isSameFile(createdFile.getParent(), dir)) {
 					immediateCreatedFiles.add(createdFile);
 				}
 			}
 		});
 		this.sendFileInfoResponse(immediateCreatedFiles, response);
 	}
 
 	private void handleRename(Path file, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to rename file [" + file + "]");
 		}
 		final String newName = request.getParameter("newName");
 		final Path newFile = file.resolveSibling(newName);
 		if (Files.exists(newFile)) {
 			refuseBadRequest(request, response, "Cannot rename [" + file
 					+ "] because newFile=[" + newFile + "] already exists");
 		} else {
 			Files.move(file, newFile);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Renamed [" + file + "] to [" + newFile + "]");
 			}
 			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		}
 	}
 
 	private boolean isOverwrite(HttpServletRequest request) {
 		return Boolean.valueOf(request.getParameter("overwrite")).booleanValue();
 	}
 
 	private void handleCopy(Path targetDir, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to copy file to directory ["
 					+ targetDir + "]");
 		}
 		try {
 			final Path source = this.getSourcePath(request);
 			if (Files.exists(source)) {
 				final Path target = source.getParent().equals(targetDir) ? getUniqueFileInDirectory(
 						targetDir, source.getFileName().toString())
 						: targetDir.resolve(source.getFileName());
 				if (Files.exists(target) && !isOverwrite(request)) {
 					this.refuseBadRequest(request, response,
 							"Refusing to copy [" + source + "] to [" + target
 									+ "], which already exists.");
 				} else {
 					final long quota = this.getQuota(request);
 					if (quota > 0) {
 						final long usage = this.getUsage(this.getBasePath(
 								request, true));
 						final long need = size(source);
 						if (usage + need > quota) {
 							refuseOverQuotaRequest(request, response, "copy "
 									+ source + " to " + target + target, need,
 									usage, quota);
 							return;
 						}
 					}
 					copy(source, target);
 					if (logger.isDebugEnabled()) {
 						logger.debug("Copied [" + source + "] to [" + target
 								+ "]");
 					}
 					this.sendFileInfoResponse(target, response);
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "Cannot copy ["
 								+ source + "] to directory [" + targetDir
 								+ "] because the source file does not exist.");
 			}
 		} catch (IllegalArgumentException e) {
 			this.refuseBadRequest(request, response, e.getMessage());
 		}
 	}
 
 	private void handleMove(Path targetDir, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to move file to directory ["
 					+ targetDir + "]");
 		}
 		try {
 			final Path source = this.getSourcePath(request);
 			if (Files.exists(source)) {
 				final Path target = targetDir.resolve(source.getFileName());
 				if (Files.isSameFile(source, target)) {
 					this.refuseBadRequest(request, response, "Cannot move ["
 							+ source + "] over itself.");
 				} else {
 					if (Files.exists(target)) {
 						if (isOverwrite(request)) {
 							if (logger.isDebugEnabled()) {
 								logger.debug("Deleting [" + target
 										+ "] in preparation to move [" + source
 										+ "] over it");
 							}
 							delete(target);
 						} else {
 							this.refuseBadRequest(request, response,
 									"Refusing to move [" + source + "] to ["
 											+ target
 											+ "], which already exists.");
 							return;
 						}
 					}
 					Files.move(source, target,
 							StandardCopyOption.REPLACE_EXISTING);
 					if (logger.isDebugEnabled()) {
 						logger.debug("Moved [" + source + "] to [" + target
 								+ "]");
 					}
 					this.sendFileInfoResponse(target, response);
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "Cannot move ["
 								+ source + "] to directory [" + targetDir
 								+ "] because the source file does not exist.");
 			}
 		} catch (IllegalArgumentException e) {
 			this.refuseBadRequest(request, response, e.getMessage());
 		}
 	}
 
 	private List<Path> getFilesFromRequest(HttpServletRequest req, Path dir) {
 		final String[] filenames = req.getParameterValues("file");
 		final List<Path> paths;
 		if (filenames == null || filenames.length == 0) {
 			paths = Collections.emptyList();
 		} else {
 			paths = new ArrayList<>(filenames.length);
 			for (String filename : filenames) {
 				Path path = this.resolveSafe(dir, filename);
 				if (path != null) {
 					if (Files.exists(path)) {
 						paths.add(path);
 					} else {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Request-specified file [" + path
 									+ "] does not exist. Skipping.");
 						}
 					}
 				}
 			}
 		}
 		if (logger.isTraceEnabled()) {
 			logger.trace("Got [" + paths.size() + "] file(s) from the request");
 		}
 		return paths;
 	}
 
 	private Path getRequestFile(HttpServletRequest request)
 			throws UnsupportedEncodingException {
 		return this.resolvePath(request.getRequestURI());
 	}
 
 	private Path resolvePath(final String requestedPath)
 			throws UnsupportedEncodingException {
 		String updatedRequestedPath = SearchAndReplace.searchAndReplace(
 				URLDecoder.decode(requestedPath, "UTF-8"), this.rewriteRules);
 		final Path resolvedPath = FileSystems.getDefault().getPath(
 				this.rootDir, updatedRequestedPath);
 		if (logger.isTraceEnabled()) {
 			logger.trace("Resolved path [" + requestedPath + "] to ["
 					+ resolvedPath + "]");
 		}
 		return resolvedPath;
 	}
 
 	private Path getSourcePath(HttpServletRequest request)
 			throws UnsupportedEncodingException {
 		final String basePath = this.getBasePath(request, false);
 		final String targetPath = request.getRequestURI();
 		final String sourcePath = request.getParameter("source");
 
 		if (sourcePath == null) {
 			throw new IllegalArgumentException(
 					"No [source] parameter specified");
 		} else if (!sourcePath.startsWith(basePath)) {
 			// refuse requests such as /WEB-INF/web.xml
 			throw new IllegalArgumentException("SourcePath [" + sourcePath
 					+ "] is outside basePath [" + basePath + "]");
 		} else if (INVALID_SOURCE_PATH_PATTERN.matcher(sourcePath).find()) {
 			// refuse requests such as /class/123/files/../../../WEB-INF/web.xml
 			throw new IllegalArgumentException("SourcePath [" + sourcePath
 					+ "] is not legal");
 		} else if (targetPath.startsWith(sourcePath)) {
 			// refuse requests such as copying /class/123/files/foo/ to
 			// /class/123/files/foo/ or /class/123/files/foo/bar/
 			throw new IllegalArgumentException("TargetPath [" + targetPath
 					+ "] starts with sourcePath [" + sourcePath + "]");
 		} else {
 			return this.resolvePath(sourcePath);
 		}
 	}
 
 	private Path resolveSafe(Path dir, String filename) {
 		if (dir == null) {
 			if (logger.isWarnEnabled()) {
 				logger.warn("No dir. Skipping.");
 			}
 		} else if (filename == null) {
 			if (logger.isWarnEnabled()) {
 				logger.warn("No file. Skipping.");
 			}
 		} else if (INVALID_FILENAME_PATTERN.matcher(filename).find()) {
 			if (logger.isWarnEnabled()) {
 				logger.warn("Invalid filename detected [" + filename
 						+ "]. Skipping.");
 			}
 		} else {
 			return dir.resolve(filename);
 		}
 		return null;
 	}
 
 	private String getMimeType(Path path) throws IOException {
 		String mimeType;
 		if (Files.isDirectory(path)) {
 			mimeType = this.directoryMimeType;
 		} else {
 			mimeType = super.getServletContext().getMimeType(
 					path.getFileName().toString());
 			if (mimeType == null) {
 				mimeType = Files.probeContentType(path);
 			}
 			if (mimeType == null) {
 				mimeType = this.defaultMimeType;
 			}
 		}
 		return mimeType;
 	}
 
 	private long writeFileInfoToJson(Iterable<Path> paths, JSONWriter jsonWriter)
 			throws JSONException, IOException {
 		long size = 0;
 		jsonWriter.array();
 		for (Path path : paths) {
 			size += writeFileInfoToJson(path, jsonWriter);
 		}
 		jsonWriter.endArray();
 		return size;
 	}
 
 	private long writeFileInfoToJson(Path path, JSONWriter jsonWriter)
 			throws JSONException, IOException {
 		final long size = size(path);
 		final long lastModified = Files.getLastModifiedTime(path).toMillis();
 		jsonWriter.object();
 		jsonWriter.key("name").value(path.getName(path.getNameCount() - 1));
 		jsonWriter.key("type").value(getMimeType(path));
 		jsonWriter.key("size").value(size);
 		jsonWriter.key("lastModified").value(lastModified);
 		jsonWriter.key("eTag").value(generateETag(size, lastModified));
 		jsonWriter.endObject();
 		return size;
 	}
 
 	private void sendFileInfoResponse(Path file, HttpServletResponse response)
 			throws IOException, JSONException {
 		response.setContentType("application/json");
 		JSONWriter jsonWriter = new JSONWriter(response.getWriter());
 		writeFileInfoToJson(file, jsonWriter);
 		response.flushBuffer();
 	}
 
 	private void sendFileInfoResponse(Collection<Path> paths,
 			HttpServletResponse response) throws IOException, JSONException {
 		response.setContentType("application/json");
 		writeFileInfoToJson(paths, new JSONWriter(response.getWriter()));
 		response.flushBuffer();
 	}
 
 	private void refuseRequest(HttpServletRequest request,
 			HttpServletResponse response, int responseCode, String msg)
 			throws ServletException, IOException {
 		this.refuseRequest(request, response, responseCode, msg, null);
 	}
 
 	private void refuseRequest(HttpServletRequest request,
 			HttpServletResponse response, int responseCode, String msg,
 			Throwable cause) throws ServletException, IOException {
 		if (logger.isWarnEnabled()) {
 			logger.warn("Refusing request with [" + responseCode + "]. " + msg,
 					cause);
 		}
 		response.setStatus(responseCode);
 	}
 
 	private void refuseBadRequest(HttpServletRequest request,
 			HttpServletResponse response, String msg) throws ServletException,
 			IOException {
 		this.refuseBadRequest(request, response, msg, null);
 	}
 
 	private void refuseBadRequest(HttpServletRequest request,
 			HttpServletResponse response, String msg, Throwable cause)
 			throws ServletException, IOException {
 		refuseRequest(request, response, HttpServletResponse.SC_BAD_REQUEST,
 				msg);
 	}
 
 	private void refuseOverQuotaRequest(HttpServletRequest request,
 			HttpServletResponse response, String msg, long need, long usage,
 			long quota) throws ServletException, IOException {
 		this.refuseRequest(request, response,
 				HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Refusing to "
 						+ msg + ". We need [" + need
 						+ "] bytes, we are using [" + usage
 						+ "], and we have available [" + (quota - usage)
 						+ "] before we reach our quota[" + quota + "]");
 	}
 
 	private void sendServerFailure(HttpServletRequest request,
 			HttpServletResponse response, String msg, Throwable cause)
 			throws ServletException, IOException {
 		logger.error(msg, cause);
 		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 	}
 
 	private void sendServerFailure(HttpServletRequest request,
 			HttpServletResponse response, String msg) throws ServletException,
 			IOException {
 		this.sendServerFailure(request, response, msg, null);
 	}
 
 	private long getQuota(HttpServletRequest request) {
 		Long quota = (Long) request.getAttribute(Constants.QUOTA);
 		if (quota == null) {
 			return 0;
 		} else {
 			return quota;
 		}
 	}
 
 	private long getUsage(String basePath) throws IOException {
 		long t = 0;
 		final Path path = this.resolvePath(basePath);
 		if (logger.isTraceEnabled()) {
 			t = System.nanoTime();
 		}
 		final long usage = size(path);
 		if (logger.isTraceEnabled()) {
 			t = System.nanoTime() - t;
 			logger.trace(path + " uses " + usage + " bytes (computed in "
 					+ (t / 1000000) + " ms)");
 		}
 		return usage;
 	}
 
 	private String getBasePath(HttpServletRequest request, boolean strict) {
 		String basePath = (String) request.getAttribute(Constants.BASE_PATH_ATTR_NAME);
 		if (basePath == null) {
 			basePath = "/";
 		}
 		if (strict) {
 			Matcher matcher = this.basePathPattern.matcher(basePath);
 			if (!matcher.matches()) {
 				if (matcher.reset(request.getRequestURI()).matches()) {
 					basePath = matcher.group(1);
 				} else {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Cannot extract strict base-path where ["
 								+ request.getRequestURI()
 								+ "] does not match ["
 								+ this.basePathPattern.pattern() + "]");
 					}
 				}
 			}
 		}
 		if (logger.isTraceEnabled()) {
 			logger.trace("Got base-path [" + basePath + "]");
 		}
 		return basePath;
 	}
 
 	private Boolean getWriteAllowed(HttpServletRequest request) {
 		return (Boolean) request.getAttribute(Constants.WRITE_ALLOWED);
 	}
 
 	private boolean fileExistsOrItIsTheBasePathAndIsCreated(Path file,
 			String uri, String basePath) throws IOException {
 		if (Files.exists(file)) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("File/directory [" + file + "] already exists");
 			}
 			return true;
 		} else if (uri != null && uri.equals(basePath)) {
 			Files.createDirectories(file);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Created the base dir [" + file + "]");
 			}
 			return true;
 		} else {
 			if (logger.isTraceEnabled()) {
 				logger.trace("File/directory [" + file
 						+ "] does not exist and uri=[" + uri
 						+ "] is not the same as the base path=[" + basePath
 						+ "]");
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "WebFilezServlet [bufferSize=" + bufferSize
 				+ ", defaultMimeType=" + defaultMimeType
 				+ ", directoryMimeType=" + directoryMimeType
 				+ ", readmeFileName=" + readmeFileName
 				+ ", readmeFileMaxLength=" + readmeFileMaxLength
 				+ ", readmeFileCharset=" + readmeFileCharset + ", rootDir="
 				+ rootDir + ", rewriteRules=" + rewriteRules
 				+ ", basePathPattern=" + basePathPattern + "]";
 	}
 }
