 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.TreeMap;
 
 /**
  * 12.01.13 21:05
  */
 public class Utils {
     static Calendar getNowDate(){
         Calendar c = Calendar.getInstance();
         //c.add(Calendar.HOUR_OF_DAY, 1);
         return c;
     }
     
     static String whitespace = " \t\n\r\0";
 
     static String trim(String s){
         char c[] = s.toCharArray();
         int i=0, j=s.length()-1;
         while(i<=j && whitespace.indexOf(c[i])>=0) ++i;
         while(i<=j && whitespace.indexOf(c[j])>=0) --j;
         return new String(c, i, j-i+1);
     }
     
     static String trimTags(String s){
         while(s.contains("<")) s = s.substring(0,s.indexOf("<"))+s.substring(s.indexOf(">")+1);
         return s;
     }
     
    static String replaceHTMLSymbols(String s){
        for(;;){
            int i = s.indexOf("&#");
            if(i<0) break;
            int j = s.indexOf(";",i+2);
            int code = Integer.parseInt(s.substring(i+2, j));
            s = s.replace(s.substring(i, j+1), ((char)code)+"");
        }
        return s;
    }
    
     static String URLToString(String urlName, String code){
         try {
             URL url = new URL(urlName);
             URLConnection con = url.openConnection();
             DataInputStream dis = new DataInputStream(con.getInputStream());
             byte bytes[] = new byte[1<<17];
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             for(;;){
                 int cnt = dis.read(bytes);
                 if(cnt==-1) break;
                 baos.write(bytes, 0, cnt);
             }
             return new String(baos.toByteArray(), code);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
     
     static BufferedImage loadImage(String url){
         try {
             return ImageIO.read(new URL(url));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 
     static void launchBrowser(String uriStr){
         if(uriStr==null || uriStr.length()==0) return;
         Desktop desktop;
         if (Desktop.isDesktopSupported()){
             desktop = Desktop.getDesktop();
             if (desktop.isSupported(Desktop.Action.BROWSE)){
                 URI uri;
                 try{
                     uri = new URI(uriStr.indexOf("http://")==0 ? uriStr : "http://" + uriStr);
                     desktop.browse(uri);
                 }catch (IOException ioe){
                     ioe.printStackTrace();
                 }catch (URISyntaxException use){
                     use.printStackTrace();
                 }
             }
         }
     }
     
     static long difference(Calendar c1, Calendar c2){
         return c1.getTimeInMillis() - c2.getTimeInMillis();
     }
     
     static Calendar sum(Calendar c1, Calendar c2){
         Calendar res = getNowDate();
         res.set(
                 c1.get(Calendar.YEAR)+c2.get(Calendar.YEAR)-1970,
                 c1.get(Calendar.MONTH)+c2.get(Calendar.MONTH),
                 c1.get(Calendar.DAY_OF_MONTH)+c2.get(Calendar.DAY_OF_MONTH)-1,
                 c1.get(Calendar.HOUR_OF_DAY)+c2.get(Calendar.HOUR_OF_DAY),
                 c1.get(Calendar.MINUTE)+c2.get(Calendar.MINUTE),
                 c1.get(Calendar.SECOND)+c2.get(Calendar.SECOND)
                 
                 );
         return res;
     }
 
     static TreeMap<String, String> month = new TreeMap<String,String>();
     static{
         month.put("январь", "01");
         month.put("января", "01");
         month.put("февраль", "02");
         month.put("февраля", "02");
         month.put("март", "03");
         month.put("марта", "03");
         month.put("апрель", "04");
         month.put("апреля", "04");
         month.put("май", "05");
         month.put("мая", "05");
         month.put("июнь", "06");
         month.put("июня", "06");
         month.put("июль", "07");
         month.put("июля", "07");
         month.put("август", "08");
         month.put("августа", "08");
         month.put("сентябрь", "09");
         month.put("сентября", "09");
         month.put("октябрь", "10");
         month.put("октября", "10");
         month.put("ноябрь", "11");
         month.put("ноября", "11");
         month.put("декабрь", "12");
         month.put("декабря", "12");
     }
 }
