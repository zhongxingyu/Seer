 package org.geogit.web.api;
 
 import java.util.Arrays;
 
 import org.geogit.api.ObjectId;
 import org.geogit.web.api.commands.BranchWebOp;
 import org.geogit.web.api.commands.Commit;
 import org.geogit.web.api.commands.Diff;
 import org.geogit.web.api.commands.FeatureDiffWeb;
 import org.geogit.web.api.commands.GetCommitGraph;
 import org.geogit.web.api.commands.Log;
 import org.geogit.web.api.commands.LsTree;
 import org.geogit.web.api.commands.RefParseWeb;
 import org.geogit.web.api.commands.RemoteWebOp;
 import org.geogit.web.api.commands.Status;
 import org.geogit.web.api.commands.TagWebOp;
 import org.geogit.web.api.commands.UpdateRefWeb;
 
 /**
  *
  */
 public class CommandBuilder {
 
     public static WebAPICommand build(String commandName, ParameterSet options)
             throws CommandSpecException {
         WebAPICommand command = null;
         if ("status".equalsIgnoreCase(commandName)) {
             command = buildStatus(options);
         } else if ("log".equalsIgnoreCase(commandName)) {
             command = buildLog(options);
         } else if ("commit".equalsIgnoreCase(commandName)) {
             command = buildCommit(options);
         } else if ("ls-tree".equalsIgnoreCase(commandName)) {
             command = buildLsTree(options);
         } else if ("updateref".equalsIgnoreCase(commandName)) {
             command = buildUpdateRef(options);
         } else if ("diff".equalsIgnoreCase(commandName)) {
             command = buildDiff(options);
         } else if ("refparse".equalsIgnoreCase(commandName)) {
             command = buildRefParse(options);
         } else if ("branch".equalsIgnoreCase(commandName)) {
             command = buildBranch(options);
         } else if ("remote".equalsIgnoreCase(commandName)) {
             command = buildRemote(options);
         } else if ("tag".equalsIgnoreCase(commandName)) {
             command = buildTag(options);
         } else if ("featurediff".equalsIgnoreCase(commandName)) {
             command = buildFeatureDiff(options);
         } else if ("getCommitGraph".equalsIgnoreCase(commandName)) {
             command = buildGetCommitGraph(options);
         } else {
             throw new CommandSpecException("'" + commandName + "' is not a geogit command");
         }
         return command;
     }
 
     static Integer parseInt(ParameterSet form, String key, Integer defaultValue) {
         String val = form.getFirstValue(key);
         Integer retval = defaultValue;
         if (val != null) {
             try {
                 retval = new Integer(val);
             } catch (NumberFormatException nfe) {
                 throw new CommandSpecException("Invalid value '" + val + "' specified for option: "
                         + key);
             }
         }
         return retval;
     }
 
     static Status buildStatus(ParameterSet options) {
         Status command = new Status();
         command.setLimit(parseInt(options, "limit", 50));
         command.setOffset(parseInt(options, "offset", 0));
         return command;
     }
 
     static Log buildLog(ParameterSet options) {
         Log command = new Log();
         command.setLimit(parseInt(options, "limit", 50));
         command.setOffset(parseInt(options, "offset", null));
         command.setPaths(Arrays.asList(options.getValuesArray("path")));
         command.setSince(options.getFirstValue("since"));
         command.setUntil(options.getFirstValue("until"));
         command.setPage(parseInt(options, "page", 0));
         command.setElementsPerPage(parseInt(options, "show", 30));
         return command;
     }
 
     static Commit buildCommit(ParameterSet options) {
         Commit commit = new Commit();
         commit.setAll(Boolean.valueOf(options.getFirstValue("all", "false")));
         commit.setMessage(options.getFirstValue("message", null));
         return commit;
     }
 
     static LsTree buildLsTree(ParameterSet options) {
         LsTree lsTree = new LsTree();
         lsTree.setIncludeTrees(Boolean.valueOf(options.getFirstValue("showTree", "false")));
         lsTree.setOnlyTrees(Boolean.valueOf(options.getFirstValue("onlyTree", "false")));
         lsTree.setRecursive(Boolean.valueOf(options.getFirstValue("recursive", "false")));
         lsTree.setVerbose(Boolean.valueOf(options.getFirstValue("verbose", "false")));
         lsTree.setRefList(Arrays.asList(options.getValuesArray("path")));
         return lsTree;
     }
 
     static UpdateRefWeb buildUpdateRef(ParameterSet options) {
         UpdateRefWeb command = new UpdateRefWeb();
         command.setName(options.getFirstValue("name", null));
         command.setDelete(Boolean.valueOf(options.getFirstValue("delete", "false")));
         command.setNewValue(options.getFirstValue("newValue", ObjectId.NULL.toString()));
         return command;
     }
 
     static Diff buildDiff(ParameterSet options) {
         Diff command = new Diff();
         command.setOldRefSpec(options.getFirstValue("oldRefSpec", null));
         command.setNewRefSpec(options.getFirstValue("newRefSpec", null));
         command.setPathFilter(options.getFirstValue("pathFilter", null));
         command.setCRS(options.getFirstValue("crs", null));
        command.setXMax(Double.valueOf(options.getFirstValue("xMax", null)));
        command.setXMin(Double.valueOf(options.getFirstValue("xMin", null)));
        command.setYMax(Double.valueOf(options.getFirstValue("yMax", null)));
        command.setYMin(Double.valueOf(options.getFirstValue("yMin", null)));
         return command;
     }
 
     static RefParseWeb buildRefParse(ParameterSet options) {
         RefParseWeb command = new RefParseWeb();
         command.setName(options.getFirstValue("name", null));
         return command;
     }
 
     static BranchWebOp buildBranch(ParameterSet options) {
         BranchWebOp command = new BranchWebOp();
         command.setList(Boolean.valueOf(options.getFirstValue("list", "false")));
         command.setRemotes(Boolean.valueOf(options.getFirstValue("remotes", "false")));
         return command;
     }
 
     static RemoteWebOp buildRemote(ParameterSet options) {
         RemoteWebOp command = new RemoteWebOp();
         command.setList(Boolean.valueOf(options.getFirstValue("list", "false")));
         return command;
     }
 
     static TagWebOp buildTag(ParameterSet options) {
         TagWebOp command = new TagWebOp();
         command.setList(Boolean.valueOf(options.getFirstValue("list", "false")));
         return command;
     }
 
     static FeatureDiffWeb buildFeatureDiff(ParameterSet options) {
         FeatureDiffWeb command = new FeatureDiffWeb();
         command.setPath(options.getFirstValue("path", null));
         command.setOldCommitId(options.getFirstValue("oldCommitId", ObjectId.NULL.toString()));
         command.setNewCommitId(options.getFirstValue("newCommitId", ObjectId.NULL.toString()));
         return command;
     }
 
     static GetCommitGraph buildGetCommitGraph(ParameterSet options) {
         GetCommitGraph command = new GetCommitGraph();
         command.setDepth(parseInt(options, "depth", 0));
         command.setCommitId(options.getFirstValue("commitId", ObjectId.NULL.toString()));
         command.setPage(parseInt(options, "page", 0));
         command.setElementsPerPage(parseInt(options, "show", 30));
         return command;
     }
 }
