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
 
 import org.sonar.api.security.LoginPasswordAuthenticator;
 
 /**
  *
  * @author Marco Tizzoni
  */
 public class PamAuthenticator implements LoginPasswordAuthenticator {
 
   private PamConfiguration configuration;
 
   /**
    * Creates a new instance of PamAuthenticator with specified configuration.
    *
    * @param configuration PAM configuration
    */
   public PamAuthenticator(PamConfiguration configuration) {
     this.configuration = configuration;
   }
 
   public void init() {
   }
 
   public boolean authenticate(final String login, final String password) {
     return configuration.getPAM().authenticateSuccessful(login, password);
   }
 }
