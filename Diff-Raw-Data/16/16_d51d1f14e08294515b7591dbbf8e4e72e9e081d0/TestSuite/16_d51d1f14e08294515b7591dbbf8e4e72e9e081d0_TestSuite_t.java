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
 
 package org.netbeans.test.javafx.editor.templates;
 
 import junit.framework.Test;
 import org.netbeans.junit.NbModuleSuite;
 
 /**
  *
  * @author Lark
  */
 public class TestSuite {
   public static Test suite() {
       NbModuleSuite.Configuration config = NbModuleSuite.
         createConfiguration(TemplateAb.class).
         addTest(TemplateAs.class).
         addTest(TemplateBi.class).
         addTest(TemplateBo.class).
         addTest(TemplateBr.class).
         addTest(TemplateCa.class).
         addTest(TemplateCl.class).
         addTest(TemplateCn.class).
         addTest(TemplateDe.class).
         addTest(TemplateEl.class).
         addTest(TemplateEli.class).
         addTest(TemplateEx.class).
         addTest(TemplateFa.class).
         addTest(TemplateFi.class).
         addTest(TemplateFor.class).
         addTest(TemplateFu.class).
         addTest(TemplateIf.class).
         addTest(TemplateIfe.class).
         addTest(TemplateIm.class).
         addTest(TemplateIn.class).
         addTest(TemplateInd.class).
         addTest(TemplateIns.class).
         addTest(TemplateInsa.class).
         addTest(TemplateInsb.class).
         addTest(TemplateInv.class).
         addTest(TemplateIof.class).
         addTest(TemplateNu.class).
         addTest(TemplatePa.class).
         addTest(TemplatePe.class).
         addTest(TemplatePu.class).
         addTest(TemplateRe.class).
         addTest(TemplateSerr.class).
         addTest(TemplateSo.class).
         addTest(TemplateSout.class).
         addTest(TemplateSt.class).
         addTest(TemplateTh.class).
         addTest(TemplateTof.class).
         addTest(TemplateTr.class).
         addTest(TemplateTrid.class).
         addTest(TemplateTrii.class).
         addTest(TemplateTrin.class).
         addTest(TemplateTrir.class).
         addTest(TemplateTw.class).
         addTest(TemplateVar.class).
         addTest(TemplateWe.class).
        addTest(TemplateWh.class).clusters(".*").enableModules(".*");
       return NbModuleSuite.create(config);
   }
 
     public static void main(java.lang.String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 }
 
