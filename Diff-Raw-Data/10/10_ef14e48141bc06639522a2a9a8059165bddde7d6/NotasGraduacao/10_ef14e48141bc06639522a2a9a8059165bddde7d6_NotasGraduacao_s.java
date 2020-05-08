 package edu.unochapeco.saa;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.jsoup.nodes.Element;
 
 /**
  *
  * @author Daniel Girotto
  */
 public class NotasGraduacao {
 
     private String session = null;
 
     public NotasGraduacao(String session) {
         this.session = session;
     }
 
     public String getNotas() throws IOException {
        String url = "https://www.unochapeco.edu.br/saa/notas.php";
 
         Document document = Jsoup.connect(url)
                 .cookie("PHPSESSID", session)
                 .timeout(8000)
                 .get();
 
         Elements disciplinas = document.select("form table:eq(0) tr td:eq(1) a");
 
         JSONArray disciplinaArray = new JSONArray();
         for (Element disciplina : disciplinas) {
             JSONObject disciplinaObject = new JSONObject();
             disciplinaObject.put("nome", disciplina.text());
             disciplinaObject.put("dados", parse(disciplina.attr("href")));
 
             disciplinaArray.put(disciplinaObject);
         }
         return new JSONObject().put("disciplinas", disciplinaArray).toString();
     }
 
     public JSONObject parse(String url) throws IOException {
         String base = "https://www.unochapeco.edu.br/saa/";
 
         Document document = Jsoup.connect(base + url)
                 .cookie("PHPSESSID", session)
                 .timeout(8000)
                 .get();
 
         if (!document.select(":contains(Disciplina Fechada!)").isEmpty()) {
 
             JSONObject avaliacaoObject = new JSONObject();
             document = Jsoup.connect(base + "notas.php")
                     .cookie("PHPSESSID", session)
                     .timeout(8000)
                     .get();
 
             Pattern pattern = Pattern.compile("coddisc=[0-9]+&");
             Matcher matcher = pattern.matcher(url);
 
             String coddisc = null;
             while (matcher.find()) {
                 coddisc = matcher.group().replaceAll("\\D", "");
                 break;
             }
 
             Element elemento = document.select("form[name$=graduacao] tr"
                     + ":contains(" + coddisc + ")").first();
 
             avaliacaoObject.put("estado", "fechada");
             avaliacaoObject.put("status", elemento.select("td:eq(11)").text());
             avaliacaoObject.put("G1", elemento.select("td:eq(6)").text());
             avaliacaoObject.put("G2", elemento.select("td:eq(7)").text());
             avaliacaoObject.put("G3", elemento.select("td:eq(8)").text());
             avaliacaoObject.put("MF", elemento.select("td:eq(9)").text());
 
             return avaliacaoObject;
         }
 
         Elements avaliacoes = document.select("form table:eq(0) tr:gt(4)");
 
         JSONArray avaliacaoArray = new JSONArray();
         for (Element avaliacao : avaliacoes) {
             if (!avaliacao.select("td:eq(3):contains(nota)").isEmpty()
                     || avaliacao.select("td:eq(3)").isEmpty()) {
                 continue;
             }
 
             JSONObject avaliacaoObject = new JSONObject();
             avaliacaoObject.put("nome", avaliacao.select("td:eq(0)").text());
             avaliacaoObject.put("peso", avaliacao.select("td:eq(1)").text());
             avaliacaoObject.put("data", avaliacao.select("td:eq(2)").text());
             avaliacaoObject.put("nota", avaliacao.select("td:eq(3)").text());
 
             avaliacaoArray.put(avaliacaoObject);
         }
 
         String mediaG1 = new String();
         String mediaG2 = new String();
         try {
             mediaG1 = document
                     .select("form table:eq(0) tr:contains(Média de G1) td:eq(1)")
                     .first()
                     .text();
 
             mediaG2 = document
                     .select("form table:eq(0) tr:contains(Média de G2) td:eq(1)")
                     .first()
                     .text();
         } catch (NullPointerException e) {
             ;
         }
 
         return new JSONObject()
                 .put("estado", "aberta")
                 .put("mediaG1", mediaG1)
                 .put("mediaG2", mediaG2)
                 .put("avaliacoes", avaliacaoArray);
     }
 }
