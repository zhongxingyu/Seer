 package br.com.bitwaysystem.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import br.com.bitwaysystem.bean.Entity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import br.com.bitwaysystem.bean.ShowCustomerCreditInformation;
 import br.com.bitwaysystem.service.RestMethods;
 import br.com.bitwaysystem.util.Connection;
 import br.com.bitwaysystem.util.FormatCNPJorCPF;
 import com.example.customermanagerjdejson.R;
 
 public class GetCustomerCreditInfo extends Activity implements
 		View.OnClickListener {
 
 	// ! ID of the progress dialog.
 	private final int DIALOG_PROGRESS = 1;
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			
 			this.onBackPressed();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setTitle("Mensagem");
 		
 		alertDialogBuilder
 				.setMessage("Deseja realmente sair?")
 				.setCancelable(false)
 				.setPositiveButton("Sim",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								finish();
 							}
 						})
 				.setNegativeButton("Não",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		super.onMenuItemSelected(featureId, item);
 
 		switch (item.getItemId()) {
 
 		case R.id.iSair:
 			finish();
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.layout);
 
 		Button ok = (Button) findViewById(R.id.button1);
 		ok.setOnClickListener(this);
 	}
 
 	/**
 	 * Called by the framework when whenever the Activity needs to provide it
 	 * with a Dialog instance. The progress dialog is the only one provided by
 	 * this Activity.
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		if (id == DIALOG_PROGRESS) {
 			// String message = getString(R.string.consulta_btn);
 			ProgressDialog dialog = new ProgressDialog(this);
 			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			dialog.setMessage("Consultando");
 			dialog.setCancelable(false);
 			return dialog;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		super.onCreateOptionsMenu(menu);
 		MenuInflater blowUp = getMenuInflater();
 		blowUp.inflate(R.menu.down_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		if (Connection.conectado(getBaseContext())) {
 			TextView idCliente = (TextView) findViewById(R.id.txt_idCliente);
 
 			if (idCliente.getText().toString().equals("")) {
 				this.camposObrigatorios("Id Cliente é obrigatório");
 			} else {
 				GetCustomerCreditInfoTask task = new GetCustomerCreditInfoTask();
 				task.execute(idCliente.getText().toString());
 			}
 
 		} else {
 
 			Toast.makeText(getApplicationContext(),
 					"Sem conexão com a Internet", Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	/**
 	 * The AsyncTask responsible for fetching the currency conversion rate. We
 	 * do this as an AsyncTask so we don't block the UI while the web service is
 	 * being called.
 	 */
 	private class GetCustomerCreditInfoTask extends
 			AsyncTask<String, Void, ShowCustomerCreditInformation> {
 		/**
 		 * Runs on the UI thread before doInBackground(Params...). We take this
 		 * opportunity to clear out any previous result data and throw up our
 		 * progress dialog.
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onPreExecute() {
 			showDialog(DIALOG_PROGRESS);
 		}
 
 		/**
 		 * Runs on the UI thread after cancel(boolean) is invoked and
 		 * doInBackground(Object[]) has finished. We simply dismiss the progress
 		 * dialog.
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onCancelled() {
 			dismissDialog(DIALOG_PROGRESS);
 		}
 
 		/**
 		 * Runs on the UI thread after doInBackground(Params...) has completed.
 		 * This is where we dismiss the progress dialog and update the result
 		 * TextView.
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onPostExecute(ShowCustomerCreditInformation result) {
 			dismissDialog(DIALOG_PROGRESS);
 
 			if (m_error != null) {
 				Toast.makeText(GetCustomerCreditInfo.this, m_error,
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			if (result != null) {
 				TextView limiteDeCredito = (TextView) findViewById(R.id.txt_Limite);
 				TextView cnpjOucpf = (TextView) findViewById(R.id.txt_CNPJCPF);
 				TextView pedidoAberto = (TextView) findViewById(R.id.txt_PedidoAberto);
 
 				limiteDeCredito.setText("");
 				cnpjOucpf.setText("");
 				pedidoAberto.setText("");
 
 				limiteDeCredito.setText(String.valueOf(result
 						.getAmountCreditLimit()));
 
 				if (!String.valueOf(result.getEntity().getEntityTaxId())
 						.equals("")) {
 
 					String cnpjOucpfformat;
 
 					if (String.valueOf(result.getEntity().getEntityTaxId())
 							.length() == 11) {
 						cnpjOucpfformat = FormatCNPJorCPF.formatarCpf(String
 								.valueOf(result.getEntity().getEntityTaxId()));
 					} else {
 						cnpjOucpfformat = FormatCNPJorCPF.formatarCnpj(String
 								.valueOf(result.getEntity().getEntityTaxId()));
 					}
 
 					cnpjOucpf.setText(cnpjOucpfformat);
 				}
 
 				if (!String.valueOf(result.getAmountTotalExposure()).equals("")) {
 					pedidoAberto.setText(String.valueOf(result
 							.getAmountTotalExposure()));
 				}
 				
				if (!String.valueOf(result.getErrorCodeBea()).equals("BEA-380001")) {
 					Toast.makeText(getApplicationContext(),
 							"Id do cliente não existe no Cadastro Geral ", Toast.LENGTH_LONG)
 							.show();
 				}
 				
 				
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"Falha de conexão com o servidor", Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 
 		/**
 		 * Performs the web service invocation on a separate thread.
 		 * 
 		 * The first parameter in params should be the "fromCurrency" value. The
 		 * second parameter in params should be the "toCurrency" value.
 		 */
 		@Override
 		protected ShowCustomerCreditInformation doInBackground(String... params) {
 			if ((params == null))
 				return null; // Shouldn't happen.
 
 			Entity entity = new Entity(Integer.parseInt(params[0]));
 
 			ShowCustomerCreditInformation customer = new ShowCustomerCreditInformation(
 					entity);
 
 			customer = RestMethods.showCredit(customer);
 
 			return customer;
 		}
 
 		// ! Error message
 		private String m_error = null;
 	}
 
 	public void camposObrigatorios(String mensagem) {
 		AlertDialog.Builder m = new AlertDialog.Builder(this);
 		m.setTitle("Aviso");
 		m.setMessage(mensagem);
 		m.setPositiveButton("OK", null);
 		m.show();
 	}
 
 }
