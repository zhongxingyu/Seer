 package org.apache.maven.doxia.ide.eclipse.common.ui;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.StringReader;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.maven.doxia.Converter;
 import org.apache.maven.doxia.ConverterException;
 import org.apache.maven.doxia.DefaultConverter;
 import org.apache.maven.doxia.UnsupportedFormatException;
 import org.apache.maven.doxia.parser.ParseException;
 import org.apache.maven.doxia.wrapper.InputReaderWrapper;
 import org.apache.maven.doxia.wrapper.OutputStreamWrapper;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.StringUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * Wraps some Doxia operations for Eclipse.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  * @since 1.0
  */
 public class DoxiaWrapper
 {
     private static final String DEFAULT_OUTPUT = "xhtml";
 
     /**
      * Convert a content using Doxia {@link Converter}. All errors are specified as <code>IMarker</code>
      * for the given file.
      *
      * @param content the content to convert
      * @param file the file associated with the content
      * @param format the wanted format
      */
     public static void convert( String content, IFile file, String format )
     {
         Reader reader = new StringReader( content );
 
         convert( reader, file, format );
     }
 
     /**
      * Convert a file using Doxia {@link Converter}.
      *
      * @param file the file to convert
      * @param format the wanted format
      * @return the file content converted
      */
     public static String convert( IFile file, String format )
     {
         Reader reader;
         try
         {
             reader = new FileReader( file.getLocation().toFile() );
         }
         catch ( FileNotFoundException e )
         {
             CommonPlugin.logError( "FileNotFoundException: " + e.getMessage(), e, true );
 
             try
             {
                 IMarker marker = file.createMarker( IMarker.PROBLEM );
                 if ( marker.exists() )
                 {
                     marker.setAttribute( IMarker.TRANSIENT, true );
                     marker.setAttribute( IMarker.MESSAGE, e.getMessage() );
                     marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
                 }
             }
             catch ( CoreException ce )
             {
                 CommonPlugin.logError( "CoreException: " + ce.getMessage(), ce, true );
 
                 return "CoreException: " + ce.getMessage();
             }
 
             return "FileNotFoundException: " + e.getMessage();
         }
 
         return convert( reader, file, format );
     }
 
     // ----------------------------------------------------------------------
     // Private methods
     // ----------------------------------------------------------------------
 
     /**
      * Internal Doxia converter.
      *
      * @param reader
      * @param file
      * @param format
      * @return the result of the Doxia conversion or an exception message if any
      */
     private static String convert( Reader reader, IFile file, String format )
     {
         OutputStream out = new ByteArrayOutputStream();
         Converter converter = new DefaultConverter();
         try
         {
             InputReaderWrapper input = InputReaderWrapper.valueOf( reader, format, converter.getInputFormats() );
            OutputStreamWrapper output = OutputStreamWrapper.valueOf( out, format, file.getCharset(), converter
                 .getOutputFormats() );
 
             converter.convert( input, output );
         }
         catch ( UnsupportedFormatException e )
         {
             String msg = ( StringUtils.isEmpty( e.getMessage() ) ? e.getClass().getName() : e.getMessage() );
             CommonPlugin.logError( "Doxia Unsupported Format Exception: " + msg, e, true );
 
             try
             {
                 clearMarkers( file );
 
                 IMarker marker = file.createMarker( IMarker.PROBLEM );
                 if ( marker.exists() )
                 {
                     marker.setAttribute( IMarker.TRANSIENT, true );
                     marker.setAttribute( IMarker.MESSAGE, msg );
                     marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
                 }
             }
             catch ( CoreException ce )
             {
                 String msgCe = ( StringUtils.isEmpty( ce.getMessage() ) ? ce.getClass().getName() : ce.getMessage() );
                 CommonPlugin.logError( "CoreException: " + msgCe, ce, true );
 
                 return "CoreException: " + msgCe;
             }
 
             return "Doxia Unsupported Format Exception: " + e.getMessage();
         }
         catch ( ConverterException e )
         {
             String msg = ( StringUtils.isEmpty( e.getMessage() ) ? e.getClass().getName() : e.getMessage() );
 
             try
             {
                 clearMarkers( file );
 
                 IMarker marker = file.createMarker( IMarker.PROBLEM );
                 if ( marker.exists() )
                 {
                     marker.setAttribute( CommonPlugin.PLUGIN_ID, CommonPlugin.PLUGIN_ID );
                     marker.setAttribute( IMarker.TRANSIENT, true );
                     marker.setAttribute( IMarker.MESSAGE, msg );
                     if ( ParseException.class.isAssignableFrom( e.getCause().getClass() ) )
                     {
                         ParseException ex = (ParseException) e.getCause();
                         marker.setAttribute( IMarker.LINE_NUMBER, ex.getLineNumber() );
                         marker.setAttribute( IMarker.LOCATION, ex.getLineNumber() );
                     }
                     marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
                 }
             }
             catch ( CoreException ce )
             {
                 String msgCe = ( StringUtils.isEmpty( ce.getMessage() ) ? ce.getClass().getName() : ce.getMessage() );
                 CommonPlugin.logError( "CoreException: " + msgCe, ce, true );
 
                 return "CoreException: " + msgCe;
             }
 
             return "Doxia Converter Exception: " + msg;
         }
         catch ( Throwable t )
         {
             String msg = ( StringUtils.isEmpty( t.getMessage() ) ? t.getClass().getName() : t.getMessage() );
 
             CommonPlugin.logError( "Doxia Converter Throwable: " + msg, t, true );
 
             try
             {
                 clearMarkers( file );
 
                 IMarker marker = file.createMarker( IMarker.PROBLEM );
                 if ( marker.exists() )
                 {
                     marker.setAttribute( IMarker.TRANSIENT, true );
                     marker.setAttribute( IMarker.MESSAGE, msg );
                     marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
                 }
             }
             catch ( CoreException ce )
             {
                 String msgCe = ( StringUtils.isEmpty( ce.getMessage() ) ? ce.getClass().getName() : ce.getMessage() );
                 CommonPlugin.logError( "CoreException: " + msgCe, ce, true );
 
                 return "CoreException: " + msgCe;
             }
 
             return "Doxia Converter Throwable: " + t.getMessage();
         }
         finally
         {
             IOUtil.close( reader );
             IOUtil.close( out );
         }
 
         try
         {
             file.deleteMarkers( IMarker.PROBLEM, true, IResource.DEPTH_INFINITE );
         }
         catch ( CoreException ce )
         {
             String msgCe = ( StringUtils.isEmpty( ce.getMessage() ) ? ce.getClass().getName() : ce.getMessage() );
             CommonPlugin.logError( "CoreException: " + msgCe, ce, true );
 
             return "CoreException: " + msgCe;
         }
 
         return out.toString();
     }
 
     /**
      * @param iFile
      * @throws CoreException
      */
     private static void clearMarkers( IFile iFile )
         throws CoreException
     {
         IMarker[] markers = iFile.findMarkers( null, true, IResource.DEPTH_ZERO );
         IMarker[] deleteMarkers = new IMarker[markers.length];
         int deleteindex = 0;
         Object owner;
 
         for ( int i = markers.length - 1; i >= 0; i-- )
         {
             IMarker marker = markers[i];
             owner = marker.getAttribute( CommonPlugin.PLUGIN_ID );
 
             if ( CommonPlugin.PLUGIN_ID.equals( owner ) )
             {
                 deleteMarkers[deleteindex++] = marker;
             }
         }
 
         if ( deleteindex > 0 )
         {
             IMarker[] todelete = new IMarker[deleteindex];
             System.arraycopy( deleteMarkers, 0, todelete, 0, deleteindex );
             iFile.getWorkspace().deleteMarkers( todelete );
         }
     }
 }
