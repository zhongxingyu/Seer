 package no.poltilbud.feed;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.xmlpull.v1.XmlSerializer;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Xml;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ProductList extends ListActivity {
 	
 	private List<Product> m_productsFromXML = null;
 	private ParsedProducts m_parsedProducts = null;
 	private String m_pubDate = null;
 	private ArrayList<Product> m_products = null;
 	private ProgressDialog m_ProgressDialog;
 	private ProductAdapter m_adapter;
 	private final String offerXMLFilename = "offersFromPoltilbud.xml";
 	
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.list);  
         
         Bundle extras = getIntent().getExtras();
         String term = extras.getString("button");
         Log.i("Poltilbud", "This button is pressed: " + term);
         
         if (isOnline() && term.equalsIgnoreCase(PoltilbudEnums.BUTTON_FETCHOFFERS)){
         	loadFeed(false, term);
         }
         else if (!isOnline() && term.equalsIgnoreCase(PoltilbudEnums.BUTTON_FETCHOFFERS)){
         	AlertDialog alertDialog = new AlertDialog.Builder(ProductList.this).create();
         	alertDialog.setTitle("Feil");
         	alertDialog.setMessage("Ingen tilgang til internett");
         	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
         	      public void onClick(DialogInterface dialog, int which) {
         	    	finish();
         	    } });
         	alertDialog.show();
         	
         }
         else if (isOnline() && !offerFileExists()) {
         	loadFeed(false, term);
         }    	
         else if (offerFileExists()) {
         	loadFeed(true, term);
 		}
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, 0, 
 				0, R.string.android_sax);
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		super.onMenuItemSelected(featureId, item);
 		ArrayAdapter<String> adapter =
 			(ArrayAdapter<String>) this.getListAdapter();
 		if (adapter.getCount() > 0){
 			adapter.clear();
 		}
 		this.loadFeed(false, null);
 		return true;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent viewProduct = new Intent(Intent.ACTION_VIEW, 
				Uri.parse(m_products.get(position).getLink().toExternalForm()));
 		this.startActivity(viewProduct);
 	}
 
 	private void loadFeed(boolean useLocalXMLFile, String typeButton){
 		final String typeButtonStr = typeButton;
     	try{
 	    	final Parser parser;
 	    	if (useLocalXMLFile){
 	    		parser = FeedParserFactory.getParser(getLocalInputStream());
 	    	} else
 	    	{
 	    		parser = FeedParserFactory.getParser();
 	    	}
 	    	
 	    	m_products = new ArrayList<Product>();
 	        this.m_adapter = new ProductAdapter(this, R.layout.row, m_products);
 	                setListAdapter(this.m_adapter);
 	    	
 	    	Runnable viewProducts = new Runnable(){
 	            public void run() {
 	            	long start = System.currentTimeMillis();
 	            	m_parsedProducts = parser.parse();
 	            	m_productsFromXML = m_parsedProducts.getProducts();	 
 	            	m_pubDate = m_parsedProducts.getDate();
 	            	m_products = new ArrayList<Product>(m_productsFromXML.size());
 	    	    	for (Product prd : m_productsFromXML){
 	    	    		if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_BEER)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_BEER))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_REDWINE)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_REDWINE))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_WHITEWINE)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_WHITEWINE))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_ROSE)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_ROSEWINE))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_CHAMP)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_CHAMP))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_FRUITWINE)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_FUITWINE))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_SPRIRT)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_SPIRIT))
 		    	    			m_products.add(prd);
 	    	    		} else if (typeButtonStr != null && typeButtonStr.equals(PoltilbudEnums.BUTTON_NONALCH)){
 	    	    			if (prd.getType().equalsIgnoreCase(PoltilbudEnums.NO_NONALCH))
 		    	    			m_products.add(prd);
 	    	    		} else {
 	    	    			m_products.add(prd);
 	    	    		}
 	    	    		
 	    	    	}
 	    	    	long duration = System.currentTimeMillis() - start;
 	    	    	Log.i("Poltilbud", "Parser duration=" + duration);
 	    	    	String xml = writeXml();
 	    	    	Log.i("Poltilbud", xml);
 	    	    	saveFileToFilesystem(xml);
 	    	    	Log.i("Poltilbud", "File written to filesystem");
 	            	runOnUiThread(returnRes);	            	
 	            }
 	        };
 		    Thread thread =  new Thread(null, viewProducts, "MagentoBackground");
 		        thread.start();
 		        
 		    String messageToUser = "";
 		    if (!useLocalXMLFile){
 		    	messageToUser = "Henter tilbud";
 		    } else
 		    {
 		    	messageToUser = "Henter tilbud fra lokalt buffer";
 		    }
 	        m_ProgressDialog = ProgressDialog.show(ProductList.this,    
 	              "Vennligst vent...", messageToUser, true);
 	
 	    	this.setListAdapter(m_adapter);
 	    	
     	} catch (Throwable t){
     		Log.e("Poltilbud",t.getMessage(),t);
     	}
     }
 	
 	private Runnable returnRes = new Runnable() {
 
         public void run() {
             if(m_products != null && m_products.size() > 0){
                 m_adapter.notifyDataSetChanged();
                 for(int i=0;i<m_products.size();i++)
                 	m_adapter.add(m_products.get(i));
             }
             m_ProgressDialog.dismiss();
             m_adapter.notifyDataSetChanged();
         }
     };
     
 	private String writeXml(){
 		XmlSerializer serializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		try {
 			serializer.setOutput(writer);
 			serializer.startDocument("UTF-8", true);
 			serializer.startTag("", "poltilbud");			
 			for (Product product: m_productsFromXML){
 				serializer.startTag("", "product");
 				
 				serializer.startTag("", "name");
 				serializer.text(product.getName());
 				serializer.endTag("", "name");
 				
 				serializer.startTag("", "url");
 				serializer.text(product.getLink().toExternalForm());
 				serializer.endTag("", "url");
 				
 				serializer.startTag("", "type");
 				serializer.text(product.getType());
 				serializer.endTag("", "type");	
 				
 				serializer.startTag("", "differance");
 				serializer.text(product.getDifferance());
 				serializer.endTag("", "differance");	
 				
 				serializer.startTag("", "differancePerc");
 				serializer.text(product.getDifferancePerc());
 				serializer.endTag("", "differancePerc");	
 				
 				serializer.startTag("", "newPrice");
 				serializer.text(product.getNewPrice());
 				serializer.endTag("", "newPrice");	
 				
 				serializer.startTag("", "oldPrice");
 				serializer.text(product.getOldPrice());
 				serializer.endTag("", "oldPrice");
 
 				serializer.endTag("", "product");
 			}
 			serializer.startTag("", "pubDate");
 			serializer.text(m_pubDate);
 			serializer.endTag("", "pubDate");
 			serializer.endTag("", "poltilbud");
 			serializer.endDocument();
 			return writer.toString();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} 
 	}
 	
 	private boolean isOnline() {
    	 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	 if (cm.getActiveNetworkInfo() == null)
    		 return false;
    	 return cm.getActiveNetworkInfo().isConnectedOrConnecting();
 
    	}
 	
 	private void saveFileToFilesystem(String file){
 		
 		try {           
 	           FileOutputStream fOut = openFileOutput(PoltilbudEnums.FILENAME,
 	           MODE_PRIVATE);
 	           OutputStreamWriter osw = new OutputStreamWriter(fOut);
 	           
 	           osw.write(file);
 	           osw.flush();
 	           osw.close();
 
 	           Log.i("Poltilbud", "File written ok");
 	           
 	           FileOutputStream fOut2 = openFileOutput(PoltilbudEnums.offerFetchedDate,
 	           MODE_PRIVATE);
 	           OutputStreamWriter osw2 = new OutputStreamWriter(fOut2);
 	           
 	           Date dateNow = new Date ();
 	           
 	           SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("ddMMyyyy");
 	    
 	           StringBuilder nowddMMyyyy = new StringBuilder( dateformatYYYYMMDD.format( dateNow ) );    
 
 	           osw2.write(nowddMMyyyy.toString());
 	           osw2.flush();
 	           osw2.close();
 
 	           Log.i("Poltilbud", "File date written ok");
 	           
 	       } catch (IOException ioe) {
 	           ioe.printStackTrace();
 	       }
 		
 	}
 	
 	private InputStream getLocalInputStream(){
 		
 		readXMLFile();
 		try {
 		    return openFileInput(PoltilbudEnums.FILENAME);		    
 		} catch (java.io.FileNotFoundException e) {
 			Log.i("Poltilbud", "File do not exists!");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private String readXMLFile(){
 		String offerXMLFile = new String();
 
 		try {
 		    InputStream in = openFileInput(offerXMLFilename);
 
 		    if (in != null) {
 		      InputStreamReader input = new InputStreamReader(in);
 		      BufferedReader buffreader = new BufferedReader(input);
 		      String str;
 		      StringBuffer stringBuffer = new StringBuffer();
 		      while (( str = buffreader.readLine()) != null) {
 		        stringBuffer.append(str + "\n");
 		      }
 		      in.close();
 		      offerXMLFile = stringBuffer.toString();
 		      Log.i("Poltilbud", "XML file from local: " + offerXMLFile);
 		    }
 		    
 		    Log.i("Poltilbud", "File read");
 		    
 		} catch (java.io.FileNotFoundException e) {
 		  Log.i("Poltilbud", "File read");
 		  e.printStackTrace();		 
 	  	} catch (IOException e) {
 		  Log.i("Poltilbud", "File read");
 		  e.printStackTrace();		  
 		}
 
 		return offerXMLFile;
 	}
 	
 	private boolean offerFileExists(){
 		try {
 		    openFileInput(offerXMLFilename);
 		} catch (java.io.FileNotFoundException e) {
 		  return false;
 		}
 	  	return true;
 	}
 	
 }
