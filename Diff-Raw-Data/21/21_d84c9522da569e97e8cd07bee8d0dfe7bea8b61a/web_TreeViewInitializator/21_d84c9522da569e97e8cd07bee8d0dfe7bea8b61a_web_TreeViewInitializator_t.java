 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 public class web_TreeViewInitializator {
 
	private static final String VERSION = "0.0.2.10";
 	private static final String AUTHORS = "EifX";
 	private static String GLOBAL_SETTINGS_FILE;
 	private static ArrayList<String> javascript = new ArrayList<String>();
 	private static ArrayList<File> files = new ArrayList<File>();
 	private static String folderSeparator;
 
 	/**
 	 * This method navigates through a XML-File by given path <tt>settings</tt><br>
 	 * Only inline attributes are excepted (Ex.: &lt;node val="test" /&gt;)
 	 * @param settings
 	 * @return The Value from an XML Node
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	public static String read_setting(String[] settings) throws ParserConfigurationException, SAXException, IOException{
 	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder builder = factory.newDocumentBuilder();
 	    Document document = builder.parse( new File(GLOBAL_SETTINGS_FILE) );
 	    NodeList list = document.getChildNodes();
 
 	    //Go to a deeper path
 	    for(int i=0;i<settings.length;i++){
 	    	if(settings.length-2>i){
 		    	for(int j=0;j<list.getLength();j++){
 		    		if(list.item(j).getNodeName()==settings[i]){
 		    			list = list.item(j).getChildNodes();
 		    			break;
 		    		}
 		    	}
 	    	}else{
 	    		//Node found, try to get his specific value
 	    		for(int j=0;j<list.getLength();j++){
 	    			if(list.item(j).getNodeName()==settings[i]){
 	    				NamedNodeMap attr = list.item(j).getAttributes();
 	    				for(int k=0;k<attr.getLength();k++){
 	    					if(attr.item(k).getNodeName()==settings[i+1]){
 	    						return attr.item(k).getNodeValue();
 	    					}
 	    				}
 	    			}
 	    		}
 	    	}
 	    }
 
 		return "";
 	}
 
 	/**
 	 * Get all files from an given path. If failureTest is set, this method said, if an path have an error file
 	 * @param path
 	 * @param failureTest
 	 * @return
 	 */
 	public static boolean getAllFiles(String path, boolean failureTest){
 		File file = new File(path);
 		File[] fileList = file.listFiles();
 		try{
 			for(int i=0;i<fileList.length;i++){
 				//If it is a Dir, go deeper
 				if(fileList[i].isDirectory()){
 					if(!getAllFiles(fileList[i].getAbsolutePath(),failureTest)){
 						return false;
 					}
 				}else{
 					//If it is a File, write it down!
 					if(fileList[i].toString().substring(fileList[i].toString().length()-4, fileList[i].toString().length()).equals(".dbg")){
 						if(fileList[i].canRead()){
 							if(failureTest){
 								try {
 									if(!(isNotAFailFile(fileList[i].getAbsolutePath()))){
 										return false;
 									}
 								} catch (IOException e) {
 									System.out.println("ERROR! DBG-File read error : "+files.get(i).getPath());
 									System.exit(-1);
 								}
 							}else{
 								files.add(fileList[i]);
 							}
 						}
 					}
 				}
 			}
 		}catch (Exception e){
 			System.out.println("ERROR! Wrong path or not enough rights to view it!");
 			System.exit(-1);
 		}
 		return true;
 	}
 
 	/**
 	 * Write the output filefalse
 	 * @param path
 	 * @param prettyOutput
 	 */
 	public static void writeTxt(String path){
 		try{
 			File f = new File(path);
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path,"rw");
 			for(int i=0;i<javascript.size();i++){
 				file.writeBytes(javascript.get(i)+"\r\n");
 			}
 			file.close();
 		}catch (IOException e){
 			System.out.println("ERROR! Javascript write error in path : "+path);
 		}
 	}
 
 	/**
 	 * This method cleans a string. Only letters and numbers are excepted in a string, all other
 	 * values are replaced by an underscore.
 	 * @param str
 	 * @return a clean String
 	 */
 	public static String cleanStr(String str){
 		String newStr = "";
 		for(int i=0;i<str.length();i++){
 			if(((str.charAt(i)>=65)&&(str.charAt(i)<=90))||((str.charAt(i)>=97)&&(str.charAt(i)<=122))||((str.charAt(i)>=48)&&(str.charAt(i)<=57))){
 				newStr += str.charAt(i);
 			}else{
 				newStr += "_";
 			}
 		}
 		return newStr;
 	}
 
 	/**
 	 * Test a given file if it is a failure file
 	 * @param path
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean isNotAFailFile(String path) throws IOException{
 		RandomAccessFile file = new RandomAccessFile(path,"r");
 		if(file.length()==0){
 			file.close();
 			return false;
 		}
 
 		String str = file.readLine();
 		boolean trueSucc = false;
 		while(str!=null){
 			if(str.contains("True\tsucceeded")){
 				trueSucc = true;
 			}
 			if(str.contains("No type errors found.")&&trueSucc){
 				file.close();
 				return true;
 			}
 			str = file.readLine();
 		}
 
 
 		file.close();
 		return false;
 	}
 
 	/**
 	 * This Method create the javascript-path
 	 * @param path
 	 */
	public static void js_tree(String path, String projectName, String defaultURI){
 		String[] oldArr = {""};
 		int notEqual = -1;
 		Double dsize = new Double(files.size());
 		int percent = 0;
 		System.out.println("|0%      |50%      |100%");
 		Double calc = 0.0;
 		for(int i=0;i<files.size();i++){
 			//Get the file
 			notEqual = -1;
 			String newPath = files.get(i).getPath().substring(path.length()+1);
 
 			String[] pathArr;
 			if(folderSeparator=="\\"){
 				pathArr = newPath.split(folderSeparator+folderSeparator);
 			}else{
 				pathArr = newPath.split(folderSeparator);
 			}
 
 			//Search for differences between the new and the last file
 			for(int j=0;j<pathArr.length;j++){
 				if(oldArr.length>j){
 					if(!pathArr[j].equals(oldArr[j])){
 						notEqual = j;
 						break;
 					}
 				}else{
 					notEqual = j;
 					break;
 				}
 			}
 
 			//If a file is exist, do nothing
 			if(notEqual==-1){
 				break;
 			}
 
 			//If the path has changed, draw the missing parts
 			if(pathArr.length-1!=notEqual){
 				for(int j=notEqual;j<pathArr.length-1;j++){
 					if(j==0){
 						javascript.add(cleanStr(pathArr[0])+" = insFld(foldersTree, gFld(\""+pathArr[0]+"\", \"\"))");
 					}else{
 						javascript.add(cleanStr(pathArr[j])+" = insFld("+cleanStr(pathArr[j-1])+", gFld(\""+pathArr[j]+"\", \"\"))");
 					}
 					//Draw foldericons
 					String p = "";
 					for(int k=0;k<=j;k++){
 						p+=folderSeparator+pathArr[k];
 					}
 					p = path+p;
 					if(getAllFiles(p,true)){
 						javascript.add(cleanStr(pathArr[j])+".iconSrc = ICONPATH + \"folderopenok.gif\"");
 						javascript.add(cleanStr(pathArr[j])+".iconSrcClosed = ICONPATH + \"folderclosedok.gif\"");
 					}else{
 						javascript.add(cleanStr(pathArr[j])+".iconSrc = ICONPATH + \"folderopenfail.gif\"");
 						javascript.add(cleanStr(pathArr[j])+".iconSrcClosed = ICONPATH + \"folderclosedfail.gif\"");
 					}
 				}
 			}
 
 			newPath = newPath.replace("\\", "/");
 			newPath = newPath.replace(" ", "_");
 
 			//Print the files!
 			if(pathArr.length==1){
				javascript.add("doc"+i+" = insDoc(foldersTree, gLnk(\"S\", \""+pathArr[pathArr.length-1]+"\", \""+defaultURI+"/files/"+projectName+"/"+newPath+"\"))");
 				//javascript.add("doc"+i+" = insDoc(foldersTree, gLnk(\"S\", \""+pathArr[pathArr.length-1]+"\", \"noLinkInserted\"))");
 			}else{
				javascript.add("doc"+i+" = insDoc("+cleanStr(pathArr[pathArr.length-2])+", gLnk(\"S\", \""+pathArr[pathArr.length-1]+"\", \""+defaultURI+"/files/"+projectName+"/"+newPath+"\"))");
 				//javascript.add("doc"+i+" = insDoc("+cleanStr(pathArr[pathArr.length-2])+", gLnk(\"S\", \""+pathArr[pathArr.length-1]+"\", \"noLinkInserted\"))");
 			}
 
 			//Draw fileicons
 			try {
 				if(isNotAFailFile(files.get(i).getPath())){
 					javascript.add("doc"+i+".iconSrc = ICONPATH + \"fileok.gif\"");
 				}else{
 					javascript.add("doc"+i+".iconSrc = ICONPATH + \"filefail.gif\"");
 				}
 			} catch (IOException e) {
 				System.out.println("ERROR! DBG-File read error : "+files.get(i).getPath());
 				System.exit(-1);
 			}
 
 			oldArr = pathArr;
 
 			//Graphical representation
 			Double di = new Double(i);
 
 			calc = (di.doubleValue() / dsize.doubleValue())*100;
 
 			if(calc>=0&&percent==0){System.out.print("|");percent=percent+5;}
 			if(calc>=5&&percent==5){System.out.print("|");percent=percent+5;}
 			if(calc>=10&&percent==10){System.out.print("|");percent=percent+5;}
 			if(calc>=15&&percent==15){System.out.print("|");percent=percent+5;}
 			if(calc>=20&&percent==20){System.out.print("|");percent=percent+5;}
 			if(calc>=25&&percent==25){System.out.print("|");percent=percent+5;}
 			if(calc>=30&&percent==30){System.out.print("|");percent=percent+5;}
 			if(calc>=35&&percent==35){System.out.print("|");percent=percent+5;}
 			if(calc>=40&&percent==40){System.out.print("|");percent=percent+5;}
 			if(calc>=45&&percent==45){System.out.print("|");percent=percent+5;}
 			if(calc>=50&&percent==50){System.out.print("|");percent=percent+5;}
 			if(calc>=55&&percent==55){System.out.print("|");percent=percent+5;}
 			if(calc>=60&&percent==60){System.out.print("|");percent=percent+5;}
 			if(calc>=65&&percent==65){System.out.print("|");percent=percent+5;}
 			if(calc>=70&&percent==70){System.out.print("|");percent=percent+5;}
 			if(calc>=75&&percent==75){System.out.print("|");percent=percent+5;}
 			if(calc>=80&&percent==80){System.out.print("|");percent=percent+5;}
 			if(calc>=85&&percent==85){System.out.print("|");percent=percent+5;}
 			if(calc>=90&&percent==90){System.out.print("|");percent=percent+5;}
 			if(calc>=95&&percent==95){System.out.print("|");percent=percent+5;}
 			if(calc>=100&&percent==100){System.out.print("|");percent=percent+5;}
 		}
 	}
 
 	/**
 	 * This Method draws the javascript-header
 	 * @param projectName
 	 * @param iconPath
 	 */
 	public static void js_header(String projectName, String iconPath, String projectPath){
 		javascript.add("USETEXTLINKS = 1");
 		javascript.add("STARTALLOPEN = 0");
 		javascript.add("ICONPATH = '"+iconPath+"'");
 		javascript.add("foldersTree = gFld(\"<i>"+projectName+"</i>\", \"\")");
 		javascript.add("foldersTree.treeID = \"Frameset\"");
 
 		if(getAllFiles(projectPath,true)){
 			javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenok.gif\"");
 			javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedok.gif\"");
 		}else{
 			javascript.add("foldersTree.iconSrc = ICONPATH + \"folderopenfail.gif\"");
 			javascript.add("foldersTree.iconSrcClosed = ICONPATH + \"folderclosedfail.gif\"");
 		}
 	}
 
 	/**
 	 * Main function
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length!=3){
 			System.out.println("Help - web_TreeViewInitializator "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: web_TreeViewInitializator [PATH] [PROJECTNAME] [GLOBAL_SETTINGS]");
 			System.out.println("\n\n[PATH]");
 			System.out.println("\n     Absolute Path for scan for TypeChef-Files\n");
 			System.out.println("\n[PROJECTNAME]");
 			System.out.println("\n     Project name\n");
 			System.out.println("\n[GLOBAL_SETTINGS]");
 			System.out.println("\n     Absolute Path for the global_settings.xml\n");
 		}else{
 			try {
 				System.out.println("\nRead needed variables...");
 				//Windows or Unix OS?
 				if(args[0].indexOf('\\')!=-1){
 					folderSeparator = "\\";
 				}else{
 					folderSeparator = "/";
 				}
 
 				GLOBAL_SETTINGS_FILE = args[2];
 
 				if(args[0].substring(args[0].length()-1).equals(folderSeparator)){
 					args[0] = args[0].substring(0,args[0].length()-1);
 				}
 
 				System.out.println("Read folder tree...");
 				getAllFiles(args[0],false);
 
 
 				System.out.println("Sort folder tree...");
 
 				String[] sortArr = new String[files.size()];
 				for(int i=0;i<sortArr.length;i++){
 					sortArr[i] = files.get(i).getAbsolutePath();
 				}
 
 				Arrays.sort(sortArr);
 
 				while(files.size()!=0){
 					files.remove(0);
 				}
 				for(int i=0;i<sortArr.length;i++){
 					files.add(new File(sortArr[i]));
 				}
 
 
 				System.out.println("Build header...");

				String[] xpath = {"settings","website","generic","treeview","icons"};
 				js_header(args[1],read_setting(xpath),args[0]);
 
 				System.out.println("Build folder tree...");
				xpath = new String[]{"settings","global","website","defaultURI"};
				js_tree(args[0],args[1],read_setting(xpath));
 
 				System.out.println("\nSave folder tree...");
 				xpath = new String[]{"settings","global","webint","path"};
 				String readedPath = read_setting(xpath);
 				xpath = new String[]{"settings","global","website","path"};
 				readedPath = readedPath+read_setting(xpath);
 				xpath = new String[]{"settings","website","generic","treeview","path"};
 				readedPath = readedPath+read_setting(xpath);
 
 				writeTxt(readedPath+folderSeparator+args[1]+".js");
 				System.out.println("DONE!");
 
 
 			} catch (Exception e) {
 				System.out.println("ERROR! File not exists, read error or XML not well-formed!\n\n");
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 }
