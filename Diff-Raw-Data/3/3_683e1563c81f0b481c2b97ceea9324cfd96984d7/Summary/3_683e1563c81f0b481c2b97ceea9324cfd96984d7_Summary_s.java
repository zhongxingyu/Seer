 // This file is part of Droopy.
 // Copyright (C) 2011  Benoit Sigoure.
 //
 // This program is free software: you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or (at your
 // option) any later version.  This program is distributed in the hope that it
 // will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 // of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 // General Public License for more details.  You should have received a copy
 // of the GNU Lesser General Public License along with this program.  If not,
 // see <http://www.gnu.org/licenses/>.
 package viewer;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * JavaScript overlay for Droopy trace summaries.
  */
 public final class Summary extends JavaScriptObject {
 
   protected Summary() {
   }
 
   public native String method() /*-{ return this.method }-*/;
   public native String resource() /*-{ return this.resource }-*/;
   public native double endToEnd() /*-{ return this.end_to_end }-*/;
   public native int numSyscalls() /*-{ return this.num_syscalls }-*/;
   public native Syscall slowestSyscall() /*-{ return this.slowest_syscall }-*/;
   // Due on an unfortunate typo in the Python tracer, some traces have
   // prev_slowest as a one-element list instead of a just a normal Syscall
   // object.  You'd surprised as how tricky it is to tell whether something
   // is an array in JavaScript.  I rely on duck typing here: if it's got a
   // "sort" function, then it's an array, otherwise it's an object.
   public native Syscall prevSlowest() /*-{
     return typeof this.prev_slowest.sort == "function" ? this.prev_slowest[0] : this.prev_slowest
   }-*/;
   public native ConnectCall prevConnect() /*-{ return this.prev_connect }-*/;
   public native boolean apacheClosed() /*-{ return this.apache_closed }-*/;
 
   public boolean hasPrevSlowest() {
     return prevSlowest() != null;
   }
 
   public String slowestBackend() {
     final ConnectCall call = prevConnect();
     if (call == null) {
       return null;
     }
     if (call.client()) {
       return "(to client)";
     }
     final String host = call.host();
     return host != null ? host : call.peer();
   }
 
   public boolean hasSlowBackend() {
     return slowestBackend() != null;
   }
 
   /** Returns the widget associated with this summary.  */
   public Widget widget() {
     return new SummaryWidget();
   }
 
   private static final NumberFormat FMT = NumberFormat.getFormat("0.00");
 
   /** Format a timing in a human readable fashion.  */
   private static String fmt(final double timing) {
     return FMT.format(timing) + "ms";
   }
 
   private static InlineLabel label(final String text) {
     return new InlineLabel(text);
   }
 
   private final class SummaryWidget extends HBox {
     SummaryWidget() {
       super.setWidth("100%");
       super.add(label(method()));
       super.add(label(resource()));
       super.add(label(fmt(endToEnd())), HBox.ALIGN_RIGHT);
       final Syscall slowest = slowestSyscall();
       final String slowdesc;
       if (hasSlowBackend()) {
         slowdesc = "Slowest backend call: " + slowestBackend();
       } else {
         slowdesc = "Slowest syscall: " + slowest.name();
       }
       super.add(label(slowdesc + ":"), HBox.ALIGN_RIGHT);
       super.add(label(fmt(slowest.duration())), HBox.ALIGN_RIGHT);
     }
   }
 
 }
