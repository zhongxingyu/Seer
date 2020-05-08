 /*
 	Sragon Bookmark is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	Sragon Bookmark is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
	along with Sragon Bookmark. If not, see <http://www.gnu.org/licenses/>.
 
 	Original Author: Sragon I&D <desarrollo03@siragon.com.ve>
 
 	Developers: <Please add your email here>
 	Contributor: <Please add your email here>
 	Translators: <Please add your email here>
 */
 
 package com.siragon.bookmark;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent; 
 import android.net.Uri;
 
 public class MainActivity extends Activity
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.siragon.com.ve"));
 		startActivity(browserIntent);
 		finish();
         System.exit(0);
     }
 }
