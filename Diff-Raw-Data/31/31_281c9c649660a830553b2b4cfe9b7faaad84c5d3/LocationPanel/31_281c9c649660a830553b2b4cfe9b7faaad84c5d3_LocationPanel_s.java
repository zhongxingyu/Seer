 package pro.kornev.kcontrol.view.panels.location;
 
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 import pro.kornev.kcontrol.service.SettingService;
 import pro.kornev.kcontrol.service.SettingsListener;
 import pro.kornev.kcontrol.service.joystick.KJoystick;
 import pro.kornev.kcontrol.service.network.ProxyService;
 import pro.kornev.kcontrol.service.network.ProxyServiceListener;
 import pro.kornev.kcontrol.view.graphics.Car3dPanel;
 import pro.kornev.kcontrol.view.panels.CustomPanel;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vkornev
  * Date: 12/11/13
  * Time: 12:15 PM
  *
  */
 public class LocationPanel extends CustomPanel implements ActionListener, SettingsListener, ProxyServiceListener {
     private static final int WIDTH = 320;
     private static final int HEIGHT = 240;
     private static final String MAP_SERVER_URL = "http://static-maps.yandex.ru/1.x/?l=sat,skl&ll=%f,%f&size=" + WIDTH+ "," + HEIGHT + "&pt=%f,%f,flag&z=%d";
     private final Canvas map;
     private final JSlider zoom;
     private final Car3dPanel car3dPanel;
     private ProxyService proxyService;
 
     public LocationPanel() {
         super("Location");
         JPanel mapPanel = new JPanel();
         map = new Canvas();
         mapPanel.add(map);
         mapPanel.setSize(WIDTH, HEIGHT);
         map.setSize(WIDTH, HEIGHT);
         add(mapPanel, getGbl().setGrid(0, 0));
 
         zoom = new JSlider(JSlider.VERTICAL, 0, 17, 10);
         add(zoom, getGbl().setGrid(1, 0).rowSpan(2));
 
         car3dPanel = new Car3dPanel();
         car3dPanel.setSize(WIDTH / 4, HEIGHT / 4);
         add(car3dPanel, getGbl().setGrid(2, 0).fillB());
 
         JButton update = new JButton("Get last location");
         update.addActionListener(this);
         add(update, getGbl().setGrid(0,1));
         SettingService.i.addListener(this);
         Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 if (proxyService == null) {
                     return;
                 }
                 Data data = new Data();
                 data.cmd = Protocol.Cmd.sensAxis();
                 proxyService.send(data);
             }
        }, 100, 200, TimeUnit.MILLISECONDS);
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         if (proxyService == null) {
             return;
         }
         Data data = new Data();
         data.cmd = Protocol.Cmd.sensGps();
         proxyService.send(data);
     }
 
     @Override
     public void onPackageReceive(Data data) {
         if (data.cmd == Protocol.Cmd.sensGps()) {
             ByteBuffer bb = ByteBuffer.wrap(data.aData);
 
             double latitude = bb.getDouble();
             double longitude = bb.getDouble();
 
             try {
                 BufferedImage img = ImageIO.read(new URL(getMapServerUrl(latitude, longitude, zoom.getValue())));
                 map.getGraphics().drawImage(img, 0, 0, WIDTH, HEIGHT, null);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         } else if (data.cmd == Protocol.Cmd.sensAxis()) {
             ByteBuffer bb = ByteBuffer.wrap(data.aData);
             float x = bb.getFloat();
             float y = bb.getFloat();
             float z = bb.getFloat();
             car3dPanel.getCar3dView().setXAngle((int) (-y * 180 / Math.PI));
             car3dPanel.getCar3dView().setYAngle((int) (x * 180 / Math.PI));
             car3dPanel.getCar3dView().setZAngle((int) (z * 180 / Math.PI));
         }
     }
 
     @Override
     public void changeJoystick(KJoystick joystick) {
 
     }
 
     @Override
     public void changeProxy(ProxyService proxyService) {
         this.proxyService = proxyService;
         this.proxyService.addListener(this);
     }
 
     private String getMapServerUrl(double latitude, double longitude, int zoom) {
         return String.format(MAP_SERVER_URL, longitude, latitude, longitude, latitude, zoom);
     }
 }
