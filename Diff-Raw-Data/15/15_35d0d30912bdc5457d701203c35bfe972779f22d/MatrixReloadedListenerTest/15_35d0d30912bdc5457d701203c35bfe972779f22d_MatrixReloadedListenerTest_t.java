 /*
  *  The MIT License
  *
  *  Copyright 2011 Praqma A/S.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 
 package net.praqma.jenkins.plugin.reloaded;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import hudson.matrix.Axis;
 import hudson.matrix.AxisList;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixRun;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixProject;
 import hudson.model.ParameterValue;
 import hudson.model.Result;
 
 import net.praqma.jenkins.plugin.reloaded.MatrixReloadedState;
 import net.praqma.jenkins.plugin.reloaded.MatrixReloadedState.BuildState;
 
 import org.junit.BeforeClass;
 import org.jvnet.hudson.test.HudsonTestCase;
 
/**
 * Class for testing the Run Listener implementation
 * @author wolfgang
 *
 */
 public class MatrixReloadedListenerTest extends HudsonTestCase {
     private AxisList axes = null;
 
     private Combination c = null;
 
     @BeforeClass
     public void init() {
 
         axes = new AxisList(new Axis("dim1", "1", "2", "3"), new Axis("dim2", "a", "b", "c"));
 
         Map<String, String> r = new HashMap<String, String>();
         r.put("dim1", "1");
         r.put("dim2", "a");
         c = new Combination(r);
     }
 
     public void testReloadedForm() throws IOException, InterruptedException, ExecutionException {
         init();
 
     }
 
     public void testReloaded() throws IOException, InterruptedException, ExecutionException {
         init();
 
         MatrixProject mp = createMatrixProject("test");
         mp.setAxes(axes);
 
         List<ParameterValue> values = new ArrayList<ParameterValue>();
         /* UUID */
         String uuid = "myuuid";
         BuildState bs = MatrixReloadedState.getInstance().getBuildState(uuid);
 
         MatrixBuild mb = mp.scheduleBuild2(0).get();
         MatrixRun mr = mb.getRun(c);
         Result r = mr.getResult();
 
         assertNotNull(mb);
     }
 
     public void testNoReloaded() throws IOException, InterruptedException, ExecutionException {
         init();
 
         MatrixProject mp = createMatrixProject("test");
         mp.setAxes(axes);
 
         MatrixBuild mb = mp.scheduleBuild2(0).get();
         MatrixRun mr = mb.getRun(c);
         Result r = mr.getResult();
 
         assertNotNull(mb);
     }
 }
