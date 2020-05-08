 package sweetpi.picontrol.webview;
 
 import sweetpi.picontrol.R;
 
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 
 import sweetpi.picontrol.CertHelper;
 import sweetpi.picontrol.PiControlActivity;
 import sweetpi.picontrol.SettingsActivity;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.http.SslCertificate;
 import android.net.http.SslError;
 import android.util.Log;
 import android.webkit.HttpAuthHandler;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 final public class PiWebViewClient extends WebViewClient {
 	/**
 	 * 
 	 */
 	private final PiControlActivity piControlActivity;
 	private final WebView mainWebView;
 	private X509Certificate ownCert;
 	private ProgressDialog dialog;
 	private AlertDialog certDialog;
 
 	public PiWebViewClient(PiControlActivity piControlActivity, WebView mainWebView) {
 		this.piControlActivity = piControlActivity;
 		this.mainWebView = mainWebView;
 		final SharedPreferences settings = piControlActivity.getSharedPreferences(SettingsActivity.PREFERENCE_FILENAME,
 				0);
 		String certString = settings.getString("trustedCert", "");
 		if (!certString.isEmpty()) {
 			try {
 				ownCert = CertHelper.unserializeCert(certString);
 			} catch (CertificateException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	@Override
 	public void onPageStarted(WebView view, String url, Bitmap favicon) {
 		dialog = ProgressDialog.show(this.piControlActivity, "", "Lade...");
 		super.onPageStarted(view, url, favicon);
 	}
 
 	@Override
 	public void onPageFinished(WebView view, String url) {
 		super.onPageFinished(view, url);
 		dialog.dismiss();
 	}
 	
 
 	@Override
 	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 		super.onReceivedError(view, errorCode, description, failingUrl);
 		try {
 			//dismiss dialog if it exist
 			dialog.dismiss();
 		} catch (Exception e) {}
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.piControlActivity);
 		alertDialogBuilder.setTitle("Fehler beim verbinden!");
 
 		// set dialog message
 		alertDialogBuilder.setMessage(description)
 				.setPositiveButton("Neu Laden", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						mainWebView.reload();
 					}
 				}).setNeutralButton("Einstellungen", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent intent = new Intent(piControlActivity, SettingsActivity.class);
 						piControlActivity.startActivity(intent);
 					}
 				}).setNegativeButton("Beenden", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						piControlActivity.finish();
 					}
 				});
 
 		// create and show alert dialog
 		alertDialogBuilder.create().show();
 
 	}
 
 	@Override
 	public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		view.loadUrl(url);
 		return true;
 	}
 
 	@Override
 	public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
 		Log.d("WebAuth", "domain: " + host + " with realm: " + realm);
 
 		if (!handler.useHttpAuthUsernamePassword()) {
 			final AlertDialog alertDialog = new AlertDialog.Builder(this.piControlActivity).create();
 			alertDialog.setMessage("Wrong http auth for domain: " + host + " with realm: " + realm);
 			alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					alertDialog.dismiss();
 					Intent intent = new Intent(piControlActivity, SettingsActivity.class);
 					piControlActivity.startActivity(intent);
 				}
 			});
 			alertDialog.show();
 			handler.cancel();
 			return;
 		}
 
 		final SharedPreferences settings = piControlActivity.getSharedPreferences(SettingsActivity.PREFERENCE_FILENAME,
 				0);
 		URL url;
 		try {
 			url = new URL(settings.getString("url", piControlActivity.getResources().getString(R.string.default_url)));
 
 			int port = url.getPort();
 			if (port == -1) {
 				if (url.getProtocol().equals("http"))
 					port = 80;
 				else if (url.getProtocol().equals("https"))
 					port = 443;
 				else
 					System.err.println("Unknown protocol port: " + url.getProtocol());
 			}
 
 			if (host.equals(url.getHost() + ":" + port) || host.equals(url.getHost()) ) {
 				final String user = settings.getString("basicAuthUser", "");
 				final String pw = settings.getString("basicAuthPW", "");
 				handler.proceed(user, pw);
 			} else {
 				String[] up = view.getHttpAuthUsernamePassword(host, realm);
 				if (up != null && up.length == 2) {
 					handler.proceed(up[0], up[1]);
 				} else {
 					Log.d("WebAuth", "Could not find user/pass for domain: " + host + " with realm = " + realm);
 					final AlertDialog.Builder dialog = new AlertDialog.Builder(this.piControlActivity);
 					dialog.setMessage("Could not find user/pass for domain: " + host + " with realm = " + realm);
 					dialog.setPositiveButton("Neu versuchen", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.dismiss();
 							handler.cancel();
 							mainWebView.reload();
 						}
 					}).setNeutralButton("Einstellungen", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							Intent intent = new Intent(piControlActivity, SettingsActivity.class);
 							piControlActivity.startActivity(intent);
 							dialog.dismiss();
 							handler.cancel();
 						}
 					}).setNegativeButton("Beenden", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							piControlActivity.finish();
 						}
 					});
 					
 					dialog.show();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			handler.cancel();
 		}
 	}
 
 	@Override
 	public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

 		if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
 			SslCertificate cert = error.getCertificate();
 
 			boolean trust = false;
 
 			// Really really bad workaround to get X509Certificate
 			// from SSLError
 			Field privateField;
 			try {
 				privateField = SslCertificate.class.getDeclaredField("mX509Certificate");
 				privateField.setAccessible(true);
 				final X509Certificate mX509Certificate = (X509Certificate) privateField.get(cert);
 
 				if (ownCert != null && ownCert.equals(mX509Certificate)) {
 					trust = true;
 				} else {
 					if (ownCert == null) {
 						if (certDialog == null) {
 							certDialog = new AlertDialog.Builder(this.piControlActivity).create();
 							certDialog.setCancelable(false);
 							certDialog
 									.setMessage("Unknown Certificate: " + cert + "\nAllways trust this certificate?");
 							certDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									certDialog.dismiss();
 									try {
 										final SharedPreferences settings = piControlActivity.getSharedPreferences(
 												SettingsActivity.PREFERENCE_FILENAME, 0);
 										SharedPreferences.Editor editor = settings.edit();
 										editor.putString("trustedCert", CertHelper.serializeCert(mX509Certificate));
 										editor.commit();
 										ownCert = mX509Certificate;
 										mainWebView.reload();
 									} catch (CertificateEncodingException e) {
 										e.printStackTrace();
 									}
 								}
 							});
 							certDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									certDialog.dismiss();
 									certDialog = null;
 								}
 							});
 							certDialog.show();
 						}
 					}
 
 				}
 
 			} catch (NoSuchFieldException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 			if (trust) {
 				handler.proceed();
 			} else {
 				if (certDialog == null) {
 					certDialog = new AlertDialog.Builder(this.piControlActivity).create();
 					certDialog.setMessage("Untusted Certificate!");
 					certDialog.setButton(Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							certDialog.dismiss();
 						}
 					});
 					certDialog.show();
 				}
 				handler.cancel();
 			}
 		} else {
			if (certDialog == null) {
				certDialog = new AlertDialog.Builder(this.piControlActivity).create();
				certDialog.setMessage("SLL Certificate Error: " + error.getPrimaryError());
				certDialog.setButton(Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						certDialog.dismiss();
					}
				});
				certDialog.show();
			}
 			handler.cancel();
 		}
 
 	}
 
 }
