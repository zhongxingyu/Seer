 // Copyright (c) 2012-2013 University of Washington
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // - Redistributions of source code must retain the above copyright notice,
 // this list of conditions and the following disclaimer.
 // - Redistributions in binary form must reproduce the above copyright
 // notice, this list of conditions and the following disclaimer in the
 // documentation and/or other materials provided with the distribution.
 // - Neither the name of the University of Washington nor the names of its
 // contributors may be used to endorse or promote products derived from this
 // software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package edu.washington.cs.tgv;
  
 import edu.washington.cs.tgv.ScanDB;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
  
 public class scanActivity extends Activity {
 	
 	private String results = "No results";	
 	private ScanDB items = null;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scan);
 		IntentIntegrator integrator = new IntentIntegrator(this);
 		integrator.initiateScan();
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
     	  if (scanResult != null) {
     				results = scanResult.getContents();
     				if(results != null){
     					items = new ScanDB(this);
     					items.open();
     					items.addText(results);
     					items.printTextDB();
     					items.close();
     				}
     	  }else{
    		  items = new ScanDB(this);
			  items.open();
			  items.addText("Nothing Scanned");
			  items.printTextDB();
			  items.close();
     	  }
    	  Intent resultsIntent = new Intent(this, MainActivity.class);
    	  startActivity(resultsIntent);
     	}
 }
