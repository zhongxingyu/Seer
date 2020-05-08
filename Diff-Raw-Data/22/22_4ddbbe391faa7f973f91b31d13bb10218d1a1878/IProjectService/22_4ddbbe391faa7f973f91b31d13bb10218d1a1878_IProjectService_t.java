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
 package org.vimide.eclipse.core.service;
 
 import org.eclipse.core.resources.IProject;
 
 /**
  * Service for project resource management.
  * 
  * @author keyhom (keyhom.c@gmail.com)
  */
 public interface IProjectService {
 
     /**
     * Refreshes the supplied project resource.
      * 
      * @param project the project.
      */
     void refresh(IProject project) throws Exception;
     
     /**
      * Created the supplied project resource.
      * 
      * @param project the project.
      * @throws Exception
      */
     void create(IProject project) throws Exception;
 
 }
