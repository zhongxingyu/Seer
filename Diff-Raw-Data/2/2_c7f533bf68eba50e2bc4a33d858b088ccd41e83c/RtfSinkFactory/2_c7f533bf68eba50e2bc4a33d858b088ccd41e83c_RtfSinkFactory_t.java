 package org.apache.maven.doxia.module.rtf;
 
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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import org.apache.maven.doxia.sink.Sink;
 import org.apache.maven.doxia.sink.SinkFactory;
 import org.codehaus.plexus.util.WriterFactory;
 
 /**
 * Rtf implementation of the Sink factory.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  * @since 1.0
  * @plexus.component role="org.apache.maven.doxia.sink.SinkFactory" role-hint="rtf"
  */
 public class RtfSinkFactory
     implements SinkFactory
 {
     /** {@inheritDoc} */
     public Sink createSink( File outputDir, String outputName )
         throws IOException
     {
         return createSink( outputDir, outputName, WriterFactory.UTF_8 );
     }
 
     /** {@inheritDoc} */
     public Sink createSink( File outputDir, String outputName, String encoding )
         throws IOException
     {
         if ( !outputDir.isDirectory() )
         {
             throw new IllegalArgumentException( "The dir '" + outputDir + "' is not a directory or not exist" );
         }
 
         OutputStream os = new FileOutputStream( new File( outputDir, outputName ) );
 
         return new RtfSink( os );
     }
 
     /** {@inheritDoc} */
     public Sink createSink( Writer writer )
     {
         throw new UnsupportedOperationException( "createSink( Writer writer ) is not implemented." );
     }
 }
