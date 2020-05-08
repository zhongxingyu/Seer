 /*
  * Copyright (c) 2010 Ecole des Mines de Nantes.
  *
  *      This file is part of Entropy.
  *
  *      Entropy is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *
  *      Entropy is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package microDSN;
 
 import btrpsl.BtrPlaceVJobBuilder;
 import btrpsl.constraint.ConstraintsCatalog;
 import btrpsl.constraint.ConstraintsCatalogBuilderFromProperties;
 import btrpsl.includes.PathBasedIncludes;
 import choco.kernel.common.logging.ChocoLogging;
 import choco.kernel.common.logging.Verbosity;
 import entropy.PropertiesHelper;
 import entropy.configuration.Configuration;
 import entropy.configuration.ManagedElementSet;
 import entropy.configuration.SimpleManagedElementSet;
 import entropy.configuration.VirtualMachine;
 import entropy.configuration.parser.FileConfigurationSerializerFactory;
 import entropy.jobsManager.Job;
 import entropy.jobsManager.JobDispatcher;
 import entropy.jobsManager.JobHandler;
 import entropy.plan.Plan;
 import entropy.plan.choco.CustomizableSplitablePlannerModule;
 import entropy.plan.durationEvaluator.DurationEvaluator;
 import entropy.plan.durationEvaluator.FastDurationEvaluatorFactory;
 import entropy.plan.parser.PlainTextTimedReconfigurationPlanSerializer;
 import entropy.template.DefaultVirtualMachineTemplateFactory;
 import entropy.template.stub.StubVirtualMachineTemplate;
 import entropy.vjob.VJob;
 import entropy.vjob.builder.DefaultVJobElementBuilder;
 import entropy.vjob.builder.VJobElementBuilder;
 import gnu.trove.THashSet;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Fabien Hermenier
  */
 public class MicroBenchHandler {
 
     public static void main(String[] args) {
         ChocoLogging.setVerbosity(Verbosity.SILENT);
         ChocoLogging.setLoggingMaxDepth(3100);
         ChocoLogging.setEveryXNodes(100);
 
         String server = null;
 
         String configs = null;
         String vjobs = null;
 
         String props = null;
 
         String output = null;
         if (args.length == 0) {
             usage();
             System.exit(0);
         }
 
         boolean remote = false;
         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("-s")) {
                 server = args[++i];
                 remote = true;
             } else if (args[i].equals("-cfgs")) {
                 configs = args[++i];
             } else if (args[i].equals("-vjobs")) {
                 vjobs = args[++i];
             } else if (args[i].equals("-props")) {
                 props = args[++i];
             } else if (args[i].equals("-o")) {
                 output = args[++i];
             } else {
                 fatal("Unknown argument: " + args[i]);
             }
         }
 
         try {
             if (remote) {
                 benchFromServer(server);
             } else {
                 benchInstance(props, configs, vjobs, output);
             }
         } catch (Exception e) {
             e.printStackTrace();
             fatal(e.getMessage());
         }
     }
 
     private static void benchInstance(String propsF, String configsPath, String vPath, String output) throws Exception {
 
         if (propsF == null || !new File(propsF).isFile()) {
             fatal("the properties file must be an existing file. Currently: '" + propsF + "'");
         }
         PropertiesHelper props = new PropertiesHelper(propsF);
 
         List<Configuration[]> configs = new ArrayList<Configuration[]>();
 
         List<VJob> vjobs = null;
 
         if (!configsPath.contains(",")) {
             File base = new File(configsPath);
             for (File f : base.listFiles()) {
                 if (f.isFile()) {
                     Configuration src = null;
                     Configuration dst = null;
                     String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
 
                     String parent = f.getParent();
                     if (name.endsWith("-src")) {
                         src = FileConfigurationSerializerFactory.getInstance().read(f.getPath());
                         dst = FileConfigurationSerializerFactory.getInstance().read(parent + name.substring(0, name.length() - 4) + "-dst.pbd");
                     }
                     configs.add(new Configuration[]{src, dst});
                 }
             }
         } else {
             String srcP, dstP;
             String[] tokens = configsPath.split(",");
             srcP = tokens[0];
             dstP = tokens[1];
 
             Configuration src = FileConfigurationSerializerFactory.getInstance().read(srcP);
             Configuration dst = FileConfigurationSerializerFactory.getInstance().read(dstP);
             configs.add(new Configuration[]{src, dst});
         }
 
         vjobs = getVJobs(configs.get(0)[0], new File(vPath), listFiles(vPath, true));
         PrintWriter out = null;
         try {
             if (output != null) {
                 out = new PrintWriter(output);
             }
             for (Configuration[] ins : configs) {
 
                 BenchResult res = bench(ins[0], ins[1], vjobs, props);
                System.err.println(res);
                 if (out != null) {
                     out.println(res.toRaw());
                 } else {
                     System.out.println(res.toString());
                 }
             }
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
 
     public static File storeResource(JobHandler h, String rc, File root) throws Exception {
 //        String name = rc.substring(rc.lastIndexOf('/') + 1, rc.length());
         File f = new File(root + File.separator + rc);
         f.getParentFile().mkdirs();
         byte[] content = h.getResource(rc);
         FileOutputStream out = new FileOutputStream(f);
         out.write(content);
         out.close();
         return f;
     }
 
     public static void benchFromServer(String host) throws Exception {
 
         if (host == null) {
             fatal("Port and hostname are required");
         }
 
         File root = new File("/tmp/" + System.currentTimeMillis());
         int port = JobDispatcher.DEFAULT_PORT;
         String hostname = host;
 
         if (host.contains(":")) {
             port = Integer.parseInt(host.substring(host.indexOf(':') + 1, host.length()));
             hostname = host.substring(0, host.indexOf(':'));
         }
         JobHandler client = new JobHandler(hostname, port, JobHandler.DEFAULT_CACHE_SIZE);
         Job j = client.dequeue();
 
         List<File> fs = null;
         File incl = null;
         while (j != null) {
             File fSrc = storeResource(client, PlanJob.getSrcConfigPath(j), root);
             File fDst = storeResource(client, PlanJob.getDstConfigPath(j), root);
             Configuration src = FileConfigurationSerializerFactory.getInstance().read(fSrc.getPath());
             Configuration dst = FileConfigurationSerializerFactory.getInstance().read(fDst.getPath());
             Plan.logger.debug("Source conf is " + fSrc.getPath() + "; dst is " + fDst.getPath());
             File f = storeResource(client, PlanJob.getPropertiesPath(j), root);
             PropertiesHelper props = new PropertiesHelper(f.getPath());
 
             if (fs == null) {
                 List<String> paths = PlanJob.getVJobPaths(j);
                 fs = store(client, paths, root);
                 incl = new File(root.getPath() + File.separator + j.get("incl"));
                 incl.deleteOnExit();
             }
             List<VJob> vjobs = getVJobs(src, incl, fs);
             Plan.logger.debug("vjobs incl= " + incl.getPath() + " size=" + vjobs.size());
 
             BenchResult res = bench(src, dst, vjobs, props);
             res.id = Integer.toString(j.getId());
             PlanJob.setMetrics(j, res.toRaw());
             File fout = File.createTempFile("foo", "bar");
             if (res.plan != null) {
                 PlainTextTimedReconfigurationPlanSerializer.getInstance().write(res.plan, fout.getPath());
                 PlanJob.setResultingPlan(j, readContent(fout));
             }
             client.commit(j);
             j = client.dequeue();
         }
     }
 
     public static BenchResult bench(Configuration src, Configuration dst, List<VJob> vjobs, PropertiesHelper props) throws Exception {
         int timeout = props.getRequiredPropertyAsInt("controlLoop.custom.planTimeout");
         boolean useRepair = props.getRequiredPropertyAsBoolean("controlLoop.custom.repair");
         DurationEvaluator ev = FastDurationEvaluatorFactory.readFromProperties(props);
         CustomizableSplitablePlannerModule.PartitioningMode pMode;
         pMode = CustomizableSplitablePlannerModule.PartitioningMode.valueOf(props.getRequiredProperty("controlLoop.custom.partitioningMode"));
         ReconfigurationProblemBench bench = new ReconfigurationProblemBench(ev, timeout, useRepair, pMode);
         bench.doOptimize(false);
         return bench.bench(src, dst, vjobs);
     }
 
     private static List<File> store(JobHandler client, List<String> rcs, File root) throws Exception {
         List<File> files = new ArrayList<File>(rcs.size());
         for (String rc : rcs) {
             files.add(storeResource(client, rc, root));
         }
         return files;
     }
 
     public static List<VJob> getVJobs(Configuration cfg, File root, List<File> vJobPaths) throws Exception {
         BtrPlaceVJobBuilder b = makeVJobBuilder();
         PathBasedIncludes incls = new PathBasedIncludes(b, root);
         b.setIncludes(incls);
         List<VJob> vJobs = new ArrayList<VJob>();
         b.getElementBuilder().useConfiguration(cfg);
         ManagedElementSet<VirtualMachine> readed = new SimpleManagedElementSet<VirtualMachine>();
         for (File path : vJobPaths) {
             VJob v = b.build(path);
             readed.addAll(v.getVirtualMachines());
             vJobs.add(v);
         }
 
         return vJobs;
     }
 
     public static List<File> listFiles(String root, boolean recursive) {
         List<File> res = new ArrayList<File>();
         if (root != null) {
             File f = new File(root);
             if (f.exists() && f.isDirectory()) {
                 for (File c : f.listFiles()) {
                     if (c.isFile()) {
                         res.add(c);
                     } else if (recursive && c.isDirectory()) {
                         res.addAll(listFiles(c.getPath(), true));
                     }
                 }
             }
         }
         return res;
     }
 
     public static String readContent(File f) throws IOException {
         BufferedReader in = null;
         StringBuilder b = new StringBuilder();
         try {
             in = new BufferedReader(new FileReader(f));
             String line = in.readLine();
             while (line != null) {
                 b.append(line).append('\n');
                 line = in.readLine();
             }
         } finally {
             if (in != null) {
                 in.close();
             }
         }
         return b.toString();
     }
 
     private static void fatal(String msg) {
         System.err.println(msg);
         System.exit(1);
     }
 
     public static BtrPlaceVJobBuilder makeVJobBuilder() throws Exception {
         DefaultVirtualMachineTemplateFactory tplFactory = new DefaultVirtualMachineTemplateFactory();
         int[] cpu = {30, 40, 50, 60};
         int[] mem = {100, 200, 300};
         for (int c : cpu) {
             for (int m : mem) {
                 StubVirtualMachineTemplate st = new StubVirtualMachineTemplate("c" + c + "m" + m, 1, c, m, new THashSet<String>());
                 tplFactory.add(st);
             }
         }
 
 
         VJobElementBuilder eb = new DefaultVJobElementBuilder(tplFactory);
         ConstraintsCatalog cat = new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("config/btrpVjobs.properties")).build();
         return new BtrPlaceVJobBuilder(eb, cat, 5000);
     }
 
     public static void usage() {
         System.out.println("A tool to bench the plan module. Available operation modes:");
         System.out.println("microBenchHandler -s server:port");
         System.out.println("\tGet the instances to test from a server and send its result\n");
         System.out.println("microBenchHandler -props properties -cfgs  (cfg1,cfg2|cfg_path) (-vjobs path) (-o output)");
         System.out.println("\t read the initial and an optional configuration, an optional folder containing vjobs");
     }
 }
