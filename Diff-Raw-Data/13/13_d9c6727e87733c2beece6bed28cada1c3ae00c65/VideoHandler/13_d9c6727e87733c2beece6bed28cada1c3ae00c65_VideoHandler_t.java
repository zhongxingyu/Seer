 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dstvo.lobber.util;
 
 import java.awt.Rectangle;
 import javax.media.Control;
 import javax.media.Player;
 import javax.tv.media.AWTVideoSize;
 import javax.tv.media.AWTVideoSizeControl;
 import javax.tv.service.selection.ServiceContentHandler;
 import javax.tv.service.selection.ServiceContext;
 import javax.tv.service.selection.ServiceContextFactory;
 
 /**
  *
  * @author Devadas.Vijayan
  */
 public class VideoHandler
 {
     // Convert the graphics bounds to video bounds - Tackle Irdeto issue
     private static boolean RESOLVE_RESOLUTION = true;
 
     public static boolean resizeVideo(Rectangle newVideoBounds)
     {
         try
         {
             Player player = null;
             ServiceContext serviceContext = ServiceContextFactory.getInstance().getServiceContexts()[0];
             if (serviceContext != null && serviceContext.getService() != null && serviceContext.getServiceContentHandlers() != null)
             {
                 ServiceContentHandler[] handlers = serviceContext.getServiceContentHandlers();
                 for (int i = 0; i < handlers.length; i++)
                 {
                     if (handlers[i] instanceof Player)
                     {
                         player = (Player) handlers[i];
                         break;
                     }
                 }
             }
             if (null == player)
             {
                 System.out.println("Player is null...");
                 return false;
             }
             Control playerControl = player.getControl("javax.tv.media.AWTVideoSizeControl");
             if (playerControl instanceof AWTVideoSizeControl)
             {
                 AWTVideoSizeControl videoSizeControl = (AWTVideoSizeControl) playerControl;
                 Rectangle videoSizeSource = videoSizeControl.getSize().getSource();
                 Rectangle destination = getResolutionResolvedRect(newVideoBounds);
                 AWTVideoSize newVideoSize = new AWTVideoSize(videoSizeSource, destination);
                 return videoSizeControl.setSize(newVideoSize);
             }
         }
         catch (Exception e)
         {
             System.out.println("Exception caught in video resize code. This is "
                     + "expected in PC, but in STB, investigate the rootcause");
         }
         return false;
     }
 
     private static Rectangle getResolutionResolvedRect(Rectangle newVideoBounds)
     {
        float x_factor = RESOLVE_RESOLUTION ? ((float)720 / 1280) : 1;
        float y_factor = RESOLVE_RESOLUTION ? ((float)576 / 720) : 1;
        System.out.println("x_factor = " + x_factor);
        int x = (int) (newVideoBounds.x * x_factor);
        int y = (int) (newVideoBounds.y * y_factor);
        int width = (int) (newVideoBounds.width * x_factor);
        int height = (int) (newVideoBounds.height * y_factor);
         return new Rectangle(x, y, width, height);
     }
 }
