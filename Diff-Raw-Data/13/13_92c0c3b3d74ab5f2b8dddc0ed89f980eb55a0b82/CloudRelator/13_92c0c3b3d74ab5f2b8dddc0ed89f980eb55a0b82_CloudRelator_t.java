 package com.jpii.navalbattle.renderer;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.*;
 
 import com.jpii.dagen.*;
 import com.jpii.navalbattle.data.Constants;
 
 public class CloudRelator implements Runnable {
     Engine eng;
     ArrayList < Cloud > clouds;
     BufferedImage cloudb;
     Random r;
     float xm = 0;
     float zm = 0;
     boolean signxm = false;
     boolean signzm = false;
     public CloudRelator() {
         r = new Random(Constants.MAIN_SEED);
         xm = (float)((r.nextDouble() * 2.5f) - 1.25f);
         zm = (float)((r.nextDouble() * 2.5f) - 1.25f);
         if (xm < 0.6f && xm > -0.6f && zm < 0.6f && zm > -0.6f) {
             xm += (float)((r.nextDouble() * 2.5f) + 1.25f);
             zm += (float)((r.nextDouble() * 2.5f) + 1.25f);
             if (r.nextBoolean()) xm *= -1;
             if (r.nextBoolean()) zm *= -1;
         }
        Console.getInstance().printInfo("Cloud kernel used: (" + xm + "," + zm + ").");
         if (xm < 0) signxm = true;
         if (zm < 0) signzm = true;
         eng = new Engine(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
         eng.generate(r.nextInt(), 1);
         clouds = new ArrayList < Cloud > ();
         /*for (int x = 0; x < Constants.WINDOW_WIDTH; x++) {
             for (int z = 0; z < Constants.WINDOW_HEIGHT; z++) {
                 if (eng.getPoint(x, z) > 0.8 && r.nextInt(5) == 1 && clouds.size() < RenderConstants.CLOUD_MAX) {
                     clouds.add(new Cloud((float)(r.nextInt(RenderConstants.CLOUD_MAX_SIZE - RenderConstants.CLOUD_MIN_SIZE) + RenderConstants.CLOUD_MIN_SIZE), r.nextInt(Constants.WINDOW_WIDTH),
                     r.nextInt(Constants.WINDOW_HEIGHT), (float)((1 - eng.getPoint(x, z)) / 0.3)));
                 }
             }
         }*/
         for (int c = 0; c < r.nextInt(8) + 8; c++) {
         	clouds.add(new Cloud(100,r.nextInt(Constants.WINDOW_WIDTH),
                     r.nextInt(Constants.WINDOW_HEIGHT)));
         }
     }
     public BufferedImage getBuffer() {
         BufferedImage img = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) img.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         for (int c = 0; c < clouds.size(); c++) {
             Cloud cloudw = clouds.get(c);
             int dh = (int)(cloudw.diameter / 2);
             g.drawImage(cloudw.buffer,cloudw.x - dh, cloudw.z - dh,null);
             /*int alpha = (int)(cloudw.diameter * 255 / (1 + RenderConstants.CLOUD_MIN_SIZE));
             if (alpha < 10) alpha = 10;
             if (alpha > 30) alpha = 30;
             int rgb = (int)(cloudw.cloudLR * 127) + 100;
             if (rgb > 255) rgb = 255;
             if (rgb < 20) rgb = 20;
             g.setColor(new Color(rgb, rgb, rgb, alpha));
             int d = (int) cloudw.diameter;
             int dh = d / 2;
             g.fillOval(cloudw.x - dh, cloudw.z - dh, d, d);*/
         }
         return img;
     }
     public void update() {
         for (int c = 0; c < clouds.size(); c++) {
             Cloud poll = clouds.get(c);
             if (xm < 1.0f && xm > -1.0f)
             	poll.x += (xm*4);
             if (zm < 1.0f && zm > -1.0f)
             	poll.z += (zm*4);
             poll.x += (xm);
             poll.z += zm;
             if (poll.x <= 0 && xm < 0) poll.x = Constants.WINDOW_WIDTH;
             if (poll.x >= Constants.WINDOW_WIDTH && xm > 0) poll.x = 0;
             if (poll.z <= 0 && zm < 0) poll.z = Constants.WINDOW_HEIGHT;
             if (poll.z >= Constants.WINDOW_HEIGHT && zm > 0) poll.z = 0;
         }
     }
     public void updateMouse(int mouseX, int mouseY) {
         for (int c = 0; c < clouds.size(); c++) {
             Cloud poll = clouds.get(c);
             double dist = Math.sqrt(Math.pow(mouseX - poll.x, 2) + Math.pow(mouseY - poll.z, 2));
             //double dist = 1234242142;
             if (dist <= (poll.diameter * 1.25)) {
                 double angv = Math.atan2(mouseY - poll.z, mouseX - poll.x);
                 int mmx = (int)(Math.cos(angv) * (dist / 12));
                 int mmy = (int)(Math.sin(angv) * (dist / 12));
 
                 poll.x -= mmx;
                 poll.z -= mmy;
             }
         }
     }
     public ArrayList < Cloud > getClouds() {
         return clouds;
     }
     public BufferedImage buffer;
     public void run() {
         update();
         buffer = getBuffer();
     }
 }
