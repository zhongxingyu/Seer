 package org.nitrox.formular;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.HashSet;
 
 import javax.script.ScriptException;
 import org.jruby.embed.LocalContextScope;
 import org.jruby.embed.LocalVariableBehavior;
 import org.jruby.embed.ScriptingContainer;
 
 public class JRubyScriptRunnerManager implements ScriptRunner {
 
     //private ScriptEngine engine;
     private ScriptingContainer ruby;
     private static String defautScript;
     private String formulaScript;
     private static final String filename = "src/main/ruby/formulaR.rb";
 
     public JRubyScriptRunnerManager() {
 
         try {
             if (defautScript == null) {
                 String path = System.getProperty("user.dir") + "/" + filename;
                 defautScript = Files.toString(new File(path), Charsets.UTF_8);
             }
             ruby = new ScriptingContainer(LocalContextScope.THREADSAFE, LocalVariableBehavior.TRANSIENT);
         } catch (IOException ioe) {
            
         }      
     }
 
     @Override
     public BigDecimal eval(String expression, HashSet<Variable> variables) {
         formulaScript = defautScript + expression;        
         
         for (Variable variable : variables) {
             ruby.put(variable.getName(), variable.getValue());
         }
         
         return (BigDecimal) ruby.runScriptlet(formulaScript);
     }   
 }
