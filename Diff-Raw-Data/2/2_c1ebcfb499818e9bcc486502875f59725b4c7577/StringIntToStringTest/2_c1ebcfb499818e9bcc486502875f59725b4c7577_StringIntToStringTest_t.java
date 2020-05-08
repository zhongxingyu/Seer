 package teach.testrig.testTypes;
 
 
 public abstract class StringIntToStringTest extends SaraQuestion {
    
    @Override
    public abstract boolean testSolution();
    
    public abstract String test(String arg1, int arg2);
    
    @Override
    public Object test(Object arg) {
       Object[] args = (Object[])arg;
      return test((String)args[0], Integer.parseInt((String)args[1]));
    }
 }
