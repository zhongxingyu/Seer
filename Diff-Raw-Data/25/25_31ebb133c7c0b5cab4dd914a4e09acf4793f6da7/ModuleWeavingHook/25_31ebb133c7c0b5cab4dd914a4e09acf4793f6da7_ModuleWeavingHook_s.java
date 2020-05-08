 package org.mosaic.modules.impl;
 
 import java.io.*;
 import java.lang.annotation.Annotation;
 import java.nio.file.*;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import javassist.*;
 import javassist.bytecode.AccessFlag;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.modules.Component;
 import org.mosaic.modules.Service;
 import org.mosaic.modules.spi.MethodAlreadyRegisteredException;
 import org.mosaic.modules.spi.MethodCache;
 import org.mosaic.modules.spi.MethodInterceptor;
 import org.mosaic.modules.spi.ModulesSpi;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.hooks.weaving.WeavingException;
 import org.osgi.framework.hooks.weaving.WeavingHook;
 import org.osgi.framework.hooks.weaving.WovenClass;
 
 import static java.lang.String.format;
 import static java.nio.file.Files.*;
 import static javassist.Modifier.*;
 import static javassist.bytecode.AccessFlag.BRIDGE;
 import static javassist.bytecode.AccessFlag.SYNTHETIC;
 
 /**
  * @author arik
  */
 final class ModuleWeavingHook implements WeavingHook
 {
     private final long bundleId;
 
     @Nonnull
     private final Path weavingCacheDir;
 
     ModuleWeavingHook( @Nonnull BundleContext bundleContext )
     {
         this.bundleId = bundleContext.getBundle().getBundleId();
 
         String workDirLocation = bundleContext.getProperty( "mosaic.home.work" );
         if( workDirLocation == null )
         {
             throw new IllegalStateException( "could not discover Mosaic work directory from bundle property 'mosaic.home.work'" );
         }
 
         Path workDir = Paths.get( workDirLocation );
         this.weavingCacheDir = workDir.resolve( "weaving" );
     }
 
     @Override
     public synchronized void weave( @Nonnull WovenClass wovenClass )
     {
         Bundle bundle = wovenClass.getBundleWiring().getBundle();
         if( bundle.getBundleId() <= this.bundleId )
         {
             return;
         }
 
         String bundleLocation = bundle.getLocation();
         if( bundleLocation.startsWith( "file:" ) )
         {
             bundleLocation = bundleLocation.substring( "file:".length() );
         }
 
         Path bundleFile = Paths.get( bundleLocation );
         long bundleFileModTime;
         try
         {
             bundleFileModTime = getLastModifiedTime( bundleFile ).toMillis();
         }
         catch( IOException e )
         {
             throw new WeavingException( "could not read modification time for bundle " + bundle.getSymbolicName() + "[" + bundleId + "] at '" + bundleFile + "' while weaving class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
         }
 
         synchronized( this )
         {
             Path nextIdFile = this.weavingCacheDir.resolve( "id" );
             if( notExists( nextIdFile ) )
             {
                 try
                 {
                     deletePath( this.weavingCacheDir );
                     createDirectories( this.weavingCacheDir );
                 }
                 catch( IOException e )
                 {
                     throw new WeavingException( "could not clean cache dir (due to missing next-id file)", e );
                 }
 
                 try
                 {
                     Files.write( nextIdFile, "0".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE );
                 }
                 catch( IOException e )
                 {
                     throw new WeavingException( "cannot create next-id file", e );
                 }
             }
         }
 
         Path bundleCacheDir = this.weavingCacheDir.resolve( bundle.getSymbolicName() + "-" + bundle.getVersion() + "/" + bundleFileModTime );
         Path bytesCacheFile = bundleCacheDir.resolve( wovenClass.getClassName() + ".class.bytes" );
         if( exists( bytesCacheFile ) )
         {
             try
             {
                 byte[] readBytes = readAllBytes( bytesCacheFile );
                 if( readBytes.length > 0 )
                 {
                     DataInputStream dataInputStream = new DataInputStream( new ByteArrayInputStream( readBytes ) );
                     int methodCount = dataInputStream.readInt();
                     for( int i = 0; i < methodCount; i++ )
                     {
                         long id = dataInputStream.readLong();
                         String methodName = dataInputStream.readUTF();
 
                         int parameterCount = dataInputStream.readInt();
                         String[] parameterNames = new String[ parameterCount ];
                         for( int j = 0; j < parameterCount; j++ )
                         {
                             parameterNames[ j ] = dataInputStream.readUTF();
                         }
 
                         MethodCache.getInstance().registerMethod( id,
                                                                   bundle.getBundleId(),
                                                                   wovenClass.getClassName(),
                                                                   methodName,
                                                                   parameterNames );
                     }
 
                     int bytecodeLength = dataInputStream.readInt();
                     byte[] bytecode = new byte[ bytecodeLength ];
                     if( dataInputStream.read( bytecode ) != bytecodeLength )
                     {
                         throw new WeavingException( "illegal format of bytecode cache at '" + bytesCacheFile + "'" );
                     }
                    wovenClass.setBytes( bytecode );
                 }
             }
             catch( MethodAlreadyRegisteredException e )
             {
                 throw new WeavingException( "could not register all weaved methods for '" + wovenClass.getClassName() + "'", e );
             }
             catch( Throwable e )
             {
                 throw new WeavingException( "could not read cached bytecode for class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
             }
         }
         else
         {
            ensureImported( wovenClass, ModulesSpi.class.getPackage().getName() );

             CtClass ctClass = loadConcreteClass( wovenClass );
             if( ctClass != null )
             {
                 weaveFieldInjections( wovenClass, ctClass );
                 weaveNonnullChecks( ctClass );
                 Collection<InterceptedMethodInfo> interceptedMethodInfos = weaveMethodInterception( bundle, ctClass );
 
                 if( ctClass.isModified() )
                 {
                     byte[] bytes;
                     try
                     {
                         bytes = ctClass.toBytecode();
                         wovenClass.setBytes( bytes );
                     }
                     catch( Throwable e )
                     {
                         throw new WeavingException( "could not compile class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
                     }
 
                     try
                     {
                         createDirectories( bytesCacheFile.getParent() );
                         try( OutputStream outputStream = Files.newOutputStream( bytesCacheFile ) )
                         {
                             DataOutputStream dataOutputStream = new DataOutputStream( outputStream );
                             dataOutputStream.writeInt( interceptedMethodInfos.size() );
                             for( InterceptedMethodInfo interceptedMethodInfo : interceptedMethodInfos )
                             {
                                 dataOutputStream.writeLong( interceptedMethodInfo.id );
                                 dataOutputStream.writeUTF( interceptedMethodInfo.methodName );
                                 dataOutputStream.writeInt( interceptedMethodInfo.paramterTypeNames.length );
                                 for( String paramterTypeName : interceptedMethodInfo.paramterTypeNames )
                                 {
                                     dataOutputStream.writeUTF( paramterTypeName );
                                 }
                             }
                             dataOutputStream.writeInt( bytes.length );
                             dataOutputStream.write( bytes );
                             dataOutputStream.flush();
                         }
                     }
                     catch( Throwable e )
                     {
                         throw new WeavingException( "could not write cached bytecode for class '" + wovenClass.getClassName() + "' into file '" + bytesCacheFile + "': " + e.getMessage(), e );
                     }
                 }
             }
         }
     }
 
     private void weaveFieldInjections( @Nonnull WovenClass wovenClass, @Nonnull CtClass ctClass )
     {
         // list of constructors that should be weaved with our modifications
         try
         {
             Collection<CtConstructor> superCallingConstructors = getSuperCallingConstructors( ctClass );
             for( CtField field : ctClass.getDeclaredFields() )
             {
                 int modifiers = field.getModifiers();
                 if( !isStatic( modifiers ) && !isFinal( modifiers ) )
                 {
                     if( field.hasAnnotation( Component.class ) || field.hasAnnotation( Service.class ) )
                     {
                         addBeforeBody(
                                 superCallingConstructors,
                                 process( "this.%s = (%s) ModulesSpi.getValueForField( %dl, %s.class, \"%s\" );",
                                          field.getName(),
                                          field.getType().getName(),
                                          wovenClass.getBundleWiring().getBundle().getBundleId(),
                                          ctClass.getName(),
                                          field.getName() ) );
                     }
                 }
             }
         }
         catch( Throwable e )
         {
             throw new WeavingException( "could not weave fields of '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
         }
     }
 
     private void weaveNonnullChecks( @Nonnull CtClass ctClass )
     {
         try
         {
             for( CtBehavior behavior : ctClass.getDeclaredBehaviors() )
             {
                 if( isAbstract( behavior.getModifiers() ) || isNative( behavior.getModifiers() ) )
                 {
                     continue;
                 }
 
                 // weave checks for @Nonnull parameters
                 Object[][] availableParameterAnnotations = behavior.getAvailableParameterAnnotations();
                 for( int i = 0; i < availableParameterAnnotations.length; i++ )
                 {
                     for( Object annotationObject : availableParameterAnnotations[ i ] )
                     {
                         Annotation annotation = ( Annotation ) annotationObject;
                         if( annotation.annotationType().equals( Nonnull.class ) )
                         {
                             behavior.insertBefore(
                                     process( "if( $%d == null )                                                                                                     \n" +
                                              "{                                                                                                                     \n" +
                                              "  throw new NullPointerException( \"Method parameter %d of method '%s' is null, but is annotated with @Nonnull\" );   \n" +
                                              "}                                                                                                                     \n",
                                              i + 1, i, behavior.getLongName()
                                     )
                             );
                         }
                     }
                 }
 
                 // weave check for return type of @Nonnull methods
                 if( behavior instanceof CtMethod )
                 {
                     CtMethod method = ( CtMethod ) behavior;
                     if( method.hasAnnotation( Nonnull.class ) && !method.getReturnType().getName().equals( Void.class.getName() ) )
                     {
                         method.insertAfter( process(
                                 "if( $_ == null )                                                                                       \n" +
                                 "{                                                                                                      \n" +
                                 "   throw new NullPointerException( \"Method '%s' returned null, but is annotated with @Nonnull\" );    \n" +
                                 "}                                                                                                      \n",
                                 method.getLongName()
                         ) );
                     }
                 }
             }
         }
         catch( Throwable e )
         {
             throw new WeavingException( "could not weave @Nonnull checks for '" + ctClass.getName() + "': " + e.getMessage(), e );
         }
     }
 
     @Nonnull
     private Collection<InterceptedMethodInfo> weaveMethodInterception( @Nonnull Bundle bundle,
                                                                        @Nonnull CtClass ctClass )
     {
         try
         {
             Collection<InterceptedMethodInfo> interceptedMethodInfos = null;
             if( !isSubtypeOf( ctClass, MethodInterceptor.class.getName() ) )
             {
                 for( CtMethod method : ctClass.getDeclaredMethods() )
                 {
                     int acc = AccessFlag.of( method.getModifiers() );
                     if( ( acc & BRIDGE ) == 0 && ( acc & SYNTHETIC ) == 0 )
                     {
                         int modifiers = method.getModifiers();
                         if( !isAbstract( modifiers ) && !isNative( modifiers ) )
                         {
                             if( interceptedMethodInfos == null )
                             {
                                 interceptedMethodInfos = new LinkedList<>();
                             }
                             interceptedMethodInfos.add( weaveMethod( bundle, method ) );
                         }
                     }
                 }
             }
             return interceptedMethodInfos == null ? Collections.<InterceptedMethodInfo>emptyList() : interceptedMethodInfos;
         }
         catch( Throwable e )
         {
             throw new WeavingException( "could not weave method interception for '" + ctClass.getName() + "': " + e.getMessage(), e );
         }
     }
 
     @Nonnull
     private InterceptedMethodInfo weaveMethod( @Nonnull Bundle bundle, @Nonnull CtMethod method )
     {
         try
         {
             // generate method id
             long id;
             synchronized( this )
             {
                 Path nextIdFile = this.weavingCacheDir.resolve( "id" );
                 try
                 {
                     id = Long.parseLong( new String( readAllBytes( nextIdFile ) ) );
                     write( nextIdFile, ( ( id + 1 ) + "" ).getBytes(), StandardOpenOption.WRITE );
                 }
                 catch( IOException e )
                 {
                     throw new WeavingException( "cannot update next-id file", e );
                 }
             }
 
             String[] methodParameterTypeNames = getMethodParameterTypeNames( method );
             MethodCache.getInstance().registerMethod( id,
                                                       bundle.getBundleId(),
                                                       method.getDeclaringClass().getName(),
                                                       method.getName(),
                                                       methodParameterTypeNames );
             InterceptedMethodInfo interceptedMethodInfo = new InterceptedMethodInfo( id, method.getName(), methodParameterTypeNames );
 
             // add code that invokes the 'after' interception
             method.insertAfter(
                     process( getReturnStatement( method.getReturnType(), "ModulesSpi.afterSuccessfulInvocation( ($w)$_ )" ) ),
                     false
             );
 
             // add code that catches exceptions
             method.addCatch(
                     process( "{" +
                              "   " + getReturnStatement( method.getReturnType(), "ModulesSpi.afterThrowable( $e )" ) +
                              "   throw $e;" +
                              "}" ),
                     method.getDeclaringClass().getClassPool().get( Throwable.class.getName() ),
                     "$e"
             );
 
             // add code that invokes the 'before' interception
             method.insertBefore( process(
                     "if( !ModulesSpi.beforeInvocation( %s, %s, $args ) ) " +
                     "{" +
                     "   %s;" +
                     "}",
                     id + "l",
                     isStatic( method.getModifiers() ) ? "null" : "this",
                     getReturnStatement( method.getReturnType(), "ModulesSpi.afterAbortedInvocation()" ) )
             );
 
             // add code that invokes the 'after' interception
             method.insertAfter( process( "ModulesSpi.cleanup( %dl );", id ), true );
             return interceptedMethodInfo;
         }
         catch( Throwable e )
         {
             throw new WeavingException( "could not weave method '" + method.getLongName() + "' of '" + method.getDeclaringClass().getName() + "': " + e.getMessage(), e );
         }
     }
 
     @Nullable
     private CtClass loadConcreteClass( @Nonnull WovenClass wovenClass )
     {
         try
         {
             ClassPool classPool = new ClassPool( false );
             classPool.appendSystemPath();
             classPool.appendClassPath( new LoaderClassPath( ModuleWeavingHook.class.getClassLoader() ) );
             classPool.appendClassPath( new LoaderClassPath( wovenClass.getBundleWiring().getClassLoader() ) );
 
             CtClass ctClass = classPool.makeClass( new ByteArrayInputStream( wovenClass.getBytes() ) );
             if( ctClass.isArray() || ctClass.isPrimitive() || ctClass.isAnnotation() || ctClass.isEnum() || ctClass.isInterface() )
             {
                 return null;
             }
             else
             {
                 return ctClass;
             }
         }
         catch( Throwable e )
         {
             throw new WeavingException( "could not read class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
         }
     }
 
     @Nonnull
     private String getReturnStatement( @Nonnull CtClass returnType, @Nonnull String valueStmt )
     {
         switch( returnType.getName() )
         {
             case "void":
                 return valueStmt + ";";
             case "boolean":
                 return format( "return ( (Boolean) %s ).booleanValue();", valueStmt );
             case "byte":
                 return format( "return ( (Number) %s ).byteValue();", valueStmt );
             case "char":
                 return format( "return ( (Character) %s ).charValue();", valueStmt );
             case "double":
                 return format( "return ( (Number) %s ).doubleValue();", valueStmt );
             case "float":
                 return format( "return ( (Number) %s ).floatValue();", valueStmt );
             case "int":
                 return format( "return ( (Number) %s ).intValue();", valueStmt );
             case "long":
                 return format( "return ( (Number) %s ).longValue();", valueStmt );
             case "short":
                 return format( "return ( (Number) %s ).shortValue();", valueStmt );
             default:
                 return format( "return (%s) %s;", returnType.getName(), valueStmt );
         }
     }
 
     @Nonnull
     private String[] getMethodParameterTypeNames( @Nonnull CtMethod method )
             throws NotFoundException, ClassNotFoundException
     {
         CtClass[] ctParameterTypes = method.getParameterTypes();
         String[] parameterTypeNames = new String[ ctParameterTypes.length ];
         for( int i = 0; i < ctParameterTypes.length; i++ )
         {
             parameterTypeNames[ i ] = ctParameterTypes[ i ].getName();
         }
         return parameterTypeNames;
     }
 
     @Nonnull
     private Collection<CtConstructor> getSuperCallingConstructors( @Nonnull CtClass ctClass )
             throws CannotCompileException
     {
         List<CtConstructor> initializers = new LinkedList<>();
         CtConstructor[] declaredConstructors = ctClass.getDeclaredConstructors();
         for( CtConstructor ctor : declaredConstructors )
         {
             if( ctor.callsSuper() )
             {
                 initializers.add( ctor );
             }
         }
         return initializers;
     }
 
     private boolean isSubtypeOf( @Nonnull CtClass type, @Nonnull String superTypeName ) throws NotFoundException
     {
         if( type.getName().equals( superTypeName ) )
         {
             return true;
         }
 
         for( String intercaceName : type.getClassFile2().getInterfaces() )
         {
             if( intercaceName.equals( MethodInterceptor.class.getName() )
                 || isSubtypeOf( type.getClassPool().get( intercaceName ), superTypeName ) )
             {
                 return true;
             }
         }
 
         String supername = type.getClassFile2().getSuperclass();
         return supername != null && isSubtypeOf( type.getClassPool().get( supername ), superTypeName );
     }
 
     private void addBeforeBody( @Nonnull Collection<CtConstructor> ctors, @Nonnull String statement )
             throws CannotCompileException
     {
         for( CtConstructor constructor : ctors )
         {
             constructor.insertBeforeBody( statement );
         }
     }
 
     @Nonnull
     private String process( @Nonnull String code, @Nonnull Object... args )
     {
         return String.format( code, args ).replace( ModulesSpi.class.getSimpleName(), ModulesSpi.class.getName() );
     }
 
     private void ensureImported( @Nonnull WovenClass wovenClass, @Nonnull String... packageNames )
     {
         for( String packageName : packageNames )
         {
             if( !wovenClass.getDynamicImports().contains( packageName ) )
             {
                 wovenClass.getDynamicImports().add( packageName );
             }
         }
     }
 
     private void deletePath( @Nonnull Path path ) throws IOException
     {
         if( Files.isDirectory( path ) )
         {
             Files.walkFileTree( path, new SimpleFileVisitor<Path>()
             {
                 @Nonnull
                 @Override
                 public FileVisitResult visitFile( @Nonnull Path file, @Nonnull BasicFileAttributes attrs )
                         throws IOException
                 {
                     Files.delete( file );
                     return FileVisitResult.CONTINUE;
                 }
 
                 @Nonnull
                 @Override
                 public FileVisitResult postVisitDirectory( @Nonnull Path dir, @Nullable IOException exc )
                         throws IOException
                 {
                     if( exc != null )
                     {
                         throw exc;
                     }
                     Files.delete( dir );
                     return FileVisitResult.CONTINUE;
                 }
             } );
         }
         else if( exists( path ) )
         {
             Files.delete( path );
         }
     }
 
     private class InterceptedMethodInfo
     {
         private final long id;
 
         @Nonnull
         private final String methodName;
 
         @Nonnull
         private final String[] paramterTypeNames;
 
         private InterceptedMethodInfo( long id, @Nonnull String methodName, @Nonnull String[] paramterTypeNames )
         {
             this.id = id;
             this.methodName = methodName;
             this.paramterTypeNames = paramterTypeNames;
         }
     }
 }
