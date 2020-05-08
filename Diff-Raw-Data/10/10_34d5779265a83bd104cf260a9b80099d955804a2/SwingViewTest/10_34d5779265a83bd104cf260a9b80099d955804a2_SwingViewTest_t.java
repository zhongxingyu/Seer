 package com.cmendenhall;
 
 import org.junit.*;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import java.io.*;
 
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 @RunWith(JUnit4.class)
 public class SwingViewTest extends TicTacToeTest{
 
     SwingView swingView;
 
     @Before
     public void setUp() {
        //swingView = new SwingView();
        //swingView.setVisible(true);
     }
 
     @Test
     public void swingViewIsVisibleOnCreation() {
        //assertTrue(swingView.isShowing());
     }
 
     @After
     public void cleanUp() {
        //swingView.dispose();
     }
 }
