 package importers;
 
 import java.io.*;
 
 import main.*;
 import math.matrices.*;
 import openGLCLInterfaces.Enumerations.BufferTarget;
 import openGLCLInterfaces.Enumerations.BufferUsage;
 import openGLCLInterfaces.Enumerations.DataType;
 import openGLCLInterfaces.openGL.buffers.*;
 
 public class CMOMLoader {
 	private String[] ambientMaps, diffuseMaps, specularMaps, emissiveMaps, dissolveMaps, bumpMaps;
 	private byte[] ambient, emissive, diffuse, specular, dissolve, shinyness, vertexData;
 	private int matCount, vertexCount, vbCount;
 	private int[] pos;
 	private File file;
 	private MaterialLibrary materialLibrary;
 
 	private CMOMLoader(File file) {
 		this.file = file;
 	}
 
 	public static Model loadModelFromCMOM(File file) throws IOException {
 		return new CMOMLoader(file).load();
 	}
 
 	private Model load() throws FileNotFoundException, IOException {
 		materialLibrary = new MaterialLibrary();
 		int index = 0;
 		byte[] data = Utils.fastReadInflated(new FileInputStream(file));
 		int ambientMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		ambientMaps = new String[ambientMC];
 		for (int i = 0; i < ambientMC; i++) {
 			int ambientCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			ambientMaps[i] = ByteConverter.byteToString(data, index, ambientCC);
 			index += ambientCC;
 		}
 
 		int diffuseMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		diffuseMaps = new String[diffuseMC];
 		for (int i = 0; i < diffuseMC; i++) {
 			int diffuseCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			diffuseMaps[i] = ByteConverter.byteToString(data, index, diffuseCC);
 			index += diffuseCC;
 		}
 
 		int specularMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		specularMaps = new String[specularMC];
 		for (int i = 0; i < specularMC; i++) {
 			int specularCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			specularMaps[i] = ByteConverter.byteToString(data, index, specularCC);
 			index += specularCC;
 		}
 
 		int emissiveMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		emissiveMaps = new String[emissiveMC];
 		for (int i = 0; i < emissiveMC; i++) {
 			int emissiveCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			emissiveMaps[i] = ByteConverter.byteToString(data, index, emissiveCC);
 			index += emissiveCC;
 		}
 
 		int dissolveMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		dissolveMaps = new String[dissolveMC];
 		for (int i = 0; i < dissolveMC; i++) {
 			int dissolveCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			dissolveMaps[i] = ByteConverter.byteToString(data, index, dissolveCC);
 			index += dissolveCC;
 		}
 
 		int bumpMC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		bumpMaps = new String[bumpMC];
 		for (int i = 0; i < ambientMC; i++) {
 			int bumpCC = ByteConverter.byteToInt(data, index);
 			index += 4;
 			bumpMaps[i] = ByteConverter.byteToString(data, index, bumpCC);
 			index += bumpCC;
 		}
 
 		int ambientC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		ambient = new byte[ambientC];
 		System.arraycopy(data, index, ambient, 0, ambientC);
 		index += ambientC;
 
 		int diffuseC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		diffuse = new byte[diffuseC];
 		System.arraycopy(data, index, diffuse, 0, diffuseC);
 		index += diffuseC;
 
 		int specularC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		specular = new byte[specularC];
 		System.arraycopy(data, index, specular, 0, specularC);
 		index += specularC;
 
 		int emissiveC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		emissive = new byte[emissiveC];
 		System.arraycopy(data, index, emissive, 0, emissiveC);
 		index += emissiveC;
 
 		int dissolveC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		dissolve = new byte[dissolveC];
 		System.arraycopy(data, index, dissolve, 0, dissolveC);
 		index += dissolveC;
 
 		int shinynessC = ByteConverter.byteToInt(data, index);
 		index += 4;
 		shinyness = new byte[shinynessC];
 		System.arraycopy(data, index, shinyness, 0, shinynessC);
 		index += shinynessC;
 
 		materialLibrary.size = (Vector2) Matrix.vec(ByteConverter.byteToFloats(data, index, 2));
 		index += 8;
 
 		matCount = ByteConverter.byteToInt(data, index);
 		index += 4;
 
 		loadMaterialLibrary();
 
 		vertexCount = ByteConverter.byteToInt(data, index);
 		index += 4;
 		pos = ByteConverter.byteToInts(data, index, 6);
 		index += 6 * 4;
 		vbCount = ByteConverter.byteToInt(data, index);
 		index += 4;
 		vertexData = new byte[vbCount];
 		System.arraycopy(data, index, vertexData, 0, vbCount);
 		DirectVertexBuffer vb = new DirectVertexBuffer(vertexData, pos, new int[] { 0, 1, 2, 3, 4,
 				5 }, new int[] { 3, 2, 3, 1, 3, 3 }, new DataType[] { DataType.FLOAT,
 				DataType.FLOAT, DataType.FLOAT, DataType.FLOAT, DataType.FLOAT, DataType.FLOAT });
 		return new Model(vb, vertexCount, materialLibrary);
 	}
 
 	private File[] toFileArray(String[] array) {
 		File[] files = new File[array.length];
 		for (int i = 0; i < array.length; i++) {
 			if (array[i].length() == 0) {
 				files[i] = null;
 				continue;
 			}
 			files[i] = new File(file.getParent(), array[i]);
 		}
 		return files;
 	}
 
 	private void loadMaterialLibrary() {
 		byte[] def = new byte[(int) (materialLibrary.size.getX() * materialLibrary.size.getY()) * 4];
 		for (int i = 0; i < def.length; i++) {
 			def[i] = (byte) 255;
 		}
 		byte[] bump = new byte[(int) (materialLibrary.size.getX() * materialLibrary.size.getY()) * 4];
 		for (int i = 0; i < bump.length; i += 4) {
 			bump[i] = (byte) 128;
 			bump[i + 1] = (byte) 128;
 			bump[i + 2] = (byte) 128;
 			bump[i + 3] = (byte) 255;
 		}
 		Texture ambientTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(ambientMaps),
 				def, (int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		Texture diffuseTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(diffuseMaps),
 				def, (int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		Texture specularTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(specularMaps),
 				def, (int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		Texture emissiveTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(emissiveMaps),
 				def, (int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		Texture dissolveTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(dissolveMaps),
 				def, (int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		Texture bumpTex = TextureLoader.load2DTextureArrayFromCTEX(toFileArray(bumpMaps), bump,
 				(int) materialLibrary.size.getX(), (int) materialLibrary.size.getY());
 		BufferObject ambientBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		BufferObject diffuseBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		BufferObject specularBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		BufferObject emissiveBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		BufferObject dissolveBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		BufferObject shinynessBuffer = new BufferObject(BufferTarget.TEXTURE_BUFFER,
 				BufferUsage.STATIC_DRAW);
 		ambientBuffer.bind();
 		ambientBuffer.bufferData(ambient);
 		ambientBuffer.unbind();
 		diffuseBuffer.bind();
 		diffuseBuffer.bufferData(diffuse);
 		diffuseBuffer.unbind();
 		specularBuffer.bind();
 		specularBuffer.bufferData(specular);
 		specularBuffer.unbind();
 		emissiveBuffer.bind();
 		emissiveBuffer.bufferData(emissive);
 		emissiveBuffer.unbind();
 		dissolveBuffer.bind();
 		dissolveBuffer.bufferData(dissolve);
 		dissolveBuffer.unbind();
 		shinynessBuffer.bind();
 		shinynessBuffer.bufferData(shinyness);
 		shinynessBuffer.unbind();
 		Texture ambientV = TextureLoader.loadTextureBuffer(ambientBuffer);
 		Texture diffuseV = TextureLoader.loadTextureBuffer(diffuseBuffer);
 		Texture specularV = TextureLoader.loadTextureBuffer(specularBuffer);
 		Texture emissiveV = TextureLoader.loadTextureBuffer(emissiveBuffer);
 		Texture shinynessV = TextureLoader.loadTextureBuffer(shinynessBuffer);
 		Texture dissolveV = TextureLoader.loadTextureBuffer(dissolveBuffer);
 		materialLibrary.ambient = ambientV;
 		materialLibrary.diffuse = diffuseV;
 		materialLibrary.specular = specularV;
 		materialLibrary.emissive = emissiveV;
 		materialLibrary.shinyness = shinynessV;
 		materialLibrary.dissolve = dissolveV;
 		materialLibrary.ambientMaps = ambientTex;
 		materialLibrary.diffuseMaps = diffuseTex;
 		materialLibrary.specularMaps = specularTex;
 		materialLibrary.emissiveMaps = emissiveTex;
 		materialLibrary.dissolveMaps = dissolveTex;
 		materialLibrary.bumpMaps = bumpTex;
 		BufferObject buffer = new BufferObject(BufferTarget.UNIFORM_BUFFER, BufferUsage.STATIC_DRAW);
 		buffer.bind(1);
		buffer.bufferData(new int[] { matCount });
 		materialLibrary.buffer = buffer;
 	}
 }
