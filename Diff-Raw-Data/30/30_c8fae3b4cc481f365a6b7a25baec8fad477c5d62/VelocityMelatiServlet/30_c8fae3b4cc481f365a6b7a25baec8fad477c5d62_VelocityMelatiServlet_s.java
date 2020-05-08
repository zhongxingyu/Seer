 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense. 
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
package org.melati.template.webmacro;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 
 import org.melati.Melati;
 import org.melati.servlet.TemplateServlet;
 import org.melati.template.TemplateContext;
 
import org.webmacro.servlet.WebContext;
import org.webmacro.WebMacroException;

 /**
 * Base class to use Melati with WebMacro.
  * Simply extend this class, override the handle method
  * 
  * @author Tim Joyce
  * $Revision$
  */
 public abstract class VelocityMelatiServlet extends TemplateServlet
 {
 
  protected String doTemplateRequest(Melati melati, TemplateContext templateContext) 
    throws Exception {
     WebContext webContext = (WebContext)templateContext.getContext();
    return handle(melati, webContext);
   }
   
   /*
   * adding the extension is left up to the application developer
   */
   protected String addExtension(String templateName) {
     return templateName;
   }
 
  protected abstract String handle(Melati melati, WebContext webContext) 
    throws Exception ;
   
 }
 
 
 
 
 
 
 
 
