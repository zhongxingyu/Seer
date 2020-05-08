 package net.danielkvasnicka.elasticsearch.scriptingengine;
 
 import clojure.lang.Var;
 import org.elasticsearch.common.settings.Settings;
 import static org.junit.Assert.*;
 
 import org.elasticsearch.script.ExecutableScript;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.HashMap;
 
 import static org.mockito.Mockito.*;
 
 /**
  * User: dkvasnicka
  * Date: 11/20/12
  * Time: 17:28
  */
 public class ClojureScriptEngineServiceTest {
 
     private static final HashMap<String,Object> VARS_MAP = new HashMap<String, Object>() {{
         put("a", 1);
         put("b", 2);
     }};
     private static final String RUNNABLE_SCRIPT_TAKING_VAR_MAP =
            "(ns test (:require [clojure.test :refer :all])) (defn x [m] (reduce + (.values m)))";
 
     private ClojureScriptEngineService service;
 
     @Before
     public void setUp() {
         Settings settings = mock(Settings.class);
         this.service = new ClojureScriptEngineService(settings);
     }
 
     @Test
     public void testCompile() throws Throwable {
 
         Object result = this.service.compile("(ns test) (defn x [y z] (+ y z))");
         assertEquals((long) 3, ((Var) result).invoke(1, 2));
     }
 
     @Test
     public void testExecutable() throws Throwable {
 
         Object result = this.service.compile(RUNNABLE_SCRIPT_TAKING_VAR_MAP);
         ExecutableScript executable = this.service.executable(result, VARS_MAP);
         Object output = executable.run();
         assertNotNull(output);
         assertEquals((long) 3, output);
     }
 
     @Test
     public void testExecute() throws Throwable {
 
         Object result = this.service.compile(RUNNABLE_SCRIPT_TAKING_VAR_MAP);
         Object output = this.service.execute(result, VARS_MAP);
         assertNotNull(output);
         assertEquals((long) 3, output);
     }
 }
