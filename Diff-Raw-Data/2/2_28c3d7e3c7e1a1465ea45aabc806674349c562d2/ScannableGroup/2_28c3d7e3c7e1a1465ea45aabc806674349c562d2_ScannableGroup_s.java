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
 
 package gda.device.scannable.scannablegroup;
 
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.scannable.ScannableBase;
 import gda.device.scannable.ScannableUtils;
 import gda.factory.Configurable;
 import gda.factory.FactoryException;
 import gda.factory.Finder;
 import gda.observable.IObserver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.python.core.Py;
 import org.python.core.PyString;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A logical group of scannables
  */
 public class ScannableGroup extends ScannableBase implements Configurable, IScannableGroup, IObserver {
 
 	private static final Logger logger = LoggerFactory.getLogger(ScannableGroup.class);
 
 	// the list of members
 	String[] groupMemberNames = new String[0]; // will use jakarta commons.lang to manipulate this
 	ArrayList<Scannable> groupMembers = new ArrayList<Scannable>();
 
 	/**
 	 * Constructor.
 	 */
 	public ScannableGroup() {
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param name
 	 * @param groupMembers
 	 */
 	public ScannableGroup(String name, Scannable[] groupMembers) {
 		setName(name);
 		setGroupMembers(groupMembers);
 	}
 
 	/**
 	 * Sets the group members that make up this scannable group.
 	 * 
 	 * @param groupMembers
 	 *            the group members
 	 */
 	public void setGroupMembers(ArrayList<Scannable> groupMembers) {
 		Scannable[] members = groupMembers.toArray(new Scannable[groupMembers.size()]);
 		setGroupMembers(members);
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 
 		// add all membernames to the list of members
 		Set<String> namesOfGroupMembers = setOfGroupMemberNames(groupMembers);
 		for (String name : groupMemberNames) {
 
 			if (name == null) {
 				continue;
 			}
 
 			if (!namesOfGroupMembers.contains(name)) {
 				try {
 					Scannable newScannable = (Scannable) Finder.getInstance().find(name.trim());
 					if (newScannable != null) {
 						addGroupMember(newScannable);
 						namesOfGroupMembers.add(name);
 						newScannable.addIObserver(this);
 					}
 				} catch (ClassCastException e) {
 					// finder must have returned something which was not a Scannable
 				}
 
 			}
 		}
 
 		// Update group member names to match group members
 		setGroupMemberNamesArrayUsingGroupMembersList();
 
 		// configure all members
 		for (Scannable scannable : groupMembers) {
 			if (scannable instanceof ScannableBase) {
 				((ScannableBase) scannable).configure();
 			}
 			scannable.addIObserver(this);
 		}
 
 		setArrays();
 
 		configured = true;
 	}
 
 	@Override
 	public void addGroupMemberName(String groupMemberName) {
 		if (!ArrayUtils.contains(groupMemberNames, groupMemberName)) {
 			groupMemberNames = (String[]) ArrayUtils.add(groupMemberNames, groupMemberName);
 		}
 	}
 
 	/**
 	 * Sets the group member names for this scannable group.
 	 * 
 	 * @param groupMemberNames
 	 *            the group member names
 	 */
 	public void setGroupMemberNames(List<String> groupMemberNames) {
 		this.groupMemberNames = new String[0];
 		for (String name : groupMemberNames) {
 			addGroupMemberName(name);
 		}
 	}
 
 	@Override
 	public String[] getGroupMemberNames() {
 		return this.groupMemberNames;
 	}
 
 	/**
 	 * Adds a scannable to this group. This will not add a Scannable if its name matches anther member's name, even if
 	 * they are different objects.
 	 * 
 	 * @param groupMember
 	 */
 	public void addGroupMember(Scannable groupMember) {
 		Scannable member = getGroupMember(groupMember.getName());
 		if (member == null) {
 			if (!this.groupMembers.contains(groupMember)) {
 				this.groupMembers.add(groupMember);
 				if (configured) {
 					setGroupMemberNamesArrayUsingGroupMembersList();
 				}
 			}
 		} else {
 			logger.info(getName() + " will not add Scannable named " + groupMember.getName()
 					+ " as it already has a Scannable with the same name");
 		}
 	}
 
 	public void removeGroupMember(Scannable groupMember) {
 		if (!this.groupMembers.contains(groupMember)) {
 			this.groupMembers.remove(groupMember);
 			if (configured) {
 				setGroupMemberNamesArrayUsingGroupMembersList();
 			}
 		}
 	}
 
 	public void removeGroupMember(String nameOfGroupMember) {
 		Scannable member = getGroupMember(nameOfGroupMember);
 		if (member != null) {
 			removeGroupMember(member);
 		}
 	}
 
 	/**
 	 * @return array of scannable objects in this group
 	 */
 	public ArrayList<Scannable> getGroupMembers() {
 		return this.groupMembers;
 	}
 
 	/**
 	 * @param name
 	 * @return the Scannable of the given name
 	 */
 	public Scannable getGroupMember(String name) {
 		for (Scannable member : groupMembers) {
 			if (member.getName().equals(name)) {
 				return member;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the members of this group
 	 * 
 	 * @param groupMembers
 	 */
 	public void setGroupMembers(Scannable[] groupMembers) {
 		this.groupMembers = new ArrayList<Scannable>(Arrays.asList(groupMembers));
 		if (configured) {
 			setGroupMemberNamesArrayUsingGroupMembersList();
 			setArrays();
 		}
 	}
 
 	/**
 	 * See __getattr__(String name).
 	 **/
 	public Object __getattr__(PyString name) {
 		return __getattr__(name.internedString());
 	}
 
 	/**
 	 * Python method used by the interpreter to get attributes that are not defined on an object. Used here to provide
 	 * dotted access to member scannables.
 	 * 
 	 * @param name
 	 * @return The named member scannable or null if it does not exist.
 	 */
 	Object __getattr__(String name) {
 		// find the member's name and return it
 		for (Scannable member : groupMembers) {
 			if (member.getName().compareTo(name) == 0) {
 				return member;
 			}
 		}
 
 		// else try adding the scanablegroups name to the request
 		String newName = getName() + "_" + name;
 		for (Scannable member : groupMembers) {
 			if (member.getName().compareTo(newName) == 0) {
 				return member;
 			}
 		}
 
 		throw Py.AttributeError(name);
 	}
 
 	@Override
 	public void asynchronousMoveTo(Object position) throws DeviceException {
 		Vector<Double[]> targets = extractPositionsFromObject(position);
 
 		// send out moves
 		for (int i = 0; i < groupMembers.size(); i++) {
 
 			Double[] thisTarget = targets.get(i);
 			if (thisTarget.length == 1) {
 				if (targets.get(i)[0] != null) {
 					groupMembers.get(i).asynchronousMoveTo(targets.get(i)[0]);
 				}
 			} else {
 				groupMembers.get(i).asynchronousMoveTo(targets.get(i));
 			}
 		}
 
 	}
 
 	protected Vector<Double[]> extractPositionsFromObject(Object position) throws DeviceException {
 		// map object to an array of doubles
 		int inputLength = 0;
 		for (Scannable member : groupMembers) {
 			inputLength += member.getInputNames().length;
 		}
 		Double[] targetPosition = gda.device.scannable.ScannableUtils.objectToArray(position);
 		if (targetPosition.length != inputLength) {
 			throw new DeviceException("Position does not have correct number of fields. Expected = " + inputLength
 					+ " actual = " + targetPosition.length + " position= " + position.toString());
 		}
 		// break down to individual commands
 		int targetIterator = 0;
 		Vector<Double[]> targets = new Vector<Double[]>();
 		for (Scannable member : groupMembers) {
 			Double[] thisTarget = new Double[member.getInputNames().length];
 			for (int i = 0; i < member.getInputNames().length; i++) {
 				thisTarget[i] = targetPosition[targetIterator];
 				targetIterator++;
 			}
 			targets.add(thisTarget);
 		}
 		return targets;
 	}
 
 	@Override
 	public Object getPosition() throws DeviceException {
 
 		// create array of correct length
 		int outputLength = 0;
 		for (Scannable member : groupMembers) {
 			outputLength += member.getInputNames().length;
 			outputLength += member.getExtraNames().length;
 		}
 		Object[] position = new Object[outputLength];
 
 		// loop through members and add position values to array
 		try {
 			int i = 0;
 			for (Scannable member : groupMembers) {
 
 				Object[] memberPosition;
 				try {
 					
 					Object pos = member.getPosition();
 					if (pos != null) {
 						memberPosition = ScannableUtils.objectToArray(pos);						
 					} else {
 						memberPosition = new Object[0];
 					}
 				} catch (Exception e) {
 					// if this fails, try getting strings instead. If that fails then let exception escalate
 					memberPosition = ScannableUtils.getFormattedCurrentPositionArray(member);
 				}
 
 				for (Object element : memberPosition) {
 					position[i] = element;
 					i++;
 				}
 			}
 		} catch (Exception e) {
 			throw new DeviceException("Exception occurred during " + getName() + " getPosition(): " + e.getMessage());
 		}
 
 		return position;
 	}
 
 	@Override
 	public boolean isBusy() throws DeviceException {
 		for (Scannable member : groupMembers) {
 			if (member.isBusy()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void setExtraNames(String[] names) {
 		// do nothing: don't want to override members' extra names
 	}
 
 	@Override
 	public void setInputNames(String[] names) {
 		// do nothing: don't want to override members' input names
 	}
 
 	@Override
 	public String toFormattedString() {
 		//TODO this works if the toFormattedString method of the members conforms to a standard. But I don't think there is one!
 		//Rather use getPosition and format here.
 		String membersOutput = getName() + " ::\n";
 		for (Scannable member : groupMembers) {
 			membersOutput += member.toFormattedString() + "\n";
 		}
 
 		String[] originalInputNames = getInputNames();
 		String[] namesToSplitOn = getInputNames();
 		String[] names = getGroupMemberNames();
 		String[] extras = getExtraNames();
 		
 		// FIXME regex-based splitting of membersOutput is broken if one group member name is a substring of another - e.g. "col_y" and "col_yaw"
 		
 		// find the longest name, to help with formatting the output
 		int longestName = 0;
 		for (String objName : originalInputNames){
 			if (objName.length() > longestName){
 				longestName = objName.length();
 			}
 		}
 
 		if (originalInputNames.length + extras.length == 0) {
 			return membersOutput;
 		}
 
 		if (extras.length > 0) {
 			namesToSplitOn = (String[]) ArrayUtils.addAll(namesToSplitOn, extras);
 		}
 
 		namesToSplitOn = (String[]) ArrayUtils.add(namesToSplitOn, getName());
 		namesToSplitOn = (String[]) ArrayUtils.addAll(namesToSplitOn, names);
 
 		String regex = "";
 		for (String name : namesToSplitOn) {
 			regex += name + " +|";
 		}
 		regex = regex.substring(0, regex.length() - 1);
 
 		String[] values = membersOutput.split(regex);
 
 		String returnString = getName() + "::\n";
 		int nextNameIndex = 0;
 		for (int i = 0; i < values.length; i++) {
 			String value = values[i].trim();
 			if (value.startsWith(":")) {
 				value = value.substring(1).trim();
 			}
 			if (StringUtils.containsOnly(value, ":()") || value.isEmpty()) {
 				continue;
 			}
			returnString += " " + StringUtils.rightPad(originalInputNames[nextNameIndex], longestName) + ": " + value
 					+ "\n";
 			nextNameIndex++;
 		}
 		returnString.trim();
 		returnString = returnString.substring(0, returnString.length() - 1);
 
 		return returnString;
 
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		for (Scannable member : groupMembers) {
 			member.stop();
 		}
 	}
 
 	/**
 	 * Acts as a fan-out for messages from the Scannables inside this group {@inheritDoc}
 	 * 
 	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		/*
 		 * only fan out if the notification did not already come from oneself. This is required for situations where a
 		 * scannable is observing its surrounding scannableGroup and the scannablegroup is observing its children.
 		 */
 		if (theObserved != this)
 			notifyIObservers(this, changeCode);
 	}
 
 	@Override
 	public String[] getExtraNames() {
 		// recalculate every time as these attributes may be dynamic
 		String[] extraNames = new String[0];
 		for (Scannable member : groupMembers) {
 			extraNames = (String[]) ArrayUtils.addAll(extraNames, member.getExtraNames());
 		}
 		return extraNames;
 	}
 
 	@Override
 	public String[] getInputNames() {
 		// recalculate every time as these attributes may be dynamic
 		String[] inputNames = new String[0];
 		for (Scannable member : groupMembers) {
 			inputNames = (String[]) ArrayUtils.addAll(inputNames, member.getInputNames());
 		}
 		return inputNames;
 	}
 
 	@Override
 	public String[] getOutputFormat() {
 		// recalculate every time as these attributes may be dynamic
 		String[] outputFormat = new String[0];
 		for (Scannable member : groupMembers) {
 			outputFormat = (String[]) ArrayUtils.addAll(outputFormat, member.getOutputFormat());
 		}
 		return outputFormat;
 	}
 
 	@Override
 	public void atPointEnd() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atPointEnd();
 		}
 	}
 
 	@Override
 	public void atPointStart() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atPointStart();
 		}
 	}
 
 	@Override
 	public void atScanLineEnd() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atScanLineEnd();
 		}
 
 	}
 
 	@Override
 	public void atScanEnd() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atScanEnd();
 		}
 	}
 
 	@Override
 	public void atLevelMoveStart() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atLevelMoveStart();
 		}
 	}
 
 	@Override
 	public void atCommandFailure() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atCommandFailure();
 		}
 	}
 
 	@Override
 	public void atScanStart() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atScanStart();
 		}
 	}
 
 	@Override
 	public void atScanLineStart() throws DeviceException {
 		for (Scannable scannable : groupMembers) {
 			scannable.atScanLineStart();
 		}
 	}
 
 	@Override
 	public String checkPositionValid(Object illDefinedPosObject) throws DeviceException {
 
 		Vector<Double[]> targets;
 		try {
 			targets = extractPositionsFromObject(illDefinedPosObject);
 		} catch (Exception e) {
 			return e.getMessage();
 		}
 
 		for (int i = 0; i < groupMembers.size(); i++) {
 			Double[] thisTarget = targets.get(i);
 			if ((thisTarget!=null) && (thisTarget.length > 0) && (thisTarget[0] != null)) {
 				String reason = groupMembers.get(i).checkPositionValid(thisTarget);
 				if (reason != null) {
 					return reason;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Updates the input names array, extra names array and format array to match the group members.
 	 */
 	protected void setArrays() {
 		// assume that the groupMembers array has been filled
 		// create array of correct length
 		int inputLength = 0;
 		int extraLength = 0;
 		int formatLength = 0;
 		for (Scannable member : groupMembers) {
 			inputLength += member.getInputNames().length;
 			extraLength += member.getExtraNames().length;
 			formatLength += member.getOutputFormat().length;
 		}
 		this.inputNames = new String[inputLength];
 		this.extraNames = new String[extraLength];
 		this.outputFormat = new String[formatLength];
 		int input = 0;
 		int extra = 0;
 		int format = 0;
 		for (Scannable member : groupMembers) {
 			String[] thisInputNames = member.getInputNames();
 			if (thisInputNames.length == 1) {
 				this.inputNames[input] = member.getName();
 				input++;
 			} else {
 				for (String element : thisInputNames) {
 					this.inputNames[input] = element;
 					input++;
 				}
 			}
 			String[] thisExtraNames = member.getExtraNames();
 			for (String element : thisExtraNames) {
 				this.extraNames[extra] = element;
 				extra++;
 			}
 			String[] thisFormats = member.getOutputFormat();
 			for (String element : thisFormats) {
 				this.outputFormat[format] = element;
 				format++;
 			}
 
 		}
 
 	}
 
 	protected void setGroupMemberNamesArrayUsingGroupMembersList() {
 		groupMemberNames = setOfGroupMemberNames(groupMembers).toArray(new String[0]);
 	}
 
 	private static Set<String> setOfGroupMemberNames(List<Scannable> groupMembers) {
 		Set<String> name = new LinkedHashSet<String>();
 		for (Scannable groupMember : groupMembers) {
 			name.add(groupMember.getName());
 		}
 		return name;
 	}
 
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		for (Scannable scannable : groupMembers) {
 			scannable.waitWhileBusy();
 		}
 	}
 
 }
