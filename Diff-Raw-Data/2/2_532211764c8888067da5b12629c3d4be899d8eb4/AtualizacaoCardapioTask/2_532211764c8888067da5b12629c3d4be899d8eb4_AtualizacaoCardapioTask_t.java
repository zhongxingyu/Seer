 package br.com.jp.cardapionamao;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import br.com.jp.cardapionamao.model.Cardapio;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class AtualizacaoCardapioTask extends AsyncTask<Void, Void, Cardapio>{
 
 	private final ProgressDialog progressDialog;
 	private final String requestURL;
 	private final MostrarCardapioActivity mostrarCardapioActivity;
 	private boolean deuErro = false;
 
 	public AtualizacaoCardapioTask(MostrarCardapioActivity mostrarCardapioActivity, ProgressDialog progressDialog, String requestURL)
 	{
 		this.mostrarCardapioActivity = mostrarCardapioActivity;
 		this.progressDialog = progressDialog;
 		this.requestURL = requestURL;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		progressDialog.show();
 	}
 	
 	@Override
 	protected void onPostExecute(Cardapio cardapio) {
 		mostrarCardapioActivity.atualizarCampos(cardapio);
 		progressDialog.dismiss();
 		
 		if(deuErro) mostrarCardapioActivity.mostrarMensagemErro();
 	}
 	
 	@Override
 	protected Cardapio doInBackground(Void... arg0) {
 		
		deuErro = false;
		
 		HttpClient httpClient = new DefaultHttpClient();
 		try {
 			HttpResponse response = httpClient.execute(new HttpGet(requestURL));
 			
 			InputStream content = response.getEntity().getContent();
 			
 			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
 			Cardapio cardapio = gson.fromJson(new InputStreamReader(content), Cardapio.class);
 			
 			return cardapio;
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			progressDialog.dismiss();
 			deuErro = true;
 		}
 		
 		return new Cardapio();
 	}
 }
