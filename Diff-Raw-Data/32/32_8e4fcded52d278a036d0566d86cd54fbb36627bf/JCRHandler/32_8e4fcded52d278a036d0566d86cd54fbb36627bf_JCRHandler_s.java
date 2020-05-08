 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Timefields Ltd
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.nodetype.NodeType;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.sdata.FileItemIterator;
 import org.apache.commons.fileupload.sdata.FileItemStream;
 import org.apache.commons.fileupload.sdata.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.sdata.util.Streams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.component.api.ComponentManager;
 import org.sakaiproject.jcr.api.JCRConstants;
 import org.sakaiproject.jcr.support.api.JCRNodeFactoryService;
 import org.sakaiproject.sdata.tool.api.Handler;
 import org.sakaiproject.sdata.tool.api.ResourceDefinition;
 import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;
 import org.sakaiproject.tool.api.Tool;
 
 /**
  * <p>
  * JCR Service is a servlet that gives access to the JCR returning the content
  * of files within the jcr or a map response (directories). The resource is
  * pointed to using the URI/URL requested (the path info part), and the standard
  * Http methods do what they are expected to in the http standard. GET gets the
  * content of the file, PUT put puts a new file, the content comming from the
  * stream of the PUT. DELETE deleted the file. HEAD gets the headers that would
  * come from a full GET.
  * </p>
  * <p>
  * The content type and content encodign headers are honored for GET,HEAD and
  * PUT, but other headers are not honored completely at the moment (range-*)
  * etc,
  * </p>
  * <p>
  * POST takes multipart uploads of content, the URL pointing to a folder and
  * each upload being the name of the file being uploaded to that folder. The
  * upload uses a streaming api, and expects that form fields are ordered, such
  * that a field starting with mimetype before the uplaod stream will specify the
  * mimetype associated with the stream.
  * </p>
  * 
  * @author ieb
  */
 public abstract class JCRHandler implements Handler
 {
 	private static final Log log = LogFactory.getLog(JCRHandler.class);
 
 	/**
 	 * Required for serialization... also to stop eclipse from giving me a
 	 * warning!
 	 */
 	private static final long serialVersionUID = 676743152200357708L;
 
 	private static final String BASE_PATH_INIT = "basepath";
 
 	private static final String DEFAULT_BASE_PATH = "/sakai/sdata";
 
 	private static final String LAST_MODIFIED = "Last-Modified";
 
 	private static final String BASE_URL_INIT = "baseurl";
 
 	private static final String DEFAULT_BASE_URL = "";
 
 	private String basePath;
 
 	private ComponentManager componentManager;
 
 	private JCRNodeFactoryService jcrNodeFactory;
 
 	private ResourceDefinitionFactory resourceDefinitionFactory;
 
 	private String baseUrl;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#init(java.util.Map)
 	 */
 	public void init(Map<String, String> config)
 	{
 		basePath = config.get(BASE_PATH_INIT);
 		if (basePath == null)
 		{
 			this.basePath = DEFAULT_BASE_PATH;
 		}
 
 		baseUrl = config.get(BASE_URL_INIT);
 		if (baseUrl == null)
 		{
 			this.baseUrl = DEFAULT_BASE_URL;
 		}
 
 		componentManager = org.sakaiproject.component.cover.ComponentManager
 				.getInstance();
 
 		jcrNodeFactory = (JCRNodeFactoryService) componentManager
 				.get(JCRNodeFactoryService.class.getName());
 
 		resourceDefinitionFactory = getResourceDefinitionFactory();
 
 	}
 
 	/**
 	 * Creates a resource definition factory suitable for controlling the
 	 * storage of items
 	 * 
 	 * @return
 	 */
 	protected ResourceDefinitionFactory getResourceDefinitionFactory()
 	{
 		return new ResourceDefinitionFactoryImpl(baseUrl, basePath);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doDelete(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doDelete(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		try
 		{
 
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
 			if (n == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 			NodeType nt = n.getPrimaryNodeType();
 
 			long lastModifiedTime = -10;
 			if (JCRConstants.NT_FILE.equals(nt.getName()))
 			{
 
 				Node resource = n.getNode(JCRConstants.JCR_CONTENT);
 				Property lastModified = resource
 						.getProperty(JCRConstants.JCR_LASTMODIFIED);
 				lastModifiedTime = lastModified.getDate().getTimeInMillis();
 
 				if (!checkPreconditions(request, response, lastModifiedTime, String
 						.valueOf(lastModifiedTime)))
 				{
 					return;
 				}
 			}
 
 			Session s = n.getSession();
 			n.remove();
 			s.save();
 			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 		}
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param request
 	 */
 	@SuppressWarnings("unchecked")
 	private void snoopRequest(HttpServletRequest request)
 	{
 		boolean snoop = "1".equals(request.getParameter("snoop"));
 		if (snoop)
 		{
 			StringBuilder sb = new StringBuilder("SData Request :");
 			sb.append("\n\tRequest Path :").append(request.getPathInfo());
 			sb.append("\n\tMethod :").append(request.getMethod());
 			for (Enumeration<String> hnames = request.getHeaderNames(); hnames
 					.hasMoreElements();)
 			{
 				String name = hnames.nextElement();
 				sb.append("\n\tHeader :").append(name).append("=[").append(
 						request.getHeader(name)).append("]");
 			}
 			if (request.getCookies() != null)
 			{
 				for (Cookie c : request.getCookies())
 				{
 					sb.append("\n\tCookie:");
 					sb.append("name[").append(c.getName());
 					sb.append("]path[").append(c.getPath());
 					sb.append("]value[").append(c.getValue());
 				}
 			}
 			sb.append("]");
 			log.info(sb.toString());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doHead(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doHead(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		try
 		{
 			log.info("Doing Head ");
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
 			if (n == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 
 			Node resource = n.getNode(JCRConstants.JCR_CONTENT);
 			Property lastModified = resource.getProperty(JCRConstants.JCR_LASTMODIFIED);
 			Property mimeType = resource.getProperty(JCRConstants.JCR_MIMETYPE);
 			Property encoding = resource.getProperty(JCRConstants.JCR_ENCODING);
 			Property content = resource.getProperty(JCRConstants.JCR_DATA);
 
 			response.setContentType(mimeType.getString());
 			response.setCharacterEncoding(encoding.getString());
 			response.setDateHeader(LAST_MODIFIED, lastModified.getDate()
 					.getTimeInMillis());
 			// we need to do something about huge files
 			response.setContentLength((int) content.getLength());
 			response.setStatus(HttpServletResponse.SC_OK);
 
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doPut(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doPut(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		OutputStream out = null;
 		try
 		{
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
 			boolean created = false;
 			if (n == null)
 			{
 				n = jcrNodeFactory.createFile(rp.getRepositoryPath());
 				created = true;
 				if (n == null)
 				{
 					throw new RuntimeException("Failed to create node at "
 							+ rp.getRepositoryPath() + " type " + JCRConstants.NT_FILE);
 				}
 			}
 			else
 			{
 				NodeType nt = n.getPrimaryNodeType();
 				if (!JCRConstants.NT_FILE.equals(nt.getName()))
 				{
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 							"Content Can only be put to a file, resource type is "
 									+ nt.getName());
 					return;
 				}
 			}
 
 			GregorianCalendar gc = new GregorianCalendar();
 			long lastMod = request.getDateHeader(LAST_MODIFIED);
 			if (lastMod > 0)
 			{
 				gc.setTimeInMillis(lastMod);
 			}
 			else
 			{
 				gc.setTime(new Date());
 			}
 			String mimeType = request.getContentType();
 			String charEncoding = request.getCharacterEncoding();
 			long contentLength = request.getContentLength();
 
 			InputStream in = request.getInputStream();
 			saveStream(n, in, mimeType, charEncoding, gc, null,null,contentLength);
 
 			in.close();
 			if (created)
 			{
 				response.setStatus(HttpServletResponse.SC_CREATED);
 			}
 			else
 			{
 				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 			}
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 
 			try
 			{
 				out.close();
 			}
 			catch (Exception ex)
 			{
 			}
 		}
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param n
 	 * @param in
 	 * @param mimeType
 	 * @param charEncoding
 	 * @param progressID
 	 * @param fieldName 
 	 * @param contentLength 
 	 * @param gc
 	 * @throws
 	 * @throws RepositoryException
 	 */
 	private long saveStream(Node n, InputStream in, String mimeType, String charEncoding,
 			Calendar lastModified, String progressID, String fieldName, long contentLength) throws RepositoryException
 	{
 		Node resource = n.getNode(JCRConstants.JCR_CONTENT);
 		resource.setProperty(JCRConstants.JCR_LASTMODIFIED, lastModified);
 		resource.setProperty(JCRConstants.JCR_MIMETYPE, mimeType);
 		resource.setProperty(JCRConstants.JCR_ENCODING, charEncoding);
 
 		Property content = resource.getProperty(JCRConstants.JCR_DATA);
 		ProgressInputStream progressStream = new ProgressInputStream(in,
 				createProgressMap(progressID), fieldName);
 
 		content.setValue(progressStream);
 
 		n.save();
 
 		return content.getLength();
 	}
 
 	/**
 	 * @param progressID
 	 * @return
 	 */
 	private Map<String, Object> createProgressMap(String progressID)
 	{
 		if (progressID == null)
 		{
 			return null;
 		}
 		Map<String, Object> progressMap = new ConcurrentHashMap<String, Object>();
 		ProgressHandler.setMap(progressID, progressMap);
 		return progressMap;
 	}
 
 	/**
 	 * @param progressID
 	 */
 	private void clearProgress(String progressID)
 	{
 		ProgressHandler.clearMap(progressID);
 	}
 	
 	/**
 	 * @param progressID
 	 * @return
 	 */
 	private Map<String, Object> getProgress(String progressID)
 	{
 		return ProgressHandler.getMap(progressID);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doGet(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doGet(final HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		OutputStream out = null;
 		InputStream in = null;
 		try
 		{
 			snoopRequest(request);
 
 			String range = request.getHeader("range");
 			boolean partialGet = (range != null && range.trim().length() != 0);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
 			if (n == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 
 			NodeType nt = n.getPrimaryNodeType();
 
 			if (JCRConstants.NT_FILE.equals(nt.getName()))
 			{
 
 				Node resource = n.getNode(JCRConstants.JCR_CONTENT);
 				Property lastModified = resource
 						.getProperty(JCRConstants.JCR_LASTMODIFIED);
 				Property mimeType = resource.getProperty(JCRConstants.JCR_MIMETYPE);
 				Property encoding = resource.getProperty(JCRConstants.JCR_ENCODING);
 				Property content = resource.getProperty(JCRConstants.JCR_DATA);
 
 				response.setContentType(mimeType.getString());
 				response.setCharacterEncoding(encoding.getString());
 				response.setDateHeader(LAST_MODIFIED, lastModified.getDate()
 						.getTimeInMillis());
 				setGetCacheControl(response, rp.isPrivate());
 
 				String currentEtag = String.valueOf(lastModified.getDate()
 						.getTimeInMillis());
 				response.setHeader("ETag", currentEtag);
 
 				boolean sendContent = true;
 				long lastModifiedTime = lastModified.getDate().getTimeInMillis();
 
 				if (!checkPreconditions(request, response, lastModifiedTime, currentEtag))
 				{
 					return;
 				}
 				long totallength = content.getLength();
 				long[] ranges = new long[2];
 				ranges[0] = 0;
 				ranges[1] = totallength;
 				if (!checkRanges(request, response, lastModifiedTime, currentEtag, ranges))
 				{
 					return;
 				}
 
 				long length = ranges[1] - ranges[0];
 
 				if (length > 1024 * 1024)
 				{
 					length = 1024 * 1024;
 					ranges[1] = ranges[0] + length;
 				}
 
 				if (totallength != length)
 				{
 					response.setHeader("Content-Range", "bytes " + ranges[0] + "-"
 							+ (ranges[1] - 1) + "/" + totallength);
 					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
 				}
 				else
 				{
 					response.setStatus(HttpServletResponse.SC_OK);
 				}
 
 				response.setContentLength((int) length);
 
 				out = response.getOutputStream();
 
 				in = content.getStream();
 				in.skip(ranges[0]);
 				byte[] b = new byte[10240];
 				int nbytes = 0;
 				while ((nbytes = in.read(b)) > 0 && length > 0)
 				{
 					if (nbytes < length)
 					{
 						out.write(b, 0, nbytes);
 						length = length - nbytes;
 					}
 					else
 					{
 						out.write(b, 0, (int) length);
 						length = 0;
 					}
 				}
 			}
 			else
 			{
 				setGetCacheControl(response, rp.isPrivate());
 
 				// Property lastModified =
 				// n.getProperty(JCRConstants.JCR_LASTMODIFIED);
 				// response.setHeader("ETag",
 				// String.valueOf(lastModified.getDate()
 				// .getTimeInMillis()));
 
 				Map<String, Object> outputMap = new HashMap<String, Object>();
 				outputMap.put("path", rp.getExternalPath(n.getPath()));
 				outputMap.put("type", nt.getName());
 				List<Map> nodes = new ArrayList<Map>();
 				NodeIterator ni = n.getNodes();
 				int i = 0;
 				while (ni.hasNext())
 				{
 					Node cn = ni.nextNode();
 					Map<String, Object> cnm = new HashMap<String, Object>();
 					cnm.put("path", rp.getExternalPath(cn.getName()));
 					NodeType cnt = cn.getPrimaryNodeType();
 					cnm.put("type", cnt.getName());
 					cnm.put("position", String.valueOf(i));
 					if (JCRConstants.NT_FILE.equals(nt.getName()))
 					{
 						Node resource = n.getNode(JCRConstants.JCR_CONTENT);
 						Property nodeLastModified = resource
 								.getProperty(JCRConstants.JCR_LASTMODIFIED);
 						Property mimeType = resource
 								.getProperty(JCRConstants.JCR_MIMETYPE);
 						Property encoding = resource
 								.getProperty(JCRConstants.JCR_ENCODING);
 						Property content = resource.getProperty(JCRConstants.JCR_DATA);
 
 						cnm.put("mime-type", mimeType.getString());
 						cnm.put("encoding", encoding.getString());
 						cnm.put("length", String.valueOf(content.getLength()));
 						cnm.put("lastModified", nodeLastModified.getDate());
 
 					}
 					nodes.add(cnm);
 					i++;
 				}
 				outputMap.put("nitems", nodes.size());
 				outputMap.put("items", nodes);
 
 				sendMap(request, response, outputMap);
 			}
 
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 
 			try
 			{
 				out.close();
 			}
 			catch (Exception ex)
 			{
 			}
 			try
 			{
 				in.close();
 			}
 			catch (Exception ex)
 			{
 			}
 		}
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param request
 	 * @param response
 	 * @param lastModifiedTime
 	 * @param currentEtag
 	 * @param ranges
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean checkRanges(HttpServletRequest request, HttpServletResponse response,
 			long lastModifiedTime, String currentEtag, long[] ranges) throws IOException
 	{
 
 		String range = request.getHeader("range");
 		long ifRangeDate = request.getDateHeader("if-range");
 		String ifRangeEtag = request.getHeader("if-range");
 
 		if (ifRangeDate != -1 && lastModifiedTime > ifRangeDate)
 		{
 			// the entity has been modified, ignore and send the whole lot
 			return true;
 		}
 		if (ifRangeEtag != null && !currentEtag.equals(ifRangeEtag))
 		{
 			// the entity has been modified, ignore and send the whole lot
 			return true;
 		}
 
 		if (range != null)
 		{
 			String[] s = range.split("=");
 			if (!"bytes".equals(s[0]))
 			{
 				response
 						.sendError(416,
 								"System only supports single range responses, specified in bytes");
 				return false;
 			}
 			range = s[1];
 			String[] r = range.split(",");
 			if (r.length > 1)
 			{
 				response.sendError(416, "System only supports single range responses");
 				return false;
 			}
 			range = range.trim();
 			if (range.startsWith("-"))
 			{
 				ranges[1] = Long.parseLong(range.substring(1));
 			}
 			else if (range.endsWith("-"))
 			{
 				ranges[0] = Long.parseLong(range.substring(0, range.length() - 1));
 			}
 			else
 			{
 				r = range.split("-");
 				ranges[0] = Long.parseLong(r[0]);
 				ranges[1] = Long.parseLong(r[1]);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean checkPreconditions(HttpServletRequest request,
 			HttpServletResponse response, long lastModifiedTime, String currentEtag)
 			throws IOException
 	{
 		lastModifiedTime = lastModifiedTime - (lastModifiedTime % 1000);
 		long ifUnmodifiedSince = request.getDateHeader("if-unmodified-since");
 		if (ifUnmodifiedSince > 0 && (lastModifiedTime > ifUnmodifiedSince))
 		{
 			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			return false;
 		}
 
 		String ifMatch = request.getHeader("if-match");
 		if (ifMatch != null && ifMatch.indexOf(currentEtag) < 0)
 		{
 			// ifMatch was present, but the currentEtag didnt match
 			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			return false;
 		}
 		String ifNoneMatch = request.getHeader("if-none-match");
 		if (ifNoneMatch != null && ifNoneMatch.indexOf(currentEtag) >= 0)
 		{
 			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			return false;
 		}
 		long ifModifiedSince = request.getDateHeader("if-modified-since");
 		if ((ifModifiedSince > 0) && (lastModifiedTime <= ifModifiedSince))
 		{
 			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param response
 	 */
 	private void setGetCacheControl(HttpServletResponse response, boolean isprivate)
 	{
 		if (isprivate)
 		{
 			response.addHeader("Cache-Control", "must-revalidate");
 			response.addHeader("Cache-Control", "private");
 			response.addHeader("Cache-Control", "no-store");
 		}
 		else
 		{
 			// response.addHeader("Cache-Control", "must-revalidate");
 			response.addHeader("Cache-Control", "public");
 		}
 		response.addHeader("Cache-Control", "max-age=600");
 		response.addHeader("Cache-Control", "s-maxage=600");
 		response.setDateHeader("Date", System.currentTimeMillis());
 		response.setDateHeader("Expires", System.currentTimeMillis() + 600000);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doPost(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		snoopRequest(request);
 		try
 		{
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 
 			// multiparts are always streamed uploads
 			if (isMultipart)
 			{
 				doMumtipartUpload(request, response, rp);
 			}
 			else
 			{
 				log.info("Got Standard");
 
 			}
 		}
 		catch (SDataException sde)
 		{
 			sendError(request, response, sde);
 
 		}
 
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param request
 	 * @param response
 	 * @param path
 	 * @param rp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	private void doMumtipartUpload(HttpServletRequest request,
 			HttpServletResponse response, ResourceDefinition rp) throws ServletException,
 			IOException
 	{
 		try
 		{
 			try
 			{
 				Node n = jcrNodeFactory.createFolder(rp.getRepositoryPath());
 				if (n == null)
 				{
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 							"Unable to uplaod to location " + rp.getRepositoryPath());
 					return;
 				}
 			}
 			catch (Exception ex)
 			{
 				sendError(request, response, ex);
 				snoopRequest(request);
 				log.error("Failed  TO service Request ", ex);
 				return;
 			}
 
 			// Check that we have a file upload request
 
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload();
 			String progressID = null;
 			List<String> errors = new ArrayList<String>();
 			
 			long contentLength = request.getContentLength();
 
 			// Parse the request
 			FileItemIterator iter = upload.getItemIterator(request);
 			Map<String,Object> responseMap = new HashMap<String, Object>();
 			Map<String, Object> uploads = new HashMap<String, Object>();
 			while (iter.hasNext())
 			{
 				FileItemStream item = iter.next();
 				log.debug("Got Upload through Uploads");
 				String name = item.getName();
 				String fieldName = item.getFieldName();
 				log.info("    Name is " + name + " field Name " + fieldName);
 				for ( String headerName : item.getHeaderNames() ) {
 					log.info("Header "+headerName+" is "+item.getHeader(headerName));
 				}
 				InputStream stream = item.openStream();
 				if (!item.isFormField())
 				{
 					try
 					{
 						if (name != null && name.trim().length() > 0)
 						{
 							String mimeType = item.getContentType();
 							Node target = jcrNodeFactory.createFile(rp
 									.getRepositoryPath(name));
 							GregorianCalendar lastModified = new GregorianCalendar();
 							lastModified.setTime(new Date());
 							long size = saveStream(target, stream, mimeType, "UTF-8",
 									lastModified, progressID, fieldName, contentLength);
 							Map<String, Object> uploadMap = new HashMap<String, Object>();
 							if (size > Integer.MAX_VALUE)
 							{
 								uploadMap.put("contentLength", String.valueOf(size));
 							}
 							else
 							{
 								uploadMap.put("contentLength", (int) size);
 							}
 							uploadMap.put("name", name);
 							uploadMap.put("url", rp.getExternalPath(rp
 									.getRepositoryPath(name)));
 							uploadMap.put("mimeType", mimeType);
 							uploadMap.put("lastModified", lastModified.getTime());
 							uploadMap.put("status", "ok");
 
 							uploads.put(fieldName, uploadMap);
 							uploadMap = new HashMap<String, Object>();
 						}
 					}
 					catch (Exception ex)
 					{
 						log.error("Failed to Upload Content", ex);
 						Map<String, Object> uploadMap = new HashMap<String, Object>();
 						uploadMap.put("mimeType", "text/plain");
 						uploadMap.put("encoding", "UTF-8");
 						uploadMap.put("contentLength", -1);
 						uploadMap.put("lastModified", 0);
 						uploadMap.put("status", "Failed");
 						uploadMap.put("cause", ex.getMessage());
 						List<String> stackTrace = new ArrayList<String>();
 						for (StackTraceElement ste : ex.getStackTrace())
 						{
 							stackTrace.add(ste.toString());
 						}
 						uploadMap.put("stacktrace", stackTrace);
 						uploads.put(fieldName, uploadMap);
 						errors.add(progressID);
 						uploadMap = new HashMap<String, Object>();
 
 					}
 
 				}
 				else
 				{
 					if ("submitid".equals(fieldName))
 					{
 						progressID = Streams.asString(stream);
 						log.info("Progress ID is "+progressID);
 					}
 
 				}
 			}
 			responseMap.put("progress", getProgress(progressID));	
 			responseMap.put("success", true);
 			responseMap.put("errors", errors.toArray(new String[1]));
 			responseMap.put("uploads", uploads);
 			sendMap(request, response, responseMap);
			log.info("Response Complete");
 			clearProgress(progressID);
 		}
 		catch (Throwable ex)
 		{
 			log.error("Failed  TO service Request ", ex);
 			sendError(request, response, ex);
 			return;
 		}
 	}
 
 
 	/**
 	 * Sends an error to the client
 	 * 
 	 * @param ex
 	 * @throws IOException
 	 */
 	protected abstract void sendError(HttpServletRequest request,
 			HttpServletResponse response, Throwable ex) throws IOException;
 
 	/**
 	 * Serailize a Map strucutre to the output stream
 	 * 
 	 * @param uploads
 	 * @throws IOException
 	 */
 	protected abstract void sendMap(HttpServletRequest request,
 			HttpServletResponse response, Map<String, Object> contetMap)
 			throws IOException;
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @return the basePath
 	 */
 	public String getBasePath()
 	{
 		return basePath;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param basePath
 	 *        the basePath to set
 	 */
 	public void setBasePath(String basePath)
 	{
 		this.basePath = basePath;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#setHandlerHeaders(javax.servlet.http.HttpServletResponse)
 	 */
 	public void setHandlerHeaders(HttpServletResponse response)
 	{
 		response.setHeader("x-sdata-handler", this.getClass().getName());
 	}
 
 }
