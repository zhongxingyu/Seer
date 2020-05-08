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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import edu.chl.asciicam.file.FileController;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Environment;
 import android.test.AndroidTestCase;
 
 public class FileControllerTest extends AndroidTestCase {
 	
 	private static final String OPTIONS_FILENAME = "OptionsAscii";
 	Context c;
 	FileController fc;
 	File file, file2;
 	//Byte array of an picture to be used for testing
 	byte [] data;
 	
 	public FileControllerTest(){
 		
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		
 		super.setUp();
 		c = getContext();
 		fc = new FileController(c);
 		InputStream strin = c.getResources().openRawResource(R.drawable.test);
 		data = new byte[strin.available()];
 		strin.read(data);
 		strin.close();
 		
 	}
 	
 	/**
 	 * Tests if we can save a image to the 
 	 * @throws IOException goodie
 	 */
 	public void testSavePic() throws IOException{
 
 		
		
 		fc.savePic(data);
		//Init sequence now
		int i = checkSeq();
 		File path = new File(Environment.getExternalStorageDirectory(), "DCIM" + File.separator + "AsciiCAM");
 		File file = new File(path.getPath() + File.separator + "ASCIIPIC_" + (i-1) + ".jpg");
 		//Make sure file is created
 		assertTrue(file.exists());
 		//Double check
 		fc.savePic(data);
 		i++;
 		File file2 = new File(path.getPath() + File.separator + "ASCIIPIC_" + (i-1) + ".jpg");
		//check sequence increment (could possibly turn out false by mistake if a picture with number i
		//already existed).
 		assertEquals(i, checkSeq());
 		assertTrue(file2.exists());
 		
 		//TearDown test
 		SharedPreferences settings = c.getSharedPreferences(OPTIONS_FILENAME, Context.MODE_PRIVATE);
 		Editor editor = settings.edit();
 		editor.putInt(FileController.SEQUENCENUMBER, 1);
 		editor.commit();
 		
 		//Remove files saved
 		file.delete();
 		file2.delete();
 		
 	}
 	
 	//Returns the saved sequence
 	private int checkSeq() throws IOException{
 		SharedPreferences settings = c.getSharedPreferences(OPTIONS_FILENAME, Context.MODE_PRIVATE);
 		return settings.getInt(FileController.SEQUENCENUMBER, 1);
 	}
 	
 	/**
 	 * Test if we can save and load pic for use within our application.
 	 * @throws IOException 
 	 */
 	public void testPrivateLoadAndSave() throws IOException{
 		// first run code and make sure we dont get any exceptions thrown
 		fc.savePicPrivate(data);
 		
 		byte[] loaded = fc.loadPicPrivate();
 		
 		//Cannot compare byte[] directly so we wrap them.
 		ByteBuffer dataBB = ByteBuffer.wrap(data);
 		ByteBuffer loadedBB = ByteBuffer.wrap(loaded);
 		assertEquals(dataBB, loadedBB);
 	}
 }
