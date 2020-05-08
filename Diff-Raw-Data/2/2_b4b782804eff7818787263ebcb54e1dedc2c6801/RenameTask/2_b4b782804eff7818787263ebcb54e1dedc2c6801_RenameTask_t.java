 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.build.ant;
 
 import java.io.File;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class RenameTask extends Task {
 
     protected File from;
 
     protected File to;
 
     public void setFrom(File from) {
         this.from = from;
     }
 
     public void setTo(File to) {
         this.to = to;
     }
 
     @Override
     public void execute() throws BuildException {
         String fromName = from.getName();
         if (fromName.endsWith("*")) {
             String prefix = fromName.substring(0, fromName.length() - 1);
             File dir = from.getParentFile();
             File[] files = dir.listFiles();
             for (int k = 0; k < files.length; k++) {
                 File f = files[k];
                if (f.getName().startsWith(prefix)) {
                     f.renameTo(to);
                     return;
                 }
             }
         } else {
             from.renameTo(to);
         }
     }
 
 }
