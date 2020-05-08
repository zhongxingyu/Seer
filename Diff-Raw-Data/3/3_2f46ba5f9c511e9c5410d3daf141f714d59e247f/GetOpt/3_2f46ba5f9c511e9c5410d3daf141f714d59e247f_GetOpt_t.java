 package com.thinkminimo.getopt;
 
 import java.io.*;
 import java.util.*;
 import gnu.getopt.LongOpt;
 
 public class GetOpt {
 
   private ArrayList<LongOpt>      mLongOpts     = new ArrayList<LongOpt>();
   private ArrayList<String>       mDescs        = new ArrayList<String>();
 
   private ArrayList<String>       mNotOpts      = new ArrayList<String>();
   private ArrayList<String>       mNotOptDescs  = new ArrayList<String>();
 
   private ArrayList<String>       mNotOptExtra  = new ArrayList<String>();
 
   private HashMap<String, String> mOpts         = new HashMap<String, String>();
 
   private String                  mAppname      = "";
   private String[]                mArgvIn       = null;
   private File                    mFile         = null;
 
   private String                  mVarArg       = null;
   private String                  mVarArgDesc   = null;
 
   public GetOpt(String[] argv) {
     this("getopt", argv);
   }
 
   public GetOpt(String name, String[] argv) {
     mAppname = name;
     mArgvIn  = argv;
 
     mLongOpts.add(new LongOpt("help", LongOpt.NO_ARGUMENT,  null, 4));
     mDescs.add("Print this help info and exit.");
 
     mLongOpts.add(new LongOpt("showconfig", LongOpt.NO_ARGUMENT,  null, 3));
     mDescs.add("Print the saved configuration for this application and exit.");
 
     Runtime.getRuntime().addShutdownHook(new Thread() {
       public void run() {
         save();
       }
     });
   }
 
   public GetOpt addOpt(String lng) {
     return addOpt(lng, null);
   }
 
   public GetOpt addOpt(String lng, String desc) {
     mLongOpts.add(
         new LongOpt(lng, LongOpt.REQUIRED_ARGUMENT,  null, 1));
     mDescs.add(desc == null ? "" : desc);
     return this;
   }
 
   public GetOpt addFlag(String lng) {
     return addFlag(lng, null);
   }
 
   public GetOpt addFlag(String lng, String desc) {
     mLongOpts.add(new LongOpt(lng, LongOpt.NO_ARGUMENT,  null, 2));
     mDescs.add(desc == null ? "" : desc);
     return this;
   }
 
   public GetOpt addArg(String lng) {
     return addArg(lng, null);
   }
 
   public GetOpt addArg(String lng, String desc) {
     mNotOpts.add(lng);
     mNotOptDescs.add(desc == null ? "" : desc);
     return this;
   }
 
   public GetOpt addVarArg(String lng) {
     return addVarArg(lng, null);
   }
 
   public GetOpt addVarArg(String lng, String desc) {
     mVarArg     = lng;
     mVarArgDesc = desc;
     return this;
   }
 
   public GetOpt setFile(File file) {
     mFile = file;
     return this;
   }
 
   private void load() {
     if (mFile == null)
       return;
 
     try {
       BufferedReader in = new BufferedReader(new FileReader(mFile));
       String line;
       for (LongOpt l : mLongOpts)
         if ((line = in.readLine()) != null && 
             l.getHasArg() != LongOpt.NO_ARGUMENT)
           setOpt(l.getName(), line);
       in.close();
     } catch (Exception e) {
       System.err.printf("can't load configuration: %s: %s\n",
           mFile.getAbsolutePath(), e.getMessage());
     }
   }
 
   private void save() {
     if (mFile == null)
       return;
 
     try {
       PrintWriter out = new PrintWriter(mFile);
       for (LongOpt l : mLongOpts)
         out.println(getOpt(l.getName()));
       out.close();
     } catch (Exception e) {}
   }
 
   public GetOpt go() throws Exception {
     load();
     LongOpt[] l = mLongOpts.toArray(new LongOpt[mLongOpts.size()]);
     gnu.getopt.Getopt g = new gnu.getopt.Getopt(mAppname, mArgvIn, "", l);
     int c;
     while ((c = g.getopt()) != -1) {
       switch (c) {
         case '?':
         case ':':
           printUsage();
           throw new Exception();
         case 1:
           mOpts.put(mLongOpts.get(g.getLongind()).getName(), g.getOptarg());
           break;
         case 2:
           mOpts.put(mLongOpts.get(g.getLongind()).getName(), "true");
           break;
         case 3:
           showConfig();
           break;
         case 4:
           printUsage();
           System.exit(1);
       }
     }
     for (int i=g.getOptind(); i<mArgvIn.length; i++) {
       String name;
      if (mNotOpts.size() > i-g.getOptind() 
          && (name = mNotOpts.get(i-g.getOptind())) != null) {
         setOpt(mNotOpts.get(i-g.getOptind()), mArgvIn[i]);
       } else {
         if (mVarArg == null) {
           System.err.println(mAppname+": unexpected argument: "+mArgvIn[i]);
           printUsage();
           throw new Exception();
         }
         mNotOptExtra.add(mArgvIn[i]);
       }
     }
     for (int i=0; i<mNotOpts.size(); i++) {
       if (getOpt(mNotOpts.get(i)) == null) {
         System.err.println(mAppname+": missing argument: "+mNotOpts.get(i));
         throw new Exception();
       }
     }
     return this;
   }
 
   public String getOpt(String opt) {
     return mOpts.get(opt);
   }
 
   public GetOpt setOpt(String opt, String val) {
     mOpts.put(opt, val);
     return this;
   }
 
   public boolean getFlag(String opt) {
     return Boolean.valueOf(mOpts.get(opt));
   }
 
   public GetOpt setFlag(String opt, boolean val) {
     mOpts.put(opt, (new Boolean(val)).toString());
     return this;
   }
 
   public ArrayList<String> getExtra() {
     return mNotOptExtra;
   }
 
   public void showConfig() {
     for (LongOpt l : mLongOpts)
       if (l.getHasArg() != LongOpt.NO_ARGUMENT || getOpt(l.getName()) != null)
         System.out.printf("%15s '%s'\n", "--"+l.getName(), 
             l.getHasArg() == LongOpt.NO_ARGUMENT 
             ? Boolean.valueOf(getFlag(l.getName())).toString()
             : getOpt(l.getName()));
     System.exit(0);
   }
 
   public void printUsage() {
     String usage = "Usage: java -jar "+mAppname+".jar";
     
     if (mLongOpts.size() > 0)
       usage += " [OPTIONS]";
 
     if (mNotOpts.size() > 0)
       for (String s : mNotOpts)
         usage += " <"+s+">";
 
     if (mVarArg != null)
       usage += " ["+mVarArg+"]";
 
     System.out.println(usage);
 
     if (mNotOpts.size() > 0) {
       System.out.print("\n");
       System.out.println("ARGUMENTS:");
       System.out.print("\n");
 
       for (int i = 0; i < mNotOpts.size(); i++) {
         String        name    = mNotOpts.get(i);
         String        desc    = mNotOptDescs.get(i);
 
         System.out.printf("    %s\n", name);
         para(desc, 65, "        ");
         System.out.printf("\n");
       }
 
       if (mVarArg != null) {
         String        name    = mVarArg;
         String        desc    = mVarArgDesc;
 
         System.out.printf("    %s\n", name);
         para(desc, 65, "        ");
         System.out.printf("\n");
       }
     }
 
     if (mLongOpts.size() > 0) {
       System.out.println("OPTIONS:");
       System.out.print("\n");
 
       for (int i = 0; i < mLongOpts.size(); i++) {
         LongOpt       o       = mLongOpts.get(i);
         String        name    = o.getName();
         int           flag    = o.getVal();
         String        desc    = mDescs.get(i);
         int           harg    = o.getHasArg();
 
         String arg = "";
         if (harg == LongOpt.REQUIRED_ARGUMENT)
           arg = " <arg>";
         else if (harg == LongOpt.OPTIONAL_ARGUMENT)
           arg = " [arg]";
 
         System.out.printf("    --%s%s\n", name, arg);
         para(desc, 65, "        ");
         System.out.printf("\n");
       }
     }
   }
 
   public static void para(String text, int w) {
     para(text, w, "");
   }
 
   public static void para(String text, int w, String pre) {
     while (text.length() > 0) {
       String tmp = (text.length() < w) ? text : 
         text.substring(0, w).replaceFirst("[^ \t\n]*$", "");
       text = text.substring(tmp.length());
       System.out.println(pre + tmp);
     }
   }
 }
