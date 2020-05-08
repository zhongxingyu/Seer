 package org.pileus.spades;
 
 public class Spades
 {
 	/* Properties */
 	public  Task    task;
 	public  Cards   cards;
 	public  String  admin;
 
 	/* Static methods */
 	private static String[] getCards(String msg, String regex)
 	{
 		String cards = msg
 			.replaceAll(regex, "$1")
 			.replaceAll(",", " ")
 			.replaceAll("♠", "s")
 			.replaceAll("♥", "h")
 			.replaceAll("♦", "d")
 			.replaceAll("♣", "c");
 		Os.debug("Cards: getCards - [" + cards + "]:" + Os.base64(cards));
 		return cards.split("\\s+");
 	}
 
 	/* Widget callback functions */
 	public Spades(String admin)
 	{
 		this.admin = admin;
 	}
 
 	/* IRC Callbacks */
 	public void onMessage(Message msg)
 	{
 		Os.debug("Spades: onMessage - " + msg.msg);
 		if (msg.type != Message.Type.PRIVMSG)
 			return;
 		if (!msg.from.equals(this.admin))
 			return;
 
 		String txt = msg.txt;
 		if (txt.startsWith("You have: ")) {
 			this.cards.hand = Spades.getCards(txt, "You have: (.*)");
 			this.cards.requestRender();
 		}
		if (txt.matches(".*turn!.*")) {
			this.cards.pile = Spades.getCards(txt, ".*turn! \\((.*)\\)");
 			this.cards.requestRender();
 		}
 	}
 
 	/* UI Callbacks */
 	public boolean onBid(int bid)
 	{
 		Os.debug("Spades: onBid - " + bid);
 		return this.send(".bid " + bid);
 	}
 
 	public boolean onPass(String card)
 	{
 		Os.debug("Spades: onPass - " + card);
 		return this.send(".pass " + card);
 	}
 
 	public boolean onLook()
 	{
 		Os.debug("Spades: onLook");
 		return this.send(".look");
 	}
 
 	public boolean onPlay(String card)
 	{
 		Os.debug("Spades: onPlay - " + card);
 		return this.send(".play " + card);
 	}
 
 	public boolean onTurn()
 	{
 		Os.debug("Spades: onTurn");
 		return this.send(".turn");
 	}
 
 	/* Helper functions */
 	private boolean send(String msg)
 	{
 		if (this.task == null)
 			return false;
 		this.task.send(msg);
 		return true;
 	}
 }
