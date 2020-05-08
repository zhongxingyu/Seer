 package fm.audiobox.core.models;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IEntity;
 
 public class Action extends AbstractEntity implements Serializable {
 
   private static final long serialVersionUID = 1L;
   private static Logger log = LoggerFactory.getLogger( Action.class );
 
   public static final String TAGNAME = "action";
   public static final String NAMESPACE = Action.TAGNAME;
 
   private String name;
   private int id;
   private Args args;
 
 
   public Action(IConfiguration config) {
     super(config);
     log.info("new Action instantiated");
   }
 
   @Override
   public String getNamespace() {
     return NAMESPACE;
   }
 
   @Override
   public String getTagName() {
     return TAGNAME;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   public Args getArgs() {
     return args;
   }
 
   public void setArgs(Args args) {
     this.args = args;
   }
 
   @Override
   public Method getSetterMethod(String tagName) throws SecurityException, NoSuchMethodException {
 
     if ( tagName.equals("name") ){
       return this.getClass().getMethod("setName", String.class);
    } else if ( tagName.equals("requestId") ){
       return this.getClass().getMethod("setId", int.class);
     } else if ( tagName.equals("args") ){
       return this.getClass().getMethod("setArgs", Args.class);
     }
 
     return null;
   }
 
   @Override
   public String getApiPath() {
     return null;
   }
 
   @Override
   public void setParent(IEntity parent) {
     // not needed
     
   }
 
   @Override
   protected void copy(IEntity entity) {
     // temporary not supported
   }
 
 }
