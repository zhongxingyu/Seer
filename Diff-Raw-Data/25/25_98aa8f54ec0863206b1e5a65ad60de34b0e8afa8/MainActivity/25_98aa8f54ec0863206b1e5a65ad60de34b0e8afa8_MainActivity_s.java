 package br.ufrj.guiafundao;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Instrumentation;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TwoLineListItem;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.koushikdutta.async.AsyncServer.ThreadQueue;
 
 
 
 @SuppressLint({ "DefaultLocale", "NewApi" })
 public class MainActivity extends Activity {
 
 	static boolean focus = true;
 	private static final String TAG = MainActivity.class.getSimpleName();
 	public JsonArray listaGuiaFundao  = new JsonArray();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         final ImageView ImagemBusca = (ImageView) findViewById(R.id.imageView1);
         final EditText campoBusca = (EditText) findViewById(R.id.editText1);
         final Button btnBusca = (Button) findViewById(R.id.button1);
         final ListView list = (ListView) findViewById(R.id.listView1);
         final View layoutResult = findViewById(R.id.layout_result);
         final EditText campoBuscaMenor = (EditText) findViewById(R.id.editTextResult);
         final ImageButton btnBuscaMenor = (ImageButton) findViewById(R.id.buttonResult);
         
         final String urlBase = "http://dcc.ufrj.br/~viniciusmello/GuiaFundao/base.json";
         
         btnBusca.setEnabled(true);
         
         //Parar de exibir o teclado  logo  ao iniciar
         InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(campoBusca.getWindowToken(), 0);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         
         getApplicationContext();
         
         File SDCard = Environment.getExternalStorageDirectory();
         
         File SDCardRoot = new File (SDCard.getAbsolutePath() + "/baseguia/base.json");
         if (!SDCardRoot.exists() && !is3G() && !isWifi()) {
         	btnBusca.setEnabled(false);
         	
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	builder.setTitle("Falha de conexão");
         	builder.setMessage("É necessário uma conexão com a rede na primeira utilização.");
         	builder.setPositiveButton("Sair",
         		    new DialogInterface.OnClickListener() {
 		        		public void onClick(DialogInterface dialog, int which) {
 		        			System.exit(0);
 		        		}
 		    });
         	builder.setCancelable(false);
         	builder.show();
         }
         else if(!SDCardRoot.exists())	{
         	btnBusca.setEnabled(false);
         	Toast toast2 = Toast.makeText(MainActivity.this,"Baixando serviços pela primeira vez...", Toast.LENGTH_SHORT);
         	toast2.show();
         }
         else	{
         	String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/baseguia/base.json";
             try {
     			listaGuiaFundao = recuperarBase(file);
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
         }
         if (is3G() || isWifi()) {
 	        try {
 	        	new RecuperarBase(this).execute(urlBase, "base.json",SDCardRoot.exists());
 
 	        } catch (Exception e) {
 	        	e.printStackTrace();
 	        }
         } /*else {
             String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/baseguia/base.json";
             try {
     			listaGuiaFundao = recuperarBase(file);
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
         }*/
         
         btnBuscaMenor.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				btnBusca.performClick();
 				
 			}
 		});
         
         campoBuscaMenor.setOnKeyListener(new OnKeyListener() {
 			
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == KeyEvent.ACTION_DOWN) {
 					if (keyCode == KeyEvent.KEYCODE_ENTER) {
 						
 						btnBusca.performClick();
 						
 						//Instrumentation inst = new Instrumentation();
 						//inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
 					}
 				}
 				return false;
 			}
 		});
         
         
         btnBusca.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				
				String stringBusca = campoBuscaMenor.getText().toString();
 				
 				//para só fazer da primeira vez
 				if (layoutResult.getVisibility()==layoutResult.GONE) {
 					ImagemBusca.setVisibility(ImagemBusca.GONE);
 					btnBusca.setVisibility(btnBusca.GONE);
					campoBusca.setVisibility(campoBusca.GONE);
 					layoutResult.setVisibility(layoutResult.VISIBLE);
 					stringBusca = campoBusca.getText().toString();
 					campoBuscaMenor.setText(stringBusca);
				}				
 				
 				getApplicationContext();
 				
 				String msgResult = new String();
 				
 				if (!stringBusca.isEmpty() && (isWifi() || is3G()))
 				{
 					new EnviarFeedback().execute(stringBusca);
 				}
 				
 				final List<Local> resultado = new ArrayList<Local>();
 				
 				String[] buscaSplit = stringBusca.split(" ");
 				JsonArray resultadoParcial  = new JsonArray();
 				//salva resultado de cada palavra da busca
 				JsonArray resultadoTemporario  = new JsonArray();
 				resultadoParcial = listaGuiaFundao;
 				
 				boolean buscaVazia = false;
 				
 				for(String busca : buscaSplit){
 					resultadoTemporario=null;
 					resultadoTemporario = new JsonArray();
 					//se busca vazia, imprime toda a lista
 					if(busca.equals("") && (buscaSplit.length == 1))	{
 						buscaVazia = true;
 					}
 					for (int i = 0; i < resultadoParcial.size(); i++) {
 						JsonObject obj = resultadoParcial.get(i).getAsJsonObject();
 						String nome = obj.get("nome").getAsString();
 						String tipo = obj.get("tipo").getAsString();
 						String centro = obj.get("centro").getAsString();
 						String sigla = obj.get("sigla").getAsString();
 						String local = obj.get("local").getAsString();
 						String diferencial = obj.get("diferencial").getAsString();
 						String preco = obj.get("preco").getAsString();
 
 						String resultadoAux = new String();
 						String resultadoAux2 = new String();
 						String montarPreco = new String();
 
 						for(int j = 0; j<Integer.parseInt(preco); j++){
 							montarPreco = montarPreco+"$";
 						}
 
 						resultadoAux = nome + "|" + tipo + "|" + centro + "|" + sigla + "|" + local + "|" + diferencial + "|";
 						resultadoAux2 = " " + nome + " " + tipo + " " + centro + " " + sigla + " " + local + " " + diferencial + " ";
 						
 						//if (resultadoAux.toLowerCase().contains(busca.toLowerCase()))
 						if (PalavraEmString(resultadoAux2.toLowerCase(), busca.toLowerCase()) || buscaVazia == true) {
 							resultadoTemporario.add(obj);
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
 							
 							
 							//Se já estiver inserido, foi achado novamente com outra palavra, coloca no topo da lista
 							/*else{
 								resultado.remove(resultado.indexOf(novo_local));
 								resultado.add(0, novo_local);
 							}*/
 						}
 						else	{
 							Local novo_local = new Local();
 							novo_local.setCentro(centro);
 							novo_local.setDiferencial(diferencial);
 							novo_local.setLocal(local);
 							novo_local.setNome(nome);
 							novo_local.setPreco(montarPreco);
 							novo_local.setSigla(sigla);
 							novo_local.setTipo(tipo);
 							if (resultado.contains(novo_local)) {
 								resultado.remove(novo_local);
 							}
 						}
 					}
 					resultadoParcial=resultadoTemporario;
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
 				            	row.getText2().setText("Local: " + local.getLocal() + " - Preço: " + local.getPreco());
 				            }else{
 				            	row.getText1().setText(local.getNome() +" - " + local.getSigla());
 				            	row.getText2().setText(" Serviço:" + local.getTipo() + "    Local: " + local.getLocal());
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
                     	
                     	String stringBusca = campoBusca.getText().toString();
                     	//para só fazer da primeira vez
         				if (layoutResult.getVisibility()==layoutResult.GONE) {
         					ImagemBusca.setVisibility(ImagemBusca.GONE);
         					btnBusca.setVisibility(btnBusca.GONE);
        					campoBusca.setVisibility(campoBusca.GONE);
         					layoutResult.setVisibility(layoutResult.VISIBLE);
         					stringBusca = campoBusca.getText().toString();
         					campoBuscaMenor.setText(stringBusca);
         				}
                     	
                     	getApplicationContext();
         				
         				String msgResult = new String();
         				
         				if (!stringBusca.isEmpty() && (isWifi() || is3G()))
         				{
         					new EnviarFeedback().execute(stringBusca);
         				}
         				
         				final List<Local> resultado = new ArrayList<Local>();
         				
         				String[] buscaSplit = stringBusca.split(" ");
         				JsonArray resultadoParcial  = new JsonArray();
         				//salva resultado de cada palavra da busca
         				JsonArray resultadoTemporario  = new JsonArray();
         				resultadoParcial = listaGuiaFundao;
         				boolean buscaVazia = false;
         				
         				for(String busca : buscaSplit){
         					resultadoTemporario=null;
         					resultadoTemporario = new JsonArray();
         					//se busca vazia, imprime toda a lista
         					if(busca.equals("") && (buscaSplit.length == 1))	{
         						buscaVazia = true;
         					}
         					for (int i = 0; i < resultadoParcial.size(); i++) {
         						JsonObject obj = resultadoParcial.get(i).getAsJsonObject();
         						String nome = obj.get("nome").getAsString();
         						String tipo = obj.get("tipo").getAsString();
         						String centro = obj.get("centro").getAsString();
         						String sigla = obj.get("sigla").getAsString();
         						String local = obj.get("local").getAsString();
         						String diferencial = obj.get("diferencial").getAsString();
         						String preco = obj.get("preco").getAsString();
 
         						String resultadoAux = new String();
         						String resultadoAux2 = new String();
         						String montarPreco = new String();
 
         						for(int j = 0; j<Integer.parseInt(preco); j++){
         							montarPreco = montarPreco+"$";
         						}
 
         						resultadoAux = nome + "|" + tipo + "|" + centro + "|" + sigla + "|" + local + "|" + diferencial + "|";
         						resultadoAux2 = " " + nome + " " + tipo + " " + centro + " " + sigla + " " + local + " " + diferencial + " ";
         						
         						//if (resultadoAux.toLowerCase().contains(busca.toLowerCase()))
         						if (PalavraEmString(resultadoAux2.toLowerCase(), busca.toLowerCase()) || buscaVazia == true) {
         							resultadoTemporario.add(obj);
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
         							
         							
         							//Se já estiver inserido, foi achado novamente com outra palavra, coloca no topo da lista
         							/*else{
         								resultado.remove(resultado.indexOf(novo_local));
         								resultado.add(0, novo_local);
         							}*/
         						}
         						else	{
         							Local novo_local = new Local();
         							novo_local.setCentro(centro);
         							novo_local.setDiferencial(diferencial);
         							novo_local.setLocal(local);
         							novo_local.setNome(nome);
         							novo_local.setPreco(montarPreco);
         							novo_local.setSigla(sigla);
         							novo_local.setTipo(tipo);
         							if (resultado.contains(novo_local)) {
         								resultado.remove(novo_local);
         							}
         						}
         					}
         					resultadoParcial=resultadoTemporario;
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
         				            	row.getText2().setText("Local: " + local.getLocal() + " - Preço: " + local.getPreco());
         				            }else{
         				            	row.getText1().setText(local.getNome() +" - " + local.getSigla());
         				            	row.getText2().setText(" Serviço:" + local.getTipo() + "    Local: " + local.getLocal());
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
         		
         		mensagem_builder.append("Serviço: " + local.getTipo());
         		mensagem_builder.append("\n");
         		
         		String localizacao = local.getSigla();
         		if(!local.getLocal().equals(""))
         			localizacao += " - " + local.getLocal();
         		mensagem_builder.append("Localização: " + localizacao);
         		mensagem_builder.append("\n");
 
         		mensagem_builder.append("Preço: " + local.getPreco());
         		mensagem_builder.append("\n");
         		
         		if(!local.getDiferencial().equals(""))
         			mensagem_builder.append("Diferencial: " + local.getDiferencial());
         		
         		adb.setMessage(mensagem_builder.toString());
         		adb.setPositiveButton("Ok", null);
         		adb.show();                     
         	}
         });
     }
 
     private boolean testaConexaoBase(String urls) {
     	boolean isConected = true;
     	try	{
     	URL url = new URL(urls);
         HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.setRequestMethod("GET");
         urlConnection.setDoOutput(true);
         
         urlConnection.connect();
         
         } catch (IOException e)	{
         	isConected =  false;
         }
 		return isConected;
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
     
     public boolean PalavraEmString(String frase, String palavra) {  
         boolean count = false;  
         Matcher matcher = Pattern.compile(" " + palavra + " ").matcher(frase);  
         if(matcher.find())	{
         	count=true;
         }         
         return count;  
     }
     
 	public void onWindowFocusChanged(boolean hasFocus) {
 
         super.onWindowFocusChanged(hasFocus);
 
         if(hasFocus) {
         	focus=true;
         }
         else	{
         	focus=false;
         }
 	}
 }
