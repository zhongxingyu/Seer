 package rebecca.jpeg;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Jpeg {
 	
 	private int precision;
 	private int height, width, components;
 	public MCU first;
 	public MCU second;
 	
 	public MCU mcu[];
 	
 	private Component[] component = new Component[3];
 	
 	public Jpeg(String file) throws IOException {
 		BitStream stream = new BitStream(file);
 		
 		// Signature
 		int signature = stream.read();
 		
 		if(signature == -0xd8)
 			System.out.println("Jpeg JFIF");
 		else
 			System.exit(0);
 		
 		// Metadata
 		int app0 = stream.read();
 		
 		if(app0 == -0xe1)
 		{
 			System.out.println("Exif");
 			int length = stream.read()*256;
 			length += stream.read();
 			
 			System.out.println(""+length);
 			for(int i = 0; i < length-2; i++) // TODO: Read metadata
 				stream.readraw();
 		}
 		else
 			System.exit(0);
 		
 		System.out.println(stream.read()+"");//-219 0xDB
 		System.out.println(stream.read()+"");
 		System.out.println(stream.read()+"");
 		
 		//System.exit(0);
 		
 		ArrayList<QTable> qtabs = new ArrayList<QTable>();
 		
 		// Quantization tables
 		int tablevalue = stream.read();
 		while(tablevalue >= 0)
 		{
 			
 			qtabs.add(new QTable(tablevalue, stream));
 			
 			System.out.print(qtabs.get(qtabs.size()-1));
 
 			
 			tablevalue = stream.read();
 		}
 		System.out.println(tablevalue+"");
 		// File info
 		//System.out.println(stream.read()+"");
 		
 		sof0(stream);
 		
 		//System.exit(0);
 		
 		// read huffman tables
 		
 		System.out.println(stream.read()+""); // -196 C4
 		HuffmanTables huff = new HuffmanTables(stream);
 		
 
 		
 		// Start of scan
 		System.out.println(stream.read()+"");
 		System.out.println(stream.read()+"");
 		System.out.println(stream.read()+"");
 		
 		MCU.setup(stream, component);
 		
 		
 		stream.read(); stream.read(); stream.read();
 		
		mcu = new MCU[20*15];
 		
		for(int i = 0; i < 20*15; i++) {
			System.out.println("NEW MCU"+i);
 			mcu[i] = new MCU(stream, huff, component, qtabs);
 		}
 		
 		//first = new MCU(stream, huff, component, qtabs);
 		//second = new MCU(stream, huff, component, qtabs);
 		
 		//first.paint();
 		
 		
 	}
 	
 	private void sof0(BitStream s) throws IOException {
 		int length;
 		length = 256*s.read();
 		length += s.read();
 		
 		precision = s.read();
 		
 		height = s.read() * 256;
 		height += s.read();
 		
 		width = s.read() * 256;
 		width += s.read();
 				
 		components = s.read();
 		
 
 		System.out.println(height + "x" + width + " c: "+components);
 		
 		int hMin = 4, vMin = 4;
 		
 		for(int i = 0; i < components; i++) {
 			int j = s.read() - 1;
 			int sampling = s.read();
 			int qTable = s.read();
 			
 			component[j] = new Component();
 			
 			component[j].colorSpace = i; // Set colorspace according to position in file
 			component[j].hSampleRate = (0xf0 & sampling) >> 4;
 			if(component[j].hSampleRate < hMin) hMin = component[j].hSampleRate;
 			component[j].vSampleRate = 0x0f & sampling;
 			if(component[j].vSampleRate < vMin) vMin = component[j].vSampleRate;
 			component[j].qTable = qTable;
 			
 			System.out.println(component[j].hSampleRate + "x" + component[j].vSampleRate);
 		}
 		MCU.setNumberOfComponents(components);
 		for(int i = 0; i < components; i++) {
 			MCU.hComp[i] = component[i].hSampleRate / hMin;
 			MCU.vComp[i] = component[i].vSampleRate / vMin;
 		}
 		
 	}
 	
 }
