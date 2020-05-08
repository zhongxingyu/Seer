 /**
  * Copyright 2013 Tommi S.E. Laukkanen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.eelis.translation;
 
 import biz.eelis.translation.model.Entry;
 import org.apache.log4j.Logger;
 import org.vaadin.addons.sitekit.dao.CompanyDao;
 import org.vaadin.addons.sitekit.dao.UserDao;
 import org.vaadin.addons.sitekit.model.Company;
 import org.vaadin.addons.sitekit.model.Group;
 import org.vaadin.addons.sitekit.model.User;
 import org.vaadin.addons.sitekit.util.EmailUtil;
 import org.vaadin.addons.sitekit.util.PropertiesUtil;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Class which synchronizes bundles to database and back.
  *
  * @author Tommi S.E. Laukkanen
  */
 public class TranslationSynchronizer {
     /** The logger. */
     private static final Logger LOGGER = Logger.getLogger(TranslationSynchronizer.class);
 
     /**
      * The entity manager.
      */
     private final EntityManager entityManager;
     /**
      * Synchronization thread.
      */
     private final Thread thread;
     /**
      * Shutdown requested.
      */
     private boolean shutdown = false;
 
     /**
      * Constructor which starts synchronizer.
      *
      * @param entityManager the entity manager.
      */
 
     public TranslationSynchronizer(final EntityManager entityManager) {
         this.entityManager = entityManager;
 
         thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 while (!shutdown) {
                     synchronize();
                     try {
                         Thread.sleep(60000);
                     } catch (final InterruptedException e) {
                         LOGGER.debug(e);
                     }
                 }
             }
         });
         thread.start();
 
     }
 
     /**
      * Synchronizes bundles and database.
      */
     private void synchronize() {
         entityManager.clear();
 
         final String bundleCharacterSet = PropertiesUtil.getProperty("translation-site", "bundle-character-set");
         final String[] prefixes = PropertiesUtil.getProperty("translation-site", "bundle-path-prefixes").split(",");
 
         for (final String prefixPart : prefixes) {
             final String prefix = prefixPart.split(":")[1];
             final String host = prefixPart.split(":")[0];
             final Company company = CompanyDao.getCompany(entityManager, host);
             final File baseBundle = new File(prefix + ".properties");
             if (!baseBundle.exists()) {
                 LOGGER.info("Base bundle does not exist: " + baseBundle.getAbsolutePath());
                 continue;
             }
 
             LOGGER.info("Base bundle exists: " + baseBundle.getAbsolutePath());
             String baseName = baseBundle.getName().substring(0, baseBundle.getName().length() - 11);
             if (baseName.indexOf('_') >= 0) {
                 baseName = baseName.substring(0, baseName.indexOf('_'));
             }
 
             final File bundleDirectory = baseBundle.getParentFile();
             final String bundleDirectoryPath = bundleDirectory.getAbsolutePath();
 
             LOGGER.info("Basename: " + baseName);
             LOGGER.info("Path: " + bundleDirectoryPath);
 
             final Set<Object> keys;
             final Properties baseBundleProperties;
             try {
                 baseBundleProperties = new Properties();
                 final FileInputStream baseBundleInputStream = new FileInputStream(baseBundle);
                 baseBundleProperties.load(new InputStreamReader(baseBundleInputStream, bundleCharacterSet));
                 keys = baseBundleProperties.keySet();
                 baseBundleInputStream.close();
             } catch (Exception e) {
                 LOGGER.error("Error reading bundle: " + baseName, e);
                 continue;
             }
 
             final Map<String, List<String>> missingKeys = new HashMap<String, List<String>>();
             final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
 
             for (final File candidate : bundleDirectory.listFiles()) {
                 if (candidate.getName().startsWith(baseName) && candidate.getName().endsWith(".properties")) {
 
                     final String name = candidate.getName().split("\\.")[0];
                     final String[] parts = name.split("_");
 
                     String candidateBaseName = parts[0];
                     if (candidateBaseName.equals(baseName)) {
                         String language = "";
                         String country = "";
                         if (parts.length > 1) {
                             language = parts [1];
                             if (parts.length > 2) {
                                 country = parts[2];
                             }
                         }
 
                         LOGGER.info("Bundle basename: '" + candidateBaseName
                                 + "' language: '" + language + "' country: '" + country + "'");
 
                         entityManager.getTransaction().begin();
                         try {
                             final Properties properties = new Properties();
                             final FileInputStream bundleInputStream = new FileInputStream(candidate);
                             properties.load(new InputStreamReader(bundleInputStream, bundleCharacterSet));
                             bundleInputStream.close();
 
                             final TypedQuery<Entry> query = entityManager.createQuery("select e from Entry as e where " +
                                     "e.path=:path and e.basename=:basename and " +
                                     "e.language=:language and e.country=:country order by e.key", Entry.class);
                             query.setParameter("path", bundleDirectoryPath);
                             query.setParameter("basename", baseName);
                             query.setParameter("language", language);
                             query.setParameter("country", country);
                             final List<Entry> entries = query.getResultList();
                             final Set<String> existingKeys = new HashSet<String>();
 
                             for (final Entry entry : entries) {
                                 if (keys.contains(entry.getKey())) {
                                     if (candidate.equals(baseBundle) || (entry.getValue().length() == 0 && properties.containsKey(entry.getKey()) &&
                                             ((String) properties.get(entry.getKey())).length() > 0)) {
                                         entry.setValue((String) properties.get(entry.getKey()));
                                         entityManager.persist(entry);
                                     }
 
                                 }
                                 existingKeys.add(entry.getKey());
                             }
 
                             for (final Object obj : keys) {
                                 final String key = (String) obj;
 
                                 final String value;
                                if (properties.containsKey(properties)) {
                                     value = (String) properties.get(key);
                                 } else {
                                     value = "";
                                 }
 
                                 if (!existingKeys.contains(key)) {
                                     final Entry entry = new Entry();
                                     entry.setOwner(company);
                                     entry.setPath(bundleDirectoryPath);
                                     entry.setBasename(baseName);
                                     entry.setLanguage(language);
                                     entry.setCountry(country);
                                     entry.setKey(key);
                                     entry.setValue(value);
                                     entry.setCreated(new Date());
                                     entry.setModified(entry.getCreated());
                                     entityManager.persist(entry);
 
                                     final String locale = entry.getLanguage() + "_" + entry.getCountry();
 
                                     if (!missingKeys.containsKey(locale)) {
                                         missingKeys.put(locale, new ArrayList<String>());
                                     }
 
                                     missingKeys.get(locale).add(entry.getKey());
                                 }
 
                             }
                             entityManager.getTransaction().commit();
 
                             if (!candidate.equals(baseBundle)) {
                                /*properties.clear();
                                for (final Entry entry : entries) {
                                    if (keys.contains(entry.getKey())) {
                                        if (entry.getValue().length() > 0) {
                                            properties.put(entry.getKey(), entry.getValue());
                                        }
                                    }
                                }
 
                                 final FileOutputStream fileOutputStream = new FileOutputStream(candidate, false);
                                properties.store(new OutputStreamWriter(fileOutputStream, bundleCharacterSet), "");
                                fileOutputStream.close();*/
                                final FileOutputStream fileOutputStream = new FileOutputStream(candidate, false);
                                 final OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream,
                                         bundleCharacterSet);
                                 final PrintWriter printWriter = new PrintWriter(writer);
 
                                 for (final Entry entry : query.getResultList()) {
                                     printWriter.print("# Modified: ");
                                     printWriter.print(format.format(entry.getModified()));
                                     if (entry.getAuthor() != null) {
                                         printWriter.print(" Author: ");
                                         printWriter.print(entry.getAuthor());
                                     }
                                     printWriter.println();
                                     printWriter.print(entry.getKey());
                                     printWriter.print("=");
                                     printWriter.println(entry.getValue());
                                 }
 
                                 printWriter.flush();
                                 printWriter.close();
                                 fileOutputStream.close();
 
 
                             }
                         } catch (Exception e) {
                             if (entityManager.getTransaction().isActive()) {
                                 entityManager.getTransaction().rollback();
                             }
                             LOGGER.error("Error reading bundle: " + baseName, e);
                             continue;
                         }
                     }
                 }
             }
 
             final String smtpHost = PropertiesUtil.getProperty("translation-site", "smtp-host");
             for (final String locale : missingKeys.keySet()) {
                 final List<String> keySet = missingKeys.get(locale);
 
                 final String subject = "Please translate " + locale;
                 String content = "Missing keys are: ";
                 for (final String key : keySet) {
                     content += key + "\n";
                 }
 
                 final Group group = UserDao.getGroup(entityManager, company, locale);
 
                 if (group != null) {
                     final List<User> users = UserDao.getGroupMembers(entityManager, company, group);
                     for (final User user : users) {
                         LOGGER.info("Sending translation request to " + user.getEmailAddress() + " for " + locale +
                                 " keys " + keySet);
                         EmailUtil.send(smtpHost,
                                 user.getEmailAddress(), company.getSupportEmailAddress(), subject, content);
                     }
                 }
             }
 
 
         }
 
     }
 
     /**
      * Shutdown.
      */
     public final void shutdown() {
         shutdown = true;
         try {
             thread.interrupt();
             thread.join();
         } catch (final InterruptedException e) {
             LOGGER.debug(e);
         }
     }
 
 }
