 package asciiWorld.chunks;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import asciiWorld.entities.Entity;
 import asciiWorld.entities.EntityFactory;
 import asciiWorld.math.RandomFactory;
 import asciiWorld.math.Vector3f;
 
 public class PerlinOverworldChunkGenerator implements IChunkGenerator {
 	
 	private static final int SEEDTREE_MAX = 80;
 	private static final int SEEDTREE_MIN = 20;
 	private static final int TREES_MIN = 80;
 	private static final int TREES_MAX = 640;
 	private static final int CAVES_COUNT = 40;
 	
 	private static final double PERSISTENCE = 0.25;
 	private static final double FREQUENCY = 0.0625; // lower frequency to stretch out the terrain
 	private static final double AMPLITUDE = 10;
 	private static final int OCTAVES = 10;
 	
 	private static final int ATTEMPTS_FINDWATER = 1024;
 	private static final int ATTEMPTS_FINDCAVEENTRANCE = 1024;
 	
 	private PrintStream _logStream;
 	
 	public Chunk generate(Chunk chunk, long seed, PrintStream logStream)
 			throws Exception {
 		
 		_logStream = logStream;
 		
 		RandomFactory.get().reseed(seed);
 		
 		chunk = generatePerlinNoise(chunk);
 		chunk = generateRiver(chunk, findWater(chunk), findWater(chunk));
 		chunk = generateTrees(chunk);
 		chunk = generateCaveEntrances(chunk);
 		
 		chunk.getComponents().add(new DayNightCycleComponent(chunk));
 		
 		return chunk;
 	}
 
     private Chunk generatePerlinNoise(Chunk chunk)
     		throws Exception {
     	_logStream.print("Generating perlin noise...");
     	
 		// This will generate weird grainy landscapes with random seeds > 25000.  Don't know why.
 		ITerrainGenerator generator = new PerlinTerrainGenerator(PERSISTENCE, FREQUENCY, AMPLITUDE, OCTAVES, RandomFactory.get().nextInt(0, 25000));
 		double[][] terrain = generator.generate(Chunk.COLUMNS, Chunk.ROWS);
 		for (int y = 0; y < Chunk.ROWS; y++) {
 			for (int x = 0; x < Chunk.COLUMNS; x++) {
 				int value = (int)terrain[y][x];
 				if (value > 1) {
 					EntityFactory.get().createDirtEntity(chunk, x, y);
 					EntityFactory.get().createStoneEntity(chunk, x, y);
 				}
 				else if (value > -3) {
 					EntityFactory.get().createGrassEntity(chunk, x, y);
 				}
 				else if (value > -4) {
 					EntityFactory.get().createSandEntity(chunk, x, y);
 				}
 				else {
 					EntityFactory.get().createWaterEntity(chunk, x, y);
 				}
 			}
 		}
 		
 		_logStream.println(" done!");
 		return chunk;
 	}
     
     private Vector2f findWater(Chunk chunk)
     		throws Exception {
     	_logStream.print("Locating water...");
     	
     	Vector2f point = new Vector2f(
     			RandomFactory.get().nextInt(0, Chunk.COLUMNS),
     			RandomFactory.get().nextInt(0, Chunk.ROWS));
     	int attempt = 0;
     	while (!chunk.getEntityAt(point, Chunk.LAYER_GROUND).getName().equals("Water")) {
         	point.x = RandomFactory.get().nextInt(0, Chunk.COLUMNS);
         	point.y = RandomFactory.get().nextInt(0, Chunk.ROWS);
         	
         	_logStream.print(".");
         	
         	attempt++;
         	if (attempt >= ATTEMPTS_FINDWATER) {
         		_logStream.println(" failed to find water.");
         		return null;
         	}
     	}
     	
     	_logStream.println(" done!");
     	return point;
     }
 
 	private Chunk generateRiver(Chunk chunk, Vector2f startingChunkPoint, Vector2f endingChunkPoint)
 			throws Exception {
 		_logStream.print("Generating a river...");
 		
 		if ((startingChunkPoint == null) || (endingChunkPoint == null)) {
 			_logStream.println(" failed to generate a river.");
 			return chunk;
 		}
 		
 		Vector2f chunkPoint = startingChunkPoint.copy();
 		while (!((chunkPoint.x == endingChunkPoint.x) && (chunkPoint.y == endingChunkPoint.y))) {
 			switch (RandomFactory.get().nextInt(0, 2)) {
 				case 0:
 					if (chunkPoint.x < endingChunkPoint.x) {
 						chunkPoint.x++;
 					} else if (chunkPoint.x > endingChunkPoint.x) {
 						chunkPoint.x--;
 					}
 					break;
 				case 1:
 					if (chunkPoint.y < endingChunkPoint.y) {
 						chunkPoint.y++;
 					} else if (chunkPoint.y > endingChunkPoint.y) {
 						chunkPoint.y--;
 					}
 					break;
 			}
 
 			// Rivers cut through mountains; lakes do not.
 			Entity groundEntity = chunk.getEntityAt(chunkPoint, Chunk.LAYER_GROUND);
 			if (groundEntity != null) {
 				chunk.removeEntity(groundEntity);
 			}
 			Entity objectEntity = chunk.getEntityAt(chunkPoint, Chunk.LAYER_OBJECT);
 			if (objectEntity != null) {
 				chunk.removeEntity(objectEntity);
 			}
 			EntityFactory.get().createWaterEntity(chunk, (int)chunkPoint.x, (int)chunkPoint.y);
 			
 			_logStream.print(".");
 		}
 		
 		_logStream.println(" done!");
 		return chunk;
 	}
 
     private Chunk generateTrees(Chunk chunk)
     		throws Exception {
     	_logStream.print("Generating trees...");
     	
 		// Generate seed trees.
 		int seedTreeCount = RandomFactory.get().nextInt(SEEDTREE_MIN, SEEDTREE_MAX);
 		List<Vector3f> seedTrees = new ArrayList<Vector3f>();
 		for (int n = 0; n < seedTreeCount; n++) {
 			Vector3f treePoint = chunk.findRandomSpawnPoint(Chunk.LAYER_OBJECT);
 			while (!chunk.getEntityAt(treePoint.toVector2f(), Chunk.LAYER_GROUND).getName().equals("Grass")) {
 				// Trees can only grow on grass.
 				treePoint = chunk.findRandomSpawnPoint(Chunk.LAYER_OBJECT);
 			}
 
 			seedTrees.add(treePoint);
 
 			EntityFactory.get().createTreeEntity(chunk, (int)treePoint.x, (int)treePoint.y);
 			
 			_logStream.print(".");
 		}
 
 		// Generate forests.
 		int totalTrees = RandomFactory.get().nextInt(TREES_MIN, TREES_MAX) - seedTreeCount;
 		for (int n = 0; n < totalTrees; n++) {
 			// Pick a seed position.
 			Vector3f seedPoint = seedTrees.get(RandomFactory.get().nextInt(0, seedTreeCount));
 			generateTree(chunk, seedPoint);
 			
 			_logStream.print(".");
 		}
 		
 		_logStream.println(" done!");
 		return chunk;
     }
 
	private Entity generateTree(Chunk chunk, Vector3f seedPoint) throws Exception {
		float angle = (float)(RandomFactory.get().nextInt(0, 360) * Math.PI / 180.0f); // pick a random angle
		int distance = 1; // keep searching outward until a suitable spawn point is found
		
 		while (true) {
 			Vector3f chunkPoint = new Vector3f(seedPoint.x + distance * (float)Math.cos(angle), seedPoint.y + distance * (float)Math.sin(angle), seedPoint.z);
 			Entity groundEntity = chunk.getEntityAt(chunkPoint.toVector2f(), Chunk.LAYER_GROUND); 
 			if ((groundEntity == null) || !groundEntity.getName().equals("Grass")) {
 				// We're moving out into the desert; start over.
				angle = (float)(RandomFactory.get().nextInt(0, 360) * Math.PI / 180.0f); // pick a random angle
				distance = 1; // keep searching outward until a suitable spawn point is found
				continue;
 			}
 			if (!chunk.isSpaceOccupied(chunkPoint)) {
 				return EntityFactory.get().createTreeEntity(chunk, (int)chunkPoint.x, (int)chunkPoint.y);
 			} else {
 				distance++;
 			}
 		}
 	}
 	
	private Chunk generateCaveEntrances(Chunk chunk) throws Exception {
 		_logStream.print("Generating cave entrances...");
 		
 		for (int n = 0; n < CAVES_COUNT; n++) {
 			if (generateCaveEntrance(chunk) == null) {
 				_logStream.println(" failed to find a place to put a cave.");
 				return chunk;
 			}
 			_logStream.print(".");
 		}
 		
 		_logStream.println(" done!");
 		return chunk;
 	}
 	
 	private Entity generateCaveEntrance(Chunk chunk)
 			throws Exception {
 		// Find a place to put the cave.
 		Vector2f chunkPoint = findPossibleCaveEntrance(chunk);
 		if (chunkPoint == null) {
 			// Unable to find a place to put the cave.
 			return null;
 		}
 
 		// Clear out some space for the entrance.
 		Entity objectEntity = chunk.getEntityAt(chunkPoint, Chunk.LAYER_OBJECT);
 		if (objectEntity != null) {
 			chunk.removeEntity(objectEntity);
 		}
 
 		// Create the entrance.
 		return EntityFactory.get().createCaveEntranceEntity(chunk, (int)chunkPoint.x, (int)chunkPoint.y);
 	}
 
 	private Vector2f findPossibleCaveEntrance(Chunk chunk)
 			throws Exception {
 		int attempt = 0;
 		while (true) {
 			attempt++;
 			if (attempt >= ATTEMPTS_FINDCAVEENTRANCE) {
 				return null;
 			}
 
 			int x = RandomFactory.get().nextInt(0, Chunk.COLUMNS);
 			int y = RandomFactory.get().nextInt(0, Chunk.ROWS);
 			
 			Entity entity = chunk.getEntityAt(new Vector2f(x, y), Chunk.LAYER_OBJECT);
 			if (entity == null) {
 				continue; // we need to find a mountain to dig into
 			}
 			if (entity.getName().equals("Stone")) {
 				chunk.removeEntity(entity);
 				return new Vector2f(x, y);
 			}
 		}
 	}
 }
