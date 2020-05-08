 package org.rulez.magwas.styledhtml.steps;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.emf.ecore.resource.Resource;
import org.rulez.magwas.styledhtml.EventLog;
 import org.rulez.magwas.styledhtml.RichExport;
 import org.w3c.dom.Element;
 
 import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;
 
 public class Export extends Step {
 
 	public Export(StepFactory sf) {
 		super(sf);
 	}
 
 	@Override
 	public void doit(Element arg0, File current) {
 		String style=arg0.getAttribute("style");
 		String keep=arg0.getAttribute("keep");
 		File policyfile = getFileFor(arg0,"policy",null,factory.styledir,current);
 		File tfile = getFileFor(arg0,"target",null,factory.targetdir,current);
		factory.log.issueInfo(null, null, "target="+tfile.getAbsolutePath(), EventLog.now());

 
 		if("".equals(style)||"rich".equals(style)) {
         	RichExport.export(factory.model,tfile,policyfile,factory.log);
 		} else if("archi".equals(style)){
 	        Resource resource = ArchimateResourceFactory.createResource(tfile);
 	        resource.getContents().add(factory.model);
 	        try {
 				resource.save(null);
 			} catch (IOException e) {
 				factory.log.issueError(null, null, "cannot export model to", tfile.getAbsolutePath());
 				factory.log.printStackTrace(e);
 				return;
 			}
 	        resource.getContents().remove(factory.model);
 		} else {
 			factory.log.issueError(null, null, "Unknown export style", style);
 		}
 		if("false".equals(keep)) {
 			factory.dontkeep.add(tfile);
 		}
 		doSubSteps(arg0, tfile);
 
 	}
 
 }
