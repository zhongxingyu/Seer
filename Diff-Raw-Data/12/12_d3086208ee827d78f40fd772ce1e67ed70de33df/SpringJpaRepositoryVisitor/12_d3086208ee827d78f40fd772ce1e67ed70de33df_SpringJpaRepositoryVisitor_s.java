 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema.jpa;
 
 import fr.imag.exschema.SpringRepositoryVisitor;
 import fr.imag.exschema.exporter.RooModel;
 
 /**
  * Visitor that identifies JPA repositories.
  * 
  * @author jccastrejon
  * 
  */
 public class SpringJpaRepositoryVisitor extends SpringRepositoryVisitor {
 
     @Override
     protected boolean isSpringRepository(final String className) {
         boolean returnValue;
         String qualifiedName;
 
         qualifiedName = this.getQualifiedName(className);
         returnValue = false;
         if (qualifiedName.startsWith("org.springframework.")) {
             if (qualifiedName.endsWith("ActiveRecord")) {
                 returnValue = true;
             }
         }
 
         return returnValue;
     }
 
     @Override
     public String getImplementation() {
        return RooModel.MONGODB.toString();
     }
 }
