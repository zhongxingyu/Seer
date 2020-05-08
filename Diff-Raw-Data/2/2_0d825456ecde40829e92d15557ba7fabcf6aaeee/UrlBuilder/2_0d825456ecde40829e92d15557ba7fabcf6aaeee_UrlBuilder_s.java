 package com.ingemark.requestage.script;
 
 import static com.ingemark.requestage.Util.sneakyThrow;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.ScriptRuntime;
 import org.mozilla.javascript.Scriptable;
 
 public class UrlBuilder
 {
  private static final Pattern urlBaseRegex = Pattern.compile("(.+?)://(.+?)(/.*?)?\\?(.*)");
   private final NativeJavaObject wrapper;
   private final StringBuilder
     pathBuilder = new StringBuilder(16),
     qparamsBuilder = new StringBuilder(16);
   private final String scheme, authority;
   private boolean withinQuery;
 
   private UrlBuilder(Scriptable scope, String urlBase) {
     wrapper = new NativeJavaObject(scope, this, getClass());
     wrapper.setPrototype(new NativeObject());
     final Matcher m = urlBaseRegex.matcher(urlBase);
     if (!m.matches())
       throw ScriptRuntime.constructError("IllegalUrl", "The URL " + urlBase + " is invalid");
     scheme = m.group(1); authority = m.group(2);
     pathBuilder.append(m.group(3));
     qparamsBuilder.append(m.group(4));
     if (m.group(4).length() > 0) withinQuery = true;
   }
 
   static Scriptable urlBuilder(Scriptable scope, String urlBase) {
     return new UrlBuilder(scope, urlBase).wrapper;
   }
 
   public Scriptable s(Object... segs) {
     if (withinQuery)
       throw new IllegalStateException("Cannot add path segments after query params");
     for (Object seg : segs) {
       if (pathBuilder.charAt(pathBuilder.length()-1) != '/') pathBuilder.append('/');
       if (seg != null) {
         if (seg instanceof Collection) {
           final Collection colseg = (Collection)seg;
           s(colseg.toArray(new Object[colseg.size()]));
         }
         else pathBuilder.append(seg);
       }
     }
     return wrapper;
   }
   public Scriptable pp(Object... pps) {
     if (withinQuery)
       throw new IllegalStateException("Cannot add path params after query params");
       for (int i = 0; i < pps.length;) {
         pathBuilder.append(';');
         final Object pv = pps[i++], p, v;
         if (pv instanceof List) {
           final Iterator it = ((List)pv).iterator();
           p = it.next(); v = it.next();
         } else { p = pv; v = pps[i++]; }
         pathBuilder.append(p.toString()).append('=').append(v.toString());
     }
     return wrapper;
   }
   public Scriptable q(Object... qps) {
     for (int i = 0; i < qps.length;) {
       qparamsBuilder.append(withinQuery? '&' : '?');
       withinQuery = true;
       final Object pv = qps[i++], p, v;
       if (pv instanceof List) {
         final Iterator it = ((List)pv).iterator();
         p = it.next(); v = it.next();
       } else { p = pv; v = qps[i++]; }
       qparamsBuilder.append(encode(p.toString())).append('=').append(encode(v.toString()));
     }
     return wrapper;
   }
   @Override public String toString() {
     try {
       return new URI(scheme, authority, pathBuilder.toString(), qparamsBuilder.toString(), null)
         .toASCIIString();
     } catch (URISyntaxException e) { return sneakyThrow(e); }
   }
 
   static String encode(Object s) {
     try { return URLEncoder.encode(s.toString(), "UTF-8"); }
     catch (UnsupportedEncodingException e) { return sneakyThrow(e); }
   }
 }
