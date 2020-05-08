 package com.rohit.bookalive;
 
 import java.util.Locale;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class InfoSnippet {
 	private RegionOfInterest roi;
 	private String text_info;
 	private String audio_path;
 	private final int MAX_POINTS = 10;
 	private final String TAG = "InfoSnippet";
 	
 	public void read(Element object) {
 		NodeList ptList = object.getElementsByTagName("roi").item(0).getChildNodes();
 		int plen = 0;
 		double[] px = new double[MAX_POINTS];
 		double[] py = new double[MAX_POINTS];
 		for(int i=0; i<ptList.getLength(); i++) {
 			Node pt = ptList.item(i);
 			if(pt.getNodeType() == Node.ELEMENT_NODE) {
 				Element pte = (Element) pt;
 				double x = Double.parseDouble(pte.getElementsByTagName("x").item(0).getTextContent());
 				double y = Double.parseDouble(pte.getElementsByTagName("y").item(0).getTextContent());
 				px[plen] = x; py[plen] = y; plen++;
 			}
 		}
 		Log.v(TAG, object.getAttribute("name"));
 		Polygon P = new Polygon(px,py,plen);
 		roi = new RegionOfInterest(object.getAttribute("name"), P);
 		Element info = (Element) object.getElementsByTagName("info").item(0);
 		Element audio = (Element) object.getElementsByTagName("audio").item(0);
 		text_info = info.getTextContent();
 		audio_path = audio.getTextContent();
 	}
 	
 	public boolean contains(double x, double y) {
 		return roi.hit(x, y);
 	}
 	
 	public void show(Context context) {
 		final TextToSpeech tts = new TextToSpeech(context, null);
 		tts.setLanguage(Locale.US);
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(text_info)
 		.setNeutralButton("ReadOut!", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// On show listener defined below to prevent closing of dialog on click here
 			}
 		})
 		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				tts.stop();
 				dialog.cancel();
 			}
 		});
 		final AlertDialog alert = builder.create();
 		alert.setOnShowListener(new DialogInterface.OnShowListener() {
 			
 			@Override
 			public void onShow(DialogInterface dialog) {
 				Button b = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
 		        b.setOnClickListener(new View.OnClickListener() {
 		            @Override
 		            public void onClick(View view) {
 		            	tts.speak(text_info, TextToSpeech.QUEUE_FLUSH, null);
 		            }
 		        });
 				
 			}
 		});
		alert.setCanceledOnTouchOutside(false);
 		alert.show();
 	}
 }
