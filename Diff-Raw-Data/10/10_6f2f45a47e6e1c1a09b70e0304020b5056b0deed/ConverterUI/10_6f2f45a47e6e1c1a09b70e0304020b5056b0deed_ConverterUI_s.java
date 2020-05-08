 /*
  * ConverterUI.java
  * Copyright (C) 2005-2007 James Lee
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  * 
  * $Id$
  */
 package org.thestaticvoid.iriverter;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.custom.*;
 import org.eclipse.swt.graphics.*;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.dnd.*;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 public class ConverterUI implements SelectionListener, CTabFolder2Listener, DropTargetListener {
 	private Display display;
 	private Shell shell;
 	private ToolItem convertTool, newSingleVideoTool, newDirectoryTool, newDVDTool;
 	private CTabFolder tabFolder;
 	private Map profileMenuItems, dimensionsMenuItems;
 	private MenuItem convert, playFile, newSingleVideo, newDirectory, newDVD, advancedJobs, manualSplit, joinVideos, moveUp, moveDown, closeJob, closeAllJobs, quit, bitrate, videoSize, panAndScan, advancedOptions, audioSync, automaticallySplit, volume, mplayerPath, contents, logViewer, about;
 	private Menu videoSizeMenu;
 	private DropTarget target;
 	private String fileName;
 	private Process proc;
 	private ProgressDialog progressDialog;
 	
 	public ConverterUI() {		
 		display = new Display();
 		
 		shell = new Shell(display);
 		shell.setText("iriverter");
 		InputStream is = getClass().getResourceAsStream("icons/iriverter-64.png");
 		shell.setImage(new Image(display, is));
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.horizontalSpacing = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 		shell.setLayout(gridLayout);
 		
 		extractResources();
 		setupMenus();
 		setupToolBar();
 		
 		tabFolder = new CTabFolder(shell, SWT.CLOSE);
 		tabFolder.setSimple(false);
 		tabFolder.setBorderVisible(true);
 		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
 		tabFolder.addSelectionListener(this);
 		tabFolder.addCTabFolder2Listener(this);
 		
 		target = new DropTarget(shell, DND.DROP_MOVE | DND.DROP_COPY);
 		target.setTransfer(new Transfer[]{FileTransfer.getInstance()});
 		target.addDropListener(this);
 		
 		shell.setMinimumSize(400, 267);
 		shell.setSize(500, 334);
 		shell.open();
 		while (!shell.isDisposed())
 			if (!display.readAndDispatch())
 				display.sleep();
 		
 		display.dispose();
 	}
 	
 	public void setupMenus() {
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 		
 		MenuItem jobs = new MenuItem(menu, SWT.CASCADE);
 		jobs.setText("&Jobs");
 		
 		Menu jobsMenu = new Menu(shell, SWT.DROP_DOWN);
 		jobs.setMenu(jobsMenu);
 		
 		convert = new MenuItem(jobsMenu, SWT.PUSH);
 		convert.setText("&Convert...\tShift+Ctrl+C");
 		convert.setAccelerator(SWT.SHIFT + SWT.CTRL + 'C');
 		convert.addSelectionListener(this);
 		
 		playFile = new MenuItem(jobsMenu, SWT.PUSH);
 		playFile.setText("&Play File...\tShift+Ctrl+F");
 		playFile.setAccelerator(SWT.SHIFT + SWT.CTRL + 'F');
 		playFile.addSelectionListener(this);
 		
 		new MenuItem(jobsMenu, SWT.SEPARATOR);
 		
 		MenuItem newJob = new MenuItem(jobsMenu, SWT.CASCADE);
 		newJob.setText("&New");
 		
 		Menu newJobMenu = new Menu(shell, SWT.DROP_DOWN);
 		newJob.setMenu(newJobMenu);
 		
 		newSingleVideo = new MenuItem(newJobMenu, SWT.PUSH);
 		newSingleVideo.setText("&Single Video\tShift+Ctrl+S");
 		newSingleVideo.setAccelerator(SWT.SHIFT + SWT.CTRL + 'S');
 		newSingleVideo.addSelectionListener(this);
 		
 		newDirectory = new MenuItem(newJobMenu, SWT.PUSH);
 		newDirectory.setText("&Directory\tShift+Ctrl+D");
 		newDirectory.setAccelerator(SWT.SHIFT + SWT.CTRL + 'D');
 		newDirectory.addSelectionListener(this);
 		
 		newDVD = new MenuItem(newJobMenu, SWT.PUSH);
 		newDVD.setText("D&VD\tShift+Ctrl+V");
 		newDVD.setAccelerator(SWT.SHIFT + SWT.CTRL + 'V');
 		newDVD.addSelectionListener(this);
 		
 		advancedJobs = new MenuItem(newJobMenu, SWT.CASCADE);
 		advancedJobs.setText("&Advanced");
 
 		advancedJobs.setEnabled(ConverterOptions.getCurrentProfile().getWrapperFormat().equals("avi"));
 		
 		Menu advancedJobsMenu = new Menu(shell, SWT.DROP_DOWN);
 		advancedJobs.setMenu(advancedJobsMenu);
 		
 		manualSplit = new MenuItem(advancedJobsMenu, SWT.PUSH);
 		manualSplit.setText("Manual &Split");
 		manualSplit.addSelectionListener(this);
 		
 		joinVideos = new MenuItem(advancedJobsMenu, SWT.PUSH);
 		joinVideos.setText("&Join Videos");
 		joinVideos.addSelectionListener(this);
 		
 		new MenuItem(jobsMenu, SWT.SEPARATOR);
 		
 		moveUp = new MenuItem(jobsMenu, SWT.PUSH);
 		moveUp.setText("Move &Up\tPage Up");
 		moveUp.setAccelerator(SWT.PAGE_UP);
 		moveUp.setEnabled(false);
 		moveUp.addSelectionListener(this);
 		
 		moveDown = new MenuItem(jobsMenu, SWT.PUSH);
 		moveDown.setText("Move &Down\tPage Down");
 		moveDown.setAccelerator(SWT.PAGE_DOWN);
 		moveDown.setEnabled(false);
 		moveDown.addSelectionListener(this);
 		
 		new MenuItem(jobsMenu, SWT.SEPARATOR);
 		
 		closeJob = new MenuItem(jobsMenu, SWT.PUSH);
 		closeJob.setText("&Close\tCtrl+W");
 		closeJob.setAccelerator(SWT.CTRL + 'W');
 		closeJob.setEnabled(false);
 		closeJob.addSelectionListener(this);
 		
 		closeAllJobs = new MenuItem(jobsMenu, SWT.PUSH);
 		closeAllJobs.setText("&Close All\tShift+Ctrl+W");
 		closeAllJobs.setAccelerator(SWT.SHIFT + SWT.CTRL + 'W');
 		closeAllJobs.setEnabled(false);
 		closeAllJobs.addSelectionListener(this);
 		
 		quit = new MenuItem(jobsMenu, SWT.PUSH);
 		quit.setText("&Quit\tCtrl+Q");
 		quit.setAccelerator(SWT.CTRL + 'Q');
 		quit.addSelectionListener(this);
 		
 		MenuItem options = new MenuItem(menu, SWT.CASCADE);
 		options.setText("&Options");
 		
 		Menu optionsMenu = new Menu(shell, SWT.DROP_DOWN);
 		options.setMenu(optionsMenu);
 		
 		MenuItem device = new MenuItem(optionsMenu, SWT.CASCADE);
 		device.setText("&Device");
 
 		Menu deviceMenu = new Menu(shell, SWT.DROP_DOWN);
 		device.setMenu(deviceMenu);
 	
 		profileMenuItems = new HashMap();
 		Profile[] profiles = Profile.getAllProfiles();
 		Profile currentProfile = ConverterOptions.getCurrentProfile();
 		
 		Map deviceToProfile = new HashMap();
 		Map brandToDevices = new TreeMap();
 		for (int i = 0; i < profiles.length; i++) {
 			Set devices = (Set) brandToDevices.get(profiles[i].getBrand());
 			if (devices == null) {
 				devices = new TreeSet();
 				brandToDevices.put(profiles[i].getBrand(), devices);
 			}
 			
 			devices.add(profiles[i].getDevice());
 			deviceToProfile.put(profiles[i].getDevice(), profiles[i]);
 		}
 		
 		Iterator brandItr = brandToDevices.keySet().iterator();
 		for (int i = 0; brandItr.hasNext(); i++) {
 			String brand = (String) brandItr.next();
 			
 			MenuItem brandMenuItem = new MenuItem(deviceMenu, SWT.CASCADE);
 			brandMenuItem.setText("&" + (i + 1) + " " + brand);
 			
 			Menu brandMenu = new Menu(shell, SWT.DROP_DOWN);
 			brandMenuItem.setMenu(brandMenu);
 			
 			Iterator deviceItr = ((Set) brandToDevices.get(brand)).iterator();
 			for (int j = 0; deviceItr.hasNext(); j++) {
 				String deviceStr = (String) deviceItr.next();
 				Profile profile = (Profile) deviceToProfile.get(deviceStr);
 				
 				MenuItem profileMenuItem = new MenuItem(brandMenu, SWT.RADIO);
 				profileMenuItem.setText("&" + (j + 1) + " " + deviceStr);
 				profileMenuItem.setSelection(profile.getProfileName().equals(currentProfile.getProfileName()));
 				profileMenuItem.addSelectionListener(this);
 				
 				profileMenuItems.put(profileMenuItem, profile.getProfileName());
 			}
 		}
 		
 		new MenuItem(optionsMenu, SWT.SEPARATOR);
 		
 		bitrate = new MenuItem(optionsMenu, SWT.PUSH);
 		bitrate.setText("&Bitrate...\tShift+Ctrl+B");
 		bitrate.setAccelerator(SWT.SHIFT + SWT.CTRL + 'B');
 		bitrate.addSelectionListener(this);
 		
 		new MenuItem(optionsMenu, SWT.SEPARATOR);
 		
 		videoSize = new MenuItem(optionsMenu, SWT.CASCADE);
 		videoSize.setText("Video &Size");
 		
 		videoSizeMenu = new Menu(shell, SWT.DROP_DOWN);
 		videoSize.setMenu(videoSizeMenu);
 
 		new MenuItem(optionsMenu, SWT.SEPARATOR);
 		
 		panAndScan = new MenuItem(optionsMenu, SWT.CHECK);
 		panAndScan.setText("&Pan and Scan\tShift+Ctrl+P");
 		panAndScan.setAccelerator(SWT.SHIFT + SWT.CTRL + 'P');
 		panAndScan.setSelection(ConverterOptions.getPanAndScan());
 		panAndScan.addSelectionListener(this);
 		
 		advancedOptions = new MenuItem(optionsMenu, SWT.CASCADE);
 		advancedOptions.setText("&Advanced");
 		
 		Menu advancedOptionsMenu = new Menu(shell, SWT.DROP_DOWN);
 		advancedOptions.setMenu(advancedOptionsMenu);
 		
 		audioSync = new MenuItem(advancedOptionsMenu, SWT.PUSH);
 		audioSync.setText("Audio &Sync...");
 		audioSync.addSelectionListener(this);
 		
 		automaticallySplit = new MenuItem(advancedOptionsMenu, SWT.PUSH);
 		automaticallySplit.setText("&Automatically Split...");
 		automaticallySplit.addSelectionListener(this);
 		
 		automaticallySplit.setEnabled(ConverterOptions.getCurrentProfile().getWrapperFormat().equals("avi"));
 		
 		volume = new MenuItem(advancedOptionsMenu, SWT.PUSH);
 		volume.setText("&Volume...");
 		volume.addSelectionListener(this);
 		
 		mplayerPath = new MenuItem(advancedOptionsMenu, SWT.PUSH);
 		mplayerPath.setText("&MPlayer Path...");
 		mplayerPath.addSelectionListener(this);
 
 		MenuItem help = new MenuItem(menu, SWT.CASCADE);
 		help.setText("&Help");
 		
 		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
 		help.setMenu(helpMenu);
 		
 		contents = new MenuItem(helpMenu, SWT.PUSH);
 		contents.setText("&Contents\tF1");
 		contents.setAccelerator(SWT.F1);
 		contents.addSelectionListener(this);
 		
 		logViewer = new MenuItem(helpMenu, SWT.PUSH);
 		logViewer.setText("&Log Viewer");
 		logViewer.addSelectionListener(this);
 		
 		new MenuItem(helpMenu, SWT.SEPARATOR);
 		
 		about = new MenuItem(helpMenu, SWT.PUSH);
 		about.setText("&About");
 		about.addSelectionListener(this);
 		
 		dimensionsMenuItems = new HashMap();
 		profileChanged();
 	}
 	
 	public void extractResources() {
 		ZipInputStream in = new ZipInputStream(getClass().getResourceAsStream("resources.zip"));
 		
 		try {
 			ZipEntry entry;
 			while ((entry = in.getNextEntry()) != null) {
 				File extractedFile = new File(ConverterOptions.CONF_DIR + File.separator + entry.getName());
 				if (entry.isDirectory() && !extractedFile.exists())
 					extractedFile.mkdirs();
 				else if (!entry.isDirectory() && (!extractedFile.exists() || entry.getTime() > extractedFile.lastModified())) {
 					OutputStream out = new FileOutputStream(extractedFile);
 					
 					int length;
 					byte[] buffer = new byte[4096];
 					while ((length = in.read(buffer)) > 0)
 						out.write(buffer, 0, length);
 					
 					out.close();
 					
 					extractedFile.setLastModified(entry.getTime());
 				}
 			}
 		} catch (IOException io) {
 			Logger.logException(io);
 			
 			MessageBox messageBox = new MessageBox(new Shell(display), SWT.ICON_ERROR | SWT.OK);
 			messageBox.setText("Could Not Extract Resources");
 			messageBox.setMessage("An error occured while extracting the resources.  Execution will try to continue.  Please see the log for details.");
 			messageBox.open();
 		}
 		
 		try {
 			in.close();
 		} catch (IOException io) {
 			Logger.logException(io);
 		}
 	}
 	
 	public void setupToolBar() {		
 		ToolBar toolBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
 		toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		convertTool = new ToolItem(toolBar, SWT.PUSH);
 		InputStream is = getClass().getResourceAsStream("icons/convert-22.png");
 		convertTool.setImage(new Image(display, is));
 		convertTool.setToolTipText("Convert");
 		convertTool.addSelectionListener(this);
 		
 		new ToolItem(toolBar, SWT.SEPARATOR);
 		
 		newSingleVideoTool = new ToolItem(toolBar, SWT.PUSH);
 		is = getClass().getResourceAsStream("icons/singlevideo-22.png");
 		newSingleVideoTool.setImage(new Image(display, is));
 		newSingleVideoTool.setToolTipText("Single Video");
 		newSingleVideoTool.addSelectionListener(this);
 		
 		newDirectoryTool = new ToolItem(toolBar, SWT.PUSH);
 		is = getClass().getResourceAsStream("icons/directory-22.png");
 		newDirectoryTool.setImage(new Image(display, is));
 		newDirectoryTool.setToolTipText("Directory");
 		newDirectoryTool.addSelectionListener(this);
 		
 		newDVDTool = new ToolItem(toolBar, SWT.PUSH);
 		is = getClass().getResourceAsStream("icons/dvd-22.png");
 		newDVDTool.setImage(new Image(display, is));
 		newDVDTool.setToolTipText("DVD");
 		newDVDTool.addSelectionListener(this);
 	}
 
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// empty
 	}
 	
 	public void widgetSelected(SelectionEvent e) {
 		if (e.getSource() == tabFolder)
 			tabChanged(false);
 		
 		if (e.getSource() == convert || e.getSource() == convertTool) {
 			java.util.List jobs = new ArrayList();
 			for (int i = 0; i < tabFolder.getItemCount(); i++)
 				jobs.add(tabFolder.getItem(i).getControl());
 			
 			progressDialog = new ProgressDialog(shell, SWT.NONE);
 			
 			boolean canceled = false;
 			while (!canceled)
 				try {
 					Converter converter = new Converter(jobs, progressDialog, MPlayerInfo.getMPlayerPath());
 					converter.start();
 					progressDialog.open();
 					converter.cancel();
 					
 					canceled = true;
 				} catch (MPlayerNotFoundException mpe) {
 					canceled = new MPlayerPathDialog(shell, SWT.NONE).open();
 				}
 		}
 		
 		if (e.getSource() == playFile) {
 			FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
 			fileDialog.setText("Input Video");
 			fileDialog.setFilterExtensions(new String[]{"*.avi;*.vob;*.mkv;*.mpg;*.mpeg;*.ogm;*.mov;*.rm;*.ram;*.wmv;*.asf", "*.avi", "*.vob", "*.mkv", "*.mpg;*.mpeg", "*.ogm", "*.mov", "*.rm;*.ram", "*.wmv;*.asf"});
 			fileDialog.setFilterNames(new String[]{"All Video Files", "AVI Video (*.avi)", "DVD Video Object (*.vob)", "Matroska Video (*.mkv)", "MPEG Video (*.mpg, *.mpeg)", "Ogg Video (*.ogm)", "Quicktime Movie (*.mov)", "Real Video (*.rm, *.ram)", "Windows Media Video (*.wmv, *.asf)"});
 			String file = fileDialog.open();
 	
 			if (file != null) {
 				boolean canceled = false;
 				while (!canceled)
 					try {
 						proc = Runtime.getRuntime().exec(new String[]{MPlayerInfo.getMPlayerPath() + "mplayer", file});
 						canceled = true;
 					} catch (IOException io) {
 						io.printStackTrace();
 						canceled = true;
 					} catch (MPlayerNotFoundException mpe) {
 						canceled = new MPlayerPathDialog(shell, SWT.NONE).open();
 					}
 			}
 		}
 		
 		if (e.getSource() == newSingleVideo || e.getSource() == newSingleVideoTool)
 			newSingleVideo();
 		
 		if (e.getSource() == newDirectory || e.getSource() == newDirectoryTool)
 			newDirectory();
 		
 		if (e.getSource() == newDVD || e.getSource() == newDVDTool)
 			newDVD();
 		
 		if (e.getSource() == manualSplit)
 			newManualSplit();
 		
 		if (e.getSource() == joinVideos)
 			newJoinVideos();
 		
 		if (e.getSource() == moveUp) {
 			Image image = tabFolder.getSelection().getImage();
 			String title = tabFolder.getSelection().getText();
 			Control control = tabFolder.getSelection().getControl();
 			int index = tabFolder.getSelectionIndex();
 			
 			tabFolder.getSelection().dispose();
 			CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE, --index);
 			tabItem.setImage(image);
 			tabItem.setText(title);
 			tabItem.setControl(control);
 			((TabItemControl) control).setTabItem(tabItem);
 			tabFolder.setSelection(index);
 			
 			tabChanged(false);
 		}
 		
 		if (e.getSource() == moveDown) {
 			Image image = tabFolder.getSelection().getImage();
 			String title = tabFolder.getSelection().getText();
 			Control control = tabFolder.getSelection().getControl();
 			int index = tabFolder.getSelectionIndex();
 			
 			tabFolder.getSelection().dispose();
 			CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE, ++index);
 			tabItem.setImage(image);
 			tabItem.setText(title);
 			tabItem.setControl(control);
 			((TabItemControl) control).setTabItem(tabItem);
 			tabFolder.setSelection(index);
 			
 			tabChanged(false);
 		}
 		
 		if (e.getSource() == closeJob) {
 			tabFolder.getSelection().dispose();
 			tabChanged(false);
 		}
 		
 		if (e.getSource() == closeAllJobs) {
 			CTabItem[] tabItems = tabFolder.getItems();
 			for (int i = 0; i < tabItems.length; i++)
 				tabItems[i].dispose();
 			tabChanged(false);
 		}
 		
 		if (e.getSource() == quit)
 			shell.dispose();
 	
 		// Consider putting the actual profile in the map
 		if (profileMenuItems.containsKey(e.getSource())) {
 			MenuItem selectedMenuItem = (MenuItem) e.getSource();
 			String selectedProfileName = (String) profileMenuItems.get(selectedMenuItem);
 			
 			if (selectedProfileName.equals(ConverterOptions.getCurrentProfile().getProfileName()))
 				return;
 			
 			for (Iterator i = profileMenuItems.keySet().iterator(); i.hasNext();)
 				((MenuItem) i.next()).setSelection(false);
 			selectedMenuItem.setSelection(true);
 			
 			ConverterOptions.setCurrentProfile(Profile.getProfile(selectedProfileName));
 			profileChanged();
 		}
 		
 		if (e.getSource() == bitrate)
 			new BitrateDialog(shell, SWT.NONE).open();
 
 		if (dimensionsMenuItems.containsKey(e.getSource())) {
 			MenuItem selectedMenuItem = (MenuItem) e.getSource();
 			Dimensions selectedDimensions = (Dimensions) dimensionsMenuItems.get(selectedMenuItem);
 			
 			if (selectedDimensions.equals(ConverterOptions.getDimensions()))
 				return;
 			
 			for (Iterator i = dimensionsMenuItems.keySet().iterator(); i.hasNext();)
 				((MenuItem) i.next()).setSelection(false);
 			selectedMenuItem.setSelection(true);
 			
 			ConverterOptions.setDimensions(selectedDimensions);
 		}
 		
 		if (e.getSource() == panAndScan)
 			ConverterOptions.setPanAndScan(panAndScan.getSelection());
 		
 		if (e.getSource() == audioSync)
 			new AudioSyncDialog(shell, SWT.NONE).open();
 		
 		if (e.getSource() == automaticallySplit)
 			new AutomaticallySplitDialog(shell, SWT.NONE).open();
 
 		
 		if (e.getSource() == volume)
 			new VolumeDialog(shell, SWT.NONE).open();
 		
 		if (e.getSource() == mplayerPath) {
 			new MPlayerPathDialog(shell, SWT.NONE).open();
 		}
 		
 		if (e.getSource() == contents)
			new HelpBrowser("file://" + ConverterOptions.CONF_DIR + "/doc/index.html");
 		
 		if (e.getSource() == logViewer) {
 			if (LogViewer.getSingleton() == null)
 				new LogViewer();
 			else
 				LogViewer.getSingleton().getShell().setActive();
 		}
 		
 		if (e.getSource() == about)
 			new AboutDialog(shell, SWT.NONE).open();
 	}
 
 	public void profileChanged() {
 		for (Iterator i = dimensionsMenuItems.keySet().iterator(); i.hasNext();)
 			((MenuItem) i.next()).dispose();
 
 		dimensionsMenuItems.clear();
 		
 		Dimensions[] dimensions = ConverterOptions.getCurrentProfile().getDimensions();
 		Dimensions currentDimensions = ConverterOptions.getDimensions();
 
 		for (int i = 0; i < dimensions.length; i++) {
 			MenuItem dimensionsMenuItem = new MenuItem(videoSizeMenu, SWT.RADIO);
 			dimensionsMenuItem.setText("&" + (i + 1) + " " + dimensions[i].toString());
 			dimensionsMenuItem.setSelection(dimensions[i].toString().equals(currentDimensions.toString()));
 			dimensionsMenuItem.addSelectionListener(this);
 
 			dimensionsMenuItems.put(dimensionsMenuItem, dimensions[i]);
 		}
 		
 		advancedJobs.setEnabled(ConverterOptions.getCurrentProfile().getWrapperFormat().equals("avi"));
 		automaticallySplit.setEnabled(ConverterOptions.getCurrentProfile().getWrapperFormat().equals("avi"));
 	}
 	
 	public void close(CTabFolderEvent event) {
 		tabChanged(true);
 	}
 	
 	public void maximize(CTabFolderEvent event) {
 		// empty
 	}
 	
 	public void minimize(CTabFolderEvent event) {
 		// empty
 	}
 	
 	public void restore(CTabFolderEvent event) {
 		// empty
 	}
 	
 	public void showList(CTabFolderEvent event) {
 		// empty
 	}
 	
 	public void tabChanged(boolean closed) {
 		int index = tabFolder.getSelectionIndex();
 		if (closed)
 			index--;
 		
 		if (index == -1) {
 			moveUp.setEnabled(false);
 			moveDown.setEnabled(false);
 			closeJob.setEnabled(false);
 			closeAllJobs.setEnabled(false);
 		} else {
 			moveUp.setEnabled(true);
 			moveDown.setEnabled(true);
 			closeJob.setEnabled(true);
 			closeAllJobs.setEnabled(true);
 			
 			if (index == 0)
 				moveUp.setEnabled(false);
 			if (index == (tabFolder.getItemCount() - (closed ? 2 : 1)))
 				moveDown.setEnabled(false);
 		}
 	}
 	
 	public void dragEnter(DropTargetEvent e) {
 		
 	}
 	
 	public void dragLeave(DropTargetEvent e) {
 		
 	}
 	
 	public void dragOperationChanged(DropTargetEvent e) {
 		
 	}
 	
 	public void dragOver(DropTargetEvent e) {
 		
 	}
 	
 	public void drop(DropTargetEvent e) {
 		if (e.getSource() == target) {
 			if (e.data == null) {
 				e.detail = DND.DROP_NONE;
 				return;
 			}
 			
 			String[] files = (String[]) e.data;
 			for (int i = 0; i < files.length; i++) {
 				File file = new File(files[i]);
 				
 				if (file.isFile() && new VideoFileFilter().accept(file))
 					newSingleVideo().setInputVideo(files[i]);
 				else if (file.isDirectory())
 					if (new File(files[i] + File.separator + "VIDEO_TS").exists())
 						newDVD().setDrive(files[i]);
 					else
 						newDirectory().setInputDirectory(files[i]);
 			}
 		}		
 	}
 	
 	public void dropAccept(DropTargetEvent e) {
 
 	}
 	
 	private SingleVideo newSingleVideo() {
 		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
 		SingleVideo singleVideo = new SingleVideo(tabFolder, SWT.NONE, tabItem);
 		tabItem.setControl(singleVideo);
 		tabFolder.setSelection(tabItem);
 		tabChanged(false);
 		
 		return singleVideo;
 	}
 	
 	private Directory newDirectory() {
 		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
 		Directory directory = new Directory(tabFolder, SWT.NONE, tabItem);
 		tabItem.setControl(directory);
 		tabFolder.setSelection(tabItem);
 		tabChanged(false);
 		
 		return directory;
 	}
 	
 	private DVD newDVD() {
 		DVD lastDVD = null;
 		for (int i = tabFolder.getItemCount() - 1; i >= 0 && lastDVD == null; i--)
 			if (tabFolder.getItem(i).getControl() instanceof DVD)
 				lastDVD = (DVD) tabFolder.getItem(i).getControl();
 		
 		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
 		DVD dvd = new DVD(tabFolder, SWT.NONE, tabItem);
 		tabItem.setControl(dvd);
 		tabFolder.setSelection(tabItem);
 		tabChanged(false);
 		
 		if (lastDVD != null && !lastDVD.getDrive().equals("")) {
 			dvd.setDrive(lastDVD.getDrive());
 			dvd.setTitleInfo(lastDVD.getTitleInfo());
 		} else
 			dvd.setTitleCombo();
 		
 		return dvd;
 	}
 	
 	private ManualSplit newManualSplit() {
 		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
 		ManualSplit manualSplit = new ManualSplit(tabFolder, SWT.NONE, tabItem);
 		tabItem.setControl(manualSplit);
 		tabFolder.setSelection(tabItem);
 		tabChanged(false);
 		
 		return manualSplit;
 	}
 	
 	private JoinVideos newJoinVideos() {
 		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
 		JoinVideos joinVideos = new JoinVideos(tabFolder, SWT.NONE, tabItem);
 		tabItem.setControl(joinVideos);
 		tabFolder.setSelection(tabItem);
 		tabChanged(false);
 		
 		return joinVideos;
 	}
 	
 	public static void main(String[] args) {
 		try {
 			new ConverterUI();
 		} catch (Throwable t) {
 			Logger.logException(t);
 			
 			MessageBox messageBox = new MessageBox(new Shell(Display.getDefault()), SWT.ICON_ERROR | SWT.OK);
 			messageBox.setText("Error");
 			messageBox.setMessage("An unhandled exception occured.  The program will close.  Please see the log for details.");
 			messageBox.open();
 		}
 	}
 }
