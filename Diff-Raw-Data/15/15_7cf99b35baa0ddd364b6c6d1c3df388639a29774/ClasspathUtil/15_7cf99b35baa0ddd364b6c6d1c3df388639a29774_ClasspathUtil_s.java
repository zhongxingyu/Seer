 /******************************************************************************
  * Copyright (c) 2006 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.common.jdt.internal.classpath;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 
 /**
  * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
  */
 
 public final class ClasspathUtil
 {
     private ClasspathUtil() {}
     
     public static Set getResolvedClasspath( final IJavaProject jproj,
                                             final IPath entryToIgnore )
     {
         return getResolvedClasspath( jproj, Collections.singleton( entryToIgnore ) );
     }
     
     public static Set getResolvedClasspath( final IJavaProject jproj,
                                             final Set entriesToIgnore )
     {
         final Set resolved = new HashSet();
         
         try 
         {
             final IClasspathEntry[] entries = jproj.getRawClasspath();
             
             for( int i = 0; i < entries.length; i++ )
             {
                 IClasspathEntry entry = entries[ i ];
                 
                 if( entriesToIgnore.contains( entry.getPath() ) )
                 {
                     continue;
                 }
                 
                 switch( entry.getEntryKind() )
                 {
                     case IClasspathEntry.CPE_LIBRARY:
                     case IClasspathEntry.CPE_PROJECT:
                     {
                         resolved.add( entry.getPath() );
                         break;
                     }
                     case IClasspathEntry.CPE_VARIABLE:
                     {
                         entry = JavaCore.getResolvedClasspathEntry( entry );
                         
                         if( entry != null )
                         {
                             resolved.add( entry.getPath() );
                         }
                         
                         break;
                     }
                     case IClasspathEntry.CPE_CONTAINER:
                     {
                         final IClasspathContainer container;
                         
                         try
                         {
                             container = JavaCore.getClasspathContainer( entry.getPath(), jproj );
                         }
                         catch( JavaModelException e )
                         {
                             Logger.getLogger().logError( e );
                             continue;
                         }
                        
                        final IClasspathEntry[] containerEntries
                            = container.getClasspathEntries();

                        for( int j = 0; j < containerEntries.length; j++ )
                        {
                            resolved.add( containerEntries[ j ].getPath() );
                         }
                     }
                 }
             }
         } 
         catch( JavaModelException e ) 
         {
             Logger.getLogger().logError( e );
         }
         
         return resolved;
     }
 
 }
