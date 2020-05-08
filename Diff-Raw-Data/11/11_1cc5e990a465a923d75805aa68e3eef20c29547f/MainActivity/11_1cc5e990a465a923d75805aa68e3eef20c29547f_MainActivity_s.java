 package com.lab.akatuslibtest;
 
 import java.io.InputStream;
 
 import com.lib.akatusmobile.AkatusAuthTemplate;
 import com.lib.akatusmobile.AkatusInstallmentTemplate;
 import com.lib.akatusmobile.AkatusReaderInterface;
 import com.lib.akatusmobile.AkatusReaderListener;
 import com.lib.akatusmobile.AkatusTransactionTemplate;
 import com.lib.akatusmobile.AuthRequest;
 import com.lib.akatusmobile.AuthResponse;
 import com.lib.akatusmobile.InstallmentsResponse.ParcelaInfo;
 import com.lib.akatusmobile.Payer;
 import com.lib.akatusmobile.TransactionInvalidException;
 import com.lib.akatusmobile.TransactionRequest;
 import com.lib.akatusmobile.TransactionResponse;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 public class MainActivity extends Activity implements AkatusReaderInterface, OnClickListener{
 	
 	private EditText txtLogin, txtPassword;
 	private TextView lblReaderStatus, lblConfig, lblTransaction;
 	private Button btnConfig, btnSwipe, btnLogin, btnSend, btLogoff, btInstallments;
 	private TransactionRequest transaction;
 	private AuthResponse user;
 	private AkatusReaderListener reader;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		lblReaderStatus = (TextView)findViewById(R.id.lbl_reader_status);
 		lblConfig = (TextView)findViewById(R.id.lbl_config_status);
 		lblTransaction = (TextView)findViewById(R.id.lblTransacao);
 		btnConfig = (Button) findViewById(R.id.btn_config);
 		btnSwipe = (Button) findViewById(R.id.btn_swipe);
 		btnLogin = (Button) findViewById(R.id.btn_login);
 		btnSend = (Button) findViewById(R.id.bt_send);
 		btLogoff = (Button) findViewById(R.id.btn_logoff);
 		btInstallments = (Button) findViewById(R.id.btn_installments);
 		txtLogin = (EditText)findViewById(R.id.txt_login);
 		txtPassword = (EditText)findViewById(R.id.txt_password);
 		
 		btnConfig.setOnClickListener(this);
 		btnSwipe.setOnClickListener(this);		
 		btnLogin.setOnClickListener(this);		
 		btnSend.setOnClickListener(this);		
 		btLogoff.setOnClickListener(this);
 		btInstallments.setOnClickListener(this);
 		
 	}
 
 	@Override
 	public void onClick(View v) {
 		try{
 			if( v == btnConfig){
 				reader = new AkatusReaderListener(this, this);
 				if(!reader.isConfigured())
 					reader.startConfig();
 				else
 					reader.connectWithProfile();
 			}else if(v== btnSwipe){
 				if(reader != null)
 					reader.startCardReading();
 			}else if(v == btnLogin){
 				String email = txtLogin.getText().toString();
 				String senha = txtPassword.getText().toString();
 				
 				AuthRequest req = new AuthRequest(email,senha,null,new double[]{1234,1234});
 				
 				user = new AkatusAuthTemplate(true).login(req);
 				if(user != null && user.getReturn_code() == 0 && user.getToken() != null && user.getToken().trim().length() > 0){
 					fillTransaction();
 					transaction.setToken(user.getToken());
 					lblTransaction.setText("Transaction: "+transaction.toString());
 					btLogoff.setVisibility(Button.VISIBLE);
 					btInstallments.setVisibility(Button.VISIBLE);
 				}else{
 					showToast("Login e/ou senha inválidos");
 				}
 			}else if(v == btnSend){
 				sendAkatusTransaction();
 			}else if(v == btLogoff){
 				user = null;
 				txtLogin.setText("");
 				txtPassword.setText("");
 				lblTransaction.setText("");
 				btLogoff.setVisibility(Button.INVISIBLE);
 				btInstallments.setVisibility(Button.INVISIBLE);
 			}else if(v == btInstallments){
 				showInstallmentsDialog();
 			}
 		}catch(Exception e){
 			showToast(e.toString());
 		}
 	}
 
 	@Override
 	public void handleAutoCfgStatus(final int status, final String percent) {
 		runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				switch (status) {
 				case AkatusReaderListener.AUTO_CFG_STARTED:
 					// Show a progress dialog: "Configuring reader..."
 					lblConfig.setText("Auto Config Started");
 					break;
 				case AkatusReaderListener.AUTO_CFG_PROGRESS:
 					// Change the progress message using "percent" parameter
 					lblConfig.setText("Auto Config Progress: "+percent);
 					break;
 				case AkatusReaderListener.AUTO_CFG_FINISHED:
 					// Dismiss the progress and wait for handleReaderStatus() method
 					lblConfig.setText("Auto Config Sucess!");
 					break;
 				case AkatusReaderListener.AUTO_CFG_FAILED:
 					// Ask the user if should run auto-config again
 					lblConfig.setText("Auto Config Failed!");
 					break;
 				}
 			}
 		});
 	}
 
 	@Override
 	public void handleCardSwiped(final byte[] data) {
 		// Set the "track1" attribute for your TransactionRequest instance
 		runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 				    String dataString = new String(data);
 				    String cardNo = dataString.substring(dataString.indexOf(";")+1, dataString.indexOf("="));
 				    // A varíavel cardNo agora representa o numero do cartão mascarado com '*'
 
 				    lblReaderStatus.setText(cardNo);
 				    if(transaction != null)
 				    	transaction.setTrack1(dataString);
 
 				} catch (IndexOutOfBoundsException e) {
 				    // Os dados não foram lidos corretamente, passe o cartão novamente
 					showToast("Passe o cartão novamente");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void handleReaderStatus(final int status) {
 		runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 
 		switch (status) {
 		case AkatusReaderListener.READER_CONNECTING:
 			// Alert the user to wait to pass a card
 			lblReaderStatus.setText("CONNECTING");
 			break;
 		case AkatusReaderListener.READER_CONNECTED_NO_PROFILE:
 			// Store this in your app and use reader.startConfig() futurelly for listen the reader 
 			lblReaderStatus.setText("CONNECTED");
 			break;
 		case AkatusReaderListener.READER_CONNECTED:
 			// A good place to call reader.startCardReading() method			
 			lblReaderStatus.setText("CONNECTED");
 			break;
 		case AkatusReaderListener.READER_DISCONNECTED:
 			// Alert the user to connect the reader
 			lblReaderStatus.setText("DISCONNECTED");
 			break;
 		}
 			}
 		});
 	}
 	
 	private void fillTransaction(){
 		try {
 			transaction = new TransactionRequest(this);
 			
			transaction.setAmount("20.0");
 
 
			transaction.setCard_number("5899703016106199");
 			transaction.setCvv("123");
 			transaction.setDescription("ITEM1");
			transaction.setExpiration("201309");
 			transaction.setGeolocation(new double[]{1234,1234});
			transaction.setHolder_name("CLIENT");
 			transaction.setToken(new AuthResponse().getToken());
 
 			InputStream sig_is = getResources().openRawResource(R.raw.assinatura);
 			byte[] sig = new byte[sig_is.available()];
 			sig_is.read(sig);
 			transaction.setSignatureBytes(sig);
 			
 			InputStream photo_is = getResources().openRawResource(R.raw.produto);
 			byte[] photo = new byte[photo_is.available()];
 			photo_is.read(photo);
 			transaction.setPhotoBytes(photo);
 			
 			Payer p = new Payer();
 			p.setCpf("38974440890");
 			p.setEmail("a@a.com");
 			p.setName("TESTE TRANSACTION");
 			p.setPhone("11987654321");
 			transaction.setPayer(p);
 			
 			lblTransaction.setText("Transaction: "+transaction.toString());
 		} catch (Exception e) {
 			showToast(e.toString());
 		}
 	}
 	
 	void showInstallmentsDialog() throws Exception{
 		String email = txtLogin.getText().toString();
 		String apiKey = user.getApi_key();
 		double value = Double.parseDouble(transaction.getAmount());			
 
 		final ParcelaInfo[] resp = new AkatusInstallmentTemplate(true).getInstallmentsList(email, apiKey, value);
 		String[] parcelasLabels = new String[resp.length];
 		for(int i = 0; i < resp.length; i++){
 			parcelasLabels[i] = resp[i].getQuantidade() + " X " + resp[i].getValor();
 		}
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setItems(parcelasLabels, new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int position) {
 				transaction.setInstallments(resp[position].getQuantidade()+"");
 				dialog.dismiss();
 				lblTransaction.setText("Transaction: "+transaction.toString());
 			}
 		});
 		builder.create().show();
 	}
 	
 	private void sendAkatusTransaction(){
 		try {	
 			if(transaction == null){
 				showToast("Efetue o login");
 				return;
 			}
 			AkatusTransactionTemplate ws = new AkatusTransactionTemplate(true);
 			TransactionResponse response = ws.postTransaction(transaction);
 			
 			if(response.getReturn_code() == AkatusTransactionTemplate.TRANSACTION_OK)
 				showToast("Transaction accepted! "+response.getMessage());
 			else
 				showToast("Transaction recused! "+response.getMessage());
 		} catch (TransactionInvalidException e) {
 			showToast(e.toString());
 		} catch (Exception e) {
 			showToast(e.toString());
 		}
 	}
 	
 	private void showToast(String message){
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 }
