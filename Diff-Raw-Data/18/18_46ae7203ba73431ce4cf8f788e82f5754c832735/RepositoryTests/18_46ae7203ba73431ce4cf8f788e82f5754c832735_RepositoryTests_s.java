 package org.jenkins.plugins.audit2db.test;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jenkins.plugins.audit2db.internal.model.BuildDetailsImpl;
 import org.jenkins.plugins.audit2db.internal.model.BuildNodeImpl;
 import org.jenkins.plugins.audit2db.internal.model.BuildParameterImpl;
 import org.jenkins.plugins.audit2db.model.BuildDetails;
 import org.jenkins.plugins.audit2db.model.BuildNode;
 import org.jenkins.plugins.audit2db.model.BuildParameter;
 
 public class RepositoryTests {
 
     public RepositoryTests() {
 	super();
     }
 
     protected BuildDetails createRandomBuildDetails() {
	final long salt = new Date().getTime() - (long) (Math.random() * 10000);
 	final BuildDetails build = new BuildDetailsImpl();
 	build.setDuration(Long.valueOf(60 + (long) (Math.random() * 60)));
 	build.setEndDate(new Date(build.getStartDate().getTime()
 		+ (build.getDuration() * 1000)));
	build.setFullName("BUILD FULL NAME" + salt);
	build.setId("BUILD ID" + salt);
	build.setName("BUILD NAME" + salt);
	build.setUserId("BUILD USER ID" + salt);
	build.setUserName("BUILD USER NAME" + salt);
 
 	final List<BuildParameter> params = new ArrayList<BuildParameter>();
	params.add(new BuildParameterImpl("PARAM_ID" + salt, "PARAM NAME"
		+ salt, "PARAM VALUE" + salt, build));
 	build.setParameters(params);
 
 	final BuildNode node = new BuildNodeImpl("NODE ADDRESS",
 		"NODE HOSTNAME", "NODE DISPLAYNAME", "NODE URL", "NODE NAME",
 		"NODE DESCRIPTION", "NODE LABEL");
 	build.setNode(node);
 
 	return build;
     }
 
 }
