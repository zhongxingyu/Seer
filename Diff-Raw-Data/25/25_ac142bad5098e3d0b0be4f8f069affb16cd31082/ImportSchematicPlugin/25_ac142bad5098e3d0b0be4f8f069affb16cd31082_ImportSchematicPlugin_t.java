 package jo.sm.plugins.ship.imp;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
import jo.sm.data.BlockTypes;
 import jo.sm.data.SparseMatrix;
 import jo.sm.data.StarMade;
 import jo.sm.logic.utils.IntegerUtils;
 import jo.sm.logic.utils.ResourceUtils;
 import jo.sm.logic.utils.XMLUtils;
 import jo.sm.mods.IBlocksPlugin;
 import jo.sm.mods.IPluginCallback;
 import jo.sm.plugins.ship.imp.nbt.IO;
 import jo.sm.plugins.ship.imp.nbt.Tag;
 import jo.sm.ship.data.Block;
 import jo.sm.ui.BlockTypeColors;
 import jo.vecmath.Point3i;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 public class ImportSchematicPlugin implements IBlocksPlugin
 {
     public static final String NAME = "Import/Schematic";
     public static final String DESC = "Import Schematic file";
     public static final String AUTH = "Jo Jaquinta";
     public static final int[][] CLASSIFICATIONS = 
         {
         { TYPE_SHIP, SUBTYPE_FILE, 25 },
         { TYPE_STATION, SUBTYPE_FILE, 25 },
         { TYPE_SHOP, SUBTYPE_FILE, 25 },
         { TYPE_PLANET, SUBTYPE_FILE, 25 },
         { TYPE_FLOATINGROCK, SUBTYPE_FILE, 25 },
         };
     private static final Map<Integer,MCMap> BLOCK_MAP = new HashMap<Integer,MCMap>();
     private static long mLastRead = 0;
 
     @Override
     public String getName()
     {
         return NAME;
     }
 
     @Override
     public String getDescription()
     {
         return DESC;
     }
 
     @Override
     public String getAuthor()
     {
         return AUTH;
     }
 
     @Override
     public Object newParameterBean()
     {
         return new ImportSchematicParameters();
     }
 	@Override
 	public void initParameterBean(SparseMatrix<Block> original, Object params,
 			StarMade sm, IPluginCallback cb)
 	{
 	}
 
     @Override
     public int[][] getClassifications()
     {
         return CLASSIFICATIONS;
     }
 
     @Override
     public SparseMatrix<Block> modify(SparseMatrix<Block> original,
             Object p, StarMade sm, IPluginCallback cb)
     {
     	readData(sm);
     	ImportSchematicParameters params = (ImportSchematicParameters)p;        
         try
         {
         	Point3i center = new Point3i(8, 8, 8);
         	if ((sm.getSelectedLower() != null) && (sm.getSelectedUpper() != null))
         	{
         		center.set(sm.getSelectedLower());
         		center.add(sm.getSelectedUpper());
         		center.scale(1, 2);
         	}
            SparseMatrix<Block> modified = new SparseMatrix<Block>();
             readFile(params.getFile(), modified, center, cb);
            modified.set(8, 8, 8, new Block(BlockTypes.CORE_ID));
             return modified;
         }
         catch (Exception e)
         {
             e.printStackTrace();
             return null;
         }
     }
     
     private void readFile(String objFile, SparseMatrix<Block> grid, Point3i center, IPluginCallback cb) throws Exception
     {
         Tag.Compound schematic;
         try
         {
             DataInputStream rdr = new DataInputStream(new FileInputStream(new File(objFile)));
             schematic = IO.Read(rdr);
             rdr.close();
         }
         catch (Exception e)
         {
             DataInputStream rdr = new DataInputStream(new FileInputStream(new File(objFile)));
             schematic = IO.ReadUncompressed(rdr);
             rdr.close();
         }
     	Tag.Short width = (Tag.Short)schematic.Get("Width");
     	Tag.Short height = (Tag.Short)schematic.Get("Height");
     	Tag.Short length = (Tag.Short)schematic.Get("Length");
     	int xSize = width.v;
     	int ySize = height.v;
     	int zSize = length.v;
     	center.x -= xSize/2;
     	center.y -= ySize/2;
     	center.z -= zSize/2;
     	cb.setStatus("Importing...");
     	cb.startTask(xSize*ySize*zSize);
     	Tag.ByteArray blocks = (Tag.ByteArray)schematic.Get("Blocks");
     	Tag.ByteArray data = (Tag.ByteArray)schematic.Get("Data");
     	int idx = 0;
 		for (int y = 0; y < ySize; y++)
     		for (int z = 0; z < zSize; z++)
     	    	for (int x = 0; x < xSize; x++)
     			{
     				cb.workTask(1);
     				int blockID = blocks.v[idx];
     				int dataID = data.v[idx];
     				idx++;
     				MCMap map = BLOCK_MAP.get((dataID<<8)|blockID);
     				if (map == null)
     					map = BLOCK_MAP.get(blockID);
     				if (map == null)
     					continue;
     				if (map.mSMBlock > 0)
     					grid.set(center.x + x, center.y + y, center.z + z, new Block((short)map.mSMBlock));
     			}
     	cb.endTask();
     }
 
     private void readData(StarMade sm)
     {
     	File jo_plugins = new File(sm.getBaseDir(), "jo_plugins");
     	File minecraftTypes = new File(jo_plugins, "schematic_map.xml");
     	if (minecraftTypes.exists())
     	{
     		if (minecraftTypes.lastModified() <= mLastRead)
     			return;
     		try
 			{
 				readDataFile(new FileInputStream(minecraftTypes));
 	    		mLastRead = minecraftTypes.lastModified();
 	    		return;
 			} catch (FileNotFoundException e)
 			{
 				e.printStackTrace();
 			}
     	}
     	if (BLOCK_MAP.size() == 0)
     		readDataFile(ResourceUtils.loadSystemResourceStream("schematic_map.xml", ImportSchematicPlugin.class));
     }
     
     private void readDataFile(InputStream is)
     {
     	Document doc = XMLUtils.readStream(is);
     	if (doc == null)
     		return;
     	BLOCK_MAP.clear();
     	for (Node b : XMLUtils.findNodes(doc, "blockMap/block"))
     	{
     		MCMap map = new MCMap();
     		String mcBlock = XMLUtils.getAttribute(b, "mcBlock");
     		if (MinecraftTypes.NAME_TO_ID.containsKey(mcBlock))
     			map.mMCBlock = MinecraftTypes.NAME_TO_ID.get(mcBlock);
     		else
    		{
    			int mcBlockNum = IntegerUtils.parseInt(mcBlock);
    			if (mcBlockNum != 0)
    				map.mMCBlock = IntegerUtils.parseInt(mcBlock);
    			else
    			{
    				System.out.println("Unknown MC Block type: "+mcBlock);
    				continue;
    			}
    		}
     		map.mMCData = IntegerUtils.parseInt(XMLUtils.getAttribute(b, "mcData"));
     		String smBlock = XMLUtils.getAttribute(b, "smBlock");
     		if (BlockTypeColors.mBlockTypes.containsKey(smBlock))
     			map.mSMBlock = IntegerUtils.parseInt(BlockTypeColors.mBlockTypes.getProperty(smBlock));
     		else
     			map.mSMBlock = IntegerUtils.parseInt(smBlock);
     		int idx = map.mMCBlock;
     		if (map.mMCData != 0)
     			idx |= map.mMCData<<8;
     		BLOCK_MAP.put(idx, map);
     	}
     }
     
     class MCMap
     {
     	public int mMCBlock;
     	public int mMCData;
     	public int mSMBlock;
     }
 }
