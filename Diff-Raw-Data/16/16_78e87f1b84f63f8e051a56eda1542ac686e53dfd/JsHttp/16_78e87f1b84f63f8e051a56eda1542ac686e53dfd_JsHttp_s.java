 package com.ingemark.requestage;
 
 import static com.ingemark.requestage.Message.INIT;
 import static com.ingemark.requestage.StressTester.fac;
 import static com.ingemark.requestage.Util.builderWrapper;
 import static com.ingemark.requestage.Util.nettySend;
 import static com.ingemark.requestage.Util.now;
 import static com.ingemark.requestage.Util.sneakyThrow;
 import static com.ingemark.requestage.script.JsFunctions.parseXml;
 import static com.ingemark.requestage.script.JsFunctions.prettyXml;
 import static org.mozilla.javascript.Context.getCurrentContext;
 import static org.mozilla.javascript.ScriptRuntime.constructError;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.mozilla.javascript.BaseFunction;
 import org.mozilla.javascript.Callable;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextAction;
 import org.mozilla.javascript.NativeJSON;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.ScriptRuntime;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.json.JsonParser;
 import org.mozilla.javascript.json.JsonParser.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ingemark.requestage.script.JdomBuilder;
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncCompletionHandlerBase;
 import com.ning.http.client.AsyncHandler;
 import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
 import com.ning.http.client.AsyncHttpClientConfig;
 import com.ning.http.client.AsyncHttpClientConfig.Builder;
 import com.ning.http.client.HttpResponseBodyPart;
 import com.ning.http.client.HttpResponseHeaders;
 import com.ning.http.client.HttpResponseStatus;
 import com.ning.http.client.ProxyServer;
 import com.ning.http.client.Response;
 
 public class JsHttp extends BaseFunction
 {
   static final ThreadLocal<AtomicInteger> PENDING_EXECUTIONS = new ThreadLocal<AtomicInteger>();
   private static final Logger log = LoggerFactory.getLogger(JsHttp.class);
   private static final Map<String, Acceptor> acceptors = hashMap(
       "any", new Acceptor() { public boolean acceptable(Response r) { return true; } },
       "success", new Acceptor() { public boolean acceptable(Response r) {
         final int st = r.getStatusCode();
         return st >= 200 && st < 400;
       }},
       "ok", new Acceptor() { public boolean acceptable(Response r) {
         final int st = r.getStatusCode();
         return st >= 200 && st < 300;
       }}
     );
   private final StressTester tester;
   volatile int index;
   volatile Acceptor acceptor = acceptors.get("success");
 
   public JsHttp(ScriptableObject parentScope, final StressTester testr) {
     super(parentScope, getFunctionPrototype(parentScope));
     this.tester = testr;
     defineHttpMethods("get", "put", "post", "delete", "head", "options");
     putProperty(this, "acceptableStatus", new Callable() {
       public Object call(Context _1, Scriptable _2, Scriptable _3, Object[] args) {
         acceptor = acceptors.get(args[0]);
         return JsHttp.this;
     }});
     putProperty(this, "declare", new Callable() {
       public Object call(Context _1, Scriptable _2, Scriptable _3, Object[] args) {
         if (!testr.lsmap.isEmpty())
           throw ScriptRuntime.constructError("LateDeclare",
               "Must declare the request names before creating any named request");
         testr.explicitLsMap = true;
         for (Object o : args) declareReq(o.toString());
         return JsHttp.this;
       }});
   }
 
   public void initDone() { index = -1; }
 
   @Override public Scriptable call(Context _1, Scriptable scope, Scriptable _3, Object[] args) {
     final BoundRequestBuilder brb = tester.client.prepareConnect("");
     return builderWrapper(scope, new ReqBuilder(scope, brb, ScriptRuntime.toString(args[0])), brb);
   }
   @Override public int getArity() { return 1; }
 
   private void declareReq(String name) {
     log.debug("Adding " + name + " under " + index);
     tester.lsmap.put(name, new LiveStats(index++, name));
   }
 
   public class ReqBuilder {
     final String name;
     double delayLow, delayHigh;
     private final BoundRequestBuilder brb;
     private Acceptor acceptor = JsHttp.this.acceptor;
 
     ReqBuilder(Scriptable scope, BoundRequestBuilder brb, String name) {
       this.brb = brb;
       this.name = name;
     }
     ReqBuilder(Scriptable scope, BoundRequestBuilder brb, String method, String url) {
       this(scope, brb, null);
       initBrb(method, url);
     }
 
     public ReqBuilder get(String url) { return initBrb("GET", url); }
     public ReqBuilder put(String url) { return initBrb("PUT", url); }
     public ReqBuilder post(String url) { return initBrb("POST", url); }
     public ReqBuilder delete(String url) { return initBrb("DELETE", url); }
     public ReqBuilder head(String url) { return initBrb("HEAD", url); }
     public ReqBuilder options(String url) { return initBrb("OPTIONS", url); }
 
     public ReqBuilder body(final Object body) {
       if (body instanceof JdomBuilder) {
         brb.addHeader("Content-Type", "text/xml;charset=UTF-8");
         brb.setBody(body.toString());
       } else if (body instanceof Scriptable) {
         brb.addHeader("Content-Type", "application/json;charset=UTF-8");
         fac.call(new ContextAction() { @Override public Object run(Context cx) {
           brb.setBody((String)NativeJSON.stringify(cx, getParentScope(), body, null, ""));
           return null;
         }});
       }
       else brb.setBody(body.toString());
       return this;
     }
 
     public ReqBuilder accept(String qualifier) {
       acceptor = acceptors.get(qualifier);
       return this;
     }
     public ReqBuilder delay(double time) { return delay(time, time); }
     public ReqBuilder delay(double lowTime, double highTime) {
       delayLow = lowTime; delayHigh = highTime;
       return this;
     }
     public void go() { go0(null, true); }
     public void go(Object f) { go0(f, false); }
     public void goDiscardingBody(Object f) { go0(f, true); }
 
     private ReqBuilder initBrb(String method, String url) {
       brb.setUrl(url).setMethod(method.toUpperCase());
       return this;
     }
 
     private void go0(Object f, boolean discardBody) {
       final Callable c;
       if (f instanceof Callable) c = (Callable)f;
       else { c = null; discardBody = true; }
       if (index >= 0) executeInit(this, c, discardBody); else executeTest(this, c, discardBody);
     }
 
     private void executeInit(ReqBuilder reqBuilder, Callable f, final boolean discardBody) {
       if (reqBuilder.name != null) {
         nettySend(tester.channel, new Message(INIT, reqBuilder.name));
         if (!tester.explicitLsMap) declareReq(reqBuilder.name);
       }
       try {
         handleResponse(reqBuilder.brb.execute(new AsyncCompletionHandlerBase() {
           public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
             return discardBody? STATE.CONTINUE : super.onBodyPartReceived(bodyPart); }}
        ).get(), -1, f);
       } catch (Exception e) { sneakyThrow(e); }
     }
 
     private void executeTest(
         final ReqBuilder reqBuilder, final Callable f, final boolean discardBody)
     {
       final AtomicInteger pendingExecs = PENDING_EXECUTIONS.get();
       pendingExecs.incrementAndGet();
       if (reqBuilder.delayLow > 0)
         tester.sched.schedule(new Runnable() { @Override public void run() {
           executeTest0(reqBuilder, f, pendingExecs, discardBody);
         }}, randomizeDelay(reqBuilder), TimeUnit.MILLISECONDS);
       else executeTest0(reqBuilder, f, pendingExecs, discardBody);
     }
     private void executeTest0(ReqBuilder reqBuilder, final Callable f,
         final AtomicInteger pendingExecs, final boolean discardBody)
     {
       final String reqName = reqBuilder.name;
       final LiveStats liveStats = resolveLiveStats(reqName);
       final int startSlot = liveStats.registerReq();
       final long start = now();
       try {
         reqBuilder.brb.execute(new AsyncCompletionHandler<Void>() {
           volatile int respSize;
           @Override public AsyncHandler.STATE onStatusReceived(HttpResponseStatus status)
               throws Exception {
             respSize += status.getProtocolText().length() + status.getStatusText().length() + 7;
             return super.onStatusReceived(status);
           }
           @Override public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
             for (Map.Entry<String, List<String>> e : headers.getHeaders())
               for (String s : e.getValue()) respSize += e.getKey().length() + s.length() + 4;
             return super.onHeadersReceived(headers);
           }
           public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
             respSize += bodyPart.getBodyByteBuffer().remaining();
             return discardBody? STATE.CONTINUE : super.onBodyPartReceived(bodyPart);
           }
           @Override public Void onCompleted(final Response resp) {
             fac.call(new ContextAction() {
               @Override public Object run(Context cx) {
                 Throwable failure = null;
                try { handleResponse(resp, respSize, f); }
                 catch (Throwable t) { failure = t; }
                 finally {
                   if (pendingExecs.decrementAndGet() == 0) tester.scriptsRunning.decrementAndGet();
                   liveStats.deregisterReq(startSlot, now(), start, respSize, failure);
                 }
                 return null;
               }
             });
             return null;
           }
           @Override public void onThrowable(Throwable t) {
             if (pendingExecs.decrementAndGet() == 0) tester.scriptsRunning.decrementAndGet();
             liveStats.deregisterReq(startSlot, now(), start, respSize, t);
           }
       });
       } catch (IOException e) { sneakyThrow(e); }
     }
     private LiveStats resolveLiveStats(String reqName) {
       LiveStats ret = null;
       if (reqName != null) ret = tester.lsmap.get(reqName);
       return ret != null? ret : mockLiveStats;
     }
 
    private void handleResponse(Response resp, int respSize, Callable f) {
       if (!acceptor.acceptable(resp))
         throw constructError("FailedResponse", resp.getStatusCode() + " " + resp.getStatusText());
      if (f != null) tester.jsScope.call(f, betterResponse(resp, respSize));
     }
   }
   static final LiveStats mockLiveStats = new LiveStats(0, "") {
     @Override int registerReq() { return -1; }
     @Override void deregisterReq(int startSlot, long now, long start, int size, Throwable t) { }
   };
 
   public Scriptable configBuilder(final AsyncHttpClientConfig.Builder b) {
     return (Scriptable)fac.call(new ContextAction() {
       @Override public Object run(Context cx) {
         return builderWrapper(getParentScope(), new ConfigBuilder(b), b);
       }
     });
   }
   public class ConfigBuilder {
     private final Builder b;
     ConfigBuilder(Builder b) { this.b = b; }
     public ConfigBuilder proxy(String proxyStr) {
       b.setProxyServer(toProxyServer(proxyStr));
       return this;
     }
   }
   private static ProxyServer toProxyServer(String proxyString) {
     if (proxyString == null) return null;
     final String[] parts = proxyString.split(":");
     return new ProxyServer(parts[0], parts.length > 1? Integer.valueOf(parts[1]) : 80);
   }
 
   Scriptable betterResponse(Response r, int size) {
     final Scriptable wrapped = new NativeJavaObject(
         getParentScope(), new BetterResponse(r, size), BetterResponse.class);
     final NativeJavaObject protoWrapper = new NativeJavaObject(getParentScope(), r, Response.class);
     protoWrapper.setPrototype(new NativeObject());
     wrapped.setPrototype(protoWrapper);
     return wrapped;
   }
   public class BetterResponse {
     private final Response r;
     public final int size;
     BetterResponse(Response r, int size) { this.r = r; this.size = size; }
     public Object xmlBody() { return parseXml(this.r); }
     public Object prettyXmlBody() { return prettyXml(r); }
     public Object jsonBody() {
       try { return new JsonParser(getCurrentContext(), getParentScope()).parseValue(
             responseBody(this.r)); }
       catch (ParseException e) { return sneakyThrow(e); }
     }
     public String stringBody() { return responseBody(this.r); }
     @Override public String toString() { return stringBody(); }
   }
 
   static String responseBody(Response r) {
     try { return r.getResponseBody(); } catch (IOException e) { return sneakyThrow(e); }
   }
   private static Map<String, Acceptor> hashMap(Object... kvs) {
     final Map<String, Acceptor> r = new HashMap<String, Acceptor>();
     for (int i = 0; i < kvs.length;) r.put((String)kvs[i++], (Acceptor)kvs[i++]);
     return r;
   }
   static long randomizeDelay(ReqBuilder r) {
     return (long) (1000*(r.delayLow + Math.random()*(r.delayHigh-r.delayLow)));
   }
 
   private void defineHttpMethods(String... methods) {
     for (final String m : methods) putProperty(this, m, new Callable() {
       public Object call(Context _1, Scriptable scope, Scriptable _3, Object[] args) {
         final BoundRequestBuilder brb = tester.client.prepareConnect("");
         return builderWrapper(
             scope, new ReqBuilder(scope, brb, m, ScriptRuntime.toString(args[0])), brb);
       }
     });
   }
 
   interface Acceptor { boolean acceptable(Response r); }
 }
