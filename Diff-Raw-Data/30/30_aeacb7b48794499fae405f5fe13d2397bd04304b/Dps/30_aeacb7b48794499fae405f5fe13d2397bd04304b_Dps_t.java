 package efrei.refresh.dps;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class Dps {
 
	//private static final long msBetweenChecks = 10*60*1000;
	private static final long msBetweenChecks = 30*1000;
 	
 	private static Lock working = new ReentrantLock();
 	private static SortedDocsList docs = new SortedDocsList();
 	private static Ui ui = new Ui();
 
 	public static void main(String[] args) {
 		Timer t = new Timer();
 		QueueProcessor qp = new QueueProcessor();
 		
 		if (loadConfig(qp))
 		{
 			Ui.askPassword();
 			ui.run(t);
 			reload();
 			ui.update();
 			qp.run();
 			ui.update();
 			t.schedule(qp, msBetweenChecks - (System.currentTimeMillis() % msBetweenChecks), msBetweenChecks);
 		}
 	}
 	
 	public static Lock Working() {
 		return working;
 	}
 
 	public static SortedDocsList getDocs() {
 		return docs;
 	}
 	
 	public static Ui getUi() {
 		return ui;
 	}
 	
 	public static boolean loadConfig(QueueProcessor qp)
 	{
 		try {
 			BufferedReader config = new BufferedReader(new FileReader("res/config.txt"));
 			String temp;
 			
 			temp = config.readLine();
 			temp = temp.substring(temp.indexOf('=') + 2);
 			qp.setColorPrinter(temp);
 			temp = config.readLine();
 			temp = temp.substring(temp.indexOf('=') + 2);
 			qp.setBwPrinter(temp);
 			temp = config.readLine();
 			temp = temp.substring(temp.indexOf('=') + 2);
 			qp.setPdfPrinter(temp);
 			temp = config.readLine();
 			temp = temp.substring(temp.indexOf('=') + 2);
 			qp.setDocxPrinter(temp);
 			
 			config.close();
 			return true;
 		} catch (NumberFormatException | IOException e) {
 			System.out.println(e.getMessage());
 			return false;
 		}
 	}
 	
 	private static void reload() {
 		Ftp ftp = new Ftp();
 		if (ftp.connect()) {
 			String[] filesNames = ftp.getFilesNames(true);
 			for (String file : filesNames) {
 				Dps.getDocs().add(new PrintedDoc(file));
 			}
 			ftp.disconnect();
 		}
 	}
 }
 
 class QueueProcessor extends TimerTask {
 	
 	private String ColorPrinter;
 	private String BwPrinter;
 	private String PdfPrinter;
 	private String DocxPrinter;
 	
 	public void setColorPrinter(String colorPrinter) {
 		ColorPrinter = colorPrinter;
 	}
 
 	public void setBwPrinter(String bwPrinter) {
 		BwPrinter = bwPrinter;
 	}
 
 	public void setPdfPrinter(String pdfPrinter) {
 		PdfPrinter = pdfPrinter;
 	}
 
 	public void setDocxPrinter(String docxPrinter) {
 		DocxPrinter = docxPrinter;
 	}
 
 	@Override
 	public void run () {
 		Dps.Working().lock();
 		
 		Ftp ftp = new Ftp();
 		if (ftp.connect()) {
			String[] filesNames = ftp.getFilesNames(false);
 			for (String file : filesNames) {
				if (ftp.getFile(file, false)) {
 					boolean printed = false;
 					PrintedDoc pdoc = new PrintedDoc(file);
 					String printLine = "";
 					if (pdoc.getExtension().equals(".pdf")) printLine = PdfPrinter + file + ' ' + (pdoc.isColored() ? ColorPrinter : BwPrinter);
					else if (pdoc.getExtension().equals(".docx")) printLine = DocxPrinter + (pdoc.isColored() ? "/q /mDPS_colorPrint " : "/q /mDPS_bwPrint ") + file;

 					try {
 						Process proc = Runtime.getRuntime().exec(printLine);
 						proc.waitFor();
 						printed = true;
 					} catch (IOException | InterruptedException e) {
 						System.out.println(e.getMessage());
 					}
 					
 					if (printed) {
 						ftp.moveToBackup(file);
 						Dps.getDocs().add(pdoc);
 						Dps.getUi().update();
 						File f = new File(file);
 						f.delete();
 					}
 				}
 			}
 			ftp.disconnect();
 		}
 		
 		Dps.Working().unlock();
 	}
 }
 
