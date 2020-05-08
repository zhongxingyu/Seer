 package de.uni_hamburg.informatik.sep.zuul.server.util;
 
 import java.util.List;
 import java.util.Random;
 
 public final class FancyFunction
 {
 	private static final Random RANDOM = new Random();
 
 	/**
 	 * Gib ein zufälliges Element aus einer Liste.
 	 * 
 	 * @param entries
 	 *            Die Liste
 	 * @return ein zufälliges Element aus der Liste
 	 */
 	public static <T> T getRandomEntry(List<T> entries)
 	{
 		int size = entries.size();
 		if(size == 0)
 			return null;
 
 		int nextInt = RANDOM.nextInt(size);
 		return entries.get(nextInt);
 	}
 
 	public static class SuperFancyReproducibleRandomEntryPicker
 	{
 		private Random _random;
 
 		public SuperFancyReproducibleRandomEntryPicker(int seed)
 		{
 			_random = new Random(seed);
 		}
 
 		/**
 		 * Gib ein zufälliges Element aus einer Liste.
 		 * 
 		 * @param entries
 		 *            Die Liste
 		 * @return ein zufälliges Element aus der Liste
 		 */
 		public <T> T pick(List<T> entries)
 		{
 			int size = entries.size();
 			if(size == 0)
 				return null;
 
 			int nextInt = _random.nextInt(size);
 			return entries.get(nextInt);
 		}

		public <T> T pickAndRemoveFromList(List<T> entries)
		{
			T pick = pick(entries);
			if(pick != null)
				entries.remove(pick);
			return pick;
		}
 	}
 }
