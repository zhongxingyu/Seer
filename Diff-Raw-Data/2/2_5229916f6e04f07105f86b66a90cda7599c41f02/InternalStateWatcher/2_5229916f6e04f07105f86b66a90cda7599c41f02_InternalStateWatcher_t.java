 package com.tngtech.internal.droneapi;
 
 import com.google.common.collect.Lists;
 import com.tngtech.internal.droneapi.commands.ATCommand;
 import com.tngtech.internal.droneapi.commands.simple.FlatTrimCommand;
 import com.tngtech.internal.droneapi.commands.simple.FlightModeCommand;
 import com.tngtech.internal.droneapi.commands.simple.FlightMoveCommand;
 import com.tngtech.internal.droneapi.data.InternalState;
 import com.tngtech.internal.droneapi.data.NavData;
 import com.tngtech.internal.droneapi.data.enums.FlightMode;
 import com.tngtech.internal.droneapi.listeners.NavDataListener;
 
 import javax.inject.Inject;
 import java.util.Collection;
 
 public class InternalStateWatcher implements NavDataListener
 {
   private static final float MOVE_THRESHOLD = 0.02f;
 
   private InternalState internalState;
 
   private NavData currentNavData;
 
   @Inject
   public InternalStateWatcher(NavigationDataRetriever navigationDataRetriever)
   {
     navigationDataRetriever.addNavDataListener(this);
     internalState = new InternalState();
   }
 
   public Collection<ATCommand> getCommandsToUpholdInternalState()
   {
     if (currentNavData == null)
     {
       return Lists.newArrayList();
     }
 
     Collection<ATCommand> commands = Lists.newArrayList();
     addNecessaryCommands(commands);
     resetState();
 
     return commands;
   }
 
   private void addNecessaryCommands(Collection<ATCommand> commands)
   {
     if (internalState.isTakeOffRequested() && !currentNavData.getState().isFlying())
     {
       commands.add(new FlightModeCommand(FlightMode.TAKE_OFF));
     }
     if (internalState.isLandRequested() && currentNavData.getState().isFlying())
     {
       commands.add(new FlightModeCommand(FlightMode.LAND));
     }
     if (internalState.isEmergencyRequested() && currentNavData.getState().isEmergency())
     {
      commands.add(new FlightModeCommand(FlightMode.EMERGENCY));
     }
     if (internalState.isFlatTrimRequested())
     {
       commands.add(new FlatTrimCommand());
     }
 
     if (internalState.isMoveRequested())
     {
       commands.add(new FlightMoveCommand(internalState.getRequestedRoll(), internalState.getRequestedPitch(),
               internalState.getRequestedYaw(), internalState.getRequestedGaz()));
     }
   }
 
   private void resetState()
   {
     // Flat trim and move are one-off command, so it is reset here
     internalState.setFlatTrimRequested(false);
     internalState.setMoveRequested(false);
 
     // Emergency state may reset itself
     // If it is set in nav data, there is no need for further checks
     if (internalState.isEmergencyRequested() && currentNavData.getState().isEmergency())
     {
       internalState.setEmergencyRequested(false);
     }
 
     // Flying and landing states can be reset whenever the requested state occurs
     if (internalState.isTakeOffRequested() && currentNavData.getState().isFlying())
     {
       internalState.setTakeOffRequested(false);
     }
     if (internalState.isLandRequested() && !currentNavData.getState().isFlying())
     {
       internalState.setLandRequested(false);
     }
   }
 
   public void requestTakeOff()
   {
     internalState.setTakeOffRequested(true);
     internalState.setLandRequested(false);
   }
 
   public void requestLand()
   {
     internalState.setLandRequested(true);
     internalState.setTakeOffRequested(false);
   }
 
   public void requestEmergency()
   {
     internalState.setEmergencyRequested(true);
   }
 
   public void requestFlatTrim()
   {
     internalState.setFlatTrimRequested(true);
   }
 
   @Override
   public void onNavData(NavData navData)
   {
     currentNavData = navData;
   }
 
   public void requestMove(float roll, float pitch, float yaw, float gaz)
   {
     if (Math.abs(roll - internalState.getRequestedRoll()) > MOVE_THRESHOLD
             || Math.abs(pitch - internalState.getRequestedPitch()) > MOVE_THRESHOLD
             || Math.abs(yaw - internalState.getRequestedYaw()) > MOVE_THRESHOLD
             || Math.abs(gaz - internalState.getRequestedGaz()) > MOVE_THRESHOLD)
     {
       internalState.setMoveRequested(true);
       internalState.setRequestedRoll(roll);
       internalState.setRequestedPitch(pitch);
       internalState.setRequestedYaw(yaw);
       internalState.setRequestedGaz(gaz);
     }
   }
 }
