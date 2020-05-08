 package com.delcyon.capo.webapp.widgets;
 
 import eu.webtoolkit.jwt.JSignal1;
 import eu.webtoolkit.jwt.TextFormat;
 import eu.webtoolkit.jwt.Utils;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WGridLayout;
 import eu.webtoolkit.jwt.WPushButton;
 import eu.webtoolkit.jwt.WText;
 
 /**
  * In the end this should be a complete wrapper for an ace editor. Still needs a toolbar, and cancel buttons.
  * Probably want a way to integrate all of the javascript as well. 
  * @author jeremiah
  *
  */
 public class WAceEditor extends WContainerWidget
 {
 
     public enum Theme
     {
         ambiance,
         chaos,
         chrome,
         clouds,
         clouds_midnight,
         cobalt,
         crimson_editor,
         dawn,
         deamweaver,
         eclipse,
         github,
         idle_fingers,
         katzenmilch,
         kr_theme,
         kuroir,
         merbivore,
         merbivore_soft,
         mono_industrial,
         monokai,
         pastel_on_dark,
         solarized_dark,
         solarized_light,
         terminal,
         textmate,
         tomorrow,
         tomorrow_night,
         tomorrow_night_blue,
         tomorrow_night_bright,
         tomorrow_night_eighties,
         twilight,
         vibrant_ink,
         xcode
     }
     
     private WText contentWText = null;
     
     private JSignal1<String> save = new JSignal1<String>(this, "save") { };
     private String mode = null;
     private Theme theme = Theme.xcode;
     private boolean readOnly = false;
 
     private WPushButton saveButton;
     
     
     public WAceEditor()
     {
         contentWText = new WText("",TextFormat.XHTMLUnsafeText);
         contentWText.setStyleClass("editorArea");
         attachEditorJS();
         saveButton = new WPushButton("Save");
         saveButton.setJavaScriptMember("onclick", "function (){"+this.save().createCall(contentWText.getJsRef()+".editor.getValue()")+"}");
         
         
         WGridLayout gridLayout = new WGridLayout(this);
         gridLayout.addWidget(contentWText,0,0);
         gridLayout.addWidget(saveButton,1,0);                
         gridLayout.setRowStretch(0, 100);
     }
     
     public WAceEditor(String content,String mode)
     {
         this();        
         setText(Utils.htmlEncode(content));
         setMode(mode);
     }
 
     //=====================Properties============================
     public void setText(String content)
     {
         contentWText.setText(Utils.htmlEncode(content));
         attachEditorJS();
     }
     
     //=====================ACE PROPERTIES========================
     public void setTheme(Theme theme)
     {
         this.theme = theme;
         contentWText.doJavaScript(contentWText.getJsRef()+".editor.setTheme('ace/theme/"+theme+"');");
     }
     
     public Theme getTheme()
     {
         return theme;
     }
     
     
     public void setReadOnly(boolean readOnly)
     {
         this.readOnly = readOnly;
         contentWText.doJavaScript(contentWText.getJsRef()+".editor.setReadOnly("+readOnly+");");        
         saveButton.setHidden(readOnly);        
     }
     
     public boolean isReadOnly()
     {
         return readOnly;
     }
     
     public void setMode(String mode)
     {
         this.mode = mode;
         contentWText.doJavaScript(contentWText.getJsRef()+".editor.getSession().setMode('ace/mode/"+mode+"');");
     }
     
     public String getMode()
     {
         return mode;
     }
     /**
      This must be called every time we change the text from the server side, 
      since a new div object is is created on this client side each time making us lose any properties we've stored on the div. 
      */
     private void attachEditorJS()
     {
       //TODO we might be able to do this with setting a javascript member, which might get automatically added each time the div is created..
         contentWText.doJavaScript(contentWText.getJsRef()+".editor = ace.edit("+contentWText.getJsRef()+");"
                 + contentWText.getJsRef()+".editor.setTheme('ace/theme/"+theme+"');"
                + contentWText.getJsRef()+".editor.setShowPrintMargin(false);" 
                + contentWText.getJsRef()+".editor.setReadOnly("+readOnly+");"
                 );
     }
     
     //========================Events=============================
     public JSignal1<String> save() { return save; }
 
     
 }
