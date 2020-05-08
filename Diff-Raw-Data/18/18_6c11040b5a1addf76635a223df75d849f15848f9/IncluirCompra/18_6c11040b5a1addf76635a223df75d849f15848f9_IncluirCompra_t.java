 package com.oranz.faturei;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.oranz.entidades.Cartao;
 import com.oranz.entidades.Compra;
 import com.oranz.util.CustomOnItemSelectedListener;
 
 public class IncluirCompra extends Activity {
 	private EditText valor;
 	private EditText descricao;
 	private TextView vezes;
 	private TextView screensizeTV;
 	private Spinner parcelas;
 	private DatePicker dataCompra;
 	private Spinner cartao;
//    private DecimalFormat df = new DecimalFormat("0.00");  
     private String array_spinner[];
     private Button btnVoltar;
     private Button btnIncluir;
     private List<Cartao> cartoes;
     private SimpleDateFormat dateformat;
     
     String dataCompraString;
 
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_incluir_compra);
         valor = (EditText) findViewById(R.id.valorET);
         descricao = (EditText) findViewById(R.id.descricaoET);
         vezes = (TextView) findViewById(R.id.novezesTV);
         screensizeTV = (TextView) findViewById(R.id.screensizeTV);
         btnVoltar = (Button) findViewById(R.id.voltarBT);
         btnIncluir = (Button)  findViewById(R.id.incluirBT);
         dateformat = new SimpleDateFormat("dd/MM/yyyy");        
 
         if(screensizeTV.getText().toString().equalsIgnoreCase("normal")){
         	dataCompra = (DatePicker) findViewById(R.id.dataCompraDP);
         }else{
         	dataCompra = (DatePicker) findViewById(R.id.dataCompraDP);
         }
  
         array_spinner= new String[12];
         array_spinner[0] = "1";
         array_spinner[1] = "2";
         array_spinner[2] = "3";
         array_spinner[3] = "4";
         array_spinner[4] = "5";
         array_spinner[5] = "6";
         array_spinner[6] = "7";
         array_spinner[7] = "8";
         array_spinner[8] = "9";
         array_spinner[9] = "10";
         array_spinner[10] = "11";
         array_spinner[11] = "12";
         parcelas = (Spinner) findViewById(R.id.parcelasSP);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         parcelas.setAdapter(adapter);
         parcelas.setSelection(0);
 
         cartoes = MainActivity.getCartoes();
         int i = 0;
         array_spinner= new String[cartoes.size()];
         for (Cartao cartao : cartoes) {
 			array_spinner[i] = cartao.getCartao();
         	i++;
 		}
         
         cartao = (Spinner) findViewById(R.id.cartaoSP);
         
         //@SuppressWarnings({ "rawtypes", "unchecked" })
 		//ArrayAdapter adapter5 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array_spinner);
         ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner);
         adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         cartao.setAdapter(adapter5);
         cartao.setSelection(0);
  	
         Intent intent = getIntent();
         String origin = ""; 
         origin = intent.getStringExtra("origin");
         // Binding Click event to Button
         if(origin.equalsIgnoreCase("fatura")){
         	btnVoltar.setOnClickListener(new View.OnClickListener() {public void onClick(View arg0) {Toast.makeText(getApplicationContext(), "NECESSÁRIO ATUALIZAR A TELA DE FATURA", Toast.LENGTH_LONG).show(); finish();}});
         }else{
         	btnVoltar.setOnClickListener(new View.OnClickListener() {public void onClick(View arg0) {finish();}});
         }
 	}
     
     public void addListenerOnSpinnerItemSelection() {
     	parcelas = (Spinner) findViewById(R.id.parcelasSP);
     	parcelas.setOnItemSelectedListener(new CustomOnItemSelectedListener());
       }
     
     // get the selected dropdown list value
     public void addListenerOnButton() {   
     	parcelas = (Spinner) findViewById(R.id.parcelasSP);
     	cartao = (Spinner) findViewById(R.id.cartaoSP);
 	  	btnIncluir = (Button) findViewById(R.id.incluirBT);
 	  	btnIncluir.setOnClickListener(new OnClickListener() {
 		  	@Override
 		  	public void onClick(View v) {   
 		  	    Toast.makeText(getApplicationContext(),"OnClickListener : " + "\nSpinner 1 : "+ String.valueOf(parcelas.getSelectedItem()),	Toast.LENGTH_SHORT).show();
 		  	  }
 	  	});
     }
     
     public void incluir(View view) {
         switch (view.getId()) {
         case R.id.incluirBT:
         	CheckBox parcelado = (CheckBox) findViewById(R.id.parceladoCB);
             Spinner parcelas = (Spinner) findViewById(R.id.parcelasSP);
             int qtdParcelas = 1;
                     	
             if (valor.getText().length() == 0) {
                 Toast.makeText(this, "Favor Inserir o total", Toast.LENGTH_LONG).show();
                 return;
             }
             
             if(!parcelado.isChecked()){
 	            Compra compra = new Compra();
 	            String data = "";
 	            if(dataCompra != null){
 	            	data = dataCompra.getDayOfMonth() + "/" + (dataCompra.getMonth() + 1) + "/" + dataCompra.getYear();
 	            }else{
 	            	data = dateformat.format(new Date());
 	            }
 	            compra.setData( data );
 	            compra.setParcelas("A vista");
 	            if (descricao.getText().length() > 0) {
 	            	compra.setDescricao(descricao.getText().toString());
 	            }else{
 	            	compra.setDescricao("");
 	            }
 	            if(cartoes.size() < 1){
 	            	MainActivity.addCartao("DEFAULT", ""+dataCompra.getDayOfMonth());
 	            	compra.setCartao("DEFAULT");
 	            }else{
 	            	compra.setCartao(cartao.getSelectedItem().toString());
 	            }
 	            String valorOk = valor.getText().toString().replace(",",".");
	            compra.setValor(Double.parseDouble(valorOk));
 	            MainActivity.addCompra(compra);
             }else{
             	qtdParcelas = Integer.parseInt(parcelas.getSelectedItem().toString());
 	            String valorOk = valor.getText().toString().replace(",",".");
 	            valorOk = valorOk.replaceAll("[^0-9.]+", "");
             	double valorCompra = Double.parseDouble(valorOk);
             	double valorCompraParcelada = valorCompra / qtdParcelas;
             	
             	for (int i = 0; i < qtdParcelas; i ++){
 		            Compra compra = new Compra();
 		            String data = "";
 		            if(dataCompra == null){
 		            	Date dataTemp = new Date();
 		            	dataTemp.setMonth(dataTemp.getMonth()+i);
 		            	data = dateformat.format(dataTemp);
 		            }else{
 			            if ((dataCompra.getMonth() + 1) + i > 12){
 			            	data = dataCompra.getDayOfMonth() + "/" + ((dataCompra.getMonth()-12) + 1 + i)  + "/" + (dataCompra.getYear() + 1);
 			            }else{
 			            	data = dataCompra.getDayOfMonth() + "/" + (dataCompra.getMonth() + 1 + i)  + "/" + dataCompra.getYear();
 			            }
 		            }
 		            compra.setData( data );
 		            compra.setParcelas((i + 1) + " de " + qtdParcelas );
 		            if (descricao.getText().length() > 0) {
 		            	compra.setDescricao(descricao.getText().toString());
 		            }else{
 		            	compra.setDescricao("");
 		            }
 		            if(cartoes.size() < 1){
 		            	if(data.contains("/")){
 		            		MainActivity.addCartao("DEFAULT", "" + (new Date().getDay()));
 		            	}else{
 		            		MainActivity.addCartao("DEFAULT", data);
 		            	}
 		            	compra.setCartao("DEFAULT");
 		            }else{
 		            	compra.setCartao(cartao.getSelectedItem().toString());
 		            }
		            compra.setValor(valorCompraParcelada);
 		            MainActivity.addCompra(compra);
             	}            	
             }
             
             if(parcelado.isChecked()){
                 Toast.makeText(this, "COMPRA PARCELADA INSERIDA", Toast.LENGTH_LONG).show();
             }else {
             	Toast.makeText(getApplicationContext(), "INSERIDO COM SUCESSO", Toast.LENGTH_LONG).show();
             }
             
             parcelado.setChecked(false);
             parcelas.setSelection(0);       
             valor.setText(null);  
             descricao.setText(null);
 	        break;
         }
       }
     
     public void parcelado(View view){
     	vezes.setVisibility(0);
         parcelas.setVisibility(0);
     }
 }
