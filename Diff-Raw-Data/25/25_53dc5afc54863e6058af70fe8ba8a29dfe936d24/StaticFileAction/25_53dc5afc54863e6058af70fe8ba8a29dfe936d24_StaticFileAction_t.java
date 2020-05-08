 package org.caudexorigo.http.netty;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Date;
 
 import org.caudexorigo.http.netty.reporting.ResponseFormatter;
 import org.caudexorigo.http.netty.reporting.StandardResponseFormatter;
 import org.caudexorigo.text.StringUtils;
 import org.caudexorigo.text.UrlCodec;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.DefaultFileRegion;
 import org.jboss.netty.channel.FileRegion;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.ssl.SslHandler;
 
 public class StaticFileAction extends HttpAction
 {
 	private final File rootDirectory;
 	private final String rootDirectoryPath;
 
 	private final long cacheAge;
 	private ResponseFormatter rspFmt;
 
 	public StaticFileAction(URI root_path)
 	{
 		this(root_path, new StandardResponseFormatter(false), 0);
 	}
 
 	public StaticFileAction(URI root_path, ResponseFormatter rspFmt)
 	{
 		this(root_path, rspFmt, 0);
 	}
 
 	public StaticFileAction(URI root_path, ResponseFormatter rspFmt, long cache_age)
 	{
 
 		rootDirectory = new File(root_path);
 		rootDirectoryPath = rootDirectory.getAbsolutePath();
 		this.rspFmt = rspFmt;
 
 		this.cacheAge = cache_age;
 
 		if (!rootDirectory.isDirectory() || !rootDirectory.canRead() || rootDirectory.isHidden())
 		{
 			throw new IllegalArgumentException("Not a valid root directory");
 		}
 
 	}
 
 	@Override
 	public void service(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
 	{
 		validateRequest(request, response);
 		File file = getFile(request, response);
 
		validateFile(response, file, request.getUri());
 
 		String abs_path = getFileAbsolutePath(response, file);
 
		response.headers().set(HttpHeaders.Names.DATE, HttpDateFormat.getCurrentHttpDate());
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, Long.toString(file.length()));
 
 		String ctype = MimeTable.getContentType(abs_path);
 		if (StringUtils.isNotBlank(ctype))
 		{
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, ctype);
 		}
 
 		if (StringUtils.isNotBlank(getContentEncoding()))
 		{
			response.headers().set(HttpHeaders.Names.CONTENT_ENCODING, getContentEncoding());
 		}
 
 		RandomAccessFile raf = null;
 		try
 		{
 			raf = new RandomAccessFile(file, "r");
 		}
 		catch (FileNotFoundException e1)
 		{
 			// this exception can be ignored because we already tested for it
 		}
 
 		response.setStatus(HttpResponseStatus.OK);
 
 		if (cacheAge > 0)
 		{
 			response.headers().set(HttpHeaders.Names.CACHE_CONTROL, String.format("max-age=%s", cacheAge));
 			response.headers().set(HttpHeaders.Names.LAST_MODIFIED, HttpDateFormat.getHttpDate(new Date(file.lastModified())));
 		}
 
 		Channel channel = ctx.getChannel();
 
 		boolean is_secure = channel.getPipeline().get(SslHandler.class) != null;
 
 		boolean is_keep_alive = HttpHeaders.isKeepAlive(request);
 
 		if (is_secure)
 		{
 			is_keep_alive = false;
 		}
 
 		if (is_keep_alive)
 		{
			response.headers().set(HttpHeaders.Names.CONNECTION, "Keep-Alive");
 		}
 
 		channel.write(response);
 
 		// Write the content.
 		ChannelFuture writeFuture;
 
 		try
 		{
 			if (is_secure)
 			{
 				// Cannot use zero-copy with HTTPS
 				// (http://www.jboss.org/file-access/default/members/netty/freezone/xref/3.2/org/jboss/netty/example/http/file/HttpStaticFileServerHandler.html)
 				// writeFuture = channel.write(new ChunkedFile(raf, 0, raf.length(), 8192));
 
 				ChannelBuffer cb = ChannelBuffers.directBuffer((int) raf.length());
 
 				int b;
 				while ((b = raf.read()) != -1)
 				{
 					cb.writeByte(b);
 				}
 
 				writeFuture = channel.write(cb);
 
 				raf.close();
 			}
 			else
 			{
 				final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
 				writeFuture = channel.write(region);
 
 				writeFuture.addListener(new ChannelFutureListener()
 				{
 					@Override
 					public void operationComplete(ChannelFuture cf) throws Exception
 					{
 						region.releaseExternalResources();
 					}
 				});
 			}
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 
 		if (!is_keep_alive)
 		{
 			writeFuture.addListener(ChannelFutureListener.CLOSE);
 		}
 	}
 
 	@Override
 	protected ResponseFormatter getResponseFormatter()
 	{
 		return this.rspFmt;
 	}
 
 	// this method serves as optional overload hook
 	public String getContentEncoding()
 	{
 		return null;
 	}
 
 	protected String getFileAbsolutePath(HttpResponse response, File file)
 	{
 		String abs_path = file.getAbsolutePath();
 
 		if (!file.getAbsolutePath().startsWith(rootDirectoryPath))
 		{
 			response.setStatus(HttpResponseStatus.FORBIDDEN);
 			throw new IllegalArgumentException("Forbidden");
 		}
 
 		return abs_path;
 	}
 
 	protected File getFile(HttpRequest request, HttpResponse response)
 	{
 		String path = sanitizePath(request.getUri());
 
 		if (path == null)
 		{
 			response.setStatus(HttpResponseStatus.FORBIDDEN);
 			throw new IllegalArgumentException("Forbidden");
 		}
 
 		File file = new File(path);
 		return file;
 	}
 
	protected void validateFile(HttpResponse response, File file, String path)
 	{
 		if (file.isHidden() || !file.exists())
 		{
			throw new WebException(new FileNotFoundException(String.format("File not found: '%s'", path)), HttpResponseStatus.NOT_FOUND.getCode());
 		}
 
 		if (!file.isFile())
 		{
 			throw new WebException(new IllegalArgumentException("Forbidden"), HttpResponseStatus.FORBIDDEN.getCode());
 		}
 	}
 
 	protected void validateRequest(HttpRequest request, HttpResponse response)
 	{
 		if (request.getMethod() != HttpMethod.GET)
 		{
 			throw new WebException(new IllegalArgumentException("Method not allowed"), HttpResponseStatus.METHOD_NOT_ALLOWED.getCode());
 		}
 
 		if (request.isChunked())
 		{
 			throw new WebException(new IllegalArgumentException("Bad request"), HttpResponseStatus.BAD_REQUEST.getCode());
 		}
 	}
 
 	private String sanitizePath(String spath)
 	{
 		String path = StringUtils.substringBefore(spath, "?");
 
 		if (StringUtils.isBlank(path))
 		{
 			return null;
 		}
 
 		// Decode the path.
 		try
 		{
 			path = UrlCodec.decode(path, "ISO-8859-1");
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			try
 			{
 				path = UrlCodec.decode(path, "UTF-8");
 			}
 			catch (UnsupportedEncodingException e1)
 			{
 				throw new RuntimeException(e1);
 			}
 		}
 
 		// Convert file separators.
 		path = path.replace('/', File.separatorChar);
 
 		// Simplistic dumb security check.
 		// You will have to do something serious in the production environment.
 		if (path.contains(File.separator + ".") || path.contains("." + File.separator) || path.startsWith(".") || path.endsWith("."))
 		{
 			return null;
 		}
 		// Convert to absolute path.
 
 		return rootDirectoryPath + File.separator + path;
 	}
 }
