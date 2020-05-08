 /*
  * Copyright 2010-2011 Jake Basile
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jakebasile.android.linkshrink;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public abstract class ShortenUrl extends Activity
 {
 	private static final String ISGD = "is.gd";
 
 	private static final String BITLY = "bit.ly";
 
 	private static final String GOOGL = "goo.gl";
 
 	private static final String ISGD_URL = "http://is.gd/api.php?longurl=%1$s";
 
 	private static final String BITLY_URL = "http://api.bit.ly/v3/shorten?login=%2$s&apiKey=%3$s&longUrl=%1$s&format=txt";
 
 	private static final String GOOGL_URL = "https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyAChsvdGhllsCuS0LhDI0uq1e4NyG2NT0o";
 
 	private Handler _handler;
 
 	private String _shortUrl;
 
 	protected boolean reshare;
 
 	@Override
 	public void onCreate(Bundle icicle)
 	{
 		super.onCreate(icicle);
 		reshare = false;
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		shorten(prefs, extras.getString(Intent.EXTRA_TEXT));
 	}
 
 	protected abstract void onUrlShortened(String shortUrl);
 
 	protected abstract void onUrlAlreadyShortened(String shortUrl);
 
 	private void shorten(SharedPreferences prefs, String longUrl)
 	{
 		final String service = prefs.getString("service", GOOGL);
 		String host = Uri.parse(longUrl).getHost();
 		if(host.endsWith(service))
 		{
 			onUrlAlreadyShortened(longUrl);
 			finish();
 			return;
 		}
 		_handler = new Handler()
 		{
 			@Override
 			public void handleMessage(Message msg)
 			{
 				switch(msg.what)
 				{
 					case 0:
 					{
 						if(_shortUrl != null)
 						{
 							onUrlShortened(_shortUrl);
 						}
 						else
 						{
 							String display = String.format(getResources().getString(
 								R.string.unknownerror), service);
 							Toast.makeText(getApplicationContext(), display,
 								Toast.LENGTH_LONG).show();
 						}
 						break;
 					}
 					case 1:
 					{
 						String display = String.format(getResources().getString(
 							R.string.commerror), service);
 						Toast.makeText(getApplicationContext(), display,
 							Toast.LENGTH_LONG).show();
 						break;
 					}
 					case 2:
 					{
 						String display = String.format(getResources().getString(
 							R.string.urlerror), service);
 						Toast.makeText(getApplicationContext(), display,
 							Toast.LENGTH_LONG).show();
 						break;
 					}
 				}
 				finish();
 				return;
 			}
 		};
 		if(service.equalsIgnoreCase(ISGD))
 		{
 			shortenWithIsgd(longUrl);
 		}
 		else if(service.equalsIgnoreCase(BITLY))
 		{
 			shortenWithBitly(longUrl, prefs);
 		}
 		else if(service.equalsIgnoreCase(GOOGL))
 		{
 			shortenWithGoogl(longUrl);
 		}
 		else
 		{
 			Toast.makeText(getApplicationContext(), R.string.badconfig, Toast.LENGTH_LONG).show();
 			finish();
 		}
 	}
 
 	private void shortenWithBitly(final String longUrl, SharedPreferences prefs)
 	{
 		final String username = prefs.getString("bitlyUsername", null);
 		final String key = prefs.getString("bitlyKey", null);
 		if(username == null || key == null)
 		{
 			Toast.makeText(getApplicationContext(), R.string.badbitlyconfig,
 				Toast.LENGTH_LONG).show();
 			_shortUrl = null;
 			finish();
 		}
 		else
 		{
 			String display = String.format(getResources().getString(
 				R.string.shortening_message), BITLY);
 			final ProgressDialog pd = ProgressDialog.show(ShortenUrl.this,
 				getResources().getString(R.string.shortening_title), display, true);
 			new Thread()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						String requestUrl = String.format(BITLY_URL, longUrl,
 							username.toLowerCase(), key);
 						ByteArrayOutputStream stream = new ByteArrayOutputStream();
 						HttpClient httpClient = new DefaultHttpClient();
 						HttpGet request = new HttpGet(new URI(requestUrl));
 						HttpResponse response = httpClient.execute(request);
 						if(response.getStatusLine().getStatusCode() == 200)
 						{
 							response.getEntity().writeTo(stream);
 							_shortUrl = new String(stream.toByteArray()).trim();
 						}
 						else
 						{
 							_shortUrl = null;
 						}
 						_handler.sendEmptyMessage(0);
 					}
 					catch(IOException ex)
 					{
 						_handler.sendEmptyMessage(1);
 					}
 					catch(URISyntaxException ex)
 					{
 						_handler.sendEmptyMessage(2);
 					}
 					finally
 					{
 						pd.dismiss();
 					}
 				}
 			}.start();
 		}
 	}
 
 	private void shortenWithIsgd(final String longUrl)
 	{
 		String display = String.format(getResources().getString(
 			R.string.shortening_message), ISGD);
 		final ProgressDialog pd = ProgressDialog.show(ShortenUrl.this,
 			getResources().getString(R.string.shortening_title), display, true);
 		new Thread()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					String requestUrl = String.format(ISGD_URL,
 						URLEncoder.encode(longUrl));
 					ByteArrayOutputStream stream = new ByteArrayOutputStream();
 					HttpClient httpClient = new DefaultHttpClient();
 					HttpGet request = new HttpGet(new URI(requestUrl));
 					HttpResponse response = httpClient.execute(request);
 					if(response.getStatusLine().getStatusCode() == 200)
 					{
 						response.getEntity().writeTo(stream);
 						_shortUrl = new String(stream.toByteArray()).trim();
 					}
 					else
 					{
 						_shortUrl = null;
 					}
 					_handler.sendEmptyMessage(0);
 				}
 				catch(IOException ex)
 				{
 					_handler.sendEmptyMessage(1);
 				}
 				catch(URISyntaxException ex)
 				{
 					_handler.sendEmptyMessage(2);
 				}
 				finally
 				{
 					pd.dismiss();
 				}
 			}
 		}.start();
 	}
 
 	private void shortenWithGoogl(final String longUrl)
 	{
 		String display = String.format(getResources().getString(
 			R.string.shortening_message), GOOGL);
 		final ProgressDialog pd = ProgressDialog.show(ShortenUrl.this,
 			getResources().getString(R.string.shortening_title), display, true);
 		new Thread()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					JSONObject request = new JSONObject();
 					request.put("longUrl", longUrl);
 					HttpClient httpClient = new DefaultHttpClient();
 					HttpPost post = new HttpPost(new URI(GOOGL_URL));
 					HttpEntity entity = new StringEntity(request.toString());
 					post.setEntity(entity);
 					post.setHeader("Content-Type", "application/json");
 					HttpResponse response = httpClient.execute(post);
 					if(response.getStatusLine().getStatusCode() == 200)
 					{
 						ByteArrayOutputStream stream = new ByteArrayOutputStream();
 						response.getEntity().writeTo(stream);
 						String jsonMessage = new String(stream.toByteArray());
 						Log.v("linkshrink", jsonMessage);
 						JSONObject googlResponse = new JSONObject(jsonMessage);
 						_shortUrl = googlResponse.getString("id");
 					}
 					else
 					{
 						_shortUrl = null;
 					}
 					_handler.sendEmptyMessage(0);
 				}
 				catch(IOException ex)
 				{
 					_handler.sendEmptyMessage(1);
 					Log.e("linkshrink", ex.getMessage());
 				}
 				catch(URISyntaxException ex)
 				{
 					_handler.sendEmptyMessage(2);
 					Log.e("linkshrink", ex.getMessage());
 				}
 				catch(JSONException ex)
 				{
 					// nothing
 					Log.e("linkshrink", ex.getMessage());
 				}
 				finally
 				{
 					pd.dismiss();
 				}
 			}
 		}.start();
 	}
 }
