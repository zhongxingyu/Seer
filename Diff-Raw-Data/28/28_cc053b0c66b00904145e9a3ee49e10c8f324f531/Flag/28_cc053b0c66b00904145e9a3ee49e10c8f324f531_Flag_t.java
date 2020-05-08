 /*
  * JBoss, Home of Professional Open Source.
  * Copyright (c) 2013, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.pull.shared;
 
import java.io.Serializable;

public class Flag implements Serializable{

    private static final long serialVersionUID = -4167575539988047120L;
 
     public enum Status {
         /**
          * The {@code UNSET} {@link Status} represents a {@link Flag} which has not been toggled. It is represented by ' '.
          */
         UNSET,
 
         /**
          * The {@code POSITIVE} {@link Status} represents a {@link Flag} which has been toggled to '+'.
          */
         POSITIVE,
 
         /**
          * The {@code NEGATIVE} {@link Status} represents a {@link Flag} which has been toggled to '-'.
          */
         NEGATIVE,
 
         /**
          * The {@code UNKNOWN} {@link Status} represents a {@link Flag} which has been toggled to '?'.
          */
         UNKNOWN
     }
 
     private String name;
     private String setter;
     private Status status;
 
     Flag(String name, String setter, Status status) {
         this.name = name;
         this.setter = setter;
         this.status = status;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getSetter() {
         return setter;
     }
 
     public void setSetter(String setter) {
         this.setter = setter;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
     public String toString() {
         return setter + "\t" + " set " + name + "\t" + " to " + status + "\t\n";
     }
 
 }
