 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ua.epam.i18n;
 
 import java.util.ListResourceBundle;
 
 /**
  *
  * @author Ivanov Alexander
  */
 public class WelcomePage_ru_RU extends ListResourceBundle{
 
     static Object[][] contents = {
         {"welcome.page.welcome", "Добро пожаловать на стартовую страницу!"},
         {"welcome.page.loggin", "Вы авторизовались как "},
         {"welcome.page.create", "Создать заявку."},
         {"welcome.page.show", "Просмотреть заявки."},
         {"welcome.page.exit", "Для выхода нажмите "},
         {"welcome.page.button", "Выход"},
        {"welcome.page.flag", "images/flag_ru.gif"}};
     
     @Override
     protected Object[][] getContents() {
         return contents;
     }
 
 }
