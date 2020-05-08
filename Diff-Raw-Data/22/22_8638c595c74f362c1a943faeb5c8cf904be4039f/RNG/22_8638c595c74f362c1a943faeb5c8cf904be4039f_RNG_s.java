 package ru.gafi.game;
 
 /**
  * User: Michael
  * Date: 27.05.13
  * Time: 14:55
  */
 
 /**
  * Pseudo-random number generator by method George Marsaglia
  */
 public class RNG {
 	private int m_w;
 	private int m_z;
 
 	public RNG(long seed) {
 		setSeed(seed);
 	}
 
 	public void setSeed(long seed) {
 		m_z = (int) (seed & 0xFFFFFFFF);
 		m_w = (int) ((seed >> 32) & 0xFFFFFFFF);
 		if (m_z == 0) m_z = 1;
 		if (m_w == 0) m_w = 1;
 	}
 
 	public long getSeed() {
		long l = m_z & (((long) m_w) << 32);
		System.out.printf("%s %s %s%n", m_z, m_w, l);
		return m_z & (((long) m_w) << 32);
 	}
 
 	public int nextInt() {
 		m_z = 36969 * (m_z & 65535) + (m_z >> 16);
 		m_w = 18000 * (m_w & 65535) + (m_w >> 16);
 		return (m_z << 16) + m_w;
 	}
 
 	public int nextInt(int n) {
 		int r = nextInt();
 		return Math.abs(r % n);
 	}
 
 	public int range(int from, int to) {
 		return from + nextInt(to - from);
 	}
 
 }
