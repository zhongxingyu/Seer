 package no.f12.agiledeploy.deployer;
 
 import java.io.File;
 import java.util.Date;
 
 import net.sourceforge.argparse4j.ArgumentParsers;
 import net.sourceforge.argparse4j.inf.ArgumentParser;
 import net.sourceforge.argparse4j.inf.ArgumentParserException;
 import net.sourceforge.argparse4j.inf.Namespace;
 import no.f12.agiledeploy.deployer.repo.PackageSpecification;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class CommandLineDeployer {
 
 	private static final Logger LOG = Logger.getLogger(CommandLineDeployer.class);
 
 	private DeployService deployService;
 	private File workingDirectory = new File(".");
	private String springContext;
 
 	public CommandLineDeployer(String context) {
		this.springContext = context;
 	}
 
 	public static void main(String[] args) {
 		LOG.info("Launched deploy at " + new Date());
 		CommandLineDeployer deployer = new CommandLineDeployer("classpath:spring/deployer-applicationContext.xml");
 		deployer.execute(args);
 		LOG.info("Deploy ended at " + new Date());
 	}
 
	public void prepare() {
		BeanFactory factory = new ClassPathXmlApplicationContext(this.springContext);
		this.deployService = (DeployService) factory.getBean("deployServiceImpl");
		this.workingDirectory = (File) factory.getBean("workingDirectory");
	}

 	public void execute(String[] args) {
 		ArgumentParser argparse = createArgumentParser();
 
 		Namespace ns = null;
 		File packageFile = null;
 		try {
 			ns = argparse.parseArgs(args);
 			packageFile = getFile(argparse, ns, "file");
 		} catch (ArgumentParserException e) {
			LOG.error("Invalid command line arguments: " + e.getMessage());
			System.out.println("\n\n");
			argparse.printHelp();
 			System.exit(1);
 		}
 
		this.prepare();

 		PackageSpecification ps = new PackageSpecification(ns.getString("group_id"), ns.getString("artifact_id"),
 				ns.getString("version"), ns.getString("packaging"));
 		String environment = ns.getString("environment");
 
 		LOG.info("Starting deploy: " + ps);
 		if (packageFile != null) {
 			deployService.deploy(ps, environment, workingDirectory, packageFile);
 		} else {
 			deployService.downloadAndDeploy(ps, environment, workingDirectory);
 		}
 	}
 
 	protected File getFile(ArgumentParser argparse, Namespace ns, String fileField) throws ArgumentParserException {
 		File packageFile = null;
 		if (ns.getString(fileField) != null) {
 			packageFile = new File(ns.getString(fileField));
 			if (!packageFile.exists()) {
 				throw new ArgumentParserException("The file you specified for parameter " + fileField
 						+ "does not exist.", argparse);
 			}
 		}
 		return packageFile;
 	}
 
 	protected static ArgumentParser createArgumentParser() {
		ArgumentParser argparse = ArgumentParsers.newArgumentParser("deployer").defaultHelp(true)
 				.description("Deploys lightweight Java applications");
 		argparse.addArgument("-e", "--environment").required(true).help("The environment this deploy is for");
 		argparse.addArgument("-g", "--group-id").required(true).help("The groupId of the artifact to deploy");
 		argparse.addArgument("-a", "--artifact-id").required(true).help("The artifactID to deploy");
 		argparse.addArgument("-v", "--version").required(true).help("The version to deploy");
 		argparse.addArgument("-f", "--file").required(false)
 				.help("The package to deploy. Will download from Maven repo if not specified");
 		argparse.addArgument("-p", "--packaging").setDefault("zip").help("The package format");
 		return argparse;
 	}
 
 	public File getWorkingDirectory() {
 		return this.workingDirectory;
 	}
 }
