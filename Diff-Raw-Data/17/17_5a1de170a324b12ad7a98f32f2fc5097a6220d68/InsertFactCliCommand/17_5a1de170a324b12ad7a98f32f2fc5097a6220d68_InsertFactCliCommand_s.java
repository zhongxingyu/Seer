 package org.plugtree.drools.shell.commands;
 
 import jline.Completor;
 import jline.ConsoleReader;
 import jline.SimpleCompletor;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 import org.drools.command.Command;
 import org.mvel2.MVEL;
 import org.plugtree.drools.commands.DummyCommand;
 import org.plugtree.drools.commands.RulesByPackageCommand;
 import org.plugtree.drools.commands.RulesForPackageCommand;
 import org.plugtree.drools.shell.exceptions.HelpRequestedException;
 import org.plugtree.drools.shell.exceptions.UnknownArgumentException;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.*;
 
 import static java.lang.Class.*;
 
 /**
  * creation date: 2/20/11
  */
 public class InsertFactCliCommand extends CliCommandSupport {
 
     private ConsoleReader reader;
     private OptionParser parser;
     private OptionSpec<String> classNameOpt;
     private OptionSpec<Void> helpOpt;
 
     public InsertFactCliCommand(ConsoleReader reader) {
         this.reader = reader;
         parser = new OptionParser();
         classNameOpt = parser.acceptsAll(Arrays.asList(new String[]{"c", "className"}), "Object class to insert")
                 .withRequiredArg().ofType(String.class);
         helpOpt = parser.acceptsAll(Arrays.asList(new String[]{"?", "help", "h"}), "This help");
     }
 
     @Override
     public Completor getCompletor() {
         return null;
     }
 
     @Override
     public Command<?> getCommand(String... args) throws UnknownArgumentException, HelpRequestedException {
         OptionSet optionSet = parseArguments(parser, args);
         isPrintHelp(optionSet, helpOpt, parser);
         checkMandatoryOption(optionSet, classNameOpt);
 
         final String className = optionSet.valueOf(classNameOpt);
         List<String> fields = new ArrayList<String>();
         Class<?> clazz = null;
         try {
             clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException("Class " + className +" not found", e);
         }
 
         //remove parent completors
         List<Completor> completors = new ArrayList<Completor>(reader.getCompletors());
         for (Completor completor : completors){
             reader.removeCompletor(completor);
         }
 
         reader.addCompletor(new SimpleCompletor(getFieldNames(clazz.getDeclaredFields())));
 
         try {
             String line;
             String classPrompt = clazz.getSimpleName().toLowerCase();
             while ((line = reader.readLine(classPrompt + "> ")) != null) {
                 line = line.trim();
                 //FIXME regex to check simple field=[']value[']
                 if("".equals(line))
                     break;
                 fields.add(line);
             }
 
 
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
 
         //readd parent completors
         for (Completor completor : completors){
             reader.addCompletor(completor);
         }
 
        return new DummyCommand(MVEL.eval(createMvelExpression(className, fields)));
     }
 
     private String createMvelExpression(String className, List<String> fieldExpressions){
         StringBuilder expression = new StringBuilder();
         expression.append("with(new ").append(className).append("()){");
         for(String fieldExpression : fieldExpressions){
             expression.append(fieldExpression);
             expression.append(",");
         }
         if(!fieldExpressions.isEmpty())
             expression.deleteCharAt(expression.lastIndexOf(","));
         expression.append("}");
 
         return expression.toString();
     }
 
     private String[] getFieldNames(Field[] fields) {
         String[] fieldNames = new String[fields.length];
         for (int i = 0; i < fields.length ; i++){
             fieldNames[i] = fields[i].getName();
         }
         return fieldNames;
     }
 }
