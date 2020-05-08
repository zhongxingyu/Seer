 /*
  * Sonar PAM plugin
  * Copyright (C) 2011 Marco Tizzoni
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.pam;
 
 import java.util.ArrayList;
 import org.sonar.api.Plugin;
 
 import java.util.List;
 import org.sonar.api.Extension;
 
 /**
  *
  * @author Marco Tizzoni
  */
 public class PamAuthPlugin implements Plugin {
 
  /**
   * @deprecated this is not used anymore
   */
   public String getKey() {
     return "pam";
   }
 
  /**
   * @deprecated this is not used anymore
   */
   public String getName() {
     return "Sonar Pam Authenticator plugin";
   }
 
  /**
   * @deprecated this is not used anymore
   */
   public String getDescription() {
     return "Delegate password management to underlying PAM.";
   }
 
   // Declare all your Sonar extensions
   public List getExtensions() {
     List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
     extensions.add(PamAuthenticator.class);
     extensions.add(PamConfiguration.class);
     return extensions;
 
   }
 
   @Override
   public String toString() {
     return getClass().getSimpleName();
   }
 }
