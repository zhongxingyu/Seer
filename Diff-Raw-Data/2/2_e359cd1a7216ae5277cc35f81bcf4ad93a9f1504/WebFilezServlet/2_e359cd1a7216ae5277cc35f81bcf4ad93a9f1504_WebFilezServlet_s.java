 package com.marakana.webfilez;
 
 import static com.marakana.webfilez.FileUtil.copy;
 import static com.marakana.webfilez.FileUtil.delete;
 import static com.marakana.webfilez.FileUtil.getBackupFile;
 import static com.marakana.webfilez.FileUtil.getUniqueFileInDirectory;
 import static com.marakana.webfilez.FileUtil.move;
 import static com.marakana.webfilez.FileUtil.size;
 import static com.marakana.webfilez.FileUtil.unzip;
 import static com.marakana.webfilez.FileUtil.zipDirectory;
 import static com.marakana.webfilez.FileUtil.zipFile;
 import static com.marakana.webfilez.FileUtil.zipFiles;
 import static com.marakana.webfilez.WebUtil.asParams;
 import static com.marakana.webfilez.WebUtil.getFileName;
 import static com.marakana.webfilez.WebUtil.getParentUriPath;
 import static com.marakana.webfilez.WebUtil.ifModifiedSince;
 import static com.marakana.webfilez.WebUtil.ifUnmodifiedSince;
 import static com.marakana.webfilez.WebUtil.isHead;
 import static com.marakana.webfilez.WebUtil.isJson;
 import static com.marakana.webfilez.WebUtil.isMultiPartRequest;
 import static com.marakana.webfilez.WebUtil.isZip;
 import static com.marakana.webfilez.WebUtil.setContentHeaders;
 import static com.marakana.webfilez.WebUtil.setNoCacheHeaders;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import org.json.JSONException;
 import org.json.JSONWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.marakana.webfilez.FileUtil.FileVisitor;
 
 public final class WebFilezServlet extends HttpServlet {
 	private static final Pattern INVALID_SOURCE_PATH_PATTERN = Pattern
 			.compile("(\\.\\.)");
 	private static final Pattern INVALID_FILENAME_PATTERN = Pattern
 			.compile("(\\.\\.)|/|\\\\");
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = LoggerFactory
 			.getLogger(WebFilezServlet.class);
 
 	private int outputBufferSize;
 
 	private int inputBufferSize;
 
 	private String defaultMimeType;
 
 	private String directoryMimeType;
 
 	private String readmeFileName;
 
 	private long readmeFileMaxLength;
 
 	private Charset readmeFileCharset;
 
 	private File rootDir;
 
 	private Collection<SearchAndReplace> rewriteRules;
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			Context ctx = new InitialContext();
 			try {
 				final Params params = asParams((Context) ctx
 						.lookup("java:comp/env/"));
 				this.inputBufferSize = params.getInteger("input-buffer-size",
 						2048);
 				this.outputBufferSize = params.getInteger("output-buffer-size",
 						2048);
 				this.directoryMimeType = params.getString(
 						"directory-mime-type", "x-directory/normal");
 				this.defaultMimeType = params.getString("default-mime-type",
 						"application/octet-stream");
 				this.readmeFileName = params.getString("readme-file-name",
 						"README.html");
 				this.readmeFileMaxLength = params.getInteger(
 						"readme-file-max-length", 5 * 1024 * 1024);
 				this.readmeFileCharset = Charset.forName(params.getString(
 						"readme-file-charset", "UTF-8"));
 				String rootDir = params.getString("root-dir");
 				this.rootDir = rootDir == null ? new File(super
 						.getServletContext().getRealPath(".")) : new File(
 						rootDir);
 				this.rewriteRules = SearchAndReplace.parse(params
 						.getString("rewrite-rules"));
 
 			} finally {
 				ctx.close();
 			}
 		} catch (NamingException e) {
 			throw new ServletException("Failed to init", e);
 		}
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String uri = request.getRequestURI();
 		String basePath = this.getBasePath(request);
 		File file = this.getRequestFile(request);
 		if (file.exists() || (uri.equals(basePath) && makeBaseDir(file))) {
 			if (file.isDirectory()) {
 				if (uri.endsWith("/")) {
 					if (isZip(request)
 							|| "zip_download".equals(request
 									.getParameter("_action"))) {
 						try {
 							this.handleZipDownloadRequest(request, response,
 									file);
 						} catch (IOException e) {
 							this.sendServerFailure(
 									request,
 									response,
 									"Failed to send ZIP of files in ["
 											+ file.getAbsolutePath() + "]", e);
 						}
 					} else {
 						try {
 							this.handleList(request, response, file, basePath);
 						} catch (JSONException e) {
 							this.sendServerFailure(request, response,
 									"Failed to send listing as JSON for ["
 											+ file.getAbsolutePath() + "]", e);
 						}
 					}
 				} else {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Adding trailing slash to [" + uri + "]");
 					}
 					response.sendRedirect(uri + "/");
 				}
 			} else if (file.isFile()) {
 				this.handleDownload(request, response, file);
 			} else {
 				this.sendServerFailure(request, response,
 						"Not a file or a directory [" + file.getAbsolutePath()
 								+ "] in response to [" + uri + "]");
 			}
 		} else {
 
 			// search for the closest folder that does exist
 			// until we get to directory root
 			for (; !file.equals(this.rootDir) && !file.exists() && uri != null
 					&& !uri.equals(basePath); file = file.getParentFile(), uri = getParentUriPath(uri)) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Trying to see if [" + uri + "] exists.");
 				}
 			}
 			if (uri != null) {
 				if (uri.equals(basePath)) {
 					this.sendServerFailure(request, response,
 							"Failed to access/create " + file.getAbsolutePath());
 				} else {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Attempting to recover. Redirecting to: "
 								+ uri);
 					}
 					response.sendRedirect(uri);
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "No such file ["
 								+ file.getAbsolutePath() + "] in response to ["
 								+ uri + "]");
 			}
 
 		}
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		File file = getRequestFile(request);
 		if (logger.isDebugEnabled()) {
 			logger.debug("Processing request to delete ["
 					+ file.getAbsolutePath() + "]");
 		}
 		if (file.exists()) {
 			if (!file.isFile()
 					|| ifUnmodifiedSince(request, file.lastModified())) {
 				if (delete(file)) {
 					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 				} else {
 					this.sendServerFailure(request, response,
 							"Failed to delete [" + file.getAbsolutePath() + "]");
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_PRECONDITION_FAILED,
 						"Refusing to delete file for which precondition failed: ["
 								+ file.getAbsolutePath() + "]");
 			}
 		} else {
 			this.refuseRequest(
 					request,
 					response,
 					HttpServletResponse.SC_NOT_FOUND,
 					"Cannot delete a file that does not exist ["
 							+ file.getAbsolutePath() + "]");
 		}
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		File file = this.getRequestFile(request);
 		String type = request.getContentType();
 		boolean makeDirRequest = this.directoryMimeType.equals(type);
 		int responseCode;
 		if (file.exists()) {
 			if (makeDirRequest) {
 				if (file.isDirectory()) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Directory already exists ["
 								+ file.getAbsolutePath() + "]. Ignoring.");
 					}
 				} else {
 					this.refuseRequest(request, response,
 							HttpServletResponse.SC_CONFLICT,
 							"Cannot create directory since a file with the same name already exists ["
 									+ file.getAbsolutePath() + "]");
 					return;
 				}
 			} else {
 				if (file.isDirectory()) {
 					this.refuseRequest(request, response,
 							HttpServletResponse.SC_CONFLICT,
 							"Cannot create file since a directory with the same name already exists ["
 									+ file.getAbsolutePath() + "]");
 					return;
 				} else {
 					if (ifUnmodifiedSince(request, file.lastModified())) {
 						this.handleUploadToFile(file, request, response);
 					} else {
 						this.refuseRequest(request, response,
 								HttpServletResponse.SC_PRECONDITION_FAILED,
 								"Cannot modify file for which precondition failed: ["
 										+ file.getAbsolutePath() + "]");
 						return;
 					}
 				}
 			}
 			responseCode = HttpServletResponse.SC_OK;
 		} else if (!file.getParentFile().exists()) {
 			this.refuseBadRequest(request, response,
 					"The parent directory for [" + file.getAbsolutePath()
 							+ "] does not exist");
 			return;
 		} else {
 			if (makeDirRequest) {
 				if (file.mkdir()) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Created directory ["
 								+ file.getAbsolutePath() + "]");
 					}
 				} else {
 					this.sendServerFailure(
 							request,
 							response,
 							"Failed to create directory ["
 									+ file.getAbsolutePath() + "]");
 					return;
 				}
 			} else {
 				if (file.createNewFile()) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Created file [" + file.getAbsolutePath()
 								+ "]");
 					}
 					this.handleUploadToFile(file, request, response);
 				} else {
 					this.sendServerFailure(request, response,
 							"Failed to create file [" + file.getAbsolutePath()
 									+ "]");
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
 								+ file.getAbsolutePath() + "]", e);
 			}
 			response.resetBuffer();
 		}
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String action = request.getParameter("_action");
 		if (action == null) {
 			refuseBadRequest(
 					request,
 					response,
 					"No action specified for POST to "
 							+ request.getRequestURI());
 		} else {
 			File file = getRequestFile(request);
 			if (file.exists()) {
 				try {
 					if (file.isDirectory()) {
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
 					} else if (file.isFile()) {
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
 						this.refuseBadRequest(
 								request,
 								response,
 								"Don't know how to handle POST to file "
 										+ file.getAbsolutePath());
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
 								+ file.getAbsolutePath());
 			}
 		}
 	}
 
 	private void handleZipDownloadRequest(HttpServletRequest request,
 			HttpServletResponse response, File dir) throws ServletException,
 			IOException {
 		if (logger.isTraceEnabled()) {
 			logger.trace("ZIP-downloading files in [" + dir.getAbsolutePath()
 					+ "]");
 		}
 		List<File> files = this.getFilesFromRequest(request, dir);
 		String filename = request.getParameter("filename");
 		if (files.isEmpty()) {
 			this.refuseBadRequest(request, response,
 					"Select at least one file to zip-download");
 		} else {
 			if (filename == null) {
 				filename = (files.size() == 1 ? files.get(0) : dir).getName()
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
 			HttpServletResponse response, File dir, String basePath)
 			throws IOException, ServletException, JSONException {
 		String uri = request.getRequestURI();
 		setNoCacheHeaders(response);
 		if (isJson(request)) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Listing files in [" + dir.getAbsolutePath() + "]");
 			}
 			response.setContentType("application/json");
 			response.setHeader("Accept-Ranges", "none");
 			long totalSize = 0;
 			File readmeFile = null;
 			JSONWriter jsonWriter = new JSONWriter(response.getWriter());
 			jsonWriter.object();
 			jsonWriter.key("files").array();
 			File[] files = dir.listFiles();
 			for (File f : files) {
 				totalSize += writeFileInfoToJson(f, jsonWriter);
 				if (f.getName().equals(this.readmeFileName)) {
 					readmeFile = f;
 				}
 			}
 			jsonWriter.endArray();
 			jsonWriter.key("uri").value(uri);
 			jsonWriter.key("name").value(dir.getName());
 			jsonWriter.key("type").value(this.directoryMimeType);
 			jsonWriter.key("size").value(totalSize);
 			jsonWriter.key("lastModified").value(dir.lastModified());
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
 				if (readmeFile.length() > this.readmeFileMaxLength) {
 					if (logger.isWarnEnabled()) {
 						logger.warn("README file ["
 								+ readmeFile.getAbsolutePath() + "] size ["
 								+ readmeFile.length() + "] exceeds max size ["
 								+ this.readmeFileMaxLength + "]. Ignoring.");
 					}
 				} else {
 					try {
 						String readme = FileUtil.readFileToString(readmeFile,
 								this.readmeFileCharset);
 						jsonWriter.key("readme").value(readme);
 					} catch (IOException e) {
 						if (logger.isWarnEnabled()) {
 							logger.warn("Failed to read the README file ["
 									+ readmeFile.getAbsolutePath()
 									+ "]. Ignoring.", e);
 						}
 					}
 				}
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("No README file present for uri [" + uri
 							+ "] and dir [" + dir.getAbsolutePath() + "]");
 				}
 			}
 			jsonWriter.endObject();
 		} else {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Sending to listing JSP to handle ["
 						+ request.getRequestURI() + "]");
 			}
 			request.setAttribute("uri", uri);
 			request.setAttribute("dir", dir);
 			request.setAttribute("writeAllowed", this.getWriteAllowed(request));
 			request.getRequestDispatcher("/WEB-INF/jsp/listing.jsp").forward(
 					request, response);
 		}
 	}
 
 	private void handleDownload(HttpServletRequest request,
 			HttpServletResponse response, File file) throws IOException,
 			ServletException {
 		if (!file.canRead()) {
 			this.refuseRequest(request, response,
 					HttpServletResponse.SC_FORBIDDEN, "Cannot send file ["
 							+ file.getAbsolutePath() + "] for request URI ["
 							+ request.getRequestURI()
 							+ "]; file cannot be read");
 		} else {
 			long lastModified = file.lastModified();
 			if (ifModifiedSince(request, lastModified)) {
 				int length = (int) file.length();
 				String mimeType = super.getServletContext().getMimeType(
 						file.getName());
 				if (mimeType == null) {
 					mimeType = this.defaultMimeType;
 				}
 				setContentHeaders(response, length, lastModified, mimeType);
 				// TODO: add support for Ranges download!!!
 				response.setHeader("Accept-Ranges", "none");
 				if (!isHead(request)) {
 					if (logger.isTraceEnabled()) {
 						logger.trace(String
 								.format("Sending file %s (length=%d, mime-type=%s, last-modified=%d) in response to request %s",
 										file.getAbsolutePath(), length,
 										mimeType, lastModified,
 										request.getRequestURI()));
 					}
 					response.setBufferSize(this.outputBufferSize);
 
 					try (InputStream in = new BufferedInputStream(
 							new FileInputStream(file), this.inputBufferSize)) {
 						OutputStream out = response.getOutputStream();
 						byte[] buffer = new byte[this.inputBufferSize];
 						int nread;
 						while ((nread = in.read(buffer)) > 0) {
 							try {
 								out.write(buffer, 0, nread);
 							} catch (IOException e) {
 								if (logger.isWarnEnabled()) {
 									logger.warn(
 											"Failed to write bytes to the client. It's possible that the client aborted the connection. Bailing out.",
 											e);
 								}
 								break;
 							}
 						}
 						try {
 							out.flush();
 						} catch (IOException e) {
 							if (logger.isWarnEnabled()) {
 								logger.warn(
 										"Failed to flush bytes to the client. It's possible that the client aborted the connection. Bailing out.",
 										e);
 							}
 						}
 					}
 					logger.trace("Done");
 				}
 			} else {
 				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
 			}
 		}
 	}
 
 	private void handleUpload(File dir, HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException,
 			JSONException {
 		Collection<Part> parts = request.getParts();
 		if (logger.isTraceEnabled()) {
 			logger.trace("Uploading " + parts.size() + " file(s)");
 		}
 		response.setContentType("text/plain");
 		response.setCharacterEncoding("UTF-8");
 		Collection<File> uploadedFiles = new LinkedList<>();
 		for (Part part : parts) {
 			uploadedFiles.add(handleUpload(part, dir));
 		}
 		sendFileInfoResponse(uploadedFiles, response);
 	}
 
 	private void handleUploadToFile(File file, HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		InputStream in = null;
 		long contentLength = 0;
 		if (isMultiPartRequest(request)) {
 			Collection<Part> parts = request.getParts();
 			if (parts.size() == 1) {
 				Part part = parts.iterator().next();
 				String partName = getFileName(part);
 				if (!file.getName().equals(partName)) {
 					if (logger.isWarnEnabled()) {
 						logger.warn("Expecting filename [" + file.getName()
 								+ "] but got [" + partName + "]. Ignoring.");
 					}
 				}
 				contentLength = part.getSize();
 				in = part.getInputStream();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Uploading [" + contentLength
 							+ "] bytes from part to [" + file.getAbsolutePath()
 							+ "]");
 				}
 			} else {
 				this.refuseBadRequest(request, response,
 						"Expecting to upload one part to  [" + file.getName()
 								+ "] but got [" + parts.size() + "]. Aborting.");
 				return;
 			}
 		}
 		if (in == null) {
 			contentLength = request.getContentLength();
 			if (contentLength > 0) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Uploading [" + contentLength
 							+ "] bytes from requst to ["
 							+ file.getAbsolutePath() + "]");
 				}
 				in = request.getInputStream();
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("Nothing to upload to ["
 							+ file.getAbsolutePath() + "]");
 				}
 			}
 		}
 
 		if (in != null) {
 			try {
 				try (FileOutputStream out = new FileOutputStream(file)) {
 					int contentUploaded = FileUtil.copy(in, out);
 					if (contentUploaded != contentLength) {
 						if (logger.isWarnEnabled()) {
 							logger.warn("Uploaded [" + contentUploaded
 									+ "] bytes to [" + file.getAbsolutePath()
 									+ "] but expected [" + contentLength
 									+ "]. Ignoring.");
 						}
 					} else {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Uploaded [" + contentUploaded
 									+ "] bytes to [" + file.getAbsolutePath()
 									+ "]");
 						}
 					}
 				}
 			} finally {
 				in.close();
 			}
 		}
 	}
 
 	private void handleZip(File dir, HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException,
 			JSONException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to zip files in directory ["
 					+ dir.getAbsolutePath() + "]");
 		}
 		File zipFile;
 		List<File> files = this.getFilesFromRequest(request, dir);
 		if (files.size() == 1) {
 			File file = files.get(0);
 			zipFile = getUniqueFileInDirectory(dir, file.getName(), ".zip");
 			if (file.isDirectory()) {
 				if (logger.isTraceEnabled()) {
 					logger.trace("Zipping directory [" + file.getAbsolutePath()
 							+ "] to [" + zipFile.getAbsolutePath() + "]");
 				}
 				zipDirectory(file, zipFile);
 			} else {
 				if (logger.isTraceEnabled()) {
 					logger.trace("Zipping file [" + file.getAbsolutePath()
 							+ "] to [" + zipFile.getAbsolutePath() + "]");
 				}
 				zipFile(file, zipFile);
 			}
 		} else {
 			zipFile = getUniqueFileInDirectory(dir, "Archive", ".zip");
 			if (logger.isTraceEnabled()) {
 				logger.trace("Zipping files [" + files + "] to ["
 						+ zipFile.getAbsolutePath() + "]");
 			}
 			zipFiles(dir, files, zipFile);
 		}
 		this.sendFileInfoResponse(zipFile, response);
 	}
 
 	private void handleUnzip(File file, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to unzip file ["
 					+ file.getAbsolutePath() + "]");
 		}
 		final File dir = file.getParentFile();
 		final Collection<File> immediateCreatedFiles = new LinkedList<>();
 		unzip(file, dir, new FileVisitor() {
 			@Override
 			public void visit(File createdFile) throws IOException {
 				if (createdFile.getParentFile().equals(dir)) {
 					immediateCreatedFiles.add(createdFile);
 				}
 			}
 		});
 		this.sendFileInfoResponse(immediateCreatedFiles, response);
 	}
 
 	private void handleRename(File file, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to rename file ["
 					+ file.getAbsolutePath() + "]");
 		}
 		String newName = request.getParameter("newName");
 		File newFile = this.getFile(file.getParentFile(), newName);
 		if (newFile == null) {
 			refuseBadRequest(request, response,
 					"Cannot rename [" + file.getAbsolutePath()
 							+ "] because the newName=[" + newName
 							+ "] is unspecified or invalid");
 		} else if (newFile.exists()) {
 			refuseBadRequest(request, response,
 					"Cannot rename [" + file.getAbsolutePath()
 							+ "] because newFile=[" + newFile.getAbsolutePath()
 							+ "] already exists");
 		} else if (file.renameTo(newFile)) {
 			if (logger.isDebugEnabled()) {
 				logger.debug("Renamed [" + file.getAbsolutePath() + "] to ["
 						+ newFile + "]");
 			}
 			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		} else {
 			this.sendServerFailure(request, response, "Failed to rename ["
 					+ file.getAbsolutePath() + "] to [" + newFile + "]");
 		}
 	}
 
 	private void handleCopy(File dir, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to copy file to directory ["
 					+ dir.getAbsolutePath() + "]");
 		}
 		try {
 			File sourceFile = this.getSourceFile(request);
 			if (sourceFile.exists()) {
 				File destinationFile = sourceFile.getParentFile().equals(dir) ? getUniqueFileInDirectory(
 						dir, sourceFile.getName()) : new File(dir,
 						sourceFile.getName());
 				copy(sourceFile, destinationFile);
 				if (logger.isDebugEnabled()) {
 					logger.debug("Copied [" + sourceFile.getAbsolutePath()
 							+ "] to [" + destinationFile.getAbsoluteFile()
 							+ "]");
 				}
 				this.sendFileInfoResponse(destinationFile, response);
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "Cannot copy ["
 								+ sourceFile.getAbsolutePath()
 								+ "] to directory [" + dir.getAbsolutePath()
 								+ "] because the source file does not exist.");
 			}
 		} catch (IllegalArgumentException e) {
 			this.refuseBadRequest(request, response, e.getMessage());
 		}
 	}
 
 	private void handleMove(File dir, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Handling request to move file to directory ["
 					+ dir.getAbsolutePath() + "]");
 		}
 		try {
 			File sourceFile = this.getSourceFile(request);
 			File destinationFile = new File(dir, sourceFile.getName());
 			if (sourceFile.equals(destinationFile)) {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "Cannot move ["
 								+ sourceFile.getAbsolutePath()
 								+ "] over itself.");
 			} else if (sourceFile.exists()) {
 				if (sourceFile.renameTo(destinationFile)) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Moved [" + sourceFile.getAbsolutePath()
 								+ "] to [" + destinationFile.getAbsoluteFile()
 								+ "]");
 					}
 					this.sendFileInfoResponse(destinationFile, response);
 				} else {
 					this.sendServerFailure(
 							request,
 							response,
 							"Failed to move [" + sourceFile.getAbsolutePath()
 									+ "] to ["
 									+ destinationFile.getAbsoluteFile() + "]");
 				}
 			} else {
 				this.refuseRequest(request, response,
 						HttpServletResponse.SC_NOT_FOUND, "Cannot move ["
 								+ sourceFile.getAbsolutePath()
 								+ "] to directory [" + dir.getAbsolutePath()
 								+ "] because the source file does not exist.");
 			}
 		} catch (IllegalArgumentException e) {
 			this.refuseBadRequest(request, response, e.getMessage());
 		}
 	}
 
 	private File handleUpload(Part part, File dir) throws IOException {
 		String filename = getFileName(part);
 		if (filename == null) {
 			throw new IOException("Failed to get filename from part.");
 		} else if (filename.indexOf("..") != -1 || filename.indexOf('/') != -1) {
 			throw new IOException("Detected illegal/invalid filename ["
 					+ filename + "]");
 		} else {
 			File file = new File(dir, filename);
 			if (logger.isTraceEnabled()) {
 				logger.trace("Uploading file name=[" + filename + "] of size=["
 						+ part.getSize() + "] of type=["
 						+ part.getContentType() + "] to ["
 						+ file.getAbsolutePath() + "]");
 			}
 
 			File backupFile = null;
 			if (file.exists()) {
 				if (!move(file, backupFile = getBackupFile(file, ".backup"))) {
 					backupFile = null;
 				}
 			}
 			try (InputStream in = new BufferedInputStream(
 					part.getInputStream(), this.inputBufferSize);
 					OutputStream out = new BufferedOutputStream(
 							new FileOutputStream(file), this.outputBufferSize)) {
 				byte[] buffer = new byte[this.inputBufferSize];
 				int nread;
 				while ((nread = in.read(buffer)) > 0) {
 					out.write(buffer, 0, nread);
 				}
 				out.flush();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Uploaded [" + filename + "] to ["
 							+ file.getAbsolutePath() + "]");
 				}
 				delete(backupFile);
 				return file;
 			} catch (IOException e) {
 				if (logger.isWarnEnabled()) {
 					logger.warn("Failed to upload [" + filename + "] to ["
 							+ file.getAbsolutePath() + "]", e);
 				}
 				delete(file);
 				move(backupFile, file);
 				throw e;
 			}
 		}
 	}
 
 	private List<File> getFilesFromRequest(HttpServletRequest req, File dir) {
 		String[] filenames = req.getParameterValues("file");
 		List<File> files = null;
 		if (filenames == null || filenames.length == 0) {
 			files = Collections.emptyList();
 		} else {
 			files = new ArrayList<File>(filenames.length);
 			for (String filename : filenames) {
 				File file = this.getFile(dir, filename);
 				if (file != null) {
 					if (file.exists()) {
 						files.add(file);
 					} else {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Request-specified file ["
 									+ file.getAbsolutePath()
 									+ "] does not exist. Skipping.");
 						}
 					}
 				}
 			}
 		}
 		if (logger.isTraceEnabled()) {
 			logger.trace("Got [" + files.size() + "] file(s) from the request");
 		}
 		return files;
 	}
 
 	private File getRequestFile(HttpServletRequest request)
 			throws UnsupportedEncodingException {
 		return this.getRequestFile(request.getRequestURI());
 	}
 
 	private File getRequestFile(final String path)
 			throws UnsupportedEncodingException {
 		File file = new File(this.rootDir, SearchAndReplace.searchAndReplace(
 				URLDecoder.decode(path, "UTF-8"), this.rewriteRules));
 		if (logger.isTraceEnabled()) {
 			logger.trace("Resolved path [" + path + "] to ["
 					+ file.getAbsolutePath() + "]");
 		}
 		return file;
 	}
 
 	private File getSourceFile(HttpServletRequest request)
 			throws UnsupportedEncodingException {
 		String basePath = this.getBasePath(request);
 		String targetPath = request.getRequestURI();
 		String sourcePath = request.getParameter("source");
 
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
 			return this.getRequestFile(sourcePath);
 		}
 	}
 
 	private File getFile(File dir, String filename) {
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
 			return new File(dir, filename);
 		}
 		return null;
 	}
 
 	private boolean makeBaseDir(File file) {
 		if (file.mkdirs()) {
 			if (logger.isDebugEnabled()) {
 				logger.debug("Created the base dir [" + file.getAbsolutePath()
 						+ "]");
 			}
 			return true;
 		} else {
 			if (logger.isWarnEnabled()) {
 				logger.warn("Failed to create base dir ["
 						+ file.getAbsolutePath() + "]");
 			}
 			return false;
 		}
 	}
 
 	private String getFileType(File file) {
 		if (file.isDirectory()) {
 			return this.directoryMimeType;
 		} else {
 			String mimeType = super.getServletContext().getMimeType(
 					file.getName());
 			return mimeType == null ? this.defaultMimeType : mimeType;
 		}
 	}
 
 	private long writeFileInfoToJson(Iterable<File> files, JSONWriter jsonWriter)
 			throws JSONException {
 		long size = 0;
 		jsonWriter.array();
 		for (File createdFile : files) {
 			size += writeFileInfoToJson(createdFile, jsonWriter);
 		}
 		jsonWriter.endArray();
 		return size;
 	}
 
 	private long writeFileInfoToJson(File file, JSONWriter jsonWriter)
 			throws JSONException {
 		jsonWriter.object();
 		jsonWriter.key("name").value(file.getName());
 		jsonWriter.key("type").value(getFileType(file));
 		long size = size(file);
 		jsonWriter.key("size").value(size);
 		jsonWriter.key("lastModified").value(file.lastModified());
 		jsonWriter.endObject();
 		return size;
 	}
 
 	private void sendFileInfoResponse(File file, HttpServletResponse response)
 			throws IOException, JSONException {
 		response.setContentType("application/json");
 		JSONWriter jsonWriter = new JSONWriter(response.getWriter());
 		writeFileInfoToJson(file, jsonWriter);
 		response.flushBuffer();
 	}
 
 	private void sendFileInfoResponse(Collection<File> files,
 			HttpServletResponse response) throws IOException, JSONException {
 		response.setContentType("application/json");
 		JSONWriter jsonWriter = new JSONWriter(response.getWriter());
 		writeFileInfoToJson(files, jsonWriter);
 		response.flushBuffer();
 	}
 
 	private void refuseRequest(HttpServletRequest request,
 			HttpServletResponse response, int responseCode, String msg)
 			throws ServletException, IOException {
 		if (logger.isWarnEnabled()) {
 			logger.warn("Refusing request with [" + responseCode + "]. " + msg);
 		}
 		response.setStatus(responseCode);
 	}
 
 	private void refuseBadRequest(HttpServletRequest request,
 			HttpServletResponse response, String msg) throws ServletException,
 			IOException {
 		refuseRequest(request, response, HttpServletResponse.SC_BAD_REQUEST,
 				msg);
 	}
 
 	private void sendServerFailure(HttpServletRequest request,
 			HttpServletResponse response, String msg, Throwable cause)
 			throws ServletException, IOException {
 		logger.error(msg, cause);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 	}
 
 	private void sendServerFailure(HttpServletRequest request,
 			HttpServletResponse response, String msg) throws ServletException,
 			IOException {
 		this.sendServerFailure(request, response, msg, null);
 	}
 
 	private String getBasePath(HttpServletRequest request) {
 		return (String) request.getAttribute(Constants.BASE_DIR_ATTR_NAME);
 	}
 
 	private Boolean getWriteAllowed(HttpServletRequest request) {
 		return (Boolean) request.getAttribute(Constants.WRITE_ALLOWED);
 	}
 
 }
