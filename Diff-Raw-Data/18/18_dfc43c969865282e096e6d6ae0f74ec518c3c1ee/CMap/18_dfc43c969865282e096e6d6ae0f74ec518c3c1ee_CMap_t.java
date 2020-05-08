 package cubetech.collision;
 
 import cubetech.Block;
 import cubetech.Game.SpawnEntities;
 import cubetech.Game.SpawnEntity;
 import cubetech.common.Common;
 import cubetech.common.Common.ErrorCode;
 import cubetech.common.Helper;
 import cubetech.gfx.CubeMaterial;
 import cubetech.gfx.CubeTexture;
 import cubetech.gfx.ResourceManager;
 import cubetech.misc.Ref;
 import cubetech.net.NetBuffer;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  * Loaded instance of a collision map
  * @author mads
  */
 public class CMap {
     public static final int MAPMAGIC = 0x98754567; // Expect maps to start with this value
     public static final int MAPVERSION = 3; // Increment when map format changes
     public static final int MINVERSION = 1;
     public String name;
     public int checkcount; // incremented on trace
     public int checksum;
     public int version;
     public ArrayList<Block> blocks = new ArrayList<Block>();
 
     public HashMap<String, Integer> textureNames = new HashMap<String, Integer>();
     public SpawnEntities spawnEntities = new SpawnEntities();
     public CubeTexture[] textures;
     public int numblocks;
 
     // mins/maxs will be calculated during loading
     public Vector2f mins = new Vector2f(9999,9999);
     public Vector2f maxs = new Vector2f(-9999,-9999);
 
     private int numSubModels = 0;
     private ArrayList<BlockModel> subModels = new ArrayList<BlockModel>();
     
 
     // Load a new map
     public CMap(String filepath) throws ClipmapException {
         if(filepath.equals("newmap")) {
             name = "newmap";
             return;
         }
         AbstractMap.SimpleEntry<NetBuffer, Integer> result;
         try {
             result = ResourceManager.OpenFileAsNetBuffer(filepath, false);
         } catch (IOException ex) {
             throw new ClipmapException("Map loading failed. "+ex.getMessage());
         }
         checksum = result.getValue();
         NetBuffer file = result.getKey();
         name = filepath;
 
         Ref.spatial.Clear();
 
         if(!LoadHeader(file))
             throw new ClipmapException("CMap: Failed to verify header.");
         if(version > 1)
             LoadEntities(file);
         LoadTextures(file);
         LoadBlocks(file);
         
     }
 
    public CMap(NetBuffer buffer) throws ClipmapException {
        Ref.spatial.Clear();
        if(!LoadHeader(buffer))
            throw new ClipmapException("CMap: Failed to verify header.");
        if(version > 1)
            LoadEntities(buffer);
        LoadTextures(buffer);
        LoadBlocks(buffer);
    }

 //    public void SubModelChange(Block b, int newModel) {
 //        if(b.CustomVal > 0)
 //        {
 //            // remove from old submodel
 //            subModels.get(b.CustomVal-1).removeBlock(b);
 //        }
 //
 //        newModel = InlineModel(newModel);
 //        subModels.get(newModel-1).addBlock(b);
 //    }
 
     public BlockModel getModel(int index) {
         return subModels.get(index-1);
     }
     
     public int ToSubModel(Block b) {
         if(b.CustomVal > 0) {
             Common.Log("ToSubModel: Warning: Block is already in a model");
             subModels.get(b.CustomVal-1).removeBlock(b);
         }
 
         numSubModels++;
         subModels.add(new BlockModel(numSubModels));
         if(numSubModels != subModels.size())
             Ref.common.Error(ErrorCode.DROP, "ToSubModel: numSubModels != subModels.size()");
 
         subModels.get(numSubModels-1).addBlock(b);
         return b.CustomVal;
     }
 
 
     public int InlineModel(int index) {
         if(index <= 0 || index > subModels.size())
             Ref.common.Error(ErrorCode.DROP, "Map error: InlineModel, Invalid index " + index);
         return index;
     }
 
     
 
    
 
     // Returns true if header was accepted
     private boolean LoadHeader(NetBuffer buf) {
         int magic = buf.ReadInt();
         if(magic != CMap.MAPMAGIC) {
             Common.Log("LoadWorld: Map is not a valid cubetech map");
             return false;
         }
         version = buf.ReadInt();
         if(version > CMap.MAPVERSION || version < CMap.MINVERSION) {
             Common.Log("LoadWorld: Map uses a different version");
             return false;
         }
         return true;
     }
 
     private void LoadEntities(NetBuffer buf) {
         int nLines = buf.ReadInt();
         System.out.println("CMap.LoadEntities line count: " + nLines);
 
         boolean inEnt = false;
         SpawnEntity spEnt = null;
         Vector2f origin = null;
         String className = null;
         int currLine = 0;
         while(currLine < nLines) {
             currLine++;
             
             String line = buf.ReadString();
             if(!inEnt) {
                 if(!line.startsWith("{"))
                     Ref.common.Error(ErrorCode.DROP, "CMap.LoadEntities: Invalid start of entitiy: " + line);
 
                 line = line.substring(1).trim();
                 inEnt = true;
                 className = line;
             } else {
                 if(line.startsWith("}")) {
                     
                     if(className.isEmpty() || origin == null)
                         Ref.common.Error(ErrorCode.DROP, "CMap.LoadEntities: Entity entry didn't contain all the required values.");
                     
                     spEnt = new SpawnEntity(className, origin);
                     
                     spawnEntities.AddEntity(spEnt);
                     spEnt = null;
                     origin = null;
                     inEnt = false;
                     className = null;
                 } else {
                     // Spawnvars
                     if(line.startsWith("origin")) {
                         if(origin != null)
                             Common.LogDebug("CMap.LoadEntities: Overwriting origin");
                         line = line.substring("origin ".length()).trim();
                         origin = CubeMaterial.parseVector2f(line);                        
                     }
                 }
             }
         }
 
         if(inEnt) {
             Common.LogDebug("CMap.LoadEntities: Warning: Loadning ended in open entity.");
         }
 
     }
 
     // Doesn't load the textures, just skips this area of the map file
     private void LoadTextures(NetBuffer buf) {
 //        String s = buf.ReadString().trim();
 //        int nTex = Integer.parseInt(s);
         int nTex = buf.ReadInt();
         textures = new CubeTexture[nTex];
         for (int i= 0; i < nTex; i++) {
             String parse = buf.ReadString().trim();
             textureNames.put(parse, i);
             textures[i] = Ref.ResMan.LoadTexture(parse);
         }
     }
 
     public Block GetBlock(int index) {
         int size = blocks.size();
         if(index >= size) {
             if(index > size) {
                 Common.Log("BlockUpdate: Missed some blocks :/");
             }
 
             
             for (int i= 0; i < (index+1)-size; i++) {
                 blocks.add(new Block(numblocks++, new Vector2f(), new Vector2f(1,1), false));
             }
         }
         Block b = blocks.get(index);
         if(b.Handle != index)
             Common.Log("CMap.GetBlock: index & block index mismatch");
         return b;
     }
 
 
     private void LoadBlocks(NetBuffer buf) throws ClipmapException {
         ArrayList<CubeMaterialCacheEntry> matLoadCache  = new ArrayList<CubeMaterialCacheEntry>();
         int nBlocks = buf.ReadInt();
         int blockpos = buf.GetBuffer().position();
 
         Common.LogDebug("LoadBlocks offset: " + blockpos);
 
         // Seek to vector chunk
         int prBlockSize = 20;
         if(version >= 3)
             prBlockSize = 24;
         buf.GetBuffer().position(blockpos + nBlocks * prBlockSize);
 
         // Read vectors
         int nvectors = buf.ReadInt();
         Vector2f[] sourceVectors = new Vector2f[nvectors];
         for (int i= 0; i < nvectors; i++) {
             sourceVectors[i] = buf.ReadVector();
         }
 
 
 
         // Seek back to blocks
         buf.GetBuffer().position(blockpos);
 
         // Read blocks
         for (int i = 0; i < nBlocks; i++) {
             Vector2f position = buf.ReadVector();
             int sizIndex = buf.ReadShort();
             if(sizIndex >= sourceVectors.length) {
                 throw new ClipmapException("Invalid sourcevector index " + sizIndex);
             }
             Vector2f size = sourceVectors[sizIndex];
             float angle = buf.ReadFloat();
             int offsetId = buf.ReadShort();
             int sizeId = buf.ReadShort();
             Vector2f texOffset = sourceVectors[offsetId];
             Vector2f texSize = sourceVectors[sizeId];
             int texIndex = buf.ReadByte();
             boolean colliable = buf.ReadByte() == 1;
             int layer = -50;
             if(version >= 3)
                 layer = buf.ReadInt();
 
             CubeMaterial mat = null;
             // Check material cache
             for (int j= 0; j < matLoadCache.size(); j++) {
                 CubeMaterialCacheEntry entry = matLoadCache.get(j);
                 if(entry.matches(texIndex, offsetId, sizeId)) {
                     mat = entry.material;
                     break;
                 }
             }
 
             if(mat == null) {
                 mat = new CubeMaterial(textures[texIndex]);
                 mat.setTextureOffset(texOffset);
                 mat.setTextureSize(texSize);
                 matLoadCache.add(new CubeMaterialCacheEntry(texIndex, offsetId, sizeId, mat));
             }
 
             if(position.x < mins.x)
                 mins.x = position.x;
             if(position.y < mins.y)
                 mins.y = position.y;
 
             if(position.x + size.x > maxs.x)
                 maxs.x = position.x + size.x;
             if(position.y + size.y > maxs.y)
                 maxs.y = position.y + size.y;
 
             Block block = new Block(numblocks, position, size, true);
             if(numblocks != blocks.size())
                 Common.Log("CMap: numblocks & block array size mismatch");
             block.SetAngle(angle);
             block.setMaterial(mat);
             block.setLayer(layer);
 //            block.TexOffset = new Vector2f(texOffset);
 //            block.TexSize = new Vector2f(texSize);
             if(texIndex < 0 || texIndex >= textures.length) {
                 Ref.common.Error(ErrorCode.DROP, "Cannot load map: Unknown texture");
             }
 //            block.Texture = textures[texIndex];
             block.Collidable = colliable;
             blocks.add(block);
             numblocks++;
         }
 
     }
 //
 //    private void LoadBlocksOld(NetBuffer buf) throws ClipmapException {
 //        try {
 //            String parse = buf.ReadString().trim();
 //            int nBlocks = Integer.parseInt(parse);
 ////            blocks = new Block[nBlocks];
 //            if(nBlocks > 0) {
 //                for (int i = 0; i < nBlocks; i++) {
 //                    parse = buf.ReadString().trim();
 //                    String[] splitted = parse.split("(:)");
 //                    if(splitted.length != 12) {
 //                        throw new ClipmapException("Failed to load");
 //                    }
 //                    Vector2f loadPos = new Vector2f(Float.parseFloat(splitted[0]), Float.parseFloat(splitted[1]));
 //                    if(loadPos.x < mins.x)
 //                        mins.x = loadPos.x;
 //                    if(loadPos.y < mins.y)
 //                        mins.y = loadPos.y;
 //
 //                    Vector2f loadSize = new Vector2f(Float.parseFloat(splitted[2]), Float.parseFloat(splitted[3]));
 //                    if(loadPos.x + loadSize.x > maxs.x)
 //                        maxs.x = loadPos.x + loadSize.x;
 //                    if(loadPos.y + loadSize.y > maxs.y)
 //                        maxs.y = loadPos.y + loadSize.y;
 //                    float loadAngle = Float.parseFloat(splitted[4]);
 //                    Block block = new Block(numblocks, loadPos, loadSize, true);
 //                    if(numblocks != blocks.size())
 //                        System.out.println("CMap: numblocks & block array size mismatch");
 //                    block.SetAngle(loadAngle);
 //                    block.TexOffset = new Vector2f(Float.parseFloat(splitted[5]), Float.parseFloat(splitted[6]));
 //                    block.TexSize = new Vector2f(Float.parseFloat(splitted[7]), Float.parseFloat(splitted[8]));
 //                    int texid = Integer.parseInt(splitted[9]);
 //                    if(texid < 0 || texid >= textures.length) {
 //                        Ref.common.Error(ErrorCode.DROP, "Cannot load map: Unknown texture");
 //                    }
 //                    block.Texture = textures[texid];
 //                    block.Collidable = Integer.parseInt(splitted[10])==1?true:false;
 //                    block.CustomVal = Integer.parseInt(splitted[11]);
 //                    blocks.add(block);
 //                    numblocks++;
 //                }
 //            }
 ////            player = new Player(this, new Vector2f(camera.Position.x + camera.VisibleSize.x/2f, camera.Position.y + camera.VisibleSize.y/2f));
 ////            WorldUpdated(false);
 //
 //        } catch (NumberFormatException ex) {
 //            throw new ClipmapException("CMap.LoadBlocks(): Failed to parse.\n"+ex.toString());
 //        }
 //    }
 
     public Block AddBlock() {
         Block b = new Block(numblocks, new Vector2f(), new Vector2f(1, 1), true);
         if(numblocks != blocks.size())
             Common.Log("CMap.AddBlock(): numblock & block.size() mismatch");
         numblocks++;
         blocks.add(b);
         return b;
     }
 
 
     // 
     public NetBuffer SerializeMap() {
         ByteBuffer bb = ByteBuffer.allocate(1024*1024*2);
         NetBuffer buf = NetBuffer.CreateCustom(bb);
 
         // Save header
         buf.Write(CMap.MAPMAGIC);
         // Save map version
         buf.Write(CMap.MAPVERSION);
 
         // Write entity strings
         ArrayList<SpawnEntity> ents = Ref.game.spawnEntities.getList();
         buf.Write(ents.size()*3);
         for (SpawnEntity spawnEntity : ents) {
             buf.Write("{ " + spawnEntity.className);
             buf.Write(String.format("origin \"%f:%f\"", spawnEntity.origin.x, spawnEntity.origin.y));
             buf.Write("}");
         }
 
         ArrayList<CubeTexture> textures = new ArrayList<CubeTexture>();
         for (int i = 0; i < numblocks; i++) {
             Block b = blocks.get(i);
             if(!textures.contains(b.Material.getTexture()))
                 textures.add(b.Material.getTexture());
         }
 
         // Assemble textures
         buf.Write(textures.size());
         for (int i= 0; i < textures.size(); i++) {
             buf.Write(textures.get(i).name);
         }
 
         Common.LogDebug("Save block offset: " + buf.GetBuffer().position());
 
         // Save sizes
         ArrayList<Vector2f> vectors = new ArrayList<Vector2f>();
 
         int actualNum = 0;
         for (int i = 0; i < numblocks; i++) {
             Block b = blocks.get(i);
             if(b.isRemoved() || !b.isLinked())
                 continue;
             actualNum++;
         }
 
         buf.Write(actualNum);
 
         for (int i = 0; i < numblocks; i++) {
             Block b = blocks.get(i);
             if(b.isRemoved()  || !b.isLinked())
                 continue;
             int sizeIndex = -1;
             int texOfsIndex = -1;
             int texSizeIndex = -1;
             for (int j= 0; j < vectors.size(); j++) {
                 Vector2f vec = vectors.get(j);
                 if(Helper.Equals(vec, b.getSize()))
                     sizeIndex = j;
                 if(Helper.Equals(vec, b.Material.getTextureOffset(0)))
                     texOfsIndex = j;
                 if(Helper.Equals(vec, b.Material.getTextureSize()))
                     texSizeIndex = j;
 
                 if(texOfsIndex >= 0 && sizeIndex >= 0 && texSizeIndex >= 0)
                     break; // got a value for everything
             }
             // Didn't get a vector for size
             if(sizeIndex == -1) {
                 vectors.add(b.getSize());
                 sizeIndex = vectors.size()-1;
             }
             // Didn't get a vector for size
             if(texOfsIndex == -1) {
                 vectors.add(b.Material.getTextureOffset(0));
                 texOfsIndex = vectors.size()-1;
             }
             // Didn't get a vector for size
             if(texSizeIndex == -1) {
                 vectors.add(b.Material.getTextureSize());
                 texSizeIndex = vectors.size()-1;
             }
 
             int j = 0;
             for (; j < textures.size(); j++) {
                 if(b.Material.getTexture() == textures.get(j))
                     break;
             }
             if(j == textures.size()) {
                 j = 0;
                 Common.Log("Couldn't find texture for block :" + b.Material.getTextureName());
             }
             //int textureIndex = textureNames.get(b.Material.getTexture().name);
             int textureIndex = j;
 //            int bsize = buf.GetBuffer().position();
 
             buf.Write(b.getPosition()); // 2*4
             buf.WriteShort(sizeIndex); // 2
             buf.Write(b.getAngle()); // 4
             buf.WriteShort(texOfsIndex); // 2
             buf.WriteShort(texSizeIndex); // 2
             buf.WriteByte(textureIndex); // 1
             buf.Write(b.Collidable); // 1
 
             buf.Write(b.getLayer()); // 4
 
 //            bsize = buf.GetBuffer().position() - bsize;
 //            int test = 2;
         }
 
         buf.Write(vectors.size());
         for (int i= 0; i < vectors.size(); i++) {
             buf.Write(vectors.get(i));
         }
 
         
 
         int size = bb.position();
         bb.limit(size);
         bb.flip();
         return buf;
     }
 
 }
