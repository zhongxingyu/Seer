 // %1730913863:de.hattrickorganizer.tools.updater%
 /*
  * Created on 16.05.2004
  *
  */
 package de.hattrickorganizer.tools.updater;
 
 import gui.UserParameter;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import plugins.IOfficialPlugin;
 import plugins.IPlugin;
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.login.LoginWaitDialog;
 import de.hattrickorganizer.model.Extension;
 import de.hattrickorganizer.model.HOMiniModel;
 import de.hattrickorganizer.model.HOParameter;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.News;
 import de.hattrickorganizer.net.MyConnector;
 import de.hattrickorganizer.net.NewsPanel;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.HelperWrapper;
 import de.hattrickorganizer.tools.ZipHelper;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Thorsten Dietz
  *
  * @since 1.35
  */
 public final class UpdateController {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     private static File zip = null;
     private static File tmp = null;
 
     /** TODO Missing Parameter Documentation */
     public static final String PLUGINS_HOMEPAGE = "http://plugins.hattrickorganizer.net";
 
     /** TODO Missing Parameter Documentation */
     protected static final String WEB_FLAGSFILE = PLUGINS_HOMEPAGE + "/xml/flags.zip";
 
     /** TODO Missing Parameter Documentation */
     protected static final String WEB_PLUGINFILE = PLUGINS_HOMEPAGE + "/xml/pluginVersionen.xml";
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      */
     public static void showDeletePluginDialog() {
         try {
             DeleteDialog dialog = new DeleteDialog();
             dialog.setVisible(true);
         } catch (Exception e1) {
             HOLogger.instance().log(UpdateController.class,e1);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void showLanguageUpdateDialog() {
         try {
             File file = createXMLFile(PLUGINS_HOMEPAGE + "/xml/languages.xml",
                                       new File(System.getProperty("user.dir") + File.separator
                                                + "sprache" + File.separator + "languages.xml"));
 
             Document doc = UpdateHelper.instance().getDocument(file);
 
             Hashtable<String, HPLanguageInfo> list = getWebLanguages(doc.getDocumentElement().getChildNodes(),
                                              new Hashtable<String, HPLanguageInfo>());
 
             LanguagesDialog dialog = new LanguagesDialog(list);
 
             dialog.setVisible(true);
         } catch (Exception e1) {
             HOLogger.instance().log(UpdateController.class,e1);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void showPluginUpdaterLibraries() {
         try {
             File file = createXMLFile(WEB_PLUGINFILE, getLocalXMLFile());
 
             if (file == null) {
                 JOptionPane.showMessageDialog(null, "Where is my xml file?", "Error",
                                               JOptionPane.ERROR_MESSAGE);
                 return;
             }
 
             Document doc = UpdateHelper.instance().getDocument(file);
             ArrayList<HPPluginInfo> tmp = new ArrayList<HPPluginInfo>();
             ArrayList<HPPluginInfo> nonVisibles = new ArrayList<HPPluginInfo>();
             ArrayList<HPPluginInfo> list = UpdateHelper.instance().getWebPlugins(doc.getDocumentElement()
                                                                       .getChildNodes(),
                                                                    new ArrayList<HPPluginInfo>(), tmp);
 
             nonVisibles = list;
             list = tmp;
 
             Vector<IPlugin> v = HelperWrapper.instance().getPlugins();
             int listSize = list.size();
 
             for (int i = 0; i < listSize; i++) {
                 HPPluginInfo hpp = (HPPluginInfo) list.get(i);
 
                 for (Iterator<IPlugin> iter = v.iterator(); iter.hasNext();) {
                     IPlugin element = iter.next();
 
                     if (element instanceof IOfficialPlugin
                         && (hpp.getPluginId() == ((IOfficialPlugin) element).getPluginID())) {
                         hpp.setOfficialPlugin((IOfficialPlugin) element);
                     }
                      // if
                 }
                  // for iter
             }
              // for list
 
             RefreshDialog dialog = new RefreshDialog(list);
             dialog.setOtherPlugins(nonVisibles);
             dialog.setVisible(true);
         } catch (Exception e1) {
             HOLogger.instance().log(UpdateController.class,e1);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void showPluginUpdaterNormal() {
         try {
             File file = createXMLFile(WEB_PLUGINFILE, getLocalXMLFile());
 
             if (file == null) {
                 JOptionPane.showMessageDialog(null, "Where is my xml file?", "Error",
                                               JOptionPane.ERROR_MESSAGE);
                 return;
             }
 
             Document doc = UpdateHelper.instance().getDocument(file);
             ArrayList<HPPluginInfo> tmp = new ArrayList<HPPluginInfo>();
             ArrayList<HPPluginInfo> nonVisibles = new ArrayList<HPPluginInfo>();
 
             ArrayList<HPPluginInfo> list = UpdateHelper.instance().getWebPlugins(doc.getDocumentElement()
                                                                       .getChildNodes(),
                                                                    new ArrayList<HPPluginInfo>(), tmp);
 
             nonVisibles = tmp;
 
             Vector<IPlugin> v = HelperWrapper.instance().getPlugins();
             int listSize = list.size();
 
             for (int i = 0; i < listSize; i++) {
                 HPPluginInfo hpp = list.get(i);
 
                 for (Iterator<IPlugin> iter = v.iterator(); iter.hasNext();) {
                     IPlugin element = iter.next();
 
                     if (element instanceof IOfficialPlugin
                         && (hpp.getPluginId() == ((IOfficialPlugin) element).getPluginID())) {
                         hpp.setOfficialPlugin((IOfficialPlugin) element);
                     }
                      // if
                 }
                  // for iter
             }
              // for list
 
             RefreshDialog dialog = new RefreshDialog(list);
             dialog.setOtherPlugins(nonVisibles);
             dialog.setVisible(true);
         } catch (Exception e1) {
             HOLogger.instance().log(UpdateController.class,e1);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void updateFlags() {
         try {
             UpdateHelper.instance().download(WEB_FLAGSFILE, getLocalZipFile());
             ZipHelper zip = new ZipHelper(getLocalZipFile());
             zip.unzip(System.getProperty("user.dir"));
             JOptionPane.showMessageDialog(null,
                                           HOVerwaltung.instance().getLanguageString("NeustartErforderlich"),
                                           "HO!", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e1) {
             HOLogger.instance().log(UpdateController.class,e1);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      *
      * @throws IOException TODO Missing Method Exception Documentation
      */
     protected static File getLocalZipFile() throws IOException {
         if (zip == null) {
             zip = File.createTempFile("tmp", "zip");
         }
 
         return zip;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      *
      * @throws IOException TODO Missing Method Exception Documentation
      */
     private static File getLocalXMLFile() throws IOException {
         if (tmp == null) {
             tmp = File.createTempFile("tmp", "xml");
         }
 
         return tmp;
     }
 
     /**
      * analyse the /sprache/languages.xml file and creates a hashtable
      *
      * @param elements TODO Missing Constructuor Parameter Documentation
      * @param list TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private static Hashtable<String,HPLanguageInfo> getWebLanguages(NodeList elements, Hashtable<String,HPLanguageInfo> list) {
         HPLanguageInfo tmp = null;
         Element element = null;
 
         for (int i = 0; i < elements.getLength(); i++) {
             if (elements.item(i) instanceof Element) {
                 element = (Element) elements.item(i);
 
                 if (element.getTagName().equals("property")) {
                     tmp = HPLanguageInfo.instance(element.getChildNodes());
                     list.put(tmp.getFilename(), tmp);
                 }
             }
         }
 
         return list;
     }
 
     /**
      * Download the xml file from Web and save it local
      *
      * @param url
      * @param tmp
      *
      * @return xml File
      *
      * @throws Exception
      */
     private static File createXMLFile(String url, File tmp) throws Exception {
         boolean showDialog = false;
         String content = "";
 
         try {
 //            if ((UserParameter.instance().LoginName == null)
 //                || (UserParameter.instance().LoginName.length() == 0)) {
 //                showDialog = true;
 //            }
 
             content = MyConnector.instance().getUsalWebPage(url, showDialog);
         } catch (Exception ex) {
             if (tmp.exists()) {
                 return tmp;
             }
 
             return null;
         }
 
         if (tmp.exists()) {
             tmp.delete();
         }
 
         FileWriter writer = new FileWriter(tmp);
         writer.write(content);
         writer.flush();
         writer.close();
 
         return tmp;
     }
 
 	public static void check4update() {
 		double version = MyConnector.instance().getLatestVersion();
 		if (version > HOMainFrame.VERSION) {
 			// Info anzeigen, dass es ein Update gibt
 			// Show update info
 			int update =
 				JOptionPane.showConfirmDialog(
 					HOMainFrame.instance(),
 					HOVerwaltung.instance().getLanguageString("updateMSG") + "\n"
 						+ HOVerwaltung.instance().getLanguageString("update") + "?",
 					HOVerwaltung.instance().getLanguageString("update")+"?",
 					JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateHO(version);
 			}
 //			int update = JOptionPane.showConfirmDialog(
 //					HOMainFrame.instance(),
 //					"" + HOVerwaltung.instance().getLanguageString("updateMSG"),
 //					"Open page?",
 //					JOptionPane.YES_NO_OPTION);
 //			if (update == JOptionPane.YES_OPTION) {
 //				HelperWrapper.instance().openUrlInUserBRowser("http://sourceforge.net/project/showfiles.php?group_id=167702");
 //			}
 		}
 	}
 
 	public static void updateHO(double version) {
 		String ver = "" + version;
 		ver = ver.replaceAll("\\.", "");
 		updateHO("http://downloads.sourceforge.net/ho1/HO_" + ver + ".zip");
 	}
 	
 	public static void updateHO(final String urlString) {
 		File tmp = new File("update.zip");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		HOLogger.instance().debug(UpdateController.class, "Try to download: " + urlString);
 		if (!UpdateHelper.instance().download(urlString, tmp)) {
 			wait.setVisible(false);
 			return;
 		}
 		wait.setVisible(false);
 		ZipHelper zip = null;
 		try {
 			zip = new ZipHelper("update.zip");
 			String dir = System.getProperty("user.dir");
 			zip.extractFile("HO.bat", dir);
 			zip.extractFile("HO.sh", dir);
 			zip.extractFile("HOLauncher.class", dir);
 		} catch (Exception e) {
 			HOLogger.instance().log(UpdateController.class, e);
 			return;
 		} finally {
 			if (zip != null) zip.close();
 		}
 		JOptionPane.showMessageDialog(null, HOVerwaltung.instance().getLanguageString("NeustartErforderlich"),
 				"", JOptionPane.INFORMATION_MESSAGE);
 
 		HOMainFrame.instance().beenden();
 	}
 	
 	/**
 	 * 
 	 * TODO: i18n
 	 */
 	public static void check4latestbeta() {
 		final VersionInfo vi = MyConnector.instance().getLatestBetaVersion();
		if (vi != null && vi.isValid() && vi.getVersion() >= HOMainFrame.VERSION) {
 			int update =
 				JOptionPane.showConfirmDialog(HOMainFrame.instance(),
 					"Update your HO to this "+(vi.isBeta()?" beta":"")+"version:"
 						+ "\n\nVersion: " + vi.getVersionString()
 						+ "\nReleased: " + vi.getReleaseDate()
 						+ "\n\n"
 						+ HOVerwaltung.instance().getLanguageString("update") + "?",
 					HOVerwaltung.instance().getLanguageString("update")+"?",
 					JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateHO("http://downloads.sourceforge.net/ho1/" + vi.getZipFileName());
 			}
 		}
 	}
 
 	public static void check4EPVUpdate() {
 		Extension data = MyConnector.instance().getEpvVersion();
 		if (HOMainFrame.VERSION >= data.getMinimumHOVersion() && data.getRelease()>HOParameter.instance().EpvRelease) {
 			// Info anzeigen, dass es ein Update gibt
 			// Show update info
 			int update =
 				JOptionPane.showConfirmDialog(
 					HOMainFrame.instance(),
 					HOVerwaltung.instance().getLanguageString("updateFile"),
 					HOVerwaltung.instance().getLanguageString("update")+"?",
 					JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateEPV(data.getRelease());
 			}
 		}
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	public static void updateUsers() {
 		try {
 			final String s =
 				MyConnector.instance().getWebPage(MyConnector.getResourceSite()+"/downloads/users.html", false, true);
 
 			final Properties pr = new Properties();
 			pr.load(new ByteArrayInputStream(s.getBytes()));
 
 			// HO Users Update
 			try {
 				int actual = Integer.parseInt(pr.getProperty("users"));
 				int total = Integer.parseInt(pr.getProperty("totusers"));
 				HOParameter.instance().HOUsers = actual;
 				HOParameter.instance().HOTotalUsers = total;
 				de.hattrickorganizer.gui.HOMainFrame.instance().getInfoPanel().setUserInfo(
 					actual,
 					total);
 			} catch (RuntimeException e1) {
 				HOLogger.instance().log(UpdateController.class, "Error updating users: " + e1);
 			}
 
 		} catch (Exception e) {
 			HOLogger.instance().log(UpdateController.class,"Kein Connect zum update: " + e);
 		}
 	}
 
 	public static void updateEPV(float release) {
 		File tmp = new File("tmp.dat");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		if (!UpdateHelper.instance().download(
 				MyConnector.getResourceSite()+"/downloads/epvWeights.mlp", tmp)) {
 			wait.setVisible(false);
 			tmp.delete();
 			return;
 		}
 		File target = new File("prediction/epvWeights.mlp");
 		target.delete();
 		tmp.renameTo(target);
 		HOParameter.instance().EpvRelease = release;
 		wait.setVisible(false);
 		JOptionPane.showMessageDialog(null, HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "",
 									  JOptionPane.INFORMATION_MESSAGE);
 
 	}
 
 	public static void check4RatingsUpdate() {
 		Extension data = MyConnector.instance().getRatingsVersion();
 		HOLogger.instance().log(UpdateController.class, "Check: " + HOMainFrame.VERSION + ">=" + (data != null ? data.getMinimumHOVersion() : -1f) + " && " + (data != null ? data.getRelease(): -1f) +" > " + HOParameter.instance().RatingsRelease);
 		if (HOMainFrame.VERSION >= data.getMinimumHOVersion() && data.getRelease()>HOParameter.instance().RatingsRelease) {
 			//Infro anzeigen das es ein Update gibt
 			int update =
 				JOptionPane.showConfirmDialog(
 					HOMainFrame.instance(),
 					HOVerwaltung.instance().getLanguageString("updateFile"),
 					HOVerwaltung.instance().getLanguageString("update")+"?",
 					JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateRatings(data.getRelease());
 			}
 		}
 	}
 
 	public static void updateRatings(float release) {
 		File tmp = new File("tmp.dat");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		if (!UpdateHelper.instance().download(
 				MyConnector.getResourceSite()+"/downloads/prediction.zip", tmp)) {
 			wait.setVisible(false);
 			tmp.delete();
 			return;
 		}
 		try {
 	        ZipHelper zip = new ZipHelper(tmp);
 	        HOLogger.instance().log(UpdateController.class, "Unzip " + tmp + " to: " + (System.getProperty("user.dir") + File.separator + "prediction"));
 	        zip.unzip(System.getProperty("user.dir") + File.separator + "prediction");
 	        tmp.delete();
 //			File target = new File("ratings.dat");
 //			target.delete();
 //			tmp.renameTo(target);
 			HOParameter.instance().RatingsRelease = release;
 		} catch (Exception e) {
 			HOLogger.instance().log(UpdateController.class,"Rating update unzip: " + e);
 		}
 		wait.setVisible(false);
 		JOptionPane.showMessageDialog(null, HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "",
 									  JOptionPane.INFORMATION_MESSAGE);
 
 	}
 
 	public static void checkNews() {
 		News news = MyConnector.instance().getLatestNews();
 		if (news.getId() > HOParameter.instance().lastNews) {
 			if (HOMainFrame.VERSION >= news.getMinimumHOVersion()) {
 				HOParameter.instance().lastNews = news.getId();
 				switch (news.getType()) {
 					case News.HO :
 						{
 							if (!UserParameter.instance().updateCheck && news.getVersion()>HOMainFrame.VERSION) {
 								int update =
 									JOptionPane.showConfirmDialog(
 										HOMainFrame.instance(),
 										HOVerwaltung.instance().getLanguageString("updateMSG"),
 										HOVerwaltung.instance().getLanguageString("update") + "?",
 										JOptionPane.YES_NO_OPTION);
 								if (update == JOptionPane.YES_OPTION) {
 									UpdateController.updateHO(news.getVersion());
 								}
 //								int update = JOptionPane.showConfirmDialog(
 //										HOMainFrame.instance(),
 //										"" + HOVerwaltung.instance().getLanguageString("updateMSG"),
 //										"Open page?",
 //										JOptionPane.YES_NO_OPTION);
 //								if (update == JOptionPane.YES_OPTION) {
 //									HelperWrapper.instance().openUrlInUserBRowser("http://sourceforge.net/project/showfiles.php?group_id=167702");
 //								}
 
 							}
 							break;
 						}
 					case News.EPV :
 						{
 							if (news.getVersion() > HOParameter.instance().EpvRelease) {
 								int update =
 									JOptionPane.showConfirmDialog(
 										HOMainFrame.instance(),
 										news.getMessages().get(0),
 										HOVerwaltung.instance().getLanguageString("update") + "?",
 										JOptionPane.YES_NO_OPTION);
 								if (update == JOptionPane.YES_OPTION) {
 									UpdateController.updateEPV(news.getVersion());
 								}
 							}
 							break;
 						}
 
 					case News.RATINGS :
 						{
 							if (news.getVersion() > HOParameter.instance().RatingsRelease) {
 
 								int update =
 									JOptionPane.showConfirmDialog(
 										HOMainFrame.instance(),
 										news.getMessages().get(0),
 										HOVerwaltung.instance().getLanguageString("update") + "?",
 										JOptionPane.YES_NO_OPTION);
 								if (update == JOptionPane.YES_OPTION) {
 
 									UpdateController.updateRatings(news.getVersion());
 								}
 							}
 							break;
 						}
 
 					case News.PLUGIN :
 						{
 							JOptionPane.showMessageDialog(
 								HOMainFrame.instance().getOwner(),
 								new NewsPanel(news),
 								"Plugin News",
 								JOptionPane.INFORMATION_MESSAGE);
 							break;
 						}
 					case News.MESSAGE :
 						{
 							JOptionPane.showMessageDialog(
 								HOMainFrame.instance().getOwner(),
 								new NewsPanel(news),
 								"HO News",
 								JOptionPane.INFORMATION_MESSAGE);
 							break;
 						}
 					default :
 						{
 							// Unsupported Message Type
 						}
 				}
 
 			}
 		}
 	}
 
 }
