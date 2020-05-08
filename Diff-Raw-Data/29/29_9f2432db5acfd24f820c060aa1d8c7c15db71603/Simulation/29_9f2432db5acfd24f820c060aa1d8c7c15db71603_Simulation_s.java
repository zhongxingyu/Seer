 import java.io.*;
 import java.util.*;
 
 public class Simulation {
 
 	static final Job jobWhichNeverArrives = new Job(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
 
 	public static void main(String[] args) throws IOException {
 		Scanner s = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
 
 		int N = s.nextInt();
 		double lambda = s.nextDouble();
 		double mu = s.nextDouble();
 
 		Job[] job = new Job[N];
 
		double arrivalTime = nextRandomExponential(lambda);
		double serviceTime = nextRandomExponential(mu);
 
 		job[0] = new Job(0, arrivalTime, serviceTime);
 
 		for (int i = 1; i < N; i++) {
 			// Inter arrival times are exponentially distributed.
 			// Service time is exponentially distributed.
 			// We want to generate random numbers which follow exponential distribution.
 			// The default random number generated follow uniform distribution.
 			// We need to convert that distribution into exponential distribution.
 
			arrivalTime = nextRandomExponential(lambda) + job[i-1].arrivalTime;
			serviceTime = nextRandomExponential(mu);
 
 			job[i] = new Job(i, arrivalTime, serviceTime); 
 		}
 
 		s.close();
 
 		Queue waitingQueue = new Queue();
 		Job currentJob = job[0];
 		Job nextJob = job[1];
 		double currentJobDepartureTime = job[0].arrivalTime + job[0].serviceTime;
 		double QiTi = 0;
 		double lastUpdate = 0;
 
 		while (true) {
 			if (nextJob.arrivalTime < currentJobDepartureTime) {
 				// Next event is an arrival.
 
 				QiTi += (waitingQueue.length * (nextJob.arrivalTime - lastUpdate));
 				lastUpdate = nextJob.arrivalTime;
 
 				if (currentJob == null) {
 					// There is no job executing currently.
 					// So, arrived job will go directly to the server instead of the waiting queue.
 					currentJob = nextJob;
 					currentJob.waitingTime = 0;
 					currentJobDepartureTime = currentJob.arrivalTime + currentJob.serviceTime;
 				} else {
 					// Arrived job will go to the waiting queue.
 					waitingQueue.add(nextJob);
 				}
 				if (nextJob.id == N-1) {
 					nextJob = jobWhichNeverArrives;
 				} else {
 					nextJob = job[nextJob.id + 1];
 				}
 			} else if (nextJob.arrivalTime > currentJobDepartureTime) {
 				// Next event is a departure.
 
 				QiTi += (waitingQueue.length * (currentJobDepartureTime - lastUpdate));
 				lastUpdate = currentJobDepartureTime;
 
 				if (currentJob.id == N-1) {
 					// Last job has departed.
 					break;
 				}
 				if (waitingQueue.isEmpty()) {
 					// Server will become idle.
 					currentJob = null;
 					currentJobDepartureTime = Integer.MAX_VALUE;
 				} else {
 					// Server gets the first job from the waiting queue.
 					currentJob = waitingQueue.remove();
 					currentJob.waitingTime = currentJobDepartureTime - currentJob.arrivalTime;
 					currentJobDepartureTime += currentJob.serviceTime;
 				}
 			} else {
 				// A job departs and another job arrives at the same time.
 
 				if (waitingQueue.isEmpty()) {
 					currentJob = nextJob;
 					currentJob.waitingTime = 0;
 					currentJobDepartureTime = currentJob.arrivalTime + currentJob.serviceTime;
 				} else {
 					// Server gets the first job from the waiting queue.
 					currentJob = waitingQueue.remove();
 					currentJob.waitingTime = currentJobDepartureTime - currentJob.arrivalTime;
 					currentJobDepartureTime += currentJob.serviceTime;
 					waitingQueue.add(nextJob);
 				}
 				if (nextJob.id == N-1) {
 					nextJob = jobWhichNeverArrives;
 				} else {
 					nextJob = job[nextJob.id + 1];
 				}
 			}
 		}
 		for (int i = 0; i < N; i++) {
 			out.write("Job ID = " + job[i].id + 
			          " Job Arrival Time = " + job[i].arrivalTime + 
					  " Job Service Time = " + job[i].serviceTime + 
					  " Job Waiting Time = " + job[i].waitingTime + 
					  " Job Response Time = " + job[i].getResponseTime() + "\n");
 		}
		out.write("QiTi = " + QiTi +
		          " Average Queue length = " + QiTi/(double)currentJobDepartureTime + "\n");
 		out.flush();
 		out.close();
 	}
 
	static double nextRandomExponential(int l) {
		Random rnd = new Random();

 		double rndU = rnd.nextDouble();
 		double expX = -1 * Math.log(1-rndU) / l;
 
 		return expX;
 	}
 }
 
 class Queue {
 
 	Job first;
 	Job last;
 	int length;
 
 	Queue() {
 		first = null;
 		last = null;
 		length = 0;
 	}
 
 	void add(Job j) {
 		if (first == null) {
 			first = j;
 			last = j;
 		} else {
 			last.next = j;
 			last = j;
 		}
 		length++;
 	}
 
 	Job remove() {
 		if (first == null) {
 			throw new RuntimeException("Trying to remove from empty queue");
 		}
 		length--;
 		Job result = first;
 		first = first.next;
 		return result;
 	}
 
 	boolean isEmpty() {
 		return length==0;
 	}
 
 }
 
 class Job {
 	int id;
 	double arrivalTime;
 	double serviceTime;
 	double waitingTime;
 	Job next;
 
 	Job (int id, double arrivalTime, double serviceTime) {
 		this.id = id;
 		this.arrivalTime = arrivalTime;
 		this.serviceTime = serviceTime;
 	}
 
 	double getResponseTime() {
 		return waitingTime + serviceTime;
 	}
 }
