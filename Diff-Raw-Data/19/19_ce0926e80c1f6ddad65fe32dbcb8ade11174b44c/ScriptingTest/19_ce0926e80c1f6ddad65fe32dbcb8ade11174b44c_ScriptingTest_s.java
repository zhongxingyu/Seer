 /**  ____    __    ____  ____  ____,,___     ____  __  __  ____
  *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
  *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
  *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
  */
 package com.razie.pub.base.test;
 
import junit.framework.TestCase;
import razie.base.scripting.ScriptContext;
import razie.base.scripting.ScriptContextImpl;
import razie.base.scripting.ScriptFactory;
import razie.base.scripting.ScriptJS;

import com.razie.pub.base.TimeOfDay;
 
 public class ScriptingTest extends TestCase {
 
    @Override
    public void setUp() {
    }
 
    public void testJs() {
       String script = "x = 'abc'; y = 3; function f(x){return x+1} f(7)";
          ScriptJS js = new ScriptJS(script);
         String res = js.eval(ScriptFactory.mkContext()).toString();
          assertTrue(res.equals("8"));
    }
    
    public void testObj() {
       String script = "TimeOfDay.value()";
          ScriptJS js = new ScriptJS(script);
 
          ScriptContext ctx = ScriptFactory.mkContext();
          ctx.setAttr("TimeOfDay", new TimeOfDay());
 
         String res = js.eval(ctx).toString();
          System.out.println(res);
          boolean found=false;
          for (String v : TimeOfDay.values)
             if (res.equals(v)) { found=true; break; }
          assertTrue(found);
          
          // script = "com.razie.playground.things.TimeOfDay.calcvalue()";
          // js = new JavaScript(script);
          // System.out.println(js.run(new ScriptContext.Impl()));
    }
 }
