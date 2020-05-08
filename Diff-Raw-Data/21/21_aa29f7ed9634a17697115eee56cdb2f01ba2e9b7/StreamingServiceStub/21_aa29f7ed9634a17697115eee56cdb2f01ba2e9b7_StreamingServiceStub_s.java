 package com.totsp.gwittir.client.stream.impl;
 
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.client.rpc.SerializationStreamFactory;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import com.totsp.gwittir.client.stream.StreamControl;
 import com.totsp.gwittir.client.stream.StreamServiceCallback;
 
 
 /**
  *
  * @author kebernet
  */
 public abstract class StreamingServiceStub {
     private SerializationStreamFactory factory = getStreamFactory();
     private String servicePath;
     private HandlerRegistration registration = null;
 
     protected abstract SerializationStreamFactory getStreamFactory();
 
     protected StreamControl invoke(String payload,
         StreamServiceCallback callback) {
         String id = "_cb__" + System.currentTimeMillis();
         final StreamControlImpl control = new StreamControlImpl(id, callback);
         this.setUp(id, callback);
         
         Element iframe = this.createIframe(id);
         
 
         Element form = this.createForm(id, payload);
         DOM.appendChild(RootPanel.getBodyElement(), form);
         this.submit(form);
        this.registration = Window.addCloseHandler(new CloseHandler(){
 
            public void onClose(CloseEvent event) {
                 control.terminate();
             }
 
         });
         return control;
     }
 
     public void onReceive(StreamServiceCallback callback, String payload) {
         try {
             callback.onReceive(factory.createStreamReader(payload).readObject());
         } catch (SerializationException se) {
             callback.onError(se);
         }
     }
 
     public void onError(StreamServiceCallback callback, String message) {
         callback.onReceive(new RuntimeException(message));
     }
 
     public void onComplete(String name, StreamServiceCallback callback) {
         callback.onComplete();
         cleanUp(name);
     }
 
     private void cleanUp(String name) {
         Element frame = DOM.getElementById(name + "frame");
         DOM.removeChild(DOM.getParent(frame), frame);
 
         Element div = DOM.getElementById(name + "div");
         DOM.removeChild(DOM.getParent(div), div);
 
         Element form = DOM.getElementById(name + "form");
         DOM.removeChild(DOM.getParent(form), form);
         this.registration.removeHandler();
     }
 
     private native void submit(Element form) /*-{
     form.submit();
     }-*/;
 
     private native void setUp(String name, StreamServiceCallback callback) /*-{
     instance = this;
     $wnd[name] = function(input){
     	instance.@com.totsp.gwittir.client.stream.impl.StreamingServiceStub::onReceive(Lcom/totsp/gwittir/client/stream/StreamServiceCallback;Ljava/lang/String;)(callback,unescape(input));
     };
     $wnd[name+"E"] = function(input){
         instance.@com.totsp.gwittir.client.stream.impl.StreamingServiceStub::onError(Lcom/totsp/gwittir/client/stream/StreamServiceCallback;Ljava/lang/String;)(callback,unescape(input));
     };
     $wnd[name+"C"] = function(){
         instance.@com.totsp.gwittir.client.stream.impl.StreamingServiceStub::onComplete(Ljava/lang/String;Lcom/totsp/gwittir/client/stream/StreamServiceCallback;)(name,callback);
         $wnd[name] = null;
         $wnd[name+"E"] = null;
         $wnd[name+"C"] = null;
     };
     }-*/;
 
     /**
      * @param servicePath the servicePath to set
      */
     public void setServicePath(String servicePath) {
         this.servicePath = servicePath;
     }
 
     private Element createForm(String id, String payload) {
         Element form = DOM.createForm();
         DOM.setStyleAttribute(form, "position", "absolute");
         DOM.setStyleAttribute(form, "top", "0px");
         DOM.setStyleAttribute(form, "right", "0px");
         DOM.setElementProperty(form, "action", this.servicePath);
         DOM.setElementProperty(form, "id", id + "form");
         DOM.setElementProperty(form, "method", "POST");
         DOM.setElementProperty(form, "target", id + "frame");
 
         Element r = DOM.createElement("input");
         DOM.setElementProperty(r, "type", "hidden");
         DOM.setElementProperty(r, "name", "r");
         DOM.setElementProperty(r, "value", payload);
 
         Element c = DOM.createElement("input");
         DOM.setElementProperty(c, "type", "hidden");
         DOM.setElementProperty(c, "name", "c");
         DOM.setElementProperty(c, "value", id);
         DOM.appendChild(form, c);
         DOM.appendChild(form, r);
 
         return form;
     }
 
     private native Element createIframe(String id)/*-{
         var style = " style='position:absolute; top:0px; right:0px; border:0px solid white; width:0px; height:0px' ";
     	var div = $doc.createElement("div");
     	div.id = id+"div";
     	$doc.body.appendChild(div);
     	div.innerHTML = "<iframe id='"+id+"frame' name='"+id+
     		"frame' "+style+"></iframe>";
     }-*/;
     
     protected class StreamControlImpl extends StreamControl {
         String name;
         StreamServiceCallback callback;
 
         StreamControlImpl(String name, StreamServiceCallback callback) {
             super();
             this.name = name;
             this.callback = callback;
         }
 
         @Override
         public boolean terminate() {
             Element iframe = DOM.getElementById(this.name + "frame");
 
             if (iframe == null) {
                 throw new RuntimeException("Failed to find frame "+this.name);
             }
             iframe.setPropertyString("onload", "this.parentNode.removeChild(this);");
             iframe.setPropertyString("src", servicePath+"?c="+name);
             onComplete(name, callback);
 
             return true;
         }
     }
 }
