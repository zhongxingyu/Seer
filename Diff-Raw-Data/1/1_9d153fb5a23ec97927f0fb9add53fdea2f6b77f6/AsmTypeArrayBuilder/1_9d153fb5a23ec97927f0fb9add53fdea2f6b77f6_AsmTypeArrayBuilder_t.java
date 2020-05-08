 package net.sf.clirr.core.internal.asm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import net.sf.clirr.core.CheckerException;
 import net.sf.clirr.core.ClassFilter;
 import net.sf.clirr.core.ClassSelector;
 import net.sf.clirr.core.spi.JavaType;
 import net.sf.clirr.core.spi.TypeArrayBuilderSupport;
 
 public class AsmTypeArrayBuilder extends TypeArrayBuilderSupport
 {
 
 
     public AsmTypeArrayBuilder()
     {
     }
 
     public JavaType[] createClassSet(File[] jarFiles, ClassLoader thirdPartyClasses, ClassFilter classSelector) throws CheckerException
     {
         if (classSelector == null)
         {
             // create a class selector that selects all classes
             classSelector = new ClassSelector(ClassSelector.MODE_UNLESS);
         }
 
         ClassLoader classLoader = createClassLoader(jarFiles, thirdPartyClasses);
 
         Repository repository = new Repository(classLoader);
 
         List selected = new ArrayList();
 
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             ZipFile zip = null;
             try
             {
                 zip = new ZipFile(jarFile, ZipFile.OPEN_READ);
             }
             catch (IOException ex)
             {
                 throw new CheckerException(
                     "Cannot open " + jarFile + " for reading", ex);
             }
             Enumeration enumEntries = zip.entries();
             while (enumEntries.hasMoreElements())
             {
                 ZipEntry zipEntry = (ZipEntry) enumEntries.nextElement();
                 if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class"))
                 {
                     final AsmJavaType javaType = extractClass(repository, zipEntry, zip);
                     if (classSelector.isSelected(javaType))
                     {
                         selected.add(javaType);
                     }
                 }
             }
         }
 
         JavaType[] ret = new JavaType[selected.size()];
         selected.toArray(ret);
         return ret;
 
     }
     
     private AsmJavaType extractClass(
             Repository repository, ZipEntry zipEntry, ZipFile zip)
             throws CheckerException
     {
         InputStream is = null;
         try
         {
             is = zip.getInputStream(zipEntry);
             
             return repository.readJavaTypeFromStream(is);
         }
         catch (IOException ex)
         {
             throw new CheckerException(
                     "Cannot read " + zipEntry.getName() + " from " + zip.getName(),
                     ex);
         }
         finally
         {
             if (is != null)
             {
                 try
                 {
                     is.close();
                 }
                 catch (IOException ex)
                 {
                     throw new CheckerException("Cannot close " + zip.getName(), ex);
                 }
             }
         }
     }
 
 
 }
