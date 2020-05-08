 package net.intensicode.idea.scripting;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyShell;
 import net.intensicode.idea.system.OptionsFolder;
 import net.intensicode.idea.system.ScriptSupport;
 import net.intensicode.idea.system.SystemContext;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * TODO: Describe this!
  */
 public final class GroovyContext implements ScriptSupport
 {
     public GroovyContext( final SystemContext aSystemContext, final List<String> aClassPathEntries )
     {
         myFolder = aSystemContext.getOptionsFolder();
 
         final Binding binding = new Binding();
         binding.setVariable( "context", this );
         binding.setVariable( "currentDir", myFolder.getConfigurationFolder() );
 
         try
         {
             final ArrayList<URL> urls = new ArrayList<URL>();
             for ( final String entry : aClassPathEntries )
             {
                urls.add( new File( myFolder.getConfigurationFolder(), entry ).toURL() );
             }
 
             final URL[] urlArray = urls.toArray( new URL[urls.size()] );
             final ClassLoader parentLoader = getClass().getClassLoader();
             final ClassLoader chainedLoader = new URLClassLoader( urlArray, parentLoader );
 
             final GroovyClassLoader loader = new GroovyClassLoader( chainedLoader );
             loader.addClasspath( myFolder.getConfigurationFolder().getPath() );
 
             myShell = new GroovyShell( loader, binding );
         }
         catch ( final MalformedURLException e )
         {
             throw new ScriptingException( e );
         }
     }
 
     public final Object source( final String aScriptName ) throws FileNotFoundException
     {
         return myShell.evaluate( myFolder.streamFile( aScriptName ) );
     }
 
     public final File getCurrentDir()
     {
         return myFolder.getConfigurationFolder();
     }
 
     // ScriptSupport
 
     public final Object createObject( final String aScriptFileName, final Class aTargetClass ) throws IOException
     {
         final Object result = source( aScriptFileName );
         try
         {
             return aTargetClass.cast( result );
         }
         catch ( final Throwable t )
         {
             final ClassLoader loader = getClass().getClassLoader();
             final Class[] interfaces = new Class[]{ aTargetClass };
             final InvocationHandler handler = new GroovyInvocationHandler( result );
             return Proxy.newProxyInstance( loader, interfaces, handler );
         }
     }
 
 
 
     private final GroovyShell myShell;
 
     private final OptionsFolder myFolder;
 }
