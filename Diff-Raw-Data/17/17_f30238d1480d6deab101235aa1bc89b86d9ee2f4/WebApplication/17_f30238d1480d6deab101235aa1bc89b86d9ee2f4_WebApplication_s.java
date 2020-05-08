 package org.zwobble.shed.parser.web;
 
 import java.io.File;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.util.List;
 
 import lombok.Data;
 
 import org.zwobble.shed.parser.parsing.CompilerError;
 import org.zwobble.shed.parser.parsing.Parser;
 import org.zwobble.shed.parser.parsing.Result;
 import org.zwobble.shed.parser.parsing.TokenIterator;
 import org.zwobble.shed.parser.parsing.nodes.SourceNode;
 import org.zwobble.shed.parser.tokeniser.TokenPosition;
 import org.zwobble.shed.parser.tokeniser.Tokeniser;
 
 import com.google.common.base.Joiner;
 import com.google.common.io.CharStreams;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 @SuppressWarnings("restriction")
 public class WebApplication {
     private static final int PORT = 8090;
     
     public static void main(String... args) throws IOException {
         HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
         httpServer.createContext("/", new Handler());
         
         httpServer.start();
     }
     
     private static class Handler implements HttpHandler {
         @Override
         public void handle(HttpExchange httpExchange) throws IOException {
             String path = httpExchange.getRequestURI().getPath();
             System.out.println("Request for: " + path);
             if (path.equals("/compile")) {
                 handleCompileRequest(httpExchange);
             } else {
                 handleFileRequest(httpExchange);
             }
             System.out.println("Finished request for: " + path);
         }
 
         private void handleCompileRequest(HttpExchange httpExchange) throws IOException {
             try {
 
                 String source = Joiner.on("\n").join(CharStreams.readLines(new InputStreamReader(httpExchange.getRequestBody())));
                 System.out.println("Source: " + source);
                 List<TokenPosition> tokens = new Tokeniser().tokenise(source);
                 Result<SourceNode> parseResult = new Parser().source().parse(new TokenIterator(tokens));
                 
                 String response = parseResultToJson(tokens, parseResult);
                 
                 byte[] responseBody = response.getBytes();
                 
                 Headers responseHeaders = httpExchange.getResponseHeaders();
                 responseHeaders.add("Content-Type", "application/json");
                 httpExchange.sendResponseHeaders(200, responseBody.length);
                 httpExchange.getResponseBody().write(responseBody);
                 httpExchange.close();
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new RuntimeException(e);
             }
         }
 
         private String parseResultToJson(List<TokenPosition> tokens, Result<SourceNode> parseResult) {
             JsonObject response = new JsonObject();
             response.add("tokens", tokensToJson(tokens));
             response.add("errors", errorsToJson(parseResult.getErrors()));
             return response.toString();
         }
 
         private JsonElement tokensToJson(List<TokenPosition> tokens) {
             JsonArray json = new JsonArray();
             for (TokenPosition tokenPosition : tokens) {
                 JsonObject tokenJson = new JsonObject();
                 tokenJson.add("lineNumber", new JsonPrimitive(tokenPosition.getLineNumber()));
                 tokenJson.add("characterNumber", new JsonPrimitive(tokenPosition.getCharacterNumber()));
                 String value = tokenPosition.getToken().getValue();
                 if (value == null) {
                     value = "";
                 }
                 tokenJson.add("value", new JsonPrimitive(value));
                 tokenJson.add("type", new JsonPrimitive(tokenPosition.getToken().getType().name().toLowerCase()));
                 json.add(tokenJson);
             }
             return json;
         }
 
         private JsonElement errorsToJson(List<CompilerError> errors) {
             JsonArray json = new JsonArray();
             for (CompilerError error : errors) {
                 JsonObject errorJson = new JsonObject();
                 errorJson.add("description", new JsonPrimitive(error.getDescription()));
                 errorJson.add("lineNumber", new JsonPrimitive(error.getLineNumber()));
                 errorJson.add("characterNumber", new JsonPrimitive(error.getCharacterNumber()));
                 json.add(errorJson);
             }
             return json;
         }
 
         private void handleFileRequest(HttpExchange httpExchange) throws IOException {
             FileInfo file = readFileFromPath(httpExchange.getRequestURI().getPath());
            byte[] responseBody = file.getBody().getBytes();
             
             Headers responseHeaders = httpExchange.getResponseHeaders();
             responseHeaders.add("Content-Type", file.getContentType());
             httpExchange.sendResponseHeaders(200, responseBody.length);
             httpExchange.getResponseBody().write(responseBody);
             httpExchange.close();
         }
         
         private FileInfo readFileFromPath(String path) {
             if (path.substring(1).contains("/")) {
                 path = "/";
             }
             path = "web" + path;
             if (!new File(path).exists() || !new File(path).isFile()) {
                 path = "web/index.html";
             }
             try {
                return new FileInfo(Joiner.on("\n").join(CharStreams.readLines(new FileReader(path))), contentTypeForPath(path));
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new RuntimeException(e);
             }
         }
 
         private String contentTypeForPath(String path) {
             if (path.endsWith(".js")) {
                 return "application/javascript";
             }
             if (path.endsWith(".css")) {
                 return "text/css";
             }
             return "text/html";
         }
     }
     
     @Data
     private static class FileInfo {
        private final String body;
         private final String contentType;
     }
 }
 
