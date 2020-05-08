 package controllerLayer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.Properties;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
import javax.ejb.Stateful;
 import javax.enterprise.inject.Alternative;
 
 import util.Config;
 import util.Percent;
 import businessLayer.StandardContractShareTableFiller;
 
 @Alternative
@Stateful
 public class StandardFillerFactoryBean extends FillerFactoryBean {
 	private static final long serialVersionUID = 1L;
 
 	public StandardFillerFactoryBean() {}
 
 	@Override
 	protected StandardContractShareTableFiller createFiller(String depDirectory) {
 		String depPath = Config.depRatesPath + depDirectory.toLowerCase() + "/";
 
 		String propertiesFilePath = depPath + "parameters.properties";
 		Properties p = new Properties();
 		InputStream in = null;
 
 		try {
 			in = new FileInputStream(propertiesFilePath);
 			p.load(in);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not find or open " + propertiesFilePath);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not read " + propertiesFilePath);
 		}
 
 		Percent structuresRate = Percent.normalizedValueOf(new BigDecimal(p.getProperty("structuresRate").trim()));
 		Percent atheneumCommonBalanceRate = Percent.normalizedValueOf(new BigDecimal(p.getProperty("atheneumCommonBalanceRate").trim()));
 
 		propertiesFilePath = depPath + "atheneumRateTable.properties";
 		p = new Properties();
 		in = null;
 
 		try {
 			in = new FileInputStream(propertiesFilePath);
 			p.load(in);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not find or open " + propertiesFilePath);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not read " + propertiesFilePath);
 		}
 
 		SortedMap<Percent, Percent> atheneumCapitalBalanceRateTable = new TreeMap<>();
 		for (String property : p.stringPropertyNames()) {
 			atheneumCapitalBalanceRateTable.put(Percent.normalizedValueOf(new BigDecimal(property.trim())),
 					Percent.normalizedValueOf(new BigDecimal(p.getProperty(property).trim())));
 		}
 
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return new StandardContractShareTableFiller(atheneumCapitalBalanceRateTable, structuresRate, atheneumCommonBalanceRate);
 	}
 
 }
