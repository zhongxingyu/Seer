 package org.bloodtorrent.view;
 
 import com.yammer.dropwizard.views.View;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 14
  * Time: 오후 2:57
  * To change this template use File | Settings | File Templates.
  */
 public class ResultView extends CommonView {
     private final String result;
     private final List<String> messages;
 
     public ResultView(String result) {
         this(result, new ArrayList<String>());
     }
 
     public ResultView(String result, List<String> messages) {
         super("/ftl/registrationResult.ftl");
         this.result = result;
         this.messages = messages;
     }
 
     public ResultView(String result, String message) {
        this(result, Arrays.asList(message));
     }
 
     public String getResult() {
         return result;
     }
 
     public List<String> getMessages() {
         return messages;
     }
 }
