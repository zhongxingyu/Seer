 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.fragments;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Document;
 import no.hials.muldvarp.v2.fragments.ListFragment.ListType;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author terje
  */
 public class DetailFragment extends MuldvarpFragment {
     View fragmentView;
     private TextView textItemTitle;
     private TextView textItemDescription;
     ListType type;
     String url;
 
     public DetailFragment(String fragmentTitle, int iconResourceID, ListType type) {
         super.fragmentTitle = fragmentTitle;
         super.iconResourceID = iconResourceID;
         this.type = type;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         fragmentView = inflater.inflate(R.layout.metadata, container, false);
         textItemTitle = (TextView) fragmentView.findViewById(R.id.item_title);
         textItemDescription = (TextView) fragmentView.findViewById(R.id.item_description);
 
         textItemTitle.setText(owningActivity.domain.getName());
         textItemDescription.setText(owningActivity.domain.getDescription());
         Document d = (Document)owningActivity.domain;
         url = d.getURI();
 
         final Button button = (Button) fragmentView.findViewById(R.id.item_button1);
 
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                     DownloadFilesTask dft = new DownloadFilesTask();
                     dft.setUrl(url);
                     dft.execute();
              }
          });
 
         final Button button2 = (Button) fragmentView.findViewById(R.id.item_button2);
 
         button2.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  System.out.println(url);
                 Uri path = Uri.parse(url);
                 if (true) {
                     Intent intent = new Intent(Intent.ACTION_VIEW);
                     intent.setDataAndType(path, "application/pdf");
                     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
                     try {
                         startActivity(intent);
                     }
                     catch (ActivityNotFoundException e) {
                         System.out.println("godamnit!");
                     }
                 }
                 Intent i = new Intent(Intent.ACTION_VIEW);
                 i.setDataAndType(path, "text/html");
                 startActivity(i);
              }
          });
 
         return fragmentView;
     }
     
     private class DownloadFilesTask extends AsyncTask{
         String url;
     public void setUrl(String url){
         this.url = url;
     }
         @Override
         protected Object doInBackground(Object... paramss) {
             URL source;
            File f = null;
             try {
                 source = new URL(url);
                f = new File(Environment.getExternalStorageDirectory(), source.getFile());
                 try {
                     FileUtils.copyURLToFile(source, f, 15000, 5000);
                 } catch (IOException ex) {
                     Logger.getLogger(DetailFragment.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.out.println(f.getName());
                 Intent i = new Intent(Intent.ACTION_VIEW);
                 i.setDataAndType(Uri.fromFile(f), "application/pdf");
                 i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                 System.out.println(f.isFile());
                 startActivity(i);
             } catch (MalformedURLException ex) {
                 Logger.getLogger(DetailFragment.class.getName()).log(Level.SEVERE, null, ex);
             }
         
         
             return null;
         }
     }
 }
