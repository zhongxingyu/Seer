 package br.uff.protesteja;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 import br.uff.utils.HTTPUtils;
 
 public class ShowPostsActivity extends Activity {
 
 	private ListView lstProtestos;
 	private JSONObject itemSelected;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_show_posts);
 
 		lstProtestos = (ListView) findViewById(R.id.lst_protestos);
 		new Getar().execute();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.show_posts, menu);
 		return true;
 	}
 
 	public void atualizar(View v) {
 		new Getar().execute();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		new Getar().execute();
 	}
 
 	private class Getar extends AsyncTask<Void, Void, JSONObject[]> {
 		ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(ShowPostsActivity.this);
 			dialog.show();
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject[] result) {
 			dialog.dismiss();
 			String[] protestos = new String[result.length];
 			final JSONObject[] items = result;
 			for (int i = 0; i < result.length; i++) {
 				JSONObject protesto = result[i];
 				try {
 					String pessoa = protesto.getString("pessoa");
 					String descricao = protesto.getString("descricao");
 					protestos[i] = pessoa + " - " + descricao;
 				} catch (JSONException e) {
 					protestos = null;
 				}
 			}
 			if (protestos != null) {
 				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 						getBaseContext(), android.R.layout.simple_list_item_1,
 						protestos);
 				lstProtestos.setAdapter(adapter);
 				if (adapter.isEmpty())
 					Toast.makeText(getApplicationContext(), "Lista vazia",
 							Toast.LENGTH_LONG).show();
 				else {
 					lstProtestos
 							.setOnItemClickListener(new OnItemClickListener() {
 
 								@Override
 								public void onItemClick(AdapterView<?> arg0,
 										View arg1, int arg2, long arg3) {
 									itemSelected = items[arg2];
 									showMyDialog();
 								}
 							});
 				}
 			} else
 				Toast.makeText(getApplicationContext(), "Erro na conexão",
 						Toast.LENGTH_LONG).show();
 
 		}
 
 		@Override
 		protected JSONObject[] doInBackground(Void... params) {
 			try {
 				String url = "https://droid-list.herokuapp.com/protestos.json";
 				String conteudo = HTTPUtils.acessar(url);
 				JSONArray resultados = new JSONArray(conteudo);
 
 				JSONObject[] protestos = new JSONObject[resultados.length()];
 
 				for (int i = 0; i < resultados.length(); i++) {
 					protestos[i] = resultados.getJSONObject(i);
 				}
 				return protestos;
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 	}
 
 	protected void showMyDialog() {
 		final Dialog dbox = new Dialog(ShowPostsActivity.this);
 		dbox.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dbox.setContentView(R.layout.dialog);
 		Button btnUpd = (Button) dbox.findViewById(R.id.btn_update);
 		Button btnDel = (Button) dbox.findViewById(R.id.btn_delete);
 		Button btnCanc = (Button) dbox.findViewById(R.id.btn_cancel);
 
 		btnUpd.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				dbox.dismiss();
 				Log.e("INFO", (itemSelected == null) ? "itemSelected null"
 						: "itemSelected OK!");
 				Intent it = new Intent(ShowPostsActivity.this,
 						UpdateActivity.class);
 				it.putExtra("itemId", itemSelected.toString());
				ShowPostsActivity.this.startActivity(it);
 			}
 		});
 
 		btnDel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				dbox.dismiss();
 				new Deletar().execute();
 			}
 		});
 
 		btnCanc.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				dbox.dismiss();
 			}
 		});
 
 		dbox.show();
 	}
 
 	private class Deletar extends AsyncTask<Void, Void, Boolean> {
 		ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(ShowPostsActivity.this);
 			dialog.show();
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			dialog.dismiss();
			Toast.makeText(ShowPostsActivity.this, ((boolean)result)?"Sucesso!":"Erro!", Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			try {
 				return HTTPUtils.deletar(itemSelected.getString("url"));
 
 			} catch (Exception e) {
 				return false;
 			}
 		}
 
 	}
 
 }
