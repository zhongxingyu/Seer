 /*
  * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
  *
  * This file is part of Resin(R) Open Source
  *
  * Each copy or derived work must preserve the copyright notice and this
  * notice unmodified.
  *
  * Resin Open Source is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Resin Open Source is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
  * of NON-INFRINGEMENT.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Resin Open Source; if not, write to the
  *
  *   Free Software Foundation, Inc.
  *   59 Temple Place, Suite 330
  *   Boston, MA 02111-1307  USA
  *
  * @author Scott Ferguson
  */
 
 package com.caucho.quercus;
 
 import com.caucho.config.ConfigException;
 import com.caucho.quercus.annotation.ClassImplementation;
 import com.caucho.quercus.env.*;
 import com.caucho.quercus.lib.file.FileModule;
 import com.caucho.quercus.lib.session.QuercusSessionManager;
 import com.caucho.quercus.module.ModuleContext;
 import com.caucho.quercus.module.ModuleInfo;
 import com.caucho.quercus.module.ModuleStartupListener;
 import com.caucho.quercus.module.QuercusModule;
 import com.caucho.quercus.module.StaticFunction;
 import com.caucho.quercus.page.InterpretedPage;
 import com.caucho.quercus.page.PageManager;
 import com.caucho.quercus.page.QuercusPage;
 import com.caucho.quercus.parser.QuercusParser;
 import com.caucho.quercus.program.ClassDef;
 import com.caucho.quercus.program.InterpretedClassDef;
 import com.caucho.quercus.program.JavaClassDef;
 import com.caucho.quercus.program.JavaImplClassDef;
 import com.caucho.quercus.program.QuercusProgram;
 import com.caucho.util.Alarm;
 import com.caucho.util.L10N;
 import com.caucho.util.Log;
 import com.caucho.util.LruCache;
 import com.caucho.util.TimedCache;
 import com.caucho.vfs.*;
 
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.SoftReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.*;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Facade for the PHP language.
  */
 public class Quercus
 {
   private static L10N L = new L10N(Quercus.class);
   private static final Logger log = Log.open(Quercus.class);
 
   private static HashSet<String> _superGlobals
     = new HashSet<String>();
 
   private final PageManager _pageManager;
   private final QuercusSessionManager _sessionManager;
 
   private final ClassLoader _loader;
   
   private ModuleContext _moduleContext;
 
   private HashMap<String, InternStringValue> _internMap
     = new HashMap<String, InternStringValue>();
 
   private HashMap<String, QuercusModule> _modules
     = new HashMap<String, QuercusModule>();
 
   private HashSet<ModuleStartupListener> _moduleStartupListeners
     = new HashSet<ModuleStartupListener>();
 
   private HashSet<String> _extensionSet
     = new HashSet<String>();
 
   private HashMap<String, Value> _constMap
     = new HashMap<String, Value>();
 
   private HashMap<String, StaticFunction> _funMap
     = new HashMap<String, StaticFunction>();
 
   private HashMap<String, StaticFunction> _lowerFunMap
     = new HashMap<String, StaticFunction>();
 
   private ClassDef _stdClassDef;
   private QuercusClass _stdClass;
 
   private HashMap<String, ClassDef> _staticClasses
     = new HashMap<String, ClassDef>();
 
   private HashMap<String, ClassDef> _lowerStaticClasses
     = new HashMap<String, ClassDef>();
 
   private HashMap<String, JavaClassDef> _javaClassWrappers
     = new HashMap<String, JavaClassDef>();
 
   private HashMap<String, JavaClassDef> _lowerJavaClassWrappers
     = new HashMap<String, JavaClassDef>();
 
   private HashMap<String, StringValue> _iniMap
     = new HashMap<String, StringValue>();
 
   private HashMap<Value, Value> _serverEnvMap
     = new HashMap<Value, Value>();
 
   private LruCache<String, QuercusProgram> _evalCache
     = new LruCache<String, QuercusProgram>(256);
 
   private TimedCache<IncludeKey, Path> _includeCache
     = new TimedCache<IncludeKey, Path>(4096, 10000);
 
   private LruCache<DefinitionKey,SoftReference<DefinitionState>> _defCache
     = new LruCache<DefinitionKey,SoftReference<DefinitionState>>(4096);
 
   private long _defCacheHitCount;
   private long _defCacheMissCount;
 
   // XXX: needs to be a timed LRU
   private LruCache<String, SessionArrayValue> _sessionMap
     = new LruCache<String, SessionArrayValue>(4096);
 
   private HashMap<String, Object> _specialMap
     = new HashMap<String, Object>();
 
   private String _scriptEncoding = "utf-8";
 
   private boolean _isStrict;
 
   private DataSource _database;
 
   private long _staticId;
 
   private Path _pwd;
   private Path _workDir;
 
   /**
    * Constructor.
    */
   public Quercus()
   {
     _loader = Thread.currentThread().getContextClassLoader();
     
     _moduleContext = getLocalContext();
     
     initStaticFunctions();
     initStaticClasses();
     initStaticClassServices();
 
     _pageManager = createPageManager();
     
     _sessionManager = createSessionManager();
 
     _workDir = getWorkDir();
   }
   
   /**
    * Returns the working directory.
    */
   public Path getPwd()
   {
     if (_pwd == null)
       _pwd = new FilePath(System.getProperty("user.dir"));
     
     return _pwd;
   }
 
   /**
    * Sets the working directory.
    */
   public void setPwd(Path path)
   {
     _pwd = path;
   }
 
   public Path getWorkDir()
   {
     if (_workDir == null)
       _workDir = getPwd().lookup("WEB-INF/work");
 
     return _workDir;
   }
 
   public void setWorkDir(Path workDir)
   {
     _workDir = workDir;
   }
 
   public String getCookieName()
   {
     return "JSESSIONID";
   }
 
   public String getVersion()
   {
     return "Resin/3.1.0";
   }
 
   public String getVersionDate()
   {
     return "20061220T1222";
   }
 
   protected PageManager createPageManager()
   {
     return new PageManager(this);
   }
 
   protected QuercusSessionManager createSessionManager()
   {
     return new QuercusSessionManager();
   }
   
   /**
    * Returns the context for this class loader.
    */
   public final ModuleContext getLocalContext()
   {
     return getLocalContext(_loader);
   }
 
   public ModuleContext getLocalContext(ClassLoader loader)
   {
     synchronized (this) {
       if (_moduleContext == null)
 	_moduleContext = createModuleContext(loader);
     }
 
     return _moduleContext;
   }
 
   protected ModuleContext createModuleContext(ClassLoader loader)
   {
     return new ModuleContext(loader);
   }
 
   /**
    * Returns the module context.
    */
   public ModuleContext getModuleContext()
   {
     return _moduleContext;
   }
 
   public QuercusSessionManager getQuercusSessionManager()
   {
     return _sessionManager;
   }
 
   /**
    * Set true if pages should be compiled.
    */
   public void setCompile(boolean isCompile)
   {
     _pageManager.setCompile(isCompile);
   }
 
   /**
    * Set true if pages should be compiled.
    */
   public void setLazyCompile(boolean isCompile)
   {
     _pageManager.setLazyCompile(isCompile);
   }
 
   public String getScriptEncoding()
   {
     return _scriptEncoding;
   }
 
   public void setScriptEncoding(String encoding)
   {
     _scriptEncoding = encoding;
   }
 
   /**
    * Sets the default data source.
    */
   public void setDatabase(DataSource database)
   {
     _database = database;
   }
 
   /**
    * Gets the default data source.
    */
   public DataSource getDatabase()
   {
     return _database;
   }
 
   /**
    * Gets the default data source.
    */
   public DataSource findDatabase(String driver, String url)
   {
     if (_database != null)
       return _database;
     else {
       throw new QuercusModuleException(L.l("Can't find database for driver '{0}' and url '{1}'",
 					   driver, url));
     }
   }
 
   /**
    * Unwrap connection if necessary.
    */
   public Connection getConnection(Connection conn)
   {
     return conn;
   }
 
   /**
    * Unwrap statement if necessary.
    */
   public Statement getStatement(Statement stmt)
   {
     return stmt;
   }
 
   /**
    * Sets the strict mode.
    */
   public void setStrict(boolean isStrict)
   {
     _isStrict = isStrict;
   }
 
   /**
    * Gets the strict mode.
    */
   public boolean isStrict()
   {
     return _isStrict;
   }
 
   /**
    * Adds a module
    */
   public void addModule(QuercusModule module)
     throws ConfigException
   {
     try {
       introspectPhpModuleClass(module.getClass());
     } catch (Exception e) {
       throw new ConfigException(e);
     }
   }
 
   /**
    * Adds a java class
    */
   public void addJavaClass(String name, Class type)
     throws ConfigException
   {
     try {
       if (type.isAnnotationPresent(ClassImplementation.class))
         introspectJavaImplClass(name, type, null);
       else
         introspectJavaClass(name, type, null, null);
     } catch (Exception e) {
       throw new ConfigException(e);
     }
   }
 
   /**
    * Adds a impl class
    */
   public void addImplClass(String name, Class type)
     throws ConfigException
   {
     try {
       introspectJavaImplClass(name, type, null);
     } catch (Exception e) {
       throw new ConfigException(e);
     }
   }
 
   /**
    * Adds a java class
    */
   public JavaClassDef getJavaClassDefinition(String className)
   {
     JavaClassDef def = _javaClassWrappers.get(className);
 
     if (def != null)
       return def;
 
     try {
       ClassLoader loader = Thread.currentThread().getContextClassLoader();
 
       Class type;
 
       try {
         type = Class.forName(className, false, loader);
       }
       catch (ClassNotFoundException e) {
         throw new ClassNotFoundException(L.l("'{0}' not valid {1}", className, e.toString()));
 
       }
 
       def = JavaClassDef.create(getModuleContext(), className, type);
 
       _javaClassWrappers.put(className, def);
 
       // def.introspect(getModuleContext());
 
       return def;
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new QuercusRuntimeException(e);
     }
   }
 
   /**
    * Finds the java class wrapper.
    */
   public ClassDef findJavaClassWrapper(String name)
   {
     ClassDef def = _javaClassWrappers.get(name);
 
     if (def != null)
       return def;
 
     return _lowerJavaClassWrappers.get(name.toLowerCase());
   }
 
   /**
    * Sets an ini file.
    */
   public void setIniFile(Path path)
   {
     // XXX: Not sure why this dependency would be useful
     // Environment.addDependency(new Depend(path));
 
     if (path.canRead()) {
       Env env = new Env(this);
 
       Value result = FileModule.parse_ini_file(env, path, false);
 
       if (result instanceof ArrayValue) {
         ArrayValue array = (ArrayValue) result;
 
         for (Map.Entry<Value,Value> entry : array.entrySet()) {
           setIni(entry.getKey().toString(), entry.getValue().toString());
         }
       }
     }
   }
 
   /**
    * Sets an ini value.
    */
   public void setIni(String name, StringValue value)
   {
     // XXX: s/b specified some other way
     if ("magic_quotes_sybase".equals(value.toString())
         || "magic_quotes_runtime".equals(value.toString())) {
       if (value.toBoolean())
         throw new UnsupportedOperationException(name);
     }
 
     _iniMap.put(name, value);
   }
 
   /**
    * Sets a server env value.
    */
   public void setServerEnv(String name, String value)
   {
     setServerEnv(new StringValueImpl(name), new StringValueImpl(value));
   }
 
   /**
    * Sets a server env value.
    */
   public void setServerEnv(StringValue name, StringValue value)
   {
     _serverEnvMap.put(name, value);
   }
 
   /**
    * Gets a server env value.
    */
   public Value getServerEnv(StringValue name)
   {
     return _serverEnvMap.get(name);
   }
 
   /**
    * Returns the server env map.
    */
   public HashMap<Value,Value> getServerEnvMap()
   {
     return _serverEnvMap;
   }
 
   /**
    * Sets an ini value.
    */
   public void setIni(String name, String value)
   {
     if ("off".equalsIgnoreCase(value))
       value = "";
 
     setIni(name, new StringValueImpl(value));
   }
 
   /**
    * Gets an ini value.
    */
   public StringValue getIni(String name)
   {
     return _iniMap.get(name);
   }
 
   /**
    * Gets all ini values having this prefix.
    */
   public HashMap<String,StringValue> getIniAll(String prefix)
   {
     if (_iniMap == null)
       return null;
 
     HashMap<String, StringValue> iniCopy = new HashMap<String, StringValue>();
 
     for (Map.Entry<String, StringValue> entry : _iniMap.entrySet()) {
       String key = entry.getKey();
       StringValue value = entry.getValue();
       if (key.startsWith(prefix))
         iniCopy.put(key, entry.getValue());
     }
 
     return iniCopy;
   }
 
   /**
    * Returns the relative path.
    */
   /*
   public String getClassName(Path path)
   {
     return _pageManager.getClassName(path);
   }
   */
 
   /**
    * Returns an include path.
    */
   public Path getIncludeCache(String include,
                               String includePath,
                               Path pwd,
                               Path scriptPwd)
   {
     IncludeKey key = new IncludeKey(include, includePath, pwd, scriptPwd);
 
     Path path = _includeCache.get(key);
 
     return path;
   }
 
   /**
    * Adds an include path.
    */
   public void putIncludeCache(String include,
                               String includePath,
                               Path pwd,
                               Path scriptPwd,
                               Path path)
   {
     IncludeKey key = new IncludeKey(include, includePath, pwd, scriptPwd);
 
     _includeCache.put(key, path);
   }
 
   /**
    * Returns the definition cache hit count.
    */
   public long getDefCacheHitCount()
   {
     return _defCacheHitCount;
   }
 
   /**
    * Returns the definition cache miss count.
    */
   public long getDefCacheMisstCount()
   {
     return _defCacheMissCount;
   }
 
   /**
    * Returns the definition state for an include.
    */
   public DefinitionState getDefinitionCache(DefinitionKey key)
   {
     SoftReference<DefinitionState> defStateRef = _defCache.get(key);
 
     if (defStateRef != null) {
       DefinitionState defState = defStateRef.get();
 
       if (defState != null) {
         _defCacheHitCount++;
 
         return defState.copyLazy();
       }
     }
 
     _defCacheMissCount++;
 
     return null;
   }
 
   /**
    * Returns the definition state for an include.
    */
   public void putDefinitionCache(DefinitionKey key,
                                  DefinitionState defState)
   {
     _defCache.put(key, new SoftReference<DefinitionState>(defState.copy()));
   }
 
   /**
    * Parses a quercus program.
    *
    * @param path the source file path
    * @return the parsed program
    * @throws IOException
    */
   public QuercusPage parse(Path path)
     throws IOException
   {
     return _pageManager.parse(path);
   }
 
   /**
    * Parses a quercus program.
    *
    * @param path the source file path
    * @return the parsed program
    * @throws IOException
    */
   public QuercusPage parse(Path path, String fileName, int line)
     throws IOException
   {
     return _pageManager.parse(path, fileName, line);
   }
 
   /**
    * Parses a quercus program.
    *
    * @param path the source file path
    * @return the parsed program
    * @throws IOException
    */
   public QuercusPage parse(ReadStream is)
     throws IOException
   {
     return new InterpretedPage(QuercusParser.parse(this, is));
   }
 
   /**
    * Parses a quercus string.
    *
    * @param code the source code
    * @return the parsed program
    * @throws IOException
    */
   public QuercusProgram parseCode(String code)
     throws IOException
   {
     QuercusProgram program = _evalCache.get(code);
 
     if (program == null) {
       program = QuercusParser.parseEval(this, code);
       _evalCache.put(code, program);
     }
 
     return program;
   }
 
   /**
    * Parses a quercus string.
    *
    * @param code the source code
    * @return the parsed program
    * @throws IOException
    */
   public QuercusProgram parseEvalExpr(String code)
     throws IOException
   {
     // XXX: possible conflict with parse eval because of the
     // return value changes
     QuercusProgram program = _evalCache.get(code);
 
     if (program == null) {
       program = QuercusParser.parseEvalExpr(this, code);
       _evalCache.put(code, program);
     }
 
     return program;
   }
 
   /**
    * Parses a function.
    *
    * @param args the arguments
    * @param code the source code
    * @return the parsed program
    * @throws IOException
    */
   public Value parseFunction(String args, String code)
     throws IOException
   {
     return QuercusParser.parseFunction(this, args, code);
   }
 
   /**
    * Returns the function with the given name.
    */
   public StaticFunction findFunction(String name)
   {
     StaticFunction fun = _funMap.get(name);
 
     if (fun == null)
       fun = _lowerFunMap.get(name.toLowerCase());
 
     return fun;
   }
 
   /**
    * Returns the function with the given name.
    */
   public StaticFunction findFunctionImpl(String name)
   {
     StaticFunction fun = _funMap.get(name);
 
     return fun;
   }
 
   /**
    * Returns the function with the given name.
    */
   public StaticFunction findLowerFunctionImpl(String lowerName)
   {
     StaticFunction fun = _lowerFunMap.get(lowerName);
 
     return fun;
   }
 
   /**
    * Returns an array of the defined functions.
    */
   public ArrayValue getDefinedFunctions()
   {
     ArrayValue internal = new ArrayValueImpl();
 
     for (String name : _funMap.keySet()) {
       internal.put(name);
     }
 
     return internal;
   }
 
   /**
    * Returns true if the variable is a superglobal.
    */
   public static boolean isSuperGlobal(String name)
   {
     return _superGlobals.contains(name);
   }
 
   /**
    * Returns the stdClass definition.
    */
   public QuercusClass getStdClass()
   {
     return _stdClass;
   }
 
   /**
    * Returns the class with the given name.
    */
   public ClassDef findClass(String name)
   {
     ClassDef def = _staticClasses.get(name);
 
     if (def == null)
       def = _lowerStaticClasses.get(name.toLowerCase());
 
     return def;
   }
 
   /**
    * Returns the class maps.
    */
   public HashMap<String, ClassDef> getClassMap()
   {
     return _staticClasses;
   }
 
   /**
    * Returns the module with the given name.
    */
   public QuercusModule findModule(String name)
   {
     return _modules.get(name);
   }
 
   /**
    * Returns a list of the modules that have some startup code to run.
    */
   public HashSet<ModuleStartupListener> getModuleStartupListeners()
   {
     return _moduleStartupListeners;
   }
 
   /**
    * Returns true if an extension is loaded.
    */
   public boolean isExtensionLoaded(String name)
   {
     return _extensionSet.contains(name);
   }
 
   /**
    * Returns true if an extension is loaded.
    */
   public HashSet<String> getLoadedExtensions()
   {
     return _extensionSet;
   }
 
   /**
    * Returns true if an extension is loaded.
    */
   public Value getExtensionFuncs(String name)
   {
     ArrayValue value = null;
 
     for (QuercusModule module : _modules.values()) {
       String []ext = module.getLoadedExtensions();
       boolean hasExt = false;
 
       for (int i = 0; i < ext.length; i++) {
         if (name.equals(ext[i]))
           hasExt = true;
       }
 
       if (hasExt) {
         value = new ArrayValueImpl();
       }
     }
 
     if (value != null)
       return value;
     else
       return BooleanValue.FALSE;
   }
 
   public HashMap<String, Value> getConstMap()
   {
     return _constMap;
   }
 
   /**
    * Interns a string.
    */
   public InternStringValue intern(String name)
   {
     synchronized (_internMap) {
       InternStringValue value = _internMap.get(name);
 
       if (value == null) {
         name = name.intern();
 
         value = new InternStringValue(name);
         _internMap.put(name, value);
       }
 
       return value;
     }
   }
 
   /**
    * Returns a named constant.
    */
   public Value getConstant(String name)
   {
     return _constMap.get(name);
   }
 
   public String createStaticName()
   {
     return ("s" + _staticId++).intern();
   }
 
   /**
    * Loads the session from the backing.
    */
   public SessionArrayValue loadSession(Env env, String sessionId)
   {
     long now = Alarm.getCurrentTime();
 
     SessionArrayValue session =
       _sessionManager.getSession(env, sessionId, now);
 
     if (session == null)
       session = _sessionManager.createSession(env, sessionId, now);
 
     return session;
   }
 
   /**
    * Saves the session to the backing.
    */
   public void saveSession(Env env, SessionArrayValue session)
   {
     _sessionManager.saveSession(env, session);
   }
 
   /**
    * Removes the session from the backing.
    */
   public void destroySession(String sessionId)
   {
     _sessionManager.removeSession(sessionId);
   }
 
   /**
    * Loads a special value
    */
   public Object getSpecial(String key)
   {
     return _specialMap.get(key);
   }
 
   /**
    * Saves a special value
    */
   public void setSpecial(String key, Object value)
   {
     _specialMap.put(key, value);
   }
 
   /**
    * Scans the classpath for META-INF/services/com.caucho.quercus.QuercusModule
    */
   private void initStaticFunctions()
   {
     Thread thread = Thread.currentThread();
     ClassLoader loader = thread.getContextClassLoader();
 
     try {
       String quercusModule
         = "META-INF/services/com.caucho.quercus.QuercusModule";
       Enumeration<URL> urls = loader.getResources(quercusModule);
 
       HashSet<URL> urlSet = new HashSet<URL>();
 
       // get rid of duplicate entries found by multiple classloaders
       // XXX: test
       while (urls.hasMoreElements()) {
         URL url = urls.nextElement();
         
         urlSet.add(url);
       }
 
       for (URL url : urlSet) {
         InputStream is = null;
         ReadStream rs = null;
         try {
           is = url.openStream();
 	  
 	  rs = new ReadStream(new VfsStream(is, null));
 
           parseServicesModule(rs);
         } catch (Throwable e) {
           log.log(Level.WARNING, e.toString(), e);
         } finally {
           if (rs != null)
             rs.close();
           if (is != null)
             is.close();
         }
       }
 
     } catch (Exception e) {
       log.log(Level.WARNING, e.toString(), e);
     }
   }
 
   /**
    * Parses the services file, looking for PHP services.
    */
   private void parseServicesModule(ReadStream in)
     throws IOException, ClassNotFoundException,
            IllegalAccessException, InstantiationException
   {
     ClassLoader loader = Thread.currentThread().getContextClassLoader();
     String line;
 
     while ((line = in.readLine()) != null) {
       int p = line.indexOf('#');
 
       if (p >= 0)
         line = line.substring(0, p);
 
       line = line.trim();
 
       if (line.length() > 0) {
         String className = line;
 
         try {
           Class cl;
           try {
             cl = Class.forName(className, false, loader);
           }
           catch (ClassNotFoundException e) {
             throw new ClassNotFoundException(L.l("`{0}' not valid {1}", className, e.toString()));
           }
 
           introspectPhpModuleClass(cl);
         } catch (Throwable e) {
           log.info("Failed loading " + className + "\n" + e.toString());
           log.log(Level.FINE, e.toString(), e);
         }
       }
     }
   }
 
   /**
    * Introspects the module class for functions.
    *
    * @param cl the class to introspect.
    */
   private void introspectPhpModuleClass(Class cl)
     throws IllegalAccessException, InstantiationException, ConfigException
   {
     log.fine("Quercus loading module " + cl.getName());
 
     QuercusModule module = (QuercusModule) cl.newInstance();
 
     ModuleContext context = getLocalContext();
 
     ModuleInfo info = context.addModule(module.getClass().getName(),
 					module);
 
     _modules.put(info.getName(), info.getModule());
 
     if (info.getModule() instanceof ModuleStartupListener)
       _moduleStartupListeners.add((ModuleStartupListener)info.getModule());
 
     for (String ext : info.getLoadedExtensions())
       _extensionSet.add(ext);
 
     Map<String, Value> map = info.getConstMap();
 
     if (map != null)
       _constMap.putAll(map);
 
     Map<String, StringValue> iniMap = info.getDefaultIni();
     _iniMap.putAll(iniMap);
 
     for (Map.Entry<String, StaticFunction> entry : info.getFunctions().entrySet()) {
       _funMap.put(entry.getKey(), entry.getValue());
       _lowerFunMap.put(entry.getKey().toLowerCase(), entry.getValue());
     }
   }
 
   public static Value objectToValue(Object obj)
   {
     if (obj == null)
       return NullValue.NULL;
     else if (Byte.class.equals(obj.getClass()) ||
              Short.class.equals(obj.getClass()) ||
              Integer.class.equals(obj.getClass()) ||
              Long.class.equals(obj.getClass())) {
       return LongValue.create(((Number) obj).longValue());
     } else if (Float.class.equals(obj.getClass()) ||
                Double.class.equals(obj.getClass())) {
       return DoubleValue.create(((Number) obj).doubleValue());
     } else if (String.class.equals(obj.getClass())) {
       return new StringValueImpl((String) obj);
     } else {
       // XXX: unknown types, e.g. Character?
 
       return null;
     }
   }
 
   /**
    * Scans the classpath for META-INF/services/com.caucho.quercus.QuercusClass
    */
   private void initStaticClassServices()
   {
     Thread thread = Thread.currentThread();
     ClassLoader loader = thread.getContextClassLoader();
 
     try {
       String quercusModule
         = "META-INF/services/com.caucho.quercus.QuercusClass";
       Enumeration<URL> urls = loader.getResources(quercusModule);
 
       while (urls.hasMoreElements()) {
         URL url = urls.nextElement();
 
         InputStream is = null;
         ReadStream rs = null;
         try {
           is = url.openStream();
 	  
 	  rs = new ReadStream(new VfsStream(is, null));
 	  
           parseClassServicesModule(rs);
         } catch (Throwable e) {
           log.log(Level.WARNING, e.toString(), e);
         } finally {
           if (rs != null)
             rs.close();
           if (is != null)
             is.close();
         }
       }
 
     } catch (Exception e) {
       log.log(Level.WARNING, e.toString(), e);
     }
   }
 
   /**
    * Parses the services file, looking for PHP services.
    */
   private void parseClassServicesModule(ReadStream in)
     throws IOException, ClassNotFoundException,
            IllegalAccessException, InstantiationException,
            ConfigException, NoSuchMethodException, InvocationTargetException
   {
     ClassLoader loader = Thread.currentThread().getContextClassLoader();
     String line;
 
     while ((line = in.readLine()) != null) {
       int p = line.indexOf('#');
 
       if (p >= 0)
         line = line.substring(0, p);
 
       line = line.trim();
 
       if (line.length() == 0)
         continue;
 
       String[] args = line.split(" ");
 
       String className = args[0];
 
       Class cl;
 
       try {
         cl = Class.forName(className, false, loader);
 
         String phpClassName = null;
         String extension = null;
         String definedBy = null;
 
         for (int i = 1; i < args.length; i++) {
           if ("as".equals(args[i])) {
             i++;
             if (i >= args.length)
               throw new IOException(L.l("expecting Quercus class name after `{0}' in definition for class {1}", "as", className));
 
             phpClassName = args[i];
           }
           else if ("provides".equals(args[i])) {
             i++;
             if (i >= args.length)
               throw new IOException(L.l("expecting name of extension after `{0}' in definition for class {1}", "extension", className));
 
             extension = args[i];
           }
           else if ("definedBy".equals(args[i])) {
             i++;
             if (i >= args.length)
               throw new IOException(L.l("expecting name of class implementing JavaClassDef after `{0}' in definition for class {1}", "definedBy", className));
 
             definedBy = args[i];
           }
           else {
             throw new IOException(L.l("unknown token `{0}' in definition for class {1} ", args[i], className));
           }
         }
 
         if (phpClassName == null)
           phpClassName = className.substring(className.lastIndexOf('.') + 1);
 
 
         Class javaClassDefClass;
 
         if (definedBy != null) {
           javaClassDefClass = Class.forName(definedBy, false, loader);
         }
         else
           javaClassDefClass = null;
 
         introspectJavaClass(phpClassName, cl, extension, javaClassDefClass);
       } catch (Throwable e) {
         log.info("Failed loading " + className + "\n" + e.toString());
         log.log(Level.FINE, e.toString(), e);
       }
     }
   }
 
   /**
    * Introspects the module class for functions.
    *
    * @param name the php class name
    * @param type the class to introspect.
    * @param extension the extension provided by the class, or null
    * @param javaClassDefClass
    */
   private void introspectJavaClass(String name, Class type, String extension,
                                    Class javaClassDefClass)
     throws IllegalAccessException, InstantiationException, ConfigException,
            NoSuchMethodException, InvocationTargetException
   {
     if (log.isLoggable(Level.FINE))
       log.fine(L.l("Quercus loading class {0}", name));
 
     ModuleContext context = getLocalContext();
 
     if (type.isAnnotationPresent(ClassImplementation.class)) {
       if (javaClassDefClass != null)
         throw new UnimplementedException();
 
       ClassDef def = context.addClassImpl(name, type, extension);
 
       _staticClasses.put(name, def);
       _lowerStaticClasses.put(name.toLowerCase(), def);
     }
     else {
       JavaClassDef def = context.addClass(name, type,
 					  extension, javaClassDefClass);
 
       _javaClassWrappers.put(name, def);
       _lowerJavaClassWrappers.put(name.toLowerCase(), def);
 
       _staticClasses.put(name, def);
       _lowerStaticClasses.put(name.toLowerCase(), def);
     }
 
     if (extension != null)
       _extensionSet.add(extension);
   }
 
   /**
    * Introspects the module class for functions.
    *
    * @param name the php class name
    * @param type the class to introspect.
    * @param extension the extension provided by the class, or null
    */
   private void introspectJavaImplClass(String name,
                                        Class type,
                                        String extension)
     throws IllegalAccessException, InstantiationException, ConfigException
   {
     if (log.isLoggable(Level.FINEST)) {
       if (extension == null)
         log.finest(L.l("Quercus loading class {0} with type {1}", name, type.getName()));
       else
         log.finest(L.l("Quercus loading class {0} with type {1} providing extension {2}", name, type.getName(), extension));
     }
 
     ModuleContext context = getLocalContext();
 
     JavaImplClassDef def = context.addClassImpl(name, type, extension);
 
     _staticClasses.put(name, def);
     _lowerStaticClasses.put(name.toLowerCase(), def);
   }
 
   /**
    * Scans the classpath for META-INF/services/com.caucho.quercus.QuercusClass
    */
   private void initStaticClasses()
   {
     _stdClassDef = new InterpretedClassDef("stdClass", null, new String[0]);
     _stdClass = new QuercusClass(_stdClassDef, null);
 
     _staticClasses.put(_stdClass.getName(), _stdClassDef);
     _lowerStaticClasses.put(_stdClass.getName().toLowerCase(), _stdClassDef);
 
     /*
     InterpretedClassDef exn = new InterpretedClassDef("Exception",
 						      null,
 						      new String[0]);
 
     try {
       exn.setConstructor(new StaticFunction(_moduleContext,
 					    null,
 					    Quercus.class.getMethod("exnConstructor", new Class[] { Env.class, Value.class, String.class })));
     } catch (Exception e) {
       throw new QuercusException(e);
     }
     
     // QuercusClass exnCl = new QuercusClass(exn, null);
 
     _staticClasses.put(exn.getName(), exn);
     _lowerStaticClasses.put(exn.getName().toLowerCase(), exn);
     */
   }
 
   public void close()
   {
     _pageManager.close();
   }
 
   public static Value exnConstructor(Env env, Value obj, String msg)
   {
     if (obj != null) {
       obj.putField(env, "message", new StringValueImpl(msg));
     }
 
     return NullValue.NULL;
 
   }
 
   static class IncludeKey {
     private final String _include;
     private final String _includePath;
     private final Path _pwd;
     private final Path _scriptPwd;
 
     IncludeKey(String include, String includePath, Path pwd, Path scriptPwd)
     {
       _include = include;
       _includePath = includePath;
       _pwd = pwd;
       _scriptPwd = scriptPwd;
     }
 
     public int hashCode()
     {
       int hash = 37;
 
       hash = 65537 * hash + _include.hashCode();
       hash = 65537 * hash + _includePath.hashCode();
       hash = 65537 * hash + _pwd.hashCode();
       hash = 65537 * hash + _scriptPwd.hashCode();
 
       return hash;
     }
 
     public boolean equals(Object o)
     {
       if (! (o instanceof IncludeKey))
         return false;
 
       IncludeKey key = (IncludeKey) o;
 
       return (_include.equals(key._include) &&
               _includePath.equals(key._includePath) &&
               _pwd.equals(key._pwd) &&
               _scriptPwd.equals(key._scriptPwd));
     }
   }
 
   static {
     _superGlobals.add("GLOBALS");
     _superGlobals.add("_COOKIE");
     _superGlobals.add("_ENV");
     _superGlobals.add("_FILES");
     _superGlobals.add("_GET");
     _superGlobals.add("_POST");
     _superGlobals.add("_SERVER");
     _superGlobals.add("_SESSION");
     _superGlobals.add("_REQUEST");
   }
 }
 
