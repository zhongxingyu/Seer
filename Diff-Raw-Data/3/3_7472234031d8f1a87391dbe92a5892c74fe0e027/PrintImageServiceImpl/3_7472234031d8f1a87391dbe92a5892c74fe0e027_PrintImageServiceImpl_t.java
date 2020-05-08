 package com.rockontrol.yaogan.service;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.yaogan.gis.mgr.IDataStoreManager;
 import org.yaogan.gis.mgr.SimpleDataStoreManagerImpl;
 
 import com.rockontrol.yaogan.dao.IPlaceDao;
 import com.rockontrol.yaogan.dao.IShapefileDao;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 
 @Service
 public class PrintImageServiceImpl implements IPrintImageService {
 
    @Autowired
    private IPlaceDao placeDao;
 
    @Autowired
    private IShapefileDao shapefileDao;
 
    private IDataStoreManager dataStoreManager = new SimpleDataStoreManagerImpl();
    /**
     * width 插入shapefile的宽 height 插入shapefile的高 //TODO
     */
    private int width = 656;
    private int heigth = 875;
 
    @Override
    public File addShapeLayer(File template, File image) throws Exception {
 
       return this.mergeImg(template, image, 64, 190);// TODO
    }
 
    @Override
    public File addComment(File template, String comment) throws Exception {
       BufferedImage image = ImageIO.read(new FileInputStream(template));
       // 得到图形上下文
       Graphics2D g = image.createGraphics();
       // 设置画笔颜色
       g.setColor(Color.BLACK);
       // 设置字体
       g.setFont(new Font("宋体", Font.LAYOUT_LEFT_TO_RIGHT, 50));
       // 写入签名 TODO
       g.drawString(comment, 404, 85);
       g.dispose();
       FileOutputStream out = new FileOutputStream(template);
       ImageIO.write(image, "JPEG", out);
       out.close();
       return template;
    }
 
    @Override
    public File getMap(Long placeId, String time, String category, String tempPath)
          throws Exception {
       // TODO
       StringBuilder url = new StringBuilder(
             "http://localhost:8080/geoserver/yaogan/wms?service=WMS&version=1.1.0&request=GetMap");
       String wmsUrl = shapefileDao.getShapefile(placeId, time, category).getWmsUrl();
       url.append("&layers=").append(wmsUrl.substring(0, wmsUrl.indexOf('?')));
       url.append("&styles=");
       url.append("&bbox=").append(wmsUrl.substring(wmsUrl.indexOf('?') + 1));
       url.append("&width=").append(width);
       url.append("&height=").append(heigth);
       // url.append("&srs=WGS84");
       url.append("&srs=EPSG:4326");
       url.append("&format=image%2Fjpeg");
       File file = new File(tempPath);
      if (!file.getParentFile().exists()) {
         file.getParentFile().mkdirs();
      }
       HttpClient client = new DefaultHttpClient();
       HttpGet get = new HttpGet(url.toString());
       HttpResponse response = client.execute(get);
       HttpEntity entity = response.getEntity();
       if (entity != null) {
          InputStream in = entity.getContent();
          FileOutputStream out = new FileOutputStream(file);
          byte[] b = new byte[100];
          int temp = 0;
          while ((temp = in.read(b)) != -1) {
             out.write(b, 0, temp);
          }
          in.close();
          out.close();
          client.getConnectionManager().shutdown();
       }
 
       return file;
    }
 
    /**
     * img2 贴到 img1 的(x,y)位置上
     * 
     * @param img1
     * @param img2
     * @param x
     * @param y
     * @return
     * @throws Exception
     */
    private File mergeImg(File img1, File img2, int x, int y) throws Exception {
       BufferedImage image = ImageIO.read(new FileInputStream(img1));
       Graphics2D g = image.createGraphics();// 得到图形上下文
       BufferedImage image2 = ImageIO.read(new FileInputStream(img2));
       g.drawImage(image2, x, y, image2.getWidth(), image2.getHeight(), null);
       g.dispose();
       FileOutputStream out = new FileOutputStream(img1);
       ImageIO.write(image, "JPEG", out);
       out.close();
       return img1;
    }
 
    private double[] getbBox(Long placeId, String time, String category) throws Exception {
       double[] bBox = new double[4];
       Shapefile shapefile = shapefileDao.getShapefile(placeId, time, category);
       FileDataStore dataStore = dataStoreManager.getDataStore(shapefile.getFilePath());
       String typeName = dataStore.getTypeNames()[0];
       FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) dataStore
             .getFeatureSource(typeName);
       FeatureCollection<SimpleFeatureType, SimpleFeature> result = featureSource
             .getFeatures();
       FeatureIterator<SimpleFeature> itertor = result.features();
       while (itertor.hasNext()) {
          SimpleFeature feature = itertor.next();
          Geometry multipolygon = (Geometry) feature.getDefaultGeometry();
          Coordinate[] cords = multipolygon.getEnvelope().getCoordinates();
          double maxX = 0, maxY = 0, minX = 0, minY = 0;
          boolean flag = true;
          for (Coordinate cor : cords) {
             if (flag) {
                maxX = cor.x;
                minX = cor.x;
                minY = cor.y;
                maxY = cor.y;
                flag = false;
                continue;
             }
             if (maxX < cor.x) {
                maxX = cor.x;
             }
             if (maxY < cor.y) {
                maxY = cor.y;
             }
             if (minX > cor.x) {
                minX = cor.x;
             }
             if (minY < cor.y) {
                minY = cor.y;
             }
          }
          bBox[0] = minX;
          bBox[1] = minY;
          bBox[2] = maxX;
          bBox[3] = maxY;
       }
       return bBox;
    }
 
    @Override
    public File copyTemplate(String templatePath, String imagePath) throws Exception {
       int read = 0;
       File template = new File(templatePath);
       File image = new File(imagePath);
       image.createNewFile();
       if (template.exists()) {
          InputStream in = new FileInputStream(template);
          FileOutputStream out = new FileOutputStream(image);
          byte[] buffer = new byte[1024];
          while ((read = in.read(buffer)) != -1) {
             out.write(buffer, 0, read);
          }
          in.close();
       }
       return image;
    }
 
 }
