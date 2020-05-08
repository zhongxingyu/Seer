 import java.util.Random;
 import java.util.Collections;
 import java.util.Arrays;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 
 public class PrePartition {
   private Random generator;
 	private int[] p;
 	private Long[] a_prime;
   private Long[] a_orig;
   private long residue;
 
 	public PrePartition(Long[] arr) {
     a_orig = arr;
     generator = new Random();
 		p = gen_random_p(arr.length);
 		a_prime = prePartition(arr);
 	}
 
   public Long[] get_a_prime() {
     return a_prime;
   }
 	// generates a random P array of length len
   public int[] gen_random_p(int len) {
     int[] p = new int[len];
     for (int i = 0; i < len; i++) {
       // generates a random int from 0 to 100 inclusive
       p[i] = generator.nextInt(len);
     }
     return p;
   }
 
   // given A, return a_prime using p
   public Long[] prePartition(Long[] a) {
     int len = a.length;
     // initialize a_prime to all zeroes
     Long[] a_prime = new Long[len];
     for (int i = 0; i < a_prime.length; i++) {
       a_prime[i] = new Long(0L);
     }
 
     for (int i = 0; i < len; i++) {
       a_prime[p[i]] += a[i];
     }
     KarmarkarKarp kk = new KarmarkarKarp(a_prime, 0);
    residue = kk.residue();
     return a_prime;
   }
 
   public long residue() {
     return residue;
   }
 
   public void getNeighbor() {
    // Choose two random indices i and j from [0, n-1] with p_i != j and set p_i to j
     int[] p_copy = p.clone();
    int j;
    int i = (generator.nextInt(a_prime.length));
     do {
      j = (generator.nextInt(a_prime.length));
    } while(j != p_copy[i]);
     p_copy[i] = j;
 
     KarmarkarKarp kk = new KarmarkarKarp(a_prime, 0);
    long new_residue = kk.residue();
 
     // Update if a better neighbor has been found
     if (new_residue < residue) {
       p = p_copy;
       a_prime = prePartition(a_orig);
     }
   }
 }
