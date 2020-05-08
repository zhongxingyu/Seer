 package org.gquery.slides.presentations.gwtcreate;
 
 import static com.google.gwt.query.client.GQuery.*;
 import static com.google.gwt.query.client.plugins.effects.PropertiesAnimation.EasingCurve.easeInOutBack;
 import static com.google.gwt.query.client.plugins.effects.PropertiesAnimation.EasingCurve.easeOutBack;
 import static org.gquery.slides.client.Utils.getRandom;
 import static org.gquery.slides.client.Utils.setTimeout;
 
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.gquery.slides.client.Prettify;
 import org.gquery.slides.client.SlidesSource;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.query.client.Promise;
 import com.google.gwt.query.client.Promise.Deferred;
 import com.google.gwt.query.client.builders.JsniBundle;
 import com.google.gwt.query.client.impl.ConsoleBrowser;
 import com.google.gwt.query.client.js.JsUtils;
 import com.google.gwt.query.client.plugins.deferred.FunctionDeferred;
 import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
 import com.google.gwt.query.client.plugins.effects.Fx;
 import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.EasingCurve;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author manolo
  *
  */
 public class GwtCreatePresentation extends SlidesSource {
 
   GQuery viewPort = $("#viewport");
   Widget resizeWidget;
 
   public void beforeBindEvent() {
     leaveBindEvent();
     viewPort.show();
   }
 
   /**
    * @ Event binding
    */
   public void testBindEvent() {
     // handle the click event on the console
     $("#console").bind("click", new Function() {
       public void f() {
         console.log("It's awesome, you click on the console!!");
       }
     });
     //
     // you can handle any type of event supported by the browser
     $("#console").bind("transitionend", new Function() {
       public void f() {
         console.log("Resizing of the console done !");
       }
     });
     //
     resizeWidget = new HTML("Hover me to resize the console");
     RootPanel.get("viewport").add(resizeWidget);
     //
     // works with widget
     $(resizeWidget).mouseover(new Function() {
       public void f() {
         $("#console").css("width", "50%");
       }
     }).mouseout(new Function() {
       public void f() {
         $("#console").css("width", "96%");
       }
     });
   }
 
   public void afterBindEvent() {
     console.log("");
   }
 
   public void leaveBindEvent() {
     viewPort.hide();
     if (resizeWidget != null) {
       resizeWidget.removeFromParent();
       resizeWidget = null;
     }
     testUnBindEvent();
   }
 
   public void enterUnBindEvent() {
     beforeBindEvent();
     testBindEvent();
     afterBindEvent();
   }
 
   /**
    * @ Unbind Event
    */
   public void testUnBindEvent() {
     // remove all handlers by event types
     $("#console").unbind("click");
     $("#console").unbind("transitionend");
     //
     // you can remove handlers of several types at once.
     $(resizeWidget).unbind("mouseover mouseout");
     //
     // remove a specific handler
     $(resizeWidget).click(new Function() {
       public void f() {
         console.log("Youhou \\o/ /o\\ ");
         $(resizeWidget).unbind("click", this);
       }
     });
   }
 
   public void leaveUnBindEvent() {
     leaveBindEvent();
   }
 
   public void beforeCustomEvent() {
     viewPort.show();
   }
 
   /**
    *  @ Custom event
    */
   public void testCustomEvent() {
     viewPort.append("<input type='text' id='text'></input><button>Send to console</button>");
     //
     // trigger the 'sendToConsole' event when we click on the button
     $("#viewport > button").click(new Function() {
       public void f() {
         $("#console").trigger("sendToConsole", $("#text").val());
       }
     });
     //
     // handle the 'sendToConsole' event
     $("#console").bind("sendToConsole", new Function() {
       @Override
       public boolean f(Event e, Object... data) {
         console.log(data[0]);
         return false;
       }
     });
 
   }
 
   public void afterCustomEvent() {
     $("#viewport > button").unbind("click");
     $("#console").unbind("sendToConsole");
     viewPort.empty().hide();
   }
 
   public void beforeNamespace() {
     viewPort.append("<button id='unbind'>Unbind events</button>").show();
   }
 
   /**
    *  @ Namespace
    */
   public void testNamespace() {
     // you can specify name space when you bind events
     $("#console").bind("click.ns1 mouseenter.ns1", new Function() {
       public void f() {
         console.log(getEvent().getType() + " from namespace [ns1]");
       }
     });
     //
     $("#console").bind("click.ns2 mouseleave.ns2", new Function() {
       public void f() {
         console.log(getEvent().getType() + " from namespace [ns2]");
       }
     });
 
     $("#unbind").click(new Function() {
       public void f() {
         // you can decide to unbind only some events of some namespace
         $("#console").unbind("click.ns2");
         //
         // or unbind all events of a certain namespace
         $("#console").unbind(".ns1");
       }
     });
     //
     console.log("Ready !");
   }
 
   public void leaveNamespace() {
     $("#console").unbind(".ns1");
     viewPort.empty().hide();
   }
 
 
   /**
    * @ Avoiding JSNI
    * @@ calling external code.
    * <pre>
    pushStateImpl("aStateObject", "aTitle", "foo/bar");
    ...
    native void pushStateImpl(Object stateObject, String title, String url) /*-{
        if ($wnd.history && $wnd.history.pushState) {
           $wnd.history.pushState(stateObject, title, url);
        }
    }-* /;
 
    NodeList nodes = querySelectorAll("div")
    ...
    native JavaScriptObject querySelectorAll(String selector) /*-{
        if ($doc.querySelectorAll) {
            return $doc.querySelectorAll(selector);
        }
    }-* /;
    * </pre>
    */
   public void testJsnicall() {
     JavaScriptObject jso = JsUtils.prop(window, "history");
     JsUtils.runJavascriptFunction(jso, "pushState", "aStateObject", "aTitle", "foo/bar");
     //
     NodeList<?> nodes = JsUtils.runJavascriptFunction(document, "querySelectorAll", "div");
     //
     console.log(Window.Location.getHref());
     console.log(nodes.getLength());
   }
 
   private String url;
   public void enterJsnicall() {
     // save pushState before changing it in the snippet
     url = Window.Location.getPath() + Window.Location.getQueryString();
   }
 
   public void leaveJsnicall() {
     // restore pushState
     JavaScriptObject jso = JsUtils.prop(window, "history");
     JsUtils.runJavascriptFunction(jso, "pushState", null, null, url + Window.Location.getHash());
   }
 
   /**
    * @ Avoiding JSNI
    * @@ Exporting Java methods
    * <pre>
 // Call the native method to export the function
 exportBar();
 ...
 
 // Write a native JSNI method to set a window property
 native void exportBar() /*-{
   $wnd.bar =
     $entry(
        @org.gquery.slides.presentations.gwtcreate.GwtCreatePresentation::bar(Ljava/lang/Object;)
     );
 }-* /;
 
 // Write a Java method to handle the call
 @include: bar
 
    * </pre>
    */
   public void testJsniexport() {
     // Just set a window property with a wrapped function
     JsUtils.prop(window, "foo", JsUtils.wrapFunction(new Function() {
       public Object f(Object... args) {
         console.log(dumpArguments());
         return "Hello from gQuery exported java";
       }
     }));
   }
 
   public static Object bar(Object args) {
     console.log(args);
     return "Hello from JSNI exported java";
   }
 
   // TODO: To copy this method in the <pre> block it is
   // difficult because the AST removes the jsni body, so
   // we have to figure out a way to get that comment block
   native void exportBar(String b)  /*-{
     $wnd.bar =
       $entry(
          @org.gquery.slides.presentations.gwtcreate.GwtCreatePresentation::bar(Ljava/lang/Object;)
       );
   }-*/;
 
   // Show the console, and run a JavaScript console emulator.
   public void beforeJsniexport() {
     console.log("Ready");
     exportBar(null);
     viewPort.append("<div>Try this javascript code:</div>" +
     "<pre>bar('hello', 'bye');\nbar(1);\nfoo('hello','bye');\nfoo('hi', 2, {a: 1, b:true, c:'foo'});</pre>");
 
     $("<input type=text id='evaljs' placeholder='Javascript console' >").appendTo(viewPort)
     .bind(Event.ONKEYDOWN, new Function() {
       public boolean f(Event e) {
         if (e.getKeyCode() == KeyCodes.KEY_ENTER) {
           String js = $(this).val();
           Object o;
           try {
             o = JsUtils.runJavascriptFunction(window, "eval", js);
           } catch (Exception e2) {
             o = e2.getMessage().replaceFirst("^.*:\\s+", "");
           }
           if (o != null) {
             console.log(o);
           }
           return false;
         }
         return true;
       }
     });
 
     viewPort.show();
     $("#play").hide();
   }
 
   public void leaveJsniexport() {
     viewPort.empty().hide();
   }
 
   /**
    * @ gQuery Animations
    * - jQuery uses javascript timers.
    * - gQuery CSS3 transitions or falls back to javascript.
    * - gQuery supports CSS3 transformation syntax in properties.
    * - Animation queue works with both CSS3 and javascript.
    * - gQuery supports all set of named Bezier curves, and allows customization.
    * - It comes with a set of predefined animations.
    *
        <div class="gQLogo" style='position: fixed; display: none'><img src="img/logo-gquery-transp.png"></div>
    */
   public void testCss3Animations() {
     $(".gQLogo").animate($$("top:50px, left:5px, background-color:#ADD9E4; rotateY:180deg, rotateX:180deg, transformOrigin: center"), 3000, easeOutBack);
     $(".gQLogo").animate($$("rotateY:0deg, rotateX:0deg, transformOrigin: center"), 1000, EasingCurve.custom.with(.31,-0.37,.47,1.5));
     $(".gQLogo").animate($$("background-color:gold"), 1000, easeInOutBack);
     $(".gQLogo").animate($$("background-color:#ADD9E4"), 1000);
   }
 
   public void leaveCss3Animations() {
     $(".gQLogo").hide();
     viewPort.css($$("width: '', height: ''")).empty().hide();
   }
 
   public void beforeCss3Animations() {
     viewPort.css($$("width: 200px, height: auto")).hide().delay(7000).fadeIn(2000);
     $(".gQLogo").show().css($$("top: 105%, left: 105%, background-color: #e54827"));
   }
 
   public void enterCss3Animations() {
     viewPort.empty().hide();
     final GQuery logo = $(".gQLogo").hide();
 
     @SuppressWarnings("serial")
     TreeMap<String, Function> animations = new TreeMap<String, Function>(){{
       put("01 fadeOut()      | opacity: 'hide'", lazy().fadeOut().done());
       put("02 fadeIn()       | opacity: 'show'", lazy().fadeIn().done());
       put("03 fadeTo(0.5)    | opacity: '0.5'", lazy().fadeTo(.5).done());
       put("04 fadeTo(1)      | opacity: '1'", lazy().fadeTo(1).done());
       put("05 fadeToggle()   | opacity: 'toggle'", null);
       put("06 slideUp()      | height: 'hide'", null);
       put("07 slideDown()    | height: 'show'", null);
       put("08 slideLeft()    | width: 'hide'", null);
       put("09 slideRight()   | width: 'show' ", null);
       put("10 slideToggle()  | height: 'toggle' ", null);
       put("11 toggle()       | opacity: 'toggle', width : 'toggle', height : 'toggle'", null);
       put("12 clipUp()       | clip-action: 'hide', clip-origin: 'top-left'", null);
       put("13 clipDown()     | clip-action: 'show', clip-origin: 'top-left'", null);
       put("14 clipDisappear()| clip-action: 'hide'", null);
       put("15 clipAppear()   | clip-action: 'show'", null);
       put("16 clipToggle()   | clip-action: 'toggle', clip-origin: 'top-left'", null);
       put("17 custom         | clip-action: 'toggle', clip-origin: 'bottom-right', opacity: toggle, background: #e54827, easing: easeOutBack, duration: 4000", null);
       put("20 css3 transform | rotateY:180deg, rotateX:180deg, background:navy, transformOrigin: center, easing: easeInOutBack, duration: 3000, scale: 0.5, opacity: 0.3", null);
       put("21 restore        | rotateY:0deg, rotateX:0deg, background: #ADD9E4, transformOrigin: center, scale: 1, opacity: 1", null);
     }};
 
     GQuery ul= $("<ul>").appendTo(viewPort);
 
     final GQuery input =
       $("<textarea placeholder='Animate' >").appendTo(viewPort)
       .bind(Event.ONKEYDOWN, new Function() {
         public boolean f(Event e) {
           if (e.getKeyCode() == KeyCodes.KEY_ENTER) {
             logo.stop(false, true).animate($$($(this).val()));
             return false;
           }
           return true;
         }
       });
 
     for (final Entry<String, Function> e : animations.entrySet()) {
       final String name = e.getKey().replaceFirst("^\\d+ (.*) *\\| *(.*)$", "$1");
       final String prop = e.getKey().replaceFirst("^\\d+ (.*) *\\| *(.*)$", "$2");
 
       $("<li>" + name).appendTo(ul).click(new Function() {
         public void f() {
           input.val(prop);
           Function f = e.getValue();
           if (f != null) {
             logo.stop(false, true).each(f);
           } else {
             logo.stop(false, true).animate($$(prop));
           }
           String code = "$(\".logo\").";
           code += name.contains("(") ? ("as(Effects)." + name) :  ("animate($$(\"" + prop + "\")" )+ ";" ;
           $("#play").hide();
           $("#css3animations .jCode-lines pre").html(Prettify.prettify(code));
         }});
     }
 
     $("<button> disable CSS3 </button>").appendTo(viewPort).click(
         new Function() {
           public void f() {
             if (Fx.css3 = !Fx.css3 ) {
               $(this).text("disable CSS3");
             } else {
               $(this).text("enable CSS3");
             }
             console.clear();
             console.log("CSS3 animations are " + (Fx.css3 ? "enabled" : "disabled"));
           }
         });
   }
 
   /**
    * @ The gQuery Browser class
    *
    * - Equivalent to the jQuery.browser object.
    * - gQuery provide these flags: webkit, mozilla, opera, msie, ie9, ie8, ie6
    * - A Generator produces the implementation which return false/true in compile time.
    * - Any code inside a non matching block is removed by the GWT optimizer.
    * - This approach saves a lot of code for browser specific code.
    */
   public void testBrowserClass() {
     if (browser.webkit) {
       console.log("This code goes to the chrome and safari permutation.");
     } else if (browser.mozilla) {
       console.log("This code goes to  the Firefox permutation.");
     } else {
       console.log("This code goes to IE and Opera permutations ");
     }
   }
 
   public static class SlidesConsole extends ConsoleBrowser {
     public void log(Object o) {
       $("#console").show()
         .append(String.valueOf(o));
     }
   }
 
   /**
    * @ The gQuery Console class
    *
    * - Equivalent to the javascript `window.console` object.
    * - Methods available: "log", "info", "warn", "error", "dir", "clear", "profile", "profileEnd"
    * - There are specific tweaks per browser (ie: IE8, IE9 console is not part of the DOM)
    * - It is injected using Deferred binding, so we can override the implementation.
    */
   public void testConsoleClass() {
     // @include: SlidesConsole
     GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
       public void onUncaughtException(Throwable e) {
         console.log(e.getMessage());
       }
     });
     //
     $(window).delay(1000, new Function(){
       public void f() {
         throw new RuntimeException("An uncaugh exception");
       }
     });
 
   }
 
   public static abstract class JsniSlidesExample implements JsniBundle {
     @MethodSource("js/jsni-example.js")
     public abstract String foo(String arg1, String arg2);
   }
 
   /**
    * @ JSNI Bundle
    * - A generated class with JSNI methods whose content is taken from external files.
    * @@ GOALS
    * - Use IDEs for editing, formating... instead of dealing with code in comment blocks.
    * - Easier to test JS in the browser before compiling it.
    * - Include third-party libraries without modification of the original source.
    * - Easier to write GWT wrappers: not need of html tags, nor .gwt.xml modifications, nor TextResources.
    * - Get rid of any jsni java method if the application does not use it.
    * - Take advantage of GWT jsni validators, obfuscators and optimizers.
    <pre>
    $ cat jsni-example.js
    return arguments[0] + " " + arguments[1] + " : gQuery rocks !" ;
    </pre>
    */
   public void testJsniBundle() {
     // @include: JsniSlidesExample
     //
     JsniSlidesExample jsniExample = GWT.create(JsniSlidesExample.class);
     console.log(jsniExample.foo("Say", "something"));
 
   }
 
   public static abstract class HighCharts implements JsniBundle {
    @LibrarySource("http://ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.min.js")
     public abstract void initJQuery();
    //
     @LibrarySource("js/highcharts.src.js")
     public abstract void initHighcharts();
    //
     public void drawChart(String id, JavaScriptObject props) {
       JavaScriptObject $container = JsUtils.runJavascriptFunction(window, "jQuery", "#" + id);
       JsUtils.runJavascriptFunction($container, "highcharts", props);
     }
   }
 
   private JavaScriptObject charProps = $$("title: {text: 'Monthly Average Temperature'},"
       + "xAxis: {categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']},"
       + "yAxis: {title: {text: 'Temperature (°C)'},},"
       + "series: [{name: 'Tokyo', data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5]},"
       + "{name: 'New York', data: [-0.2, 0.8, 5.7, 11.3, 17.0, 22.0]},"
       + "{name: 'London',data: [3.9, 4.2, 5.7, 8.5, 11.9, 15.2]}]");
 
   /**
    * @ JSNI Bundle: Importing third-party javascript files
    *
    * <div id="container" style='display: none'></div>
    */
   public void testHighCharts() {
     // @include: HighCharts
     //
     HighCharts highCharts = GWT.create(HighCharts.class);
     //
     highCharts.initJQuery();
     highCharts.initHighcharts();
     highCharts.drawChart("viewport", charProps);
   }
 
   public void beforeHighCharts() {
     viewPort.show();
   }
 
   /**
    * @ What is the Deferred object?
    *
    * - It is a chainable object that holds our callbacks into queues.
    * - It can invoke callback queues, and relay the success or failure state of any synchronous or asynchronous function.
    * - We can easily resolve or reject that deferred
    * - We can return, pass around, and store Promises, an interface for the deferred which prevents change the state.
    * - Resolved or rejected data is passed to the callbacks no matter where/when they were assigned
    * - It is Based on the CommonJS Promises/A and Promise/A+ specs.
    */
   public void slide1() {
   }
 
   /**
    * @ What does it look like?
    */
   public void testN1() throws Exception {
     // create a Deferred
     Deferred dfd = Deferred();
     // do something when it's done
     dfd.promise()
     .done(new Function(){public void f(){
       console.log( "dun dun dum" );
     }});
     // resolve (tip the done bucket)
     dfd.resolve();
   }
 
   /**
    * @ Handling success and failures
    */
   public void testN2() throws Exception {
     // create a Deferred
     Deferred dfd = Deferred();
     // do something when it's done
     dfd.promise()
     .done(new Function(){public void f(){
       console.log("success!");
     }})
     .fail(new Function(){public void f(){
       console.log("broked!");
     }});
     // resolve (tip the done bucket)
     dfd.reject();
   }
 
 
 
 
   Promise doSomethingAsync(final boolean ok) {
     return new PromiseFunction() {public void f(final Deferred dfd) {
       setTimeout(new Function(){public void f(){
         if (ok) dfd.resolve("OK");
         else dfd.reject("ERR");
       }}, 1000);
     }};
   }
 
   /**
    * @ Receiving succeed or failed data
    */
   public void testN3() throws Exception {
     // Call an asynchronous method which will succeed
     doSomethingAsync(true)
     .done(new Function(){public void f(){
       // get the succeed data
       console.log("we " + arguments(0));
     }});
     // Call an asynchronous method which will fail
     doSomethingAsync(false)
     .fail(new Function(){public void f(){
       // get the failed message
       console.log("we " + arguments(0));
     }});
   }
 
   /**
    * @ Chaining
    */
   public void testN4_1() throws Exception {
     Function didIt = new Function(){public void f(){
       console.log("did it!");
     }};
     Function failed = new Function(){public void f(){
       console.log("failed!");
     }};
 
     // Call a failed asynchronous
     doSomethingAsync(false)
     .done(didIt)
     .fail(failed)
     .always(didIt);
   }
 
   /**
    * @ Pipelining
    */
   public void testN4_2() throws Exception {
     Function didIt = new Function(){public void f(){
       console.log("did it!");
     }};
     Function failed = new Function(){public void f(){
       console.log("failed!");
     }};
 
     // Call a failed asynchronous
     doSomethingAsync(true)
     .then(didIt, failed)
     .done(didIt)
     .fail(failed);
 
     // Call a failed asynchronous
     doSomethingAsync(false)
     .then(didIt, failed)
     .done(didIt)
     .fail(failed);
   }
 
   /**
    * @ Promises maintain status and data.
    */
   public void testN5() throws Exception {
     // create a Deferred
     final Deferred dfd = Deferred();
     // resolve it
     dfd.resolve( "OH NO YOU DIDNT");
 
     // Check that the promise status is resolved
     console.log("Promise status is: " + dfd.promise().state());
 
     // Add a handle to the promise in the future.
     setTimeout( new Function(){public void f(){
       dfd.promise().done( new Function(){public void f() {
         console.log(arguments(0));
       }});
     }}, 1000 );
   }
 
   public void testN5_2() throws Exception {
     final Promise customDfd = getRandom();
     when(customDfd)
     .done(new Function(){public void f(){
       console.log(dumpArguments());
       when(customDfd).done(new Function(){public void f(){
         console.log(dumpArguments());
       }});
     }});
   }
 
   public void testN6() throws Exception {
     // call an async random generator
     getRandom().done(new Function(){public void f(){
       // We get an array of arguments
       console.log(arguments(0));
     }});
   }
 
   /**
    * @ Joining multiple calls
    */
   public void testN7() throws Exception {
     // We can join simultaneous promises, functions or data into a single promise which
     // will be resolved only in the case all of them succeed.
     when( getRandom(), "JQ", getRandom(), true, new Button())
     .done( new Function(){public void f(){
       // We get a bi-dimensional array with the output of each call
       console.log(arguments(0, 0));
       console.log(arguments(1, 0));
       console.log(arguments(2, 0));
       console.log(arguments(3, 0));
       console.log(arguments(4, 0));
     }});
   }
 
   /**
    * @ The helper method `dumpArguments`
    */
   public void testN7_2() throws Exception {
     // Join different calls
     when( getRandom(), "JQ", new Boolean[]{true, false})
     .done( new Function(){public void f(){
       // helper method to inspect the content of the arguments array
       console.log(dumpArguments());
     }});
   }
 
   /**
    * @ Wait until everything is resolved.
    */
   public void testN8() throws Exception {
     // customized deferred will be resolved after a delay
     Function customDfd = new Function(){public Object f(Object...args){
       final Deferred dfd = Deferred();
       setTimeout(new Function(){public void f(){
         dfd.resolve("all done!");
       }}, 4000);
       return dfd.promise();
     }};
     // run simultaneous asynchronous callbacks
     when( getRandom(), customDfd)
     .done(new Function(){public void f(){
       // Done will be fired when all promises are resolved
       console.log(arguments(0, 0));
       console.log(arguments(1, 0));
     }});
   }
 
   public void testN9() throws Exception {
     Function customDfd = new Function(){public Object f(Object...args){
       final Deferred dfd = Deferred();
       setTimeout(new Function(){public void f(){
         dfd.resolve("all done!");
       }}, 4000);
       return dfd.promise();
     }};
 
     when( getRandom(), customDfd)
     .done(new Function(){public void f(){
       console.log(dumpArguments());
     }});
   }
 
   /**
    * @ gQuery Helper Functions
    */
   public void testN10() throws Exception {
     // The normal way create a function returning a promise
     Function customDfd = new Function(){public Object f(Object...args){
       Deferred dfd = Deferred();
       dfd.resolve("all done 1 !");
       return dfd.promise();
     }};
     // Simplified way to create a promise
     Promise customPrms = new PromiseFunction() {public void f(final Deferred dfd) {
       dfd.resolve("all done 2 !");
     }};
     // Simplified way to create a deferred function
     Function customFncDfd = new FunctionDeferred() {protected void f(final Deferred dfd) {
       dfd.resolve("all done 3 !");
     }};
     // We can join at the same time Functions and Promises
     when(customDfd, customPrms, customFncDfd)
     .done(new Function(){public void f(){
       console.log(dumpArguments());
     }});
   }
 
   /**
    * @ How to use deferred for caching calls
    */
   public void testN12() throws Exception {
     // Define a customized deferred function
     final Function customDfd = new Function() {
       // Response cache
       Integer cache;
       public Object f(Object...o) {
         if (cache != null) {
           return cache;
         } else {
           // If the call is not cached return the callback promise
           return getRandom().done(new Function(){public void f(){
             cache = arguments(0);
           }});
         }
       }
     };
 
     // Calling the function always returns the same value
     when(customDfd)
     .done(new Function(){public void f(){
       console.log(dumpArguments());
       when(customDfd).done(new Function(){public void f(){
         console.log(dumpArguments());
       }});
     }});
   }
 
   Function drop(final GQuery ball, final int timeout) {
     return new Function(){public Object f(Object...o){
       System.out.println(ball);
       return ball.animate($$("bottom: 0"), timeout, EasingCurve.custom.with(.31,-0.37,.47,1.5)).promise();
     }};
   }
 
   GQuery red = $(".red"), blue = $(".blue"), yellow = $(".yellow");
   void drawBalls() {
     red.css($$("bottom:8em;right:1em;display:block"));
     blue.css($$("bottom:4em;right:2em;display:block"));
     yellow.css($$("bottom:6em;right:2.7em;display:block"));
   }
   public void testSomething() {
     drawBalls();
     when(drop(blue,500)).then(drop(yellow,2000)).then(drop(red,4000))
     .done(new Function(){public void f(){
       System.out.println(arguments(3));
     }});
   }
 
   public void testSomething1() {
     drawBalls();
     when($(".ball").animate($$("bottom: 0"), 1700, EasingCurve.custom.with(.31,-0.37,.47,1.5)))
     .done(new Function(){public void f(){
       console.log("all done");
     }});
   }
 
   public void testSomething2() {
     drawBalls();
     final Function $a = drop(blue,500),
     $b = drop(yellow,2000),
     $c = drop(red,4000);
 
     when($a, $b, $c)
     .done(new Function(){public void f(){
       console.log("all done");
     }});
   }
   public void testSomething3() {
     drawBalls();
     final Function
     $a = drop(blue,4000),
     $b = drop(yellow,2000),
     $c = drop(red,4000);
 
     setTimeout(new Function(){public void f(){
       red.stop();
     }}, 1000);
     when($a, $b, $c)
     .done(new Function(){public void f(){
       console.log("all done");
     }});
   }
   public void testSomething4() {
     drawBalls();
     final Function $a = drop(blue,500),
     $b = drop(yellow,2000),
     $c = drop(red,4000);
 
     when($a, $b, $c, getRandom())
     .done(new Function(){public void f(){
       System.out.println(arguments(3));
     }});
   }
 
   public void testSomething5() {
     drawBalls();
     Promise promise = getRandom()
     .then(new Function(){public Object f(Object...args){
       console.log(dumpArguments());
       return getRandom();
     }});
 
     when(promise).then(new Function(){public void f(){
       console.log(dumpArguments());
     }});
   }
 
 
 }
