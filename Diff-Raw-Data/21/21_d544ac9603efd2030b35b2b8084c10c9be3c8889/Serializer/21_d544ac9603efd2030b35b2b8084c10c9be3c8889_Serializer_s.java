 package net.contextfw.web.commons.cloud.serializer;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.zip.Deflater;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.InflaterInputStream;
 
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.development.DevelopmentModeListener;
 import net.contextfw.web.application.development.DevelopmentTools;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Singleton;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.mapper.MapperWrapper;
 
 @Singleton
 public class Serializer implements DevelopmentModeListener {
 
     private ThreadLocal<XStream> xstream = getNewXStream();
     
     private final Injector injector;
     
     private KeyConverter keyConverter = new KeyConverter();
     
     private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     
     @Inject
     public Serializer(final Injector injector, DevelopmentTools developmentTools) {
         this.injector = injector;
         developmentTools.addListener(this);
     }
     
     public byte[] serialize(Object obj) {
         ByteArrayOutputStream out = null;
         DeflaterOutputStream dout = null;
         try {
            Deflater d = new Deflater();
            out = new ByteArrayOutputStream();
            dout = new DeflaterOutputStream(out, d);
             dout.write(xstream.get().toXML(obj).getBytes("UTF-8"));
            return out.toByteArray();
         } catch (IOException e) {
            throw new WebApplicationException(e);
        } finally {
            close(out);
             close(dout);
         }
     }
     
     private void close(OutputStream stream) {
         try {
             stream.close();
         } catch (IOException e) {
             // Just ignore
         }
     }
     
     public Object unserialize(byte[] serialized) {
         InputStream xml = new InflaterInputStream(
             new ByteArrayInputStream(serialized));
         return xstream.get().fromXML(xml);
     }
 
     private ThreadLocal<XStream> getNewXStream() {
         return new ThreadLocal<XStream>() {
             @Override
             protected XStream initialValue() {
                 XStream rv = new XStream() {
                     @Override
                     protected MapperWrapper wrapMapper(MapperWrapper next) {
                         return new DependencyMapper(next, injector, classLoader);
                     }
                 };
                 rv.registerConverter(keyConverter);
                 rv.setClassLoader(classLoader);
                 return rv;
             }
         };
     }
 
     @Override
     public void classesReloaded(ClassLoader classLoader) {
         this.classLoader = classLoader;
         keyConverter.setClassLoader(classLoader);
         xstream = getNewXStream();
     }
 
     @Override
     public void resourcesReloaded() {
     }
 }
