 package togos.minecraft.mapgen.app;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 import org.jnbt.CompoundTag;
 import org.jnbt.NBTOutputStream;
 
 import togos.minecraft.mapgen.PathUtil;
 import togos.minecraft.mapgen.ScriptUtil;
 import togos.minecraft.mapgen.world.gen.ChunkMunger;
 import togos.minecraft.mapgen.world.gen.SimpleWorldGenerator;
 import togos.minecraft.mapgen.world.gen.TNLWorldGeneratorCompiler;
 import togos.minecraft.mapgen.world.gen.WorldGenerator;
 import togos.minecraft.mapgen.world.structure.ChunkData;
 import togos.noise2.lang.ScriptError;
 
 public class ChunkWriter
 {
 	public static ChunkWriter instance = new ChunkWriter();
 	
 	public String chunkPath( int x, int z ) {
		return PathUtil.mcChunkDir(x,z) +
			PathUtil.chunkBaseName(x,z);
 	}
 	
 	public void writeChunk( ChunkData cd, OutputStream os ) throws IOException {
 		NBTOutputStream nbtos = new NBTOutputStream(os);
 		
 		HashMap levelRootTags = new HashMap();
 		levelRootTags.put("Level",cd.toTag());
 		CompoundTag fileRootTag = new CompoundTag("",levelRootTags);
 		
 		nbtos.writeTag(fileRootTag);
 		nbtos.close();
 	}
 	
 	public void writeChunkToFile( ChunkData cd, String baseDir ) throws IOException {
 		String fullPath = baseDir + "/" + chunkPath( cd.getChunkX(), cd.getChunkZ() );
 		File f = new File(fullPath);
 		File dir = f.getParentFile();
 		if( dir != null && !dir.exists() ) dir.mkdirs();
 		FileOutputStream os = new FileOutputStream(f);
 		try {
 			writeChunk( cd, os );
 		} finally {
 			os.close();
 		}
 	}
 		
 	protected String chunkBaseDir;
 	public ChunkWriter( String baseDir ) {
 		this.chunkBaseDir = baseDir;
 	}
 	
 	public ChunkWriter() {
 		this( null );
 	}
 	
 	public void writeChunk( int cx, int cz, ChunkMunger cm ) throws IOException {
 		ChunkData cd = new ChunkData(cx,cz);
 		cm.mungeChunk(cd);
 		writeChunkToFile(cd, chunkBaseDir);
 	}
 	
 	public static String USAGE =
 		"Usage: ChunkWriter [options]\n" +
 		"\n" +
 		"Options:\n" +
 		"  -chunk-dir <dir>  ; directory under which to store chunk data\n" +
 		"  -x, -z, -width, -depth  ; bounds of area to generate";
 	
 	public static void main(String[] args) {
 		int boundsX = 0;
 		int boundsZ = 0;
 		int boundsWidth = 1;
 		int boundsDepth = 1;
 		String chunkDir = ".";
 		String scriptFile = null;
 		for( int i=0; i<args.length; ++i ) {
 			if( "-chunk-dir".equals(args[i]) ) {
 				chunkDir = args[++i];
 			} else if( "-x".equals(args[i]) ) {
 				boundsX = Integer.parseInt(args[++i]);
 			} else if( "-z".equals(args[i]) ) {
 				boundsZ = Integer.parseInt(args[++i]);
 			} else if( "-width".equals(args[i]) ) {
 				boundsWidth = Integer.parseInt(args[++i]);
 			} else if( "-depth".equals(args[i]) ) {
 				boundsDepth = Integer.parseInt(args[++i]);
 			} else if( !args[i].startsWith("-") ) {
 				scriptFile = args[i];
 			} else {
 				System.err.println("Unrecognised argument: "+args[i]);
 				System.err.println(USAGE);
 				System.exit(1);
 			}
 		}
 		
 		try {
 			ChunkWriter chunkWriter = new ChunkWriter(chunkDir);
 			
 			WorldGenerator worldGenerator;
 			if( scriptFile != null ) {
 				try {
 					worldGenerator = (WorldGenerator)ScriptUtil.compile( new TNLWorldGeneratorCompiler(), new File(scriptFile) );
 				} catch( ScriptError e ) {
 					System.err.println(ScriptUtil.formatScriptError(e));
 					System.exit(1);
 					return;
 				} catch( FileNotFoundException e ) {
 					System.err.println(e.getMessage());
 					System.exit(1);
 					return;
 				} catch( IOException e ) {
 					throw new RuntimeException(e);
 				}
 			} else {
 				worldGenerator = SimpleWorldGenerator.DEFAULT;
 			}
 			
 			ChunkMunger cfunc = worldGenerator.getChunkMunger();
 			for( int z=0; z<boundsDepth; ++z ) {
 				for( int x=0; x<boundsWidth; ++x ) {
 					ChunkData cd = new ChunkData(boundsX+x,boundsZ+z);
 					cfunc.mungeChunk(cd);
 					chunkWriter.writeChunkToFile(cd, chunkDir);
 				}
 			}
 		} catch( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 }
