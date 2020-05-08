 package resources;
 
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.nio.file.attribute.FileTime;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Vector;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 //import java.util.Random;
 //import java.util.Vector;
 import javax.swing.SwingWorker;
 import java.util.concurrent.ExecutionException;
 import java.util.zip.Deflater;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 //import javax.swing.JButton;
 //import javax.swing.ImageIcon;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 
 
 
 /**
  *
  * @author Alexander Artyomov
  */
 public class MainProgram extends SwingWorker<String[], String> {    	
     private int _numOfMonth;
     private boolean _word,_exel,_pdf,_pow;
     private File _source;
     private ArrayList<File> _listOfFiles;
     private JList<String> list;
     private final JProgressBar progress;
     private final HandlerView handlerFrame;
     private File archiveDirectory;
     private static final String DATE_FORMAT_NOW = "yyyy_MM_dd_HH_mm_ss";
             
     public MainProgram(int num, File source, HandlerView hr)
     {
         _listOfFiles = new ArrayList<File>();
         _numOfMonth = num;	
         _source = source;
         archiveDirectory = createArchive();
         /*TODO if added only for running UnitTest - should be without if*/
         if(hr != null){       
         	list = hr.getList();
         	progress = hr.getBar();       
         	handlerFrame = hr;
         }
         else
         {
         	list = null;
         	progress = null;
         	handlerFrame = null;
         }
         
         
     }
 
     public int get_numOfMonth() {
         return _numOfMonth;
     }
     
     protected ArrayList<File> getListOfFiles()
     {
         if(!_listOfFiles.isEmpty())    
             return _listOfFiles;
         return null;
     }
 
     public void set_numOfMonth(int numOfMonth) {
         _numOfMonth = numOfMonth;
     }
 
     public boolean is_word() {
         return _word;
     }
 
     public void set_word(boolean word) {
         _word = word;
     }
 
     public boolean is_exel() {
         return _exel;	
     }
 
     public void set_exel(boolean exel) {
         _exel = exel;
     }
 
     public boolean is_pdf() {
         return _pdf;	
     }
 
     public void set_pdf(boolean pdf) {
         _pdf = pdf;	
     }
 
     public boolean is_pow() {
         return _pow;	
     }
 
     public void set_pow(boolean pow) {
         _pow = pow;	
     }
 
     public File get_source() {
         return _source;	
     }
 
     public void set_source(File source) {
         _source = source;	
     }
    
     @SuppressWarnings({ "unchecked", "serial", "rawtypes" })
 	protected String[] doInBackground()
     {    
         handlerFrame.disableButtons();
         list.setModel(new javax.swing.AbstractListModel() {  
             String [] strings = {"Searching..."};
             public int getSize() {return 1; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         if(isCancelled())
         {
             list.setModel(new javax.swing.AbstractListModel() {             
                 String [] strings = {"Search operation was canceled"};            
                 public int getSize() {return 1; }            
                 public Object getElementAt(int i) { return strings[i]; }
         });
             return fileList();
         }
         else
         {
             list.setEnabled(false);      
             searchDirectory(_source);        
             return fileList();
         }
         
     }
     
     @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
 	@Override
     protected void done()
     {  
         progress.setIndeterminate(false);
         if(!isCancelled())
         {
 	        try               
 	        {    
 	            if(!_listOfFiles.isEmpty())
 	            {
 	                handlerFrame.enableButtons();                        
 	                list.setModel(new javax.swing.AbstractListModel() {            
 	                    String [] strings = get();                                
 	                    public int getSize() { return strings.length; }                                
 	                    public Object getElementAt(int i) { return strings[i]; }                                
 	                }); 
 	                list.setEnabled(true);
 	            }
 	            else
 	            {
 	                list.setModel(new javax.swing.AbstractListModel() {                  
 	                   String [] strings = {"No Files Were Found"};                
 	                   public int getSize() {return 1; }            
 	                   public Object getElementAt(int i) { return strings[i]; }});               
 	                JOptionPane.showMessageDialog(handlerFrame,                                                         
 	                        "No Files Were Found", "DrCleaner",                                                                           
 	                        JOptionPane.INFORMATION_MESSAGE); 
 	                handlerFrame.dispose();
 	            }
 	        }                
 	        catch(InterruptedException ex)                
 	        {        
 	            JOptionPane.showMessageDialog(null,             
 	                    "Interupted while waiting for results", "Error",                                        
 	                    JOptionPane.ERROR_MESSAGE);                
 	        }                       
 	        catch(ExecutionException ex)                
 	        {        
 	            JOptionPane.showMessageDialog(null,             
 	                    "Error encountered while performing search", "Error",                                        
 	                    JOptionPane.ERROR_MESSAGE);                    
 	        } 
         }
         else
         	handlerFrame.dispose();
     }
     
     /* TODO Should be private changed to public in order to perform a Unit test */
     public void searchDirectory(File source)
     {
         File[] listOfFiles = source.listFiles();
          
          if(listOfFiles != null){
              for (int i = 0; i < listOfFiles.length; i++)  
              {
                  if (listOfFiles[i].isFile())   
                  {  
                      if(isFileSearched(listOfFiles[i]))
                          _listOfFiles.add(listOfFiles[i]); 
                  }
                  else
                  {
                      if(listOfFiles[i].isDirectory() && !listOfFiles[i].isHidden())
                          searchDirectory(listOfFiles[i]);
                  }
              }
          }
     }
     
     //Returns true if current file is in right type and 
     //wasn't used more than _numOfMonth
     private boolean isFileSearched(File file)
     {
         Path p = file.toPath();   
         long cutoff = System.currentTimeMillis() - ((long)_numOfMonth * 30 * 24 * 60
                 * 60 * 1000);  
         try
         {    
             FileTime time = (FileTime)Files.getAttribute(p,            
                     "lastAccessTime" , LinkOption.NOFOLLOW_LINKS);                    
             long lastAccessed = time.toMillis();
             if(lastAccessed<=cutoff && file.isHidden() == false)
             {       
                 if(_word)      
                     if( file.getName().endsWith(".doc")||file.getName().endsWith(".docx")     
                             ||file.getName().endsWith(".DOC")||file.getName().endsWith(".DOCX"))
                         return true;
                 if(_exel)      
                     if( file.getName().endsWith(".xls")||file.getName().endsWith(".xlsx")     
                             ||file.getName().endsWith(".XLS")||file.getName().endsWith(".XLSX"))
                         return true;
                 if(_pow)      
                     if( file.getName().endsWith(".ppt")||file.getName().endsWith(".pptx")     
                             ||file.getName().endsWith(".PPT")||file.getName().endsWith(".PPTX"))
                         return true;
                 if(_pdf)      
                     if( file.getName().endsWith(".pdf")||file.getName().endsWith(".PDF"))
                         return true;
             }
         }
         catch (IOException e)
         {
             return false;
         }
         return false;
     }
     
     public String [] fileList()
     {
         String [] str = new String[_listOfFiles.size()];
         for(int i = 0;i<_listOfFiles.size();i++)
             str[i] = _listOfFiles.get(i).getAbsolutePath();
         return str;
     }
     
 	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
 	public JList<String> deleteFiles(JList<String> l)
     {
 		 list = l;	        
 		 if(list.getMaxSelectionIndex() == -1)	     
 			 JOptionPane.showMessageDialog(null, "Please select files to be deleted! ", "DrCleaner" ,JOptionPane.INFORMATION_MESSAGE); 	        
 		 else	     
 		 {	     
 			 int j = JOptionPane.showConfirmDialog(null, 	         
 					 "Are you sure you want to delete selected fles? ", 	                 
 					 "DrCleaner", 	                 
 					 JOptionPane.YES_NO_OPTION);	            
 			 if( j == JOptionPane.YES_OPTION)    	         
 			 {	         
 				 long space = 0;	             
 				 int numOfFilesThatCouldntDelete = 0;	             
 				 int numOfselected = 0;	             
 				 final Vector<String> temp = new Vector<String>();	             
 				 for(int i = 0; i < list.getModel().getSize(); i++)	             
 				 { 	             
 					 if(list.isSelectedIndex(i))	                 
 					 {	                 
 						 numOfselected++;                   	                     
 						 File f = new File((String)list.getModel().getElementAt(i));                	                     
 						 space += f.length();                           	                     
 						 if(!f.delete())                	                     
 						 {                                                   	                     
 							 space -= f.length();                                    	                         
 							 numOfFilesThatCouldntDelete++;	                     
 						 }						 	                    
 					 }	                 
 					 else	                 
 						 temp.add((String)list.getModel().getElementAt(i));	             
 				 }				 	                	             
 				 list.setModel(new javax.swing.AbstractListModel() {	             
 					 public int getSize() {	                 
 						 return temp.size();                        	                   
 					 }	                 
 					 public Object getElementAt(int i) {	                 
 						 return temp.get(i);	                    
 					 }	                
 				 });	             
 				 list.repaint();	             
 				 String str = "From " + numOfselected + " selected files, " 	             
 						 + (numOfselected - numOfFilesThatCouldntDelete) +	                     
 						 " were deleted\n Total saved space is: " + (space/1024)+"KB";	                
 				 JOptionPane.showMessageDialog(null, str, "DrCleaner" ,JOptionPane.INFORMATION_MESSAGE);	             
 				 if(temp.isEmpty())	             
 				 {	             
 					 JOptionPane.showMessageDialog(handlerFrame,                                                         	                 
 							 "You have no more files to handle", "DrCleaner",                                                                           	                        
 							 JOptionPane.INFORMATION_MESSAGE);	                   
 					 handlerFrame.dispose();	                
 				 }	            
 			 }	        
 		 }	       	    
 		 return list;
     }
 	
 	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
 	public JList<String> archiveFiles(JList<String> l)
     {
     	list = l;
         if(list.getMaxSelectionIndex() == -1)
             JOptionPane.showMessageDialog(null, "Please select files to be archived! ", "DrCleaner" ,JOptionPane.INFORMATION_MESSAGE); 
         else
         {
             int j = JOptionPane.showConfirmDialog(null, 
                     "Are you sure you want to archive selected fles? ", 
                     "DrCleaner", 
                     JOptionPane.YES_NO_OPTION);
             if( j == JOptionPane.YES_OPTION)    
             {       		            		
             	String archiveName = now();                
             	File newArchive = new File(archiveDirectory.getAbsolutePath() + File.separatorChar + archiveName);                
             	newArchive.mkdir();                
             	File zipFile = new File(newArchive.getAbsolutePath()+ ".zip");                                      	
 				long space = 0;                           	
 				int numOfFilesThatCouldntArchive = 0;                
 				int numOfselected = 0;                
            	final Vector<String> temp = new Vector<String>(); 
            	Vector<File> archiveListOfFileNames = new Vector<File>();
             	for(int i = 0; i < list.getModel().getSize(); i++)                
             	{                
             		if(list.isSelectedIndex(i))                    
             		{                   
             			numOfselected++;                                           
            			File from = new File((String)list.getModel().getElementAt(i));      			
            			int extensionStartsAt = from.getName().lastIndexOf('.');
            			String newName = from.getName().substring(0, extensionStartsAt);
            			String extension = from.getName().substring(extensionStartsAt, from.getName().length());
            			int counterOfFilesWithSameName = 2;
            			String newName2 = new String(newName);
            			File to = new File(newArchive.getAbsolutePath() + File.separatorChar + from.getName());
            			for(int k = 0; k < archiveListOfFileNames.size(); k++)
            			{
            				if(archiveListOfFileNames.get(k).getName().equals(to.getName()))
            				{
            					newName2 = newName+"(" + counterOfFilesWithSameName + ")";            				
                				to = new File(newArchive.getAbsolutePath() + File.separatorChar + newName2 + extension);
                				counterOfFilesWithSameName++;   
                				k=-1;
            				}
            			}
             			try                        
             			{                       
             				copyFile(from, to);
            				archiveListOfFileNames.add(to);
             				space += from.length();                            
             				if(to.exists())
                             	from.delete();                    	              
                             } 
                             catch (IOException ex)
                             {
                             	ex.printStackTrace();
                             }                       
                         }
                         else
                             temp.add((String)list.getModel().getElementAt(i));
                     }
                     try
                     {           
                     	//making archive file from all selected files
                     	zipDirectory(newArchive, zipFile);
                     	//checking that zip file is not corrupted
                     	if(isValid(zipFile))
                     	{
                     		for(File f: newArchive.listFiles())
                     			f.delete();
                         	newArchive.delete();
                     	}                    	
                     }                    
                     catch (IOException ex)
                     {
                 		JOptionPane.showMessageDialog(null, "Problem occured while making archive ", "DrCleaner" ,JOptionPane.ERROR_MESSAGE);
                     	ex.printStackTrace();
                     }
                     catch(SecurityException ex)
                     {
                 		JOptionPane.showMessageDialog(null, "Problem occured while deleting old files that were supposed to move to archive ",
                 				"DrCleaner" ,JOptionPane.ERROR_MESSAGE);
                     	ex.printStackTrace();
                     }                   
                     
                     list.setModel(new javax.swing.AbstractListModel() {
                         public int getSize() {
                             return temp.size();                        
                         }
                         public Object getElementAt(int i) {
                             return temp.get(i);
                         }
                     });
                     list.repaint();
                     long savedSpace = space - zipFile.length();
                     String str = "From " + numOfselected + " selected files, " 
                             + (numOfselected - numOfFilesThatCouldntArchive) +
                             " were archived\n Total saved space is: " + (savedSpace/1024)+"KB\n" + "Your archive file is at: " + newArchive.getAbsolutePath();
                     JOptionPane.showMessageDialog(null, str, "DrCleaner" ,JOptionPane.INFORMATION_MESSAGE);
                     if(temp.isEmpty())
                     {
                         JOptionPane.showMessageDialog(handlerFrame,                                                         
                             "You have no more files to handle", "DrCleaner",                                                                           
                             JOptionPane.INFORMATION_MESSAGE);
                         handlerFrame.dispose();
                   
                     }                                
             	}                    	         	
             }   
         return list;       
     }
                     		
     
 	
 	public static String now() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         return sdf.format(cal.getTime()); 
     }
     
     private void copyFile(File from, File to) throws IOException
     {
     	FileChannel in = (new FileInputStream(from)).getChannel();
     	FileChannel out = (new FileOutputStream(to)).getChannel();
     	in.transferTo(0, from.length(), out);
     	in.close();
     	out.close();
     }
     
     public static void zipDirectory(File dir, File zipfile)    	   
     		throws IOException {    	       	
     	String[] entries = dir.list();    	
     	byte[] buffer = new byte[4096]; // Create a buffer for copying    	
     	int bytesRead;    	
     	ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));    	
     	out.setLevel(Deflater.BEST_COMPRESSION);    	
     	for (int i = 0; i < entries.length; i++) {    	
     		File f = new File(dir, entries[i]);    	    
     		FileInputStream in = new FileInputStream(f); // Stream to read file    	    
     		ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry    	    
     		out.putNextEntry(entry); // Store entry    	    
     		while ((bytesRead = in.read(buffer)) != -1)    	    
     			out.write(buffer, 0, bytesRead);    	      
     		in.close(); 
     	}    	    
     	out.close();  
     }
     
     //checks that archive is not corrupted
     private boolean isValid(File file) {     
     	ZipFile zipfile = null;     
     	try {        
     		zipfile = new ZipFile(file);         
     		return true;     
     		} 
     	catch (ZipException e) {
     		JOptionPane.showMessageDialog(null, "The Archive is corupted ", "DrCleaner" ,JOptionPane.ERROR_MESSAGE);
     		return false;     
     		} 
     	catch (IOException e) 
     	{       
     		JOptionPane.showMessageDialog(null, "Problem occured while accessing files for zipping ", "DrCleaner" ,JOptionPane.ERROR_MESSAGE);
     		return false;     	
     	} 
     	finally 
     	{         
     		try 
     		{             
     			if (zipfile != null) 
     			{                 
     				zipfile.close();                
     				zipfile = null;     				
     			} 
     		} 
     		catch (IOException e)
     		{    
         		JOptionPane.showMessageDialog(null, "Problem occured while closing zipped file ", "DrCleaner" ,JOptionPane.ERROR_MESSAGE);
 
     		}     
     	}
     } 
 
 	private File createArchive()
     {
     	archiveDirectory = null;
     	try
     	{  					
     		String homePath = System.getProperty("user.home");
 			archiveDirectory = new File(homePath+File.separatorChar +"Archive");
 	    	if(!archiveDirectory.exists())
 	    		archiveDirectory.mkdir();
 	    	
     	}
     	catch(SecurityException ex)
     	{
     		ex.printStackTrace();
     	}
     	catch(NullPointerException ex)
     	{
     		ex.printStackTrace();
     	}
     	catch(IllegalArgumentException ex)
     	{
     		ex.printStackTrace();
     	}
     	return archiveDirectory;    	
     }
 	public File getArchiveDirectory()
 	{
 		return archiveDirectory;
 	}
 }
