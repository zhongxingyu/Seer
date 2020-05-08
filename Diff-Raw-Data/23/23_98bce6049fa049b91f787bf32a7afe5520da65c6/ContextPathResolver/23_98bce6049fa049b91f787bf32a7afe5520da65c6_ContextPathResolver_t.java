 /*
  * $Id$
  * --------------------------------------------------------------------------------------
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
  */
 
 package org.mule.galaxy.web.server;
 
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.context.WebApplicationContext;
 
 /**
  * Correctly resolves root context path both for WAR deployments in root/non-root contexts,
  * as well as for test runs.
  */
 public class ContextPathResolver
 {
    /**
     * Returns:
     * <ul>
     *  <li>"" (empty) string when no servlet container is available (e.g. a test run)
     *  <li>"" (empty) string when deployed to a servlet container root context
     *  <li>context path name otherwise
     * </ul>
     * @return path
     */
     public String getContextPath()
     {
        String path = "";
         WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        if (context != null)
        {
            path = context.getServletContext().getServletContextName();
            // when deployed in a servlet container under the server root
            if ("/".equals(path))
            {
                path = "";
            }
        }
        return path;
     }
 }
