 /**
  * Copyright (c) 2012 keyhom.c@gmail.com.
  *
  * This software is provided 'as-is', without any express or implied warranty.
  * In no event will the authors be held liable for any damages arising from
  * the use of this software.
  *
  * Permission is granted to anyone to use this software for any purpose
  * excluding commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  *
  *     1. The origin of this software must not be misrepresented; you must not
  *     claim that you wrote the original software. If you use this software
  *     in a product, an acknowledgment in the product documentation would be
  *     appreciated but is not required.
  *
  *     2. Altered source versions must be plainly marked as such, and must not
  *     be misrepresented as being the original software.
  *
  *     3. This notice may not be removed or altered from any source
  *     distribution.
  */
 package org.vimide.eclipse.core.servlet.project;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.core.resources.IProject;
 import org.vimide.core.servlet.VimideHttpServletRequest;
 import org.vimide.core.servlet.VimideHttpServletResponse;
 import org.vimide.eclipse.core.natures.NaturesMapping;
 import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Request which obtains a list of projects and project paths for use by clients
 * to determine which project a file belongs to.
  * 
  * @author keyhom (keyhom.c@gmail.com)
  */
 @WebServlet(urlPatterns = "/projects")
 public class ProjectsServlet extends GenericVimideHttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.vimide.core.servlet.VimideHttpServlet#doGet(org.vimide.core.servlet.VimideHttpServletRequest,
      *      org.vimide.core.servlet.VimideHttpServletResponse)
      */
     @Override
     protected void doGet(VimideHttpServletRequest req,
             VimideHttpServletResponse resp) throws ServletException,
             IOException {
         final List<Map<String, Object>> results = Lists.newArrayList();
 
         for (IProject project : getProjects()) {
             Map<String, Object> info = Maps.newHashMap();
             info.put("name", project.getName());
             info.put("path", project.getLocation().toOSString());
 
             if (project.isOpen()) {
                 try {
                     String[] aliases = NaturesMapping.getNatureAliases(project
                             .getDescription().getNatureIds());
                     if (null == aliases || aliases.length == 0)
                         aliases = new String[] { "none" };
                     info.put("natures", aliases);
                 } catch (final Exception e) {
                     info.put("natures", ArrayUtils.EMPTY_STRING_ARRAY);
                 }
             }
 
             results.add(info);
         }
 
         resp.writeAsJson(results);
     }
 
 }
