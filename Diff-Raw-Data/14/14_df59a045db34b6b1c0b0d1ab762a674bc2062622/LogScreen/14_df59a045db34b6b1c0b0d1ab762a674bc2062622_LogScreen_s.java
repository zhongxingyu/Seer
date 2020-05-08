 package com.ikkerens.serverui.screens;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import com.ikkerens.serverui.Screen;
 import com.ikkerens.serverui.ServerUIPlugin;
 import com.ikkerens.serverui.components.LogTextArea;
 import com.ikkerens.serverui.components.ServerCommandLine;
 
 public class LogScreen extends Screen {
 
     @Override
     protected String getTabTitle() {
         return "Server Log";
     }
 
     @Override
     protected JPanel buildContents( final ServerUIPlugin plugin ) {
         final JPanel panel = new JPanel();
         panel.setLayout( new BorderLayout() );
 
        panel.add( new LogTextArea( plugin.getLogger() ), BorderLayout.CENTER );
         panel.add( new ServerCommandLine( plugin.getServer() ), BorderLayout.SOUTH );
 
         return panel;
     }
 }
