 package com.discretesoftworks.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.content.res.AssetManager;
 import android.graphics.BitmapFactory;
 
 public class ModelLoader {
 	public static String convertStreamToString(InputStream is) {
 		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
 		return s.hasNext() ? s.next() : "";
 	}
 	public static RenderModel loadOBJ(RenderModel object, String filename, AssetManager assets) throws IOException {
 		String file = convertStreamToString(assets.open(filename));
 		String[] lines = file.split("\\r?\\n");
 		String line;
 		
 		Sprite sprite = null;
 		
 //		ArrayList<Float> verticies = new ArrayList<Float>();
 		ArrayList<Float> positionCoords = new ArrayList<Float>();
 		ArrayList<Float> textureCoords = new ArrayList<Float>();
 		ArrayList<Float> vertexNormals = new ArrayList<Float>();
 		ArrayList<Short> faces = new ArrayList<Short>();
 		ArrayList<Short> facesTex = new ArrayList<Short>();
 		
 		for (int i = 0; i < lines.length; i++) {
 			line = lines[i];
 			switch(line.charAt(0)) {
 			case '#':
 				System.out.println("Comment!");
 				break;
 			case 'v':
 				//Vertex, or texture coord, or vertex normal
 				String[] coords = line.split(" ");
 				if (line.charAt(1) == 't') {
 					//texture
 					textureCoords.add(Float.parseFloat(coords[1]));
 					textureCoords.add(-Float.parseFloat(coords[2]));
 				} else if (line.charAt(1) == 'n') {
 					//normal
 					vertexNormals.add(Float.parseFloat(coords[1]));
 					vertexNormals.add(Float.parseFloat(coords[2]));
 					vertexNormals.add(Float.parseFloat(coords[3]));
 				} else if (line.charAt(1) == 'p') {
 					//Parameter Space
 				} else {
 					//Regular vertex
 					positionCoords.add(Float.parseFloat(coords[1]));
 					positionCoords.add(Float.parseFloat(coords[2]));
 					positionCoords.add(Float.parseFloat(coords[3]));
 				}
 				break;
 			case 'f':
 				//face
 				String[] tripIndic = line.split(" ");
 				for (short j = 1; j < 4; j++) {
 					short positionIndex = (short)(Short.parseShort(tripIndic[j].split("/")[0]) - 1);
 					short texIndex = (short)(Short.parseShort(tripIndic[j].split("/")[1]) - 1);
 
 					faces.add(positionIndex);
 					facesTex.add(texIndex);
 				}
 				
 				break;
 			case 'm':
 			{
 				//mtllib
 				String[] mtllibFileName = line.split(" ");
 				String[] mtllibFile = convertStreamToString(assets.open(mtllibFileName[1])).split("\\r?\\n");
 				String texFileName = "";
 				String[] mtllibFileLine = null;
 				for (int j = 0; j < mtllibFile.length; j++) {
 					mtllibFileLine = mtllibFile[j].split(" ");
 					if (mtllibFileLine[0].equals("map_Kd")) {
 						texFileName = mtllibFileLine[1];
 						break;
 					}
 				}
 				sprite = new Sprite(BitmapFactory.decodeStream(assets.open(texFileName)),1);
 			}
 				break;
 			case 'u':
 				//usemtl
 				break;
 			case 'o':
 				//object
 				break;
 			default:
 				//s off, etc
 					
 			}
 		}
		float[] verticiesArray = new float[(textureCoords.size()/2)*3];
		float[] texCoords = new float[textureCoords.size()];
		short[] indiciesArray = new short[facesTex.size()];
		for (int i = 0; i < textureCoords.size(); i++) {
			texCoords[i] = textureCoords.get(i);
 		}
		for (int i = 0; i < facesTex.size(); i++) {
			indiciesArray[i] = facesTex.get(i);
			verticiesArray[facesTex.get(i)*3] = positionCoords.get(faces.get(i)*3);
			verticiesArray[facesTex.get(i)*3+1] = positionCoords.get(faces.get(i)*3+1);
			verticiesArray[facesTex.get(i)*3+2] = positionCoords.get(faces.get(i)*3+2);
 		}
 		object.setupModel(verticiesArray, texCoords, indiciesArray);
 		object.setSprite(sprite);
		
 		return object;
 	}
 
 }
