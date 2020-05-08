 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave.internal;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * A {@code URIPattern} is the object that indicates which handler should be
  * invoked according to the requested URI. A {@code URIPattern} is similar in
  * nature to the string part of the URI except for having wildcards and
  * replacement variables.
  * 
  * Replacement variables look like {@code ${var}} and are supplied to handler
  * methods as arguments to the method if annotated. From this point of view,
  * though, consider replacement variables as a single wildcard match.
  * 
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class URIPatternImpl implements URIPattern {
     
     public static final String FORWARD_SLASH = "/";
 
     private String pattern;
     private String[] parts;
 
     public URIPatternImpl(String pattern) {
         if (!pattern.startsWith("/")) {
             throw new IllegalArgumentException(
                 "The supplied pattern must begin with a forward slash (\"/\") "
                     + "where the root is relative to the context path.");
         }
         if (pattern.contains("**/${")) {
             throw new IllegalArgumentException(
                 "The supplied pattern is nondeterministic.  There is no way of "
                     + "knowing when to stop matching with this type of pattern: /**/${var}/");
         }
         this.pattern = normalizePattern(pattern);
         if (pattern.length() > 1) {
             this.parts = pattern.substring(1).split("/");
         }
     }
 
     public String getPattern() {
         return pattern;
     }
 
     protected String normalizePattern(String pattern) {
         URI uri;
         try {
             // replace illegal characters in the pattern so that URI can do the
             // work of normalizing the uri - hopefully nobody chooses these
             // strings in the url :-P
             pattern = pattern.replace("${", "~~agave~~start~~delim~~");
             pattern = pattern.replace("}", "~~agave~~end~~delim~~");
             uri = new URI(pattern);
         } catch (URISyntaxException ex) {
             throw new IllegalArgumentException("Malformed URI pattern: " + pattern, ex);
         }
         String normalizedUri = stripTrailingSlash(uri.normalize().toString());
         normalizedUri = normalizedUri.replace("~~agave~~start~~delim~~", "${");
         normalizedUri = normalizedUri.replace("~~agave~~end~~delim~~", "}");
         normalizedUri = condenseWildcards(normalizedUri);
         return normalizedUri;
     }
 
     /**
      * Condenses multiple successive wildcards into the most generic wildcard
      */
     private String condenseWildcards(String pattern) {
         while (pattern.contains("**/**") || pattern.contains("**/*")
             || pattern.contains("*/**")) {
             pattern = pattern.replace("**/**", "**");
             pattern = pattern.replace("**/*", "**");
             pattern = pattern.replace("*/**", "**");
         }
         return pattern;
     }
 
     /**
      * Normalizes the URI string so that .. and . are properly handled and
      * condensed.
      * 
      * @param uriStr
      *            the URI string to normalize
      * @return the normalized URI string
      */
     public String normalizeURI(String uriStr) {
         URI uri;
         try {
             uri = new URI(uriStr);
         } catch (URISyntaxException ex) {
             throw new IllegalArgumentException("Malformed URI: " + uriStr, ex);
         }
         return stripTrailingSlash(uri.normalize().toString());
     }
 
     private String stripTrailingSlash(String input) {
         if (!input.equals(FORWARD_SLASH) && input.endsWith(FORWARD_SLASH)) {
             return input.substring(0, input.length() - 1);
         }
         return input;
     }
 
     /**
      * Determines whether the supplied URI string matches the pattern that this
      * {@code URIPattern} encapsulates. The URI string supplied as an argument
      * must start with a forward slash ('/'). The URI string is normalized with
      * URI.normalize() from the Java API, then compared against the stored
      * pattern where wildcards and replacement variables help determine the
      * match. Replacement variables look like <code>${someVar}</code> and are
      * taken as an automatic match. A single asterisk represents a wildcard
      * match where the supplied token matches automatically as well. A double
      * asterisk matches multiple tokens until the next token in the pattern is
      * matched against the URI.
      * @param uri the uri string
      * @return true if the uri matches this pattern
      */
     public boolean matches(String uri) {
         if (!uri.startsWith(FORWARD_SLASH)) {
             throw new IllegalArgumentException("URI must begin with a forward slash ('/')");
         }
 
         // check for root (so as to not match all patterns starting with a '/')
        if ("/".equals(uri)) {
            if ("/".equals(pattern)) {
                 return true;
             }
             return false;
         }
         
         String[] patternTokens = pattern.split(FORWARD_SLASH);
         String[] uriTokens = normalizeURI(uri).split(FORWARD_SLASH);
         
         int pi = 0, ui = 0;
         for (; pi < patternTokens.length && ui < uriTokens.length; pi++, ui++) {
             if ("**".equals(patternTokens[pi])) {
                 ++pi;
                 if (pi >= patternTokens.length) {
                     // matches the rest of the uri
                     return true;
                 } else {
                     // slurp the uri tokens until they match the next position
                     // in the pattern
                     while (ui < uriTokens.length) {
                         if (uriTokens[ui].equalsIgnoreCase(patternTokens[pi])) {
                             break;
                         }
                         ++ui;
                     }
                     if (ui >= uriTokens.length) {
                         return false;
                     }
                 }
             } else if (!uriTokens[ui].equalsIgnoreCase(patternTokens[pi])
                 && !"*".equals(patternTokens[pi])
                 && !Pattern.compile("\\$\\{.*\\}").matcher(patternTokens[pi]).matches()) {
                 return false;
             }
         }
 
         if ((pi >= patternTokens.length && ui < uriTokens.length)
             || (ui < uriTokens.length && !"**".equals(patternTokens[pi]))) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (!(obj instanceof URIPattern))
             return false;
         URIPattern that = (URIPattern) obj;
         return pattern.equalsIgnoreCase(that.getPattern());
     }
 
     @Override
     public int hashCode() {
         return pattern.hashCode();
     }
 
     /**
      * Compares two {@code URIPattern}s for greater specificity. A more
      * specific {@code URIPattern} should always be sorted before a more generic
      * one.
      * 
      * @param that
      *            the {@code URIPattern} to compare against
      * @return -1 if this {@code URIPattern} is more specific, 0 if they are
      *         equal in specificity and 1 if that {@code URIPattern} is more
      *         specific
      */
     public int compareTo(URIPattern that) {
         if (this.equals(that)) {
             return 0;
         }
 
         Integer value = null;
 
         String[] thisTokens = pattern.split(FORWARD_SLASH);
         String[] thatTokens = that.getPattern().split(FORWARD_SLASH);
 
         for (int i = 0; i < thisTokens.length && i < thatTokens.length; i++) {
             if ("**".equals(thisTokens[i]) && !"**".equals(thatTokens[i])) {
                 value = 1;
                 break;
             } else if (!"**".equals(thisTokens[i]) && "**".equals(thatTokens[i])) {
                 value = -1;
                 break;
             } else if ("*".equals(thisTokens[i]) && !"*".equals(thatTokens[i])) {
                 value = 1;
                 break;
             } else if (!"*".equals(thisTokens[i]) && "*".equals(thatTokens[i])) {
                 value = -1;
                 break;
             }
         }
 
         if (value == null) {
             if (thisTokens.length > thatTokens.length) {
                 value = -1;
             } else if (thisTokens.length < thatTokens.length) {
                 value = 1;
             } else {
                 int length = (thisTokens.length > thatTokens.length) ? thisTokens.length : thatTokens.length;
                 for (int i = 0; i < length; i++) {
                     value = thisTokens[i].compareToIgnoreCase(thatTokens[i]);
                     if (value != 0) {
                         break;
                     }
                 }
             }
         }
 
         return value;
     }
 
     public Map<String, String> getParameterMap(HttpServletRequest request) {
         String uri = request.getRequestURI();
         if (request.getContextPath() != null) {
             uri = uri.substring(request.getContextPath().length());
         }
         Map<String, String> parameterMap = new HashMap<String, String>();
         if (parts != null && uri != null && uri.length() > 1) {
             String[] requestedParts = uri.substring(1).split("/");
             if (requestedParts.length >= parts.length) {
                 Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");
                 for (int i = 0; i < parts.length; i++) {
                     Matcher matcher = pattern.matcher(parts[i]);
                     if (matcher.matches() && matcher.groupCount() > 0) {
                         String paramName = matcher.group(1);
                         String paramValue = requestedParts[i]; 
                         parameterMap.put(paramName, paramValue);
                     }
                 }
             }
         }
         return parameterMap;           
     }
 
     @Override
     public String toString() {
     	return getPattern();
     }
     
 }
