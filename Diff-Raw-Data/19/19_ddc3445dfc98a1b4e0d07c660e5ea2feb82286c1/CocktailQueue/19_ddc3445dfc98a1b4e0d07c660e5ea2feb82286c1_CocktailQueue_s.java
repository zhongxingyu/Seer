 package genBot2;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class CocktailQueue implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private LinkedList<CocktailWithName> queue;
 	private final Lock lock;
 		
 	public CocktailQueue() {
 		this.queue = new LinkedList<CocktailWithName>();
 		this.lock = new ReentrantLock();
 	}
 	
 	public void addCocktail(CocktailWithName cocktail) {
 		cocktail.getCocktail().setQueued(true);
 		
 		lock.lock();
 		
 		queue.addLast(cocktail);
 		
 		lock.unlock();
 	}
 		
 	public void addCocktail(String evolutionAlgorithmManager, String cocktailName) {
 		CocktailWithName[] cocktails = EvolutionStackContainer.getInstance().getEvolutionAlgorithmManager(evolutionAlgorithmManager).getGenManager().getNamedCocktailGeneration();
 		boolean cocktailFound = false;
 		
 		for (int i = 0; i < cocktails.length; i++) {
 			if (cocktails[i].getName().equals(cocktailName)) {
 				addCocktail(cocktails[i]);
 				cocktailFound = true;
 			}
 		}
 		
 		if (cocktailFound == false) {
 			throw new IllegalArgumentException(cocktailName + "not found in " + evolutionAlgorithmManager + "!");
 		}
 	}
 	
 	public CocktailWithName getAndRemoveFirstCocktail() {
 		CocktailWithName retCocktail =  queue.getFirst();
 		
 		lock.lock();
 	
 		queue.removeFirst();
 		
 		lock.unlock();
 		
 		return retCocktail;
 	}
 	public LinkedList<CocktailWithName> getLinkedList() {
 		return queue;
 	}
 	
 	public void deleteCocktail(CocktailWithName cocktail) {
 		Iterator<CocktailWithName> queueIt = queue.iterator();
 
 		while (queueIt.hasNext()) {
 			CocktailWithName curCocktail = queueIt.next();
 			
 			if (curCocktail.getName().equals(cocktail.getName())) {
 				lock.lock();
 				
 				curCocktail.getCocktail().setQueued(false);
 				queueIt.remove();
 				
 				lock.unlock();
 			}
 		}
 	}
 	
 	public void deleteCocktail(String evolutionAlgorithmManager, String cocktailName) {
		CocktailWithName[] cocktails = EvolutionStackContainer.getInstance().getEvolutionAlgorithmManager(evolutionAlgorithmManager).getGenManager().getRatedNamedCocktailGeneration();
 		boolean cocktailFound = false;
 		
 		for (int i = 0; i < cocktails.length; i++) {
 			if (cocktails[i].getName().equals(cocktailName)) {
 				deleteCocktail(cocktails[i]);
 				cocktailFound = true;
 			}
 		}
 		
 		if (cocktailFound == false) {
 			throw new IllegalArgumentException(cocktailName + " not found in " + evolutionAlgorithmManager + "!");
 		}
 	}
 	
 	public void deleteCocktail(String cocktailName) {
 		boolean cocktailFound = false;
 		
 		for (CocktailWithName actualCocktail : queue) {
 			if (actualCocktail.getName().equals(cocktailName)) {
 				deleteCocktail(actualCocktail.getEvolutionStackName(), cocktailName);
 				cocktailFound = true;
 			}
 		}
 		
 		if (cocktailFound == false) {
 			throw new IllegalArgumentException(cocktailName + "not found !");
 		}
 	}
 	
 	public void reorder(String[] cocktailNameList) {
 		lock.lock();
 		int queueLength = 0;
 		Iterator<CocktailWithName> it = queue.iterator();
 		while (it.hasNext()) {
 			queueLength++;
 			it.next();
 		}
 		
 		if (cocktailNameList.length != queueLength) {
 			throw new IllegalArgumentException("Queue has " + queueLength + " items, argument array has only " + cocktailNameList.length + " items!");
 		}
 		
 		LinkedList<CocktailWithName> newQueue = new LinkedList<CocktailWithName>();
 		
 		for (int i = 0; i < cocktailNameList.length; i++) {
 			it = queue.iterator();
 			
 			while (it.hasNext()) {
 				CocktailWithName actCocktail = it.next();
 				
 				if (actCocktail.getName().equals(cocktailNameList[i])) {
 					newQueue.add(actCocktail);
 				}
 			}
 		}
 		
 		it = newQueue.iterator();
 		while (it.hasNext()) {
 			queueLength++;
 			it.next();
 		}
 		
 		if (cocktailNameList.length != queueLength) {
 			throw new IllegalArgumentException("Something went wrong... specified a name twice? Looks like, at least!");
 		}
 		
 		this.queue = newQueue;
 		
 		lock.unlock();
 	}
 	
 	public boolean isEmpty() {
 		return queue.isEmpty();
 	}
 }
