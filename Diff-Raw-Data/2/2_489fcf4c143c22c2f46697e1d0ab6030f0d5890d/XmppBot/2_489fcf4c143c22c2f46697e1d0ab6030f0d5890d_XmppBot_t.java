 package de.raion.xmppbot;
 
 /*
  * #%L
  * XmppBot Core
  * %%
  * Copyright (C) 2012 - 2013 Bernd Kiefer <b.kiefer@raion.de>
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import net.dharwin.common.tools.cli.api.CLIContext;
 import net.dharwin.common.tools.cli.api.CommandLineApplication;
 import net.dharwin.common.tools.cli.api.annotations.CLICommand;
 import net.dharwin.common.tools.cli.api.annotations.CLIEntry;
 import net.dharwin.common.tools.cli.api.exceptions.CLIInitException;
 import net.dharwin.common.tools.cli.api.utils.CLIAnnotationDiscovereryListener;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.FromContainsFilter;
 import org.jivesoftware.smack.filter.NotFilter;
 import org.jivesoftware.smackx.muc.DiscussionHistory;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.impetus.annovention.ClasspathDiscoverer;
 import com.impetus.annovention.Discoverer;
 
 import de.raion.xmppbot.annotation.MultiUserChatListener;
 import de.raion.xmppbot.annotation.PacketInterceptor;
 import de.raion.xmppbot.command.core.AbstractXmppCommand;
 import de.raion.xmppbot.config.BotConfiguration;
 import de.raion.xmppbot.config.XmppConfiguration;
 import de.raion.xmppbot.plugin.AbstractMessageListenerPlugin;
 import de.raion.xmppbot.plugin.MessageListenerPlugin;
 import de.raion.xmppbot.plugin.PluginStatusListener;
 
 /**
  *<h2>Enbot Botson</h2>
  *
  *a simple xmppbot providing a framework for commands and plugins.<br>
  *
  *@see AbstractXmppCommand for commands
  *@see CLICommand marker annotation for commands
  *@see AbstractMessageListenerPlugin for plugins
  *@see MessageListenerPlugin marker annotation for plugins
  */
 @SuppressWarnings("rawtypes")
 @CLIEntry
 public class XmppBot extends CommandLineApplication implements ChatManagerListener, PluginStatusListener {
 
 	private static Logger log = LoggerFactory.getLogger(XmppBot.class);
 
 	private Map<String, XMPPConnection> connectionMap;
 	private Map<String, MultiUserChat> multiUserChatMap;
 	private Map<String, Chat> chatMap;
 	private HashMap<MultiUserChat, Set<String>> multiUserChatPresenceMap;
 	private ChatMessageListener messageHandler;
 	private Map<String, Class<PacketInterceptor>> packetInterceptorMap;
 	private Map<String, Class<MultiUserChatListener>> multiUserChatListenerMap;
 
 	private BotConfiguration configuration;
 
 
 	/**
 	 * constructor
 	 * @throws CLIInitException if the initilization of the CommandLineInterface fails
 	 */
 	public XmppBot() throws CLIInitException {
 
 		connectionMap = new HashMap<String, XMPPConnection>();
 		multiUserChatMap = new HashMap<String, MultiUserChat>();
 		chatMap = new HashMap<String, Chat>();
 		multiUserChatPresenceMap = new HashMap<MultiUserChat, Set<String>>();
 		messageHandler = new ChatMessageListener(this);
 
 		packetInterceptorMap = loadPacketInterceptors();
 		multiUserChatListenerMap = loadMultiUserChatListener();
 
 	}
 
 
 	/**
 	 * initializes the bot with the given configuration
 	 * @param aConfig the configuration to use
 	 */
 	@SuppressWarnings("unchecked")
 	public void init(BotConfiguration aConfig)  {
 		try {
 			configuration = aConfig;
 
 			super._commands = loadCommands();
 
 			Map<String, ConnectionConfiguration> conConfigMap = prepareConnectionConfiguration(aConfig.getConfigurations());
 
 			connectionMap = initConnections(aConfig, conConfigMap);
 
 			this.registerChatListener(this, connectionMap);
 
 			getContext().init();
 
 			Collection<XMPPConnection> connections = connectionMap.values();
 
 			for (XMPPConnection connection : connections) {
 				addPlugins(connection);
 			}
 		}
 		catch(Exception e) {
 			log.error("init(BotConfiguration) - ", e);
 		}
 	}
 
 
 	/**
 	 * @return context with thread-specific settings
 	 */
 	public XmppContext getContext() {
 		return (XmppContext) _appContext;
 	}
 
 
 	/** implementation of the ChatManagerListener interface.<br>
 	 *  method is called when a new chat request is incoming
 	 *  @param chat the incoming chat
 	 *  @param createdLocally true if local created, otherwise false
 	 * @see org.jivesoftware.smack.ChatManagerListener#chatCreated(org.jivesoftware.smack.Chat, boolean)
 	 */
 	public void chatCreated(Chat chat, boolean createdLocally) {
 		if (!createdLocally) {
 			chat.addMessageListener(messageHandler);
 			log.info("incoming chat from {} with threadId {}",chat.getParticipant(), chat.getThreadID());
 			chatMap.put(chat.getParticipant().trim(), chat);
 		}
 	}
 
 
 	/**
 	 * processes a incoming command
 	 * @param cmdString the command as string
 	 */
 	public void processCommand(String cmdString) {
 		log.debug("Thread = " + Thread.currentThread().getName());
 		super.processInputLine(cmdString);
 	}
 
 
 	/**
 	 * get multiuserchat by name
 	 * @param mucName name of the multiuserchat
 	 * @return multiuserchat or null if not available
 	 */
 	public MultiUserChat getMultiUserChat(String mucName) {
 		return this.multiUserChatMap.get(mucName);
 	}
 
 
 	/**
 	 * marks aUser for the MultiUserChat muc as available
 	 * @param muc MultiUserChat
 	 * @param aUser the user
 	 */
 	public void userAvailable(MultiUserChat muc, String aUser) {
 	
 		if (this.multiUserChatPresenceMap.containsKey(muc)) {
 			this.multiUserChatPresenceMap.get(muc).add(aUser);
 		} else {
 			HashSet<String> userSet = new HashSet<String>();
 			userSet.add(aUser);
 			this.multiUserChatPresenceMap.put(muc, userSet);
 		}
 	}
 
 
 	/**
 	 * removes aUser from the {@link #multiUserChatPresenceMap} mapped by muc
 	 * @param muc MultiUserChat as key
 	 * @param aUser the user to mark as unavailable
 	 */
 	public void userUnavailabe(MultiUserChat muc, String aUser) {
 	
 		if (this.multiUserChatPresenceMap.containsKey(muc)) {
 			this.multiUserChatPresenceMap.get(muc).remove(aUser);
 		}
 	}
 
 
 	/**
 	 * the available user for a certain multiuserchat
 	 * @param muc multiuserchat
 	 * @return available user names
 	 */
 	public Set<String> getAvailableUser(MultiUserChat muc) {
 		return multiUserChatPresenceMap.get(muc);
 	}
 
 	
 	/**
 	 * chat by name
 	 * @param participant user name
 	 * @return the chat or null if not available
 	 */
 	public Chat getChat(String participant) {
 		return chatMap.get(participant);
 	}
 
 
 	/**
 	 * Load the necessary commands for this application.
 	 *
 	 * @return The map of commands.
 	 * @throws CLIInitException when the initialization of the CLIContext failed
 	 */
 	private Map<String, Class<AbstractXmppCommand>> loadCommands() throws CLIInitException  {
 
 		Discoverer discoverer = new ClasspathDiscoverer();
 		CLIAnnotationDiscovereryListener discoveryListener = new CLIAnnotationDiscovereryListener(
 				new String[] { CLICommand.class.getName() });
 		discoverer.addAnnotationListener(discoveryListener);
 		discoverer.discover();
 
 		return loadCommands(discoveryListener.getDiscoveredClasses());
 	}
 
 
 	private Map<String, Class<AbstractXmppCommand>> loadCommands(List<String> commandClasses)
 			throws CLIInitException {
 
 		Map<String, Class<AbstractXmppCommand>> commandMap = new HashMap<String, Class<AbstractXmppCommand>>();
 
 		for (String commandClassName : commandClasses) {
 
 			try {
 				@SuppressWarnings("unchecked")
 				Class<AbstractXmppCommand> commandClass = (Class<AbstractXmppCommand>) Class
 						.forName(commandClassName);
 
 				if (AbstractXmppCommand.class.isAssignableFrom(commandClass)) {
 					CLICommand annotation = commandClass.getAnnotation(CLICommand.class);
 
 					commandMap.put(annotation.name().toLowerCase(), commandClass);
 					log.debug("Loaded command [" + annotation.name() + "].");
 				}
 
 			} catch (ClassNotFoundException e) {
 				throw new CLIInitException("Unable to find command class [" + commandClassName
 						+ "].");
 			} catch (Exception e) {
 				throw new CLIInitException("Unable to load command class [" + commandClassName
 						+ "]: " + e.getMessage());
 			}
 		}
 		return commandMap;
 	}
 
 
 	/**
 	 * maps the configuration information for Xmpp connections into smackx ConnectionConfiguration
 	 * @param configMap the configuration info to use
 	 * @return smackx ConnectionConfiguration mapped by the configured name
 	 */
 	private Map<String, ConnectionConfiguration> prepareConnectionConfiguration(
 			Map<String, XmppConfiguration> configMap) {
 
 		Map<String, ConnectionConfiguration> connections = new HashMap<String, ConnectionConfiguration>();
 
 		Set<String> keySet = configMap.keySet();
 
 		for (String key : keySet) {
 
 			XmppConfiguration xmppConfig = configMap.get(key);
 
 			String host = xmppConfig.getHost();
 			int port = xmppConfig.getPort();
 			String serviceName = xmppConfig.getServiceName();
 
 			if (serviceName == null || serviceName.equals("")) {
 				serviceName = host;
 			}
 
 			connections.put(key.toLowerCase(), new ConnectionConfiguration(host, port, serviceName));
 		}
 		return connections;
 	}
 
 
 	/**
 	 * establishes the XMPPConnections with the given BotConfiguration and
 	 * the XMPP ConnectionConfiguration and joins the configured MultiUserChats and Chats
 	 * @param aConfig the BotConfiguration to use
 	 * @param connectionConfigurationMap the ConnectionConfigurations to use for establishing
 	 * 		  XMPPConnections mapped by configured name
 	 * @return established XMPPConnections mapped by the configured name from the BotConfiguration
 	 */
 	private Map<String, XMPPConnection> initConnections(BotConfiguration aConfig,
 			Map<String, ConnectionConfiguration> connectionConfigurationMap) {
 
 		Map<String, XMPPConnection> aConnectionMap = new HashMap<String, XMPPConnection>();
 		Map<String, XmppConfiguration> xmppConfigMap = aConfig.getConfigurations();
 
 		Connection.DEBUG_ENABLED = aConfig.isXmppConnectionDebuggingEnabled();
 
 		Set<String> keySet = connectionConfigurationMap.keySet();
 		for (String key : keySet) {
 
 			ConnectionConfiguration cc = connectionConfigurationMap.get(key);
 			XmppConfiguration xmppConfig = xmppConfigMap.get(key);
 
 			try {
 
 				XMPPConnection connection = new XMPPConnection(cc);
 				connection.connect();
 				log.info("connection established to server '{}'", xmppConfig.getHost());
 
 				String jabberId = xmppConfig.getJabberId() + "/bot";
 				String pwd = xmppConfig.getPassword();
 
 				connection.login(jabberId, pwd);
 				log.info("logged in with name '{}'", jabberId);
 
 				joinMultiUserChats(xmppConfig, connection);
 				joinChats(xmppConfig, connection);
 
 				aConnectionMap.put(key, connection);
 			} catch (XMPPException e) {
 				log.error("login failed to server {} with nickname {}", xmppConfig.getHost(),
 						xmppConfig.getNickName());
 			}
 		}
 		return aConnectionMap;
 	}
 
 
 	private XMPPConnection addPlugins(XMPPConnection connection) {
 
 		Collection<AbstractMessageListenerPlugin> plugins = getContext().getPluginManager()
 				                                                        .getEnabledPlugins()
 				                                                        .values();
 		// excluding messages from enbot himself :)
 		List<String> nickNameList = getOwnNickNames();
 		List<NotFilter> notFromFilterList = new ArrayList<NotFilter>();
 
 		for (String nickName : nickNameList) {
 			FromContainsFilter fromFilter = new FromContainsFilter(nickName);
 			notFromFilterList.add(new NotFilter(fromFilter));
 		}
 
 		for(AbstractMessageListenerPlugin plugin : plugins){
 
 			NotFilter[] notFilter = new NotFilter[notFromFilterList.size()];
 			AndFilter andFilter = new AndFilter(notFromFilterList.toArray(notFilter));
 			andFilter.addFilter(plugin.getAcceptFilter());
 
 			connection.addPacketListener(plugin, andFilter);
 		}
 		return connection;
 	}
 
 
 	private List<String> getOwnNickNames() {
 		List<String> list = new ArrayList<String>();
 		Collection<XmppConfiguration> c = configuration.getConfigurations().values();
 		for (XmppConfiguration xmppConfiguration : c) {
 			list.add(xmppConfiguration.getNickName());
 		}
 		return list;
 	}
 
 
 
 	private void joinMultiUserChats(XmppConfiguration xmppConfig, XMPPConnection connection) {
 
 		Collection<String> mucNameCollection = xmppConfig.getMultiUserChats().values();
 
 		for (String mucName : mucNameCollection) {
 			try {
 				if (packetInterceptorMap.containsKey(xmppConfig.getServiceType())) {
 					AbstractPacketInterceptor interceptor = (AbstractPacketInterceptor) packetInterceptorMap
 							.get(xmppConfig.getServiceType()).newInstance();
 
 					interceptor.setContext(getContext());
 					connection.addPacketInterceptor(interceptor, interceptor.getPacketFilter());
 				}
 
 				// start TODO remove workareound handly of muclistener
 				DiscussionHistory history = new DiscussionHistory();
 				history.setMaxStanzas(0);
 				MultiUserChat muc = new MultiUserChat(connection, mucName);
 
 				if(multiUserChatListenerMap.containsKey(xmppConfig.getServiceType())) {
 					Class<MultiUserChatListener> mucListenerClass = multiUserChatListenerMap.get(xmppConfig.getServiceType());
 					Constructor<MultiUserChatListener> constructor = mucListenerClass.getConstructor(XmppBot.class);
 					AbstractMultiUserChatListener mucListener = (AbstractMultiUserChatListener)constructor.newInstance(this);
 					muc.addMessageListener(mucListener);
 				}
 
 				muc.join(xmppConfig.getNickName(), xmppConfig.getPassword(), history,
 						SmackConfiguration.getPacketReplyTimeout());
 				log.info("joined multiuserchat '{}' with address {}", mucName, muc.getRoom());
 
 				this.multiUserChatMap.put(mucName, muc);
 
 				// TODO maybe removing
 				Iterator<String> it = muc.getOccupants();
 
 				while (it.hasNext()) {
 					userAvailable(muc, it.next());
 				}
 
 			} catch (Exception e) {
				log.error("Exception caught in joinChannels for multiuserchat '{}' : {}", mucName, e.getMessage() );
 			}
 		}
 	}
 
 
 	private Map<String, Class<PacketInterceptor>> loadPacketInterceptors() {
 		Discoverer discoverer = new ClasspathDiscoverer();
 		CLIAnnotationDiscovereryListener discoveryListener = new CLIAnnotationDiscovereryListener(
 				new String[] { PacketInterceptor.class.getName() });
 		discoverer.addAnnotationListener(discoveryListener);
 		discoverer.discover();
 
 		List<String> list = discoveryListener.getDiscoveredClasses();
 
 		HashMap<String, Class<PacketInterceptor>> map = new HashMap<String, Class<PacketInterceptor>>();
 
 		for (String className : list) {
 			try {
 				@SuppressWarnings("unchecked")
 				Class<PacketInterceptor> clazz = (Class<PacketInterceptor>) Class.forName(className);
 
 				PacketInterceptor annotation = clazz.getAnnotation(PacketInterceptor.class);
 
 				map.put(annotation.service().toLowerCase(), clazz);
 
 			} catch (ClassNotFoundException e) {
 				log.error("loadPacketInterceptors()", e);
 			}
 		}
 		return map;
 	}
 
 
 	// TODO reduce redundance of duplicated code use template instead
 	private Map<String, Class<MultiUserChatListener>> loadMultiUserChatListener() {
 		Discoverer discoverer = new ClasspathDiscoverer();
 		CLIAnnotationDiscovereryListener discoveryListener = new CLIAnnotationDiscovereryListener(
 				new String[] { MultiUserChatListener.class.getName() });
 		discoverer.addAnnotationListener(discoveryListener);
 		discoverer.discover();
 
 		List<String> list = discoveryListener.getDiscoveredClasses();
 
 		HashMap<String, Class<MultiUserChatListener>> map = new HashMap<String, Class<MultiUserChatListener>>();
 
 		for (String className : list) {
 
 			try {
 				@SuppressWarnings("unchecked")
 				Class<MultiUserChatListener> clazz = (Class<MultiUserChatListener>) Class.forName(className);
 
 				MultiUserChatListener annotation = clazz.getAnnotation(MultiUserChatListener.class);
 
 				map.put(annotation.service().toLowerCase(), clazz);
 
 			} catch (ClassNotFoundException e) {
 				log.error("loadMultiUserChatListener()", e);
 			}
 		}
 		return map;
 	}
 
 
 	// TODO implement!
 	private void joinChats(XmppConfiguration xmppConfig, XMPPConnection connection) {
 
 		Set<String> keySet = xmppConfig.getChats().keySet();
 	}
 
 
 	private void registerChatListener(ChatManagerListener chatListener,
 			Map<String, XMPPConnection> conMap) {
 
 		Set<String> keySet = conMap.keySet();
 
 		for (String key : keySet) {
 
 			XMPPConnection connection = conMap.get(key);
 			connection.getChatManager().addChatListener(chatListener);
 		}
 	}
 
 
 	/**
 	 * <b>does nothing! disables shutdown</b>
 	 * @see net.dharwin.common.tools.cli.api.CommandLineApplication#shutdown()
 	 */
 	@Override
 	protected void shutdown() {/* do nothing here */}
 
 	@Override
 	protected CLIContext createContext() { return new XmppContext(this); }
 
 
 	/**
 	 * @return configuration object of enbot
 	 */
 	public BotConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	/**
 	 * checks if Command command is available
 	 * @param command cmd
 	 * @return true if available otherwise false
 	 */
 	public boolean hasCommand(String command) {
 		return getCommandNames().contains(command);
 
 	}
 
 	public <T> void pluginDisabled(String pluginName, AbstractMessageListenerPlugin<T> plugin) {
 		// TODO Auto-generated method stub
 	}
 
 
 	public <T> void pluginEnabled(String pluginName, AbstractMessageListenerPlugin<T> plugin) {
 		// TODO Auto-generated method stub
 	}
 
 
 	/**
 	 * starting the xmppbot
 	 * @param args arguments, arg[0] should link to the named configfile, otherwise
 	 * 		  Enbot will lookup for <code>xmppbot.json</code> in the workingdirectory
 	 * @throws Exception if an not expected Exception occure
 	 */
 	public static void main(String[] args) throws Exception {
 
 		XmppBot bot = new XmppBot();
 
 		File configFile = null;
 
 		if (args.length == 0) {
 			String fileName = bot.getContext().getString("xmppbot.configuration.filename",
 					"xmppbot.json");
 			configFile = new File(fileName);
 
 		} else {
 			configFile = new File(args[0]);
 		}
 
 		log.info(configFile.getAbsolutePath());
 
 		ObjectMapper mapper = new ObjectMapper();
 		BotConfiguration config = mapper.readValue(configFile, BotConfiguration.class);
 
 		log.debug(config.toString());
 
 		bot.init(config);
 		TimeUnit.HOURS.sleep(1);
 
 	}
 }
