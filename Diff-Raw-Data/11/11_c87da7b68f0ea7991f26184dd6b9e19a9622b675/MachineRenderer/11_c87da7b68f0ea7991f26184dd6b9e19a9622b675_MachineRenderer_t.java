 /*
     This file is part of SchedVis.
 
     SchedVis is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SchedVis is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SchedVis.  If not, see <http://www.gnu.org/licenses/>.
 
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.rendering;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.swing.SwingWorker;
 
 import org.apache.log4j.Logger;
 
 import cz.muni.fi.spc.SchedVis.model.Database;
 import cz.muni.fi.spc.SchedVis.model.entities.Event;
 import cz.muni.fi.spc.SchedVis.model.entities.Machine;
 
 /**
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public final class MachineRenderer extends SwingWorker<Image, Void> {
 
 	/**
 	 * Holds the machine whose schedule is currently being rendered.
 	 */
 	private final Machine m;
 
 	/**
 	 * Holds a name of the database so that the cached schedule images from
 	 * different databases don't interfere.
 	 */
 	private static final String instanceId = new File(Database.getName())
 	    .getName();
 
 	/**
 	 * Holds the position on the timeline that is currently being rendered.
 	 */
 	private final Integer clock;
 
 	/**
 	 * How many pixels shall one CPU of a machine occupy on the y axis of the
 	 * schedule.
 	 */
 	private static final Integer NUM_PIXELS_PER_CPU = 4;
 
 	/**
 	 * How many pixels shall be used per a single tick on the x axis of the
 	 * schedule.
 	 */
 	private static final Float NUM_PIXELS_PER_TICK = new Float(0.1);
 	/**
 	 * Total length of the x axis of the schedule. If you need to change it,
 	 * please change the input values, not the equation.
 	 */
 	private static final Integer LINE_WIDTH = Math.round(Event.getMaxJobSpan()
 	    * MachineRenderer.NUM_PIXELS_PER_TICK);
 
 	/**
 	 * Colors that are available for the jobs. This array can be extended at will
 	 * and the color-picking code will adjust to it.
 	 * 
 	 * Please remember not to use following colors: white (background for
 	 * machines), dark gray (background for disabled machines) and red (overdue
 	 * jobs).
 	 */
 	private static final Color[] colors = { Color.BLUE, Color.CYAN, Color.GREEN,
 	    Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY, Color.PINK,
 	    Color.YELLOW };
 
 	/**
 	 * Holds a font used throughout the schedules. Memory use improvement.
 	 */
 	private static Font font = new Font("Monospaced", Font.PLAIN, 9);
 
 	private final boolean isCaching;
 
 	private List<Event> events;
 
 	private static Logger logger = Logger.getLogger(MachineRenderer.class);
 
 	private final Map<String, Integer[]> sets = new HashMap<String, Integer[]>();
 
 	public MachineRenderer(final Machine m, final Integer clock,
 	    final boolean isCaching, final PropertyChangeListener l) {
 		this.m = m;
 		this.clock = clock;
 		this.isCaching = isCaching;
 		if (l != null) {
 			this.addPropertyChangeListener(l);
 		}
 	}
 
 	/**
 	 * Performs the actual drawing of the machine schedule. Draws a frame and
 	 * calls another method to perform drawing of jobs.
 	 * 
 	 * @return
 	 */
 	private BufferedImage actuallyDraw() {
 		Double time = Double.valueOf(System.nanoTime());
 		this.events = Machine.getLatestSchedule(this.m, this.clock);
 		final BufferedImage img = new BufferedImage(MachineRenderer.LINE_WIDTH,
 		    this.m.getCPUs() * MachineRenderer.NUM_PIXELS_PER_CPU,
 		    BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g = (Graphics2D) img.getGraphics();
 		this.fineTuneGraphics(g);
 		g.setFont(MachineRenderer.font);
 		boolean isActive = Machine.isActive(this.m, this.clock);
 		if (isActive) {
 			g.setColor(Color.WHITE);
 		} else {
 			g.setColor(Color.DARK_GRAY);
 		}
 		g.fillRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
 		this.drawJobs(g);
 		if (isActive) {
 			g.setColor(Color.BLACK);
 			g.drawString(this.m.getName(), 1, 9);
 		} else {
 			g.setColor(Color.WHITE);
 			g.drawString(this.m.getName() + " (off-line)", 1, 9);
 		}
 		int zeroPos = this.getZero();
 		if (zeroPos > 0) {
 			g.drawLine(zeroPos, 0, zeroPos, this.m.getCPUs()
 			    * MachineRenderer.NUM_PIXELS_PER_CPU);
 		}
 		time = (System.nanoTime() - time) / 1000 / 1000 / 1000;
 		MachineRenderer.logger.debug(this.m.getName() + "@" + this.clock
 		    + " finished rendering. Took " + time + " seconds.");
 		return img;
 	}
 
 	/**
 	 * Background task to render the images used for machine schedules.
 	 * 
 	 * The logic in this method tries to cache rendered images whenever possible.
 	 * If a cache file is not found, image is rendered and, if possible, cached so
 	 * that it needs not be rendered next time.
 	 */
 	@Override
 	public Image doInBackground() {
 		File f = new File(this.getFilename()).getAbsoluteFile();
 		BufferedImage img = null;
 		if (!f.exists()) {
 			img = this.actuallyDraw();
 			try {
 				ImageIO.write(img, "gif", f);
 			} catch (IOException e) {
 				MachineRenderer.logger.warn("Won't cache machine " + this.m.getId()
 				    + "@" + this.clock + ". Failed to write into a file "
 				    + f.getAbsolutePath() + ".");
 			}
 		} else if (!this.isCaching) {
			MachineRenderer.logger.debug("3");
 			try {
 				img = ImageIO.read(f);
 			} catch (IOException e) {
 				MachineRenderer.logger.warn("Cannot read cache for machine "
 				    + this.m.getId() + "@" + this.clock
 				    + ". Failed to write into a file " + f.getAbsolutePath() + ".");
 				img = this.actuallyDraw();
 			}
 		}
 		if (!this.isCaching) {
 			return img;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Takes machine schedule data and renders them.
 	 * 
 	 * @param img
 	 * @todo Produces unclear job boundaries, probably because of rounding.
 	 */
 	private void drawJobs(final Graphics2D g) {
 		// render jobs in a schedule, one by one
 		for (final Event evt : this.events) {
 			// get assigned CPUs, set will ensure they are unique and sorted
 			synchronized (this.sets) {
 				if (!this.sets.containsKey(evt.getAssignedCPUs())) {
 					final Set<Integer> assignedCPUs = new HashSet<Integer>();
 					for (final String num : evt.getAssignedCPUs().split(",")) {
 						assignedCPUs.add(Integer.valueOf(num));
 					}
 					this.sets.put(evt.getAssignedCPUs(), assignedCPUs
 					    .toArray(new Integer[0]));
 				}
 			}
 			/*
 			 * now isolate all the contiguous blocks of CPUs in the job and paint
 			 * them.
 			 */
 			final Integer[] cpus = this.sets.get(evt.getAssignedCPUs());
 			for (int i = 0; i < cpus.length; i++) {
 				final int crntCPU = cpus[i];
 				try {
 					while (cpus[i + 1] == cpus[i] + 1) {
 						// loop until a gap is found in the list of used CPUs
 						i++;
 					}
 				} catch (final ArrayIndexOutOfBoundsException e) {
 					// but finish when all the CPUs have been seeked through
 				}
 				final int lastCPU = cpus[i];
 				final int numCPUs = lastCPU - crntCPU + 1;
 				// now draw
 				final int jobStartX = this.getStartingPosition(evt);
 				if (jobStartX < 0) {
 					MachineRenderer.logger.warn("Machine " + this.m.getName() + " at "
 					    + this.clock + " is drawing " + jobStartX
 					    + " before its boundary.");
 				}
 				final int jobLength = this.getJobLength(evt);
 				final int ltY = crntCPU * MachineRenderer.NUM_PIXELS_PER_CPU;
 				final int jobHgt = numCPUs * MachineRenderer.NUM_PIXELS_PER_CPU;
 				if ((evt.getDeadline() > -1) && (evt.getDeadline() < this.clock)) {
 					// the job has a deadline and has missed it
 					g.setColor(Color.RED);
 				} else {
 					// job with no deadlines
 					g.setColor(this.getJobColor(evt.getJob()));
 				}
 				g.fillRect(jobStartX, ltY, jobLength, jobHgt);
 				g.setColor(Color.BLACK);
 				g.drawString(evt.getJob().toString(), jobStartX + 2, ltY + jobHgt - 2);
 				int rightBoundary = jobStartX + jobLength - MachineRenderer.LINE_WIDTH;
 				if (rightBoundary > 0) {
 					MachineRenderer.logger.warn("Machine " + this.m.getName() + " at "
 					    + this.clock + " is drawing " + rightBoundary
 					    + " over its boundary.");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update the graphics object so that it performs better.
 	 * 
 	 * @param g
 	 */
 	private void fineTuneGraphics(final Graphics2D g) {
 		g.setRenderingHint(RenderingHints.KEY_RENDERING,
 		    RenderingHints.VALUE_RENDER_SPEED);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 		    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
 		    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
 		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
 		    RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 		    RenderingHints.VALUE_ANTIALIAS_OFF);
 	}
 
 	/**
 	 * Get filename for the cache file.
 	 * 
 	 * @return The file name.
 	 * @todo Make the max length of the id unlimited.
 	 */
 	private String getFilename() {
 		String id = "0000000000" + this.m.getId();
 		String id2 = "0000000000" + this.clock;
 		return "../tmp/schedvis-" + MachineRenderer.instanceId + "-"
 		    + id.substring(id.length() - 10, id.length()) + "-"
 		    + id2.substring(id2.length() - 10, id2.length()) + ".gif";
 	}
 
 	/**
 	 * Make sure a job has always the same color, no matter when and where it is
 	 * painted.
 	 * 
 	 * @param jobId
 	 * @return A color that shall be used for that job. May be ignored when the
 	 *         job is overdue.
 	 */
 	private Color getJobColor(final Integer jobId) {
 		return MachineRenderer.colors[jobId % MachineRenderer.colors.length];
 	}
 
 	private int getJobLength(final Event evt) {
 		try {
 			return Math.round((evt.getExpectedEnd() - evt.getExpectedStart())
 			    * MachineRenderer.NUM_PIXELS_PER_TICK);
 		} catch (final NullPointerException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Get the starting position for the event, when being rendered on the screen.
 	 * 
 	 * @param evt
 	 *          The event in question.
 	 * @return The X starting coordinate.
 	 */
 	private int getStartingPosition(final Event evt) {
 		try {
 			return Math.round((evt.getExpectedStart() - this.events.get(0)
 			    .getExpectedStart())
 			    * MachineRenderer.NUM_PIXELS_PER_TICK);
 		} catch (final NullPointerException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Get the position of the current clock on the timeline being rendered.
 	 * 
 	 * 
 	 * @return
 	 */
 	private int getZero() {
 		return Math.round(Math.abs(Math.min(this.events.get(0).getExpectedStart()
 		    - this.clock, 0))
 		    * MachineRenderer.NUM_PIXELS_PER_TICK);
 	}
 
 }
