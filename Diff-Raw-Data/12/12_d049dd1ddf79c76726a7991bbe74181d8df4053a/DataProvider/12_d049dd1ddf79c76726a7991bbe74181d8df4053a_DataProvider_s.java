 package Data;
 
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 
 import ch.systemsx.cisd.hdf5.HDF5Factory;
 import ch.systemsx.cisd.hdf5.IHDF5Reader;
 
 public class DataProvider 
 {
 	private String path;
 	private ImageData[] data;
 	
 	public String getPath() {
 		return path;
 	}
 	public void setPath(String path) {
 		this.path = path;
 	}
 	
 	public DataProvider(String path)
 	{
 		this.path = path;
 		this.data = readData();
 	}
 	
 
 	public ImageData[][] getData(int numberOfParts)
 	{
 		int dataLenght = data.length;
 		int count = (int) dataLenght / numberOfParts ;
 		ImageData[][] learnData = new ImageData[numberOfParts][count];
 		for(int i = 0; i <  numberOfParts; i++)
 		{
 			for(int j = 0; j < count; j++)
 			learnData[i][j] = data[i*count+j];
 		}
 		return learnData;
 	}
 	
 	private ImageData[] readData(){
 		File file = new File(path);
 		String filename = file.getName();
 		System.out.println("Loading ImageDate: " + filename);
 		if(filename.matches(".*.h5")){
 			System.out.println("Importing ImageData (H5 Format)");
 		
 			try{
 				IHDF5Reader reader = HDF5Factory.openForReading(path);
 				 float[][] data = reader.readFloatMatrix("data/data");
 				 float[] labels = reader.readFloatArray("data/label");
 				 reader.close();
 				 ImageData[] imageDataArray = new ImageData[labels.length];
 				 if(labels.length == data[0].length)
 				 {
 					 for(int i = 0; i < labels.length; i++)
 					 {
 						 float greyValues[] = new float[data.length];
 						 for(int j=0; j < data.length ;j++)
 						 {
 							 if(data[j][i] > 40)
 								 greyValues[j] = 1;
 							 else
 								 greyValues[j] = 0;
 						 }
 						 imageDataArray[i] = new ImageData(greyValues, (int) labels[i]);
 					 }
 					 shuffle(imageDataArray);
 					 return imageDataArray;
 				 } else
 					 return null;
 			}catch(Exception e)
 			{
 				return null;
 			}	
 		}
 		else if (filename.matches(".*.jpg")){
 			System.out.println("Importing ImageData (JPG Format)");
 			try {
 				//Import as Grayscale
 				BufferedImage image = ImageIO.read(file);
 				ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
 				ColorConvertOp op = new ColorConvertOp(cs, null);
 				BufferedImage img = op.filter(image, null);
 				
 				
 				float[] grayvalues = new float[28*28];
 				int temp = 0;
 				for (int x = 0; x < 28; x++){
 					for (int y = 0; y < 28; y++) {
 						//Extract one color and invert 
 						temp = 255 - (img.getRGB(x, y) & 0x000000ff);
 						//Normalisation
 						if (temp > 40)
 							grayvalues[x + y * 28] = 1;
 						else
 							grayvalues[x + y * 28] = 0;
 						//grayvalues[x + y * 28] = img.getRGB(x, y) & 0x000000ff;
 						//System.out.println(grayvalues[x + y * 28] + " , " + temp);
 					}
 				}
 				//Label can be stored as the first character in the filename, i.e "6_00.jpg" => 6
 				int label = 0;
 				try {
					label=Integer.parseInt("6_00".substring(0, 1));
 				}
 				catch (Exception e) {
 					System.out.println("No Label for " + file + " has been found! Defaulting to 0.");
 				}
 				
 				System.out.println("Label: " + label);
 				ImageData[] imageDataArray = new ImageData[1];
 				imageDataArray[0] = new ImageData(grayvalues, label);
 				return imageDataArray;
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		else {
			System.out.println("Can't import Imagedate : Unknown Format");
 		}
 		return null;
 	}
 	
 	// Shuffles an array
     private static <T> void shuffle(T[] array) {
         for (int i = array.length; i > 1; i--) {
                 T temp = array[i - 1];
                 int randIx = (int) (Math.random() * i);
                 array[i - 1] = array[randIx];
                 array[randIx] = temp;
         }
     }
 }
