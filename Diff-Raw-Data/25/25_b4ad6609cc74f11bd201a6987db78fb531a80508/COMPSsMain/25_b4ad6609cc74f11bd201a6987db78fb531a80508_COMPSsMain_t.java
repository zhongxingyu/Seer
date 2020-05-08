 package it.cnr.aquamaps;
 
 import org.github.scopt.OptionParser;
 import net.lag.configgy.Config;
 import com.google.inject.*;
 import scala.collection.Iterator;
import scala.xml.XML;
 
 public class COMPSsMain {
     public static void main(String[] args) {
         Config conf = Main.loadConfiguration();
 
         if (!Main.parseArgs(args))
             return;
 
         Injector injector = Main.createInjector(conf);
         Partitioner partitioner = injector.getInstance(Partitioner.class);
        COMPSsGenerator generator = injector.getInstance(COMPSsGenerator.class);
        COMPSsCollectorEmitter<HSPEC> emitter = injector.getInstance(Key.get(new TypeLiteral<COMPSsCollectorEmitter<HSPEC>>() {}));
 
         Iterator<Partition> p = partitioner.partitions();
        while(p.hasNext()) {
            String tmpFile = generator.mkTmp(".xml");
            String outputFile = generator.mkTmp(".csv.gz");

            XML.save(tmpFile, new P2XML(p.next()).toXml(), "UTF-8", false, null);

            StaticFileParamsGenerator.staticDelegate(tmpFile, outputFile);

            /*! Keep it for later */
            emitter.add(outputFile);
        }
         
 
         emitter.flush();
     }
}
