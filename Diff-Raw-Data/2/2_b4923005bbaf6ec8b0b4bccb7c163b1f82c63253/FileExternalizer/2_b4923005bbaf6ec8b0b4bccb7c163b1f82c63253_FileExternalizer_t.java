 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.externalizer;
 
 import java.awt.Image;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.servlet.ServletConfig;
 import javax.swing.ImageIcon;
 
 import Acme.JPM.Encoders.GifEncoder;
 
 import org.wings.ResourceImageIcon;
 
 /**
  * This externalizer writes objects into a temporary directory.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @author <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
  * @version $Revision$
  */
 public class FileExternalizer
     extends AbstractExternalizer
 {
     /**
      * TODO: documentation
      */
     protected String fileDir = null;
 
     /**
      * TODO: documentation
      */
     protected String httpAddress = null;
 
 
     /**
      * TODO: documentation
      *
      */
     public FileExternalizer( ServletConfig config ) {
         this( config.getInitParameter("externalizer.file.path"),
               config.getInitParameter("externalizer.file.url") );
     }
 
     public FileExternalizer( String dir, String addr ) {
         if ( dir == null || addr == null ) {
            throw new IllegalStateException("externalizer.file.path and externalizer.file.url required in initArgs");
         }
 
         fileDir = dir;
         httpAddress = addr;
     }
 
 
     /**
      * TODO: documentation
      *
      * @throws java.io.IOException
      */
     protected void doExternalize(ExternalizedInfo info)
         throws java.io.IOException
     {
         String fname = fileDir + info.extFileName;
         Object obj   = info.extObject;
         ObjectHandler handler = info.handler;
 
         FileOutputStream out = new FileOutputStream(fname);
         handler.write(obj, out);
         out.flush();
         out.close();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void doDelete(ExternalizedInfo info) {
         try {
             File f = new File(fileDir, info.extFileName);
             f.delete();
         }
         catch (Exception e) {
             debug("cannot remove externalized " + info.extFileName);
         }
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     protected String getExternalizedURL(ExternalizedInfo info) {
         return httpAddress + "/" + info.extFileName;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
