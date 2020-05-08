 package com.oranz.faturei;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.oranz.entidades.Cartao;
 import com.oranz.entidades.Compra;
 
 public class VerFatura extends ListActivity {
 	private Button btnVoltar;
 	private Spinner cartoes;
 	private Spinner mesSpinner;
     private String array_spinner[];
 	private TextView totalView;
     private DecimalFormat df = new DecimalFormat("0.00"); 
 	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
     private SimpleAdapter notes;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ver_fatura);
         btnVoltar = (Button) findViewById(R.id.faturavoltar);
         totalView = (TextView) findViewById(R.id.totalTextView);
         List<Cartao> listaCartoes = MainActivity.getCartoes();
         
         array_spinner= new String[listaCartoes.size()+1];
         int i = 1;
         array_spinner[0] = "TODOS";
         for (Cartao cartao : listaCartoes) {
             array_spinner[i] = cartao.getCartao();
             i++;
 		}
         cartoes = (Spinner) findViewById(R.id.spinnercartoes);        
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         cartoes.setAdapter(adapter);
         cartoes.setSelection(0);
         
         array_spinner= new String[13];
         array_spinner[0] = "TODOS";
         array_spinner[1] = "1-Janeiro";
         array_spinner[2] = "2-Fevereiro";
         array_spinner[3] = "3-Março";
         array_spinner[4] = "4-Abril";
         array_spinner[5] = "5-Maio";
         array_spinner[6] = "6-Junho";
         array_spinner[7] = "7-Julho";
         array_spinner[8] = "8-Agosto";
         array_spinner[9] = "9-Setembro";
         array_spinner[10] = "10-Outubro";
         array_spinner[11] = "11-Novembro";
         array_spinner[12] = "12-Dezembro";
         mesSpinner = (Spinner) findViewById(R.id.mesSpinner);        
 		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner);
 		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         mesSpinner.setAdapter(adapter2);
         mesSpinner.setSelection(0);
         
 
         notes = new SimpleAdapter( 
  				this, 
  				list,
  				R.layout.main_item_two_line_row,
  				new String[] { "line1","line2" },
  				new int[] { R.id.text1, R.id.text2 }  );
         setListAdapter( notes );
         
         verFatura();
         
         btnVoltar.setOnClickListener(new View.OnClickListener() {public void onClick(View arg0) {finish();}});
     }
     
     public void verFatura(){
         try {
            	listaFiltrado();
         }catch (Exception e) {
             Toast.makeText(getApplicationContext(), "ERROR: " + e.toString(), Toast.LENGTH_LONG).show();
         }
     }
     
     private void addItem(String first, String second) {
   	  HashMap<String,String> item = new HashMap<String,String>();
   	  item.put( "line1", first );
   	  item.put( "line2", second);
   	  list.add( item );
       notes.notifyDataSetChanged();
   	}
     
     public void filtraMes(View view){
     	switch (view.getId()) {
 	        case R.id.okMesButton:
 	        	listaFiltrado();
     	}
     }
     
     public void listaFiltrado(){
     	list.clear();
     	List<Compra> listaCompras = new ArrayList<Compra>();
     	List<Cartao> listaCartao = new ArrayList<Cartao>();
     	String cartaoEscolhido = cartoes.getSelectedItem().toString();
     	String mesSelecionado = mesSpinner.getSelectedItem().toString();
     	String[] mesArray = mesSelecionado.split("-");
     	String mesEscolhido = mesArray[0];
 		int mesEscolhidoInt = 0;
     	if(!mesEscolhido.equals("TODOS")){ 
     		mesEscolhidoInt = Integer.parseInt(mesEscolhido);
     	}
     	
     	List<Compra> listaComprasList = new ArrayList<Compra>();
     	
         double total = 0.0;
     	
         listaCompras = MainActivity.getCompras();
         listaCartao = MainActivity.getCartoes();
     	for (Compra compraAux : listaCompras) {
     		if(cartaoEscolhido.equals(compraAux.getCartao().toString()) || cartaoEscolhido.equals("TODOS")){
     			String dataCompra = compraAux.getData().toString();
     			String cartaoCompra = compraAux.getCartao().toString();
     			String[] dataArray = dataCompra.split("/");
     			String diaCompra = dataArray[0];
     			String mesCompra = dataArray[1];
     			int diaFechamentoCartao = 0;
     			
     			for (Cartao cartaoAux : listaCartao) {
 					if(cartaoAux.getCartao().equals(cartaoCompra)){
 						if(cartaoAux.getFechamento().toString().length() > 0){
 							diaFechamentoCartao = Integer.parseInt(cartaoAux.getFechamento());
 						}else{
 							diaFechamentoCartao = 0;}
 					}
 				}
     			
     			int diaCompraInt = Integer.parseInt( diaCompra );
     			int mesCompraInt = Integer.parseInt( mesCompra );
     			
     			
     			if( ((mesEscolhidoInt == mesCompraInt ) && diaCompraInt > diaFechamentoCartao ) || ((mesEscolhidoInt == mesCompraInt ) && diaCompraInt <= diaFechamentoCartao ) || mesEscolhido.equals("TODOS")){
    				//total = total + Double.parseDouble(df.format(compraAux.getValor()));
    				total = total + compraAux.getValor();
     				listaComprasList.add(compraAux);
     			}
     		}
 		}  	
 
     	int listaSize = listaComprasList.size();
         String[] values = new String[listaSize];// { "Android", "iPhone", "WindowsMobile","Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X","Linux", "OS/2" };
 
         for (int i = 0; i < listaSize; i++) {
 			values[i]=("[" + listaComprasList.get(i).getCartao().toString() + "] " + listaComprasList.get(i).getData() + "\nR$ " + df.format(listaComprasList.get(i).getValor()) + " - " + listaComprasList.get(i).getDescricao() + " - " + listaComprasList.get(i).getParcelas() + "\n");
 			addItem(("[" + listaComprasList.get(i).getCartao().toString() + "] " + listaComprasList.get(i).getData()),("R$ " + df.format(listaComprasList.get(i).getValor()) + " - " + listaComprasList.get(i).getDescricao() + " - " + listaComprasList.get(i).getParcelas()));
 		}
 
         totalView.setVisibility(1);
         totalView.setText("\nTotal: R$" + df.format(total));   
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
       boolean result = super.onCreateOptionsMenu(menu);
       menu.add(0, 1, Menu.NONE, R.string.incluir_compra );
       menu.add(0, 2, Menu.NONE, R.string.exportar);
       menu.add(0, 3, Menu.NONE, R.string.importar);
       return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch ( item.getItemId() ) {
           case 1:   	
           	Intent nextScreen = new Intent(getApplicationContext(), IncluirCompra.class);
 
           	nextScreen.putExtra("origin", "fatura");
         	startActivity(nextScreen);
         	
           case 2:
         	  try{
 	        	File sdCard = Environment.getExternalStorageDirectory();
 	        	File dir = new File (sdCard.getAbsolutePath() + "/oranz/faturei");
 	        	dir.mkdirs();
 	        	String compras = "";
 
 		      	//DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 		      	//Date date = new Date();
 		      	//String filename = "faturei_export_"+dateFormat.format(date)+".csv";
 		      	String filename = "faturei_export.csv";
 	        	File file = new File(dir, filename);
 	        	FileOutputStream f = new FileOutputStream(file);
 	        	
 	        	List<Compra> listaCompras  = MainActivity.getCompras();
 	        	
 	        	for (Compra compra : listaCompras) {
 					compras = compras + "\n" + compra.getCartao() + ";" + compra.getData() + ";" + compra.getValor() + ";" + compra.getDescricao() + ";" + compra.getParcelas();
 				}
 	        	
 	        	System.out.println(compras);
 	        	f.write(compras.getBytes());
 	    
 	        	if(file.canWrite()){
 	        		f.close();
 	        		Toast.makeText(this, "Exportado com sucesso para: " + dir + filename, Toast.LENGTH_LONG).show();
 	        	}else{
 	        		Toast.makeText(this, "Não foi possível exportar devido a problema de permissão.", Toast.LENGTH_LONG).show();
 	        	}
         	  }catch(Exception e){
         		  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
         		  e.printStackTrace();
         	  }
           case 3:
         	  String eol = System.getProperty("line.separator");
         	  BufferedReader input = null;
         	  try {
     			File sdCard = Environment.getExternalStorageDirectory();
         	    input = new BufferedReader(new InputStreamReader(openFileInput(sdCard.getAbsolutePath() + "/oranz/faturei/faturei_export.csv")));
         	    String line;
         	    StringBuffer buffer = new StringBuffer();
         	    while ((line = input.readLine()) != null) {
         	    	buffer.append(line + eol);
         	    	System.out.println("COMPRAS");
         	    	System.out.println(line);
         	    }
         	  } catch (Exception e) {
         	     e.printStackTrace();
         	  } finally {
         	  if (input != null) {
         	    try {
         	    	input.close();
         	    } catch (IOException e) {
         	      e.printStackTrace();
         	    }
         	    }
         	  }
 	        	
         }
         listaFiltrado();
         return super.onOptionsItemSelected(item);
     }
 }
