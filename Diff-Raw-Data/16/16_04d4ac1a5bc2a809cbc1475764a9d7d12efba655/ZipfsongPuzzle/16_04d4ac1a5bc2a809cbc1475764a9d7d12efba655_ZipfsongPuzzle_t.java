 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 public class ZipfsongPuzzle {
 	static class Song implements Comparable<Song> {
 		private String name;
 		private long frequency;
 		private long quality;
 		private int album_position;
 		
 		Song(long frequency, String name, int originalOrder) {
 			this.frequency      = frequency;
 			this.name           = name;
 			this.album_position = originalOrder;
 			this.quality        = 0;
 		}
 		
 		@Override
 		public String toString() {
 			return name;
 		}
 
 		@Override
 		public int compareTo(Song o) {
 			long res = o.quality - quality;
 			
 			// If two songs have the same quality
 			// make sure that their original position
 			// in the song list remains (stable sorting).
 			if (res == 0) {
 				res = album_position - o.album_position;
 			}
 			
 			return (int) Math.signum(res);
 		}
 	}
 	
 	public static void main(String... args) {
 		try {
 			/**
 			 * Assumes all input is valid, as no error handling requirements
 			 * were stated.
 			 * Removed Scanner and regular expressions for performance reasons.
 			 */
 			
 			int n, m;
 			String[] tmp;
 			Song[] songs;
 			
 			/**
 			 * 1. Read from stdin
 			 */
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 			
 			tmp = reader.readLine().split(" ");
 			n = Integer.parseInt(tmp[0]);
 			m = Integer.parseInt(tmp[1]);
 			
 			songs = new Song[n];
 			for(int i=0; i < n; ++i) {
 				tmp = reader.readLine().split(" ");
 				songs[i] = new Song(Long.parseLong(tmp[0]), tmp[1].trim(), i);
 			}
 			
 			/**
 			 * 2. Calculate song qualities
 			 */
 			for(int i=0; i < songs.length; ++i) {
 				/**
 				 * I simplified the original formula as this
 				 * should eliminate any inaccuracies of floating
 				 * points and also make comparison and calculation
 				 * faster.
 				 * 
 				 * The maximum value for song quality is: 50.000 * 10^12
 				 * This is still way smaller than MAX_LONG.
 				 */
 				songs[i].quality = songs[i].frequency * (i+1);
 			}
 
 			/**
 			 * 3. Select the m songs with the greatest quality
 			 */
 			if (m > n >> 1) {
 				// Mergesort (stable)
 				Arrays.sort(songs);
 			} else {
 				// Modified Quicksort
 				biggestOf(songs, m);
 			}
 			
   		    /**
   		     * 4. Output
   		     */
            StringBuilder outBuffer = new StringBuilder();
			for(int i=0; i < m-1; ++i) {
				outBuffer.append(songs[i])
                         .append('\n');
 			}

	    	// Honorary Judge Kattis does not like
			// a new line at the end
            outBuffer.append(songs[m-1]);     
 			
 			System.out.print(outBuffer);
 		} catch (Exception e) {
 			System.err.println(e);
 			e.printStackTrace();
 		}
 	}
 	
 	static void biggestOf(Song[] songs, int m) {
 		biggestOf(songs, 0, songs.length - 1, m);
 	}
 	
 	static void biggestOf(Song[] songs, int left, int right, int m) {
 		if (left < right) {
 			// Middle element is the pivot
 			int pivotIndex = (left + right) >>> 1;
             Song pivot = songs[pivotIndex];
             // Move the pivot to the end of the array
 			swap (songs, pivotIndex, right);
 			
 			// Move every element bigger than pivot
 			// to the left
 			int pivotIndexNew = left;
 			for (int i=left; i < right; ++i) {
 				if (pivot.compareTo(songs[i]) > 0) {
 					swap (songs, pivotIndexNew, i);
 					pivotIndexNew++;
 				}
 			}
 			
 			// Move the pivot to it's correct position
 			swap (songs, right, pivotIndexNew);
 			
 			biggestOf(songs, left, pivotIndexNew - 1, m);
 			if (pivotIndexNew < m) {
 				biggestOf(songs, pivotIndexNew + 1, right, m);
 			}
 		}
 	}
 	
 	static <T> void swap(T[] array, int i, int j) {
 		T tmp = array[i];
 		array[i] = array[j];
 		array[j] = tmp;
 	}
 }
