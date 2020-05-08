 package ch.raffael.util.cli;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.google.common.base.Defaults;
 import com.google.common.base.Throwables;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.IllegalAnnotationError;
 import org.kohsuke.args4j.Option;
 import org.kohsuke.args4j.spi.OptionHandler;
 import org.kohsuke.args4j.spi.Setter;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class ReflectionCommandHandler implements CommandHandler {
 
     private final Object instance;
     private final Method method;
     private final String description;
     private final Class[] paramTypes;
     private final int outIndex;
     private final Object[] defs;
 
     public ReflectionCommandHandler(Object instance, Method method, String description) {
         try {
             this.instance = instance;
             this.method = method;
             description = description != null ? description.trim() : null;
             if ( description == null || description.isEmpty() ) {
                 this.description = "(no description)";
             }
             else {
                 this.description = description;
             }
             paramTypes = method.getParameterTypes();
             defs = new Object[paramTypes.length];
             Annotation[][] annotations = method.getParameterAnnotations();
             outIndex = getOutIndex(method, paramTypes);
             for ( int i = 0; i < paramTypes.length; i++ ) {
                 Option option = null;
                 Argument argument = null;
                 for ( Annotation a : annotations[i] ) {
                     if ( a instanceof Option ) {
                         option = (Option)a;
                     }
                     else if ( a instanceof Argument ) {
                         argument = (Argument)a;
                     }
                 }
                 if ( option != null && argument != null ) {
                     throw illegalMethod(method, "Annotated with both @Option and @Argument");
                 }
                 if ( i == outIndex ) {
                     if ( option != null || argument != null ) {
                         throw illegalMethod(method, "Output parameter annotated with @Option or @Argument");
                     }
                     continue;
                 }
                 if ( option == null && argument == null ) {
                     throw illegalMethod(method, "Non-annotated parameter");
                 }
                 else if ( option != null ) {
                     defs[i] = new ParamOption(option, paramTypes[i].isArray());
                 }
                 else {
                     defs[i] = new ParamArgument(argument, paramTypes[i].isArray());
                 }
             }
             // try to create a parser, to see if everything's fine
             createCmdLineParser(null);
         }
         catch ( IllegalAnnotationError e ) {
             throw illegalMethod(method, e.getMessage(), e);
         }
     }
 
     private static int getOutIndex(Method method, Class[] paramTypes) {
         int i = 0;
         int index = -1;
         for ( Class t : paramTypes ) {
             if ( t.equals(PrintWriter.class) ) {
                 if ( index < 0 ) {
                     index = i;
                 }
                 else {
                     throw new IllegalArgumentException("[" + method + "] More than one output parameter");
                 }
             }
         }
         return index;
     }
 
     public static IllegalArgumentException illegalMethod(Method method, String msg) {
         return illegalMethod(method, msg, null);
     }
 
     public static IllegalArgumentException illegalMethod(Method method, String msg, Throwable cause) {
         return new IllegalArgumentException("[" + method + "] " + msg, cause);
     }
 
     @Override
     public void execute(PrintWriter out, String[] args) throws Exception {
         Object[] argArray = new Object[paramTypes.length];
         if ( outIndex >= 0 ) {
             argArray[outIndex] = out;
         }
         try {
             createCmdLineParser(argArray).parseArgument(args);
         }
         catch ( org.kohsuke.args4j.CmdLineException e ) {
             throw new CmdLineArgumentsException(e.getLocalizedMessage(), e);
         }
         try {
             method.invoke(instance, argArray);
         }
         catch ( InvocationTargetException e ) {
            Throwables.propagateIfPossible(e.getTargetException(), Exception.class);
             throw e;
         }
     }
 
     @Override
     public String getHelp(String name) {
         boolean hasArgs = false;
         for ( Object def : defs ) {
             if ( def != null ) {
                 hasArgs = true;
                 break;
             }
         }
         StringWriter help = new StringWriter();
         help.write(description);
         help.write("\n\n");
         help.write("Usage: ");
         help.write(name);
         if ( hasArgs ) {
             CmdLineParser parser = createCmdLineParser(null);
             parser.printSingleLineUsage(help, null);
             help.write("\n\n");
             parser.printUsage(help, null);
             String helpStr = help.toString().trim();
             // replace all non-UNIX newlines with UNIX newlines
             return helpStr.replace("\n\r", "\n").replace('\r', '\n');
         }
         else {
             return help.toString();
         }
     }
 
     private CmdLineParser createCmdLineParser(Object[] argArray) {
         CmdLineParser parser = new CmdLineParser(null);
         int i = 0;
         for ( Object def : defs ) {
             if ( def instanceof Option ) {
                 parser.addOption(new ParamSetter(i, argArray), (Option)def);
             }
             else if ( def instanceof Argument ) {
                 parser.addArgument(new ParamSetter(i, argArray), (Argument)def);
             }
             i++;
         }
         return parser;
     }
 
     @SuppressWarnings("ClassExplicitlyAnnotation")
     private static final class ParamOption implements Option {
 
         private final Option option;
         private final boolean multiValued;
 
         private ParamOption(Option option, boolean multiValued) {
             this.option = option;
             this.multiValued = multiValued;
         }
 
         @Override
         public String name() {
             return option.name();
         }
 
         @Override
         public String[] aliases() {
             return option.aliases();
         }
 
         @Override
         public String usage() {
             return option.usage();
         }
 
         @Override
         public String metaVar() {
             return option.metaVar();
         }
 
         @Override
         public boolean required() {
             return option.required();
         }
 
         @Override
         public Class<? extends OptionHandler> handler() {
             return option.handler();
         }
 
         @Override
         public boolean multiValued() {
             return multiValued;
         }
 
         @Override
         public Class<? extends Annotation> annotationType() {
             return Option.class;
         }
     }
 
     @SuppressWarnings("ClassExplicitlyAnnotation")
     private final static class ParamArgument implements Argument {
 
         private final Argument argument;
         private final boolean multiValued;
 
         private ParamArgument(Argument argument, boolean multiValued) {
             this.argument = argument;
             this.multiValued = multiValued;
         }
 
         @Override
         public String usage() {
             return argument.usage();
         }
 
         @Override
         public String metaVar() {
             return argument.metaVar();
         }
 
         @Override
         public boolean required() {
             return argument.required();
         }
 
         @Override
         public Class<? extends OptionHandler> handler() {
             return argument.handler();
         }
 
         @Override
         public int index() {
             return argument.index();
         }
 
         @Override
         public boolean multiValued() {
             return multiValued;
         }
 
         @Override
         public Class<? extends Annotation> annotationType() {
             return Argument.class;
         }
     }
 
     private final class ParamSetter implements Setter {
 
         private final int index;
         private final Object[] argArray;
 
         @SuppressWarnings("unchecked")
         private ParamSetter(int index, Object[] argArray) {
             this.index = index;
             this.argArray = argArray;
             if ( argArray != null ) {
                 if ( paramTypes[index].isArray() ) {
                     argArray[index] = Array.newInstance(paramTypes[index].getComponentType(), 0);
                 }
                 else {
                     argArray[index] = Defaults.defaultValue(paramTypes[index]);
                 }
             }
         }
 
         @Override
         public void addValue(Object value) throws org.kohsuke.args4j.CmdLineException {
             if ( paramTypes[index].isArray() ) {
                 Object prev = argArray[index];
                 int len = Array.getLength(prev);
                 argArray[index] = Array.newInstance(prev.getClass().getComponentType(), len + 1);
                 //noinspection SuspiciousSystemArraycopy
                 System.arraycopy(prev, 0, argArray[index], 0, len);
                 Array.set(argArray[index], len, value);
             }
             else {
                 argArray[index] = value;
             }
         }
 
         @Override
         public Class getType() {
             if ( paramTypes[index].isArray() ) {
                 return paramTypes[index].getComponentType();
             }
             else {
                 return paramTypes[index];
             }
         }
 
         @Override
         public boolean isMultiValued() {
             return false;
         }
     }
 
 }
