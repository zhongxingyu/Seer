 package com.delcyon.capo.webapp.widgets;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 import com.delcyon.capo.parsers.GrammarParser;
 
 import eu.webtoolkit.jwt.Orientation;
 import eu.webtoolkit.jwt.Signal1;
 import eu.webtoolkit.jwt.TextFormat;
 import eu.webtoolkit.jwt.WApplication;
 import eu.webtoolkit.jwt.WCompositeWidget;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WContainerWidget.Overflow;
 import eu.webtoolkit.jwt.WLayout;
 import eu.webtoolkit.jwt.WLength;
 import eu.webtoolkit.jwt.WMouseEvent;
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
     private WBoundedContainerWidget implemetationWidget = new WBoundedContainerWidget();
     private WContainerWidget textContainerWidget = new WContainerWidget();
     //we should always have a copy of the app that created us, as it's the one that we will always want to update
     private  WApplication application =  WApplication.getInstance();
     private int bufferSize = 100;
     private boolean autoscroll = true;
     private ArrayList<PatternStyleHolder> regexStylePatterns = new ArrayList<>();
     private ArrayList<Object[]> grammerStyles = new ArrayList<>();
     private ArrayList<PatternHolder> testPatternList = new ArrayList<>();
     
     private WText empty = new WText();
     
     public WConsoleWidget()
     {
        
        setImplementation(implemetationWidget);
        textContainerWidget.addStyleClass("console-msgs");
        textContainerWidget.setInline(false);
        
        implemetationWidget.addLayoutWidget(empty,0);
        implemetationWidget.addLayoutWidget(textContainerWidget,1);       
        
        empty.addStyleClass("empty-console-msgs");       
        textContainerWidget.setOverflow(Overflow.OverflowAuto,Orientation.Vertical);
        textContainerWidget.setOverflow(Overflow.OverflowVisible,Orientation.Horizontal);
        
     }
     
     public void setScrollWidth(WLength wLength)
     {
         implemetationWidget.setScrollWidth(wLength);
         
     }
     
     public void setScrollHeight(WLength wLength)
     {
         implemetationWidget.setScrollHeight(wLength);
         
     }
 
     /**
      * set the overflow properties of the text widget contained by this console. 
      * @param overflow
      * @param orientation
      * @param orientations
      */
     public void setTextOverflow(Overflow overflow,Orientation orientation, Orientation...orientations)
     {
     	textContainerWidget.setOverflow(overflow,orientation,orientations);
     }
     
     /**
      * sets the title of the console widget
      * @param title
      */
     public void setTitle(String title)
     {
         implemetationWidget.setTitle(title);
     }
     
     /**
      * This will add a button to the toolbar that requires tha associated permission, and will call the associated click listener on clicked()
      * @param buttonName
      * @param permission
      * @param clickListener
      * @throws Exception
      */
     public void addToolButton(String buttonName, String permission, Signal1.Listener<WMouseEvent> clickListener) throws Exception
     {
         implemetationWidget.addToolButton(buttonName, permission, clickListener);
         
     }
     
     /**
      * text to be displayed while there is no data on the console.
      * @param emptyText
      */
     public void setEmptyText(String emptyText)
     {
         empty.setText(emptyText);
     }
     
     /**
      * This is a convenience method to run a block of code against the WApplication that this class was created by. 
      * @param runnable
      */
     public void execute(Runnable runnable)
     {
         WApplication.UpdateLock lock = application.getUpdateLock();
         
         runnable.run();
         
         if(lock != null)
         {
             application.triggerUpdate();
             lock.release();
         }
     }
     
     
     /**
      * The regex does not need to match the whole line, the tail will be automatically appended.
      * @param regex a regular expression with grouping, that will be matched against any appended message
      * @param styleClasses the CSS classes that will be applied on a per matching group basis. 
      */
     public void addRegexStyler(String regex, String...styleClasses)
     {        
         regexStylePatterns.add(new PatternStyleHolder(Pattern.compile(regex),styleClasses));
     }
     
     
     /**
      * Main method. Adds a pure pile of data to the widget surrounded by a div tag 
      * @param message
      */
     public void append(String message,TextFormat textFormat)
     {
         
         //must match all tests, if any
         for (PatternHolder patternHolder : testPatternList)
         {
             if(patternHolder.pattern.matcher(message).matches() != patternHolder.passingMatchRule)
             {
                 return; 
             }
         }
         
         //check to see if we have any simple regex styles to add
         for (PatternStyleHolder patternStyleHolder : regexStylePatterns)
         {
             if(patternStyleHolder.matches(message))
             {
                 textFormat = TextFormat.XHTMLText;
                 message = patternStyleHolder.format(message);
                 break;
             }
         }
        
         //process an complex grammer styles
         for (Object[] objects : grammerStyles)
         {
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GrammarParser grammarParser = (GrammarParser) objects[0];
             Transformer transformer = (Transformer) objects[1];
             try
             {
                 transformer.transform(new DOMSource(grammarParser.parse(new ByteArrayInputStream(message.getBytes()))), new StreamResult(byteArrayOutputStream));
                 if(byteArrayOutputStream.size() > 0)
                 {                    
                     
                     message = new String(byteArrayOutputStream.toByteArray());
                     textFormat = TextFormat.XHTMLText;
                     break;
                 }
             }            
             catch (Exception e)
             {                
                 e.printStackTrace();
             }    
         }
         
         //WApplication.getInstance(); this will NOT work. 
         //You can only call getInsatnce on widget creation from that app that will need to be notified.
         
         WApplication.UpdateLock lock = application.getUpdateLock();;
 
         if(textContainerWidget.getCount() == 0)
         {
             empty.setHidden(false);
         }
         else if(empty.isHidden() == false)
         {
             empty.setHidden(true);
         }
                 
         /*
          * Format and append the line to the conversation.
          *         
          */                
         WText w = new WText(message,textFormat);
         w.setInline(isInline());
         w.setStyleClass("console-msg-"+textFormat);
         textContainerWidget.addWidget(w);
         
         /*
          * Leave not more than getBufferSize messages in the back-log
          */
         if (textContainerWidget.getCount() > getBufferSize())
         {
             textContainerWidget.getChildren().get(0).remove();            
         }
         
 
         /*
          * Little javascript trick to make sure we scroll along with new content
          */
         if(isAutoscroll() == true)
         {
         	if(textContainerWidget.isVisible())
         	{
         		application.doJavaScript("if ("+textContainerWidget.getJsRef()+" != null) {"+textContainerWidget.getJsRef() + ".scrollTop += "+ textContainerWidget.getJsRef() + ".scrollHeight;}");
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
 
     /**
      * Set contents margins (in pixels).
      * <p>
      * The default contents margins are 9 pixels in all directions. 
      * </p>
      * 
      * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
      */
     public void setContentsMargins(int left, int top, int right, int bottom) 
     {
         implemetationWidget.setContentsMargins(left, top, right, bottom);
     }
 
     
     /**
      * This takes a regex and will only append messages that match the regex
      * @param regex
      */
     public void addFilter(String regex)
     {
         addFilter(regex, true);
     }
     
 
     /**
      * This will cause each message to be appened to be run though a grammer checker, which, if matching, 
      * will then run the result against an xsl style sheet associated with that grammer.
      * @param grammarParser
      * @param xslDocument
      * @throws TransformerConfigurationException
      */
     public void addGrammerStyle(GrammarParser grammarParser, Document xslDocument) throws TransformerConfigurationException
     {   
         
         TransformerFactory tFactory = TransformerFactory.newInstance();                
         grammerStyles.add(new Object[]{grammarParser,tFactory.newTransformer(new DOMSource(xslDocument))});
     }
 
     /**
      * expected match can be used to invert the match as opposed to writing an complicated inverse regex
      * 
      * @param string
      * @param expected
      */
     public void addFilter(String regex, boolean match)
     {
         Pattern pattern = Pattern.compile(regex);        
         testPatternList.add(new PatternHolder(match, pattern));
         
     }
     
     /**
      * This associates a pattern with a match rule 
      * @author jeremiah
      *
      */
     private class PatternHolder
     {
         boolean passingMatchRule = true;
         Pattern pattern = null;
         /**
          * @param passingMatchRule
          * @param pattern
          */
         private PatternHolder(boolean passingMatchRule, Pattern pattern)
         {
             super();
             this.passingMatchRule = passingMatchRule;
             this.pattern = pattern;
         }
         
         
     }
     
     /**
      * this associates a pattern with an array of style classes
      * @author jeremiah
      *
      */
     private class PatternStyleHolder {
         
         private Pattern pattern;
         private String[] styleClasses;
         private Matcher matcher = null;
         private PatternStyleHolder(Pattern pattern, String[] styleClasses)
         {
             this.pattern = pattern;
             this.styleClasses = styleClasses;
         }
 
         
 
         public boolean matches(String message)
         {
             
             matcher = pattern.matcher(message);
             return matcher.find();
         }
         
         /**
          * wraps any matched message into a number of spans based grouping from the regex. You must have a positive match test first
          * @param message
          * @return
          */
         public String format(String message)
         {
            if(matcher == null)
            {
                return message;
            }
            
            StringBuffer buffer = new StringBuffer(); 
            
            for(int group = 0; group < styleClasses.length && group < matcher.groupCount(); group++)
            {
                buffer.append("<span class='"+styleClasses[group]+"'>");
               buffer.append(matcher.group(group));
                buffer.append("</span>");
            }
            
            //tack on anything left over on the end
            buffer.append(message.substring(matcher.end()));
            
            return buffer.toString();
         }
     }
        
 }
