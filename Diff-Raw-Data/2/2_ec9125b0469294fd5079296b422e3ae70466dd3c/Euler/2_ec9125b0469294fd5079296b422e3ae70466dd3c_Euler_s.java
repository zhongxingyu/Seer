 package utils;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Euler {
 
 	/** FOR PRIMEARRAYBETWEEN SE PROBLEM111 
 	 * @throws IOException */
 
 	public static void main(String[] args) throws IOException {
 		for (int i = 1; i <= 10; i++) {
 			printMatrix(matrixPow(new long[][] {{1,1}, {1,0}},i,(long)Math.pow(10, 15)));
 		}
 		
 	}
 	
 	public static long fib(long n) {
 		return matrixPow(new long[][] {{1,1}, {1,0}},n)[1][0];
 	}
 	
 	public static long fib(long n, long mod) {
 		return matrixPow(new long[][] {{1,1}, {1,0}},n, mod)[1][0];
 	}
 	
 	public static long pow(int x, int pow) {
 		int length = (int) (Math.log(pow)/Math.log(2))+1;
 		long[] ints = new long[length];
 		long[] powArray = new long[length];
 		
 		for (int i = 0; i < ints.length; i++) {
 			ints[i] = (long)Math.pow(2, i);
 		}
 		
 		powArray[0] = x;
 		for (int i = 1; i < powArray.length; i++) {
 			powArray[i] = powArray[i-1]*powArray[i-1];
 		}
 		
 		long pows = 0;
 		long result = 1;
 		for (int i = ints.length-1; i >= 0; i--) {
 			long a = ints[i];
 			
 			if (((pow-pows)/a) == 1) {
 				pows += a;
 				result *= powArray[i];
 			}
 		}
 		
 		return result;
 	}
 	
 	public static void printMatrix(long[][] A) {
 		for (int i = 0; i < A.length; i++) {
 			System.out.println(Arrays.toString(A[i]));
 		}
 		System.out.println();
 	}
 	
 	private static void printMatrix(long[][] A, long mod) {
 		for (int i = 0; i < A.length; i++) {
 			System.out.print("[");
 			for (int j = 0; j < A.length; j++) {
 				System.out.print(A[i][j]%mod + (j != A.length-1?",":""));
 			}
 			System.out.println("]");
 		}
 		System.out.println();
 	}
 	
 	public static long[][] matrixPow(long[][] matrix, long pow) {
 		int length = (int) (Math.log(pow)/Math.log(2))+1;
 		long[] ints = new long[length];
 		long[][][] powArray = new long[length][][];
 		
 		for (int i = 0; i < ints.length; i++) {
 			ints[i] = (long)Math.pow(2, i);
 		}
 		
 		powArray[0] = matrix;
 		for (int i = 1; i < powArray.length; i++) {
 			powArray[i] = matrixMult(powArray[i-1], powArray[i-1]);
 		}
 
 		long pows = 0;
 		long[][] result = new long[matrix.length][matrix.length];
 		//identity matrix
 		for (int i = 0; i < result.length; i++) {
 			result[i][i] = 1;
 		}
 		for (int i = ints.length-1; i >= 0; i--) {
 			long a = ints[i];
 			
 			if (((pow-pows)/a) == 1) {
 				pows += a;
 				result = matrixMult(result, powArray[i]);
 			}
 		}
 		
 		return result;
 	}
 	
 	public static long[][] matrixMult(long[][] A, long[][] B) {
 		long[][] res = new long[A.length][A.length];
 		for (int i = 0; i < res.length; i++) {
 			for (int j = 0; j < res.length; j++) {
 				for (int k = 0; k < res.length; k++) {
 					res[i][j] += (A[i][k]*B[k][j]);
 				}
 			}
 		}
 		
 		
 		return res;
 	}
 	
 	public static long[][] matrixPow(long[][] matrix, long pow, long mod) {
 		int length = (int) (Math.log(pow)/Math.log(2))+1;
 		long[] ints = new long[length];
 		long[][][] powArray = new long[length][][];
 		
 		for (int i = 0; i < ints.length; i++) {
 			ints[i] = (long)Math.pow(2, i);
 		}
 		powArray[0] = matrix;
 		for (int i = 1; i < powArray.length; i++) {
 			powArray[i] = matrixMult(powArray[i-1], powArray[i-1], mod);
 		}
 
 		long pows = 0;
 		long[][] result = new long[matrix.length][matrix.length];
 		//identity matrix
 		for (int i = 0; i < result.length; i++) {
 			result[i][i] = 1;
 		}
 		for (int i = ints.length-1; i >= 0; i--) {
 			long a = ints[i];
 			
 			if (((pow-pows)/a) == 1) {
 				pows += a;
 				result = matrixMult(result, powArray[i], mod);
 			}
 		}
 		
 		return result;
 	}
 	
 	public static long[][] matrixMult(long[][] A, long[][] B, long mod) {
 		long[][] res = new long[A.length][A.length];
 		for (int i = 0; i < res.length; i++) {
 			for (int j = 0; j < res.length; j++) {
 				BigInteger temp = BigInteger.ZERO;
 				for (int k = 0; k < res.length; k++) {
 					temp = temp.add(BigInteger.valueOf(A[i][k]).multiply(BigInteger.valueOf(B[k][j])));
 					
 				}
 				res[i][j] = temp.mod(BigInteger.valueOf(mod)).longValue();
 			}
 		}
 		
 		
 		return res;
 	}
 	
 	public static String toBinaryString(int number, int pow) {
 		StringBuilder sb = new StringBuilder(Integer.toBinaryString(number));
 		for (int i = sb.length(); i < pow; i++) {
 			sb.insert(0, '0');
 		}
 		return sb.toString();
 	}
 
 	private static void swap(int[] s, int a, int b) {
 		int temp = s[a];
 		s[a] = s[b];
 		s[b] = temp;
 	}
 
 	public static boolean permute(int[] str, int len) {
 		int key = len - 1;
 		int newkey = len - 1;
 
 		while ((key > 0) && (str[key] <= str[key - 1])) {
 			key--;
 		}
 
 		key--;
 		if (key < 0)
 			return false;
 
 		newkey = len - 1;
 		while ((newkey > key) && (str[newkey] <= str[key])) {
 			newkey--;
 		}
 
 		swap(str, key, newkey);
 
 		len--;
 		key++;
 
 		while (len > key) {
 			swap(str, len, key);
 			key++;
 			len--;
 		}
 
 		return true;
 	}
 
 	public static boolean permuteReversed(int[] str, int len) {
 		int key = len - 1;
 		int newkey = len - 1;
 
 		while ((key > 0) && (str[key] >= str[key - 1])) {
 			key--;
 		}
 
 		key--;
 		if (key < 0)
 			return false;
 
 		newkey = len - 1;
 		while ((newkey > key) && (str[newkey] >= str[key])) {
 			newkey--;
 		}
 
 		swap(str, key, newkey);
 
 		len--;
 		key++;
 
 		while (len > key) {
 			swap(str, len, key);
 			key++;
 			len--;
 		}
 
 		return true;
 	}
 	
 	public static List<Long> primeListBetween(long start, long limit) {
 		limit++;
 		if (start == 1)
 			start = 2;
 
 		boolean[] isPrime = new boolean[(int)(limit-start)];
 		Arrays.fill(isPrime, true);
 		
 		for (Integer prime : Euler.primeList((int)(Math.sqrt(limit)+1))) {
 			long s = (start/prime);
 			s*=prime;
 			if (s != start)
 				s+=prime;
 			for (long j = s; j < limit; j += prime) {
 				/**
 				 * skjønner ikke at denne er helt riktig men
 				 */
 				if (j != prime) {
 					isPrime[(int)(j-start)] = false;
 				}
 			}
 		}
 		
 		List<Long> primesBetween = new ArrayList<Long>();
 		for (int i = 0; i < isPrime.length; i++) {
 			if (isPrime[i]) {
 				primesBetween.add(i+start);
 			}
 		}
 		return primesBetween;
 	}
 
 	public static List<Integer> bedrePrimeList(int limit) {
 		int sievebound = limit / 2 + (limit%2 == 1?1:0); // last index of sieve
 		boolean[] sieve = new boolean[sievebound];
 		int crosslimit = ((int) (Math.sqrt(limit)) / 2)+1;
 		for (int i = 1; i < crosslimit; i++) {
 			if (!sieve[i])
 				for (int j = 2 * i * (i + 1); j < sievebound; j += 2 * i + 1)
 					sieve[j] = true;
 		}
 
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(2);
 		for (int l = 1; l < sieve.length; l++) {
 			if (!sieve[l])
 				list.add(2 * l + 1);
 		}
 		return list;
 	}
 	
 	public static List<Long> primeListLong(int limit) {
 		boolean[] array = primeArray(limit);
 		ArrayList<Long> list = new ArrayList<Long>();
 
 		for (long i = 2; i < array.length; i++) {
 			if (array[(int)i])
 				list.add(i);
 		}
 
 		return list;
 	}
 
 	public static List<Integer> primeList(int limit) {
 		boolean[] array = primeArray(limit);
 		ArrayList<Integer> list = new ArrayList<Integer>();
 
 		for (int i = 2; i < array.length; i++) {
 			if (array[i])
 				list.add(i);
 		}
 
 		return list;
 	}
 
 	public static boolean[] primeArray(int limit) {
 		++limit;
 		boolean[] array = new boolean[limit];
 		for (int i = 2; i < array.length; i++) {
 			array[i] = true;
 		}
 
 		for (int j = 2 * 2; j < array.length; j += 2) {
 			array[j] = false;
 		}
 		for (int i = 3; i <= Math.sqrt(limit); i = i + 2) {
 			if (array[i]) {
 				for (int j = 2 * i; j < limit; j += i) {
 					array[j] = false;
 				}
 			}
 		}
 		return array;
 	}
 
 	public static boolean isPrime(long number) {
 		if (number < 2)
 			return false;
 		if (number < 4)
 			return true;
 		if (number % 2 == 0)
 			return false;
 		if (number < 9)
 			return true;
 		if (number % 3 == 0)
 			return false;
 		int sqrt = (int) Math.sqrt(number);
 		for (long i = 5; i <= sqrt; i = i + 6) {
 			if (number % i == 0 || number % (i + 2) == 0)
 				return false;
 		}
 		return true;
 	}
 
 	public static int divisorAmount(int numbers) {
 		int factors = 1;
 		int n = numbers;
 		int teller = 0;
 		for (int i = 2; i <= n / i; i++) {
 			teller = 0;
 			while (n % i == 0) {
 				// cout<<n<<endl;
 				teller++;
 				n /= i;
 			}
 			factors *= (teller + 1);
 		}
 		if (n > 1)
 			factors *= 2;
 		return factors;
 	}
 
 	public static List<Integer> primeFactorList(long numbers) {
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		long n = numbers;
 		for (int i = 2; i <= n / i; i++) {
 			while (n % i == 0) {
 				list.add(i);
 				n /= i;
 			}
 		}
 		if (n > 1)
 			list.add((int)n);
 		return list;
 	}
 
 	public static List<Integer>[] primeFactorDistinctListsBelow(int limit) {
 		List<Integer>[] primefactors = new List[limit+1];
 		for (int i = 0; i < primefactors.length; i++) {
 			primefactors[i] = new ArrayList<Integer>();
 		}
 		
 		for (int prime : Euler.primeList(limit)) {
 			for (int i = prime; i < primefactors.length; i+=prime) {
 				primefactors[i].add(prime);
 			}
 		}
 		
 		return primefactors;
 	}
 	
 	public static List<Integer> primeFactorDistinctList(int numbers) {
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		int n = numbers;
 		for (int i = 2; i <= n / i; i++) {
 			if (n % i == 0)
 				list.add(i);
 			while (n % i == 0) {
 				n /= i;
 			}
 		}
 		if (n > 1)
 			list.add(n);
 		return list;
 	}
 
 	public static List<Integer> divisorList(int n) {
 		int limit = n;
 		ArrayList<Integer> divisors = new ArrayList<Integer>();
		for (int i = 1; i < limit && i <= n/i; ++i) {
 			if (n % i == 0) {
 				int d2 = n / i;
 				divisors.add(i);
 				// such that for every divisor d of n, d+n/d is prime.
 				if (i != d2)
 					divisors.add(d2);
 
 				// System.out.println(n/i + " " + i);
 				limit = n / i;
 			}
 		}
 
 		return divisors;
 	}
 	
 	public static List<Long> divisorList(long n) {
 		long limit = n;
 		List<Long> divisors = new ArrayList<Long>();
 		for (long i = 1; i < limit && i <= n/i; ++i) {
 			if (n % i == 0) {
 				long d2 = n / i;
 				divisors.add(i);
 				// such that for every divisor d of n, d+n/d is prime.
 				if (i != d2)
 					divisors.add(d2);
 
 				// System.out.println(n/i + " " + i);
 				limit = n / i;
 			}
 		}
 
 		return divisors;
 	}
 	
 	public static Map<Integer, Integer> primeFactorMap(int n) {
 		Map<Integer, Integer> map = new HashMap<Integer,Integer>();
 		for (int prime : primeFactorList(n)) {
 			if (map.containsKey(prime)) {
 				map.put(prime, map.get(prime)+1);
 			} else {
 				map.put(prime, 1);
 			}
 		}
 		return map;
 	}
 	
 	public static Map<Integer, Integer> primeFactorMap(int...numbers) {
 		Map<Integer, Integer> factors = new HashMap<Integer, Integer>();
 		for (int number : numbers) {
 			Map<Integer, Integer> numberFactors = Euler.primeFactorMap(number);
 			for (Map.Entry<Integer,Integer> factor : numberFactors.entrySet()) {
 				if (factors.containsKey(factor.getKey())) {
 					factors.put(factor.getKey(), factor.getValue()+factors.get(factor.getKey()));
 				} else {
 					factors.put(factor.getKey(), factor.getValue());
 				}
 			}
 		}
 		return factors;
 	}
 
 	public static long modPow(long n, long exp, long mod) {
 		long res = 1;
 		while (exp > 0) {
 			if (exp % 2 == 1) {
 				res = (res * n) % mod;
 			}
 			exp = exp >> 1;
 			
 			n = (n * n) % mod;
 		}
 		return res;
 	}
 	
 	public static int fi(int n) {
 		int result = n;
 		for (int i = 2; i * i <= n; i++) {
 			if (n % i == 0)
 				result -= result / i;
 			while (n % i == 0)
 				n /= i;
 		}
 		if (n > 1)
 			result -= result / n;
 		return result;
 	}
 	
 	public static long fi(long n) {
 		long result = n;
 		for (int i = 2; i * i <= n; i++) {
 			if (n % i == 0)
 				result -= result / i;
 			while (n % i == 0)
 				n /= i;
 		}
 		if (n > 1)
 			result -= result / n;
 		return result;
 	}
 
 	public static long gcd(long a, long b) {
 		if (a < 0) {
 			a *= -1;
 		}
 		if (b < 0) {
 			b *= -1;
 		}
 		while (b != 0) {
 			long temp = b;
 			b = a % b;
 			a = temp;
 		}
 		return a;
 	}
 
 	public static boolean isPerfectSquare(long input) {
 		long closestRoot = (long) Math.sqrt(input);
 		return input == closestRoot * closestRoot;
 	}
 	
 	public static BigInteger factorial(int factorial) {
 		BigInteger prod = BigInteger.ONE;
 		
 		for (int i = 2; i <= factorial; i++) {
 			prod = prod.multiply(BigInteger.valueOf(i));
 		}
 		
 		return prod;
 	}
 }
