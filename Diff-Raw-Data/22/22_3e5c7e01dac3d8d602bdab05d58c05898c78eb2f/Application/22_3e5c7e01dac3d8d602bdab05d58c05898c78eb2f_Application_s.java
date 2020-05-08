 package jerry;
 
 import jerry.parse.Interpreter;
 import jline.ConsoleReader;
 import org.fusesource.jansi.Ansi;
 import org.fusesource.jansi.AnsiConsole;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.expression.Expression;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.ParseException;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.ResponseEntity;
 import org.springframework.util.StringUtils;
 import org.springframework.web.client.RestClientException;
import jerry.http.HttpCommand;
import jerry.config.ApplicationConfig;
import jerry.config.CommandsConfig;
import jerry.format.Formatter;
import jerry.parse.ParsingException;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 
import static org.fusesource.jansi.Ansi.Color.*;
 import static org.fusesource.jansi.Ansi.ansi;
 
 
 /**
  * @author Tareq Abedrabbo
  */
 public class Application {
 
     private ExpressionParser expressionParser;
 
     private Buffer buffer;
 
     private ConsoleReader consoleReader;
 
     private Interpreter interpreter;
 
     private Formatter formatter;
 
     private Settings settings;
     
     private String prompt = "jerry> ";
 
     public void setExpressionParser(ExpressionParser expressionParser) {
         this.expressionParser = expressionParser;
     }
 
     public void setBuffer(Buffer buffer) {
         this.buffer = buffer;
     }
 
     public void setFormatter(Formatter formatter) {
         this.formatter = formatter;
     }
 
     public void setConsoleReader(ConsoleReader consoleReader) {
         this.consoleReader = consoleReader;
     }
 
     public void setInterpreter(Interpreter interpreter) {
         this.interpreter = interpreter;
     }
 
     public void setSettings(Settings settings) {
         this.settings = settings;
     }
 
     public void run() throws IOException {
         String line;
         PrintWriter out = new PrintWriter(System.out);
 
         while ((line = consoleReader.readLine(prompt)) != null) {
             line = line.trim();
             if (StringUtils.hasText(line)) {
                 if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                     break;
                 }
 
                 if (line.equals("buffer")) {
                     printBuffer(out);
                     continue;
                 }
 
                 // expression parsing
                 if (line.startsWith("@")) {
                     evaluateExpression(line, out);
                     continue;
                 }
 
                 // http parsing
                 runHttpCommand(line, out);
             }
         }
     }
 
     private void runHttpCommand(String line, PrintWriter out) {
         try {
             HttpCommand httpCommand = interpreter.interpret(line);
             ResponseEntity<Map<String, Object>> response = httpCommand.run();
             printResponse(out, response);
         } catch (ParsingException e) {
             out.println(formatError(e.getMessage()));
         } catch (RestClientException e) {
             Throwable cause = e.getRootCause() != null ? e.getRootCause() : e;
             out.println(formatError(cause.getMessage()));
         } finally {
             out.flush();
         }
     }
 
     private void evaluateExpression(String line, PrintWriter out) {
         String expressionString = line.substring(line.indexOf('@') + 1).trim();
         try {
             Expression expression = expressionParser.parseExpression(expressionString);
             Object value = expression.getValue(buffer);
             out.println(value);
         } catch (ParseException e) {
             out.println(formatError(e.getMessage()));
         } finally {
             out.flush();
         }
     }
 
     private void printBuffer(PrintWriter out) {
         out.println(buffer);
         out.flush();
     }
 
     private void printResponse(PrintWriter out, ResponseEntity<Map<String, Object>> response) {
         if (settings.printResponseDetails) {
             Ansi ansi = ansi();
             ansi.a("http status: ").fg(GREEN).a(response.getStatusCode()).reset().newline();
             ansi.fg(YELLOW).a("==== headers ====").reset().newline();
             HttpHeaders headers = response.getHeaders();
             for (String key : headers.keySet()) {
                 String value = StringUtils.collectionToCommaDelimitedString(headers.get(key));
                 ansi.a(key).a(": ").fg(GREEN).a(value).newline().reset();
             }
             ansi.fg(YELLOW).a("=================").reset();
             out.println(ansi);
         }
         out.println(formatter.format(response.getBody()));
     }
 
     private String formatError(String message) {
        return new StringBuilder("=== ").append(message).append(" ===").toString();
     }
 
     public static void main(String[] args) throws IOException {
         AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ApplicationConfig.class, CommandsConfig.class);
         context.registerShutdownHook();
         Application application = context.getBean(Application.class);
         AnsiConsole.systemInstall();
         printLogo();
         application.run();
     }
 
     private static void printLogo() {
         Ansi ansi = ansi();
 
         ansi.newline().fg(GREEN).a("    __                     \n" +
                 " __ / /___ __________ __    \n" +
                 "/ // // -_) __/ __/ // /    \n" +
                 "\\___/ \\__/_/ /_/  \\_, /     \n" +
                 "                 /___/      ").newline().reset();
         System.out.println(ansi);
     }
 
 }
