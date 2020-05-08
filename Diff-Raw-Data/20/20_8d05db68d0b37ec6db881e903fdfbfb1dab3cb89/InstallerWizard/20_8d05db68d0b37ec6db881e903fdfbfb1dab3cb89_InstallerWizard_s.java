 package ca.rokc.ide4edu.installer.handlers;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.operations.InstallOperation;
 import org.eclipse.equinox.p2.operations.ProvisioningSession;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 import org.osgi.framework.ServiceReference;
 
 import ca.rokc.ide4edu.installer.Activator;
 
 public class InstallerWizard extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Job job = null;
 		// get the agent
 		ServiceReference sr = Activator.context
 				.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
 		IProvisioningAgentProvider agentProvider = null;
 		if (sr == null)
 
 			return agentProvider;
 
 		agentProvider = (IProvisioningAgentProvider) Activator.context
 				.getService(sr);
		IProvisioningAgent agent = null;
 		try {
 			agent = agentProvider.createAgent(new URI(
 					"file:/Applications/eclipse36/p2"));
		} catch (ProvisionException e1) {
 			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
			e1.printStackTrace();
 		}

 		// get the repository managers and define our repositories
 		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent
 				.getService(IMetadataRepositoryManager.SERVICE_NAME);
 		IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
 				.getService(IArtifactRepositoryManager.SERVICE_NAME);
 		try {
 			manager.addRepository(new URI("file:/Users/Pascal/tmp/demo/"));
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			artifactManager.addRepository(new URI(
 					"file:/Users/Pascal/tmp/demo/"));
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		// Load and query the metadata
 		IMetadataRepository metadataRepo = null;
 		try {
 			metadataRepo = manager.loadRepository(new URI(
 					"file:/Users/Pascal/tmp/demo/"), new NullProgressMonitor());
 		} catch (ProvisionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (OperationCanceledException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Collection toInstall = metadataRepo
 				.query(QueryUtil
 						.createIUQuery("org.eclipse.equinox.p2.demo.feature.group"),
 						new NullProgressMonitor()).toUnmodifiableSet();
 
 		// Creating an operation
 		InstallOperation installOperation = new InstallOperation(
 				new ProvisioningSession(agent), toInstall);
 		if (installOperation.resolveModal(new NullProgressMonitor()).isOK()) {
 			job = installOperation
 					.getProvisioningJob(new NullProgressMonitor());
 			job.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
					boolean close = agent.close();
 				}
 			});
 
 		}
 		job.schedule();
 		return null;
 	}
 
 }
