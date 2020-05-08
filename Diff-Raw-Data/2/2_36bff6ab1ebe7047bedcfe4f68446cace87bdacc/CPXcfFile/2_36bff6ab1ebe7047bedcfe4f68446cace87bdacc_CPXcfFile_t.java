 package chibipaint.engine;
 
 import java.io.IOException;
 import java.io.FileOutputStream;
 // TODO: make abstract class to serve as parent for all file classes + possibly add ones for simple images (line PNG) too
 
 public class CPXcfFile {
 	protected static final byte gimp_xcf_[] = { 'g', 'i', 'm', 'p', ' ', 'x', 'c', 'f', ' '};
 	protected static final byte v0[] = { 'f', 'i', 'l', 'e'};
 	protected static final byte v2[] = { 'v', '0', '0', '2'};
 
 	static public boolean write(FileOutputStream os, CPArtwork a)
 	{
 		try {
 			writeMagic (os);
 			writeVersion (os, minVersion (a));
 			writeInt (os, a.width);  // image width
 			writeInt (os, a.height); // image height
 			writeInt (os, 0); // RGB Color - mode, other modes don't concern us
 			// Image properties
 			writeProperties (os, a);
 			// LayerLinks
 			int[] layerLinks = new int[a.layers.size ()];
 			int layerLinksPoisiton = (int) os.getChannel ().position ();
 			// dummy links to fill later
 			for (int i = 0; i < a.layers.size (); i++)
 				writeInt (os, 0);
 			writeInt (os, 0); // Terminator int for layers
 			// Channels (?)
 			writeInt (os, 0); // Terminator int for channels
 			// Layers
 			for (int i = 0; i < a.layers.size (); i++)
 			{
 				layerLinks[i] = (int) os.getChannel ().position ();
 				int usedLayer = a.layers.size () - 1 - i; // Since layers are reversed in xcf
 				writeLayer (os, a.layers.get(usedLayer), a.activeLayer == usedLayer);
 			}
 
 			long actualPosition = os.getChannel ().position ();
 			os.getChannel ().position (layerLinksPoisiton);
 			for (int i = 0; i < a.layers.size (); i++)
 				writeInt (os, layerLinks[i]);
 
 			os.getChannel ().position (actualPosition);
 
 			return true;
 		}
 		catch (IOException e) {
 			return false;
 		}
 	}
 
 	static public int minVersion (CPArtwork a)
 	{
 		for (int i = 0; i < a.layers.size (); i++)
 			if (a.layers.get(i).getBlendMode() == CPLayer.LM_SOFTLIGHT)
 				return 2;
 		return 0;
 	}
 
 	// TODO:These functions should be outside of these classes:
 	static public void writeInt(FileOutputStream os, int i) throws IOException {
 		byte[] temp = { (byte) (i >>> 24), (byte) ((i >>> 16) & 0xff), (byte) ((i >>> 8) & 0xff), (byte) (i & 0xff) };
 		os.write(temp);
 	}
 
 	static public void writeString (FileOutputStream os, String s) throws IOException
 	{
 		byte[] s_data = s.getBytes ("UTF-8"); // converting to utf-8
 		writeInt (os, s_data.length + 1); // size of bytes + 1 for terminating byte
 		os.write(s_data);
 		os.write(0); // null-terminating byte
 	}
 
 	static public void writeFloat (FileOutputStream os, float f) throws IOException
 	{
 		writeInt (os, Float.floatToRawIntBits (f));
 	}
 
 	// End of todo
 
 	static public void writeProperties(FileOutputStream os, CPArtwork a) throws IOException {
 		// Compression, using default one - RLE
 		writeInt (os, 17); // PROP_COMPRESSION
 		writeInt (os, 1); // payload size
 		os.write (1); // Means RLE
 
 		writeInt (os, 19); // PROP_RESOLUTION
 		writeInt (os, 8); // 2 Floats payload
 		writeFloat (os, 72.0f);
 		writeFloat (os, 72.0f);
 
 		writeInt (os, 0); // PROP_END
 		writeInt (os, 0); // empty payload
 	}
 
 	static public void writeLayer(FileOutputStream os, CPLayer layer, boolean isActive) throws IOException {
 		writeInt (os, layer.width); // layer width // Well in our case they are the as picture all the time
 		writeInt (os, layer.height); // layer height
 		writeInt (os, 1); // layer type, 1 - means 24 bit color with alpha
 		writeString (os, layer.name); // layer name
 		// Layer properties
 
 		// Is Layer Active
 		if (isActive) // TODO:implement this stuff in chi also (very convinient)
 		{
 			writeInt (os, 2); // PROP_ACTIVE_LAYER
 			writeInt (os, 0); // empty payload
 		}
 
 		// Layer Mode
 		writeInt (os, 7); // PROP_MODE
 		writeInt (os, 4); // payload size, 1 int
 		switch (layer.blendMode) {
 		case CPLayer.LM_NORMAL:
 			writeInt (os, 0); // 0: Normal
 			break;
 
 		case CPLayer.LM_MULTIPLY:
 			writeInt (os, 3); // 3: Multiply
 			break;
 
 		case CPLayer.LM_ADD:
 			writeInt (os, 7); // 7: Addition
 			break;
 
 		case CPLayer.LM_SCREEN:
 			writeInt (os, 4); // 4: Screen
 			break;
 
 		case CPLayer.LM_LIGHTEN:
 			writeInt (os, 10); // 10: Lighten Only
 			break;
 
 		case CPLayer.LM_DARKEN:
 			writeInt (os, 9); // 9: Darken Only
 			break;
 
 		case CPLayer.LM_SUBTRACT:
 			writeInt (os, 8); // 8: Subtract
 			break;
 
 		case CPLayer.LM_DODGE:
 			writeInt (os, 16); // 16: Dodge
 			break;
 
 		case CPLayer.LM_BURN:
 			writeInt (os, 17); // 17: Burn
 			break;
 
 		case CPLayer.LM_OVERLAY:
 			writeInt (os, 5); // 5: Overlay
 			break;
 
 		case CPLayer.LM_HARDLIGHT:
 			writeInt (os, 18); // 18: Hard Light
 			break;
 
 		case CPLayer.LM_SOFTLIGHT:
 			writeInt (os, 19); // 19: Soft Light (XCF version >= 2 only)
 			break;
 			// TODO: Three below looks like missing in gimp, maybe I should check formulas to investigate it
 		case CPLayer.LM_VIVIDLIGHT:
 			writeInt (os, 18); // 18: Hard Light
 			break;
 
 		case CPLayer.LM_LINEARLIGHT:
 			writeInt (os, 18); // 18: Hard Light
 			break;
 
 		case CPLayer.LM_PINLIGHT:
 			writeInt (os, 18); // 18: Hard Light
 			break;
 		}
 
 		// Offsets in our case it's dummy 0s
 
 		writeInt (os, 15); // PROP_OFFSETS
 		writeInt (os, 8); // Two ints
 		writeInt (os, 0);
 		writeInt (os, 0);
 
 		// Layer opacity:
 
 		writeInt (os, 6); // PROP_OPACITY
 		writeInt (os, 4); // one Int
 		writeInt (os, layer.getAlpha () * 255 / 100);
 
 		// Layer visibility:
 
 		writeInt (os, 8); // PROP_VISIBLE
 		writeInt (os, 4); // one Int
 		writeInt (os, layer.visible ? 1 : 0);
 
 		writeInt (os, 0); // PROP_END
 		writeInt (os, 0); // empty payload
 
 		// Pixel Data
 		writeInt (os, (int)os.getChannel().position() + 8); // After this and 0 for mask goes hierarchy structure
 
 		// Mask (in our case 0)
 		writeInt (os, 0);
 
 		//Hierarchy Structure
 		writeInt (os, layer.width); // Once again width
 		writeInt (os, layer.height); // Once again height
 		writeInt (os, 4); // Bytes per pixel
 		// Now we need to calculate how many level structures will be there
 
 		// now we have to write 1 + dummyLevelsCount int pointer then 3 ints for each dummy level then real level
 		int curPos = (int) os.getChannel ().position();
 		writeInt (os, curPos + 4 + 4); // The pointer to structure beyond this pointer and terminating zero
 		writeInt (os, 0); // Terminating zero for levels
 
 		// Actual level
 		writeInt (os, layer.width);
 		writeInt (os, layer.height);
 		// Now there goes some pointers for tile data
 		int wTiles = (int) (Math.ceil ((double )layer.width / 64));
 		int hTiles = (int) (Math.ceil ((double )layer.height / 64));
 		int numberOfTiles = wTiles * hTiles;
 		int[] tilePointers = new int[numberOfTiles];
 		int pointerPos = (int) os.getChannel ().position(); // Remembering position to fill it up later
 		for (int i = 0; i < numberOfTiles; i++)
 			writeInt (os, 0); // Just reserving places for future writing of actual pointer
 
 		writeInt (os, 0); // Terminating zero;
 		// Then we're starting to write actual tiles
 		for (int i = 0; i < hTiles; i++) // outer loop is vertical one
 			for (int j = 0; j < wTiles; j++)
 			{
 				tilePointers[i * wTiles + j] = (int) os.getChannel ().position();
 				writeTileRLE (os, layer.data, j, i, layer.width, layer.height); // Writing actual tile Info
 			}
 
 		int actualPos = (int) os.getChannel ().position();
 
 		os.getChannel ().position (pointerPos);
 		for (int i = 0; i < numberOfTiles; i++)
 			writeInt (os, tilePointers[i]);
 
 		os.getChannel ().position (actualPos);
 	}
 
 	static public byte getByte (int x, int pos)
 	{
 		return (byte) ((x >> ((3 - pos) * 8)) & 0xFF);
 	}
 
 	static public void writeTileRLE (FileOutputStream os, int[] data, int x, int y, int w, int h)  throws IOException
 	{
 		int sizeX = (x + 1) * 64 <= w ? 64 : w - x * 64;
 		int sizeY = (y + 1) * 64 <= h ? 64 : h - y * 64;
 		int m = sizeX * sizeY;
 		byte[] arr = new byte[m]; // uncompressed info
 		try
 		{
 			for (int p = 1; p < 5; p++)
 			{
 				int k = p % 4; // To write in order r-g-b alpha
 				int t = 0;
 				for (int i = 0; i < sizeY; i++)
 					for (int j = 0; j < sizeX; j++)
 					{
 						arr[t] = getByte (data [(y * 64 + i) * w + (x * 64 + j)], k);
 						t++;
 					}
 				int curPos = 0;
 
 				while (curPos < m)
 				{
 					// scanning for identical bytes
 					if (curPos + 1 < m && arr[curPos] == arr[curPos + 1])
 					{
 						int pos = curPos + 2;
 						while (pos < m && arr[pos] == arr[curPos])
 							pos++;
 						pos--; // getting back where we were still identical / in array boundaries
 						// writing (pos - curPos + 1) identical bytes, 2 cases
						if (pos - curPos <= 126) // short run of identical bytes
 						{
 							os.write (pos - curPos); // -1, cause value will be repeated n+1 times actually
 							os.write (arr[curPos]);
 						}
 						else  // long run of identical bytes
 						{
 							os.write (127);
 							int count = pos - curPos + 1;
 							os.write (getByte (count, 2));
 							os.write (getByte (count, 3));
 							os.write (arr[curPos]);
 						}
 						curPos = pos + 1;
 					}
 					if (curPos >= m)
 						break;
 					// scanning for different bytes
 					if (curPos + 1 >= m || arr[curPos] != arr[curPos + 1])
 					{
 						int pos = 0;
 						if (curPos + 1 >= m)
 							pos = m - 1;
 						else
 						{
 							pos = curPos + 1;
 							while (pos + 1 < m && arr[pos] != arr[pos + 1])
 								pos++;
 							if (pos != m - 1)
 								pos--;
 							// getting back where we were still different
 						}
 
 						// writing (pos - curPos + 1) different bytes, 2 cases
 						if (pos - curPos + 1 <= 127) // short run of different bytes
 						{
 							os.write (256 - (pos - curPos + 1));
 							for (int i = curPos; i <= pos; i++)
 								os.write (arr[i]);
 						}
 						else  // long run of identical bytes
 						{
 							os.write (128);
 							int count = pos - curPos + 1;
 							os.write (getByte (count, 2));
 							os.write (getByte (count, 3));
 							for (int i = curPos; i <= pos; i++)
 								os.write (arr[i]);
 						}
 						curPos = pos + 1;
 					}
 				}
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 			os.write(123);
 		}
 	}
 
 	static public void writeMagic (FileOutputStream os) throws IOException
 	{
 		os.write (gimp_xcf_);
 	}
 
 	static public void writeVersion (FileOutputStream os, int minVersion) throws IOException
 	{
 		// For now let's try version 0
 		switch (minVersion)
 		{
 		case 0:
 			os.write (v0);
 			break;
 		case 2:
 			os.write (v2);
 			break;
 		}
 		os.write (0); // zero-terminator byte for version
 	}
 }
 
