 package com.amateurbikenerd.echoLocation.math;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 
 public class GenerateCodeThatIsTooBig {
 	private Hashtable<Integer, Hashtable<Integer, List<Short>>> leftImpulses;
 	private Hashtable<Integer, Hashtable<Integer, List<Short>>> rightImpulses;
 	public static void main(String[] args)throws Exception{
                 //GenerateCodeThatIsTooBig data = new GenerateCodeThatIsTooBig("/home/darshan/src/echolocation/mit_full");
 		GenerateCodeThatIsTooBig data = new GenerateCodeThatIsTooBig("/home/dmiles/mit_full");
 		Hashtable<String, short[][]> ht = new Hashtable<String, short[][]>();
 		for(int azimuth : data.getAzimuths()){
 			for(int elevation : data.getElevations()){
 				String compositeKey = azimuth + "_" + elevation;
 				//System.out.println(compositeKey);
 				List<Short> listLeft = data.getImpulse('L', elevation, azimuth);
 				if(listLeft == null)
 					continue;
 				short[] left = new short[listLeft.size()];
 				for(int i = 0; i < listLeft.size(); i++)
 					left[i] = listLeft.get(i).shortValue();
 				List<Short> listRight = data.getImpulse('R', elevation, azimuth);
 				short[] right = new short[listRight.size()];
 				for(int i = 0; i < listRight.size(); i++)
 					right[i] = listRight.get(i).shortValue();
 				short[][] toStore = new short[][]{right, left};
 				ht.put(compositeKey, toStore);
 			}
 		}
 		System.out.println("import java.util.Hashtable;");
 		System.out.println("public class MITData{");
		System.out.println("    public Hashtable<String, short[][]> ht = new Hashtable<String, short[][]>();\n");
 
                String constructor = "    public MITData(){\n";
 		for(String compositeKey : ht.keySet()){
 			short[] left = ht.get(compositeKey)[1];
 			short[] right = ht.get(compositeKey)[0];
                        String funcName = "f" + compositeKey;
                        constructor += "        " + funcName + "();\n";
			System.out.println("    private void " + funcName + "(){");
                        System.out.print("        ht.put(\"" + compositeKey + "\", new short[][]{{");
 			for(int i = 0; i < right.length; i++){
 				System.out.print(right[i]);
 				if(i != right.length - 1)
 					System.out.print(", ");
 			}
 			System.out.print("},{");
 			for(int i = 0; i < left.length; i++){
 				System.out.print(left[i]);
 				if(i != left.length - 1)
 					System.out.print(", ");
 			}
			System.out.println("}});");
                        System.out.println("    }");
 		}
                constructor += "    }";
		System.out.println(constructor);
 		System.out.println("    public static void main(String[] args){");
		System.out.println("        MITData data = new MITData();");
		System.out.println("        short a = data.ht.get(\"220_10\")[0][0] ;");
 		System.out.println("        System.out.println(\"Hello World!\");");
		System.out.println("        System.out.println(\"data.ht[220_10][0][0] = \" + a);");
 		System.out.println("    } // close main");
 		System.out.println("} //close class");
 	}
 	public GenerateCodeThatIsTooBig(String directoryName) throws IllegalArgumentException, IllegalAccessException{
 		leftImpulses = new Hashtable<Integer, Hashtable<Integer, List<Short>>>();
 		rightImpulses = new Hashtable<Integer, Hashtable<Integer, List<Short>>>();
 		File mitDataDirectory = new File(directoryName);
 		String[] elevationFolders = mitDataDirectory.list();
 		for(int i = 0; i < elevationFolders.length; i++){
 			String elevationFolderName = elevationFolders[i];
 			if(! (elevationFolderName.length() > 4 && elevationFolderName.substring(0, 4).toLowerCase().equals("elev")))
 				continue;
 			// count backwards from the end of the file name until the character is
 			// not a digit. Use that index to get the elevation number.
 			int elevIdx = elevationFolderName.length() - 1;
 			while(Character.isDigit(elevationFolderName.charAt(elevIdx)))
 				elevIdx--;
 			elevIdx++;
 			int elevation = Integer.parseInt(elevationFolderName.subSequence(elevIdx, elevationFolderName.length()).toString());
 			File elevationFolder = new File(directoryName + "/" + elevationFolderName);
 			String[] datFiles = elevationFolder.list();
 			for(int j = 0; j < datFiles.length; j++){
 				String datFileName = datFiles[j];
 				if(datFileName.length() > 0 && (datFileName.charAt(0) == 'L' || datFileName.charAt(0) == 'R')){
 					// Here, we're going to use a char for L or R (left or right)
 					// and gather integers for elevation and azimuth
 					int indexOfLetterE = datFileName.indexOf('e');
 					int indexOfLetterA = datFileName.indexOf('a');
 					int felevation = Integer.parseInt(datFileName.substring(1, indexOfLetterE));
 					assert(felevation == elevation);
 					int azimuth = Integer.parseInt(datFileName.substring(indexOfLetterE + 1, indexOfLetterA));
 					List<Short> impulse = readImpulse(directoryName + "/" + elevationFolderName + "/" + datFileName);
 					Integer elevationKey = new Integer(elevation);
 					Hashtable<Integer, Hashtable<Integer, List<Short>>> impulses = leftImpulses;
 					if(datFileName.toUpperCase().charAt(0) == 'R')
 						impulses = rightImpulses;
 					if(! impulses.containsKey(elevationKey))
 						impulses.put(elevationKey, new Hashtable<Integer, List<Short>>());
 					Integer azimuthKey = new Integer(azimuth);
 					if(! impulses.get(elevationKey).containsKey(azimuthKey))
 						impulses.get(elevationKey).put(azimuthKey, impulse);
 				}
 			}
 		}
 	}
 	public void serialize()throws Exception{
 		ObjectOutputStream out = new ObjectOutputStream(
 				new FileOutputStream("/home/dmiles/two_channel_mit_data_serialized_file"));
 		out.writeObject(this);
 	}
 	public static GenerateCodeThatIsTooBig deserialize(InputStream location){
 		try{
 			ObjectInputStream in = new ObjectInputStream(location);
 			return (GenerateCodeThatIsTooBig)in.readObject();
 		}catch(IOException e){
 			e.printStackTrace();
 			return null;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	public List<Integer> getElevations(){
 		// BOZO: this assumes left and right impulses all have the same elevations
 		List<Integer> l = new ArrayList<Integer>(leftImpulses.size());
 		for(Integer i : leftImpulses.keySet())
 			l.add(i);
 		return l;
 	}
 	public List<Integer> getAzimuths(){
 		// BOZO: this assumes left and right impulses all have the same azimuths
 		List<Integer> l = new ArrayList<Integer>();
 		for(Integer elevation : leftImpulses.keySet()){
 			for(Integer azimuth : leftImpulses.get(elevation).keySet()){
 				l.add(azimuth);
 			}
 		}
 		return l;
 	}
 	public List<Short> getImpulse(char channel, int elevation, int azimuth){
 		Hashtable<Integer, Hashtable<Integer, List<Short>>> impulses = leftImpulses;
 		if(channel == 'R')
 			impulses = rightImpulses;
 		if(azimuth == 360)
 			azimuth = 0;
 		Integer elevationKey = new Integer(elevation);
 		Integer azimuthKey = new Integer(azimuth);
 		if(! impulses.containsKey(elevationKey))
 			return null;
 		if(! impulses.get(elevationKey).containsKey(azimuthKey))
 			return null;
 		return impulses.get(elevationKey).get(azimuthKey);
 	}
 	private static List<Short> readImpulse(String filename){
 		File file = new File(filename);
 		int fileLength = (int) file.length();
 		if (fileLength % 2 != 0) {
 			System.err
 			.println("Error, file does not contain an even number of bytes. Invalid format");
 			System.exit(1);
 		}
 		InputStream is = null;
 		try {
 			is = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		byte[] bytes = new byte[fileLength];
 		int read = 0;
 		try {
 			read = is.read(bytes, 0, fileLength);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (read != fileLength) {
 			System.err.println("Error, did not read whole file");
 			System.exit(1);
 		}
 		ByteBuffer byteBuf = ByteBuffer.wrap(bytes);
 		List<Short> values = new ArrayList<Short>();
 		for(int bytesIdx = 0; bytesIdx < fileLength; bytesIdx += 2){
 			values.add(byteBuf.getShort(bytesIdx));
 		}
 		return values;
 
 	}
 }
