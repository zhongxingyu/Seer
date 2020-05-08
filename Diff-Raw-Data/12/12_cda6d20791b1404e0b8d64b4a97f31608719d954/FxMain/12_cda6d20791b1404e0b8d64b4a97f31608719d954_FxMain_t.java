 package com.tngtech.internal.leapcontrol.entry;
 
 import com.tngtech.internal.leapcontrol.injection.Context;
 import javafx.application.Application;
 import javafx.stage.Stage;
 
 public class FxMain extends Application
 {
   public static void main(String[] args)
   {
     launch(args);
   }
 
   @Override
   public void start(Stage primaryStage) throws Exception
   {
     Main main = Context.getBean(Main.class);
     main.start(primaryStage);
   }
}
