 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.osm.cli.commands;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 import java.util.List;
 
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.osm.internal.EmptyOSMDownloadException;
 import org.geogit.osm.internal.Mapping;
 import org.geogit.osm.internal.OSMDownloadReport;
 import org.geogit.osm.internal.OSMImportOp;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 /**
  * Imports data from an OSM file
  */
 @Parameters(commandNames = "import", commandDescription = "Import OpenStreetMap data from a file")
 public class OSMImport extends AbstractCommand implements CLICommand {
 
     @Parameter(arity = 1, description = "OSM file path", required = true)
     public List<String> apiUrl = Lists.newArrayList();
 
     @Parameter(names = { "--add" }, description = "Do not remove previous data before importing")
     public boolean add = false;
 
     @Parameter(names = { "--no-raw" }, description = "Do not import raw data when using a mapping")
     public boolean noRaw = false;
 
     @Parameter(names = { "--mapping" }, description = "The file that contains the data mapping to use")
     public String mappingFile;
 
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());
         checkArgument(apiUrl != null && apiUrl.size() == 1, "One file must be specified");
         File importFile = new File(apiUrl.get(0));
         checkArgument(importFile.exists(), "The specified OSM data file does not exist");
 
         Mapping mapping = null;
         if (mappingFile != null) {
             mapping = Mapping.fromFile(mappingFile);
         }
 
         try {
             Optional<OSMDownloadReport> report = cli.getGeogit().command(OSMImportOp.class)
                     .setDataSource(importFile.getAbsolutePath()).setMapping(mapping)
                     .setNoRaw(noRaw).setAdd(add).setProgressListener(cli.getProgressListener())
                     .call();
             if (report.isPresent() && report.get().getUnpprocessedCount() > 0) {
                 cli.getConsole().println(
                         "Some elements in the by specified file could not be processed.\nProcessed entities: "
                                 + report.get().getCount() + "\nWrong or uncomplete elements: "
                                 + report.get().getUnpprocessedCount());
             }
 
         } catch (EmptyOSMDownloadException e) {
             throw new IllegalArgumentException(
                     "The specified filter did not contain any valid element.\n"
                             + "No changes were made to the repository.\n");
         } catch (RuntimeException e) {
            new IllegalStateException("Error importing OSM data: " + e.getMessage(), e);
         }
 
     }
 
 }
