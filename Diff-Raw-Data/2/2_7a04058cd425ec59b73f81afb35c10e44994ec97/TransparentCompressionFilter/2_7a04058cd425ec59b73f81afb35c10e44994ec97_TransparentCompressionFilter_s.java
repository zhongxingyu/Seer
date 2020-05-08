 package com.nesscomputing.httpserver.jetty;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 import java.util.zip.Deflater;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.inject.Singleton;
 
 import net.jpountz.lz4.LZ4BlockOutputStream;
 
 import org.eclipse.jetty.continuation.Continuation;
 import org.eclipse.jetty.continuation.ContinuationListener;
 import org.eclipse.jetty.continuation.ContinuationSupport;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.http.gzip.AbstractCompressedStream;
 import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
 
 import com.nesscomputing.logging.Log;
 
 /**
  * <b>NOTE: this is a copy of the Jetty GzipFilter, updated to add LZ4 support!</b>
  *
  * LZ4/GZIP Filter
  * This filter will lz4 or gzip or deflate the content of a response if: <ul>
  * <li>The filter is mapped to a matching path</li>
  * <li>accept-encoding header is set to any of lz4, gzip, deflate or a combination of those</li>
  * <li>The response status code is >=200 and <300
  * <li>The content length is unknown or more than the <code>minGzipSize</code> initParameter or the minGzipSize is 0(default)</li>
  * <li>The content-type is in the comma separated list of mimeTypes set in the <code>mimeTypes</code> initParameter or
  * if no mimeTypes are defined the content-type is not "application/gzip"</li>
  * <li>No content-encoding is specified by the resource</li>
  * </ul>
  *
  * <p>
  * If multiple acceptable encodings are supplied, lz4 trumps gzip trumps deflate.
  * </p>
  * <p>
  * Compressing the content can greatly improve the network bandwidth usage, but at a cost of memory and
  * CPU cycles. If this filter is mapped for static content, then use of efficient direct NIO may be
  * prevented, thus use of the gzip mechanism of the {@link org.eclipse.jetty.servlet.DefaultServlet} is
  * advised instead.
  * </p>
  * <p>
  * This filter extends {@link UserAgentFilter} and if the the initParameter <code>excludedAgents</code>
  * is set to a comma separated list of user agents, then these agents will be excluded from gzip content.
  * </p>
  * <p>Init Parameters:</p>
  * <PRE>
  * bufferSize                 The output buffer size. Defaults to 8192. Be careful as values <= 0 will lead to an
  *                            {@link IllegalArgumentException}.
  *                            @see java.util.zip.GZIPOutputStream#GZIPOutputStream(java.io.OutputStream, int)
  *                            @see java.util.zip.DeflaterOutputStream#DeflaterOutputStream(java.io.OutputStream, Deflater, int)
  *
  * minGzipSize                Content will only be compressed if content length is either unknown or greater
  *                            than <code>minGzipSize</code>.
  *
  * deflateCompressionLevel    The compression level used for deflate compression. (0-9).
  *                            @see java.util.zip.Deflater#Deflater(int, boolean)
  *
  * deflateNoWrap              The noWrap setting for deflate compression. Defaults to true. (true/false)
  *                            @see java.util.zip.Deflater#Deflater(int, boolean)
  *
  * mimeTypes                  Comma separated list of mime types to compress. See description above.
  *
  * excludedAgents             Comma separated list of user agents to exclude from compression. Does a
  *                            {@link String#contains(CharSequence)} to check if the excluded agent occurs
  *                            in the user-agent header. If it does -> no compression
  *
  * excludeAgentPatterns       Same as excludedAgents, but accepts regex patterns for more complex matching.
  *
  * excludePaths               Comma separated list of paths to exclude from compression.
  *                            Does a {@link String#startsWith(String)} comparison to check if the path matches.
  *                            If it does match -> no compression. To match subpaths use <code>excludePathPatterns</code>
  *                            instead.
  *
  * excludePathPatterns        Same as excludePath, but accepts regex patterns for more complex matching.
  * </PRE>
  */
 @Singleton
 public class TransparentCompressionFilter implements Filter
 {
     private static final Log LOG = Log.findLog();
 
     private final static String LZ4 = "lz4";
     private final static String GZIP="gzip";
     private final static String DEFLATE="deflate";
 
     protected Set<String> _mimeTypes;
     protected int _bufferSize=8192;
     protected int _minGzipSize=256;
     protected int _deflateCompressionLevel=Deflater.DEFAULT_COMPRESSION;
     protected boolean _deflateNoWrap = true;
     protected Set<String> _excludedPaths;
     protected Set<Pattern> _excludedPathPatterns;
 
 
     /* ------------------------------------------------------------ */
     /**
      * @see org.eclipse.jetty.servlets.UserAgentFilter#init(javax.servlet.FilterConfig)
      */
     @Override
     public void init(FilterConfig filterConfig) throws ServletException
     {
         String tmp=filterConfig.getInitParameter("bufferSize");
         if (tmp!=null)
             _bufferSize=Integer.parseInt(tmp);
 
         tmp=filterConfig.getInitParameter("minGzipSize");
         if (tmp!=null)
             _minGzipSize=Integer.parseInt(tmp);
 
         tmp=filterConfig.getInitParameter("deflateCompressionLevel");
         if (tmp!=null)
             _deflateCompressionLevel=Integer.parseInt(tmp);
 
         tmp=filterConfig.getInitParameter("deflateNoWrap");
         if (tmp!=null)
             _deflateNoWrap=Boolean.parseBoolean(tmp);
 
         tmp=filterConfig.getInitParameter("mimeTypes");
         if (tmp!=null)
         {
             _mimeTypes=new HashSet<String>();
             StringTokenizer tok = new StringTokenizer(tmp,",",false);
             while (tok.hasMoreTokens())
                 _mimeTypes.add(tok.nextToken());
         }
 
         tmp=filterConfig.getInitParameter("excludePaths");
         if (tmp!=null)
         {
             _excludedPaths=new HashSet<String>();
             StringTokenizer tok = new StringTokenizer(tmp,",",false);
             while (tok.hasMoreTokens())
                 _excludedPaths.add(tok.nextToken());
         }
 
         tmp=filterConfig.getInitParameter("excludePathPatterns");
         if (tmp!=null)
         {
             _excludedPathPatterns=new HashSet<Pattern>();
             StringTokenizer tok = new StringTokenizer(tmp,",",false);
             while (tok.hasMoreTokens())
                 _excludedPathPatterns.add(Pattern.compile(tok.nextToken()));
         }
     }
 
     /* ------------------------------------------------------------ */
     /**
      * @see org.eclipse.jetty.servlets.UserAgentFilter#destroy()
      */
     @Override
     public void destroy()
     {
     }
 
     /* ------------------------------------------------------------ */
     /**
      * @see org.eclipse.jetty.servlets.UserAgentFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
      */
     @Override
     public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
     {
         HttpServletRequest request=(HttpServletRequest)req;
         HttpServletResponse response=(HttpServletResponse)res;
 
         String compressionType = selectCompression(request.getHeader("accept-encoding"));
         if (compressionType!=null && !response.containsHeader("Content-Encoding") && !HttpMethods.HEAD.equalsIgnoreCase(request.getMethod()))
         {
             String requestURI = request.getRequestURI();
             if (isExcludedPath(requestURI))
             {
                 chain.doFilter(request,response);
                 return;
             }
 
             CompressedResponseWrapper wrappedResponse = createWrappedResponse(request,response,compressionType);
 
             boolean exceptional=true;
             try
             {
                 chain.doFilter(request,wrappedResponse);
                 exceptional=false;
             }
             finally
             {
                 Continuation continuation = ContinuationSupport.getContinuation(request);
                 if (continuation.isSuspended() && continuation.isResponseWrapped())
                 {
                     continuation.addContinuationListener(new ContinuationListenerWaitingForWrappedResponseToFinish(wrappedResponse));
                 }
                 else if (exceptional && !response.isCommitted())
                 {
                     wrappedResponse.resetBuffer();
                     wrappedResponse.noCompression();
                 }
                 else
                     wrappedResponse.finish();
             }
         }
         else
         {
             chain.doFilter(request,response);
         }
     }
 
     /* ------------------------------------------------------------ */
     private String selectCompression(String encodingHeader)
     {
         // TODO, this could be a little more robust.
         // prefer gzip over deflate
         if (encodingHeader!=null)
         {
             if (encodingHeader.toLowerCase().contains(LZ4))
                 return LZ4;
             else if (encodingHeader.toLowerCase().contains(GZIP))
                 return GZIP;
             else if (encodingHeader.toLowerCase().contains(DEFLATE))
                 return DEFLATE;
         }
         return null;
     }
 
     protected CompressedResponseWrapper createWrappedResponse(HttpServletRequest request, HttpServletResponse response, final String compressionType)
     {
         CompressedResponseWrapper wrappedResponse = null;
         if (compressionType.equals(LZ4))
         {
             wrappedResponse = new CompressedResponseWrapper(request,response)
             {
                 @Override
                 protected AbstractCompressedStream newCompressedStream(HttpServletRequest request,HttpServletResponse response,long contentLength,int bufferSize, int minCompressSize) throws IOException
                 {
                     return new AbstractCompressedStream(compressionType,request,response,contentLength,bufferSize,minCompressSize)
                     {
                         @Override
                         protected DeflaterOutputStream createStream() throws IOException
                         {
                             return new DeflaterShim(new LZ4BlockOutputStream(_response.getOutputStream(),_bufferSize));
                         }
                     };
                 }
             };
         }
         else if (compressionType.equals(GZIP))
         {
             wrappedResponse = new CompressedResponseWrapper(request,response)
             {
                 @Override
                 protected AbstractCompressedStream newCompressedStream(HttpServletRequest request,HttpServletResponse response,long contentLength,int bufferSize, int minCompressSize) throws IOException
                 {
                     return new AbstractCompressedStream(compressionType,request,response,contentLength,bufferSize,minCompressSize)
                     {
                         @Override
                         protected DeflaterOutputStream createStream() throws IOException
                         {
                             return new GZIPOutputStream(_response.getOutputStream(),_bufferSize);
                         }
                     };
                 }
             };
         }
         else if (compressionType.equals(DEFLATE))
         {
             wrappedResponse = new CompressedResponseWrapper(request,response)
             {
                 @Override
                 protected AbstractCompressedStream newCompressedStream(HttpServletRequest request,HttpServletResponse response,long contentLength,int bufferSize, int minCompressSize) throws IOException
                 {
                     return new AbstractCompressedStream(compressionType,request,response,contentLength,bufferSize,minCompressSize)
                     {
                         @Override
                         protected DeflaterOutputStream createStream() throws IOException
                         {
                             return new DeflaterOutputStream(_response.getOutputStream(),new Deflater(_deflateCompressionLevel,_deflateNoWrap));
                         }
                     };
                 }
             };
         }
         else
         {
             throw new IllegalStateException(compressionType + " not supported");
         }
         configureWrappedResponse(wrappedResponse);
         return wrappedResponse;
     }
 
     protected void configureWrappedResponse(CompressedResponseWrapper wrappedResponse)
     {
         wrappedResponse.setMimeTypes(_mimeTypes);
         wrappedResponse.setBufferSize(_bufferSize);
         wrappedResponse.setMinCompressSize(_minGzipSize);
     }
 
    private class ContinuationListenerWaitingForWrappedResponseToFinish implements ContinuationListener{
 
         private final CompressedResponseWrapper wrappedResponse;
 
         public ContinuationListenerWaitingForWrappedResponseToFinish(CompressedResponseWrapper wrappedResponse)
         {
             this.wrappedResponse = wrappedResponse;
         }
 
         @Override
         public void onComplete(Continuation continuation)
         {
             try
             {
                 wrappedResponse.finish();
             }
             catch (IOException e)
             {
                 LOG.warn(e);
             }
         }
 
         @Override
         public void onTimeout(Continuation continuation)
         {
         }
     }
 
     /**
      * Checks to see if the path is excluded
      *
      * @param requestURI
      *            the request uri
      * @return boolean true if excluded
      */
     private boolean isExcludedPath(String requestURI)
     {
         if (requestURI == null)
             return false;
         if (_excludedPaths != null)
         {
             for (String excludedPath : _excludedPaths)
             {
                 if (requestURI.startsWith(excludedPath))
                 {
                     return true;
                 }
             }
         }
         if (_excludedPathPatterns != null)
         {
             for (Pattern pattern : _excludedPathPatterns)
             {
                 if (pattern.matcher(requestURI).matches())
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Obnoxiously, the {@link AbstractCompressedStream} requires a subclass of
      * DeflaterOutputStream for no good reason.  So we wrap the LZ4 output stream
      * to look like a Deflate stream, but then we completely ignore the
      * superclass... :-(
      */
     private static class DeflaterShim extends DeflaterOutputStream
     {
 
         DeflaterShim(OutputStream out)
         {
             super(out);
         }
 
         @Override
         public void write(int b) throws IOException
         {
             out.write(b);
         }
 
         @Override
         public void write(byte[] b) throws IOException
         {
             out.write(b);
         }
 
         @Override
         public void write(byte[] b, int off, int len) throws IOException
         {
             out.write(b, off, len);
         }
 
         @Override
         protected void deflate() throws IOException
         {
             throw new UnsupportedOperationException("We don't want to deflate...");
         }
 
         @Override
         public void flush() throws IOException
         {
             out.flush();
         }
 
         @Override
         public void close() throws IOException
         {
             out.close();
         }
     }
 }
