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
 
 package org.netbeans.test.javafx.bestpractices.samples;
 
 import junit.framework.Test;
 import org.netbeans.junit.NbModuleSuite;
 
 /**
  *
  * @author Lark
  */
 public class TestSuite {
   public static Test suite() {
       NbModuleSuite.Configuration config = NbModuleSuite.
         createConfiguration(ColorWheel.class).
 //        addTest(ColorWheel.class, ColorWheel.TESTS).
         addTest(LinearGradient.class, LinearGradient.TESTS).
         addTest(PointsAndLines.class, PointsAndLines.TESTS).
         addTest(ShapePrimitives.class, ShapePrimitives.TESTS).
         addTest(Bounce.class, Bounce.TESTS).
         addTest(BackgroundImage.class, BackgroundImage.TESTS).
         addTest(Displaying.class, Displaying.TESTS).
         addTest(Transparency.class, Transparency.TESTS).
         addTest(Clock.class, Clock.TESTS).
         addTest(Constrain.class, Constrain.TESTS).
         addTest(Easing.class, Easing.TESTS).
         addTest(Milliseconds.class, Milliseconds.TESTS).
         addTest(Mouse1D.class, Mouse1D.TESTS).
         addTest(Mouse2D.class, Mouse2D.TESTS).
         addTest(MousePress.class, MousePress.TESTS).
         addTest(StoringInput.class, StoringInput.TESTS).
         addTest(Arctangent.class, Arctangent.TESTS).
         addTest(Distance1D.class, Distance1D.TESTS).
         addTest(Distance2D.class, Distance2D.TESTS).
         addTest(Sine.class, Sine.TESTS).
         addTest(SineWave.class, SineWave.TESTS).
         addTest(BouncyBubbles.class, BouncyBubbles.TESTS).
         addTest(Linear.class, Linear.TESTS).
         addTest(Flocks.class, Flocks.TESTS).
         addTest(SimpleParticleSystem.class, SimpleParticleSystem.TESTS).
         addTest(SmokeParticleSystem.class, SmokeParticleSystem.TESTS).
         addTest(Rotate.class, Rotate.TESTS).
         addTest(Scale.class, Scale.TESTS).
        addTest(Translate.class, Translate.TESTS);
       return NbModuleSuite.create(config);
   }
 
     public static void main(java.lang.String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 }
 
