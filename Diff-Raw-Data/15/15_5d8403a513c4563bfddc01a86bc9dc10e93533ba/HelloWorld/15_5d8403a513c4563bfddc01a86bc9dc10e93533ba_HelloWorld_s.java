 
 // import the WebMacro core interfaces
 import org.webmacro.*;
 
 // import the WebMacro servlet classes
 import org.webmacro.servlet.*;
 
 
 /**
   * HelloWorld -- the simplest possible WebMacro servlet.
   * The easiest way to create a WebMacro servlet is to
   * subclass from org.webmacro.servlet.WMServlet
   *
   * Before trying to run this example, be sure to do
   * all of the following:
   *
   *   1.  Add webmacro.jar to your CLASSPATH
   *
   *   2.  Compile this servlet and install it in your servlet directory
   *
   *   3.  Add webmacro.jar to your servlet runner's classpath,
   *       and put webmacro.properties in the same place
   *
   *   4.  Edit webmacro.properties to configure WebMacro for your setup
   *
   *   5.  Start your servlet runner
   *
   */
 public class HelloWorld extends WMServlet
 {
 
    /**
      * WebMacro creates a WebContext and passes it to your servlet. 
      * The WebContext contains all the information you'll need to 
      * handle the request. It is also the only object you share 
      * with the template. If you add objects to the WebContext via
      * its put(key,value)method,they become available in the 
      * template as variables named after the key.
      */
    public Template handle(WebContext context) throws HandlerException
    {
 
       // Put whatever data you like into the context. In
       // this example we just put in a simple string
 
       context.put("Hello", "Hello, brave new world!");
 
 
       // Select a template. You could choose the template based 
       // on any criteria you like: user preference, current 
       // action, different templates for different results, etc.
 
       // We need to acquire a template somehow
       Template view;
 
       try {
 
          // The following line asks WebMacro's resource broker to
          // find the "helloWorld.wm" template, cache it, and return a 
          // parsed copy to us.
 
          view = getTemplate("helloWorld.wm");
 
       } catch (NotFoundException e) {
 
          // If this happens you have not set WebMacro up correctly:
          // either WebMacro isn't up and running well enough to be able 
          // to locate any template, or else we asked for a template which
          // doesn't exist. However, the HelloWorld.wm example should exist
          // if you have everything set up properly!
 
          throw new HandlerException(
               "HelloWorld loaded and executed OK, but was unable to load \n"
             + "a template. There are a couple of things that could be wrong \n"
             + "here. First, you may have put webmacro.properties somewhere \n"
             + "outside your servlet classpath. Try putting it in the same \n"
             + "place as you put webmacro.jar. Second, you may have set the \n"
             + "TemplateRoot directory incorectly. The file helloWorld.wm \n"
             + "must be in one of the directories listed in that option in \n"
             + "the webmacro.properties file. You should check the server \n"
             + "logs or output (or WebMacro's log if it's set and being used \n"
             + "for helpful messages which may indicate what the problem is. \n"
             + "Since your servlet runner may have d fewifferent classpath \n"
             + "settings, check that webmacro.properties is in the same \n"
             + "classpath that was used to load webmacro.jar. \n"
            + "\n"
            + "Here is the actual exception that was raised:\n" + e);
 
       }
 
       // Now that we have done all our processing, it's time to produce 
       // some output. We return the template we have created. WebMacro
       // will automatically execute it for us. If you don't want this 
       // to happen, you can return "null" instead and WebMacro will 
       // do nothing--leaving you responsible for what is returned to
       // the user. You could execute the template yourself if wanted,
       // giving you a chance to do some post execution cleanup--in 
       // that case you would do a view.execute(context) here, followed
       // by whatever you wanted to do, followed by a return null.
       return view;
 
    }
 
 }
 
