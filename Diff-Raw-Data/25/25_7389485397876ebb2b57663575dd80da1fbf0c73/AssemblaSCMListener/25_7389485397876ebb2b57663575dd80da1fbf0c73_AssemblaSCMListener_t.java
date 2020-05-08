 package jenkins.plugin.assembla;
 
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.model.listeners.SCMListener;
 import hudson.scm.ChangeLogSet;
 
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jenkins.plugin.assembla.api.AssemblaSite;
 import jenkins.plugin.assembla.api.AssemblaTicketsAPI;
 import jenkins.plugin.assembla.api.AssemblaTicketsAPI.AssemblaTicket;
 
 public class AssemblaSCMListener extends SCMListener {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(AssemblaSCMListener.class.getName());
 
 	private transient AssemblaSite site = null;
 
 	@Override
 	public void onChangeLogParsed(AbstractBuild<?, ?> build,
 			BuildListener listener, ChangeLogSet<?> changelog) throws Exception {
 
 		site = AssemblaSite.get(build.getProject());
 
 		if(!site.isPluginEnabled()){
 			
 			return;
 		}
 		
 		if (site.isBacktrackEnabled()) {
 
 			LOGGER.info("ASSEMBLA backtrack enabled");
 			Iterator<?> changeLogIterator = changelog.iterator();
 			while (changeLogIterator.hasNext()) {
 
 				ChangeLogSet.Entry changeEntry = (ChangeLogSet.Entry) changeLogIterator
 						.next();
 				checkChangeForAssemblaTicketRef(build, changeEntry);
 			}
 		} else {
 			LOGGER.info("ASSEMBLA backtrack not enabled");
 		}
 
 		super.onChangeLogParsed(build, listener, changelog);
 	}
 
 	private void checkChangeForAssemblaTicketRef(AbstractBuild<?, ?> build,
 			ChangeLogSet.Entry change) {
 
 		LOGGER.info("Checking for ASSEMBLA ticket pattern");
 
 		String commitMessage = change.getMsg();
 
		Pattern pattern = Pattern.compile(site.getPatternInternal());
 		Matcher m = pattern.matcher(commitMessage);
 
 		AssemblaTicketsAPI ticketApi = new AssemblaTicketsAPI(site);
 
 		while (m.find()) {
 			if (m.groupCount() >= 1) {
 
 				String ticketNumber = m.group(1).substring(1);
 				LOGGER.info("ASSEMBLA ticket pattern matches");
 				LOGGER.info("Getting ASSEMBLA ticket: '" + ticketNumber + "'");
 
 				AssemblaTicket ticket = ticketApi.getTicket(site.getSpace(),
 						ticketNumber);
 
 				if (ticket == null) {
 					continue;
 				}
 
 				LOGGER.info("Posting comment to ASSEMBLA ticket: '"
 						+ ticketNumber + "'");
 				ticketApi.doCommentTicket(site.getSpace(), ticketNumber,
 						"[[url:" + Hudson.getInstance().getRootUrl() + "/"
 								+ build.getUrl() + "]] " + m.group(2));
 
 			} else {
 				LOGGER.log(Level.WARNING, "The ASSEMBLA pattern " + pattern
 						+ " doesn't define a capturing group!");
 			}
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		return obj != null && obj instanceof AssemblaSCMListener;
 	}
 }
