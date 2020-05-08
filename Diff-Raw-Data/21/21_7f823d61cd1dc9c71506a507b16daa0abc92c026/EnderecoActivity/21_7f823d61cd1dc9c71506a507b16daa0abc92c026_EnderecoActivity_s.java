 package com.example.pizzadelivery;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EnderecoActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_endereco);
 		
 		Button btnConfirmarEndereco = (Button) findViewById(R.id.btnConfirmarEndereco);
 		btnConfirmarEndereco.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				ConfirmarEndereco();				
 			}
 		});
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		Intent k = new Intent(this, PedidoActivity.class);		
 		startActivity(k);
 		this.finish();
 	}
 	
 	private void ConfirmarEndereco()
 	{
 		TextView txtNomeCliente = (TextView) findViewById(R.id.txtNomeCliente);
 		TextView txtTelefone = (TextView) findViewById(R.id.txtTelefone);
 		TextView txtEndereco = (TextView) findViewById(R.id.txtEndereco);
 		TextView txtBairro = (TextView) findViewById(R.id.txtBairro);
 		
 		if (txtNomeCliente.getText().length() == 0)
 		{
 			Toast.makeText(getApplication(), "Digite seu nome!", Toast.LENGTH_SHORT).show();
 		}
 		else if (txtTelefone.getText().length() == 0)
 		{
 			Toast.makeText(getApplication(), "Digite seu telefone!", Toast.LENGTH_SHORT).show();
 		}
 		else if (txtEndereco.getText().length() == 0)
 		{
 			Toast.makeText(getApplication(), "Digite seu endereo!", Toast.LENGTH_SHORT).show();
 		}
 		else if (txtBairro.getText().length() == 0)
 		{
 			Toast.makeText(getApplication(), "Digite seu bairro!", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			Pedido p = Pedido.getInstancia();
 			p.NomeCliente = txtNomeCliente.getText().toString();
 			p.Telefone = txtTelefone.getText().toString();
 			p.Endereco = txtEndereco.getText().toString();
 			p.Bairro = txtBairro.getText().toString();
 			
 			Intent k = new Intent(this, PagamentoActivity.class);
 			startActivity(k);
 			this.finish();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		return true;
 	}
 
 }
