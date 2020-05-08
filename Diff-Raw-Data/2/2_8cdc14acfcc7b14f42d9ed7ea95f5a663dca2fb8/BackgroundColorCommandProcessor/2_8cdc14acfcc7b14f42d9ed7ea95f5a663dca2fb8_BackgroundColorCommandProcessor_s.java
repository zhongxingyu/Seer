 package org.fitfest.core;
 
 import java.awt.Component;
 
 import org.fest.swing.core.ComponentFinder;
 import org.fest.swing.fixture.ComponentFixture;
 import org.fest.swing.fixture.FrameFixture;
 
 public class BackgroundColorCommandProcessor implements CommandProcessor
 {
 
     @Override
     public String getCommandString()
     {
         return "backgroundColor";
     }
 
     @Override
     public void handleRow( FrameFixture window, RowHandler rowHandler )
     {
         ComponentFinder finder = window.robot.finder();
         ComponentFixture<Component> componentFixture = 
            new ComponentFixture<Component>(window.robot, finder.findByName( rowHandler.getText( 1 ) ))
         {
         };
         
         try
         {
             componentFixture.background().requireEqualTo( rowHandler.getText( 2 ) );
             rowHandler.right( 2 );
         }
         catch ( final AssertionError e )
         {
             rowHandler.wrong( 2, componentFixture.background().target().toString() );
         }
 
     }
 
 }
