 package ru.forxy.common.logging;
 
 import ru.forxy.common.exceptions.ServiceException;
 import ru.forxy.common.logging.metadata.IHttpFieldExtractor;
 import ru.forxy.common.logging.support.Fields;
 import ru.forxy.common.logging.wrapper.BufferedRequestWrapper;
 import ru.forxy.common.logging.wrapper.BufferedResponseWrapper;
 import ru.forxy.common.support.Context;
 import ru.forxy.common.support.SystemProperties;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Request/Response logging filter
  */
 public class LoggingServletFilter extends AbstractPerformanceLogger implements Filter {
 
     public static enum Configs {
        IsHttpInfoLoggingEnabled
     }
 
     private boolean isHttpInfoLoggingEnabled = false;
 
     @Override
     public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
             throws IOException, ServletException {
         final HttpServletRequest rq = (HttpServletRequest) request;
         final HttpServletResponse rs = (HttpServletResponse) response;
         final String url = getRequestUrl(rq);
         if (isPerformanceLoggingEnabled) {
             final long timestampStart = System.currentTimeMillis();
             final long timestampStartNano = System.nanoTime();
            final BufferedRequestWrapper brq = new BufferedRequestWrapper(rq, -1);
            final BufferedResponseWrapper brs = new BufferedResponseWrapper(rs, -1);
 
             Context.push();
             try {
                 //handle request, add request data to context
                 handleRequest(brq, timestampStart, url);
                 //chain downstream
                 chain.doFilter(brq, brs);
             } catch (IOException io) {
                 processException(io);
                 throw io;
             } catch (ServletException se) {
                 processException(se);
                 throw se;
             } catch (ServiceException se) {
                 processException(se);
                 throw se;
             } catch (Exception e) {
                 processException(e);
                 if (exceptionHandler != null) {
                     exceptionHandler.handleException(e);
                 } else {
                     throw new ServletException(e);
                 }
             } finally {
                 //handle response, add response data to context
                 handleResponse(brq, brs, timestampStart, timestampStartNano);
                 Context.pop();
             }
         } else {
             chain.doFilter(rq, rs);
         }
     }
 
     private void handleRequest(final BufferedRequestWrapper rq, final long timestampStart, final String url) {
         Context.addGlobal(Fields.ProductName, SystemProperties.getServiceName());
         Context.addGlobal(Fields.ActivityGUID, UUID.randomUUID().toString());
         Context.addFrame(Fields.ActivityName, activityName);
         Context.addFrame(Fields.ActivityStep, Fields.ActivitySteps.rq);
         Context.addFrame(Fields.TimestampStart, new Date(timestampStart));
         Context.addFrame(Fields.Timestamp, new Date(timestampStart));
         Context.addFrame(Fields.HostLocal, SystemProperties.getHostAddress());
         Context.addFrame(Fields.HostRemote, rq.getRemoteAddr());
 
         if (requestFieldExtractors != null
                 || isHttpInfoLoggingEnabled
                 || isPayloadLoggingEnabled) {
             final Map<String, List<String>> rqHeaders = getHeaderMap(rq);
             //capture request http details
             if (isHttpInfoLoggingEnabled) {
                 Context.addFrame(Fields.RequestURL, url);
                 Context.addFrame(Fields.RequestMethod, rq.getMethod());
                 Context.addFrame(Fields.RequestHeaders, rqHeaders);
             }
             //extract request custom fields from payload
             final byte[] payload = rq.getRequestBody();
             if (requestFieldExtractors != null) {
                 for (final IHttpFieldExtractor extractor : requestFieldExtractors) {
                     final Map<String, Object> frame = Context.peek().getFrame();
                     final Map<String, Object> extracted = extractor.extract(payload, frame, rq, null, rqHeaders, null);
                     frame.putAll(extracted);
                 }
             }
             //capture request payload
             if (isPayloadLoggingEnabled) {
                 Context.addFrame(Fields.RequestPayload, payload);
                 writeFrame(requestPayloadWriter);
             }
         }
         //write request frame
         writeFrame(requestWriter);
     }
 
     private void handleResponse(final BufferedRequestWrapper rq, final BufferedResponseWrapper rs,
                                 final long timestampStart, final long timestampStartNano) {
         final long timestampEnd = System.currentTimeMillis();
         final long timestampEndNano = System.nanoTime();
         Context.addFrame(Fields.ActivityStep, Fields.ActivitySteps.rs);
         Context.addFrame(Fields.Timestamp, new Date(timestampEnd));
         Context.addFrame(Fields.TimestampEnd, new Date(timestampEnd));
         Context.addFrame(Fields.Duration, timestampEnd - timestampStart);
         Context.addFrame(Fields.DurationNano, (timestampEndNano - timestampStartNano) / 1000000);
         //capture response http details
         if (isHttpInfoLoggingEnabled) {
             Context.addFrame(Fields.ResponseStatus, rs.getResponseStatus());
             Context.addFrame(Fields.ResponseURL, rs.getResponseRedirectURL());
             Context.addFrame(Fields.ResponseHeaders, rs.getResponseHeaders());
         }
         if (isPayloadLoggingEnabled || responseFieldExtractors != null) {
             final byte[] payload = rs.getResponseBody();
             //extract response custom fields from payload
             if (responseFieldExtractors != null) {
                 for (final IHttpFieldExtractor fe : responseFieldExtractors) {
                     final Map<String, Object> frame = Context.peek().getFrame();
                     final Map<String, Object> extracted = fe.extract(payload, frame,
                             rq, rs, getHeaderMap(rq), rs.getResponseHeaders());
                     frame.putAll(extracted);
                 }
             }
             //capture response payload
             if (isPayloadLoggingEnabled) {
                 Context.addFrame(Fields.ResponsePayload, payload);
                 writeFrame(responsePayloadWriter);
             }
         }
         writeFrame(responseWriter);
     }
 
     private static Map<String, List<String>> getHeaderMap(final HttpServletRequest rq) {
         Map<String, List<String>> result = null;
         final Enumeration names = rq.getHeaderNames();
         if (names != null && names.hasMoreElements()) {
             result = new LinkedHashMap<String, List<String>>();
             while (names.hasMoreElements()) {
                 final String name = (String) names.nextElement();
 
                 @SuppressWarnings("unchecked")
                 final Enumeration<String> values = rq.getHeaders(name);
 
                 if (values != null) {
                     result.put(name, Collections.list(values));
                 }
             }
         }
         return result;
     }
 
     private static String getRequestUrl(final HttpServletRequest rq) {
         final StringBuilder url = new StringBuilder(256).append(rq.getRequestURL());
         if (rq.getQueryString() != null) {
             url.append("?").append(rq.getQueryString());
         }
         return url.toString();
     }
 
     @Override
     public void init(final FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void destroy() {
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         super.afterPropertiesSet();
         if (configuration != null) {
             isHttpInfoLoggingEnabled = configuration.getBoolean(Configs.IsHttpInfoLoggingEnabled);
         }
     }
 }
