 package dk.statsbiblioteket.newspaper.md5checker;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
 
 import java.io.File;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 public class MockupIteratorSuper extends MD5CheckerComponent {
     /**
      * Constructor matching super. Super requires a properties to be able to initialise the tree iterator, if needed.
      * If you do not need the tree iterator, ignore properties.
      *
      * You can use properties for your own stuff as well
      *
      * @param properties properties
      */
     public MockupIteratorSuper(Properties properties) {
         super(properties);
     }
 
     /**
      * We override this method to be able to inject our own tree iterator
      * @param batch the batch to iterate on
      * @return a tree iterator
      */
     @Override
     protected TreeIterator createIterator(Batch batch) {
        File dataDir = new File(Thread.currentThread().getContextClassLoader().getResource("scratch").getFile());
        File batchDir = new File(dataDir, batch.getFullID());
        return new TransformingIteratorForFileSystems(batchDir, Pattern.quote("."),".*\\.jp2$",".md5");
     }
 }
