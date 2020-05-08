 package src;
 
 import test.KuckuckHashtableTest;
 
 public class MainLalalala {
 
 	private static HashMap hm;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		
 		// first: create a hashmap
 		if(args[0].contains("linsort")){
 			hm = new LinSortHashTable(Integer.parseInt(args[1]), new SimpleHash(Integer.parseInt(args[1])));
 		} else {
 			hm = new KuckuckHashTable(Integer.parseInt(args[1]));
 		}
 		
 		// then: insert, delete or search in this map
 		for (int i = 2; i < args.length; i++) {
 			int newKey = Integer.parseInt(args[i].substring(1));
 			if(args[i].startsWith("i")){
 				System.out.printf("inserting: %d\n",newKey);
 				hm.insert(newKey);
 				
 			}else if((args[i].startsWith("d"))){
 				System.out.printf("deleting: %d\n",newKey);
 				hm.delete(newKey);
 				
 			}else if((args[i].startsWith("s"))){
 				System.out.printf("searching: %d ... %s\n",newKey,hm.search(newKey));
 			}
 		}
		System.out.println("finished. Table describes itself as:");
 		System.out.println(hm.describe());
 		
 	}
 		
 }
