 package concurrent.collision.detection;
 
 import main.Main;
 import cl.niclabs.skandium.muscles.Execute;
 import data.structures.HashBitmapPair;
 import data.structures.HashList;
 
 public class ExecuteContainsCheck implements Execute<HashList, HashList> {
 
 	@Override
 	public HashList execute(HashList param) throws Exception {
 		HashList result = new HashList();
 		
 		for (HashBitmapPair pair : param) {
			if (Main.trie.contains(pair.getHash())) {
 				result.add(pair);
 			}
 		}
 		
 		return result;
 	}
 
 }
