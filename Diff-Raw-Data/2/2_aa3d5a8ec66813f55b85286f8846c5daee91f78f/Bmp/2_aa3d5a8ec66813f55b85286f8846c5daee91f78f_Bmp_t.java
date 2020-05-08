 /**
  * 
  */
 package image.bmp;
 
 import image.Image;
 
 import java.io.*;
 /**
  * @author Duan Jiong <djduanjiong@gmail.com>
  *
  */
 public class Bmp {
 	BmpFileHeader file_h;
 	BmpInfoHeader info_h;
 	
 	public Bmp() {
 		file_h = new BmpFileHeader();
 		info_h = new BmpInfoHeader();
 	}
 	
 	public Image bmpToImage(String filename) {
 		int width;
 		int heigth;
 		int numcomps;
 		int pad, w, h;
 		byte[] rgb;
 		Image image = null;
 		RandomAccessFile dataInput = null;
 		try {
 			dataInput = new RandomAccessFile(filename, "r");
 			file_h.bfType = dataInput.readUnsignedShort();
 			
 			if (file_h.bfType != 19778) {
 				System.out.println("Error, not a Bmp file!");
 				dataInput.close();
 				return null;
 			} else {
 				/*
 				 * file header
 				 */
 				file_h.bfSize = dataInput.readUnsignedByte();
 				file_h.bfSize += (dataInput.readUnsignedByte()<<8);
 				file_h.bfSize += (dataInput.readUnsignedByte()<<16);
 				file_h.bfSize += (dataInput.readUnsignedByte()<<24);
 				
 				//two reserverd fileds
 				dataInput.readUnsignedShort();
 				dataInput.readUnsignedShort();
 				
 				file_h.bfOffBits = dataInput.readUnsignedByte();
 				file_h.bfOffBits += (dataInput.readUnsignedByte()<<8);
 				file_h.bfOffBits += (dataInput.readUnsignedByte()<<16);
 				file_h.bfOffBits += (dataInput.readUnsignedByte()<<24);
 				
 				
 				/*
 				 * info header
 				 */
 				info_h.biSize = dataInput.readUnsignedByte();
 				info_h.biSize += (dataInput.readUnsignedByte()<<8);
 				info_h.biSize += (dataInput.readUnsignedByte()<<16);
 				info_h.biSize += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biWidth = dataInput.readUnsignedByte();
 				info_h.biWidth += (dataInput.readUnsignedByte()<<8);
 				info_h.biWidth += (dataInput.readUnsignedByte()<<16);
 				info_h.biWidth += (dataInput.readUnsignedByte()<<24);
 				width = (int)info_h.biWidth;
 				
 				info_h.biHeigth = dataInput.readUnsignedByte();
 				info_h.biHeigth += (dataInput.readUnsignedByte()<<8);
 				info_h.biHeigth += (dataInput.readUnsignedByte()<<16);
 				info_h.biHeigth += (dataInput.readUnsignedByte()<<24);
 				heigth = (int)info_h.biHeigth;
 				
 				info_h.biPlanes = dataInput.readUnsignedShort();
 				
 				info_h.biBitCount = dataInput.readUnsignedShort();
 				
 				info_h.biCompression = dataInput.readUnsignedByte();
 				info_h.biCompression += (dataInput.readUnsignedByte()<<8);
 				info_h.biCompression += (dataInput.readUnsignedByte()<<16);
 				info_h.biCompression += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biSizeImage = dataInput.readUnsignedByte();
 				info_h.biSizeImage += (dataInput.readUnsignedByte()<<8);
 				info_h.biSizeImage += (dataInput.readUnsignedByte()<<16);
 				info_h.biSizeImage += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biXpelsPerMeter = dataInput.readUnsignedByte();
 				info_h.biXpelsPerMeter += (dataInput.readUnsignedByte()<<8);
 				info_h.biXpelsPerMeter += (dataInput.readUnsignedByte()<<16);
 				info_h.biXpelsPerMeter += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biYpelsPerMeter = dataInput.readUnsignedByte();
 				info_h.biYpelsPerMeter += (dataInput.readUnsignedByte()<<8);
 				info_h.biYpelsPerMeter += (dataInput.readUnsignedByte()<<16);
 				info_h.biYpelsPerMeter += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biClrUsed = dataInput.readUnsignedByte();
 				info_h.biClrUsed += (dataInput.readUnsignedByte()<<8);
 				info_h.biClrUsed += (dataInput.readUnsignedByte()<<16);
 				info_h.biClrUsed += (dataInput.readUnsignedByte()<<24);
 				
 				info_h.biClrImportant = dataInput.readUnsignedByte();
 				info_h.biClrImportant += (dataInput.readUnsignedByte()<<8);
 				info_h.biClrImportant += (dataInput.readUnsignedByte()<<16);
 				info_h.biClrImportant += (dataInput.readUnsignedByte()<<24);
 				
 			}
 			
 			if (info_h.biBitCount == 24) {
 				image = new Image(heigth, width, 3);
 				if (image == null) {
 					dataInput.close();
 					return null;
 				}
 				
 				dataInput.seek(0);
 				dataInput.seek(file_h.bfOffBits);
 				
 				w = (int)info_h.biWidth;
 				h = (int)info_h.biHeigth;
 				
 				pad = ((3*w)%4 != 0) ? (4-(3*w)%4) : 0;
 				rgb = new byte[(3*w+pad)*h];
 				dataInput.read(rgb);
 				
 				int index = 0;
 				for (int y = 0; y < h; y++) {
 					for (int x = 0; x < w; x++) {
 						image.comps[0][index] = rgb[(3*w+pad)*(h-1-y)+3*x];
 						image.comps[1][index] = rgb[(3*w+pad)*(h-1-y)+3*x];
 						image.comps[2][index] = rgb[(3*w+pad)*(h-1-y)+3*x];
 						index++;
 					}
 				}
 				
			} else {
				System.out.println("the file format is not supported!");
 			}
 			
 			dataInput.close();
 			return image;
 		} catch (Exception e) {
 			// TODO Զɵ catch 
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 }
 
 class BmpFileHeader {
 	int bfType =0;
 	long bfSize =0;
 	long bfOffBits =0;
 }
 
 class BmpInfoHeader {
 	long biSize;
 	long biWidth;
 	long biHeigth;
 	int biPlanes;
 	int biBitCount;
 	long biCompression;
 	long biSizeImage;
 	long biXpelsPerMeter;
 	long biYpelsPerMeter;
 	long biClrUsed;
 	long biClrImportant;
 }
