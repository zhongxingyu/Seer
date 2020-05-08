 package vinna.route;
 
 import vinna.exception.ConfigException;
 import vinna.util.Conversions;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 public class RoutesParser {
     private final BufferedReader reader;
 
     public RoutesParser(Reader reader) {
         this.reader = new BufferedReader(reader);
     }
 
     private List<String> lines = new ArrayList<>();
 
     private String readLine() {
         if (lines.isEmpty()) {
             try {
                 return reader.readLine();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         } else {
             String res = lines.get(0);
             lines = lines.subList(1, lines.size());
             return res;
         }
     }
 
     private void pushBack(String line) {
         lines.add(line);
     }
 
     private boolean ignoreLine(String line) {
         String l = line.trim();
         return l.isEmpty() || l.startsWith("#");
     }
 
     private Pattern constraint(String prefix) {
         final String eolOrComment = "(\\s+#.*|\\s*)";
         if (!prefix.isEmpty()) {
             return Pattern.compile("\\s+" + Pattern.quote(prefix) + "(.+?)" + eolOrComment + "$");
         } else {
             return Pattern.compile("\\s+(.+?)" + eolOrComment + "$");
         }
     }
 
     private Pattern constraintWithPattern(String prefix) {
         final String eolOrComment = "(\\s+#.*|\\s*)";
         if (!prefix.isEmpty()) {
             return Pattern.compile("\\s+" + Pattern.quote(prefix) + "(.+?)\\s*:\\s*(.+?)" + eolOrComment + "$");
         } else {
             return Pattern.compile("\\s+(.+?)\\s*:\\s*(.+?)" + eolOrComment + "$");
 
         }
     }
 
     public List<Route> load(String prefix) {
         List<Route> routes = new ArrayList<>();
         String line;
         int lineNum = 0;
         String verbp = "(?<verb>[^\\s]+)";
         String controllerAndMethodp = "(?<controller>.+)\\.(?<method>[^\\.]+)";
         String actionp = controllerAndMethodp + "\\s*\\((?<args>.*)\\)";
         String pathp = "(?<path>.+?)";
         Pattern routeLine = Pattern.compile(verbp + "\\s+" + pathp + "\\s+" + actionp);
 
         try {
             while ((line = readLine()) != null) {
                 lineNum++;
                 if (!ignoreLine(line)) {
                     Matcher rm = routeLine.matcher(line);
                     if (!rm.matches()) {
                         throw new ConfigException("Invalid syntax in routes file (line " + lineNum + ")\n" + line);
                     } else {
                         String verb = rm.group("verb");
                         String path = prefix + rm.group("path");
                         String controller = rm.group("controller");
                         String method = rm.group("method");
                         String args = rm.group("args").trim();
 
                         //read constraints
                         Map<String, Pattern> queryVars = new HashMap<>();
                         Map<String, Pattern> headers = new HashMap<>();
                         Map<String, String> pathVarsConstraints = new HashMap<>();
 
                         Pattern constraintp = constraintWithPattern("");// Pattern.compile("\\s+(.+?)\\s*:\\s*(.+?)\\s*$");
                         Pattern qvPatConstraintp = constraintWithPattern("req.param.");// Pattern.compile("\\s+req\\.param\\.(.+?)\\s*:\\s*(.+?)\\s*$");
                         Pattern qvConstraintp = constraint("req.param.");// Pattern.compile("\\s+req\\.param\\.(.+?)\\s*$");
 
                         Pattern hConstraintp = constraint("req.header.");// Pattern.compile("\\s+req\\.header\\.(.+?)\\s*$");
                         Pattern hPatConstraintp = constraintWithPattern("req.header.");//  Pattern.compile("\\s+req\\.header\\.(.+?)\\s*:\\s*(.+?)\\s*$");
 
                         String cline;
                         while ((cline = readLine()) != null) {
                             if (!ignoreLine(cline)) {
                                 Matcher m;
                                 if ((m = qvPatConstraintp.matcher(cline)).matches()) {
                                     queryVars.put(m.group(1), Pattern.compile(m.group(2)));
                                 } else if ((m = qvConstraintp.matcher(cline)).matches()) {
                                     queryVars.put(m.group(1), null);
                                 } else if ((m = hPatConstraintp.matcher(cline)).matches()) {
                                     headers.put(m.group(1), Pattern.compile(m.group(2)));
                                 } else if ((m = hConstraintp.matcher(cline)).matches()) {
                                     headers.put(m.group(1), null);
                                 } else if ((m = constraintp.matcher(cline)).matches()) {
                                     String pat = m.group(2);
                                     try {
                                         Pattern.compile(pat);
                                     } catch (PatternSyntaxException e) {
                                         throw new ConfigException("Invalid path variable pattern '" + pat + "'", e);
                                     }
                                     pathVarsConstraints.put(m.group(1), pat);
                                 } else {
                                     pushBack(cline);
                                     break;
                                 }
                             }
                         }
 
                         ParsedPath parsedPath = parsePath(path, pathVarsConstraints);
                         routes.add(new Route(verb, parsedPath.pathPattern, parsedPath.variableNames, queryVars, headers, new Route.Action(controller, method, parseArgs(args))));
                     }
                 }
             }
         } finally {
             try {
                 reader.close();
             } catch (IOException e) {
                 //warning
             }
         }
         return routes;
 
     }
 
     private static ParsedPath parsePath(String path, Map<String, String> pathVarsConstraints) {
         if (!path.startsWith("/")) {
             path = ".*?/" + path;
         }
         String ref = "\\{(.+?)\\}";
 
         Pattern refp = Pattern.compile(ref);
 
         Set<String> pathVariables = new HashSet<>();
         StringBuffer pathPattern = new StringBuffer();
         Matcher m = refp.matcher(path);
         while (m.find()) {
             String var = m.group(1);
             pathVariables.add(var);
             if (pathVarsConstraints.containsKey(var)) {
                 m.appendReplacement(pathPattern, "(?<$1>");
                             /*
                             Beware, for the beast is prawling the streets
                             Matcher#appendReplacement ignores "\" characters, and hence will
                             mess up the user specified regular expressions
                             That's why we have to use the following hack: use appendReplacement for the
                             safe part, and then directly append the user regexp to the StingBuffer
                             */
                 pathPattern.append(pathVarsConstraints.get(var)).append(")");
             } else {
                 m.appendReplacement(pathPattern, "(?<$1>[^/]+)");
             }
         }
         m.appendTail(pathPattern);
 
         Pattern compiledPathPattern;
         try {
             compiledPathPattern = Pattern.compile(pathPattern.toString());
             return new ParsedPath(compiledPathPattern, pathVariables);
         } catch (PatternSyntaxException e) {
             throw new ConfigException("Illegal path " + pathPattern);
         }
     }
 
     public static ParsedPath parsePath(String path) {
         String variable = "\\{(?<name>.+?)(\\s*:\\s*(?<pattern>.+?))?\\}";
         Pattern pathSegmentPattern = Pattern.compile("(?<ls>/)(" + variable + "|(?<seg>[^/?]+))");
 
         List<String> variablesNames = new ArrayList<>();
         StringBuilder pathPattern = new StringBuilder();
         if (!path.startsWith("/")) {
             pathPattern.append(".*?");
             path = "/" + path;
         }
         Matcher m = pathSegmentPattern.matcher(path);
         int lastEnd = 0;
         while (m.find()) {
             lastEnd = m.toMatchResult().end();
             pathPattern.append("/");
             if (m.group("seg") != null) {
                 pathPattern.append(m.group("seg"));
             } else {
                 String pattern = m.group("pattern");
                 String name = m.group("name");
                 variablesNames.add(name);
                 if (pattern != null) {
                     pathPattern.append("(?<").append(name).append(">").append(pattern).append(")");
                 } else {
                     pathPattern.append("(?<").append(name).append(">").append("[^/]+").append(")");
                 }
             }
         }
 
         if (path.substring(lastEnd).startsWith("/")) {
             pathPattern.append("/");
         }
 
         return new ParsedPath(Pattern.compile(pathPattern.toString()), variablesNames);
     }
 
     public static final class ParsedPath {
         public final Pattern pathPattern;
         public final Collection<String> variableNames;
 
         public ParsedPath(Pattern pathPattern, Collection<String> variableNames) {
             this.pathPattern = pathPattern;
             this.variableNames = variableNames;
         }
     }
 
     public static final class ParsedAction {
         public final String controller;
         public final String method;
         public final List<ActionArgument> parameters;
 
         public ParsedAction(String controller, String method, List<ActionArgument> parameters) {
             this.controller = controller;
             this.method = method;
             this.parameters = parameters;
         }
     }
 
     private static Pattern argPattern(String prefix) {
         String typep = "(?:\\s*:\\s*(.+))?";
         if (prefix.isEmpty()) {
             return Pattern.compile("\\{(.+?)" + typep + "\\}");
         } else {
             return Pattern.compile("\\{" + Pattern.quote(prefix) + "(.+?)" + typep + "\\}");
         }
     }
 
     private static final Map<String, Class<?>> TYPES_NAMES;
 
     static {
         TYPES_NAMES = new HashMap<>();
         TYPES_NAMES.put("byte", Byte.TYPE);
         TYPES_NAMES.put("short", Short.TYPE);
         TYPES_NAMES.put("int", Integer.TYPE);
         TYPES_NAMES.put("long", Long.TYPE);
         TYPES_NAMES.put("float", Float.TYPE);
         TYPES_NAMES.put("double", Double.TYPE);
 
         TYPES_NAMES.put("Byte", Byte.class);
         TYPES_NAMES.put("Short", Short.class);
         TYPES_NAMES.put("Integer", Integer.class);
         TYPES_NAMES.put("Long", Long.class);
         TYPES_NAMES.put("Float", Float.class);
         TYPES_NAMES.put("Double", Double.class);
 
         TYPES_NAMES.put("boolean", Boolean.TYPE);
         TYPES_NAMES.put("Boolean", Boolean.class);
 
         TYPES_NAMES.put("BigInteger", BigInteger.class);
         TYPES_NAMES.put("BigDecimal", BigDecimal.class);
 
         TYPES_NAMES.put("String", String.class);
     }
 
     private static void fillInTypes(ActionArgument.ChameleonArgument arg, String type) {
         if (type != null) {
             String primitiveTypes = "int|long|float|double|short|byte|boolean";
             String objectTypes = "Integer|Long|Float|Double|Short|Byte|Boolean|BigInteger|BigDecimal|String";
             String types = primitiveTypes + "|" + objectTypes;
             String collTypes = "\\[(" + objectTypes + ")\\]";
 
             Matcher m = Pattern.compile(types).matcher(type);
             if (m.matches()) {
                 arg.type = TYPES_NAMES.get(type);
             } else {
                 m = Pattern.compile(collTypes).matcher(type);
                 if (m.matches()) {
                     arg.type = Collection.class;
                     arg.typeArg = TYPES_NAMES.get(m.group(1));
                 } else {
                     throw new ConfigException("Unknown type " + type);
                 }
             }
         }
 
     }
 
     public static List<ActionArgument> parseArgs(String argsString) {
         List<ActionArgument> parameters = new ArrayList<>();
         Pattern pbody = Pattern.compile("\\{" + Pattern.quote("req.body") + "\\}");
         Pattern pqvar = argPattern("req.param.");
         Pattern pheader = argPattern("req.header.");
         Pattern pvar = argPattern("");
         Pattern pstr = Pattern.compile("\"((\\.|.)*)\"");
         Pattern pbool = Pattern.compile("(true|false)");
         Pattern pnull = Pattern.compile("null");
 
         if (!argsString.isEmpty()) {
             String[] args = argsString.split("\\s*,\\s*");
             for (String arg : args) {
                 Matcher pm;
 
                 if ((pm = pbody.matcher(arg)).matches()) {
                     final ActionArgument.RequestBody res = new ActionArgument.RequestBody();
                     parameters.add(res);
                 } else if ((pm = pqvar.matcher(arg)).matches()) {
                     final ActionArgument.RequestParameter res = new ActionArgument.RequestParameter(pm.group(1));
                     fillInTypes(res, pm.group(2));
                     parameters.add(res);
                 } else if ((pm = pheader.matcher(arg)).matches()) {
                     final ActionArgument.Header res = new ActionArgument.Header(pm.group(1));
                     fillInTypes(res, pm.group(2));
                     parameters.add(res);
                 } else if ((pm = pvar.matcher(arg)).matches()) {
                     final ActionArgument.Variable res = new ActionArgument.Variable(pm.group(1));
                     fillInTypes(res, pm.group(2));
                     parameters.add(res);
                 } else if ((pm = pstr.matcher(arg)).matches()) {
                     parameters.add(new ActionArgument.Const<String>(pm.group(1)));
                 } else if ((pm = pbool.matcher(arg)).matches()) {
                     parameters.add(new ActionArgument.Const<Boolean>(Boolean.parseBoolean(pm.group(1))));
                 } else if ((pm = pnull.matcher(arg)).matches()) {
                     parameters.add(new ActionArgument.Const<Object>(null));
                 } else {
                     try {
                         parameters.add(new NumConst(new BigDecimal(arg)));
                     } catch (NumberFormatException e) {
                         throw new ConfigException("Invalid action argument " + arg);
                     }
                 }
             }
         }
 
         return parameters;
     }
 
     private static final class NumConst implements ActionArgument {
         private final BigDecimal value;
 
         public NumConst(BigDecimal value) {
             this.value = value;
         }
 
         @Override
         public Object resolve(Environment env, Class<?> targetType) {
             return Conversions.convertNumeric(value, targetType);
         }
 
         @Override
         public boolean compatibleWith(Class<?> type) {
             return Number.class.isAssignableFrom(type);
         }
     }
 }
