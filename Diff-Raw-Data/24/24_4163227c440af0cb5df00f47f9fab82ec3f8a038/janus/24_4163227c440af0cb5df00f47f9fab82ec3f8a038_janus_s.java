 package dk.client;
 
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.user.client.*;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.logical.shared.*;
 
 public class janus implements EntryPoint {
   static TextArea ta;
   static String content = "";
 
   public void onModuleLoad() {
     ta = new TextArea();
     ta.setCharacterWidth(120);
     ta.setVisibleLines(80);
     ta.setReadOnly(true);
     ta.setWidth("100%");
     ta.setHeight("100%");
 
final VerticalPanel vp = new VerticalPanel();
vp.add(ta); vp.setWidth("100%");
vp.setHeight((int) (((float) Window.getClientHeight())*0.9) + "px");
Window.addResizeHandler(new ResizeHandler() {
 
 public void onResize(ResizeEvent event) {
   int height = (int) (((float) event.getHeight())*0.9);
   vp.setHeight(height + "px");
 }
});
RootPanel.get().add(vp);
 
     IntegerMath.exportStaticMethod();
     launchEmscripten();
   }
   public static void addMsg(String msg) {
 	content += msg + "\n";
 	ta.setText(content);
   }
   private static native void launchEmscripten() /*-{
 	$wnd.msg = $entry(@dk.client.janus::addMsg(Ljava/lang/String;));
 	$wnd.run();
   }-*/;
 }
