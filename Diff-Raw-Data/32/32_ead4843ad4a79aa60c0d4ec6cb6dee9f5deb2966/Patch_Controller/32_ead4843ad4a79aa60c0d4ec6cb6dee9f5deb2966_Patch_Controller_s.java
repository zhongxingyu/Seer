 package net.soartex.patcher;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 
 import net.soartex.patcher.helpers.AppZip;
 import net.soartex.patcher.helpers.Strings;
 import net.soartex.patcher.helpers.UnZip;
 
 public class Patch_Controller {
 
 	String zipLocation;
 	JLabel title;
 	JProgressBar aJProgressBar;
 	JFrame frame1;
 
 	public Patch_Controller(String path){
 		zipLocation=path;
 	}
 
 	private void makeProgressWiondow(){
 		frame1 = new JFrame("Patching");
 		GridLayout g1 = new GridLayout(2,1);
 		frame1.setLayout(g1);
 
 		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		aJProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
 		aJProgressBar.setStringPainted(true);
 		frame1.add(aJProgressBar);
 
 		title = new JLabel("", JLabel.CENTER);
 		title.setForeground(Color.white);
 		frame1.add(title);
 		try {
 			URL url = getClass().getClassLoader().getResource(Strings.ICON_NAME);
 			frame1.setIconImage(Toolkit.getDefaultToolkit().createImage(url));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		frame1.setSize(300, 80);
 		frame1.setResizable(false);
 		frame1.setLocationRelativeTo(Soartex_Patcher.frame);
 		frame1.setVisible(true);
 	}
 
 	public void run(){
 
 		makeProgressWiondow();
 		Soartex_Patcher.frame.setEnabled(false);
 
 		title.setText("Extracting Main Zip");
 		aJProgressBar.setValue(0);
 		System.out.println("==================");
 		System.out.println("Extracting Main Zip");
 		System.out.println("==================");
 		extractTexturePack();
 
 		title.setText("Downloading Mods");
 		aJProgressBar.setValue(20);
 		System.out.println("==================");
 		System.out.println("Downloading Mods");
 		System.out.println("==================");
 		downloadModTextures();
 
 		title.setText("Compiling New Modlist");
 		aJProgressBar.setValue(40);
 		System.out.println("==================");
 		System.out.println("Compiling New Modlist");
 		System.out.println("==================");
 		makeNewCSVFile();
 
 		title.setText("Extracting Mods");
 		aJProgressBar.setValue(60);
 		System.out.println("==================");
 		System.out.println("Extracting Mods");
 		System.out.println("==================");
 		extractModTextures();
 
 		title.setText("Compiling");
 		aJProgressBar.setValue(80);
 		System.out.println("==================");
 		System.out.println("Compiling....");
 		System.out.println("==================");
 		compressPatchedFiles();
 
 		title.setText("Done!");
 		aJProgressBar.setValue(100);
 		System.out.println("==================");
 		System.out.println("Done!");
 		System.out.println("==================");
 
 		frame1.setVisible(false);
 		Soartex_Patcher.frame.setEnabled(true);
 	}
 
 	private void downloadModTextures() {
 		final byte[] buffer = new byte[1048576];
 
 		new File(Strings.TEMPORARY_DATA_LOCATION_A).mkdirs();
 		new File(Strings.TEMPORARY_DATA_LOCATION_A).deleteOnExit();
 
 		ArrayList<String> temp = new ArrayList<String>();
 		for(int i=0; i<Soartex_Patcher.tableData.length;i++){
 			if(Soartex_Patcher.tableData[i][0] != null && (Boolean)Soartex_Patcher.tableData[i][0]){
 				temp.add((String) Soartex_Patcher.tableData[i][1]);
 			}
 		}
 		for (String mod: temp) {
 			try {
 				String tempUrl = Strings.MODDED_URL+ mod.replace(Strings.SPACE, Strings.UNDERSCORE) + Strings.ZIP_FILES_EXT.substring(1);
 				final InputStream in = new URL(tempUrl).openStream();
 
 				System.out.println("Downloading: "+mod);
 
 				final File destinationFile = new File(Strings.TEMPORARY_DATA_LOCATION_A + File.separator + new File(tempUrl).getName()).getAbsoluteFile();
 				destinationFile.getParentFile().mkdirs();
 				destinationFile.deleteOnExit();
 				destinationFile.getParentFile().deleteOnExit();
 
 				final FileOutputStream out = new FileOutputStream(destinationFile);
 
 				int len;
 				while ((len = in.read(buffer)) > -1) {
 					out.write(buffer, 0, len);
 				}
 				out.close();
 
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void extractTexturePack() {
 		UnZip.unZipIt(zipLocation, Strings.TEMPORARY_DATA_LOCATION_B);		
 	}
 
 	// TODO: add this method to keep track of ods added
 	private void makeNewCSVFile() {
 		try{
 			//old modTable
 			ArrayList<String[]> data = new ArrayList<String[]>();
 			try {
 				@SuppressWarnings("resource")
 				BufferedReader in = new BufferedReader(new FileReader(Strings.TEMPORARY_DATA_LOCATION_B+ File.separator +Strings.MODTABLE_EXPORT));
 				String readline = in.readLine();
 				while (readline != null) {
 
 					//add file info
 					String[] text = new String[2];
 					text[0] = readline.split(Strings.COMMA)[0];
 					text[1] = readline.split(Strings.COMMA)[1];
 					data.add(text);
 
 					readline = in.readLine();
 				}
 			}
 			catch(Exception e){
 				System.out.println("No Old Mod Table");
 			}
 			//selected mods
 			ArrayList<String[]> data2 = new ArrayList<String[]>();
 			for(int i=0; i<Soartex_Patcher.tableData.length;i++){
 				//if selected on the table add the name and datemodified
 				if(Soartex_Patcher.tableData[i][0] != null && (Boolean)Soartex_Patcher.tableData[i][0]){
 					String[] text = new String[2];
 					text[0] = (String) Soartex_Patcher.tableData[i][1];
 					text[1] = (String) Soartex_Patcher.tableData[i][5];
 					data2.add(text);
 				}
 			}
 
 			//combine the 2 sets of data
 			ArrayList<String[]> finalData = new ArrayList<String[]>();
 			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyy");  
 			for(int i=0;i<data2.size(); i++){
 				String[] temp = data2.get(i);
 				
 				for(int z=0;z<data.size(); z++){
 					if(data2.get(i)[0].equals(data.get(z)[0])){
 						Date oldFile = formatter.parse(data.get(z)[1]);
 						Date selectedMod = formatter.parse(data2.get(i)[1]);
 						if(oldFile.before(selectedMod)){
 							data.remove(z);
 							break;
 						}
 					}
 				}//innner
 				System.out.println("Added "+temp[0]+" | "+temp[1]);
 				finalData.add(temp);
 			}//outer
 			
 			//add the old mods that are not over written
 			for(int z=0;z<data.size(); z++){
 				System.out.println("Added "+data.get(z)[0]+" | "+data.get(z)[1]);
 				finalData.add(data.get(z));
 			}
 			
 			//write the file out
 			try{
 				new File(Strings.TEMPORARY_DATA_LOCATION_B+ File.separator +Strings.MODTABLE_EXPORT).getParentFile().mkdirs();
 				File export = new File(Strings.TEMPORARY_DATA_LOCATION_B+ File.separator +Strings.MODTABLE_EXPORT);
 				System.out.println(export.getAbsolutePath());
 				FileWriter fw = new FileWriter(export);
 				PrintWriter pw = new PrintWriter(fw);
 				for (String[] temp : finalData) {
 					pw.print(temp[0]);
 					pw.print(",");
 					pw.print(temp[1]);
 					pw.print(",");
 					pw.print("\n");
 				}
 				pw.flush();
 				pw.close();
 				fw.close();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	private void extractModTextures() {
 		new File(Strings.TEMPORARY_DATA_LOCATION_B).deleteOnExit();
 		final ArrayList<File> files = new ArrayList<File>();
 		getFiles(new File(Strings.TEMPORARY_DATA_LOCATION_A), files);
 
 		for (final File file : files) {
 			//debug
 			System.out.println("Extracting: "+file.getName());
 			UnZip.unZipIt(file.getAbsolutePath(), Strings.TEMPORARY_DATA_LOCATION_B);
 		}
 		delete(new File(Strings.TEMPORARY_DATA_LOCATION_A));		
 	}
 
 	private void compressPatchedFiles() {
 		AppZip.makeList(new File(Strings.TEMPORARY_DATA_LOCATION_B));
 		AppZip.zipIt(zipLocation);
 		delete(new File(Strings.TEMPORARY_DATA_LOCATION_B));		
 	}
 
 	// TODO: Helper Methods
 	private static void getFiles (final File f, final ArrayList<File> files) {
 
 		if (f.isFile()) return;
 
 		final File[] afiles = f.getAbsoluteFile().listFiles();
 
 		if (afiles == null) return;
 
 		for (final File file : afiles) {
 
 			if (file.isDirectory()) getFiles(file, files);
 
 			else files.add(file.getAbsoluteFile());
 
 		}
 
 	}
 
 	public static void delete (final File f) {
 
 		f.delete();
 
 		if (f.isFile()) return;
 
 		final File[] files = f.getAbsoluteFile().listFiles();
 
 		if (files == null) return;
 
 		for (final File file : files) {
 
 			delete(file);
 
 			f.delete();
 
 		}
 
 	}
 }
