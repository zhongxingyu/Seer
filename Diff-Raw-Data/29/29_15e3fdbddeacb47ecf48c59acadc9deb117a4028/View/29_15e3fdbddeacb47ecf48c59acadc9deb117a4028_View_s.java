 package com.robertdiebels.project.lolimagecollector;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.TreeSet;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ViewPart;
 
 public class View extends ViewPart implements SelectionListener {
 	public static final String ID = "LoLImageCollector.view";
 
 	private String filePath;
 
 	// Output folder constants
 	private final static String OUTPUT_FOLDER_LOL_IMAGES = "\\LoL_images",
			OUTPUT_FOLDER_CHAMPION = "\\champion",
 			OUTPUT_FOLDER_ITEMS = "\\items",
 			OUTPUT_FOLDER_MASTERIES = "\\masteries",
 			OUTPUT_FOLDER_RUNES = "\\runes",
 			OUTPUT_FOLDER_SPELLS = "\\summoner_spells",
 			OUTPUT_FOLDER_ABILITIES = "\\abilities",
 			OUTPUT_FOLDER_SKINS = "\\skins",
 			OUTPUT_FOLDER_SKINS_LARGE = "\\large",
 			OUTPUT_FOLDER_SKINS_SMALL = "\\small",
 			FILE_NAME_HEADSHOT = "headshot";
 	// Input folder constants
 	private final static String INPUT_FOLDER_CHAMPIONS = "\\champions",
 			INPUT_FOLDER_RELEASES = "\\rads\\projects\\lol_air_client\\releases\\",
 			INPUT_FOLDER_IMAGES = "\\images";
 	String inputFolderPath, outputFolderPath;
 
 	// Messages
 	private final static String MSG_INPUT_CAN_NOT_BE_EMPTY = "Input cannot be empty.",
 			MSG_OUTPUT_CAN_NOT_BE_EMPTY = "Output cannot be empty.",
 			MSG_DIRECTORY_DOES_NOT_EXIST = "Directory does not exist.",
 			MSG_CREATE_ROOT_DIRECTORIES = "Root directories.",
 			MSG_CREATE_CHAMPION_FOLDERS_FOR = "Champion folders for, ",
 			MSG_COPY_CHAMPION_IMAGES = "Images for champion, ",
 			MSG_DONE = "Done.";
 	// Message prefixes
 	private final static String PREFIX_COPYING = "Copying: ",
 			PREFIX_CREATING = "Creating: ";
 
 	private Button directoryButton;
 
 	private Text directoryText;
 
 	private Text outputText;
 
 	private Button outputButton;
 
 	private Button startButton;
 
 	private Label messageLabel;
 
 	private File outputDirectory;
 
 	private File lolDirectory;
 
 	private void init() {

 		if (lolDirectory.exists()) {
 			String directoryPath = directoryText.getText();
 
 			if (directoryPath != null && !StringUtils.isEmpty(directoryPath)) {
 
 				inputFolderPath = directoryPath + INPUT_FOLDER_RELEASES;
 				File inputFolder = new File(inputFolderPath);
 				File[] files = inputFolder.listFiles();
 				TreeSet<File> directories = new TreeSet<File>();
 				for (File file : files) {
 					if (file.isDirectory()) {
 						directories.add(file);
 					}
 				}
 				File directory = directories.last();
 
 				inputFolderPath = directory.getAbsolutePath()
 						+ "\\deploy\\assets";
 			} else
 				messageLabel.setText(MSG_INPUT_CAN_NOT_BE_EMPTY);
 		} else
 			messageLabel.setText(MSG_DIRECTORY_DOES_NOT_EXIST);
 		if (outputDirectory.exists()) {
 			String directoryPath = outputText.getText();
 			if (directoryPath != null && !StringUtils.isEmpty(directoryPath)) {
 				outputFolderPath = outputText.getText();
 			} else
 				messageLabel.setText(MSG_OUTPUT_CAN_NOT_BE_EMPTY);
 		} else
 			messageLabel.setText(MSG_DIRECTORY_DOES_NOT_EXIST);
 
 		CreateDirectories();
 		MoveChampions();
 
 		messageLabel.setText(MSG_DONE);

 	}
 
 	private void CreateDirectories() {
 		messageLabel.setText(PREFIX_CREATING + MSG_CREATE_ROOT_DIRECTORIES);
		messageLabel.redraw();
 
 		new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 				+ OUTPUT_FOLDER_CHAMPION).mkdirs();
 		new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 				+ OUTPUT_FOLDER_ITEMS).mkdirs();
 		new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 				+ OUTPUT_FOLDER_MASTERIES).mkdirs();
 		new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 				+ OUTPUT_FOLDER_RUNES).mkdirs();
 		new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 				+ OUTPUT_FOLDER_SPELLS).mkdirs();
 	}
 
 	public void MoveChampions() {
 		File fileEntry = new File(inputFolderPath + INPUT_FOLDER_IMAGES
 				+ INPUT_FOLDER_CHAMPIONS);
 		File[] fileEntries = fileEntry.listFiles();
 
 		for (File file : fileEntries) {
 
 			String fileName = file.getName();
 			String[] fileNameSplit = fileName.split("_");
 			String wantedFileName = "\\"
 					+ fileNameSplit[fileNameSplit.length - 1];
 
 			String[] fileExtensionSplit = fileName.split("\\.");
 			String champNameFolder = "\\" + fileNameSplit[0];
 			String champName = champNameFolder.replace("\\", "");
 
 			messageLabel.setText(PREFIX_CREATING
 					+ MSG_CREATE_CHAMPION_FOLDERS_FOR + champName);
 
 			File champDirectory = new File(outputFolderPath
 					+ OUTPUT_FOLDER_LOL_IMAGES + OUTPUT_FOLDER_CHAMPION
 					+ champNameFolder);
 			champDirectory.mkdirs();
 			if (champDirectory.exists()) {
 				new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 						+ OUTPUT_FOLDER_CHAMPION + champNameFolder
 						+ OUTPUT_FOLDER_ABILITIES).mkdirs();
 				new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 						+ OUTPUT_FOLDER_CHAMPION + champNameFolder
 						+ OUTPUT_FOLDER_SKINS + OUTPUT_FOLDER_SKINS_LARGE)
 						.mkdirs();
 				new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 						+ OUTPUT_FOLDER_CHAMPION + champNameFolder
 						+ OUTPUT_FOLDER_SKINS + OUTPUT_FOLDER_SKINS_SMALL)
 						.mkdirs();
 			}
 
 			try {
 				CopyChampionImages(fileName, file.getAbsolutePath(),
 						wantedFileName, fileExtensionSplit, champNameFolder,
 						champName);
 			} catch (IOException e) {
 
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void CopyChampionImages(String fileName, String path,
 			String wantedFileName, String[] fileExtensionSplit,
 			String champNameFolder, String champName) throws IOException {
 		messageLabel.setText(PREFIX_COPYING + MSG_COPY_CHAMPION_IMAGES
 				+ champName);
 		if (fileName.toLowerCase().indexOf("Splash".toLowerCase()) >= 0) {
 			File splash = new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 					+ OUTPUT_FOLDER_CHAMPION + champNameFolder
 					+ OUTPUT_FOLDER_SKINS + OUTPUT_FOLDER_SKINS_LARGE
 					+ wantedFileName);
 
 			if (!splash.exists()) {
 
 				FileUtils.copyFile(new File(path), splash);
 			}
 		} else if (fileName.toLowerCase().indexOf("Square".toLowerCase()) >= 0) {
 			File square = new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 					+ OUTPUT_FOLDER_CHAMPION + champNameFolder + "\\"
 					+ FILE_NAME_HEADSHOT + '.'
 					+ fileExtensionSplit[fileExtensionSplit.length - 1]);
 			if (!square.exists()) {
 				FileUtils.copyFile(new File(path), square);
 			}
 		} else if (fileName.toLowerCase().indexOf("Web".toLowerCase()) >= 0) {
 			// Do nothing.
 		} else {
 
 			File file = new File(outputFolderPath + OUTPUT_FOLDER_LOL_IMAGES
 					+ OUTPUT_FOLDER_CHAMPION + champNameFolder
 					+ OUTPUT_FOLDER_SKINS + OUTPUT_FOLDER_SKINS_SMALL
 					+ wantedFileName);
 			if (!file.exists()) {
 				FileUtils.copyFile(new File(path), file);
 			}
 		}
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(3, true);
 		parent.setLayout(layout);
 
 		Label directoryLabel = new Label(parent, SWT.NONE);
 		directoryLabel.setText("LOL directory: ");
 		directoryText = new Text(parent, SWT.FILL | SWT.BORDER);
 		directoryButton = new Button(parent, SWT.NONE);
 		directoryButton.setText("...");
 		directoryButton.addSelectionListener(this);
 
 		Label outputLabel = new Label(parent, SWT.NONE);
 		outputLabel.setText("Output directory: ");
 		outputText = new Text(parent, SWT.FILL | SWT.BORDER);
 		outputButton = new Button(parent, SWT.NONE);
 		outputButton.setText("...");
 		outputButton.addSelectionListener(this);
 
 		new Label(parent, SWT.NONE);
 		new Label(parent, SWT.NONE);
 		startButton = new Button(parent, SWT.NONE);
 		startButton.setText("Start");
 		startButton.addSelectionListener(this);
 
 		messageLabel = new Label(parent, SWT.FILL);
		GridData layoutData = new GridData(0, 0, true, false);
		messageLabel.setLayoutData(layoutData);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	@Override
 	public void setFocus() {
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (e.getSource() == directoryButton) {
 			DirectoryDialog dialog = new DirectoryDialog(getSite().getShell());
 			directoryText.setText(dialog.open());
 			lolDirectory = new File(directoryText.getText());
 		}
 		if (e.getSource() == outputButton) {
 
 			DirectoryDialog dialog = new DirectoryDialog(getSite().getShell());
 			outputText.setText(dialog.open());
 			outputDirectory = new File(outputText.getText());
 		}
 		if (e.getSource() == startButton) {
 			init();
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
