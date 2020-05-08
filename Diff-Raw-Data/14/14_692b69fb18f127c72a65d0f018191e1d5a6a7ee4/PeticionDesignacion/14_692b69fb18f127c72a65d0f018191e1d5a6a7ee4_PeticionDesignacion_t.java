 package com.federacion.designadroid;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.federacion.designadroid.modelo.Designacion;
 import com.federacion.designadroid.modelo.Miembro;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  * Created by Pablo on 6/08/13.
  */
 public class PeticionDesignacion extends AsyncTask<Void, Void, Void>{
 
     private String dni;
 
     private String licencia;
 
     private HttpResponse response;
 
     public PeticionDesignacion(String pdni, String plicencia){
         dni = pdni;
         licencia = plicencia;
     }
 
     public String getDni() {
         return dni;
     }
 
     public String getLicencia() {
         return licencia;
     }
 
     @Override
     protected void onPostExecute(Void aVoid) {
         Log.d("PeticionDesignacion:", response.getEntity().toString());
     }
 
     @Override
     protected Void doInBackground(Void... voids) {
         try{
             HttpClient client = new DefaultHttpClient();
             HttpPost request = new HttpPost("https://intrafeb.feb.es/ConsultaDesignacion/Inicio.aspx");
 
             List<NameValuePair> parameters = new ArrayList<NameValuePair>();
             parameters.add(new BasicNameValuePair("__VIEWSTATE","dDwtMTAyNjg0NjQ2Nzt0PDtsPGk8MT47PjtsPHQ8O2w8aTwxPjs+O2w8dDw7bDxpPDE+Oz47bDx0PDtsPGk8MD47PjtsPHQ8O2w8aTwxMz47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8XGU7Pj47Pjs7Pjs+Pjs+Pjs+Pjs+Pjs+Pjs+cdLwYQJNLMDHWmtEZNKUTjo+53g="));
             parameters.add(new BasicNameValuePair("txtNick", getLicencia()));
             parameters.add(new BasicNameValuePair("txtDni", getDni()));
             parameters.add(new BasicNameValuePair("cmbTipoConsulta","1"));
             parameters.add(new BasicNameValuePair("btnValidar", "Validar"));
             request.setEntity(new UrlEncodedFormEntity(parameters));
 
             response = client.execute(request);
             HttpEntity entity = response.getEntity();
             String html = generaHTML(entity.getContent(),(int)entity.getContentLength());
             Designacion des = parseHtml(html);
         }
         catch(UnsupportedEncodingException e){
             Log.d("Excepcion:",e.toString());
         }
         catch(ClientProtocolException e){
             Log.d("Excepcion:",e.toString());
         }
         catch(IOException e){
             Log.d("Excepcion:",e.toString());
         }
 
         return null;
     }
 
     public String generaHTML(InputStream input, int size){
         BufferedReader reader =  new BufferedReader(new InputStreamReader(input), size);
         StringBuilder sbHtml = new StringBuilder();
         try{
             String line;
 
             while ((line=reader.readLine()) != null) {
                 sbHtml.append(line);
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return sbHtml.toString();
     }
 
     Designacion parseHtml(String s){
         Designacion designacion = new Designacion();
         Document doc = Jsoup.parse(s);
 
         String fraseFechas = doc.select("#lblFechas").first().html();
         String[] fraseFechasSplitted = fraseFechas.split("\\s");
        String[] fecha1Splitted = fraseFechasSplitted[4].split("\\D+");
        String[] fecha2Splitted = fraseFechasSplitted[6].split("\\D+");
 
         GregorianCalendar fecha1 = new GregorianCalendar(Integer.parseInt(fecha1Splitted[2])+1900,Integer.parseInt(fecha1Splitted[1]),
                 Integer.parseInt(fecha1Splitted[0]));
         GregorianCalendar fecha2 = new GregorianCalendar(Integer.parseInt(fecha2Splitted[2])+1900,Integer.parseInt(fecha2Splitted[1]),
                 Integer.parseInt(fecha2Splitted[0]));
 
         designacion.setFechaInicio(fecha1);
         designacion.setFechaFin(fecha2);
 
         designacion.setMiembroCTA(new Miembro(doc.select("#lblNombre").first().html()));
 
         //TODO: Parsear la informacion de los partidos.
 
         return designacion;
     }
 }
