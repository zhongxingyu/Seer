 package uk.ac.susx.mlcl.erl.tac.cmd;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.internal.Lists;
 import nu.xom.ParsingException;
 import uk.ac.susx.mlcl.erl.linker.Linker;
 import uk.ac.susx.mlcl.erl.tac.NilLinker;
 import uk.ac.susx.mlcl.erl.tac.io.LinkIO;
 import uk.ac.susx.mlcl.erl.tac.io.QueryIO;
 import uk.ac.susx.mlcl.erl.tac.io.Tac2012LinkIO;
 import uk.ac.susx.mlcl.erl.tac.queries.Link;
 import uk.ac.susx.mlcl.erl.tac.queries.Query;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hiam20
  * Date: 01/08/2013
  * Time: 16:33
  * To change this template use File | Settings | File Templates.
  */
 public class LinkQueries implements Runnable {
 
     /**
      *
      */
    private final Linker linker = new NilLinker();
     /**
      *
      */
     @Parameter(names = {"-i"}, required = true, description = "Input queries file")
     File srcQueries;
     /**
      *
      */
     @Parameter(names = {"-o"}, required = true, description = "Output links file")
     File dstLinks;
 
     public static void main(String[] args) {
 
         ConvertKB instance = new ConvertKB();
 
         final JCommander jc = new JCommander();
         jc.setProgramName("nel");
         jc.addObject(instance);
 
         jc.parse(args);
 
         instance.run();
 
     }
 
     @Override
     public void run() {
 
         try {
             runWithExceptions();
         } catch (Exception t) {
             if (t instanceof RuntimeException)
                 throw (RuntimeException) t;
             else throw new RuntimeException(t);
         }
     }
 
     protected void runWithExceptions() throws IOException, ParsingException {
 
         final QueryIO qio = QueryIO.detectFormat(srcQueries);
         final LinkIO lio = new Tac2012LinkIO();
 
         List<Link> links = Lists.newArrayList();
         for (Query query : qio.readAll(srcQueries)) {
             links.add(linker.link(query));
         }
         lio.writeAll(dstLinks, links);
 
     }
 
 }
