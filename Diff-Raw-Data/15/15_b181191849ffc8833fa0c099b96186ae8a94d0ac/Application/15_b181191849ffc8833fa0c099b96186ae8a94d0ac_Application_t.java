 package controllers;
 
 import play.mvc.*;
 import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         List<Book> books = Book.books();
         render(books);
     }
 
     public static void book(Long id) {
         Book book = Book.findById(id);
         List<Page> pages = Page.listByBook(id);
         render(book, pages);
     }
 
     public static void page(Long id) {
         Page page = Page.findById(id);
         List<Sentence> sentences = Sentence.listByPage(id);
         render(page, sentences);
     }
 
    public static void audio(Long id) throws IOException {
         Sentence sentence = Sentence.findById(id);
         if (sentence.audioHash != 0) {
             Audio audio = Audio.findByAudioHash(sentence.audioHash);
             InputStream is = new ByteArrayInputStream(audio.audio.getBytes());
             renderBinary(is, "audio", "audio/mpeg", true);
         } else {
             Page page = Page.findById(sentence.page.id);
             Book book = Book.findById(page.book.id);
             Language language = Language.findById(book.language.id);
            URL url = new URL("http://translate.google.com/translate_tts?tl="+language.iso+"&q="+URLEncoder.encode(sentence.text, "UTF-8"));
            URLConnection conn =  url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)");
            renderBinary(new BufferedInputStream(conn.getInputStream()), "audio", "audio/mpeg", true);
         }
     }
 }
