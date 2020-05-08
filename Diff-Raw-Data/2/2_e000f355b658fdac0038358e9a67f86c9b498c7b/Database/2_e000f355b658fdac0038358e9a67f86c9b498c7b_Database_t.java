 /*-
  * Copyright (c) 2012 Red Hat, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fedoraproject.javadeptools;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
 import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
 
 class Database {
 
   private static final int N_THREAD = Math.min(8 * Runtime.getRuntime()
           .availableProcessors(), 64);
   private static boolean OPTIMIZE_OUT_JRE = true;
   private static boolean OPTIMIZE_SUBSETS = true;
 
   // What classes each package contains?
   private final Map<String, Set<String>> cntmap;
   // What classes each class reference?
   private final Map<String, Set<String>> refmap;
 
   // What packages contain each class?
   final Map<String, Set<String>> revmap = new TreeMap<String, Set<String>>();
   // What packages each class depends on?
   final Map<String, List<Set<String>>> classdep = new TreeMap<String, List<Set<String>>>();
   // What packages each package depends on?
   final Map<String, Set<String>> pkgdep = new TreeMap<String, Set<String>>();
 
   static void read_directory(List<File> list, File dir) {
     if (!dir.isDirectory()) {
       if (dir.getName().endsWith(".rpm"))
         list.add(dir);
       else
         System.err.println("Skipping file " + dir + ": not a RPM file");
       return;
     }
 
     String[] subdirs = dir.list();
     if (subdirs == null) {
       System.err.println("Skipping directory " + dir + ": I/O exception");
       return;
     }
     for (String child : subdirs)
       read_directory(list, new File(dir, child));
   }
 
   private void addRpm(File rpm) {
     try {
       FedoraPackage pkg = new FedoraPackage(rpm);
       if (pkg.isJavaPackage())
         addPkg(pkg);
       else
         System.err.println("Skipping package " + pkg.getName()
                 + ": Not a Java package");
     } catch (IOException e) {
       System.err.println("Failed to sprocess RPM file " + rpm + ": " + e);
     } catch (InterruptedException e) {
       System.err.println("Failed to sprocess RPM file " + rpm + ": " + e);
     }
   }
 
   private void addPkg(FedoraPackage pkg) {
     final String pn = pkg.getName();
 
     final Set<String> cnt = new TreeSet<String>();
     for (JavaClass clazz : pkg.getClasses())
       cnt.add(clazz.getName());
 
     final Map<String, Set<String>> ref = new TreeMap<String, Set<String>>();
     for (JavaClass clazz : pkg.getClasses()) {
       String cn = clazz.getName();
       Set<String> refs = new TreeSet<String>();
       refs.addAll(clazz.getDependencies());
       ref.put(pn + "@" + cn, refs);
     }
 
     synchronized (refmap) {
       cntmap.put(pn, cnt);
       refmap.putAll(ref);
     }
   }
 
   public Database(File dir, int foo) {
     cntmap = new TreeMap<String, Set<String>>();
     refmap = new TreeMap<String, Set<String>>();
 
     System.err.println("Reading directory contents...");
     final List<File> rpm_list = new ArrayList<File>();
     read_directory(rpm_list, dir);
     if (rpm_list.isEmpty()) {
       System.err.println("No RPM files were found");
       return;
     }
     System.err.println(rpm_list.size() + " RPM files found.");
     Collections.sort(rpm_list);
 
     final int max = rpm_list.size();
 
     final List<FedoraPackage> pkg_list = new ArrayList<FedoraPackage>();
 
     Thread[] threads = new Thread[N_THREAD];
     for (int i = 0; i < N_THREAD; i++) {
       threads[i] = new Thread() {
         public void run() {
           List<FedoraPackage> my_pkg_list = new ArrayList<FedoraPackage>();
           for (;;) {
             File rpm;
             synchronized (rpm_list) {
               int left = rpm_list.size();
               if (left == 0) {
                 pkg_list.addAll(my_pkg_list);
                 return;
               }
               rpm = rpm_list.remove(0);
               System.err.println("Processing " + (max - left + 1) + "/" + max
                       + ": " + rpm);
             }
 
             addRpm(rpm);
           }
         }
       };
     }
 
     try {
       for (Thread t : threads)
         t.start();
       for (Thread t : threads)
         t.join();
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
 
     System.err.printf("\u250c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510%n");
     System.err.printf("\u2502 Total RPMs processed : %5d \u2502%n", max);
     System.err.printf("\u2502 Java packages found  : %5d \u2502%n", cntmap.size());
     System.err
             .printf("\u2502 Skipped packages     : %5d \u2502%n", max - cntmap.size());
     System.err.printf("\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518%n");
   }
 
   static void optimize_JRE(List<Set<String>> deps) {
     if (!OPTIMIZE_OUT_JRE)
       return;
 
     boolean needs_devel = false;
 
     for (int i = 0; i < deps.size(); i++) {
       if (deps.get(i).contains("java-1.7.0-openjdk"))
         deps.remove(i--);
       else if (deps.get(i).contains("java-1.7.0-openjdk-devel")) {
         deps.remove(i--);
         needs_devel = true;
       }
     }
 
     if (needs_devel)
       deps.add(Collections.singleton("java-devel"));
     else
       deps.add(Collections.singleton("java"));
     deps.add(Collections.singleton("jpackage-utils"));
   }
 
   static void optimize_subsets(List<Set<String>> deps) {
     if (!OPTIMIZE_SUBSETS)
       return;
     // Naive and slow, but who cares?
     again: for (;;) {
       for (Set<String> d1 : deps)
         for (Set<String> d2 : deps)
           if (d1 != d2 && d1.containsAll(d2)) {
             deps.remove(d2);
             continue again;
           }
       break;
     }
   }
 
   /**
    * Read database from given input stream
    * 
    * @param is
    *          the input stream to read database from
    * @throws IOException
    *           if I/O exception occurs when reading the database
    */
   @SuppressWarnings("unchecked")
   public Database(File f) throws IOException {
     System.err.print("Reading database...");
     try {
       InputStream is = new ProgressFileInputStream(f);
       InputStream zis = new XZCompressorInputStream(is);
       ObjectInputStream ois = new ObjectInputStream(zis);
       cntmap = (Map<String, Set<String>>) ois.readObject();
       refmap = (Map<String, Set<String>>) ois.readObject();
       ois.close();
     } catch (ClassNotFoundException e) {
       throw new IOException("invalid database format");
     }
 
     System.err.println("Number of indexed packages : " + cntmap.size());
     System.err.println("Number of indexed classes  : " + refmap.size());
   }
 
   /**
    * Write database to given output stream
    * 
    * @param os
    *          the output stream to write database to
    * @throws IOException
    * @throws FileNotFoundException
    * @throws UnsupportedOptionsException
    */
   public void write(OutputStream os) throws IOException {
     System.err.println("Writing database...");
     OutputStream zos = new XZCompressorOutputStream(os);
     ObjectOutputStream oos = new ObjectOutputStream(zos);
     oos.writeObject(cntmap);
     oos.writeObject(refmap);
     oos.close();
   }
 
   public void prepare() {
 
     System.err.println("Building dependency map...");
     for (String pn : cntmap.keySet()) {
       for (String cn : cntmap.get(pn)) {
         Set<String> prov = revmap.get(cn);
         if (prov == null) {
           prov = new TreeSet<String>();
           revmap.put(cn, prov);
         }
         prov.add(pn);
       }
     }
 
     for (String pn : cntmap.keySet()) {
       Set<String> cdeps = new TreeSet<String>();
       List<Set<String>> pdeps = new ArrayList<Set<String>>();
       classdep.put(pn, pdeps);
       for (String cn : cntmap.get(pn))
         cdeps.addAll(refmap.get(pn + "@" + cn));
       cdeps.removeAll(cntmap.get(pn));
       for (String cn : cdeps) {
         Set<String> deps = revmap.get(cn);
         if (deps == null) {
           pdeps.add(Collections.singleton("@unresolved@"
                   + cn.replaceAll("\\.[^\\.]+$", "")));
         } else {
           pdeps.add(deps);
         }
       }
     }
 
     for (String pn : cntmap.keySet()) {
       Set<String> pdeps = new TreeSet<String>();
       pkgdep.put(pn, pdeps);
 
       Map<String, Set<String>> uniq = new TreeMap<String, Set<String>>();
       for (Set<String> dep : classdep.get(pn))
         uniq.put(join(" | ", dep), dep);
 
       List<Set<String>> deps = new ArrayList<Set<String>>(uniq.values());
       optimize_JRE(deps);
       optimize_subsets(deps);
       while (!deps.isEmpty()) {
         Set<String> d1 = deps.remove(0);
         pdeps.add(join(" | ", d1));
       }
     }
 
     System.err.println("Executing query...");
   }
 
   private Set<String> filter_pkgs(String pattern) {
     String orig_pattern = pattern;
     pattern = pattern.replaceAll("\\.", "\\.");
     pattern = pattern.replaceAll("\\*", ".*");
     pattern = "^(" + pattern + ")$";
 
     Set<String> match_pkgs = new TreeSet<String>();
     for (String pn : cntmap.keySet()) {
       if (pn.matches(pattern))
         match_pkgs.add(pn);
     }
     if (match_pkgs.isEmpty()) {
       System.err.println("No match for: " + orig_pattern);
       System.exit(1);
     }
 
     return match_pkgs;
   }
 
   private static String join(String delim, Set<String> depx) {
     StringBuffer sb = new StringBuffer();
     for (String dep : depx) {
       if (sb.length() > 0)
         sb.append(delim);
       sb.append(dep);
     }
     return sb.toString();
   }
 
   private void print_result(final Map<String, Set<String>> result) {
     for (String key : result.keySet()) {
       System.out.println(key + ":");
       for (String val : result.get(key))
         System.out.println("\t" + val);
     }
   }
 
   public void query_provides(String pattern) {
     Set<String> packages = filter_pkgs(pattern);
 
     final Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
     for (String pn : packages)
       result.put(pn, cntmap.get(pn));
     print_result(result);
   }
 
   public void query_what_provides(String regex) {
     final Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
     Set<String> set = new TreeSet<String>();
     result.put(regex, set);
     regex = regex.replaceAll("\\.", "\\.");
     regex = regex.replaceAll("\\?", ".");
     regex = regex.replaceAll("\\*", ".*");
     for (String pn : cntmap.keySet())
       for (String cn : cntmap.get(pn))
         if (cn.matches("^(" + regex + ")$"))
           set.add(pn);
     print_result(result);
   }
 
   public void query_requires(String[] args) throws IOException,
           InterruptedException {
     Set<String> packages;
     packages = new TreeSet<String>();
     for (String fn : args) {
       if (fn.endsWith(".jar") || fn.endsWith(".rpm")) {
         File f = new File(fn);
         FedoraPackage pkg = new FedoraPackage(f);
         if (!pkg.isJavaPackage()) {
           System.err.println(pkg.getName() + ": not a Java package");
         }
         packages.add(pkg.getName());
         addPkg(pkg);
       } else
         packages.addAll(filter_pkgs(fn));
     }
     if (packages.isEmpty())
       System.exit(1);
 
     prepare();
     final Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
     for (String pn : packages)
       result.put(pn, pkgdep.get(pn));
     print_result(result);
   }
 
   public void query_why(String pattern, String dep_name) throws IOException,
           InterruptedException {
     if (dep_name.equals("java"))
       dep_name = "java-1.7.0-openjdk";
     if (dep_name.equals("java-devel"))
       dep_name = "java-1.7.0-openjdk-devel";
 
     Set<String> packages;
     if (pattern.endsWith(".jar") || pattern.endsWith(".rpm")) {
       File f = new File(pattern);
       FedoraPackage pkg = new FedoraPackage(f);
       if (!pkg.isJavaPackage()) {
         System.err.println("No Java classes found.");
         System.exit(1);
       }
       packages = Collections.singleton(pkg.getName());
       addPkg(pkg);
     } else
       packages = filter_pkgs(pattern);
 
     prepare();
     final Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
     for (String pn : packages) {
       Set<String> cdeps = new TreeSet<String>();
       for (String cn : cntmap.get(pn))
         cdeps.addAll(refmap.get(pn + "@" + cn));
       cdeps.removeAll(cntmap.get(pn));
       Set<String> pres = new TreeSet<String>();
       for (String cn : cdeps) {
         Set<String> deps = revmap.get(cn);
        if (deps != null && deps.contains(dep_name))
           pres.add(cn);
       }
       result.put(pn, pres);
     }
     print_result(result);
   }
 
   public void list_packages() {
     for (String s : cntmap.keySet())
       System.out.println(s);
   }
 }
