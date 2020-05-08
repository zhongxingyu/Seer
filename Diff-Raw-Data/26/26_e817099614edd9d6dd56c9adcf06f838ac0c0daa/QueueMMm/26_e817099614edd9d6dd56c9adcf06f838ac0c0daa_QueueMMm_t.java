 package ch.ethz.mlmq.model.queue;
 
 import java.math.BigDecimal;
 
 public class QueueMMm extends Queue {
 
 	/**
 	 * m: number of servers
 	 */
 	private int m;
 
 	public QueueMMm(String name, double lambda, double s, int m, int queueMultiplicity) {
 		super(name, lambda, s, queueMultiplicity);
 		this.m = m;
 	}
 
 	@Override
 	public BigDecimal getServiceRateWithNJobs(int n) {
		if ((n * queueMultiplicity) < (m * queueMultiplicity)) {
			return new BigDecimal(n).divide(new BigDecimal(s), PRECISION, ROUND).multiply(new BigDecimal(queueMultiplicity));
 		} else {
			return new BigDecimal(m).divide(new BigDecimal(s), PRECISION, ROUND).multiply(new BigDecimal(queueMultiplicity));
 		}
 	}
 
 	public BigDecimal getTraficIntensity() {
 		BigDecimal mu = getMeanServiceRate();
 		BigDecimal mTimesMu = new BigDecimal(m).multiply(mu);
 		return new BigDecimal(lambda).divide(mTimesMu, PRECISION, ROUND);
 	}
 
 	public BigDecimal getProbabilityZeroJobsInSystem() {
 
 		BigDecimal rho = getTraficIntensity();
 		BigDecimal mTimesRho = rho.multiply(new BigDecimal(m));
 
 		// second summand
 		BigDecimal tmp = mTimesRho.pow(m);
 		tmp = tmp.divide(factorial(m), PRECISION, ROUND);
 		tmp = tmp.divide(BigDecimal.ONE.subtract(rho), PRECISION, ROUND);
 
 		// third summand
 		BigDecimal tmp2 = BigDecimal.ZERO;
 		for (int n = 1; n <= m - 1; n++) {
 			tmp2 = tmp2.add(mTimesRho.pow(n).divide(factorial(n), PRECISION, ROUND));
 		}
 
 		tmp = BigDecimal.ONE.add(tmp).add(tmp2);
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
 			tmp = tmp.divide(factorial(n), PRECISION, ROUND);
 			return tmp.multiply(p0);
 		} else {
 
 			BigDecimal tmp = rho.pow(n);
 
 			BigDecimal tmp2 = new BigDecimal(m);
 			tmp2 = tmp2.pow(m);
 
 			tmp = tmp.multiply(tmp2);
 			tmp = tmp.multiply(p0);
 			tmp = tmp.divide(factorial(m), PRECISION, ROUND);
 			return tmp;
 		}
 	}
 
 	/**
 	 * Probability of Queueing
 	 * 
 	 * P(>= m jobs)
 	 * 
 	 * @return
 	 */
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
 
 	public BigDecimal getMeanNrOfJobsInSystem() {
 		BigDecimal rho = getTraficIntensity();
 		BigDecimal mTimesRho = rho.multiply(new BigDecimal(m));
 
 		BigDecimal tmp = getMeanNrOfJobsInQueue();
 		tmp = tmp.add(mTimesRho);
 
 		return tmp;
 	}
 
 	public BigDecimal getMeanNrOfJobsInQueue() {
 		BigDecimal rho = getTraficIntensity();
 
 		BigDecimal tmp = BigDecimal.ONE.subtract(rho);
 		tmp = tmp.multiply(getProbabilityOfQueueing());
 		tmp = tmp.multiply(rho);
 		return tmp;
 	}
 
 	@Override
 	public String toString() {
 		return name + " M/M/" + m;
 	}
 
 	public boolean isLoadDependent() {
 		return true;
 	}
 
 	@Override
 	public int getServiceNodeCount() {
 		return m;
 	}
 }
