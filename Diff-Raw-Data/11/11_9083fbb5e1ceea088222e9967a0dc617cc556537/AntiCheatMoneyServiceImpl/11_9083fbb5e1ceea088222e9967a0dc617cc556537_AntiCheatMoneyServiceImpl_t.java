 package net.gtaun.shoebill.example.anticheat;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import net.gtaun.shoebill.SampObjectFactory;
 import net.gtaun.shoebill.Shoebill;
 import net.gtaun.shoebill.event.PlayerEventHandler;
 import net.gtaun.shoebill.event.TimerEventHandler;
 import net.gtaun.shoebill.event.player.PlayerConnectEvent;
 import net.gtaun.shoebill.event.player.PlayerDisconnectEvent;
 import net.gtaun.shoebill.event.timer.TimerTickEvent;
 import net.gtaun.shoebill.object.Player;
 import net.gtaun.shoebill.object.Timer;
 import net.gtaun.shoebill.proxy.GlobalProxyManager;
 import net.gtaun.shoebill.proxy.MethodInterceptor;
 import net.gtaun.shoebill.proxy.MethodInterceptor.Helper;
 import net.gtaun.shoebill.proxy.MethodInterceptor.Interceptor;
 import net.gtaun.shoebill.proxy.MethodInterceptor.InterceptorPriority;
 import net.gtaun.util.event.EventManager;
 import net.gtaun.util.event.EventManager.HandlerPriority;
 import net.gtaun.util.event.ManagedEventManager;
 
 public class AntiCheatMoneyServiceImpl implements AntiCheatMoneyService
 {
 	private static final String METHOD_NAME_SET_MONEY = "setMoney";
 	private static final Class<?>[] METHOD_SIGN_SET_MONEY = {int.class};
 	
 	private static final String METHOD_NAME_GIVE_MONEY = "giveMoney";
 	private static final Class<?>[] METHOD_SIGN_GIVE_MONEY = {int.class};
 	
 	private static final String METHOD_NAME_GET_MONEY = "getMoney";
 	private static final Class<?>[] METHOD_SIGN_GET_MONEY = {};
 	
 	
 	private ManagedEventManager eventManager;
 	private Map<Player, PlayerMoneyChecker> playerMoneyCheckers;
 	
 	private MethodInterceptor setMoneyMethodInterceptor;
 	private MethodInterceptor giveMoneyMethodInterceptor;
 	private MethodInterceptor getMoneyMethodInterceptor;
 	
 	private Timer timer;
 	
 	
 	public AntiCheatMoneyServiceImpl(Shoebill shoebill, EventManager rootEventManager) throws NoSuchMethodException, SecurityException
 	{
 		eventManager = new ManagedEventManager(rootEventManager);
 		playerMoneyCheckers = new WeakHashMap<>();
 		
 		Method setMoneyMethod = Player.class.getMethod(METHOD_NAME_SET_MONEY, METHOD_SIGN_SET_MONEY);
 		Method giveMoneyMethod = Player.class.getMethod(METHOD_NAME_GIVE_MONEY, METHOD_SIGN_GIVE_MONEY);
 		Method getMoneyMethod = Player.class.getMethod(METHOD_NAME_GET_MONEY, METHOD_SIGN_GET_MONEY);
 		
		for (Player player : shoebill.getSampObjectStore().getPlayers()) createPlayerMoneyChecker(player);
		
 		GlobalProxyManager proxyManager = shoebill.getGlobalProxyManager();
 		setMoneyMethodInterceptor = proxyManager.createMethodInterceptor(setMoneyMethod, moneyChangeMethodInterceptor, InterceptorPriority.BOTTOM);
 		giveMoneyMethodInterceptor = proxyManager.createMethodInterceptor(giveMoneyMethod, moneyChangeMethodInterceptor, InterceptorPriority.BOTTOM);
 		getMoneyMethodInterceptor = proxyManager.createMethodInterceptor(getMoneyMethod, getMoneyInterceptor, InterceptorPriority.MONITOR);
 		
 		eventManager.registerHandler(PlayerConnectEvent.class, playerEventHandler, HandlerPriority.MONITOR);
 		eventManager.registerHandler(PlayerDisconnectEvent.class, playerEventHandler, HandlerPriority.BOTTOM);
 		
 		SampObjectFactory factory = shoebill.getSampObjectFactory();
 		timer = factory.createTimer(1000);
 		timer.start();
 		
 		eventManager.registerHandler(TimerTickEvent.class, timer, timerEventHandler, HandlerPriority.NORMAL);
 	}
 	
 	public void uninitialize()
 	{
 		timer.stop();
 		
 		playerMoneyCheckers.clear();
 		
 		setMoneyMethodInterceptor.cancel();
 		giveMoneyMethodInterceptor.cancel();
 		getMoneyMethodInterceptor.cancel();
 		
 		eventManager.cancelAll();
 	}
 	
 	@Override
 	public boolean isMoneyCheckEnabled(Player player)
 	{
 		return playerMoneyCheckers.get(player).isCheckEnabled();
 	}
 	
 	@Override
 	public void setMoneyCheckEnabled(Player player, boolean check)
 	{
 		playerMoneyCheckers.get(player).setCheckEnabled(check);
 	}
 	
 	@Override
 	public void setMoneyCheckTimerInterval(int ms)
 	{
 		timer.setInterval(ms);
 	}
 	
 	private Interceptor moneyChangeMethodInterceptor = new Interceptor()
 	{
 		@SuppressWarnings("deprecation")
 		public Object intercept(Helper helper, Method method, Object obj, Object[] args) throws Throwable
 		{
 			Player player = (Player) obj;
 			PlayerMoneyChecker checker = playerMoneyCheckers.get(player);
 			
 			if (checker.isCheckEnabled() == false) return helper.invokeLower(obj, args);
 			
 			if (checker.isSkipProxying()) return helper.invokeOriginal(obj, args);
 			checker.setSkipProxying(true);
 			
 			try
 			{
 				checker.check();
 				Object ret = helper.invokeLower(obj, args);
 				checker.update();
 				
 				return ret;
 			}
 			finally
 			{
 				checker.setSkipProxying(false);
 			}
 		}
 	};
 	
 	private Interceptor getMoneyInterceptor = new Interceptor()
 	{
 		@SuppressWarnings("deprecation")
 		public Object intercept(Helper helper, Method method, Object obj, Object[] args) throws Throwable
 		{
 			Player player = (Player) obj;
 			PlayerMoneyChecker checker = playerMoneyCheckers.get(player);
 			
 			if (checker.isCheckEnabled() == false) return helper.invokeLower(obj, args);
 			
 			if (checker.isSkipProxying()) return helper.invokeOriginal(obj, args);
 			checker.setSkipProxying(true);
 			
 			try
 			{
 				checker.check();
 				return helper.invokeLower(obj, args);
 			}
 			finally
 			{
 				checker.setSkipProxying(false);
 			}
 		}
 	};
 	
	private void createPlayerMoneyChecker(Player player)
	{
		playerMoneyCheckers.put(player, new PlayerMoneyChecker(player, AntiCheatMoneyServiceImpl.this, eventManager));
	}
	
 	private PlayerEventHandler playerEventHandler = new PlayerEventHandler()
 	{
 		public void onPlayerConnect(PlayerConnectEvent event)
 		{
 			Player player = event.getPlayer();
			createPlayerMoneyChecker(player);
 		}
 		
 		public void onPlayerDisconnect(PlayerDisconnectEvent event)
 		{
 			Player player = event.getPlayer();
 			playerMoneyCheckers.remove(player);
 		}
 	};
 	
 	private TimerEventHandler timerEventHandler = new TimerEventHandler()
 	{
 		public void onTimerTick(TimerTickEvent event)
 		{
 			for (PlayerMoneyChecker checker : playerMoneyCheckers.values())
 			{
 				checker.check();
 			}
 		}
 	};
 }
