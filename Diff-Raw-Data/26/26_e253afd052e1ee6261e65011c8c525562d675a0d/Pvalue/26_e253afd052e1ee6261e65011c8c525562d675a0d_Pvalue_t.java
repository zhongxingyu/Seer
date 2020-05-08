 package org.gwaspi.statistics;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class Pvalue {
 
 	private Pvalue() {
 	}
 
 	public static double calculatePvalueFromChiSqr(double chiSqr, int df) {
 		if (df == 1) {
 			if (chiSqr < 1) {
 				return calculatePvalueFromChiSqrPep(chiSqr, df);
 			} else {
 				return calculatePvalueFromChiSqrBelen(chiSqr, df);
 			}
 		} else if (df == 2) {
 			return calculatePvalueFromChiSqrBelen(chiSqr, df);
 		} else {
 			return calculatePvalueFromChiSqrPep(chiSqr, df);
 		}
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private static double calculatePvalueFromChiSqrPep(double chiSqr, int df) {
 		double q;
 		double p;
 
 		if ((chiSqr > 1000) || (df > 1000)) {
 			q = (calculateNormal((Math.pow(chiSqr / df, (double) 1 / 3) + (double) 2 / (9 * df) - 1)
 					/ Math.sqrt((double) 2 / (9 * df))))
 					/ 2;
 
 			if (chiSqr > df) {
 				return (q);
 			}
 			return (1 - q);
 		}
 
 		p = Math.exp(-0.5 * chiSqr);
 		if ((df % 2) == 1) {
 			p = p * Math.sqrt(2 * (chiSqr / Math.PI));
 		}
 
 		int k = df;
 		while (k >= 2) {
 			p = p * chiSqr / k;
 			k = k - 2;
 		}
 
 		double t = p;
 		int a = df;
 		while (t > 1e-15 * p) {
 			a += 2;
 			t = t * chiSqr / a;
 			p = p + t;
 		}
 
 		return (1 - p);
 	}
 
	private static double calculatePvalueFromChiSqrBelen(double inputChiSqr, int df) {
 		double result = 0;
 		if (df == 1) {
 			double chiSqr = inputChiSqr;
 
 			double incr = 0.5;
 			int r = 7;
 			double w = incr / (4 * r);
 
 			double tempQ = 1;
 			double Q = 0;
 
 			while (tempQ >= 1e-300) {
 				tempQ = 2 * w / 45 * (7 * (dBelen(chiSqr) + dBelen(chiSqr + 28 * w))
 						+ 12 * (dBelen(chiSqr + 2 * w) + dBelen(chiSqr + 6 * w)
 						+ dBelen(chiSqr + 10 * w)
 						+ dBelen(chiSqr + 14 * w)
 						+ dBelen(chiSqr + 18 * w)
 						+ dBelen(chiSqr + 22 * w)
 						+ dBelen(chiSqr + 26 * w))
 						+ 14 * (dBelen(chiSqr + 4 * w)
 						+ dBelen(chiSqr + 8 * w)
 						+ dBelen(chiSqr + 12 * w)
 						+ dBelen(chiSqr + 16 * w)
 						+ dBelen(chiSqr + 20 * w)
 						+ dBelen(chiSqr + 24 * w))
 						+ 32 * (dBelen(chiSqr + w)
 						+ dBelen(chiSqr + 3 * w)
 						+ dBelen(chiSqr + 5 * w)
 						+ dBelen(chiSqr + 7 * w)
 						+ dBelen(chiSqr + 9 * w)
 						+ dBelen(chiSqr + 11 * w)
 						+ dBelen(chiSqr + 13 * w)
 						+ dBelen(chiSqr + 15 * w)
 						+ dBelen(chiSqr + 17 * w)
 						+ dBelen(chiSqr + 19 * w)
 						+ dBelen(chiSqr + 21 * w)
 						+ dBelen(chiSqr + 23 * w)
 						+ dBelen(chiSqr + 25 * w)
 						+ dBelen(chiSqr + 27 * w)));
 
 				Q += tempQ;
 				chiSqr += incr;
 			}
 			return Q;
 		} else if (df == 2) {
 			result = Math.exp(-1 * inputChiSqr / 2);
 		}
 		return result;
 	}
 
	private static double calculateNormal(double z) {
 		double q = Math.pow(z, 2);
 
 		if (Math.abs(z) > 7) {
 			return (1 - (1 / q) + (3 / (q * q))) * Math.exp(-q / 2)
 					/ (Math.abs(z) * Math.sqrt(Math.PI / 2));
 		}
 		return calculatePvalueFromChiSqr(q, 1);
 	}
 
	private static double dBelen(double x) {
 		double fx = (double) 1
 				/ Math.sqrt(x
 				* Math.exp(x)
 				* Math.PI
 				* 2);
 		return fx;
 	}
 	//</editor-fold>
 }
