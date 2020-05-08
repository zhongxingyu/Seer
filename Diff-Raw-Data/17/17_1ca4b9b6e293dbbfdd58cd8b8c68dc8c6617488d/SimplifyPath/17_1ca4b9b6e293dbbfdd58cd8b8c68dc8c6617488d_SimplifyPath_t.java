import java.util.Stack;

 public class SimplifyPath {
     public String simplifyPath(String path) {
         String[] elements = path.split("/");
         Stack<String> s = new Stack<String>();
         for (int i = 0; i < elements.length; i++){
             if (elements[i].equals(".") || elements[i].length() == 0){
                 
             } else if (elements[i].equals("..")){
                 if (!s.isEmpty()){
                     s.pop();
                 }
             } else {
                 s.push(elements[i]);
             }
         }
         String result = "";
         while (!s.isEmpty()){
             result = "/" + s.pop() + result;
         }
         if (result.length() == 0) return "/";
         return result;
     }
 }
