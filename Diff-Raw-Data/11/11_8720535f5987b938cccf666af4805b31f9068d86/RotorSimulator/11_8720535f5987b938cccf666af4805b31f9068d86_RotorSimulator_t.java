 package org.hbird.test.rotor;
 
 import org.hbird.core.commons.tmtc.CommandGroup;
 import org.hbird.core.commons.tmtc.ParameterGroup;
 import org.hbird.core.commons.tmtc.exceptions.UnknownParameterException;
 import org.hbird.core.spacesystemmodel.exceptions.UnknownParameterGroupException;
 import org.hbird.core.spacesystempublisher.interfaces.SpaceSystemPublisher;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RotorSimulator {
 
 	private SpaceSystemPublisher publisher;
 
 	private final static Logger LOG = LoggerFactory.getLogger(RotorSimulator.class);
 
 	public void setPublisher(final SpaceSystemPublisher publisher) {
 		this.publisher = publisher;
 	}
 
 	private double az = 0;
 	private double el = 0;
 
 	private int targetAz = 0;
 	private int targetEl = 0;
 
 	private final int maxAz = 450; // Value taken from the Yaesu G-5500 user manual
 	private final int maxEl = 180; // Value taken from the Yaesu G-5500 user manual
 
 	private final double azPerMs = 360d / 58 / 1000; // Values taken from the Yaesu
 	// G-5500 user manual
 	private final double elPerMs = 180d / 67 / 1000; // Values taken from the Yaesu
 	// G-5500 user manual
 
 	private DateTime lastMoved = new DateTime();
 
 	/**
 	 * 
 	 * @param targetAz
 	 * @param targetEl
 	 */
 	public void slewRotor(int targetAz, int targetEl) {
 		this.targetAz = targetAz;
 		// Sanity checks.
 		if (targetAz > maxAz) {
			this.targetAz = maxAz;
 		}
 		else if (targetAz < 0) {
			this.targetAz = 0;
 		}
 
 		this.targetEl = targetEl;
 		// Sanity checks.
 		if (targetEl > maxEl) {
			this.targetEl = maxEl;
 		}
 		else if (targetEl < 0) {
			this.targetEl = 0;
 		}
 
 		// start moving now!
 		this.lastMoved = new DateTime();
 	}
 
 	/**
 	 * This function propagates the simulated rotor model forwards in time. Also, it's the only way to get a telemetry
 	 * reading out of the simulator. It's therefore a bit of a lazy hack, but it gets around the need to have a
 	 * dedicated thread propagating the simulator model. The model is only updated when somebody observes it - if you
 	 * don't look, nothing happens.
 	 * 
 	 * @return Current Az/El reading
 	 * @throws UnknownParameterGroupException
 	 * @throws UnknownParameterException
 	 */
 	public ParameterGroup tick() throws UnknownParameterGroupException, UnknownParameterException {
 		final DateTime now = new DateTime();
 		final Duration moveTime = new Duration(lastMoved, now);
 
 		LOG.trace("Target = " + targetAz + " :: " + targetEl);
 
 		lastMoved = now;
 
 		final double azMoved = azPerMs * moveTime.getMillis();
 		final double elMoved = elPerMs * moveTime.getMillis();
 
 		LOG.trace("azMoved=" + azMoved + ", elMoved=" + elMoved);
 
 		// move az
 		if (Math.abs(targetAz - az) < azMoved) {
 			az = targetAz;
 		}
 		else {
 			// Potential use case for the glorious crement?
 			if (targetAz > az) {
 				az += azMoved;
 			}
 			else {
 				az -= azMoved;
 			}
 		}
 
 		// move el
 		if (Math.abs(targetEl - el) < azMoved) {
 			el = targetEl;
 		}
 		else {
 			// Potential use case for the glorious crement?
 			if (targetEl > el) {
 				el += elMoved;
 			}
 			else {
 				el -= elMoved;
 			}
 		}
 		LOG.trace("Current = " + az + " :: " + el);
 		// return current readings
 		ParameterGroup pg = null;
 		if (publisher != null) {
 			pg = publisher.getParameterGroup("Stock6.tm.PositionPayload");
 			pg.getIntegerParameter("Stock6.tm.Azimuth").setValue((int) az);
 			pg.getIntegerParameter("Stock6.tm.Elevation").setValue((int) el);
 		}
 		else {
 			LOG.error("Rotor Simulator can't construct telemetry because there is no publisher available.");
 		}
 		return pg;
 	}
 
 	public void slewRotor(final CommandGroup cg) throws UnknownParameterException {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Received command group; slewing rotor");
 		}
 		final int targetAz = cg.getIntegerParameter("Stock6.tc.Target Azimuth").getValue();
 		final int targetEl = cg.getIntegerParameter("Stock6.tc.Target Elevation").getValue();
 		slewRotor(targetAz, targetEl);
 	}
 
 }
