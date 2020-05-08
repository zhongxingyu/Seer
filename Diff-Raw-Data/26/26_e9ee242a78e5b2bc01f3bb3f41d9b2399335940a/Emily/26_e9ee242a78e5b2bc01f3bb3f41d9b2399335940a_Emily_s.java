 package roujo.emily.core;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.ConnectEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 import roujo.emily.core.commands.CommandHandler;
 
 public class Emily extends ListenerAdapter<PircBotX> {
 	private State state;
 	private String password;
 	private PircBotX bot;
 
 	public Emily(String nickname, String password) {
 		this.password = password;
 		this.state = new State();
 		this.bot = new PircBotX();
 		this.bot.getListenerManager().addListener(this);
 		this.bot.setCapEnabled(true);
 
 		bot.setName(nickname);
 		bot.setAutoNickChange(true);
 		bot.setLogin("Emily");
		bot.setVersion("Emily v1.0 | IRC Bot based on PircBotX 1.9");
 	}
 
 	@Override
 	public void onConnect(ConnectEvent<PircBotX> event) throws Exception {
 		if (!password.equals(""))
 			bot.identify(password);
 	};
 
 	@Override
 	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
 		if (state.shouldQuit())
 			return;
 
 		MessageContext context = new MessageContext(state, event);
 		CommandHandler.processMessage(context);
 	}
 
 	@Override
 	public void onPrivateMessage(PrivateMessageEvent<PircBotX> event)
 			throws Exception {
 		if (state.shouldQuit())
 			return;
 
 		MessageContext context = new MessageContext(state, event);
 		CommandHandler.processMessage(context);
 	}
 
 	// Has to be reworked, so removed for now
 	/*
 	 * public void onConsoleMessage(String message) { Context context = new
 	 * Context(this, getNick(), message);
 	 * CommandHandler.processMessage(context); }
 	 */
 
 	public State getState() {
 		return state;
 	}
 
 	public PircBotX getBot() {
 		return bot;
 	}
 }
