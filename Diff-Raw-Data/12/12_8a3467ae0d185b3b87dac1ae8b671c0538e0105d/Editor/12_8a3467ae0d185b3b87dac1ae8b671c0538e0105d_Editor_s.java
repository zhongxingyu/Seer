 /*
  * Created on 30.06.2006
  */
 package com.aavu.client.widget.RichText2;
 
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 
 /**
  * This is the main class of the rich text editor. A typical use is:
  * <code><pre>
  *      Editor editor = new Editor();
  *      editor.setTextPixelSize(400, 300);
  *      RootPanel.get().add(editor);
  *      editor.setHTML("My editor text.");
  * </pre></code>
  * The editor is using the follow stylenames:
  * <ul>
  * <li>Toolbar
  * <li>ImageButton
  * <li>ImageButton-over
  * <li>ImageButton-pressed
  * <li>Tooltip
  * </ul>
  * 
  * <!--webbot bot="Include" U-Include="../../../../../../_private/google-banner.html" TAG="BODY" -->
  * @author Volker Berlin
  */
 public class Editor extends Composite /* implements HasHTML*/{
 
 	private String width;
 	private String height;
 	private String skin;
 	private String baseUrl;
 	private Frame text;
 	private Element textElement;
 	private VerticalPanel panel;
 
 	
 	private HorizontalPanel toolbar = new HorizontalPanel();
 	private boolean inited = false;
 	private boolean loadedOk = false;
 
     private KeyCodeEventListener listener;
 
 
 	/**
 	 * Constructor of a Rich Text Editor
 	 */
 	public Editor(){
 		panel = new VerticalPanel();			 
 		initWidget(panel);
 	}
 
 
 
 	/**
 	 * This method is called when the editor becomes attached to the browser's
 	 * document. In this method all elements are created with the current skin.
 	 * 
 	 */
 	protected void onLoad(){
 		super.onLoad();		
 		System.out.println("on load");
 		if(!inited ){
 
 			System.out.println("do init");
 
 			initFrame();
 
 			if(skin == null || skin.length() == 0){
 				skin = "default";
 			}
 			baseUrl = "richtext/skins/" + skin + "/";
 			setStylesheet(baseUrl);
 
 
 			ListBox fontname = new ListBox();
 			fontname.addItem("Arial");
 			fontname.addItem("Courier New");
 			fontname.addItem("Times New Roman");
 			fontname.addChangeListener(new ChangeListener(){
 				public void onChange(Widget sender) {
 					String value = ((ListBox)sender).getItemText(((ListBox)sender).getSelectedIndex());
 					format(textElement, "FontName", value);
 				}
 			});
 			toolbar.add(fontname);
 
 			ListBox fontsize = new ListBox();
 			addItem(fontsize,"8","1");
 			addItem(fontsize,"10","2");
 			addItem(fontsize,"12","3");
 			addItem(fontsize,"14","4");
 			addItem(fontsize,"18","5");
 			addItem(fontsize,"24","6");
 			addItem(fontsize,"36","7");
 			fontsize.addChangeListener(new ChangeListener(){
 				public void onChange(Widget sender) {
 					Element child = DOM.getChild( sender.getElement(), ((ListBox)sender).getSelectedIndex());
 					String value = DOM.getAttribute(child, "value");
 					format(textElement, "FontSize", value);
 				}
 			});
 			toolbar.add(fontsize);
 
 			addImages();
 
 			toolbar.setStyleName("Toolbar");
 			toolbar.setVisible(true);
 
 			//
 			//
 			panel.clear();
 			panel.add(toolbar);
 			panel.add(text);
 
 			inited = true;
 		}
 
 	}
 	/**
 	 * Default toolbar. 
 	 * Over-ride with what you want.
 	 *
 	 */
 	protected void addImages(){
 		addFormatImageButton("bold.gif", "Bold", "Bold");
 		addFormatImageButton("italic.gif", "Italic", "Italic");
 		addFormatImageButton("underline.gif", "Underline", "Underline");
 
 		addFormatImageButton("left.gif", "Left", "JustifyLeft");
 		addFormatImageButton("center.gif", "Center", "JustifyCenter");
 		addFormatImageButton("right.gif", "Right", "JustifyRight");
 
 		addFormatImageButton("numbered.gif", "Numbered List", "InsertOrderedList");        
 		addFormatImageButton("list.gif", "Bullet List", "InsertUnorderedList");
 		addFormatImageButton("outdent.gif", "Outdent", "Outdent");        
 		addFormatImageButton("indent.gif", "Indent", "Indent");        
 
 		addFormatImageButton("hr.gif", "Horizontal Rule", "InsertHorizontalRule");
 
 	}
 
 	/**
 	 * Sets the text's size, in pixels, not including decorations such as
 	 * border, margin, padding and the toolbar.
 	 * 
 	 * @param width the text's new width, in pixels
 	 * @param height the text's new height, in pixels
 	 */
 	public void setTextSize(String width, String height){
 		this.width = width;
 		this.height = height;
 		if(text != null){
 			text.setWidth(width);
 			text.setHeight(height);
 		}
 	}
 

 	public void setFocus(boolean b) {
		text.setFocus(b);
 	}
 
 	/**
 	 * Set the skin of the editor. If not set then the default skin is used.
 	 * This method must be called before the editor is added to the parent.
 	 * @param skin the name of the skin.
 	 */
 	void setSkin(String skin){
 		this.skin = skin;
 	}
 
 
 	/**
 	 * Create the IFrame that hold the editable text.
 	 */
 
 	private void initFrame(){
 
 
 		System.out.println("111");
 		if (isFrameLoaded(textElement)){
 			//if frame is loaded, then set it to editor
 			//setDesignMode(text.getElement(), true);
 			System.out.println("2222");
 
 
 
 			if(!loadedOk){
 				System.out.println("setEdit");
 				setEditable(true);
 
 				System.out.println("attach events to textElement");
 				attachEventsToFrame(textElement);							
 				
 			}
 			loadedOk  = true;
 
 
 			//doOnInitSucess();
 
 
 //			final RichTextArea rta = this;
 //			attachEventsToFrame(rta, editor.getElement(), isAuotoResizeHeight());
 ////			Window.alert("initFrame, will set html to " + this.html);		  		
 //			if(this.html != null){
 //			setHtmlToFrame();
 //			}else{
 //			//setHTML will fire the onDisplay event, donot need fire again, so that
 //			//this statement was written in the 'else'.
 //			onDisplayChanged();
 //			}
 		}else{
 			System.out.println("151");
 			text = new Frame();
 			text.setWidth(width);
 			text.setHeight(height);			
 			textElement = text.getElement();			
 
 			//run the second half of the initialization above
 			//
 			Timer t = new Timer() {
 				public void run() {
 					initFrame();
 				}
 			};
 			t.schedule(500);
 		}
 
 	}
 
 	/**
 	 * lifecycle call for extended classes
 	 * over-ride if you need to know when we're setup
 	 */
 	protected void doOnInitSucess(){
 		//do nothing
 	}
 
 
 	/**
 	 * Add a ImageButton to the toolbar with a standard edit command.
 	 * @param url The Imagename relative to the skin.
 	 * @param tooltip The tooltip of the button.
 	 * @param command The edit command
 	 */	
 	protected void addFormatImageButton(String url, String tooltip, final String command){
 		addFormatImageButton(url, tooltip, command,new ClickListener(){
 			public void onClick(Widget sender) {
 				format(textElement, command, null);
 			}
 		});
 
 	}
 	protected void addFormatImageButton(String url, String tooltip, final String command,ClickListener cl){
 		Image image = new ImageButton( baseUrl + url, tooltip);
 		image.addClickListener(cl);
 		toolbar.add(image);
 	}
 
 	/**
 	 * Get the current HTML text of the editor.
 	 * @see #setHTML(String)
 	 */
 	public String getHTML(){
 		return getHTML(textElement);
 	}
 
 
 	native static String getHTML(Element element)/*-{
         var doc = element.contentWindow.document;
         return doc.documentElement.innerHTML;
     }-*/;
 
 
 	/**
 	 * Sets this editor's contents via HTML. This method can be call first after it was add to the parent.
 	 * 
 	 * @param html the editor's new HTML
 	 */
 	public void setHTML(final String html) {
 		if (isFrameLoaded(textElement)){
 			System.out.println("really set");
 			setHTML(textElement, html);
 		}else{			
 			Timer t = new Timer() {
 				public void run() {
 					setHTML(html);
 				}
 			};
 			t.schedule(500);
 		}
 
 	}
 
 
 	native static void setHTML(Element element, String html)/*-{
         var doc = element.contentWindow.document;
         if (!html)
             html = '';
         doc.open(); 
         doc.write(html); 
         doc.close();
     }-*/;
 
 
 	/**
 	 * Enable or disable the edit mode. Depending on the mode the toolbar is visible or not.
 	 * @param editable true means that is can be edit.
 	 */
 	public void setEditable(boolean editable){
 		System.out.println("set edit "+editable);
 
 		if(editable){
 			setEditable(textElement,editable);
 		}else{
 			setEditable(textElement,editable);
 
 			String html = getHTML();
 //			Element old = textElement;
 //			initFrame();
 //			replaceNode(old, textElement);
 //			setHTML(html);
 		}
 		//toolbar.setVisible(editable);
 		//edit.setVisible(!editable);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param command
 	 * @param option
 	 */
 	protected void format(String command, String option){
 		
 		format(textElement,command,option);
 	}
 	
 	public JavaScriptObject getExpandedSelection(){
 		System.out.println("expand");
 		expandSelectionJS(textElement);
 		return getSelectionRange();
 	}
 	
 	
 	native static void expandSelectionJS(Element element)/*-{
 		
 		//element.contentWindow.focus();
 		//var doc = element.contentWindow.document;		  
 		//var range = doc.selection.createRange();
 		//var suc = range.expand("word");
 		
 		
 	//	alert(range.htmlText);
 	//	alert(range.text);
     //	alert(suc+" "+range.text);
 	//	alert("doc.selection: "+doc.selection);
 	//	doc.selection.setSelectionRange(2, 4);
 	//  doc.selection = range;
 	//	return range.text;
 	//  doc.execCommand("CreateLink",false,"#cathedral");	
     }-*/;
 	
 	native static void setEditable(Element element,boolean b)/*-{
 
         var doc = element.contentWindow.document;        
 
         element.contentWindow.focus();
 
 
         try {
         setTimeout(function(){
                 //
                 //crucial! running this code in IE kills the keyboard listener for some reason
                 //
         		if(!doc.all){
         			doc.designMode="On";
         			doc.body.contentEditable = true;
         		}
         	},1000);
         }catch(e){
         	alert("fail");
         }
 
     }-*/;
 
 
 	native static void replaceNode(Element oldElement, Element newElement)/*-{
         oldElement.parentNode.replaceChild(newElement, oldElement);
     }-*/;
 	
 	native static void format(Element editor, String command, String option)/*-{
         editor.contentWindow.focus();
         editor.contentWindow.document.execCommand(command, false, option);
     }-*/;	
 
 	/**
 	 * Add the style sheet from the skin to the page. 
 	 * @param baseUrl The baseURL of the skin.
 	 */
 	native void setStylesheet(String baseUrl)/*-{
         var css = $doc.createElement('link');
         css.rel = 'stylesheet';
         css.type = 'text/css';
         css.href = baseUrl + 'skin.css';
         $doc.documentElement.firstChild.appendChild(css);
     }-*/;
 
 
 	/**
 	 * A hack because currently the ListBox does not support the attribut VALUE.
 	 * @param list
 	 * @param display
 	 * @param value
 	 */
 	private static void addItem(ListBox list, String display, String value) {
 		Element option = DOM.createElement("OPTION");
 		DOM.setInnerText(option, display);
 		DOM.setAttribute(option, "value", value);
 		DOM.appendChild(list.getElement(), option);
 	}
 
 
 	private native boolean isFrameLoaded(Element editor) /*-{
 	try{		
     	if(editor != null && editor.contentWindow != null 
     		&& editor.contentWindow.document != null  
     		&& editor.contentWindow.document.body != null){
 
     		return true;
     	}else{
     		return false;
     	}
     }catch(e){
     	alert("error occured while adjust whether frame is loaded.\n" + e.message);
     	return false;
     }
 }-*/;
 
 	public void keyEvent(JavaScriptObject o){
 		//	java.lang.Object;
 
 		String str = o+" "+(o instanceof Event);
 
 //		DOM.eventGetKeyCode((Event) o);
 
 		Window.alert(str);
 
 		System.out.println("o: "+o);
 		//System.out.println(o.getClass());
 		System.out.println(o instanceof Event);
 	}
 		
 	/**
 	 * called from javascript
 	 * 
 	 * pass to overridable linkEventCallback(i)
 	 * 
 	 * @param i
 	 */
 	private void keyEvent(int i){
 		if(listener != null){
 			listener.keyCodeEvent(i);
 		}
 	}
 	private void muppet(int i){
 	//	System.out.println("MUPPET");
 	//	System.out.println(expandSelection());
 	}
 
 	public void setKeyEventlistener(KeyCodeEventListener listener) {
 		this.listener = listener;	
 	}
 	
 	private native void attachEventsToFrame(Element elem)/*-{
     var d = elem.contentWindow.document;
 
    	var callBackTarget = this;
     var handleEvent = function(arg){    		
 //    	alert("handle");
 //		alert("handle ctrl "+arg.ctrlKey);
 //		alert("ev code: "+arg.keyCode);
 //		alert("ev which: "+arg.which);	 				
 		var code = arg.which ? arg.which : arg.keyCode;
 //		alert("ev code: "+code);		
 		
 		if(arg.ctrlKey){											    	                															    	
     		callBackTarget.@com.aavu.client.widget.RichText2.Editor::keyEvent(I)(code);    		
     	}
     };
     var muppet = function(arg){    		
 		var code = 1;															    	                															    	
     	callBackTarget.@com.aavu.client.widget.RichText2.Editor::muppet(I)(code);
     };
 
     var _eventPatch = function(editor_id) {
 		var win, e;
 
 		try {
 			// Try selected instance first
 
 
 //
 //			win = elem.contentWindow;
 //			alert("win: "+win);
 //			alert("win.event: "+win.event);
 //			alert("oe: "+$wnd.event);
 //			if (win && win.event) {
 //				e = win.event;
 //
 //				alert("!e.tag "+!e.target);
 //				if (!e.target)
 //					e.target = e.srcElement;
 //				alert("e: "+e);
 //				alert("e src: "+e.srcElement);
 //				handleEvent(e);
 //				return;
 //			}
 
 
 			for (var i=0; i<$doc.frames.length; i++) {
 
 //				alert("i "+i);
  	                    if ($doc.frames[i].event) {
  	                                var event = $doc.frames[i].event;
 //				alert("ev: "+event);
  	                                if (!event.target)
  	                                        event.target = event.srcElement;
 //				alert("ev ctrl: "+event.ctrlKey);
 //				alert("ev code: "+event.keyCode);
 //				alert("ev which: "+event.which);
 				//alert("ev keycode: "+(evt.which ? evt.which : evt.keyCode));				
  	                                handleEvent(event);
  	                                return;
 						}
 			}
 
 
 		} catch (ex) {
 			alert(ex);
 			// Ignore error if iframe is pointing to external URL
 		}
 	};
 
     function addEvent(o, n, h) {
 		if (o.attachEvent)
 			o.attachEvent("on" + n, h);
 		else
 			o.addEventListener(n, h, false);
 	};
 
 
 	if (document.all) {	
 		addEvent(d, "mouseup", muppet);
 		addEvent(d, "keypress", _eventPatch);		
 	} else {
 		addEvent(d, "mouseup", muppet);
 		addEvent(d, "keypress", handleEvent);
 	}
 }-*/;
 
 	
 
 	public void setSelectionRange(JavaScriptObject range){
 		nativeSetSelectionRange(textElement, range);
 	}
 	private native void nativeSetSelectionRange(Element e, JavaScriptObject range) /*-{
 	    if(range == null){
 	        return;
 	    }
 	    if(document.all){
 	        range.select();
 	    }else{
 			var sel = e.contentWindow.getSelection();
 			sel.removeAllRanges();
 			sel.addRange(range);
 	    }
 	}-*/;
 	public JavaScriptObject getSelectionRange(){
 		return nativeGetSelectionRange(textElement);
 	}
 	
 	
 	//http://www.quirksmode.org/js/selected.html#
 	private native JavaScriptObject nativeGetSelection()/*-{
 	function getSel()
 	{
 		var txt = '';
 		var foundIn = '';
 		if (window.getSelection)
 		{
 			txt = window.getSelection();
 			foundIn = 'window.getSelection()';
 		}
 		else if (document.getSelection)
 		{
 			txt = document.getSelection();
 			foundIn = 'document.getSelection()';
 		}
 		else if (document.selection)
 		{
 			txt = document.selection.createRange().text;
 			foundIn = 'document.selection.createRange()';
 		}
 		else return;
 		document.forms[0].selectedtext.value = 'Found in: ' + foundIn + '\n' + txt;
 	}
 		}-*/;
 	
 	private native JavaScriptObject nativeGetSelectionRange(Element e)/*-{		
 		try{
 			if (document.all) {			
 		        var sel = e.contentWindow.document.selection;		    
 				return sel.createRange();
 			} else {
 		        var sel = e.contentWindow.getSelection();
 				e.contentWindow.focus();
 				if (typeof sel != "undefined") {
 					try {
 						return sel.getRangeAt(0);
 					} catch(e) {
 						return e.contentWindow.document.createRange();
 					}
 				} else {
 					return e.contentWindow.document.createRange();
 				}
 			}
 		}catch(e){
 			alert("error occured while get the selection. Please contact with vendor.\n" + e.message);
 		}
 	}-*/;
 	
 
 }
