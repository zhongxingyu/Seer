 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 /**
  * Controls saving the chunks in a map.
  * @author Charles
  *
  */
 public class MapSaver {
 	private LinkedList<SaveCommand> saveCommands;
 	private HashMap<String, LinkedList<SaveCommand>> mapChunksE;
 	private HashSet<SaveCommand> deleteCommands;
 	private MapDeleter mapDel;
 	private int progress;
 	
 	/**
 	 * Constructor for the MapSaver.  Takes a linked list of save commands 
 	 * and the MapDeleter to access the progress bar.
 	 * @param saveCommandsGet A LinkedList of save commands.
 	 * @param parent The MapDeleter containing the progress bar.
 	 */
 	public MapSaver(LinkedList<SaveCommand> saveCommandsGet, MapDeleter parent){
 		
 		
 		this.mapDel = parent;
 		this.saveCommands = saveCommandsGet;
 		Iterator<SaveCommand> siter = saveCommands.iterator();
 		while(siter.hasNext())
 			System.out.println(siter.next().getChunkA());
 		this.deleteCommands = new HashSet<SaveCommand>();
 		this.mapChunksE = new HashMap<String, LinkedList<SaveCommand>>();
 	}
 	
 	/**
 	 * Saves all of the chunks.
 	 */
 	public void save(){
 		Iterator<SaveCommand> siter = saveCommands.iterator();
 		int previousCommand = -1;
 		int count = 0;
 		//Iterate through all of the SaveCommands.
 		while(siter.hasNext()){
 			this.progress = (int)((double)count / (double)this.saveCommands.size() * 100);
 			this.mapDel.setProgressBarValue(this.progress, "Saving Map");
 			count++;
 			SaveCommand tempCo = siter.next();
 			//If it's a copy command. Make sure all of the previous commands are saved then copy.
 			if(tempCo.getCommand() == SaveCommand.COPY_COMMAND){
 				this.saveCurrentChunks();
 				this.mapChunksE = null;
 				try {
 					MapChunkExtended copyFrom = new MapChunkExtended(tempCo.getChunkA());
 					MapChunkExtended copyTo = new MapChunkExtended(tempCo.getChunkB());
 					copyTo.copy(copyFrom);
 					copyTo.save();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			//If it's a delete command put it on a different list to execute later.
 			if(tempCo.getCommand() == SaveCommand.DELETE_COMMAND){
 				System.out.println("Delete Command");
 				this.deleteCommands.add(tempCo);
 			}
 			//If it's another command add it to a map 
 			//containing the chunk to save and the data to save.
			if(tempCo.getCommand() != SaveCommand.ADD_BLOCKS_COMMAND &&
 				tempCo.getCommand() != SaveCommand.CHANGE_HEIGHT_COMMAND){
 				continue;
             }
			// Just to make sure that we have a place to add the commands.
			if(this.mapChunksE == null){
				this.mapChunksE = new HashMap<String, LinkedList<SaveCommand>>();
			}
			// If a reference to the chunk path is not in the map, add it.
			//  Also add the SaveCommand to the list in the map.
			if(!this.mapChunksE.containsKey(tempCo.getChunkA())){
				LinkedList<SaveCommand> tempList = new LinkedList<SaveCommand>();
				tempList.add(tempCo);
				this.mapChunksE.put(tempCo.getChunkA(), tempList);
			} else {
				this.mapChunksE.get(tempCo.getChunkA()).add(tempCo);
 			}
		}
 		
 		this.saveCurrentChunks();
 		this.delete();
 		this.mapDel.progressBarComplete("Finished Saving");
 	}
 	
 	/**
 	 * Save the chunks that are in the map.
 	 */
 	private void saveCurrentChunks(){
 		System.out.println("Saving Chunks");
 		if(mapChunksE == null)
 			return;
 		Set<String> keySet = mapChunksE.keySet();
 		int newProg = keySet.size();
 		int newCurrProg = 0;
 		Iterator<String> iter = keySet.iterator();
 		while(iter.hasNext()){
 			newCurrProg++;
 			this.mapDel.setProgressBarValue((int)(newCurrProg / newProg * 100.0F), "Saving Map - Saving Chunks");
 			try {
 				String chunkPath = iter.next();
 
 				System.out.println(chunkPath+ "--------------------++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 				MapChunkExtended saveChunk = new MapChunkExtended(chunkPath);
 				LinkedList<SaveCommand> tempList = mapChunksE.get(chunkPath);
 				Iterator<SaveCommand> citer = tempList.iterator();
 				while(citer.hasNext()){
 					SaveCommand tempCo = citer.next();
 					if(tempCo.getCommand() == SaveCommand.CHANGE_HEIGHT_COMMAND){
 						System.out.println("CHANGE HEIGHT COMMAND");
 						saveChunk.raiseBlocks(tempCo.getCoordinates());
 					}
 					if(tempCo.getCommand() ==  SaveCommand.ADD_BLOCKS_COMMAND){
 						saveChunk.addBlocks(tempCo.getCoordinates());
 					}
 				}
 				saveChunk.save();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	/**
 	 * Delete the chunks in the specific list if there are any to delete.
 	 */
 	private void delete(){
 		Iterator<SaveCommand> diter = this.deleteCommands.iterator();
 		while(diter.hasNext()){
 			String diterStr = diter.next().getChunkA();
 			System.out.println(diterStr);
 			File tempFile = new File(diterStr);
 			String parentFileString = tempFile.getParent();
 			File parentFile = new File(parentFileString);
 			tempFile.delete();
 			boolean keepDeleting = true;
 			while(keepDeleting){
 				if(parentFile.listFiles().length == 1){
 					parentFileString = parentFile.getParent();
 					parentFile.delete();
 					parentFile = new File(parentFileString);
 				}
 				else{
 					keepDeleting = false;
 				}
 			}
 		}
 	}
 }
