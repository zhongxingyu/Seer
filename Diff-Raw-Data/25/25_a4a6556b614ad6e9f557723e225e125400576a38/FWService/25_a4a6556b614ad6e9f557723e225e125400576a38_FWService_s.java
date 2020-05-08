 /**
  * 
  */
 package com.k99k.ij.resize;
 
 import it.sauronsoftware.ftp4j.FTPClient;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 import org.stringtree.json.JSONReader;
 import org.stringtree.json.JSONValidatingReader;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 /**
  * Featured wallpapers的图片上传处理服务
  * @author keel
  *
  */
 public class FWService implements Runnable {
 	
 	
 	private static final Logger log = Logger.getLogger(FWService.class);
 	
 	private static final int TASK_NO = 0;
 	private static final int TASK_SCAN = 1;
 	private static final int TASK_BUILD = 2;
 	
 	/**
 	 * wallTask表中的state,2为成功
 	 */
 	private static final int TASKDONE_OK = 2;
 	/**
 	 * wallTask表中的state,3为失败
 	 */
 	private static final int TASKDONE_FAIL = 3;
 	
 	
 	
 	private String prePath = "d:/fwservice/prePath";
 	private String readyPath = "d:/fwservice/readyPath";
 	private String srcPath = "d:/fwservice/srcPath";
 	private String outPath = "d:/fwservice/outPath";
 	private String datePath = "d:/fwservice/datePath";
 	private String tmpPath = "d:/fwservice/datePath";
 	private String config = "fws.json";
 	private String webPath = "/orion";
 	private int sleep = 5000;
 	private int preWidth = 300;
 	private int preHeight = 200;
 	private final static IJBuilderForFW ij = new IJBuilderForFW();
 	
 	private boolean runFlag = true;
 	
 	private Map<String,String>[] ftps;
 	
 	private String mongoIP = "127.0.0.1";
 	private int mongoPort = 27017;
 	
 	/**
 	 * 当前任务id,用于任务处理后更新任务状态(wallTask表中的state,1为待处理,2为成功,3为失败)
 	 */
 	private ObjectId currentTaskId;
 	
 	private static Mongo mongo;
 	
 	private DB db;
 	
 	/**
 	 * 获取数据库连接并验证
 	 * @return
 	 */
 	private final boolean getDB(){
 		try {
 			if (this.db !=null && this.db.getName().equals("fwall")) {
 				return true;
 			}
 			if (mongo == null) {
 				mongo = new Mongo(this.mongoIP , this.mongoPort );
 			}
 			this.db = mongo.getDB("fwall");
 			boolean auth = db.authenticate("sikewall009", "6667441".toCharArray());
 			if (auth) {
 				return true;
 			}
 		} catch (UnknownHostException e) {
 			log.error("getDB error", e);
 			return false;
 		} catch (MongoException e) {
 			log.error("getDB error", e);
 			mongo = null;
 			return false;
 		}catch (Exception e) {
 			mongo = null;
 			log.error("getDB error", e);
 			return false;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * 连接数据库监控任务表，与WEB程序沟通,同时确定任务id(wallTask表中的state,1为待处理,2为成功,3为失败)
 	 * @return 0表示无任务，其他与任务标识匹配
 	 */
 	private final int checkTask(){
 		int task = TASK_NO;
 		if (!this.getDB()) {
 			return TASK_NO;
 		}
 		DBCollection coll = db.getCollection("wallTask");
 		DBCursor cur = coll.find(new BasicDBObject("state",1)).sort(new BasicDBObject("addTime",1));
 		if (cur.hasNext()) {
 			DBObject c = cur.next();
 			task = (Integer)(c.get("task"));
 			currentTaskId = (ObjectId) c.get("_id");
 			log.info("new task:"+task);
 		}
 		return task;
 	}
 	
 	/**
 	 * 更新任务状态 :wallTask表中的state,1为待处理,2为成功,3为失败
 	 * @param oid
 	 * @param state
 	 */
 	private final void updateTaskState(ObjectId oid,int state){
 		if (!this.getDB()) {
 			log.error("updateTaskState - getDB error");
 			return;
 		}
 		DBCollection coll = db.getCollection("wallTask");
 		coll.update(new BasicDBObject("_id",this.currentTaskId), new BasicDBObject("$set",new BasicDBObject("state",state)));
 	}
 	
 	/**
 	 * 获取每个分类的最大picId值
 	 * @return
 	 */
 	private HashMap<String,Integer> getInitIDMap(){
 		//db.wallPic.find({'cate':'space','state':1}).sort({'picId':-1}).limit(1)
 		HashMap<String, Integer> initIdMap = new HashMap<String, Integer>();
 		if (!this.getDB()) {
 			log.error("getInitIDMap - getDB error!");
 			return initIdMap;
 		}
 		DBCollection coll = db.getCollection("wallCate");
 		DBCollection piccoll = db.getCollection("wallPic");
 		DBCursor cur = coll.find(new BasicDBObject("state",1));
 		while (cur.hasNext()) {
 			DBObject c = cur.next();
 			String cate = (String) c.get("catePre");
 			DBCursor piccur = piccoll.find(new BasicDBObject("cate",cate).append("state",1)).sort(new BasicDBObject("picId",-1)).limit(1);
 			if (piccur.hasNext()) {
 				DBObject cc = piccur.next();
 				initIdMap.put(cate, (Integer)cc.get("picId"));
 			}
 		}
 		return initIdMap;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private boolean init(){
 		//读取配置文件
 		try {
 			
 			JSONReader jsonReader = new JSONValidatingReader();
 			String path = (this.getClass().getResource("/").getPath());//.replaceAll("\\\\", "/");
 			if (path.startsWith("/")) {
 				path = path.substring(1);
 			}
 			log.info("Read json:"+path+config);
 			BufferedReader in = new BufferedReader(
 			            new InputStreamReader(new FileInputStream(path+config), "UTF8"));
 			String str;
 			StringBuilder sb = new StringBuilder();
 			while ((str = in.readLine()) != null){
 				sb.append(str);
 			}
 			Map<String,Object> json = (Map<String,Object>) jsonReader.read(sb.toString());
 			
 //			Properties ini = new Properties();
 //			FileInputStream fls;
 //			
 //			log.info("Read ini:"+path+config);
 //			ini.load(fls = new FileInputStream(path+config));
 			this.prePath = json.get("prePath").toString();
 			this.readyPath = json.get("readyPath").toString();
 			this.outPath = json.get("outPath").toString();
 			this.srcPath = json.get("srcPath").toString();
 			this.datePath = json.get("datePath").toString();
 			this.webPath = json.get("webPath").toString();
 			this.mongoIP = json.get("mongoIP").toString();
 			this.mongoPort = Integer.parseInt(json.get("mongoPort").toString());
 			this.sleep = Integer.parseInt(json.get("sleep").toString());
 			this.preWidth = Integer.parseInt(json.get("preWidth").toString());
 			this.preHeight = Integer.parseInt(json.get("preHeight").toString());
 			ArrayList ftps =  (ArrayList) json.get("synftp");
 			this.ftps = new HashMap[ftps.size()];
 			int i = 0;
 			for (Iterator it = ftps.iterator(); it.hasNext();) {
 				this.ftps[i] = (Map) it.next();
 				i++;
 			}
 			
 			
 			
 //			fls.close();
 		} catch (IOException  e) {
 			log.error("config read failed.", e);
 			return false;
 		} catch (Exception e) {
 			log.error("config read failed.", e);
 			return false;
 		}
 		log.info("-------init FWS ok!--------");
 		log.info("prePath:"+prePath);
 		log.info("readyPath:"+readyPath);
 		log.info("srcPath:"+srcPath);
 		log.info("outPath:"+outPath);
 		log.info("webPath:"+webPath);
 		log.info("mongoIP:"+mongoIP);
 		log.info("mongoPort:"+mongoPort);
 		log.info("sleep:"+sleep);
 		log.info("preWidth:"+preWidth);
 		log.info("preHeight:"+preHeight);
 		for (int i = 0; i < this.ftps.length; i++) {
 			log.info("synftp:"+this.ftps[i].get("ip")+":"+this.ftps[i].get("port")+" "+this.ftps[i].get("dir"));
 		}
 		log.info("---------------------------");
 		return true;
 	}
 	
 	 /**
 	  * Deletes all files and subdirectories under dir.
 	  * Returns true if all deletions were successful.
 	 * @param dir
 	 * @return
 	 */
 	public final static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					// 跳过
 					// return false;
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	/**
 	 * 复制整个文件夹到另一位置
 	 * @param from File 文件夹
 	 * @param to File 新的文件夹
 	 */
 	private static final void copyFullDir(File from,File to){
 		if (from.exists()) {
 			if (from.isDirectory()) {
 				to.mkdirs();
 				String[] children = from.list();
 				for (int i = 0; i < children.length; i++) {
 					copyFullDir(new File(from, children[i]),new File(to, children[i]));
 				}
 
 			}else{
 				copy(from,to);
 			}
 		}
 	}
 	
 	/**
 	 * 复制单个文件,如原文件存在则直接覆盖
 	 * @param fileFrom
 	 * @param fileTo
 	 * @return
 	 */
 	private static final boolean copy(File fileFrom, File fileTo) {  
         try {  
             FileInputStream in = new FileInputStream(fileFrom);  
             FileOutputStream out = new FileOutputStream(fileTo);  
             byte[] bt = new byte[1024*5];  
             int count;  
             while ((count = in.read(bt)) > 0) {  
                 out.write(bt, 0, count);  
             }  
             in.close();  
             out.close();  
             return true;
         } catch (IOException e) { 
         	log.error("copy error", e);
             return false;  
         }  
     } 
 	
 	
 //	/**
 //	 * 上传文件夹及下面的所有文件
 //	 * @param client FTPClient
 //	 * @param file 文件夹
 //	 * @throws Exception
 //	 */
 //	private final void uploadFile(FTPClient client,File file,boolean includeDir) throws Exception{
 //		try {
 //			
 //			if (file.isDirectory()) {
 //				if (includeDir) {
 //					String remotePath = client.currentDirectory()+"/"+file.getName();
 //					try {
 //						client.changeDirectory(remotePath);
 //					} catch (Exception e) {
 //						client.createDirectory(remotePath);
 //						client.changeDirectory(remotePath);
 //					}
 //					includeDir = true;
 //				}
 //				log.info("remotePath----:"+client.currentDirectory());
 //				String[] children  = file.list();
 //				for (int i = 0; i < children .length; i++) {
 //					uploadFile(client,new File(file, children[i]),true);
 //				}
 //			}else{
 //				client.upload(file);
 //				log.info("uploadFile:"+file.getName());
 //			}
 //		} catch (Exception e) {
 //			log.error("uploadFile error:", e);
 //		}
 //		
 //	}
 	
 	/**
 	 * 上传文件夹及下面的所有文件
 	 * @param client FTPClient
 	 * @param srcDir 本地文件夹
 	 * @param targetDir 远程目标文件夹
 	 * @throws Exception
 	 */
 	private final void uploadFile(FTPClient client,String srcDir,String targetDir) throws Exception{
 		try {
 			
 			//移动至目标文件夹,若无则创建
 			try {
 				client.changeDirectory(targetDir);
 			} catch (Exception e) {
 				client.createDirectory(targetDir);
 				client.changeDirectory(targetDir);
 			}
 			
 			log.info("remotePath----:"+client.currentDirectory());
 			
 			File dirf = new File(srcDir);
 			File[] fileList  = dirf.listFiles();
 			for (int i = 0; i < fileList.length; i++) {
 				if (fileList[i].isFile()) {
 					//上传文件
 					client.upload(fileList[i]);
 					log.info(fileList[i].getName());
 				}else if(fileList[i].isDirectory()){
 					//上传文件夹
 					String children = srcDir+"/"+fileList[i].getName();
 					String remote = targetDir+"/"+fileList[i].getName();
 					uploadFile(client,children,remote);
 				}
 			}
 		} catch (Exception e) {
 			log.error("uploadFile error:", e);
 		}
 		
 	}
 	
 	/**
 	 * ftp同步到配置文件中的所有ftp服务器
 	 */
	private final boolean synFtps(){
 		boolean re = true;
 		for (int i = 0; i < this.ftps.length; i++) {
 			try {
 				FTPClient client = new FTPClient();
 				client.connect(ftps[i].get("ip"),Integer.parseInt(ftps[i].get("port")));
 				client.login(ftps[i].get("user"),ftps[i].get("pwd"));
 				String cdir = ftps[i].get("dir");
 				//上传生成图
				log.info("synftp:"+client.getHost()+" dir:"+cdir+" tmpPath:"+tmpPath);
				uploadFile(client,tmpPath,cdir);	
 				//上传源图
 				cdir = ftps[i].get("src");//+"/"+now();
				log.info("synftp-src:"+client.getHost()+" srcdir:"+cdir+" srcPath:"+srcPath);
				uploadFile(client,srcPath,cdir+"/"+srcPath.substring(srcPath.lastIndexOf("/")+1));
 				client.disconnect(true);
 			} catch (Exception e) {
 				log.error("synftps error!", e);
 				re = false;
 			} 
 		}
 		return re;
 	}
 	
 	/**
 	 * 保存picList到文件
 	 * @param picList
 	 */
 	private static final void saveToFile(ArrayList<String> picList,String savePath){
 		if (picList == null || picList.size() <= 0) {
 			return;
 		}
 		String fileName = picList.get(0);
 		try {
 	        BufferedWriter out = new BufferedWriter(new FileWriter(savePath+"/"+fileName, true));
 	        out.write("------"+new Date()+"----\n");
 	        for (Iterator<String> iterator = picList.iterator(); iterator.hasNext();) {
 				String s = iterator.next();
 				out.write(s+"\n");
 			}
 	        out.close();
 	    } catch (IOException e) {
 	    	log.error("picList saveToFile error!", e);
 	    }
 
 	}
 	
 	/**
 	 * 处理完图片后更新数据库
 	 * @param picList ArrayList catePre#picId
 	 * @return 是否更新成功
 	 */
 	private final boolean updateDB(ArrayList<String> picList){
 		if (picList == null || picList.size() <= 0) {
 			return true;
 		}
 		if (!this.getDB()) {
 			log.error("updateDB - getDB error!");
 			//备份未成功的数据到outPath
 			saveToFile(picList,this.outPath);
 			return false;
 		}
 		try {
 			DBCollection coll = db.getCollection("wallCate");
 			DBCollection piccoll = db.getCollection("wallPic");
 			for (Iterator<String> iterator = picList.iterator(); iterator.hasNext();) {
 				String s = iterator.next();
 				SavePic sp = new SavePic(s,this.webPath);
 				piccoll.insert(sp);
 			}
 			//更新wallCate表的max字段
 			DBCursor cur = coll.find();
 			while (cur.hasNext()) {
 				DBObject c = cur.next();
 				ObjectId oid = (ObjectId) c.get("_id");
 				String cate = (String) c.get("catePre");
 				DBCursor piccur = piccoll.find(new BasicDBObject("cate",cate)).sort(new BasicDBObject("picId",-1)).limit(1);
 				if (piccur.hasNext()) {
 					DBObject cc = piccur.next();
 					c.put("max", (Integer)cc.get("picId"));
 					coll.update(new BasicDBObject("_id",oid), c);
 				}
 			}
 		} catch (Exception e) {
 			log.error("updateDB error!",e);
 			//备份未成功的数据到outPath
 			saveToFile(picList,this.outPath);
 			return false;
 		}
 		log.info("updateDB ok!");
 		return true;
 	}
 	
 	/**
 	 * 生成需要保存的WallPic对象
 	 * 
 	 */
 	class SavePic extends BasicDBObject {
 		
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * @param picstr  catePre#picId
 		 * @param webPath web相对路径
 		 */
 		SavePic(String picstr,String webPath){
 			super();
 			String[] sarr = picstr.split("#");
 			String cate = sarr[0];
 			int pid = 0;
 			if (sarr.length != 2 || (!sarr[1].matches("\\d+"))) {
 				this.put("cate", "errcate"+picstr);
 				this.put("picId",1);
 			}else{
 				this.put("cate", sarr[0]);
 				pid = Integer.parseInt(sarr[1]);
 				this.put("picId",pid);
 			}
 			BasicDBList paths = new BasicDBList();
 			paths.put(0, webPath+"/"+cate+"/"+cate+pid+".jpg");
 			paths.put(1, webPath+"/"+cate+"/"+cate+pid+"_l.jpg");
 			paths.put(2, webPath+"/"+cate+"/b_"+cate+pid+".jpg");
 			paths.put(3, webPath+"/"+cate+"/b_"+cate+pid+"_l.jpg");
 			this.put("picPath", paths);
 			this.put("picSource", "");
 			this.put("picName", picstr);
 			this.put("addTime", new Date());
 			this.put("click", 0);
 			this.put("download", 0);
 			this.put("setWall", 0);
 			this.put("stars", 0);
 			this.put("starInfo", new BasicDBList());
 			this.put("info", "");
 			//将状态标记为2,由web程序每天将状态刷为1
 			this.put("state", 2);
 			this.put("topId", 1);
 		}
 	}
 
 	static final String now(){
 		return new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
 	}
 	
 	@Override
 	public void run() {
 		runFlag = this.init();
 		int re = 0;
 		while (runFlag) {
 			//每次只查找一个任务
 			int t = this.checkTask();
 			switch (t) {
 			case TASK_SCAN:
 				//扫描preSource文件夹,单层目录，生成缩略图
 				if(!ij.makePreviews(prePath, preWidth, preHeight)){
 					re = -1;
 				}
 				log.info("=========================\n");
 				break;
 			case TASK_BUILD:
 				//以日期时间为临时目录
				tmpPath = this.datePath+"/"+now();
				String srcPathA = this.srcPath+"/"+now();
 				//发布图片
 				ArrayList<String> picList = ij.buildNewPics(readyPath, tmpPath, getInitIDMap(),srcPathA);
 				//处理临时文件
 				copyFullDir(new File(tmpPath),new  File(outPath));
 				//更新数据库
 				if(!this.updateDB(picList)){
 					re = -2;
 				}
 				//同步ftp
				if(!this.synFtps()){
 					re = -3;
 				}
 				log.info("=========================\n");
 				break;
 			default:
 				break;
 			}
 			//更新任务状态
 			if (t != TASK_NO) {
 				if (re == 0) {
 					this.updateTaskState(currentTaskId, TASKDONE_OK);
 				}else{
 					this.updateTaskState(currentTaskId, re);
 				}
 			}
 			
 			try {
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 				log.info("Sleep interrupted.", e);
 			}
 		}
 		
 		
 	}
 	
 	
 	
 	void testBuild(){
 		if (!this.getDB()) {
 			System.out.println("...");
 		}
 		DBCollection coll = db.getCollection("wallTask");
 		BasicDBObject t = new BasicDBObject();
 		t.put("task", 2);
 		t.put("state", 1);
 		t.put("addTime", new Date());
 		coll.save(t);
 	}
 
 	
 	/**
 	 * 服务入口 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		FWService fws = new FWService();
 		Thread t = new Thread(fws,"fws");
 		t.start();
 		
 		//测试 
 		//fws.testBuild();
 		
 		
 	}
 
 }
