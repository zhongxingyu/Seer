 package com.psychobit.hardcore;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.psychobit.hardcore.Hardcore;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(PlayerLoginEvent.class)
 public class HardcoreTest
 {
     @Test
     public void TestCampfireOnNewPlayerLogin()
     {
         Player newPlayer = mock(Player.class);
         when(newPlayer.getName()).thenReturn("Test");
         
         PlayerLoginEvent loginEvent = PowerMockito.mock(PlayerLoginEvent.class);
         when(loginEvent.getPlayer()).thenReturn(newPlayer);
         
         Hardcore hardcoreListener = new Hardcore();
         
         hardcoreListener.onPlayerLogin(loginEvent);
         
         // TODO: test database conditions
     }
     
     @Test
     public void TestCampfireOnExistingPlayerLogin()
     {
     }
 }
