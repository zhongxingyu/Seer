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
 public class FrameCCTests extends CompletionTestPerformer {
     
     /** Creates a new instance of AllCCTests */
     public FrameCCTests(String name) {
         super(name);
     }
 
     /*
      * 
      * Frame Level Tests
      * 
      */
     public void testFXFrame001() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/Frame001.fx",
                11, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     /*
      * 
      * Stage Level Tests
      * 
      */
     public void testFXStage() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/Stage001.fx",
                 19, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContent() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/Stage001.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentArc1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentArc1.fx",
                 24, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentCircle1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentCircle1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentEllipse1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentEllipse1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentImage1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentImage1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentImage2() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentImage1.fx",
                 23, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentLine1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentLine1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentPolygon1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentPolygon1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentRectangle1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentRectangle1.fx",
                 21, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentText1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentText1.fx",
                 22, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentText2() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentText1.fx",
                 24, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     
     public void testFXStageContentColor() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "fill: Color.", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/ContentColor.fx",
                 26, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
 
     public void testFXStageContentLinearGradient1() throws Exception {
         new CompletionTestCase(this).test(
                 outputWriter, logWriter, 
                 "", // what should be typed in the editor
                 false, 
                 getDataDir(),
                 "fx-prj-1",
                 "frame/stage/content/LinearGradient1.fx",
                 23, // line number where the cursor should be
                 CompletionProvider.COMPLETION_QUERY_TYPE);        
     }
     public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(FrameCCTests.class).enableModules(".*").clusters("ide.*|java.*|javafx.*"));
     }
 
 }
