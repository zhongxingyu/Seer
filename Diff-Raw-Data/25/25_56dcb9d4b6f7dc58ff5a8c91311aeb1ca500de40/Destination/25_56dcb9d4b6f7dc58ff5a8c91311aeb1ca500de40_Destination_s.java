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
 package agave;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.servlet.ServletContext;
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class Destination {
     
     public static final String ENCODED_AMPERSAND = "&amp;";
 
     private String path;
     private Map<String, List<String>> parameters = new TreeMap<String, List<String>>();
     private Boolean redirect;
     
     /**
      * Create a new {@code Destination} within the deployed context.  The destination will be 
      * redirected to if the HTTP request method was a POST, otherwise it will be forwarded to.
      * @param path
      */
     public Destination(String path) {
         setPath(path);
     }
     
     public Destination(String path, boolean redirect) {
         setPath(path);
         setRedirect(Boolean.valueOf(redirect));
     }
     
     /**
      * Adds a parameter to the {@code Destination}.  Multi-valued parameters are supported
      * and will be serialized into a query string with the same parameter name and multiple
      * values, eg: {@code /somePath?dogs=woof&dogs=bark&cats=meow&cats=purr}
      * @param name the name of the parameter
      * @param value the value of the parameter
      */
     public void addParameter(String name, String value) {
         if (!parameters.containsKey(name)) {
             parameters.put(name, new ArrayList<String>());
         }
         parameters.get(name).add(value);
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         if (!path.startsWith("/")) {
             throw new IllegalArgumentException("Destination paths should start with a forward slash ('/')"
                     + " signifying the root of the context");
         }
         this.path = path;
     }
 
     public Map<String, List<String>> getParameters() {
         return parameters;
     }
 
     public void setParameters(Map<String, List<String>> parameters) {
         this.parameters = parameters;
     }
 
     public Boolean getRedirect() {
         return redirect;
     }
 
     public void setRedirect(Boolean redirect) {
         this.redirect = redirect;
     }
     
     /**
      * Encodes the {@code Destination} path and available parameters. The parameters have any ampersands
      * replaced with &{@code &amp;amp;} and parameter names and their associated values are sorted (mainly for
      * testing purposes).
      * 
      * @param context
      * @return
      */
     public String encode(ServletContext context) {
        StringBuilder path = new StringBuilder(context.getContextPath());
         if (getPath() != null) {
             path.append(getPath());
         }
         if (!getParameters().isEmpty()) {
             path.append("?");
             boolean first = true;
             for (String parameterName : getParameters().keySet()) {
                 Collections.sort(getParameters().get(parameterName));
                 for (String parameterValue : getParameters().get(parameterName)) {
                     if (!first) {
                         path.append("&");
                     }
                     if (parameterValue.contains("&")) {
                         parameterValue = parameterValue.replace("&", ENCODED_AMPERSAND);
                     }
                     path.append(parameterName).append("=").append(parameterValue);
                     first = false;
                 }
             }
         }
         return path.toString();
     }
     
 }
