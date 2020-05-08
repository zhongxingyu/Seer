 package org.nohope.spring;
 
import akka.actor.UntypedActor;

 import javax.inject.Inject;
 import javax.inject.Named;
 import java.io.Serializable;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 9/16/12 11:12 PM
  */
 public class Bean implements Serializable {
     private static final long serialVersionUID = -4715220004379531899L;
 
     private final Integer param1;
     private final String param2;
     private final String param3;
 
     @Inject
     public Bean(final Integer param1,
                 @Named("param2") final String param2,
                 @Named("param3") final String param3) {
         this.param1 = param1;
         this.param2 = param2;
         this.param3 = param3;
     }
 
     public Integer getParam1() {
         return param1;
     }
 
     public String getParam2() {
         return param2;
     }
 
     public String getParam3() {
         return param3;
     }
 }
