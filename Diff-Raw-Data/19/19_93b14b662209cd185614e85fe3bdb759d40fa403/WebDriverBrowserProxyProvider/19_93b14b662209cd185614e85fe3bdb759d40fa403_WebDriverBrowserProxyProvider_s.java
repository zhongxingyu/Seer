 package webdriverrunner.engine;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import webdriverrunner.api.Browser;
 
 /**
  *
  * @author Witold Szczerba
  */
 public class WebDriverBrowserProxyProvider {
 
     private final ThreadLocalBrowsers delegate;
 
     public WebDriverBrowserProxyProvider(ThreadLocalBrowsers delegate) {
         this.delegate = delegate;
     }
     
     public Browser get() {
         return (Browser) Proxy.newProxyInstance( //
                 Browser.class.getClassLoader(), //
                 new Class[] { Browser.class }, //
                 proxyInvocationHandler);
     }
     
     private final InvocationHandler proxyInvocationHandler = new InvocationHandler() {
 
         @Override
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             Browser browserForCurrentThread = delegate.get();
            return method.invoke(browserForCurrentThread, args);
         }
     };
    
 }
