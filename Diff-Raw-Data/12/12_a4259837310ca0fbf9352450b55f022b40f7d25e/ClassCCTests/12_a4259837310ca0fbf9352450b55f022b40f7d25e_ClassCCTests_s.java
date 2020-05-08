 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
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
  */
 
 package org.netbeans.test.javafx.editor.completion;
 
 import junit.framework.Test;
 import org.netbeans.junit.NbModuleSuite;
 import org.netbeans.spi.editor.completion.CompletionProvider;
 
 /**
  *
  * @author David Strupl
  */
 public class ClassCCTests extends CompletionTestPerformer {
     
     /** Creates a new instance of AllCCTests */
     public ClassCCTests(String name) {
         super(name);
     }
 
     /*
      * 
      * Class level completion
      * 
      */
     public void testFXClass001() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass001.fx",
                 4, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClass002a() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass002.fx",
                 9, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClass002b() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass002.fx",
                 11, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClass002c() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass002.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClassPublic() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "public ", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass001.fx",
                 4, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClass003a() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass003.fx",
                 11, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXClass003b() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass003.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
 // /*   Fails
     public void testFXClassVar1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "var a : ", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClassVar1.fx",
                 4, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
 
     public void testFXClass004() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter,
                 "a.", // what should be typed in the editor
                 false,
                 getDataDir(),
                 "fx-prj-1",
                 "classes/FXTestClass004.fx",
                 18, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);
     }
 
     public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(ClassCCTests.class).enableModules(".*").clusters("ide.*|java.*|javafx.*"));
     }
 
 }
