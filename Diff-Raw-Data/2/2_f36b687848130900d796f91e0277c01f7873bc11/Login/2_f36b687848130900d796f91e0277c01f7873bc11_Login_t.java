 package org.rev317.randoms;
 
 import org.parabot.core.ui.components.LogArea;
 import org.parabot.environment.api.utils.Time;
 import org.parabot.environment.input.Keyboard;
 import org.parabot.environment.input.Mouse;
 import org.parabot.environment.randoms.Random;
 import org.rev317.api.methods.Game;
 import org.rev317.randoms.ui.controllers.LoginController;
 import org.rev317.randoms.ui.controllers.UIController;
 
 
 import java.awt.*;
 
 /**
  * Author: Sully
  */
 @SuppressWarnings("deprecation")
 public class Login implements Random {
     @Override
     public boolean activate() {
        return !Game.isLoggedIn() && UIController.randoms.get("Auto login");
     }
 
     @Override
     public void execute() {
         LogArea.log("Starting on login random");
         //ToDo click cancel first
         Point u = createPoint(Reader.readProvider("usernamePoint"));
         Point p = createPoint(Reader.readProvider("passwordPoint"));
         Point l = createPoint(Reader.readProvider("loginButtonPoint"));
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
