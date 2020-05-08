 package edu.chl.asciicam.activity.test;
 
 //Copyright 2012 Robin Braaf, Ossian Madisson, Martin Thrnesson, Fredrik Hansson and Jonas strm.
 //
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.io.IOException;
 import java.io.InputStream;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.test.AndroidTestCase;
 import edu.chl.asciicam.util.Convert;
 
 /**
  * Test class for Convert class in edu.chl.asciicam.util package.
  * 
  *
  */
 public class ConvertTest extends AndroidTestCase {
 	
 	private Context c;
 	private byte[] data;
 	
 	@Override
 	protected void setUp() throws Exception {
 		
 		super.setUp();
 		c = getContext();
 		InputStream strin = c.getResources().openRawResource(R.drawable.test);
 		data = new byte[strin.available()];
 		strin.read(data);
 		strin.close();
 		
 	}
 	
 	public void testConversions() throws IOException{
 		
 		Bitmap bmp1 = Convert.byteArrayToBitmap(data);
 		Bitmap bmp2 = Convert.compressPicture(data, 2);
 		//bmp2 should be half the size of bmp1 now.
 		assertEquals((int)bmp1.getHeight()/2 , bmp2.getHeight());
 		assertEquals((int)bmp1.getWidth()/2 , bmp2.getWidth());
 		
 	}
 }
