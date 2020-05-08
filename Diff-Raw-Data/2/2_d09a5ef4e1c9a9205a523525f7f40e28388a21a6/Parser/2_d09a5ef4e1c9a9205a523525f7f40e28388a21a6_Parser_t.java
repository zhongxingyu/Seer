 import java.util.*;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.MalformedURLException;
 import java.io.*;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.*;
 import org.jsoup.select.Elements;
 
 /**
  * User: sergey
  * Date: 13.03.13
  * Time: 0:51
  */
 public class Parser {
     public static final String BASE_URL = "http://yandex.ua/yandsearch?lr=146&text=";   // Базовый URL
     public static final String SELECT = "div.b-body-items ol li";                       // Селектор для подокументного перебора
 
     /**
      * Метод для получения кода страницы
      * @param url   Адрес страницы
      * @return      HTML-код страницы
      */
     public String getHtmlPage(String url) {
         String line, html = "";
         try {
             URL page = new URL(url);
             BufferedReader reader = new BufferedReader(new InputStreamReader(page.openStream()));
             while ((line = reader.readLine()) != null) {
                 html += line;
             }
             reader.close();
 
             String charset = this.getCharset(html);
             if (!charset.regionMatches(true, 0, "utf-8", 0, 5)) {
                 html = this.getHtmlPage(page, charset);
             }
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return html;
     }
 
     /**
      * Метод для получения кода страницы
      * @param page      Ссылка на объект URL
      * @param charset   Кодировка
      * @return          HTML-код страницы
      */
     public String getHtmlPage(URL page, String charset) {
         String line, html = "";
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(page.openStream(), charset));
             while ((line = reader.readLine()) != null) {
                 html += line;
             }
             reader.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return html;
     }
 
     /**
      * Метод для разбора документа на составляющие
      * @param document      Документ для разбора
      * @return              Массив элементов
      */
     public String[] parseDocument(Element document) {
         String[] docElements = new String[4];
         docElements[0] = document.select("h2 a.b-serp-item__title-link").attr("href");
         if (!docElements[0].regionMatches(0, "http:", 0, 5)) {
             docElements[0] = "http:" + docElements[0];
         }
         docElements[1] = document.select("h2 a.b-serp-item__title-link").text();
         docElements[2] = document.select("div.b-serp-item__text").text();
         if (docElements[2] == null) {
             docElements[2] = document.select("td").text();
         }
         docElements[3] = document.select(".b-serp-url").text();
         return docElements;
     }
 
     /**
      * Метод вывода информации о документе
      * @param docElements   Массив элементов для вывода
      */
     public void printDocument(String[] docElements) {
         System.out.println("URL: " + docElements[0]);
         System.out.println("Title: " + docElements[1]);
         System.out.println("Snippet: " + docElements[2]);
         System.out.println("Green line: " + docElements[3] + "\n");
     }
 
     /**
      * Метод записи данных в файл
      * @param data          Данные для записи в файл
      * @param fileName      Имя файла
      */
     public void createFile(String data, String fileName, String charset) {
         try {
             PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("Docs" + "/" + fileName), charset));
             out.println(data);
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Метод для получения кодировки страницы
      * @param html  HTML-код страницы
      * @return      Кодировка страницы
      */
     public String getCharset(String html) {
         String charset = "UTF-8";
         Document page = Jsoup.parse(html);
         String contentType = page.select("meta[http-equiv=Content-Type]").attr("content");
 
         int encodingStart = contentType.indexOf("charset=");
         if (encodingStart != -1) {
             charset = contentType.substring(encodingStart + 8);
         }
         return charset;
     }
 
     /**
      * Точка входа в программу
      * @param args      Строковые аргументы
      */
     public static void main(String[] args) {
         try {
             System.out.println("Введите поисковый запрос:");
             Scanner input = new Scanner(System.in);
            String query = URLEncoder.encode(input.nextLine(), "UTF-8");
             System.out.println();
 
             Parser parser = new Parser();
             String html = parser.getHtmlPage(Parser.BASE_URL + query);
             Document page = Jsoup.parse(html);
             Elements documents = page.select(Parser.SELECT);
 
             File directory = new File("Docs");
             if (!directory.exists()) {
                 directory.mkdir();
             }
 
             int counter = 0;
             String data;
             for (Element document : documents) {
                 counter++;
                 String[] docElements = parser.parseDocument(document);
                 parser.printDocument(docElements);
                 data = parser.getHtmlPage(docElements[0]);
                 String charset = parser.getCharset(data);
                 parser.createFile(data, Integer.toString(counter) + ".html", charset);
             }
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
     }
 }
