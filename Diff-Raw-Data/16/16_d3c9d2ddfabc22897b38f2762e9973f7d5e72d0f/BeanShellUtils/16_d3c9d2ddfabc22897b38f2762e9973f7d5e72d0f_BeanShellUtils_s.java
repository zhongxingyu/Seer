 /*
  * BeanShell Web
  * Copyright (C) 2012 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 package ste.web.beanshell;
 
 import bsh.EvalError;
 import bsh.Interpreter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import static ste.web.beanshell.Constants.*;
 
 /**
  *
  * @author ste
  */
 public abstract class BeanShellUtils {
     
     // --------------------------------------------------------------- Constants
     
     public static final String DEFAULT_CONTROLLERS_PREFIX = "/";
     public static final String DEFAULT_VIEWS_PREFIX = "/";    
     public static final String LOG_NAME = "ste.web";
     public static final String PARAM_CONTROLLERS = "controllers-prefix";
     public static final String PARAM_VIEWS = "views-prefix";
     
     public static final Logger log = Logger.getLogger(LOG_NAME);
     
     // ---------------------------------------------------------- Public methods
     
     /**
      * Reads the given script enclosing it into a try-catch block.
      * 
     * @param script the file to read
      * 
      * @return the script content
      * 
      * @throws IOException in case of IO errors
      */
     public static String getScript(File script) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         FileInputStream is = null;
         try {
             is = new FileInputStream(script);
            if (is == null) {
                throw new FileNotFoundException(script.getAbsolutePath());
            }
             byte[] buf = new byte[1024];
             int n = 0;
             while ((n = is.read(buf)) >= 0) {
                 baos.write(buf, 0, n);
             }
             return "try { " + baos.toString() + "} catch (Throwable t) { t.printStackTrace(); throw t; }";
         } finally {
             if (is != null) {
                 is.close();
             }
            if (baos != null) {
                baos.close();
            }
         }
     }
     
     public static void setup(final Interpreter         interpreter,
                              final HttpServletRequest  request    , 
                              final HttpServletResponse response   ) 
     throws EvalError, IOException {    
         //
         // Set attributes as script variables
         //
         String key;
         for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
             key = ((String) e.nextElement()).replaceAll("\\.", "_");
             interpreter.set(key, request.getAttribute(key));
         }
         
         //
         // Set request parameters as script variables. Note that parameters
         // override attributes
         //
         for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
             key = ((String) e.nextElement()).replaceAll("\\.", "_");
             interpreter.set(key, request.getParameter(key));
         }
         
         interpreter.set(VAR_REQUEST,  request                  );
         interpreter.set(VAR_RESPONSE, response                 );
         interpreter.set(VAR_SESSION,  request.getSession(false));
         interpreter.set(VAR_OUT,      response.getWriter()     );
         interpreter.set(VAR_LOG,      log                      );
     }
 }
