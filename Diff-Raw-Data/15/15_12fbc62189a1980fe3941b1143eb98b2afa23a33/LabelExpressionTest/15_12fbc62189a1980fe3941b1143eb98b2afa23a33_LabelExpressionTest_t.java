 /*******************************************************************************
  *
  * Copyright (c) 2010, InfraDNA, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: 
  *
  *   
  *       
  *
  *******************************************************************************/ 
 
 package hudson.model.labels;
 
import org.antlr.runtime.RecognitionException;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.AppointedNode;
 import hudson.model.BuildListener;
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.Label;
 import hudson.model.Node.Mode;
 import hudson.slaves.DumbSlave;
 import hudson.slaves.RetentionStrategy;
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.jvnet.hudson.test.SequenceLock;
 import org.jvnet.hudson.test.TestBuilder;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.concurrent.Future;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class LabelExpressionTest extends HudsonTestCase {
 
     /**
      * Tests the expression parser.
      */
     public void testParser() throws Exception {
         parseAndVerify("foo", "foo");
         parseAndVerify("32bit.dot", "32bit.dot");
         parseAndVerify("foo||bar", "foo || bar");
 
         // user-given parenthesis is preserved
         parseAndVerify("foo||bar&&zot", "foo||bar&&zot");
         parseAndVerify("foo||(bar&&zot)", "foo||(bar&&zot)");
 
         parseAndVerify("(foo||bar)&&zot", "(foo||bar)&&zot");
         parseAndVerify("foo->bar", "foo ->\tbar");
         parseAndVerify("!foo<->bar", "!foo <-> bar");
     }
 
    private void parseAndVerify(String expected, String expr) throws RecognitionException {
         assertEquals(expected, LabelExpression.parseExpression(expr).getName());
     }
 
     public void testParserError() throws Exception {
         parseShouldFail("foo bar");
         parseShouldFail("foo (bar)");
     }
 
     public void testLaxParsing() {
         // this should parse as an atom
         LabelAtom l = (LabelAtom)hudson.getLabel("lucene.zones.apache.org (Solaris 10)");
         assertEquals(l.getName(),"lucene.zones.apache.org (Solaris 10)");
         assertEquals(l.getExpression(),"\"lucene.zones.apache.org (Solaris 10)\"");
     }
 
     public void testDataCompatibilityWithHostNameWithWhitespace() throws Exception {
         DumbSlave slave = new DumbSlave("abc def (xyz) - test", "dummy",
                 createTmpDir().getPath(), "1", Mode.NORMAL, "", createComputerLauncher(null), RetentionStrategy.NOOP, Collections.EMPTY_LIST);
         hudson.addNode(slave);
 
 
         FreeStyleProject p = createFreeStyleProject();
         p.setAssignedLabel(hudson.getLabel("abc def"));
         assertEquals("abc def",p.getAssignedLabel().getName());
         assertEquals("\"abc def\"",p.getAssignedLabel().getExpression());
 
         // but if the name is set, we'd still like to parse it
         p.setAppointedNode(new AppointedNode("a:b c", false));
         assertEquals("a:b c",p.getAssignedLabel().getName());
     }
 
     public void testQuote() {
         Label l = hudson.getLabel("\"abc\\\\\\\"def\"");
         assertEquals("abc\\\"def",l.getName());
     }
 
     /**
      * The name should have parenthesis at the right place to preserve the tree structure.
      */
     public void testComposite() {
         LabelAtom x = hudson.getLabelAtom("x");
         assertEquals("!!x",x.not().not().getName());
         assertEquals("(x||x)&&x",x.or(x).and(x).getName());
         assertEquals("x&&x||x",x.and(x).or(x).getName());
     }
 
     public void ignore_testDash() {
         hudson.getLabelAtom("solaris-x86");
     }
 
     private void parseShouldFail(String expr) {
         try {
             LabelExpression.parseExpression(expr);
             fail(expr + " should fail to parse");
        } catch (RecognitionException e) {
             // expected
         }
     }
 
 }
