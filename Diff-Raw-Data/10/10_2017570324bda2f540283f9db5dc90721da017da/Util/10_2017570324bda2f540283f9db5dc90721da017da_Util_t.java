 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.palette.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;
 
 /**
  *
  * @author Michal Skvor
  */
 public class Util {
 
     public static void addImport( final JTextComponent targetComponent, final String im ) {
        String doc = null;
        try {
            doc = targetComponent.getDocument().getText(0, targetComponent.getDocument().getEndPosition().getOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if( doc == null ) return;
         
         Pattern packagePattern = Pattern.compile( "package [a-zA-Z0-9_.]+;" );
         Pattern importPattern = Pattern.compile( "import [a-zA-Z0-9_.*]+;" );
         
         List<Import> imports = new ArrayList<Import>();
         int position = 0;
         try {
             BufferedReader in = new BufferedReader( new StringReader( doc ));
             String line = "";
             while( line != null ) {
                 char c;
                 line = null;
                 for(;;) {
                     c = (char) in.read();
                     position++;
                     if( c == 65535 ) { line = null; break; }
                     if( c == '\n' || c == '\r' ) { line = line == null ? "" : line ; break; }
                     if( line == null ) line = "" + c;
                     else line += c;
                 }
                 if( line == null ) continue;
                 
                 if( packagePattern.matcher( line ).matches()) {
                     // Package
                     Import i = new Import();
                     i.pkg = true;
                     i.end = position;
                     i.value = line.trim();
                     imports.add( i );
                 }
                 if( importPattern.matcher( line ).matches()) {
                     // Import
                     Import i = new Import();
                     i.end = position;
                     i.value = line.trim();
                     imports.add( i );
                 }
             }
             in.close();
         } catch( IOException e ) {
             throw new IllegalStateException( e );
         }
         
         final String pkgImport = im.substring( 0, im.lastIndexOf( '.' )) + ".*";
         // Check whether the import is in there
         boolean found = false;
         Import lastImport = null;
         for( Import i : imports ) {
             if( lastImport == null ) lastImport = i; 
             else if( lastImport.end < i.end ) lastImport = i;
 
             if(( "import " + im + ";" ).equals( i.value ) || ( "import " + pkgImport + ";" ).equals( i.value )) {
                 found = true;
                 break;
             }
         }
         if( !found ) {
             if( lastImport == null ) {
                 try {
                     targetComponent.getDocument().insertString( 0, "import " + im + ";\r\n", null );
                 } catch( BadLocationException e ) {
                     throw new IllegalStateException( e );
                 }
             } else {
                 if( lastImport.pkg ) {
                     try {
                         targetComponent.getDocument().
                                 insertString((int) lastImport.end, "\r\nimport " + im + ";\r\n", null );
                     } catch( BadLocationException e ) {
                         throw new IllegalStateException( e );
                     }
                 } else {
                     try {
                         targetComponent.getDocument().
                                 insertString((int) lastImport.end, "import " + im + ";\r\n", null );
                     } catch( BadLocationException e ) {
                         throw new IllegalStateException( e );
                     }
                 }
             }
         }
     }
     
     /*
     private void addImport( final JTextComponent targetComponent, final String im ) {
         // Get package import name
         final String pkgImport = im.substring( 0, im.lastIndexOf( '.' )) + ".*";
         try {
             JavaFXSource js = JavaFXSource.forDocument( targetComponent.getDocument());
             js.runUserActionTask(new Task<CompilationController>() {
 
                 public void run(CompilationController controller) throws Exception {
                     if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED))
                         return;
                     final CompilationUnitTree cu = controller.getCompilationUnit();
                     final MyVisitor v = new MyVisitor( controller, cu );
                     Object o = v.scan( cu, v );  
                     List<Import> l = (List<Import>)o;
                     
                     // Check whether the import is in there
                     boolean found = false;
                     Import lastImport = null;
                     for( Import i : l ) {
                         if( lastImport == null ) lastImport = i; 
                         else if( lastImport.end < i.end ) lastImport = i;
                         
                         if(( "import " + im + ";\r\n" ).equals( i.value ) || ( "import " + pkgImport + ";\r\n" ).equals( i.value )) {
                             found = true;
                             break;
                         }
                     }
                     if( !found ) {
                         if( lastImport == null ) {
                             targetComponent.getDocument().
                                     insertString( 0, "import " + im + ";\r\n", null );
                         } else {
                             if( lastImport.pkg ) {
                                 targetComponent.getDocument().
                                         insertString((int) lastImport.end + 2, "\r\nimport " + im + ";\r\n", null );
                             } else {
                                 targetComponent.getDocument().
                                         insertString((int) lastImport.end + 2, "import " + im + ";\r\n", null );
                             }
                         }
                     }
                 }
 
             }, true );
         } catch( IOException ioe ) {
             throw new IllegalStateException(ioe);
         }
     }
     
     private final class MyVisitor extends CancellableTreePathScanner<Object, Object> {
 
         
         private SourcePositions sp;
         
         public MyVisitor( CompilationController controller, CompilationUnitTree tree ) {
             sp = controller.getTrees().getSourcePositions();
         }
 
         @Override
         public Object visitCompilationUnit(CompilationUnitTree tree, Object p) {        
             List<Import> result = new ArrayList<Import>();
             // Get Package
             //PackageElement pkg = (PackageElement)tree.getPackageName();
             if( tree.getPackageName() != null ) {
                 ExpressionTree pkg = tree.getPackageName();
                 Import i = new Import();
                 i.pkg = true;
                 i.start = sp.getStartPosition( tree, pkg );
                 i.end = sp.getEndPosition( tree, pkg );
                 i.value = pkg.toString();
                 result.add( i );
             }
             
             // Get all imports
             for( ImportTree imp : tree.getImports()) {
                 //                
                 Import i = new Import();
                 i.start = sp.getStartPosition( tree, imp );
                 i.end = sp.getEndPosition( tree, imp );
                 i.value = imp.toString();
                 result.add( i );
             }
             //return super.visitCompilationUnit(tree, p);
             return result;
         }
         
 
         
         @Override
         public Object visitImport(ImportTree tree, Object p) {
             System.err.println(" - " + tree.toString());
             System.err.println(" - scanning import");
             return super.visitImport(tree, p);
         }
                 
     }    
     */
     
     private static class Import {
         public boolean pkg = false;
         public long start;
         public long end;
         public String value; 
     }
     
 }
