 package com.ingemark.requestage;
 
 import static com.ingemark.requestage.Message.INIT;
 import static com.ingemark.requestage.StressTester.fac;
 import static com.ingemark.requestage.Util.nettySend;
 import static com.ingemark.requestage.Util.now;
 import static com.ingemark.requestage.Util.sneakyThrow;
 import static com.ingemark.requestage.Util.wrapper;
 import static com.ingemark.requestage.script.JsFunctions.parseXml;
 import static com.ingemark.requestage.script.JsFunctions.prettyXml;
 import static org.mozilla.javascript.Context.getCurrentContext;
 import static org.mozilla.javascript.Context.javaToJS;
 import static org.mozilla.javascript.ScriptRuntime.constructError;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.mozilla.javascript.BaseFunction;
 import org.mozilla.javascript.Callable;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextAction;
 import org.mozilla.javascript.NativeJSON;
 import org.mozilla.javascript.NativeJavaObject;
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
 import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
 import com.ning.http.client.AsyncHttpClientConfig;
 import com.ning.http.client.AsyncHttpClientConfig.Builder;
 import com.ning.http.client.HttpResponseBodyPart;
 import com.ning.http.client.ProxyServer;
 import com.ning.http.client.Response;
 
 public class JsHttp extends BaseFunction
 {
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
     putProperty(this, "declare", new Callable() {
       public Object call(Context _1, Scriptable _2, Scriptable _3, Object[] args) {
         for (Object name : args)
           tester.lsmap.put(name.toString(), new LiveStats(index++, name.toString()));
         return null;
       }});
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
 
   @Override public Object call(Context _1, Scriptable scope, Scriptable _3, Object[] args) {
     return new ReqBuilder(scope, ScriptRuntime.toString(args[0])).wrapper;
   }
   @Override public int getArity() { return 1; }
 
   private void declareReq(String name) {
     log.debug("Adding " + name + " under " + index);
     tester.lsmap.put(name, new LiveStats(index++, name));
   }
 
   public class ReqBuilder {
     final NativeJavaObject wrapper;
     final String name;
     double sleepLow, sleepHigh;
     public BoundRequestBuilder brb;
     private Acceptor acceptor = JsHttp.this.acceptor;
 
     ReqBuilder(Scriptable scope, String name) {
       this.wrapper = wrapper(scope, this);
       this.name = name;
     }
     ReqBuilder(Scriptable scope, String method, String url) { this(scope, null); brb(method, url); }
 
     public Scriptable get(String url) { return brb("GET", url); }
     public Scriptable put(String url) { return brb("PUT", url); }
     public Scriptable post(String url) { return brb("POST", url); }
     public Scriptable delete(String url) { return brb("DELETE", url); }
     public Scriptable head(String url) { return brb("HEAD", url); }
     public Scriptable options(String url) { return brb("OPTIONS", url); }
 
     public Scriptable body(final Object body) {
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
       return wrapper;
     }
 
     public Scriptable accept(String qualifier) {
       acceptor = acceptors.get(qualifier);
       return wrapper;
     }
     public Scriptable sleep(double time) { return sleep(time, time); }
     public Scriptable sleep(double lowTime, double highTime) {
       sleepLow = lowTime; sleepHigh = highTime;
       return wrapper;
     }
     public void go() { go0(null, true); }
     public void go(Object f) { go0(f, false); }
     public void goDiscardingBody(Object f) { go0(f, true); }
 
     private Scriptable brb(String method, String url) {
       brb = tester.client.prepareConnect(url).setMethod(method.toUpperCase());
       return wrapper;
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
         ).get(), f);
       } catch (Exception e) { sneakyThrow(e); }
     }
 
     private void executeTest(
         final ReqBuilder reqBuilder, final Callable f, final boolean discardBody)
     {
       if (reqBuilder.sleepLow > 0)
         tester.sched.schedule(new Runnable() { @Override public void run() {
           executeTest0(reqBuilder, f, discardBody);
         }}, randomizeSleep(reqBuilder), TimeUnit.MILLISECONDS);
      else executeTest0(reqBuilder, f, discardBody);
     }
     private void executeTest0(ReqBuilder reqBuilder, final Callable f, final boolean discardBody) {
       final String reqName = reqBuilder.name;
       final LiveStats liveStats = resolveLiveStats(reqName);
       final int startSlot = liveStats.registerReq();
       final long start = now();
       try {
         reqBuilder.brb.execute(new AsyncCompletionHandler<Void>() {
           public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
             return discardBody? STATE.CONTINUE : super.onBodyPartReceived(bodyPart);
           }
           @Override public Void onCompleted(final Response resp) {
             fac.call(new ContextAction() {
               @Override public Object run(Context cx) {
                 Throwable failure = null;
                 try { handleResponse(resp, f); }
                 catch (Throwable t) { failure = t; }
                 finally { liveStats.deregisterReq(startSlot, now(), start, failure); }
                 return null;
               }
             });
             return null;
           }
           @Override public void onThrowable(Throwable t) {
             liveStats.deregisterReq(startSlot, now(), start, t);
           }
       });
       } catch (IOException e) { sneakyThrow(e); }
     }
     private LiveStats resolveLiveStats(String reqName) {
       LiveStats ret = null;
       if (reqName != null) ret = tester.lsmap.get(reqName);
       return ret != null? ret : mockLiveStats;
     }
 
     private void handleResponse(Response resp, Callable f) {
       if (!acceptor.acceptable(resp))
         throw constructError("FailedResponse", resp.getStatusCode() + " " + resp.getStatusText());
       if (f != null) tester.jsScope.call(f, betterResponse(resp));
     }
   }
   static final LiveStats mockLiveStats = new LiveStats(0, "") {
     @Override int registerReq() { return -1; }
     @Override void deregisterReq(int startSlot, long now, long start, Throwable t) { }
   };
 
   public Scriptable betterAhccBuilder(final AsyncHttpClientConfig.Builder b) {
     return (Scriptable)fac.call(new ContextAction() {
       @Override public Object run(Context cx) {
         final Scriptable bb = (Scriptable) javaToJS(new BetterAhccBuilder(b), getParentScope());
         bb.setPrototype((Scriptable) javaToJS(b, getParentScope()));
         return bb;
       }
     });
   }
   public class BetterAhccBuilder {
     private final Builder b;
     BetterAhccBuilder(Builder b) { this.b = b; }
     public Object proxy(String proxyStr) {
       b.setProxyServer(toProxyServer(proxyStr));
       return this;
     }
   }
   private static ProxyServer toProxyServer(String proxyString) {
     if (proxyString == null) return null;
     final String[] parts = proxyString.split(":");
     return new ProxyServer(parts[0], parts.length > 1? Integer.valueOf(parts[1]) : 80);
   }
 
   Scriptable betterResponse(Response r) {
     final Scriptable br = (Scriptable) javaToJS(new BetterResponse(r), getParentScope());
     br.setPrototype((Scriptable) javaToJS(r, getParentScope()));
     return br;
   }
   public class BetterResponse {
     private final Response r;
     BetterResponse(Response r) { this.r = r; }
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
   static long randomizeSleep(ReqBuilder r) {
     return (long) (1000*(r.sleepLow + Math.random()*(r.sleepHigh-r.sleepLow)));
   }
 
   private void defineHttpMethods(String... methods) {
     for (final String m : methods) putProperty(this, m, new Callable() {
       public Object call(Context _1, Scriptable scope, Scriptable _3, Object[] args) {
         return new ReqBuilder(scope, m, ScriptRuntime.toString(args[0])).wrapper;
       }
     });
   }
 
   interface Acceptor { boolean acceptable(Response r); }
 }
