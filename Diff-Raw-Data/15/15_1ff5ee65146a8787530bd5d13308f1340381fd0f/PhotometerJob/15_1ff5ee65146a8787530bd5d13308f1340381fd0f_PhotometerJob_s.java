 /**
  *
  */
 package ca.eandb.jmist.packages.job;
 
 import java.io.PrintStream;
 import java.io.Serializable;
 
 import ca.eandb.jmist.framework.Material;
 import ca.eandb.jmist.framework.measurement.CollectorSphere;
 import ca.eandb.jmist.framework.measurement.Photometer;
 import ca.eandb.jmist.toolkit.SphericalCoordinates;
 
 import ca.eandb.jdcp.job.AbstractParallelizableJob;
 import ca.eandb.jdcp.job.TaskWorker;
 import ca.eandb.util.progress.ProgressMonitor;
 
 /**
  * @author bkimmel
  *
  */
 public final class PhotometerJob extends
 		AbstractParallelizableJob implements Serializable {
 
 	public PhotometerJob(Material specimen,
 			SphericalCoordinates[] incidentAngles, double[] wavelengths,
 			long samplesPerMeasurement, long samplesPerTask, CollectorSphere prototype) {
 
 		this.worker						= new PhotometerTaskWorker(specimen, prototype);
 		this.incidentAngles				= incidentAngles.clone();
 		this.wavelengths				= wavelengths.clone();
 		this.samplesPerMeasurement		= samplesPerMeasurement;
 		this.samplesPerTask				= samplesPerTask;
 		this.results					= new CollectorSphere[wavelengths.length * incidentAngles.length];
 		this.totalTasks					= wavelengths.length
 												* incidentAngles.length
 												* ((int) (samplesPerMeasurement / samplesPerTask) + ((samplesPerMeasurement % samplesPerTask) > 0 ? 1
 														: 0));
 
 		for (int i = 0; i < this.results.length; i++) {
 			this.results[i] = prototype.clone();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#getNextTask()
 	 */
 	public Object getNextTask() {
 
 		if (!this.isComplete()) {
 
 			PhotometerTask task = this.getPhotometerTask(this.nextMeasurementIndex);
 
 			if (++this.nextMeasurementIndex >= this.results.length) {
 				this.outstandingSamplesPerMeasurement += this.samplesPerTask;
 				this.nextMeasurementIndex = 0;
 			}
 
 			return task;
 
 		} else {
 
 			return null;
 
 		}
 
 	}
 
 	private PhotometerTask getPhotometerTask(int measurementIndex) {
 		return new PhotometerTask(
 				this.getIncidentAngle(measurementIndex),
 				this.getWavelength(measurementIndex),
 				this.samplesPerTask,
 				measurementIndex
 		);
 	}
 
 	private SphericalCoordinates getIncidentAngle(int measurementIndex) {
 		return this.incidentAngles[measurementIndex / this.wavelengths.length];
 	}
 
 	private double getWavelength(int measurementIndex) {
 		return this.wavelengths[measurementIndex % this.wavelengths.length];
 	}
 
 	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.ParallelizableJob#submitTaskResults(java.lang.Object, java.lang.Object, ca.eandb.jmist.framework.reporting.ProgressMonitor)
 	 */
 	public void submitTaskResults(Object task, Object results,
 			ProgressMonitor monitor) {
 
 		PhotometerTask		info		= (PhotometerTask) task;
 		CollectorSphere		collector	= (CollectorSphere) results;
 
 		this.results[info.measurementIndex].merge(collector);
 
 		monitor.notifyProgress(++this.tasksReturned, this.totalTasks);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#isComplete()
 	 */
 	public boolean isComplete() {
 		return this.outstandingSamplesPerMeasurement >= this.samplesPerMeasurement;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#finish()
 	 */
 	public void finish() {
 
 		PrintStream out = new PrintStream(createFileOutputStream("photometer.csv"));
 
 		this.writeColumnHeadings(out);
 
 		for (int incidentAngleIndex = 0, n = 0; incidentAngleIndex < this.incidentAngles.length; incidentAngleIndex++) {
 
 			SphericalCoordinates			incidentAngle			= this.incidentAngles[incidentAngleIndex];
 
 			for (int wavelengthIndex = 0; wavelengthIndex < this.wavelengths.length; wavelengthIndex++, n++) {
 
 				double						wavelength				= this.wavelengths[wavelengthIndex];
 				CollectorSphere				collector				= this.results[n];
 
 				for (int sensor = 0; sensor < collector.sensors(); sensor++) {
 
 					SphericalCoordinates	exitantAngle			= collector.getSensorCenter(sensor);
 					double					solidAngle				= collector.getSensorSolidAngle(sensor);
 					double					projectedSolidAngle		= collector.getSensorProjectedSolidAngle(sensor);
 					long					hits					= collector.hits(sensor);
 					double					reflectance				= (double) hits / (double) this.outstandingSamplesPerMeasurement;
 
 					out.printf(
 							"%f,%f,%e,%d,%f,%f,%f,%f,%d,%d,%f,%e,%e",
 							incidentAngle.polar(),
 							incidentAngle.azimuthal(),
 							wavelength,
 							sensor,
 							exitantAngle.polar(),
 							exitantAngle.azimuthal(),
 							solidAngle,
 							projectedSolidAngle,
 							this.outstandingSamplesPerMeasurement,
 							hits,
 							reflectance,
 							reflectance / projectedSolidAngle,
 							reflectance / solidAngle
 					);
 					out.println();
 
 				}
 
 			}
 
 		}
 
 		out.close();
 
 	}
 
 	/**
 	 * Writes the CSV column headings to the result stream.
 	 * @param out The <code>PrintStream</code> to write the column headings to.
 	 */
 	private void writeColumnHeadings(PrintStream out) {
 
 		out.print("\"Incident Polar (radians)\",");
 		out.print("\"Incident Azimuthal (radians)\",");
 		out.print("\"Wavelength (m)\",");
 		out.print("\"Sensor\",");
 		out.print("\"Exitant Polar (radians)\",");
 		out.print("\"Exitant Azimuthal (radians)\",");
 		out.print("\"Solid Angle (sr)\",");
 		out.print("\"Projected Solid Angle (sr)\",");
 		out.print("\"Samples\",");
 		out.print("\"Hits\",");
 		out.print("\"Reflectance\",");
 		out.print("\"BSDF\",");
 		out.print("\"SPF\"");
 		out.println();
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.ParallelizableJob#worker()
 	 */
 	public TaskWorker worker() {
 		return this.worker;
 	}
 
 	private static class PhotometerTask implements Serializable {
 
 		public final SphericalCoordinates	incident;
 		public final double					wavelength;
 		public final long					samples;
 		public final int					measurementIndex;
 
 		public PhotometerTask(SphericalCoordinates incident, double wavelength, long samples, int measurementIndex) {
 			this.incident			= incident;
 			this.wavelength			= wavelength;
 			this.samples			= samples;
 			this.measurementIndex	= measurementIndex;
 		}
 
 		/**
 		 * Serialization version ID.
 		 */
 		private static final long serialVersionUID = 3363497232661744930L;
 
 	}
 
 	private static class PhotometerTaskWorker implements TaskWorker, Serializable {
 
 		/**
 		 * Creates a new <code>PhotometerTaskWorker</code>.
 		 * @param specimen The <code>Material</code> to be measured.
 		 * @param prototype The prototype <code>CollectorSphere</code> from
 		 * 		which clones are constructed to record hits to.
 		 */
 		public PhotometerTaskWorker(Material specimen, CollectorSphere prototype) {
 			this.specimen = specimen;
 			this.prototype = prototype;
 		}
 
 		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.TaskWorker#performTask(java.lang.Object, ca.eandb.jmist.framework.reporting.ProgressMonitor)
 		 */
 		public Object performTask(Object task, ProgressMonitor monitor) {
 
 			CollectorSphere		collector		= this.prototype.clone();
 			Photometer			photometer		= new Photometer(collector);
 			PhotometerTask		info			= (PhotometerTask) task;
 
 			photometer.setSpecimen(this.specimen);
 			photometer.setIncidentAngle(info.incident);
 			photometer.setWavelength(info.wavelength);
 			photometer.castPhotons(info.samples, monitor);
 
 			return collector;
 
 		}
 
 		/**
 		 * The <code>Material</code> to be measured.
 		 */
 		private final Material specimen;
 
 		/**
 		 * The prototype <code>CollectorSphere</code> from which clones are
 		 * constructed to record hits to.
 		 */
 		private final CollectorSphere prototype;
 
 		/**
 		 * Serialization version ID.
 		 */
 		private static final long serialVersionUID = -2402548700898513324L;
 
 	}
 
 	/** The <code>TaskWorker</code> that performs the work for this job. */
 	private final TaskWorker worker;
 
 	private final double[] wavelengths;
 	private final SphericalCoordinates[] incidentAngles;
 	private final long samplesPerMeasurement;
 	private final long samplesPerTask;
 	private final CollectorSphere[] results;
 	private int nextMeasurementIndex = 0;
 	private long outstandingSamplesPerMeasurement = 0;
 	private final int totalTasks;
 	private int tasksReturned = 0;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 5640925441217948685L;
 
 }
