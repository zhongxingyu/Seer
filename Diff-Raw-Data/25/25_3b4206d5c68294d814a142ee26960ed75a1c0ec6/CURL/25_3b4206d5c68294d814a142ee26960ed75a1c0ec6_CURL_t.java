 package net.bodz.bas.c.java.net;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.TreeMap;
 
 import net.bodz.bas.c.java.util.Arrays;
 import net.bodz.bas.c.string.StringArray;
 import net.bodz.bas.err.UnexpectedException;
 import net.bodz.bas.fn.legacy.Filt1;
 
 /**
  * <pre>
  * CURL: 
  *   [TYPE://] {ALPHA,'#'} ['?' {PARAM,'&amp;'}]
  * 
  * ALPHA: 
  *   [{INIT-PARAM,':'} '@'] {BETA,'/'}
  * 
  * PARAM:
  *   NAME '=' VALUE
  * </pre>
  */
 public class CURL {
 
     private String type;
     private Alpha[] alphas;
     private Map<String, String> parameters;
 
     public CURL(String type, Alpha... alphas) {
         this(type, alphas, null);
     }
 
     public CURL(String type, Alpha[] alphas, Map<String, String> parameters) {
         if (alphas == null)
             throw new NullPointerException("alphas");
         if (alphas.length == 0)
             throw new NullPointerException("alphas is empty");
         this.type = type;
         this.alphas = alphas;
         this.parameters = parameters;
     }
 
     public CURL(String s) {
         if (s == null)
             throw new NullPointerException("s");
         int p = s.indexOf("://");
         if (p != -1) {
             type = s.substring(0, p);
             s = s.substring(p + 3);
         }
 
         p = s.indexOf('?');
         if (p != -1) {
             parameters = parseParameters(s.substring(p + 1));
             s = s.substring(0, p);
         }
 
         alphas = parseAlphas(s);
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Alpha[] getAlphas() {
         assert alphas != null;
         return alphas;
     }
 
     public void setAlphas(Alpha[] alphas) {
         this.alphas = alphas;
     }
 
     public void setAlphas(String[] alphas) {
         if (alphas == null)
             throw new NullPointerException("alphas");
         Alpha[] parsed = new Alpha[alphas.length];
         for (int i = 0; i < alphas.length; i++)
             parsed[i] = Alpha.parse(alphas[i]);
         this.alphas = parsed;
     }
 
     public String formatAlphas() {
         assert alphas != null;
         // already encoded in Alpha.toString.
         String s = StringArray.join("#", alphas);
         return s;
     }
 
     /**
      * at least an empty alpha will be added, if alphas="".
      */
     public static Alpha[] parseAlphas(String s) {
         if (s == null)
             throw new NullPointerException();
         List<Alpha> alphas = new ArrayList<Alpha>();
         int p;
         while (true) {
             p = s.indexOf('#');
             if (p == -1)
                 p = s.length();
             Alpha alpha = Alpha.parse(s.substring(0, p));
             alphas.add(alpha);
             if (p == s.length())
                 break;
             s = s.substring(p + 1);
         }
         return alphas.toArray(new Alpha[0]);
     }
 
     public Map<String, String> getParameters() {
         return parameters;
     }
 
     public void setParameters(Map<String, String> parameters) {
         this.parameters = parameters;
     }
 
     public String getParameter(String name) {
         if (parameters == null)
             return null;
         return parameters.get(name);
     }
 
     public void setParameter(String name, String value) {
         if (parameters == null)
             parameters = new TreeMap<String, String>();
         parameters.put(name, value);
     }
 
     public String formatParameters() {
         StringBuffer buf = null;
         for (Entry<String, String> e : parameters.entrySet()) {
             if (buf == null)
                 buf = new StringBuffer(parameters.size() * 30);
             else
                 buf.append("&");
             String name = e.getKey();
             String value = e.getValue();
             buf.append(encode(name));
             if (value != null) {
                 buf.append("=");
                 buf.append(encode(value));
             }
         }
        if (buf == null)
            return "";
        else
            return buf.toString();
     }
 
     public static Map<String, String> parseParameters(String s) {
         if (s == null)
             throw new NullPointerException("s");
         Map<String, String> parameters = new TreeMap<String, String>();
         int p;
         while (true) {
             p = s.indexOf('&');
             if (p == -1)
                 p = s.length();
             String name, value;
             int eq = s.indexOf('=');
             if (eq < p && eq != -1) {
                 name = s.substring(0, eq);
                 value = s.substring(eq + 1, p);
             } else {
                 name = s.substring(0, p);
                 value = null;
             }
             name = decode(name);
             value = decode(value);
             parameters.put(name, value);
             if (p == s.length())
                 break;
             s = s.substring(p + 1);
         }
         return parameters;
     }
 
     public String format() {
         int cap = 16 + alphas.length * 80;
         if (parameters != null)
             cap += parameters.size() * 30;
         StringBuffer buf = new StringBuffer(cap);
         if (type != null) {
             buf.append(type);
             buf.append("://");
         }
         for (int i = 0; i < alphas.length; i++) {
             if (i != 0)
                 buf.append('#');
             buf.append(alphas[i]);
         }
         String parameters = formatParameters();
         if (parameters != null) {
             buf.append('?');
             buf.append(parameters);
         }
         return buf.toString();
     }
 
     public static CURL parse(String s) {
         return new CURL(s);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof CURL))
             return false;
         CURL c = (CURL) obj;
         if (!Objects.equals(type, c.type))
             return false;
         if (!Arrays.equals(alphas, c.alphas))
             return false;
         if (!Objects.equals(parameters, c.parameters))
             return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         int h = 0;
         if (type != null)
             h += type.hashCode();
         h += Arrays.hashCode(alphas);
         if (parameters != null)
             h += parameters.hashCode();
         return h;
     }
 
     @Override
     public String toString() {
         return format();
     }
 
     public static class Alpha {
 
         private String[] initParameters;
         private String[] betas;
 
         public Alpha(String[] betas) {
             this(null, betas);
         }
 
         public Alpha(String[] initParameters, String[] betas) {
             if (betas == null)
                 throw new NullPointerException("betas");
             this.initParameters = initParameters;
             this.betas = betas;
         }
 
         public Alpha(String betas) {
             this(null, betas);
         }
 
         public Alpha(String initParameters, String betas) {
             if (betas == null)
                 throw new NullPointerException("betas");
             if (initParameters == null)
                 this.initParameters = null;
             else
                 this.initParameters = parseInitParameters(initParameters);
             this.betas = parseBetas(betas);
         }
 
         public String[] getInitParameters() {
             return initParameters;
         }
 
         public void setInitParameters(String[] initParameters) {
             this.initParameters = initParameters;
         }
 
         public String[] getBetas() {
             return betas;
         }
 
         public void setBetas(String[] betas) {
             this.betas = betas;
         }
 
         public String formatInitParameters() {
             if (initParameters == null || initParameters.length == 0)
                 return null;
             String[] encoded = Arrays.map(initParameters, ENCODER);
             return StringArray.join(":", encoded);
         }
 
         public static String[] parseInitParameters(String s) {
             if (s == null)
                 throw new NullPointerException("s");
             String[] v = s.split(":");
             v = Arrays.map(v, DECODER);
             return v;
         }
 
         public String formatBetas() {
             assert betas != null;
             String[] encoded = Arrays.map(betas, ENCODER);
             return StringArray.join("/", encoded);
         }
 
         public static String[] parseBetas(String s) {
             if (s == null)
                 throw new NullPointerException("s");
             String[] v = s.split("/");
             return Arrays.map(v, DECODER);
         }
 
         public String format() {
             if (initParameters == null)
                 return formatBetas();
             else
                 return formatInitParameters() + "@" + formatBetas();
         }
 
         public static Alpha parse(String s) {
             if (s == null)
                 throw new NullPointerException("s");
             String initParameters = null;
             int p = s.indexOf('@');
             if (p != -1) {
                 initParameters = s.substring(0, p);
                 s = s.substring(p + 1);
             }
             return new Alpha(initParameters, s);
         }
 
         @Override
         public boolean equals(Object obj) {
             if (!(obj instanceof Alpha))
                 return false;
             Alpha a = (Alpha) obj;
             if (!Arrays.equals(initParameters, a.initParameters))
                 return false;
             if (!Arrays.equals(betas, a.betas))
                 return false;
             return true;
         }
 
         @Override
         public int hashCode() {
             int h = 0;
             if (initParameters != null)
                 h += Arrays.hashCode(initParameters);
             assert betas != null;
             h += Arrays.hashCode(betas);
             return h;
         }
 
         @Override
         public String toString() {
             return format();
         }
 
     }
 
     public static String encode(String s) {
         try {
             s = URLEncoder.encode(s, "utf-8");
             s = s.replace("%3A", ":");
             s = s.replace("%5C", "/");
         } catch (UnsupportedEncodingException e) {
             throw new UnexpectedException(e);
         }
         return s;
     }
 
     public static String decode(String s) {
         try {
             s = URLDecoder.decode(s, "utf-8");
         } catch (UnsupportedEncodingException e) {
             throw new UnexpectedException(e);
         }
         return s;
     }
 
     static final Filt1<String, String> ENCODER;
     static final Filt1<String, String> DECODER;
     static {
         ENCODER = new Filt1<String, String>() {
             @Override
             public String filter(String s) {
                 return encode(s);
             }
         };
         DECODER = new Filt1<String, String>() {
             @Override
             public String filter(String s) {
                 return decode(s);
             }
         };
     }
 
 }
