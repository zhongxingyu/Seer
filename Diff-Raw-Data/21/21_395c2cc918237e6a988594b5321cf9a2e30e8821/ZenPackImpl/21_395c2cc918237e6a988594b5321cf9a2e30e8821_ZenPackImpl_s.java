 /*
  * Copyright (C) 2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.utils.impl;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.utils.ZenPack;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class ZenPackImpl implements ZenPack {
 
     private static final Logger logger = LoggerFactory.getLogger(ZenPackImpl.class);
     private static final Splitter DOT_SPLITTER = Splitter.on('.');
 
     private String _name;
     private String _rootPath;
     private String _version;
 
     public ZenPackImpl(String path) {
         _rootPath = path;
         findZenPackInfo();
     }
 
     /**
      * Get the name of the ZenPack. This is read from setup.py.
      *
      * @return The fully qualified dotted name of the ZenPack.
      */
     public String getName() {
         return _name;
     }
 
     /**
      * Return the path to the ZenPack root directory.
      *
      * @return An absolute path.
      */
     public String getPath() {
         return _rootPath;
     }
 
     @Override
     public String getVersion() {
         return _version;
     }
 
     @Override
     public String packPath(String... segments) {
         List<String> parts = new ArrayList<String>();
         parts.add(_rootPath);
         if (_name != null) {
             for (String s : DOT_SPLITTER.split(_name)) {
                 parts.add(s);
             }
         }
         parts.addAll(Arrays.asList(segments));
         return Joiner.on(File.separator).join(parts);
     }
 
     private void findZenPackInfo() {
         // First try the EGG-INFO/PKG-INFO file for ZenPacks installed from an .egg
         Map<String,String> eggInfo = parseEggInfo(new File(_rootPath, "EGG-INFO/PKG-INFO"));
         if (eggInfo.isEmpty()) {
             File[] eggInfoDirs = new File(_rootPath).listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File file) {
                     return file.isDirectory() && file.getName().toLowerCase().endsWith(".egg-info");
                 }
             });
             if (eggInfoDirs != null) {
                 for (File eggInfoDir : eggInfoDirs) {
                     eggInfo = parseEggInfo(new File(eggInfoDir, "PKG-INFO"));
                     if (!eggInfo.isEmpty()) {
                         break;
                     }
                 }
             }
         }
        logger.debug("Found egg-info for ZenPack {}: {}", this._rootPath, eggInfo);
        this._name = eggInfo.get("name");
        if (this._name == null) {
            logger.warn("Failed to find ZenPack name in: {}", this._rootPath);
         }
        this._version = eggInfo.get("version");
        if (this._version == null) {
            logger.warn("Failed to find ZenPack version in: {}", this._rootPath);
         }
     }
 
     private Map<String,String> parseEggInfo(File file) {
         if (!file.isFile()) {
             return Collections.emptyMap();
         }
 
         Map<String,String> values = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
         BufferedReader br = null;
         try {
             br = new BufferedReader(new FileReader(file));
             String line;
             while ((line = br.readLine()) != null) {
                 int colonIdx = line.indexOf(':');
                 if (colonIdx > 0) {
                     String name = line.substring(0, colonIdx).trim();
                     String value = line.substring(colonIdx + 1).trim();
                     values.put(name, value);
                 }
             }
         } catch (IOException e) {
             logger.warn("Failed to parse file: {}, exception: {}", file.getAbsolutePath(), e.getLocalizedMessage());
         } finally {
             if (br != null) {
                 try {
                     br.close();
                 } catch (IOException e) {
                     logger.debug("Failed closing file", e);
                 }
             }
         }
         return values;
     }
 
     @Override
     public List<String> getQueueConfigPaths() {
         List<String> paths = new ArrayList<String>();
         File protocolRoot = new File(packPath("protocols"));
         if (protocolRoot.isDirectory()) {
             File[] filesarray = protocolRoot.listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File file) {
                     return file.isFile() && file.getName().endsWith(".qjs");
                 }
             });
             for (File f : filesarray) {
                 paths.add(f.getAbsolutePath());
             }
         }
         return paths;
     }
 
 }
