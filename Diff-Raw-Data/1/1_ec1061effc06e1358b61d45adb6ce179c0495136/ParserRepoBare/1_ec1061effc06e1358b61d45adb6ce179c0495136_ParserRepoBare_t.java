 /**
  * RepoBareParser, 	auxiliary class to Aptoide's ServiceData
  * Copyright (C) 2011 Duarte Silveira
  * duarte.silveira@caixamagica.pt
  * 
  * derivative work of previous Aptoide's RssHandler with
  * Copyright (C) 2009  Roberto Jacinto
  * roberto.jacinto@caixamagica.pt
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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 package pt.caixamagica.aptoide.appsbackup.data.xml;
 
 import java.util.ArrayList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewAppDownloadInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplication;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.EnumAgeRating;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.EnumMinScreenSize;
 import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
 
 import android.util.Log;
 
 /**
  * RepoBareParser, handles Bare Repo xml Sax parsing
  * 
  * @author dsilveira
  * @since 3.0
  *
  */
 public class ParserRepoBare extends DefaultHandler{
 	private ManagerXml managerXml = null;
 	
 	private ViewXmlParse parseInfo;
 	private ViewApplication application;
 	private ViewAppDownloadInfo downloadInfo;	
 	private ArrayList<ViewApplication> applications = new ArrayList<ViewApplication>(Constants.APPLICATIONS_IN_EACH_INSERT);
 	private ArrayList<ArrayList<ViewApplication>> applicationsInsertStack = new ArrayList<ArrayList<ViewApplication>>(2);
 	private ArrayList<ViewAppDownloadInfo> downloadsInfo = new ArrayList<ViewAppDownloadInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
 	private ArrayList<ArrayList<ViewAppDownloadInfo>> downloadsInfoInsertStack = new ArrayList<ArrayList<ViewAppDownloadInfo>>(2);
 	
 	private EnumXmlTagsBare tag = EnumXmlTagsBare.apklst;
 	
 	private String packageName = "";
 	private int parsedAppsNumber = 0;
 	private boolean firstBucket = true;
 	private boolean secondBucket = true;
 	
 	private StringBuilder tagContentBuilder;
 	
 		
 	public ParserRepoBare(ManagerXml managerXml, ViewXmlParse parseInfo){
 		this.managerXml = managerXml;
 		this.parseInfo = parseInfo;
 	}
 	
 	@Override
 	public void characters(final char[] chars, final int start, final int length) throws SAXException {
 		super.characters(chars, start, length);
 		
 		tagContentBuilder.append(new String(chars, start, length).trim());
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		super.endElement(uri, localName, qName);
 		
 		tag = EnumXmlTagsBare.safeValueOf(localName.trim());
 		
 		switch (tag) {
 			case apkid:
 				packageName = tagContentBuilder.toString();
 				break;
 			case vercode:
 				int versionCode = Integer.parseInt(tagContentBuilder.toString().trim());
 				application = new ViewApplication(packageName, versionCode, false);
 				application.setRepoHashid(parseInfo.getRepository().getHashid());
 				break;
 			case ver:
 				application.setVersionName(tagContentBuilder.toString());
 				break;
 			case name:
 				application.setApplicationName(tagContentBuilder.toString());
 				break;
 			case catg2:
 				application.setCategoryHashid((tagContentBuilder.toString().trim()).hashCode());
 //				Log.d("Aptoide-RepoBareParser", "app: "+application.getApplicationName()+", appHashid (Not full): "+application.getHashid()+", category: "+tagContentBuilder.toString().trim()+", categoryHashid: "+application.getCategoryHashid());
 				break;
 			case timestamp:
 				application.setTimestamp(Long.parseLong(tagContentBuilder.toString().trim())*1000);
 				break;
 			case age:
 				application.setRating(EnumAgeRating.safeValueOf(tagContentBuilder.toString().trim()).ordinal());
 				break;
 			case minScreen:
 				application.setMinScreen(EnumMinScreenSize.valueOf(tagContentBuilder.toString().trim()).ordinal());
 				break;
 			case minSdk:
 				application.setMinSdk(Integer.parseInt(tagContentBuilder.toString().trim()));
 				break;
 			case minGles:
 				float gles = Float.parseFloat(tagContentBuilder.toString().trim());
 				if(gles < 1.0){
 					gles = 1;
 				}
 				application.setMinGles(gles);
 				break;
 				
 			case path:
 				String appRemotePathTail = tagContentBuilder.toString();
 				downloadInfo = new ViewAppDownloadInfo(appRemotePathTail, application.getFullHashid());
 				break;
 				
 			case md5h:
 				downloadInfo.setMd5hash(tagContentBuilder.toString());
 				break;
 				
 			case sz:
 				downloadInfo.setSize(Integer.parseInt(tagContentBuilder.toString()));
 				if(downloadInfo.getSize()==0){	//TODO complete this hack with a flag <1KB
 					downloadInfo.setSize(1);
 				}
 				break;
 				
 			case pkg:
 				
 				if((firstBucket && parsedAppsNumber >= managerXml.getDisplayListsDimensions().getFastReset()) 
 						|| (secondBucket && parsedAppsNumber >= (managerXml.getDisplayListsDimensions().getCacheSize()-managerXml.getDisplayListsDimensions().getFastReset())) 
 						|| parsedAppsNumber >= Constants.APPLICATIONS_IN_EACH_INSERT){
 					
 					
 					final boolean insertingFirstBucket;
 					if(firstBucket){
 						firstBucket = false;
 						insertingFirstBucket = true;
 						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 						Log.d("Aptoide-RepoBareParser", "initial bucket full, inserting apps: "+applications.size());
 					}else if(secondBucket){
 						secondBucket = false;
 						insertingFirstBucket = false;
 //						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 						Log.d("Aptoide-RepoBareParser", "second bucket full, inserting apps: "+applications.size());
 					}else{
 						insertingFirstBucket = false;
 						Log.d("Aptoide-RepoBareParser", "bucket full, inserting apps: "+applications.size());
 					}
 
 					parseInfo.getNotification().incrementProgress(applications.size());
 					
 					parsedAppsNumber = 0;
 					applicationsInsertStack.add(applications);
 					downloadsInfoInsertStack.add(downloadsInfo);
 			
 					try{
 						new Thread(){
 							public void run(){
 								this.setPriority(Thread.MIN_PRIORITY);
 								
 								final ArrayList<ViewApplication> applicationsInserting = applicationsInsertStack.remove(Constants.FIRST_ELEMENT);
 								managerXml.getManagerDatabase().insertApplications(applicationsInserting);
 								
 								final ArrayList<ViewAppDownloadInfo> downloadsInfoInserting = downloadsInfoInsertStack.remove(Constants.FIRST_ELEMENT);
 								managerXml.getManagerDatabase().insertDownloadsInfo(downloadsInfoInserting);
 								
 //								if(insertingFirstBucket && !managerXml.serviceData.getManagerPreferences().getShowApplicationsByCategory()){
 //									managerXml.serviceData.resetAvailableLists();
 //								}
 							}
 						}.start();
 			
 					} catch(Exception e){
 						/** this should never happen */
 						e.printStackTrace();
 					}
 					
 					applications = new ArrayList<ViewApplication>(Constants.APPLICATIONS_IN_EACH_INSERT);
 					downloadsInfo = new ArrayList<ViewAppDownloadInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
 				
 				}
 				
 				parsedAppsNumber++;
 				
 				downloadsInfo.add(downloadInfo);
 				applications.add(application);
 				break;
 			
 				
 			case basepath:
 				parseInfo.getRepository().setBasePath(tagContentBuilder.toString());
 				break;	
 			case iconspath:
 				parseInfo.getRepository().setIconsPath(tagContentBuilder.toString());
 				break;	
 			case screenspath:
 				parseInfo.getRepository().setScreensPath(tagContentBuilder.toString());
 				break;	
 			case appscount:
 				int size = Integer.parseInt(tagContentBuilder.toString());
 				parseInfo.getRepository().setSize(size);		
 				parseInfo.getNotification().setProgressCompletionTarget(size);
 				
 				if(size > Constants.MAX_APPLICATIONS_IN_STATIC_LIST_MODE){
 					managerXml.serviceData.switchAvailableListToDynamic();
 				}else{
 					managerXml.serviceData.switchAvailableListToStatic();					
 				}
 				break;
 			case hash:
 				parseInfo.getRepository().setDelta(tagContentBuilder.toString());
 				break;
 				
 			case repository:
 				managerXml.getManagerDatabase().insertRepository(parseInfo.getRepository());
 				
				managerXml.serviceData.getManagerPreferences().setServerInconsistentState(false, null, false);
 				managerXml.serviceData.repoInserted();
 				break;
 				
 			default:
 				break;
 		}		
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 		super.startElement(uri, localName, qName, attributes);
 
 		tagContentBuilder = new StringBuilder();
 		
 	}
 	
 	
 	
 	
 	@Override
 	public void startDocument() throws SAXException {	//TODO refacto Logs
 		Log.d("Aptoide-RepoBareParser","Started parsing XML from " + parseInfo.getRepository().getRepoName() + " ...");
 		managerXml.serviceData.loadingAvailableProgressIndeterminate();
 		super.startDocument();
 	}
 
 	@Override
 	public void endDocument() throws SAXException {
 		Log.d("Aptoide-RepoBareParser","Done parsing XML from " + parseInfo.getRepository().getRepoName() + " ...");
 
 		if(!applications.isEmpty()){
 			parseInfo.getNotification().incrementProgress(applications.size());
 			
 			Log.d("Aptoide-RepoBareParser", "bucket not empty, apps: "+applications.size());
 			applicationsInsertStack.add(applications);
 			downloadsInfoInsertStack.add(downloadsInfo);
 		}
 		Log.d("Aptoide-RepoBareParser", "buckets: "+applicationsInsertStack.size());
 		while(!applicationsInsertStack.isEmpty()){
 			managerXml.getManagerDatabase().insertApplications(applicationsInsertStack.remove(Constants.FIRST_ELEMENT));
 			managerXml.getManagerDatabase().insertDownloadsInfo(downloadsInfoInsertStack.remove(Constants.FIRST_ELEMENT));			
 		}
 		
 		parseInfo.getNotification().setCompleted(true);
 		
 		managerXml.getManagerDatabase().optimizeQuerys();
 		
 		managerXml.parsingRepoBareFinished(parseInfo.getRepository());
 		super.endDocument();
 	}
 
 
 }
