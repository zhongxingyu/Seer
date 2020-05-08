 package com.ninja_squad.jb.codestory.action;
 
 import com.google.common.collect.ImmutableMap;
 import com.ninja_squad.jb.codestory.Action;
 import com.ninja_squad.jb.codestory.ActionFactory;
 import com.ninja_squad.jb.codestory.HttpHeaders;
 import com.ninja_squad.jb.codestory.HttpRequest;
 import com.ninja_squad.jb.codestory.HttpResponse;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * The action factory for CodeStory.
  * @author JB
  */
 public class CodeStoryActionFactory implements ActionFactory {
 
     public static final String DEFAULT_ANSWER = "What?";
 
     /**
      * The default action. Returns a 404.
      */
     private static final Action DEFAULT_ACTION = new Action() {
         @Override
         public HttpResponse execute(HttpRequest request) throws IOException {
             return new HttpResponse(HttpResponse.Status._404_NOT_FOUND,
                                     HttpHeaders.NO_HEADER,
                                    DEFAULT_ANSWER.getBytes());
         }
     };
 
     private final Map<String, Action> actionsByPath =
         ImmutableMap.<String, Action>of("/", new RootAction());
 
     /**
      * Gets the appropriate answerer based on the given request path
      */
     @Override
     public Action getAction(HttpRequest request) {
         Action action = actionsByPath.get(request.getPath());
         if (action == null) {
             action = DEFAULT_ACTION;
         }
         return action;
     }
 }
