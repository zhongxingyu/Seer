 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Aug 19, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk.view.document.edit;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLDocument;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeNotifying;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.otrunk.view.OTObjectListViewer;
 import org.concord.otrunk.view.document.OTCompoundDoc;
 import org.concord.otrunk.view.document.OTDocument;
 import org.concord.otrunk.view.document.OTDocumentEditorKit;
 import org.concord.otrunk.view.document.OTDocumentView;
 import org.concord.otrunk.view.document.OTHTMLFactory;
 
 
 /**
  * @author sfentress
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class OTDocumentEditView extends OTDocumentView implements
 		ChangeListener, HyperlinkListener, OTXHTMLView, OTViewEntryAware, ActionListener, KeyListener {
 	
 	boolean showMenuIcons = true;
 	
 	DocumentEditConfig documentEditConfig;
 
 	private OTDocumentEditorKit editorKit;
 
 	private JButton viewSrcButton;
 
 	public String getXHTMLText(OTObject otObject) {
 		cleanDuplicateDocRefs(otObject);
 
 		return super.getXHTMLText(otObject);
 	}
 	
 	public JComponent getComponent(OTObject otObject) {
 		this.otObject = otObject;
 
 		cleanDuplicateDocRefs(otObject);
 		
 		setup(otObject);
 		initTextAreaModel();
 		
 		((OTChangeNotifying) this.otObject).addOTChangeListener(this);
 		
 		if (documentConfig instanceof OTDocumentEditViewConfig){
 			documentEditConfig = (DocumentEditConfig) documentConfig;
 		}
 		
 		if (tabbedPane != null) {
 			tabbedPane.removeChangeListener(this);
 		}
 
 		// need to use the PfCDEditorKit
 		updateFormatedView();
 
 		setReloadOnViewEntryChange(true);
 		
 		if (documentConfig != null){
 			OTViewContainer thisViewContainer = getViewContainer();
 			if (thisViewContainer != null){
 				getViewContainer().setUpdateable(documentConfig.getViewContainerIsUpdateable());
 			}
 		}
 
 		// JScrollPane renderedScrollPane = new JScrollPane(previewComponent);
 		// renderedScrollPane.getViewport().setViewPosition(new Point(0,0));
 
 	//	JPanel wrapper = new JPanel(new BorderLayout());
 	//	wrapper.add(previewComponent, BorderLayout.CENTER);
 		
 		if (otObject instanceof OTCompoundDoc && ((OTCompoundDoc)otObject).getShowEditBar()){
 			JPanel wrapper = new JPanel(new BorderLayout());
 			JScrollPane pageScroller = new JScrollPane(previewComponent);
 			pageScroller.setPreferredSize(new Dimension(350,100));
 			((JEditorPane)previewComponent).setCaretPosition(0);
 			wrapper.add(pageScroller, BorderLayout.CENTER);
 			JPanel leftJustify = new JPanel(new FlowLayout(FlowLayout.LEADING));
 			
 			EditBar editBar = new EditBar();
 			leftJustify.add(editBar);
 			
 			viewSrcButton = new JButton("<src>");
 			editBar.add(Box.createHorizontalStrut(15));
 			editBar.add(viewSrcButton);
 			viewSrcButton.setActionCommand("viewSrc");
 			viewSrcButton.addActionListener(this);
 			
 			JButton addObjectButton = new JButton("Insert Object");
 			editBar.add(Box.createHorizontalStrut(15));
 			editBar.add(addObjectButton);
 			addObjectButton.setActionCommand("insertObject");
 			addObjectButton.addActionListener(this);
 			
 			wrapper.add(leftJustify, BorderLayout.NORTH);
 			return wrapper;
 		}
 	//	final JScrollPane pageScroller = new JScrollPane();
 	//	pageScroller.setViewportView(previewComponent);
 		return previewComponent;
 	}
 
 	private void cleanDuplicateDocRefs(OTObject otObject)
     {
 	    // clean up duplicate document references
 		if(otObject instanceof OTCompoundDoc){
 			OTCompoundDoc doc = (OTCompoundDoc) otObject;
 			OTObjectList documentRefs = doc.getDocumentRefsAsObjectList();
 			ArrayList uniqueRefs = new ArrayList();
 			for(int i=0; i<documentRefs.size(); i++){
 				OTObject ref = documentRefs.get(i);
 				// remove nulls, there shouldn't be any but just incase
 				if(ref == null){
 					documentRefs.remove(i);
 					i--;
 					continue;
 				}
 				
 				if(uniqueRefs.contains(ref)){
 					documentRefs.remove(i);
 					i--;
 					System.err.println("removing duplicate reference");
 					continue;
 				}				
 				uniqueRefs.add(ref);
 			}
 		}
     }
 
 	
 
 	public String updateFormatedView() {
 		if (pfObject == null)
 			return null;
 
 		// System.out.println(this+" updateFormatedView");
 
 		String markupLanguage = pfObject.getMarkupLanguage();
 		if (markupLanguage == null) {
 			markupLanguage = System.getProperty("org.concord.portfolio.markup",
 					null);
 		}
 
 		String bodyText = pfObject.getDocumentText();
 		bodyText = substituteIncludables(bodyText);
 		
 		int origCaretPos = 0;
 
 		// default to html viewer for now
 		// FIXME the handling of the plain markup is to test the new view entry
 		// code
 		// it isn't quite valid because plain text might have html chars in it
 		// so it will get parsed incorrectly.
 		if (markupLanguage == null
 				|| markupLanguage.equals(OTDocument.MARKUP_PFHTML)
 				|| markupLanguage.equals(OTDocument.MARKUP_PLAIN)) {
 			if (editorPane == null) {
 				editorPane = new MyJEditorPane();
 				OTHTMLFactory kitViewFactory = new OTHTMLFactory(this);
 				editorKit = new OTDocumentEditorKit(
 						kitViewFactory);
 				editorPane.setEditorKit(editorKit);
 				editorPane.setEditable(true);
 				editorPane.addHyperlinkListener(this);
 				
 				editorPane.addKeyListener(this);
 			//	editorPane.setContentType("text/plain") ;
 			}
 			
 			origCaretPos = editorPane.getCaretPosition();
 			bodyText = htmlizeText(bodyText);
 
 			if (documentConfig != null) {
 				
 				String css = documentConfig.getCssText();
 				
 				String XHTML_PREFIX = XHTML_PREFIX_START + css
 						+ XHTML_PREFIX_END;
 				bodyText = XHTML_PREFIX + bodyText + XHTML_SUFFIX;
 			}
 			// when this text is set it will recreate all the
 			// OTDocumentObjectViews, so we need to clear and
 			// close all the old panels first
 			removeAllSubViews();
 
 			editorPane.setText(bodyText);
 
 			previewComponent = editorPane;
 
 		} else {
 			System.err.println("xhtml markup not supported");
 		}
 
 		if (parsedTextArea == null) {
 			parsedTextArea = new JTextArea();
 		}
 		parsedTextArea.setText(bodyText);
 		
 		editorPane.setCaretPosition(origCaretPos);
 		
 		return bodyText;
 	}
 	
 	
 	
 	private OTObject getObjectToInsertFromUser()
 	{
 		OTObject otObj = null;
 		
 		otObj = OTObjectListViewer.showDialog(previewComponent,
 		        "Choose object to add", getFrameManager(), getViewFactory(),
 		        documentEditConfig.getObjectsToInsert(), otObject
 		                .getOTObjectService(), true, true);
 
 		return otObj;
 	}
 	
 	public void setViewEntry(OTViewEntry viewEntry) {
 		super.setViewEntry(viewEntry);
 		this.viewEntry = viewEntry;
 		if (viewEntry instanceof OTDocumentEditViewConfig) {
 			documentConfig = new DocumentEditConfig((OTDocumentEditViewConfig)viewEntry);
 			documentEditConfig = (DocumentEditConfig) documentConfig;
 			setViewMode(documentConfig.getMode());
 		}
 	}
 
 
 
 	public void actionPerformed(ActionEvent e)
     {
 		if (e.getActionCommand().equals("insertObject")) {
 
 			OTObject objToInsert = getObjectToInsertFromUser();
 
 			if (objToInsert == null) {
 				// No object to insert. Either we couldn't find one, or the user
 				// changed his mind
 				return;
 			}
 
 			OTObjectService objectService = otObject.getOTObjectService();
 			String strObjID = objectService.getExternalID(objToInsert);
 
 			String strObjText = "<object refid=\"" + strObjID + "\"></object><br>";
 
 			int pos = editorPane.getSelectionStart();
 			HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
 			
 			try {
 			//	Reader read = new StringReader(strObjText);
 			//	editorKit.read(read, doc, pos);
 				editorKit.insertHTML(doc, pos, strObjText, 0, 0, HTML.Tag.OBJECT);
 				editorKit.insertHTML(doc, pos, "<br></br>", 0, 0, HTML.Tag.BR);
             } catch (BadLocationException e1) {
 	            // TODO Auto-generated catch block
 	            e1.printStackTrace();
             } catch (IOException e2) {
 	            // TODO Auto-generated catch block
 	            e2.printStackTrace();
             }
             
             String htmlDoc = editorPane.getText();
             
             // if there is an object in the head, move it to the body
             String objectText = null;
             Pattern headPattern = Pattern.compile("(?s)<head>(.*)</head>");
             Matcher m = headPattern.matcher(htmlDoc);
             m.find();
             String headDoc = m.group(1);
             Pattern objectPattern = Pattern.compile("(?s)<object(.*?)>");
             m = objectPattern.matcher(headDoc);
             if (m.find())
             	objectText = m.group();
             
             Pattern bodyPattern = Pattern.compile("(?s)<body>(.*)</body>");
             Matcher m2 = bodyPattern.matcher(htmlDoc);
             m2.find();
             htmlDoc = m2.group(1);
             
             if (objectText != null){
             	htmlDoc = objectText + "<br/>" + htmlDoc;
             }
             
             htmlDoc = cleanHTML(htmlDoc);
             
 			pfObject.setDocumentText(htmlDoc);
 			
 			if (objectText != null){
 				updateFormatedView();
             }
             
 		} else if (e.getActionCommand().equalsIgnoreCase("viewSrc")){
 			final JFrame popupEditor = new JFrame();
 			
 			Point variablesFrameLocation = viewSrcButton.getRootPane().getLocationOnScreen();
 			variablesFrameLocation.translate(40, 15);
 			popupEditor.setLocation(variablesFrameLocation);
 
 			final JTextArea textArea = new JTextArea(10,20);
 			textArea.setText(pfObject.getDocumentText());
 			
 			JScrollPane scroll = new JScrollPane(textArea);
 			scroll.setPreferredSize(new Dimension(500,400));
 			
 			textArea.setCaretPosition(0);
 			
 			popupEditor.add(scroll);
 			
 			JButton updateBtn = new JButton("Update");
 			updateBtn.addActionListener(new ActionListener(){
 
 				public void actionPerformed(ActionEvent e)
                 {
 	                pfObject.setDocumentText(textArea.getText());
 	                updateFormatedView();
                 }});
 			
 			JButton cancelBtn = new JButton("Cancel");
 			cancelBtn.addActionListener(new ActionListener(){
 
 				public void actionPerformed(ActionEvent e)
                 {
 	                popupEditor.dispose();
                 }});
 			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
 			bottomPanel.add(cancelBtn);
 			bottomPanel.add(updateBtn);
 			popupEditor.add(bottomPanel, BorderLayout.SOUTH);
 			popupEditor.pack();
 			popupEditor.setVisible(true);
 			popupEditor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		}
     }
 	
 	public void stateChanged(OTChangeEvent e)
 	{
 		if (isChangingText()) {
 			// we have caused this event ourselves
 			return;
 		}
 
 		try {
 			textAreaModel.replace(0, textAreaModel.getLength(), pfObject
 			        .getDocumentText(), null);
 		} catch (Exception exc) {
 			// exc.printStackTrace();
 		}
 
 	}
 
 
 
 	public void keyPressed(KeyEvent e)
     {
 		if (e.getKeyCode() == e.VK_ENTER){
 			int pos = editorPane.getSelectionStart();
 			HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
 			try {
 	            editorKit.insertHTML(doc, pos, "<br></br>", 0, 0, HTML.Tag.BR);
             } catch (Exception e1) {
 	            e1.printStackTrace();
             }
 		}
 		
     }
 	
 	private String cleanHTML(String text){
 		Pattern allHead = Pattern.compile("<head>.*</head>", Pattern.DOTALL);
 		text = allHead.matcher(text).replaceAll("");
 		text = text.replaceAll("</?html.*>", "");
 		text = text.replaceAll("</?body.*>", "");
 		text = text.replaceAll("</object>","");
 		text = text.replaceAll("<object(.*?)/>", "<object$1>");
 		text = text.replaceAll("<object(.*?)>", "<object$1></object>");
 		text = text.replaceAll("</img>","");
 		text = text.replaceAll("<img(.*?)/>", "<img$1>");
 		text = text.replaceAll("<img(.*?)>", "<img$1></img>");
 		text = text.replaceAll("<br>", "<br/>");
 		text = text.replaceAll("</br>", "");
 		
 		return text;
 	}
 
 
 
 	public void keyReleased(KeyEvent e)
     {
 		try {
 			String text = editorPane.getText();
 			text = cleanHTML(text);
 			
 			pfObject.setDocumentText(text);
 		} catch (Exception exc) {
 			// exc.printStackTrace();
 		}
     }
 
 
 
 	public void keyTyped(KeyEvent e)
     {
 		
     }
 }
