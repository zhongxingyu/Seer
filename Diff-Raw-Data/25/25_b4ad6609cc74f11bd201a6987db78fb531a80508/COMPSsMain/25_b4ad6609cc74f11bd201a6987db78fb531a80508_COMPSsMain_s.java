 package it.cnr.aquamaps;
 
 import org.github.scopt.OptionParser;
 import net.lag.configgy.Config;
 import com.google.inject.*;
 import scala.collection.Iterator;
 
 public class COMPSsMain {
     public static void main(String[] args) {
         Config conf = Main.loadConfiguration();
 
         if (!Main.parseArgs(args))
             return;
 
         Injector injector = Main.createInjector(conf);
         Partitioner partitioner = injector.getInstance(Partitioner.class);
        Generator generator = injector.getInstance(Generator.class);
        Emitter<HSPEC> emitter = injector.getInstance(Key.get(new TypeLiteral<Emitter<HSPEC>>() {}));

 
         Iterator<Partition> p = partitioner.partitions();
        while(p.hasNext())
            generator.computeInPartition(p.next());
         
 
         emitter.flush();
     }
}
