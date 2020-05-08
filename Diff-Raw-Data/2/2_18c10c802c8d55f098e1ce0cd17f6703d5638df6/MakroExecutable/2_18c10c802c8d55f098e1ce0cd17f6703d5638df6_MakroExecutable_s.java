 package settakassa.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MakroExecutable implements Executable {
    private final List<Executable> executableList = new ArrayList<Executable>();
 
    public void add(Executable executable) {
       executableList.add(executable);
    }
 
    public void execute(EntityId id) {
       for (Executable executable : executableList) {
         executable.equals(id);
       }
    }
 }
