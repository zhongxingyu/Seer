package chattest;

import org.jibble.pircbot.*;

public class ChatBot extends PircBot {

	public ChatBot()
	{
		super();
		this.setName("GuestBot");
		this.setAutoNickChange(true);
		this.setLogin("Guest");
	}
}
