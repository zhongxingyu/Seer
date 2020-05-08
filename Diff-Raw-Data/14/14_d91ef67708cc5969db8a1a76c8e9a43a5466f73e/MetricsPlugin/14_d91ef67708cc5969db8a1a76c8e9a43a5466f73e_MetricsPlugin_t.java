 /*
     Copyright 2013, Strategic Gains, Inc.
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 		http://www.apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
  */
 package com.strategicgains.restexpress.plugin.metrics;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.slf4j.Logger;
 
 import com.strategicgains.restexpress.Request;
 import com.strategicgains.restexpress.Response;
 import com.strategicgains.restexpress.RestExpress;
 import com.strategicgains.restexpress.pipeline.MessageObserver;
 import com.strategicgains.restexpress.pipeline.Postprocessor;
 import com.strategicgains.restexpress.pipeline.Preprocessor;
 import com.strategicgains.restexpress.plugin.Plugin;
 import com.strategicgains.restexpress.route.Route;
 import com.strategicgains.util.date.DateAdapterConstants;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Counter;
 import com.yammer.metrics.core.Timer;
 
 /**
  * Enables full metrics on all routes in the service suite via the Yammer (Coda Hale) Metrics library.  Metrics are available
  * via JMX, but can be published to statsd by configuring the Yammer (Coda Hale) Metrics publisher as follows:
  * <p/>
  * GraphiteReporter.enable(1, TimeUnit.MINUTES, "graphite.example.com", 2003);
  * <p/>
  * This plugin maintains metrics for the following:
  * currently active requests (counter), all exceptions occurred (counter), all times (timer, milliseconds/hours),
  * times by route (timer, milliseconds/hours), exceptions by route (counter), counters by return status (counter).
  * <p/>
  * In addition, the overall response time is set in the response header, X-Response-Time, in milliseconds.
  * <p/>
  * Note: Named routes are reported using their name.  Unnamed routes are reported using the URL pattern.
  * 
  * @author toddf
  * @since Feb 11, 2013
  */
 public class MetricsPlugin
 extends MessageObserver
 implements Plugin, Preprocessor, Postprocessor
 {
     private static final Counter ACTIVE_REQUESTS_COUNTER = Metrics.newCounter(MetricsPlugin.class, "activeRequests");
     private static final Counter ALL_EXCEPTIONS_COUNTER = Metrics.newCounter(MetricsPlugin.class, "all-exceptions");
    private static final Timer ALL_TIMES_TIMER = Metrics.newTimer(MetricsPlugin.class, "all-times", TimeUnit.MILLISECONDS, TimeUnit.HOURS);
     
 	private static final ConcurrentHashMap<String, Timer> ROUTE_TIMERS = new ConcurrentHashMap<String, Timer>();
 	private static final ConcurrentHashMap<String, Long> START_TIMES_BY_CORRELATION_ID = new ConcurrentHashMap<String, Long>();
 	private static final ConcurrentHashMap<String, Counter> EXCEPTION_COUNTERS_BY_ROUTE = new ConcurrentHashMap<String, Counter>();
 	private static final ConcurrentHashMap<Integer, Counter> COUNTERS_BY_RESPONSE = new ConcurrentHashMap<Integer, Counter>();
 	
 	private final DateFormat DATE_FORMAT = new SimpleDateFormat(DateAdapterConstants.TIMESTAMP_OUTPUT_FORMAT);
 
 	private boolean isRegistered = false;
 	private List<Logger> loggers = null;
 	private String machineName = null;
 
 	public MetricsPlugin()
 	{
 		super();
 	}
 
 	// SECTION: PLUGIN
 
 	/**
 	 * Register the MetricsPlugin with the RestExpress server.
 	 * 
 	 * @param server
 	 * @return
 	 */
 	@Override
 	public MetricsPlugin register(RestExpress server)
 	{
 		if (isRegistered) return this;
 
 		server.registerPlugin(this);
 		this.isRegistered = true;
 
 		server
 			.addMessageObserver(this)
			.addPreprocessor(this)
 			.addPostprocessor(this);
 
 		return this;
 	}
 	
 	/**
 	 * Add a SLF4J Logger to the MetricsPlugin to log INFO message.
 	 * 
 	 * @param logger an SLF4J logger.
 	 * @return
 	 */
 	public MetricsPlugin logger(Logger logger)
 	{
 		if (loggers == null)
 		{
 			loggers = new ArrayList<Logger>();
 		}
 
 		loggers.add(logger);
 		return this;
 	}
 	
 	/**
 	 * Set the machine name for use only if there is a SLF4J logger added to the MetricsPlugin.
 	 * 
 	 * @param name a unique machine name, used for logging only.
 	 * @return
 	 */
 	public MetricsPlugin machine(String name)
 	{
 		this.machineName = name;
 		return this;
 	}
 
 	@Override
 	public void bind(RestExpress server)
 	{
 		// Do nothing (did it in register).
 	}
 
 	@Override
 	public void shutdown(RestExpress server)
 	{
 		// Do nothing (no resources allocated that need releasing).
 	}
 
 	// SECTION: MESSAGE OBSERVER
 
 	@Override
 	protected void onReceived(Request request, Response response)
 	{
 		ACTIVE_REQUESTS_COUNTER.inc();
 		START_TIMES_BY_CORRELATION_ID.put(request.getCorrelationId(), System.nanoTime());
 	}
 
 	@Override
 	protected void onException(Throwable exception, Request request,
 	    Response response)
 	{
 		ALL_EXCEPTIONS_COUNTER.inc();
 
		String name = getRouteName(request);
 		if (name == null || name.isEmpty()) return;
 
 		Counter exceptionCounter = EXCEPTION_COUNTERS_BY_ROUTE.get(name);
 		if (exceptionCounter == null) return;
 
 		exceptionCounter.inc();
 	}
 
 	@Override
 	protected void onComplete(Request request, Response response)
 	{
 		ACTIVE_REQUESTS_COUNTER.dec();
 		Long duration = getDurationMillis(START_TIMES_BY_CORRELATION_ID.remove(request.getCorrelationId()));
 
 		if (duration != null && duration.longValue() > 0)
 		{
 			ALL_TIMES_TIMER.update(duration, TimeUnit.MILLISECONDS);
 
			String name = getRouteName(request);
 			if (name == null || name.isEmpty()) return;
 
 			ROUTE_TIMERS.get(name).update(duration, TimeUnit.MILLISECONDS);
 		}
 
 		Counter responseCounter = COUNTERS_BY_RESPONSE.get(response.getResponseStatus().getCode());
 		
 		if (responseCounter == null)
 		{
 			responseCounter = Metrics.newCounter(MetricsPlugin.class, getResponseCounterName(response.getResponseStatus()));
 			COUNTERS_BY_RESPONSE.putIfAbsent(response.getResponseStatus().getCode(), responseCounter);
 		}
 
 		responseCounter.inc();
 		log(request, response, duration);
 	}
 
 	private void log(Request request, Response response, Long duration)
     {
 		if (loggers == null) return;
 
 	    StringBuilder builder = new StringBuilder();
 		builder.append("Time=" + ((DateFormat) DATE_FORMAT.clone()).format(new Date()));
 		builder.append(" RequestTime=" + duration);
 		builder.append(" Url=" + request.getUrl());
 		builder.append(" RequestType=" + request.getHttpMethod().getName());
 		builder.append(" RequestFormat=" + request.getFormat());
 		builder.append(" Resource=" + getRouteName(request));
 		
 		if (hasMachineName())
 		{
 			builder.append(" Machine=" + machineName);
 		}
 
 		builder.append(" Status=" + response.getResponseStatus().getCode());
 
 		if (request.getRawHeader("User-Agent") != null)
 		{
 			builder.append(" UserAgent=" + request.getRawHeader("User-Agent"));
 		}
 
 		if (request.getRawHeader("Referer") != null)
 		{
 			builder.append(" UrlReferer=" + request.getRawHeader("Referer"));
 		}
 
 		for (Logger logger : loggers)
 		{
 			logger.info(builder.toString());
 		}
     }
 
 
 	// SECTION: PREPROCESSOR
 
 	@Override
 	public void process(Request request)
 	{
 		String name = getRouteName(request);
 
 		if (name == null || name.isEmpty()) return;
 
 		if (!ROUTE_TIMERS.containsKey(name))
 		{
 			ROUTE_TIMERS.putIfAbsent(name, Metrics.newTimer(MetricsPlugin.class,
 				getTimerName(name), TimeUnit.MILLISECONDS, TimeUnit.HOURS));
 		}
 
 		if (!EXCEPTION_COUNTERS_BY_ROUTE.containsKey(name))
 		{
 			EXCEPTION_COUNTERS_BY_ROUTE.putIfAbsent(name, Metrics.newCounter(
 				MetricsPlugin.class, getExceptionCounterName(name)));
 		}
 	}
 
 
 	// SECTION: POSTPROCESSOR
 
 	/**
 	 * Set the 'X-Response-Time' header to the response time in milliseconds.
 	 */
 	@Override
 	public void process(Request request, Response response)
 	{
 		Long duration = getDurationMillis(START_TIMES_BY_CORRELATION_ID.get(request.getCorrelationId()));
 		
 		if (duration != null && duration.longValue() > 0)
 		{
 			response.addHeader("X-Response-Time", String.valueOf(duration));
 		}
 	}
 
 
 	// SECTION: UTILITY - PRIVATE
 
 	private boolean hasMachineName()
 	{
 		return (machineName != null);
 	}
 
 	private Long getDurationMillis(Long started)
 	{
 		Long duration = 0L;
 
 		if (started != null && started.longValue() > 0)
 		{
 			duration = Math.max(0L, (System.nanoTime() - started) / 1000000);
 		}
 
 		return duration;
 	}
 
 	private String getRouteName(Request request)
 	{
 		String name = request.getResolvedRoute().getName();
 		
 		if (name == null || name.trim().isEmpty())
 		{
 			name = request.getResolvedRoute().getFullPattern();
 		}
 
 		return name;
 	}
 	private String getTimerName(String routeName)
 	{
 		return routeName + "-times";
 	}
 
 	private String getExceptionCounterName(String routeName)
 	{
 		return routeName + "-exceptions";
 	}
 
 	private String getResponseCounterName(HttpResponseStatus responseStatus)
     {
 	    return responseStatus.getCode() + "-" + responseStatus.getReasonPhrase() + "-count";
     }
 }
