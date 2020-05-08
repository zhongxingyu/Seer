 package org.kalibro;
 
 import java.io.File;
 
import org.kalibro.client.ClientDaoFactory;

 public final class ImportConfiguration {
 
 	public static void main(String[] arguments) {
		if (arguments.length < 2)
			throw new IllegalArgumentException("Expected 2 arguments: file path and service address.");
		String filePath = arguments[0];
		String serviceAddress = arguments[1];
		Configuration configuration = Configuration.importFrom(new File(filePath));
		new ClientDaoFactory(serviceAddress).createConfigurationDao().save(configuration);
 	}
 
 	private ImportConfiguration() {
 		return;
 	}
 }
