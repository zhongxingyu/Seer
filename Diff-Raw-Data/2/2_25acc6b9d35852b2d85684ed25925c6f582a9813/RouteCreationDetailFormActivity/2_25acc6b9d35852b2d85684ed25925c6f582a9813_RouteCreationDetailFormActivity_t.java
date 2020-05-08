 package jp.knct.di.c6t.ui.route;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.BasicClient;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.ui.HomeActivity;
 import jp.knct.di.c6t.util.ActivityUtil;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import android.accounts.NetworkErrorException;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 public class RouteCreationDetailFormActivity extends Activity implements OnClickListener {
 
 	private Route mRoute;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route_creation_detail_form);
 
 		mRoute = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_ROUTE);
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.route_creation_detail_form_cancel,
 				R.id.route_creation_detail_form_ok,
 		});
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.route_creation_detail_form_cancel:
 			setResult(RESULT_CANCELED);
 			finish();
 			break;
 
 		case R.id.route_creation_detail_form_ok:
 			setDetailsFromEditForms(mRoute);
 			BasicClient client = new BasicClient();
 			mRoute.setUser(client.getUserFromLocal(this));
 			if (mRoute.isValid()) {
 				// TODO: display alert dialog
 				new PostingTask().execute();
 			}
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void setDetailsFromEditForms(Route route) {
 		ActivityUtil getter = new ActivityUtil(this);
 		String name = getter.getText(R.id.route_creation_detail_form_name);
 		String description = getter.getText(R.id.route_creation_detail_form_description);
 		route.setName(name);
 		route.setDescription(description);
 	}
 
 	private class PostingTask extends AsyncTask<Void, String, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			BasicClient client = new BasicClient();
 			HttpResponse response = null;
 			try {
 				int id = client.postRoute(mRoute).getId();
 				mRoute.setId(id);
 			}
 			catch (ClientProtocolException e) {
 				publishProgress(e.getLocalizedMessage());
 				e.printStackTrace();
 				cancel(true);
 				return null;
 			}
 			catch (JSONException e) {
 				publishProgress(e.getLocalizedMessage());
 				e.printStackTrace();
 				cancel(true);
 				return null;
 			}
 			catch (IOException e) {
 				publishProgress(e.getLocalizedMessage());
 				e.printStackTrace();
 				cancel(true);
 				return null;
 			}
 			catch (NetworkErrorException e) {
 				publishProgress(e.getLocalizedMessage());
 				e.printStackTrace();
 				cancel(true);
 				return null;
 			}
 			catch (ParseException e) {
 				publishProgress(e.getLocalizedMessage());
 				e.printStackTrace();
 				cancel(true);
 				return null;
 			}
 
 			publishProgress("[g쐬܂");
 
 			for (int i = 0; i <= 4; i++) {
 				try {
 					response = client.putQuestImage(mRoute, i);
 				}
 				catch (ClientProtocolException e) {
 					publishProgress(e.getLocalizedMessage());
 					e.printStackTrace();
 					cancel(true);
 					return null;
 				}
 				catch (IOException e) {
 					publishProgress(e.getLocalizedMessage());
 					e.printStackTrace();
 					cancel(true);
 					return null;
 				}
 
 				int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode < 300) {
					publishProgress("NGXgC[WAbv[h܂ (" + (i + 1) + "/5)");
 				}
 				else {
 					publishProgress("error: " + statusCode);
 					cancel(true);
 					return null;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... progressText) {
 			Toast.makeText(getApplicationContext(), progressText[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(Void results) {
 			Toast.makeText(getApplicationContext(), "[g̍쐬܂", Toast.LENGTH_SHORT).show();
 			startActivity(new Intent(RouteCreationDetailFormActivity.this, HomeActivity.class)
 					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 		}
 	}
 }
