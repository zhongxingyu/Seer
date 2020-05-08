 package yuuki.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipFile;
 
 import yuuki.entity.Character;
 import yuuki.entity.Stat;
 import yuuki.entity.VariableStat;
 
 /**
  * Loads entity definition files.
  */
 public class EntityLoader extends CsvResourceLoader {
 	
 	/**
 	 * Creates a new EntityLoader for entity definition files at the specified
 	 * location.
 	 * 
 	 * @param directory The directory containing the definition files to be
 	 * loaded.
 	 */
 	public EntityLoader(File directory) {
 		super(directory);
 	}
 	
 	/**
 	 * Creates a new EntityLoader for resource files in the given ZIP file.
 	 *
 	 * @param archive The ZIP file containing the resource files to be loaded.
 	 * @param zipRoot The root within the ZIP file of all files to be loaded.
 	 * @param actions The ActionFactory to use for creating the definition
 	 * actions.
 	 */
 	public EntityLoader(ZipFile archive, String zipRoot) {
 		super(archive, zipRoot);
 	}
 	
 	/**
 	 * Loads the data from an entity definitions resource file into an
 	 * EntityFactory object.
 	 * 
 	 * @param resource The path to the entity definitions file to load,
 	 * relative to the resource root.
 	 * @return A map containing entity names mapped to the loaded entities.
 	 * @throws ResourceNotFoundException If the resource does not exist.
 	 * @throws ResourceFormatException
 	 * @throws IOException If an IOException occurs.
 	 */
 	public Map<String, Character.Definition> load(String resource) throws
 	ResourceNotFoundException, ResourceFormatException, IOException {
 		Map<String, Character.Definition> entities;
 		entities = new HashMap<String, Character.Definition>();
 		String[][] records = loadRecords(resource);
 		for (int i = 0; i < records.length; i++) {
 			try {
 				parseRecord(records, i, entities);
 			} catch (RecordFormatException e) {
 				throw new ResourceFormatException(resource, e);
 			}
 			advanceProgress(1.0 / records.length);
 		}
 		return entities;
 	}
 	
 	/**
 	 * Parses the attacks value into valid Actions by using the ActionFactory
 	 * in this EntityLoader.
 	 * 
 	 * @param value The exact value of the field containing the moves.
 	 * 
 	 * @return The Action indexes
 	 * 
 	 * @throws FieldFormatException If the moves field contains an invalid
 	 * value.
 	 */
 	private int[] parseMoves(String value) throws FieldFormatException {
 		String[] moves = splitMultiValue(value);
 		int[] actions = null;
 		try {
			actions = parseIntArray(moves, 0);
 		} catch (NumberFormatException e) {
 			throw new FieldFormatException("moves", value);
 		}
 		return actions;
 	}
 	
 	/**
 	 * Parses a record and adds it to the factory.
 	 * 
 	 * @param records The record list to get the record from.
 	 * @param num The index of the record being parsed.
 	 * @param map The map to add the record to.
 	 * 
 	 * @throws RecordFormatException If the given record is invalid.
 	 */
 	private void parseRecord(String[][] records, int num,
 			Map<String, Character.Definition> map) throws
 			RecordFormatException {
 		String[] r = records[num];
 		Character.Definition d = new Character.Definition();
 		d.name = r[0];
 		//d.disp = r[1].charAt(0);
 		try {
 			d.moves = parseMoves(r[2]);
 		} catch (FieldFormatException e) {
 			throw new RecordFormatException(num, e);
 		}
 		int hp = Integer.parseInt(r[3]);
 		int mp = Integer.parseInt(r[4]);
 		int str = Integer.parseInt(r[5]);
 		int def = Integer.parseInt(r[6]);
 		int agl = Integer.parseInt(r[7]);
 		int acc = Integer.parseInt(r[8]);
 		int mag = Integer.parseInt(r[9]);
 		int luk = Integer.parseInt(r[10]);
 		int hpg = Integer.parseInt(r[11]);
 		int mpg = Integer.parseInt(r[12]);
 		int strg = Integer.parseInt(r[13]);
 		int defg = Integer.parseInt(r[14]);
 		int aglg = Integer.parseInt(r[15]);
 		int accg = Integer.parseInt(r[16]);
 		int magg = Integer.parseInt(r[17]);
 		int lukg = Integer.parseInt(r[18]);
 		d.hp = new VariableStat("health", hp, hpg);
 		d.mp = new VariableStat("mana", mp, mpg);
 		d.str = new Stat("strength", str, strg);
 		d.def = new Stat("defense", def, defg);
 		d.agl = new Stat("agility", agl, aglg);
 		d.acc = new Stat("accuracy", acc, accg);
 		d.mag = new Stat("magic", mag, magg);
 		d.luk = new Stat("luck", luk, lukg);
 		d.xp = Integer.parseInt(r[19]);
 		d.overworldArt = r[20];
 		map.put(d.name.toLowerCase(), d);
 	}
 	
 }
