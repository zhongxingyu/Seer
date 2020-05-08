 package org.apache.maven.doxia.editor;
 
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
 
 import org.apache.maven.doxia.editor.windows.EditorWindow;
import org.codehaus.plexus.embed.Embedder;
 
 import javax.swing.*;
 
 /**
  * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
  * @version $Id$
  */
 public class DoxiaEditor
 {
     public static void main( String[] args )
         throws Exception
     {
         new DoxiaEditor().work();
     }
 
     private void work()
         throws Exception
     {
        Embedder embedder = new Embedder();
 
        embedder.start();
 
        Application application = (Application) embedder.lookup( Application.ROLE );
 
         EditorWindow window = new EditorWindow( application );
 
         application.setEditorWindow( window );
 
         window.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
 
         application.getActionManager().getAction( "open-document" ).actionPerformed( null );
 
         // ----------------------------------------------------------------------
         // Power on
         // ----------------------------------------------------------------------
 
         window.show();
 
         // ----------------------------------------------------------------------
         // Power off
         // ----------------------------------------------------------------------
 
        embedder.release( application );
     }
 }
