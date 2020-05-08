 package com.possebom.visavale;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Main extends Activity {
 	public static final String PREFS_NAME = "VisaValePrefs";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);    
 		final Button button = (Button) findViewById(R.id.button);
 		final EditText cardNumber = (EditText)findViewById(R.id.cardNumber); 
 		final TextView view = (TextView)findViewById(R.id.view);
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		cardNumber.setText(settings.getString("cardNumber", ""));
 		view.setText(clearHistory(settings.getString("saldo", "")));
 
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				updateView();
 			}
 		});        
 	}
 
 	private String getUrl() {
 		final EditText cardNumber = (EditText)findViewById(R.id.cardNumber); 
 		URI myURL = null;
 		try {
 			myURL = new URI("http://www.cbss.com.br/inst/convivencia/SaldoExtrato.jsp?numeroCartao=" + cardNumber.getText());
 		} catch (URISyntaxException e1) {
 			e1.printStackTrace();
 		}
 
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpGet getMethod = new HttpGet(myURL);
 		HttpResponse httpResponse;
 
 		String result = "Erro pegando dados.";
 
 		try {
 			httpResponse = httpClient.execute(getMethod);
 			HttpEntity entity = httpResponse.getEntity();
 			if (entity != null) {
 				InputStream instream = entity.getContent();
 				BufferedReader reader = new BufferedReader( new InputStreamReader(instream));
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				try {
 					while ((line = reader.readLine()) != null) {
 						if(line.contains("lido."))
 						{
 							sb.append("Cartão Inválido");
 							break;
 						}
 						if(line.contains("topTable"))
 							continue;
 					if(line.contains("400px") )
 					{
 						sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append(" - ");
 					}
 					if(line.contains("50px") )
 					{
 						if(line.contains("R"))
 							sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append("\n");
 						else if (line.contains("/"))
 							sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append(" - ");
 					}
 						
 						if(line.contains("Saldo d") )
 						{
 							sb.append(line.replaceAll("\\<.*?>","").replaceAll("dispo.*vel:", " : ").trim());
 							sb.append("\n");
 							sb.append(getDate());
 						}
 						
 					}
 				} catch (Exception e) {
 					result = "Erro carregando dados.";
 				} finally {
 					try {
 						instream.close();
 					} catch (Exception e) {
 						result = "Erro carregando dados.";
 					}
 				}
 				result = sb.toString();
 			}
 		} catch (Exception e) {
 			result = "Erro carregando dados.";
 		}
 		return result;
 	}
 
 
 	public static String getDate() {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
 		return sdf.format(cal.getTime());
 	}
 
 	private void updateView() {          
 		final TextView view = (TextView)findViewById(R.id.view);
 		final EditText cardNumber = (EditText)findViewById(R.id.cardNumber); 
 		view.setText("Carregando dados...");
 		String data = getUrl();
 		if(data.contains("Saldo"))
 		{
 			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putString("saldo", data);
 			editor.putString("cardNumber", cardNumber.getText().toString());
 			editor.commit();
 			
 		}
 		view.setText(clearHistory(data));
 	}
 	
 	private String clearHistory(String str)
 	{
 		String[] oi = str.split("\n");
 		str = oi[oi.length-2] + "\n" + oi[oi.length-1];
 		return str;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.menu_quit:			
 			finish();
 			break;
 		case R.id.menu_about:
 			final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setIcon(R.drawable.icon);
 			alertDialog.setTitle("Visa Vale");
 			alertDialog.setMessage("Desenvolvido por Alexandre Possebom <alexandrepossebom@gmail.com>");
 			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					alertDialog.dismiss();
 				}
 			});
 			alertDialog.show();
 			break;
 		case R.id.menu_history:
 			startActivity(new Intent(this, History.class));
 			break;
 		}
 
 		return true;
 	}
 }
