 package jewas.http.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author fcamblor
  */
 public class NamedString extends NamedHttpData {
 
     private final List<String> values = new ArrayList<String>();
 
     public NamedString(String name, String value){
         this(name, Arrays.asList(value));
     }
 
     public NamedString(String name, List<String> values){
         super(name);
         this.values.addAll(values);
     }
 
     @Override
     public boolean isCompleted() {
         // by default, String read will always be completed
         return true;
     }
 
     public String value(){
         return this.values.get(0);
     }
 
     public List<String> values(){
        return Collections.unmodifiableList(this.values);
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("NamedString");
         sb.append('{');
         sb.append("name='").append(name).append('\'');
         if(this.values.isEmpty()){
             sb.append("value=null");
         }else if(this.values.size() == 1){
             sb.append("value='").append(value()).append('\'');
         }else{
             sb.append("value=[").append(values().toString()).append(']');
         }
         sb.append('}');
         return sb.toString();
     }
 }
