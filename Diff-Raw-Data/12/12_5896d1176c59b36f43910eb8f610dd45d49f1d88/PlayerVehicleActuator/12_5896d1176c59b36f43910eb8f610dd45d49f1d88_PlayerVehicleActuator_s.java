 package net.gtaun.wl.vehicle;
 
 import net.gtaun.shoebill.Shoebill;
 import net.gtaun.shoebill.common.player.PlayerLifecycleHolder.PlayerLifecycleObject;
 import net.gtaun.shoebill.constant.VehicleComponentModel;
 import net.gtaun.shoebill.event.TimerEventHandler;
 import net.gtaun.shoebill.event.VehicleEventHandler;
 import net.gtaun.shoebill.event.timer.TimerTickEvent;
 import net.gtaun.shoebill.event.vehicle.VehicleUpdateDamageEvent;
 import net.gtaun.shoebill.object.Player;
 import net.gtaun.shoebill.object.Timer;
 import net.gtaun.shoebill.object.Vehicle;
 import net.gtaun.util.event.EventManager;
 import net.gtaun.util.event.EventManager.HandlerPriority;
 
 class PlayerVehicleActuator extends PlayerLifecycleObject
 {
 	private final Timer timer;
 	
 	boolean isLockNOS;
 	boolean isLockVHP;
 	
 	
 	public PlayerVehicleActuator(Shoebill shoebill, EventManager eventManager, Player player)
 	{
 		super(shoebill, eventManager, player);
 		timer = shoebill.getSampObjectFactory().createTimer(10000);
 		addDestroyable(timer);
 	}
 
 	@Override
 	protected void onInitialize()
 	{
 		eventManager.registerHandler(TimerTickEvent.class, timer, timerEventHandler, HandlerPriority.NORMAL);
		eventManager.registerHandler(VehicleUpdateDamageEvent.class, vehicleEventHandler, HandlerPriority.NORMAL);
 		timer.start();
 	}
 
 	@Override
 	protected void onUninitialize()
 	{
 		
 	}
 	
 	private TimerEventHandler timerEventHandler = new TimerEventHandler()
 	{
 		protected void onTimerTick(TimerTickEvent event)
 		{
 			if (player.isInAnyVehicle())
 			{
 				Vehicle vehicle = player.getVehicle();
 				int vehicleModel = vehicle.getModelId();
 				if (isLockNOS && VehicleComponentModel.isVehicleSupported(vehicleModel, VehicleComponentModel.NITRO_10_TIMES))
 				{
 					vehicle.getComponent().add(VehicleComponentModel.NITRO_10_TIMES);
 				}
 			}
 		}
 	};
 	
 	private VehicleEventHandler vehicleEventHandler = new VehicleEventHandler()
 	{
 		protected void onVehicleUpdateDamage(VehicleUpdateDamageEvent event)
 		{
 			Vehicle vehicle = event.getVehicle();
 			if (isLockVHP && vehicle == player.getVehicle()) vehicle.repair();
 		}
 	};
 }
