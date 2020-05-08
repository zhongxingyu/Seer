 package org.pinion.core.preprocessor;
 
 import com.google.common.base.Optional;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.pinion.core.asset.Asset;
 import org.pinion.core.files.FileResolver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DirectiveProcessor implements Preprocessor {
 
   private final String HEADER_PATTERN = "\\A( (?m:\\s*)((/\\*(?s:.*?)\\*/)|(// .* \\n?)+))+";
   private final String DIRECTIVE_PATTERN = "^ \\W* = \\s* (\\w+.*?) (\\*/)? $ ";
   private final Pattern headerPattern = Pattern.compile(HEADER_PATTERN, Pattern.COMMENTS);
   private final Pattern directivePattern = Pattern.compile(DIRECTIVE_PATTERN, Pattern.COMMENTS);
 
   @Override
   public String run(final String input, final Asset asset) throws FileSystemException {
     //Look for a header to process
     Optional<String> header = findHeader(input);
     if (!header.isPresent())
       return input;
 
     //Search the header for directives to process
     List<Directive> directives = findDirectives(header.get());
 
     //Process the directives...currently only have one directive so either this needs to be
     //simplified or abstracted
     List<String> requiredPaths = new ArrayList<>();
     for (Directive d: directives) {
       if (!d.args.isPresent())
         continue;
       for (String arg: d.args.get()) {
         List<FileObject> files = FileResolver.resolveGlob(asset.fsPath, arg.replace("\"", ""));
         for (FileObject file : files) {
          requiredPaths.add(file.getName().getBaseName());
         }
       }
     }
 
     //push the found dependencies back into the asset
     asset.getDependencyPaths().clear();
     asset.getDependencyPaths().addAll(requiredPaths);
 
     //TODO remove the directive lines from the file and return the rest
 
     return input;
   }
 
   private Optional<String> findHeader(final String input) {
     Matcher matcher = headerPattern.matcher(input);
     if (matcher.find()) {
       return Optional.of(matcher.group());
     } else {
       return Optional.absent();
     }
   }
 
   private List<Directive> findDirectives(final String input) {
     List<Directive> result = new ArrayList<>();
     String [] lines = input.split("\n");
     int lineNumber = 1;
     for (String line: lines) {
       Matcher matcher = directivePattern.matcher(line);
       if (matcher.find()) {
 
         String[] command = matcher.group(1).split(" ");
 
         if (command.length < 1)  //no command found, shouldn't happen
           throw new IllegalStateException("Fail");
 
         String directive = command[0];
         Optional<String[]> args = Optional.absent();
         if (command.length > 1) { //handle arguments
           args = Optional.of(Arrays.copyOfRange(command, 1, command.length));
         }
 
         result.add(new Directive(lineNumber, directive, args));
       }
       lineNumber++;
     }
     return result;
   }
 
   private class Directive {
     int lineNumber;
     String directive;
     Optional<String[]> args;
 
     public Directive (int lineNumber, String directive, Optional<String[]> args) {
       this.lineNumber = lineNumber;
       this.directive = directive;
       this.args = args;
     }
   }
 
 }
