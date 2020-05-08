 package br.com.amigooculto;
 
 import java.util.List;
 
 import br.com.amigooculto.business.Amigo;
 import br.com.amigooculto.business.Sorteio;
 import br.com.amigooculto.validations.Validate;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Principal extends Activity implements OnClickListener {
 	
 	private Button btnAdicionarAmigo;
 	private EditText edtNomeAmigo;
 	private EditText edtEMailAmigo;
 	private EditText edtTelefoneAmigo;
 	private Sorteio sorteio = new Sorteio();
 	private static final int REQUEST_CODE = 10;
 	private String email = null;
 	private String senha = null;
 	private Validate validar;
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         edtNomeAmigo = (EditText)this.findViewById(R.id.editNomeAmigo);
         edtNomeAmigo.setText(null);
         
         edtEMailAmigo = (EditText)this.findViewById(R.id.editEmailAmigo);
         edtEMailAmigo.setText(null);
         
         edtTelefoneAmigo = (EditText)this.findViewById(R.id.editTelefoneAmigo);
         edtTelefoneAmigo.setText(null);
         
         btnAdicionarAmigo = (Button)this.findViewById(R.id.btnAdicionarAmigo);
         btnAdicionarAmigo.setOnClickListener(this);
     }
         
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }     
     
     public void adicionarAmigo()
     {
     	try
     	{
 	    	Amigo amigo = new Amigo();
 	    	amigo.setNome(edtNomeAmigo.getText().toString());
 	    	amigo.setTelefone(edtTelefoneAmigo.getText().toString());
 	    	amigo.setEMail(edtEMailAmigo.getText().toString());
 	    	
 	    	this.validar = new Validate(amigo);
 		
 			this.sorteio.addAmigo(amigo);
 			
 			Toast toast = Toast.makeText(getApplicationContext(), R.string.msgUserAdded, Toast.LENGTH_SHORT);
 			toast.show();
 			
 			Log.i("ao", "Amigo adicionado na lista: "+amigo.getNome());
 			
 			limparEdit();
     	}
     	catch (Exception e) {
     		Log.w ("ao", e.getMessage());
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
     }
     
     public void limparEdit()
     {
 		edtNomeAmigo.setText(null);
 		edtEMailAmigo.setText(null);
 		edtTelefoneAmigo.setText(null);
     }
     
     public void sortear(){
     	sorteio.sortearNaTela();
     	
     	CheckBox chkReperir = (CheckBox)this.findViewById(R.id.chkRepetirAmigos);
 		
 		sorteio.repetirAmigo(chkReperir.isChecked());
 		Amigo amigoSorteado = sorteio.sortear();
 	
 		if (amigoSorteado==null) {
 			Toast toast = Toast.makeText(getApplicationContext(), R.string.erroSorteio, Toast.LENGTH_SHORT);
 			toast.show();			
 			
 			Log.e("ao",getString(R.string.erroSorteio));
 		}
 		else {
 				Toast toast = Toast.makeText(getApplicationContext(), R.string.msgAmigoSorteado, Toast.LENGTH_SHORT);
 				toast.show();
 				toast = Toast.makeText(getApplicationContext(), amigoSorteado.getNome(), Toast.LENGTH_LONG);
 				toast.show();
 				
 				Log.i("ao",getString(R.string.msgAmigoSorteado)+" "+amigoSorteado.getNome());
 				
 				if (!sorteio.isSortAvaliable()) {
 					toast = Toast.makeText(getApplicationContext(), R.string.sortEnd, Toast.LENGTH_LONG);
 					toast.show();
 					
 					Log.i("ao",getString(R.string.sortEnd));
 				}
 		}
     }
     
     public void limparLista() {
     	sorteio.clearList();
     	Toast toast = Toast.makeText(getApplicationContext(), R.string.msgCleanList, Toast.LENGTH_SHORT);
 		toast.show();
 		
 		Log.i("ao",getString(R.string.msgCleanList));
     }
     
 	public void onClick(View v) {
 		
 		switch (v.getId()){
 			case R.id.btnAdicionarAmigo:adicionarAmigo();break;
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId()){
 			case R.id.menuLimparLista:limparLista();
 			break;
 			case R.id.menuSMS :sortearPorSMS();
 			break;
 			case R.id.menuSortear:sortear();
 			break;
 			case R.id.menuEMail:try {
 				setEMail();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				Log.e("ao",e.getMessage());
 				Toast.makeText(this, "Ocorreu um erro ao enviar e-mail",Toast.LENGTH_LONG);
 			}
 			break;
 			case R.id.menuAddContato:lista();
 			break;
 			case R.id.menuSobre:sobre();
 			break;
 		}
 		return true;
 	}
 	
 	public void sortearPorSMS(){
 		Log.i("ao","Sorteando por SMS");
 		
 		sorteio.sortearPorSMS();
 		
 		while (sorteio.isSortAvaliable()) {
 			List<Amigo> amigos = sorteio.sortearSMSEMail();
 			if (amigos.size()>0) {
 				mensagemSMS(amigos.get(0),amigos.get(1));
 			}
 		}	
 		
 		Toast toast = Toast.makeText(getApplicationContext(), R.string.sortEnd, Toast.LENGTH_LONG);
 		toast.show();
 		
 		Log.i("ao",getString(R.string.sortEnd));
 	}
 	
 	public void sortearPorEMail() {
 		try {
 			Log.i("ao","Sorteando por E-Mail");
 			
 			sorteio.sortearPorEMail();
 		
 			ProgressDialog dialog = null;
 			dialog = ProgressDialog.show(this, getString(R.string.sorteioEmail),getString(R.string.sortingFriends), true);
 			while (sorteio.isSortAvaliable()) {			
 				List<Amigo> amigos = sorteio.sortearSMSEMail();
 				if (amigos.size()>0) {
 					email(amigos.get(0),amigos.get(1));
 				}
 			}	
 		
 			dialog.hide();
 			dialog.dismiss();
 			
 			Toast toast = Toast.makeText(getApplicationContext(), R.string.sortEnd, Toast.LENGTH_LONG);
 			toast.show();
 		
 			Log.i("ao",getString(R.string.sortEnd));
 		}
 		catch(Exception e)
 		{
 			Log.e("ao", e.getMessage());		
 		}
 	}
 	
 	public void mensagemSMS(Amigo amigo,Amigo amigoSoreteado) {
 		// AndroidManifest.xml must have the following permission:
 		// <uses-permission android:name="android.permission.SEND_SMS"/>
 		
		try
		{
			SmsManager m = SmsManager.getDefault();
			
			String destinationNumber = amigo.getTelefone();  
			String text = getString(R.string.ola)+" "+amigo.getNome()+", \n"+getString(R.string.amigoSorteado)+" "+amigoSoreteado.getNome()+"\n"+getString(R.string.byLagix);
			
			m.sendMultipartTextMessage(destinationNumber, null, m.divideMessage(text), null,null);
		}
		catch (Exception e)
		{
			Log.e("ao", e.getMessage()+"\n"+e.getStackTrace());
		}
 	}
 	
 	public void setEMail() throws Exception 
 	{
 		AccountManager mgr = AccountManager.get(this); 
 		
 		Account[] accounts = mgr.getAccountsByType("com.google");
 		
 		if(!(accounts.length>0)) 
 		{
 			throw new Exception(getString(R.string.msgAccountNotFound));
 		}
 		else {
 			for (Account account : accounts) {
 				
 				if (account.name.contains("gmail")) 
 				{
 					email = account.name;	
 					
 					AlertDialog.Builder alert = new AlertDialog.Builder(this);
 					final EditText input = new EditText(this);
 					alert.setView(input);
 					alert.setPositiveButton("Enviar", new DialogInterface.OnClickListener() 
 					{
 						public void onClick(DialogInterface dialog, int whichButton) 
 						{
 							senha = input.getText().toString().trim();
 							
 							sortearPorEMail();
 						}
 					});
 
 					alert.setNegativeButton("Cancelar",
 							new DialogInterface.OnClickListener() 
 					{
 								public void onClick(DialogInterface dialog, int whichButton) 
 								{
 									dialog.dismiss();
 									dialog.cancel();
 								}
 							});
 				
 					alert.show();
 					
 				}
 			}
 			
 			if (email.length()<=0) {
 				throw new Exception(getString(R.string.msgAccountNotFound));
 			}
 		}
 	}
 	
 	public void email(Amigo amigo,Amigo amigoSoreteado) {
 		String text = getString(R.string.ola)+" "+amigo.getNome()+", \n"+getString(R.string.amigoSorteado)+" "+amigoSoreteado.getNome()+"\n"+getString(R.string.byLagix);
 		
 		try {   
 			Log.i("ao","Enviando E-Mail para: "+amigo.getEMail().toString());
 			
             GMailSender sender = new GMailSender(email, senha);
             sender.sendMail("Amigo Oculto",text,   
                     email,   
                     amigo.getEMail().toString());   
         } catch (Exception e) {   
         	Log.e("ao", e.getMessage());
         }
 	}
 	
 	public void lista() {
 		Intent i = new Intent(this, Lista.class);
 		startActivityForResult(i, REQUEST_CODE);  
 	}
 	
 	public void sobre(){
 		Intent i = new Intent(this, Sobre.class);  
 		startActivity(i);
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		try{
 			if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
 				if (data.hasExtra("returnNome")) {
 					edtNomeAmigo.setText(data.getExtras().getString("returnNome"));
 					
 					if (!data.getExtras().getString("returnEMail").equals("")) {
 						edtEMailAmigo.setText(data.getExtras().getString("returnEMail"));
 					} else {
 						edtEMailAmigo.setText("-");
 					}
 					
 					if (!data.getExtras().getString("returnTelefone").equals("")) {
 						edtTelefoneAmigo.setText(data.getExtras().getString("returnTelefone").replace("-", ""));
 					} else {
 						edtTelefoneAmigo.setText("-");
 					}
 				}
 			}
 		} catch (Exception e){
 			Log.e("ao", e.getMessage());
 		}
 	}
 }
