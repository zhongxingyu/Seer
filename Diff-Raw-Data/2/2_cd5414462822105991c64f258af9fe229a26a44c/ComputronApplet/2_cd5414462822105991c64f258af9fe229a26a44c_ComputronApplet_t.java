 package org.computron;
 
 import java.applet.Applet;
 import java.util.ArrayList;
 import org.jruby.Ruby;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.runtime.builtin.IRubyObject;
 
 public class ComputronApplet extends Applet {
 
   Ruby runtime;
   ComputronEvaluator computron;
 
   public void init() {
     runtime = JavaEmbedUtils.initialize(new ArrayList());
     IRubyObject engineClass = JavaEmbedUtils.newRuntimeAdapter().eval(runtime, "require 'computron_engine'; ComputronEngine");
     computron = (ComputronEvaluator)JavaEmbedUtils.invokeMethod(runtime, engineClass, "new", null, ComputronEvaluator.class);
   }
 
  public void evaluate(String program) {
     computron.evaluate(program);
   }
 
   public void setEnvironmentValue(String name, int value) {
     computron.setEnvironmentValue(name, value);
   }
 
   public int getEnvironmentValue(String name) {
     return computron.getEnvironmentValue(name);
   }
 
   public void reset() {
     computron.reset();
   }
 }
