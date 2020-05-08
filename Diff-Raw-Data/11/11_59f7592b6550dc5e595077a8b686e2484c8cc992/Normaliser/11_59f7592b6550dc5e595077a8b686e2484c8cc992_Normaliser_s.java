 package norm.jvm;
 import clojure.lang.IFn;
 import java.util.List;
 
 public class Normaliser {
   private IFn normalise_token_list;
 
   public Normaliser (IFn normalise_token_list) {
     this.normalise_token_list = normalise_token_list;
   }
 
   public List<String> normalise(List<String> tkns) {
     return (List<String>) normalise_token_list.invoke(tkns);
   }
 
}
