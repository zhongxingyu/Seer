 package ucbang.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import ucbang.core.Card;
 import ucbang.core.Deck;
 import ucbang.core.Player;
 
 import ucbang.network.Client;
 
 public class Field implements MouseListener, MouseMotionListener{
 	Client client;
 	public BSHashMap<Card, Clickable> clickies = new BSHashMap<Card, Clickable>();
 	CardDisplayer cd;
 	Point pointOnCard;
 	Clickable movingCard;
 	Card clicked;
 	ArrayList<Card> pick;
 	public ArrayList<HandSpace> handPlacer = new ArrayList<HandSpace>(); //to avoid npe
 	String description;
 	Point describeWhere;
 	long lastMouseMoved = System.currentTimeMillis();
 	int tooltipWidth = 0;
 	int tooltipHeight = 0;
 	Point hoverpoint;
         
 	public Field(CardDisplayer cd, Client c) {
 		this.cd=cd;
 		client = c;
 	}
 	/**
 	 * Adds a card to the specified location, owned by the specified player
 	 * <p>This method specifies the location of the card to be placed, so for most
 	 * cases it should not be used. Use add(Card, int, boolean) whenever possible</p>
 	 * @param card The card to be added
 	 * @param x The x coordinate of the location
 	 * @param y The y coordinate of the location
 	 * @param player The player who owns the card
 	 * @param field Whether the card is in the field or not
 	 */
 	public void add(Card card, int x, int y, int player, boolean field){
 		clickies.put(card, new CardSpace(card, new Rectangle(x,y,60,90), player, field));
 	}
 	/**
 	 * Removes the last card in the hand of a player, used when
 	 * the player is an opponent whose hand is unknown and they just
 	 * played a card
 	 * @param player the player whose hand to remove a card from
 	 */
 	public void removeLast(int player){
 		clickies.remove(handPlacer.get(player).removeLast().card);
 	}
 	/**
 	 * Adds a card to the field owned by the specified player
 	 * <p>This method is "smart" and can locate cards automatically.
 	 * Use this whenever players exist</p>
 	 * @param card
 	 * @param player
 	 * @param field
 	 */
 	public void add(Card card, int player, boolean field){
 		if(client.id==player)
 			System.out.println("Client has "+client.player.hand.size()+"cards in his hand.");
 		if(card.type==1){//this a character card
 			int x=350;
 			int y=200;
 			clickies.put(card, new CardSpace(card, new Rectangle(x, y,60,90), player, false));
 		}else{
 			HandSpace hs = handPlacer.get(player);
 			int fieldoffset = (field?100:0);
 			double handoffset = 30*(!field?client.players.get(player).hand.size():client.players.get(player).field.size());
 			int xoffset = (int)(handoffset * Math.sin(hs.theta))+(int)(fieldoffset*Math.sin(hs.theta));
 			int yoffset = (int)(handoffset * Math.cos(hs.theta))+(int)(fieldoffset*Math.cos(hs.theta));
			int x=(int) hs.rect.x+hs.rect.width+xoffset;
 			int y=(int) hs.rect.y+yoffset;
 			CardSpace cs = new CardSpace(card, new Rectangle(x,y, 60,90), player, field);
 			clickies.put(card, cs);
 			hs.addCard(cs);
 			//if(hs.autoSort) sortHandSpace(hs);
 		}
 	}
         
 	int textHeight(String message, Graphics2D graphics){
 		int lineheight=(int)graphics.getFont().getStringBounds("|", graphics.getFontRenderContext()).getHeight();
 		return message.split("\n").length*lineheight;
 	}
 	int textWidth(String message, Graphics2D graphics){
 		String[] lines = message.split("\n");
 		int width=0;
 		for(int i=0;i<lines.length;i++){
 			int w=(int)graphics.getFont().getStringBounds(lines[i], graphics.getFontRenderContext()).getWidth();
 			if(width<w)
 				width=w;
 		}
 		return width;
 	}
 	void improvedDrawString(String message, int x, int y, Graphics2D graphics){
 		int lineheight=(int)graphics.getFont().getStringBounds("|", graphics.getFontRenderContext()).getHeight();
 		String[] lines = message.split("\n");
 		for(int i=0;i<lines.length;i++){
 			graphics.drawString(lines[i], x, y+i*lineheight);
 		}
 	}
 	public void paint(Graphics2D graphics){
 		for(HandSpace hs : handPlacer){
 			if(hs.autoSort)
 				graphics.fill(hs.rect);
 			else
 				graphics.draw(hs.rect);
 		}
 		//draw HP cards first
 		/*Iterator<CardSpace> it = hpcards.iterator();
 		while(it.hasNext()){
 			CardSpace hp = it.next();
 			cd.paint("BULLETBACK", graphics, hp.rect.x, hp.rect.y, hp.rect.width, hp.rect.height, Color.BLUE, Color.GRAY);
 		}*/
 		Iterator<Clickable> iter = clickies.values().iterator();
 		ArrayList<CardSpace> Char = new ArrayList<CardSpace>();
 		ArrayList<CardSpace> Bullet = new ArrayList<CardSpace>();
 		while(iter.hasNext()){
 			Clickable temp = iter.next();
 			if(temp instanceof CardSpace){
 				CardSpace crd = (CardSpace)temp;
 				if(crd.card.name=="BULLETBACK"){
 					Bullet.add(crd);
 				}
 				else if(crd.card.type==1){
 					Char.add(crd);
 				}
 				else{
 					Color inner;
 					switch(crd.card.location){
 					case 0:
 						inner=Color.BLACK;
 						break;
 					case 1:
 						if(crd.card.type==5)
 							inner=new Color(100,100,200);
 						else{
 							inner = ((crd.card.name!="JAIL"||crd.card.name!="DYNAMITE")?inner=new Color(100,200,100):new Color(100,100,200));
 						}
 						break;
 					default:
 						inner=new Color(200,100,100);
 					}
 					//Color outer=client.id==1?Color.RED:Color.BLUE;
 					Color outer;
 					switch(crd.playerid){
 					case 0: outer = Color.RED; break;
 					case 1: outer = Color.BLUE; break;
 					case 2: outer = Color.CYAN; break;
 					case 3: outer = Color.MAGENTA; break;
 					case 4: outer = Color.YELLOW; break;
 					case 5: outer = Color.ORANGE; break;
 					case 6: outer = Color.GREEN; break;
 					case 7: outer = Color.LIGHT_GRAY; break;
 					case 8: outer = Color.WHITE; break;
 					case 9: outer = Color.PINK; break;
 					default: outer = Color.BLACK; break;
 					}
 					cd.paint(crd.card.name, graphics, crd.rect.x, crd.rect.y, crd.rect.width, temp.rect.height, 
 							inner,outer);
 				}
 			}else if(temp instanceof HandSpace){
 				HandSpace hs = (HandSpace)temp;
 				graphics.draw3DRect(hs.rect.x, hs.rect.y, hs.rect.width, hs.rect.height, true);
 			}else{
 				System.out.println("WTF");
 			}
 		}
 
 		for(CardSpace crd:Bullet){
 			cd.paint(crd.card.name, graphics, crd.rect.x, crd.rect.y, crd.rect.width, crd.rect.height, Color.BLACK, Color.BLACK);
 		}
 		for(CardSpace crd:Char){
 			cd.paint(crd.card.name, graphics, crd.rect.x, crd.rect.y, crd.rect.width, crd.rect.height, Color.BLACK, Color.BLACK);
 		}
 
 		if(description==null&&System.currentTimeMillis()-lastMouseMoved>1000){
 			//create description
 			Clickable cl = binarySearchCardAtPoint(hoverpoint);
 			if (cl instanceof CardSpace) {
 				CardSpace cs = (CardSpace) cl;
 				if (cs != null && cs.card != null){
 					if(cs.card.description.equals(""))
 						description = cs.card.name.replace('_', ' ');
 					else
 						description = cs.card.name+" - "+cs.card.description;
 					if(cs.card.type==1){
 						if(client.players.get(cs.playerid).maxLifePoints>0)
 							description = description + "\n" + client.players.get(cs.playerid).lifePoints +"HP";;
 						//TODO: add distance to tooltip?
 					}
 					describeWhere = hoverpoint;
 					tooltipWidth = textWidth(description, graphics);
 					tooltipHeight = textHeight(description, graphics);
 				}
 			}
 		}
 		if(description!=null){
 			Rectangle2D bounds=graphics.getFont().getStringBounds(description, graphics.getFontRenderContext());
 			Color temp=graphics.getColor();
 			graphics.setColor(Color.YELLOW);
 			graphics.fill3DRect(describeWhere.x, describeWhere.y-(int)bounds.getHeight()+32, tooltipWidth, tooltipHeight,false);
 			graphics.setColor(Color.BLACK);
 			improvedDrawString(description, describeWhere.x, describeWhere.y+30,graphics);
 			graphics.setColor(temp);
 		}
 	}
 	public Clickable binarySearchCardAtPoint(Point ep){
 		//bsearch method
 		int start;
 		int end;
 
 		ArrayList<Clickable> al = clickies.values(); //search the values arrayList for...
 		if(al.isEmpty()||ep==null)return null;
 		int a = 0, b = al.size(), index = al.size() / 2;
 
 		while (a != b) {
 			if (ep.y > al.get(index).rect.y + 85) { // the "start" is the value of the card whose bottom is closest to the cursor (and on the cursor)
 				a = index + 1;
 			} else {
 				b = index;
 			}
 			index = a + (b - a) / 2;
 		}
 		start = a;
 		a = 0;
 		b = al.size();
 		index = al.size() / 2;
 		while (a != b) {
 			if (ep.y > al.get(index).rect.y) { // the "end" is the value of the card whose top is closest to the cursor (and on the cursor)
 				a = index + 1;
 			} else {
 				b = index;
 			}
 			index = a + (b - a) / 2;
 		}
 		end = a - 1;
 		for (int n = end; n>= start; n--) {
 			Clickable s = al.get(n);
 			if (s.rect.contains(ep.x, ep.y)) {
 				return al.get(n);
 			}
 		}
 		return null;
 	}
 	public void start2(){
 		handPlacer = new ArrayList<HandSpace>(client.numPlayers);
 		double theta;
 		HandSpace hs = null;
 		clear();
 		for(int player = 0; player<client.numPlayers; player++){
 			theta = -(player-client.id)*(2*Math.PI/client.numPlayers)-Math.PI/2;
 			int hsx = client.gui.width/2+(int)((client.gui.width-150)/2*Math.cos(theta));
 			int hsy = 280-(int)(220*Math.sin(theta));
 			hs=new HandSpace(new Rectangle(hsx, hsy,10,10), player, theta);
 			handPlacer.add(hs);
 			Card chara=null;
 			if(client.players.get(player).character>=0){
 				System.out.println(player+":"+Deck.Characters.values()[client.players.get(player).character]);
 				chara = new Card(Deck.Characters.values()[client.players.get(player).character]);
 			}else if(client.id==player){
 				System.out.println(player+":"+Deck.Characters.values()[client.player.character]);
 				chara = new Card(Deck.Characters.values()[client.player.character]);
 			}
 			if(chara!=null){
 				int x=(int) hs.rect.x-60;
 				int y=(int) hs.rect.y;
 				CardSpace csp = new CardSpace(chara,new Rectangle(x,y-60,60,90), player, false);
 				//generate HP card
 				Card hp = new Card(Deck.CardName.BULLETBACK);
 				CardSpace hps = new CardSpace(hp, new Rectangle(x+
 						10 * client.players.get(player).maxLifePoints,y-60,90,60),player, false);
 				hps.setPartner(csp);
 				csp.setPartner(hps);
 				//hps.rotate(1);
 				clickies.put(hp, hps);
 				clickies.put(chara, csp);
 				hs.setCharHP(csp, hps);
 			}
 		}
 	}
 	public void clear(){
 		pointOnCard = null;
 		movingCard = null;
 		clickies.clear();
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		Point ep=e.getPoint();
 		////the ugly proxy skip turn button
 		if(new Rectangle(760, 560, 40, 40).contains(ep)){
 			if(client.prompting&&!client.forceDecision){
 				client.outMsgs.add("Prompt:-1");
 				client.prompting = false;
 			}
 			return;
 		}
 		Clickable cl = binarySearchCardAtPoint(ep);
 		if (cl instanceof CardSpace) {
 			CardSpace cs = (CardSpace) cl;
 			if (cs != null && cs.card != null){
 			    if(cs.playerid != -1)
 				client.gui.appendText(String.valueOf(client.players.get(cs.playerid).hand.indexOf(cs.card))+" "+(cs.hs!=null?String.valueOf(cs.hs.cards.indexOf(cs)):""));
                             else if(pick != null)
                                 client.gui.appendText(String.valueOf(pick.contains(cs.card)));
 			}
 			else
 				return;
 			if (e.getButton() == MouseEvent.BUTTON3) {
 				//Put right click stuff here, or not
 			}else if (client.prompting){
 				if(pick!= null && pick.contains(cs.card)) {
 					System.out.println("000000000000000000000000000000 "+pick.size()+" "+client.player.hand.size());
 					System.out.println("sending prompt...");
 					if (cs.card.type == 1) {
 						client.outMsgs.add("Prompt:"
 								+ pick.indexOf(cs.card));
 						client.player.hand.clear(); //you just picked a character card
 						clear();
 					} else {
 						client.outMsgs.add("Prompt:" + pick.indexOf(cs.card));
 					}
 					pick = null;
 					client.prompting = false;
 				} else if(client.forceDecision==false){
 					//it's your turn
 					if(client.targetingPlayer){
 						if(cs.card.type==1||cs.card.name=="BULLETBACK"){
 							client.targetingPlayer = false;   
 							client.prompting = false;
 							client.outMsgs.add("Prompt:" + cs.playerid);
 						}
 					}
 					else if(client.nextPrompt==-1){
 						Player p = client.players.get(cs.playerid);
 						if(cs.card.location==0){
 							client.nextPrompt = p.hand.indexOf(cs.card);
 							client.gui.appendText("Index of card is "+client.nextPrompt);
 						}
 						else{
 							client.nextPrompt = ((0-client.players.get(cs.playerid).field.indexOf(cs.card))-3);
 							client.gui.appendText("lol "+client.nextPrompt);
 						}
 						client.outMsgs.add("Prompt:"+p.id);
 					}
 					else{
 						client.outMsgs.add("Prompt:" + ((0-client.player.field.indexOf(cs.card))-3));
 						pick = null;
 						client.prompting = false;
 					}
 				}
 				else{
 					System.out.println("i was prompting, but a bad card was given");
 				}
 			}
 		}
 		else if(cl == null){
 			for(HandSpace cs : handPlacer)
 				if(cs.rect.contains(e.getPoint())){
 					cl=cs;
 				}
 			if(cl!=null){
 				if(e.getButton()==MouseEvent.BUTTON1)
 					sortHandSpace((HandSpace)cl);
 				if(e.getButton()==MouseEvent.BUTTON3)
 					((HandSpace)cl).autoSort = !((HandSpace)cl).autoSort;
 			}
 		}
 	}
 
 	public void mousePressed(MouseEvent e) {
 		movingCard = binarySearchCardAtPoint(e.getPoint());
 
 		if(movingCard==null){//placer handler
 			for(HandSpace cs : handPlacer)
 				if(cs.rect.contains(e.getPoint())){
 					movingCard=cs;
 				}
 		}
 		if(movingCard!=null){
 			pointOnCard = new Point(e.getPoint().x-movingCard.rect.x, e.getPoint().y-movingCard.rect.y);
 			//System.out.println("picked up card");
 		}
 	}
 	public void mouseReleased(MouseEvent e) {
 		if(movingCard!=null){
 			//System.out.println("card dropped");
 		}
 		movingCard = null;
 		description = null;
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		//System.out.println("dragging");
 		lastMouseMoved = System.currentTimeMillis();
 		if(movingCard!=null){
 			movingCard.move(Math.max(0, Math.min(e.getPoint().x-pointOnCard.x,client.gui.getWidth()-55)),Math.max(0, Math.min(e.getPoint().y-pointOnCard.y,client.gui.getHeight()-85))); //replace boundaries with width()/height() of frame?
 		}
 		else{
 			//System.out.println("not dragging");
 		}
 	}
 
 	public void sortHandSpace(HandSpace hs){
 		if(hs==null){
 			System.out.println("WTWFWTWFWWTFWTWTWWAFSFASFASFS");
 			return;
 		}
 		client.gui.appendText("Sorting...");
 		int player = hs.playerid;
 		for(int n = 0; n<hs.cards.size(); n++){
 			int x = (int) hs.rect.x+hs.rect.width+30*n;
 			int y = (int) hs.rect.y+(0); //more trinarytrinary fun!
 			hs.cards.get(n).rect.x = x;
 			hs.cards.get(n).rect.y = y;
 		}
 		for(int n = 0; n<hs.fieldCards.size(); n++){
 			int x = (int) hs.rect.x+hs.rect.width+30*n;
 			int y = (int) hs.rect.y+(0) +(player==client.id?-100:100); //more trinarytrinary fun!
 			hs.fieldCards.get(n).rect.x = x;
 			hs.fieldCards.get(n).rect.y = y;
 		}
 	}
 
 	public class BSHashMap<K,V> extends HashMap<K,V>{
 		ArrayList<V> occupied = new ArrayList<V>();
 
 		public V put(K key, V value){
 			occupied.add(value);
 			return super.put(key, value);
 		}
 
 		public ArrayList<V> values(){
 			ArrayList<V> al = new ArrayList<V>();
 			Collections.sort(occupied, new Comparator(){
 				public int compare(Object o1, Object o2) {
 					return ((Comparable<Object>)o1).compareTo(o2);
 				}
 			});
 			al.addAll(occupied);
 			return al;
 		}
 		public void clear(){
 			occupied.clear();
 			super.clear();
 		}
 		public V remove(Object o){
 			if(o instanceof Card){
 				CardSpace cs =(CardSpace)get(o);
                                 if(cs==null){
                                     client.gui.appendText("WTFWTFWTF");
                                 }
 				if(cs.hs != null){
 					if(!cs.field)
 						cs.hs.cards.remove(cs);
 					else
 						cs.hs.fieldCards.remove(cs);
                                         if(cs.hs.autoSort){
                                                 sortHandSpace(cs.hs);
                                         }
 				}
                                 //System.out.println(cs.card.name+" "+cs.playerid+" "+(cs.hs==null)+" "+handPlacer.get(cs.playerid).fieldCards.contains(cs));
 			}                        
 			occupied.remove(get(o));
 			V oo = super.remove(o);
 			return oo;
 		}
 	}
 
 	/*
 	 * Contains a card and a rectangle
 	 */
 	private class CardSpace extends Clickable{
 		public Card card;
 		public boolean field;
 		HandSpace hs;
 
 		/**
 		 * @param c The card this CardSpace describes
 		 * @param r The bounds of the card
 		 * @param player The player who owns the card
 		 * @param f Whether the card is on the field
 		 * @param partner The parent container of the card
 		 */
 		public CardSpace(Card c, Rectangle r, int player, boolean f){
 			super(r);
 			card = c;
 			rect = r;
 			playerid = player;
 			field = f;
 			if(!handPlacer.isEmpty()&& player != -1)
 				hs = handPlacer.get(playerid);
 		}
 	}
 
 	public class HandSpace extends Clickable{
 		public ArrayList<CardSpace> cards = new ArrayList<CardSpace>();
 		public ArrayList<CardSpace> fieldCards = new ArrayList<CardSpace>();
 		CardSpace character, hp;
 		boolean autoSort = true;
 		double theta;
 		/**
 		 * @param r
 		 * @param player
 		 * @param theta
 		 */
 		public HandSpace(Rectangle r, int player, double theta){
 			super(r);
 			playerid = player;
 			this.theta = theta;
 		}
 		/**
 		 * @param character
 		 * @param hp
 		 */
 		public void setCharHP(CardSpace character, CardSpace hp){
 			this.character = character;
 			this.hp = hp;
 		}
 		/**
 		 * @param card
 		 */
 		public void addCard(CardSpace card){
 			if(!card.field)
 				cards.add(card);
 			else
 				fieldCards.add(card);
 		}
 		/**
 		 * @return
 		 */
 		public CardSpace removeLast(){
 			return cards.remove(cards.size()-1);
 		}
 		/* (non-Javadoc)
 		 * @see ucbang.gui.Field.Clickable#move(int, int)
 		 */
 		public void move(int x, int y){
 			int dx = x-rect.x;
 			int dy = y-rect.y;
 			super.move(x, y);
 			Iterator<CardSpace> iter = cards.iterator();
 			while(iter.hasNext()){
 				iter.next().translate(dx, dy);
 			}
 			//add a special boolean here                        
 			iter = fieldCards.iterator();
 			while(iter.hasNext()){
 				iter.next().translate(dx, dy);
 			}
 
 			if(character!=null)character.translate(dx, dy);
 			if(hp!=null)hp.translate(dx, dy);
 		}
 		/* (non-Javadoc)
 		 * @see ucbang.gui.Field.Clickable#translate(int, int)
 		 */
 		public void translate(int dx, int dy){
 			super.translate(dx, dy);
 			Iterator<CardSpace> iter = cards.iterator();
 			while(iter.hasNext()){
 				iter.next().translate(dx, dy);
 			}
 			//add a special boolean here                        
 			iter = fieldCards.iterator();
 			while(iter.hasNext()){
 				iter.next().translate(dx, dy);
 			}
 			if(character!=null)character.translate(dx, dy);
 			if(hp!=null)hp.translate(dx, dy);
 		}
 	}
 	/**
 	 * @author Ibrahim
 	 *
 	 */
 	private abstract class Clickable implements Comparable<Clickable>{
 		public Rectangle rect;
 		//public int location; //position of card on field or in hand
 		public int playerid;
 		public AffineTransform at;
 		private int oldrotation=0;
 		private Clickable partner;
 		/**
 		 * @param r
 		 */
 		public Clickable(Rectangle r){
 			rect=r;
 		}
 		public int compareTo(Clickable o) {
 			if(o.rect.getLocation().y!=rect.getLocation().y)
 				return ((Integer)rect.getLocation().y).compareTo(o.rect.getLocation().y);
 			else
 				return ((Integer)rect.getLocation().x).compareTo(o.rect.getLocation().x);
 		}
 		/**
 		 * Moves the Clickable to the specified location
 		 * @param x
 		 * @param y
 		 */
 		public void move(int x, int y){
 			int dx = x-rect.x;
 			int dy = y-rect.y;
 			if(at!=null)at.translate(rect.x-x, rect.y-y);
 			rect.setLocation(x, y);
 			if(partner!=null){
 				partner.translate(dx, dy);
 			}
 		}
 		
 		/**
 		 * Sets the Clickable's partner.
 		 * <p>If a Clickable has a partner defined, moving it will also
 		 * translate the partner so that they move together.</p>
 		 * @param partner the other Clickable to be set as the partner
 		 */
 		public void setPartner(Clickable partner){
 			this.partner=partner;
 		}
 		/**
 		 * Rotates the clickable the specified number of quadrants, i.e. 90 degree intervals.
 		 * <p>This is fairly buggy, and should not be called more than once under any circumstances for
 		 * a given card or type of card. Deprecated until further notice, since only the bullet card is
 		 * currently rotated. For rotation to work nicely, Clickable will have to store an image of the
 		 * card or whatever so that it is rotated independently of other instances of the same card.
 		 * @param quadrant the number of 90 degree intervals to rotate
 		 */
 		public void rotate(int quadrant){//rotates in terms of 90 degree increments. call with 0 to reset.
 			int realrotation=quadrant-oldrotation;
 			if(realrotation>0 && realrotation<4){
 				if(this instanceof CardSpace){
 					cd.rotateImage(((CardSpace)this).card.name, quadrant);
 				}
 				at = AffineTransform.getQuadrantRotateInstance(realrotation, rect.x+rect.width/2, rect.y+rect.height/2);
 				oldrotation=quadrant;
 				PathIterator iter = rect.getPathIterator(at);
 				int i=0;
 				float[] pts= new float[6];
 				int newx=-1, newy=-1, newwidth=-1, newheight=-1;
 				while(!iter.isDone()){
 					int type = iter.currentSegment(pts);
 					switch(type){
 					case PathIterator.SEG_MOVETO :
 						//temp.add((int)pts[0],(int)pts[1]);
 						//System.out.println(pts[0]+","+pts[1]);
 						break;
 					case PathIterator.SEG_LINETO :
 						if(i==1){
 							newx=(int) pts[0];//misnomers for this part lol.
 							newy=(int) pts[1];
 						}else if(i==3){
 							newwidth=(int)Math.abs(newx-(int)pts[0]);
 							newheight=(int)Math.abs(newy-(int)pts[1]);
 							newx=(int) pts[0];
 							newy=(int) pts[1];
 						}
 						break;
 					}
 					i++;
 					iter.next();
 				}
 				rect = new Rectangle(newx, newy, newwidth, newheight);
 				System.out.println(rect);
 				at=null;
 			}else{
 				//at=null;
 			}
 		}
 		/**
 		 * @param dx
 		 * @param dy
 		 */
 		public void translate(int dx, int dy){
 			rect.translate(dx, dy);
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void mouseMoved(MouseEvent e) {
 		lastMouseMoved = System.currentTimeMillis();
 		hoverpoint=e.getPoint();
 		description=null;
 	}
 	/**
 	 * Scales all objects to the newly resized coordinates.
 	 * @param width
 	 * @param height
 	 * @param width2
 	 * @param height2
 	 */
 	public void resize(int width, int height, int width2, int height2) {
 		ArrayList<Clickable> stuff = clickies.values();
 		Iterator<Clickable> iter = stuff.iterator();
                 for(HandSpace hs: handPlacer){
                     hs.move(hs.rect.x*width2/width, hs.rect.y*height2/height);
                 }
 	}
 	/**
 	 * Sets the given players's HP and updates the bullet display accordingly
 	 * @param playerid the id of the player whose HP changed
 	 * @param lifePoints the amount of HP the player lost
 	 */
 	public void setHP(int playerid, int lifePoints) {
 		if(lifePoints == 0) //bug when saloon is played when you have full hp
 			return;
 		CardSpace hpc = handPlacer.get(playerid).hp;
 		hpc.translate(-10*lifePoints, 0);
 	}
 }
