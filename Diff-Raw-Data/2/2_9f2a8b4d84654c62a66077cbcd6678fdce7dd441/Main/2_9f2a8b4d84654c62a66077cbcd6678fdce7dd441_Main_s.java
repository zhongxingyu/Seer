 package rosetta;
 
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 
 import java.lang.String;
 
 public class Main {
     public static void main(String[] args) {
         RubyInstanceConfig config = new RubyInstanceConfig();
         config.setArgv(args);
         Ruby ruby = Ruby.newInstance(config);
        ruby.evalScriptlet("require 'rubygems'; require 'rosetta/cli'; Rosetta::Command.run");
     }
 }
