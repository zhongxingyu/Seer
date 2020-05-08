 package me.kevinwells.darxen.loaders;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import me.kevinwells.darxen.data.DataFile;
 import me.kevinwells.darxen.data.RadialDataPacket;
 import me.kevinwells.darxen.data.RadialPacket;
 import me.kevinwells.darxen.model.Buffers;
 import me.kevinwells.darxen.model.Palette;
 import me.kevinwells.darxen.model.PaletteType;
 import me.kevinwells.darxen.model.RadarData;
 import me.kevinwells.darxen.model.RadarRenderData;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
import android.util.FloatMath;
 
 public class RenderRadar extends CachedAsyncLoader<RadarRenderData> {
 
 	private static final String ARG_DATA = "Data";
 	
 	public static Bundle bundleArgs(RadarData data) {
 		Bundle args = new Bundle();
 		args.putParcelable(ARG_DATA, data);
 		return args;
 	}
 	
 	public static RenderRadar createInstance(Context context, Bundle args) {
 		RadarData data = args.getParcelable(ARG_DATA);
 		return new RenderRadar(context, data);
 	}
 
 	public static RenderRadar getInstance(LoaderManager manager, int id) {
 		Loader<RadarRenderData> res = manager.getLoader(id);
 		return (RenderRadar)res;
 	}
 	
 	private RadarData mData;
 	
 	private RenderRadar(Context context, RadarData data) {
 		super(context);
 		mData = data;
 	}
 	
 	public RadarData getData() {
 		return mData;
 	}
 
 	@Override
 	protected RadarRenderData doInBackground() {
 		
 		if (mData.getRadarRenderData() != null) {
 			return mData.getRadarRenderData();
 		}
 		
 		Thread.currentThread().setPriority(Thread.NORM_PRIORITY+2);
 		
 		DataFile file = mData.getDataFile();
 		RadarRenderData renderData = new RadarRenderData();
 		
 		//require a file to render
 		if (file == null) {
 			return renderData;
 		}
 		
 		//set the palette
 		renderData.setPalette(getPalette(file));
 		
 		//render the radial data
 		RadialDataPacket packet = (RadialDataPacket)file.description.symbologyBlock.packets[0];
 		renderRadialData(packet, renderData);
 		
 		//cache the result and return
 		mData.setRadarRenderData(renderData);
 		return renderData;
 	}
 	
 	private static Palette getPalette(DataFile file) {
 		switch (file.description.opmode) {
 		case PRECIPITATION:
 			return new Palette(PaletteType.REFLECTIVITY_PRECIPITATION);
 			
 		case CLEAN_AIR:
 			return new Palette(PaletteType.REFLECTIVITY_CLEAN_AIR);
 			
 		case MAINTENANCE:
 			return new Palette(PaletteType.REFLECTIVITY_CLEAN_AIR);
 			
 		default:
 			return new Palette(PaletteType.REFLECTIVITY_CLEAN_AIR);
 		}
 	}
 
 	private void renderRadialData(RadialDataPacket packet, RadarRenderData renderData) {
 		float kmPerRangeBin = 1.0f;
 		
 		FloatBuffer[] radialBuffers = new FloatBuffer[16];
 		int[] radialSizes = new int[16];
 		
 		for (int i = 1; i < 16; i++) {
 			TriangleBuffer buffer = new TriangleBuffer();
 			for (int az = 0; az < packet.radials.length; az++) {
 				RadialPacket radial = packet.radials[az];
 				
 				float start = 90.0f - (radial.start + radial.delta);
 				float end = start + radial.delta;
				float cosx1 = FloatMath.cos((float)Math.toRadians(start));
				float siny1 = FloatMath.sin((float)Math.toRadians(start));
				float cosx2 = FloatMath.cos((float)Math.toRadians(end));
				float siny2 = FloatMath.sin((float)Math.toRadians(end));
 				
 				int startRange = 0;
 				for (int range = 0; range < radial.codes.length; range++) {
 					int color = radial.codes[range];
 					if (startRange == 0 && color == i)
 						startRange = range;
 					
 					if ((startRange != 0 && color < i) ||
 							(startRange != 0 && (range == packet.rangeBinCount-1))) {
 						if (color >= i && range == packet.rangeBinCount-1)
 							range++;
 						Vertex v1 = new Vertex(
 								(startRange-1) * kmPerRangeBin * cosx1,
 								(startRange-1) * kmPerRangeBin * siny1);
 						Vertex v2 = new Vertex(
 								(range-1) * kmPerRangeBin * cosx1,
 								(range-1) * kmPerRangeBin * siny1);
 						Vertex v3 = new Vertex(
 								(range-1) * kmPerRangeBin * cosx2,
 								(range-1) * kmPerRangeBin * siny2);
 						Vertex v4 = new Vertex(
 								(startRange-1) * kmPerRangeBin * cosx2,
 								(startRange-1) * kmPerRangeBin * siny2);
 						
 						buffer.addTriangle(v1, v2, v3);
 						buffer.addTriangle(v3, v4, v1);
 						startRange = 0;
 					}
 				}
 			}
 			radialBuffers[i] = buffer.getBuffer();
 			radialSizes[i] = buffer.size();
 		}
 		renderData.setBuffers(radialBuffers, radialSizes);
 	}
 	
 	public class Vertex {
 		public float x;
 		public float y;
 		public Vertex(float x, float y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 	
 	public class TriangleBuffer {
 		
 		private class Triangle {
 			public Vertex p1;
 			public Vertex p2;
 			public Vertex p3;
 			public Triangle(Vertex p1, Vertex p2, Vertex p3) {
 				this.p1 = p1;
 				this.p2 = p2;
 				this.p3 = p3;
 			}
 		}
 		
 		private ArrayList<Triangle> buffer;
 		
 		public TriangleBuffer() {
 			buffer = new ArrayList<Triangle>();
 		}
 		
 		public int size() {
 			return buffer.size() * 3;
 		}
 
 		public void addTriangle(Vertex p1, Vertex p2, Vertex p3) {
 			buffer.add(new Triangle(p1, p2, p3));
 		}
 		
 		public FloatBuffer getBuffer() {
 			FloatBuffer buf = Buffers.allocateFloat(buffer.size() * 2 * 3);
 			
 			for (Triangle t : buffer) {
 				buf.put(t.p1.x);
 				buf.put(t.p1.y);
 				buf.put(t.p2.x);
 				buf.put(t.p2.y);
 				buf.put(t.p3.x);
 				buf.put(t.p3.y);
 			}
 			buf.position(0);
 			return buf;
 		}
 	}
 }
