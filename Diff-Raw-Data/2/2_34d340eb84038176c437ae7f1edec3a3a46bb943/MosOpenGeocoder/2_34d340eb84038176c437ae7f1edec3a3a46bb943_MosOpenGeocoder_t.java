 package ru.yandex.hackaton.server.geocoder;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Singleton;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import ru.yandex.hackaton.server.geocoder.data.DistrictInfo;
 import ru.yandex.hackaton.server.geocoder.geo.Line;
 import ru.yandex.hackaton.server.geocoder.geo.Point;
 
 /**
  * @author Sergey Polovko
  */
 @Singleton
 public class MosOpenGeocoder {
 
     private final DataHost<Line> dataHost = new DataHost<Line>("mosopen.ru") {
         @Override
         protected Line parseResponse(InputStream content, Charset charset) throws IOException {
             return parseDistrictInfo(content, charset);
         }
 
         @Override
         protected Line emptyResponse(Charset charset) {
             return null;
         }
     };
 
     public DistrictInfo geocode(String districtName) {
         System.out.println(districtName);
         String translitName = TransLiterator.translitRustoEng(districtName);
         Line borders = dataHost.get(
                 "/public/ymapsml.php", "p", String.format("region/%s/map_xml", translitName));
         return new DistrictInfo(districtName, borders);
     }
 
     private Line parseDistrictInfo(InputStream xml, Charset charset) throws IOException {
         Document document = Jsoup.parse(xml, charset.name(), "");
         List<Point> points = new ArrayList<>();
         for (Element pos : document.getElementsByTag("gml:pos")) {
             points.add(Point.parseGml(pos.text()));
         }
         Line borders = new Line(points);
         return borders;
     }
 }
