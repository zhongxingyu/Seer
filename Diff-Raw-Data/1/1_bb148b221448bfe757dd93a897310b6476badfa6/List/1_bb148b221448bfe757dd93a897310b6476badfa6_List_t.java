 /*****************************************************************************
  *                                Waba Extras
  *
  * Version History
  * Date                Version  Programmer
  * ----------  -------  -------  ------------------------------------------
  * 21/04/1999  New      1.0.0    Rob Nielsen
  * Class created
  *
  * 20/08/1999  New      1.0.1    Rob Nielsen
  * Changed getMainWindow() call to MainWindow.getMainWindow()
  *
  ****************************************************************************/
 
 package extra.ui;
 
 import waba.fx.*;
 import waba.ui.*;
 import waba.util.Vector;
 
 /**
  * This is a standard palm drop down list.  It normally displays a down arrow followed
  * by the currently selected item.  When it is tapped it will drop down a list of all the
  * options.  When one of these is tapped, the list will fold back up with the new option
  * displayed.  A PRESSED event is also propogated down the hierachy.
  * The selected string get be obtained with getSelected() and it's index with
  * getSelectedIndex().  Items can be added to the list with the add() method.
  *
  * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
  * @version    1.0.1 20 August 1999
  */
 public class List extends Control implements PreferredSize
 {
 	String name;
 
 	Image arrow;
 	boolean dropped=false;
 	protected Popup popup=null;
 	protected Vector options;
 	int selected=0;
 	int oldselected=0;
 	protected int textHeight;
 	protected int expandedWidth=0;
 	protected int numDisplayed=0;
 	protected int scrollOffset=0;
 	protected int initialYOffset = 0;
 	int maxScrollOffset=0;
 	boolean scrollUp;
 	Timer scrollTimer=null;
 	boolean clicked;
 	protected FontMetrics fm=null;
 
 	/**
 	 * Construct a new empty list of default size
 	 */
 	public List()
 	{
 		this(new Vector());
 	}
 
 	/**
 	 * Construct a new list with the given options
 	 * @param options an array of the choices available
 	 */
 	public List(String[] options)
 	{
 		this();
 		for(int i=0,size=options.length;i<size;i++)
 			add(options[i]);
 	}
 
 	/**
 	 * Construct a new list with the given options
 	 * @param options a vector of the choices available
 	 */
 	public List(Vector options)
 	{
 		this.options=options;
 	}
 
 	/**
 	 * Add a new option to the end of the list
 	 * @param name the option to add
 	 */
 	public void add(String name)
 	{
 		options.add(name);
 		int w;
 		if (fm!=null&&(w=fm.getTextWidth(name))>expandedWidth)
 			expandedWidth=w;
 	}
 
 	/**
 	 * Gets the text of the currently selected item
 	 * @returns the selected text
 	 */
 	public String getSelected()
 	{
 		if(options == null) return null;
 		if(selected < 0 || selected >= options.getCount()) return null;
 		return (String)options.get(selected);
 	}
 
 	/**
 	 * Get the index of the currently selected option
 	 * @returns the index
 	 */
 	public int getSelectedIndex()
 	{
 		return selected;
 	}
 
 	/**
 	 * Sets the currently selected index
 	 * @param i the index of the item to select
 	 */
 	public void setSelectedIndex(int i)
 	{
 		if (i>=0&&i<options.getCount())
 			{
 				selected=i;
 				oldselected=i;
 				postEvent(new ControlEvent(ControlEvent.PRESSED,this));
 				if (parent instanceof RelativeContainer)
 					((RelativeContainer)parent).layout();
 				//repaint();
 			}
 	}
 
 	public void setSelectedIndex(String s)
 	{
 		if(s == null) return;
 		for(int i = 0; i < options.getCount(); i++){
 			if(s.equals((String)options.get(i))){
 				setSelectedIndex(i);
 				return;
 			}
 		}
 	}
 
 
 	public int getPreferredWidth(FontMetrics fm)
 	{
 		return fm.getTextWidth((String)options.get(selected))+10;
 	}
 
 	public int getPreferredHeight(FontMetrics fm)
 	{
 		return fm.getHeight();
 	}
 
 	public Dimension getPreferredSize(){
 		if(fm==null) fm=getFontMetrics(MainWindow.defaultFont);
 		if(fm == null) return null;
 		return new Dimension(getPreferredWidth(fm),getPreferredHeight(fm));
 	}
 
 	public void clear(){
 		options = new Vector();
 		calcSizes();
 	}
 
 	public boolean calcSizes()
 	{
 		if (fm==null)
 			fm=getFontMetrics(MainWindow.defaultFont);
 		if (fm==null)
 			return false;
 		int size=options.getCount();
 		expandedWidth=0;
 		int t;
     
 		for(int i=0;i<size;i++){
 			String str = (String)options.get(i);
 			if(str == null) continue;
 			if((t=fm.getTextWidth(str))>expandedWidth)
 				expandedWidth=t;
 		}
 
 		if(expandedWidth + 10 < width) expandedWidth = width - 10;
 
 		return true;
 	}
 
 	public void onPaint(Graphics g)
 	{
 		if (fm==null)
 			calcSizes();
 		if (popup==null)
 			{
 				g.translate(0,4);
 				g.setColor(0,0,0);
 				g.drawLine(0,0,7,0);
 				g.drawLine(1,1,6,1);
 				g.drawLine(2,2,5,2);
 				g.drawLine(3,3,4,3);
 				g.translate(0,-4);
 				g.drawText((String)options.get(selected),10,0);
 			}
 		else
 			{
 				drawList(g);
 				g.setColor(0,0,0);
 				g.drawLine(1,0,width-3,0);
 				g.drawLine(0,1,0,height-3);
 				g.drawLine(width-2,1,width-2,height-2);
 				g.drawLine(1,height-2,width-3,height-2);
 				g.drawLine(2,height-1,width-3,height-1);
 				g.drawLine(width-1,2,width-1,height-3);
 			}
 	}
 
 	public void drawList(Graphics g)
 	{
 		g.setColor(255,255,255);
 		g.fillRect(1,1,width-3,height-3);
 		g.setColor(0,0,0);
 		for(int i=0;i<numDisplayed;i++)
 			{
 				if (i+scrollOffset==selected)
 					{
 						g.fillRect(1,i*textHeight+1+initialYOffset,width-3,textHeight);
 						g.setColor(255,255,255);
 					}
 				g.drawText((String)options.get(i+scrollOffset),3,i*textHeight+1+initialYOffset);
 				if (i+scrollOffset==selected)
 					g.setColor(0,0,0);
 			}
 	}
 
 	/**
 	 * Process pen and key events to this component
 	 * @param event the event to process
 	 */
 	public void onEvent(Event event)
 	{
 		if (event.type==ControlEvent.FOCUS_OUT){
 			if (popup!=null){
 				popup.unpop();
 				popup=null;
 			}
 		} else if (event.type==ControlEvent.TIMER) {
 			scrollOffset+=(scrollUp?-1:1);
 			selected+=(scrollUp?-1:1);
 			if (scrollOffset<=0||scrollOffset>=maxScrollOffset)
 				removeTimer(scrollTimer);
 			drawList(createGraphics());
 			return;
 		} else if (event instanceof PenEvent) {
 			int px=((PenEvent)event).x;
 			int py=((PenEvent)event).y - initialYOffset;
 			switch (event.type){
 			case PenEvent.PEN_DOWN:
 			case PenEvent.PEN_DRAG:
 				if (popup!=null){					
 					clicked=true;
 					int position=py/textHeight;
 					if ((py<=5&&scrollOffset>0)||(py + initialYOffset>=height-5&&scrollOffset<maxScrollOffset)){
 						if (scrollTimer==null){
 							scrollUp=(py<=5);
 							scrollTimer=addTimer(400);
 						}
 						return;
 					} else if (scrollTimer!=null){
 						removeTimer(scrollTimer);
 						scrollTimer=null;
 					}
 
 					if (position<0||position>=numDisplayed) return;
 					selected=position+scrollOffset;
 					if (selected!=oldselected){
 						Graphics g=createGraphics();
 						g.setColor(255,255,255);
 						g.fillRect(1,(oldselected-scrollOffset)*textHeight+1+initialYOffset,width-3,textHeight);
 						g.setColor(0,0,0);
 						g.drawText((String)options.get(oldselected),3,(oldselected-scrollOffset)*textHeight+1+initialYOffset);
 						g.fillRect(1,(selected-scrollOffset)*textHeight+1+initialYOffset,width-3,textHeight);
 						g.setColor(255,255,255);
 						g.drawText((String)options.get(selected),3,(selected-scrollOffset)*textHeight+1+initialYOffset);
 						oldselected=selected;
 					}
 				}
 				break;
 			case PenEvent.PEN_UP:
 				if (popup==null){
 					textHeight=height;
 					numDisplayed=options.getCount();
 					scrollOffset=0;
 					Rect mr=MainWindow.getMainWindow().getRect();
 					if (numDisplayed*textHeight>mr.height) numDisplayed=mr.height/textHeight;
 					maxScrollOffset=options.getCount()-numDisplayed;
 					doPopup();
 					oldselected=selected;
 					clicked=false;
 				} else {
 					
 					if (scrollTimer!=null) {
 						removeTimer(scrollTimer);
 						scrollTimer=null;
 					}
 					popup.unpop();
 					popup=null;
 					postEvent(new ControlEvent(ControlEvent.PRESSED,this));				
 				}
 				break;
 			}
 		}
 	}
   
 
 	public void doPopup(){
		if(getParent() == null) return;
 		popup=new Popup(this);
 		popup.popup(x,y,expandedWidth+10,textHeight*numDisplayed+3+initialYOffset);
 	}
 	public boolean isPopup(){return (popup != null);}
 	public int getNumbDisplayed(){return numDisplayed;}
 	public int getScrollOffset(){return scrollOffset;}
 	public int getTextHeight(){return textHeight;}
 	public waba.fx.FontMetrics getFontMetrics(){return fm;}
 }
