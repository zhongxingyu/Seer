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
 
 package org.concord.view;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.FocusListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.Scrollable;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.JTextComponent;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.concord.framework.text.MarkupTextContained;
 import org.concord.swing.util.Util;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 
 //public class XMLText extends JPanel{
 public class XMLText extends JTextComponent 
 	implements ClipboardOwner, Scrollable, MarkupTextContained
 {
 
 static String		eolStr 			= System.getProperty("line.separator");
 static String 		urlPrefix 		= "file:/";
 static Font 		defaultFont 	= new Font(mapFontName("Serif"),Font.PLAIN,12);
 
 private Image 		offImage;
 	private int hp = 0;
 	private int wp = 0;
 private CCFont 		lastFont 		= new CCFont();
 private boolean 	needLayout 		= true;
 private Frame 		nearestFrame 	= null;
 private CCParagraph currParagraph	= null;
 private ImageIcon 	backImage;
 
 Vector				paragraphs;
 
 
 
 boolean 			autowrap 		= false;
 boolean 			linkCursor 		= false;
 
 Vector 				hyperlinkListeners = new Vector();
 HyperlinkEvent 		exitEvent;
 
 
 private Hashtable	externalProperties = new Hashtable();
 
 private java.awt.Insets	margins;
 
 private boolean  adjustTextAutomatically = true;
 //private boolean  adjustTextAutomatically = false;
 
 Object	xmlLock = new Object();
 
 String toolTipGeneral;
 
 private int fontSizeFactor = 0;
 private boolean fontSizeFactorWasChanged = false;
 
 int     globalScriptShift = 0;
 
 final static int SUP_SCRIPT_SHIFT           = -4;
 final static int SUB_SCRIPT_SHIFT           = 3;
 final static int FONT_SUB_SUP_SIZE_SHIFT    = -3;
 
 	static Hashtable modifiedFonts = new Hashtable();
 
 	public static void modifyFont(String name, String style, int size,
 							 int lineHeight)
 	{
 		modifyFont(name, convertJavaFace(style), size, lineHeight);
 	}
 
 	public static void modifyFont(String name, int style, int size,
 							 int lineHeight)
 	{
 		CCFont font = new CCFont(name, style, size, Color.black);
 		CCFontMetrics fm = new CCFontMetrics(null, lineHeight);
 		modifiedFonts.put(font, fm);
 	}
 
 	public XMLText(){
 	    setOpaque(true);
 		initialize("");
 	}
 	
 	public XMLText(String str){
 	    setOpaque(true);
 		initialize(str);
 	}
 	
 	public void decrementFontSizeFactor(){
 	    setFontSizeFactor(fontSizeFactor - 1);
 	}
 	
 	public void inrementFontSizeFactor(){
 	    setFontSizeFactor(fontSizeFactor + 1);
 	}
 	
 	public void clearFontSizeFactor(){
 	    setFontSizeFactor(0);
 	}
 	
 	public int getFontSizeFactor(){return fontSizeFactor;}
 	
 	public void setFontSizeFactor(int fontSizeFactor){
 	    fontSizeFactorWasChanged = (this.fontSizeFactor != fontSizeFactor);
 	    this.fontSizeFactor = fontSizeFactor;
 	    setText(getText());
 	    layoutText();
 	    repaint();
 	}
 	
 	public void updateUI(){//TextComponent
 	}
 	
 	public void addMouseListener(MouseListener listener){//TextComponent
 		if (listener instanceof MouseAdapter)
 			super.addMouseListener(listener);
 	}
 	
 	public void addFocusListener(FocusListener listener){//TextComponent
 		if (listener.getClass().getName().endsWith("MutableCaretEvent"))
 			return;
 		super.addFocusListener(listener);
 	}
 	
 	public void setEditable(boolean b) {//TextComponent
 	}
 	
     public String getToolTipText(MouseEvent event) {
     	String retValue = toolTipGeneral;
 		CCWord word = findWord(event.getX(),event.getY());
 		if(word != null){
 			if(word.link && (word.toolTip !=null)) retValue = word.toolTip;
 		}
        return retValue;
     }
     protected static final int NONE_CLIPBOARD_KEY		= 0;
     protected static final int COPY_CLIPBOARD_KEY		= 1;
     protected static final int PASTE_CLIPBOARD_KEY		= 2;
     protected static final int CUT_CLIPBOARD_KEY		= 3;
 
     protected static int getClipboardKey(KeyEvent event){
     	int retValue = NONE_CLIPBOARD_KEY;
         int code = event.getKeyCode();
         int modifiers = event.getModifiers();
         if((code == KeyEvent.VK_C) && ((modifiers & (InputEvent.META_MASK | InputEvent.CTRL_MASK)) != 0)){
 			retValue = COPY_CLIPBOARD_KEY;
         }else if((code == KeyEvent.VK_V) && ((modifiers & (InputEvent.META_MASK | InputEvent.CTRL_MASK)) != 0)){
 			retValue = PASTE_CLIPBOARD_KEY;
         }else if((code == KeyEvent.VK_X) && ((modifiers & (InputEvent.META_MASK | InputEvent.CTRL_MASK)) != 0)){
 			retValue = CUT_CLIPBOARD_KEY;
         }
         return retValue;
     }
 
 	public String getPlainText(){
 		String retValue = "";
 		if(paragraphs == null) return retValue;
 		for(int i = 0; i < paragraphs.size(); i++){
 			CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 			retValue += paragraph.getPlainText();
 		}
 		return retValue;
 	}
 
 	public void copyTextToClipboard(){
 		StringSelection contents = new StringSelection(getPlainText());
 		getToolkit().getSystemClipboard().setContents(contents,this);
 	}
 
 	public void initialize(String str){
 //		setBackground(Color.white);
 		addKeyListener(new KeyAdapter(){
     		public void keyReleased(KeyEvent e) {
     			int clipboardKey = getClipboardKey(e);
     			if(clipboardKey != COPY_CLIPBOARD_KEY) return;
     			copyTextToClipboard();
     			
     		}
 		});
 		
 	
 		javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
 		margins  = new java.awt.Insets(0,0,0,0);
 		exitEvent 	= new HyperlinkEvent(this,HyperlinkEvent.EventType.EXITED,null);
 		setText(str);
 		addMouseListener(new MouseAdapter(){
     		public void mouseClicked(MouseEvent e) {
     			CCWord word = findWord(e.getX(),e.getY());
     			try{
     			    if(!hasFocus()) requestFocus();
     			}catch(Throwable t){//JDK 1.1 doesn't have hasFocus
     			    requestFocus();
     			}
     			if(word != null && word.link){
 					try{
 						notifyHyperlinkListeners(new HyperlinkEvent(this,HyperlinkEvent.EventType.ACTIVATED,new java.net.URL(word.url),word.word));
 					}catch(Exception ex){
 						ex.printStackTrace();
 					}
     			}
     		}
     		public void mouseExited(MouseEvent e) {
     			if(linkCursor){
     				linkCursor = false;
 					/*
 	    			if(nearestFrame == null){
 	    				nearestFrame = findNearestFrame(XMLText.this);
 	    			}
 	    			if(nearestFrame == null) return;
 	    			nearestFrame.setCursor(Frame.DEFAULT_CURSOR);
 					*/
 					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	    		}
     		}
 		});
 				
 		addMouseMotionListener(new MouseMotionAdapter(){
     		public void mouseMoved(MouseEvent e) {
 				/*
     			if(nearestFrame == null){
     				nearestFrame = findNearestFrame(XMLText.this);
     			}
     			if(nearestFrame == null) return;
 				*/
     			CCWord word = findWord(e.getX(),e.getY());
     			if(word != null){
     				if(word.link && !linkCursor){
     					linkCursor = true;
     					// nearestFrame.setCursor(Frame.HAND_CURSOR);
 						setCursor(new Cursor(Cursor.HAND_CURSOR));
    					try{
     						notifyHyperlinkListeners(new HyperlinkEvent(this,HyperlinkEvent.EventType.ENTERED,new java.net.URL(word.url),word.word));
     					}catch(Exception ex){}
     				}else if(!word.link && linkCursor){
    					linkCursor = false;
 					// nearestFrame.setCursor(Frame.DEFAULT_CURSOR);
 					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
     					notifyHyperlinkListeners(exitEvent);
     				}
     			}else if(linkCursor){
     				linkCursor = false;
     				//nearestFrame.setCursor(Frame.DEFAULT_CURSOR);
 					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
     				notifyHyperlinkListeners(exitEvent);
     			}
     		}
 		});
 	}
 
 	protected void defineBackground(Color color){
 		//externalProperties.remove("backgroundcolor");
 		setBackground(color,false);
 	}
 	protected void defineForeground(Color color){
 		//externalProperties.remove("backgroundcolor");
 		setForeground(color,false);
 	}
 
 	public void setBackground(Color color){
 		setBackground(color,true);
 	}
 	public void setBackground(Color color,boolean putInProperties){
 		if(putInProperties && color == null){
 			externalProperties.remove("backgroundcolor");
 		}
 		if(color != null) super.setBackground(color);
 		if(putInProperties && color != null){
 			externalProperties.put("backgroundcolor",color);
 		}
 	}
 
 	public void setForeground(Color color){
 		setForeground(color,true);
 	}
 
 	public void setForeground(Color color,boolean putInProperties){
 		if(putInProperties && color == null){
 			externalProperties.remove("foregroundcolor");
 		}
 		if(color != null) super.setForeground(color);
 		if(putInProperties && color != null){
 		    externalProperties.put("foregroundcolor",color);
 		}
 	}
 
 	public Color getBackground(){
 		if(externalProperties.get("backgroundcolor") != null){
 			return (Color)externalProperties.get("backgroundcolor");
 		}
 		return super.getBackground();
 	}
 
 	public Color getForeground(){
 		if(externalProperties.get("foregroundcolor") != null){
 			return (Color)externalProperties.get("foregroundcolor");
 		}
 		return super.getForeground();
 	}
 
 	public void setSize(Dimension d){
 		super.setSize(d);
 		newsize();
 	}
 	
 	public void setSize(int w,int h){
 		super.setSize(w,h);
 		newsize();
 	}
 	
 	public void setMargins(int top,int left,int bottom, int right){
 		if(margins == null){
 			margins = new java.awt.Insets(top,left,bottom,right);
 		}else{
 			margins.top 		= top;
 			margins.left 		= left;
 			margins.bottom		= bottom;
 			margins.right 		= right;
 		}
 		newsize();
 	}
 	
 	public int getTopMargin(){
 		if(margins == null) margins = new java.awt.Insets(0,0,0,0);
 		return margins.top;
 	}
 	
 	public int getLeftMargin(){
 		if(margins == null) margins = new java.awt.Insets(0,0,0,0);
 		return margins.left;
 	}
 	
 	public int getBottomMargin(){
 		if(margins == null) margins = new java.awt.Insets(0,0,0,0);
 		return margins.bottom;
 	}
 	
 	public int getRightMargin(){
 		if(margins == null) margins = new java.awt.Insets(0,0,0,0);
 		return margins.right;
 	}
 	
 	private void newsize(){
 		if(offImage != null){
 			offImage.flush();
 			offImage = null;
 		}
 		setText(origText,false);
 	}
 	
 	public synchronized void paintComponent(Graphics g){
 		if(g == null) return;
 		if(wp != getSize().width ||
 		   hp != getSize().height){
 			wp = getSize().width;
 			hp = getSize().height;
 			newsize();
 		}
 		if(wp <= 0 || hp <= 0)  return;
 		if(offImage == null){
 			//offImage = createImage(wp,hp);
 		} 
 		Graphics cloneg = g.create();
 		if(cloneg == null) return;
 		Util.setAntialisingWithReflection(cloneg,true);
 		Graphics og = (offImage != null)?offImage.getGraphics():cloneg;
 		if(isOpaque()){
 		    og.setColor(getBackground());
 		    og.clearRect(0,0,wp,hp);//1.4.1 mac os x ???
 		    og.fillRect(0,0,wp,hp);
 		}
         og.setColor(getForeground());
 		
 		if(getBackgroundIcon() != null && getBackgroundIcon().getImage() != null){
 			og.drawImage(getBackgroundIcon().getImage(),0,0,this);
 		}
 		
 		if(needLayout){
 			layoutText(og);
 			needLayout = false;
 			if(adjustTextAutomatically) adjustText();
 			revalidate();
 		}
 		
 		if(paragraphs != null){
 			int y0 = (margins == null)?0:margins.top;
 			for(int i = 0; i < paragraphs.size(); i++){
 				CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 				y0 = paragraph.draw(og,y0);
 			}
 		}
 		if(offImage != null){
 		    cloneg.drawImage(offImage,0,0,null);
 		    og.dispose();
 		    og = null;
 		}
 		cloneg.dispose();
 	}
 
     public Dimension getMinimumSize() {
     	int wmin = 0;
     	int hmin = 0;
     	if(paragraphs != null){
 			for(int i = 0; i < paragraphs.size(); i++){
 				CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 				Dimension dp = paragraph.getSize();
 				dp.width += (getLeftMargin() + getRightMargin());
 				hmin += dp.height;
 				wmin = Math.max(wmin,dp.width);
 			}
     	}
       	return new Dimension(wmin+10,hmin+10);
     }
 	
     public Dimension getPreferredSize() {
       	return getMinimumSize();
     }
     
 	public void addHyperlinkListener(HyperlinkListener l){
 		if(l == null) return;
 		if(!hyperlinkListeners.contains(l)){
 			hyperlinkListeners.addElement(l);
 		}
 	}
 	
 	public void removeHyperlinkListener(HyperlinkListener l){
 		if(l == null) return;
 		if(hyperlinkListeners.contains(l)){
 			hyperlinkListeners.removeElement(l);
 		}
 	}
 	
 	private String origText;
 	
 	public String getText(){
 		return origText;
 	}
 	
 	
 	public void setTextWithAdjusting(String text){
 	    adjustTextAutomatically = true;
 	    setText(text);
 	}
 	public void setTextWithoutAdjusting(String text){
 	    adjustTextAutomatically = false;
 	    setText(text);
 	}
 	
 	public void setText(String text){
 		setText(text,true);
 	}
 	public void setText(String text,boolean doRepaint){
 	    if(!fontSizeFactorWasChanged && origText != null && text != null && origText.equals(text)) return;
 	    if(fontSizeFactorWasChanged) fontSizeFactorWasChanged = false;
 		origText = text;
 		if(text == null) return;
 		clearAll();
 		if(text.length() < 1) return;
 		lastFont = new CCFont();
 		parseText(getXMLTextHeader() + text);
 		if(doRepaint){
 		    repaint();
 		}
 //		System.out.println("text "+text);
 	}
 	private void delay(int dt){
 	    try{
 	        Thread.sleep(dt);
 	    }catch(Throwable t){
 	    }
 	}
 	
 	public static String getXMLTextHeader(){
 		String str = "<?xml version=\"1.0\"?>" + eolStr;
		// This was broken in Mac OS X 10.4 Java version 1.5.0_16.
		// The string dtdStr below no longer renders into a parsable URL
		// URL dtdURL = XMLText.class.getResource("/org/concord/view/dtd/xmltext.dtd");
		// String dtdStr = dtdURL.toExternalForm();
		// str += ("<!DOCTYPE TEXT PUBLIC \"-//Concord.ORG//DTD LabBook Description//EN\" \"" +
		// 		dtdStr + "\">" + eolStr);
 		return str;
 	}
 	public static String getTestDoc(){
         String str ="<TEXT>"+
 					  //"<FONT face='b' size='20'/>Constructing bbb Vector<BR/>"+
 					  //"<FONT face='p' size='20'/>Constructing bbb Vector<BR/>"+
 					  "<FONT face='b' size='12'/>before ampersand &amp; after ampersand bbb<SUP>10</SUP> Vector<BR/>"+
 					  "<FONT face='p' size='12'/>iiiiiiiiiiii aaa<SUB>11</SUB> Vector<BR/>"+
 					  "<FONT face='i' size='12'/>iiiiiiiiiiii bbb Vector<BR/>"+
 					"</TEXT>";
         return str;
     }	
 /*
 	public static String getTestDoc(){
 //D		String str = ("<TEXT bgcolor=\"00FF00\" fgcolor=\"FF0000\" tooltip = \"general tooltip\" backimage = \"ccjar:collisions/ui/images/bigspace.jpg\">"+eolStr);
 		String str = ("<TEXT bgcolor=\"00FF00\" fgcolor=\"FF0000\" >"+eolStr);
 		str += ("    11111yyyQasdasdadadadadadadadawqeqweq <BR/>  11111"+eolStr);
 		str += ("aaaa   bbbbb "+eolStr);
 		str += ("<FONT color=\"000000\" name=\"Monospaced\" face=\"i\" size=\"24\">"+eolStr);
 		str += ("	inside of the"+eolStr);
 		str += ("FONT TAG."+eolStr);
 		str += ("old line is continuing"+eolStr);
 		str += ("</FONT>");
 		str += ("	22222<BR/>"+eolStr);
 		str += ("<A ID=\"link1\"  tooltip = \"link1 tooltip\" href=\"href1\">"+eolStr);
 		str += ("link1 link2"+eolStr);
 		str += ("</A>"+eolStr);
 		str += ("<P align=\"left\">"+eolStr);
 		str += ("<FONT color=\"00FFFF\" size=\"10\" face=\"bi\"/>dima dima               <FONT face=\"p\"/>dima1 dima1"+eolStr);
 		str += ("here is right alignment"+eolStr);
 		str += ("<A ID=\"link2\"  tooltip = \"link2 tooltip\" href=\"href2\">"+eolStr);
 		str += ("<FONT color=\"101010\" size=\"24\"/>"+eolStr);
 		str += ("right alignment link"+eolStr);
 		str += ("<FONT size=\"18\">"+"font18</FONT>"+eolStr);
 		str += ("again24"+eolStr);
 		str += ("<FONT size=\"36\">"+"font36</FONT>"+eolStr);
 		str += ("</A>"+eolStr);
 		str += ("</P>"+eolStr);
 		str += ("DDDDDDDDDDDDDDDDDDDD "+eolStr);
 		str += ("<FONT size=\"12\"/>"+eolStr);
 		str += ("3333"+eolStr);
 		str += ("<P align=\"center\">"+eolStr);
 		str += ("<FONT color=\"00FFFF\" size=\"10\"/>"+eolStr);
 		str += ("here is center alignment"+eolStr);
 		str += ("<A ID=\"link3\"  tooltip = \"link3 tooltip\"  href=\"href3\">"+eolStr);
 		str += ("<FONT color=\"808080\" size=\"20\"/>"+eolStr);
 		str += ("center alignment link"+eolStr);
 		str += ("</A>"+eolStr);
 		str += ("</P>"+eolStr);
 		str += ("<FONT size=\"16\"/>"+eolStr);
 		str += ("size 16"+eolStr);
 		str += ("<FONT face=\"bi\"/>"+eolStr);
 		str += ("bold and italic"+eolStr);
 		str += ("<FONT face=\"p\"/>"+eolStr);
 		str += ("plain"+eolStr);
 		str += ("<FONT size=\"9\"/>"+eolStr);
 		str += ("<BR/>"+eolStr);
 		str += ("tab1__<TAB/>__aftertabulation"+eolStr);
 		str += ("<BR/>"+eolStr);
 		str += ("tab2__<TAB n=\"3\"/>__aftertabulation"+eolStr);
 		str += ("<BR/>"+eolStr);
 		str += ("tab3__<TAB n=\"3\" dospaces=\"true\" spaces=\"10\"/>__aftertabulation"+eolStr);
 		str += ("</TEXT>"+eolStr);
 
 		return str;
 	}
 */
 	public void defineBackImage(Element elem){
 		String imageURL = elem.getAttribute("backimage");
 		if(imageURL != null && imageURL.length() > 0){
 			setBackgroundIcon(elem.getAttribute("backimage"),false);
 		}
 	}
 	
 	public void defineCommonToolTip(Element elem){
 		toolTipGeneral = elem.getAttribute("tooltip");
 		if(toolTipGeneral != null && toolTipGeneral.length() < 1){
 			toolTipGeneral = null;
 		}
 		if(toolTipGeneral == null) 	setToolTipText("");
 		else						setToolTipText(toolTipGeneral);					
 	}
 	
 	
 	
 	public void setBackgroundIcon(String imageURL){
 		setBackgroundIcon(imageURL,true);
 	}
 
 	public synchronized void setBackgroundIcon(String imageURL,boolean putIntoProperties){
 		if(putIntoProperties || imageURL == null || imageURL.length() < 1){
 			Object obj =  externalProperties.get("backimage");
 			if(obj instanceof ImageIcon){
 				if(imageURL != null && imageURL.equals(((ImageIcon)obj).getDescription())){
 					return;
 				}else{
 					externalProperties.remove("backimage");
 					ImageIcon oldImg = (ImageIcon)obj;
 					if(oldImg.getImage() != null){
 						oldImg.getImage().flush();
 					}
 				}
 			}
 		}//ImageIcon.java
 		if(imageURL != null){
 			try{
 				ImageIcon oldImage = backImage;
 				backImage = new ImageIcon(new java.net.URL(imageURL),imageURL);
 				if(oldImage != null && oldImage.getImage() != null){
 					oldImage.getImage().flush();
 				}
 			}catch(Throwable t){
 				t.printStackTrace();
 				backImage = null;
 			}
 		}else if(putIntoProperties){
 			backImage = null;
 			externalProperties.put("backimage","");
 		}
 		if(putIntoProperties && (backImage != null)){
 			externalProperties.put("backimage",backImage);
 		}
 	}
 
 	public synchronized ImageIcon getBackgroundIcon(){
 		Object img = externalProperties.get("backimage");
 		ImageIcon extImageIcon = backImage;
 		if(img instanceof ImageIcon){
 			extImageIcon = (ImageIcon)img;
 		}else if(img instanceof String){
 			if(((String)img).equals("")){
 				extImageIcon = null;
 			}
 		}
 		return extImageIcon;
 	}
 
 	void notifyHyperlinkListeners(HyperlinkEvent ev){
 		if(ev == null || hyperlinkListeners.size() < 1) return;
 		if(nearestFrame == null){
 			nearestFrame = findNearestFrame(XMLText.this);
 		}
 		if(ev.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
 			if(nearestFrame != null) nearestFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		}
 		for(int i = 0; i < hyperlinkListeners.size(); i++){
 			HyperlinkListener l = (HyperlinkListener)hyperlinkListeners.elementAt(i);
 			l.hyperlinkUpdate(ev);
 		}
 		if(ev.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
 			if(nearestFrame == null) return;
 			nearestFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		}
 
 	}
 	
 	public synchronized void layoutText(){
 	    Graphics g = getGraphics();
 	    layoutText(g);
 	}
 
     public void adjustText(){
     	javax.swing.SwingUtilities.invokeLater(new Runnable(){
     	    public void run(){
                 int nIter = 0;
 //                clearFontSizeFactor();
 	            fontSizeFactorWasChanged = true;
                 fontSizeFactor = 0;
             	while((nIter < 50) && (getPreferredSize().height-10 > (getHeight() - getTopMargin() - getBottomMargin()))){
             		decrementFontSizeFactor();
             		nIter++;
             	}
                 delay(200);
             	repaint();
     	    }
     	});
     }
 
 
 	
 	synchronized void  layoutText(Graphics g){
 	    if(g == null) return;
 		if(paragraphs == null) return;
 		int y0 = (margins == null)?0:margins.top;
 		for(int i = 0; i < paragraphs.size(); i++){
 			CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 			y0 = paragraph.layoutText(g,y0);
 		}
 	}
 	
 	void clearAll(){
 		if(paragraphs != null){
 			for(int i = 0; i < paragraphs.size(); i++){
 				CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 				paragraph.clearAll();
 			}
 			paragraphs.removeAllElements();
 			paragraphs = null;
 		}
 		needLayout = true;
 		globalScriptShift = 0;
 	}
 	
 	void parseText(String text){
 		if(text == null) return;
 		Throwable ext = null;
 		globalScriptShift = 0;
 		try{
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			
 			
 	        // Optional: set various configuration options
 	        dbf.setValidating(true);
 	        dbf.setIgnoringComments(true);
 	        dbf.setIgnoringElementContentWhitespace(true);//???
 	        dbf.setCoalescing(true);
 	        
 	        DocumentBuilder db = dbf.newDocumentBuilder();
 			
 			
 			// Set an ErrorHandler before parsing
 			OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, "UTF-8");
 			db.setErrorHandler(new MyErrorHandlerXMLParser(new PrintWriter(errorWriter, true)));
 			
             Document doc = db.parse(new InputSource(new StringReader(text)));
             Element rootElement = doc.getDocumentElement();
 // 			System.out.println(rootElement);
             String bColorString = rootElement.getAttribute("bgcolor");
             if(bColorString == null || bColorString.length() < 1){
 			    defineBackground(Color.white);
             }else{
 			    defineBackground(new Color( Util.getIntColorFromStringColor(bColorString)));
             }
             String fColorString = rootElement.getAttribute("fgcolor");
             if(fColorString == null || fColorString.length() < 1){
 			    defineForeground(Color.black);
             }else{
 			    defineForeground(new Color( Util.getIntColorFromStringColor(fColorString)));
             }
 
 			defineBackImage(rootElement);
 			defineCommonToolTip(rootElement);
 			
 			autowrap = Util.getBooleanFromString(rootElement.getAttribute("autowrap"));
 								
 			lastFont = createCCFontFromTEXTElement(rootElement,lastFont,fontSizeFactor);
             NodeList nlist = rootElement.getChildNodes();
         	if(nlist == null) return;
         	
         	int rootAlignment = getAlignmentFromAttr(rootElement);
         	
         	paragraphs = new Vector();
         	boolean needParagraph = true;
             for(int i = 0; i < nlist.getLength(); i++){
             	Node node = nlist.item(i);
             	if(isNodeParagraph(node)){
             		int alignment = getAlignmentFromAttr((Element)node);
         			currParagraph = new CCParagraph(this,alignment);
         			paragraphs.addElement(currParagraph);
             		NodeList pList = node.getChildNodes();
             		if(pList == null) continue;
             		for(int n = 0; n < pList.getLength(); n++){
             			parseTextElements(currParagraph,pList.item(n));
             		}
             		currParagraph.addNewWord(new CCWord(eolStr));
             		needParagraph = true;
             	}else{
             		if(needParagraph){
         				currParagraph = new CCParagraph(this,rootAlignment);
         				paragraphs.addElement(currParagraph);
         				needParagraph = false;
         			}
             		parseTextElements(currParagraph,node);
             	}
             }
         } catch (ParserConfigurationException pce) {
 	        System.err.println(pce);
 	        ext = pce;
 	    } catch (SAXException se) {
 	    	System.err.println(se.getMessage());
 	        ext = se;
 	    } catch (IOException ioe) {
 	        System.err.println(ioe);
 	        ext = ioe;
 	    }catch(Exception e){
 			System.out.println("XMLText <init> Exception "+e);
 	        ext = e;
 		}
 		if(ext != null){
 	        System.out.println("Throwable class "+ext.getClass());
 	        ext.printStackTrace();
 	        System.out.println(text);
 		}
 		needLayout = true;
 	}
 
 
 	static int getAlignmentFromAttr(Element element){
     	int alignment = java.awt.Label.LEFT;
     	String alignStr = element.getAttribute("align");
     	if(alignStr != null){
     		if(alignStr.equals("center")){
     			alignment = java.awt.Label.CENTER;
     		}else if(alignStr.equals("right")){
     			alignment = java.awt.Label.RIGHT;
     		}
     	}
     	return alignment;
 	}
 	
 	static boolean isNodeParagraph(Node node){
 		if(node == null) return false;
 		return (node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getTagName().equals("P"));
 	}
 
 	void parseFontElement(CCParagraph paragraph, Element element){
 		CCFont previousFont = lastFont;
 		lastFont = createCCFontFromFONTElement(element,lastFont,fontSizeFactor);
 		NodeList fontChildren = element.getChildNodes();
 		if(fontChildren.getLength() > 0){
 			for(int fc = 0; fc < fontChildren.getLength(); fc++){
 				Node fontNode = fontChildren.item(fc);
 				if(fontNode == null || fontNode.getNodeType() != Node.TEXT_NODE) continue;
 				extractWords(paragraph,fontNode);
 			}
 			lastFont = previousFont;
 //			System.out.println("fontChildren.getLength() "+fontChildren.getLength());
 		}
 	}
 
 
 	void parseScriptVariant(CCParagraph paragraph,Node node,int textShift){
 	    if(node.getNodeType() != Node.ELEMENT_NODE) return;
 		Element element = (Element)node;
 		String elementName = element.getTagName();
 		if(!elementName.equals("SUP") && !elementName.equals("SUB")) return;
 	    NodeList sList = element.getChildNodes();
         for(int i = 0;  i < sList.getLength(); i++){
     	    Node sNode = sList.item(i);
     	    if(sNode.getNodeType() != Node.TEXT_NODE) continue;
     	    globalScriptShift = textShift;
 		    extractWords(paragraph,sNode);
     	    globalScriptShift = 0;
         }
 	}
 	
 	void parseTextElements(CCParagraph paragraph,Node node){
 		if(node == null) return;
 		switch(node.getNodeType()){
 			case Node.ELEMENT_NODE:
 				Element element = (Element)node;
 				String elementName = element.getTagName();
 				if(elementName.equals("BR")){//BR
 					paragraph.addNewWord(new CCWord(eolStr));
 				}else if(elementName.equals("SUP")){
                     parseScriptVariant(paragraph,node,SUP_SCRIPT_SHIFT);
 				}else if(elementName.equals("SUB")){
                     parseScriptVariant(paragraph,node,SUB_SCRIPT_SHIFT);
 				}else if(elementName.equals("A")){//A
 					String toolTip = element.getAttribute("tooltip");
 					String href = element.getAttribute("href");
 					if(href == null) href = "";
 					if(href.indexOf(":/") < 0){
 						href = urlPrefix + href;
 					}
 					NodeList aList = element.getChildNodes();
 					CCFont oldLastFont = lastFont;
     				for(int a = 0; a < aList.getLength(); a++){
     					Node aNode = aList.item(a);
 						switch(aNode.getNodeType()){
 							case Node.ELEMENT_NODE:
 								String aElementName = ((Element)aNode).getTagName();
 								if(aElementName.equals("SUP")){
                                     parseScriptVariant(paragraph,aNode,SUP_SCRIPT_SHIFT);
                                     break;
 								}else if(aElementName.equals("SUB")){
                                     parseScriptVariant(paragraph,aNode,SUB_SCRIPT_SHIFT);
                                     break;
 								}
 								if(!aElementName.equals("FONT")) break;
 								parseFontElement(paragraph,(Element)aNode);
 //								lastFont = createCCFontFromFONTElement((Element)aNode,lastFont);
 								break;
 							case Node.TEXT_NODE:
 								extractWords(paragraph,aNode,href,toolTip);
 								break;
 						}
     				}
     				lastFont = oldLastFont;
 				}else if(elementName.equals("FONT")){
 					parseFontElement(paragraph,element);
 /*
 					{
 						CCFont previousFont = lastFont;
 						lastFont = createCCFontFromFONTElement(element,lastFont);
 						NodeList fontChildren = element.getChildNodes();
 						if(fontChildren.getLength() > 0){
 							for(int fc = 0; fc < fontChildren.getLength(); fc++){
 								Node fontNode = fontChildren.item(fc);
 								if(fontNode == null || fontNode.getNodeType() != Node.TEXT_NODE) continue;
 								System.out.println("fontNode  "+fontNode);
 								extractWords(paragraph,fontNode);
 							}
 							lastFont = previousFont;
 //							System.out.println("fontChildren.getLength() "+fontChildren.getLength());
 						}
 					}
 */
 				}else if(elementName.equals("TAB")){//TAB
 					int nTimes = Util.getIntFromString(element.getAttribute("n"));		
 					if(nTimes < 1) 	nTimes = 1;		
 					boolean dospaces = Util.getBooleanFromString(element.getAttribute("dospaces"));
 					int nSpaces = Util.getIntFromString(element.getAttribute("spaces"));		
 					if(nSpaces < 1) 	nSpaces = 1;	
 					String tabStr = "";
 					for(int i = 0; i < 	nTimes; i++){
 						if(!dospaces){
 							tabStr += '\t';
 						}else{
 							for(int sp = 0; sp < nSpaces; sp++) tabStr += ' ';
 						}
 					}
 					paragraph.addNewWord(new CCWord(tabStr));
 				}
 				break;
 			case Node.TEXT_NODE:
 				extractWords(paragraph,node);
 				break;
 			default:
 				break;
 		}
 	}
 
 	void extractWords(CCParagraph paragraph,Node node){
 		extractWords(paragraph,node,null,null);
 	}
 	void extractWords(CCParagraph paragraph,Node node,String href){
 		extractWords(paragraph,node,href,null);
 	}
 	void extractWords(CCParagraph paragraph,Node node,String href,String toolTip){
 		if(node == null) return;
 		String tData = removeLFRT(((org.w3c.dom.CharacterData)node).getData());
 		java.util.StringTokenizer strParser = new java.util.StringTokenizer(tData," \t\n\r\f",true);
 		while(strParser.hasMoreTokens()){
 			String token = strParser.nextToken();
 			if(token.equals("\n")) token = " ";
 			if(token.equals("\r")) token = " ";
 			if(token.equals("\f")) token = " ";
 			if(token.equals("\t")) token = "    ";//????
 			CCWord word = (href != null)?new CCWord(token,lastFont,href,globalScriptShift):new CCWord(token,lastFont,globalScriptShift);
 			word.setToolTip(toolTip);
 			paragraph.addNewWord(word);
 		}
 	}
 	
 	static String removeLFRT(String str){
 		if(str == null || str.length() < 1) return str;
 		int bind = 0;
 		int eind = str.length();
 		
 		while(bind < eind){
 			char c = str.charAt(bind);
 			if(c == '\r' || c == '\n'){
 				bind++;
 			}else{
 				break;
 			}
 		}
 		while(eind > bind && eind >= 0){
 			char c = str.charAt(eind - 1);
 			if(c == '\r' || c == '\n'){
 				eind--;
 			}else{
 				break;
 			}
 		}
 		return str.substring(bind,eind);
 	}
 	
 	protected static int convertJavaFace(String face)
 	{
 		if(face.equals("b")){
 			return Font.BOLD;
 		}else if(face.equals("i")){
 			return Font.ITALIC;
 		}else if(face.equals("bi")){
 			return Font.BOLD | Font.ITALIC;
 		}
 
 		return Font.PLAIN;
 	}
 	
 	protected static CCFont createCCFontFromTEXTElement(Element element,CCFont lastFont){
 	    return createCCFontFromTEXTElement(element,lastFont,0);
 	}
 	protected static CCFont createCCFontFromTEXTElement(Element element,CCFont lastFont,int fontSizeFactor){
 		if(element == null) return null;
 		String elementName = element.getTagName();
 	    if(!elementName.equals("TEXT")) return null;
 		CCFont retFont = lastFont;
 		if(retFont == null) retFont = new CCFont();
 		String fName = element.getAttribute("font");
 		if(fName == null || fName.length() < 1){
 			fName = lastFont.font.getName();
 		}
 		
 		String sizeStr = element.getAttribute("size");
 		int size = (sizeStr == null || sizeStr.length() < 1)?lastFont.font.getSize():Util.getIntFromString(sizeStr)+fontSizeFactor;
 		if(size < 9) size = 9;
 		String face = element.getAttribute("face");
 		int javaFace = Font.PLAIN;
 		if(face == null || face.length() < 1){
 			javaFace = lastFont.font.getStyle();
 		}else{
 			javaFace = convertJavaFace(face);
 		}
 		
 		String colorStr = element.getAttribute("fgcolor");
 		Color color = (colorStr == null || colorStr.length() < 1)?lastFont.color:new Color(Util.getIntColorFromStringColor(colorStr));
 		return new CCFont(fName, javaFace, size, color);
 	}
 	protected static CCFont createCCFontFromFONTElement(Element element,CCFont lastFont){
 	    return createCCFontFromFONTElement(element,lastFont,0);
 	}
 	
 	protected static CCFont createCCFontFromFONTElement(Element element,CCFont lastFont,int fontSizeFactor){
 		
 		if(element == null) return null;
 		String elementName = element.getTagName();
 	    if(!elementName.equals("FONT")) return null;
 		CCFont retFont = lastFont;
 		if(retFont == null) retFont = new CCFont();
 		String fName = element.getAttribute("name");
 		if(fName == null || fName.length() < 1){
 			fName = lastFont.font.getName();
 		}
 		String sizeStr = element.getAttribute("size");
 		int size = (sizeStr == null || sizeStr.length() < 1)?lastFont.font.getSize():Util.getIntFromString(sizeStr)+fontSizeFactor;
 		if(size < 9) size = 9;
 		String face = element.getAttribute("face");
 		int javaFace = Font.PLAIN;
 		if(face == null || face.length() < 1){
 			javaFace = lastFont.font.getStyle();
 		}else{
 			javaFace = convertJavaFace(face);
 		}
 		String colorStr = element.getAttribute("color");
 		Color color = (colorStr == null || colorStr.length() < 1)?lastFont.color:new Color(Util.getIntColorFromStringColor(colorStr));
 		return new CCFont(fName,javaFace,size,color);
 	}
 		
 	static Frame findNearestFrame(Component c){
 		if(c == null) return null;
 		Component currComp = c;
 		while(!(currComp instanceof Frame)){
 			currComp = currComp.getParent();
 			if(currComp == null) break;
 		}
 		if(!(currComp instanceof Frame)){
 			currComp = null;
 		}
 		return (Frame)currComp;
 	}
 	
 	CCWord findWord(int x,int y){
 		CCWord retWord = null;
 		if(paragraphs == null) return  null;
 		for(int i = 0; i < paragraphs.size(); i++){
 			CCParagraph paragraph = (CCParagraph)paragraphs.elementAt(i);
 			retWord = paragraph.findWord(x,y);
 			if(retWord != null) break;
 		}
 		
 		if(retWord != null){
 			if(!retWord.link && retWord.word.trim().length() < 1){
 				retWord = null;
 			}
 		}
 		return retWord;
 	}
 	
 	static class CCParagraph{
 		XMLText 			owner;
 	
 		LinkedList			textObjects;
 		Vector 				lines;
 		
 		int					alignment = java.awt.Label.LEFT;
 		
 		
 		CCParagraph(XMLText owner,int alignment){
 			this.owner = owner;
 			this.alignment = alignment;
 		}
 		
 		public String getPlainText(){
 			String retValue = "";
 			if(lines == null) return retValue;
 			for(int i = 0; i < lines.size(); i++){
 				CCLine line = (CCLine)lines.elementAt(i);
 				retValue += line.getPlainText();
 			}
 			return retValue;
 		}
 		
 		int draw(Graphics g,int y0){
 			int retValue = y0;
 	        if(lines != null){
 	        	for(int i = 0; i < lines.size(); i++){
 		        	int x0 = owner.getLeftMargin();
 	        		CCLine line = (CCLine)lines.elementAt(i);
 	        		retValue += line.getHeight();
 		        	switch(alignment){
 		        		case java.awt.Label.LEFT:
 		        		default:
 		        			x0 = owner.getLeftMargin();
 		        			break;
 		        		case java.awt.Label.CENTER:
 	        				x0 = (owner.getSize().width - owner.getRightMargin() +owner.getLeftMargin())/2 - 1 - line.getWidth()/2;
 		        			break;
 		        		case java.awt.Label.RIGHT:
 	        				x0 = owner.getSize().width - owner.getRightMargin() - 2 - line.getWidth();
 		        			break;
 		        	}
 	        		line.draw(g,x0,retValue);
 	        	}
 	        }
 	        return retValue;
 		}
 		
 		CCWord findWord(int x,int y){
 			CCWord retWord = null;
 			if(lines == null) return retWord;
 			for(int i = 0; i < lines.size(); i++){
 	 			CCLine line = (CCLine)lines.elementAt(i);
 				for(int w = 0; w < line.getNumbWords(); w++){
 					CCWord word = line.getNthWord(w);
 					if(word.inWord(x,y)){
 						retWord = word;
 						break;
 					}
 				}
 			}
 			if(retWord != null){
 				if(!retWord.link && retWord.word.trim().length() < 1){
 					retWord = null;
 				}
 			}
 			return retWord;
 		}
 		
 		int setGeometryForWords(Graphics g,int y0){
 			int retValue = y0;
 			if(lines == null || lines.size() < 1) return retValue;
 	    	for(int i = 0; i < lines.size(); i++){
 	    		int x0 = owner.getLeftMargin();
 	    		CCLine line = (CCLine)lines.elementAt(i);
 	    		retValue += line.getHeight();
 	        	switch(alignment){
 	        		case java.awt.Label.LEFT:
 	        		default:
 	        			x0 = owner.getLeftMargin();
 	        			break;
 	        		case java.awt.Label.CENTER:
 	        			x0 = (owner.getSize().width - owner.getRightMargin() +owner.getLeftMargin())/2 - 1 - line.getWidth()/2;
 	        			break;
 	        		case java.awt.Label.RIGHT:
 	        			x0 = owner.getSize().width - owner.getRightMargin() - 2 - line.getWidth();
 	        			break;
 	        	}
 	    		line.setGeometryForWords(g,x0,retValue);
 	    	}
 	    	return retValue;
 		}
 		
 		Dimension getSize(){
 			int w = 0;
 			int h = 0;
 			if(lines != null && lines.size() > 0){
 		    	for(int i = 0; i < lines.size(); i++){
 		    		CCLine line = (CCLine)lines.elementAt(i);
 		    		h += line.getHeight();
 		    		w = Math.max(w,line.getWidth());
 		    	}
 	    	}
 	    	return new Dimension(w,h);
 		}
 		
 		
 		synchronized int layoutText(Graphics g, int y0){
 			int retValue = y0;
 			if(textObjects == null || g == null) return retValue;
 			if(lines != null){
 				lines.removeAllElements();
 				lines = null;
 			}
 			lines = new Vector();
 			CCLine currLine = null;
 	 
 	    	ListIterator iterator = textObjects.listIterator(0);
 	    	int xBeginLine = owner.getLeftMargin();
 	    	int widthLine = owner.getSize().width - owner.getLeftMargin() - owner.getRightMargin();
 	    	int currX = xBeginLine;
 	    	
 	    	int lineHeight = 0;
 	    	int lineWidth = 0;
 	    	boolean lastEOLWasHard = true;
 	    	while(iterator.hasNext()){
 	    		CCWord word = (CCWord)iterator.next();
 	    		boolean needNewLine = false;
 				boolean wasHardEOL = false;
 				CCFontMetrics fm = word.font.getFontMetrics(g);
 				
 	    		if(word.word.equals(eolStr)){
 	     			needNewLine = true;
 	     			wasHardEOL = true;
 	     			lastEOLWasHard = true;
 	   			}else{
 	    			currX += fm.stringWidth(word.word);
 	    			if(owner.autowrap && currX > widthLine){
 	    				needNewLine = true;
 	    				lastEOLWasHard = false;
 	    				iterator.previous();
 	    			}
 	    		}
 	    		if(!needNewLine){
 	    			if(currLine == null){
 	    			    if(!lastEOLWasHard && word.word.equals(" ")) continue;
 	    			    currLine = new CCLine();
 	    			}
 	    			currLine.addWord(word);
 	    			lineHeight = Math.max(lineHeight,fm.getHeight());
 	    			lineWidth += fm.stringWidth(word.word);
 	    		}else{
 	    			if(!wasHardEOL && (currLine == null || currLine.getNumbWords() < 1)){//put word even if it was too wide
 	    				if(currLine == null) currLine = new CCLine();
 	    				lineHeight = Math.max(lineHeight,fm.getHeight());
 		    			lineWidth += fm.stringWidth(word.word);
 	    				
 	    				currLine.addWord(word);
 	    				iterator.next();
 	    			}
 	    			if(currLine != null){
 	    				currLine.setHeight(lineHeight);
 	    				currLine.setWidth(lineWidth);
 	    				lines.addElement(currLine);
 	    				currLine = null;
 	    			}
 	    			currX = xBeginLine;
 	    			lineHeight = 0;
 	    			lineWidth = 0;
 	    		}
 	    	}
 	    	if(currLine != null){
 	    		currLine.setHeight(lineHeight);
 	    		currLine.setWidth(lineWidth);
 	     		lines.addElement(currLine);
 	   		}
 	   		retValue = setGeometryForWords(g,y0);
 	   		return retValue;
 	    	
 		}
 		void addNewWord(CCWord word){
 			if(word == null) return;
 			if(textObjects == null) textObjects = new LinkedList();
 			textObjects.add(word);
 		}
 		void clearAll(){
 			if(textObjects != null){
 				textObjects.clear();
 				textObjects = null;
 			}
 			if(lines != null){
 				lines.removeAllElements();
 				lines = null;
 			}
 		}
 	}
 	
 	static class CCLine{
 		Vector words;
 		int height = 0;
 		int width = 0;
 		
 		public String getPlainText(){
 			String retValue = "";
 			if(words == null) return retValue;
 			for(int w = 0; w < words.size(); w++){
 				CCWord word = (CCWord)words.elementAt(w);
 				retValue += word.word;
 			}
 			retValue += eolStr;
 			return retValue;
 		}
 		void addWord(CCWord word){
 			if(word == null) return;
 			if(words == null) words = new Vector();
 			words.addElement(word);
 		}
 		
 		Vector getWords(){return words;}
 		
 		int getNumbWords(){
 			if(words == null) return 0;
 			return words.size();
 		}
 		
 		CCWord getNthWord(int index){
 			if(index < 0 || index >= getNumbWords()) return null;
 			return (CCWord)words.elementAt(index);
 		}
 		
 		void draw(Graphics g,int x,int y){
 			int currx = x;
 			Color oldColor = g.getColor();
 			Font  oldFont = g.getFont();
 			for(int w = 0; w < getNumbWords(); w++){
 				CCWord word = getNthWord(w);
 				Font needFont = word.font.font;
 				/*if(word.scriptShift != 0){
                     needFont = new Font(needFont.getName(),needFont.getStyle(),needFont.getSize()-2);
 				}*/
 	    		g.setFont(needFont);
 				CCFontMetrics fm = word.font.getFontMetrics(g);
 	  			int wl = fm.stringWidth(word.word); //dima dima
 	  			//java.awt.font.TextLayout layout = new java.awt.font.TextLayout(word.word, word.font.font, ((java.awt.Graphics2D)g).getFontRenderContext()); 
 	  			//java.awt.geom.Rectangle2D r2d = layout.getBounds();
 	  			//int wl = g.getFontMetrics(word.font.font).stringWidth(word.word);
                 //System.out.println("word["+w+"] <<" + word.word+">> "+wl);
 				g.setColor(word.font.color);
 				//layout.draw(g, (float)currx, (float)y);
 				g.drawString(word.word,currx,y+word.scriptShift);
 				if(word.link){
 					g.drawLine(currx,y+1,currx+wl,y+1+word.scriptShift);
 				}
 				currx += wl;
 			}
 			g.setColor(oldColor);
 			g.setFont(oldFont);
 		}
 
 		void setGeometryForWords(Graphics g,int x,int y){
 			int currx = x;
 			for(int w = 0; w < getNumbWords(); w++){
 				CCWord word = getNthWord(w);
 				CCFontMetrics fm = word.font.getFontMetrics(g);
 	  			int wl = fm.stringWidth(word.word);
 	  			word.setGeometry(currx,y - fm.getHeight(),wl,fm.getHeight());
 				currx += wl;
 			}
 		}
 		int getHeight(){return height;}
 		void setHeight(int height){this.height = height;}
 
 		int getWidth(){return width;}
 		void setWidth(int width){this.width = width;}
 	}
 
 
 	static class CCWord{
 		String  	word;
 		CCFont		font;
 		boolean 	link;
 		String 		url;
 		int			x,y,width,height;
 		int         scriptShift = 0;
 		String		toolTip;
 
 		CCWord(String word){
 			this(word,null,null,0);
 		}
 		CCWord(String word,String url){
 			this(word,null,url,0);
 		}
 		CCWord(String word,CCFont font){
 			this(word,font,null,0);
 		}
 		CCWord(String word,CCFont font,int scriptShift){
 			this(word,font,null,scriptShift);
 		}
 		CCWord(String word,CCFont font,String url){
 			this(word,font,url,0);
 		}
 		CCWord(String word,CCFont font,String url,int scriptShift){
 			this.word = word;
 			this.url = url;
 			this.font = (font == null)?new CCFont(FONT_SUB_SUP_SIZE_SHIFT):font;
 			this.link = (url != null);
             this.scriptShift = scriptShift;
             if(this.scriptShift != 0 && font != null){
                 this.font = new CCFont(this.font,FONT_SUB_SUP_SIZE_SHIFT);
             }
 		}
 		void setGeometry(int x,int y,int width,int height){
 			this.x 		= x;
 			this.y 		= y;
 			this.width 	= width;
 			this.height = height;
 		}
 		
 		void setToolTip(String toolTip){
 			this.toolTip = toolTip;
 			if(this.toolTip != null && this.toolTip.length() < 1){
 				this.toolTip = null;
 			}
 		}
 		
 		
 		
 		boolean inWord(int xp,int yp){
 			if(width <= 0 || height <=0 ) return false;
 			return ((xp >= x) && (xp <= x + width) && (yp >= y) && (yp <= y + height));
 		}
 	}
 
 	static class CCFontMetrics
 	{
 		FontMetrics fm;
 		int height = -1;
 
 		CCFontMetrics(FontMetrics fm)
 		{
 			this.fm = fm;
 		}
 
 		CCFontMetrics(FontMetrics fm, int height)
 		{
 			this.fm = fm;
 			this.height = height;
 		}
 
 		int getHeight()
 		{
 			if(height != -1){
 				return height;
 			} else {			 
 				return fm.getHeight();
 			}
 		}
 
 		int stringWidth(String str)
 		{
 			return fm.stringWidth(str);
 		}
 
 		void setFM(FontMetrics fm)
 		{
 			this.fm = fm;
 		}
 	}
 
 	static class CCFont{
 		Color  color;
 		Font	font;
 		String name;
 		int style;
 		int size;
 
 		CCFont(){
 			this(0);
 		}
 		CCFont(int sizeOffset){
 			this(defaultFont.getName(), defaultFont.getStyle(),
 				 defaultFont.getSize()+sizeOffset, Color.black);
 		}
 
 		CCFont(String fontName, int style, int size, Color color)
 		{
 			this.color = color;
 			this.name = mapFontName(fontName);
 			this.font = new Font(this.name, style, size);
 			this.style = style;
 			this.size = size;
 		}
 		CCFont(CCFont prototype,int sizeOffset){
             color       = prototype.color;
             name        = prototype.name;
             style       = prototype.style;
 			font        = new Font(name, style, prototype.size+sizeOffset);
             size        = prototype.size + sizeOffset;
 		}
 		
 		CCFontMetrics getFontMetrics(Graphics g)
 		{
 			if(modifiedFonts != null)
 			{
 				Object modFM = modifiedFonts.get(this);
 				if(modFM instanceof CCFontMetrics){
 					
 					((CCFontMetrics)modFM).setFM(g.getFontMetrics(font));
 					return (CCFontMetrics)modFM;
 				}
 			}
 
 			return new CCFontMetrics(g.getFontMetrics(font));
 		}
 
 		/*
 		 * note this is a hack because fonts of different colors
 		 * are equal
 		 */		 
 		public boolean equals(Object obj)
 		{
 			if(!(obj instanceof CCFont)){
 				return false;
 			}
 			
 			CCFont font = (CCFont)obj;
 			return font.name.equals(name) && font.style == style &&
 				font.size == size;
 		}		
 
 		public int hashCode()
 		{
 			return name.hashCode() + style * 1000 + size;
 		}
 	}
     private static class MyErrorHandlerXMLParser implements ErrorHandler {
         /** Error handler output goes here */
         private PrintWriter out;
 
         MyErrorHandlerXMLParser(PrintWriter out) {
             this.out = out;
         }
 
         /**
          * Returns a string describing parse exception details
          */
         private String getParseExceptionInfo(SAXParseException spe) {
             String systemId = spe.getSystemId();
             if (systemId == null) {
                 systemId = "null";
             }
             String info = "Line=" + spe.getLineNumber() +
                 ": " + spe.getMessage();
             return info;
         }
 
         // The following methods are standard SAX ErrorHandler methods.
         // See SAX documentation for more info.
 
         public void warning(SAXParseException spe) throws SAXException {
             out.println("Warning: " + getParseExceptionInfo(spe));
         }
         
         public void error(SAXParseException spe) throws SAXException {
             String message = "Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
 
         public void fatalError(SAXParseException spe) throws SAXException {
             String message = "Fatal Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
     }
 	public void lostOwnership(Clipboard clipboard,Transferable t){}
 
 	public Dimension getPreferredScrollableViewportSize()
 	{
 		return getPreferredSize();
 	}
 
 	public int getScrollableUnitIncrement(Rectangle visibleRect,
 										  int orientation,
 										  int direction)
 	{
 		return 1;
 	}
 
 	public int getScrollableBlockIncrement(Rectangle visibleRect,
 										   int orientation,
 										   int direction)
 	{
 		return 1;
 	}
 
 	public boolean getScrollableTracksViewportWidth()
 	{
 		return true;
 	}
 
 	public boolean getScrollableTracksViewportHeight()
 	{
 		if(getMinimumSize() != null &&
 		   getParent() != null && 
 		   getParent().getParent() instanceof javax.swing.JScrollPane &&
 		   getMinimumSize().height < getParent().getParent().getSize().height)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
     public void setAdjustTextAutomatically(boolean adjustTextAutomatically){this.adjustTextAutomatically = adjustTextAutomatically;}
     public boolean isAdjustTextAutomatically(){return adjustTextAutomatically;}
 
 
     static String mapFontName(String fontName){//Panther bug workaround
         return fontName;
 /*
         if(fontName == null || (!fontName.equalsIgnoreCase("serif") && 
                                 !fontName.equalsIgnoreCase("sansserif") &&
                                 !fontName.equalsIgnoreCase("dialog"))) return fontName;
         String mrjVersion = System.getProperty("mrj.version");
         if(mrjVersion == null || !mrjVersion.equals("99")) return fontName;
         if(fontName.equalsIgnoreCase("sansserif")) return "Verdana";
         if(fontName.equalsIgnoreCase("dialog")) return "Helvetica Neue";
         return "Georgia";
 */
     }
 }
 
