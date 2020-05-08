 package com.zubisoft.matintSMS;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 
 public class ReceptorSMS extends BroadcastReceiver {
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		
 		//Declaramos variables para utilizar.
 		Object[] pdus;
 		SmsMessage mensaje;
 		Intent SMSIntent;
 		HttpClient webclient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost("http://maternidadinteractiva.jit.su/alarm");
 		List<NameValuePair> dataPost = new ArrayList<NameValuePair>(2);
 		HttpResponse response;
 		
 		//si no recibe nada retornar
 		Bundle extras = intent.getExtras();
 		if (extras == null) {
 			return;
 		}
 		
 		//Incializamos nuestras listas con el tamaño adecuado.
 		pdus = (Object[]) extras.get("pdus");
 		
 		//procesamos cada SMS.
 		for ( int i = 0; i < pdus.length ; i += 1){
 			mensaje = SmsMessage.createFromPdu((byte[])pdus[i]);
 			
 			// esterilizamos los valores
 			dataPost.clear();
 			
 			//Enviamos los datos al servidor
 			dataPost.add(new BasicNameValuePair("source",mensaje.getOriginatingAddress()));
			dataPost.add(new BasicNameValuePair("id",mensaje.getMessageBody()));
 			
 			try {
 				httpPost.setEntity(new UrlEncodedFormEntity(dataPost));
 				response = webclient.execute(httpPost);
 				
 				SMSIntent = new Intent("SMSMail.Intent.MAIN");
 				SMSIntent.putExtra("enviador", mensaje.getOriginatingAddress());
 				SMSIntent.putExtra("contenido", mensaje.getMessageBody());
 				SMSIntent.putExtra("respuesta", EntityUtils.toString(response.getEntity()));
 				
 				context.sendBroadcast(SMSIntent);
 				
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 	            // writing exception to log
 	            e.printStackTrace();
 	        } catch (IOException e) {
 	            // writing exception to log
 	            e.printStackTrace();
 	 
 	        } catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
	        }
 		}
 	}
 }
