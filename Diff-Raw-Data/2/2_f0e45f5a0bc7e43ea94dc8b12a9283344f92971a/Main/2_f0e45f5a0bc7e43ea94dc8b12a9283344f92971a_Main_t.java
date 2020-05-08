 package org.jefflau.themechanger;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 public class Main {
 	public static final String APKSUFFIX=".apk";
 	public static final String ORGDIR="org";
 	public static final String ORGTMPDIR="org/tmp";
 	
 	public static final String ORGTMPRESDIR="org/tmp/res";
 	
 	public static final String ORGTMPRESXMLDIR="org/tmp/res/xml";
 	
 	public static final String DESTDIR="dest";
 	public static final String DESTTMPDIR="dest/tmp";
 	public static final String DESTDIRZIPS="dest/zips";
 	
 	public static final String PREFIXDRAW="drawable";
 	public static final String PREFIXMIPMAP="mipmap";
 	public static final String RES = "res";
 	public static final String DESTRES="destres";
 	
 	private static boolean apktool(String apkName){
 		Log.d("=========================뿪ʼ=======================");
 		
 		File file=new File(ORGDIR+File.separator+apkName);
 		if(null==file||!file.isFile()||!apkName.endsWith(APKSUFFIX)){
 			Log.d(file.getAbsolutePath()+" ļڻ߲"+APKSUFFIX+"ļ");
 		}else{
 			String filePath=file.getAbsolutePath();
 			Log.d("ļ·"+filePath);
 			file=new File(ORGTMPDIR);
 			if(null!=file&&!file.exists()){
 				file.mkdirs();
 				Log.d(ORGTMPDIR+" Ŀ¼ɹ");
 			}
 			String command="tools/apktool.bat d -f "+ORGDIR+File.separator+apkName+" "+ORGTMPDIR;
 			try {
 				Process proc=Runtime.getRuntime().exec(command);
 				
 				BufferedReader in=new BufferedReader(new InputStreamReader(proc.getInputStream()));
 				String out=null;
 				while((out=in.readLine())!=null){
 					Log.d("----"+out);
 				}
 				proc.waitFor();
 				if(proc.exitValue()==0){
 					Log.d("ɹ file="+apkName);
 					return true;
 				}
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
 		return false;
 	}
 	private static void delFile(String fileName){
 		delFile(new File(fileName));
 	}
 	private static void delFile(File file){
 		if(null==file||!file.exists())return;
 		if(file.isDirectory()){
 			File[] list=file.listFiles();
 			for(File child:list){
 				delFile(child);
 			}
 		}
 		file.delete();
 	}
 	private static void delEmptyDir(File file){
 		if(null==file||!file.exists())return;
 		if(file.isDirectory()){
 			File[] list=file.listFiles();
 			for(File child:list){
 				delEmptyDir(child);
 			}
 			file.delete();
 		}
 		//file.delete();
 	}
 	private static boolean cleanDestTempFile(){
 		delFile(new File(DESTTMPDIR));
 		delFile(new File(DESTDIRZIPS));
 		return true;
 	}
 	
 	private static boolean cleanDestTempEmptyDir(){
 		File file=new File(DESTTMPDIR);
 		delEmptyDir(file);
 		
 		return true;
 	}
 	
 	
 	private static boolean mkdirs(String pkgName,File[]res){
 		/*File destResDir=new File(DESTTMPRESDIR+File.separator+pkgName);
 		if(!destResDir.exists()){
 			destResDir.mkdirs();
 		}*/
 		
 		for(File draw:res){
 			String drawName=draw.getName();
 			if(draw.isDirectory()
 				&&(drawName.startsWith(PREFIXDRAW)||drawName.startsWith(PREFIXMIPMAP))){
 				File destResDir=new File(DESTTMPDIR+
 						File.separator+pkgName+
 						File.separator+DESTRES+
 						File.separator+draw.getName());
 				if(!destResDir.exists()){
 					destResDir.mkdirs();
 				}
 			}
 		}
 		return true;
 	}
 
 	private static boolean copyFile(String org, String dest) throws IOException {
 		FileInputStream fin = null;
 		FileOutputStream fout = null;
 		try {
 			fin = new FileInputStream(org);
 			fout = new FileOutputStream(dest);
 			byte[] b = new byte[512];
 			int n;
 			while ((n = fin.read(b)) != -1) {
 				fout.write(b, 0, n);// ĸ
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				fin.close();
 				fout.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	private static boolean change(File xmlFile,File[]res){
 		boolean flag=false;
 		Log.d("ʼתļ path="+xmlFile.getAbsolutePath());
 		HashMap<String,String>map=parseXml(xmlFile.getAbsolutePath());
 		
 		if(null==map||map.size()==0){
 			Log.d("ļ");
 			return false;
 		}
 		String pkgName=xmlFile.getName();
 		
 		// ת
 		pkgName=pkgName.substring(0, pkgName.lastIndexOf("."));
 		pkgName=pkgName.replace("_", ".");
 		
 		flag=mkdirs(pkgName,res);
 		if(!flag)return flag;
 		for(File resDir:res){
 			String[] resDirFileList=resDir.list();
 			//Log.d(resDirFileList);
 			Set<String> keyList=map.keySet();
 			
 			for(String str:keyList){
 				//Log.d("key="+str+" value="+map.get(str));
 				// 1.org/tmp/res/... Ŀ¼¿dest/tmp/$pkgName/res/... Ŀ¼
 				boolean nightFlag=true;
 				String drawable=map.get(str);//@drawable/framework_btn_default_disabled_holo_light
 				drawable=drawable.substring(drawable.lastIndexOf("/")+1)+".9.png";
 				//Log.d(drawable);
 				int index=Arrays.binarySearch(resDirFileList, drawable);
 				if(index<0){
 					drawable=drawable.replace(".9","");
 					index=Arrays.binarySearch(resDirFileList, drawable);
 					nightFlag=false;
 				}
 				if(index>=0){
 					//System.out.println("ddddd "+resDirFileList[index]);
 					String orgDrawable=resDir.getAbsolutePath()+File.separator+drawable;
 					
 					
 					str=str.substring(str.lastIndexOf("/")+1);
 					String destDrawable=DESTTMPDIR+
 						File.separator+pkgName+
 						File.separator+DESTRES+
 						File.separator+resDir.getName()+
 						File.separator+str;
 					if(nightFlag){
 						destDrawable=destDrawable+".9.png";
 					}else{
 						destDrawable=destDrawable+".png";
 					}
 					//ļ orgDrawabledestDrawable
 					
 					try {
 						copyFile(orgDrawable,destDrawable);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}else{
 					
 				}
 				// 2.ɾûļĿ¼
 				
 				
 			}
 		}
 		
 		return true;
 	}
 	private static HashMap<String,String> parseXml(String path){
 		//create factory
 		SAXParserFactory factory=SAXParserFactory.newInstance();
 		XmlMapHandler handler=new XmlMapHandler();
 		SAXParser sp;
 		try {
 			//get SAXParser
 			sp = factory.newSAXParser();
 			XMLReader reader=sp.getXMLReader();
 			reader.setContentHandler(handler);
 			
 			reader.parse(path);
 			
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return handler.getMap();
 		
 		
 	}
 	private static boolean changDrawable(){
 		File file=new File(ORGTMPRESDIR);
 		if(null==file||!file.exists()||!file.isDirectory()){
 			Log.d("res ļ  resPath="+file.getAbsolutePath());
 			return false;
 		}
 		File[] res=file.listFiles();
 		if(null==res||res.length<=2){
 			return false;
 		}
 		file=new File(ORGTMPRESXMLDIR);
 		if(null==file||!file.exists()||!file.isDirectory()){
 			Log.d("xml ļĿ¼");
 			return false;
 		}
 		File[] xml=file.listFiles();
 		
 		// ɾdest/tmpʱĿ¼
 		cleanDestTempFile();
 		
 		for(File xmlFile:xml){
 			change(xmlFile,res);
 		}
 		//ɾյĿ¼
 		cleanDestTempEmptyDir();
 		Log.d("========================ͼƬת==============");
 		return true;
 		
 	}
 	private static void printMap(HashMap<String,String>map){
 		
 		Set<String> keyList=map.keySet();
 		for(String str:keyList){
 			Log.d("key="+str+" value="+map.get(str));
 		}
 	}
 	private static boolean  aapt(boolean delDestRes){
 		File file=new File(DESTTMPDIR);
 		if(null==file||!file.isDirectory()){
 			return false;
 		}
 		File []list=file.listFiles();
 		if(null==list||list.length<=0){
 			return false;
 		}
 		for(File destPkg:list){
 			String command="tools/aapt.exe c -S "+DESTTMPDIR+File.separator+destPkg.getName()+File.separator+DESTRES
 					+" -C "+DESTTMPDIR+File.separator+destPkg.getName()+File.separator+RES;
 			try {
 				Process proc=Runtime.getRuntime().exec(command);
 				
 				BufferedReader in=new BufferedReader(new InputStreamReader(proc.getInputStream()));
 				String out=null;
 				while((out=in.readLine())!=null){
 					Log.d("----"+out);
 				}
 				proc.waitFor();
 				if(proc.exitValue()==0){
 					Log.d("pngͼƬ file="+destPkg.getName());
 					
 					if(delDestRes){
 						delFile(new File(DESTTMPDIR+File.separator+destPkg.getName()+File.separator+DESTRES));
 					}
 				}
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		Log.d("=============pngͼƬ===========");
 		return true;
 	}
 	private static boolean zipFiles(String apkName,boolean zipOne,boolean delTmp){
 		File file=new File(DESTTMPDIR);
 		if(null==file||!file.isDirectory()){
 			return false;
 		}
 		apkName=apkName.substring(0,apkName.lastIndexOf("."));
 		apkName=DESTDIR+File.separator+apkName;
 		
 		File zips=new File(DESTDIRZIPS);
 		delFile(zips);
 		if(!zips.exists()){
 			zips.mkdirs();
 		}
 		for(File chr:file.listFiles()){
 			try {
 				ZipUtils.zip( chr,DESTDIRZIPS+File.separator+chr.getName());
 				Log.d("ļѹ name="+chr.getName());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 		}
 		//ѹɴ
 		if(zipOne){
 			delFile(apkName);
 			try {
 				ZipUtils.zip(zips,apkName);
 				Log.d("=========================ļѹ"+apkName+"============");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		if(delTmp){
 			delFile(file);
 			delFile(zips);
 		}
 		return true;
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		//apkName
 		
 		File file=new File(ORGDIR);
 		if(!file.exists()){
 			file.mkdirs();
 		}
 		String[]list=file.list();
		if(null!=list&&list.length>=1){
 			for(String apkName:list){
 				if(apkName.endsWith(APKSUFFIX)){
 					Log.d("=====================תļʼ"+apkName+"=====================");
 					boolean flag=apktool(apkName);
 					if(!flag){
 						Log.d("ʧܡ˳");
 						return;
 					}
 					// ͼƬת
 					changDrawable();
 					
 					// aapt ͼƬ
 					aapt(true);
 					// ѹļļ
 					zipFiles(apkName,true,false);
 					Log.d("=====================תļ"+apkName+"=====================");
 				};
 				
 			}
 		}
 		Log.d("==========================================");
 		
 		
 	}
 	
 }
