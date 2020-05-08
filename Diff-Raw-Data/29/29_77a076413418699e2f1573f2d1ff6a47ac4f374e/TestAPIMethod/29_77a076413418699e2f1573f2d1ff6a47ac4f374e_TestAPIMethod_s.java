 package testAPIMethodRes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 public abstract class TestAPIMethod {
     public void methodA() {
     }
 
     public static final int methodB(int x, int y) {
         return 0;
     }
 
     protected File methodC(List itemList, float x, java.util.Queue queue) {
         return null;
     }
 
     public void methodD() throws IOException, Exception {
     }
 
    public abstract <G, H extends List & Number> Set<G> write4(List<H> a);
 
 }
