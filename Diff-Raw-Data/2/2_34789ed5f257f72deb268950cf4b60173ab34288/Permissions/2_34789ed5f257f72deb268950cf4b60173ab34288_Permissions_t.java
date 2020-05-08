 /*
     This file is part of secure-permissions-play-module.
     
     Copyright Lunatech Research 2010
 
     secure-permissions-play-module is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     secure-permissions-play-module is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU General Lesser Public License
     along with secure-permissions-play-module.  If not, see <http://www.gnu.org/licenses/>.
 */
 package play.modules.securePermissions;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderConfiguration;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.command.Command;
 import org.drools.command.CommandFactory;
 import org.drools.event.rule.AfterActivationFiredEvent;
 import org.drools.event.rule.DefaultAgendaEventListener;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatelessKnowledgeSession;
 
 import play.Logger;
 import play.Play;
 import play.Play.Mode;
 import play.vfs.VirtualFile;
 
 /**
  * Permissions checker that uses a JBoss Rules stateless session to perform the check.
  */
 public class Permissions {
 
 	private static final String RULES_FILE_NAME = "permissions.drl";
 	private static KnowledgeBase knowledgeBase;
 	private static String rulesFileName;
 	private static Long rulesLastModified;
 	private static String rulesMD5;
 
 	private Permissions() {
 	}
 
 	static void maybeLoadKnowledgeBase(){
 		String newRulesFileName = Play.configuration.getProperty("secure.rules");
 		boolean defaultUsed = false;
 		if(newRulesFileName == null){
 			newRulesFileName = RULES_FILE_NAME;
 			defaultUsed = true;
 		}
 		VirtualFile rulesFile = Play.getVirtualFile("conf/"+newRulesFileName);
 
 		if(rulesFile == null || !rulesFile.exists()){
 			if(defaultUsed){
 				Logger.warn("No permissions file specified and none found: all permissions checks will be denied.");
 				knowledgeBase = null;
 				return;
 			}
 			// throw only if one was specified and is missing
 			throw new RuntimeException("Rules file conf/"+newRulesFileName+" does not exist");
 		}
 		if(Play.mode == Mode.PROD){
 			rulesFileName = newRulesFileName;
 			loadKnowledgeBase(rulesFile.content());
 		}else{
 			// see if the file changed
 			Long newLastModified = rulesFile.lastModified();
 			byte[] content = rulesFile.content();
 			String newMD5 = DigestUtils.md5Hex(content);
 			if(rulesFileName == null || !rulesFileName.equals(newRulesFileName)
 					|| rulesLastModified == null || !newLastModified.equals(rulesLastModified)
 					|| rulesMD5 == null || !newMD5.equals(rulesMD5)){
 				Logger.info("old file: %s, new file: %s, old modified: %s, new modified: %s, old md5: %s, new md5: %s",
 						rulesFileName, newRulesFileName, rulesLastModified, newLastModified, rulesMD5, newMD5);
 				rulesFileName = newRulesFileName;
 				rulesLastModified = newLastModified;
 				rulesMD5 = newMD5;
 				loadKnowledgeBase(content);
 			}
 		}
 	}
 
 	/**
 	 * Load and compile the rules definitions.
 	 */
 	private static void loadKnowledgeBase(byte[] rulesContent) {
 		Logger.info("Loading rules from %s", rulesFileName);
 		
 		// Configure the drools compiler to use Janino, instead of JDT, with the Play classloader, so that compilation will load 
 		// model classes from the classloader, and not as .class file resources. https://jira.jboss.org/browse/JBRULES-1229 
 		Properties properties = new Properties();
 		properties.put("drools.dialect.java.compiler", "JANINO");
 		// this is needed because it's not set in prod
 		Thread.currentThread().setContextClassLoader(Play.classloader);
 		final KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(properties, Play.classloader);
 		final KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(configuration);
 		
 		// Compile the rules file.
 		builder.add(ResourceFactory.newByteArrayResource(rulesContent), ResourceType.DRL);
 		if (builder.hasErrors()) {
 			Logger.error(builder.getErrors().toString());
 			throw new RuntimeException("Drools compilation failed: " + builder.getErrors().size() + " errors");
 		}
 		
 		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
 		knowledgeBase.addKnowledgePackages(builder.getKnowledgePackages());
 	}
 
 	/**
 	 * Check the given permission using a stateless rules session.
 	 * 
 	 * @param check The permission to check
 	 * @param user The name of the user whose roles we will check
 	 * @param roles The names of the roles that the given user has
 	 * @return true if the permission is granted
 	 */
 	public static boolean check(final PermissionCheck check, final String user, final Collection<String> roles) {
		Logger.debug("%s, user=%s, roles=%s", check, user, roles);
 
 		if(knowledgeBase == null){
 			Logger.debug("DENIED (no permissions file specified)");
 			return false;
 		}
 		
 		final StatelessKnowledgeSession session = knowledgeBase.newStatelessKnowledgeSession();
 		session.addEventListener(new AgendaLogger());
 
 		final List<Command> commands = new ArrayList<Command>();
 		commands.add(CommandFactory.newInsert(check));
 		commands.add(CommandFactory.newInsert(check.getTarget()));
 		commands.add(CommandFactory.newInsert(asPrincipal(user)));
 		commands.add(CommandFactory.newInsertElements(asRoles(roles)));
 		session.execute(CommandFactory.newBatchExecution(commands));
 
 		Logger.debug((check.isGranted() ? "GRANTED" : "DENIED") + "\n");
 		return check.isGranted();
 	}
 
 	/**
 	 * Wraps the given user name in a Principal object.
 	 */
 	private static Principal asPrincipal(final String user) {
 		final Principal principal = new Principal() {
 			public String getName() {
 				return user;
 			}
 		};
 		return principal;
 	}
 
 	/**
 	 * Wraps the given array of role names in a collection of Role objects.
 	 */
 	private static Collection<Role> asRoles(final Collection<String> names) {
 		final Collection<Role> roles = new HashSet<Role>();
 		for (String name : names) {
 			roles.add(new Role(name));
 		}
 		return roles;
 	}
 
 	private static class AgendaLogger extends DefaultAgendaEventListener {
 		@Override
 		public void afterActivationFired(AfterActivationFiredEvent event) {
 			Logger.debug("RULE '%s' %s", event.getActivation().getRule().getName(), event.getActivation().getFactHandles());
 		}
 	}
 
 }
