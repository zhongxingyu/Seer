 package com.example.pizzadelivery;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.os.Looper;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import com.google.gson.Gson;
 
 public class PagamentoActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pagamento);
 		Pedido.getInstancia().FormaPagto = 1;
 		
 		RadioGroup radFormaPagamento = (RadioGroup) findViewById(R.id.radFormaPagamento);
 		radFormaPagamento.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				AlterarFormaPagamento();
 			}
 		});
 		
 		TextView txtEdtValorTotal = (TextView) findViewById(R.id.txtEdtValorTotal);
 		txtEdtValorTotal.setText(String.format("%.2f", Pedido.getValorTotal()));
 		
 		TextView txtEdtTrocoPara = (TextView) findViewById(R.id.txtEdtTrocoPara);
 		txtEdtTrocoPara.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				CalcularTroco();
 				return false;
 			}
 		});
 		
 		TextView txtEdtValorTroco = (TextView) findViewById(R.id.txtEdtValorTroco);
 		txtEdtValorTroco.setText(String.format("%.2f", 0.0f));
 		
 		Button btnPostarPedido = (Button) findViewById(R.id.btnPostarPedido);
 		btnPostarPedido.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				PostarPedido();
 			}
 		});
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		Intent k = new Intent(this, EnderecoActivity.class);
 		startActivity(k);
 		this.finish();
 	}
 	
 	private boolean trocoValido = false;
 	
 	private void AlterarFormaPagamento()
 	{
 		RadioButton radPagtoDinheiro = (RadioButton) findViewById(R.id.radPagtoDinheiro);
 		RadioButton radPagtoMaster = (RadioButton) findViewById(R.id.radPagtoMaster);
 		RadioButton radPagtoVisa = (RadioButton) findViewById(R.id.radPagtoVisa);
 		View grpTrocoPara = findViewById(R.id.grpTrocoPara);
 		View grpValorTroco = findViewById(R.id.grpValorTroco);
 
 		grpTrocoPara.setVisibility(radPagtoDinheiro.isChecked() ? View.VISIBLE : View.GONE);
 		grpValorTroco.setVisibility(radPagtoDinheiro.isChecked() ? View.VISIBLE : View.GONE);
 		
 		int formaPagto = 0;
 		
 		if (radPagtoDinheiro.isChecked())
 		{
 			formaPagto = 1;
 			CalcularTroco();
 		}
 		else
 		{
 			if (radPagtoMaster.isChecked())
 			{
 				formaPagto = 2;
 			}
 			else
 			{
 				formaPagto = 3;
 			}
 			Pedido.getInstancia().TrocoPara = 0.0;
 			trocoValido = true;
 		}
 		
 		Pedido.getInstancia().FormaPagto = formaPagto;
 	}
 	
 	private void CalcularTroco()
 	{
 		try
 		{
 			double valorTotal = Pedido.getValorTotal(), trocoPara, valorTroco;
 			TextView txtEdtTrocoPara = (TextView) findViewById(R.id.txtEdtTrocoPara);
 			TextView txtEdtValorTroco = (TextView) findViewById(R.id.txtEdtValorTroco);
 			
 			trocoPara = Double.parseDouble(txtEdtTrocoPara.getText().toString());
 			Pedido.getInstancia().TrocoPara = trocoPara;
 			valorTroco = trocoPara - valorTotal;
 			
 			txtEdtValorTroco.setText(String.format("%.2f", valorTroco));
 			trocoValido = (valorTroco >= 0);
 		}
 		catch (Exception e)
 		{
 			trocoValido = false;
 		}
 	}
 	
 	public void showToast(final String toast)
 	{
 	    runOnUiThread(new Runnable() {
 	        public void run()
 	        {
 	            Toast.makeText(PagamentoActivity.this, toast, Toast.LENGTH_SHORT).show();
 	        }
 	    });
 	}
 	
 	public void sucesso()
 	{
     	Intent k = new Intent(this, PedidoRealizadoActivity.class);
     	startActivity(k);
     	this.finish();
 	}
 	
 	private void PostarPedido()
 	{	
 		if (trocoValido == false)
 		{
 			Toast.makeText(getApplicationContext(), "Valor de troco invlido!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		Thread t = new Thread()
 		{
             public void run()
             {
             	Looper.prepare();
         		Gson gson = new Gson();
         		String json = gson.toJson(Pedido.getInstancia());
 
                 try
                 {
                     DefaultHttpClient client = new DefaultHttpClient();
                     HttpPost post = new HttpPost("http://192.168.1.101/DeliveryService/api/pedido");
                     StringEntity se = new StringEntity(json);
                     post.setEntity(se);
                     post.setHeader("Accept", "application/json");
                     post.setHeader("Content-type", "application/json");
                     HttpResponse resp = client.execute(post);
                     
                     if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
                     {
                     	showToast("Pedido realizado com sucesso!");
                     	sucesso();
                     }
                     else
                     {
                    	showToast("Erro ao postar pedido!");
                     }
                     
                     Looper.myLooper().quit();
                 }
                 catch(Exception e)
                 {
                	showToast("Falha ao postar pedido!");
                 }
                 
                 Looper.loop();
             }
         };
 
         t.start();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.pagamento, menu);
 		return true;
 	}
 
 }
