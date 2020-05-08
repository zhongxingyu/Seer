 package Screens;
 
 import helpers.Delegate;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hp
  * Date: 23/04/13
  * Time: 3:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ScreenManager {
     private Screen CurrentScreen;
     private Screen[] screens;
     Delegate delegate;
 
     public ScreenManager(Delegate d) {
         delegate = d;
         initScreens();
     }
 
     private void initScreens() {
 
         Delegate d = new Delegate() {
             @Override
             public void change(int val) {
                 switchScreens(val);
             }
         };
 
 
         screens = new Screen[]{
                 new MainMenuScreen(d),
                 new SettingsScreen(d),
                 new GameplayScreen(d),
                 new GameplayScreen(d)};
 
         CurrentScreen = screens[0];
     }
 
     private void switchScreens(int val) {
         CurrentScreen = screens[val];
        CurrentScreen.Initialize();
         delegate.change(1);
     }
 
     public void Initialize(){
         CurrentScreen.Initialize();
     }
 
     public void Render(){
         CurrentScreen.Render();
     }
 
     public void Update(){
         CurrentScreen.Update();
     }
 
 
 }
