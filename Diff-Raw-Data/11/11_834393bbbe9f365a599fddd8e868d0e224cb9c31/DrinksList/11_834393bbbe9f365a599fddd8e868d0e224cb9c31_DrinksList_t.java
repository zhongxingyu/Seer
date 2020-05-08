 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 
 package se.turbotorsk.mybar;
 
 import se.turbotorsk.mybar.controller.Controller;
 import android.app.ListActivity;
 import android.content.Intent;
import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.content.ContentResolver;
 
 
 public class DrinksList extends ListActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.activity_drinks_list);
         
         //Storing some random variables
         Controller controller = Controller.controller;
        ContentResolver contentResolver = null;
		String[] values = controller.getDrinkNamesAsArray(contentResolver);
         
         //Adds everything to an adapter. We are also chosing which layout the program should use. This is where we're going to choose our own layout when that works.
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 R.layout.rowlayout, R.id.drink, values);
         
         //Sets the adapter that we just did
         setListAdapter(adapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_drinks_list, menu);
         return true;
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
       //String item = (String) getListAdapter().getItem(position);
       //We will replace this to start another activity instead. Dont know how to do that yet.
       //Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
       
       Intent intent = new Intent(this, View_Drink_Activity.class);
       intent.putExtra("drinkname", (String) getListAdapter().getItem(position));
       intent.putExtra("rating", (String) "1/5");
       intent.putExtra("ingredients", (String) "En massa ingredienser hr...");
       intent.putExtra("descrip", (String) "En frklaring p drinken hr... kanske lite fakta osv?  - - Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eu diam eu sapien fringilla bibendum in at lacus. Cras id sagittis velit. Proin vel arcu libero, id elementum ligula. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Ut eleifend tincidunt facilisis. Nullam ut egestas sapien. Vestibulum tempus nibh sit amet felis ornare sit amet mattis neque placerat. Vivamus porta venenatis nulla nec scelerisque. Vivamus rhoncus vulputate diam tincidunt porta. Nulla et nunc quis metus mollis vehicula ut eget sapien. Sed quis est sem. Aliquam elementum ullamcorper nisi, sed imperdiet orci blandit ac. Sed eleifend, ligula ut cursus iaculis, libero ligula luctus nunc, facilisis vestibulum odio turpis in mi. Praesent ac augue quis mi scelerisque mollis. Aenean convallis, tortor id vestibulum sodales, elit elit lobortis metus, eget sollicitudin arcu turpis ac massa. Nulla nec arcu in purus consectetur iaculis. Vestibulum ac eros in est ullamcorper ornare. Proin rhoncus posuere mauris nec commodo. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nam rhoncus bibendum auctor. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi at lorem ut risus eleifend consequat. Integer et porttitor ipsum. Suspendisse id tortor enim, ac convallis tellus. Vivamus convallis, arcu vitae pharetra aliquet, augue massa pulvinar nunc, nec auctor eros felis at diam. Nullam nec quam erat, sit amet porttitor libero. Mauris elit purus, faucibus at posuere condimentum, mollis sed mauris.");
       startActivity(intent);
     }
 }
