 package com.bookofbrilliantthings.mustache4j;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.nio.charset.Charset;
 
 public class JarLoader
     implements MustacheLoader
 {
     private final ClassLoader classLoader;
     private final Charset charset;
 
     public JarLoader(ClassLoader classLoader, Charset charset)
     {
         this.classLoader = classLoader;
         this.charset = charset;
     }
 
     @Override
     public MustacheEdition load(MustacheServices services, String name, Class<?> forClass)
             throws MustacheParserException
     {
         final InputStream inputStream = classLoader.getResourceAsStream(name);
        if (inputStream == null)
            throw new MustacheParserException("couldn't open class loader resource stream \"" + name + "\"");
         final Reader reader = new InputStreamReader(inputStream, charset);
         final MustacheRenderer renderer = Mustache.compile(services, reader, forClass);
         return new MustacheEdition(renderer);
     }
 }
