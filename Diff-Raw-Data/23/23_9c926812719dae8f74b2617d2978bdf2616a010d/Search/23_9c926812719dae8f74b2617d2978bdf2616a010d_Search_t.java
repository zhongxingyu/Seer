 package com.bubblespot;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
import com.bubblespot.lojas.ListAllShops;
import com.bubblespot.promocoes.ListPromo;
import com.bubblespot.shoppings.ListShoppings;

 public class Search {
 
 	private static Bundle b;
 
 	public static void pesquisa(final Context c, final Activity a) {
 		final CharSequence[] items = {"Shoppings", "Lojas", "Promoes"};
 
 		ImageView logo = (ImageView) a.findViewById(R.id.logo);
 		logo.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), BubbleSpot.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				a.startActivityForResult(intent, 0);
 			}
 		});
 
 		ImageView lupa = (ImageView) a.findViewById(R.id.lupa);
 		lupa.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(c);
 				builder.setTitle("Pesquisar");
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						switch(item){
 						case 0:
 							if (!checkNetwork(c, a)){
 								Dialog dialog2 = new Dialog(c);
 								dialog2.setContentView(R.layout.shoppingsearch);
 								dialog2.setTitle("Pesquisa de Shoppings");
 								dialog2.setCancelable(true);
 
 								final Spinner spinner = (Spinner) dialog2.findViewById(R.id.spinner);
 								ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 										c, R.array.opcoes_shopping, android.R.layout.simple_spinner_item);
 								adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 								spinner.setAdapter(adapter);
 
 								final EditText nome = (EditText) dialog2.findViewById(R.id.pesq_nome);
 
 								final Button pesquisa = (Button) dialog2.findViewById(R.id.pesquisar);
 								pesquisa.setOnClickListener(new View.OnClickListener() {
 
 									public void onClick(View v) {
 										if (!checkNetwork(c,a)){
 											String query = "";
 											try {
 												String nomep = ""+nome.getText();
												String queryTemp = nomep.trim().replace(" ","+");
												System.out.println(queryTemp);
 												query = URLEncoder.encode(queryTemp, "utf-8");
												query = query.trim().replace("%2B","+");
 												String pesquisa = "/search/shoppings?query=" + query + "&pesquisa=";
												System.out.println(pesquisa);
 												if(spinner.getSelectedItem().toString().equals("Nome")){
 													pesquisa+="0";
 												}
 												else if (spinner.getSelectedItem().toString().equals("Localizao")){
 													pesquisa+="1";
 												}
 												pesquisa+="&format=json";
 												Intent intent = new Intent(v.getContext(), ListShoppings.class);
 												b = new Bundle();
 												b.putString("text", pesquisa);
 												intent.putExtras(b);
 												a.startActivityForResult(intent, 0);
 											} catch (UnsupportedEncodingException e) {
 												Toast.makeText(c, "Erro ao tentar efetuar a pesquisa.", Toast.LENGTH_LONG).show();
 											}
 										}
 									}
 								});
 								dialog2.show();
 							}
 							break;
 						case 1:
 							if (!checkNetwork(c, a)){
 								Dialog dialog2 = new Dialog(c);
 								dialog2.setContentView(R.layout.shoppingsearch);
 								dialog2.setTitle("Pesquisa de Lojas");
 								dialog2.setCancelable(true);
 
 								final Spinner spinner = (Spinner) dialog2.findViewById(R.id.spinner);
 								ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 										c, R.array.opcoes_loja, android.R.layout.simple_spinner_item);
 								adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 								spinner.setAdapter(adapter);
 
 								final EditText nome = (EditText) dialog2.findViewById(R.id.pesq_nome);
 
 								final Button pesquisa = (Button) dialog2.findViewById(R.id.pesquisar);
 								pesquisa.setOnClickListener(new View.OnClickListener() {
 
 									public void onClick(View v) {
 										if (!checkNetwork(c,a)){
 											String query = "";
 											try {
 												String nomep = ""+nome.getText();
 												String queryTemp = nomep.trim().replace(" ", "+");
 												query = URLEncoder.encode(queryTemp, "utf-8");
												query = query.trim().replace("%2B","+");
 												System.out.println("QUERY: "+query);
 												String pesquisa = "/search/lojas?query=" + query + "&pesquisa=";
 												if(spinner.getSelectedItem().toString().equals("Nome")){
 													pesquisa+="0";
 												}
 												else if (spinner.getSelectedItem().toString().equals("rea de Negcio")){
 													pesquisa+="1";
 												}
 												pesquisa+="&format=json";
 												Intent intent = new Intent(v.getContext(), ListAllShops.class);
 												b = new Bundle();
 												b.putString("text", pesquisa);
 												intent.putExtras(b);
 												a.startActivityForResult(intent, 0);
 											} catch (UnsupportedEncodingException e) {
 												Toast.makeText(c, "Erro ao tentar efetuar a pesquisa.", Toast.LENGTH_LONG).show();
 											}
 										}
 									}
 								});
 								dialog2.show();
 							}
 							break;
 						case 2:
 							if (!checkNetwork(c, a)){
 								Dialog dialog2 = new Dialog(c);
 								dialog2.setContentView(R.layout.shoppingsearch);
 								dialog2.setTitle("Pesquisa de Promoes");
 								dialog2.setCancelable(true);
 
 								final Spinner spinner = (Spinner) dialog2.findViewById(R.id.spinner);
 								spinner.setVisibility(View.GONE);
 
 								final EditText nome = (EditText) dialog2.findViewById(R.id.pesq_nome);
 
 								final Button pesquisa = (Button) dialog2.findViewById(R.id.pesquisar);
 								pesquisa.setOnClickListener(new View.OnClickListener() {
 
 									public void onClick(View v) {
 										if (!checkNetwork(c,a)){
 											String query = "";
 											try {
 												String nomep = ""+nome.getText();
 												String queryTemp = nomep.trim().replace(" ", "+");
 												query = URLEncoder.encode(queryTemp, "utf-8");
												query = query.trim().replace("%2B","+");
 												System.out.println("QUERY: "+query);
 												String pesquisa = "/search/promos?query=" + query;
 												pesquisa+="&format=json";
 												Intent intent = new Intent(v.getContext(), ListPromo.class);
 												b = new Bundle();
 												b.putString("text", pesquisa);
 												intent.putExtras(b);
 												a.startActivityForResult(intent, 0);
 											} catch (UnsupportedEncodingException e) {
 												Toast.makeText(c, "Erro ao tentar efetuar a pesquisa.", Toast.LENGTH_LONG).show();
 											}
 										}
 									}
 								});
 								dialog2.show();
 							}
 							break;
 						}
 					}
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		});
 	}
 
 	public static boolean checkNetwork(final Context c, final Activity a) {
 		if (!isNetworkAvailable(c, a)) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(c);
 			builder.setMessage("Por favor ligue-se  Internet!")
 			.setCancelable(true)
 			.setPositiveButton("Definies",
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					a.startActivityForResult(
 							new Intent(
 									android.provider.Settings.ACTION_WIRELESS_SETTINGS),
 									0);
 				}
 			})
 			.setNegativeButton("Sair",
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					a.finish();
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean isNetworkAvailable(final Context c, final Activity a) {
 		ConnectivityManager connectivityManager = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 }
