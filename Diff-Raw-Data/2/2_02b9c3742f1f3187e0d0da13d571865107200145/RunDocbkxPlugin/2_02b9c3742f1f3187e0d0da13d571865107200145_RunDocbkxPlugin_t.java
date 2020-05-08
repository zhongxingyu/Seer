 /*
  * Copyright (c) 2010 Kathryn Huxtable
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kathrynhuxtable.maven.plugins.docbkxwrapper;
 
 import java.io.File;
 
 import java.lang.reflect.Field;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 import com.agilejava.docbkx.maven.DocbkxXhtmlMojo;
 
 /**
  * DOCUMENT ME!
  *
  * @author Kathryn Huxtable
  */
 public class RunDocbkxPlugin {
 
     /**
      * DOCUMENT ME!
      *
      * @param  log                    DOCUMENT ME!
      * @param  sourceDirectory        DOCUMENT ME!
      * @param  filePattern            DOCUMENT ME!
      * @param  docbookOutputDirectory DOCUMENT ME!
      *
      * @throws MojoFailureException
      * @throws MojoExecutionException
      */
     public void generateXhtml(Log log, File sourceDirectory, String filePattern, File docbookOutputDirectory) throws MojoFailureException,
         MojoExecutionException {
         DocbkxXhtmlMojo docbkxMojo = new DocbkxXhtmlMojo();
 
         docbkxMojo.setLog(log);
 
         setValue(docbkxMojo, "sourceDirectory", sourceDirectory);
         setValue(docbkxMojo, "includes", filePattern);
         setValue(docbkxMojo, "targetDirectory", docbookOutputDirectory);
        setValue(docbkxMojo, "xhtmlCustomization", "/org/kathrynhuxtable/maven/plugins/docbkxwrapper/xsl/html.xsl");
 
         setValue(docbkxMojo, "targetFileExtension", "html");
         setValue(docbkxMojo, "imgSrcPath", "./");
         // setValue(docbkxMojo, "chunkedOutput", false);
         setValue(docbkxMojo, "generateMetaAbstract", "false");
         setValue(docbkxMojo, "generateToc", "false");
         setValue(docbkxMojo, "highlightSource", "true");
         setValue(docbkxMojo, "highlightDefaultLanguage", null);
         // setValue(docbkxMojo, "htmlCellSpacing", 2);
         // setValue(docbkxMojo, "htmlCellPadding", 2);
         setValue(docbkxMojo, "suppressHeaderNavigation", "true");
         setValue(docbkxMojo, "suppressFooterNavigation", "true");
         setValue(docbkxMojo, "tableBordersWithCss", "true");
         setValue(docbkxMojo, "tableFrameBorderThickness", "0");
         setValue(docbkxMojo, "tableCellBorderThickness", "0");
         setValue(docbkxMojo, "useExtensions", "true");
         setValue(docbkxMojo, "calloutsExtension", "true");
 
         docbkxMojo.execute();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  o         DOCUMENT ME!
      * @param  fieldName DOCUMENT ME!
      * @param  value     DOCUMENT ME!
      *
      * @throws MojoFailureException DOCUMENT ME!
      */
     private void setValue(Object o, String fieldName, Object value) throws MojoFailureException {
         Class<?> c     = o.getClass();
         Field    field;
 
         try {
             field = c.getDeclaredField(fieldName);
             field.setAccessible(true);
             field.set(o, value);
         } catch (Exception e) {
             e.printStackTrace();
             throw new MojoFailureException(e.getMessage());
         }
     }
 }
