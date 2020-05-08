 package hu.schmidtsoft.timeboss.server.localfile;
 
 import hu.schmidtsoft.timeboss.server.AbstractTimeBossServer;
 import hu.schmidtsoft.timeboss.server.Activity;
 import hu.schmidtsoft.timeboss.util.UtilEvent;
 import hu.schmidtsoft.timeboss.util.UtilTime;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class LocalFileTimeBossServer extends AbstractTimeBossServer {
 	File dir;
 	File logs;
 
 	public LocalFileTimeBossServer(File dir, UtilEvent setTitleEvent) {
 		super(setTitleEvent);
 		this.dir = dir;
 		this.logs = new File(dir, "logs");
 		logs.mkdirs();
 		File[] fs = logs.listFiles();
 		List<File> files=new ArrayList<File>();
 		if (fs != null) {
 			for (File f : fs) {
 				files.add(f);
 			}
 		}
 		Collections.sort(files, new Comparator<File>(){
 			@Override
 			public int compare(File o1, File o2) {
 				return o1.getName().compareTo(o2.getName());
 			}});
 		if(files.size()>0)
 		{
 			Activity last=null;
 			int i=files.size()-1;
			while(last==null&&i>=0)
 			{
				File f=files.get(files.size()-1);
 				try {
 					Activity act=new Activity(UtilFile.loadFile(f));
					if(act.getWork())
 					{
 						last=act;
 					}
 				} catch (Exception e) {
 					Logger.getLogger(LocalFileTimeBossServer.class.getName()).log(Level.SEVERE, "loading last activity", e);
 				}
 				--i;
 			}
 			if(last!=null)
 			{
 				setLastActivity(last);
 			}
 		}
 	}
 
 	@Override
 	public void setPreferences(byte[] prefs) {
 		try {
 			UtilFile.saveFile(new File(dir, "preferences.xml"), prefs);
 		} catch (IOException e) {
 			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Saving preferences", e);
 		}
 	}
 
 	@Override
 	public byte[] getPreferences() throws IOException {
 		return UtilFile.loadFile(new File(dir, "preferences.xml"));
 	}
 
 	@Override
 	protected void doSaveActivity(Activity activity) throws IOException {
 		String fileName = UtilTime.timeToFileName(activity.getStartTime());
 		File f = new File(logs, "" + fileName + ".act");
 		UtilFile.saveFile(f, activity.toXml());
 	}
 
 }
