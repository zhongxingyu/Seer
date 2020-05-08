 // HelloBean.java
 //
 
 package Hello;
 
 import javax.ejb.*;
 
 public class HelloBean implements SessionBean
 {
     public SessionContext ctx;
 
     public HelloBean()
     {
         // constructor
     }
 
     public void ejbCreate()
         throws CreateException
     {
         // when bean is created
     }
 
     public void ejbActivate()
     {
         // when bean is activate
     }
 
     public void ejbPassivate()
     {
         // when bean is deactivated
     }
 
     public void ejbRemove()
     {
         // when bean is removed
     }
 
     public void setSessionContext(SessionContext ctx)
     {
        this.ctx = ctx;
     }
 
     public void unsetSessionContext()
     {
        this.ctx = null;
     }
 
     public String sayHello(String myName) throws EJBException
     {
        return ("Hello " + myName);
     }
 
 }
