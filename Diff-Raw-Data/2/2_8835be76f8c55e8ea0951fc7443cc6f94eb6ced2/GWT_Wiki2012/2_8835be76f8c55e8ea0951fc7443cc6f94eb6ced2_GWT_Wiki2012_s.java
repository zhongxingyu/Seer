 package com.akjava.gwt.wiki.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.akjava.gwt.bootstrapwiki.client.BootstrapHtmlDocumentConverter;
 import com.akjava.gwt.lib.client.GWTHTMLUtils;
 import com.akjava.gwt.lib.client.IStorageControler;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.lib.client.StorageControler;
 import com.akjava.gwt.lib.client.StorageException;
 import com.akjava.gwt.lib.client.ValueUtils;
 import com.akjava.gwt.wiki.client.ui.AlertInput;
 import com.akjava.gwt.wiki.client.ui.AnchorInput;
 import com.akjava.gwt.wiki.client.ui.BtnInput;
 import com.akjava.gwt.wiki.client.ui.LabelInput;
 import com.akjava.gwt.wiki.client.ui.ProgressInput;
 import com.akjava.gwt.wiki.client.ui.TemplatePanel;
 import com.akjava.wiki.client.core.RootDocument;
 import com.akjava.wiki.client.core.StringLineDocumentBuilder;
 import com.akjava.wiki.client.core.WikiException;
 import com.akjava.wiki.client.keyword.Keyword;
 import com.akjava.wiki.client.util.TagUtil;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FormHandler;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormSubmitEvent;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class GWT_Wiki2012 implements EntryPoint {
	IStorageControler storageControler=new StorageControler();
 	  private HTML htmlLabel;
 	  private TextArea textArea;
 	  private TextArea textHtmlArea;
 	  private ScrollPanel htmlFolder;
 	  
 	  private int historyIndex;
 	  private List<String> textHistory=new ArrayList<String>();
 	  
 	 // private String defaultValueInputId;
 	 // private String outputTextHiddenId;
 	//  private String outputHtmlHiddenId;
 	  
 	  public static final String PEOPERTY_READ_ONLY="wiki2012readonly";
 	  public static final String PEOPERTY_SESSION_ID="wiki2012sessionid";
 	  
 	  public static final String PEOPERTY_DEFAULT_ID="wiki2012defaultid";
 	  public static final String PEOPERTY_OUTPUT_TEXT="wiki2012outputtext";
 	  public static final String PEOPERTY_OUTPUT_HTML="wiki2012outputhtml";
 	  
 	  public static final String KEY_SESSION="wiki2012session_value";//for session storage
 	  public static final String KEY_LAST_SESSION_ID="wiki2012session_last_session_id";
 	  /**
 	     * This is the entry point method.
 	     */
 	    public void onModuleLoad() {
 	     //parse from html
 	    	LogUtils.log("onModuleLoad");
 	    	/*
 	    	defaultValueInputId=ValueUtils.getFormValueById(PEOPERTY_DEFAULT_ID, "");
 	    	outputTextHiddenId=ValueUtils.getFormValueById(PEOPERTY_OUTPUT_TEXT, "");
 	    	outputHtmlHiddenId=ValueUtils.getFormValueById(PEOPERTY_OUTPUT_HTML, "");
 	    	
 	    	LogUtils.log("inited:"+defaultValueInputId+","+outputTextHiddenId+","+outputHtmlHiddenId);
 	    	*/
 	    
 	    	//now work
 	    	/*
 	    	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 				@Override
 				public void execute() {
 					editData();
 				}
 			});
 			*/
 	  	  editData();
 	       
 	    }
 	    private boolean readOnly;
 	    public void editData(){
 	    	
 	      RootPanel panel=RootPanel.get(PEOPERTY_READ_ONLY);
 	      if(panel!=null){
 	    	  readOnly=true;
 	      }
 	  	  HorizontalPanel trueRoot=new HorizontalPanel();
 	  	  trueRoot.setSpacing(16);
 	  	
 	  	  final FormPanel formPanel=new FormPanel();
 	  	  	
 	  	  
 	        VerticalPanel verticalPanel=new VerticalPanel();
 	        formPanel.setWidget(verticalPanel);
 	        formPanel.setMethod("post");
 	        formPanel.setAction("exec");
 	        trueRoot.add(formPanel);
 	        
 	        formPanel.addFormHandler(new FormHandler() {
 	            public void onSubmit(FormSubmitEvent event) {
 	            // GWT.log(event.getSource().toString(), null);
 	            }
 
 	            public void onSubmitComplete(FormSubmitCompleteEvent event) {
 	             Window.open("Main.html","_self",null);
 	              //Window.alert(event.getResults());
 	            }
 	          });
 
 	        
 	        
 	        Button update = new Button("Push to Wiki2Html!", new ClickListener() {
 	            public void onClick(Widget sender) {
 	          	//doWiki(); 
 	            }
 	          });
 
 	        
 	        HorizontalPanel buttons=new HorizontalPanel();
 	        if(readOnly){
 	        	buttons.setVisible(false);
 	        }
 	        TextInsertTarget target=new TextInsertTarget() {
 				@Override
 				public void insertText(String text) {
 					insertBetweenSelectionText(text,"");
 				}
 
 				@Override
 				public void tagText(String header, String footer) {
 					insertBetweenSelectionText(header,footer);
 				}
 
 				@Override
 				public void insertAndCursorCenter(String header, String footer) {
 					TextSelection selection=getTextSelection();
 					String before=selection.getSelectionBefore()+header+selection.getSelection();
 					selection.setText(before+footer+selection.getSelectionAfter());
 					selection.setCursorPos(before.length());
 				}
 			};
 			
 			  TextInsertTarget justInsertTarget=new TextInsertTarget() {
 					@Override
 					public void insertText(String header) {
 						TextSelection selection=getTextSelection();
 						String before=selection.getSelectionBefore()+header+selection.getSelection();
 						selection.setText(before+selection.getSelectionAfter());
 						selection.setCursorPos(before.length()+header.length());
 					}
 
 					@Override
 					public void tagText(String header, String footer) {
 						LogUtils.log("not supported");
 					}
 
 					@Override
 					public void insertAndCursorCenter(String header, String footer) {
 						LogUtils.log("not supported");
 					}
 				};
 				
 	        LabelInput labelInput=new LabelInput(target);
 	        buttons.add(labelInput);
 	        
 	        AlertInput alertInput=new AlertInput(target);
 	        buttons.add(alertInput);
 	        
 	        //verticalPanel.add(update);
 	        verticalPanel.add(buttons);
 	        
 	        Button codeBt=new Button("code");
 	        codeBt.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					insertBetweenSelectionText("<code>","</code>");
 				}
 			});
 	        buttons.add(codeBt);
 	        
 	        Button boldBt=new Button("bold");
 	        boldBt.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					insertBetweenSelectionText("''","''");
 				}
 			});
 	        buttons.add(boldBt);
 	       
 	        buttons.add(insertTextButton("#div(pull-right)\n","\n#text\n"));
 	     
 	        
 	        buttons.add(insertTextButton("#text\n",""));
 	        buttons.add(insertTextButton("#title(",")"));
 	        buttons.add(insertTextButton("#code\n","\n#text"));
 	        buttons.add(insertTextButton("#tag\n","\n#text"));
 	        buttons.add(new AnchorInput(target));
 	        
 	       
 	        
 	        
 	        HorizontalPanel buttons2=new HorizontalPanel();
 	        if(readOnly){
 	        	buttons2.setVisible(false);
 	        }
 	        verticalPanel.add(buttons2);
 	        
 	        Button smallBt=new Button("small");
 	        smallBt.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					insertBetweenSelectionText("<small>","</small>");
 				}
 			});
 	        buttons2.add(smallBt);
 	        
 	        Button escape=new Button("&escape");
 	        escape.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					escape();
 				}
 
 				private void escape() {
 					TextSelection selection=getTextSelection();
 			    	String selectText=selection.getSelection();
 			    	selectText=selectText.replace("&", "&amp;");
 			    	selectText=selectText.replace("<", "&lt;");
 			    	selectText=selectText.replace(">", "&gt;");
 			    	
 			    	selection.replace(selectText);
 		    		
 		    		selection.setCursorPos(selection.getStart());
 		    		
 			    	
 				}
 			});
 	        buttons2.add(escape);
 	        buttons2.add(new BtnInput(target));
 	        
 	        Button untagBt=new Button("untag");
 	        untagBt.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					untag();
 				}
 			});
 	       
 	        buttons2.add(new ProgressInput(justInsertTarget));
 	        
 	        buttons2.add(untagBt);
 	        
 	        textArea = new TextArea();
 	        if(readOnly){
 	        	textArea.setEnabled(false);
 	        }
 	        
 	        textArea.setName("textArea");
 	        textArea.setStylePrimaryName("textbg");
 	  	    textArea.setWidth("560px");
 	        textArea.setHeight("700px");
 	        textArea.addKeyUpHandler(new KeyUpHandler() {
 				@Override
 				public void onKeyUp(KeyUpEvent event) {
 					if(event.getNativeKeyCode()==KeyCodes.KEY_ENTER){
 					doWiki();	
 					}else{
 					convertWikiToHtml();//not need?
 					syncOutput();
 					}
 				}
 			});
 	        textArea.addChangeHandler(new ChangeHandler() {
 				
 				@Override
 				public void onChange(ChangeEvent event) {
 					GWT.log("changed:");
 					doWiki();
 				}
 			});
 	        textArea.addMouseOutHandler(new MouseOutHandler() {
 				
 				@Override
 				public void onMouseOut(MouseOutEvent event) {
 					doWiki();
 				}
 			});
 	        verticalPanel.add(textArea);
 	        
 	        TabPanel tabPanel = new TabPanel();
 	        
 	       
 	        if(readOnly){
 	        	tabPanel.getDeckPanel().setHeight("670px");	
 	        }else{
 	        tabPanel.getDeckPanel().setHeight("730px");
 	        }
 	        tabPanel.getDeckPanel().setWidth("960px");
 	       
 	        htmlLabel = new HTML();
 	        
 	        htmlFolder = new ScrollPanel(htmlLabel);
 	      
 	       
 	        htmlFolder.setHeight("700px");
 	        
 	  	  tabPanel.add(htmlFolder,"renderd-html");
 	  	
 	  	  if(!readOnly){
 	  	  tabPanel.add(new TemplatePanel(),"template");
 	  	  }
 	       
 	        textHtmlArea = new TextArea();
 	        //textHtmlArea.setWidth("300px");
 	        
 	        tabPanel.add(textHtmlArea,"text-html");
 	        tabPanel.selectTab(0);
 	        trueRoot.add(tabPanel);
 	        
 	        HorizontalPanel bottomButtons=new HorizontalPanel();
 	        if(readOnly){
 	        	bottomButtons.setVisible(false);
 	  	  	}
 	        verticalPanel.add(bottomButtons);
 	        bottomButtons.add(new Button("Clear", new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					setTextArea(textArea,"");
 					
 					doWiki();
 				}
 			}));
 	        bottomButtons.add(new Button("Undo", new ClickHandler() {//TODO better
 				@Override
 				public void onClick(ClickEvent event) {
 					
 					int undoIndex=historyIndex-1;
 					if(undoIndex<0){
 						return;
 					}
 						
 					String text=textHistory.get(undoIndex);
 					GWT.log("size:"+textHistory.size()+",index="+historyIndex+",text="+text);
 					textArea.setText(text);
 					lastHistory=text;
 					historyIndex--;
 					if(historyIndex<0){
 						historyIndex=0;
 					}
 					
 				}
 			}));
 	        bottomButtons.add(new Button("Redo", new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					if(historyIndex<textHistory.size()){
 						int redoIndex=historyIndex+1;
 						if(redoIndex>=textHistory.size()){
 							return;
 						}
 						String text=textHistory.get(redoIndex);
 						GWT.log("size:"+textHistory.size()+",index="+historyIndex+",text="+text);
 						textArea.setText(text);
 						lastHistory=text;
 						historyIndex++;
 						if(historyIndex>=textHistory.size()){
 							historyIndex=textHistory.size()-1;
 						}
 					}
 				}
 			}));
 	        
 	        autoWiki = new CheckBox();
 	        bottomButtons.add(autoWiki);
 	        autoWiki.setValue(true);
 	        bottomButtons.add(new Label("Auto wiki"));
 	        
 	        bottomButtons.add(new Button("Manual Wiki",new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					execWiki();
 				}
 			}));
 	      
 	        
 	        
 	       
 	       Frame frame=new Frame("manual.html");
 	      
 	       frame.setHeight("640x");
 	      // trueRoot.add(frame);
 	       tabPanel.add(frame,"Help");
 	      
 	       GWTHTMLUtils.getPanelIfExist("gwtwikicontainer").add(trueRoot);
 	        
 	        //load default value from input
 	       // if(!defaultValueInputId.isEmpty()){
 	       
 	        String session_id=ValueUtils.getFormValueById(PEOPERTY_SESSION_ID,"");
 	        if(!session_id.isEmpty()){
 	        	try {
 	        		String lastSessionId = storageControler.getValue(KEY_LAST_SESSION_ID, "");
 	        		if(session_id!=lastSessionId){
 	        			//new situation
 	        			String data=ValueUtils.getFormValueById(PEOPERTY_DEFAULT_ID, "");
 	    		        textArea.setText(data);
 	    		        storageControler.setValue(KEY_LAST_SESSION_ID, session_id);//mark used
 	        		}else{
 	        			String lastModified=storageControler.getValue(KEY_SESSION, "");
 	        			textArea.setText(lastModified);
 	        		}
 				} catch (StorageException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        	
 	        }else{
 	        	String data=ValueUtils.getFormValueById(PEOPERTY_DEFAULT_ID, "");
 		        textArea.setText(data);
 	        }
 	       
 	        
 	        //}
 	        
 	        
 	        
 	        
 	        /*
 	         * ignore auto resume
 	        String historyText=null;
 			try {
 				historyText = storageControler.getValue("history", null);
 			} catch (StorageException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	        if(historyText!=null){
 	        	textArea.setText(historyText);
 	        }
 	        */
 	        
 	        addHistory(textArea.getText());
 	        //doWiki();
 	        doWiki();
 	    }
 	    
 	    
 	    
 	    private String lastWik;
 	    public void doWiki(){
 	    	if(!autoWiki.getValue()){
 	    		return;
 	    	}else{
 	    		execWiki();
 	    	}
 	    	syncOutput();
 	    }
 	    
 	    public void syncOutput(){
 	    	String text=textArea.getText();
 	    	String html=textHtmlArea.getText();
 	    	//if(!outputTextHiddenId.isEmpty()){
 	    		RootPanel panel=RootPanel.get(PEOPERTY_OUTPUT_TEXT);
 	    		if(panel!=null){
 	    			panel.getElement().setAttribute("value", text);
 	    		}
 	    		//RootPanel.get(outputTextHiddenId).getElement().setAttribute("value", text);
 	    	//}
 	    		RootPanel htmlpanel=RootPanel.get(PEOPERTY_OUTPUT_HTML);
 	    		if(htmlpanel!=null){
 	    			htmlpanel.getElement().setAttribute("value", html);
 	    		}
 	    		/*
 	    	if(!outputHtmlHiddenId.isEmpty()){
 	    		RootPanel.get(outputHtmlHiddenId).getElement().setAttribute("value", html);
 	    	}*/
 	    		
 	    		try {
 					storageControler.setValue(KEY_SESSION, textArea.getText());
 				} catch (StorageException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	    }
 	    
 	    public String convertWikiToHtml(){
 	    	//StringLineDocumentBuilder builder=new StringLineDocumentBuilder();
 	    	/*
 	    	 * converter first
 	    	 */
 	    	 BootstrapHtmlDocumentConverter converter=new BootstrapHtmlDocumentConverter();
 	    	 
 	     	  RootDocument document;
 			try {
 				document = StringLineDocumentBuilder.createDocument(StringLineDocumentBuilder.splitLine(textArea.getText()),"/test.html");
 				String headerText="";
 		     	  String footerText="";
 		     	
 		     	 
 		           converter.setHeaderText(headerText);
 		           converter.setFooterText(footerText);
 		           converter.setPrettyPrint(true);
 		           
 		         //  converter.setIconHost("http://www.xucker.jpn.org");
 		        //   converter.setImageHost("http://www.xucker.jpn.org");
 		        //   converter.setStartImage("/img2/");
 		           
 		           
 		           Keyword gwt=new Keyword("GWT","http://code.google.com/webtoolkit/");
 		           Keyword[] keywords={gwt};
 		          // KeyWordUtils.insertKeyword(document,keywords,null,new String[]{""});
 		           String result= converter.convert(document);
 		          
 		          // GWT.log(result, null);
 		           return result;
 		          
 		           //htmlLabel.removeFromParent();
 		          
 			} catch (WikiException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 	    }
 	    
 	    public void execWiki(){
 	    	
 	    	String result=convertWikiToHtml();
 	    	
 	    	System.out.println("input:"+textArea.getText());
 	    	System.out.println("result:"+result);
 	    	 htmlLabel.setHTML(result);
 	           
 		        
 	          // htmlFolder.add(htmlLabel);
 	           textHtmlArea.setText(result);
 	           if(!result.equals(lastWik)){
 	        	   addHistory(textArea.getText());
 	        	   lastWik=result;
 	           }
 	     	  
 	     	  
 			
 	  	  
 	    }
 	    private void untag(){
 	    	TextSelection selection=getTextSelection();
 	    	String selectText=selection.getSelection();
 	    	if(selectText.startsWith("''")){
 	    		int end=selectText.indexOf("''",2);
 	    		if(end!=-1){
 	    			String contain=selectText.substring(2,end);
 		    		String remain=selectText.substring(end+2);
 		    		selection.replace(contain+remain);
 		    		
 		    		selection.setCursorPos(selection.getStart());
 	    		}
 	    	}else{
 	    	
 	    	String tag=TagUtil.getTag(selectText);
 	    	if(tag!=null){
 	    		String contain=TagUtil.getContain(tag);
 	    		String remain=selectText.substring(tag.length());
 	    		
 	    		selection.replace(contain+remain);
 	    		
 	    		selection.setCursorPos(selection.getStart());
 	    	}
 	    	}
 	    }
 	    
 	    private Button insertTextButton(final String header,final String footer){
 	        final Button tagButton=new Button();
 	       // tagButton.setWidth("80px");
 	        tagButton.setText(header);
 	        tagButton.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					TextSelection selection=getTextSelection();
 					String before=selection.getSelectionBefore()+header+selection.getSelection();
 					selection.setText(before+footer+selection.getSelectionAfter());
 					selection.setCursorPos(before.length());
 				}
 			});
 	        
 	        return tagButton;
 	    }
 	    
 	    private void insertBetweenSelectionText(String header,String footer){
 	  	 // dosubmit=false;
 	  		try{
 	  		if(textArea.getSelectedText()==null || textArea.getSelectedText().equals("")){
 	  			return;
 	  		}}catch(Exception e){
 	  			return;
 	  		}
 	  		int pos=textArea.getCursorPos();
 	  		int ch=textArea.getText().charAt(pos);
 	  		int len=textArea.getSelectionLength();
 	  		if(ch==13){
 	  			pos+=2;
 	  		}
 	  		
 	  		
 	  		String realSelect=textArea.getText().substring(pos,pos+len);
 	  		//GWT.log("realSelect:"+realSelect+":len"+realSelect.length(), null);
 	  		//String select=textArea.getSelectedText();
 	  		//GWT.log("select:"+select+":len"+select.length(), null);
 	  		//GWT.log("ch:"+ch+":len"+textArea.getText().length(), null);
 	  		//GWT.log(""+pos+":"+textArea.getText(), null);
 	  		String h=textArea.getText().substring(0,pos);
 	  		String f=textArea.getText().substring(pos+textArea.getSelectionLength());
 	  		
 	  		setTextArea(textArea, h+header+realSelect+footer+f);
 	  		
 	  		textArea.setCursorPos((h+header+realSelect).length());
 	  		textArea.setFocus(true);
 	    }
 	    
 	    public TextSelection getTextSelection(){
 	    	try{
 	    		GWT.log("selection:"+textArea.getSelectedText());
 		  		if(textArea.getSelectedText()==null ){
 		  			return null;
 		  		}}catch(Exception e){
 		  			return null;
 		  		}
 		  		int pos=textArea.getCursorPos();
 		  		if(pos==textArea.getText().length()){
 		  			return new TextSelection(textArea.getText().length(),textArea.getText().length(),textArea);
 		  		}
 		  		GWT.log("pos:"+pos);
 		  		int ch=textArea.getText().charAt(pos);
 		  		int len=textArea.getSelectionLength();
 		  		GWT.log("ch:"+ch+","+len);
 		  		if(ch==13){
 		  			pos+=2;
 		  		}
 		  		GWT.log("pos:"+pos);
 		  		
 		  		
 		  		//String realSelect=textArea.getText().substring(pos,pos+len);
 		  		return new TextSelection(pos,pos+len,textArea);
 	    }
 	    
 	    public class TextSelection{
 	    	TextArea targetTextArea;
 	    	public TextSelection(int start,int end,TextArea text){
 	    		this.start=start;
 	    		this.end=end;
 	    		this.targetTextArea=text;
 	    	}
 	    	public void replace(String replace) {
 				setText(getSelectionBefore()+replace+getSelectionAfter());
 			}
 			int start;
 	    	int end;
 	
 			public int getStart() {
 				return start;
 			}
 			public void setStart(int start) {
 				this.start = start;
 			}
 			public int getEnd() {
 				return end;
 			}
 			public void setEnd(int end) {
 				this.end = end;
 			}
 			public String getSelection() {
 				return textArea.getText().substring(start,end);
 			}
 			public String getSelectionBefore(){
 				return textArea.getText().substring(0,start);
 			}
 			public String getSelectionAfter(){
 				return textArea.getText().substring(end);
 			}
 			public void setText(String text){
 				setTextArea(targetTextArea,text);
 				
 			}
 			public void setFocus(){
 				targetTextArea.setFocus(true);
 			}
 			public void setCursorPos(int pos){
 				targetTextArea.setCursorPos(pos);
 			}
 			
 	    }
 	    
 	    public void setTextArea(TextArea targetTextArea,String text){
 	    	targetTextArea.setText(text);
 			addHistory(text);
 	    }
 	    
 	    private String lastHistory;
 		private CheckBox autoWiki;
 	    public void addHistory(String text){
 	    	if(text.equals(lastHistory)){
 	    		return;
 	    	}
 	    	textHistory.add(text);
 	    	lastHistory=text;
 	    	GWT.log("add history:"+text);
 	    	if(textHistory.size()>1000){
 	    		textHistory.remove(0);
 	    	}
 	    	historyIndex=textHistory.size()-1;
 	    }
 }
