 package org.sonatype.aether.ant.types;
 
 /*******************************************************************************
  * Copyright (c) 2010-2011 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution.
  * The Eclipse Public License is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  * The Apache License v2.0 is available at
  *   http://www.apache.org/licenses/LICENSE-2.0.html
  * You may elect to redistribute this code under either of these licenses.
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.DataType;
 import org.apache.tools.ant.types.Reference;
 
 /**
  * @author Benjamin Bentmann
  */
 public class Dependencies
     extends DataType
 {
 
     private Pom pom;
 
     private List<Dependency> dependencies = new ArrayList<Dependency>();
 
     private List<Exclusion> exclusions = new ArrayList<Exclusion>();
 
     protected Dependencies getRef()
     {
         return (Dependencies) getCheckedRef();
     }
 
     public void validate( Task task )
     {
         if ( isReference() )
         {
             getRef().validate( task );
         }
         else
         {
            if ( getPom() != null && getPom().getFile() == null )
             {
                 throw new BuildException( "A <pom> used for dependency resolution has to be backed by a pom.xml-file" );
             }
             for ( Dependency dependency : dependencies )
             {
                 dependency.validate( task );
             }
         }
     }
 
     public void setRefid( Reference ref )
     {
         if ( pom != null || !exclusions.isEmpty() || !dependencies.isEmpty() )
         {
             throw noChildrenAllowed();
         }
         super.setRefid( ref );
     }
 
     public void addPom( Pom pom )
     {
         checkChildrenAllowed();
         if ( this.pom != null )
         {
             throw new BuildException( "You must not specify multiple <pom> elements" );
         }
         this.pom = pom;
     }
 
     public Pom getPom()
     {
         if ( isReference() )
         {
             return getRef().getPom();
         }
         return pom;
     }
 
     public void setPomRef( Reference ref )
     {
         if ( pom == null )
         {
             pom = new Pom();
             pom.setProject( getProject() );
         }
         pom.setRefid( ref );
     }
 
     public void addDependency( Dependency dependency )
     {
         checkChildrenAllowed();
         this.dependencies.add( dependency );
     }
 
     public List<Dependency> getDependencies()
     {
         if ( isReference() )
         {
             return getRef().getDependencies();
         }
         return dependencies;
     }
 
     public void addExclusion( Exclusion exclusion )
     {
         checkChildrenAllowed();
         this.exclusions.add( exclusion );
     }
 
     public List<Exclusion> getExclusions()
     {
         if ( isReference() )
         {
             return getRef().getExclusions();
         }
         return exclusions;
     }
 
 }
