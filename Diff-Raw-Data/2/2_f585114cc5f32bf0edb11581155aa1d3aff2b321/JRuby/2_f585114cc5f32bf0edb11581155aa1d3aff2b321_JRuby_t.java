 package com.neopoly.rubyfox;
 
 import com.smartfoxserver.v2.extensions.SFSExtension;
 import org.jruby.CompatVersion;
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.javasupport.JavaUtil;
 import org.jruby.runtime.builtin.IRubyObject;
 
 public class JRuby {
     private Ruby _ruby;
     private SFSExtension _sfsExtension;
     private IRubyObject _extensionHandler;
 
     public JRuby(SFSExtension sfsExtension) {
         _sfsExtension = sfsExtension;
         boot();
     }
 
     private void boot() {
         log("Booting JRuby");
 
         RubyInstanceConfig config = new RubyInstanceConfig();
         config.setCompatVersion(CompatVersion.RUBY1_9);
         _ruby = Ruby.newInstance(config);
         log("  " + eval("%{Version: jruby-#{JRUBY_VERSION} (ruby-#{RUBY_VERSION})}").toString());
 
         appendLoadPath(getConfigProperty("load_path", null));
     }
 
     public void load() {
         require("rubyfox");
         initRubyExtension(getConfigProperty("module_name", "Rubyfox"));
        log("Booting JRuby completed");
     }
 
     public void handleInit() {
         delegateToHandler("on_init");
     }
 
     public void handleClientRequest(Object... p) {
         delegateToHandler("on_request", p);
     }
 
     public void handleServerEvent(Object... p) {
         delegateToHandler("on_event", p);
     }
 
     public void handleDestroy() {
         delegateToHandler("on_destroy");
     }
 
     private void delegateToHandler(String method, Object... p) {
         _extensionHandler.callMethod(_ruby.getCurrentContext(), method, JavaUtil.convertJavaArrayToRuby(_ruby, p));
     }
 
     private void initRubyExtension(String moduleName) {
         IRubyObject module = evalLogged(moduleName);
         // Rubyfox.init(sfs_extension)
         _extensionHandler = module.callMethod(_ruby.getCurrentContext(), "init", JavaUtil.convertJavaArrayToRuby(_ruby, new Object[]{ _sfsExtension }));
     }
 
     private String getConfigProperty(String name, String def) {
         String value = _sfsExtension.getConfigProperties().getProperty(name);
         if (value == null) value = def;
         return value;
     }
 
     private void appendLoadPath(String loadPath) {
         String[] pathes = loadPath.split(":");
         for (String path : pathes) {
             evalLogged("$LOAD_PATH << \"" + path + "\"");
         }
     }
 
     private void log(String msg) {
         _sfsExtension.trace(msg);
     }
 
     private IRubyObject eval(String code) {
         return _ruby.evalScriptlet(code);
     }
 
     private IRubyObject evalLogged(String code) {
         log("  " + code);
         IRubyObject result = eval(code);
         log("  # => " + result);
         return result;
     }
 
     private void require(String lib) {
         evalLogged("require '" + lib + "'");
     }
 }
