 package br.ufrj.guiafundao;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.TwoLineListItem;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 
 @SuppressLint({ "DefaultLocale", "NewApi" })
 public class MainActivity extends Activity {
 
 	private static final String TAG = MainActivity.class.getSimpleName();
 	private JsonArray listaGuiaFundao  = new JsonArray();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
       
         final EditText campoBusca = (EditText) findViewById(R.id.editText1);
         final Button btnBusca = (Button) findViewById(R.id.button1);
         final ListView list = (ListView) findViewById(R.id.listView1);
         
         btnBusca.setEnabled(false);
         
         //Parar de exibir o teclado  logo  ao iniciar
         InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(campoBusca.getWindowToken(), 0);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         
         getApplicationContext();
         
         File SDCard = Environment.getExternalStorageDirectory();
         
         //File SDCardRoot = new File (SDCard.getAbsolutePath() + "/baseguia");
         File SDCardRoot = new File (SDCard.getAbsolutePath() + "/baseguia/base.json");
         if(!SDCardRoot.exists()) {
         	if (!is3G() && !isWifi())  {
        		Toast.makeText(MainActivity.this," necessrio uma conexo com a rede na primeira utilizao.", Toast.LENGTH_LONG).show();
        	}
        	else	{
	        	try{
	        		new RecuperarBase(this).execute("http://dcc.ufrj.br/~viniciusmello/GuiaFundao/base.json", "base.json");
	        	}catch(Exception e) {
	    			e.printStackTrace();
	    		}
         	}
         }
         else	{
         	btnBusca.setEnabled(true);
         }
         
         String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/baseguia/base.json";
         try {
 			listaGuiaFundao = recuperarBase(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
         btnBusca.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				
 				getApplicationContext();
 				
 				String msgResult = new String();
 				String stringBusca = campoBusca.getText().toString();
 				
 				if (!stringBusca.isEmpty() && (isWifi() || is3G()))
 				{
 					new EnviarFeedback().execute(stringBusca);
 				}
 				
 				final List<Local> resultado = new ArrayList<Local>();
 				
 				String[] buscaSplit = stringBusca.split(" ");
 				
 				for(String busca : buscaSplit){
 					for (int i = 0; i < listaGuiaFundao.size(); i++) {
 						JsonObject obj = listaGuiaFundao.get(i).getAsJsonObject();
 						String nome = obj.get("nome").getAsString();
 						String tipo = obj.get("tipo").getAsString();
 						String centro = obj.get("centro").getAsString();
 						String sigla = obj.get("sigla").getAsString();
 						String local = obj.get("local").getAsString();
 						String diferencial = obj.get("diferencial").getAsString();
 						String preco = obj.get("preco").getAsString();
 
 						String resultadoAux = new String();
 						String montarPreco = new String();
 
 						for(int j = 0; j<Integer.parseInt(preco); j++){
 							montarPreco = montarPreco+"$";
 						}
 
 						resultadoAux = nome + "|" + tipo + "|" + centro + "|" + sigla + "|" + local + "|" + diferencial + "|";
 
 						if (resultadoAux.toLowerCase().contains(busca.toLowerCase())) {
 							Local novo_local = new Local();
 							novo_local.setCentro(centro);
 							novo_local.setDiferencial(diferencial);
 							novo_local.setLocal(local);
 							novo_local.setNome(nome);
 							novo_local.setPreco(montarPreco);
 							novo_local.setSigla(sigla);
 							novo_local.setTipo(tipo);
 							
 							if(!resultado.contains(novo_local)){
 								resultado.add(novo_local);
 							} 
 							//Se j� estiver inserido, foi achado novamente com outra palavra, coloca no topo da lista
 							else{
 								resultado.remove(resultado.indexOf(novo_local));
 								resultado.add(0, novo_local);
 							}
 						}
 					}
 				}
 				if (resultado.isEmpty()) {
 					Toast.makeText(MainActivity.this, "Nenhum resultado encontrado.", Toast.LENGTH_LONG).show();
 					list.setAdapter(null);
 				} else{
 					ArrayAdapter<Local> adapter = new ArrayAdapter<Local>(MainActivity.this,android.R.layout.simple_list_item_2, resultado){
 				        @Override
 				        public View getView(int position, View convertView, ViewGroup parent){
 				            TwoLineListItem row;            
 				            if(convertView == null){
 				                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				                row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);                    
 				            }else{
 				                row = (TwoLineListItem) convertView;
 				            }
 				            Local local = resultado.get(position);
 
 				            if(local.getTipo() == "restaurante"){
 				            	row.getText1().setText(local.getTipo() + " " + local.getNome() +" - " + local.getSigla());
 				            	row.getText2().setText("Local: " + local.getLocal() + " - Preo: " + local.getPreco());
 				            }else{
 				            	row.getText1().setText(local.getNome() +" - " + local.getSigla());
 				            	row.getText2().setText(" Servio:" + local.getTipo() + "    Local: " + local.getLocal());
 				            }
 				 
 				            return row;
 				        }
 				    };
 				    
 			        ListView list = (ListView) findViewById(R.id.listView1);
 					list.setAdapter(adapter);
 					
 					if(resultado.size() == 1)	
 						msgResult =" resultado encontrado.";
 					else 
 						msgResult =" resultados encontrados.";
 					
 					//Toast.makeText(MainActivity.this,String.valueOf(resultado.size()) + msgResult, Toast.LENGTH_LONG).show();
 				}
 				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
 				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 			}
 		});
         
         campoBusca.setOnKeyListener(new OnKeyListener()
         {
         	@Override
             public boolean onKey(View v, int keyCode, KeyEvent event)
             {
                 if (event.getAction() == KeyEvent.ACTION_DOWN)
                 {
                     if(keyCode == KeyEvent.KEYCODE_ENTER){
         				getApplicationContext();
         				
         				String msgResult = new String();
         				String stringBusca = campoBusca.getText().toString();
         				
         				if (!stringBusca.isEmpty() && (isWifi() || is3G()))
         				{
         					new EnviarFeedback().execute(stringBusca);
         				}
         				
         				final List<Local> resultado = new ArrayList<Local>();
         				
         				String[] buscaSplit = stringBusca.split(" ");
         				
         				for(String busca : buscaSplit){
         					for (int i = 0; i < listaGuiaFundao.size(); i++) {
         						JsonObject obj = listaGuiaFundao.get(i).getAsJsonObject();
         						String nome = obj.get("nome").getAsString();
         						String tipo = obj.get("tipo").getAsString();
         						String centro = obj.get("centro").getAsString();
         						String sigla = obj.get("sigla").getAsString();
         						String local = obj.get("local").getAsString();
         						String diferencial = obj.get("diferencial").getAsString();
         						String preco = obj.get("preco").getAsString();
 
         						String resultadoAux = new String();
         						String montarPreco = new String();
 
         						for(int j = 0; j<Integer.parseInt(preco); j++){
         							montarPreco = montarPreco+"$";
         						}
 
         						resultadoAux = nome + "|" + tipo + "|" + centro + "|" + sigla + "|" + local + "|" + diferencial + "|";
 
         						if (resultadoAux.toLowerCase().contains(busca.toLowerCase())) {
         							Local novo_local = new Local();
         							novo_local.setCentro(centro);
         							novo_local.setDiferencial(diferencial);
         							novo_local.setLocal(local);
         							novo_local.setNome(nome);
         							novo_local.setPreco(montarPreco);
         							novo_local.setSigla(sigla);
         							novo_local.setTipo(tipo);
         							
         							if(!resultado.contains(novo_local)){
         								resultado.add(novo_local);
         							} 
         							//Se j� estiver inserido no resultado, foi achado novamente com alguma outra palavra da busca. Coloca no topo da lista
         							else{
         								resultado.remove(resultado.indexOf(novo_local));
         								resultado.add(0, novo_local);
         							}
         						}
         					}
         				}
         				if (resultado.isEmpty()) {
         					Toast.makeText(MainActivity.this, "Nenhum resultado encontrado.", Toast.LENGTH_LONG).show();
         					list.setAdapter(null);
         				} else{
         					ArrayAdapter<Local> adapter = new ArrayAdapter<Local>(MainActivity.this,android.R.layout.simple_list_item_2, resultado){
         				        @Override
         				        public View getView(int position, View convertView, ViewGroup parent){
         				            TwoLineListItem row;            
         				            if(convertView == null){
         				                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         				                row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);                    
         				            }else{
         				                row = (TwoLineListItem) convertView;
         				            }
         				            Local local = resultado.get(position);
 
         				            row.getText1().setText(local.getTipo() + " " + local.getNome());
         				            row.getText2().setText("Local: " + local.getSigla() + ", Preço: " + local.getPreco());
         				 
         				            return row;
         				        }
         				    };
         				    
         			        ListView list = (ListView) findViewById(R.id.listView1);
         					list.setAdapter(adapter);
         					
         					if(resultado.size() == 1)	
         						msgResult =" resultado encontrado.";
         					else 
         						msgResult =" resultados encontrados.";
         					
         					//Toast.makeText(MainActivity.this,String.valueOf(resultado.size()) + msgResult, Toast.LENGTH_LONG).show();
         				}
         				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
         				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                     }
                 }
                 return false;
             }
              
         });
     
         list.setOnItemClickListener(new OnItemClickListener()
         {
         	public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
         	{
         		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
         		Local local = (Local) list.getItemAtPosition(position);
         		adb.setTitle(local.getNome());
         		
         		StringBuilder mensagem_builder = new StringBuilder();
         		
         		mensagem_builder.append("Servio: " + local.getTipo());
         		mensagem_builder.append("\n");
         		
         		String localizacao = local.getSigla();
         		if(!local.getLocal().equals(""))
         			localizacao += " - " + local.getLocal();
         		mensagem_builder.append("Localizao: " + localizacao);
         		mensagem_builder.append("\n");
 
         		mensagem_builder.append("Preo: " + local.getPreco());
         		mensagem_builder.append("\n");
         		
         		if(!local.getDiferencial().equals(""))
         			mensagem_builder.append("Diferencial: " + local.getDiferencial());
         		
         		adb.setMessage(mensagem_builder.toString());
         		adb.setPositiveButton("Ok", null);
         		adb.show();                     
         	}
         });
     }
 
     public void enviarFeedback(String busca){
 
 	}
     
     public void downloadFromUrl(String DownloadUrl, String fileName) {
 
     }
     
     public JsonArray recuperarBase(String filePath) throws IOException{
     
 		Gson gson = new Gson();
 		String file = readFile(filePath, Charset.defaultCharset());
 		return gson.fromJson(file, JsonArray.class);
 		
     }
     
     public String readFile(String file, Charset cs) throws IOException 
 	{
         FileInputStream stream = new FileInputStream(file);
         try {
             Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
             StringBuilder builder = new StringBuilder();
             char[] buffer = new char[8192];
             int read;
             while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                 builder.append(buffer, 0, read);
             }
             return builder.toString();
         } finally {
             stream.close();
         }   
 	}
     
     public boolean isWifi() {
     	WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);		
     	return wifi.isWifiEnabled();
 	}
 
 	public boolean is3G() {
 		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		return manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
 				.isConnectedOrConnecting();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 }
