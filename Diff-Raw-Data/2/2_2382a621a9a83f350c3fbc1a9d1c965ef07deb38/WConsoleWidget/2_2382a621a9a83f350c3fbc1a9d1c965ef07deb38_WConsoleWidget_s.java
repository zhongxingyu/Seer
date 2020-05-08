 package com.delcyon.capo.webapp.widgets;
 
 import eu.webtoolkit.jwt.TextFormat;
 import eu.webtoolkit.jwt.WApplication;
 import eu.webtoolkit.jwt.WCompositeWidget;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WContainerWidget.Overflow;
 import eu.webtoolkit.jwt.WText;
 
 /**
  * This class can be called from other threads to enable console logging to be pushed to the client.
  * You must call WApplication.getInstance().enableUpdates(true); for this to work.
  * WConsoleWidget.append is the method that you will always want to use for this.  
  * @author jeremiah
  *
  */
 public class WConsoleWidget extends WCompositeWidget
 {
     //wrapped implementation widget
     private WContainerWidget implemetationWidget = new WContainerWidget();
     //we should always have a copy of the app that created us, as it's the one that we will always want to update
     private  WApplication application =  WApplication.getInstance();
     private int bufferSize = 100;
     private boolean autoscroll = true;
     
     public WConsoleWidget()
     {
        setImplementation(implemetationWidget);
        setStyleClass("console-msgs");
        setInline(false);
        // Display scroll bars if contents overflows
        implemetationWidget.setOverflow(Overflow.OverflowAuto);
     }
     
     /**
      * Main method. Adds a pure pile of data to the widget surrounded by a div tag 
      * @param message
      */
     public void append(String message,TextFormat textFormat)
     {
         //WApplication.getInstance(); this will NOT work. 
         //You can only call getInsatnce on widget creation from that app that will need to be notified.
         
         WApplication.UpdateLock lock = application.getUpdateLock();;
 
         /*
          * Format and append the line to the conversation.
          *         
          */
         WText w = new WText(message,textFormat);
         w.setInline(isInline());
         w.setStyleClass("console-msg-"+textFormat);
         implemetationWidget.addWidget(w);
         
         /*
          * Leave not more than 100 messages in the back-log
          */
         if (implemetationWidget.getCount() > getBufferSize())
         {
             implemetationWidget.getChildren().get(0).remove();            
         }
         
 
         /*
          * Little javascript trick to make sure we scroll along with new content
          */
         if(isAutoscroll() == true)
         {
         	if(implemetationWidget.isVisible())
         	{
        		application.doJavaScript(implemetationWidget.getJsRef() + ".scrollTop += "+ implemetationWidget.getJsRef() + ".scrollHeight;");
         	}
         }
         
         /*
          * This is where the "server-push" happens. This method is called when a
          * new event or message needs to be notified to the user. It is being posted
          * from another session, but within the context of this sesssion, i.e.
          * with proper locking of this session.
          */        
         
         if(lock != null)
         {
             application.triggerUpdate();
             lock.release();
         }
     }
 
     public void destroy()
     {
         application = null;        
     }
     
     /**
      * Sets number of appends allowed before we start removing the first thing appended, 
      * Basically scroll size, except appended items can take up more than one line.   
      * @param bufferSize
      */
     public void setBufferSize(int bufferSize)
     {
         this.bufferSize = bufferSize;
     }
     
     
     public int getBufferSize()
     {
         return bufferSize;
     }
     
     /**
      * determines whether or not we automatically scroll the window to the position of the newly appended text
      * @param autoscroll
      */
     public void setAutoscroll(boolean autoscroll)
     {
         this.autoscroll = autoscroll;
     }
     
     public boolean isAutoscroll()
     {
         return autoscroll;
     }
     
 }
