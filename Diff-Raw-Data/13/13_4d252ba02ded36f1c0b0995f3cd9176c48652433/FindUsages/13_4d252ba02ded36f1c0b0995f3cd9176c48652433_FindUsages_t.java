 package com.codingstory.polaris.cli.command;
 
 import com.codingstory.polaris.NoOpController;
 import com.codingstory.polaris.cli.Command;
 import com.codingstory.polaris.cli.Help;
 import com.codingstory.polaris.cli.Option;
 import com.codingstory.polaris.cli.Run;
 import com.codingstory.polaris.parser.ParserProtos.FileHandle;
 import com.codingstory.polaris.parser.ParserProtos.JumpTarget;
 import com.codingstory.polaris.parser.ParserProtos.Usage;
 import com.codingstory.polaris.parser.ParserProtos.Position;
 import com.codingstory.polaris.search.CodeSearchImpl;
 import com.codingstory.polaris.search.SearchProtos.GetTypeRequest;
 import com.codingstory.polaris.search.SearchProtos.GetTypeResponse;
import com.codingstory.polaris.search.SearchProtos.ListUsagesRequest;
import com.codingstory.polaris.search.SearchProtos.ListUsagesResponse;
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 import static com.codingstory.polaris.cli.CommandUtils.checkStatus;
 import static com.codingstory.polaris.cli.CommandUtils.die;
 
 @Command(name = "findusages")
 public class FindUsages {
     @Option(name = "index", shortName = "i", defaultValue = "index")
     public String index;
 
     @Run
     public void run(String[] args) throws IOException {
         if (args.length != 1) {
             die("Require exactly one class");
         }
 
         final String className = args[0];
         CodeSearchImpl searcher = new CodeSearchImpl(new File(index));
         try {
             GetTypeRequest getTypeReq = GetTypeRequest.newBuilder()
                     .setTypeName(className)
                     .build();
             GetTypeResponse getTypeResp = searcher.getType(NoOpController.getInstance(), getTypeReq);
             checkStatus(getTypeResp.getStatus());
            ListUsagesRequest listUsageReq = ListUsagesRequest.newBuilder()
                    .setKind(Usage.Kind.TYPE)
                    .setId(getTypeResp.getClassType().getHandle().getId())
                     .build();
            ListUsagesResponse listUsageResp = searcher.listUsages(NoOpController.getInstance(), listUsageReq);
             checkStatus(listUsageResp.getStatus());
             for (Usage usage : listUsageResp.getUsagesList()) {
                 if (usage.getKind() == Usage.Kind.TYPE) {
                     JumpTarget target = usage.getJumpTarget();
                     FileHandle file = target.getFile();
                     Position position = target.getSpan().getFrom();
                     System.out.println(usage.getType().getKind().name() + ": file #" + file.getId()
                             + " " + file.getPath() + " (" + position.getLine() + "," + position.getColumn() + ")");
                 }
             }
         } finally {
             IOUtils.closeQuietly(searcher);
         }
     }
 
     @Help
     public void help() {
         System.out.println("Usage:\n" +
                 "  polaris findusages <qualified-class> [--index=<dir>]\n" +
                 "\n" +
                 "Options:\n" +
                 HelpMessages.INDEX +
                 "\n");
     }
 }
