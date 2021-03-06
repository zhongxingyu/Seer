 package org.crosswire.jsword.book.install.sword;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import org.crosswire.common.progress.Job;
 import org.crosswire.common.progress.JobManager;
 import org.crosswire.common.util.IOUtil;
 import org.crosswire.common.util.Logger;
 import org.crosswire.common.util.NetUtil;
 import org.crosswire.common.util.Reporter;
 import org.crosswire.jsword.book.BookMetaData;
 import org.crosswire.jsword.book.Books;
 import org.crosswire.jsword.book.basic.AbstractBookList;
 import org.crosswire.jsword.book.install.InstallException;
 import org.crosswire.jsword.book.install.Installer;
 import org.crosswire.jsword.book.sword.ModuleType;
 import org.crosswire.jsword.book.sword.SwordBookDriver;
 import org.crosswire.jsword.book.sword.SwordBookMetaData;
 import org.crosswire.jsword.book.sword.SwordConstants;
 import org.crosswire.jsword.util.Project;
 
 import com.ice.tar.TarEntry;
 import com.ice.tar.TarInputStream;
 
 /**
  * .
  * 
  * <p><table border='1' cellPadding='3' cellSpacing='0'>
  * <tr><td bgColor='white' class='TableRowColor'><font size='-7'>
  *
  * Distribution Licence:<br />
  * JSword is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License,
  * version 2 as published by the Free Software Foundation.<br />
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.<br />
  * The License is available on the internet
  * <a href='http://www.gnu.org/copyleft/gpl.html'>here</a>, or by writing to:
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  * MA 02111-1307, USA<br />
  * The copyright to this program is held by it's authors.
  * </font></td></tr></table>
  * @see gnu.gpl.Licence
  * @author Joe Walker [joe at eireneh dot com]
  * @version $Id$
  */
 public abstract class AbstractSwordInstaller extends AbstractBookList implements Installer
 {
     /**
      * Utility to download a file from a remote site
      * @param job The way of noting progress
      * @param dir The directory from which to download the file
      * @param file The file to download
      * @throws InstallException
      */
     protected abstract void download(Job job, String dir, String file, URL dest) throws InstallException;
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.install.Installer#toLocalURL(org.crosswire.jsword.book.BookMetaData)
      */
     public URL toLocalURL(BookMetaData bmd)
     {
         File fulldir = toLocalDirectory(bmd);
 
         try
         {
             return new URL(NetUtil.PROTOCOL_FILE, null, fulldir.getAbsolutePath());
         }
         catch (MalformedURLException ex)
         {
             log.error("Failed to create URL for file: "+fulldir, ex); //$NON-NLS-1$
             assert false;
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.install.Installer#isNewer(org.crosswire.jsword.book.BookMetaData)
      */
     public boolean isNewer(BookMetaData bmd)
     {
         URL local = toLocalURL(bmd);
 
         SwordBookMetaData sbmd = (SwordBookMetaData) bmd;
         URL conf = NetUtil.lengthenURL(local, sbmd.getDiskName() + SwordConstants.EXTENSION_CONF);
 
         URL remote = toRemoteURL(bmd);
         return NetUtil.isNewer(remote, conf);
     }
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.BookList#getBookMetaDatas()
      */
     public List getBookMetaDatas()
     {
         try
         {
             if (!loaded)
             {
                 loadCachedIndex();
             }
 
             List mutable = new ArrayList();
             mutable.addAll(entries.values());
             return Collections.unmodifiableList(mutable);
         }
         catch (InstallException ex)
         {
             log.error("Failed to reload cached index file", ex); //$NON-NLS-1$
             return new ArrayList();
         }
     }
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.install.Installer#install(org.crosswire.jsword.book.BookMetaData)
      */
     public void install(BookMetaData bmd)
     {
         if (!(bmd instanceof SwordBookMetaData))
         {
             assert false;
             return;
         }
 
         // Is the book already installed? Then nothing to do.
         if (Books.installed().getBookMetaData(bmd.getName()) != null)
         {
             return;
         }
 
         final SwordBookMetaData sbmd = (SwordBookMetaData) bmd;
 
         // So now we know what we want to install - all we need to do
         // is installer.install(name) however we are doing it in the
         // background so we create a job for it.
         final Thread worker = new Thread("DisplayPreLoader") //$NON-NLS-1$
         {
             public void run()
             {
                 URL predicturl = Project.instance().getWritablePropertiesURL("sword-install"); //$NON-NLS-1$
                 Job job = JobManager.createJob(Msg.INSTALLING.toString(sbmd.getName()), predicturl, this, true);
 
                 yield();
 
                 try
                 {
                     job.setProgress(Msg.JOB_INIT.toString());
 
                     URL desturl = toLocalURL(sbmd);
                     NetUtil.makeDirectory(desturl);
 
                     URL temp = NetUtil.getTemporaryURL("swd", "zip"); //$NON-NLS-1$ //$NON-NLS-2$
 
                     download(job, directory + '/' + PACKAGE_DIR, sbmd.getInitials() + ZIP_SUFFIX, temp);
 
                     IOUtil.unpackZip(NetUtil.getAsFile(temp), desturl);
 
                     File dldir = SwordBookDriver.getDownloadDir();
                     job.setProgress(Msg.JOB_CONFIG.toString());
                     File confdir = new File(dldir, SwordConstants.DIR_CONF);
                     confdir.mkdirs();
                     File conf = new File(confdir, sbmd.getDiskName() + SwordConstants.EXTENSION_CONF);
                     URL configurl = new URL(NetUtil.PROTOCOL_FILE, null, conf.getAbsolutePath());
                     sbmd.save(configurl);
 
                     SwordBookDriver.registerNewBook(sbmd, dldir);
                 }
                 catch (Exception ex)
                 {
                     Reporter.informUser(this, ex);
                     job.ignoreTimings();
                 }
                 finally
                 {
                     job.done();
                 }
             }
         };
 
         // this actually starts the thread off
         worker.setPriority(Thread.MIN_PRIORITY);
         worker.start();
     }
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.install.Installer#reloadIndex()
      */
     public void reloadBookList() throws InstallException
     {
         Job job = JobManager.createJob(Msg.JOB_DOWNLOADING.toString(), Thread.currentThread(), false);
 
         try
         {
             URL scratchfile = getCachedIndexFile();
             download(job, LIST_DIR + '/' + directory, FILE_LIST_GZ, scratchfile);
             loaded = false;
         }
         catch (InstallException ex)
         {
             job.ignoreTimings();
             throw ex;
         }
         finally
         {
             job.done();
         }
     }
 
     /* (non-Javadoc)
      * @see org.crosswire.jsword.book.install.Installer#downloadSearchIndex(org.crosswire.jsword.book.BookMetaData, java.net.URL)
      */
     public void downloadSearchIndex(BookMetaData bmd, URL localDest) throws InstallException
     {
         Job job = JobManager.createJob(Msg.JOB_DOWNLOADING.toString(), Thread.currentThread(), false);
 
         try
         {
             download(job, directory + '/' + SEARCH_DIR, bmd.getInitials() + ZIP_SUFFIX, localDest);
         }
         catch (Exception ex)
         {
             job.ignoreTimings();
             throw new InstallException(Msg.UNKNOWN_ERROR, ex);
         }
         finally
         {
             job.done();
         }                
     }
 
     /**
      * Load the cached index file into memory
      */
     private void loadCachedIndex() throws InstallException
     {
         entries.clear();
 
         URL cache = getCachedIndexFile();
         if (!NetUtil.isFile(cache))
         {
             reloadBookList();
         }
 
         try
         {
             InputStream in = cache.openStream();
             GZIPInputStream gin = new GZIPInputStream(in);
             TarInputStream tin = new TarInputStream(gin);
 
             while (true)
             {
                 TarEntry entry = tin.getNextEntry();
                 if (entry == null)
                 {
                     break;
                 }
 
                 String internal = entry.getName();
                 if (!entry.isDirectory())
                 {
                     try
                     {
                         int size = (int) entry.getSize();
                         byte[] buffer = new byte[size];
                         tin.read(buffer);
 
                         if (internal.endsWith(SwordConstants.EXTENSION_CONF))
                         {
                             internal = internal.substring(0, internal.length() - 5);
                         }
 
                         if (internal.startsWith(SwordConstants.DIR_CONF + '/'))
                         {
                             internal = internal.substring(7);
                         }
 
                         Reader rin = new InputStreamReader(new ByteArrayInputStream(buffer));
                         SwordBookMetaData sbmd = new SwordBookMetaData(rin, internal);
 
                         if (sbmd.isSupported())
                         {
                             entries.put(sbmd.getName(), sbmd);
                         }
                     }
                     catch (Exception ex)
                     {
                         log.warn("Failed to load config for entry: " + internal, ex); //$NON-NLS-1$
                     }
                 }
             }
 
             IOUtil.close(tin);
             IOUtil.close(gin);
             IOUtil.close(in);
 
             loaded = true;
         }
         catch (IOException ex)
         {
             throw new InstallException(Msg.CACHE_ERROR, ex);
         }
     }
 
     /**
      * @return Returns the directory.
      */
     public String getDirectory()
     {
         return directory;
     }
 
     /**
      * @param directory The directory to set.
      */
     public void setDirectory(String directory)
     {
         this.directory = directory;
         loaded = false;
     }
 
     /**
      * @return Returns the host.
      */
     public String getHost()
     {
         return host;
     }
 
     /**
      * @param host The host to set.
      */
     public void setHost(String host)
     {
         this.host = host;
         loaded = false;
     }
 
     /**
      * The URL for the cached index file for this installer
      */
     protected URL getCachedIndexFile() throws InstallException
     {
         try
         {
             URL scratchdir = Project.instance().getTempScratchSpace(getTempFileExtension(host, directory), false);
             return NetUtil.lengthenURL(scratchdir, FILE_LIST_GZ);
         }
         catch (IOException ex)
         {
             throw new InstallException(Msg.URL_FAILED, ex);
         }
     }
 
     /**
      * What are we using as a temp filename?
      */
     private static String getTempFileExtension(String host, String directory)
     {
         return DOWNLOAD_PREFIX + host + directory.replace('/', '_'); //$NON-NLS-1$
     }
 
     /**
      * Get a local directory to which a book is installed
      */
     protected File toLocalDirectory(BookMetaData bmd)
     {
         if (!(bmd instanceof SwordBookMetaData))
         {
             assert false;
             return null;
         }
 
         SwordBookMetaData sbmd = (SwordBookMetaData) bmd;
 
         ModuleType type = sbmd.getModuleType();
         String modpath = type.getInstallDirectory();
         String destname = modpath + '/' + sbmd.getDiskName();
 
         File dldir = SwordBookDriver.getDownloadDir();
         File moddir = new File(dldir, SwordConstants.DIR_DATA);
         return new File(moddir, destname);
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object object)
     {
         if (!(object instanceof AbstractSwordInstaller))
         {
             return false;
         }
         AbstractSwordInstaller that = (AbstractSwordInstaller) object;
 
         if (!equals(this.host, that.host))
         {
             return false;
         }
 
         if (!equals(this.directory, that.directory))
         {
             return false;
         }
 
         return true;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     public int compareTo(Object arg0)
     {
         HttpSwordInstaller myClass = (HttpSwordInstaller) arg0;
 
         int ret = host.compareTo(myClass.host);
         if (ret != 0)
         {
             ret = directory.compareTo(myClass.directory);
         }
         return ret;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     public int hashCode()
     {
         return host.hashCode() + directory.hashCode();
     }
 
     /**
      * Quick utility to check to see if 2 (potentially null) strings are equal
      */
     protected boolean equals(String string1, String string2)
     {
         if (string1 == null)
         {
             return string2 == null;
         }
         return string1.equals(string2);
     }
 
     /**
      * The relative path of the dir holding the zip files
      */
     protected static final String PACKAGE_DIR = "packages/rawzip"; //$NON-NLS-1$
 
     /**
      * The relative path of the dir holding the index file
      */
     private static final String LIST_DIR = "raw"; //$NON-NLS-1$
 
     /**
      * A map of the entries in this download area
      */
     protected Map entries = new HashMap();
 
     /**
      * The remote hostname.
      */
     protected String host;
 
     /**
      * The directory containing modules on the <code>host</code>.
      */
     protected String directory = "/"; //$NON-NLS-1$
 
     /**
      * Do we need to reload the index file
      */
     protected boolean loaded;
 
     /**
      * The sword index file
      */
     protected static final String FILE_LIST_GZ = "mods.d.tar.gz"; //$NON-NLS-1$
 
     /**
      * The suffix of zip modules on this server
      */
     protected static final String ZIP_SUFFIX = ".zip"; //$NON-NLS-1$
 
     /**
      * The log stream
      */
     private static final Logger log = Logger.getLogger(AbstractSwordInstaller.class);
 
     /**
      * The relative path of the dir holding the search index files
      */
     protected static final String SEARCH_DIR = "seach/jsword/L1"; //$NON-NLS-1$
 
     /**
      * When we cache a download index
      */
     protected static final String DOWNLOAD_PREFIX = "download-"; //$NON-NLS-1$
 }
