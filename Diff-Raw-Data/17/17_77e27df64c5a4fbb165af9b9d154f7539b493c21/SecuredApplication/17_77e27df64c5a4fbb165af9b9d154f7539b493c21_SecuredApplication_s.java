 package com.smash.revolance.ui.explorer.application.secured;
 
 /*
         This file is part of Revolance UI Suite.
 
         Revolance UI Suite is free software: you can redistribute it and/or modify
         it under the terms of the GNU General Public License as published by
         the Free Software Foundation, either version 3 of the License, or
         (at your option) any later version.
 
         Revolance UI Suite is distributed in the hope that it will be useful,
         but WITHOUT ANY WARRANTY; without even the implied warranty of
         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         GNU General Public License for more details.
 
         You should have received a copy of the GNU General Public License
         along with Revolance UI Suite.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import com.smash.revolance.ui.explorer.application.Application;
 import com.smash.revolance.ui.explorer.application.ApplicationManager;
 import com.smash.revolance.ui.explorer.page.IPage;
 import com.smash.revolance.ui.explorer.user.User;
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.By;
 
 import java.io.File;
 
 /**
  * User: wsmash
  * Date: 26/01/13
  * Time: 17:25
  */
 public class SecuredApplication extends Application
 {
     public SecuredApplication(ApplicationManager manager, File usersCfg) throws Exception
     {
         super(manager, usersCfg);
     }
 
     @Override
     public void enterLogin(User user, IPage page) throws Exception
     {
         if( page.getUrl().endsWith("login.html") )
         {
             user.getBrowser().findElement(By.id("username")).sendKeys(user.getLogin());
             user.getBrowser().findElement(By.id("password")).sendKeys(user.getPasswd());
         }
     }
 
     @Override
     public boolean isPageBroken(IPage page)
     {
        return false;
     }
 
     @Override
     public boolean isUnauthorized(IPage page)
     {
         return false;
     }
 
     @Override
     public boolean awaitPageLoaded(IPage page)
     {
         return true;
     }
 
     @Override
     public boolean isSecured()
     {
         return true;
     }
 
     @Override
     public void handleAlert(Alert alert)
     {
         alert.accept();
     }
 
     @Override
     public void enterNewPassword(User user, IPage page) throws Exception
     {
         if(page.getUrl().endsWith("change-passwd.html"))
         {
             user.getBrowser().findElement(By.id("newPasswd")).sendKeys(user.getNewPasswd());
             user.getBrowser().findElement(By.id("confNewPasswd")).sendKeys(user.getNewPasswd());
         }
     }
 
 }
