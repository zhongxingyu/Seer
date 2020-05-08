 package org.rev317.randoms;
 
import org.parabot.core.Context;
import org.parabot.core.Core;
 import org.parabot.environment.api.utils.Time;
 import org.parabot.environment.input.Keyboard;
 import org.parabot.environment.input.Mouse;
 import org.parabot.environment.randoms.Random;
 import org.rev317.api.methods.Game;
 import org.rev317.randoms.ui.controllers.LoginController;
 import org.rev317.randoms.ui.controllers.UIController;
 
 import java.awt.*;
import java.util.Properties;
 
 /**
  * Author: Sully
  */
 public class Login implements Random {
     @Override
     public boolean activate() {
         return !Game.isLoggedIn() && UIController.randoms.get("Auto login");
     }
 
     @Override
     public void execute() {
        Core.verbose("Starting on login random");
         //ToDo click cancel first
        Properties properties = Context.getInstance().getServerProviderInfo().getProperties();
        Point u = createPoint(properties.getProperty("usernamePoint"));
        Point p = createPoint(properties.getProperty("passwordPoint"));
        Point l = createPoint(properties.getProperty("loginButtonPoint"));
         Mouse.getInstance().click(u, true);
         Time.sleep(2000);
         Keyboard.getInstance().sendKeys(LoginController.getUsername());
         Time.sleep(2000);
         Mouse.getInstance().click(p, true);
         Time.sleep(2000);
         Keyboard.getInstance().sendKeys(LoginController.getPassword());
         Time.sleep(2000);
         Mouse.getInstance().click(l, true);
         Time.sleep(2000);
     }
 
     public static Point createPoint(String s) {
         return new Point(Integer.parseInt((s.split(","))[0]), Integer.parseInt((s.split(","))[1]));
     }
 }
