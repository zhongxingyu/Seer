 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.rcp.views.dashboard;
 
 import gda.device.DeviceException;
 import gda.device.Monitor;
 import gda.device.Scannable;
 import gda.device.ScannableMotionUnits;
 import gda.device.scannable.ScannableMotor;
 import gda.factory.Finder;
 import gda.jython.InterfaceProvider;
 import gda.jython.JythonServerFacade;
 import gda.jython.scriptcontroller.Scriptcontroller;
 import gda.observable.IObserver;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A ScannableObject in the dashboard. Assumes that value is notified as changing by observer. This works for motors
  * which are being moved by GDA. If the Scannable is defined from Jython the dashboard can still work if a script
  * controller called "DashboardObserver" is defined from the Spring file. This controller must be notified from the
  * scannable (for instance a PseudoDevice implementation) when the value changes. This object listens to that observer
  * when it identifies that the scannable is defined in Jythin and not findable.
  */
 public class SimpleScannableObject extends ServerObject {
 
 	/*
 	 * Name of ScriptController that must be present for notification of scannable changes in the dashboard
 	 */
 	private static final String DASHBOARD_OBSERVER = "DashboardObserver";
 	private static final Logger logger = LoggerFactory.getLogger(SimpleScannableObject.class);
 	private static final Pattern TYPE_PATTERN = Pattern.compile("<type '(.*)'>");
 	private static final Pattern CLASS_PATTERN = Pattern.compile("<class '(.*)'>");
 	private static final Pattern MOTOR_PATTERN = Pattern
 			.compile("<type 'gda.device.scannable.ScannableMotor\\$\\$.*'>");
 
 	private String scannableName;
 	private IObserver pseudoObserver;
 
 	/**
 	 * 
 	 */
 	public SimpleScannableObject() {
 		super();
 	}
 
 	/**
 	 * @param scannableName
 	 */
 	public SimpleScannableObject(final String scannableName) {
 		this(scannableName, null);
 	}
 
 	/**
 	 * @param scannableName
 	 * @param unit
 	 */
 	public SimpleScannableObject(final String scannableName, final String unit) {
 
 		this.scannableName = scannableName;
 		this.unit = unit;
 	}
 
 	@Override
 	protected void connect() {
 
 		if (scannableName == null || scannableName.isEmpty()) {
 			setError(true);
 			return;
 		}
 
 		final Scannable scannable = (Scannable) Finder.getInstance().findNoWarn(scannableName);
 		if (scannable == null) {
 			if (createPseudoListener(scannableName))
 				return;
 		}
 		if (scannable == null) {
 			setLabel(getScannableName());
 			setObservableValue("could not connect");
 			setError(true);
 			return;
 		}
 
 		setError(false);
 
 		updateValue(scannable);
 
 		scannable.addIObserver(this);
 
 		try {
 			// Just to be sure we stop the task if the scannable is no longer busy.
 			if (scannable.isBusy() || scannable instanceof Monitor) {
 				updateValue(scannable);
 			}
 		} catch (Exception e1) {
 			logger.debug("Scannable " + label + " is not saying if it is busy.", e1);
 		}
 		createDescription();
 
 	}
 
 	private void createDescription() {
 
 		setDescription("-");
 		setClassName(null);
 
 		if (isError())
 			return;
 		if (scannableName == null || "".equals(scannableName))
 			return;
 
 		String className = null;
 		boolean isScript = false;
 		String clazz = JythonServerFacade.getInstance().evaluateCommand(scannableName + ".__class__");
 
 		if (MOTOR_PATTERN.matcher(clazz).matches()) {
 			clazz = JythonServerFacade.getInstance().evaluateCommand(scannableName + ".getMotor().__class__");
 		}
 
 		Matcher m = TYPE_PATTERN.matcher(clazz);
 		if (m.matches()) {
 			className = m.group(1);
 		} else {
 			m = CLASS_PATTERN.matcher(clazz);
 			if (m.matches()) {
 				className = m.group(1);
 				isScript = true;
 			}
 		}
 
 		setClassName(className);
 		setDescription(isDummy(className, scannableName) ? "Dummy" : isScript ? "Jython" : "Connected");
 	}
 
 	private boolean isDummy(final String className, final String scannableName) {
 		if (scannableName == null || "".equals(scannableName))
 			return false;
 		if (ScannableMotor.class.getName().equals(className)) {
 			final String motor = JythonServerFacade.getInstance().evaluateCommand(
 					scannableName + ".getMotor().__class__");
 			if (motor != null && !"".equals(motor))
 				return motor.contains(".Dummy") ? true : false;
 		}
 		return className.contains(".Dummy") ? true : false;
 	}
 
 
 	private boolean createPseudoListener(final String scannableName) {
 
 		if (scannableName == null || "".equals(scannableName))
 			return false;
 		String val;
 		try {
 			val = InterfaceProvider.getCommandRunner().evaluateCommand("pos " + scannableName);
 		} catch (Exception e1) {
 			return false;
 		}
 		if (val != null) {
 			setError(false);
 			setLabel(scannableName);
 			setUnit(getParsedUnit(val));
 			setObservableValue(getParsedValue(val));
 
 			pseudoObserver = new IObserver() {
 				@Override
 				public void update(Object source, Object arg) {
 					if (!(arg instanceof String))
 						return;
 					final String value = getParsedValue((String) arg);
 					if (value != null) {
 						setObservableValue(value);
 						ServerObjectEvent e = new ServerObjectEvent(SimpleScannableObject.this);
 						notifyServerObjectListeners(e);
 					}
 				}
 			};
 
 			final Scriptcontroller dashOb = (Scriptcontroller) Finder.getInstance().find(DASHBOARD_OBSERVER);
 			if (dashOb != null)
 				dashOb.addIObserver(pseudoObserver);
 			return true;
 		}
 		return false;
 	}
 
 	private String getParsedValue(String val) {
 		return getParsedGroup(val, 1);
 	}
 
 	private String getParsedGroup(final String val, int igroup) {
 		final Pattern pattern = Pattern.compile(scannableName + " : value:\\s*(-?\\d*\\.?\\d*)(\\S*)\\s*");
 		final Matcher mat = pattern.matcher(val);
 		if (mat.matches())
 			return mat.group(igroup);
		final Pattern pattern1 = Pattern.compile(scannableName + " : \\s*(-?\\d*\\.?\\d*)(\\S*).*");
 		final Matcher mat1 = pattern1.matcher(val);
 		if (mat1.matches())
 			return mat1.group(igroup);
 		return null;
 	}
 
 	private String getParsedUnit(final String val) {
 		String unit = getParsedGroup(val, 2);
 		if (unit == null || "".equals(unit.trim())) {
 			if (scannableName != null && !"".equals(scannableName)) {
 				final String unitLine = JythonServerFacade.getInstance().evaluateCommand(
 						scannableName + ".getAttribute(\"userunits\")");
 				if (unitLine != null && !"".equals(unitLine)) {
 					unit = unitLine;
 				}
 			}
 		}
 		return unit;
 	}
 
 	private void disconnectScannables() {
 		if (pseudoObserver != null) {
 			final Scriptcontroller dashOb = (Scriptcontroller) Finder.getInstance().find(DASHBOARD_OBSERVER);
 			if (dashOb != null)
 				dashOb.deleteIObserver(pseudoObserver);
 			pseudoObserver = null;
 		}
 		if (scannableName != null) {
 			final Scannable scannable = (Scannable) Finder.getInstance().findNoWarn(this.scannableName);
 			if (scannable != null)
 				scannable.deleteIObserver(this);
 		}
 	}
 
 	@Override
 	public void disconnect() {
 		super.disconnect();
 		disconnectScannables();
 		if (listeners != null)
 			listeners.clear();
 	}
 
 	private String getUnit(Scannable scannable) {
 		try {
 			String userUnit = scannable.getAttribute(ScannableMotionUnits.USERUNITS).toString();
 			return userUnit;
 		} catch (Exception ignored) {
 			// Scannables are allowed to have no units
 			return null;
 		}
 	}
 
 	private void setLimits(Scannable scannable) {
 		try {
 			final Object lims = scannable.getAttribute("limits");
 			if (lims != null && lims instanceof double[]) {
 				final double[] da = (double[]) lims;
 				setMinimum(da[0]);
 				setMaximum(da[1]);
 				return;
 			}
 
 			// else use the attributes provided by ScannableMotionBase
 			final Object uppLim = scannable.getAttribute("upperGdaLimits");
 			final Object lowlim = scannable.getAttribute("lowerGdaLimits");
 			if (uppLim != null && uppLim instanceof Double[] && lowlim != null && lowlim instanceof Double[]) {
 				Double[] da = (Double[]) uppLim;
 				setMaximum(da[0]);
 				da = (Double[]) lowlim;
 				setMinimum(da[0]);
 				return;
 			}
 
 			setMinimum(null);
 			setMaximum(null);
 		} catch (Exception ignored) {
 			// Scannables are allowed to have no bounds
 			setMinimum(null);
 			setMaximum(null);
 		}
 	}
 
 	/**
 	 * Can be called in any thread. Normally this is called in the timer thread @see ServerObject.startUpdateTask(...)
 	 * and @see ServerObject.startUpdateTask.doSingleTask(...)
 	 */
 	@Override
 	protected void updateValue(final Object event) {
 		final Scannable scannable = (Scannable) event;
 		setError(false);
 		Job job = new Job("Dashboard - " + scannable.getName()) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					setLabel(scannable.getName());
 					setLimits(scannable);
 					setObservableValue("...");
 					setUnit(getUnit(scannable));
 					setObservableValue(scannable.getPosition());
 				} catch (DeviceException e) {
 					setObservableValue(e.getMessage());
 					setError(true);
 				} finally {
 					notifyServerObjectListeners(new ServerObjectEvent(SimpleScannableObject.this));
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(false);
 		job.schedule();
 	}
 
 	public String getScannableName() {
 		return scannableName;
 	}
 
 	public void setScannableName(String scannableName) {
 
 		if (scannableName.equals(this.scannableName)) {
 			return;
 		}
 		disconnectScannables();
 
 		this.scannableName = scannableName;
 	}
 
 	@Override
 	public void delete() {
 		super.delete();
 		disconnectScannables();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
 		return result;
 	}
 
 }
