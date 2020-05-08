 package ch.ethz.mlmq.model.queue;
 
 import java.math.BigDecimal;
 
 public class QueueMMmB extends Queue {
 
 	/**
 	 * m: number of servers
 	 */
 	private int m;
 
 	/**
 	 * Buffer Size B
 	 */
 	private int B;
 
 	public QueueMMmB(String name, double lambda, double s, int m, int B, int queueMultiplicity) {
 		super(name, lambda, s, queueMultiplicity);
 		this.m = m;
 		this.B = B;
 	}
 
 	@Override
 	public BigDecimal getServiceRateWithNJobs(int n) {
		if (n < m) {
			return new BigDecimal(n).divide(new BigDecimal(s), PRECISION, ROUND);
 		} else {
			return new BigDecimal(m).divide(new BigDecimal(s), PRECISION, ROUND);
 		}
 	}
 
 	public BigDecimal getTraficIntensity() {
 
 		BigDecimal mu = getMeanServiceRate();
 		BigDecimal mTimesMu = new BigDecimal(m).multiply(mu);
 		return new BigDecimal(lambda).divide(mTimesMu, PRECISION, ROUND);
 	}
 
 	public BigDecimal getProbabilityZeroJobsInSystem() {
 		if (m != 1) {
 			return getProbabilityZeroJobsInSystemGeneral();
 		} else {
 
 			BigDecimal rho = getTraficIntensity();
 			if (BigDecimal.ONE.equals(rho)) {
 				return BigDecimal.ONE.divide(new BigDecimal(B + 1), PRECISION, ROUND);
 			} else {
 				BigDecimal tmp = BigDecimal.ONE.subtract(rho);
 				BigDecimal tmp2 = BigDecimal.ONE.subtract(rho.pow(B + 1));
 				return tmp.divide(tmp2, PRECISION, ROUND);
 			}
 		}
 	}
 
 	public BigDecimal getProbabilityZeroJobsInSystemGeneral() {
 		BigDecimal rho = getTraficIntensity();
 		BigDecimal mTimesRho = rho.multiply(new BigDecimal(m));
 
 		// second summand
 		BigDecimal tmp = mTimesRho.pow(m);
 		BigDecimal tmp2 = BigDecimal.ONE.subtract(rho.pow(B - m + 1));
 		tmp = tmp.multiply(tmp2);
 		tmp = tmp.divide(factorial(m), PRECISION, ROUND);
 		tmp = tmp.divide(BigDecimal.ONE.subtract(rho), PRECISION, ROUND);
 
 		// third summand
 		BigDecimal tmp3 = BigDecimal.ZERO;
 		for (int n = 1; n <= m - 1; n++) {
 			tmp3 = tmp3.add(mTimesRho.pow(n).divide(factorial(n), PRECISION, ROUND));
 		}
 
 		tmp = BigDecimal.ONE.add(tmp).add(tmp3);
 		return BigDecimal.ONE.divide(tmp, PRECISION, ROUND);
 	}
 
 	public BigDecimal getProbabilityNumJobsInSystem(int n) {
 		BigDecimal p0 = getProbabilityZeroJobsInSystem();
 
 		if (n == 0) {
 			return p0;
 		}
 
 		BigDecimal rho = getTraficIntensity();
 
 		if (n < m) {
 			BigDecimal mTimesRho = rho.multiply(new BigDecimal(m));
 
 			BigDecimal tmp = mTimesRho.pow(n);
 			tmp = tmp.multiply(p0);
 			tmp = tmp.divide(factorial(n), PRECISION, ROUND);
 
 			return tmp;
 		} else {
 
 			BigDecimal tmp = rho.pow(n);
 			BigDecimal tmp2 = new BigDecimal(m).pow(m);
 
 			tmp = tmp.multiply(tmp2);
 			tmp = tmp.multiply(p0);
 			tmp = tmp.divide(factorial(m), PRECISION, ROUND);
 			return tmp;
 		}
 	}
 
 	/**
 	 * Expected number of jobs in the system
 	 * 
 	 * @return
 	 */
 	public BigDecimal getMeanNrOfJobsInSystem() {
 		BigDecimal res = BigDecimal.ZERO;
 		for (int n = 1; n <= B; n++) {
 			res = res.add(getProbabilityNumJobsInSystem(n).multiply(new BigDecimal(n)));
 		}
 
 		return res;
 	}
 
 	public BigDecimal getMeanNrOfJobsInQueue() {
 		BigDecimal res = BigDecimal.ZERO;
 		for (int n = m + 1; n <= B; n++) {
 			res = res.add(getProbabilityNumJobsInSystem(n).multiply(new BigDecimal(n - m)));
 		}
 
 		return res;
 	}
 
 	@Override
 	public String toString() {
 		return name + " M/M/" + m + "/" + B;
 	}
 
 	public int getQueueSize() {
 		return B;
 	}
 
 	public BigDecimal getProbabilityOfQueueing() {
 		BigDecimal p0 = getProbabilityZeroJobsInSystem();
 		BigDecimal rho = getTraficIntensity();
 		BigDecimal mTimesRho = rho.multiply(new BigDecimal(m));
 
 		BigDecimal tmp = mTimesRho.pow(m);
 		tmp = tmp.multiply(p0);
 		tmp = tmp.divide(BigDecimal.ONE.subtract(rho), PRECISION, ROUND);
 		tmp = tmp.divide(factorial(m), PRECISION, ROUND);
 		return tmp;
 
 	}
 
 	public boolean isLoadDependent() {
 		return true;
 	}
 
 	@Override
 	public int getServiceNodeCount() {
 		return m;
 	}
 }
