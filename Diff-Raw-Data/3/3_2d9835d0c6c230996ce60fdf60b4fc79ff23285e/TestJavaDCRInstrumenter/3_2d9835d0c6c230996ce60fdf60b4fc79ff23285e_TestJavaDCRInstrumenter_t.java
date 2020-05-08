 // Copyright (C) 2009 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.process.instrumenter.dcr;
 
 import java.lang.instrument.Instrumentation;
 import java.lang.instrument.UnmodifiableClassException;
 
 import test.MyClass;
 import test.MyExtendedClass;
 import junit.framework.TestCase;
 import net.grinder.engine.process.ScriptEngine.Recorder;
 import net.grinder.script.NotWrappableTypeException;
 import net.grinder.testutility.RandomStubFactory;
 import net.grinder.util.weave.Weaver;
 import net.grinder.util.weave.WeavingException;
 import net.grinder.util.weave.agent.ExposeInstrumentation;
 import net.grinder.util.weave.j2se6.ASMTransformerFactory;
 import net.grinder.util.weave.j2se6.DCRWeaver;
 
 
 /**
  * Unit tests for {@link JavaDCRInstrumenter}.
  *
  * @author Philip Aston
  * @version $Revision:$
  */
 public class TestJavaDCRInstrumenter extends TestCase {
 
   private static final Weaver s_weaver;
 
   private final JavaDCRInstrumenter m_instrumenter;
 
   private final RandomStubFactory<Recorder> m_recorderStubFactory =
       RandomStubFactory.create(Recorder.class);
   private final Recorder m_recorder = m_recorderStubFactory.getStub();
 
   static {
     try {
       s_weaver =
         new DCRWeaver(new ASMTransformerFactory(RecorderLocator.class),
                       ExposeInstrumentation.getInstrumentation());
     }
     catch (WeavingException e) {
       throw new ExceptionInInitializerError(e);
     }
   }
 
   private Instrumentation m_originalInstrumentation;
 
   public TestJavaDCRInstrumenter() throws Exception {
     m_instrumenter =
       new JavaDCRInstrumenter(s_weaver, RecorderLocator.getRecorderRegistry());
   }
 
   @Override
   protected void setUp() throws Exception {
     super.setUp();
     m_originalInstrumentation = ExposeInstrumentation.getInstrumentation();
   }
 
   @Override
   protected void tearDown() throws Exception {
     super.tearDown();
     ExposeInstrumentation.premain("", m_originalInstrumentation);
     RecorderLocator.clearRecorders();
   }
 
   private void assertNotWrappable(Object o) throws Exception {
     try {
       m_instrumenter.createInstrumentedProxy(null, null, o);
       fail("Expected NotWrappableTypeException");
     }
     catch (NotWrappableTypeException e) {
     }
   }
 
   public void testGetDescription() throws Exception {
     assertTrue(m_instrumenter.getDescription().length() > 0);
   }
 
   public void testCreateProxyWithNonWrappableParameters() throws Exception {
 
     assertNotWrappable(Object.class);
     assertNotWrappable(new Object());
     assertNotWrappable(new String());
     assertNotWrappable(java.util.Random.class);
 
     // Can't wrap classes in net.grinder.*.
     assertNotWrappable(this);
   }
 
   public void testInstrumentClass() throws Exception {
 
     assertEquals(6, MyClass.staticSix());
 
     final MyClass c1 = new MyClass();
 
     assertEquals(0, c1.getA());
     m_recorderStubFactory.assertNoMoreCalls();
 
     m_instrumenter.createInstrumentedProxy(null, m_recorder, MyClass.class);
     m_recorderStubFactory.assertNoMoreCalls();
 
     MyClass.staticSix();
 
     m_recorderStubFactory.assertSuccess("start");
     m_recorderStubFactory.assertSuccess("end", true);
     m_recorderStubFactory.assertNoMoreCalls();
 
     assertEquals(0, c1.getA());
     m_recorderStubFactory.assertNoMoreCalls();
 
     final MyClass c2 = new MyClass();
     m_recorderStubFactory.assertSuccess("start");
     m_recorderStubFactory.assertSuccess("start");
     m_recorderStubFactory.assertSuccess("end", true);
    m_recorderStubFactory.assertSuccess("end", true);
     assertEquals(0, c1.getA());
     assertEquals(0, c2.getA());
     m_recorderStubFactory.assertNoMoreCalls();
 
     m_instrumenter.createInstrumentedProxy(null,
                                            m_recorder,
                                            MyExtendedClass.class);
     m_recorderStubFactory.assertNoMoreCalls();
 
     MyClass.staticSix();
 
     // Single call - instrumenting extension shouldn't instrument superclass
     // statics.
     m_recorderStubFactory.assertSuccess("start");
     m_recorderStubFactory.assertSuccess("end", true);
     m_recorderStubFactory.assertNoMoreCalls();
   }
 
   public void testInstrumentUnmofifiableCLass() throws Exception {
 
     final RandomStubFactory<Instrumentation> instrumentationStubFactory =
       RandomStubFactory.create(Instrumentation.class);
     final Instrumentation instrumentation =
       instrumentationStubFactory.getStub();
 
     ExposeInstrumentation.premain("", instrumentation);
 
     instrumentationStubFactory.setThrows("retransformClasses",
                                          new UnmodifiableClassException());
 
     // Create a new weaver to force the weaving.
     final JavaDCRInstrumenter instrumenter =
       new JavaDCRInstrumenter(
         new DCRWeaver(new ASMTransformerFactory(RecorderLocator.class),
                       ExposeInstrumentation.getInstrumentation()),
         RecorderLocator.getRecorderRegistry());
 
     try {
       instrumenter.createInstrumentedProxy(null, m_recorder, MyClass.class);
       fail("Expected NotWrappableTypeException");
     }
     catch (NotWrappableTypeException e) {
     }
   }
 
   public void testInstrumentInstance() throws Exception {
 
     RecorderLocator.clearRecorders();
 
     final MyClass c1 = new MyExtendedClass();
 
     assertEquals(0, c1.getA());
     m_recorderStubFactory.assertNoMoreCalls();
 
     m_instrumenter.createInstrumentedProxy(null, m_recorder, c1);
     m_recorderStubFactory.assertNoMoreCalls();
 
     MyClass.staticSix();
     m_recorderStubFactory.assertNoMoreCalls();
 
     assertEquals(0, c1.getA());
     m_recorderStubFactory.assertSuccess("start");
     m_recorderStubFactory.assertSuccess("end", true);
     m_recorderStubFactory.assertNoMoreCalls();
 
     final MyClass c2 = new MyClass();
     assertEquals(0, c2.getA());
     m_recorderStubFactory.assertNoMoreCalls();
   }
 }
