 /**
  * Copyright 2009 by Dmitry Mikhaylov.
  * 
  * This file is part of web-utils.
  * 
  * web-utils is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * web-utils is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with web-utils.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mikha.utils.web;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.mikha.utils.net.UrlAndParams;
 import org.mikha.utils.web.multipart.MultipartRequestWrapper;
 
 /**
  * Enhances standard {@link HttpServletRequest} with additional methods to
  * simplify parsing HTTP parameters.
  * @author mikha
  */
 public class HttpParamsRequest extends HttpServletRequestWrapper
 {
 
     /** Helper interface to parse HTTP parameters */
     private static interface ParamParser<T>
     {
 
         T parse(HttpParamsRequest req, String name, String value, boolean mandatory, T defValue);
 
         T[] createArray(int length);
     }
 
     /**
      * Constant for string match - email address. Sets that this parameter must
      * be valid email address. Taken from
      * http://www.regular-expressions.info/email.html
      */
     public static final Pattern PATTERN_EMAIL = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}",
         Pattern.CASE_INSENSITIVE);
 
     /** request attribute key to store map of parameter errors */
     public static final String ATTR_ERRORS = "_errors";
 
     /** request attribute to store message text */
     public static final String ATTR_MESSAGE_TEXT = "_message";
 
     /** request attribute to store message parameters */
     public static final String ATTR_MESSAGE_PARAMS = "_message_params";
 
     /** request attribute to store message action names */
     public static final String ATTR_MESSAGE_ACTIONS = "_message_actions";
 
     /** helper to parse string parameters */
     private static final ParamParser<String> STRING_PARSER = new ParamParser<String>()
     {
         public String parse(HttpParamsRequest req, String name, String value, boolean mandatory, String defValue)
         {
             if (value == null)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
            value = value.trim();
            if (value.length() == 0)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
             return value;
         }
 
         public String[] createArray(int length)
         {
             return new String[length];
         }
     };
 
     /** helper to parse integer parameters */
     private static final ParamParser<Integer> INTEGER_PARSER = new ParamParser<Integer>()
     {
         public Integer parse(HttpParamsRequest req, String name, String value, boolean mandatory, Integer defValue)
         {
             if (value == null)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
             value = value.trim();
             if (value.length() == 0)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
             try
             {
                 return Integer.parseInt(value);
             }
             catch (NumberFormatException nfex)
             {
                 req.logParameterError(name);
                 return null;
             }
         }
 
         public Integer[] createArray(int length)
         {
             return new Integer[length];
         }
     };
 
     /** helper to parse floating-point parameters */
     private static final ParamParser<Double> DOUBLE_PARSER = new ParamParser<Double>()
     {
         public Double parse(HttpParamsRequest req, String name, String value, boolean mandatory, Double defValue)
         {
             if (value == null)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
             value = value.trim();
             if (value.length() == 0)
             {
                 if (mandatory)
                 {
                     req.logParameterError(name);
                 }
                 return defValue;
             }
             try
             {
                 return Double.parseDouble(value);
             }
             catch (NumberFormatException nfex)
             {
                 req.logParameterError(name);
                 return null;
             }
         }
 
         public Double[] createArray(int length)
         {
             return new Double[length];
         }
     };
 
     /** helper to parse boolean parameters */
     private static final ParamParser<Boolean> BOOLEAN_PARSER = new ParamParser<Boolean>()
     {
         public Boolean parse(HttpParamsRequest req, String name, String value, boolean mandatory, Boolean defValue)
         {
             return (value != null ? Boolean.parseBoolean(value) : Boolean.FALSE);
         }
 
         public Boolean[] createArray(int length)
         {
             return new Boolean[length];
         }
     };
 
     /**
      * Wraps given request into <code>EnhancedHttpRequest</code> (or simply
      * casts request <code>EnhancedHttpRequest</code>, if possible).
      * @param request original request
      * @return instance of <code>EnhancedHttpRequest</code> that wraps
      *         original request
      * @throws ServletException if failed to process uploaded file
      */
     @SuppressWarnings("unchecked")
     public static HttpParamsRequest wrap(ServletRequest request) throws ServletException
     {
         if (request instanceof HttpParamsRequest)
         {
             return (HttpParamsRequest) request;
         }
         HttpServletRequest req = (HttpServletRequest) request;
 
         Map<String, String> errors = (Map<String, String>) req.getAttribute(ATTR_ERRORS);
         if (errors == null)
         {
             errors = new HashMap<String, String>();
             req.setAttribute(ATTR_ERRORS, errors);
         }
 
         return new HttpParamsRequest((HttpServletRequest) request, errors);
     }
 
     private final Map<String, String> errors;
 
     private boolean hasRecentErrors = false;
 
     private HttpParamsRequest(HttpServletRequest request, Map<String, String> errors)
     {
         super(request);
         this.errors = errors;
     }
 
     /**
      * Returns map of file names to file items.
      * @return map of file names to file items or <code>null</code>, if the
      *         request didn't contain <code>multipart/form-data</code> data
      */
     public Map<String, FileItem> getFilesMap()
     {
         return MultipartRequestWrapper.getFilesMap(this);
     }
 
     /**
      * Returns file item for file with given name.
      * @param name file name
      * @return file item for file with given name or <code>null</code>, if no
      *         file with given name present
      */
     public FileItem getFile(String name)
     {
         return MultipartRequestWrapper.getFile(this, name);
     }
 
     /**
      * Parses mandatory string parameter. Logs error if parameter is missing.
      * @param name parameter name
      * @return value of parameter or <code>null</code>, if parameter is
      *         missing
      */
     public String getString(String name)
     {
         return STRING_PARSER.parse(this, name, getParameter(name), true, null);
     }
 
     /**
      * Parses optional string parameter.
      * @param name parameter name
      * @param defValue default value
      * @return value of parameter or <code>defValue</code>, if parameter is
      *         missing
      */
     public String getString(String name, String defValue)
     {
         return STRING_PARSER.parse(this, name, getParameter(name), false, defValue);
     }
 
     /**
      * Parses array of mandatory string parameters. Logs errors if parameters
      * are missing.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @return array, each entry is either parameter value, or <code>null</code>
      *         if corresponding parameter is missing
      */
     public String[] getStringArray(String name, int maxLength)
     {
         return parseArray(STRING_PARSER, name, maxLength, true, null);
     }
 
     /**
      * Parses array of optional string parameters.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @param defValue default value for parameters
      * @return array, each entry is either parameter value, or
      *         <code>defValue</code> if corresponding parameter is missing
      */
     public String[] getStringArray(String name, int maxLength, String defValue)
     {
         return parseArray(STRING_PARSER, name, maxLength, false, defValue);
     }
 
     /**
      * Parses mandatory e-mail parameter. Logs error if parameter is missing or
      * invalid.
      * @param name parameter name
      * @return value of parameter or <code>null</code>, if parameter is
      *         missing or invalid
      */
     public String getEmail(String name)
     {
         String r = STRING_PARSER.parse(this, name, getParameter(name), true, null);
         if (r != null)
         {
             Matcher m = PATTERN_EMAIL.matcher(r);
             if (!m.matches())
             {
                 logParameterError(name);
                 return null;
             }
         }
         return r;
     }
 
     /**
      * Parses optional e-mail parameter. Logs error, if parameter is invalid.
      * @param name parameter name
      * @param defValue default value
      * @return value of parameter or <code>defValue</code>, if parameter is
      *         missing or <code>null</code>, if parameter is invalid
      */
     public String getEmail(String name, String defValue)
     {
         String r = STRING_PARSER.parse(this, name, getParameter(name), false, defValue);
         if (r != null)
         {
             Matcher m = PATTERN_EMAIL.matcher(r);
             if (!m.matches())
             {
                 logParameterError(name);
                 return null;
             }
         }
         return r;
     }
 
     /**
      * Parses mandatory integer parameter. Logs error if parameter is missing or
      * invalid.
      * @param name parameter name
      * @return value of parameter or <code>null</code>, if parameter is
      *         missing or invalid
      */
     public Integer getInteger(String name)
     {
         return INTEGER_PARSER.parse(this, name, getParameter(name), true, null);
     }
 
     /**
      * Parses optional integer parameter. Logs error, if parameter is invalid.
      * @param name parameter name
      * @param defValue default value
      * @return value of parameter or <code>defValue</code>, if parameter is
      *         missing or <code>null</code>, if parameter is invalid
      */
     public Integer getInteger(String name, Integer defValue)
     {
         return INTEGER_PARSER.parse(this, name, getParameter(name), false, defValue);
     }
 
     /**
      * Parses array of mandatory integer parameters. Logs errors if parameters
      * are missing or invalid.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @return array, each entry is either parameter value, or <code>null</code>
      *         if corresponding parameter is missing or invalid
      */
     public Integer[] getIntegerArray(String name, int maxLength)
     {
         return parseArray(INTEGER_PARSER, name, maxLength, true, null);
     }
 
     /**
      * Parses array of optional integer parameters. Logs errors if parameters
      * are invalid.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @param defValue default value for parameters
      * @return array, each entry is either parameter value, or
      *         <code>defValue</code> if corresponding parameter is missing, or
      *         <code>null</code> if corresponding parameter is invalid
      */
     public Integer[] getIntegerArray(String name, int maxLength, Integer defValue)
     {
         return parseArray(INTEGER_PARSER, name, maxLength, false, defValue);
     }
 
     /**
      * Parses mandatory floating-point parameter. Logs error if parameter is
      * missing or invalid.
      * @param name parameter name
      * @return value of parameter or <code>null</code>, if parameter is
      *         missing or invalid
      */
     public Double getDouble(String name)
     {
         return DOUBLE_PARSER.parse(this, name, getParameter(name), true, null);
     }
 
     /**
      * Parses optional floating-point parameter. Logs error, if parameter is
      * invalid.
      * @param name parameter name
      * @param defValue default value
      * @return value of parameter or <code>defValue</code>, if parameter is
      *         missing or <code>null</code>, if parameter is invalid
      */
     public Double getDouble(String name, Double defValue)
     {
         return DOUBLE_PARSER.parse(this, name, getParameter(name), false, defValue);
     }
 
     /**
      * Parses array of mandatory floating-point parameters. Logs errors if
      * parameters are missing or invalid.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @return array, each entry is either parameter value, or <code>null</code>
      *         if corresponding parameter is missing or invalid
      */
     public Double[] getDoubleArray(String name, int maxLength)
     {
         return parseArray(DOUBLE_PARSER, name, maxLength, true, null);
     }
 
     /**
      * Parses array of optional floating-point parameters. Logs errors if
      * parameters are invalid.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @param defValue default value for parameters
      * @return array, each entry is either parameter value, or
      *         <code>defValue</code> if corresponding parameter is missing, or
      *         <code>null</code> if corresponding parameter is invalid
      */
     public Double[] getDoubleArray(String name, int maxLength, Double defValue)
     {
         return parseArray(DOUBLE_PARSER, name, maxLength, false, defValue);
     }
 
     /**
      * Parses boolean parameter. Boolean parameters are optional by definition,
      * default value is always {@link Boolean#FALSE}.
      * @param name parameter name
      * @return value of parameter or <code>Boolean.FALSE</code>, if parameter
      *         is missing or invalid
      */
     public boolean getBoolean(String name)
     {
         return BOOLEAN_PARSER.parse(this, name, getParameter(name), false, Boolean.FALSE);
     }
 
     /**
      * Parses array of boolean parameters. Boolean parameters are optional by
      * definition, default value is always {@link Boolean#FALSE}.
      * @param name base name for parameters
      * @param maxLength max allowed length of array
      * @return array, each entry is either parameter value, or
      *         <code>Boolean.FALSEValue</code> if corresponding parameter is
      *         missing orinvalid
      */
     public Boolean[] getBooleanArray(String name, int maxLength)
     {
         return parseArray(BOOLEAN_PARSER, name, maxLength, false, Boolean.FALSE);
     }
 
     /**
      * Parses parameter as single index in array of options. 
      * @param <O> option class
      * @param name parameter
      * @param options array of possible options
      * @return single option from array of possible options (indexed by parameter) 
      */
     public <O> O getOption(String name, O[] options)
     {
         O result = null;
         String[] values = (String[]) getParameterMap().get(name);
         if (values == null)
         {
             logParameterError(name);
             return null;
         }
         for (String v : values)
         {
             if (result != null) {
                 logParameterError(name);
                 return null;
             }
             int i;
             try
             {
                 i = Integer.parseInt(v.trim());
             }
             catch (NumberFormatException nfex)
             {
                 logParameterError(name);
                 return null;
             }
             if (i < 0 || i >= options.length)
             {
                 logParameterError(name);
                 return null;
             }
             result = options[i];
         }
         if (result == null) {
             logParameterError(name);
             return null;
         }
         return result;
     }
 
     /**
      * Parses parameter as arrays of indices in array of options. 
      * @param <O> option class
      * @param name parameter
      * @param options array of possible options
      * @return list containing options from array of possible options (indexed by parameters) 
      */
     public <O> List<O> getOptions(String name, O[] options)
     {
         List<O> result = new LinkedList<O>();
         String[] values = (String[]) getParameterMap().get(name);
         if (values == null)
         {
             return result;
         }
         for (String v : values)
         {
             int i;
             try
             {
                 i = Integer.parseInt(v.trim());
             }
             catch (NumberFormatException nfex)
             {
                 logParameterError(name);
                 return null;
             }
             if (i < 0 || i >= options.length)
             {
                 logParameterError(name);
                 return null;
             }
             result.add(options[i]);
         }
         return result;
     }
 
     /**
      * Logs default error related to given parameter.
      * @param name parameter name
      * @deprecated use {@link #logParameterError(String)} instead
      */
     @Deprecated
     public void logError(String name)
     {
         logParameterError(name);
     }
 
     /**
      * Logs custom error related to given parameter.
      * @param name parameter name
      * @param error custom error
      * @deprecated use {@link #logParameterError(String)} instead
      */
     @Deprecated
     public void logError(String name, String error)
     {
         logParameterError(name, error);
     }
 
     /**
      * Logs default error related to given parameter.
      * @param name parameter name
      */
     public void logParameterError(String name)
     {
         hasRecentErrors = true;
         errors.put(name, "error." + name);
     }
 
     /**
      * Logs custom error related to given parameter.
      * @param name parameter name
      * @param error custom error
      */
     public void logParameterError(String name, String error)
     {
         hasRecentErrors = true;
         errors.put(name, error);
     }
 
     /**
      * Returns whether this request had errors since last call to
      * {@link #clearRecentErrors()}.
      * @return <code>true</code> if this request has recent errors;
      *         <code>false</code> otherwise
      * @see #clearRecentErrors()
      */
     public boolean hasRecentErrors()
     {
         return hasRecentErrors;
     }
 
     /**
      * Clears "recent errors" flag.
      * @see #hasRecentErrors()
      */
     public void clearRecentErrors()
     {
         hasRecentErrors = false;
     }
 
     /**
      * Check whether there are parsing and validation errors.
      * @return <code>true</code> if there are parsing and validation errors;
      *         <code>false</code> otherwise
      */
     public boolean hasErrors()
     {
         return (errors.size() > 0);
     }
 
     /**
      * Returns a error (if present) for a parameter with given name.
      * @param param parameter name
      * @return error for a parameter with given name or <code>null</code>
      * @deprecated Use {@link #getParameterError(String)} instead
      */
     @Deprecated
     public String getError(String param)
     {
         return getParameterError(param);
     }
 
     /**
      * Returns a error (if present) for a parameter with given name.
      * @param param parameter name
      * @return error for a parameter with given name or <code>null</code>
      */
     public String getParameterError(String param)
     {
         return errors.get(param);
     }
 
     /**
      * Sets message text and parameters. These can be used via
      * <code>&lt;msg&gt;</code> tag.
      * @param msg message text, can contain positional parameter placeholders
      * @param params positional parameters
      * @see MessageFormat
      */
     public void setMessage(String msg, Object... params)
     {
         setAttribute(ATTR_MESSAGE_TEXT, msg);
         setAttribute(ATTR_MESSAGE_PARAMS, params);
     }
 
     /**
      * Redirects given response to given URL.
      * @param rsp response
      * @param url URL to redirect to. If starts with '/', is considered to be
      *        context-relative.
      * @throws ServletException if failed to write redirect
      */
     public void redirect(HttpServletResponse rsp, String url) throws ServletException
     {
         if (url.charAt(0) == '/')
         {
             url = getContextPath() + url;
         }
         url = rsp.encodeRedirectURL(url);
         try
         {
             rsp.sendRedirect(url);
         }
         catch (IOException ex)
         {
             throw new ServletException(String.format("Failed to redirect to \"%s\"", url), ex);
         }
     }
 
     /**
      * Redirects given response to given URL with parameters.
      * @param rsp response
      * @param url URL to redirect to
      * @throws ServletException if failed to write redirect
      */
     public void redirect(HttpServletResponse rsp, UrlAndParams url) throws ServletException
     {
         redirect(rsp, url.toString());
     }
 
     @SuppressWarnings("unchecked")
     private <T> T[] parseArray(ParamParser<T> parser, String name, int maxLength, boolean mandatory, T defValue)
     {
         boolean[] pr = new boolean[maxLength];
         Object[] arr = new Object[maxLength];
         int l = name.length();
         int length = 0;
         for (Object o : getParameterMap().entrySet())
         {
             Map.Entry<String, String[]> e = (Entry<String, String[]>) o;
             String n = e.getKey();
             if (!n.startsWith(name))
             {
                 continue;
             }
             if (n.length() < l + 2)
             {
                 continue;
             }
             if (n.charAt(l) != '-')
             {
                 continue;
             }
             int idx = 0;
             try
             {
                 idx = Integer.parseInt(n.substring(l + 1));
             }
             catch (NumberFormatException nfex)
             {
                 continue;
             }
             if (idx >= maxLength)
             {
                 continue;
             }
             if (idx >= length)
             {
                 length = idx + 1;
             }
             String[] v = e.getValue();
             if (v.length != 1)
             {
                 continue;
             }
             arr[idx] = parser.parse(this, n, v[0], mandatory, defValue);
             pr[idx] = true;
         }
         for (int idx = 0; idx < length; idx++)
         {
             if (!pr[idx])
             {
                 String n = name + '-' + idx;
                 arr[idx] = parser.parse(this, n, null, mandatory, defValue);
             }
         }
         T[] r = parser.createArray(length);
         System.arraycopy(arr, 0, r, 0, length);
         return r;
     }
 }
