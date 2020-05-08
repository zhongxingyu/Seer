 package com.cjra.battleship_project;
 
 import android.widget.TextView;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.RobolectricTestRunner;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(RobolectricTestRunner.class)
 public class FleetDeploymentActivityTests {
 
     @Test
    public void DisplaysCorrectNumberOfShipsTextForDeployment(){
         FleetDeploymentActivity activity = new FleetDeploymentActivity();
         activity.onCreate(null);
 
         activity.setNumberOfAvailableShips(12);
 
         TextView text = (TextView)activity.findViewById(R.id.ship_count);
 
         assertEquals("You have 12 ships to place.\n" +
                     "Touch grid to place ships.", text.getText());
     }
 }
