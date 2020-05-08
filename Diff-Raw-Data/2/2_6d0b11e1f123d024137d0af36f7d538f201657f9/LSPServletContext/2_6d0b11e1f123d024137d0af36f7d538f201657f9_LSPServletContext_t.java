 /*
  * Copyright (c) 2004-2005, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp.servlet;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import nu.staldal.lsp.*;
 
 
 /**
  * Context for LSP extension libraries.
  */
 public class LSPServletContext
 {
 	private final ServletContext servletContext;
 	private final HttpServletRequest servletRequest;
 	private final HttpServletResponse servletResponse;
     private final LSPManager lspManager;
 	
 
     public LSPServletContext(ServletContext servletContext,
         HttpServletRequest servletRequest, 
         HttpServletResponse servletResponse,
         LSPManager lspManager)
     {
         this.servletContext = servletContext;
         this.servletRequest = servletRequest;
         this.servletResponse = servletResponse;
         this.lspManager = lspManager;
     }
     
 
     /**
      * Get the {@link javax.servlet.ServletContext}.
      *
      * @return the {@link javax.servlet.ServletContext}
      */
     public ServletContext getServletContext()
     {
         return servletContext;
     }
     
 
     /**
      * Get the {@link javax.servlet.http.HttpServletRequest}.
      *
      * @return the {@link javax.servlet.http.HttpServletRequest}
      */
     public HttpServletRequest getServletRequest()
     {
         return servletRequest;
     }
     
 
     /**
      * Get the {@link javax.servlet.http.HttpServletResponse}.
      *
      * @return the {@link javax.servlet.http.HttpServletResponse}
      */
     public HttpServletResponse getServletResponse()
     {
         return servletResponse;
     }
 
 
     /**
      * Get the {@link nu.staldal.lsp.servlet.LSPManager}.
      *
      * @return the {@link nu.staldal.lsp.servlet.LSPManager}
      */
     public LSPManager getLSPManager()
     {
         return lspManager;
     }
 
 
     /**
      * Get a localized resource for the user's locale.
      *<p>
      * This method is used by the LSP ExtLib <code>lang</code> 
      * element and function.
      *
      * @param pageName LSP page name, 
      *                 or <code>null</code> for global resources only
      * @param key      the key
      *
      * @return [<var>key</var>] if not found.
      */
     public String lang(String pageName, String key)
         throws Exception
     {
         if (key == null || key.length() == 0) return "";
         
         String x = getLSPManager().getLocalizedString(
             getServletRequest(), pageName, key);
         if (x == null)
             return '[' + key + ']';
         else
             return x;			
     }
 
 
     /**
      * Get a localized resource for the user's locale.
     *<p>
     * Same as <code>lang(null,<var>key</var>)</code>.
      *
      * @param key      the key
      *
      * @return [<var>key</var>] if not found.
      */
     public String lang(String key)
         throws Exception
     {
         return lang(null, key);
     }
     
     
     /**
      * Encode an URL for Servlet session tracking.
      * 
      * @see javax.servlet.http.HttpServletResponse#encodeURL
      */
     public String encodeURL(String url)
     {
         return getServletResponse().encodeURL(url);
     }
     
 }
 
