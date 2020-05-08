 package genepi.io.legend;
 
 import genepi.io.FileUtil;
 import genepi.io.text.AbstractLineReader;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
import java.util.Set;
 
 public class LegendFileReader extends AbstractLineReader<String> {
 
 	private Map<Integer, Integer> index = new HashMap<Integer, Integer>();
 
	private Set<Integer> duplicates = new HashSet<Integer>();

 	private String line;
 
 	private int oldOffset = 0;
 
 	private BufferedReader myIn;
 
 	private LegendEntry entry = new LegendEntry();
 
 	private String population;
 
 	private int idCol = -1;
 	private int posCol = -1;
 	private int a0Col = -1;
 	private int a1Col = -1;
 	private int afrAafCol = -1;
 	private int amrAafCol = -1;
 	private int eurAafCol = -1;
 	private int asnAafCol = -1;
 
 	public LegendFileReader(DataInputStream inputStream) throws IOException {
 		super(inputStream);
 	}
 
 	public LegendFileReader(String filename, String population)
 			throws IOException {
 		super(filename);
 		this.population = population;
 	}
 
 	public void createIndex() throws IOException {
 
 		int offset = 0;
 
 		while (next()) {
 			String line = get();
 			if (!line.startsWith("id")) {
 				String[] tiles = line.split(" ", 3);
 				int position = Integer.parseInt(tiles[posCol]);

				//store duplicates
				if (index.containsKey(position)) {
					duplicates.add(position);
				}

 				index.put(position, offset);

 			} else {
 
 				// parse header
 				String[] tiles = line.split(" ");
 				int i = 0;
 				for (String tile : tiles) {
 					if (tile.equals("id")) {
 						idCol = i;
 					}
 					if (tile.equals("position")) {
 						posCol = i;
 					}
 					if (tile.equals("a0")) {
 						a0Col = i;
 					}
 					if (tile.equals("a1")) {
 						a1Col = i;
 					}
 					if (tile.equals("afr.aaf")) {
 						afrAafCol = i;
 					}
 					if (tile.equals("amr.aaf")) {
 						amrAafCol = i;
 					}
 					if (tile.equals("eur.aaf")) {
 						eurAafCol = i;
 					}
 					if (tile.equals("asn.aaf")) {
 						asnAafCol = i;
 					}
 
 					i++;
 				}
 
 			}
 			offset += line.length() + 1;
 		}
 		close();
 	}
 
 	@Override
 	public String get() {
 		return line;
 	}
 
 	public String findByPosition(int position) throws IOException {
 
		//ignore duplicates
		if (duplicates.contains(position)){
			return null;
		}
		
 		Integer offset = index.get(position);
 		if (offset != null) {
 
 			FileInputStream inputStream = new FileInputStream(getFilename());
 			InputStream in2 = FileUtil.decompressStream(inputStream);
 			BufferedReader in = new BufferedReader(new InputStreamReader(in2));
 			in.skip(offset);
 			String line = in.readLine();
 			in.close();
 			return line;
 
 		} else {
 
 			return null;
 
 		}
 	}
 
 	public void initSearch() throws IOException {
 		FileInputStream inputStream = new FileInputStream(getFilename());
 		InputStream in2 = FileUtil.decompressStream(inputStream);
 		myIn = new BufferedReader(new InputStreamReader(in2));
 	}
 
 	public LegendEntry findByPosition2(int position) throws IOException {
 
 		Integer offset = index.get(position);
 		if (offset != null) {
 
 			myIn.skip(offset - oldOffset);
 			String line = myIn.readLine();
 			oldOffset = offset + line.length() + 1;
 
 			String[] tiles = line.split(" ");
 
 			entry.setRsId(tiles[idCol]);
 			entry.setAlleleA(tiles[a0Col].charAt(0));
 			entry.setAlleleB(tiles[a1Col].charAt(0));
 			entry.setType("-");
 
 			float aaf = 0;
 
 			if (population.equals("afr")) {
 				aaf = Float.parseFloat(tiles[afrAafCol]);
 			}
 
 			if (population.equals("amr")) {
 				aaf = Float.parseFloat(tiles[amrAafCol]);
 			}
 
 			if (population.equals("asn")) {
 				aaf = Float.parseFloat(tiles[asnAafCol]);
 			}
 
 			if (population.equals("eur")) {
 				aaf = Float.parseFloat(tiles[eurAafCol]);
 			}
 
 			entry.setFrequencyA(1 - aaf);
 			entry.setFrequencyB(aaf);
 
 			return entry;
 
 		} else {
 
 			return null;
 
 		}
 	}
 
 	@Override
 	public Iterator<String> iterator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void parseLine(String arg0) throws Exception {
 		line = arg0;
 	}
 
 }
