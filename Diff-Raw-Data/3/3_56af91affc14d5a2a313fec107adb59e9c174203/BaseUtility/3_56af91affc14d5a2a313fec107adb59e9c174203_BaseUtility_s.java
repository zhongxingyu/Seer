 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ============================================================================*/
 
 package net.sf.okapi.applications.rainbow.utilities;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import javax.swing.event.EventListenerList;
 
 import org.eclipse.swt.widgets.Shell;
 
 import net.sf.okapi.applications.rainbow.lib.FilterAccess;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.Util;
 
 public abstract class BaseUtility implements IUtility {
 
 	public static final String VAR_PROJDIR = "${ProjDir}";
 	
 	protected final Logger logger = Logger.getLogger(getClass().getName());
 	protected EventListenerList listenerList = new EventListenerList();
 	protected FilterAccess fa;
 	protected String paramsFolder;
 	protected Shell shell;
 	protected IHelp help;
 	protected ArrayList<InputData> inputs;
 	protected ArrayList<OutputData> outputs;
 	protected String inputRoot;
 	protected String outputRoot;
 	protected String srcLang;
 	protected String trgLang;
 	protected String commonFolder;
 	protected String updateCommand;
 	protected String projectDir;
 	protected boolean canPrompt;
 
 	public BaseUtility () {
 		inputs = new ArrayList<InputData>();
 		outputs = new ArrayList<OutputData>();
 	}
 	
 	public void addCancelListener (CancelListener listener) {
 		listenerList.add(CancelListener.class, listener);
 	}
 
 	public void removeCancelListener (CancelListener listener) {
 		listenerList.remove(CancelListener.class, listener);
 	}
 
 	public String getDescription() {
 		// TODO: Implement real description
 		return null;
 	}
 	
 	public void setContextUI (Object contextUI,
 		IHelp helpParam,
 		String updateCommand,
 		String projectDir,
 		boolean canPrompt)
 	{
 		shell = (Shell)contextUI;
 		help = helpParam;
 		this.updateCommand = updateCommand;
 		this.projectDir = projectDir;
 		this.canPrompt = canPrompt;
 	}
 
 	public void setOptions (String sourceLanguage,
 		String targetLanguage)
 	{
 		srcLang = sourceLanguage;
 		trgLang = targetLanguage;
 	}
 
 	public void setFilterAccess (FilterAccess filterAccess,
 		String paramsFolder)
 	{
 		fa = filterAccess;
 		this.paramsFolder = paramsFolder;
 	}
 
 	protected void fireCancelEvent (CancelEvent event) {
 		Object[] listeners = listenerList.getListenerList();
 		for ( int i=0; i<listeners.length; i+=2 ) {
 			if ( listeners[i] == CancelListener.class ) {
 				((CancelListener)listeners[i+1]).cancelOccurred(event);
 			}
 		}
 	}
 
 	public void addInputData (String path,
 		String encoding,
 		String filterSettings)
 	{
 		inputs.add(new InputData(path, encoding, filterSettings));
 	}
 
 	public void addOutputData (String path,
 		String encoding)
 	{
 		outputs.add(new OutputData(path, encoding));
 		// Compute the longest common folder
 		commonFolder = Util.longestCommonDir(commonFolder,
 			Util.getDirectoryName(path), !Util.isOSCaseSensitive());
 	}
 
 	public String getInputRoot () {
 		return inputRoot;
 	}
 
 	public String getOutputRoot () {
 		return outputRoot;
 	}
 
 	public void resetLists () {
 		inputs.clear();
 		outputs.clear();
		commonFolder = null;
 	}
 
 	public void setRoots (String inputRoot,
 		String outputRoot)
 	{
 		this.inputRoot = inputRoot;
 		this.outputRoot = outputRoot;
 	}
 
 	public String getFolderAfterProcess () {
 		return commonFolder;
 	}
 
 	public String getInputPath (int index) {
 		if ( index > inputs.size()-1 ) return null;
 		return inputs.get(index).path;
 	}
 	
 	public String getInputEncoding (int index) {
 		if ( index > inputs.size()-1 ) return null;
 		return inputs.get(index).encoding;
 	}
 
 	public String getInputFilterSettings (int index) {
 		if ( index > inputs.size()-1 ) return null;
 		return inputs.get(index).filterSettings;
 	}
 
 	public String getOutputPath (int index) {
 		if ( index > inputs.size()-1 ) return null;
 		return outputs.get(index).path;
 	}
 	
 	public String getOutputEncoding (int index) {
 		if ( index > inputs.size()-1 ) return null;
 		return outputs.get(index).encoding;
 	}
 
 	public void cancel () {
 		fireCancelEvent(new CancelEvent(this));
 	}
 
 	public void destroy () {
 		// Do nothing by default
 	}
 
 	public boolean hasNext () {
 		return false;
 	}
 
 }
