 package org.dyndns.pamelloes.SpoutCasino.gui.holdem;
 
 import org.dyndns.pamelloes.SpoutCasino.cards.Card;
 import org.dyndns.pamelloes.SpoutCasino.cards.Card.CardNumber;
 import org.dyndns.pamelloes.SpoutCasino.cards.Card.Suit;
 import org.dyndns.pamelloes.SpoutCasino.games.HoldemController;
 import org.dyndns.pamelloes.SpoutCasino.gui.CardWidget;
 import org.dyndns.pamelloes.SpoutCasino.gui.TableGui;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.Gradient;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class HoldemGui extends TableGui {
 	private HoldemController control;
 	private CardWidget left, right;
 	private OtherPlayerGui[] others = new OtherPlayerGui[4];
 	private Container community;
 	private Gradient bg;
 	private Color dealercolor = new Color(49,70,224,128), turncolor = new Color(224,70,49,128);
 	private int id;
 	private Label remaining, remcaption, pot, caption;
 	private Button call, fold, raise1, raise10;
	private boolean turn, dealer, full;
 	private int callamnt;
 
 	public HoldemGui(SpoutPlayer player, HoldemController control, int id) {
 		super(player);
 		this.control = control;
 		this.id = id;
 		makeGui();
 	}
 
 	protected void makeGui() {
 		setSize(200,200);
 		
 		Container rows = new GenericContainer();
 		rows.setLayout(ContainerType.VERTICAL);
 		rows.addChild(getTopRow());
 		rows.addChild(getMiddleRow());
 		rows.addChild(getBottomRow());
 		rows.setMargin(10);
 		container.addChild(rows);
 		
 		remcaption = new GenericLabel("Time Left:");
 		remcaption.setAlign(WidgetAnchor.TOP_LEFT).setWidth(200).setHeight(200).setFixed(true).setVisible(false).setMargin(15).setPriority(RenderPriority.Lowest);
 		remaining = new GenericLabel("0");
 		remaining.setAlign(WidgetAnchor.TOP_LEFT).setWidth(200).setHeight(200).setFixed(true).setVisible(false).setMargin(25,15).setPriority(RenderPriority.Lowest);
 		container.addChildren(remcaption,remaining);
 	}
 	
 	private Container getTopRow() {
 		Container top = new GenericContainer();
 		top.setLayout(ContainerType.HORIZONTAL);
 		top.addChild(new GenericLabel(""));
 		
 		OtherPlayerGui opg = new OtherPlayerGui(control.getDeck());
 		top.addChild(opg);
 		others[(id + 2) % 4] = opg;
 		
 		top.addChild(new GenericLabel(""));
 		return top;
 	}
 	
 	private Container getMiddleRow() {
 		Container mid = new GenericContainer();
 		mid.setLayout(ContainerType.HORIZONTAL);
 		
 		OtherPlayerGui opg = new OtherPlayerGui(control.getDeck());
 		mid.addChild(opg);
 		others[(id + 1) % 4] = opg;
 		
 		Container center = new GenericContainer() {
 			@Override
 			public Container setWidth(int width) {
 				pot.setMarginLeft(width/2);
 				return super.setWidth(width);
 			}
 			
 			@Override
 			public Container setHeight(int height) {
 				pot.setMarginTop(height/4);
 				return super.setWidth(height);
 			}
 		};
 		center.setLayout(ContainerType.VERTICAL);
 		pot = new GenericLabel("Pot: 0");
 		pot.setAlign(WidgetAnchor.CENTER_CENTER);
 		center.addChild(pot);
 		community = new GenericContainer();
 		community.setLayout(ContainerType.HORIZONTAL);
 		center.addChild(community);
 		mid.addChild(center);
 		
 		opg = new OtherPlayerGui(control.getDeck());
 		mid.addChild(opg);
 		others[(id + 3) % 4] = opg;
 		return mid;
 	}
 	
 	private Container getBottomRow() {
 		Container bot = new GenericContainer();
 		bot.setLayout(ContainerType.HORIZONTAL);
 		Container lcont = new GenericContainer();
 		lcont.setLayout(ContainerType.VERTICAL);
 		call = new GenericButton("Call") {
 			@Override
 			public void onButtonClick(ButtonClickEvent e) {
 				bet(callamnt);
 			}
 		};
 		call.setMargin(5).setPriority(RenderPriority.Lowest).setWidth(10).setHeight(10);
 		lcont.addChild(call);
 		fold = new GenericButton("Fold") {
 			@Override
 			public void onButtonClick(ButtonClickEvent e) {
 				control.fold();
 			}
 		};
 		fold.setMargin(5).setPriority(RenderPriority.Lowest).setWidth(10).setHeight(10);
 		lcont.addChild(fold);
 		bot.addChild(lcont);
 		
 		Container cards = new GenericContainer();
 		cards.setLayout(ContainerType.HORIZONTAL).setMargin(0,5,0,-5);
 		left = new CardWidget(new Card(control.getDeck(),Suit.Joker, CardNumber.Joker1));
 		left.setWidth(34).setFixed(true).setPriority(RenderPriority.Low).setMargin(3);
 		right = new CardWidget(new Card(control.getDeck(),Suit.Joker, CardNumber.Joker2));
 		right.setWidth(34).setFixed(true).setPriority(RenderPriority.Low).setMargin(3);
 		cards.addChildren(left,right);
 		bot.addChild(cards);
 
 		Container rcont = new GenericContainer();
 		rcont.setLayout(ContainerType.VERTICAL);
 		raise1 = new GenericButton("Raise 1") {
 			@Override
 			public void onButtonClick(ButtonClickEvent e) {
 				bet(callamnt + 1);
 			}
 		};
 		raise1.setMargin(5,9,5,-5).setPriority(RenderPriority.Lowest).setWidth(10).setHeight(10);
 		rcont.addChild(raise1);
 		raise10 = new GenericButton("Raise 10") {
 			@Override
 			public void onButtonClick(ButtonClickEvent e) {
 				bet(callamnt + 10);
 			}
 		};
 		raise10.setMargin(5,9,5,-5).setPriority(RenderPriority.Lowest).setWidth(10).setHeight(10);
 		rcont.addChild(raise10);
 		bot.addChild(rcont);
 		
 		GenericContainer bottom = new GenericContainer() {
 			@Override
 			public Container setWidth(int width) {
 				caption.setMarginLeft(width/2 - 8);
 				return super.setWidth(width);
 			}
 			
 			@Override
 			public Container setHeight(int height) {
 				caption.setMarginTop(height/4);
 				return super.setWidth(height);
 			}
 		};
 		bg = new GenericGradient(dealercolor);
 		bg.setPriority(RenderPriority.Normal).setVisible(false).setMargin(2,0,-2,0).setWidth(10).setHeight(10);
 		caption = new GenericLabel("Waiting");
 		caption.setScale(1.5f).setTextColor(new Color(1.0f,0.3f,0.3f)).setAlign(WidgetAnchor.CENTER_CENTER).setPriority(RenderPriority.Low).setMargin(0);
 		bottom.setLayout(ContainerType.OVERLAY);
 		bottom.addChildren(bot,bg,caption);
 		return bottom;
 	}
 	
 	@Override
 	public void onClose(ScreenCloseEvent e) {
 		control.handlePlayerExit(e.getPlayer());
 	}
 	
 	public void showCards(Card left, Card right) {
 		this.left.setCard(left);
 		this.right.setCard(right);
 	}
 	
 	public void setState(int id, int state) {
 		if(others[id] == null) setState(state);
 		else others[id].setState(state);
 	}
 	
 	public void setTurn(int id, boolean full) {
		this.full = full;
 		if(id == this.id) {
 			turn = true;
 			bg.setVisible(true);
 			bg.setColor(turncolor);
 			int chips = control.countChips(player);
 			call.setEnabled(chips >= callamnt);
 			fold.setEnabled(true);
 			raise1.setEnabled(full && chips >= callamnt + 1);
 			raise10.setEnabled(full && chips >= callamnt + 10);
 		} else {
 			turn = false;
 			call.setEnabled(false);
 			fold.setEnabled(false);
 			raise1.setEnabled(false);
 			raise10.setEnabled(false);
 			if(dealer) {
 				bg.setVisible(true);
 				bg.setColor(dealercolor);
 			} else {
 				bg.setVisible(false);
 			}
 		}
 		
 		for(int i = 0; i < 4; i++) if(others[i]!=null) {
 			if(i == id) others[i].setTurn(true);
 			else others[i].setTurn(false);
 		}
 	}
 	
 	public void setDealer(int id) {
 		if(id == this.id) {
 			dealer = true;
 			if(!turn) {
 				bg.setVisible(true);
 				bg.setColor(dealercolor);
 			}
 		} else {
 			dealer = false;
 			if(!turn) bg.setVisible(false);
 		}
 		
 		for(int i = 0; i <4; i++) if(others[i]!=null) {
 			if(i == id) others[i].setDealer(true);
 			else others[i].setDealer(false);
 		}
 	}
 	
 	public void flop(Card a, Card b, Card c) { 
 		community.addChild(new CardWidget(a).setWidth(23));
 		community.addChild(new CardWidget(b).setWidth(23));
 		community.addChild(new CardWidget(c).setWidth(23));
 	}
 	
 	public void turn(Card a) {
 		community.addChild(new CardWidget(a).setWidth(23));
 	}
 	
 	public void river(Card a) {
 		community.addChild(new CardWidget(a).setWidth(23));
 	}
 	
 	public void clearCommunity() {
 		for(Widget w : community.getChildren()) community.removeChild(w);
 	}
 	
 	public void setTimeRemaining(int time, boolean inprogress) {
 		remaining.setText("" + time);
 		remcaption.setText(inprogress ? "Time Left:" : "New Game:");
 	}
 	
 	public void setPot(int pot) {
 		this.pot.setText("Pot: " + pot);
 	}
 	
 	public void setCall(int call) {
 		this.call.setText(call > 0 ? "Call " + call : "Check");
 		callamnt = call;
 	}
 	
 	private void setState(int state) {
 		switch(state) {
 		case 0:
 			caption.setText("Empty").setVisible(true); //this shouldn't be possible....
 			left.setVisible(false);
 			right.setVisible(false);
 			call.setVisible(false);
 			fold.setVisible(false);
 			raise1.setVisible(false);
 			raise10.setVisible(false);
 			break;
 		case 1:
 			caption.setText("Waiting").setVisible(true);
 			left.setVisible(false);
 			right.setVisible(false);
 			call.setVisible(false);
 			fold.setVisible(false);
 			raise1.setVisible(false);
 			raise10.setVisible(false);
 			break;
 		case 2:
 			caption.setVisible(false);
 			left.setVisible(true);
 			right.setVisible(true);
 			call.setVisible(true);
 			fold.setVisible(true);
 			raise1.setVisible(true);
 			raise10.setVisible(true);
 			break;
 		case 3:
 			caption.setText("Folded").setVisible(true);
 			left.setVisible(false);
 			right.setVisible(false);
 			call.setVisible(false);
 			fold.setVisible(false);
 			raise1.setVisible(false);
 			raise10.setVisible(false);
 			break;
 		}
 	}
 	
 	private void bet(int amnt) {
 		control.takeChips(player,amnt);
		if(full) control.setCall(amnt);
 		control.bet(amnt);
 		control.advanceTurn();
 	}
 }
