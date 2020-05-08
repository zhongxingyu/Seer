 package uk.co.benjiweber.puppetsafe.core;
 
 import uk.co.benjiweber.puppetsafe.serializer.ClassSerializer;
 import uk.co.benjiweber.puppetsafe.uk.co.benjiweber.puppetsafe.builder.ABSENT;
 import uk.co.benjiweber.puppetsafe.uk.co.benjiweber.puppetsafe.builder.PRESENT;
 
import java.util.*;

 public class File implements Puppetable {
 
     public final String source;
     public final String target;
     public final Ensure ensure;
     public final Set<Puppetable> dependencies;
 
    @Override
     public void serialize(ClassSerializer serializer, StringBuilder builder) {
         serializer.serialize(this, builder);
     }
 
    @Override
     public void serializeAsDependency(ClassSerializer serializer, StringBuilder builder) {
         serializer.serializeAsDependency(this, builder);
     }
 
     public static enum Ensure {
         directory,
         link,
         file
     }
 
     public File(FileBuilder<PRESENT, PRESENT> builder) {
         this(builder.source, builder.target, builder.ensure, builder.dependencies);
     }
 
     private File(String source, String target, Ensure ensure, Set<Puppetable> dependencies) {
         this.source = source;
         this.target = target;
         this.ensure = ensure;
         this.dependencies = dependencies;
     }
 
     public static class FileBuilder<SOURCE, TARGET> {
         String source;
         String target;
         private Ensure ensure = Ensure.file;
         private Set<Puppetable> dependencies = new LinkedHashSet<Puppetable>();
 
         public FileBuilder() {}
 
         public FileBuilder(String source, String target, Ensure ensure, Set<Puppetable> dependencies) {
             this.source = source;
             this.target = target;
             this.ensure = ensure;
             this.dependencies = dependencies;
         }
 
         public FileBuilder<SOURCE, PRESENT> target(String target) {
             return new FileBuilder<SOURCE, PRESENT>(this.source, target, this.ensure, this.dependencies);
         }
 
         public FileBuilder<PRESENT, TARGET> source(String source) {
             return new FileBuilder<PRESENT, TARGET>(source, this.target, this.ensure, this.dependencies);
         }
 
         public static FileBuilder<ABSENT, ABSENT> with() {
             return new FileBuilder<ABSENT, ABSENT>();
         }
 
         // Links and Directories do not need a Source
         public FileBuilder<PRESENT, TARGET> ensure(Ensure ensure) {
             return new FileBuilder<PRESENT, TARGET>(this.source, this.target, ensure, this.dependencies);
         }
 
         public FileBuilder<SOURCE, TARGET> requires(Puppetable puppetable) {
             LinkedHashSet<Puppetable> newDependencies = new LinkedHashSet<Puppetable>(dependencies);
             newDependencies.add(puppetable);
             return new FileBuilder<SOURCE, TARGET>(this.source, this.target, this.ensure, newDependencies);
         }
 
     }
 
     public static File file(FileBuilder<PRESENT, PRESENT> spec) {
         return new File(spec);
     }
 
 
 }
