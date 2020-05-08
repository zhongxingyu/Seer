 package Time_Log_Inserter.src.profiler;
 
 import java.io.*;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Opcodes;
 
 public class CustomClassFileTransformer implements ClassFileTransformer
 {
 
     public static Map< String, HashSet< String > > profiles;
 
     public static String currentClass;
 
     static
     {
         profiles = new HashMap< String, HashSet< String > >();
         String dir = System.getenv( "PROFILER_HOME" );
        System.out.println( dir );
         try
         {
             BufferedReader br = new BufferedReader( new FileReader( new File( dir + "\\classes.txt" ) ) );
             String line;
             while ( ( line = br.readLine() ) != null )
             {
                 String[] tokens = line.split( "\t" );
                 String classInfo = tokens[0];
                 String methodInfo = tokens[1];
                 HashSet< String > methods;
                 if ( profiles.containsKey( classInfo ) )
                 {
                     methods = profiles.get( classInfo );
                 }
                 else
                 {
                     methods = new HashSet< String >();
                 }
 
                 methods.add( methodInfo );
                 profiles.put( classInfo, methods );
             }
         }
         catch ( FileNotFoundException e )
         {
             System.out.println( "FileNotFoundException" + e );
         }
         catch ( IOException e )
         {
             System.out.println( "IOException" + e );
         }

     }
 
     @Override
     public byte[] transform( ClassLoader loader, String className, Class< ? > classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer ) throws IllegalClassFormatException
     {
         System.out.println( "Loading Class " + className + " with ASM 4" );
 
         currentClass = className;
 
        if ( profiles.containsKey( getOnlyClassName( className ) ) )
         {
             System.out.println( "Profiling - Class " + getOnlyClassName( className ) );
 
             ClassReader cr = new ClassReader( classfileBuffer );
 
             ClassWriter cw = new ClassWriter( cr, ClassWriter.COMPUTE_MAXS );
 
             MyClassVisitor myClassVisitor = new MyClassVisitor( Opcodes.ASM4, cw );
 
             cr.accept( myClassVisitor, Opcodes.ASM4 );
 
             return cw.toByteArray();
         }
         else
         {
            System.out.println( "Skipping - Class " + getOnlyClassName( className ) );
 
             return classfileBuffer;
         }
     }
 
     private String getOnlyClassName( String className )
     {
        if ( className.contains( "\\" ) )
         {
            return className.substring( className.lastIndexOf( "\\" ), className.length() );
         }
 
         return className;
 
     }
 
 }
